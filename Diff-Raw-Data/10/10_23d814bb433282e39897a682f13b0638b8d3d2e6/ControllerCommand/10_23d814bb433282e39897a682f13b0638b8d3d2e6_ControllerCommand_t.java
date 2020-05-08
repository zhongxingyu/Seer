 package controller.cmd;
 
 import hydna.ntnu.student.api.HydnaApi;
 import aQute.bnd.annotation.component.Component;
 import aQute.bnd.annotation.component.Reference;
 
 import org.apache.felix.service.command.*;
 
 import communication.CommunicationPoint;
 import communication.api.Message;
 import communication.api.Serializer;
 import componenttypes.api.ComponentTypes;
 import controller.api.IAccessController;
 import controller.api.IdentificationCallback;
 
 @Component(properties =	{
 		/* Felix GoGo Shell Commands */
 		CommandProcessor.COMMAND_SCOPE + ":String=accessController",
 		CommandProcessor.COMMAND_FUNCTION + ":String=run",
 	},
 	provide = Object.class
 )
 
 public class ControllerCommand extends CommunicationPoint  {
 
 	private IAccessController accessControllerSvc;
 	private String accessPointId;
 	private String accessPointType;
 	private String activeAuthorizationType;
 	private boolean authorizable = false;
 	private boolean associated = false;
 	
 	private long keepalive_delay = 1000; //default value
 	
 	public ControllerCommand() {
 		
 	}
 
 	@Reference
 	public void setAccessController(IAccessController accessControllerSvc) {
 		this.accessControllerSvc = accessControllerSvc;
 		this.type = accessControllerSvc.getType().name();
 	}
 	@Reference
 	public void setHydnaSvc(HydnaApi hydnaSvc) {
 		this.hydnaSvc = hydnaSvc;
 	}
 	
 	public void run(String location, String associationKeyword) {
 		this.location = location;
 		this.associationKeyword = associationKeyword;
 		setUp();
 		while (true) {
 			try {
 				Thread.sleep(keepalive_delay);
 			} catch (InterruptedException e) {
 				System.out.println("Sleep interrupted...");
 				e.printStackTrace();
 			}
 			Message msg = new Message(Message.Type.KEEP_ALIVE, Message.MANAGER, this.id);
 			msg.addData(Message.Field.COMPONENT_TYPE, Message.ComponentType.CONTROLLER.name());
 			System.out.println("Sending KEEP_ALIVE...");
 			hydnaSvc.sendMessage(Serializer.serialize(msg));
 		}
 	}
 	
 	public void handleAuthorizationResponse(String result) {
 		if (result.equals("true")) {
 			System.out.println("Authorization success!");
 		}
 		else {
 			System.out.println("Authorization failed.");
 		}
 	}
 	
 	public void printInfo() {
 		System.out.println("AccessController: "+this.id);
 		System.out.println("Type: "+this.type);
 		System.out.println("Controlling AccessPoint: "+this.accessPointId);
 		System.out.println("AccessPoint type: "+this.accessPointType);
 	}
 	
 	public ComponentTypes.AccessController getType() {
 		return accessControllerSvc.getType();
 	}
 
 	private boolean isActive() {
 		return this.registered && this.associated && this.authorizable;
 	}
 	
 	private void requestIdentification() {
 		accessControllerSvc.requestIdentification(new IdentificationCallback() {
 			@Override
 			public void callback(Message message) {
 				message.setFrom(id);
 				message.addData(Message.Field.AUTH_TYPE, activeAuthorizationType);
 				System.out.println("Requesting authorization of type "+activeAuthorizationType);
 				hydnaSvc.sendMessage(Serializer.serialize(message));
 			}
 		});
 	}
 	
 	/* 
 	 * Logic for handling incoming messages
 	 * 
 	 * (non-Javadoc)
 	 * @see communication.CommunicationPoint#handleMessage(communication.api.Message)
 	 */
 	@Override
 	protected void handleMessage(final Message msg) {
 		if (msg.getTo().equals(this.id)) {
 			if (msg.getType().equals(Message.Type.ACCESS_RSP)) {
 				handleAuthorizationResponse(msg.getData(Message.Field.ACCESS_RES));
 			}
 			else if (msg.getType().equals(Message.Type.NEW_ID)) {
 				System.out.println("Registration confirmation from "+Message.MANAGER+"!");
 				this.id = msg.getData(Message.Field.COMPONENT_ID);
 				if (!msg.getData(Message.Field.AUTH_TYPE).equals(ComponentTypes.Authorization.NOT_AVAILABLE.name())) {
 					this.activeAuthorizationType = msg.getData(Message.Field.AUTH_TYPE);
 					this.authorizable = true;
 					System.out.println("Authorization type available: "+activeAuthorizationType);
 				}
				else {
					this.activeAuthorizationType = ComponentTypes.Authorization.NOT_AVAILABLE.name();
					this.authorizable = false;
					System.out.println("Requested authorization types not available.");
				}
 				this.keepalive_delay = Long.valueOf(msg.getData(Message.Field.TIMEOUT))*3/4;
 				this.registered = true;
 				System.out.println("New ID: "+this.id);
 			}
 			else if (msg.getType().equals(Message.Type.ASSOCIATE)) {
 				this.accessPointId = msg.getData(Message.Field.COMPONENT_ID);
 				this.accessPointType = msg.getData(Message.Field.COMPONENT_SUBTYPE);
 				associated = true;
 				printInfo();
 				if (isActive()) {
 					requestIdentification();
 				}
 			}
 			else if (msg.getType().equals(Message.Type.DISASSOCIATE)) {
 				this.accessPointId = null;
 				this.accessPointType = null;
 				associated = false;
 				accessControllerSvc.deactivate();
 				System.out.println("Disassociated, waiting for new association...");
 			}
 			else if (msg.getType().equals(Message.Type.CHANGE_AUTH)) {
				if (msg.getData(Message.Field.AUTH_TYPE).equals(ComponentTypes.Authorization.NOT_AVAILABLE.name())) {
					this.activeAuthorizationType = ComponentTypes.Authorization.NOT_AVAILABLE.name();
 					this.authorizable = false;
 					accessControllerSvc.deactivate();
 				}
 				else {
 					this.activeAuthorizationType = msg.getData(Message.Field.AUTH_TYPE);
 					if (!this.authorizable && this.associated && this.registered) {
 						requestIdentification();
 					}
 					this.authorizable = true;
 				}
 			}
 		}
 	}
 	
 	/*
 	 * Register with the manager.
 	 * 
 	 * (non-Javadoc)
 	 * @see communication.CommunicationPoint#registerCommunicationPoint()
 	 */
 	protected void registerCommunicationPoint() {
 		this.id = this.type+System.currentTimeMillis(); //temporary unique ID
 		Message msg = new Message(Message.Type.REGISTER, Message.MANAGER, this.id);
 		msg.addData(Message.Field.LOCATION, this.location);
 		msg.addData(Message.Field.ASSOCIATION_KEY, this.associationKeyword);
 		msg.addData(Message.Field.COMPONENT_TYPE, Message.ComponentType.CONTROLLER.name());
 		msg.addData(Message.Field.COMPONENT_SUBTYPE, this.type);
 		msg.addData(Message.Field.PREFERRED_AUTH_TYPE, this.accessControllerSvc.getPreferredAuthorizationType().name());
 		msg.addData(Message.Field.ALT_AUTH_TYPE, this.accessControllerSvc.getAltAuthorizationType().name());
 		hydnaSvc.sendMessage(Serializer.serialize(msg));
 	}
 }

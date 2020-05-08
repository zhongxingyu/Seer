 package controller.cmd;
 
import hydna.ntnu.student.api.HydnaApi;
 import aQute.bnd.annotation.component.Component;
 import aQute.bnd.annotation.component.Reference;
 
 import org.apache.felix.service.command.*;
 
 import communication.CommunicationPoint;
 import communication.Message;
 import communication.Serializer;
 import controller.api.IAccessController;
 
 @Component(properties =	{
 		/* Felix GoGo Shell Commands */
 		CommandProcessor.COMMAND_SCOPE + ":String=accesspoint",
 		CommandProcessor.COMMAND_FUNCTION + ":String=acInfo",
 		CommandProcessor.COMMAND_FUNCTION + ":String=setLocation",
 		CommandProcessor.COMMAND_FUNCTION + ":String=connect",
 		CommandProcessor.COMMAND_FUNCTION + ":String=getId",
 		CommandProcessor.COMMAND_FUNCTION + ":String=getType",
 		CommandProcessor.COMMAND_FUNCTION + ":String=grantAccess",
 		CommandProcessor.COMMAND_FUNCTION + ":String=revokeAccess",
 	},
 	provide = Object.class
 )
 
 public class ControllerCommand extends CommunicationPoint {
 
 	private IAccessController accessControllerSvc;
 	private String accessPoint;
 	private String authorizationServer;
 
 	@Reference
 	public void setAccessController(IAccessController accessControllerSvc) {
 		this.accessControllerSvc = accessControllerSvc;
 	}
	@Reference
	public void setHydnaSvc(HydnaApi hydnaSvc) {
		this.hydnaSvc = hydnaSvc;
	}
 	
 	public void connect() {
 		this.accessPoint = "test";
 		this.location = "testlocation";
 		this.type = "test";
 		super.setUp();
 	}
 	
 	public String requestIdentification() {
 		return accessControllerSvc.requestIdentification();
 	}
 	
 	private Message createAuthorizationRequest(String passcode){
 		return new Message(null, "", "");
 	}
 	
 	public void requestAuthorization(String passcode) {
 		
 	}
 	
 	public void handleAuthorizationResponse(String result) {
 		if (result.equals("true")) {
 			System.out.println("Passcode OK!.");
 			grantAccess();
 			long timer = System.currentTimeMillis();
 			while (timer+10000 > System.currentTimeMillis()){}
 			System.out.println("Timeout.");
 			revokeAccess();
 			requestIdentification();
 		}
 		else {
 			System.out.println("Passcode not OK, try again.");
 			requestIdentification();
 		}
 	}
 	
 	public void grantAccess() {
 		System.out.println("Granting access...");
 		Message msg = new Message(Message.Type.OPEN, this.accessPoint, this.id);
 		hydnaSvc.sendMessage(Serializer.serialize(msg));
 	}
 	
 	public void revokeAccess() {
 		System.out.println("Revoking access...");
 		Message msg = new Message(Message.Type.CLOSE, this.accessPoint, this.id);
 		hydnaSvc.sendMessage(Serializer.serialize(msg));
 	}
 	
 	public void acInfo() {
 		System.out.println("This is AccessController "+this.id+" of type "+accessControllerSvc.getType()+".");
 	}
 
 	@Override
 	protected void handleMessage(Message msg) {
 		if (msg.getTo().equals(this.id)) {
 			if (msg.getType().equals(Message.Type.ACCESSRSP)) {
 				if (msg.getData(Message.Field.ACCESSRES).equals("true")) {
 					grantAccess();
 				}
 				else {
 					revokeAccess();
 				}
 			}
 		}
 	}
 }

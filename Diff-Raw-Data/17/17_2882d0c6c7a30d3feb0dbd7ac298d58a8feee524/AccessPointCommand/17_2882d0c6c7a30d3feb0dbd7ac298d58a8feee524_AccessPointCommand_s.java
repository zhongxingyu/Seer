 package accesspoint.cmd;
 
 import aQute.bnd.annotation.component.Component;
 import aQute.bnd.annotation.component.Reference;
 import accesspoint.api.IAccessPoint;
 
 import org.apache.felix.service.command.*;
 
 import communication.CommunicationPoint;
 import communication.Message;
 
 @Component(properties =	{
 		/* Felix GoGo Shell Commands */
 		CommandProcessor.COMMAND_SCOPE + ":String=accesspoint",
 		CommandProcessor.COMMAND_FUNCTION + ":String=apinfo",
 		CommandProcessor.COMMAND_FUNCTION + ":String=setLocation",
 		CommandProcessor.COMMAND_FUNCTION + ":String=connect",
 		CommandProcessor.COMMAND_FUNCTION + ":String=grantAccess",
 		CommandProcessor.COMMAND_FUNCTION + ":String=revokeAccess",
 		CommandProcessor.COMMAND_FUNCTION + ":String=getId",
 		CommandProcessor.COMMAND_FUNCTION + ":String=getType",
 	},
 	provide = Object.class
 )
 
 public class AccessPointCommand extends CommunicationPoint {
 
 	private IAccessPoint accessPointSvc;
 	private String accessPointType;
 
 	@Reference
 	public void setAccessPoint(IAccessPoint accessPointSvc) {
 		this.accessPointSvc = accessPointSvc;
 	}
 	
 	public void connect() {
 		this.location = "testlocation";
 		this.type = "test";
 		setUp();
 		this.id = "test";
 	}
 	
 	public void grantAccess() {
 		accessPointSvc.grantAccess();
 	}
 	
 	public void revokeAccess() {
 		accessPointSvc.revokeAccess();
 	}
 	
 	public void apinfo() {
 		System.out.println("This is AccessController "+this.id+" of type "+this.accessPointType+".");
 	}
 
 	@Override
 	protected void handleMessage(Message msg) {
 		if (msg.getTo().equals(this.id)) {
 			if (msg.getType().equals(Message.Type.OPEN)) {
 				grantAccess();
 			}
 			else if (msg.getType().equals(Message.Type.CLOSE)) {
 				revokeAccess();
 			}
 		}
 	}
 }

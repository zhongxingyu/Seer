 package AP2DX.specializedMessages;
 
 import AP2DX.AP2DXMessage;
 
 public class SensorCoordinatorMessage extends CoordinatorMessage {
 	
 	private boolean activateSensor ;
 
 	public SensorCoordinatorMessage(AP2DXMessage message, Command command, boolean activateSendor) {
		super(message, command);
 		this.activateSensor = activateSensor;
 	}
 	
 	public boolean GetActivateSensor () {
 		return activateSensor;
 	}
 	
 	public boolean SetActivateSensor (boolean activateSensor) {
 		this.activateSensor = activateSensor;
 		return activateSensor;
 	}
 	
 
 }

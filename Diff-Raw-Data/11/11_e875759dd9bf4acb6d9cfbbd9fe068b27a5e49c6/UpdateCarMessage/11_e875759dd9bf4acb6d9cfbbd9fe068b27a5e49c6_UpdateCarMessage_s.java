 package at.fhv.audioracer.communication.world.message;
 
 public class UpdateCarMessage extends CameraMessage {
 	
 	public int carId;
 	public float posX;
 	public float posY;
 	public float direction;
 	
	public UpdateCarMessage(MessageId id) {
 		super(MessageId.UPDATE_CAR);
 	}
 }

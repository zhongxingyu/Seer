 package raisa.comms;
 
 import static org.junit.Assert.assertEquals;
 
 import org.junit.Test;
 
 public class ControlMessageTest {
	private ControlMessage controlMessage = new ControlMessage(5, -4, true, 42, 10, false, true, false);
 	{
 		controlMessage.setTimestamp(10l);
 	}
 
 	@Test
 	public void jsonSerialization() {
 		String value = "{\"id\":0,\"leftSpeed\":5,\"rightSpeed\":-4,\"panServoAngle\":42,\"tiltServoAngle\":10,\"lights\":true,\"takePicture\":false,\"servos\":true,\"timestamp\":10}";
 		assertEquals(value, controlMessage.toJson());
 	}
 
 	@Test
 	public void jsonDeserialization() {
 		String value = "{\"id\":1,\"leftSpeed\":5,\"rightSpeed\":-4,\"lights\":true,\"servos\":true,\"timestamp\":10,\"unsupported-field\": 'abc', \"panServoAngle\":42,\"tiltServoAngle\":10, \"takePicture\":false}";
 		assertEquals(controlMessage, ControlMessage.fromJson(value));
 	}
 
 }

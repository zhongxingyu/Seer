 package net.contentcube.robot.nxt.commands;
 
 import net.contentcube.robot.nxt.NXTCommand;
 
 public class Factory {
 	
 	public static final byte MOTOR_A = 0;
 	public static final byte MOTOR_B = 1;
 	public static final byte MOTOR_C = 2;
 	
 	public static final byte FORWARD_SPEED = 100;
 	public static final byte BACKWARD_SPEED = -100;
 	
 	public static final byte TURN_SPEED = 75;
 	
 	public static byte[][] build(NXTCommand.Command command)
 	{
 		byte[][] buffers = new byte[2][14];
 		
 		switch (command)
 		{
 			case FORWARD:
 				
 				buffers[0] = forward(MOTOR_A);
 				buffers[1] = forward(MOTOR_B);
 				
 				break;
 				
 			case BACKWARD:
 				
 				buffers[0] = backward(MOTOR_A);
 				buffers[1] = backward(MOTOR_B);
 				
 				break;
 				
 			case TURN_RIGHT:
 				
 				buffers[0] = forward(MOTOR_A);
 				buffers[0][5] = TURN_SPEED;
 				buffers[1] = backward(MOTOR_B);
 				buffers[1][5] = -TURN_SPEED;
 				
 				break;
 				
 			case TURN_LEFT:
 
 				buffers[0] = backward(MOTOR_A);
 				buffers[0][5] = -TURN_SPEED;
 				buffers[1] = forward(MOTOR_B);
 				buffers[1][5] = TURN_SPEED;
 				
 				break;
 			
 			case BRAKE:
 				
 				buffers[0] = brake(MOTOR_A);
 				buffers[1] = brake(MOTOR_B);
 				
 				break;	
 			
 			case PING:
 			default:
 				
 				buffers[0] = ping();
 								
 				break;
 		}
 		
 		return buffers;
 	}
 	
 	private static byte[] forward(byte motor)
 	{
 		byte[] buffer = new byte[14];
 		buffer[0] = (byte) (14-2);  // length lsb
 		buffer[1] = 0; // length msb
 		buffer[2] = 0; // direct command (with response)
 		buffer[3] = 0x04; // set output state
 		buffer[4] = motor; // motor (A:0, B:1, C:2)
 		buffer[5] = FORWARD_SPEED; // speed range (-100 : 100)
 		buffer[6] = 1 + 2; // mode (MOTOR_ON)
 		buffer[7] = 0;
 		buffer[8] = 0;
 		buffer[9] = 0x20; // run state (RUNNING)
 		buffer[10] = 0;
 		buffer[11] = 0;
 		buffer[12] = 0;
 		buffer[13] = 0;
 		
 		return buffer;
 	}
 	
 	private static byte[] backward(byte motor)
 	{
 		byte[] buffer = new byte[14];
 		buffer[0] = (byte) (14-2);  // length lsb
 		buffer[1] = 0; // length msb
 		buffer[2] = 0; // direct command (with response)
 		buffer[3] = 0x04; // set output state
 		buffer[4] = motor; // motor (A:0, B:1, C:2)
 		buffer[5] = BACKWARD_SPEED; // speed range (-100 : 100)
 		buffer[6] = 1 + 2; // mode (MOTOR_ON)
 		buffer[7] = 0;
 		buffer[8] = 0;
 		buffer[9] = 0x20; // run state (RUNNING)
 		buffer[10] = 0;
 		buffer[11] = 0;
 		buffer[12] = 0;
 		buffer[13] = 0;
 		
 		return buffer;
 	}
 	
 	private static byte[] brake(byte motor)
 	{
 		byte[] buffer = new byte[14];
 		buffer[0] = (byte) (14-2);  // length lsb
 		buffer[1] = 0; // length msb
 		buffer[2] = 0; // direct command (with response)
 		buffer[3] = 0x04; // set output state
 		buffer[4] = motor; // motor (A:0, B:1, C:2)
 		buffer[5] = 0; // speed range (-100 : 100)
 		buffer[6] = 2; // mode (MOTOR_ON)
 		buffer[7] = -100;
 		buffer[8] = 0;
 		buffer[9] = 0x20; // run state (RUNNING)
 		buffer[10] = 0;
 		buffer[11] = 0;
 		buffer[12] = 0;
 		buffer[13] = 0;
 		
 		return buffer;
 	}
 	
	private static byte[] ping()
 	{
		byte[] buffer = new byte[3];
 		return buffer;
 	}
 }

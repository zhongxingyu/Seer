 package tape;
 import java.io.*;
 
 /** Contains special methods for the Master Robot
  * 
  * @author Nils Breyer
  * 
  */
 public class MasterRobot extends Robot {
 
 	/**
 	 * Constructor for a MasterRobot
 	 * @param name Name of the Robot
 	 * @param mac_address MAC address of the Robot
 	 */
 	public MasterRobot(String name, String mac_address) {
 		super(name,mac_address);
 	}
 	/**
 	 * Implementation to send the MasterRobot the command to read a symbol
 	 * @return Returns the symbol that have been read on the tape
 	 * @throws IOException Thrown if the sending or receiving fails
 	 */
 	public char read() throws IOException {
 		this.sendCommand('r');
 		return this.receiveCommand();
 	}
 
 	/**
 	 * Send the MasterRobot the command to move to the next position right of the current
 	 * @throws IOException Thrown if the sending or receiving of commands failed or if the robot returned an error
 	 */
 	public void moveLeft() throws IOException {
 		System.out.println(this.name + ": Moving left...");
 		this.sendCommand('L');
 		char received = this.receiveCommand();
 		if (received == '.') {
 			System.out.println(this.name + ": Moving left finished succesfully.");
 		} else if (received == '!') {
 			System.out.println(this.name + ": Moving left failed.");
			throw new IOException("Received error from robot '" + this.name + "': Moving left failed.");
 		} else {
 			System.out.println(this.name + ": Common Fail, read from Robot: " + received);
			throw new IOException("Received unexpected symbol from robot '" + this.name + "'.");
 		}
 	}
 
 	/**
 	 * Send the MasterRobot the command to move to the next position left of the current
 	 * @throws IOException Thrown if the sending or receiving of commands failed or if the robot returned an error
 	 */
 	public void moveRight() throws IOException {
 		System.out.println(this.name + ": Moving right...");
 		this.sendCommand('R');
 		char received = this.receiveCommand();
 		if (received == '.') {
 			System.out.println(this.name + ": Success");
 		} else if (received == '!') {
 			System.out.println(this.name + ": Moving right failed.");
			throw new IOException("Received error from robot '" + this.name + "': Moving right failed.");
 		} else {
 			System.out.println(this.name + ": Common Fail, read from Robot: " + received);
			throw new IOException("Received unexpected symbol from robot '" + this.name + "'.");
 		}
 	}
 
 	/**
 	 * Test the tape
 	 * @throws IOException Thrown if the sending failed
 	 */
 	public void test() throws IOException { //TODO: remove
 		this.sendCommand('t');
 	}
 }

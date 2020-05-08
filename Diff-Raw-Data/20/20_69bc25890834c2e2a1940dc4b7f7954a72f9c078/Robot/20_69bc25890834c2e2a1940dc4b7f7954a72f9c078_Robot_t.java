import lejos.pc.comm.*;
import java.io.*;

 /** Contains common function for Master and Slave Robots
  * 
  * @author Nils Breyer, Phillipp Neumann
  * 
  */
 public abstract class Robot {
 	protected String name;
 	private String mac_address;
 	private NXTComm comm = null;
 	private DataInputStream input = null;
 	private DataOutputStream output = null;
 
 	/**
 	 * Constructor for a Robot
 	 * @param name Name of the Robot
 	 * @param mac_address MAC Adress of the Robot
 	 */
 	
 	public Robot(String name, String mac_address) {
 		this.name = name;
 		this.mac_address = mac_address;
 	}
 
 	/**
 	 * Method to connect to the Robot
 	 * @throws NXTCommException 
 	 * @throws IOException
 	 */
 	
 	public void connect() throws NXTCommException, IOException {
 		System.out.println("Connecting to '" + this.name + "'...");
 
 		try {
 			this.comm = NXTCommFactory.createNXTComm(NXTCommFactory.BLUETOOTH);
 		}
 		catch (NXTCommException e) {
 			System.out.println("Connecting to '" + this.name + "' failed while preparing bluetooth: " + e.getMessage());
 			throw e;
 		}
 
 		NXTInfo info = new NXTInfo(NXTCommFactory.BLUETOOTH, this.name, this.mac_address);
 
 		this.comm.open(info);
 
 		InputStream is = comm.getInputStream();
 		OutputStream os = comm.getOutputStream();
 
 		this.input = new DataInputStream(is);
 		this.output = new DataOutputStream(os);
 
 		System.out.println("Connected to '" + this.name + "'.");
 	}
 	
 	/**
 	 * Method to disconnect from the Robot
 	 */
 
 	public void disconnect() {
 		System.out.println("Disconnecting '" + this.name + "'...");
 		this.sendCommand('q');
 		try {
 			this.output.close();
 			this.comm.close();
 		}
 		catch (IOException e) {
 			System.out.println("WARNING: Disconnecting '" + this.name + "' didn't work properly: " + e.getMessage());
 		}
 
 		System.out.println("Disconnected '" + this.name + "'.");
 	}
 	
 	/**
 	 * Method to send a character to the robot
 	 * @param current symbol that is currently on the position to write
 	 * @param write symbol that is send to the robot
 	 */
 	
 	public void write(char current, char write) {
 		System.out.println(this.name + ": Write from "+ current + " to " + write + " ...");
 		this.sendCommand('w');
 		this.sendCommand(current);
 		this.sendCommand(write);
 		char received = this.receiveCommand();		
 		if (received == '.') {
 			System.out.println(this.name + ": Success");
 		} else if (received == '!') {
 			System.out.println(this.name + ": Write Failed");
 		} else {
 			System.out.println(this.name + ": Common Fail, read from Robot: " + received);
 		}
 	}
 	
 	/**
 	 * Send a command to the robot
 	 * @param cmd character that is send to the robot
 	 */
 	
 	protected void sendCommand(char cmd) {
 		System.out.println("Sending to '" + this.name + "' the command '" + cmd + "'...");
 		try {
 			this.output.writeChar(cmd);
 			this.output.flush();
 			System.out.println("Sending to '" + this.name + "' the command '" + cmd + "' finished.");
 		}
 		catch (IOException e) {
 			System.out.println("Sending to '" + this.name + "' the command '" + cmd + "' failed: " + e.getMessage());
 		}
 	}
 
 	/**
 	 * 
 	 * @return a character that have been send from the robot
 	 */
 	protected char receiveCommand() {
 		System.out.println("Receiving from '" + this.name + "'...");
 		try {
 			char cmd = this.input.readChar();
 			System.out.println("Received from '" + this.name + "' the command '" + cmd + "'.");
 			return cmd;
 		}
 		catch (IOException e) {
 			System.out.println("Receiving from '" + this.name + "' failed: " + e.getMessage());
 			return ' '; //TODO: better solution?
 		}
 	}
 }

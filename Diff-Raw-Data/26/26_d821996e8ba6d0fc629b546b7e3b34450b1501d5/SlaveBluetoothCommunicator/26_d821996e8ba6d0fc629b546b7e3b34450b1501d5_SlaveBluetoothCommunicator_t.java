 /**
  * 
  */
 package slave;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 
 import lejos.nxt.LCD;
 import lejos.nxt.Sound;
 import lejos.nxt.comm.BTConnection;
 import lejos.nxt.comm.Bluetooth;
import lejos.util.Delay;
 
 import common.IDefender;
 import common.Instructions;
 import common.ParseInstructions;
 import common.Protocol;
 
 /**
  * This class takes care of receiving requests from the other NXT brick.
  * 
  * @author alex
  * 
  */
 public class SlaveBluetoothCommunicator {
 
 	private ILauncher launcher;
 	private IDefender defender;
 
 	private BTConnection conn;
 	private DataOutputStream out;
 	private DataInputStream in;
 
 	public SlaveBluetoothCommunicator(ILauncher launcher, IDefender defender) {
 		this.launcher = launcher;
 		this.defender = defender;
 		this.conn = null;
 	}
 
 	/**
 	 * This method will wait for connection with the master brick and will
 	 * handle incoming requests in a continuous loop.
 	 */
 	public void handleRequests() {
 		LCD.clear(0);
 		LCD.drawString("Unconnected", 0, 0);
 		Bluetooth.setFriendlyName(Protocol.SLAVE_NAME);
 		// Connect with master
 		conn = Bluetooth.waitForConnection();
 		LCD.clear(0);
 		LCD.drawString("Connected", 0, 0);
 		out = conn.openDataOutputStream();
 		in = conn.openDataInputStream();
 
 		// Handle requests
 		while (true) {
 			try {
 				DataInputStream input = conn.openDataInputStream();
 				int request = input.readInt();
 				switch (request) {
 				case Protocol.LAUNCH_POSITION_REQUEST:
 					handleLaunchPositionRequest();
 					break;
 				case Protocol.LAUNCH_REQUEST:
 					handleLaunchRequest();
 					break;
 				case Protocol.UPDATE_INSTRUCTIONS_REQUEST:
 					handleUpdateInstructionsRequest();
 					break;
 				}
 			} catch (IOException e) {
 				Sound.buzz();
 				LCD.clear(6);
 				LCD.drawString("IOException: " + e.getMessage(), 0, 6);
 			}
 		}
 	}
 
 	private void handleUpdateInstructionsRequest() {
 		LCD.clear(0);
 		LCD.drawString("Received instructions", 0, 0);
 		Instructions inst = ParseInstructions.parse(in);
 		launcher.updateInstructions(inst);
 		//LCD.drawString("w1: " + inst.w1, 0, 1);
 	}
 
 	private void handleLaunchRequest() {
 		LCD.clear(0);
 		LCD.drawString("Received launch request", 0, 0);
		this.launcher.retract();
 		this.launcher.launch();
 		LCD.clear(0);
 		LCD.drawString("Launched", 0, 0);
		
 	}
 
 	private void handleLaunchPositionRequest() throws IOException {
 		LCD.clear(0);
 		LCD.drawString("Received launch position request", 0, 0);
 		this.launcher.calculateLaunchPosition();
 		out.writeDouble(this.launcher.getLaunchX());
 		out.writeDouble(this.launcher.getLaunchY());
 		out.writeDouble(this.launcher.getLaunchTheta());
 		out.flush();
 	}
 
 }

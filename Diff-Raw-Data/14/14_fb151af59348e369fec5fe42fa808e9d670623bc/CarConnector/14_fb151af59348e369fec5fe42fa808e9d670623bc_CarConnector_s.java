 import java.io.*;
 
 import lejos.nxt.*;
 import lejos.robotics.navigation.*;
 import lejos.nxt.comm.*;
 
 import javax.bluetooth.*;
 
 public class CarConnector {
 	public static final byte COMMAND_FORWARD	= 1 << 0;
 	public static final byte COMMAND_BACKWARD	= 1 << 1;
 	public static final byte COMMAND_STOP		= 1 << 2;
 	public static final byte COMMAND_STEER		= 1 << 3;
 	
 	public static final byte MOVING_FORWARD		= COMMAND_FORWARD;
 	public static final byte MOVING_BACKWARD	= COMMAND_BACKWARD;
 	public static final byte MOVING_STOPPED		= COMMAND_STOP;
 	
 	private Boolean _connected 				= false;
 	private BTConnection _connection 		= null;
 	private RemoteDevice _remoteDevice 		= null;
 	private DataInputStream _dataInStream 	= null;
 	private DifferentialPilot _pilot 		= null;
 	
	private static byte moving = MOVING_STOPPED;
 	
 	public CarConnector(DifferentialPilot pilot) {
 		_pilot = pilot;
 	}
 	
 	public Boolean isConnected() {
 		return _connected;
 	}
 	
 	public BTConnection waitForConnection() {
 		if (this.isConnected()) {
 			System.out.println("We are already connected.");
 			return null;
 		}
 		
 		LCD.drawString("Waiting for connection...", 0, 0);
 		 
 		BTConnection connection = Bluetooth.waitForConnection();
 		
 		if (!this.useConnection(connection)) {
 			return null;
 		}
 		
 		return connection;
 	}
 	
 	public Boolean useConnection(BTConnection connection) {
 		if (this.isConnected()) {
 			System.out.println("We are already connected.");
 			return false;
 		}
 		
 		_remoteDevice = null;
 		_dataInStream = null;
 		try {
 			_remoteDevice = RemoteDevice.getRemoteDevice(connection);
 		    _dataInStream = connection.openDataInputStream();
 		} catch (IOException e) {
 			System.out.println("Failed to get device: " + e);
 			return false;
 		}
 		
 		_connected = true;
 
 		String friendlyDeviceName = _remoteDevice.getFriendlyName(false);
 		
 		LCD.clear();
 		LCD.drawString("Connected to " + friendlyDeviceName, 0, 0);
 		
 		return true;
 	}
 	
 	public void closeConnection() {
 		if (!this.isConnected()) {
 			System.out.println("We are not actually connected.");
 			return;
 		}
 
 		try {
 			_dataInStream.close();
 			_remoteDevice = null;
 			_connection.close();
 		} catch (IOException e) {
 			System.out.println("Failed to close connection: " + e);
 			return;
 		}
 		
 		_connected = false;
 	}
 	
 	public void waitForAndHandleCommand() {
 		if (!this.isConnected()) {
 			System.out.println("We are not actually connected.");
 			return;
 		}
 		
 		byte command = 0;
 		try {
 			command = _dataInStream.readByte();
 		} catch (IOException e) {
 			System.out.println("Failed to read command: " + e);
 			return;
 		}
 		
 		handleCommand(command);
 	}
 	
 	public void handleCommand(byte command) {
 		switch (command) {
 		case COMMAND_FORWARD:
 		case COMMAND_BACKWARD: {
 			int speed = -1;
 			try {
 				speed = _dataInStream.readInt();
 			} catch (IOException e) {
 				System.out.println("Failed to read speed: " + e);
 				return;
 			}
 			if (speed > -1) {
 				_pilot.setTravelSpeed(speed);
 			}
 			
			if (moving != command) {
 				if (command == COMMAND_FORWARD) {
 					_pilot.forward();
 				}
 				else {
 					_pilot.backward();
 				}
 			}
			moving = command;
 			
 			break;
 		}
 		
 		case COMMAND_STOP: {
 			_pilot.stop();
			moving = command;
 			
 			break;
 		}
 		
 		case COMMAND_STEER: {
 			int angle = -1;
 			try {
 				angle = _dataInStream.readInt();
 			} catch (IOException e) {
 				System.out.println("Failed to read angle: " + e);
 				return;
 			}
 			int turnRate = 50;
 			if (angle > 0) {
 				turnRate = -turnRate;
 			}
 			angle = Math.abs(angle);
 			// turnRate: ratio of inside to outside motor: ratio = 1 - turnRate / 100
 			// positive turnRate => left  motor drives the inside wheel => car turns left
 			// negative turnRate => right                                            right
 			// turnRate == 0   => ratio = 1 - 0/100   = 1.0  => car travels in straight line
 			// turnRate == 100 => ratio = 1 - 100/100 = 0.0  => inside motor stops
 			// turnRate == 200 => ratio = 1 - 200/100 = -1.0 => car turns in place
 			_pilot.steer(turnRate, angle, false);
 			
 			break;
 		}
 		
 		default: {
 			System.out.println("Command not recognized: " + command);
 		}
	}
 	}
 }

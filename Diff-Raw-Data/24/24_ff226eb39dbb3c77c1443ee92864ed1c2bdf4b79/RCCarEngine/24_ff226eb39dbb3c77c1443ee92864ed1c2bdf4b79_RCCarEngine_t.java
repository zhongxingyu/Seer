 import java.io.*;
 
 import lejos.robotics.navigation.*;
 import lejos.nxt.comm.*;
 
 import javax.bluetooth.*;
 
 public class RCCarEngine {
 	public static final byte COMMAND_FORWARD	= 1 << 0;
 	public static final byte COMMAND_BACKWARD	= 1 << 1;
 	public static final byte COMMAND_STOP		= 1 << 2;
 	public static final byte COMMAND_STEER		= 1 << 3;
 	
 	public static final byte MOVING_FORWARD		= COMMAND_FORWARD;
 	public static final byte MOVING_BACKWARD	= COMMAND_BACKWARD;
 	public static final byte MOVING_STOPPED		= COMMAND_STOP;
 	
 	private boolean _connected 				= false;
 	private BTConnection _connection 		= null;
 	private RemoteDevice _remoteDevice 		= null;
 	private DataInputStream _dataInStream 	= null;
 	private DifferentialPilot _pilot 		= null;
 	
 	private byte _moving = MOVING_STOPPED;
 	
 	public RCCarEngine(DifferentialPilot pilot) {
 		_pilot = pilot;
 	}
 	
 	public Boolean isConnected() {
 		return _connected;
 	}
 	
 	public RemoteDevice getRemoteDevice() {
 		return _remoteDevice;
 	}
 	
 	public void log(String message) {
 		System.out.println("CC: " + message);
 	}
 	
 	public BTConnection waitForConnection() {
 		if (this.isConnected()) {
 			this.log("We are already connected.");
 			return null;
 		}
 		
 		this.log("Waiting for connection...");
 		 
 		BTConnection connection = Bluetooth.waitForConnection();
 		
 		if (connection == null) {
 			return null;
 		}
 		
 		if (!this.useConnection(connection)) {
 			return null;
 		}
 		
 		return connection;
 	}
 	
 	public boolean useConnection(BTConnection connection) {
 		if (this.isConnected()) {
 			this.log("We are already connected.");
 			return false;
 		}
 		
 		_connection = connection;
 		
 		_remoteDevice = null;
 		_dataInStream = null;
 		try {
 			_remoteDevice = RemoteDevice.getRemoteDevice(_connection);
 		    _dataInStream = _connection.openDataInputStream();
 		} catch (IOException e) {
 			this.log("Failed to get device, stream: " + e);
 			_connection = null;
 			return false;
 		}
 		
 		_connected = true;
 		
 		return true;
 	}
 	
 	public void closeConnection() {
 		if (!this.isConnected()) {
 			this.log("We are not actually connected.");
 			return;
 		}
 
 		try {
 			_dataInStream.close();
 			_remoteDevice = null;
 			_connection.close();
 			_connection = null;
 		} catch (IOException e) {
 			this.log("Failed to close connection: " + e);
 			return;
 		}
 		
 		_connected = false;
 	}
 	
 	public boolean waitForAndHandleCommand() {
 		if (!this.isConnected()) {
 			this.log("We are not actually connected.");
 			return false;
 		}
 		
 		this.log("Waiting for command...");
 		
 		byte command = 0;
 		try {
 			command = _dataInStream.readByte();
 		} catch (EOFException e) {
 			this.log("EOF reached.");
 			return false;
 		} catch (IOException e) {
 			this.log("Failed to read command: " + e);
 			return false;
 		}
 		
 		this.log("Received command " + command);
 		
 		return handleCommand(command);
 	}
 	
 	public boolean handleCommand(byte command) {
 		switch (command) {
 			case COMMAND_FORWARD:
 			case COMMAND_BACKWARD: {
 				this.log("Waiting for speed...");
 				
 				int speed = -1;
 				try {
 					speed = _dataInStream.readInt();
 				} catch (IOException e) {
 					this.log("Failed to read speed: " + e);
 					return false;
 				}
 				
 				this.log("Received speed " + speed);
 				
 				if (speed > -1) {
 					_pilot.setTravelSpeed(speed);
 				}
 				
 				if (_moving != command) {
 					if (command == COMMAND_FORWARD) {
 						_pilot.forward();
 					}
 					else {
 						_pilot.backward();
 					}
 				}
 				_moving = command;
 				
 				break;
 			}
 			
 			case COMMAND_STOP: {
 				_pilot.stop();
 				_moving = command;
 				
 				break;
 			}
 			
 			case COMMAND_STEER: {
 				this.log("Waiting for angle...");
 				
				int angle = 0;
 				try {
 					angle = _dataInStream.readInt();
 				} catch (IOException e) {
 					this.log("Failed to read angle: " + e);
 					return false;
 				}
 				
 				this.log("Received angle " + angle);
 				
 				int turnRate = 50;
				if (angle > 0) { // Positive angle: We want to go right!
					// But NXJ doesn't agree, see below. 
 					turnRate = -turnRate;
					angle = -angle;
 				}
				
 				// turnRate: ratio of inside to outside motor: ratio = 1 - turnRate / 100
				// positive turnRate => left  motor drives the inside wheel => car turns left, forward  or right, backward
				// negative turnRate => right                                            right, forward or left, backward
 				// turnRate == 0   => ratio = 1 - 0/100   = 1.0  => car travels in straight line
 				// turnRate == 100 => ratio = 1 - 100/100 = 0.0  => inside motor stops
 				// turnRate == 200 => ratio = 1 - 200/100 = -1.0 => car turns in place
				
				// positive angle => move in direction that increases heading => car turns left
				// negative angle =>                        decreases         =>           right
				
				// === SO ===
				// left: positive turnRate, positive angle
				// right: negative turnRate, negative angle
 				_pilot.steer(turnRate, angle, false);
 				
 				break;
 			}
 			
 			default: {
 				this.log("Command not recognized: " + command);
 				return false;
 			}
 		}
 		
 		this.log("Handled command " + command);
 		return true;
 	}
 }

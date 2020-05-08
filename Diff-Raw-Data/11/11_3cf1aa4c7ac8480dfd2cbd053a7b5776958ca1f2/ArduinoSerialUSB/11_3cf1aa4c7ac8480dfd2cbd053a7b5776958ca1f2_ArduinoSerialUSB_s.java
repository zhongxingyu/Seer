 package Controller;
 
 import gnu.io.*;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.Enumeration;
 import java.util.TooManyListenersException;
 
 public class ArduinoSerialUSB implements SerialPortEventListener {
 
 
 	SerialPort serialPort;
 
	String hostname = Settings.config.getProperty("emailServer");
	String username = Settings.config.getProperty("emailUser");
	String password = Settings.config.getProperty("emailPassword");
	/**
	 * The port we're normally going to use.
	 */
 	static String PORT_NAMES[] = {
		                               "FROM SETTINGS", // Mac OS X
 		                               "/dev/tty.usbmodemfd131", // Mac OS X2
 		                               "/dev/tty.usbmodemfd141", // Mac OS X3

 		                               "/dev/ttyUSB0", // Linux
 		                               "COM4", // Windows
 	};
 
 
 	/**
 	 * Buffered input stream from the port
 	 */
 	InputStream input;
 	/**
 	 * The output stream to the port
 	 */
 	OutputStream output;
 	/**
 	 * Milliseconds to block while waiting for port open
 	 */
 	static final int TIME_OUT = 2000;
 	/**
 	 * Default bits per second for COM port.
 	 */
 	static final int DATA_RATE = 9600;
 
 	public void initialize() {
 		CommPortIdentifier portId = null;
 		LoadSettings.load();
 
 		PORT_NAMES[0] = Settings.config.getProperty("usbSerial");
 
 		Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();
 
 		portId = setupPort(portId, portEnum);
 
 		if (portId == null) {
 			System.out.println("Could not find COM port.");
 			return;
 		}
 
 		try {
 			setupSerial(portId);
 
 		} catch (Exception e) {
 			System.err.println(e.toString());
 		}
 	}
 
 	CommPortIdentifier setupPort(CommPortIdentifier portId, Enumeration portEnum) {
 		// iterate through, looking for the port
 		while (portEnum.hasMoreElements()) {
 			CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
 			for (String portName : PORT_NAMES) {
 				if (currPortId.getName().equals(portName)) {
 					portId = currPortId;
 					break;
 				}
 			}
 		}
 		return portId;
 	}
 
 	void setupSerial(CommPortIdentifier portId) throws PortInUseException, UnsupportedCommOperationException, IOException, TooManyListenersException {
 		// open serial port, and use class name for the appName.
 		serialPort = (SerialPort) portId.open(this.getClass().getName(),
 		                                      TIME_OUT);
 
 		// set port parameters
 		serialPort.setSerialPortParams(DATA_RATE,
 		                               SerialPort.DATABITS_8,
 		                               SerialPort.STOPBITS_1,
 		                               SerialPort.PARITY_NONE);
 
 		// open the streams
 		input = serialPort.getInputStream();
 		output = serialPort.getOutputStream();
 
 		// add event listeners
 		serialPort.addEventListener(this);
 		serialPort.notifyOnDataAvailable(true);
 	}
 
 	public synchronized void serialEvent(SerialPortEvent oEvent) {
 		if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
 			try {
 				if(input.available() > 0){
 
 					byte chunk[] = new byte[1];
 					input.read(chunk, 0, 1);
 
 					int valueFromArduino = chunk[0]& 0xff;
 					System.out.println("Chunk: "+valueFromArduino);
 
 					WebSocketServer.notifyAllClients(valueFromArduino);
 
 				}
 
 
 
 			} catch (Exception e) {
 				System.err.println(e.toString());
 			}
 		}
 		// Ignore all the other eventTypes, but you should consider the other ones.
 	}
 
 }

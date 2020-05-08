 package com.dropoutdesign.ddf.module;
 
 import javax.comm.*;
 import java.io.*;
 
 public class SerialConnection extends ModuleConnection {
 	
 	public static final String PROTOCOL = "serial";
 	
 	public static final int SERIAL_BAUD = 57600;
 
 	private SerialPort serialPort;
 	
 	protected SerialConnection(String port) throws ModuleIOException {
 		this(port, 2000);
 	}
 	
 	protected SerialConnection(String port, long timeout) throws ModuleIOException {
 		super(PROTOCOL+":"+port);
 
 		CommPortIdentifier portID;
 		OutputStream outputStream;
 		InputStream inputStream;
 	
 		try {
 			portID = CommPortIdentifier.getPortIdentifier(port);
			serialPort = (SerialPort)portID.open("Disco Dance Floor", (int)timeout);
 			serialPort.setSerialPortParams(SERIAL_BAUD,
 						   SerialPort.DATABITS_8,
 						   SerialPort.STOPBITS_1,
 						   SerialPort.PARITY_NONE);
 			//serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
 		
 			outputStream = serialPort.getOutputStream();
 			inputStream = serialPort.getInputStream();		
 
 		} catch (NoSuchPortException e) {
 			throw new ModuleIOException("No serial port called \""+port+"\".", e);
 
 		} catch (PortInUseException e) {
 			throw new ModuleIOException("Serial port \""+port+"\" is in use.", e);
 
 		} catch (UnsupportedCommOperationException e) {
 			throw new ModuleIOException("Could not initialize serial port \""+port+"\".", e);
 
 		} catch (IOException e) {
 			throw new ModuleIOException("I/O error on serial port \""+port+"\".", e);
 		}
 
 		init(outputStream, inputStream);
 	}
 	
 	public void close() {
 		super.close();
 		serialPort.close();
 	}
 }

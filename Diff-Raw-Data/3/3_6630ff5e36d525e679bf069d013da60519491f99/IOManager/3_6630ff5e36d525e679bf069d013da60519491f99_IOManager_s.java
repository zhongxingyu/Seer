 package org.crockeo.bongomerouter.io;
 
 import org.crockeo.bongomerouter.gui.GuiManager;
 import org.crockeo.bongomerouter.io.serial.*;
 //import org.crockeo.bongomerouter.io.osc.*;
 
 import gnu.io.PortInUseException;
 import gnu.io.NoSuchPortException;
 import gnu.io.CommPortIdentifier;
 import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;
 
 /*
  * Class: IOManager
  * 
  * Purpose:
  *  Managing all input and output
  */
 
 public class IOManager {
 	// Serial IO
 	private SerialPort serialPort;
 	private SerialOut serialOut;
 	private SerialIn serialIn;
 	
 	private IOManager() {
 		recreateSerial();
 		recreateOsc();
 	}
 	
 	// (Re)creating the serial ports
 	public void recreateSerial() {
 		try {
 			serialPort = (SerialPort)CommPortIdentifier.getPortIdentifier(GuiManager.getCommPortName())
 													   .open("Bongome Router", 2000);
 		} catch (NoSuchPortException | PortInUseException e) { e.printStackTrace(); }
 		
 		try {
 			serialPort.setSerialPortParams(GuiManager.getBaudrate(),
 										   GuiManager.getDataBits(),
 										   GuiManager.getStopBits(),
 										   GuiManager.getParity());
 		} catch (UnsupportedCommOperationException e) { e.printStackTrace(); }
 		
 		serialOut = new SerialOut(serialPort);
 		serialIn = new SerialIn(serialPort);
 	}
 	
 	// (Re)creating the osc ports
 	public void recreateOsc() {
 		
 	}
 	
 	
 	// Accessors
 	public SerialOut getSerialOut() { return serialOut; }
 	public SerialIn getSerialIn() { return serialIn; }
 	
 //	public OscOut getOscOut() { return oscOut; }
 //	public OscIn getOscIn() { return oscIn; }
 	
 	// Singleton
 	public final IOManager clone() { return null; }
 	
 	private static IOManager instance;
 	public static IOManager instance() {
 		if (instance == null)
 			instance = new IOManager();
 		return instance;
 	}
 }

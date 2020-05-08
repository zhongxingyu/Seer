 package firmware_interfaces;
 
 import gnu.io.CommPortIdentifier;
 import gnu.io.SerialPort;
 import gnu.io.SerialPortEvent;
 import gnu.io.SerialPortEventListener;
 
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.Enumeration;
 
import uORCInterface.OrcController;

 public class DeltaInterface implements SerialPortEventListener{
 	SerialPort serialPort;
 	/** The port we're normally going to use. */
 	private static final String PORT_NAMES[] = {
 		"/dev/tty.usbserial-A9007UX1", // Mac OS X
 		"/dev/ttyUSB0", // Linux
 		"COM8", // Windows
 	};
 	/** Buffered input stream from the port */
 	private InputStream input;
     /** The output stream to the port */
     private OutputStream output;
     /** Milliseconds to block while waiting for port open */
     private static final int TIME_OUT = 2000;
     /** Default bits per second for COM port. */
     private static final int DATA_RATE = 9600;
 
     private String inputBuffer="";
     
     public boolean ready;
 
     public DeltaInterface() {
 
 		CommPortIdentifier portId = null;
 		Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();
 
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
 
     	if (portId == null) {
     		System.out.println("Could not find COM port.");
         	return;
     	} else {
         	System.out.println("Found your Port");
     	}
 
     	try {
         	// open serial port, and use class name for the appName.
         	serialPort = (SerialPort) portId.open(this.getClass().getName(), TIME_OUT);
 
         	// set port parameters
         	serialPort.setSerialPortParams(DATA_RATE, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
                 	SerialPort.PARITY_NONE);
 
         	// open the streams
         	input = serialPort.getInputStream();
         	output = serialPort.getOutputStream();
 
 	        // add event listeners
     	    serialPort.addEventListener(this);
      	   	serialPort.notifyOnDataAvailable(true);
 
         } catch (Exception e) {
             System.err.println(e.toString());
         }
     	
     }
 
     /**
      * This should be called when you stop using the port. This will prevent
      * port locking on platforms like Linux.
      */
     public synchronized void close() {
         if (serialPort != null) {
             serialPort.removeEventListener();
             serialPort.close();
         }
     }
 
     /**
      * This Method can be called to print a String to the serial connection
      */
     public synchronized void sendString(String msg) {
         try {
             //msg += '\n';// add a newline character
             output.write(msg.getBytes());// write it to the serial
             output.flush();// refresh the serial
             System.out.print("<- " + msg);// output for debugging
             System.out.println();
         } catch (Exception e) {
             System.err.println(e.toString());
         }
     }
 
 	/**
     * This Method is called when Serialdata is recieved
     */
     public synchronized void serialEvent (SerialPortEvent oEvent) {
     	if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
         	try {
         		ready = true;
             	char in = (char)input.read();
                 System.out.println("received: ");
                 System.out.println(in);
          	} catch (Exception e) {
             	System.err.println(e.toString());
             }
       	}
   	}
         
     public void move(int[] steps){
     	ready = false;
     	String command = "";
     	command += "F";
     	for(int i = 0; i < steps.length; i++){
         	command += Integer.toString(steps[i]);
         	command += "S";
         }
     	command += "E";
         this.sendString(command);
  	}
   
  
    	public static void main(String[] args) throws Exception {
   		DeltaInterface main = new DeltaInterface();
         System.out.println("Started");
         int[] steps = {5000,5000,5000};
         int[] isteps = {-1000,-1000,-1000};
         Thread.sleep(7000);
         
         main.move(isteps);
		
         //main.move(isteps);
 
  	}
 
 }

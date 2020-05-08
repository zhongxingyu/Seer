 package no.ntnu.osnap.com.deprecated;
 
 
 import gnu.io.CommPortIdentifier;
 import gnu.io.SerialPort;
 import gnu.io.SerialPortEvent;
 import gnu.io.SerialPortEventListener;
 import java.io.*;
 import java.util.Enumeration;
 import java.util.logging.Level;
 import java.util.logging.Logger;
import no.ntnu.osnap.com.ComLayerListener;
 import no.ntnu.osnap.com.Protocol;
 
 public class ComLayer extends Protocol implements SerialPortEventListener {
 
     SerialPort serialPort;
     /**
      * Buffered input stream from the port
      */
     private InputStream input;
     /**
      * The output stream to the port
      */
     private OutputStream output;
     /**
      * Milliseconds to block while waiting for port open
      */
     private static final int TIME_OUT = 2000;
     /**
      * Default bits per second for COM port.
      */
     private static final int DATA_RATE = 9600;
     
     /*
      *  0 = scanning
      *  1 = active
      */
     private enum ConnectionState {
         SCANNING,
         ACTIVE
     }
     
     private ConnectionState state = ConnectionState.SCANNING;
     
     private final byte[] ack = {(byte)0xFF, (byte)0x04, (byte)0x00, (byte)0xFF, (byte)0x00};
     //public final byte[] text = {(byte)0x04, (byte)0x01, (byte)0xFF, "H".getBytes()[0]};
     
 
     public ComLayer() {
         while (!findArduino());
     }
     
     private boolean findArduino(){
         
         CommPortIdentifier portId = null;
         Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();
 
         // iterate through, looking for the port
         while (portEnum.hasMoreElements()) {
             portId = (CommPortIdentifier) portEnum.nextElement();
             
             if (portId.getPortType() != CommPortIdentifier.PORT_SERIAL)
                 continue;
             // Test com port
             
             if (portId == null) {
                 System.out.println("Could not find COM port.");
                 continue;
             }
 
             try {
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
             } catch (Exception e) {
                 continue;
             }
             
             System.out.print("COM port(" + portId.getName() + ") found, pinging for Arduino...");
             try {
                 Thread.sleep(1500);
             } catch (InterruptedException ex) {
             }
             try {
                 // Check for arduino
                 sendBytes(ack);
             } catch (IOException ex) {
                 System.out.println("DERP");
                 continue;
             }
             
             
             try {
                 Thread.sleep(500);
             } catch (InterruptedException ex) {
             }
             
             if (state == ConnectionState.SCANNING){
                 close();
                 System.out.println(" No response.");
                 close();
                 continue;
             }
             
             break;
         }
         
         if (state == ConnectionState.SCANNING){
             System.out.println("ERROR: No arduinos found");
             try {
                 Thread.sleep(500);
             } catch (InterruptedException ex) {
                 Logger.getLogger(ComLayer.class.getName()).log(Level.SEVERE, null, ex);
             }
             return false;
         }
         System.out.println(" Arduino found");
         System.out.println("Com port found: " + portId.getName());
         return true;
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
 
     private byte lastByte = (byte)0xFE;
     /**
      * Handle an event on the serial port. Read the data and print it.
      */
     @Override
     public synchronized void serialEvent(SerialPortEvent oEvent) {
         if (state == ConnectionState.ACTIVE) {
             if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
                 try {
                     while (input.available() > 0) {
                         byte chunk[] = new byte[1];
                         input.read(chunk, 0, 1);
                         
                         byteReceived(chunk[0]);
                     }
                 } catch (Exception e) {
                     System.err.println(e.toString());
                 }
             }
         } 
         else {
             if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
                 try {
                     while (input.available() > 0) {
                         byte[] response = new byte[1];
                         input.read(response, 0, 1);
                         if (lastByte == (byte)0x00 && response[0] == (byte)0xFF){
                             state = ConnectionState.ACTIVE;
                         }
                         
                         lastByte = response[0];
                     }
                 } catch (Exception e) {
                     System.err.println(e.toString());
                 }
             }
         }
         // Ignore all the other eventTypes, but you should consider the other ones.
     }
     
     @Override
     public synchronized void sendBytes(byte[] bytes) throws IOException {
         output.write(bytes);
     }
 }

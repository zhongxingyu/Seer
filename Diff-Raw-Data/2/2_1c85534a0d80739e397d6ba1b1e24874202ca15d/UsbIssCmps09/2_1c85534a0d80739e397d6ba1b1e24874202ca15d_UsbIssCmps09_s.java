 import java.io.*;
 import net.tinyos.comm.*;
 
 public class UsbIssCmps09 implements SerialPortListener {
 
     private SerialPort port;
     private String portName;
     private InputStream in;
     private OutputStream out;
 
     Object readBlock = new Object();
 
     private QueryThread queryThread;
 
     private int bearing;
 
     private class QueryThread extends Thread {
       public void run() {
         while (true) {
           try {
             bearing = queryCMPS10();
           } catch (IOException e) {
             // TODO: handle error in the serial comm
           }
         }
       }
     }
 
     public UsbIssCmps09(String portName) throws IOException, UnsupportedCommOperationException {
        this.portName = portName;
        open();
        setI2CMode();
 
        queryThread = new QueryThread();
        queryThread.start();
     }
 
     private void open() throws IOException, UnsupportedCommOperationException {
         System.out.println("Opening port " + portName);
         port = new TOSSerial(portName);
 
 
 
 
         try {
             //port.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
             port.setSerialPortParams(19200, 8, SerialPort.STOPBITS_2, false);
             port.addListener(this);
             port.notifyOn(SerialPortEvent.DATA_AVAILABLE, true);
             port.notifyOn(SerialPortEvent.OUTPUT_EMPTY, true);
         } catch (Exception e) {
             port.close();
             throw new IOException("Could not configure " + portName + ": "+ e.getMessage());
         }
 
         in = port.getInputStream();
         out = port.getOutputStream();
 
         port.open();
     }
 
     private void setI2CMode() throws IOException {
     	byte[] cmd = {
     	        0x5A,	// USB_ISS command
     	        0x02,	// Set mode
     	        0x40,	// Set mode to 100KHz I2C
     	        0x00    // Spare pins set to output low
     	};
     	byte[] resp = new byte[2];
     	
         System.out.println("Writing " + cmd[0] + " " + cmd[1] + " " + cmd[2] + " " + cmd[3]);
     	out.write(cmd);
     	out.flush();
     	read(resp);
         System.out.println("Response " + resp[0] + " " + resp[1]);
     	if (resp[0] != (byte)0xFF) {
     		throw new IOException("**set_i2c_mode: Error setting I2C mode!**");
     	}
   }
 
   // TOSSerial's InputStream is not a real InputStream, it does not respect the semantics in that it is non-blocking.
   // This is a helper function to implement the blocking variant, already for n bytes
   private int read(byte[] b) throws IOException  {
     int r = 0;
 
     while (r < b.length) {
       synchronized (readBlock) {
         if (in.available() == 0) {
           try {
             readBlock.wait();
           } catch (InterruptedException e) {	//continue the cycle
           }
         } else {
           int ret = in.read(b);
           if (ret == -1) {
             return -1;
           } else {
             r += ret;
           }
         }
       }
     }
     return r;
   }
 
   public void serialEvent(SerialPortEvent ev) {
     if (ev.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
       synchronized (readBlock) {
         readBlock.notify();
       }
     }
   }
 
     private int queryCMPS10() throws IOException {
     	int bearing;
     	byte[] cmd = {
     			0x55,	// USBI2C command for single byte address device
     			(byte)0xC1,	// CMPS09 address with R/W bit set high
     			0x00,	// Register we want to read from (0 is software version)
     			0x06
     	};
     	byte[] resp = new byte[6];
 
     	out.write(cmd);
     	out.flush();
     	read(resp);
 
    	bearing = ((resp[2] << 8) + resp[3]) /10;
     	return bearing;
     }
 
     public int getDirection() {
       return bearing;
     }
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) throws IOException, UnsupportedCommOperationException {
 			UsbIssCmps09 compass = new UsbIssCmps09(args[0]);
 			while(true) {
 				System.out.println(compass.getDirection());
 			}
 	}
 
 }

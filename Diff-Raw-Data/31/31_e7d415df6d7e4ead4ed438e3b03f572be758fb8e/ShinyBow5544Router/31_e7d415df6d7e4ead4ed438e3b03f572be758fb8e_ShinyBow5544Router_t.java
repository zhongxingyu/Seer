 package net.tuschhcm.routercontrol.router;
 
 import java.io.PrintStream;
 
 import gnu.io.CommPortIdentifier;
 import gnu.io.SerialPort;
 
 /**
  * Router implementation for a ShinyBow SB-5544
  * 
  */
 public class ShinyBow5544Router implements Router {
	private static final int MSG_DELAY = 50;
	
     private final SerialPort mSerialPort;
     private final PrintStream mOut;
 
     /**
      * Create a new ShinyBow router using the given comm port.
      * 
      * @param portName Com port name
      */
     public ShinyBow5544Router(final String portName) {
         try {
             CommPortIdentifier portId = CommPortIdentifier.getPortIdentifier(portName);
             
             // 9600 baud, 8 bit, no parity, 1 stop bit
            mSerialPort = (SerialPort) portId.open("routercontrol", 100);
             mSerialPort.setSerialPortParams(9600,
                                             SerialPort.DATABITS_8,
                                             SerialPort.STOPBITS_1,
                                             SerialPort.PARITY_NONE);
 
             mOut = new PrintStream(mSerialPort.getOutputStream());
         } catch (Exception e) {
             throw new IllegalArgumentException("Unable to open serial port: " + e.getMessage(), e);
         }
     }
     
     @Override
     public void switchInput(int output, int input)
             throws IllegalArgumentException {
         if (output < 1 || output > 4) {
            throw new IllegalArgumentException("Invalid Output");
         }
 
         if (input < 1 || input > 4) {
            throw new IllegalArgumentException("Invalid Input");
         } 
 
 
        mOut.format("SBI0%dO0%d", input, output);
        try {
        	Thread.sleep(MSG_DELAY);
        	
        } catch (InterruptedException e) {
        	// ignore
        }
     }
 
     @Override
     public void setPower(boolean on) {
         if (on) {
             mOut.print("SBSYSMON");
 
         } else {
             mOut.print("SBSYSMOF");
         }
        
        try {
        	Thread.sleep(MSG_DELAY);
        	
        } catch (InterruptedException e) {
        	// ignore
        }
     }
 
     @Override
     public void setLockControls(boolean enabled) {
         if (enabled) {
             mOut.print("SBSYSMLK");
 
         } else {
             mOut.print("SBSYSMUK");
         }
        
        try {
        	Thread.sleep(MSG_DELAY);
        	
        } catch (InterruptedException e) {
        	// ignore
        }
     }
     
     @Override
     public void close() {
         mOut.close();
         mSerialPort.close();
     }
 }

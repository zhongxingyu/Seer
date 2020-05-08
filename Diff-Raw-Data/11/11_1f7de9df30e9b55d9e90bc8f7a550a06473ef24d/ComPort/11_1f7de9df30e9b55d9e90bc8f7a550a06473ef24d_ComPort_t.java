 package jivko.util;
 
 import gnu.io.CommPort;
 import gnu.io.CommPortIdentifier;
 import gnu.io.SerialPort;
 
 import java.io.InputStream;
 import java.io.OutputStream;
 
 /**
  *
  * @author Sergii Smehov (smehov.com)
  */
 public class ComPort {
     
   public static boolean isComEnabled() {
     if (OsUtils.isWindows())
       return false;
     
    return true;
   }
   
   private InputStream in;
   private OutputStream out;
 
   public InputStream getIn() {
     return in;
   }
 
   public OutputStream getOut() {
     return out;
   }
   
   private String portName;
   private int speed;  
 
   public ComPort(String portName, int speed) throws Exception {
     this.portName = portName;
     this.speed = speed;
     
     connect(portName, speed);
   }
   
   private void connect(String portName, int speed) throws Exception {
     if (!isComEnabled())
       return;
     
     CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
     if (portIdentifier.isCurrentlyOwned()) {
       System.out.println("Error: Port is currently in use");
     } else {
       CommPort commPort = portIdentifier.open(this.getClass().getName(), 2000);
 
       if (commPort instanceof SerialPort) {
         SerialPort serialPort = (SerialPort) commPort;
         serialPort.setSerialPortParams(speed, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
 
         in = serialPort.getInputStream();
         out = serialPort.getOutputStream();
         
       } else {
         System.out.println("Error: Only serial ports are handled");
       }
     }
   }
   
   public void write(String data) throws Exception {
     if (!isComEnabled())
       return;
     
     out.write(data.getBytes());
     
     Thread.sleep(150);
     
     printInBuffer();
   }
   
   public boolean sentReceivedEquals(String sent, String received) {
     if (!isComEnabled())
       return true;
     
     if (!received.contains("\r\n"))
       return false;
     
     if (!sent.startsWith("#") || received.startsWith("#"))
       return false;
     
     boolean result = false;
     
     char sentCode = sent.charAt(1);
     
     String receivedCodeStr = received.substring(1).split(" ")[0];
     int receivedCode = Integer.parseInt(receivedCodeStr);
     
     return sentCode == (char)receivedCode;
   }
   
   /*public void writeCharByCharUntilReceived(String data, int delay, int maxRetries) throws Exception {
     String buffer;
         
     for (int i = 0; i < maxRetries; ++i) {
       writeCharByChar(data, delay);
       buffer = readInBuffer();
       if (sentReceivedEquals(data, buffer))
         break;
     }        
   }*/
   
   public void writeCharByCharUntilReceived(String data, int delay, int maxRetries) throws Exception {
     if (!isComEnabled())
       return;
     
     String buffer;
         
     for (int i = 0; i < maxRetries; ++i) {
       writeCharByChar(data, delay);
       buffer = readInBuffer();
       if (data.contains("\r\n"))
         break;
     }        
   }
   
   public void writeCharByChar(String data, int delay) throws Exception {
     if (!isComEnabled())
       return;
     
     for (int i = 0; i < data.length(); ++i) {
       out.write(data.charAt(i));      
       Thread.sleep(delay);
     }
     
     Thread.sleep(150);        
   }  
   
   private String readInBuffer() throws Exception {
     if (!isComEnabled())
       return "";
     
     String bufferStr = "";
     byte[] bytes = new byte[1024];
     while (in.available() != 0) {      
       int readBytes = in.read(bytes);
       for (int i = 0; i < readBytes; ++i) {
         bufferStr += (char)bytes[i];       
       }      
     }
     return bufferStr;
   }
   
   private void printInBuffer() throws Exception {
     if (!isComEnabled())
       return;
     
     byte[] bytes = new byte[1024];
     while (in.available() != 0) {      
       String bufferStr = "";
       int readBytes = in.read(bytes);
       for (int i = 0; i < readBytes; ++i) {
         bufferStr += (char)bytes[i];       
       }
       System.out.println("buffer = " + bufferStr);
     }    
   }
 }

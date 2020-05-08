 /*
  * Created on 16-Oct-2004
  */
 package org.tom.weather.comm;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Enumeration;
 import java.util.List;
 
 import javax.comm.CommPortIdentifier;
 import javax.comm.NoSuchPortException;
 import javax.comm.PortInUseException;
 import javax.comm.SerialPort;
 import javax.comm.UnsupportedCommOperationException;
 
 import org.apache.log4j.Logger;
 
 import uk.me.jstott.jweatherstation.util.CRC;
 import uk.me.jstott.jweatherstation.util.UnsignedByte;
 
 /**
  * 
  * 
  * @author Jonathan Stott
  * @version 1.1
  * @since 1.0
  */
 public abstract class Station {
   static protected final Logger LOGGER = Logger.getLogger(Station.class);
   private static final int ACK = 6;
   static protected final byte LF = '\n';
   static protected final byte CR = '\r';
   private InputStream inputStream = null;
   protected Calendar lastDate = Calendar.getInstance();
   private List uploaders;
   private OutputStream outputStream = null;
   private SerialPort port = null;
   private CommPortIdentifier portID = null;
   private String portName;
   private int baudRateOrPort;
   protected CRC crc = new CRC();
   protected InetAddress ip;
   protected boolean usingSerial;
   protected List posterList;
   protected String location;
   
   public Station(String portName, int baudRate, int rainGauge) throws PortInUseException,
       NoSuchPortException, IOException {
       LOGGER.debug("rainGauge: " + rainGauge);
       // parse the portname as an IP addr
       // if it is a vaid one, then treat baudRateOrPort as a port number
     try {
         ip = InetAddress.getByName(portName);
         // portName must be a valid IP address, so assign port number
         baudRateOrPort = baudRate;
         Socket s = new Socket(ip.getHostAddress(), baudRate);
         inputStream = new DataInputStream(new BufferedInputStream(s.getInputStream()));
         outputStream = new DataOutputStream(new BufferedOutputStream(s.getOutputStream()));
         usingSerial = false;
         LOGGER.debug(inputStream);
         LOGGER.debug(outputStream);
 
     } catch (UnknownHostException ex) {
         // must be a COM port
        LOGGER.info(portName + " not an IP address, using serial");
         this.portName = portName;
         this.baudRateOrPort = baudRate;
         portID = getPortID(portName);
         usingSerial = true;
         openPort();
     }
   }
 
   private CommPortIdentifier getPortID(String portName)
       throws NoSuchPortException {
     if (LOGGER.isDebugEnabled()) {
       LOGGER.debug("main: Listing ports: ");
       Enumeration pList = CommPortIdentifier.getPortIdentifiers();
       // Process the list, putting serial and parallel into ComboBoxes
       while (pList.hasMoreElements()) {
         CommPortIdentifier cpi = (CommPortIdentifier) pList.nextElement();
         LOGGER.debug(cpi.getName());
       }
       LOGGER.debug("Done listing ports");
     }
     return CommPortIdentifier.getPortIdentifier(portName);
   }
 
   /**
    * Open and configure the serial port ready for use.
    * 
    * @return true if the serial port was successfully opened - false otherwise.
    * @throws PortInUseException
    *           if the serial port is already in use and we are unable to get a
    *           lock on it.
    * @since 1.0
    */
   private boolean openPort() throws PortInUseException {
     // Try to open the port
     try {
       port = (SerialPort) portID.open("Davis Station", 2000);
       if (port == null) {
         LOGGER.error("Error opening port " + portID.getName());
         return false;
       }
       LOGGER.info("Opening port: " + port);
       // Get the input stream
       try {
         inputStream = port.getInputStream();
       } catch (IOException e) {
         LOGGER.error("Cannot open input stream", e);
       }
       // Get the output stream
       try {
         outputStream = port.getOutputStream();
       } catch (IOException e) {
         LOGGER.error("Cannot open output stream");
       }
       // Create the transmitter and receiver
       // transmitter = new TransmitterDmp(this);
       // receiver = new ReceiverDmp(this, transmitter);
       // Setup an event listener for the port
       try {
         port.setSerialPortParams(baudRateOrPort, SerialPort.DATABITS_8,
             SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
       } catch (UnsupportedCommOperationException e) {
         LOGGER.error(e);
       }
       // startCommThreads(); try to communicate manually
     } catch (PortInUseException e) {
       LOGGER.info("Queueing open for " + portID.getName() + ": port in use by "
           + e.currentOwner);
     }
     return true;
   }
 
   /**
    * Close the serial port.
    * 
    * @since 1.0
    */
   public void shutdown() {
     // Close the port
     port.close();
     port = null;
   }
 
   protected InputStream getInputStream() {
     return inputStream;
   }
 
   private OutputStream getOutputStream() {
     return outputStream;
   }
 
   protected SerialPort getPort() {
     return port;
   }
 
   protected void clearInputBuffer() throws IOException {
     int bytesAvailable = getInputStream().available();
     LOGGER.debug(bytesAvailable + " bytes available from the input stream");
     for (int i = 0; i < bytesAvailable; i++) {
       this.getInputStream().read();
     }
     if (LOGGER.isDebugEnabled()) {
       if (bytesAvailable > 0) {
         LOGGER.debug("cleared: " + bytesAvailable
             + " bytes from the input stream");
       }
     }
   }
 
   protected void sendByte(byte b) throws IOException {
     sendBytes(new byte[] { b });
   }
 
   protected void delay(long millis) {
     try {
       Thread.sleep(millis);
     } catch (InterruptedException e) {
       LOGGER.warn(e);
     }
   }
 
   protected boolean getAck() throws IOException {
     delay(500);
     int ack = getInputStream().read();
     if (ack == ACK) {
       return true;
     } else {
       if (LOGGER.isDebugEnabled()) {
         LOGGER.debug("missed ack: " + ack);
       }
       return false;
     }
   }
 
   protected void sendUnsignedBytes(UnsignedByte[] bytes) throws IOException {
     byte[] bs = new byte[bytes.length];
     for (int i = 0; i < bytes.length; i++) {
       bs[i] = (byte) (bytes[i].getByte() & 0xFF);
 //      if (LOGGER.isDebugEnabled()) {
         // LOGGER.debug("0x" + Integer.toHexString((bs[i] & 0xFF)) + ", ");
 //      }
     }
     sendBytes(bs);
   }
 
   protected void sendBytes(byte[] bytes) throws IOException {
     int count;
     count = bytes.length;
     if (count > 0) {
       try {
         for (int i = 0; i < count; i++) {
 //          if (LOGGER.isDebugEnabled()) {
 //            LOGGER.debug("tx: Sending bytes >0x"
 //              + Integer.toHexString(bytes[i] & 0xFF) + "<");
 //          }
           getOutputStream().write(bytes[i] & 0xFF);
           crc.updateCRC(bytes[i]);
         }
         getOutputStream().flush();
       } catch (IOException ex) {
         LOGGER.error(ex);
         throw ex;
       }
     }
   }
 
   public void setPortName(String portName) {
     this.portName = portName;
   }
 
   public String getPortName() {
     return portName;
   }
 
   public void setUploaderList(List uploaders) {
     this.uploaders = uploaders;
   }
 
   public List getUploaderList() {
     return uploaders;
   }
 
   protected void readBytes(byte[] buffer) throws IOException {
     int bytes = 0;
     int error_count = 0;
     while (((bytes = getInputStream().available()) != buffer.length) && (error_count++ < 20)) {
         //LOGGER.debug("waiting for bytes available");
         delay(100);
     }
     if (error_count >= 20) {
       IOException e = new IOException("20 errors in determining bytes avaiable from stream");
       LOGGER.error(e);
     }
     //LOGGER.debug(bytes + " available)");
     if (bytes == buffer.length) {
       bytes = getInputStream().read(buffer);
     } else {
       LOGGER.warn("unexpected buffer size of: " + bytes + " - throwing IOException");
       throw new IOException();
     }
   }
 
   protected byte readByte() throws IOException {
     int bytes = getInputStream().available();
     byte b;
     if (bytes >= 1) {
       b = (byte)getInputStream().read();
     } else {
       LOGGER.warn("no bytes available");
       throw new IOException();
     }
     return b;
   }
 
   public void setPosterList(List posterList) {
     this.posterList = posterList;
   }
 
   public List getPosterList() {
     return posterList;
   }
 
   public abstract String getLocation();
 
   public abstract void setLocation(String location);
 
   public abstract boolean test() throws IOException;
 
   public abstract void readArchiveMemory() throws Exception;
 
   public abstract void readCurrentConditions() throws Exception;
 
 }

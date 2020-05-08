 /*
  * Created on Feb 25, 2005
  *
  * TODO To change the template for this generated file go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 package org.concord.sensor.serial;
 
 import gnu.io.CommPortIdentifier;
 import gnu.io.RXTXCommDriver;
 import gnu.io.UnsupportedCommOperationException;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.Enumeration;
 
 
 /**
  * @author Informaiton Services
  *
  * TODO To change the template for this generated type comment go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 public class SensorSerialPortRXTX 
 	implements SensorSerialPort 
 {
     RXTXCommDriver commDriver = null;
 	gnu.io.SerialPort port = null;
 	InputStream inStream = null;
 	OutputStream outStream = null;
     private int currentTimeout;
 	
 	public void open(String portName)
 		throws IOException
 	{
 		if(commDriver == null) {
 			commDriver = new RXTXCommDriver();
 			commDriver.initialize();
 		}
 		
 		if(port != null) {
 			// assert
 			throw new RuntimeException("The port was not closed before being opened");
 		}
 		
		Enumeration ports = CommPortIdentifier.getPortIdentifiers();
		while(ports.hasMoreElements()) {
			CommPortIdentifier portID = (CommPortIdentifier)ports.nextElement();
			System.out.println("found port: " + portID.getName());
		}
		
 		port = (gnu.io.SerialPort) commDriver.getCommPort(portName, 
 				CommPortIdentifier.PORT_SERIAL);
 		
 		if(port == null) {
 			throw new IOException("can't open serial port");
 		}
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.concord.sensor.dataharvest.DHSerialPort#close()
 	 */
 	public void close() throws IOException 
 	{
 		if(port == null) {
		    System.err.println("Port was not opened before being closed");
 		    return;
 		}
 		port.close();
 		port = null;
 	}
 	/* (non-Javadoc)
 	 * @see org.concord.sensor.dataharvest.SerialPort#setSerialPortParams(int, int, int, int)
 	 */
 	public void setSerialPortParams(int b, int d, int s, int p)
 			throws IOException 
 	{
 		try {
 			port.setSerialPortParams(b, d, s, p);
 		} catch (UnsupportedCommOperationException e) {
 			throw new IOException("UnsupportedCommOperation");
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.concord.sensor.dataharvest.SerialPort#getBaudRate()
 	 */
 	public int getBaudRate() 
 	{
 		return port.getBaudRate();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.concord.sensor.dataharvest.SerialPort#getDataBits()
 	 */
 	public int getDataBits() 
 	{
 		return port.getDataBits();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.concord.sensor.dataharvest.SerialPort#getStopBits()
 	 */
 	public int getStopBits() 
 	{
 		return port.getStopBits();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.concord.sensor.dataharvest.SerialPort#getParity()
 	 */
 	public int getParity() 
 	{
 		return port.getParity();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.concord.sensor.dataharvest.SerialPort#setFlowControlMode(int)
 	 */
 	public void setFlowControlMode(int flowcontrol) 
 		throws IOException 
 	{
 		try {
 			port.setFlowControlMode(flowcontrol);
 		} catch (UnsupportedCommOperationException e) {
 			throw new IOException("UnsupportedCommOperation");
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.concord.sensor.dataharvest.SerialPort#disableReceiveTimeout()
 	 */
 	public void disableReceiveTimeout() 
 	{
 		port.disableReceiveTimeout();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.concord.sensor.dataharvest.SerialPort#enableReceiveTimeout(int)
 	 */
 	public void enableReceiveTimeout(int time) throws IOException 
 	{
 	    currentTimeout = time;
 		try {
 			port.enableReceiveTimeout(time);
 		} catch (UnsupportedCommOperationException e) {
 			throw new IOException("UnsupportedCommOperation");
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.concord.sensor.dataharvest.SerialPort#getInputStream()
 	 */
 	public InputStream getInputStream() throws IOException 
 	{
 	    inStream = port.getInputStream();
 		return inStream;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.concord.sensor.dataharvest.SerialPort#getOutputStream()
 	 */
 	public OutputStream getOutputStream() 
 		throws IOException 
 	{
 	    outStream = port.getOutputStream();
 		return outStream;
 	}
 
 	public int readBytes(byte [] buf, int off, int len, long timeout)
 		throws IOException
 	{	
 		// at least one of the receive time and theshold
 		// don't work on windows.
 		if(!System.getProperty("os.name").startsWith("Windows")){
 			try {
 				port.enableReceiveTimeout((int)timeout);
 				port.enableReceiveThreshold(len);
 				
 				int numRead = inStream.read(buf, off, len);
 				
 				port.disableReceiveThreshold();
 				port.enableReceiveTimeout(currentTimeout);
 				return numRead;
 			} catch (UnsupportedCommOperationException e) {
 				System.err.println("timeout or threshold not available on this platform");
 			}
 		}
 		
 	    // Fall back to polling method. 
 	    // this method still assumes some form of timeout is supported
 	    // to handle no timeout support we'll need multiple threads.
 	    int size = 0;	    
 	    long startTime = System.currentTimeMillis();
 	    while(size != -1 && size < len &&
 	            (System.currentTimeMillis() - startTime) < timeout){
 	        int readSize = inStream.read(buf, size+off, len - size);
 	        if(readSize < 0) {	      
 	            System.err.println();
 	            System.err.println("error in readBytes: " + readSize);
 	            
 	            return readSize;
 	        }
 	        size += readSize;
 	    }
 		    
 	    return size;	
 	}	
 }

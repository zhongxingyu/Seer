 /*
  *  Copyright (C) 2004  The Concord Consortium, Inc.,
  *  10 Concord Crossing, Concord, MA 01742
  *
  *  Web Site: http://www.concord.org
  *  Email: info@concord.org
  *
  *  This library is free software; you can redistribute it and/or
  *  modify it under the terms of the GNU Lesser General Public
  *  License as published by the Free Software Foundation; either
  *  version 2.1 of the License, or (at your option) any later version.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  *  Lesser General Public License for more details.
  *
  *  You should have received a copy of the GNU Lesser General Public
  *  License along with this library; if not, write to the Free Software
  *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  * END LICENSE */
 
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
 
 import org.concord.sensor.impl.Vector;
 
 
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
 	
     int baud;
     int dataBits;
     int stopBits;
     int parity;
     int flowcontrol;
     
     public Vector getAvailablePorts()
     {
         Vector availablePorts = new Vector();
         
         if(commDriver == null) {
             commDriver = new RXTXCommDriver();
             commDriver.initialize();
         }
         
         Enumeration ports = CommPortIdentifier.getPortIdentifiers();
         while(ports.hasMoreElements()) {
             CommPortIdentifier portID = (CommPortIdentifier)ports.nextElement();
             System.out.println("RXTX: found port: " + portID.getName());
             availablePorts.add(portID.getName());
         }
         
         return availablePorts;
     }
     
 	public void open(String portName)
 		throws SerialException
 	{
 		if(commDriver == null) {
 			commDriver = new RXTXCommDriver();
 			commDriver.initialize();
 
 			Enumeration ports = CommPortIdentifier.getPortIdentifiers();
 			while(ports.hasMoreElements()) {
 				CommPortIdentifier portID = (CommPortIdentifier)ports.nextElement();
 				System.out.println("RXTX: found port: " + portID.getName());
 			}
 		}
 		
 		if(port != null) {
 			// assert
 			throw new RuntimeException("The port was not closed before being opened");
 		}
 		
 		System.out.println("RXTX: opening port: " + portName);
 		port = (gnu.io.SerialPort) commDriver.getCommPort(portName, 
 				CommPortIdentifier.PORT_SERIAL);
 		
 		if(port == null) {
 			throw new SerialException("can't open serial port");
 		}
         
         // We'll have to test if this is ok
         // we are changing some params of the port after
         // we open so we might need to reset these
         // streams
         try {
             inStream = port.getInputStream();
             outStream = port.getOutputStream();
         } catch (IOException e){
             throw new SerialException("can't open streams", e);
         }
         
         try {
             port.setSerialPortParams(baud, dataBits, stopBits, parity);
             port.setFlowControlMode(flowcontrol);
         } catch (UnsupportedCommOperationException e) {
            throw new SerialException("UnsupportedCommOperation", e);
         }        
         
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.concord.sensor.dataharvest.DHSerialPort#close()
 	 */
 	public void close() throws SerialException 
 	{
 		if(port == null) {
 		    return;
 		}
 		port.close();
 		port = null;
 	}
 	
 	public boolean isOpen()
 	{
 	    return port != null;
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.concord.sensor.dataharvest.SerialPort#setSerialPortParams(int, int, int, int)
 	 */
 	public void setSerialPortParams(int b, int d, int s, int p)
 			throws SerialException 
 	{
         baud = b;
         dataBits = d;
         stopBits = s;
         parity = p;
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
 		throws SerialException 
 	{
         this.flowcontrol = flowcontrol;
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
 	public void enableReceiveTimeout(int time) throws SerialException 
 	{
 	    currentTimeout = time;
 		try {
 			port.enableReceiveTimeout(time);
 		} catch (UnsupportedCommOperationException e) {
 			throw new SerialException("UnsupportedCommOperation");
 		}
 	}
 
 	public int readBytes(byte [] buf, int off, int len, long timeout)
 		throws SerialException
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
 			} catch (IOException e) {
 			    throw new SerialException("threshold read error", e);
             }
 		}
 		
 	    // Fall back to polling method. 
 	    // this method still assumes some form of timeout is supported
 	    // to handle no timeout support we'll need multiple threads.
 		try {
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
         } catch (IOException e) {
             throw new SerialException("polling read error", e);            
         }
 	}	
     
     public void write(byte[] buffer) 
     throws SerialException
     {
         try {
             outStream.write(buffer);
         }   catch (IOException e) {
             throw new SerialException("write error", e); 
         }
     }
     
     public void write(byte[] buffer, int start, int length) 
     throws SerialException
     {
         try {
             outStream.write(buffer, start, length);        
         }   catch (IOException e) {
             throw new SerialException("write error", e); 
         }
     }
     
     public void write(int value) 
     throws SerialException
     {
         try {
             outStream.write(value);
         }   catch (IOException e) {
             throw new SerialException("write error", e); 
         }
     }
 }

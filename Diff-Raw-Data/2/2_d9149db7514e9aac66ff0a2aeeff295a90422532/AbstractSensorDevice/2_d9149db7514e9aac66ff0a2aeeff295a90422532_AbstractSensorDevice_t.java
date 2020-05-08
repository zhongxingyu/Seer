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
  * Created on Feb 15, 2005
  *
  * TODO To change the template for this generated file go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 package org.concord.sensor.device.impl;
 
 import org.concord.sensor.device.DeviceService;
 import org.concord.sensor.device.DeviceServiceAware;
 import org.concord.sensor.device.SensorDevice;
 import org.concord.sensor.impl.ExperimentConfigImpl;
 import org.concord.sensor.impl.Vector;
 import org.concord.sensor.serial.SensorSerialPort;
 import org.concord.sensor.serial.SerialException;
 
 /**
  * @author scott
  *
  * TODO To change the template for this generated type comment go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 public abstract class AbstractSensorDevice 
 	implements SensorDevice, DeviceServiceAware
 {
 	public final static int ERROR_NONE			= 0;
 	public final static int ERROR_GENERAL			= -1;
 	public final static int ERROR_PORT				= -2;
 	public final static int ERROR_DEV_VERSION		= -3;	
 
 	// Default is SD for sensor device;
 	protected String deviceLabel = "SD";
 	
 	protected ExperimentConfigImpl currentConfig = null;
     protected boolean attached = false;
 
     protected String portName;
 	protected SensorSerialPort port = null;
 	
 	protected int error = 0;
 	protected int portError = 0;
 
 
     protected DeviceService devService;
     
 	/* (non-Javadoc)
 	 * @see org.concord.sensor.device.SensorDevice#isAttached()
 	 */
 	public boolean isAttached()
 	{
 		if(port == null || !port.isOpen()){
 			return false;
 		}
 		return isAttachedInternal(portName);
 	}
 	
     public void setDeviceService(DeviceService provider)
     {
         this.devService = provider;
     }
  
 	protected void closePort()
 	{
 	    if(port == null || !port.isOpen()) {
 	        // port should already be closed
 	        return;
 	    }
 	    
 	    log("closing port: " + portName);
 	    
 	    try {
 	    	port.close();
 	    } catch (SerialException e){
 	    	e.printStackTrace();
 	    }	    
 	}
 	
 	protected boolean openPort()
 	{
 		// This comes from the airlink implementation these error codes
 		// are not used by every device implementation
     	error = ERROR_NONE;
 
 	    // This is for backward compatibility with older LabBooks
 	    // which have empty port names.
         if(portName == null || portName.trim().length() == 0) {
             portName = "bluetooth";
         }
         
         if("_auto_".equals(portName)) {
             Vector availablePorts = getAvailablePorts();
             for(int i=0; i<availablePorts.size(); i++) {  
                 String possiblePort = (String)availablePorts.get(i);
                 
                 // Try opening the port
                 if(openPortName(possiblePort)){
                 	// replace _auto_ with the opened port name
                     portName = possiblePort;
                     return true;
                 }
             }
         } else {
         	if(openPortName(portName)) {
         		return true;
         	}
         }
 
         return false;
 	}
 
 	protected boolean openPortName(String possiblePort)
 	{
     	log("looking for device on port: " + possiblePort);
         if(!attemptToOpenPort(possiblePort)){
             // we couldn't even open this port
         	log("could not open port: " + possiblePort);
        	// close the port just to be safe
        	closePort();
             return false;
         }
         
         // we could at least open the port but we don't know if
         // the device is attached.
         if(!isAttachedInternal(possiblePort)) {
             // we opened the port but nothing was there
         	log("could not find device on port: " + possiblePort);
         	closePort();
             return false;
         }
         
         // if we got this far then we found a device
         // change the portname.
         // That way we won't check every port each time this
         // method is called.
         // Currently this does mean they 
         // will have to restart the program if the device changes
         // ports for whatever reason
         log("found device on port: " + possiblePort);
         return true;
 	}
 	
 	protected void log(String message)
 	{
 		devService.log(deviceLabel + ": " + message);
 	}
 	
 	/**
 	 * By default this return the operating system serial port
 	 * this is a bit confusing because it is really an object
 	 * which can open the operating system serial ports not a 
 	 * particular serial port.
 	 * 
 	 * It should be overriden if different type of
 	 * SensorSerialPort is needed by the device.
 	 * 
 	 * @return
 	 */
 	protected SensorSerialPort getSensorSerialPort()
 	{
 		return devService.getSerialPort("os", port);
 	}
 	
     protected boolean isAttachedInternal(String portLabel)
     {
 		return attached;
     }
 
     protected Vector getAvailablePorts()
     {
 	    // Make sure the port is closed before opening it
 	    closePort();
 	    
     	if(port == null) {
 	    	port = getSensorSerialPort();
 	    }
 
 	    if(port == null) {
             log("Cannot open serial driver");
 		    return null;				        
 	    }
 
         return port.getAvailablePorts();    	
     }
     
     public boolean attemptToOpenPort(String portName)
     {
 	    // Make sure the port is closed before opening it
 	    closePort();
 	    
     	if(port == null) {
 	    	port = getSensorSerialPort();
 	    }
 
 	    if(port == null) {
             log("Cannot open serial driver");
 		    return false;				        
 	    }
 
 	    int [] spp = getSerialPortParams();
 	    
         // set the basic serial params
         try {
 			port.setSerialPortParams(spp[0], spp[1], spp[2],spp[3]);
 	        port.setFlowControlMode(spp[4]);
             port.open(portName);
             log("opened port: " + portName);
 		} catch (SerialException e) {
             portError = e.getPortError();
             port = null;
             error = ERROR_PORT;
             log("Cannot open port " + portName + " err: " + portError);
             log("  msg: " + e.getMessage());
             return false;
 		}
 	    
         return initializeOpenPort(portName);        
     }
     
         
     protected abstract boolean initializeOpenPort(String portName);
     
     /**
      * This shoudl return an array of size 5 which contains the serial port
      * params.
      * @return
      */
     protected abstract int [] getSerialPortParams();
 }

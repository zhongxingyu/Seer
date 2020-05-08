 /* Copyright (c) 2007, 2008 Bug Labs, Inc.
  * All rights reserved.
  *   
  * This program is free software; you can redistribute it and/or  
  * modify it under the terms of the GNU General Public License version  
  * 2 only, as published by the Free Software Foundation.   
  *   
  * This program is distributed in the hope that it will be useful, but  
  * WITHOUT ANY WARRANTY; without even the implied warranty of  
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU  
  * General Public License version 2 for more details (a copy is  
  * included at http://www.gnu.org/licenses/old-licenses/gpl-2.0.html).   
  *   
  * You should have received a copy of the GNU General Public License  
  * version 2 along with this work; if not, write to the Free Software  
  * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  
  * 02110-1301 USA   
  *
  */
 package com.buglabs.bug.input.pub;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import org.osgi.service.log.LogService;
 
 import com.buglabs.bug.jni.common.FCNTL_H;
 import com.buglabs.bug.jni.input.InputDevice;
 import com.buglabs.bug.jni.input.InputEvent;
 import com.buglabs.device.ButtonEvent;
 import com.buglabs.device.IButtonEventListener;
 import com.buglabs.device.IButtonEventProvider;
 
 public class InputEventProvider extends Thread implements IButtonEventProvider {
 
 	private ArrayList listeners;
 	private final LogService log;
 	private String inputDevice;
 
 	public InputEventProvider(String inputDevice, LogService log) {
 		this.log = log;
 		listeners = new ArrayList();
 		this.inputDevice = inputDevice;
 	}
 	
 	public void addListener(IButtonEventListener listener) {
 		if(!listeners.contains(listener)) {
 			listeners.add(listener);
 		}
 	}
 
 
 	public void removeListener(IButtonEventListener listener) {
 		synchronized(listeners) {
 			listeners.remove(listener);
 		}
 	}
 
 	public void run() {
 		InputDevice dev = new InputDevice();
 		if (dev.open(inputDevice, FCNTL_H.O_RDWR) < 0) {
 			log.log(LogService.LOG_ERROR, "Unable to open input device: " + inputDevice);
 		}
 		
 		while(!isInterrupted()) {
 			InputEvent[] inputEvents = dev.readEvents();
 			
 			synchronized(listeners) {
 				Iterator iter = listeners.iterator();
 				
 				for(int i = 0; i < inputEvents.length; ++i) {					
					ButtonEvent b = new ButtonEvent(inputEvents[i].code, 0, inputEvents[i].code, convertButtonAction(inputEvents[i].value), this.getClass().toString());
 				
 					while(iter.hasNext()) {
 						IButtonEventListener l = (IButtonEventListener) iter.next();
 						l.buttonEvent(b);
 					}
 				}
 			}
 		}
 		
 		dev.close();
 	}
 
 	private long convertButtonAction(long value) {
 		if (value == 1) {
 			return ButtonEvent.KEY_DOWN;
 		}
 		
 		if (value == 0) {
 			return ButtonEvent.KEY_UP;
 		}
 		
 		return value;
 	}
 
 	public void tearDown() {
 		interrupt();
 	}
 }

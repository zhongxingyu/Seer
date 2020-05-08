 /*
 Copyright (c) 2013 J. L. Canales Gasco
  
 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.
  
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
  
 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA}]
 */
 package org.rotarysource.listener;
 
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 import java.util.Map.Entry;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.espertech.esper.client.EventBean;
 import com.espertech.esper.client.UpdateListener;
 
 /**
  * Implements Base class for a Esper listener to manage calls that
  * Collects an EvenProd event.
  * This listener allow create EvenProd based listeners avoiding the 
  * complexity to map Event Fiels to object and concentrate in 
  * Business Logic
  * 
  * @author J. L. Canales
  * 
  */
 public abstract class MapBaseListener implements UpdateListener{
 
 	private static Logger  log = LoggerFactory.getLogger(MapBaseListener.class);	
 	
 	 /** 
      * UpdateListener update overriden method
      * This method implements the UpdateListener interfaces mapping
      * EventBean object to a Map Object
      * 
      @param newEvents. Event data received from esper engine
      @param oldEvents. Old Event Data received from esper engine
      * 
      */
 	@Override
 	public void update(EventBean[] newEvents, EventBean[] oldEvents) {
 	    if (newEvents == null)
 	    {
 	        return;
 	    }
 	   
 	    for (int i = 0; i < newEvents.length; i++)
 	    {
 
 	        String propertyNames[] = newEvents[i].getEventType().getPropertyNames();
 	        Map<String, String> eventMap = new HashMap<String, String>();
 
         	for (int j = 0; j<propertyNames.length; j++){
	        	log.debug("Mapping key {} to value  {}", propertyNames[j], newEvents[i].get(propertyNames[j]));
	        	if( newEvents[i].get(propertyNames[j]) != null){
        			eventMap.put( propertyNames[j], newEvents[i].get(propertyNames[j]).toString());
        		}
	        	else{
	        		eventMap.put( propertyNames[j], "null");
	        	}
         	}
 
         	
 	    	// DEBUG event information
         	if (log.isDebugEnabled()){	        	
             
 	            Set<Entry<String, String>> set = eventMap.entrySet();
 	            // Get an iterator
 	            Iterator<Entry<String, String>> it = set.iterator();
 	            // Display elements
 	            StringBuffer logText = new StringBuffer();
 
 	            while(it.hasNext()) {
 	            	Entry<String, String> mapEntry = it.next();
 	            	logText.append( mapEntry.getKey())
 	    		    		.append("=")
 	    		    		.append( mapEntry.getValue())
 	    		    		.append("; ");
 	            }
 	        	log.debug(logText.toString());
         	}
 	    	//DEBUG information.
        	
 			processEvent(eventMap);
 	    }
 	}
 	
 	/**
 	 * Abstract method to allow child classes to implement the processing logic for
 	 * each Map object.
 	 * 
 	 * @param event Map Object to be processed
 	 */
 	public abstract void processEvent( Map<String, String> eventMap);
 }

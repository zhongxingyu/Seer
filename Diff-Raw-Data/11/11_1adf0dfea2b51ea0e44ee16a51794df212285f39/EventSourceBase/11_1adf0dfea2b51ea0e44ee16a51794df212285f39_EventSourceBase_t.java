 /*******************************************************************************
  * Forwarding on Gates Simulator/Emulator
  * Copyright (C) 2012, Integrated Communication Systems Group, TU Ilmenau.
  * 
  * This program and the accompanying materials are dual-licensed under either
  * the terms of the Eclipse Public License v1.0 as published by the Eclipse
  * Foundation
  *  
  *   or (per the licensee's choosing)
  *  
  * under the terms of the GNU General Public License version 2 as published
  * by the Free Software Foundation.
  ******************************************************************************/
 package de.tuilmenau.ics.fog.util;
 
 import java.util.LinkedList;
 import java.util.Observer;
 
 import de.tuilmenau.ics.fog.facade.EventSource;
 import de.tuilmenau.ics.fog.facade.events.Event;
 import de.tuilmenau.ics.fog.ui.Logging;
 
 
 public class EventSourceBase implements EventSource
 {
 	@Override
 	public synchronized void registerListener(EventListener observer)
 	{
 		if(observer != null) {
 			if(observers == null) observers = new LinkedList<EventListener>();
 			
 			synchronized (observers) {
 				observers.add(observer);
 			}
 			
 			// relay events occurred previously to new listener
 			if(events != null) {
 				while(!events.isEmpty()) {
					// maybe notifyObservers() has deleted all observers!
					if(observers != null){
						if(!observers.isEmpty()){
							notifyObservers(events.removeFirst());
						}
					}
 				}
 				
 				events = null;
 			}
 		}
 	}
 
 	@Override
 	public synchronized boolean unregisterListener(EventListener observer)
 	{
 		boolean res = false;
 		
 		if(observers != null) {
 			synchronized (observers) {
 				// are we currently in a list iteration process?
 				if(loopCounter > 0) {
 					// yes! -> remove observer later
 					res = observers.contains(observer);
 					if(res) {
 						//Logging.getInstance().trace(this, "Store delayed removal " +observer);
 						
 						if(observersDeletion == null) observersDeletion = new LinkedList<EventSource.EventListener>();
 						
 						observersDeletion.add(observer);
 					}
 				} else {
 					// no! -> remove it immediately
 					res = observers.remove(observer);
 				}
 			}
 		}
 
 		return res;
 	}
 
 	public synchronized void notifyObservers(Event event)
 	{
 		if(observers != null) {
 			synchronized (observers) {
 				if(!observers.isEmpty()) {
 					loopCounter++;
 					
 					for(EventListener obs : observers) {
 						try {
 							obs.eventOccured(event);
 						}
 						catch(Error err) {
 							Logging.getInstance().err(this, "Error in observer '" +obs +"'.", err);
							System.exit(1);
 						}
 						catch(Exception exc) {
 							Logging.getInstance().err(this, "Exception in observer '" +obs +"'.", exc);
							System.exit(1);
 						}
 					}
 					
 					loopCounter--;
 					
 					// do we have to delete an observer after the iteration?
 					if((loopCounter <= 0) && (observersDeletion != null)) {
 						//Logging.getInstance().trace(this, "Delayed removal of " +observersDeletion.size() +" observers");
 						
 						for(EventListener obs : observersDeletion) {
 							observers.remove(obs);
 						}
 						
 						observersDeletion = null;
 					}
 				} else {
 					storeEvent(event);
 				}
 			}
 		} else {
 			storeEvent(event);
 		}
 	}
 	
 	/**
 	 * Stores events until listener is registered
 	 */
 	private void storeEvent(Event event)
 	{
 		if(events == null) events = new LinkedList<Event>();
 		
 		events.addLast(event);
 	}
 
 	private LinkedList<EventListener> observers = null;
 	private LinkedList<Event> events = null;
 	private LinkedList<EventListener> observersDeletion = null;
 	private int loopCounter = 0;
 }

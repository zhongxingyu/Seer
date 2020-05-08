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
 
 import de.tuilmenau.ics.fog.facade.EventSource;
 import de.tuilmenau.ics.fog.facade.NetworkException;
 import de.tuilmenau.ics.fog.facade.EventSource.EventListener;
 import de.tuilmenau.ics.fog.facade.events.Event;
 import de.tuilmenau.ics.fog.ui.Logging;
 
 
 public class BlockingEventHandling implements EventListener
 {
 	public BlockingEventHandling(EventSource source, int numberEvents)
 	{
 		this.source = source;
 		this.numberEvents = numberEvents;
 		
 		this.source.registerListener(this);
 	}
 
 	@Override
 	public synchronized void eventOccured(Event event) throws Exception
 	{
 		if((firstEvent == null) && (events != null)) {
 			firstEvent = event;
 		} else {
 			if(events == null) events = new LinkedList<Event>();
 			events.addLast(event);
 		}
 		
 		numberEvents--;
 		if(numberEvents <= 0) {
 			source.unregisterListener(this);
 		}
 		
 		notify();
 	}
 	
 	public synchronized Event waitForEvent()
 	{
 		return waitForEvent(0);
 	}
 	
 	public synchronized Event waitForEvent(double pTimeout)
 	{
 		Event res = null;
 		boolean tInterrupted = false;
 		int tAttempt = 0;
 		
 		do {
 			if(firstEvent != null) {
 				res = firstEvent;
 				firstEvent = null;
 			} else {
 				if(events != null) {
 					if(!events.isEmpty()) {
 						res = events.removeFirst();
 					}
 				}
 			}
 			if(res == null) {
 				Logging.getInstance().log(this, "Waiting for event for " + pTimeout + " s - attempt " + tAttempt);
 				try {
 					wait((long)(pTimeout * 1000));
 				} catch (InterruptedException exc) {
 					tInterrupted = true;
 				}
 			}
 			tAttempt++;
 		}
		while((res == null) && (!tInterrupted));
 		
 		return res;
 	}
 	
 	private EventSource source;
 	private Event firstEvent;
 	private LinkedList<Event> events;
 	private int numberEvents;
 }

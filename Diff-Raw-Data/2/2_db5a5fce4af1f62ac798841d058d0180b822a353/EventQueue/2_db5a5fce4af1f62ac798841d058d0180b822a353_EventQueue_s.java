 /*
  * This file is part of the OpenNMS(R) Application.
  *
  * OpenNMS(R) is Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
  * OpenNMS(R) is a derivative work, containing both original code, included code and modified
  * code that was published under the GNU General Public License. Copyrights for modified
  * and included code are below.
  *
  * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
  *
  * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
  *
  * For more information contact:
  * OpenNMS Licensing       <license@opennms.org>
  *     http://www.opennms.org/
  *     http://www.opennms.com/
  */
 package org.opennms.opennmsd;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 
 import edu.emory.mathcs.backport.java.util.concurrent.BlockingQueue;
 import edu.emory.mathcs.backport.java.util.concurrent.LinkedBlockingQueue;
 
 /**
  * An EventQueue that handles the differentiation between preserved and only accepted events.
  * 
  * The order of events is as follows m_nextBatch first if not null, then the events
  * in m_preservedQueue. Then any events that exist are in m_queue.
  * 
  * State transition:
  * 
  * Forwarding: 
  *  getEventsToForward() returns a batch of up to maxBatch events from queue
  *  
  *  addEventToPerservedQ:
  *    if (not max'd) add it
  *    if (max'd) clear queue then add it
  *  
  *  if (success) state=Forwarding
  *  if (failure) {
  *      add all perserved events that failed to preserveQ
  *      if (preserveQ is empty) {
  *         state=Forwarding
  *      } else {
  *         state=Failing
  *      }
  *  }
  *  
  * Failing:
  *  addEventToPerserveQ:
  *    if (not max'd) add it
  *    if (max'd) clear nextBatch, clear queue then add it
  *  
  *  // this state should imply that one of nextBatch and preserveQ is not empty
  *  remove all queue events and put pending ones into preserveQ list
  *  
  *  if nextBatch is not filled to maxBatchSize events then add as many events as are available
  *  from the preserveQ to nextBatch
  *
  *  return nextBatch
  *  
 
  *  if (failure) state = Failing
  *  if (success) 
  *    nextBatch.clear
  *    if (preserveQ is empty) state = Forwarding
  *    else state = Recovering
  *    
  *    
  *  Recovering:
  *  addEventToPreserveQ:
  *    if (not max'd) add it
  *    if (max'd) clear nextBatch clear queue then add it
 
  *    // next batch should be empty at the beginning of the call
  *    getEventsToForward() 
         fill nextBatch with as many as possible events from preserveQ
         return nextBatch
  *    
  *    if (success) {
  *       if (preserveQ is empty) { state = Forwarding }
  *       else { state = Recovering }
  *    } 
  *    
  *    if (failure) {
         state = Failing
  *    }
  *    
  * 
  * 
  *
  * @author brozow
  */
 public class EventQueue {
     
     private static Logger log = Logger.getLogger(EventQueue.class);
     
     public abstract class State {
         abstract List getEventsToForward() throws InterruptedException;
         abstract void forwardSuccessful(List events);
         abstract void forwardFailed(List events);
 
         protected void addToPreservedQueue(Event e) {
             if (m_preservedQueue.size() >= m_maxPreservedEvents) {
                 m_nextBatch.clear();
                 m_preservedQueue.clear();
		m_preservedQueue.offer(StatusEvent.createSyncLostEvent());
             }
             m_preservedQueue.offer(e);
         }
         
         protected void discardNonPreservedEvents() {
             List events = new ArrayList(m_queue.size());
             m_queue.drainTo(events);
             
             addPreservedToPreservedQueue(events);
         }
 
         protected void addPreservedToPreservedQueue(List events) {
             for(Iterator it = events.iterator(); it.hasNext(); ) {
                 Event e = (Event)it.next();
                 if (e.isPreserved()) {
                     addToPreservedQueue(e);
                 }
             }
         }
 
         protected void loadNextBatch() {
             m_preservedQueue.drainTo(m_nextBatch, m_maxBatchSize - m_nextBatch.size());
         }
         
     }
     
     private final State FORWARDING = new State() {
 
         public List getEventsToForward() throws InterruptedException {
             List events = new ArrayList(m_maxBatchSize);
             
             Event e = (Event) m_queue.take();
             events.add(e);
             
             m_queue.drainTo(events, m_maxBatchSize - events.size());
             
             return events;
             
         }
         
         public void forwardSuccessful(List events) {
             // no need to do anything here
         }
 
         public void forwardFailed(List events) {
             
             addPreservedToPreservedQueue(events);
             
             if (!m_preservedQueue.isEmpty()) {
                setState(FAILING);
             }
         }
         
         public String toString() { return "FORWARDING"; }
 
 
 
     };
     
     private final State FAILING = new State() {
 
         public List getEventsToForward() {
             discardNonPreservedEvents();
 
             loadNextBatch();
             return m_nextBatch;
         }
 
         public void forwardFailed(List events) {
             // do nothing we are already failing
         }
 
         public void forwardSuccessful(List events) {
             m_nextBatch.clear();
             if (m_preservedQueue.isEmpty()) {
                 setState(FORWARDING);
             } else {
                 setState(RECOVERING);
             }
         }
 
         public String toString() { return "FAILING"; }
         
     };
     
     private final State RECOVERING = new State() {
 
         public List getEventsToForward() {
             loadNextBatch();
             return m_nextBatch;
         }
         
         public void forwardFailed(List events) {
             setState(FAILING);
         }
 
         public void forwardSuccessful(List events) {
             m_nextBatch.clear();
             if (m_preservedQueue.isEmpty()) {
                 setState(FORWARDING);
             }
         }
 
         public String toString() {
             return "RECOVERING"; 
         }
         
     };
     
 
     // operational parameters
     private int m_maxPreservedEvents = 300000;
     private int m_maxBatchSize = 100;
 
     // queue for all events to be forwarded
     private BlockingQueue m_queue = new LinkedBlockingQueue();
     
     // queue for preserving events that are being saved during a forwarding failure
     private BlockingQueue m_preservedQueue = new LinkedBlockingQueue();
     
     // a list of events that are pending due to a forwarding failure
     private List m_nextBatch;
     
     // used to define the behaviour of the getNext and forwardSuccessful and forwardFailed
     private State m_state = FORWARDING;
     
     private void setState(State state) {
         m_state = state;
         log.debug("Setting state of EventQueue to "+m_state);
     }
     
     public int getMaxPreservedEvents() {
         return m_maxPreservedEvents;
     }
 
     public void setMaxPreservedEvents(int maxPreservedEvents) {
         m_maxPreservedEvents = maxPreservedEvents;
     }
 
     public int getMaxBatchSize() {
         return m_maxBatchSize;
     }
 
     public void setMaxBatchSize(int maxBatchSize) {
         m_maxBatchSize = maxBatchSize;
     }
     
     public void init() {
        m_nextBatch = new ArrayList(m_maxBatchSize); 
     }
 
     public void discard(Event e) {
         // do nothing
     }
     
     public void accept(Event e) {
         m_queue.offer(e);
     }
     
     public void preserve(Event e) {
         e.setPreserved(true);
         m_queue.offer(e);
     }
     
     public List getEventsToForward() throws InterruptedException {
         return m_state.getEventsToForward();
     }
     
     public void forwardSuccessful(List events) {
         m_state.forwardSuccessful(events);
     }
     
     public void forwardFailed(List events) {
         m_state.forwardFailed(events);
     }
 
 
 }

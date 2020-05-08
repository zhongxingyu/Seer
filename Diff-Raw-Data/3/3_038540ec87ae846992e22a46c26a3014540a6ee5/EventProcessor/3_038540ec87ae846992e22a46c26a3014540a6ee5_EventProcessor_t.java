 /**
  * Copyright (c) 2012 Daniele Pantaleone, Mathias Van Malderen
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  * 
  * @author      Daniele Pantaleone
  * @version     1.0
  * @copyright   Daniele Pantaleone, 4 September, 2013
  * @package     com.orion.misc
  **/
 
 package com.orion.misc;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.Collection;
 import java.util.concurrent.BlockingQueue;
 
 import org.apache.commons.logging.Log;
 import org.joda.time.DateTime;
 
 import com.google.common.collect.Multimap;
 import com.orion.event.Event;
 import com.orion.exception.EventInterruptedException;
 import com.orion.misc.RegisteredMethod;
 import com.orion.plugin.Plugin;
 
 public class EventProcessor implements Runnable {
     
     private final Log log;
     private BlockingQueue<Event> eventBus;
     private Multimap<Class<?>, RegisteredMethod> regMethod;
     
     
     /**
      * Object constructor
      * 
      * @author Daniele Pantaleone
      * @param  log Main logger object reference
      * @param  eventBus A <tt>BlockingQueue</tt> from where to fetch events
     * @param  regMethod A <tt>Multimap</tt> which associate each <tt>Event</tt> to a method
      **/
     public EventProcessor(Log log, 
                           BlockingQueue<Event> eventBus, 
                           Multimap<Class<?>, RegisteredMethod> regMethod) {
         
         this.log = log;
         this.eventBus = eventBus;
         this.regMethod = regMethod;
         
         this.log.debug("Event processor initialized: " + this.regMethod.size() + " events registered");
         
     }
     
     
     /**
      * Runnable implementation<br>
      * Will iterate throught all the events stored by the parser in the queue
      * It peeks an <tt>Event</tt> from the queue and process it over all
      * the mapped <tt>Methods</tt> retrieved from the registered events map
      * 
      * @author Daniele Pantaleone
      **/
     @Override
     public void run() {
         
         this.log.debug("Event processor started: " + new DateTime().toString());
         
         while (true) {
             
             try {
                 
                 if (Thread.interrupted()) {
                     throw new InterruptedException();
                 }
                 
                 Event event = this.eventBus.take();
                 Collection<RegisteredMethod> collection = this.regMethod.get(event.getClass());
                 
                 // Skip if this event is not mapped over any method
                 if ((collection == null) || (collection.size() == 0)) {
                     continue;
                 }
                 
                 // Iterating over all the RegisteredEvent
                 for (RegisteredMethod r : collection) {
                     
                     try {
                         
                         Method method = r.getMethod();
                         Plugin plugin = r.getPlugin();
                         
                         if (!plugin.isEnabled()) {
                             continue;
                         }
                         
                         method.invoke(plugin, event);
                     
                     } catch (InvocationTargetException | IllegalAccessException | IllegalArgumentException e) {
                         
                         // A plugin requested to stop processing this event so
                         // we'll not iterate through the remaining event handlers
                         if (e.getCause().getClass().equals(EventInterruptedException.class)) {
                             continue;
                         }
                         
                         // Logging the Exception and keep processing events anyway
                         this.log.error("[" + r.getPlugin().getClass().getSimpleName() + "] Could not process event " + event.getClass().getSimpleName(), e);
                         continue;
                         
                     }
                     
                 }
                     
     
             } catch (InterruptedException e) {
                 
                 // Thread has received interrupt signal
                 // Breaking the cycle so it will terminate
                 break;
             
             }
         
         }
         
         this.log.debug("Event processor stopped: " + new DateTime().toString());
     
     }
 
 }

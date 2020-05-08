 /*
  * Spokela Events Dispatcher Utility
  *
  * Copyright (c) 2013-2014, Julien Ballestracci <julien@nitronet.org>.
  * All rights reserved.
  *
  * For the full copyright and license information, please view the LICENSE
  * file that was distributed with this source code.
  *
  * @author    Julien Ballestracci <julien@nitronet.org>
  * @copyright 2013-2014 Julien Ballestracci <julien@nitronet.org>
  * @license   http://www.opensource.org/licenses/bsd-license.php  BSD License
  * @link      http://www.spokela.com
  */
 package com.spokela.events;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.HashMap;
 import java.util.LinkedList;
 
 /**
  * Dispatcher
  * 
  * This class is an Event Dispatcher.
  * It can be herited by other classes or used as-is.
  * 
  * @author neiluj
  */
 public class Dispatcher {
     protected LinkedList<Object> listeners = new LinkedList();
     
     public Dispatcher addListener(final Object listener)
     {
         this.listeners.add(listener);
         return this;
     }
     
     public Dispatcher removeListener(final Object listener)
     {
         if (this.listeners.contains(listener)) {
             this.listeners.remove(listener);
         }
         
         return this;
     }
     
     public Dispatcher notify(final Event event) {
         // doing the ucfirst stuff
         String methodName = "on"+ event.getName().substring(0, 1).toUpperCase() + 
                 event.getName().substring(1);
         
         Method delegate;
         
         for (final Object listener : this.listeners) {
             try {
                delegate = listener.getClass().getMethod(methodName, new Class[]{event.getClass()});
                 delegate.invoke(listener, event);
                 
                 if (event.isStopped()) {
                     break;
                 }
             }
             catch (SecurityException 
                 | NoSuchMethodException | IllegalAccessException 
                 | IllegalArgumentException | InvocationTargetException e) {
             }
         }
         
         return this;
     }
     
     public Dispatcher notify(final String eventName) {
         return this.notify(new Event(eventName));
     }
     
     public Dispatcher notify(final String eventName, final HashMap<String, Object> eventData) {
         return this.notify(new Event(eventName, eventData));
     }
 }

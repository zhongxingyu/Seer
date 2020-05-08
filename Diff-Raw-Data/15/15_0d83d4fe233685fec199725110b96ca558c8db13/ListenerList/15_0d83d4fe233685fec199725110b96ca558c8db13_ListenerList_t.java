 /*
  * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes
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
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 
 package com.dmdirc.util;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 /**
  * Represents a list of event listeners, similar to EventListenerList, but
  * not swing specific.
  * 
  * @author chris
  */
 public class ListenerList {
     
     /** The map of class->listener or string->listener that we're using. */
     private final Map<Object, Collection<Object>> listeners
             = new HashMap<Object, Collection<Object>>();
     
     /**
      * Creates a new instance of ListenerList.
      */
     public ListenerList() {
         // Do nothing
     }
     
     /**
      * Adds a new listener of the specified type to this listener list.
      * 
     * @param <T> The type of listener to be added
      * @param listenerType The type of listener to be added
      * @param listener The listener to be added
      */
     public <T> void add(final Class<T> listenerType, final T listener) {
         if (!listeners.containsKey(listenerType)) {
             listeners.put(listenerType, new CopyOnWriteArrayList<Object>());
         }
 
         listeners.get(listenerType).add(listener);
     }
     
     /**
      * Adds a new listener of the specified type to this listener list.
      * 
      * @param listenerType The name of the type of listener that's being added
      * @param listener The listener to be added
      */
     public void add(final String listenerType, final Object listener) {
         if (!listeners.containsKey(listenerType)) {
             listeners.put(listenerType, new CopyOnWriteArrayList<Object>());
         }
 
         listeners.get(listenerType).add(listener);
     }
     
     /**
      * Removes the specified listener from the list of listeners for the
      * specified type.
     *
     * @param <T> The type of listener to be removed
      * @param listenerType The type that the listener should be removed from
      * @param listener The listener to be removed
      */
     public <T> void remove(final Class<T> listenerType, final T listener) {
        get(listenerType).remove(listener);
     }
     
     /**
      * Removes the specified listener from the list of listeners for the
      * specified type.
      * 
      * @param listenerType The name of the type that the listener should be
      * removed from
      * @param listener The listener to be removed
      */
     public void remove(final String listenerType, final Object listener) {
        get(listenerType).remove(listener);
     }
     
     /**
      * Retrieves the list of listeners for the specified type.
     *
     * @param <T> The type of listener to be retrieved
      * @param listenerType The type of listener that's being retrieved
      * @return A list of listeners for the specified type
      */
     @SuppressWarnings("unchecked")
     public <T> Collection<T> get(final Class<T> listenerType) {
         if (listeners.containsKey(listenerType)) {
             return (Collection<T>) listeners.get(listenerType);
         } else {
             return new CopyOnWriteArrayList<T>();
         }
     }
     
     /**
      * Retrieves the list of listeners for the specified type.
      * 
      * @param listenerType The type of listener to be retrieved
      * @return A list of listeners for the specified type
      */
     public Collection<Object> get(final String listenerType) {
         if (listeners.containsKey(listenerType)) {
             return listeners.get(listenerType);
         } else {
             return new CopyOnWriteArrayList<Object>();
         }
     }
 
 }

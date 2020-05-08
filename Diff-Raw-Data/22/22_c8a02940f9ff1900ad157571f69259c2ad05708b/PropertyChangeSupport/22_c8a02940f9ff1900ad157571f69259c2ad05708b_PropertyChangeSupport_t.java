 /*
  * Copyright (C) 2003-2011 eXo Platform SAS.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 package org.exoplatform.social.client.core.util;
 
 
 import org.exoplatform.social.client.api.event.PropertyChangeEvent;
 import org.exoplatform.social.client.api.event.PropertyChangeListener;
 import org.exoplatform.social.client.api.model.Model;
 
 /**
  * This is a utility class that can be used by models that support bound
  * properties.  You can use an instance of this class as a member field
  * of your bean and delegate various work to it.
  *
  * 
  * @author thanh_vucong
  *
  */
 public final class PropertyChangeSupport {
 
   /**
    * The source bean for property change events that we will broadcast.
    */
   private Model source = null;
   /**
    * The set of registered PropertyChangeListeners for event notifications.
    */
   private PropertyChangeListener listeners[] = new PropertyChangeListener[0];
   
   /**
    * Lock object for change to listeners
    */
   private final Object listenersLock = new Object();
   
   
   /**
    * Constructs a new <code>PropertyChangeSupport</code> object associated with the specified Model object.
    *
    * @param source
    */
   public PropertyChangeSupport(Model source) {
     this.source = source;
   }
   
   /**
    * Adds a PropertyChange event listener to this component.
    *
    * @param listener The listener is added.
    */
   public void addPropertyChangeListener(PropertyChangeListener listener) {
     
     synchronized(listenersLock) {
       PropertyChangeListener results[] = new PropertyChangeListener[listeners.length + 1];
      System.arraycopy(listeners, 0, results, 0, listeners.length);
       //Add the PropertyChangeListener to the new position.    
       results[listeners.length] = listener;
       listeners = results;
     }
   }
   
   /**
    * Notifies all PropertyChange event listeners that a particular event has
    * occurred for this Container. The default implementation performs 
    * this notification synchronously using the calling thread.
    * 
    * No event is fired if old and new are equal and non-null.
    * 
    * @param propertyName The PropertyName string
    * @param oldValue oldValue object.
    * @param newValue newValue object.
    */
   public void propertyChange(String propertyName, Object oldValue, Object newValue) {
     
     if (oldValue != null && newValue != null && oldValue.equals(newValue)) {
       return;
     }
     PropertyChangeEvent event = new PropertyChangeEvent(this.source, propertyName, oldValue, newValue);
     PropertyChangeListener interested[] = listeners;
     for (int i = 0; i < interested.length; i++) {
       interested[i].propertyChange(event);
     }
   }
   
   /**
    * Removes a PropertyChange event listener which was registered to model
    * @param listener The listener will be removed.
    */
   public void removeLifecycleListener(PropertyChangeListener listener) {
     synchronized (listenersLock) {
       int n = -1;
       for (int i = 0; i < listeners.length; i++) {
         if (listeners[i] == listener) {
           n = i;
           break;
         }
       }
       //not found any listener in Listeners
       if (n < 0) {
         return;
       }
 
       /**
        * Execute to remove the listener
        */
       PropertyChangeListener results[] = new PropertyChangeListener[listeners.length - 1];
       int j = 0;
       for (int i = 0; i < listeners.length; i++) {
         if (i != n) {
           results[j++] = listeners[i];
         }
       }
       listeners = results;
     }
   }
   
   /**
    * Gets the lifecycle listeners associated with this lifecycle. If this
    * Lifecycle has no listeners registered, a zero-length array is returned.
    */
   public PropertyChangeListener[] findPropertyChangeListeners() {
       return listeners;
   }
   
 }

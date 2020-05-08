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
 
 import org.exoplatform.social.client.api.event.Lifecycle;
 import org.exoplatform.social.client.api.event.LifecycleEvent;
 import org.exoplatform.social.client.api.event.LifecycleListener;
 
 /**
  * 
  * This is a utility class that can be used by models that assist
  * in firing LifecycleEvent notifications to registered LifecycleListeners
  * and delegate various work to it
  * 
  * @author thanh_vucong
  *
  */
 public final class LifecycleSupport<M, L> {
 
   /**
    * The source component for lifecycle events that we will broadcast.
    */
   private Lifecycle<M,L> lifecycle = null;
   
   /**
    * The set of registered LifecycleListeners for event notifications.
    */
   private LifecycleListener<M,L> listeners[] = new LifecycleListener[0];
   
   /**
    * Locks object for change to listeners
    */
   private final Object listenersLock = new Object();
   
   /**
    * Constructs a new LifecycleHelper object associated with the specified Lifecycle component.
    * @param lifecycle
    */
   public LifecycleSupport(Lifecycle<M,L> lifecycle) {
     this.lifecycle = lifecycle;
   }
   
   
   
   /**
    * Adds a Lifecycle event listener to this component.
    *
    * @param listener The listener is added.
    */
   public void addLifecycleListener(LifecycleListener<M,L> listener) {
     
     synchronized(listenersLock) {
       LifecycleListener<M,L> results[] = new LifecycleListener[listeners.length + 1];
      for (int i = 0; i < listeners.length; i++) {
        results[i] = listeners[i];
      }
       //Add the LifecycleListener to the new position.    
       results[listeners.length] = listener;
       listeners = results;
     }
   }
   
   /**
    * Notifies all Lifecycle event listeners that a particular event has
    * occurred for this Container. The default implementation performs 
    * this notification synchronously using the calling thread.
    * 
    * @param type Event type
    * @param data Event data
    */
   public void broadcastEvent(String type, L data) {
     LifecycleEvent<M,L> event = new LifecycleEvent<M,L>(this.lifecycle, type, data);
     LifecycleListener<M,L> interested[] = listeners;
     for (int i = 0; i < interested.length; i++) {
       interested[i].broadcast(event);
     }
   }
   
   /**
    * Removes a lifecycle event listener which was registered to component.
    *
    * @param listener The listener will be removed.
    */
   public void removeLifecycleListener(LifecycleListener<M, L> listener) {
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
 
 
       //Execute to remove the listener
       LifecycleListener<M, L> results[] = new LifecycleListener[listeners.length - 1];
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
   public LifecycleListener<M, L>[] findLifecycleListeners() {
 
     return listeners;
 
   }
   
   
 }

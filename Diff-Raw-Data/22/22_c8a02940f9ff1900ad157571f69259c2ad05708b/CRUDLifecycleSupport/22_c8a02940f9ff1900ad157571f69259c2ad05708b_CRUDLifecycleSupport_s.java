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
 
 import org.exoplatform.social.client.api.event.CRUDLifecycle;
 import org.exoplatform.social.client.api.event.CRUDLifecycleEvent;
 import org.exoplatform.social.client.api.event.CRUDLifecycleListener;
 
 /**
  * 
  *  This is a utility class that can be used by models that assist 
  *  in firing CRUDLifecycleEvent notifications to registered CRUDLifecycleListeners 
  *  and delegate various work to it
  * 
  * @author thanh_vucong
  *
  */
 public class CRUDLifecycleSupport<M> {
   /**
    * The source component for crud lifecycle events that we will broadcast.
    */
   private CRUDLifecycle<M> crudlifecycle = null;
   
   /**
    * The set of registered LifecycleListeners for event notifications.
    */
   private CRUDLifecycleListener<M> listeners[] = new CRUDLifecycleListener[0];
   
   /**
    * Lock object for change to listeners
    */
   private final Object listenersLock = new Object();
   
   /**
    * Constructs a new LifecycleHelper object associated with the specified Lifecycle component.
    * @param crudlifecycle
    */
   public CRUDLifecycleSupport(CRUDLifecycle<M> crudlifecycle) {
     this.crudlifecycle = crudlifecycle;
   }
   
   
   
   /**
    * Add a Lifecycle event listener to this component
    * @param listener The listener is added.
    */
   public void addCRUDLifecycleListener(CRUDLifecycleListener<M> listener) {
     
     synchronized(listenersLock) {
       CRUDLifecycleListener<M> results[] = new CRUDLifecycleListener[listeners.length + 1];
      for (int i = 0; i < listeners.length; i++) {
        results[i] = listeners[i];
      }
       //Add the LifecycleListener to the new position.    
       results[listeners.length] = listener;
       listeners = results;
     }
   }
   
   /**
    * Notify all CRUDLifecycle event listeners that a particular event has 
    * occurred for this Container. The default implementation performs 
    * this notification synchronously using the calling thread.
    * 
    * @param type Event type
    * @param data Event data
    */
   public void broadcastEvent(String type, M data) {
     CRUDLifecycleEvent<M> event = new CRUDLifecycleEvent<M>(this.crudlifecycle, type, data);
     CRUDLifecycleListener<M> interested[] = listeners;
     for (int i = 0; i < interested.length; i++) {
       interested[i].broadcast(event);
     }
   }
   
   /**
    * Remove a CRUDLifecycle event listener which was registered to component
    * @param listener The listener will be removed.
    */
   public void removeCRUDLifecycleListener(CRUDLifecycleListener<M> listener) {
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
       CRUDLifecycleListener<M> results[] = new CRUDLifecycleListener[listeners.length - 1];
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
    * Get the crud lifecycle listeners associated with this lifecycle. If this 
    * crud Lifecycle has no listeners registered, a zero-length array is returned.
    */
   public CRUDLifecycleListener<M>[] findCRUDLifecycleListeners() {
       return listeners;
   }
 
 }

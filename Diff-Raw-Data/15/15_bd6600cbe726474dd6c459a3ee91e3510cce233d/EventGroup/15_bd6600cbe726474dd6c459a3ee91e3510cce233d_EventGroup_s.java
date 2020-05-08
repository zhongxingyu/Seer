 /*
  * Copyright (C) 2013 Pauli Kauppinen
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 package org.javnce.eventing;
 
 import java.lang.ref.WeakReference;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 /**
  * The Class EventGroup is a event dispatcher container class. EventGroup
  * contains event dispatcher and child groups. EventGroup is thread safe.
  */
public class EventGroup {
 
     /**
      * The synchronization lock.
      */
     final private Object lock;
     /**
      * The set of EventDispatcher reference.
      */
     final private Set<WeakReference<EventDispatcher>> refs;
     /**
      * The set of child groups.
      */
     final private Set<EventGroup> children;
     /**
      * The parent.
      */
     private EventGroup parent;
     /**
      * The root group.
      */
     static final private EventGroup root = new EventGroup();
 
     /**
      * Instantiates a new event group.
      */
     private EventGroup() {
         lock = new Object();
         refs = new HashSet<>();
         children = new HashSet<>();
         parent = null;
     }
 
     /**
      * The root instance getter.
      *
      * @return the root group
      */
    public static EventGroup instance() {
         return root;
     }
 
     /**
      * Find given dispatcher from set.
      *
      * @param dispatcher the dispatcher
      * @return the weak reference to null or found object
      */
     private WeakReference<EventDispatcher> find(EventDispatcher dispatcher) {
         WeakReference<EventDispatcher> ref = new WeakReference<>(null);
         if (null != dispatcher) {
             synchronized (lock) {
                 for (Iterator<WeakReference<EventDispatcher>> i = refs.iterator(); i.hasNext();) {
                     WeakReference<EventDispatcher> temp = i.next();
                     if (temp.get() == dispatcher) {
                         ref = temp;
                         break;
                     }
                 }
             }
         }
         return ref;
     }
 
     /**
      * Adds a dispatcher into set.
      *
      * @param dispatcher the dispatcher
      */
     void add(EventDispatcher dispatcher) {
         if (null != dispatcher) {
             WeakReference<EventDispatcher> ref = find(dispatcher);
             if (null == ref.get()) {
                 synchronized (lock) {
                     refs.add(new WeakReference<>(dispatcher));
                 }
             }
         }
         refresh();
     }
 
     /**
      * Removes the dispatcher from set.
      *
      * @param dispatcher the dispatcher
      */
     void remove(EventDispatcher dispatcher) {
         WeakReference<EventDispatcher> ref = find(dispatcher);
         if (null != ref.get()) {
             synchronized (lock) {
                 refs.remove(ref);
             }
         }
         refresh();
     }
 
     /**
      * Checks if group has now dispatchers or child groups.
      *
      * @return true, if is empty
      */
     boolean isEmpty() {
         boolean empty = false;
         synchronized (lock) {
             if (refs.isEmpty() && children.isEmpty()) {
                 empty = true;
             }
         }
         return empty;
     }
 
     /**
      * Request Shutdown of all child groups and dispatcher.
      */
    public void shutdown() {
 
         //Loop all children
         for (Iterator<EventGroup> i = childrenList().iterator(); i.hasNext();) {
             i.next().shutdown();
         }
 
         //Loop all refs
         for (Iterator<WeakReference<EventDispatcher>> i = refsList().iterator(); i.hasNext();) {
             WeakReference<EventDispatcher> ref = i.next();
             EventDispatcher dispatcher = ref.get();
             if (null != dispatcher) {
                 dispatcher.shutdown(this);
             }
         }
         refresh();
 
     }
 
     /**
      * Snapshot of children.
      *
      * @return the child groups in a list
      */
     List<EventGroup> childrenList() {
         ArrayList<EventGroup> list = null;
         synchronized (lock) {
             list = new ArrayList<>(children);
         }
         return list;
     }
 
     /**
      * Snapshot of dispatchers.
      *
      * @return the list of dispatchers in the group
      */
     List<WeakReference<EventDispatcher>> refsList() {
         ArrayList<WeakReference<EventDispatcher>> list = null;
         synchronized (lock) {
             list = new ArrayList<>(refs);
         }
         return list;
     }
 
     /**
      * Goes through all child groups and dispatchers and checks the weak
      * reference.
      */
     private void refresh() {
         //Refresh all children
         for (Iterator<EventGroup> i = childrenList().iterator(); i.hasNext();) {
             i.next().refresh();
         }
         synchronized (lock) {
             //Loop all refs
             for (Iterator<WeakReference<EventDispatcher>> i = refs.iterator(); i.hasNext();) {
                 EventDispatcher dispatcher = i.next().get();
                 if (null == dispatcher) {
                     i.remove();
                 }
             }
         }
         //If this is empty then remove from parent (if any)
         if (isEmpty()) {
             EventGroup temp = parent();
             if (null != parent) {
                 temp.remove(this);
             }
         }
     }
 
     /**
      * Removes given child from group.
      *
      * @param child the child
      */
     private void remove(EventGroup child) {
         if (null != child) {
             synchronized (lock) {
                 child.parent = null;
                 children.remove(child);
             }
         }
         refresh();
     }
 
     /**
      * Creates child and moves dispatcher into created child.
      *
      * @param dispatcher the dispatcher to be moved
      * @return the new child group
      */
     EventGroup moveToNewChild(EventDispatcher dispatcher) {
         EventGroup child = null;
         if (null != dispatcher) {
             synchronized (lock) {
                 child = new EventGroup();
                 child.parent = this;
                 remove(dispatcher);
                 child.add(dispatcher);
                 children.add(child);
             }
         }
         refresh();
         return child;
     }
 
     /**
      * Publish an event. If dispatchers in group does not support the event then
      * it is broadcasted to root object.
      *
      * @param event the published event
      */
     void publish(Event event) {
         if (false == dispatch(event)) {
             instance().broadcast(event);
         }
     }
 
     /**
      * Dispatch event to all dispatchers within group.
      *
      * @param event the published event
      * @return true, if dispatchers did handle it
      */
     private boolean dispatch(Event event) {
         boolean subscribed = false;
 
         for (Iterator<WeakReference<EventDispatcher>> i = refsList().iterator(); i.hasNext();) {
             EventDispatcher dispatcher = i.next().get();
             if (null != dispatcher && dispatcher.dispatchEvent(event)) {
                 subscribed = true;
             }
         }
 
         return subscribed;
     }
 
     /**
      * Broadcast event to all child groups and dispatchers.
      *
      * @param event the published event
      */
     private void broadcast(Event event) {
         dispatch(event);
 
         for (Iterator<EventGroup> i = childrenList().iterator(); i.hasNext();) {
             i.next().dispatch(event);
         }
     }
 
     /**
      * Parent.
      *
      * @return the parent group
      */
     private EventGroup parent() {
         EventGroup temp = null;
 
         synchronized (lock) {
             temp = parent;
         }
         return temp;
     }
 }

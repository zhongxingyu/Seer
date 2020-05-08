 /**
  * Copyright (C) 2009 (nick @ objectdefinitions.com)
  *
  * This file is part of JTimeseries.
  *
  * JTimeseries is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * JTimeseries is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with JTimeseries.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.od.jtimeseries.source.impl;
 
 import com.od.jtimeseries.source.EventTimer;
 import com.od.jtimeseries.source.QueueTimer;
 import com.od.jtimeseries.source.ValueSourceListener;
 import com.od.jtimeseries.util.identifiable.Identifiable;
import com.od.jtimeseries.util.identifiable.IdentifiableTreeListener;
 
 import java.util.List;
 import java.util.Properties;
 import java.util.concurrent.locks.ReentrantReadWriteLock;
 
 /**
  *
  */
 public class DefaultQueueTimer implements QueueTimer {
 
     private Object queuedTimeEvent;
     private final Object internalLock = new Object();
     private EventTimer timingSource;
 
     public DefaultQueueTimer(String id, String description, ValueSourceListener... sourceDataListeners) {
         timingSource = new DefaultEventTimer(id, description, sourceDataListeners);
     }
 
     public void addValueListener(ValueSourceListener sourceDataListener) {
         timingSource.addValueListener(sourceDataListener);
     }
 
     public void removeValueListener(ValueSourceListener sourceDataListener) {
         timingSource.removeValueListener(sourceDataListener);
     }
 
     public void objectAddedToQueue(Object theEvent) {
         synchronized (internalLock) {
             if ( queuedTimeEvent == null ) {
                 queuedTimeEvent = theEvent;
                 timingSource.startEventTimer();
             }
         }
     }
 
     public void objectRemovedFromQueue(Object event) {
         synchronized (internalLock) {
             if ( queuedTimeEvent == event) {
                 queuedTimeEvent = null;
                 timingSource.stopEventTimer();
             }
         }
     }
 
     public String getId() {
         return timingSource.getId();
     }
 
     public String getParentPath() {
         return timingSource.getParentPath();
     }
 
     public String getPath() {
         return timingSource.getPath();
     }
 
     public Identifiable getParent() {
         return timingSource.getParent();
     }
 
     public Identifiable setParent(Identifiable parent) {
         return timingSource.setParent(parent);
     }
 
     public ReentrantReadWriteLock getContextLock() {
         return timingSource.getContextLock();
     }
 
     public Identifiable getRoot() {
         return timingSource.getRoot();
     }
 
     public boolean isRoot() {
         return timingSource.isRoot();
     }
 
     public Identifiable get(String id) {
         return timingSource.get(id);
     }
 
     public <E extends Identifiable> E get(String id, Class<E> classType) {
         return timingSource.get(id, classType);
     }
 
     public <E extends Identifiable> E create(String id, String description, Class<E> clazz, Object... parameters) {
         return timingSource.create(id, description, clazz, parameters);
     }
 
     public boolean containsChildWithId(String id) {
         return timingSource.containsChildWithId(id);
     }
 
     public boolean containsChild(Identifiable i) {
         return timingSource.containsChild(i);
     }
 
     public String getDescription() {
         return timingSource.getDescription();
     }
 
     public void setDescription(String description){
         timingSource.setDescription(description);
     }
 
     public List<Identifiable> getChildren() {
         return timingSource.getChildren();
     }
 
     public <E extends Identifiable> List<E> getChildren(Class<E> classType) {
         return timingSource.getChildren(classType);
     }
 
     public boolean removeChild(Identifiable c) {
         return timingSource.removeChild(c);
     }
     
     public String getProperty(String propertyName) {
         return timingSource.getProperty(propertyName);
     }
 
     public String findProperty(String propertyName) {
         return timingSource.findProperty(propertyName);
     }
 
     public String setProperty(String propertyName, String value) {
         return timingSource.setProperty(propertyName, value);
     }
 
     public Identifiable addChild(Identifiable... identifiables) {
         return timingSource.addChild(identifiables);
     }
 
     public <E extends Identifiable> E getFromAncestors(String id, Class<E> clazz) {
         return timingSource.getFromAncestors(id, clazz);
     }
 
     public Properties getProperties() {
         return timingSource.getProperties();
     }
 
     public void putAllProperties(Properties p) {
         timingSource.putAllProperties(p);
     }
 
     public Identifiable remove(String path) {
         return timingSource.remove(path);
     }
 
     public <E extends Identifiable> E remove(String path, Class<E> classType) {
         return timingSource.remove(path, classType);
     }

    public boolean addTreeListener(IdentifiableTreeListener l) {
        return timingSource.addTreeListener(l);
    }

    public boolean removeTreeListener(IdentifiableTreeListener l) {
        return timingSource.removeTreeListener(l);
    }
 }

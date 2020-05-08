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
 
 import com.od.jtimeseries.source.Counter;
 import com.od.jtimeseries.source.ValueSourceListener;
 import com.od.jtimeseries.util.identifiable.Identifiable;
 
 import java.util.List;
 import java.util.Properties;
 import java.util.concurrent.atomic.AtomicLong;
 import java.util.concurrent.locks.ReentrantReadWriteLock;
 
 /**
  * Created by IntelliJ IDEA.
  * User: Nick Ebbutt
  * Date: 05-Dec-2008
  * Time: 14:05:55
  */
 public class DefaultCounter implements Counter {
 
     private DefaultValueRecorder simpleSource;
     private final AtomicLong currentValue = new AtomicLong();
 
     public DefaultCounter(String id, String description, ValueSourceListener... sourceDataListeners) {
         simpleSource = new DefaultValueRecorder(id, description, sourceDataListeners);
     }
 
     public void addValueListener(ValueSourceListener sourceDataListener) {
         simpleSource.addValueListener(sourceDataListener);
     }
 
     public void removeValueListener(ValueSourceListener sourceDataListener) {
         simpleSource.removeValueListener(sourceDataListener);
     }
 
     public void incrementCount() {
         simpleSource.newValue(currentValue.incrementAndGet());
     }
 
     public void decrementCount() {
         simpleSource.newValue(currentValue.decrementAndGet());
     }
 
     public void reset() {
         currentValue.set(0);
     }
 
     public String getId() {
         return simpleSource.getId();
     }
 
     public String getParentPath() {
         return simpleSource.getParentPath();
     }
 
     public String getPath() {
         return simpleSource.getPath();
     }
 
     public String getDescription() {
         return simpleSource.getDescription();
     }
 
     public void setDescription(String description) {
         simpleSource.setDescription(description);
     }
 
     public Identifiable getParent() {
         return simpleSource.getParent();
     }
 
     public Identifiable setParent(Identifiable parent) {
         return simpleSource.setParent(parent);
     }
 
     public List<Identifiable> getChildren() {
         return simpleSource.getChildren();
     }
 
     public <E extends Identifiable> List<E> getChildren(Class<E> classType) {
         return simpleSource.getChildren(classType);
     }
 
     public boolean removeChild(Identifiable c) {
         return simpleSource.removeChild(c);
     }
 
     public ReentrantReadWriteLock getContextLock() {
         return simpleSource.getContextLock();
     }
 
     public Identifiable getRoot() {
         return simpleSource.getRoot();
     }
 
     public boolean isRoot() {
         return simpleSource.isRoot();
     }
 
     public Identifiable get(String id) {
         return simpleSource.get(id);
     }
 
     public <E extends Identifiable> E get(String id, Class<E> classType) {
         return simpleSource.get(id, classType);
     }
 
     public <E extends Identifiable> E create(String id, String description, Class<E> clazz, Object... parameters) {
         return simpleSource.create(id, description, clazz, parameters);
     }
 
     public boolean containsChildWithId(String id) {
         return simpleSource.containsChildWithId(id);
     }
 
     public boolean containsChild(Identifiable i) {
         return simpleSource.containsChild(i);
     }
 
     public String setProperty(String propertyName, String value) {
         return simpleSource.setProperty(propertyName, value);
     }
 
     public Identifiable addChild(Identifiable... identifiables) {
         return simpleSource.addChild(identifiables);
     }
 
     public <E extends Identifiable> E getFromAncestors(String id, Class<E> clazz) {
         return simpleSource.getFromAncestors(id, clazz);
     }
 
     public String findProperty(String propertyName) {
         return simpleSource.findProperty(propertyName);
     }
 
     public String getProperty(String propertyName) {
         return simpleSource.getProperty(propertyName);
     }
 
     public Properties getProperties() {
         return simpleSource.getProperties();
     }
 
     public void putAllProperties(Properties p) {
         simpleSource.putAllProperties(p);
     }
 
     public String toString() {
         return simpleSource.toString();
     }
 
     public Identifiable remove(String path) {
         return simpleSource.remove(path);
     }
 
     public <E extends Identifiable> E remove(String path, Class<E> classType) {
         return simpleSource.remove(path, classType);
     }
 
 
 }

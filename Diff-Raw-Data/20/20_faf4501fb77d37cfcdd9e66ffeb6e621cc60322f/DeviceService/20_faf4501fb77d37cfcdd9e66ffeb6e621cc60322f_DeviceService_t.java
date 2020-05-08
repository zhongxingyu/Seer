 /*
  * Copyright (C) 2011 Pixmob (http://github.com/pixmob)
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.pixmob.droidlink.gae.service;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Set;
 
 import com.google.common.collect.Iterables;
 import com.google.inject.Inject;
 import com.googlecode.objectify.Key;
 import com.googlecode.objectify.Objectify;
 import com.googlecode.objectify.ObjectifyFactory;
 
 /**
  * Business service for managing devices.
  * @author Pixmob
  */
 public class DeviceService {
     private final ObjectifyFactory of;
     
     /**
      * Package protected constructor: use Guice to get an instance of this
      * class.
      */
     @Inject
     DeviceService(final ObjectifyFactory of) {
         this.of = of;
     }
     
     public Device getDevice(String user, String deviceId) throws DeviceNotFoundException,
             AccessDeniedException {
         checkNotNull(user, "User is required");
         
         final Device device = of.begin().find(Device.class, deviceId);
         if (device == null) {
             throw new DeviceNotFoundException(deviceId);
         }
         if (!user.equals(device.user)) {
             throw new AccessDeniedException();
         }
         return device;
     }
     
     public Event getEvent(String user, String deviceId, String eventId)
             throws DeviceNotFoundException, EventNotFoundException, AccessDeniedException {
         checkNotNull(user, "User is required");
         checkNotNull(deviceId, "Device identifier is required");
         
         final Objectify session = of.begin();
         final Key<Device> deviceKey = new Key<Device>(Device.class, deviceId);
         final Device device = session.find(deviceKey);
         if (device == null) {
             throw new DeviceNotFoundException(deviceId);
         }
         if (!user.equals(device.user)) {
             throw new AccessDeniedException();
         }
         
         final Event event = session.find(new Key<Event>(deviceKey, Event.class, eventId));
         if (event == null) {
             throw new EventNotFoundException(eventId);
         }
         
         return event;
     }
     
     public Iterable<Event> getEvents(String user, String deviceId) throws DeviceNotFoundException,
             AccessDeniedException {
         checkNotNull(user, "User is required");
         
         final Objectify session = of.begin();
         if (deviceId != null) {
             // Get events from this device.
             final Device device = session.find(Device.class, deviceId);
             if (device == null) {
                 throw new DeviceNotFoundException(deviceId);
             }
             if (!user.equals(device.user)) {
                 throw new AccessDeniedException();
             }
             return session.query(Event.class).ancestor(device);
         }
         
         // Get events from every user device.
         final Set<Iterable<Event>> eventsByDevice = new HashSet<Iterable<Event>>(4);
         final Iterable<Key<Device>> deviceKeys = session.query(Device.class).filter("user", user)
                 .fetchKeys();
         for (final Key<Device> deviceKey : deviceKeys) {
             eventsByDevice.add(session.query(Event.class).ancestor(deviceKey));
         }
         
         return Iterables.concat(eventsByDevice);
     }
     
     public Event deleteEvent(String user, String eventId) throws AccessDeniedException {
         checkNotNull(user, "User is required");
         
         final Objectify session = of.begin();
        for (final Key<Device> deviceKey : session.query(Device.class).fetchKeys()) {
            // Find the device which is linked to this event.
            final Event event = session.find(new Key<Event>(deviceKey, Event.class, eventId));
            if (event != null) {
                final Device device = session.find(event.device);
                if (device != null && !user.equals(device.user)) {
                    throw new AccessDeniedException();
                }
                session.delete(event);
                
                return event;
             }
         }
         
         return null;
     }
     
     public void deleteEvents(String user) {
         checkNotNull(user, "User is required");
         
         final Objectify session = of.begin();
         for (final Key<Device> deviceKey : session.query(Device.class).filter("user", user)
                 .fetchKeys()) {
             final Iterable<Key<Event>> eventKeys = session.query(Event.class).ancestor(deviceKey)
                     .fetchKeys();
             session.delete(eventKeys);
         }
     }
     
     public Iterable<Device> getDevices(String user) {
         checkNotNull(user, "User is required");
         return of.begin().query(Device.class).filter("user", user);
     }
     
     public Iterable<String> getRegisteredUsers() {
         final Set<String> users = new HashSet<String>(128);
         for (final Device device : of.begin().query(Device.class)) {
             users.add(device.user);
         }
         
         return Collections.unmodifiableCollection(users);
     }
     
     public Device registerDevice(String user, String deviceId, String deviceName, String c2dm)
             throws AccessDeniedException {
         checkNotNull(user, "User is required");
         checkNotNull(deviceId, "Device identifier is required");
         
         final Objectify session = of.beginTransaction();
         Device device = null;
         try {
             device = session.find(new Key<Device>(Device.class, deviceId));
             if (device == null) {
                 device = new Device();
                 device.id = deviceId;
                 device.user = user;
             } else {
                 if (!device.user.equals(user)) {
                     throw new AccessDeniedException();
                 }
             }
             
             if (deviceName != null) {
                 device.name = deviceName;
             }
             if (c2dm != null) {
                 device.c2dm = c2dm;
             }
             
             session.put(device);
             session.getTxn().commit();
         } finally {
             if (session.getTxn().isActive()) {
                 session.getTxn().rollback();
             }
         }
         
         return device;
     }
     
     public Set<String> unregisterDevice(String user, String deviceId) throws AccessDeniedException {
         checkNotNull(user, "User is required");
         
         final Objectify session = of.begin();
         if (deviceId != null) {
             // Unregister a single device.
             final Device device = session.find(Device.class, deviceId);
             if (device != null) {
                 if (!user.equals(device.user)) {
                     throw new AccessDeniedException();
                 }
                 final Iterable<Key<Event>> events = session.query(Event.class).ancestor(device)
                         .fetchKeys();
                 session.delete(events);
                 session.delete(Device.class, deviceId);
                 return Collections.singleton(deviceId);
             }
         }
         
         // Unregister every user device.
         final Iterable<Key<Device>> devices = session.query(Device.class).filter("user", user)
                 .fetchKeys();
         final Set<String> deviceIds = new HashSet<String>(4);
         for (final Key<Device> device : devices) {
             final Iterable<Key<Event>> events = session.query(Event.class).ancestor(device)
                     .fetchKeys();
             session.delete(events);
             session.delete(device);
             deviceIds.add(deviceId);
         }
         
         return deviceIds;
     }
     
     public void addEvent(String user, String deviceId, String eventId, long eventDate,
             EventType eventType, String eventNumber, String eventName, String eventMessage)
             throws DeviceNotFoundException, AccessDeniedException {
         checkNotNull(user, "User is required");
         checkNotNull(deviceId, "Device identifier is required");
         checkNotNull(eventId, "Event identifier is required");
         checkNotNull(eventType, "Event type is required");
         
         final Objectify session = of.begin();
         final Device device = session.find(Device.class, deviceId);
         if (device == null) {
             throw new DeviceNotFoundException(deviceId);
         }
         if (!user.equals(device.user)) {
             throw new AccessDeniedException();
         }
         
         final Event event = new Event();
         event.id = eventId;
         event.device = new Key<Device>(Device.class, deviceId);
         event.date = eventDate;
         event.type = eventType;
         event.number = eventNumber;
         event.name = eventName;
         event.message = eventMessage;
         event.update = System.currentTimeMillis();
         session.put(event);
     }
     
     public void cleanEvents(String deviceId, long maxAge) {
         if (deviceId != null) {
             final Objectify session = of.beginTransaction();
             try {
                 final long now = System.currentTimeMillis();
                 final long limit = now - maxAge;
                 final Set<Event> eventsToDelete = new HashSet<Event>(16);
                 
                 final Iterable<Event> events = session.query(Event.class).ancestor(
                     new Key<Device>(Device.class, deviceId));
                 for (final Event event : events) {
                     if (event.date < limit) {
                         eventsToDelete.add(event);
                     }
                 }
                 
                 session.delete(eventsToDelete);
                 session.getTxn().commit();
             } finally {
                 if (session.getTxn().isActive()) {
                     session.getTxn().rollback();
                 }
             }
         }
     }
 }

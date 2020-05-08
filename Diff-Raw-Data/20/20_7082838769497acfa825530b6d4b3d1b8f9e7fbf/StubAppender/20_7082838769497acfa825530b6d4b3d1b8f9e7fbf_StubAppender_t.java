 /*******************************************************************************
  * Copyright (c) 2008, 2010 VMware Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   VMware Inc. - initial contribution
  *******************************************************************************/
 
 package org.eclipse.virgo.medic.log.appender;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import ch.qos.logback.classic.spi.LoggingEvent;
 import ch.qos.logback.core.AppenderBase;
 
public final class StubAppender extends AppenderBase<LoggingEvent> {

    private static final Object monitor = new Object();
 
     private static Map<String, List<LoggingEvent>> loggingEvents = new HashMap<String, List<LoggingEvent>>();
 
     @Override
     protected void append(LoggingEvent eventObject) {
        synchronized (monitor) {
            getLoggingEventsByName(name).add(eventObject);
        }
     }
 
     public static List<LoggingEvent> getAndResetLoggingEvents(String name) {
        synchronized (monitor) {
            List<LoggingEvent> loggingEvents = getLoggingEventsByName(name);
            List<LoggingEvent> response = new ArrayList<LoggingEvent>(loggingEvents);
            loggingEvents.clear();
            return response;
        }
     }
 
     private static List<LoggingEvent> getLoggingEventsByName(String name) {
         List<LoggingEvent> loggingEventsForName = loggingEvents.get(name);
         if (loggingEventsForName == null) {
             loggingEventsForName = new ArrayList<LoggingEvent>();
             loggingEvents.put(name, loggingEventsForName);
         }
         return loggingEventsForName;
     }
 }

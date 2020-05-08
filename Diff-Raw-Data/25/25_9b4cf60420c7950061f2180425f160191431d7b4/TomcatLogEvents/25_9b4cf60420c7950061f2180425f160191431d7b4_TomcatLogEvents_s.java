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
 
 package org.eclipse.virgo.web.tomcat;
 
 import org.eclipse.virgo.kernel.serviceability.LogEventDelegate;
 import org.eclipse.virgo.medic.eventlog.Level;
 import org.eclipse.virgo.medic.eventlog.LogEvent;
 
 public enum TomcatLogEvents implements LogEvent {
 
     STARTING_TOMCAT(0, Level.INFO), //
     TOMCAT_STARTED(1, Level.INFO), //
     STOPPING_TOMCAT(2, Level.INFO), //
     TOMCAT_STOPPED(3, Level.INFO), //        
     
     CREATING_CONNECTOR(10, Level.INFO), //
    PORT_IN_USE(11, Level.ERROR);
 
     private static final String PREFIX = "TC";
 
     private final LogEventDelegate delegate;
 
     private TomcatLogEvents(int code, Level level) {
         this.delegate = new LogEventDelegate(PREFIX, code, level);
     }
 
     /**
      * {@inheritDoc}
      */
     public String getEventCode() {
         return this.delegate.getEventCode();
     }
 
     /**
      * {@inheritDoc}
      */
     public Level getLevel() {
         return this.delegate.getLevel();
     }
 }

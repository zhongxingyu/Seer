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
 
 package org.eclipse.virgo.medic.dump.impl.thread;
 
 import java.io.PrintWriter;
 import java.lang.management.MonitorInfo;
 import java.lang.management.ThreadInfo;
 
 class StandardThreadInfoWriter implements ThreadInfoWriter {
 
     public void write(ThreadInfo threadInfo, PrintWriter writer) {
        writer.print(threadInfo.toString().replaceAll("\n",  System.getProperty("line.separator")));
     }
 
     private static MonitorInfo findLockedMonitor(StackTraceElement stackTraceElement, MonitorInfo[] lockedMonitors) {
         for (MonitorInfo monitorInfo : lockedMonitors) {
             if (stackTraceElement.equals(monitorInfo.getLockedStackFrame())) {
                 return monitorInfo;
             }
         }
         return null;
     }
 }

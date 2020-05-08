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
        writer.print(String.format("\"%s\" Id=%s %s", threadInfo.getThreadName(), threadInfo.getThreadId(), threadInfo.getThreadState()));
        if (threadInfo.getLockName() != null) {
            writer.print(String.format(" on %s", threadInfo.getLockName()));
            if (threadInfo.getLockOwnerName() != null) {
                writer.print(String.format(" owned by \"%s\" Id=%s", threadInfo.getLockOwnerName(), threadInfo.getLockOwnerId()));
            }
        }
        if (threadInfo.isInNative()) {
            writer.println(" (in native)");
        } else {
            writer.println();
        }
        MonitorInfo[] lockedMonitors = threadInfo.getLockedMonitors();

        StackTraceElement[] stackTraceElements = threadInfo.getStackTrace();
        for (StackTraceElement stackTraceElement : stackTraceElements) {
            writer.println("    at " + stackTraceElement);
            MonitorInfo lockedMonitor = findLockedMonitor(stackTraceElement, lockedMonitors);
            if (lockedMonitor != null) {
                writer.println("    - locked " + lockedMonitor.getClassName() + "@" + lockedMonitor.getIdentityHashCode());
            }
        }
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

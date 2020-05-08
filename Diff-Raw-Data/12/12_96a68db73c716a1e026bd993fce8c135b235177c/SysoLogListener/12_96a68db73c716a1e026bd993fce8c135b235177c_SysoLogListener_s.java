 /*******************************************************************************
 * Copyright (c) 2007 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.core.logging;
 
 import java.util.Date;
 
 import org.eclipse.equinox.log.ExtendedLogEntry;
 import org.osgi.service.log.LogEntry;
 import org.osgi.service.log.LogListener;
 import org.osgi.service.log.LogService;
 
 public class SysoLogListener implements LogListener {
 
 	public void logged(LogEntry entry) {
 		ExtendedLogEntry eEntry = (ExtendedLogEntry) entry;
		StringBuffer buffer = new StringBuffer();
		long time = eEntry.getTime();
		if (time == 0) {
			buffer.append(new Date().toString()).append(' ');
		} else {
			buffer.append(new Date(eEntry.getTime()).toString()).append(' ');
		}
 		String level;
 		switch (eEntry.getLevel()) {
 		case LogService.LOG_DEBUG:
 			level = "DEBUG";
 			break;
 		case LogService.LOG_WARNING:
 			level = "WARNING";
 			break;
 		case LogService.LOG_ERROR:
 			level = "ERROR";
 			break;
 		case LogService.LOG_INFO:
 			level = "INFO";
 			break;
 		default:
 			level = "UNKNOWN";
 			break;
 		}
 		buffer.append(level).append(' ');
 		buffer.append("[Thread-" + eEntry.getThreadID() + "] ");
 		buffer.append(eEntry.getLoggerName()).append(' ');
 		if (eEntry.getContext() != null) {
 			buffer.append(eEntry.getContext()).append(' ');
 		}
 		buffer.append(entry.getMessage());
 		System.out.println(buffer.toString());
 		if (eEntry.getException() != null) {
 			eEntry.getException().printStackTrace(System.out);
 		}
 	}
 }

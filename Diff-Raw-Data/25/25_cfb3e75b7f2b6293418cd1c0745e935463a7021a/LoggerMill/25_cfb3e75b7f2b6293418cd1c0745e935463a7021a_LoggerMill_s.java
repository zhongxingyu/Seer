 /*******************************************************************************
  * Copyright (c) 2007, 2009 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.internal.core.logging;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.osgi.service.log.LogListener;
 
 import org.eclipse.equinox.log.ExtendedLogReaderService;
 import org.eclipse.equinox.log.ExtendedLogService;
 import org.eclipse.equinox.log.LogFilter;
 import org.eclipse.equinox.log.Logger;
 
 import org.eclipse.riena.core.logging.CommandProviderLogFilter;
 import org.eclipse.riena.core.logging.ILogCatcher;
 import org.eclipse.riena.core.logging.LogServiceLogCatcher;
 import org.eclipse.riena.core.logging.PlatformLogCatcher;
 import org.eclipse.riena.core.logging.SysoLogListener;
 import org.eclipse.riena.core.wire.WireWith;
 import org.eclipse.riena.internal.core.ignore.IgnoreFindBugs;
 
 /**
  * The {@code LoggerMill} is responsible for delivering ready to use {@code
  * Logger} instances and also for the configuration of the riena logging.<br>
  * For the curious: There are so many LoggerFactories out there. So, not another
  * one!
  */
 @WireWith(LoggerMillWiring.class)
 public class LoggerMill {
 
 	/**
	 * When set to true and no loggers have been defined than a default logging
	 * setup will be used.
 	 */
 	public static final String RIENA_DEFAULT_LOGGING = "riena.defaultlogging"; //$NON-NLS-1$
 
 	private List<LogListener> logListeners = new ArrayList<LogListener>();
 	private List<ILogCatcher> logCatchers = new ArrayList<ILogCatcher>();;
 	private ILogListenerDefinition[] listenerDefs;
 	private ILogCatcherDefinition[] catcherDefs;
 
 	private ExtendedLogService logService = null;
 
 	/**
 	 * Get the logger for the specified category.<br>
 	 * <b>Note:</b> Use the log levels defined in
 	 * {@link org.osgi.service.log.LogService}
 	 * 
 	 * @param category
 	 *            logger name
 	 * @return
 	 */
 	public Logger getLogger(String category) {
 		synchronized (this) {
 			return isReady() ? logService.getLogger(category) : null;
 		}
 	}
 
 	/**
 	 * Bind ExtendedLogService
 	 * 
 	 * @param logService
 	 */
 	public void bind(ExtendedLogService logService) {
 		synchronized (this) {
 			this.logService = logService;
 		}
 	}
 
 	/**
 	 * Unbind ExtendedLogService
 	 * 
 	 * @param logService
 	 */
 	public void unbind(ExtendedLogService logService) {
 		synchronized (this) {
 			this.logService = null;
 		}
 	}
 
 	/**
 	 * Bind ExtendedLogReaderService
 	 * 
 	 * @param logReaderService
 	 */
 	public void bind(ExtendedLogReaderService logReaderService) {
		final boolean isDefaultLogging = Boolean.getBoolean(RIENA_DEFAULT_LOGGING);
		if (listenerDefs.length == 0 && isDefaultLogging) {
 			listenerDefs = new ILogListenerDefinition[] { new SysoLogListenerDefinition() };
 		}
 		for (ILogListenerDefinition logListenerDef : listenerDefs) {
 			LogListener listener = logListenerDef.createLogListener();
 			if (listener == null) {
 				// this can only happen, if the mandatory attribute is not defined, i.e. a schema violation
 				continue;
 			}
 			if (logListenerDef.isSynchronous()) {
 				listener = new SynchronousLogListenerAdapter(listener);
 			}
 			logListeners.add(listener);
 			LogFilter filter = logListenerDef.createLogFilter();
 			if (filter == null) {
 				logReaderService.addLogListener(listener);
 			} else {
 				logReaderService.addLogListener(listener, filter);
 			}
 		}
		if (catcherDefs.length == 0 && isDefaultLogging) {
 			catcherDefs = new ILogCatcherDefinition[] { new PlatformLogCatcherDefinition(),
 					new LogServiceLogCatcherDefinition() };
 		}
 		for (ILogCatcherDefinition catcherDef : catcherDefs) {
 			ILogCatcher logCatcher = catcherDef.createLogCatcher();
 			logCatcher.attach();
 			logCatchers.add(logCatcher);
 		}
 	}
 
 	/**
 	 * Unbind ExtendedLogReaderService
 	 * 
 	 * @param logReaderService
 	 */
 	public void unbind(ExtendedLogReaderService logReaderService) {
 		for (LogListener logListener : logListeners) {
 			logReaderService.removeLogListener(logListener);
 		}
 
 		for (ILogCatcher logCatcher : logCatchers) {
 			logCatcher.detach();
 		}
 	}
 
 	@IgnoreFindBugs(value = "EI_EXPOSE_REP2", justification = "deep cloning the listenerDefs is too expensive")
 	public void update(final ILogListenerDefinition[] listenerDefs) {
 		this.listenerDefs = listenerDefs;
 	}
 
 	@IgnoreFindBugs(value = "EI_EXPOSE_REP2", justification = "deep cloning the catcherDefs is too expensive")
 	public void update(final ILogCatcherDefinition[] catcherDefs) {
 		this.catcherDefs = catcherDefs;
 	}
 
 	public boolean isReady() {
 		synchronized (this) {
 			return logService != null;
 		}
 	}
 
 	/**
 	 * Definition of log listener that defines a {@code SysoLogListener} with a
 	 * {@code CommandProviderLogFilter}.
 	 */
 	private static final class SysoLogListenerDefinition implements ILogListenerDefinition {
 		public boolean isSynchronous() {
 			return true;
 		}
 
 		public LogFilter createLogFilter() {
 			return new CommandProviderLogFilter();
 		}
 
 		public LogListener createLogListener() {
 			return new SysoLogListener();
 		}
 
 		public String getName() {
 			return "DefaultLogListner"; //$NON-NLS-1$
 		}
 	}
 
 	/**
 	 * Definition of log catcher that defines a {@code PlatformLogCatcher}.
 	 */
 	private final static class PlatformLogCatcherDefinition implements ILogCatcherDefinition {
 
 		public ILogCatcher createLogCatcher() {
 			return new PlatformLogCatcher();
 		}
 
 		public String getName() {
 			return "DefaultPlatformLogCatcher"; //$NON-NLS-1$
 		}
 	}
 
 	/**
 	 * Definition of log catcher that defines a {@code LogServiceLogCatcher}.
 	 */
 	private final static class LogServiceLogCatcherDefinition implements ILogCatcherDefinition {
 
 		public ILogCatcher createLogCatcher() {
 			return new LogServiceLogCatcher();
 		}
 
 		public String getName() {
 			return "DefaultLogServiceLogCatcher"; //$NON-NLS-1$
 		}
 	}
 
 }

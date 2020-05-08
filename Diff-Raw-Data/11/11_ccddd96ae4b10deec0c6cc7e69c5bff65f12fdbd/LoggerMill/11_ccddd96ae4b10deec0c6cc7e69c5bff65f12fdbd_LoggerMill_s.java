 /*******************************************************************************
  * Copyright (c) 2007, 2010 compeople AG and others.
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
 
 import org.eclipse.riena.core.RienaStatus;
 import org.eclipse.riena.core.logging.CommandProviderLogFilter;
 import org.eclipse.riena.core.logging.ILogCatcher;
 import org.eclipse.riena.core.logging.LogServiceLogCatcher;
 import org.eclipse.riena.core.logging.PlatformLogCatcher;
 import org.eclipse.riena.core.logging.SynchronousLogListenerAdapter;
 import org.eclipse.riena.core.logging.SysoLogListener;
 import org.eclipse.riena.core.wire.InjectExtension;
 import org.eclipse.riena.core.wire.InjectService;
 import org.eclipse.riena.internal.core.ignore.IgnoreFindBugs;
 
 /**
  * The {@code LoggerMill} is responsible for delivering ready to use
  * {@code Logger} instances and also for the configuration of the riena logging.<br>
  * For the curious: There are so many LoggerFactories out there. So, not another
  * one!
  */
 public class LoggerMill {
 
 	private final List<LogListener> logListeners = new ArrayList<LogListener>();
 	private final List<ILogCatcher> logCatchers = new ArrayList<ILogCatcher>();;
 	private ILogListenerExtension[] listenerDefs;
 	private ILogCatcherExtension[] catcherDefs;
 
 	private ExtendedLogService logService;
 
 	/**
 	 * Get the logger for the specified category.<br>
 	 * <b>Note:</b> Use the log levels defined in
 	 * {@link org.osgi.service.log.LogService}
 	 * 
 	 * @param category
 	 *            logger name
 	 * @return
 	 */
 	public Logger getLogger(final String category) {
 		synchronized (this) {
 			return isReady() ? logService.getLogger(category) : null;
 		}
 	}
 
 	/**
 	 * Bind ExtendedLogService
 	 * 
 	 * @param logService
 	 */
	@InjectService(useRanking = true)
 	public void bind(final ExtendedLogService logService) {
 		synchronized (this) {
 			this.logService = logService;
 		}
 	}
 
 	/**
 	 * Unbind ExtendedLogService
 	 * 
 	 * @param logService
 	 */
 	public void unbind(final ExtendedLogService logService) {
 		synchronized (this) {
 			this.logService = null;
 		}
 	}
 
 	/**
 	 * Bind ExtendedLogReaderService
 	 * 
 	 * @param logReaderService
 	 */
	@InjectService(useRanking = true)
 	public void bind(final ExtendedLogReaderService logReaderService) {
 		// When isDevelopment() is explicitly set to {@code false} no default logging will be used. Otherwise if 
 		// no loggers have been defined than a default logging setup will be used.
 		final boolean useDefaultLogging = RienaStatus.isDevelopment();
 		if (listenerDefs.length == 0 && useDefaultLogging) {
 			listenerDefs = new ILogListenerExtension[] { new SysoLogListenerDefinition() };
 		}
 		for (final ILogListenerExtension logListenerDef : listenerDefs) {
 			LogListener listener = logListenerDef.createLogListener();
 			if (listener == null) {
 				// this can only happen, if the mandatory attribute is not defined, i.e. a schema violation
 				continue;
 			}
 			if (logListenerDef.isSynchronous()) {
 				listener = new SynchronousLogListenerAdapter(listener);
 			}
 			logListeners.add(listener);
 			final LogFilter filter = logListenerDef.createLogFilter();
 			if (filter == null) {
 				logReaderService.addLogListener(listener);
 			} else {
 				logReaderService.addLogListener(listener, filter);
 			}
 		}
 		if (catcherDefs.length == 0 && useDefaultLogging) {
 			catcherDefs = new ILogCatcherExtension[] { new PlatformLogCatcherDefinition(),
 					new LogServiceLogCatcherDefinition() };
 		}
 		for (final ILogCatcherExtension catcherDef : catcherDefs) {
 			final ILogCatcher logCatcher = catcherDef.createLogCatcher();
 			logCatcher.attach();
 			logCatchers.add(logCatcher);
 		}
 	}
 
 	/**
 	 * Unbind ExtendedLogReaderService
 	 * 
 	 * @param logReaderService
 	 */
 	public void unbind(final ExtendedLogReaderService logReaderService) {
 		for (final LogListener logListener : logListeners) {
 			logReaderService.removeLogListener(logListener);
 		}
 
 		for (final ILogCatcher logCatcher : logCatchers) {
 			logCatcher.detach();
 		}
 	}
 
 	@IgnoreFindBugs(value = "EI_EXPOSE_REP2", justification = "deep cloning the listenerDefs is too expensive")
	@InjectExtension
 	public void update(final ILogListenerExtension[] listenerDefs) {
 		this.listenerDefs = listenerDefs;
 	}
 
 	@IgnoreFindBugs(value = "EI_EXPOSE_REP2", justification = "deep cloning the catcherDefs is too expensive")
	@InjectExtension
 	public void update(final ILogCatcherExtension[] catcherDefs) {
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
 	private static final class SysoLogListenerDefinition implements ILogListenerExtension {
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
 	private final static class PlatformLogCatcherDefinition implements ILogCatcherExtension {
 
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
 	private final static class LogServiceLogCatcherDefinition implements ILogCatcherExtension {
 
 		public ILogCatcher createLogCatcher() {
 			return new LogServiceLogCatcher();
 		}
 
 		public String getName() {
 			return "DefaultLogServiceLogCatcher"; //$NON-NLS-1$
 		}
 	}
 
 }

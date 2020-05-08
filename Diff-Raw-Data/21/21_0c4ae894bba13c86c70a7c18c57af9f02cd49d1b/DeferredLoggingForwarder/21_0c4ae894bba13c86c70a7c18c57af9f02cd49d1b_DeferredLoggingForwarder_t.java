 /*******************************************************************************
  * Copyright (c) 2007, 2011 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.internal.core.logging;
 
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.TimeUnit;
 
 import org.osgi.service.log.LogService;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.equinox.log.Logger;
 
 import org.eclipse.riena.core.Log4r;
 import org.eclipse.riena.core.logging.ConsoleLogger;
 import org.eclipse.riena.internal.core.Activator;
 
 /**
  * The worker thread that delivers collected log events.
  */
 public class DeferredLoggingForwarder extends Job {
 
 	private final LoggerProvider loggerProvider;
 	private final BlockingQueue<DeferredLogEvent> queue;
	private static final String WE_ARE_FAMILY = Activator.PLUGIN_ID;
 
 	public DeferredLoggingForwarder(final LoggerProvider loggerProvider, final BlockingQueue<DeferredLogEvent> queue) {
 		super("DeferredLoggingForwarder"); //$NON-NLS-1$
 		setSystem(true);
 		this.loggerProvider = loggerProvider;
 		this.queue = queue;
 	}
 
 	@Override
 	public boolean belongsTo(final Object family) {
 		return WE_ARE_FAMILY.equals(family);
 	}
 
 	@Override
 	protected IStatus run(final IProgressMonitor monitor) {
 		while (!monitor.isCanceled()) {
 			DeferredLogEvent logEvent;
 			try {
 				logEvent = queue.poll(1, TimeUnit.MILLISECONDS);
 			} catch (final InterruptedException e1) {
 				Thread.currentThread().interrupt();
 				break;
 			}
 
 			if (logEvent == null) {
 				continue;
 			}
 
 			while (!loggerProvider.hasReadyLoggerMill()) {
 				try {
 					Thread.sleep(10);
 				} catch (final InterruptedException e) {
 					Thread.currentThread().interrupt();
 					break;
 				}
 			}
 			try {
 				final Logger logger = Log4r.getLogger(Activator.getDefault(), logEvent.getLoggerName());
 				logger.log(logEvent.getLevel(), logEvent.toString());
 			} catch (final Exception e) {
 				new ConsoleLogger(DeferredLoggingForwarder.class.getName()).log(LogService.LOG_ERROR,
 						"Could not deliver defered log message.", e); //$NON-NLS-1$
 			}
 		}
 		return Status.CANCEL_STATUS;
 	}
 
 }

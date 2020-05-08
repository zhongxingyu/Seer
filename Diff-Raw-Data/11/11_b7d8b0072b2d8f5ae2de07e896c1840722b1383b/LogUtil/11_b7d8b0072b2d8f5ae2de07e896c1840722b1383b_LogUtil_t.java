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
 
 import org.eclipse.equinox.log.ExtendedLogReaderService;
 import org.eclipse.equinox.log.ExtendedLogService;
 import org.eclipse.equinox.log.Logger;
 import org.eclipse.riena.core.service.ServiceId;
 import org.osgi.framework.BundleContext;
 
 /**
  * Wrapper to access the existing Logger.
  */
 public class LogUtil {
 
 	private static ExtendedLogService logService;
 	private ExtendedLogReaderService logReaderService;
 	private static boolean initialized = false;
 	private BundleContext context;
 
 	public LogUtil(BundleContext context) {
 		this.context = context;
 	}
 
 	public Logger getLogger(String name) {
 		if (!initialized) {
 			init();
 			initialized = true;
 		}
 		if (logService != null) {
 			return logService.getLogger(name);
 		} else {
 			return new NullLogger();
 		}
 
 	}
 
 	/**
 	 * Bind ExtendedLogService
 	 * 
 	 * @param logService
 	 */
 	public void bind(ExtendedLogService logServiceParm) {
 		logService = logServiceParm;
 	}
 
 	/**
 	 * Unbind ExtendedLogService
 	 * 
 	 * @param logService
 	 */
 	public void unbind(ExtendedLogService logServiceParm) {
 		logService = null;
 	}
 
 	/**
 	 * Bind ExtendedLogReaderService
 	 * 
 	 * @param logReaderService
 	 */
 	public void bind(ExtendedLogReaderService logReaderService) {
 		this.logReaderService = logReaderService;
 		// TODO remove SysoLogListener if we have Log4jLogListener
 		this.logReaderService.addLogListener(new SysoLogListener(), new AlwaysFilter());
 		this.logReaderService.addLogListener(new Log4jLogListener(), new AlwaysFilter());
 	}
 
 	/**
 	 * Unbind ExtendedLogReaderService
 	 * 
 	 * @param logReaderService
 	 */
 	public void unbindLogReaderService(ExtendedLogReaderService logReaderService) {
 		this.logReaderService = null;
 	}
 
 	/**
 	 * initialize LogUtil
 	 */
 	public void init() {
		new ServiceId(ExtendedLogService.class.getName()).useRanking().injectInto(this).start(context);
		new ServiceId(ExtendedLogReaderService.class.getName()).useRanking().injectInto(this).start(context);
 	}
 }

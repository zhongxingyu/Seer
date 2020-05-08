 /*******************************************************************************
  * Copyright (c) May 18, 2011 Zend Technologies Ltd. 
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Eclipse Public License v1.0 
  * which accompanies this distribution, and is available at 
  * http://www.eclipse.org/legal/epl-v10.html  
  *******************************************************************************/
 package org.zend.sdkcli.internal.logger;
 
 import org.apache.log4j.Logger;
 import org.apache.log4j.PropertyConfigurator;
 import org.zend.sdklib.logger.ILogger;
 
 /**
  * Implementation of {@link ILogger}. It provides log4j logging.
  * 
  * @author Wojciech Galanciak, 2011
  * 
  */
 public class CliLogger implements ILogger {
 
 	private Logger logger;
 
 	public CliLogger() {
 		PropertyConfigurator.configure("log4j.properties");
 	}
 
	private CliLogger(String creatorName) {
		this();
		this.logger = Logger.getLogger(creatorName);
	}

 	@Override
	public ILogger getLogger(String creatorName) {
		return new CliLogger(creatorName);
 	}
 
 	@Override
 	public void debug(Object message) {
 		logger.debug(message);
 	}
 
 	@Override
 	public void info(Object message) {
 		logger.info(message);
 	}
 
 	@Override
 	public void error(Object message) {
 		logger.error(message);
 	}
 
 	@Override
 	public void warning(Object message) {
 		logger.warn(message);
 	}
 
 }

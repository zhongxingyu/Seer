 /*-
  * Copyright © 2010 Diamond Light Source Ltd.
  *
  * This file is part of GDA.
  *
  * GDA is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License version 3 as published by the Free
  * Software Foundation.
  *
  * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along
  * with GDA. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package gda.util.logging;
 
 import java.util.List;
 
 import org.slf4j.LoggerFactory;
 
 import ch.qos.logback.classic.Level;
 import ch.qos.logback.classic.Logger;
 import ch.qos.logback.classic.LoggerContext;
 import ch.qos.logback.classic.spi.ILoggingEvent;
 import ch.qos.logback.core.Appender;
 import ch.qos.logback.core.ConsoleAppender;
 import junit.framework.TestCase;
 
 public class LogbackUtilsTest extends TestCase {
 	
	private static final String ROOT_LOGGER_NAME = "ROOT";
 	
 	public void testReset() {
 		LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
 		
 		// This creates a couple of extra loggers besides the root logger...
 		LoggerFactory.getLogger("xxx.yyy");
 		// ...so there should be at least 3
 		assertTrue(loggerContext.getLoggerList().size() >= 3);
 		
 		// Resetting the logging system shouldn't remove existing loggers...
 		LogbackUtils.resetLogging();
 		// ...so there should still be at least 3
 		assertTrue(loggerContext.getLoggerList().size() >= 3);
 		
 		// None of the loggers should have any appenders, or a level (except for the root logger)
 		for (Logger logger : loggerContext.getLoggerList()) {
 			if (logger.getName().equals(ROOT_LOGGER_NAME)) {
 				assertEquals(Level.DEBUG, logger.getLevel());
 			} else {
 				assertNull(logger.getLevel());
 			}
 			assertEquals(0, LogbackUtils.getAppendersForLogger(logger).size());
 		}
 	}
 	
 	public void testResetLoggingToDefaultConfiguration() {
 		LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
 		
 		// This creates a couple of extra loggers besides the root logger...
 		LoggerFactory.getLogger("xxx.yyy");
 		// ...so there should be at least 3
 		assertTrue(loggerContext.getLoggerList().size() >= 3);
 		
 		// Resetting the logging system to the default configuration shouldn't remove existing loggers...
 		LogbackUtils.resetLoggingToDefaultConfiguration();
 		// ...so there should still be at least 3
 		assertTrue(loggerContext.getLoggerList().size() >= 3);
 		
 		// Except for the root logger, none of the loggers should have any appenders, or a level
 		for (Logger logger : loggerContext.getLoggerList()) {
 			final List<Appender<ILoggingEvent>> appenders = LogbackUtils.getAppendersForLogger(logger);
 			if (logger.getName().equals(ROOT_LOGGER_NAME)) {
 				assertEquals(Level.DEBUG, logger.getLevel());
 				assertEquals(1, appenders.size());
 				assertEquals(ConsoleAppender.class, appenders.get(0).getClass());
 			} else {
 				assertNull(logger.getLevel());
 				assertEquals(0, appenders.size());
 			}
 		}
 	}
 	
 
 }

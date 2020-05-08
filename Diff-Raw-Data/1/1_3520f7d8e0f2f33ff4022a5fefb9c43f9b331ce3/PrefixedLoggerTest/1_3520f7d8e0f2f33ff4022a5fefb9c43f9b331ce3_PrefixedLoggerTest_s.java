 /*******************************************************************************
  Copyright (c) 2013 James Richardson.
 
  PrefixedLoggerTest.java is part of bukkit-utilities.
 
  BukkitUtilities is free software: you can redistribute it and/or modify it
  under the terms of the GNU General Public License as published by the Free
  Software Foundation, either version 3 of the License, or (at your option) any
  later version.
 
  BukkitUtilities is distributed in the hope that it will be useful, but WITHOUT
  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 
  You should have received a copy of the GNU General Public License along with
  BukkitUtilities. If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 
 package name.richardson.james.bukkit.utilities.logging;
 
 import java.util.logging.Handler;
 import java.util.logging.Level;
 import java.util.logging.LogRecord;
 import java.util.logging.Logger;
 
 import junit.framework.Assert.*;
 import junit.framework.TestCase;
 import org.junit.Before;
 import org.junit.Test;
 
 import static org.mockito.Matchers.anyObject;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.times;
 import static org.mockito.Mockito.verify;
 
 public class PrefixedLoggerTest extends TestCase {
 
 	@Test
 	public void testLogLocalisedMessage()
 	throws Exception {
 		Logger logger = PrefixedLogger.getLogger(this.getClass());
 		Handler handler = mock(Handler.class);
 		logger.addHandler(handler);
 	  logger.info("test-message");
 		verify(handler, times(1)).publish((LogRecord) anyObject());
 	}
 
 	@Test
 	public void testNormalLog()
 	throws Exception {
 		Logger logger = PrefixedLogger.getLogger(this.getClass());
 		Handler handler = mock(Handler.class);
 		logger.addHandler(handler);
 		logger.severe("test");
 		logger.warning("test");
 		logger.info("test");
 		logger.fine("test");
 		logger.finer("test");
 		logger.finest("test");
 		verify(handler, times(3)).publish((LogRecord) anyObject());
 	}
 
 	@Test
 	public void testDebugLog()
 	throws Exception {
 		Logger logger = PrefixedLogger.getLogger(this.getClass());
 		Handler handler = mock(Handler.class);
 		logger.addHandler(handler);
 		logger.setLevel(Level.ALL);
 		logger.severe("test");
 		logger.warning("test");
 		logger.info("test");
 		logger.fine("test");
 		logger.finer("test");
 		logger.finest("test");
 		verify(handler, times(6)).publish((LogRecord) anyObject());
 	}
 
 	@Test
 	public void testParentResolution()
 	throws Exception {
 		Logger logger1 = PrefixedLogger.getLogger("name.richardson.james.bukkit.utilities.logging");
 		Logger logger2 = PrefixedLogger.getLogger("name.richardson.james.bukkit.utilities.logging.resolution.frank");
 		assertEquals("Logger parent has not been set correctly!!", logger1, logger2.getParent());
 	}
 
 	@Test
 	public void testSetPrefix()
 	throws Exception {
 		PrefixedLogger.setPrefix("test");
 		assertEquals("Prefix has not been set correctly!", PrefixedLogger.getPrefix(), "test");
 	}
 
 	@Test
 	public void testGetLoggerByName()
 	throws Exception {
 		Logger logger = PrefixedLogger.getLogger("name.richardson.james.bukkit.utilities.logging");
 		assertNotNull("Logger should not be null!", logger);
 		assertEquals("Logger name is not correct!", "name.richardson.james.bukkit.utilities.logging", logger.getName());
 	}
 
 	@Test
 	public void testGetLoggerByClass()
 	throws Exception {
 		Logger logger = PrefixedLogger.getLogger(this.getClass());
 		assertNotNull("Logger should not be null!", logger);
 		assertEquals("Logger name is not correct!", "name.richardson.james.bukkit.utilities.logging", logger.getName());
 	}
 
 	@Test
 	public void testNoDuplicateLogger() {
 		Logger logger1 = PrefixedLogger.getLogger(this.getClass());
 		Logger logger2 = PrefixedLogger.getLogger(this.getClass());
 		assertEquals("Both loggers should be the same instance!", logger1, logger2);
 	}
 
 }

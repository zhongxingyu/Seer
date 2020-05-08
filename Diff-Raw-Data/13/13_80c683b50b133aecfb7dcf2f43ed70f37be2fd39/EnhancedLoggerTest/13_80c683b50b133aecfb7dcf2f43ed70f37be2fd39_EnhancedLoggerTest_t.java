 package org.nohope.logging;
 
 import org.easymock.Capture;
 import org.junit.Test;
 
 import static org.easymock.EasyMock.*;
 import static org.junit.Assert.assertEquals;
 
 /**
  * @author ketoth xupack <ketoth.xupack@gmail.com>
  * @since 3/19/12 11:19 PM
  */
 public final class EnhancedLoggerTest {
 
     @Test
     public void debug() {
        final org.slf4j.Logger logger = createMock(org.slf4j.Logger.class);
 
        final Capture<Object[]> argsC = new Capture<>();
        final Capture<String> formatC = new Capture<>();
 
         logger.debug(capture(formatC), capture(argsC));
         expectLastCall();
         replay(logger);
 
        final EnhancedLogger elogger = new EnhancedLogger(logger);
 
         elogger.debug("%s %s %s %s %s", 1, 2, 3, 4, 5);
         assertEquals("%s %s %s %s %s", formatC.getValue());
 
         verify(logger);
 
        final Logger logger1 = LoggerFactory.getLogger(EnhancedLoggerTest.class);
         logger1.warn(new IllegalStateException(), "{} {}", 1, 2);
 
     }
 
 
 }

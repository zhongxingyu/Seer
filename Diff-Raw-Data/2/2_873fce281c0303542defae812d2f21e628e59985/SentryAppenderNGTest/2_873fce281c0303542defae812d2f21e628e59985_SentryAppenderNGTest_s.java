 package net.kencochrane.raven.log4j;
 
 import com.google.common.base.Joiner;
 import mockit.Expectations;
 import mockit.Injectable;
 import mockit.Mocked;
 import mockit.Verifications;
 import net.kencochrane.raven.Raven;
 import net.kencochrane.raven.event.Event;
 import net.kencochrane.raven.event.EventBuilder;
 import net.kencochrane.raven.event.interfaces.ExceptionInterface;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.apache.log4j.spi.LoggingEvent;
 import org.hamcrest.Matchers;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import java.util.Collections;
 import java.util.Date;
 import java.util.UUID;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.is;
 
 public class SentryAppenderNGTest {
     private SentryAppender sentryAppender;
     @Mocked
     private Raven mockRaven = null;
     @Injectable
     private Logger mockLogger = null;
 
     @BeforeMethod
     public void setUp() {
         sentryAppender = new SentryAppender(mockRaven);
     }
 
     @Test
    public void testSimpleMesageLogging() throws Exception {
         final String loggerName = UUID.randomUUID().toString();
         final String message = UUID.randomUUID().toString();
         final String threadName = UUID.randomUUID().toString();
         final Date date = new Date(1373883196416L);
         new Expectations() {{
             onInstance(mockLogger).getName();
             result = loggerName;
         }};
 
         sentryAppender.append(new LoggingEvent(null, mockLogger, date.getTime(), Level.INFO, message, threadName,
                 null, null, null, null));
 
         new Verifications() {{
             Event event;
             mockRaven.runBuilderHelpers(withAny(new EventBuilder()));
             mockRaven.sendEvent(event = withCapture());
             assertThat(event.getMessage(), is(message));
             assertThat(event.getLogger(), is(loggerName));
             assertThat(event.getExtra(), Matchers.<String, Object>hasEntry(SentryAppender.THREAD_NAME, threadName));
             assertThat(event.getTimestamp(), is(date));
         }};
     }
 
     @Test
     public void testLevelConversion() throws Exception {
         assertLevelConverted(Event.Level.DEBUG, Level.TRACE);
         assertLevelConverted(Event.Level.DEBUG, Level.DEBUG);
         assertLevelConverted(Event.Level.INFO, Level.INFO);
         assertLevelConverted(Event.Level.WARNING, Level.WARN);
         assertLevelConverted(Event.Level.ERROR, Level.ERROR);
         assertLevelConverted(Event.Level.FATAL, Level.FATAL);
     }
 
     private void assertLevelConverted(final Event.Level expectedLevel, Level level) throws Exception {
         sentryAppender.append(new LoggingEvent(null, mockLogger, 0, level, null, null));
 
         new Verifications() {{
             Event event;
             mockRaven.sendEvent(event = withCapture());
             assertThat(event.getLevel(), is(expectedLevel));
         }};
     }
 
     @Test
     public void testExceptionLogging() throws Exception {
         final Exception exception = new Exception(UUID.randomUUID().toString());
 
         sentryAppender.append(new LoggingEvent(null, mockLogger, 0, Level.ERROR, null, exception));
 
         new Verifications() {{
             Event event;
             Throwable throwable;
             mockRaven.sendEvent(event = withCapture());
             ExceptionInterface exceptionInterface = (ExceptionInterface) event.getSentryInterfaces()
                     .get(ExceptionInterface.EXCEPTION_INTERFACE);
             throwable = exceptionInterface.getThrowable();
             assertThat(throwable.getMessage(), is(exception.getMessage()));
             assertThat(throwable.getStackTrace(), is(exception.getStackTrace()));
         }};
     }
 
     @Test
     public void testMdcAddedToExtra() throws Exception {
         final String extraKey = UUID.randomUUID().toString();
         final String extraValue = UUID.randomUUID().toString();
 
         sentryAppender.append(new LoggingEvent(null, mockLogger, 0, Level.ERROR, null, null,
                 null, null, null, Collections.singletonMap(extraKey, extraValue)));
 
         new Verifications() {{
             Event event;
             mockRaven.sendEvent(event = withCapture());
             assertThat(event.getExtra(), Matchers.<String, Object>hasEntry(extraKey, extraValue));
         }};
     }
 
     @Test
     public void testNdcAddedToExtra() throws Exception {
         final String ndcEntries = Joiner.on(' ').join(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
 
         sentryAppender.append(new LoggingEvent(null, mockLogger, 0, Level.ERROR, null, null,
                 null, ndcEntries, null, null));
 
         new Verifications() {{
             Event event;
             mockRaven.sendEvent(event = withCapture());
             assertThat(event.getExtra(), Matchers.<String, Object>hasEntry(SentryAppender.LOG4J_NDC, ndcEntries));
         }};
     }
 }

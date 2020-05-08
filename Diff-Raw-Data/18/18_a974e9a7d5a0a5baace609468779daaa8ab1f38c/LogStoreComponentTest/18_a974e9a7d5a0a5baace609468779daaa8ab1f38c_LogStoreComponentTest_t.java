 package org.javakontor.sherlog.core.impl.internal.store;
 
 import java.util.LinkedList;
 import java.util.List;
 
import junit.framework.Assert;
import junit.framework.TestCase;

 import org.javakontor.sherlog.core.LogEvent;
 import org.javakontor.sherlog.core.LogLevel;
 import org.javakontor.sherlog.core.store.LogEventStoreChangeEvent;
 import org.javakontor.sherlog.core.store.LogEventStoreListener;
 
public class LogStoreComponentTest extends TestCase {
 
   private static long _eventIdentifier = 1L;
 
   public void test_Simple() {
     LogStoreComponent logStoreComponent = new LogStoreComponent();
 
     LogEvent testEvent = generateLogEvent();
     logStoreComponent.addLogEvent(testEvent);
     assertEquals(1, logStoreComponent.getLogEventCount());
     assertEquals(testEvent, logStoreComponent.getLogEvents().get(0));
     assertEquals(1, logStoreComponent.getFilteredLogEventCount());
     SimpleTestLogEventFilter simpleTestLogEventFilter = new SimpleTestLogEventFilter();
     logStoreComponent.addLogEventFilter(simpleTestLogEventFilter);
 
     // test adding filter
     assertEquals(1, logStoreComponent.getLogEventCount());
     assertEquals(testEvent, logStoreComponent.getLogEvents().get(0));
     assertEquals(0, logStoreComponent.getFilteredLogEventCount());
 
     // test modifying filter I
     simpleTestLogEventFilter.setShouldMatch(true);
     assertEquals(1, logStoreComponent.getLogEventCount());
     assertEquals(testEvent, logStoreComponent.getLogEvents().get(0));
     assertEquals(1, logStoreComponent.getFilteredLogEventCount());
 
     // test modifying filter II (filter should *not* match anymore)
     simpleTestLogEventFilter.setShouldMatch(false);
     assertEquals(1, logStoreComponent.getLogEventCount());
     assertEquals(testEvent, logStoreComponent.getLogEvents().get(0));
     assertEquals(0, logStoreComponent.getFilteredLogEventCount());
 
     // remove filter
     logStoreComponent.removeLogEventFilter(simpleTestLogEventFilter);
     assertEquals(1, logStoreComponent.getLogEventCount());
     assertEquals(testEvent, logStoreComponent.getLogEvents().get(0));
     assertEquals(1, logStoreComponent.getFilteredLogEventCount());
 
     // add a listener
     MyListener listener = new MyListener();
     logStoreComponent.addLogStoreListener(listener);
 
     // add some events without filter
     List<LogEvent> newEvents = new LinkedList<LogEvent>();
     final LogEventMock newLogEvent = generateLogEvent();
     newEvents.add(newLogEvent);
     final LogEventMock logEventMock = generateLogEvent();
     logEventMock.setCategory("new.category");
     newEvents.add(logEventMock);
     logStoreComponent.addLogEvents(newEvents);
     assertEquals(1, listener._addedInvocations);
     assertEquals(0, listener._storeResetInvocations);
     Assert.assertNotNull(listener._lastEvent);
     assertEquals(newEvents, listener._lastEvent.getLogEvents());
     assertEquals(newEvents, listener._lastEvent.getFilteredLogEvents());
     assertEquals(1, listener._lastEvent.getCategories().size());
     assertEquals("new.category", listener._lastEvent.getCategories().get(0));
 
     // add more events - with filter
     listener.reset();
     logStoreComponent.addLogEventFilter(new CategoryLogEventFilter("org.javakontor.sherlog.test"));
     logStoreComponent.addLogEvents(newEvents);
 
     assertEquals(1, listener._addedInvocations);
    // adding a filter leads to a store reset
    assertEquals(1, listener._storeResetInvocations);
     Assert.assertNotNull(listener._lastEvent);
     assertEquals(newEvents, listener._lastEvent.getLogEvents());
     // only the 'org.javakontor.sherlog.test' log event should have been filtered
     assertEquals(1, listener._lastEvent.getFilteredLogEvents().size());
     assertEquals(newLogEvent, listener._lastEvent.getFilteredLogEvents().get(0));
 
     // no new categories should have been added
     assertEquals(0, listener._lastEvent.getCategories().size());
 
   }
 
   class MyListener implements LogEventStoreListener {
 
     int                      _addedInvocations      = 0;
 
     int                      _storeResetInvocations = 0;
 
     LogEventStoreChangeEvent _lastEvent             = null;
 
     void reset() {
       _addedInvocations = 0;
       _storeResetInvocations = 0;
       _lastEvent = null;
     }
 
     public void logEventsAdded(LogEventStoreChangeEvent event) {
       _addedInvocations++;
       _lastEvent = event;
     }
 
     public void logEventStoreReset() {
       _storeResetInvocations++;
       _lastEvent = null;
     }
   }
 
   class CategoryLogEventFilter extends AbstractTestLogEventFilter {
     private final String _category;
 
     private CategoryLogEventFilter(String category) {
       super();
       _category = category;
     }
 
     public boolean matches(LogEvent event) {
       return _category.equals(event.getCategory());
     }
   }
 
   class SimpleTestLogEventFilter extends AbstractTestLogEventFilter {
     private boolean _shouldMatch = false;
 
     public boolean matches(LogEvent event) {
       return _shouldMatch;
     }
 
     public boolean isShouldMatch() {
       return _shouldMatch;
     }
 
     public void setShouldMatch(boolean shouldMatch) {
       _shouldMatch = shouldMatch;
       fireFilterChange();
     }
   }
 
   private LogEventMock generateLogEvent() {
 
     String category = "org.javakontor.sherlog.test"; // this.LOG_CATEGORIES[this.random.nextInt(this.LOG_CATEGORIES.length)];
     LogLevel logLevel = LogLevel.INFO; // this.ALL_LEVEL[this.random.nextInt(this.ALL_LEVEL.length)];
 
     Throwable throwable = null;
     if ((logLevel == LogLevel.WARN) || (logLevel == LogLevel.ERROR) || (logLevel == LogLevel.FATAL)) {
       throwable = new Throwable();
     }
 
     LogEventMock mock = new LogEventMock(category, ++_eventIdentifier, logLevel, "my source", "Message-"
         + _eventIdentifier, "Thread-1", throwable);
     return mock;
   }
 
 }

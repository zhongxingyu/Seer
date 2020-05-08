 package org.doxla.eventualj;
 
 import org.hamcrest.BaseMatcher;
 import org.hamcrest.Description;
 
 import java.util.concurrent.TimeoutException;
 
 import static com.google.code.tempusfugit.temporal.Duration.seconds;
 import static com.google.code.tempusfugit.temporal.Timeout.timeout;
 import static com.google.code.tempusfugit.temporal.WaitFor.waitOrTimeout;
 import static java.lang.String.format;
 import static org.doxla.eventualj.EventualCondition.condition;
 
 class EventuallyMatcher<T> extends BaseMatcher<T> {
     private final T expected;
     private final RecordingEventualContext eventualContext;
 
     EventuallyMatcher(T expected, RecordingEventualContext<T> eventualContext) {
         this.expected = expected;
         this.eventualContext = eventualContext;
     }
 
     public boolean matches(Object o) {
         TimeoutException timedout = null;
         try {
             waitOrTimeout(condition(expected, eventualContext), timeout(seconds(60L)));
         } catch (TimeoutException e) {
             timedout = e;
         } catch (InterruptedException e) {
             Thread.currentThread().interrupt();
         }
 
         return timedout == null;
     }
 
     public void describeTo(Description description) {
        description.appendText(format("Timeout occurred before %s was returned", expected));
     }
 }

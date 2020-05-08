 package org.analogweb.util;
 
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertThat;
 
 import java.util.concurrent.TimeUnit;
 
 import org.hamcrest.BaseMatcher;
 import org.hamcrest.Description;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.rules.ExpectedException;
 
 public class StopWatchTest {
 
     @Rule
     public ExpectedException thrown = ExpectedException.none();
 
     @Test
     public void test() throws Exception {
         StopWatch sw = new StopWatch();
         sw.start();
        TimeUnit.MILLISECONDS.sleep(100L);
         long actual = sw.stop();
        assertThat(actual, is(over(100 - 16)));
        assertThat(actual, is(under(100 + 16)));
     }
 
     @Test
     public void testStopFirst() throws Exception {
         thrown.expect(IllegalStateException.class);
         StopWatch sw = new StopWatch();
         sw.stop();
     }
 
     @Test
     public void testStartTwice() throws Exception {
         StopWatch sw = new StopWatch();
         sw.start();
         // continues silently.
         sw.start();
        TimeUnit.MILLISECONDS.sleep(100L);
         long actual = sw.stop();
        assertThat(actual, is(over(100 - 16)));
        assertThat(actual, is(under(100 + 16)));
     }
 
     private BaseMatcher<Long> over(final long overThe) {
         return new BaseMatcher<Long>() {
 
             @Override
             public boolean matches(Object arg0) {
                 if (arg0 instanceof Long) {
                     return (Long) arg0 >= overThe;
                 }
                 return false;
             }
 
             @Override
             public void describeTo(Description arg0) {
                 arg0.appendValue(overThe);
             }
         };
     }
 
     private BaseMatcher<Long> under(final long underThe) {
         return new BaseMatcher<Long>() {
 
             @Override
             public boolean matches(Object arg0) {
                 if (arg0 instanceof Long) {
                     return (Long) arg0 < underThe;
                 }
                 return false;
             }
 
             @Override
             public void describeTo(Description arg0) {
                 arg0.appendValue(underThe);
             }
         };
     }
 
 }

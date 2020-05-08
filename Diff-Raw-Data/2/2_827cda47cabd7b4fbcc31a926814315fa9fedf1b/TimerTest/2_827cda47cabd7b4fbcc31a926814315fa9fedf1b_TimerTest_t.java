 package bnorm.timer;
 
 import org.junit.Assert;
 import org.junit.Test;
 import org.powermock.api.mockito.PowerMockito;
 
import bnorm.timer.ITimer.TimerState;
 
 /**
  * A group of unit tests for {@link Timer}.
  *
  * @author bnorman
  */
 public class TimerTest {
 
     /**
      * Tests the {@link Timer#getTimerState()} method.
      */
     @Test
     public void testGetTimerState() {
         ITimer timer = new Timer();
         Assert.assertEquals(TimerState.STOPPED, timer.getTimerState());
         timer.reset();
         Assert.assertEquals(TimerState.STOPPED, timer.getTimerState());
         timer.start();
         Assert.assertEquals(TimerState.RUNNING, timer.getTimerState());
         timer.reset();
         Assert.assertEquals(TimerState.RUNNING, timer.getTimerState());
         timer.stop();
         Assert.assertEquals(TimerState.STOPPED, timer.getTimerState());
         timer.reset();
         Assert.assertEquals(TimerState.STOPPED, timer.getTimerState());
         timer.start();
         Assert.assertEquals(TimerState.RUNNING, timer.getTimerState());
         timer.reset();
         Assert.assertEquals(TimerState.RUNNING, timer.getTimerState());
         timer.stop();
         Assert.assertEquals(TimerState.STOPPED, timer.getTimerState());
         timer.reset();
         Assert.assertEquals(TimerState.STOPPED, timer.getTimerState());
     }
 
     /**
      * Tests the {@link Timer#start()} method.
      */
     @Test
     public void testStart() {
         ITimer timer = PowerMockito.spy(new Timer());
         Assert.assertEquals(TimerState.STOPPED, timer.getTimerState());
         Assert.assertEquals(0.0, timer.getStartTime(), 0.0);
 
         PowerMockito.when(timer.getCurrentTime()).thenReturn(1.0);
         timer.start();
         Assert.assertEquals(TimerState.RUNNING, timer.getTimerState());
         Assert.assertEquals(1.0, timer.getStartTime(), 0.0);
 
         PowerMockito.when(timer.getCurrentTime()).thenReturn(2.0);
         timer.start();
         Assert.assertEquals(TimerState.RUNNING, timer.getTimerState());
         Assert.assertEquals(2.0, timer.getStartTime(), 0.0);
     }
 
     /**
      * Tests the {@link Timer#stop()} method.
      */
     @Test
     public void testStop() {
         ITimer timer = PowerMockito.spy(new Timer());
         Assert.assertEquals(TimerState.STOPPED, timer.getTimerState());
         Assert.assertEquals(0.0, timer.getEndTime(), 0.0);
 
         PowerMockito.when(timer.getCurrentTime()).thenReturn(1.0);
         timer.stop();
         Assert.assertEquals(TimerState.STOPPED, timer.getTimerState());
         Assert.assertEquals(0.0, timer.getEndTime(), 0.0);
 
         PowerMockito.when(timer.getCurrentTime()).thenReturn(2.0);
         timer.start();
         Assert.assertEquals(TimerState.RUNNING, timer.getTimerState());
         Assert.assertEquals(Timer.RUNNING_END_TIME, timer.getEndTime(), 0.0);
 
         PowerMockito.when(timer.getCurrentTime()).thenReturn(3.0);
         timer.stop();
         Assert.assertEquals(TimerState.STOPPED, timer.getTimerState());
         Assert.assertEquals(3.0, timer.getEndTime(), 0.0);
 
         PowerMockito.when(timer.getCurrentTime()).thenReturn(4.0);
         timer.stop();
         Assert.assertEquals(TimerState.STOPPED, timer.getTimerState());
         Assert.assertEquals(3.0, timer.getEndTime(), 0.0);
     }
 
     /**
      * Tests the {@link Timer#reset()} method.
      */
     @Test
     public void testReset() {
         ITimer timer = PowerMockito.spy(new Timer());
         Assert.assertEquals(TimerState.STOPPED, timer.getTimerState());
         Assert.assertEquals(0.0, timer.getStartTime(), 0.0);
 
         PowerMockito.when(timer.getCurrentTime()).thenReturn(1.0);
         timer.start();
         Assert.assertEquals(TimerState.RUNNING, timer.getTimerState());
         Assert.assertEquals(1.0, timer.getStartTime(), 0.0);
         Assert.assertEquals(Timer.RUNNING_END_TIME, timer.getEndTime(), 0.0);
 
         PowerMockito.when(timer.getCurrentTime()).thenReturn(2.0);
         timer.reset();
         Assert.assertEquals(TimerState.RUNNING, timer.getTimerState());
         Assert.assertEquals(2.0, timer.getStartTime(), 0.0);
         Assert.assertEquals(Timer.RUNNING_END_TIME, timer.getEndTime(), 0.0);
 
         PowerMockito.when(timer.getCurrentTime()).thenReturn(3.0);
         timer.stop();
         Assert.assertEquals(TimerState.STOPPED, timer.getTimerState());
         Assert.assertEquals(2.0, timer.getStartTime(), 0.0);
         Assert.assertEquals(3.0, timer.getEndTime(), 0.0);
 
         PowerMockito.when(timer.getCurrentTime()).thenReturn(4.0);
         timer.reset();
         Assert.assertEquals(TimerState.STOPPED, timer.getTimerState());
         Assert.assertEquals(0.0, timer.getStartTime(), 0.0);
         Assert.assertEquals(0.0, timer.getEndTime(), 0.0);
     }
 
     /**
      * Tests the {@link Timer#getCurrentTime()} method.
      */
     @Test
     public void testGetCurrentTime() {
         ITimer timer = PowerMockito.spy(new Timer());
 
         PowerMockito.when(timer.getCurrentTime()).thenReturn(0.0);
         Assert.assertEquals(0.0, timer.getCurrentTime(), 0.0);
 
         PowerMockito.when(timer.getCurrentTime()).thenReturn(1.0);
         Assert.assertEquals(1.0, timer.getCurrentTime(), 0.0);
 
         PowerMockito.when(timer.getCurrentTime()).thenReturn(2.0);
         Assert.assertEquals(2.0, timer.getCurrentTime(), 0.0);
 
         PowerMockito.when(timer.getCurrentTime()).thenReturn(-1.0);
         Assert.assertEquals(-1.0, timer.getCurrentTime(), 0.0);
 
         PowerMockito.when(timer.getCurrentTime()).thenReturn(3.14);
         Assert.assertEquals(3.14, timer.getCurrentTime(), 0.0);
     }
 
     /**
      * Tests the {@link Timer#getStartTime()} method.
      */
     @Test
     public void testGetStartTime() {
         // Covered in other cases
     }
 
     /**
      * Tests the {@link Timer#getEndTime()} method.
      */
     @Test
     public void testGetEndTime() {
         // Covered in other cases
     }
 
     /**
      * Tests the {@link Timer#getElapsedTime()} method.
      */
     @Test
     public void testGetElapsedTime() {
         ITimer timer = PowerMockito.spy(new Timer());
         Assert.assertEquals(TimerState.STOPPED, timer.getTimerState());
         Assert.assertEquals(0.0, timer.getElapsedTime(), 0.0);
 
         PowerMockito.when(timer.getCurrentTime()).thenReturn(1.0);
         timer.start();
         Assert.assertEquals(0.0, timer.getElapsedTime(), 0.0);
 
         PowerMockito.when(timer.getCurrentTime()).thenReturn(2.0);
         Assert.assertEquals(1.0, timer.getElapsedTime(), 0.0);
         timer.reset();
         Assert.assertEquals(0.0, timer.getElapsedTime(), 0.0);
 
         PowerMockito.when(timer.getCurrentTime()).thenReturn(3.0);
         Assert.assertEquals(1.0, timer.getElapsedTime(), 0.0);
         timer.stop();
         Assert.assertEquals(1.0, timer.getElapsedTime(), 0.0);
 
         PowerMockito.when(timer.getCurrentTime()).thenReturn(4.0);
         Assert.assertEquals(1.0, timer.getElapsedTime(), 0.0);
         timer.start();
         Assert.assertEquals(0.0, timer.getElapsedTime(), 0.0);
 
         PowerMockito.when(timer.getCurrentTime()).thenReturn(5.0);
         Assert.assertEquals(1.0, timer.getElapsedTime(), 0.0);
 
         PowerMockito.when(timer.getCurrentTime()).thenReturn(6.0);
         Assert.assertEquals(2.0, timer.getElapsedTime(), 0.0);
         timer.stop();
         Assert.assertEquals(2.0, timer.getElapsedTime(), 0.0);
 
         PowerMockito.when(timer.getCurrentTime()).thenReturn(7.0);
         Assert.assertEquals(2.0, timer.getElapsedTime(), 0.0);
         timer.reset();
         Assert.assertEquals(0.0, timer.getElapsedTime(), 0.0);
     }
 }

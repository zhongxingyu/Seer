 package uk.co.acuminous.julez.event.pipe;
 
 import static org.junit.Assert.assertSame;
 
 import org.junit.Test;
 
 import uk.co.acuminous.julez.event.TestEvent;
 import uk.co.acuminous.julez.event.handler.EventMonitor;
 
 public class FanOutPipeTest {
 
     @Test
     public void forwardsEventsToAllHandlers() {
         EventMonitor handler1 = new EventMonitor();
         EventMonitor handler2 = new EventMonitor();
         
         FanOutPipe pipe = new FanOutPipe();
         pipe.registerAll(handler1, handler2);
         
         TestEvent event = new TestEvent();
         pipe.onEvent(event);
         
         assertSame(event, handler1.getEvents().get(0));
        assertSame(event, handler1.getEvents().get(0));
     }
     
 }

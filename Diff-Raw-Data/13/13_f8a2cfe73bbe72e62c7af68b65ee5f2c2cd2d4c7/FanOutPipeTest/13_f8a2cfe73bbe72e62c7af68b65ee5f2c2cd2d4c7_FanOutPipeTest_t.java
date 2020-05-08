 package uk.co.acuminous.julez.event.pipe;
 
import static org.junit.Assert.assertEquals;
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
         
        assertEquals(1, handler1.getEvents().size());
         assertSame(event, handler1.getEvents().get(0));
        
        assertEquals(1, handler2.getEvents().size());        
        assertSame(event, handler2.getEvents().get(0));
    }
    
    @Test
    public void tolleratesNoHandlers() {        
        new FanOutPipe().onEvent(new TestEvent());
     }
     
 }

 package ch9k.eventpool;
 
 import org.junit.Before;
 import org.junit.Test;
 import static org.junit.Assert.*;
 import static org.easymock.EasyMock.*;
 
 /**
  * @author Pieter De Baets
  */
 public class EventPoolTest {
     private EventPool pool;
 
     @Before
     public void setUp() {
         pool = new EventPool();
     }
 
     /**
      * Test of raiseEvent method, of class EventPool.
      * @throws InterruptedException 
      */
     @Test
     public void testRaiseEvent() throws InterruptedException {
         Event event = new MyEvent();
 
         EventListener listener = createMock(EventListener.class);
         listener.handleEvent(event);
         replay(listener); // the previous calls to listener should now be repeated
 
         pool.addListener(listener, new TypeEventFilter(MyEvent.class));
         pool.raiseEvent(event);
        Thread.sleep(50); // wait for the event to be propagated
         verify(listener); // assert that the event was received
     }
 
     public class MyEvent extends Event {
         @Override
         public Object getSource() {
             return null;
         }
     }
 }

 package org.kahuna.main;
 
import static org.junit.Assert.*;

 import static org.hamcrest.CoreMatchers.*;
 
 import org.junit.*;
 
 public class EventLoopTest {
 
    @Test
    public void sendEvent() {
        EventLoop eventLoop = new EventLoop();
        eventLoop.registerEventHandler(new EventHandler() {
            public void handle(Event event) {
                assertThat(event.toString(), is("myEvent"));
            }
        });
        eventLoop.sendEvent(new Event("myEvent"));
    }
 
 }

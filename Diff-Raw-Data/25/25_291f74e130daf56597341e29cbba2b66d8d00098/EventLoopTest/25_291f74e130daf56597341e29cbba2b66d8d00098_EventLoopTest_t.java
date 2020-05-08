 package org.kahuna.main;
 
 import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
 
 import org.junit.*;
 
 public class EventLoopTest {
 
	@Test
	public void sendEvent() {
		EventLoop eventLoop = EventLoop.getInstance();
		eventLoop.registerEventHandler(new EventHandler() {
			public void handle(Event event) {
				assertThat(event.toString(), is("myEvent"));
			}
		});
		eventLoop.sendEvent(new Event("myEvent"));
	}
 
 }

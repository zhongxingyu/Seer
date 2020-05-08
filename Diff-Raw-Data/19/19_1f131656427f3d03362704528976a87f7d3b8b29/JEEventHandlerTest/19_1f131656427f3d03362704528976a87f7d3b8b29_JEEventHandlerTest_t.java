 package commons.sim.jeevent;
 
 
 import static org.junit.Assert.assertEquals;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import commons.io.Checkpointer;
 
 import util.CleanConfigurationTest;
 
 public class JEEventHandlerTest extends CleanConfigurationTest {
 	
 	private JEEventScheduler scheduler;
 
 	@Before
	public void setUp() throws Exception{
		super.setUp();
		scheduler = new JEEventScheduler(Checkpointer.INTERVAL);
 	}
 
 	@Test
 	public void testJEEventHandler(){
 		JEEventHandler handler = new JEAbstractEventHandler(scheduler){
			/**
			 * 
			 */
			private static final long serialVersionUID = 8253763434426330037L;

 			@Override
 			public void handleEvent(JEEvent event) {/* Empty implementation. */}
 		};
 		assertEquals(handler, scheduler.getHandler(handler.getHandlerId()));
 	}
 
 }

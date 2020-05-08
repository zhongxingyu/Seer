 package engine.agent.evan.test;
 
 
 import java.util.*;
 import java.util.concurrent.Semaphore;
 
 import org.junit.Test;
 
 import shared.Glass;
 import shared.enums.*;
 import transducer.*;
import engine.agent.david.test.mock.MockWorkstation;
 import engine.agent.evan.*;
 import engine.agent.evan.test.mock.*;
 import junit.framework.TestCase;
 
 public class PopupAgentUnitTest extends TestCase {
 	
 	//the agent to be tested
 	PopupAgent popup;
 	
 	/*
 	 * This is a long, complicated test that goes through everything the PopupAgent ever has to do.
 	 * First come two consecutive pieces of glass both needing machine processing, so the popup loads both of the machines.
 	 * Then comes an arbitrary number of glass not needing processing (in this case 5), and the popup sends them through.
 	 * When a piece of glass needing to be processed comes, the popup will not take it because both machines are occupied,
 	 * so there is nowhere for it to go. When one of the machines finishes processing the glass, the popup releases it from the
 	 * machine and sends it to the next CF.  Then it loads the now empty machine with the previously pending glass needing processing.
 	 * Then the popup waits for the machines to finish processing the two current pieces of glass and sequentially releases and then
 	 * sends on each of them.
 	 */
 	public void testPopupProcesses() {
 		MockConveyorFamily cf = new MockConveyorFamily("cf1");
 		MockConveyor c = new MockConveyor("c1");
 		Transducer t = new Transducer();
 		t.startTransducer();
 		
 		// for waiting for other threads to act
 		Timer timer = new Timer();
 		Semaphore sem = new Semaphore(0);
 		
 		int id = 100;
 		// create arguments for transducer events
 		Integer[] idArgs = {id}, m0Args = {0}, m1Args = {1};
 		
 		// the type of the machines adjacent to the popup
 		MachineType mt = MachineType.DRILL;
 		TChannel mtc = TChannel.DRILL;
 		
 		// channels for mock animation to listen to
 		TChannel[] channels = {TChannel.POPUP, mtc};
 		MockAnimation anim = new MockAnimation("anim1", t, channels);
 		
 		MockWorkstation[] mach = new MockWorkstation[2];
 		mach[0] = new MockWorkstation("ws0"); mach[1] = new MockWorkstation("ws1");
 		
 		popup = new PopupAgent("p" + id, cf, c, mach, mt, t, id);
 		
 		// popup agent has semaphores and waits for animation so it needs to be tested in its own thread
 		// (calling pickAndExecuteAnAction can result in acquiring a semaphore that must be released by another thread)
 		popup.startThread();
 		
 		// create glass, one list for glass needing processing, one for glass not needing processing
 		int numGlass = 10;
 		Glass[] gp = new Glass[numGlass]; // g - processing
 		MachineType[] recipe = {mt};
 		for (int i = 0; i < numGlass; ++i)
 			gp[i] = new Glass(recipe);
 		
 		Glass[] gnp = new Glass[numGlass]; // g - no processing
 		for (int i = 0; i < numGlass; ++i)
 			gnp[i] = new Glass();
 		
 		int waitTime = 10; // ms to wait for other threads to act
 		
 		{ /* Loading machine 0 block */
 			popup.msgNextGlass(gp[0]); // gs == pending
 			
 			wait(timer, sem, waitTime); // should tell mock conveyor free, and then start waiting
 			// shouldn't tell animation to move down because popup starts down (if it does, this will be caught later)
 			
 			assertTrue(
 					"Mock conveyor should have been told popup is open. Event log: "
 							+ c.log.toString(), c.log
 							.containsString("Received message msgPositionFree."));
 			assertEquals(
 					"Only 1 message should have been sent to the conveyor. Event log: "
 							+ c.log.toString(), 1, c.log.size());
 			assertTrue("Popup should be waiting for animation.", popup.isWaiting(0));
 			
 			// tell popup glass has been loaded, this should make popup stop waiting
 			t.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_LOAD_FINISHED, idArgs);
 			wait(timer, sem, waitTime);
 			assertFalse("Popup should no longer be waiting for animation.", popup.isWaiting(0));
 			
 			// gs == needsProcessing && mFree[0]
 			// sched -> gs == atMachine, should tell anim to move up
 			
 			assertTrue(
 					"Mock animation should have been told to move up popup. Event log: "
 					+ anim.log.toString(), anim.log.containsString(
 					"Channel: " + TChannel.POPUP + ", Event: " + TEvent.POPUP_DO_MOVE_UP + ", Arguments: " + id + "."));
 			assertEquals(
 					"Only 2 events should have been sent to the animation. Event log: " // half from test
 							+ anim.log.toString(), 2, anim.log.size());
 			assertTrue("Popup should be waiting for animation.", popup.isWaiting(1));
 			
 			// tell popup agent it has moved up, this should make popup stop waiting
 			t.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_UP, idArgs);
 			wait(timer, sem, waitTime);
 			assertFalse("Popup should no longer be waiting for animation.", popup.isWaiting(1));
 			
 			// popup should send glass to machine mock 0, tell anim to load machine, and wait
 			assertTrue(
 					"Mock machine 0 should have been given glass. Event log: "
 							+ mach[0].log.toString(), mach[0].log
 							.containsString("Received message msgHereIsGlass for " + gp[0] + "."));
 			assertEquals(
 					"Only 1 message should have been sent to the mock machine. Event log: "
 							+ mach[0].log.toString(), 1, mach[0].log.size());
 			assertTrue(
 					"Mock animation should have been told to load machine 0. Event log: "
 					+ anim.log.toString(), anim.log.containsString(
 					"Channel: " + mtc + ", Event: " + TEvent.WORKSTATION_DO_LOAD_GLASS + ", Arguments: " + 0 + "."));
 			assertEquals(
 					"Only 4 events should have been sent to the animation. Event log: " // half from test
 							+ anim.log.toString(), 4, anim.log.size());
 			assertTrue("Popup should be waiting for animation.", popup.isWaiting(4));
 			
 			// tell popup agent machine load finished, this should make popup stop waiting
 			t.fireEvent(mtc, TEvent.WORKSTATION_LOAD_FINISHED, m0Args);
 			wait(timer, sem, waitTime);
 			assertFalse("Popup should no longer be waiting for animation.", popup.isWaiting(4));
 		} /* Glass is now being processed on machine 0 and popup can accept new glass. */
 		
 		c.log.clear();
 		anim.log.clear();
 		mach[0].log.clear();
 		
 		{ /* Loading machine 1 block */
 			popup.msgNextGlass(gp[1]); // gs == pending
 			
 			wait(timer, sem, waitTime); // should tell anim to move down and wait
 			
 			assertTrue(
 					"Mock animation should have been told to move down popup. Event log: "
 					+ anim.log.toString(), anim.log.containsString(
 					"Channel: " + TChannel.POPUP + ", Event: " + TEvent.POPUP_DO_MOVE_DOWN + ", Arguments: " + id + "."));
 			assertEquals(
 					"Only 1 event should have been sent to the animation. Event log: "
 							+ anim.log.toString(), 1, anim.log.size());
 			assertTrue("Popup should be waiting for animation.", popup.isWaiting(2));
 			
 			// tell popup agent it has moved up, this should make popup stop waiting
 			t.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_DOWN, idArgs);
 			wait(timer, sem, waitTime);
 			assertFalse("Popup should no longer be waiting for animation.", popup.isWaiting(2));
 			
 			// should tell mock conveyor open and wait
 			
 			assertTrue(
 					"Mock conveyor should have been told popup is open. Event log: "
 							+ c.log.toString(), c.log
 							.containsString("Received message msgPositionFree."));
 			assertEquals(
 					"Only 1 message should have been sent to the conveyor. Event log: "
 							+ c.log.toString(), 1, c.log.size());
 			assertTrue("Popup should be waiting for animation.", popup.isWaiting(0));
 			
 			// tell popup glass has been loaded, this should make popup stop waiting
 			t.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_LOAD_FINISHED, idArgs);
 			wait(timer, sem, waitTime);
 			assertFalse("Popup should no longer be waiting for animation.", popup.isWaiting(0));
 			
 			// gs == needsProcessing && mFree[1]
 			// sched -> gs == atMachine, should tell anim to move up
 			
 			assertTrue(
 					"Mock animation should have been told to move up popup. Event log: "
 					+ anim.log.toString(), anim.log.containsString(
 					"Channel: " + TChannel.POPUP + ", Event: " + TEvent.POPUP_DO_MOVE_UP + ", Arguments: " + id + "."));
 			assertEquals(
 					"Only 4 events should have been sent to the animation. Event log: " // half from test
 							+ anim.log.toString(), 4, anim.log.size());
 			assertTrue("Popup should be waiting for animation.", popup.isWaiting(1));
 			
 			// tell popup agent it has moved up, this should make popup stop waiting
 			t.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_UP, idArgs);
 			wait(timer, sem, waitTime);
 			assertFalse("Popup should no longer be waiting for animation.", popup.isWaiting(1));
 			
 			// popup should send glass to machine mock 1, tell anim to load machine, and wait
 			assertTrue(
 					"Mock machine 1 should have been given glass. Event log: "
 							+ mach[1].log.toString(), mach[1].log
 							.containsString("Received message msgHereIsGlass for " + gp[1] + "."));
 			assertEquals(
 					"Only 1 message should have been sent to the mock machine. Event log: "
 							+ mach[1].log.toString(), 1, mach[1].log.size());
 			assertTrue(
 					"Mock animation should have been told to load machine 1. Event log: "
 					+ anim.log.toString(), anim.log.containsString(
 					"Channel: " + mtc + ", Event: " + TEvent.WORKSTATION_DO_LOAD_GLASS + ", Arguments: " + 1 + "."));
 			assertEquals(
 					"Only 6 events should have been sent to the animation. Event log: " // half from test
 							+ anim.log.toString(), 6, anim.log.size());
 			assertTrue("Popup should be waiting for animation.", popup.isWaiting(4));
 			
 			// tell popup agent machine load finished, this should make popup stop waiting
 			t.fireEvent(mtc, TEvent.WORKSTATION_LOAD_FINISHED, m1Args);
 			wait(timer, sem, waitTime);
 			assertFalse("Popup should no longer be waiting for animation.", popup.isWaiting(4));
 		}
 		
 		/* Glass is now being processed on both machines and popup can only accept new glass if it doesn't need processing. */
 		
 		c.log.clear();
 		anim.log.clear();
 		mach[1].log.clear();
 		
 		/* Allow glass that doesn't need processing to go through. */
 		for (int i = 0; i < 5; ++i) {
 			popup.msgNextGlass(gnp[i]); // gs == pending
 			
 			wait(timer, sem, waitTime); // should tell anim to move down and wait
 			
 			if (i == 0) { // popup only has to move down the first time
 				assertTrue(
 						"Mock animation should have been told to move down popup. Event log: "
 						+ anim.log.toString(), anim.log.containsString(
 						"Channel: " + TChannel.POPUP + ", Event: " + TEvent.POPUP_DO_MOVE_DOWN + ", Arguments: " + id + "."));
 				assertEquals(
 						"Only 1 event should have been sent to the animation. Event log: "
 								+ anim.log.toString(), 1, anim.log.size());
 				assertTrue("Popup should be waiting for animation.", popup.isWaiting(2));
 				
 				// tell popup agent it has moved down, this should make popup stop waiting
 				t.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_DOWN, idArgs);
 				wait(timer, sem, waitTime);
 				assertFalse("Popup should no longer be waiting for animation.", popup.isWaiting(2));
 				
 				anim.log.clear();
 			}
 			
 			// should tell mock conveyor open and wait
 			
 			assertTrue(
 					"Mock conveyor should have been told popup is open. Event log: "
 							+ c.log.toString(), c.log
 							.containsString("Received message msgPositionFree."));
 			assertEquals(
 					"Only 1 message should have been sent to the conveyor. Event log: "
 							+ c.log.toString(), 1, c.log.size());
 			assertTrue("Popup should be waiting for animation.", popup.isWaiting(0));
 			
 			// tell popup glass has been loaded, this should make popup stop waiting
 			t.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_LOAD_FINISHED, idArgs);
 			wait(timer, sem, waitTime);
 			assertFalse("Popup should no longer be waiting for animation.", popup.isWaiting(0));
 			
 			// gs == waiting && posFree == false -> nothing should happen
 			
 			assertEquals(
 					"Only 1 events should have been sent to the animation. Event log: " // from test
 							+ anim.log.toString(), 1, anim.log.size());
 			
 			popup.msgPositionFree();
 			wait(timer, sem, waitTime);
 			
 			// gs == waiting && posFree == true -> should send glass to next CF and tell anim to release glass
 			
 			assertTrue(
 					"Mock CF should have been sent glass. Event log: "
 							+ cf.log.toString(), cf.log
 							.containsString("Received message msgHereIsGlass for " + gnp[i] + "."));
 			assertEquals(
 					"Only 1 message should have been sent to the CF. Event log: "
 							+ cf.log.toString(), 1, cf.log.size());
 			assertTrue(
 					"Mock animation should have been told to release glass. Event log: "
 					+ anim.log.toString(), anim.log.containsString(
 					"Channel: " + TChannel.POPUP + ", Event: " + TEvent.POPUP_RELEASE_GLASS + ", Arguments: " + id + "."));
 			assertEquals(
 					"Only 2 events should have been sent to the animation. Event log: " // half from test
 							+ anim.log.toString(), 2, anim.log.size());
 			assertTrue("Popup should be waiting for animation.", popup.isWaiting(3));
 			
 			// tell popup agent it has finished releasing glass, this should make popup stop waiting
 			t.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_RELEASE_FINISHED, idArgs);
 			wait(timer, sem, waitTime);
 			assertFalse("Popup should no longer be waiting for animation.", popup.isWaiting(3));
 			
 			assertEquals(
 					"Only 3 events should have been sent to the animation. Event log: " // 3 from test
 							+ anim.log.toString(), 3, anim.log.size());
 			assertEquals(
 					"Only 1 message should have been sent to the CF. Event log: "
 							+ cf.log.toString(), 1, cf.log.size());
 			assertEquals(
 					"Only 1 message should have been sent to the conveyor. Event log: "
 							+ c.log.toString(), 1, c.log.size());
 			
 			anim.log.clear();
 			cf.log.clear();
 			c.log.clear();
 		}
 		
 		/* Popup shouldn't accept the next piece of glass if it requires processing and both machines are occupied. */
 		
 		popup.msgNextGlass(gp[2]); // gs == pending, both machines occupied
 		wait(timer, sem, waitTime);
 		
 		assertEquals(
 				"No events should have been sent to the animation. Event log: "
 						+ anim.log.toString(), 0, anim.log.size());
 		
 		{ /* Remove processed glass gp[1] from machine 1 and release. */
 			popup.msgGlassDone(gp[1], 1);
 			wait(timer, sem, waitTime); // should tell anim to move up and wait
 			
 			assertTrue(
 					"Mock animation should have been told to move up popup. Event log: "
 					+ anim.log.toString(), anim.log.containsString(
 					"Channel: " + TChannel.POPUP + ", Event: " + TEvent.POPUP_DO_MOVE_UP + ", Arguments: " + id + "."));
 			assertEquals(
 					"Only 1 event should have been sent to the animation. Event log: " // half from test
 							+ anim.log.toString(), 1, anim.log.size());
 			assertTrue("Popup should be waiting for animation.", popup.isWaiting(1));
 			
 			// tell popup agent it has moved up, this should make popup stop waiting
 			t.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_UP, idArgs);
 			wait(timer, sem, waitTime);
 			assertFalse("Popup should no longer be waiting for animation.", popup.isWaiting(1));
 			
 			// should tell animation to release glass from machine
 			assertTrue(
 					"Mock animation should have been told to release glass from machine 1. Event log: "
 					+ anim.log.toString(), anim.log.containsString(
 					"Channel: " + mtc + ", Event: " + TEvent.WORKSTATION_RELEASE_GLASS + ", Arguments: " + 1 + "."));
 			assertEquals(
 					"Only 3 events should have been sent to the animation. Event log: " // 1 from test
 							+ anim.log.toString(), 3, anim.log.size());
 			assertTrue("Popup should be waiting for animation.", popup.isWaiting(0));
 			
 			// tell popup glass has been loaded, this should make popup stop waiting
 			t.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_LOAD_FINISHED, idArgs);
 			wait(timer, sem, waitTime);
 			assertFalse("Popup should no longer be waiting for animation.", popup.isWaiting(0));
 			
 			// should move popup down
 			assertTrue(
 					"Mock animation should have been told to move down popup. Event log: "
 					+ anim.log.toString(), anim.log.containsString(
 					"Channel: " + TChannel.POPUP + ", Event: " + TEvent.POPUP_DO_MOVE_DOWN + ", Arguments: " + id + "."));
 			assertEquals(
 					"Only 5 events should have been sent to the animation. Event log: " // 2 from test
 							+ anim.log.toString(), 5, anim.log.size());
 			assertTrue("Popup should be waiting for animation.", popup.isWaiting(2));
 			
 			// tell popup agent it has moved down, this should make popup stop waiting
 			t.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_DOWN, idArgs);
 			wait(timer, sem, waitTime);
 			assertFalse("Popup should no longer be waiting for animation.", popup.isWaiting(2));
 			
 			// should wait to receive message that next CF is free
 			assertEquals(
 					"Only 6 events should have been sent to the animation. Event log: " // 3 from test
 							+ anim.log.toString(), 6, anim.log.size());
 			
 			anim.log.clear();
 			
 			// send glass to next CF
 			popup.msgPositionFree();
 			wait(timer, sem, waitTime);
 			
 			// gs == waiting && posFree == true -> should send glass to next CF and tell anim to release glass
 			
 			assertTrue(
 					"Mock CF should have been sent glass. Event log: "
 							+ cf.log.toString(), cf.log
 							.containsString("Received message msgHereIsGlass for " + gp[1] + "."));
 			assertEquals(
 					"Only 1 message should have been sent to the CF. Event log: "
 							+ cf.log.toString(), 1, cf.log.size());
 			assertTrue(
 					"Mock animation should have been told to release glass. Event log: "
 					+ anim.log.toString(), anim.log.containsString(
 					"Channel: " + TChannel.POPUP + ", Event: " + TEvent.POPUP_RELEASE_GLASS + ", Arguments: " + id + "."));
 			assertEquals(
 					"Only 1 event should have been sent to the animation. Event log: "
 							+ anim.log.toString(), 1, anim.log.size());
 			assertTrue("Popup should be waiting for animation.", popup.isWaiting(3));
 			
 			// tell popup agent it has finished releasing glass, this should make popup stop waiting
 			t.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_RELEASE_FINISHED, idArgs);
 			wait(timer, sem, waitTime);
 			assertFalse("Popup should no longer be waiting for animation.", popup.isWaiting(3));
 			
 			assertEquals(
 					"Only 2 events should have been sent to the animation. Event log: " // 3 from test
 							+ anim.log.toString(), 2, anim.log.size());
 			assertEquals(
 					"Only 1 message should have been sent to the CF. Event log: "
 							+ cf.log.toString(), 1, cf.log.size());
 			assertEquals(
 					"Only 1 message should have been sent to the conveyor. Event log: "
 							+ c.log.toString(), 1, c.log.size());
 			
 			anim.log.clear();
 			cf.log.clear();
 		}
 		
 		{ /* Load machine 1 with previously pending glass gp[2]. */
 			// popup should have told mock conveyor open, and popup should be waiting for gp[2] now
 			assertTrue(
 					"Mock conveyor should have been told popup is open. Event log: "
 							+ c.log.toString(), c.log
 							.containsString("Received message msgPositionFree."));
 			assertEquals(
 					"Only 1 message should have been sent to the conveyor. Event log: "
 							+ c.log.toString(), 1, c.log.size());
 			assertTrue("Popup should be waiting for animation.", popup.isWaiting(0));
 			
 			// tell popup glass has been loaded, this should make popup stop waiting
 			t.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_LOAD_FINISHED, idArgs);
 			wait(timer, sem, waitTime);
 			assertFalse("Popup should no longer be waiting for animation.", popup.isWaiting(0));
 			
 			// gs == needsProcessing && mFree[1]
 			// sched -> gs == atMachine, should tell anim to move up
 			
 			assertTrue(
 					"Mock animation should have been told to move up popup. Event log: "
 					+ anim.log.toString(), anim.log.containsString(
 					"Channel: " + TChannel.POPUP + ", Event: " + TEvent.POPUP_DO_MOVE_UP + ", Arguments: " + id + "."));
 			assertEquals(
 					"Only 2 events should have been sent to the animation. Event log: " // half from test
 							+ anim.log.toString(), 2, anim.log.size());
 			assertTrue("Popup should be waiting for animation.", popup.isWaiting(1));
 			
 			// tell popup agent it has moved up, this should make popup stop waiting
 			t.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_UP, idArgs);
 			wait(timer, sem, waitTime);
 			assertFalse("Popup should no longer be waiting for animation.", popup.isWaiting(1));
 			
 			// popup should send glass to machine mock 1, tell anim to load machine, and wait
 			assertTrue(
 					"Mock machine 1 should have been given glass. Event log: "
 							+ mach[1].log.toString(), mach[1].log
 							.containsString("Received message msgHereIsGlass for " + gp[2] + "."));
 			assertEquals(
 					"Only 1 message should have been sent to the mock machine. Event log: "
 							+ mach[1].log.toString(), 1, mach[1].log.size());
 			assertTrue(
 					"Mock animation should have been told to load machine 1. Event log: "
 					+ anim.log.toString(), anim.log.containsString(
 					"Channel: " + mtc + ", Event: " + TEvent.WORKSTATION_DO_LOAD_GLASS + ", Arguments: " + 1 + "."));
 			assertEquals(
 					"Only 4 events should have been sent to the animation. Event log: " // half from test
 							+ anim.log.toString(), 4, anim.log.size());
 			assertTrue("Popup should be waiting for animation.", popup.isWaiting(4));
 			
 			// tell popup agent machine load finished, this should make popup stop waiting
 			t.fireEvent(mtc, TEvent.WORKSTATION_LOAD_FINISHED, m1Args);
 			wait(timer, sem, waitTime);
 			assertFalse("Popup should no longer be waiting for animation.", popup.isWaiting(4));
 			
 			anim.log.clear();
 			cf.log.clear();
 			c.log.clear();
 		}
 		
 		{ /* Remove processed glass gp[0] from machine 0 and release. */
 			popup.msgGlassDone(gp[0], 0);
 			wait(timer, sem, waitTime); // should tell anim to move up and wait
 			
 			// should tell animation to release glass from machine
 			assertTrue(
 					"Mock animation should have been told to release glass from machine 0. Event log: "
 					+ anim.log.toString(), anim.log.containsString(
 					"Channel: " + mtc + ", Event: " + TEvent.WORKSTATION_RELEASE_GLASS + ", Arguments: " + 0 + "."));
 			assertEquals(
 					"Only 1 event should have been sent to the animation. Event log: "
 							+ anim.log.toString(), 1, anim.log.size());
 			assertTrue("Popup should be waiting for animation.", popup.isWaiting(0));
 			
 			// tell popup glass has been loaded, this should make popup stop waiting
 			t.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_LOAD_FINISHED, idArgs);
 			wait(timer, sem, waitTime);
 			assertFalse("Popup should no longer be waiting for animation.", popup.isWaiting(0));
 			
 			// should move popup down
 			assertTrue(
 					"Mock animation should have been told to move down popup. Event log: "
 					+ anim.log.toString(), anim.log.containsString(
 					"Channel: " + TChannel.POPUP + ", Event: " + TEvent.POPUP_DO_MOVE_DOWN + ", Arguments: " + id + "."));
 			assertEquals(
 					"Only 3 events should have been sent to the animation. Event log: " // 1 from test
 							+ anim.log.toString(), 3, anim.log.size());
 			assertTrue("Popup should be waiting for animation.", popup.isWaiting(2));
 			
 			// tell popup agent it has moved down, this should make popup stop waiting
 			t.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_DOWN, idArgs);
 			wait(timer, sem, waitTime);
 			assertFalse("Popup should no longer be waiting for animation.", popup.isWaiting(2));
 			
 			// should wait to receive message that next CF is free
 			assertEquals(
 					"Only 4 events should have been sent to the animation. Event log: " // 3 from test
 							+ anim.log.toString(), 4, anim.log.size());
 			
 			anim.log.clear();
 			
 			// send glass to next CF
 			popup.msgPositionFree();
 			wait(timer, sem, waitTime);
 			
 			// gs == waiting && posFree == true -> should send glass to next CF and tell anim to release glass
 			
 			assertTrue(
 					"Mock CF should have been sent glass. Event log: "
 							+ cf.log.toString(), cf.log
 							.containsString("Received message msgHereIsGlass for " + gp[0] + "."));
 			assertEquals(
 					"Only 1 message should have been sent to the CF. Event log: "
 							+ cf.log.toString(), 1, cf.log.size());
 			assertTrue(
 					"Mock animation should have been told to release glass. Event log: "
 					+ anim.log.toString(), anim.log.containsString(
 					"Channel: " + TChannel.POPUP + ", Event: " + TEvent.POPUP_RELEASE_GLASS + ", Arguments: " + id + "."));
 			assertEquals(
 					"Only 1 event should have been sent to the animation. Event log: "
 							+ anim.log.toString(), 1, anim.log.size());
 			assertTrue("Popup should be waiting for animation.", popup.isWaiting(3));
 			
 			// tell popup agent it has finished releasing glass, this should make popup stop waiting
 			t.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_RELEASE_FINISHED, idArgs);
 			wait(timer, sem, waitTime);
 			assertFalse("Popup should no longer be waiting for animation.", popup.isWaiting(3));
 			
 			assertEquals(
 					"Only 2 events should have been sent to the animation. Event log: " // 3 from test
 							+ anim.log.toString(), 2, anim.log.size());
 			assertEquals(
 					"Only 1 message should have been sent to the CF. Event log: "
 							+ cf.log.toString(), 1, cf.log.size());
 			assertEquals(
 					"No messages should have been sent to the conveyor. Event log: "
 							+ c.log.toString(), 0, c.log.size());
 			
 			anim.log.clear();
 			cf.log.clear();
 		}
 		
 		{ /* Remove processed glass gp[2] from machine 1 and release. */
 			popup.msgGlassDone(gp[2], 1);
 			wait(timer, sem, waitTime); // should tell anim to move up and wait
 			
 			assertTrue(
 					"Mock animation should have been told to move up popup. Event log: "
 					+ anim.log.toString(), anim.log.containsString(
 					"Channel: " + TChannel.POPUP + ", Event: " + TEvent.POPUP_DO_MOVE_UP + ", Arguments: " + id + "."));
 			assertEquals(
 					"Only 1 event should have been sent to the animation. Event log: " // half from test
 							+ anim.log.toString(), 1, anim.log.size());
 			assertTrue("Popup should be waiting for animation.", popup.isWaiting(1));
 			
 			// tell popup agent it has moved up, this should make popup stop waiting
 			t.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_UP, idArgs);
 			wait(timer, sem, waitTime);
 			assertFalse("Popup should no longer be waiting for animation.", popup.isWaiting(1));
 			
 			// should tell animation to release glass from machine
 			assertTrue(
 					"Mock animation should have been told to release glass from machine 1. Event log: "
 					+ anim.log.toString(), anim.log.containsString(
 					"Channel: " + mtc + ", Event: " + TEvent.WORKSTATION_RELEASE_GLASS + ", Arguments: " + 1 + "."));
 			assertEquals(
 					"Only 3 events should have been sent to the animation. Event log: " // 1 from test
 							+ anim.log.toString(), 3, anim.log.size());
 			assertTrue("Popup should be waiting for animation.", popup.isWaiting(0));
 			
 			// tell popup glass has been loaded, this should make popup stop waiting
 			t.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_LOAD_FINISHED, idArgs);
 			wait(timer, sem, waitTime);
 			assertFalse("Popup should no longer be waiting for animation.", popup.isWaiting(0));
 			
 			// should move popup down
 			assertTrue(
 					"Mock animation should have been told to move down popup. Event log: "
 					+ anim.log.toString(), anim.log.containsString(
 					"Channel: " + TChannel.POPUP + ", Event: " + TEvent.POPUP_DO_MOVE_DOWN + ", Arguments: " + id + "."));
 			assertEquals(
 					"Only 5 events should have been sent to the animation. Event log: " // 2 from test
 							+ anim.log.toString(), 5, anim.log.size());
 			assertTrue("Popup should be waiting for animation.", popup.isWaiting(2));
 			
 			// tell popup agent it has moved down, this should make popup stop waiting
 			t.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_DOWN, idArgs);
 			wait(timer, sem, waitTime);
 			assertFalse("Popup should no longer be waiting for animation.", popup.isWaiting(2));
 			
 			// should wait to receive message that next CF is free
 			assertEquals(
 					"Only 6 events should have been sent to the animation. Event log: " // 3 from test
 							+ anim.log.toString(), 6, anim.log.size());
 			
 			anim.log.clear();
 			
 			// send glass to next CF
 			popup.msgPositionFree();
 			wait(timer, sem, waitTime);
 			
 			// gs == waiting && posFree == true -> should send glass to next CF and tell anim to release glass
 			
 			assertTrue(
 					"Mock CF should have been sent glass. Event log: "
 							+ cf.log.toString(), cf.log
 							.containsString("Received message msgHereIsGlass for " + gp[2] + "."));
 			assertEquals(
 					"Only 1 message should have been sent to the CF. Event log: "
 							+ cf.log.toString(), 1, cf.log.size());
 			assertTrue(
 					"Mock animation should have been told to release glass. Event log: "
 					+ anim.log.toString(), anim.log.containsString(
 					"Channel: " + TChannel.POPUP + ", Event: " + TEvent.POPUP_RELEASE_GLASS + ", Arguments: " + id + "."));
 			assertEquals(
 					"Only 1 event should have been sent to the animation. Event log: "
 							+ anim.log.toString(), 1, anim.log.size());
 			assertTrue("Popup should be waiting for animation.", popup.isWaiting(3));
 			
 			// tell popup agent it has finished releasing glass, this should make popup stop waiting
 			t.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_RELEASE_FINISHED, idArgs);
 			wait(timer, sem, waitTime);
 			assertFalse("Popup should no longer be waiting for animation.", popup.isWaiting(3));
 			
 			assertEquals(
 					"Only 2 events should have been sent to the animation. Event log: " // 3 from test
 							+ anim.log.toString(), 2, anim.log.size());
 			assertEquals(
 					"Only 1 message should have been sent to the CF. Event log: "
 							+ cf.log.toString(), 1, cf.log.size());
 			assertEquals(
 					"No messages should have been sent to the conveyor. Event log: "
 							+ c.log.toString(), 0, c.log.size());
 			
 			anim.log.clear();
 			cf.log.clear();
 		}
 	}
 	
 	/* Waits. Used to wait for the other threads. */
 	private void wait(Timer timer, final Semaphore sem, int time) {
 		timer.schedule(new TimerTask(){ public void run() {		    
 			sem.release(); } }, time); // sem will be released after <time> ms
 		try {
 			sem.acquire();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 }

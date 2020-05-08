 package ultraextreme.controller;
 
 import static org.junit.Assert.*;
 
 import org.andengine.entity.scene.Scene;
 import org.junit.Test;
 
 /**
  * 
  * @author Bjorn Persson Mattsson
  * 
  */
 public class AbstractControllerTest {
 
 	private static boolean listenerUpdated;
 
 	@Test
 	public void testAddListener() {
 		AbstractController testController = new AbstractController() {
 
 			@Override
 			public Scene getScene() {
 				return null;
 			}

			@Override
			public void activateController() {
			}

			@Override
			public void deactivateController() {
			}
 		};
 		IControllerListener testListener = new IControllerListener() {
 
 			@Override
 			public void controllerListenerUpdate(ControllerEvent event) {
 				AbstractControllerTest.setListenerUpdated(true);
 			}
 		};
 		assertFalse(listenerUpdated);
 
 		testController.addListener(testListener);
 		testController.fireEvent(null);
 
 		assertTrue(listenerUpdated);
 
 		setListenerUpdated(false);
 		testController.removeListener(testListener);
 		testController.fireEvent(null);
 
 		assertFalse(listenerUpdated);
 	}
 
 	private static void setListenerUpdated(boolean b) {
 		listenerUpdated = b;
 	}
 }

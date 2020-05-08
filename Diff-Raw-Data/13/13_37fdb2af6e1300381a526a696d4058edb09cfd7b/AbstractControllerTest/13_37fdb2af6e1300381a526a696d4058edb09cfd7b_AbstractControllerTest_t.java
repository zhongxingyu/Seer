 /* ============================================================
  * Copyright 2012 Bjorn Persson Mattsson, Johan Gronvall, Daniel Jonsson,
  * Viktor Anderling
  *
  * This file is part of UltraExtreme.
  *
  * UltraExtreme is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * UltraExtreme is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with UltraExtreme. If not, see <http://www.gnu.org/licenses/>.
  * ============================================================ */
 
 package ultraextreme.controller;
 
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import org.andengine.entity.scene.Scene;
 import org.junit.Test;
 
 /**
  * 
  * @author Bjorn Persson Mattsson
  * 
  */
 public class AbstractControllerTest {
 
 	private static boolean listenerUpdated;
 
 	private static void setListenerUpdated(boolean b) {
 		listenerUpdated = b;
 	}
 
 	@Test
 	public void testAddListener() {
 		AbstractController testController = new AbstractController() {
 
 			@Override
 			public void activateController() {
 			}
 
 			@Override
 			public void deactivateController() {
 			}
 
 			@Override
 			public Scene getScene() {
 				return null;
 			}

			@Override
			public void backButtonPressed() {
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
 }

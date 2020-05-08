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
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotSame;
 import static org.junit.Assert.assertNull;
 
 import org.andengine.entity.scene.Scene;
 import org.junit.Test;
 
 import ultraextreme.controller.ControllerEvent.ControllerEventType;
 
 /**
  * 
  * @author Bjorn Persson Mattsson
  * 
  */
 public class ControllerEventTest {
 
 	@Test
 	public void testControllerEvent() {
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
 		};
 		ControllerEvent event = new ControllerEvent(null, null);
 		assertNull(event.getSource());
 		assertNull(event.getEventType());
 
 		event = new ControllerEvent(testController,
 				ControllerEventType.SWITCH_TO_GAME);
 		assertEquals(testController, event.getSource());
 		assertEquals(ControllerEventType.SWITCH_TO_GAME, event.getEventType());
 		assertNotSame(ControllerEventType.SWITCH_TO_HIGHSCORE,
 				event.getEventType());
 
 		event = new ControllerEvent(null, ControllerEventType.SWITCH_TO_MENU);
 		assertNull(event.getSource());
 		assertEquals(ControllerEventType.SWITCH_TO_MENU, event.getEventType());
 		assertNotSame(ControllerEventType.SWITCH_TO_GAME, event.getEventType());
 	}
 }

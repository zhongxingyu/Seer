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
 
 package ultraextreme.model.item;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.ArrayList;
import java.util.Collection;
 import java.util.HashMap;
 
 import junit.framework.TestCase;
 
 import org.junit.Test;
 
 import ultraextreme.model.entity.WeaponPickup;
 import ultraextreme.model.util.Constants;
 import ultraextreme.model.util.ObjectName;
 
 /**
  * Test for the pickupManager
  * 
  * @author Johan Gronvall
  * 
  */
 public class PickupManagerTest extends TestCase {
 
 	/**
 	 * Add this as a listener to the pickupManager and collects its pickups
 	 * 
 	 * @author Daniel Jonsson
 	 * @author Johan Gronvall
 	 * 
 	 */
 	public class PickupCollector implements PropertyChangeListener {
 
 		private HashMap<String,ArrayList<WeaponPickup>> map;
 
 		public PickupCollector() {
 			map = new HashMap<String, ArrayList<WeaponPickup>>();
 		}
 
 		public HashMap<String,ArrayList<WeaponPickup>> getPickupMap() {
 			return map;
 		}
 
 		@Override
 		public void propertyChange(PropertyChangeEvent event) {
 
 			if (!map.containsKey(event.getPropertyName())) {
 				map.put(event.getPropertyName(), new ArrayList<WeaponPickup>());
 			}
 			map.get(event.getPropertyName()).add(
 					(WeaponPickup) event.getNewValue());
 		}
 	}
 
 	private PickupManager manager;
 	private WeaponPickup pickup;
 	private WeaponPickup pickup2;
 	private PickupCollector collector;
 
 	@Override
 	public void setUp() {
 		manager = new PickupManager();
 		pickup = new WeaponPickup(0, 0, ObjectName.BOMB);
 		pickup2 = new WeaponPickup(0, 0, ObjectName.BASIC_WEAPON);
 		collector = new PickupCollector();
 		manager.addPropertyChangeListener(collector);
 		manager.addPickup(pickup);
 		manager.addPickup(pickup2);
 	}
 
 	@Test
 	public void testAddPickup() {
 		// addPickup called in setup
 		assertTrue(manager.getPickups().get(0).equals(pickup));
 		assertTrue(manager.getPickups().get(1).equals(pickup2));
 		assertTrue(collector.getPickupMap().get(Constants.EVENT_NEW_ENTITY)
 				.contains(pickup));
 		// check if the map has stored an event for the secondary addPickup call
 		assertTrue(collector.getPickupMap().get(Constants.EVENT_NEW_ENTITY)
 				.contains(pickup2));
 	}
 
 	public void testClearAllPickups() {
 		manager.clearAllPickups();
 		assertTrue(collector.getPickupMap().get(Constants.EVENT_REMOVED_ENTITY).contains(pickup));
 		assertTrue(collector.getPickupMap().get(Constants.EVENT_REMOVED_ENTITY).contains(pickup2));
 	}
 
 	@Test
 	public void testGetPickups() {
 		assertTrue(manager.getPickups() != null);
 		assertTrue(manager.getPickups().get(0).equals(pickup));
 		assertTrue(manager.getPickups().get(1).equals(pickup2));
 	}
 
 	@Test
 	public void testRemovePickUpInt() {
 		manager.removePickup(0);
 		assertTrue(manager.getPickups().get(0).equals(pickup2));
 		assertTrue(collector.getPickupMap().get(Constants.EVENT_REMOVED_ENTITY)
 				.contains(pickup));
 	}
 
 	@Test
 	public void testRemovePickupWeaponPickup() {
 		manager.removePickup(pickup);
 		assertTrue(manager.getPickups().get(0).equals(pickup2));
 		assertTrue(collector.getPickupMap().get(Constants.EVENT_REMOVED_ENTITY)
 				.contains(pickup));
 	}
 
 	@Test
 	public void testRemovePickupsOffScreen() {
 		manager.clearPickupsOffScreen();
 		assertTrue(manager.getPickups().contains(pickup));
 		assertTrue(manager.getPickups().contains(pickup2));
 
 		pickup.doMovement(1000);
 		pickup2.doMovement(1000);
 		manager.clearPickupsOffScreen();
 		assertFalse(manager.getPickups().contains(pickup));
 		assertFalse(manager.getPickups().contains(pickup2));
 		assertTrue(collector.getPickupMap().get(Constants.EVENT_REMOVED_ENTITY).contains(pickup));
 		assertTrue(collector.getPickupMap().get(Constants.EVENT_REMOVED_ENTITY).contains(pickup2));
 	}
 }

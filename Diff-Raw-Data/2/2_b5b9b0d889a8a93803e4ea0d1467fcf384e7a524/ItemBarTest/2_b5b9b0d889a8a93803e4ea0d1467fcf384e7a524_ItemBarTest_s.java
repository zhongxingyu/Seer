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
 
 import java.util.ArrayList;
 import java.util.List;
 
 import junit.framework.TestCase;
 import ultraextreme.model.util.PlayerID;
 import ultraextreme.model.util.Position;
 import ultraextreme.model.util.Rotation;
 
 /**
  * 
  * @author Daniel Jonsson
  * 
  */
 public class ItemBarTest extends TestCase {
 
 	private PlayerID playerId;
 	private BulletManager bulletManager;
 	private ItemBar itemBar;
 
 	private void addItemSizeTester(int numberOfItems) {
 		for (int i = 0; i < numberOfItems; i++) {
 			itemBar.addItem(new BasicWeapon(bulletManager));
 			assertEquals(i + 1, itemBar.getMarkerPosition());
 		}
 		assertEquals(itemBar.getItems().size(), numberOfItems);
 	}
 
 	private AbstractWeapon getNewItem() {
 		return new BasicWeapon(bulletManager);
 	}
 
 	private AbstractWeapon getNewWeapon() {
 		return new BasicWeapon(bulletManager);
 	}
 
 	private void resetInstanceVariables(int slots) {
 		bulletManager = new BulletManager();
 		playerId = PlayerID.PLAYER1;
		itemBar = new ItemBar(playerId, bulletManager, new Rotation(0), slots);
 	}
 
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		this.resetInstanceVariables(10);
 	}
 
 	/**
 	 * Add a lot of items to the item bar, get them from the item bar and
 	 * finally check if the size is correct.
 	 */
 	public void testAddItemsAndCheckSize() {
 		for (int i = 0; i < 1000; i += 100) {
 			resetInstanceVariables(1000);
 			addItemSizeTester(i);
 		}
 	}
 
 	/**
 	 * Add 15 items to an item bar that can only take 10 items, and then check
 	 * that the size of items is correct in the item bar.
 	 */
 	public void testAddTooManyItems() {
 		resetInstanceVariables(10);
 		for (int i = 0; i < 15; i++) {
 			itemBar.addItem(getNewItem());
 		}
 		assertEquals(itemBar.getItems().size(), 10);
 	}
 
 	/**
 	 * Fire a weapon a couple of times and check if the bullets are added to the
 	 * bullet manager.
 	 */
 	public void testFireWeapon() {
 		float epsilon = 0.001f;
 		float cooldown = BasicWeapon.getInitCooldown();
 		itemBar.fireWeapons(new Position(), cooldown * (1 + epsilon));
 		assertTrue(bulletManager.getBullets().size() == 0);
 
 		itemBar.addItem(getNewWeapon());
 		itemBar.fireWeapons(new Position(), cooldown * (1 + epsilon));
 		int bulletsShot = bulletManager.getBullets().size();
 		assertTrue(bulletsShot > 0);
 
 		itemBar.fireWeapons(new Position(), cooldown * (1 + epsilon));
 		assertTrue(bulletManager.getBullets().size() > bulletsShot);
 	}
 
 	public void testLoseItems() {
 		BasicWeapon item = new BasicWeapon(new BulletManager());
 		itemBar.addItem(item);
 		itemBar.addItem(item);
 		itemBar.addItem(item);
 		assertEquals(3, itemBar.getItems().size());
 		assertEquals(3, itemBar.getMarkerPosition());
 		itemBar.loseItems();
 		assertEquals(2, itemBar.getItems().size());
 		assertEquals(2, itemBar.getMarkerPosition());
 		itemBar.loseItems();
 		assertEquals(1, itemBar.getItems().size());
 		assertEquals(1, itemBar.getMarkerPosition());
 		itemBar.loseItems();
 		assertEquals(0, itemBar.getItems().size());
 		assertEquals(0, itemBar.getMarkerPosition());
 		itemBar.loseItems();
 		assertEquals(0, itemBar.getItems().size());
 		assertEquals(0, itemBar.getMarkerPosition());
 	}
 
 	/**
 	 * Fill an item bar with items, then see if the marker moves correctly when
 	 * an item is lost.
 	 */
 	public void testLoseItemsFromFullBar() {
 		BasicWeapon item = new BasicWeapon(new BulletManager());
 		resetInstanceVariables(5);
 		// Fill the item bar
 		for (int i = 0; i < 5; ++i)
 			itemBar.addItem(item);
 		assertEquals("Marker on first position", 0, itemBar.getMarkerPosition());
 		itemBar.loseItems();
 		assertEquals("Marker on last position", 4, itemBar.getMarkerPosition());
 	}
 
 	/**
 	 * This test checks so the marker's position wraps correctly when the item
 	 * bar gets full.
 	 */
 	public void testMarkerPositionWrapping() {
 		BasicWeapon item = new BasicWeapon(new BulletManager());
 
 		// Add items and check if it wraps
 		for (int i = 0; i < 30; ++i) {
 			assertEquals(i % 10, itemBar.getMarkerPosition());
 			itemBar.addItem(item);
 		}
 	}
 
 	/**
 	 * Add 7 items to an item bar that can only hold 5 items. Then check so it's
 	 * the last 5 added items that are in the item bar. Also check so they are
 	 * in the right order.
 	 */
 	public void testWhetherItemsAreWrittenOverCorrectly() {
 		resetInstanceVariables(5);
 		List<AbstractWeapon> items = new ArrayList<AbstractWeapon>();
 		for (int i = 0; i < 7; i++) {
 			AbstractWeapon item = getNewItem();
 			items.add(item);
 			itemBar.addItem(item);
 			assertEquals((i + 1) % 5, itemBar.getMarkerPosition());
 		}
 		assertFalse(itemBar.getItems().contains(items.get(0)));
 		assertFalse(itemBar.getItems().contains(items.get(1)));
 		assertSame(itemBar.getItems().get(0), items.get(5));
 		assertSame(itemBar.getItems().get(1), items.get(6));
 		assertSame(itemBar.getItems().get(2), items.get(2));
 		assertSame(itemBar.getItems().get(3), items.get(3));
 		assertSame(itemBar.getItems().get(4), items.get(4));
 	}
 }

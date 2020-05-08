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
 
 package ultraextreme.model;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeSupport;
 
 import junit.framework.TestCase;
 import ultraextreme.model.enemy.BasicEnemy;
 import ultraextreme.model.enemy.IEnemy;
 import ultraextreme.model.entity.AbstractEntity;
 import ultraextreme.model.entity.EnemyShip;
 import ultraextreme.model.entity.PlayerShip;
 import ultraextreme.model.item.AbstractWeapon;
 import ultraextreme.model.item.BasicWeapon;
 import ultraextreme.model.item.BulletManager;
 import ultraextreme.model.item.ItemBar;
 import ultraextreme.model.item.WeaponFactory;
 import ultraextreme.model.util.Constants;
 import ultraextreme.model.util.PlayerID;
 import ultraextreme.model.util.Position;
 
 /**
  * 
  * @author Daniel Jonsson
  * @author Bjorn Persson Mattsson
  * @author Viktor Anderling
  * 
  */
 public class PlayerTest extends TestCase {
 
 	private Player player;
 	private BulletManager bulletManager;
 	private PlayerID playerId;
 
 	private void resetInstanceVariables() {
 		bulletManager = new BulletManager();
 		playerId = PlayerID.PLAYER1;
 		player = new Player(playerId, bulletManager);
 	}
 
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		this.resetInstanceVariables();
 	}
 
 	public void testGetInvincibilityTime() {
 		double epsilon = 0.00001;
 		assertTrue(Math.abs(player.getInvincibilityTime() 
 				- Constants.getShipInvincibilityTime()) < epsilon);
 	}
 
 	public void testGetItemBar() {
 		// TODO Maybe not the best test?
 		ItemBar itemBar = player.getItemBar();
 		itemBar.addItem(new BasicWeapon(bulletManager));
 		assertEquals(itemBar.getItems().size(), player.getItemBar().getItems()
 				.size());
 	}
 
 	/**
 	 * Test if the get method works.
 	 */
 	public void testGetLives() {
 		assertTrue(player.getLives() == Constants.getInitShipLives());
 		player.getShip().receiveDamage(1);
 		player.update(new ModelInput(0, 0, false, false), 1);
 		assertTrue(player.getLives() == Constants.getInitShipLives() - 1);
 	}
 
 	/**
 	 * Test if it's possible to get the player ID.
 	 */
 	public void testGetPlayerId() {
 		assertEquals(player.getPlayerId(), playerId);
 	}
 
 	/**
 	 * Test if the get method works.
 	 */
 	public void testGetShip() {
 		PlayerShip shipBefore = player.getShip();
 		player.getShip().receiveDamage(1);
 		assertTrue(shipBefore == player.getShip());
 	}
 
 	public void testGiveWeapon() {
 		// TODO Only checking the amount of weapons is correct. Better test
 		// maybe?
 		ItemBar itemBar = player.getItemBar();
 		int preNoOfWeapons = itemBar.getItems().size();
 		player.giveWeapon(new BasicWeapon(bulletManager));
 		assertEquals(preNoOfWeapons, itemBar.getItems().size() - 1);
 	}
 
 	public void testIsInvincible() {
 		player.getShip().receiveDamage(1);
 		player.update(new ModelInput(0, 0, false, false), 
 				(float)(Constants.getShipInvincibilityTime() / 2));
 	}
 
 	public void testItemBarSize() {
 		ItemBar itemBar = player.getItemBar();
 		for (int i = 0; i < 20; i++)
 			itemBar.addItem(new BasicWeapon(bulletManager));
 		assertEquals("Correct item bar size", 10, itemBar.getItems().size());
 	}
 
 	public void testPlayerListener() {
 		fail("Not yet tested");
 	}
 
 	public void testReset() {
 		WeaponFactory.initialize(bulletManager);
		BasicEnemy enemy = new BasicEnemy(new Position(0, 0));
 		PropertyChangeEvent event = new PropertyChangeEvent(new Object(), 
 				Constants.EVENT_ENEMY_KILLED, null, enemy);
 		player.propertyChange(event);
 		
 		for(int i = 0; i < Constants.getInitShipLives(); i++) {
 			player.getShip().receiveDamage(1);
 			player.update(new ModelInput(0, 0, false, false), 
 					(float)(Constants.getShipInvincibilityTime() + 0.001));
 		}		
 		assertEquals(player.getLives(), 0);
 		assertEquals(player.getScore(), enemy.getScoreValue());
 		assertTrue(player.getShip().isDestroyed());
 		
 		
 		player.reset();
 		
 		assertEquals(player.getLives(), Constants.getInitShipLives());
 		assertEquals(player.getScore(), 0);
 		
 		assertFalse(player.getShip().isDestroyed());
 		assertFalse(player.getShip().justGotHit());
 	}
 
 	public void testScore() {
 		final int scoreValue = 12;
 		PropertyChangeSupport pcs = new PropertyChangeSupport(this);
 		pcs.addPropertyChangeListener(player);
 		IEnemy enemy = new IEnemy() {
 
 			@Override
 			public int getScoreValue() {
 				return scoreValue;
 			}
 
 			@Override
 			public EnemyShip getShip() {
 				return null;
 			}
 
 			@Override
 			public AbstractWeapon getWeapon() {
 				return null;
 			}
 
 			@Override
 			public boolean isDead() {
 				return false;
 			}
 
 			@Override
 			public boolean shouldSpawnPickup() {
 				return false;
 			}
 		};
 		assertTrue(player.getScore() == 0);
 
 		pcs.firePropertyChange(Constants.EVENT_ENEMY_KILLED, null, enemy);
 		assertTrue(player.getScore() == scoreValue);
 
 		pcs.firePropertyChange(Constants.EVENT_ENEMY_KILLED, null, enemy);
 		assertTrue(player.getScore() == 2 * scoreValue);
 	}
 
 	/**
 	 * Test the update method with a lot of different values.
 	 */
 	public void testUpdate() {
 		// FIXME: Doesn't test the drop bomb feature yet.
 		updateTester(0, 0, false, false);
 		updateTester(5, 10, false, false);
 		updateTester(-5, -10, false, false);
 		updateTester(0, 0, true, true);
 		updateTester(5, 10, true, true);
 		updateTester(-5, -10, true, true);
 		updateTester(100, -100, true, true);
 
 		// Testing to see if the player dies.
 		ModelInput m = new ModelInput(0, 0, false, false);
 		ItemBar itemBar = player.getItemBar();
 		itemBar.addItem(new BasicWeapon(bulletManager));
 		int nOfPreWeapons = itemBar.getItems().size();
 		player.getShip().receiveDamage(1);
 		player.update(m, 1);
 		assertEquals(nOfPreWeapons, itemBar.getItems().size() + 1);
 		this.resetInstanceVariables();
 
 		itemBar = player.getItemBar();
 		int preLives = player.getLives();
 		itemBar.addItem(new BasicWeapon(bulletManager));
 		while (itemBar.getItems().size() > 1) {
 			player.getShip().receiveDamage(1);
 			player.update(m, 1);
 		}
 		assertEquals(preLives, player.getLives());
 		assertFalse(player.getShip().isDestroyed());
 
 		player.getShip().receiveDamage(1);
 		player.update(m, 1);
 		assertEquals(preLives, player.getLives());
 		assertFalse(player.getShip().isDestroyed());
 
 		while (player.getLives() > 0) {
 			player.getShip().receiveDamage(1);
 			player.update(m, 1);
 		}
 		assertTrue(player.getShip().isDestroyed());
 	}
 
 	/**
 	 * Helper method that runs an update on the player and checks wether his
 	 * ship has moved and if his weapons have been fired.
 	 * 
 	 * @param dX
 	 *            Delta X distance that he has moved.
 	 * @param dY
 	 *            Delta Y distance that he has moved.
 	 * @param fireWeapons
 	 *            If he has fired his guns.
 	 * @param dropBomb
 	 *            If he has dropped a bomb.
 	 */
 	private void updateTester(int dX, int dY, boolean fireWeapons,
 			boolean dropBomb) {
 		this.resetInstanceVariables();
 		Position pOld = new Position(player.getShip().getPositionClone());
 		ModelInput m = new ModelInput(dX, dY, fireWeapons, dropBomb);
 		player.update(m, 1);
 		Position pNew = player.getShip().getPositionClone();
 		assertEquals(pOld.getX() + dX, pNew.getX());
 		assertEquals(pOld.getY() + dY, pNew.getY());
 		if (fireWeapons)
 			assertTrue(bulletManager.getBullets().size() > 0);
 		else
 			assertTrue(bulletManager.getBullets().size() == 0);
 		// assertEquals(bulletManager.isBombDropped(), dropBomb);
 	}
 }

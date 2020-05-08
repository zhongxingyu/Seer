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
 
 package ultraextreme.model.enemy;
 
 import junit.framework.TestCase;
 import ultraextreme.model.item.BulletManager;
 import ultraextreme.model.item.WeaponFactory;
 import ultraextreme.model.util.ObjectName;
 import ultraextreme.model.util.Position;
 
 /**
  * Test for a hitAndRunEnemy
  * 
  * @author Johan Gronvall
  * 
  */
 public class HitAndRunEnemyTest extends TestCase {
 	Position startPoint;
 	Position endPoint;
 	Position firePoint;
 	HitAndRunEnemy enemy;
 	BulletManager manager;
 
 	@Override
 	public void setUp() {
 		manager = new BulletManager();
 		WeaponFactory.initialize(manager);
 		startPoint = new Position(5, 10);
 		endPoint = new Position(20, 20);
 		firePoint = new Position(15, 15);
 		enemy = new HitAndRunEnemy(startPoint, firePoint, endPoint,
 				ObjectName.SPINNING_WEAPON);
 	}
 
 	public void testGoTowardPosition() {
 		enemy.goTowardPosition(firePoint, (float) 0.01);
 		assertTrue(enemy.getShip().getPositionClone().getX() > 5.0);
 		assertTrue(enemy.getShip().getPositionClone().getY() > 10.0);
 
 		enemy.goTowardPosition(firePoint, (float) 1000.0);
 		assertEquals(firePoint, enemy.getShip().getPositionClone());
 
 		Position negativePosition = new Position(-5, -5);
 		enemy.goTowardPosition(negativePosition, (float) 0.005);
 		assertTrue(enemy.getShip().getPositionClone().getX() < 15);
 		assertTrue(enemy.getShip().getPositionClone().getY() < 15);
 
 		enemy.goTowardPosition(negativePosition, 1000);
 		assertEquals(enemy.getShip().getPositionClone(), negativePosition);
 	}
 
 	public void testHitAndRunEnemyConstructor() {
 		assertNotNull(enemy);
 		assertNotNull(enemy.getWeapon());
 		assertEquals(startPoint, enemy.getShip().getPositionClone());
 	}
 
 	public void testUpdate() {
 		boolean stopped = false;
 		float secondsStopped = 0;
 		boolean supposedToStartMoving = false;
 		boolean startedMovingAgain = false;
 		boolean shot = false;
 		for (int i = 0; i < 100000; i++) {
 			enemy.update((1));
 			if (enemy.getShip().getPositionClone().equals(new Position(15, 15))) {
 				secondsStopped += 1;
 			}
			if (secondsStopped == enemy.getWaitingTime() && stopped == false) {
 				stopped = true;
 				supposedToStartMoving = true;
 				shot = (!manager.getBullets().isEmpty());
 			}
 			if (supposedToStartMoving
 					&& (enemy.getShip().getPositionClone().equals(endPoint))) {
 				startedMovingAgain = true;
 			}
 		}
 		assertTrue(stopped);
 		assertTrue(startedMovingAgain);
 		assertTrue(shot);
 	}
 }

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
 
 package ultraextreme.model.entity;
 
 import ultraextreme.model.util.PlayerID;
 import ultraextreme.model.util.Rotation;
 
 /**
  * 
  * @author Daniel Jonsson
  * 
  */
 public class BasicBulletTest extends AbstractBulletTest {
 	private float speed;
 
 	@Override
 	protected AbstractBullet getNewAbstractBullet(double x, double y,
 			int width, int height, PlayerID playerId, Rotation direction) {
 		BasicBullet bullet = new BasicBullet(x, y, width, height, playerId,
 				direction);
 		speed = BasicBullet.speed;
 		return bullet;
 	}
 
 	@Override
 	public void testDoMovement() {
 
 		AbstractBullet bullet = getNewAbstractBullet(10, 20, 30, 40,
 				PlayerID.PLAYER1, new Rotation(0));
 
 		bullet.doMovement(1);
 		assertEquals((float) bullet.getPositionClone().getY(), 20 + 1 * speed);
 
 		bullet.doMovement(1);
 		assertEquals((float) bullet.getPositionClone().getY(), 20 + 2 * speed);
 
 		bullet.doMovement(10);
 		assertEquals((float) bullet.getPositionClone().getY(), 20 + 12 * speed);
 

 	}
 }

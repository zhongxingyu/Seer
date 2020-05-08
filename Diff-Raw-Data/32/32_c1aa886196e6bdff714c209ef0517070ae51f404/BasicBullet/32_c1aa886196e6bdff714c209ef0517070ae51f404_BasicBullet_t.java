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
 
 import ultraextreme.model.util.ObjectName;
 import ultraextreme.model.util.PlayerID;
 import ultraextreme.model.util.Rotation;
 
 /**
  * A basic bullet that flies straight forward.
  * 
  * @author Daniel Jonsson
  * @author Viktor Anderling
  * 
  */
 public class BasicBullet extends AbstractBullet {
 	// public for test reasons
	public final static float SPEED = 720f;
 
 	public BasicBullet(final double x, final double y, final int width,
 			final int height, PlayerID playerId, final Rotation rotation,
 			int bulletDamage) {
 		super(x, y, width, height, playerId, rotation, ObjectName.BASIC_BULLET,
 				bulletDamage);
 	}
 
 	@Override
 	public void doMovement(float timePassed) {
		this.move(0, timePassed * SPEED);
 	}
 
 }

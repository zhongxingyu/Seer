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
 
 import ultraextreme.model.entity.EnemyShip;
 import ultraextreme.model.item.AbstractWeapon;
 import ultraextreme.model.item.WeaponFactory;
 import ultraextreme.model.util.ObjectName;
 import ultraextreme.model.util.Position;
 import ultraextreme.model.util.Rotation;
 
 /**
  * Class representing a very basic enemy that only flies downwards and has a
  * basic weapon.
  * 
  * @author Bjorn Persson Mattsson
  * @author Daniel Jonsson
  * @author Johan Gronvall
  * @author Viktor Anderling
  * 
  */
 public class BasicEnemy extends AbstractEnemy {
 
 	private static final int SHIP_SIZE = 80;
 	private static final int SCORE = 10;
 	private static final int Y_SPEED = 170;
 
 	private BasicEnemy(final EnemyShip ship, final AbstractWeapon weapon) {
 		super(ship, weapon);
 	}
 
 	/**
 	 * Constructor for an enemy with predetermined ship and weapon. Specific to
 	 * this type of enemy
 	 * 
 	 * @param position
 	 *            Coordinate for the enemyShip
 	 */
 	public BasicEnemy(final Position position) {
 		this(position, new Rotation(0), ObjectName.BASIC_WEAPON);
 	}
 
 	/**
 	 * Constructor for an enemy with predetermined ship and weapon. Specific to
 	 * this type of enemy
 	 * 
 	 * @param position
 	 *            Coordinate for the enemyShip
 	 * @param rotation
 	 *            Rotation modifier of the enemy's flying path.
 	 */
 	public BasicEnemy(final Position position, final Rotation rotation,
 			ObjectName weaponName) {
 		this(new EnemyShip(position.getX(), position.getY(), SHIP_SIZE,
				SHIP_SIZE, rotation, 40, ObjectName.BASIC_ENEMYSHIP),
 				WeaponFactory.getNewWeapon(weaponName));
 	}
 
 	@Override
 	public void update(float timeElapsed) {
 		this.getShip().move(0, timeElapsed * Y_SPEED);
 		this.shoot(timeElapsed);
 	}
 
 	@Override
 	public int getScoreValue() {
 		return SCORE;
 	}
 }

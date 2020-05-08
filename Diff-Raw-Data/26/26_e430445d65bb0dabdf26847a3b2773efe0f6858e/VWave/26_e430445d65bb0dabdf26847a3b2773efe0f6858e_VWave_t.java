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
 
 package ultraextreme.model.enemyspawning.wave;
 
 import ultraextreme.model.enemy.BasicEnemy;
 import ultraextreme.model.util.ObjectName;
 import ultraextreme.model.util.Position;
 import ultraextreme.model.util.Rotation;
 
 /**
  * This is an enemy wave where the formation is like a V.
  * 
  * @author Daniel Jonsson
  * 
  */
 public class VWave extends AbstractWave {
 
 	private static final int DISPERSION = 100;
	private static final int NR_OF_ENEMY_ROWS = 4;
 	private float timer;
 	private int counter;
 	private final Rotation rotation;
 	private final Position spawningPositon;
 
 	/**
 	 * Create a enemy wave flying like a V. There are 7 enemies in the wave and
 	 * it's 700 units wide.
 	 * 
 	 * @param rotation
 	 *            How much you want to rotate the formation.
 	 * @param x
 	 *            X position where the enemies should spawn.
 	 * @param y
 	 *            Y position where the enemies should spawn.
 	 */
 	public VWave(final double rotation, final int x, final int y) {
 		timer = 2;
 		counter = 0;
 		this.rotation = new Rotation(rotation);
 		this.spawningPositon = new Position(x, y);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void update(final float timeElapsed) {
 		timer += timeElapsed;
 		if (timer >= 2) {
 			if (counter == 0) {
 				fireNewEnemySpawned(new BasicEnemy(spawningPositon,
 						this.rotation, ObjectName.BASIC_WEAPON));
 			} else {
 				fireNewEnemySpawned(new BasicEnemy(new Position(
 						spawningPositon.getX() - counter * DISPERSION,
 						spawningPositon.getY()), rotation,
 						ObjectName.BASIC_WEAPON));
 				fireNewEnemySpawned(new BasicEnemy(new Position(
 						spawningPositon.getX() + counter * DISPERSION,
 						spawningPositon.getY()), rotation,
 						ObjectName.BASIC_WEAPON));
 			}
 			timer -= 2;
 			counter++;
			if (counter >= NR_OF_ENEMY_ROWS) {
 				this.fireWaveEnded();
 			}
 		}
 	}
 }

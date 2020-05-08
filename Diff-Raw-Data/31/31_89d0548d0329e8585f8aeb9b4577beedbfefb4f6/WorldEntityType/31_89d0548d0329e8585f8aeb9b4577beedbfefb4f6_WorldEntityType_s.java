 /* Acid - Provides a Java cell API to display fancy cell boxes.
  * Copyright (C) 2013  Miguel Gonzalez
  * 
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
  */
 
 package de.myreality.acidsnake.world;
 
 import java.util.Set;
 
 import com.badlogic.gdx.graphics.g2d.ParticleEffect;
 
 import de.myreality.acid.CellRenderer;
 import de.myreality.acidsnake.Resources;
 import de.myreality.acidsnake.core.Snake;
 import de.myreality.acidsnake.core.SnakeListener;
 
 /**
  * Type of a world object
  * 
  * @author Miguel Gonzalez <miguel-gonzalez@gmx.de>
  * @since 1.0
  * @version 1.0
  */
 public enum WorldEntityType implements SnakeListener {
 
 	SNAKE {
 		
 		final int SNAKE_LENGTH = 3;
 
 		@Override
 		public void onEnterPosition(int indexX, int indexY, Snake snake) {
 			
 		}
 
 		@Override
 		public void onCollide(int indexX, int indexY, Snake snake,
 				WorldEntity target) {
 			if (target != null && target.getType().equals(this)) {
 				snake.kill();
 			}
 		}
 
 		@Override
 		public void onKill(Snake snake) {
 			
 		}
 
 		@Override
 		public void onSpawn(Snake snake) {
 			for (int i = 0; i < SNAKE_LENGTH; ++i) {
 				snake.addChunk();
 			}
 		}
 
 		@Override
 		public ParticleEffect getFieldEffect() {
 			return null;
 		}
 
 		@Override
 		public ParticleEffect getExplodeEffect() {
 			return null;
 		}
 
 		@Override
 		public CellRenderer getCellRenderer() {
 			return Resources.CELL_RENDERER_GREEN;
 		}
 	},
 	
 	SMALL_FOOD {
 
 		@Override
 		public void onEnterPosition(int indexX, int indexY, Snake snake) {
 			
 		}
 
 		@Override
 		public void onCollide(int indexX, int indexY, Snake snake,
 				WorldEntity target) {
 			if (target.getType().equals(this)) {
 				snake.addChunk();
 				snake.getWorld().getPlayer().addPoints(10);
 				snake.getWorld().removeEntity(target);
 				spawnAtRandomPosition(this, snake.getWorld());
 			}
 		}
 
 		@Override
 		public void onKill(Snake snake) {
 			
 		}
 
 		@Override
 		public void onSpawn(Snake snake) {
 			spawnAtRandomPosition(this, snake.getWorld());
 			spawnAtRandomPosition(this, snake.getWorld());
 			spawnAtRandomPosition(this, snake.getWorld());
 		}
 
 		@Override
 		public ParticleEffect getFieldEffect() {
 			return null;
 		}
 
 		@Override
 		public ParticleEffect getExplodeEffect() {
 			return Resources.PARTICLE_EXPLOSION_VIOLET;
 		}
 
 		@Override
 		public CellRenderer getCellRenderer() {
 			return Resources.CELL_RENDERER_VIOLET;
 		}
 		
 		@Override
 		public int getParticleDecreaseFactor() {
 			return 5;
 		}
 		
 	},
 	
 	RARE_FOOD {
 		
 		private static final double CHANCE = 10.0;
 
 		@Override
 		public void onEnterPosition(int indexX, int indexY, Snake snake) {
 			
 			
 		}
 
 		@Override
 		public void onCollide(int indexX, int indexY, Snake snake,
 				WorldEntity target) {
 			
 			if (target.getType().equals(this)) {
 				snake.addChunk();
 				snake.getWorld().getPlayer().addPoints(50);
 				snake.getWorld().removeEntity(target);
 			}
 			
 			if (isChance(CHANCE)) {
 				spawnAtRandomPosition(this, snake.getWorld());
 			}
 			
 		}
 
 		@Override
 		public void onKill(Snake snake) {
 			
 		}
 
 		@Override
 		public void onSpawn(Snake snake) {
 			if (isChance(CHANCE)) {
 				spawnAtRandomPosition(this, snake.getWorld());
 			}
 		}
 
 		@Override
 		public ParticleEffect getFieldEffect() {
 			return Resources.PARTICLE_FIELD_VIOLET;
 		}
 
 		@Override
 		public ParticleEffect getExplodeEffect() {
 			return Resources.PARTICLE_EXPLOSION_VIOLET;
 		}
 
 		@Override
 		public CellRenderer getCellRenderer() {
 			return Resources.CELL_RENDERER_VIOLET;
 		}
 		
 	},
 	
 	TELEPORTER {
 		
 		private static final double SPAWN_CHANCE = 3.0;
 		
 		private static final int ALLOWED_COUNT = 2;
 
 		@Override
 		public void onEnterPosition(int indexX, int indexY, Snake snake) {
 			
 		}
 
 		@Override
 		public void onCollide(int indexX, int indexY, Snake snake,
 				WorldEntity target) {
 			
 			World world = snake.getWorld();
 			
 			if (target.getType().equals(this)) {
 				
 				WorldEntity targetPortal = null;
 				
 				// 1. Fetch the other portal
 				
 				Set<WorldEntity> portals = world.getEntitiesOfType(this);				
 				
 				for (WorldEntity tempPortal : portals) {
 					if (!tempPortal.equals(target)) {
 						targetPortal = tempPortal;
 						break;
 					}
 				}
 
 				// 2. Cleanup
 				
 				world.removeEntity(target);
 				world.removeEntity(targetPortal);
 				
 				// 3. Move snake to portal
 				snake.setIndex(targetPortal.getIndexX(), targetPortal.getIndexY());
 				
 			} else if (world.getEntityCount(this) < ALLOWED_COUNT && isChance(SPAWN_CHANCE)) {
 				spawnAtRandomPosition(this, world); // TELEPORT A
 				spawnAtRandomPosition(this, world); // TELEPORT B
 			}
 		}
 
 		@Override
 		public void onKill(Snake snake) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		@Override
 		public void onSpawn(Snake snake) {
 			
 			World world = snake.getWorld();
 			
 			if (world.getEntityCount(this) < ALLOWED_COUNT && isChance(SPAWN_CHANCE)) {
 				spawnAtRandomPosition(this, world); // TELEPORT A
 				spawnAtRandomPosition(this, world); // TELEPORT B
 			}
 		}
 
 		@Override
 		public ParticleEffect getFieldEffect() {
 			return Resources.PARTICLE_FIELD_BLUE;
 		}
 
 		@Override
 		public ParticleEffect getExplodeEffect() {
 			return Resources.PARTICLE_EXPLOSION_BLUE;
 		}
 
 		@Override
 		public CellRenderer getCellRenderer() {
 			return Resources.CELL_RENDERER_BLUE;
 		}
 		
 	},
 	
 	ACID {
 		
 		private static final double SPAWN_CHANCE = 50.0;
 		
 		private static final int MIN_SNAKE_LENGTH = 8;
 		
 		private static final int MAX_FACTOR = 3;
 
 		@Override
 		public void onEnterPosition(int indexX, int indexY, Snake snake) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		@Override
 		public void onCollide(int indexX, int indexY, Snake snake,
 				WorldEntity target) {
 			
 			if (target.getType().equals(this)) {
 				
 				snake.getWorld().removeEntity(target);
 				
 				int amount = (int) (MAX_FACTOR * Math.random() + 1);
 				
 				while (amount-- > 0 && snake.getLength() > MIN_SNAKE_LENGTH) {
 					snake.removeChunk();
 				}
 				
 			}
 			
 			if (snake.getLength() > MIN_SNAKE_LENGTH && isChance(SPAWN_CHANCE)) {
				//spawnAtRandomPosition(this, snake.getWorld());
 			}
 		}
 
 		@Override
 		public void onKill(Snake snake) {
 			
 		}
 
 		@Override
 		public void onSpawn(Snake snake) {
 			if (snake.getLength() > MIN_SNAKE_LENGTH && isChance(SPAWN_CHANCE)) {
				//spawnAtRandomPosition(this, snake.getWorld());
 			}
 		}
 
 		@Override
 		public ParticleEffect getFieldEffect() {
 			return Resources.PARTICLE_FIELD_GREEN;
 		}
 
 		@Override
 		public ParticleEffect getExplodeEffect() {
 			return Resources.PARTICLE_EXPLOSION_GREEN;
 		}
 
 		@Override
 		public CellRenderer getCellRenderer() {
 			return Resources.CELL_RENDERER_GREEN;
 		}
 		
 	};
 	
 	private static WorldEntityFactory entityFactory = null; // TODO
 	
 	public abstract ParticleEffect getFieldEffect();
 	public abstract ParticleEffect getExplodeEffect();
 	public abstract CellRenderer getCellRenderer();
 	
 	public int getParticleDecreaseFactor() {
 		return 1;
 	}
 	
 	private static void spawnAtRandomPosition(WorldEntityType type, World world) {
 		
 		if (entityFactory == null) {
 			entityFactory = new SimpleWorldEntityFactory(world);
 		}
 		
 		
 		int randomX = 0, randomY = 0;
 		boolean validPosition = false;
 		
 		while (!validPosition) {
 			
 			randomX = (int) (Math.random() * world.getWidth());
 			randomY = (int) (Math.random() * world.getHeight());
 			
 			validPosition = !world.hasEntity(randomX, randomY);
 		}
 		
 		world.putEntity(randomX, randomY, entityFactory.create(randomX, randomY, type));
 	}
 	
 	private static boolean isChance(double chance) {
 		return Math.random() * 100.0 <= chance;
 	}
 }

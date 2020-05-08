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
 
 package de.myreality.acidsnake.google;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import de.myreality.acidsnake.core.Player;
 import de.myreality.acidsnake.core.PlayerListener;
 import de.myreality.acidsnake.core.Snake;
 import de.myreality.acidsnake.core.SnakeListener;
 import de.myreality.acidsnake.util.Timer;
 import de.myreality.acidsnake.world.World;
 import de.myreality.acidsnake.world.WorldEntity;
 import de.myreality.acidsnake.world.WorldEntityType;
 import de.myreality.acidsnake.world.WorldHandler;
 
 /**
  * Manages all archievements
  * 
  * @author Miguel Gonzalez <miguel-gonzalez@gmx.de>
  * @since 1.0
  * @version 1.0
  */
 public class ArchievementManager implements SnakeListener, PlayerListener {
 
 	// ===========================================================
 	// Constants
 	// ===========================================================
 
 	// ===========================================================
 	// Fields
 	// ===========================================================
 	
 	private GoogleInterface google;
 	
 	private int lastLength;
 	
 	private BlockCounter counter30, counter40, counter50;
 	
 	private List<WorldEntityType> collected;
 	
 	private ComboDetector comboDetector;
 	
 	private DirectComboDetector directComboDetector;
 	
 	private List<AchievementListener> listeners;
 
 	// ===========================================================
 	// Constructors
 	// ===========================================================
 
 	public ArchievementManager(World world, GoogleInterface googleInterface) {
 		google = googleInterface;
 		collected = new ArrayList<WorldEntityType>();
 		listeners = new ArrayList<AchievementListener>();
 		counter30 = new BlockCounter(30, 60000);
 		counter40 = new BlockCounter(40, 60000);
 		counter50 = new BlockCounter(50, 60000);
 		world.addListener(counter30);
 		world.addListener(counter40);
 		world.addListener(counter50);
 		comboDetector = new ComboDetector();
 		directComboDetector = new DirectComboDetector();
 	}
 
 	// ===========================================================
 	// Getters and Setters
 	// ===========================================================
 
 	// ===========================================================
 	// Methods from Superclass
 	// ===========================================================
 
 	@Override
 	public void onEnterPosition(int indexX, int indexY, Snake snake) {
 		
 		if (lastLength != snake.getLength()) {		
 			switch (snake.getLength()) {
 				case 15:
 					submitAchievement(Achievements.THE_LONG_SNAKE, indexX, indexY);
 					break;
 				case 30:
 					submitAchievement(Achievements.THE_MONSTER_SNAKE, indexX, indexY);
 					break;
 				case 50:
 					submitAchievement(Achievements.THE_GIANT_SNAKE, indexX, indexY);
 					break;
 			}
 			
 			lastLength = snake.getLength();
 		}
 		
 		counter30.update();
 		counter40.update();
 		counter50.update();
 		
 		if (counter30.check()) {
 			submitAchievement(Achievements.IN_A_ROW_30, indexX, indexY);
 		}
 		
 		if (counter40.check()) {
 			submitAchievement(Achievements.IN_A_ROW_40, indexX, indexY);
 		}
 		
 		if (counter50.check()) {
 			submitAchievement(Achievements.IN_A_ROW_50, indexX, indexY);
 		}
 		
 		World world = snake.getWorld();
 		
 		if (!world.hasEntity(indexX, indexY)) {
 			directComboDetector.clear();
 		}
 		
 	}
 
 	@Override
 	public void onCollide(int indexX, int indexY, Snake snake,
 			WorldEntity target) {
 		
 		comboDetector.update(target, indexX, indexY);
 		directComboDetector.update(target, indexX, indexY);
 		
 		WorldEntityType type = target.getType();
 		
		if (type.equals(WorldEntityType.RARE_FOOD)) {
			type = WorldEntityType.SMALL_FOOD;
		}
		
 		if (directComboDetector.hasCombo(type, 3)) {
 			submitAchievement(Achievements.COMBO_SAIYAJIN, indexX, indexY);
 			directComboDetector.clear();
 		} else if (directComboDetector.hasCombo(type, 2)) {
 			submitAchievement(Achievements.COMBO_EXPERT, indexX, indexY);
 		}
 		
 		switch (target.getType()) {
 		case ACID:
 			break;
 		case BOMB:
 			incrementAchievement(Achievements.MR_KAMIKAZE, 1, indexX, indexY);
 			break;
 		case RARE_FOOD:
 			break;
 		case SMALL_FOOD:
 			break;
 		case SNAKE:
 			if (target.equals(snake.getTail())) {
 				submitAchievement(Achievements.CIRCLE_OF_DEATH, indexX, indexY);
 			}
 			break;
 		case TELEPORTER:
 			submitAchievement(Achievements.BEAM_ME_UP_SCOTTIE, indexX, indexY);
 			incrementAchievement(Achievements.THE_CAKE_IS_A_LIE, 1, indexX, indexY);
 			
 			if (comboDetector.hasCombo(target.getType(), 3)) {
 				submitAchievement(Achievements.MASTER_OF_TIME, indexX, indexY);
 				comboDetector.clear();
 			} else if (comboDetector.hasCombo(target.getType(), 2)) {
 				submitAchievement(Achievements.TIME_TRAVELLER, indexX, indexY);
 			}
 			break;
 		case ICE:
 			World world = snake.getWorld();
 			// count the bombs 
 			int bombCount = world.getEntityCount(WorldEntityType.BOMB);
 			
 			if (bombCount >= 6) {
 				submitAchievement(Achievements.ICE_CRUSHER, indexX, indexY);
 			}
 			break;
 		default:
 			break;
 		}
 		
 		counter30.count();
 		counter40.count();
 		counter50.count();
 		
 		if (!target.getType().equals(WorldEntityType.SNAKE)) {
 			incrementAchievement(Achievements.ACID_HUNTER, 1, indexX, indexY);
 			incrementAchievement(Achievements.ACID_MASTER, 1, indexX, indexY);
 			incrementAchievement(Achievements.ACID_LEGEND, 1, indexX, indexY);
 		}
 		
 		if (!target.getType().equals(WorldEntityType.SNAKE)) {
 			
 			collected.add(type);
 			
 			if (collected.size() >= 4) {
 				submitAchievement(Achievements.QUADROCOPTER, indexX, indexY);
 			}
 		}		
 	}
 
 	@Override
 	public void onKill(Snake snake) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onSpawn(Snake snake) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onPointsAdd(int points, int level, Player player) {
 		
 	}
 	
 	@Override
 	public void onLevelUp(int oldLevel, int newLevel, Player player) {
 		switch (newLevel) {
 			case 5:
 				google.submitAchievement(Achievements.LEVEL_5);
 				break;
 			case 10:
 				google.submitAchievement(Achievements.LEVEL_10);
 				break;
 			case 20:
 				google.submitAchievement(Achievements.LEVEL_20);
 				break;
 		}
 	}
 
 	// ===========================================================
 	// Methods
 	// ===========================================================
 	
 	public void addListener(AchievementListener listener) {
 		if (!listeners.contains(listener)) {
 			listeners.add(listener);
 		}
 	}
 	
 	private void submitAchievement(String achievementID, int indexX, int indexY) {
 		google.submitAchievement(achievementID);
 		for (AchievementListener listener : listeners) {
 			listener.onAchieve(achievementID, indexX, indexY);
 		}
 	}
 	
 	private void incrementAchievement(String achievementID, int steps, int indexX, int indexY) {
 		google.incrementAchievement(achievementID, steps);
 		for (AchievementListener listener : listeners) {
 			listener.onIncrementalAchieve(achievementID, indexX, indexY);
 		}
 	}
 
 	// ===========================================================
 	// Inner classes
 	// ===========================================================
 	
 	class BlockCounter extends WorldHandler {
 		
 		private final int BLOCKS;
 		
 		private final long TIME;
 		
 		private int pool, totalPool, index, maxIndex;
 		
 		private Map<Integer, Integer> poolMap;
 		
 		private Timer timer;
 		
 		public BlockCounter(final int BLOCKS, final long TIME) {
 			this.BLOCKS = BLOCKS;
 			this.TIME = TIME;
 			pool = 0;
 			totalPool = 0;
 			poolMap = new HashMap<Integer, Integer>();
 			timer = new Timer();
 			timer.start();
 			index = 0;
 			maxIndex = 0;
 		}
 		
 		public void count() {
 			pool++;
 		}
 		
 		public void update() {
 			if (timer.getTicks() > 1000) {
 				
 				final int TIME_FACTOR = (int) (TIME / 1000f);
 				
 				timer.reset();
 				
 				if (poolMap.size() < TIME_FACTOR) {
 					maxIndex = poolMap.size();
 					poolMap.put(poolMap.size(), pool);
 					totalPool += pool;
 				} else {
 					
 					totalPool -= poolMap.get(index);
 					poolMap.remove(index++);
 					poolMap.put(++maxIndex, pool);
 					totalPool += pool;
 				}
 				
 				pool = 0;
 			}
 		}
 		
 		public boolean check() {
 			return totalPool >= BLOCKS;
 		}
 
 		@Override
 		public void onPaused(boolean state) {
 			super.onPaused(state);
 			
 			if (state) {
 				timer.pause();
 			} else {
 				timer.start();
 			}
 		}
 	}
 	
 	
 	
 	class ComboDetector {
 		
 		protected Map<WorldEntityType, Integer> combos;
 		
 		public ComboDetector() {
 			combos = new HashMap<WorldEntityType, Integer>();
 		}
 		
 		public void update(WorldEntity entity, int indexX, int indexY) {
 			
 			if (entity != null) {
 				WorldEntityType type = entity.getType();
 				
 				if (type.equals(WorldEntityType.RARE_FOOD)) {
 					type = WorldEntityType.SMALL_FOOD;
 				}
 				
 				Integer value = combos.get(type);
 				
 				if (value == null) {
 					combos.clear();
 					value = 0;
 				}
 				
 				combos.put(type, ++value);
 			}
 		}
 		
 		public boolean hasCombo(WorldEntityType type, int steps) {
 			return combos.get(type) != null && combos.get(type) == steps;
 		}
 		
 		public void clear() {
 			combos.clear();
 		}
 		
 		public int size(WorldEntityType type) {			
 			Integer size = combos.get(type);			
 			return size != null ? size : 0;
 		}
 	}
 	
 	class DirectComboDetector extends ComboDetector {
 		
 		private int lastIndexX = -1, lastIndexY = -1;
 
 		@Override
 		public void update(WorldEntity entity, int indexX, int indexY) {
 			
 			if (lastIndexX > 0 || lastIndexY > 0) {
 				int deltaX = Math.abs(indexX - lastIndexX);
 				int deltaY = Math.abs(indexY - lastIndexY);
 				
 				if (size(entity.getType()) == 0 || (deltaX <= 1 && deltaY <= 1)) {
 					super.update(entity, indexX, indexY);
 				} else {
 					clear();
 					super.update(entity, indexX, indexY);
 				}
 			} else {
 				super.update(entity, indexX, indexY);
 			}
 			
 			lastIndexX = indexX;
 			lastIndexY = indexY;
 		}	
 	}
 }

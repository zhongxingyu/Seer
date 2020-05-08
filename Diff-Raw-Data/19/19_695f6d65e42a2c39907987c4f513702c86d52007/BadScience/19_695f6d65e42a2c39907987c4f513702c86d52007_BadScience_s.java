 /* 
  * Copyright 2012 Bryan Wyatt
  * 
  * This file is part of BadScience!.
  *  
  * BadScience! is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * BadScience! is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with BadScience!.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package net.brwyatt.badscience;
 
 import java.util.ArrayList;
 
 import net.brwyatt.badscience.levels.levels.TestLevel;
 import net.brwyatt.badscience.levels.menus.MainMenu;
 import net.brwyatt.brge.BRGE;
 import net.brwyatt.brge.Game;
 import net.brwyatt.brge.graphics.ScreenObjects;
 import net.brwyatt.brge.levelgrid.LevelGrid;
 import net.brwyatt.brge.levels.Level;
 
 
 public class BadScience extends Game {
 	private final String title="Bad Science!";
	private LevelGrid levelGrid;
 	private Level currentLevel=null;
 	private ArrayList<Level> levels;
 	
 	public BadScience(ScreenObjects so){
 		screenObjects=so;
 		levels=new ArrayList<Level>();
 		
		levelGrid=new LevelGrid(BRGE.getWidth(),BRGE.getHeight());
 		levels.add(new MainMenu(this, screenObjects));
		levels.add(new TestLevel(this, screenObjects,levelGrid));
 	}
 	
 	public void startGame() {
 		loadLevel(0);
 	}
 	public void nextLevel() {
 	}
 	public void lastLevel() {
 	}
 	public void loadLevel(int l) {
 		if(currentLevel!=null){
 			currentLevel.endLevel();
 		}
 		currentLevel=levels.get(l);
 		currentLevel.startLevel();
 	}
 	public void saveGame() {
 	}
 	public void loadGame() {
 	}
 	public void pauseGame() {
 	}
 	public void unpauseGame() {
 	}
 	public void showInGameMenu() {
 	}
 	public void keyPressed(int key) {
 		currentLevel.keyPressed(key);
 	}
 	public void keyReleased(int key) {
 		currentLevel.keyReleased(key);
 	}
 	public void keyTyped(int key) {
 		currentLevel.keyTyped(key);
 	}
 	public String getGameTitle() {
 		return title;
 	}
 }

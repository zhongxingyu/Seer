 package brwyatt.brge.examplegame;
 
 import java.util.ArrayList;
 
 import brwyatt.brge.Game;
 import brwyatt.brge.examplegame.levels.ExampleLevel;
 import brwyatt.brge.examplegame.levels.ExampleMenu;
 import brwyatt.brge.graphics.ScreenObjects;
 import brwyatt.brge.levels.Level;
 
 public class ExampleGame extends Game {
	private final String title="BRGE Test Game";
 	private Level currentLevel=null;
 	private ArrayList<Level> levels;
 	
 	public ExampleGame(ScreenObjects so){
 		screenObjects=so;
 		levels=new ArrayList<Level>();
 
 		levels.add(new ExampleMenu(this, screenObjects));
 		levels.add(new ExampleLevel(this, screenObjects));
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

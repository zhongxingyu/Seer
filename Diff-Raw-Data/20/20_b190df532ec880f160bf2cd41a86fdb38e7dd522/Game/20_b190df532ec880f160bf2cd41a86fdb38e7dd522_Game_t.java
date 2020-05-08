 package brwyatt.brge;
 
 import brwyatt.brge.graphics.ScreenObjects;
 
 public abstract class Game {
 	protected ScreenObjects screenObjects;
 	
 	public Game(){
 	}
 	public Game(ScreenObjects so){
 		screenObjects=so;
 	}
 	public abstract void startGame();
 	public abstract void nextLevel();
 	public abstract void lastLevel();
 	public abstract void loadLevel(int l);
 	public abstract void saveGame();
 	public abstract void loadGame();
 	public abstract void pauseGame();
 	public abstract void unpauseGame();
 	public abstract void showInGameMenu();
 	public abstract void keyPressed(int key);
 	public abstract void keyReleased(int key);
 	public abstract void keyTyped(int key);
 	public abstract String getGameTitle();
	public ScreenObjects getScreenObjects(){
		return screenObjects;
	}
 }

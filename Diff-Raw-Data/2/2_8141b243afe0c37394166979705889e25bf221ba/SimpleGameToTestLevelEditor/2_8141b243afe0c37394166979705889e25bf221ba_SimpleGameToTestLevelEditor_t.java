 /**
  * @author Michael Zhou (Dominator008)
  */
 package game;
 
 import java.awt.Dimension;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.event.KeyEvent;
 import java.io.File;
 
 import leveleditor.VoogaUtilities;
 
 import com.golden.gamedev.Game;
 import com.golden.gamedev.GameLoader;
 import com.golden.gamedev.object.*;
 import com.golden.gamedev.object.background.*;
 
 public class SimpleGameToTestLevelEditor extends Game {
 	
     private PlayField myPlayfield;  
     private Background myBackground;
     private GameScroller myGameScroller;
     
     private SpriteGroup myPlayers;
     private SpriteGroup myCharacters;
     private SpriteGroup mySetting;
     private Sprite myHero;
     private int LEVEL_WIDTH = 1000;
     private int LEVEL_HEIGHT = 500;
 	
     public static void main(String[] args) {
         GameLoader game = new GameLoader();
         game.setup(new SimpleGameToTestLevelEditor(), new Dimension(800,600), false);
 	game.start();
     }
 
     private void hardCodedLoadLevel() {
 	LevelState loadedState = VoogaUtilities.loadLevel(new File("saves/level2.lvl"));
 	for (Point p: loadedState.mySpriteMap.keySet()) {
 	    Sprite tocreate = loadedState.mySpriteMap.get(p).getSprite();
 	    tocreate.setLocation(p.x, p.y);
 	    myPlayfield.add(tocreate);
 	}
 	myHero = myPlayfield.getExtraGroup().getActiveSprite(); // RANDOM
 	myBackground = new ImageBackground(getImage(loadedState.myBackGroundSrc));
 	myPlayfield.setBackground(myBackground);
 	//myGameScroller = new GameScroller(myPlayers, myBackground);
     }
     
 	public void initResources() {
 		// TODO LOAD STUFF GOES HERE
 		myPlayfield = new PlayField();
 	    	hardCodedLoadLevel();
 		super.initEngine();
 		// Sprite Loading
 		myPlayers = myPlayfield.addGroup( new SpriteGroup("Player Group") );
 		myCharacters = myPlayfield.addGroup( new SpriteGroup("Character Group") );
 		mySetting = myPlayfield.addGroup( new SpriteGroup("Setting Group") );
 	}
 	
 	public void update(long arg0) {
 		myPlayfield.update(arg0);
 		if (keyDown(KeyEvent.VK_UP)) {
 		    myHero.setVerticalSpeed(-0.5);
 		}
 		else if (keyDown(KeyEvent.VK_DOWN)) {
 		    myHero.setVerticalSpeed(0.5);
 		}
 		else if (keyDown(KeyEvent.VK_RIGHT)) {
 		    myHero.setHorizontalSpeed(0.5);
 		}
 		else if (keyDown(KeyEvent.VK_LEFT)) {
 		    myHero.setHorizontalSpeed(-0.5);
 		}
 		else myHero.setSpeed(0, 0);
 	}
 
 	public void render(Graphics2D arg0) {
		//myGameScroller.scroll();
 		myPlayfield.render(arg0);	
 	}
     
     
 
 }

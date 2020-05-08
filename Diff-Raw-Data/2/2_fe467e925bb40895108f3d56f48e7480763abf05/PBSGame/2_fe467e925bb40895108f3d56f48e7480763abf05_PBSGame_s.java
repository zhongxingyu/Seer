 package pbs;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.awt.geom.*;
 import java.io.*;
 import java.util.Scanner;
 
 import jig.engine.*;
 import jig.engine.hli.*;
 import jig.engine.physics.*;
 import jig.engine.util.*;
 import jig.engine.audio.jsound.*;
 
 import pbs.Level.Layer;
 import pbs.Entity.*;
 import pbs.Weapons.*;
 import pbs.Updater.*;
 import pbs.parser.*;
 import pbs.Renders.*;
 
 public class PBSGame extends ScrollingScreenGame {
     
     public static int SCREEN_WIDTH = 640;
     public static int SCREEN_HEIGHT = 480;
     public static int X_MID = SCREEN_WIDTH / 2;
     public static int Y_MID = SCREEN_HEIGHT / 2;
     public static int P_STARTX = 20;
     public static int P_STARTY = Y_MID;
     public static long FRAME_SIZE = 16;
     public static String SPRITE_SHEET = "resources/pbs-spritesheet.png";
     private static final String START_LEVEL = "resources/splash.lvl";
     private static final int START_LIVES = 3;
     int s;
     public static int PLAYER_MAX_HP = 10;
     
     ResourceFactory rf;
     
     Level levelData;
 
     EntityFactory ef;
     
     Entity player;
 
     String currentLevel;
     int highScore;
     int lives;
 
 
     
     protected boolean waitForReset;
 
     // hud variables
     protected FontResource hudFont;
     
     public PBSGame() {
 	super(SCREEN_WIDTH, SCREEN_HEIGHT, false);
 //	s = 0;
 	waitForReset = false;
 
 	ef = new EntityFactory();
 	
 	rf = ResourceFactory.getFactory();
 	rf.loadResources("resources/", "pbs-resources.xml");
 
 	Font sFont = new Font("Sans Serif", Font.BOLD, 18);
 	try {
 		sFont = Font.createFont(Font.TRUETYPE_FONT, new java.io.File("./build/resources/prstartk.ttf"));
 		sFont = sFont.deriveFont(12f);
 	} catch (FontFormatException e) {
 		e.printStackTrace();
 	} catch (IOException e) {
 		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
 		for(String s : ge.getAvailableFontFamilyNames())
 			System.out.println("Font:" + s);
 		System.out.println(ResourceFactory.findResource("/resources/prstartk.ttf").getPath());
 		System.out.println("Canonical Path:" + new java.io.File(".").getAbsolutePath());
 		e.printStackTrace();
 	}
 //	hudFont = rf.getFontResource(new Font("Sans Serif", Font.PLAIN, 24), Color.white, null);
 	hudFont = rf.getFontResource(sFont, Color.white, null);
 
 	currentLevel = START_LEVEL;
 	lives = START_LIVES;
 	highScore = getHighScore();
 	resetLevel();
 
 	GameClock.TimeManager tm = new GameClock.SleepIfNeededTimeManager(60.0);
 	theClock.setTimeManager(tm);
     }
 
     // this method renders the hud
     public void render(RenderingContext rc) {
 		super.render(rc);
 	
 		ImageResource image;
 		String message = "Score: " + levelData.getScore();
 		int x = 10;
 		int y = 10; //SCREEN_HEIGHT - hudFont.getHeight() - 10;
 		hudFont.render(message, rc, AffineTransform.getTranslateInstance(x, y));
 
 		message = "High Score: ";
 		x = SCREEN_WIDTH - hudFont.getStringWidth(message) - 75;
 		hudFont.render(message, rc, AffineTransform.getTranslateInstance(x, y));
 		
 		x = SCREEN_WIDTH - 75;
 		message = "" + highScore;
 		hudFont.render(message, rc, AffineTransform.getTranslateInstance(x, y));
 		
 		image = rf.getFrames(SPRITE_SHEET + "#shipico").get(0);
 		x = 10;
 		y = SCREEN_HEIGHT - (hudFont.getHeight() / 2) - (image.getHeight() / 2) - 10;
 		image.render(rc, AffineTransform.getTranslateInstance(x, y));
 		
 		message = " x" + lives;
 		x = 10 + image.getWidth();
 		y = SCREEN_HEIGHT - hudFont.getHeight() - 10;
 		hudFont.render(message, rc, AffineTransform.getTranslateInstance(x, y));
 		
 		image = rf.getFrames(SPRITE_SHEET + "#fullhp").get(0);
 		message = "Health:"; // + player.hp + " / " + PLAYER_MAX_HP;
 		x = X_MID - ((hudFont.getStringWidth(message) + (image.getWidth() * PLAYER_MAX_HP)) / 2);
 		y = SCREEN_HEIGHT - hudFont.getHeight() - 10;
 		hudFont.render(message, rc, AffineTransform.getTranslateInstance(x, y));
 		x += hudFont.getStringWidth(message);
 		y = SCREEN_HEIGHT - (hudFont.getHeight() / 2) - (image.getHeight() / 2) - 10;
 		for(int i = 0; i < PLAYER_MAX_HP; i++) {
 			if(i == player.hp)
 				image = rf.getFrames(SPRITE_SHEET + "#deadhp").get(0);
 			x += image.getWidth();
 			image.render(rc, AffineTransform.getTranslateInstance(x, y));
 		}
 
 		message = levelData.getMessage();
 		x = X_MID  - hudFont.getStringWidth(message)/2;
 		y = SCREEN_HEIGHT - 50;
 		hudFont.render(message, rc, AffineTransform.getTranslateInstance(x, y));
 		
 		
     }
 
     public void update(long deltaMs) {
     
 	//centerOnPoint(levelData.getCam()); // center on level camera
 	centerOnPoint(levelData.getCam());
 	if(levelData.getScore() > highScore) {
 		setHighScore(levelData.getScore());
 	} 
 	
 	Vector2D topleft = screenToWorld(new Vector2D(0, 0));
 	Vector2D botright = screenToWorld(new Vector2D(SCREEN_WIDTH, SCREEN_HEIGHT));
 
 	pushPlayerToBounds(topleft, botright);
 	
 	player.setPosition(player.getPosition().translate(levelData.getScrollSpeed()
 							  .scale(deltaMs/100.0)));
 	levelData.update(FRAME_SIZE, topleft, botright);
 
 
 	//if level complete, get next level
 	if(levelData.levelComplete() || keyboard.isPressed(KeyEvent.VK_C)){
 //		s = levelData.score;
 		//levelData.setMessage("Congratulations! Level Complete!");
 	    currentLevel = levelData.getNextLevel();
 	    waitForReset = true;
 	}
 	//if player dead, reset current level
 	if(player.alive() == false && !waitForReset){
 		lives--;
 		waitForReset = true;
 		
 		if(lives < 0) { //if player dead, and out of lives, reset game
		    levelData.setMessage("Game Over! Press Spacebar to start a new game");
 		    if(highScore > getHighScore())
 		    	saveHighScore(highScore);
 		    currentLevel = START_LEVEL;
 		    lives = START_LIVES;
 		    levelData.score = 0;
 		} else {
 			levelData.setMessage("Better luck next time!");
 		}	    
 	}
 
 	//reset when we hit hte space bar
 	if(waitForReset && keyboard.isPressed(KeyEvent.VK_SPACE)){
 	    int scr = levelData.score;
 		player = null;
 	    resetLevel();
 	    levelData.score = scr;
 	}
 
     }
 
     public void resetLevel(){
 	LevelParser lp = new LevelParser(currentLevel);
 	levelData = lp.createLevel();
 
 	waitForReset = false;
 	
 	player = new Entity(SPRITE_SHEET + "#defender2");
 	player.setPosition(new Vector2D(P_STARTX, P_STARTY));
 	player.setCustomUpdate(new KeyboardControls(keyboard));
 	player.setCustomWeapon(new FriendlySpread());
 	levelData.add(player, Layer.PLAYER);
 
 	gameObjectLayers.clear();
 	gameObjectLayers = levelData.getLayers();
     }
 
     public void pushPlayerToBounds(Vector2D tl, Vector2D br){
 
 	if(waitForReset)
 	    return;
 
 	Vector2D p = player.getPosition();
 	double newx = p.getX();
 	double newy = p.getY();
 
 	if(p.getX() < tl.getX()){
 	    newx = tl.getX();
 	} else if(p.getX() + player.getWidth()-1 > br.getX()){
 	    newx = br.getX() - player.getWidth()-1;
 	}
 
 	if(p.getY() < tl.getY()){
 	    newy = tl.getY();
 	} else if(p.getY() + player.getHeight()-1 > br.getY()){
 	    newy = br.getY() - player.getHeight()-1;
 
 	}
 
 	player.setPosition(new Vector2D(newx, newy));
     }
 
     protected class KeyboardControls implements CustomUpdate {
 	Keyboard key;
 	public KeyboardControls(Keyboard k) {
 	    key = k;
 	}
 
 	public void update(Entity e, long deltaMs) {
 
 	    boolean left = key.isPressed(KeyEvent.VK_LEFT);
 	    boolean right = key.isPressed(KeyEvent.VK_RIGHT);
 	    boolean up = key.isPressed(KeyEvent.VK_UP);
 	    boolean down = key.isPressed(KeyEvent.VK_DOWN);
 
 	    boolean reset = left && right;
 
 	    boolean fire = key.isPressed(KeyEvent.VK_SPACE);
 
 	    Vector2D pos = e.getPosition();
 	    e.setVelocity(new Vector2D(0, 0));
 
 	    if (fire) {
 		e.shoot(levelData, deltaMs);
 	    }
 
 	    if (left && !right) {
 		e.setVelocity(new Vector2D(-30, 0));
 	    }
 
 	    if (right && !left) {
 		e.setVelocity(new Vector2D(30, 0));
 	    }
 
 	    if (up && !down) {
 		e.setVelocity(new Vector2D(0, -30));
 	    }
 
 	    if (down && !up) {
 		e.setVelocity(new Vector2D(0, 30));
 	    }
 
 	    e.setPosition(pos.translate(e.getVelocity().scale(deltaMs / 100.0)));
 	}
     }
     
 	public int getHighScore() {
 		File file = null;
 		Scanner input = null;
 		PrintWriter writer = null;
 		int highScore = 1000;
 		
 		try {
 			file = new File("highscores.dat");
 			if(!file.exists()) {
 				file.createNewFile();
 				writer = new PrintWriter(new FileWriter(file));
 				writer.println(highScore);
 			} else { 
 				input = new Scanner(file);
 				highScore = input.nextInt();
 			}
 		} catch (IOException ex) {
 			System.err.println("ERROR: IO Error creating getHighScore\n" + ex);
 		} finally {
 			if(input != null) input.close();
 			if(writer != null) writer.close();
 		}
 		return highScore;
 	}
 	
 	public void setHighScore(int highScore) {
 		this.highScore = highScore;
 	}
 	
 	public void saveHighScore(int highScore) {
 		File file = null;
 		PrintWriter writer = null;
 		try {
 			file = new File("highscores.dat");
 			if(!file.exists()) {
 				file.createNewFile();
 				highScore = 1000;
 			} 
 			
 			this.highScore = highScore;
 			writer = new PrintWriter(new FileWriter(file));
 			writer.println(highScore);			
 		} catch (IOException ex) {
 			System.err.println("ERROR: Error saving high score\n" + ex);
 		} finally {
 			if(writer != null) writer.close();
 		}
 	}
 
     public static void main(String[] args) {
 
 	PBSGame game = new PBSGame();
 	game.run();
 
     }
 
 }

 package me.bevilacqua.ld48;
 
 import me.bevilacqua.ld48.Level.Level;
 import me.bevilacqua.ld48.Mob.Player;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.Sound;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 public class Play extends BasicGameState {
 
 	private Level level1 , level2 , level3 , level4 , level5 , level6 , level7 , level8 , level9 , finalLevel ,  holdLevel;
 	private Image p1 , p2 , p3 , p4;
 	private static Sound levelSwitch;
 	private Image[] images = new Image[4];
 	private static float x , y;
	private static InputHandler handler;
 	private Player player;
 	private static Level[] levels = new Level[10]; //TODO:adjust number of levels acourdingly
	private static byte currentLevel;
 	private static boolean switchin = false;
 	private static boolean endGame;
 	public static boolean lf = false;
 	
 	private static boolean flag = false;
 	private static int elapsedTime;
 	private int DELAY = 2000;
 	
 	private byte playerMovementID = 0;
 
	public static InputHandler getHandler() {
		return handler;
	}
	
 	@Override
 	public void init(GameContainer gc, StateBasedGame sbg) throws SlickException {
 		level1 = new Level("/res/Lvl1.tmx" , "Level1" , "/music/Levels/1.wav"  , 1000 * 30, "/res/Diary/Chp1.png"); //Probably not lvl1 ... nevermind it is :)
 		level2 = new Level("/res/Lvl2.tmx" , "Level2" , "/music/Levels/2.wav" , 1000 * 25, "/res/Diary/Chp2.png");
 		level3 = new Level("/res/Lvl3.tmx" , "Level3" , "/music/Levels/3.wav" , 1000 * 30, "/res/Diary/Chp3.png");
 		level4 = new Level("/res/Lvl4.tmx" , "Level4" , "/music/Levels/4.wav" , 1000 * 50, "/res/Diary/Chp4.png");
 		level5 = new Level("/res/Lvl5.tmx" , "Level5" , "/music/Levels/5.wav" , 1000 * 58, "/res/Diary/Chp5.png");
 		level6 = new Level("/res/Lvl6.tmx" , "Level6" , "/music/Levels/6.wav" , 1000 * 20, "/res/Diary/Chp6.png");
 		level7 = new Level("/res/Lvl7.tmx" , "Level7" , "/music/Levels/7.wav" , 1000 * 30, "/res/Diary/Chp7.png");
 		level8 = new Level("/res/Lvl8.tmx" , "Level8" , "/music/Levels/8.wav" , 1000 * 21, "/res/Diary/Chp8.png");
 		level9 = new Level("/res/Lvl9.tmx" , "Level9" , "/music/Levels/9.wav" , 1000 * 33, "/res/Diary/Chp9.png");
 		finalLevel = new Level("/res/Final.tmx" , "Final" , "/music/Levels/final.wav" , 1000 * 33, "/res/Diary/Chp10.png");
 		levels[0] = level1;
 		levels[1] = level2;
 		levels[2] = level3;
 		levels[3] = level4;
 		levels[4] = level5;
 		levels[5] = level6;
 		levels[6] = level7;
 		levels[7] = level8;
 		levels[8] = level9;
 		levels[9] = finalLevel;
 		
 		p1 = new Image("/res/Mob/playerUp.png");
 		p2 = new Image("/res/Mob/playerLeft.png");
 		p3 = new Image("/res/Mob/playerRight.png");
 		p4 = new Image("/res/Mob/player.png");
 		images[0] = p1;
 		images[1] = p2;
 		images[2] = p3;
 		images[3] = p4;
 		
 		levelSwitch = new Sound("/sfx/levelSwitch.wav");
 		
 		handler = new InputHandler(gc);
 	
 		player = new Player(images, handler , levels[currentLevel]);
 		System.out.println( levels.length + " Levels");
 
 	}
 
 	@Override
 	public void render(GameContainer gc, StateBasedGame sbg, Graphics g) throws SlickException {
 		if(!endGame) {
 			levels[currentLevel].render(Math.round(x), Math.round(y), 0, 0, g);
 			if(!levels[currentLevel].preMode) {
 				player.render();
 				g.drawString("ScrewDrivers: " + player.getScore() + " / 3",300 , 40);
 			}
 		}
 		
 	}
 
 	@Override
 	public void update(GameContainer gc, StateBasedGame sbg, int Delta) throws SlickException {	
 		handler.update(); //That was terrifying DO NOT FORGET THIS!!!
 		playerMovementID = player.update(Delta);
 		if(!endGame) {
 			levels[currentLevel].update(Delta);
 		}
 		
 		if(elapsedTime > DELAY) {
 			flag = false;
 			elapsedTime = 0;
 		}
 		
 		elapsedTime += Delta;
 		
 		if(lf == true) {
 			failLevel();
 			player = new Player(images , handler , levels[currentLevel]);
 			lf = false;
 		}
 		
 		if(switchin) {
 			player = new Player(images , handler , levels[currentLevel]);
 			switchin = false;
 		}
 		
 		if(endGame) {
 			System.out.println("DoubleConfirm");
 			sbg.enterState(3);
 		}
 		
 		if(playerMovementID != -1) 
 		if(playerMovementID == 0) {
 			if( y > 0 ) {
 				y -= 1;
 			}
 		}
 		
 		if(playerMovementID == 1) {
 			if(x > 0 ) {
 				x -= 1;
 			}
 		}
 		
 		if(playerMovementID == 3) {
 			if(y < levels[currentLevel].getHeight()) { 
 				y += 1;
 			}
 		}
 		
 		if(playerMovementID == 2) {
 			if(x < levels[currentLevel].getWidth()) {
 				x += 1;
 			}
 		}
 		if(handler.Reset()) {
 			if(flag == false) lf = true;
 			flag = true;
 		}
 		
 		
 	}
 
 	public static int getX() {
 		return (int) x;
 	}
 	
 	public static int getY() {
 		return (int) y;
 	}
 	
 	public static void switchLevel() {
 		levels[currentLevel].endMusic();
 		currentLevel++;
 		if(currentLevel  > 9) {
 			endGame = true;
 			System.out.println("Going to endGame");
 		} else {
 			System.out.println("Switching to: " + levels[currentLevel].getName());
 			resetXY();
 			levelSwitch.play();
 			switchin = true;
 		}
 	}
 	
 	public void failLevel() throws SlickException {
 		levels[currentLevel].failLevel();
 		holdLevel = new Level(levels[currentLevel].getMapPath() , levels[currentLevel].getName() ,levels[currentLevel].getSoundPath() , levels[currentLevel].getTimer() , levels[currentLevel].getDiaryPath());
 		levels[currentLevel] = holdLevel;
 		System.out.println("resettingLevel");
 		resetXY();
 	}
 	
 	public static void resetXY() {
 		Play.x =0;
 		Play.y =0;
 	}
 	
 	@Override
 	public int getID() {
 		return 2;
 	}
 
 }

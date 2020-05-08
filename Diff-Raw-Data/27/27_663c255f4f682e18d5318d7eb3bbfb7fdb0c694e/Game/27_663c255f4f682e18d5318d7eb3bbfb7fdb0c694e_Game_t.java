 package isr;
 
 import org.newdawn.slick.AppGameContainer;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.SlickException;
 
 import backend.ui.UIStateBasedGame;
 
 public class Game extends UIStateBasedGame
 {
 	public static final String TITLE = "Our ISR game";
 	public static final int SCREEN_WIDTH = 1000;
 	public static final int SCREEN_HEIGHT = 700;
	public static final String ASSETS_DIR = "assets/";
	public static final boolean DEBUG = false;
 	
 	public static final int MAIN_MENU = 1;
 	public static final int INTRO_MISSION = 2;
 	public static final int INTRO_DIVE = 3;
 	public static final int GAME_PLAY = 4;
 	public static final int GAME_LOOSE = 6;
 	public static final int GAME_WIN = 7;
 	public static final int CREDITS = 8;
 
 	public static void main(String args[])
 	{
 		try
 		{
 			AppGameContainer gc = new AppGameContainer(new Game());
 			gc.setDisplayMode(SCREEN_WIDTH, SCREEN_HEIGHT, false);
 			gc.setTargetFrameRate(60);
 			gc.setMaximumLogicUpdateInterval(200);
 			gc.start();
 		} 
 		catch (SlickException e)
 		{
 			e.printStackTrace();
 		}
 		
 	}
 
 	public Game()
 	{
 		super(TITLE);
 		this.addState(new MainMenu());
 		this.addState(new IntroMission());
 		this.addState(new IntroDive());
 		this.addState(new GamePlay());
 		this.addState(new Credits());
 		this.addState(new GameWin());
 		this.addState(new GameLoose());
 	}
 
 	@Override
 	public void initStatesList(GameContainer gc) throws SlickException
 	{
 		Ship.loadContent();
 		Room.loadContent();
 		Sounds.load();
 		Character.loadContent();
 		ScrollBackground.loadContent();
 		DescriptionBar.loadContent();
 		EnemyShip.loadContent();
 		
 //		this.getState(MAIN_MENU).init(gc, this);
 //		this.getState(GAME_PLAY).init(gc, this);
 	}
 
 }
 
 

 package game;
 
 import org.newdawn.slick.AppGameContainer;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.StateBasedGame;
 
 import states.EndGameScreen;
 import states.GameState;
 import states.HistoryScreen;
 import states.InGameMenu;
 import states.PreGameMenu;
 import states.StartScreen;
 import entities.Camera;
 
 public class GunsAndHats extends StateBasedGame {
 
 	private static int FRAMEWIDTH = 800;
 	private static int FRAMEHEIGHT = 600;
 	private static int FRAMERATE = 60;
 	
 	
 	//perhaps change these to public?? - peter
	public static int STARTSCREEN = 1;
	public static int PREGAMESTATE = 2;
	public static int GAMESTATE = 3;
	public static int INGAMEMENU = 4;
	public static int ENDGAMESCREEN = 5;
	public static int HISTORYSCREEN = 6;
 	
 	
 	
 	private Camera camera;
 	
 	public GunsAndHats() {
 		super("Guns and Hats");
 	}
 
 	@Override
 	public void initStatesList(GameContainer gc) throws SlickException {
 		this.addState(new StartScreen(STARTSCREEN, camera));
 		this.addState(new PreGameMenu(PREGAMESTATE, camera));
 		this.addState(new GameState(GAMESTATE, camera));
 		this.addState(new InGameMenu(INGAMEMENU, camera));
 		this.addState(new EndGameScreen(ENDGAMESCREEN, camera));
 		this.addState(new HistoryScreen(HISTORYSCREEN, camera));
 		
 		this.enterState(STARTSCREEN);
 	}
 	
 	public void setCamera(int frameWidth, int frameHeight, int screenWidth, int screenHeight) {
 		camera = new Camera(screenWidth, screenHeight, frameWidth, frameHeight);
 	}
 	
 	public static void main(String[] args) throws SlickException {
 		GunsAndHats game = new GunsAndHats();
 		AppGameContainer app = new AppGameContainer(game);
 		game.setCamera(FRAMEWIDTH, FRAMEHEIGHT, app.getScreenWidth(), app.getScreenHeight());
 		app.setDisplayMode(app.getScreenWidth(), app.getScreenHeight(), true);
 		game.setCamera(FRAMEWIDTH, FRAMEHEIGHT, FRAMEWIDTH, FRAMEHEIGHT);
 		app.setDisplayMode(FRAMEWIDTH, FRAMEHEIGHT, false);
 		app.setVSync(true); // Keepin' it smooooooothhh...
 		app.setTargetFrameRate(FRAMERATE);
 		app.setShowFPS(false);
 		app.start();
 	}
 
 }

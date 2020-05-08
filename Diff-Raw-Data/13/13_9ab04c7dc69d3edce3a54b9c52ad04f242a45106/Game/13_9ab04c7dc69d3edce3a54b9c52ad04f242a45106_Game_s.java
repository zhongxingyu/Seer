 package game;
 
 import org.newdawn.slick.AppGameContainer;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Music;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.StateBasedGame;
 
 public class Game extends StateBasedGame {
 	private static Music music;
 	public static AppGameContainer app;
 
 	public Game() {
 		super("Redneck Simulator 2 Super HD Turbo Remix");
 		try {
 			music = new Music("res/music/zelda.ogg");
 		} catch (SlickException e) {
 			e.printStackTrace();
 		}
 		
 		startMusic();
 		Controller.init();
 	}
 
 	@Override
 	public void initStatesList(GameContainer arg0) throws SlickException {
 		addState(new MenuState());
 		addState(new InGameState());
 		addState(new SettingsState());
 		
 	}
 
 	public static void main(String[] args) {
 		try {
 			
 			app = new AppGameContainer(new Game());
			app.setDisplayMode(1920, 1080, true);
 			app.setTargetFrameRate(60);
 			app.setShowFPS(false);
 			app.setSmoothDeltas(true);
 			app.start();
 			
 			
 		} catch (SlickException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public static void startMusic() {
 		music.loop();
 	}
 	
 	public static void stopMusic() {
 		music.stop();
 	}
 	
 	
 }

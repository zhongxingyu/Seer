 package game;
 
 import org.lwjgl.LWJGLException;
 import org.lwjgl.opengl.DisplayMode;
 import org.lwjgl.opengl.Display;
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
 			
 			try {
 				DisplayMode[] modes = Display.getAvailableDisplayModes();
 				for (int i=0;i<modes.length;i++) {
 		             DisplayMode current = modes[i];
 		             System.out.println(current.getWidth() + "x" + current.getHeight() + "x" +
 		                                 current.getBitsPerPixel() + " " + current.getFrequency() + "Hz");
 		         }
		         //System.out.println(gc.getAspectRatio());
 			} catch (LWJGLException e) {
 				e.printStackTrace();
 			}
 			
 			app = new AppGameContainer(new Game());
			app.setDisplayMode(1680, 1050, true);
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

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
 	private static DisplayMode dispMode;
 	
 	public static AppGameContainer app;
 	public static int centerHeight;
 	public static int centerWidth;
 	public static boolean hitBox;
 
 	public Game() {
 		super("Redneck Simulator 2 Super HD Turbo Remix");
 		try {
 			music = new Music("res/music/zelda.ogg");
 		} catch (SlickException e) {
 			e.printStackTrace();
 		}
 		
 		hitBox = false;
 		music.loop();
 		music.pause();
 		Controller.init();
 	}
 
 	@Override
 	public void initStatesList(GameContainer arg0) throws SlickException {
 		centerHeight = app.getHeight() / 2;
 		centerWidth = app.getWidth() / 2;
 		
 		addState(new MenuState());
 		addState(new InGameState());
 		addState(new SettingsState());
 		
 		
 	}
 
 	public static void main(String[] args) {
 		try {
 			
 			try {
 				DisplayMode[] modes = Display.getAvailableDisplayModes();
 				dispMode = modes[0];
 				for (int i=0;i<modes.length;i++) {
		             //DisplayMode current = modes[i];
 		             if(modes[i].getWidth() > dispMode.getWidth()){
 		            	 dispMode = modes[i];
 		             }
 		         }
 			} catch (LWJGLException e) {
 				e.printStackTrace();
 			}
 			
 			app = new AppGameContainer(new Game());
 			app.setDisplayMode(dispMode.getWidth(), dispMode.getHeight(), true);
 			app.setTargetFrameRate(60);
 			app.setShowFPS(false);
 			app.setSmoothDeltas(true);
 			app.start();
 			
 			
 			
 
 		} catch (SlickException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public static Music getMusic() {
 		return music;
 	}
 	
 	public static DisplayMode getDisplayMode(){
 		return dispMode;
 	}
 	
 }

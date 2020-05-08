 package game;
 
 import org.newdawn.slick.AppGameContainer;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.StateBasedGame;
 
 public class Game extends StateBasedGame {
 
 	public static AppGameContainer app;
 
 	public Game() {
		super("best game");
 	}
 
 	@Override
 	public void initStatesList(GameContainer arg0) throws SlickException {
 		addState(new InGameState());
 		addState(new MenuState());
 
 	}
 
 	public static void main(String[] args) {
 		try {
 			app = new AppGameContainer(new Game(), 800, 600, false);
 			app.setTargetFrameRate(60);
 			app.setShowFPS(false);
 			app.start();
 		} catch (SlickException e) {
 			e.printStackTrace();
 		}
 	}
 }

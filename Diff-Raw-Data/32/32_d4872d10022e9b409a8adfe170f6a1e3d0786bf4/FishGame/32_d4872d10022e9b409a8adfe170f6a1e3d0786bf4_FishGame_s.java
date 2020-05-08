 package base;
 
 import org.newdawn.slick.AppGameContainer;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.StateBasedGame;
 
 public class FishGame extends StateBasedGame {
 
 	public static final int MAIN_MENU_STATE = 1;
 	public static final int GAMEPLAY_STATE = 2;
 	public static final int GAMEOVER_STATE = 3;
 
 	public FishGame() {
 		super("Fish Shit");
 	}
 
 	@Override
 	public void initStatesList(GameContainer gc) throws SlickException {
 		this.addState(new MainMenuState(MAIN_MENU_STATE));
 		this.addState(new GameplayState(GAMEPLAY_STATE));
 		this.addState(new GameOverState(GAMEOVER_STATE));
 	}
 
 	public static void main(String[] args) throws SlickException {
 		FishGame game = new FishGame();
 
 		AppGameContainer container = new AppGameContainer(game);
 
		container.setTargetFrameRate(30);

 		container.setDisplayMode(1000, 600, false);
 
 		container.start();
 
 		return;
 	}
 
 }

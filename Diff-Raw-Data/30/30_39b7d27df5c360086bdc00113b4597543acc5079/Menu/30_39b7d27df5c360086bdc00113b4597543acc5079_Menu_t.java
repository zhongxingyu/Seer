 package chalmers.TDA367.B17.states;
 
 import chalmers.TDA367.B17.view.MenuButton;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.SpriteSheet;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 import chalmers.TDA367.B17.Tansk;
 import chalmers.TDA367.B17.controller.GameController;
 import chalmers.TDA367.B17.sound.SoundHandler.MusicType;
 
 public class Menu extends BasicGameState{
 	
 
 	private MenuButton playgroundButton;
 	private MenuButton hostButton;
 	private MenuButton joinButton;
 	private MenuButton exitButton;
 	private MenuButton settingsButton;
 	private int state;
 	private SpriteSheet background;
 	
 	public Menu(int state) {
 		this.state = state;
 	}
 
 	public void init(GameContainer gc, StateBasedGame sbg)
 			throws SlickException {
 		playgroundButton = new MenuButton(100, 175, GameController.getInstance().getImageHandler().getSprite("button_playground"),
 				GameController.getInstance().getImageHandler().getSprite("button_playground_pressed"),
 				GameController.getInstance().getImageHandler().getSprite("button_playground_hover"));
 		hostButton = new MenuButton(100, 275, GameController.getInstance().getImageHandler().getSprite("button_host"),
 				GameController.getInstance().getImageHandler().getSprite("button_host_pressed"),
 				GameController.getInstance().getImageHandler().getSprite("button_host_hover"));
 		joinButton = new MenuButton(100, 375, GameController.getInstance().getImageHandler().getSprite("button_join"),
 				GameController.getInstance().getImageHandler().getSprite("button_join_pressed"),
 				GameController.getInstance().getImageHandler().getSprite("button_join_hover"));
 		settingsButton = new MenuButton(100, 475, GameController.getInstance().getImageHandler().getSprite("button_settings"),
 				GameController.getInstance().getImageHandler().getSprite("button_settings_pressed"),
 				GameController.getInstance().getImageHandler().getSprite("button_settings_hover"));
 		exitButton = new MenuButton(100, 675, GameController.getInstance().getImageHandler().getSprite("button_exit"),
 				GameController.getInstance().getImageHandler().getSprite("button_exit_pressed"),
 				GameController.getInstance().getImageHandler().getSprite("button_exit_hover"));
 		
 		background = new SpriteSheet(GameController.getInstance().getImageHandler().getSprite("background"),
 				Tansk.SCREEN_WIDTH, Tansk.SCREEN_HEIGHT);
 	}
 	
 	@Override
 	public void enter(GameContainer gc, StateBasedGame stateBasedGame){
 		GameController.getInstance().getSoundHandler().playMusic(MusicType.MENU_MUSIC);
 	}
 	
 	@Override
 	public void render(GameContainer gc, StateBasedGame sbg, Graphics g)
 			throws SlickException {
 		background.draw();
 		playgroundButton.draw();
 		hostButton.draw();
 		joinButton.draw();
 		exitButton.draw();
 		settingsButton.draw();
 	}
 	
 	@Override
 	public void update(GameContainer gc, StateBasedGame sbg, int delta) throws SlickException {				
		if(gc.getInput().isKeyPressed(Input.KEY_ESCAPE)){
 			gc.exit();
 		}
 		
 		if(playgroundButton.isClicked(gc.getInput()))
 			sbg.enterState(Tansk.PLAY);
 		else if(hostButton.isClicked(gc.getInput()))
 			sbg.enterState(Tansk.HOST);
 		else if(joinButton.isClicked(gc.getInput()))
 			sbg.enterState(Tansk.JOIN);
 		else if(settingsButton.isClicked(gc.getInput()))
 			sbg.enterState(Tansk.SETTINGS);
 		else if(exitButton.isClicked(gc.getInput()))
 			gc.exit();
 	}
 
 	@Override
 	public int getID() {
 		return this.state;
 	}
 
 }

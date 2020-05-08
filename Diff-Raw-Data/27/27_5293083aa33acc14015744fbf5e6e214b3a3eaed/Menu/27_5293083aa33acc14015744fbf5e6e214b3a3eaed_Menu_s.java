 package chalmers.TDA367.B17.states;
 
 import chalmers.TDA367.B17.view.MenuButton;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.SpriteSheet;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 import chalmers.TDA367.B17.Tansk;
 import chalmers.TDA367.B17.controller.GameController;
 import chalmers.TDA367.B17.sound.SoundHandler.MusicType;
 
 public class Menu extends BasicGameState{
 	
 
	private MenuButton playButton;
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
		playButton = new MenuButton(100, 125, GameController.getInstance().getImageHandler().getSprite("button_play"),
				GameController.getInstance().getImageHandler().getSprite("button_play_pressed"));
 		hostButton = new MenuButton(100, 225, GameController.getInstance().getImageHandler().getSprite("button_host"),
 				GameController.getInstance().getImageHandler().getSprite("button_host_pressed"));
 		joinButton = new MenuButton(100, 325, GameController.getInstance().getImageHandler().getSprite("button_join"),
 				GameController.getInstance().getImageHandler().getSprite("button_join_pressed"));
		exitButton = new MenuButton(100, 425, GameController.getInstance().getImageHandler().getSprite("button_exit"),
 				GameController.getInstance().getImageHandler().getSprite("button_exit_pressed"));

 		background = new SpriteSheet(GameController.getInstance().getImageHandler().getSprite("background"),
 				Tansk.SCREEN_WIDTH, Tansk.SCREEN_HEIGHT);
 		GameController.getInstance().getSoundHandler().playMusic(MusicType.MENU_MUSIC);
 	}
 	
 
 	@Override
 	public void render(GameContainer gc, StateBasedGame sbg, Graphics g)
 			throws SlickException {
 		background.draw(0,0);
		playButton.draw();
 		hostButton.draw();
 		joinButton.draw();
 		exitButton.draw();
 	}
 
 	@Override
 	public void update(GameContainer gc, StateBasedGame sbg, int delta)
 			throws SlickException {
		if(playButton.isClicked(gc.getInput()))
 			sbg.enterState(Tansk.PLAY);
 		else if(hostButton.isClicked(gc.getInput()))
 			sbg.enterState(Tansk.HOST);
 		else if(joinButton.isClicked(gc.getInput()))
 			sbg.enterState(Tansk.JOIN);
 		else if(exitButton.isClicked(gc.getInput()))
 			gc.exit();
 	}
 
 	@Override
 	public int getID() {
 		return this.state;
 	}
 
 }

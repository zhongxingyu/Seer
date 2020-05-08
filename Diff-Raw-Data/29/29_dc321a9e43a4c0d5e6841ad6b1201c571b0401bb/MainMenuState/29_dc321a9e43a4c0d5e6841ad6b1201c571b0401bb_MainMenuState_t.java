 package base;
 
 import gameObjects.BackgroundEffects;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 public class MainMenuState extends BasicGameState {
 
 	private final int stateID;
 	private final Camera camera = new Camera(0, 0);
 
 	private BackgroundEffects background = new BackgroundEffects();
 
 	public MainMenuState(int stateID) {
 		this.stateID = stateID;
 	}
 
 	@Override
 	public void init(GameContainer gc, StateBasedGame game)
 			throws SlickException {
 		// gc.setTargetFrameRate(30);
 
 	}
 
 	@Override
 	public void render(GameContainer container, StateBasedGame game, Graphics g)
 			throws SlickException {
 		background.render(camera, g);
 
 		g.setColor(Color.white);
 
 		g.drawString("Here, Fishy Fish Fish!", 400, 200);
 		g.drawString("Hit SPACE to play", 400, 230);
 	}
 
 	@Override
 	public void update(GameContainer container, StateBasedGame game, int delta)
 			throws SlickException {
 		Input input = container.getInput();
 
 		if (input.isKeyPressed(Input.KEY_SPACE) || input.isButton1Pressed(0)) {
 			game.enterState(FishGame.GAMEPLAY_STATE);
 		}
 
 		background.update(delta, 0);
 	}
 
 	@Override
 	public int getID() {
 		return stateID;
 	}
 
 	@Override
 	public void enter(GameContainer container, StateBasedGame game)
 			throws SlickException {
 		background = new BackgroundEffects();
		Input input = container.getInput();

		input.clearKeyPressedRecord();
		input.clearControlPressedRecord();
 	}
 
 }

 package base;
 
 import gameObjects.BackgroundEffects;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 import util.Assets;
 import util.AudioManager;
 
 public class GameplayState extends BasicGameState {
 
 	private final int stateID;
 
 	private final Camera camera = new Camera(0, 0);
 	private World world;
 
 	private BackgroundEffects background = new BackgroundEffects();
 
 	public static int score = 0;
 
 	// private int timeRan = 0;
 
 	// private final int timeLimit = 90 * 1000;
 
 	public GameplayState(int stateID) throws SlickException {
 		this.stateID = stateID;
 	}
 
 	@Override
 	public void init(GameContainer gc, StateBasedGame game)
 			throws SlickException {
 		new Assets();
 		AudioManager.initAudioManager();
 		world = new World(this);
 	}
 
 	@Override
 	public void update(GameContainer container, StateBasedGame game, int delta)
 			throws SlickException {
 		// timeRan += delta;
 		//
 
 		if (world.playerHitFish()) {
 			game.enterState(FishGame.GAMEOVER_STATE);
 		}
 
 		background.update(delta, score);
 		world.update(container, delta);
 		camera.update(container, delta, score);
 
 		actOnInput(container, game);
 	}
 
 	private void actOnInput(GameContainer container, StateBasedGame game) {
 
 		Input input = container.getInput();
 
 		if (input.isKeyPressed(Input.KEY_R) || input.isButton1Pressed(0)) {
 			world.resetPlayer();
 		} else if (input.isKeyPressed(Input.KEY_ESCAPE)
 				|| input.isButton2Pressed(0)) {
 			game.enterState(FishGame.MAIN_MENU_STATE);
 		}
 
 	}
 
 	public void fishLandsInBucket() {
 
 		score++;
 		if (score % 2 == 1) {
 			world.addAIFish();
 		}
 		world.resetPlayer();
 	}
 
 	@Override
 	public void render(GameContainer container, StateBasedGame game, Graphics g)
 			throws SlickException {
 
 		background.render(camera, g);
 		world.render(camera, g);
 
 		renderUI(container, game, g);
 	}
 
 	private void renderUI(GameContainer container, StateBasedGame game,
 			Graphics g) {
 		g.setColor(Color.white);
 		g.drawString("Score: " + Integer.toString(score), 400, 10);
 
 		// int timeLeft = timeLimit - timeRan;
 		// int seconds = Math.max(timeLeft / 1000, 0);
 		// int milliseconds = Math.max(timeLeft % 1000, 0);
 		//
 		// g.drawString("Time left: " + seconds + "." + milliseconds, 400, 30);
 	}
 
 	@Override
 	public int getID() {
 		return stateID;
 	}
 
 	@Override
 	public void enter(GameContainer container, StateBasedGame game)
 			throws SlickException {
 		score = 0;
 		// timeRan = 0;
 		world.resetAll();
 		background = new BackgroundEffects();

		Input input = container.getInput();

		input.clearKeyPressedRecord();
		input.clearControlPressedRecord();
 	}
 }

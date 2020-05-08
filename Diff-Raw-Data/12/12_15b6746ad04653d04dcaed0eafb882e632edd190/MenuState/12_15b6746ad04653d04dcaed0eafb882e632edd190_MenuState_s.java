 package game;
 
 import java.util.ArrayList;
 
 import org.lwjgl.input.Mouse;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.geom.Vector2f;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 import org.newdawn.slick.state.transition.FadeOutTransition;
 
 import components.ImageRenderComponent;
 
 public class MenuState extends BasicGameState {
 
 	public static final int ID = 2;
 	private int centerHeight = Game.app.getHeight() / 2;
 	private int centerWidth = Game.app.getWidth() / 2;
 
 	private ArrayList<Entity> buttons;
 	private ArrayList<Image> buttonImages;
 
 	@Override
 	public void init(GameContainer gc, StateBasedGame sb) throws SlickException {
 		buttons = new ArrayList<Entity>();
 		buttonImages = new ArrayList<Image>();
 
		Entity newGameButton = new Entity("New Game");
		buttonImages.add(new Image("res/buttons/new_game.png"));
		newGameButton.AddComponent(new ImageRenderComponent("New Game Button",
 				buttonImages.get(0)));
		newGameButton.setPosition(new Vector2f(centerWidth - 100,
 				centerHeight - 125));
		buttons.add(newGameButton);
 
 		Entity settingsButton = new Entity("Settings");
 		buttonImages.add(new Image("res/buttons/settings.png"));
 		settingsButton.AddComponent(new ImageRenderComponent("Settings Button",
 				buttonImages.get(1)));
 		settingsButton
 				.setPosition(new Vector2f(centerWidth - 100, centerHeight));
 		buttons.add(settingsButton);
 
 		Entity exitButton = new Entity("Exit");
 		buttonImages.add(new Image("res/buttons/exit.png"));
 		exitButton.AddComponent(new ImageRenderComponent("Exit Button",
 				buttonImages.get(2)));
 		exitButton.setPosition(new Vector2f(centerWidth - 100,
 				centerHeight + 125));
 		buttons.add(exitButton);
 	}
 
 	@Override
 	public void render(GameContainer gc, StateBasedGame sb, Graphics g)
 			throws SlickException {
 
 		for (Entity e : buttons) {
 			e.render(gc, sb, g);
 		}
 
 	}
 
 	@Override
 	public void update(GameContainer gc, StateBasedGame sb, int delta)
 			throws SlickException {
 
 		Input input = gc.getInput();
 
 		int posX = Mouse.getX();
 		int posY = Math.abs(Mouse.getY() - Game.app.getHeight());
 
 		// Play
 		if (posX > buttons.get(0).getPosition().getX()
 				&& posX < buttons.get(0).getPosition().getX()
 						+ buttonImages.get(0).getWidth()
 				&& posY > buttons.get(0).getPosition().getY()
 				&& posY < buttons.get(0).getPosition().getY()
 						+ buttonImages.get(0).getHeight()) {
 			if (input.isMousePressed(0)) {
 				sb.enterState(InGameState.ID);
 			}
 		}
 
 		// Settings
 		if (posX > buttons.get(1).getPosition().getX()
 				&& posX < buttons.get(1).getPosition().getX()
 						+ buttonImages.get(1).getWidth()
 				&& posY > buttons.get(1).getPosition().getY()
 				&& posY < buttons.get(1).getPosition().getY()
 						+ buttonImages.get(1).getHeight()) {
 			if (input.isMousePressed(0)) {
 				sb.enterState(SettingsState.ID, new FadeOutTransition(
 						Color.black, 200), null);
 			}
 		}
 
 		// Exit
 		if (posX > buttons.get(2).getPosition().getX()
 				&& posX < buttons.get(2).getPosition().getX()
 						+ buttonImages.get(2).getWidth()
 				&& posY > buttons.get(2).getPosition().getY()
 				&& posY < buttons.get(2).getPosition().getY()
 						+ buttonImages.get(2).getHeight()) {
 			if (input.isMousePressed(0)) {
 				Game.stopMusic();
 				System.exit(0);
 			}
 		}
 
 	}
 
 	@Override
 	public int getID() {
 		return ID;
 	}
 
 }

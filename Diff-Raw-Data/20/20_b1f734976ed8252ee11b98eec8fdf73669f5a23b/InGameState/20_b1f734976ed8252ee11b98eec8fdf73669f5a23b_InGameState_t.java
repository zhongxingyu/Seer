 package game;
 
 import java.util.ArrayList;
 
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
 
 import components.*;
 
 public class InGameState extends BasicGameState {
 
 	public static final int ID = 1;
 	private ArrayList<Entity> entities;
 	private static ArrayList<Entity> shots;
 
 	@Override
 	public void init(GameContainer gc, StateBasedGame sb) throws SlickException {
 		entities = new ArrayList<Entity>();
 		shots = new ArrayList<Entity>();
 
 		Entity background = new Entity("background");
 		background.AddComponent(new ImageRenderComponent("BackgroundRender",
 				new Image("res/sprites/background.png")));
 		entities.add(background);
 
 		Entity player = new Entity("player");
 		player.AddComponent(new ImageRenderComponent("PlayerRender", new Image(
 				"res/sprites/Character.png")));
 		player.AddComponent(new PlayerMovementComponent("PlayerMovement"));
 		player.setPosition(new Vector2f(400, 300));
 		entities.add(player);
 
 	}
 
 	@Override
 	public void render(GameContainer gc, StateBasedGame sb, Graphics g)
 			throws SlickException {
 		for (Entity e : entities) {
 			e.render(gc, sb, g);
 		}
 		for (Entity e : shots) {
 			e.render(gc, sb, g);
 		}
 		
 
 	}
 
 	@Override
 	public void update(GameContainer gc, StateBasedGame sb, int delta)
 			throws SlickException {
 		for (Entity e : entities) {
 			e.update(gc, sb, delta);
 		}
		for(int i=0; i<shots.size(); i++){
			shots.get(i).update(gc, sb, delta);
			if(shots.get(i).getHealth() <= 0){
				shots.remove(i);
				i--;
			}
 		}
 
 		Input input = gc.getInput();
 		if (Controller.isShortcutPressed("Exit", input))
 			System.exit(0);
 		if (Controller.isShortcutPressed("Fullscreen", input))
 			Game.app.setFullscreen(!Game.app.isFullscreen());
 		if (Controller.isShortcutPressed("Menu", input)) {
 			sb.enterState(MenuState.ID, new FadeOutTransition(Color.black, 200), null);
 		}
 	}
 
 	@Override
 	public int getID() {
 		return ID;
 	}
 	
 	public static void addShot(float rotation, Vector2f position){
 		Entity newShot = new Entity("Shot");
 		try {
			ImageRenderComponent temp = new ImageRenderComponent("Shot Image", new Image("res/sprites/shot.png"));
			newShot.AddComponent(temp);
			newShot.setRadius(temp.getRadius());
 		} catch (SlickException e) {
 			System.err.println("Couldn't load shot image.");
 		}
 		newShot.AddComponent(new ShotComponent("Shot", rotation, position));
 		newShot.setHealth(1);
 		shots.add(newShot);
 	}
 
 }

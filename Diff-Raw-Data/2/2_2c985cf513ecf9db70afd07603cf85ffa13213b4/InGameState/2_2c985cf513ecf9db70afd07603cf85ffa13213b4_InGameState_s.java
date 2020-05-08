 package game;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.geom.Vector2f;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 import org.newdawn.slick.state.transition.FadeInTransition;
 import org.newdawn.slick.state.transition.FadeOutTransition;
 
 import components.*;
 
 public class InGameState extends BasicGameState {
 
 	public static final int ID = 1;
 	private ArrayList<Entity> entities;
 	private static ArrayList<Entity> shots;
 	private ArrayList<Entity> enemies;
 
 	@Override
 	public void init(GameContainer gc, StateBasedGame sb) throws SlickException {
 		entities = new ArrayList<Entity>();
 		shots = new ArrayList<Entity>();
 		enemies = new ArrayList<Entity>();
 
 		//Add a background
 		Entity background = new Entity("background");
 		background.AddComponent(new ImageRenderComponent("BackgroundRender",
 				new Image("res/sprites/background.png")));
 		background.setHealth(1);
 		entities.add(background);
 		
 		//add a base
 		Entity base = new Entity("base");
 		ImageRenderComponent temp = new ImageRenderComponent("house", new Image("res/sprites/House.png"));
 		//AnimationRenderComponent temp2 = new AnimationRenderComponent("house", new Image[] {new Image("res/sprites/House.png")}, 10);
 		base.AddComponent(temp);
 		base.setRadius(temp.getRadius());
 		base.setPosition(new Vector2f(Game.centerWidth, Game.centerHeight));
 		base.setHealth(100);
 		base.AddComponent(new HealthBarComponent("BaseHealthBar"));
 		entities.add(base);
 
 
 		//Add a player
 		Entity player = new Entity("player");
		AnimationRenderComponent temp2 = new AnimationRenderComponent("playerRender", new Image[] {new Image("res/sprites/hero/hero2.png")}, 10);
 		//ImageRenderComponent temp = new ImageRenderComponent("PlayerRender", new Image("res/sprites/hero/hero1.png"));
 		player.AddComponent(temp2);
 		player.setRadius(temp2.getRadius());
 		player.AddComponent(new PlayerMovementComponent("PlayerMovement"));
 		player.setPosition(new Vector2f(400, 300));
 		player.setHealth(100);
 		player.AddComponent(new HealthBarComponent("PlayerHealthBar"));
 		entities.add(player);
 		
 		//Adds a wave of enemies. TODO make better!
 		Random random = new Random();
 		for(int i=0; i<10; i++){
 			Entity enemy = new Entity("enemy");
 			temp = new ImageRenderComponent("EnemyRender", new Image("res/sprites/enemy.png"));
 			enemy.AddComponent(temp);
 			enemy.setRadius(temp.getRadius());
 			enemy.AddComponent(new EnemyMovementComponent("EnemyMovement"));
 			enemy.setPosition(new Vector2f(random.nextInt(1920), random.nextInt(1080)));
 			enemy.setHealth(10);
 			enemy.AddComponent(new HealthBarComponent("EnemyHealthBar"));
 			enemies.add(enemy);
 		}
 
 	}
 
 	@Override
 	public void render(GameContainer gc, StateBasedGame sb, Graphics g)
 			throws SlickException {
 		for (Entity e : entities) {
 			e.render(gc, sb, g);
 		}
 		for (Entity e : enemies){
 			e.render(gc, sb, g);
 		}
 		for (Entity e : shots) {
 			e.render(gc, sb, g);
 		}	
 	}
 
 	@Override
 	public void update(GameContainer gc, StateBasedGame sb, int delta) throws SlickException {
 		
 		updateEntityArray(entities, gc, sb, delta);
 		updateEntityArray(shots, gc, sb, delta);
 		updateEntityArray(enemies, gc, sb, delta);
 		
 		for(Entity e1 : shots){
 			for(Entity e2 : enemies){
 				if(collision(e1, e2)){
 					e2.damage(2);
 					e1.setHealth(0);
 				}
 			}
 		}
 
 		Input input = gc.getInput();
 		if (Controller.isShortcutPressed("Exit", input))
 			System.exit(0);
 		if (Controller.isShortcutPressed("Fullscreen", input))
 			Game.app.setFullscreen(!Game.app.isFullscreen());
 		if (Controller.isShortcutPressed("Menu", input)) {
 			sb.enterState(MenuState.ID, new FadeOutTransition(Color.black, 200), new FadeInTransition(Color.black,
 					200));
 		}
 	}
 	
 	private boolean collision(Entity e1, Entity e2){
 		float radii = e1.getRadius() + e2.getRadius();
 		float dx = e2.getPosition().x - e1.getPosition().x + radii;
 		float dy = e2.getPosition().y - e1.getPosition().y + radii;
 		if( dx * dx + dy * dy < radii * radii){
 			return true;
 		}
 		return false;
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
 	
 	/**
 	 * Loops through the given array and updates every entity. If the entity's health reaches 0 or less it will be removed.
 	 */
 	private void updateEntityArray(ArrayList<Entity> array, GameContainer gc, StateBasedGame sb, int delta){
 		for(int i=0; i<array.size(); i++){
 			array.get(i).update(gc, sb, delta);
 			if(array.get(i).getHealth() <= 0){
 				array.remove(i);
 				i--;
 			}
 		}
 	}
 
 	@Override
 	public int getID() {
 		return ID;
 	}
 	
 }

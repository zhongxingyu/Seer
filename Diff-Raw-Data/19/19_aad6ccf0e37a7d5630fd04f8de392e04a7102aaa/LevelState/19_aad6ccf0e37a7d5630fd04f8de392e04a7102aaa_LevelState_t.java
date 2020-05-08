 package states;
 
 import input.Controls;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import main.MainGame;
 import map.Map;
 
 import org.jbox2d.callbacks.DebugDraw;
 import org.jbox2d.common.Vec2;
 import org.jbox2d.dynamics.World;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 import ui.Cursor;
 import util.Box2DDebugDraw;
 import camera.Camera;
 
 import combat.CombatContact;
 
 import config.Config;
 
 import entities.Player;
 import entities.enemies.Enemy;
 
 public class LevelState extends BasicGameState{
 	private int id;
 	private String mapString;
 	private Map map;
 	private Vec2 gravity;
 	private World world;
 	private Player player;
 	private ArrayList<Enemy> enemies;
 	private Box2DDebugDraw debugdraw;
 	private boolean viewDebug = false;
 	private static Camera camera;
 	private Cursor cursor;
 	
 	public LevelState(String mapString, int id) {
 		super();
 		this.id = id;
 		this.mapString = mapString;
 	}
 		
 	@Override
 	public void init(GameContainer gc, StateBasedGame game)
 			throws SlickException {
 		Controls.setGC(gc);
 		
 		gravity = new Vec2(0,Config.GRAVITY);
 		world = new World(gravity);
 		
 		world.setContactListener(new CombatContact());
 		
 		map = new Map(mapString, world);
 		map.parseMapObjects();
 		
 		debugdraw = new Box2DDebugDraw();
 		debugdraw.setFlags(DebugDraw.e_shapeBit);
 		world.setDebugDraw(debugdraw);
 				
 		player = MainGame.player;
 		player.addToWorld(world);
		player.init(map.getPlayerLoc().x, map.getPlayerLoc().y);		
 		
 		enemies = map.getEnemies();
 		for (int i = 0; i < enemies.size(); i++) {
 			enemies.get(i).addToWorld(world);
 		}
 		
 		camera = new Camera(gc, map.getTiledMap());
 		cursor = new Cursor(player);
 	}
 
 	@Override
 	public void render(GameContainer gc, StateBasedGame game, Graphics graphics)
 			throws SlickException {
 		camera.drawMap();
 		camera.translateGraphics();
 		if (viewDebug) {
 			debugdraw.setGraphics(graphics);
 			world.drawDebugData();
 		}
 		player.render(graphics);
 		for (Enemy e : enemies) {
 			e.render(graphics);			
 		}
 		
 		cursor.render(graphics);
 		
 	}
 
 	@Override
 	public void update(GameContainer gc, StateBasedGame game, int delta)
 			throws SlickException {
 		if (gc.getInput().isKeyPressed(Input.KEY_ESCAPE)) init(gc, game);
 		if (player.getHp() <= 0) init(gc, game);
 		
 		world.step(delta/1000f, Config.VELOCITY_ITERATIONS, Config.POSITION_ITERATIONS);
 		player.update(gc, delta);
 		
 		for (Iterator<Enemy> it = enemies.iterator(); it.hasNext(); ) {
 			Enemy e = it.next();
 			if (e.getHp() > 0) {
 				e.update(gc, delta);
 			} else {
 				it.remove();
 				e.kill();
 			}
 		}
 
 		if (gc.getInput().isKeyPressed(Input.KEY_F3)) viewDebug = !viewDebug;
 		camera.centerOn(player.getX(),player.getY());
 		
 		cursor.update(gc, delta);
 	}
 
 	@Override
 	public int getID() {
 		return id;
 	}
 	
 	/**
 	 * @return the camera
 	 */
 	public static Camera getCamera() {
 		return camera;
 	}
 
 }

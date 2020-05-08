 package states;
 
 import input.Controls;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import main.MainGame;
 import map.Map;
 
 import org.jbox2d.callbacks.DebugDraw;
 import org.jbox2d.common.Vec2;
 import org.jbox2d.dynamics.World;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 import ui.Cursor;
 import ui.Timer;
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
 	private Vec2 goalLoc;
 	private String backgroundString;
 	private Image background;
 	private Timer timer;
 	
 	public LevelState(String mapString, String backgroundString, int id) {
 		super();
 		this.id = id;
 		this.mapString = mapString;
 		this.backgroundString = backgroundString;
 	}
 		
 	@Override
 	public void init(GameContainer gc, StateBasedGame game)
 			throws SlickException {
 		Controls.setGC(gc);
 		
 		gravity = new Vec2(0,Config.GRAVITY);
 		world = new World(gravity);
 		
 		world.setContactListener(new CombatContact());
 		
 		background = new Image(backgroundString);
 		
 		map = new Map(mapString, world);
 		map.parseMapObjects();
 		
 		background = background.getScaledCopy((float) map.getHeight()/ (float) background.getHeight());
 		
 		debugdraw = new Box2DDebugDraw();
 		debugdraw.setFlags(DebugDraw.e_shapeBit);
 		world.setDebugDraw(debugdraw);
 				
 		player = MainGame.player;
 		player.addToWorld(world);
 		player.reset();
 		player.setPosition(map.getPlayerLoc().x, map.getPlayerLoc().y);
 		
 		enemies = map.getEnemies();
 		for (int i = 0; i < enemies.size(); i++) {
 			enemies.get(i).addToWorld(world);
 		}
 		
 		goalLoc = map.getGoalLoc();
 		
 		camera = new Camera(gc, map.getTiledMap());
 		cursor = new Cursor(player);
 		timer = new Timer();
 	}
 
 	@Override
 	public void render(GameContainer gc, StateBasedGame game, Graphics graphics)
 			throws SlickException {
 		camera.translateGraphics();
 		drawBackground(graphics);
 		camera.untranslateGraphics();
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
 		
 		// timer draw
 		graphics.setColor(Color.white);
		graphics.drawString(timer.getTime(), camera.getPosition().getMinX()+100, camera.getPosition().getMinY()+100);
 	}
 
 	@Override
 	public void update(GameContainer gc, StateBasedGame game, int delta)
 			throws SlickException {
 		if (gc.getInput().isKeyPressed(Input.KEY_ESCAPE)) init(gc, game);
 		if (player.getHp() <= 0) init(gc, game);
 		if (Math.abs(player.getX()-goalLoc.x) < 30 && Math.abs(player.getY() - goalLoc.y) < 30) init(gc,game);
 		
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
 		timer.update(delta);
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
 	
 	// kinda janky, remove when paralaxing set up
 	private void drawBackground(Graphics graphics) {
 		int backgroundX = 0;
 		while (backgroundX < map.getWidth()){
 			graphics.drawImage(background,  backgroundX,  0);
 			backgroundX += background.getWidth();
 		}
 	}
 
 }

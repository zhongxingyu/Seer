 package states;
 
 import input.Controls;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Queue;
 
 import main.MainGame;
 import map.Map;
 
 import org.jbox2d.callbacks.DebugDraw;
 import org.jbox2d.common.Vec2;
 import org.jbox2d.dynamics.World;
 import org.newdawn.slick.AppGameContainer;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.Music;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 import ui.Cursor;
 import ui.DebugInfo;
 import ui.PauseScreen;
 import ui.Time;
 import ui.Timer;
 import ui.Transitions;
 import util.Box2DDebugDraw;
 import camera.Camera;
 
 import combat.CombatContact;
 
 import config.Config;
 import config.Section;
 import entities.Mushroom;
 import entities.Player;
 import entities.Salmon;
 import entities.enemies.Enemy;
 
 public class LevelState extends BasicGameState{
 	public static Queue<Section> sectionQueue;
 	private Section section;
 	private Map map;
 	private Player player;
 	private ArrayList<Enemy> enemies;
 	private ArrayList<Salmon> salmons;
 	private static Box2DDebugDraw debugdraw;
 	private boolean viewDebug = false;
 	public static boolean godMode = false;
 	private static Camera camera;
 	private Cursor cursor;
 	private Vec2 goalLoc;
 	private Image background;
 	
 	private Time currentTime;
 	private Time lastTime;
 	private Time bestTime;
 	private static Timer timer;
 	private static DebugInfo info;
 	private static PauseScreen pauseScrn;
 	private ArrayList<Mushroom> mushrooms;
 	
 	private static Music forestLoop;
 	
 	static {
 		sectionQueue = new LinkedList<Section>();
 		debugdraw = new Box2DDebugDraw();
 		debugdraw.setFlags(DebugDraw.e_shapeBit | DebugDraw.e_jointBit | DebugDraw.e_centerOfMassBit);
 		timer = new Timer(100, 100);
 		info = new DebugInfo(Config.RESOLUTION_WIDTH - 500, 100);
 		pauseScrn = new PauseScreen();
 		try {
 			forestLoop = new Music("assets/sounds/Field19.wav");
 		} catch (SlickException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	public LevelState(Section section) {
 		super();
 		this.section = section;
 	}
 		
 	@Override
 	public void init(GameContainer gc, StateBasedGame game)
 			throws SlickException {
 		forestLoop.loop();
 	}
 
 	@Override
 	public void render(GameContainer gc, StateBasedGame game, Graphics graphics)
 			throws SlickException {
 		camera.translateGraphics(gc);
		drawBackground(graphics, gc, game);
 		camera.untranslateGraphics(gc);
 		camera.drawMap();
 		timer.updateTime(currentTime, lastTime, bestTime);
 		timer.render(graphics);
 		camera.translateGraphics(gc);
 		
 		if (viewDebug) {
 			debugdraw.setGraphics(graphics);
 			map.getWorld().drawDebugData();
 			camera.untranslateGraphics(gc);
 			info.render(graphics);
 			camera.translateGraphics(gc);
 		} else {
 			player.render(graphics);
 			for (Enemy e : enemies) {
 				e.render(graphics);
 			}
 			for (Salmon s : salmons) {
 				s.render(graphics);
 			}
 			/*
 			for (Mushroom m : mushrooms) {
 				m.render(graphics);
 			}
 			*/
 		}
 		cursor.render(graphics);
 		
 		// so that transitions render correctly
 		camera.untranslateGraphics(gc);
 		
 		if (gc.isPaused()) pauseScrn.render(graphics);
 	}
 
 	@Override
 	public void update(GameContainer gc, StateBasedGame game, int delta)
 			throws SlickException {
 		// check toggles
 		if (gc.getInput().isKeyPressed(Input.KEY_F3)) viewDebug = !viewDebug;
 		if (gc.getInput().isKeyPressed(Input.KEY_F4)) godMode = !godMode;
 		if (gc.getInput().isKeyPressed(Input.KEY_F11)) MainGame.setFullscreen((AppGameContainer) gc, !gc.isFullscreen());
 		
 		if (gc.isPaused()) {
 			pauseScrn.update(gc, delta);
 			return;
 		}
 		// this goes second to avoid calling isKeyPressed() more than once
 		if (!gc.hasFocus() || gc.getInput().isKeyPressed(Input.KEY_ESCAPE)) pause(gc);
 		
 		if (gc.getInput().isKeyPressed(Input.KEY_F5)) {
 			nextLevel(game);
 		}
 		
 		// if the goal is reached
 		if (closeToGoal()) {
 			if (lastTime == null) lastTime = new Time();
 			lastTime.set(currentTime.getMillis());
 			if (bestTime == null) bestTime = new Time(currentTime.getMillis());
 			else if (lastTime.getMillis() < bestTime.getMillis()) {
 				bestTime.set(currentTime.getMillis());
 			}
 			nextLevel(game);
 		}
 		
 		// update world
 		map.getWorld().step(delta/1000f, Config.VELOCITY_ITERATIONS, Config.POSITION_ITERATIONS);
 		
 		// update player
 		player.update(gc, delta);
 		
 		// update enemies
 		for (Iterator<Enemy> it = enemies.iterator(); it.hasNext(); ) {
 			Enemy e = it.next();
 			if (e.getHp() > 0) {
 				e.update(gc, delta);
 			} else {
 				it.remove();
 				e.kill();
 			}
 		}
 		
 		for (Salmon s : salmons) {
 			s.update(gc, delta);
 		}
 		for (Mushroom m : mushrooms) {
 			m.update(gc, delta);
 		}
 
 		// check toggles
 		if (gc.getInput().isKeyPressed(Input.KEY_F3)) viewDebug = !viewDebug;
 		if (gc.getInput().isKeyPressed(Input.KEY_F4)) godMode = !godMode;
 		if (gc.getInput().isKeyPressed(Input.KEY_F11)) gc.setFullscreen(!gc.isFullscreen());
 		
 		camera.centerOn(player.getX(),player.getY());
 		cursor.update(gc, delta);
 		currentTime.update(delta);
 	}
 
 	@Override
 	public void enter(GameContainer gc, StateBasedGame game)
 			throws SlickException {
 		super.enter(gc, game);
 		Controls.setGC(gc);
 		
 		map = new Map(section.getMapPath(), section.getGravity());
 		map.parseMapObjects();
 		map.getWorld().setContactListener(new CombatContact());
 		map.getWorld().setDebugDraw(debugdraw);
 		
 		background = new Image(section.getBackgroundPath());
		background = background.getScaledCopy((float) (map.getHeight() > gc.getHeight() ? map.getHeight() : gc.getHeight())/ (float) background.getHeight());
 
 		player = MainGame.player;
 		player.addToWorld(map.getWorld(), map.getPlayerLoc().x, map.getPlayerLoc().y 
 				+ (Config.TILE_HEIGHT / 2) - (Config.PLAYER_HEIGHT / 2)); // move up to avoid getting stuck in the ground
 		player.reset();
 		
 		currentTime = new Time();
 		
 		goalLoc = map.getGoalLoc();
 		camera = new Camera(gc, map.getTiledMap());
 		cursor = new Cursor(player);
 
 		
 		currentTime = new Time();
 		
 		enemies = map.getEnemies();
 		salmons = map.getSalmons();
 		mushrooms = map.getMushrooms();
 		World world = map.getWorld();
 		for (Enemy e : enemies) {
 			e.addToWorld(world, e.getX(), e.getY());
 		}
 		for (Salmon s : salmons) {
 			s.addToWorld(world, s.getX(), s.getY(), currentTime);
 		}
 		
 		for (Mushroom m : mushrooms) {
 			m.addToWorld(world, m.getX(), m.getY());
 		}
 	}
 
 	private void pause(GameContainer gc) {
 		gc.setPaused(true);
 		gc.setTargetFrameRate(Config.INACTIVE_FRAME_RATE);
 	}
 
 	private boolean closeToGoal() {
 		return Math.abs(player.getCenterX()-goalLoc.x) < 30 && Math.abs(player.getCenterY() - goalLoc.y) < 30;
 	}
 	
 	private void nextLevel(StateBasedGame game) {
 		if (sectionQueue.isEmpty()) game.enterState(IntroState.ID, Transitions.fadeOut(), Transitions.fadeIn());
 		else game.enterState(LevelState.sectionQueue.poll().getID(), Transitions.fadeOut(), Transitions.fadeIn());
 	}
 	
 	// kinda janky, remove when paralaxing set up
	private void drawBackground(Graphics graphics, GameContainer gc, StateBasedGame game) {
		int backgroundX = -gc.getWidth();
 		while (backgroundX < map.getWidth()){
			graphics.drawImage(background,  backgroundX,  map.getHeight() > gc.getHeight() ? 0 : map.getHeight() - gc.getHeight());
 			backgroundX += background.getWidth();
 		}
 	}
 	
 	@Override
 	public int getID() {
 		return section.getID();
 	}
 	
 	public static Camera getCamera() {
 		return camera;
 	}
 
 }

 package pl.pepuch.wildjoe.core.world;
 
 import static playn.core.PlayN.graphics;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.jbox2d.callbacks.DebugDraw;
 import org.jbox2d.common.Vec2;
 import org.jbox2d.dynamics.World;
 
 import pl.pepuch.wildjoe.controller.Background;
 import pl.pepuch.wildjoe.controller.DynamicActor;
 import pl.pepuch.wildjoe.controller.GameOver;
 import pl.pepuch.wildjoe.controller.Player;
 import pl.pepuch.wildjoe.controller.Scoreboard;
 import pl.pepuch.wildjoe.core.WildJoe;
 import playn.core.CanvasImage;
 import playn.core.DebugDrawBox2D;
 import playn.core.ImageLayer;
 import playn.core.PlayN;
 
 public class GameWorld {
 	
 	// pozwala debugowac Box2d
 	public DebugDrawBox2D debugDraw;
 	// swiat Box2d
 	private World world;
 	// lista dodanych cial
 	List<DynamicActor> gameBodyList;
 	
 	// pozycja areny
 	private Vec2 arenaPosition;
 	
 	private Background background;
 	private float worldWidth;
 	public WildJoe game;
 	private Player player;
 	private Scoreboard scoreboard;
 	public GameOver gameOver;
 
 	public GameWorld(WildJoe game) {
 		gameOver = null;
 		this.game = game;
 		gameBodyList = new ArrayList<DynamicActor>();
 		Vec2 gravity = new Vec2(0.0f, 10.0f);
 		boolean doSleep = true;
 	    world = new World(gravity, doSleep);
 		if (WildJoe.debug) {
 	    	this.enableDebug();
 	    }
 
 		// tablica wynikow
 		scoreboard = new Scoreboard();
 		PlayN.graphics().rootLayer().addAt(scoreboard.view().layer(), 0, 0);
 		scoreboard.setVisible(false);
 		// zawodnik
 		player = new Player(this, new Vec2(1.0f, 0.0f));
 		PlayN.graphics().rootLayer().add(player.view().layer());
 		player.setVisible(false);
 		// background
 		background = new Background(this, new Vec2(0.0f, 0.0f));
         PlayN.graphics().rootLayer().add(background.view().layer());
         player.setVisible(false);
 		// arena initial position
 		setArenaPosition(new Vec2(0.0f, 0.0f));
 	}
 	
 	public void init() {
 		player.setVisible(true);
         background.setVisible(true);
 		scoreboard.setVisible(true);
 		// set world contact listener
 		world.setContactListener(new WorldContactListener(this));
 		// reset world width
 		worldWidth = 0.0f;
 	}
 	
 	public void paint(float alpha) {
 		
 		if (WildJoe.debug) {
 			debugDraw.getCanvas().clear();
 			world.drawDebugData();
 		}
 		
 		// paint blocks
 		for (Iterator<DynamicActor> iterator = gameBodyList.iterator(); iterator.hasNext();) {
 			DynamicActor body = (DynamicActor)iterator.next();
 		    body.paint(alpha);
 		}
 		
 		scoreboard.paint(alpha);
 		player.paint(alpha);
 	}
 	
 	public void update(float delta) {
 		world.step(0.033f, 10, 10);
 		
 		float leftEnd = getScreenWidth()*0.33f-player.model().width();
 		float rightEnd = getScreenWidth()*0.66f;
 		if (player.model().position().x<leftEnd && player.isMovingLeft()) {
 			background.moveLeft();
 			for (Iterator<DynamicActor> actor = gameBodyList.iterator(); actor.hasNext();) {
 				DynamicActor body = actor.next();
 				float x = body.model().position().x+player.model().speed();
 				float y = body.model().position().y;
 				body.model().setPosition(new Vec2(x, y));
 			}
 		}
 		else if (player.model().position().x>rightEnd && player.isMovingRight()) {
 			background.moveRight();
 			for (Iterator<DynamicActor> actor = gameBodyList.iterator(); actor.hasNext();) {
 				DynamicActor body = actor.next();
 				float x = body.model().position().x-player.model().speed();
 				float y = body.model().position().y;
 				body.model().setPosition(new Vec2(x, y));
 			}
 		}
 		else {
 			if (player.isMovingLeft()) {
 				player.moveLeft();
 			}
 			if (player.isMovingRight()) {
 				player.moveRight();
 			}
 		}
 		
 		// update bodies
 		for (Iterator<DynamicActor> actor = gameBodyList.iterator(); actor.hasNext();) {
 			DynamicActor body = actor.next();
 			body.update(delta);
 		}
 		player.update(delta);
 		scoreboard.update(delta);
 	}
 	
 	private void enableDebug() {
 		CanvasImage image = graphics().createImage((int) (getScreenWidth() / WildJoe.physUnitPerScreenUnit), 
 				(int) (getScreenHeight() / WildJoe.physUnitPerScreenUnit));
 		ImageLayer layer = graphics().createImageLayer(image);
 		layer.setDepth(10);
 		PlayN.graphics().rootLayer().add(layer);
 		debugDraw = new DebugDrawBox2D();
 		debugDraw.setCanvas(image);
 		debugDraw.setFlipY(false);
 		debugDraw.setStrokeAlpha(150);
 		debugDraw.setFillAlpha(75);
 		debugDraw.setStrokeWidth(2.0f);
 		debugDraw.setFlags(DebugDraw.e_shapeBit | DebugDraw.e_jointBit | DebugDraw.e_aabbBit);
 		debugDraw.setCamera(0, 0, 1f / WildJoe.physUnitPerScreenUnit);
 		world.setDebugDraw(debugDraw);
 	}
 	
 	public void add(final DynamicActor gameBody) {
 		gameBodyList.add(gameBody);
 		PlayN.graphics().rootLayer().add(gameBody.view().layer());
 	}
 	
 	public void remove(final DynamicActor gameBody) {
		gameBody.model().body().setActive(false); // inactive body will not collide
 		PlayN.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 				gameBodyList.remove(gameBody);
 				gameBody.destroy();
 			}
 		});
 	}
 	
 	public void setArenaPosition(Vec2 arenaPosition) {
 		this.arenaPosition = arenaPosition;
 	}
 	
 	public float getArenaPositionX() {
 		return arenaPosition.x+2.0f; // TODO +2.0f powinno byc z automatu a nie z reki wpisane!
 	}
 	
 	public float getScreenWidth() {
 		return graphics().width()*WildJoe.physUnitPerScreenUnit;
 	}
 	
 	public float getScreenHeight() {
 		return graphics().height()*WildJoe.physUnitPerScreenUnit;
 	}
 	
 	public float getWorldHeight() {
 		return getScreenHeight();
 	}
 	
 	public float getWorldWidth() {
 		if (worldWidth==0.0f) {
 			for (Iterator<DynamicActor> iterator = gameBodyList.iterator(); iterator.hasNext();) {
 				DynamicActor body = (DynamicActor)iterator.next();
 				if (worldWidth < body.model().position().x)
 					worldWidth = body.model().position().x;
 			}
 			worldWidth++; //zwiekszam szerokosc o 1, tzn o szerokosc jednego klocka
 		}
 		return worldWidth;
 	}
 	
 	public List<DynamicActor> getGameBodyList() {
 		return gameBodyList;
 	}
 	
 	public void gameOver() {
 		gameOver = new GameOver(this);
 		gameOver.show();
 		PlayN.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 				destroy();
 			}
 		});
 	}
 	
 	// call befere next level
 	public void clear() {
 		player.setVisible(false);
 		background.destroy();
 		background = new Background(this, new Vec2(0.0f, 0.0f));
 		
 		PlayN.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 		        PlayN.graphics().rootLayer().add(background.view().layer());
 				background.setVisible(false);
 				scoreboard.setVisible(false);
 				for (Iterator<DynamicActor> actor = gameBodyList.iterator(); actor.hasNext();) {
 					DynamicActor body = actor.next();
 					body.destroy();
 				}
 				gameBodyList.clear();
 			}
 		});
 	}
 	
 	public void destroy() {
 		player.die();
 		player = null;
 		scoreboard.destroy();
 		scoreboard = null;
 		background.destroy();
 		background = null;
 		
 		for (Iterator<DynamicActor> actor = gameBodyList.iterator(); actor.hasNext();) {
 			DynamicActor body = actor.next();
 			body.destroy();
 		}
 		
 		gameBodyList.clear();
 		world.setContactListener(null);
 		arenaPosition = null;
 		world = null;
 	}
 	
 	public Player player() {
 		return player;
 	}
 	
 	public Scoreboard scoreboard() {
 		return scoreboard;
 	}
 	
 	public World world() {
 	    return world;
 	}
 	
 	public WildJoe game() {
 	    return game;
 	}
 	
 }

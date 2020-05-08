 package com.amphibian.ffz.engine;
 
 import io.socket.SocketIO;
 
 import java.io.InputStreamReader;
 import java.lang.reflect.Type;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.UUID;
 
 import org.apache.commons.configuration.ConfigurationException;
 import org.apache.commons.configuration.XMLConfiguration;
 import org.json.JSONObject;
 
 import android.os.SystemClock;
 import android.util.Log;
 
 import com.amphibian.ffz.App;
 import com.amphibian.ffz.FrogSocketMessageHandler;
 import com.amphibian.ffz.R;
 import com.amphibian.ffz.data.ObstacleDeserializer;
 import com.amphibian.ffz.engine.layers.Ground;
 import com.amphibian.ffz.engine.layers.InfoLayer;
 import com.amphibian.ffz.engine.layers.SpriteLayer;
 import com.amphibian.ffz.engine.layers.Water;
 import com.amphibian.ffz.engine.sprite.FrameDataManager;
 import com.amphibian.ffz.engine.sprite.Frog;
 import com.amphibian.ffz.engine.sprite.Obstacle;
 import com.amphibian.ffz.engine.sprite.Rabbit;
 import com.amphibian.ffz.engine.sprite.Sprite;
 import com.amphibian.ffz.engine.world.Area;
 import com.amphibian.ffz.engine.world.Tile;
 import com.amphibian.ffz.geometry.ConvexPolygon;
 import com.amphibian.ffz.input.InputSource;
 import com.amphibian.ffz.opengl.StandardProgram;
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.google.gson.reflect.TypeToken;
 
 public class Engine {
 
 	private long lastUpdate;
 	
 	private Viewport viewport;
 	
 
 	private InputSource inputSource = null;
 
 	private MusicManager music;
 	
 	private Frog frog = null;
 	private Frog frog2 = null;
 	private Rabbit rabbit = null;
 	private Ground ground;
 	private InfoLayer infoLayer;
 	//private WaterLayer waterLayer;
 	private Water water;
 	
 	//private Obstacles obstacles;
 	private SpriteLayer spriteLayer;
 	
 	//private int fps = 0;
 	private long cycle = 0;
 	private long secondCounter = 0;
 	private int frameCounter = 0;
 	
 	private StandardProgram prog;
 	
 	private SpriteSorter spriteSorter;
 	
 	private SocketIO socket;
 	
 	//private ConvexPolygon testBlock;
 	private List<ConvexPolygon> blockers;
 	
 	private List<Sprite> sprites;
 	
 	private List<Sprite> newSprites;
 	private List<Sprite> remSprites;
 	
 	private String fid = UUID.randomUUID().toString();
 	
 	
 	float[] correction = new float[2];
 
 	private String newGround;
 	private String newObstacles;
 
 	public Engine() {
 		
 		try {
 			
 			XMLConfiguration config = new XMLConfiguration();
 			config.load(App.getContext().getResources().openRawResource(R.raw.config));
 			
 		} catch (ConfigurationException e) {
 			Log.e(App.name, "error loading configuration", e);
 		}
 		
 		music = new MusicManager();
 		
 		spriteSorter = new SpriteSorter();
 
 	}
 	
 	/**
 	 * not used right now
 	 */
 	public void connectWebsocket() {
 		
 		try {
 			socket = new SocketIO("http://www.amphibian.com:8080/");
 			socket.connect(new FrogSocketMessageHandler());
 			JSONObject jo = new JSONObject();
 			jo.put("fid", fid);
 			socket.emit("startup", jo);
 		} catch (Exception e) {
 			Log.e(App.name, "socket.io error", e);
 		}
 		
 	}
 	
 	public void setup() {
 		glSetup();
 		init();
 	}
 	
 	public void glSetup() {
 
 
 		spriteLayer = new SpriteLayer();
 
 		infoLayer = new InfoLayer();
 		
 		ground = new Ground();
 		
 		try {
 
 			Gson gson = new GsonBuilder()
 				.registerTypeAdapter(Obstacle.class, new ObstacleDeserializer())
 				.create();
 			
 
 			Tile[][] tiles = gson.fromJson(new InputStreamReader(App
 					.getContext().getResources().openRawResource(R.raw.area4)),
 					Tile[][].class);
 			//ground = new Ground(tiles);
 			water = new Water(tiles);
 
 		} catch (Exception e) {
 			Log.e(App.name, "json error", e);
 		}
 
 
 		prog = new StandardProgram();
 		prog.addTextures(ground.getTextures());
 		prog.addTextures(water.getTextures());
 		prog.addTextures(spriteLayer.getTextures());
 		prog.addTextures(infoLayer.getTextures());
 		prog.loadTextures();
 
 		
 	}
 	
 	public void init() {
 		
 
 		lastUpdate = SystemClock.elapsedRealtime();
 		cycle = SystemClock.elapsedRealtime();
 
 		frog = new Frog();
 		frog.faceRight();
 		frog.setEngine(this);
 
 		rabbit = new Rabbit();
 		
 		infoLayer.setFrog(frog);
 		
 		if (inputSource != null) {
 			frog.setInputSource(inputSource);
 		}
 		
 		blockers = new ArrayList<ConvexPolygon>();
 		sprites = new ArrayList<Sprite>(100);
 		newSprites = new ArrayList<Sprite>(50);
 		remSprites = new ArrayList<Sprite>(50);
 
 		try {
 
 			Gson gson = new GsonBuilder()
 				.registerTypeAdapter(Obstacle.class, new ObstacleDeserializer())
 				.create();
 			
 			Area a = gson.fromJson(new InputStreamReader(App.getContext()
 					.getResources().openRawResource(R.raw.test_area)),
 					Area.class);
 			
 			ground.setTiles(a.getGround());
 			music.playMusic(a.getMusicId());
 			sprites.addAll(a.getObstacles());
 			blockers.addAll(getBlockers(a.getObstacles()));
 			
 			//TODO: also get blockers from the environment (walls, etc.)
 			
 		} catch (Exception e) {
 			Log.e(App.name, "json error", e);
 		}
 
 
 		addSprite(frog);
 		addSprite(rabbit);
 		updateSprites();

		blockers.addAll(getBlockers());
		
 		
 	}
 	
 	/**
 	 * Unloads textures, clears arrays of vertex data, and stops music.
 	 */
 	public void cleanup() {
 		
 		prog.unloadTextures();
 		FrameDataManager.destroy();
 		
 		music.stopMusic();
 		
 	}
 	
 	public void resume() {
 		
 	}
 	
 	public void setInputSource(InputSource is) {
 		this.inputSource = is;
 	}
 	
 	public void setNewGround(String ng) {
 		this.newGround = ng;
 	}
 	
 	public void setNewObstacles(String no) {
 		this.newObstacles = no;
 	}
 	
 	private void resetArea(String groundJson) {
 		
 		try {
 			
 			Log.d(App.name, "resetting the ground");
 			
 			Gson gson = new GsonBuilder()
 				.registerTypeAdapter(Obstacle.class, new ObstacleDeserializer())
 				.create();
 
 			Tile[][] tiles = gson.fromJson(groundJson, Tile[][].class);
 			ground = new Ground(tiles);
 			water = new Water(tiles);
 			viewport.setAreaHeight(ground.getHeight());
 			viewport.setAreaWidth(ground.getWidth());
 			
 		} catch (Exception e) {
 			Log.e(App.name, "json error", e);
 		}
 
 	}
 	
 	private void resetObstacles(String obJson) {
 		
 		try {
 
 			Gson gson = new GsonBuilder()
 				.registerTypeAdapter(Obstacle.class, new ObstacleDeserializer())
 				.create();
 			
 			Type collectionType = new TypeToken<List<Obstacle>>(){}.getType();
 			List<Obstacle> obs = gson.fromJson(obJson, collectionType);
 			
 			//removeAllSprites();
 			addSprites(obs);
 
 			blockers = getBlockers(obs);
 
 			//addSprite(frog); // TODO: not sure why I put this here explicitly
 
 		} catch (Exception e) {
 			Log.e("ff", "json error", e);
 		}
 
 	}
 	
 	public void addSprite(Sprite s) {
 		this.newSprites.add(s);
 	}
 	
 	public void addSprites(Collection s) {
 		this.newSprites.addAll(s);
 	}
 	
 	public void removeSprite(Sprite s) {
 		this.remSprites.add(s);
 	}
 	
 	public void removeAllSprites() {
 		this.remSprites.addAll(sprites);
 	}
 	
 	private void updateSprites() {
 		if (newGround != null) {
 			resetArea(newGround);
 			newGround = null;
 		}
 		if (newObstacles != null) {
 			resetObstacles(newObstacles);
 			newObstacles = null;
 		}
 		if (remSprites.size() > 0) {
 			for (Sprite s : remSprites) {
 				this.sprites.remove(s);
 				this.blockers.removeAll(s.getBlockers());
 			}
 			//this.sprites.removeAll(remSprites);
 			this.remSprites.clear();
 		}
 		if (newSprites.size() > 0) {
 			for (Sprite s : newSprites) {
 				this.sprites.add(s);
 				this.blockers.addAll(s.getBlockers());
 			}
 			//this.sprites.addAll(newSprites);
 			this.newSprites.clear();
 			//blockers = getBlockers();
 		}
 	}
 	
 	public void createViewport(int height, int width) {
 		
 		viewport = new Viewport(height, width);
 		if (frog != null) {
 			viewport.setFollow(frog);
 		}
 		if (ground != null) {
 			viewport.setAreaHeight(ground.getHeight());
 			viewport.setAreaWidth(ground.getWidth());
 		}
 		
 	}
 	
 	public void update() {
 		
 		this.updateSprites();
 		
 		// how long has it been?
 		long now = SystemClock.elapsedRealtime();
 		long delta = now - lastUpdate;
 		lastUpdate = now;
 		
 //		if (input2 == null) {
 //			OuyaController oc2 = OuyaController.getControllerByPlayer(1);
 //			if (oc2 != null) {
 //				if (frog2 == null) {
 //					frog2 = new Frog();
 //					input2 = new OuyaInputSource(oc2);
 //					frog2.setInputSource(input2);
 //					addSprite(frog2);
 //				}
 //			}
 //		}
 		
 		
 		// move things that might move
 		for (Sprite s : sprites) {
 			s.update(delta);
 			if (s.remove()) {
 				this.removeSprite(s);
 			}
 		}
 		viewport.update(delta);
 		
 		
 		
 		// now check for collisions. we may have to back some things off
 		for (Sprite s: sprites) {
 			
 			if (s.checkMovement()) {
 				
 				List<ConvexPolygon> blist = s.getBlockers();
 				Iterator<ConvexPolygon> pi = blist.iterator();
 				while (pi.hasNext()) {
 					
 					ConvexPolygon poly = pi.next();
 					
 					//TODO: put the ground blockers in the same array as below...
 					float[] mtv = new float[3];//poly.intersectsWith(testBlock);
 					correction[0] = 0f;//mtv[0] * mtv[2];
 					correction[1] = 0f;//mtv[1] * mtv[2];//= new float[] { mtv[0] * mtv[2], mtv[1] * mtv[2] };
 					
 					for (int j = 0; j < blockers.size(); j++) {
 						ConvexPolygon cp = blockers.get(j);
 						mtv = poly.intersectsWith(cp);
 						if (mtv[0] != 0 || mtv[1] != 0) {
 							if (cp.getOwner() != null) {
 								s.hurt();
 								mtv[0] = 0f;
 								mtv[1] = 0f;
 							}
 						}
 						correction[0] += mtv[0] * mtv[2];
 						correction[1] += mtv[1] * mtv[2];
 					}

 					s.move(correction[0], correction[1]);
 
 				}
 				
 			}
 			
 		}
 		
 		
 		if (water.isWater(frog.getDrawX(), frog.getDrawY())) {
 			frog.hydrate(delta);
 			frog.setSwimming(true);
 		} else {
 			frog.setSwimming(false);
 		}
 		
 		
 		viewport.center();
 		
 
 		/*
 		JSONObject o = new JSONObject();
 		try {
 			o.put("id", fid);
 			o.put("imgId", "3");
 			o.put("inArea", "7");
 			JSONObject pos = new JSONObject();
 			pos.put("x", square.x);
 			pos.put("y", -square.y);
 			o.put("pos", pos);
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		socket.emit("frogmove", o);
 		*/
 		
 		// order the sprites so they draw correctly
 		Collections.sort(sprites, spriteSorter);
 		//Arrays.sort(sprites, spriteSorter);
 		
 		
 	}
 	
 	public void draw() {
 
 		long now = SystemClock.elapsedRealtime();
 		long delta = now - cycle;
 		cycle = now;
 		secondCounter += delta;
 		if (secondCounter > 5000) {
 			float se = ((float)secondCounter/1000f);
 			//Log.d("ffz", "frames = " + frameCounter);
 			//Log.d("ffz", "seconds elapsed = " + se);
 			Log.d(App.name, frameCounter / se + " FPS");
 			Log.d(App.name, "there are " + this.sprites.size() + " sprites");
 			secondCounter = 0;
 			frameCounter = 0;
 		}
 		
 		prog.enable();
 
 		ground.draw(prog, viewport);
 		
 		//waterLayer.draw(prog, viewport);
 		water.draw(prog, viewport);
 
 		spriteLayer.draw(sprites, prog, viewport);
 		
 		infoLayer.draw(prog, viewport);
 		
 		frameCounter++;
 		
 	}
 
    public List<ConvexPolygon> getBlockers() {
    	
    	List<ConvexPolygon> blockers = new ArrayList<ConvexPolygon>();
    	for (Sprite s : sprites) {
    		blockers.addAll(s.getBlockers());
    	}
    	return blockers;
    	
    }

     public List<ConvexPolygon> getBlockers(List<Obstacle> obs) {
     	
     	List<ConvexPolygon> blockers = new ArrayList<ConvexPolygon>();
     	for (Sprite s : obs) {
     		blockers.addAll(s.getBlockers());
     	}
     	return blockers;
     	
     }
 	
 	
 }

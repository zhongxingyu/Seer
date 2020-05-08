 package org.racenet.racesow;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.microedition.khronos.opengles.GL10;
 
 import org.racenet.framework.Camera2;
 import org.racenet.framework.GLGame;
 import org.racenet.framework.GameObject;
 import org.racenet.framework.TexturedBlock;
 import org.racenet.framework.SpatialHashGrid;
 import org.racenet.framework.TexturedShape;
 import org.racenet.framework.TexturedTriangle;
 import org.racenet.framework.Vector2;
 import org.racenet.framework.XMLParser;
 import org.racenet.racesow.threads.SubmitScoreThread;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.util.Log;
 
 public class Map {
 	
 	private GL10 gl;
 	private List<TexturedShape> ground = new ArrayList<TexturedShape>();
 	private List<TexturedShape> walls = new ArrayList<TexturedShape>();
 	public List<TexturedShape> items = new ArrayList<TexturedShape>();
 	public List<TexturedShape> pickedUpItems = new ArrayList<TexturedShape>();
 	private TexturedShape[] decals = new TexturedShape[MAX_DECALS];
 	private static final short MAX_DECALS = 64;
 	private float[] decalTime = new float[MAX_DECALS];
 	private TexturedBlock sky;
 	private float skyPosition;
 	private TexturedBlock background;
 	private float backgroundSpeed = 2;
 	private SpatialHashGrid groundGrid;
 	private SpatialHashGrid wallGrid;
 	private List<GameObject> funcs = new ArrayList<GameObject>();
 	public float playerX = 0;
 	public float playerY = 0;
 	private boolean raceStarted = false;
 	private boolean raceFinished = false;
 	private float startTime = 0;
 	private float stopTime = 0;
 	private boolean drawOutlines = false;
 	private Camera2 camera;
 	public String fileName;
 	private GLGame game;
 	
 	public Map(GL10 gl, Camera2 camera, boolean drawOutlines) {
 		
 		this.gl = gl;
 		this.camera = camera;
 		this.drawOutlines = drawOutlines;
 		
 		for (int i = 0; i < MAX_DECALS; i++) {
 			
 			this.decalTime[i] = -1;
 		}
 	}
 	
 	public boolean load(GLGame game, String fileName) {
 		
 		this.fileName = fileName;
 		this.game = game;
 		XMLParser parser = new XMLParser();
 		try {
 			
 			parser.read(game.getFileIO().readAsset("maps" + File.separator + fileName));
 			
 		} catch (IOException e) {
 			
 			try {
 				
 				parser.read(game.getFileIO().readFile("racesow" + File.separator + "maps" + File.separator + fileName));
 				
 			} catch (IOException e2) {
 				
 				return false;
 			}
 		}
 		
 		NodeList playern = parser.doc.getElementsByTagName("player");
 		if (playern.getLength() == 1) {
 			
 			Element player = (Element)playern.item(0);
 			try {
 				
 				playerX = Float.valueOf(parser.getValue(player, "x")).floatValue();
 				
 				playerY = Float.valueOf(parser.getValue(player, "y")).floatValue();
 			} catch (NumberFormatException e) {
 				
 				playerX = 100;
 				playerY = 10;
 			}
 		}
 		
 		float worldWidth = 0;
 		float worldHeight = 0;
 		
 		NodeList blocks = parser.doc.getElementsByTagName("block");
 		int numblocks = blocks.getLength();
 		for (int i = 0; i < numblocks; i++) {
 			
 			Element block = (Element)blocks.item(i);
 			float blockMaxX = Float.valueOf(parser.getValue(block, "x")).floatValue() + Float.valueOf(parser.getValue(block, "width")).floatValue();
 			if (worldWidth < blockMaxX) {
 				
 				worldWidth = blockMaxX;
 			}
 			
 			float blockMaxY = Float.valueOf(parser.getValue(block, "y")).floatValue() + Float.valueOf(parser.getValue(block, "height")).floatValue();
 			if (worldHeight < blockMaxY) {
 				
 				worldHeight = blockMaxY;
 			}
 		}
 		
 		this.groundGrid = new SpatialHashGrid(worldWidth, worldHeight, 30);
 		this.wallGrid = new SpatialHashGrid(worldWidth, worldHeight, 30);
 		
 		NodeList items = parser.doc.getElementsByTagName("item");
 		int numItems = items.getLength();
 		for (int i = 0; i < numItems; i++) {
 			
 			Element xmlItem = (Element)items.item(i);
 			float itemX = Float.valueOf(parser.getValue(xmlItem, "x")).floatValue();
 			float itemY = Float.valueOf(parser.getValue(xmlItem, "y")).floatValue();
 			//Integer.valueOf(parser.getValue(xmlItem, "ammo"));
 			
 			String type = parser.getValue(xmlItem, "type");
 			short func = GameObject.FUNC_NONE;
 			if (type.equals("rocket")) func = GameObject.ITEM_ROCKET; 
 			else if (type.equals("plasma")) func = GameObject.ITEM_PLASMA; 
 			
 			TexturedBlock item = new TexturedBlock(game,
 				"items/" + type + ".png",
 				func,
 				-1,
 				-1,
 				new Vector2(itemX, itemY),
 				new Vector2(itemX + 3, itemY + 3)
 			);
 
 			this.items.add(item);
 		}
 		
 		NodeList startTimerN = parser.doc.getElementsByTagName("starttimer");
 		if (startTimerN.getLength() == 1) {
 			
 			Element xmlStartTimer = (Element)startTimerN.item(0);
 			float startTimerX = Float.valueOf(parser.getValue(xmlStartTimer, "x")).floatValue();
 			//GameObject startTimer = new Func(Func.START_TIMER, startTimerX, 0, 1, worldHeight);
 			GameObject startTimer = new GameObject(new Vector2(startTimerX, 0), new Vector2(startTimerX + 1, 0), new Vector2(startTimerX + 1, worldHeight), new Vector2(startTimerX, worldHeight));
 			startTimer.func = GameObject.FUNC_START_TIMER;
 			this.funcs.add(startTimer);
 		}
 		
 		NodeList stopTimerN = parser.doc.getElementsByTagName("stoptimer");
 		if (stopTimerN.getLength() == 1) {
 			
 			Element xmlStopTimer = (Element)stopTimerN.item(0);
 			float stopTimerX = Float.valueOf(parser.getValue(xmlStopTimer, "x")).floatValue();
 			GameObject stopTimer = new GameObject(new Vector2(stopTimerX, 0), new Vector2(stopTimerX + 1, 0), new Vector2(stopTimerX + 1, worldHeight), new Vector2(stopTimerX, worldHeight));
 			stopTimer.func = GameObject.FUNC_STOP_TIMER;
 			this.funcs.add(stopTimer);
 		}
 		
 		NodeList skyN = parser.doc.getElementsByTagName("sky");
 		if (skyN.getLength() == 1) {
 			
 			try {
 				
 				this.skyPosition = Float.valueOf(parser.getValue((Element)skyN.item(0), "position")).floatValue();
 				
 			} catch (NumberFormatException e) {
 				
 				this.skyPosition = 0;
 			}
 			
 			this.sky = new TexturedBlock(game,
 					parser.getValue((Element)skyN.item(0), "texture"),
 					GameObject.FUNC_NONE,
 					-1,
 					-1,
 					new Vector2(0,skyPosition),
 					new Vector2(this.camera.frustumWidth, skyPosition)
 					);
 		}
 		
 		NodeList backgroundN = parser.doc.getElementsByTagName("background");
 		if (backgroundN.getLength() == 1) {
 			
 			try {
 				
 				this.backgroundSpeed = Float.valueOf(parser.getValue((Element)backgroundN.item(0), "speed")).floatValue();
 				
 			} catch (NumberFormatException e) {
 				
 				this.backgroundSpeed = 2;
 			}
 			
 			float backgroundPosition;
 			try {
 				
 				backgroundPosition = Float.valueOf(parser.getValue((Element)backgroundN.item(0), "position")).floatValue();
 			
 			} catch (NumberFormatException e) {
 				
 				backgroundPosition = 0;
 			}
 			
 			this.background = new TexturedBlock(game,
 					parser.getValue((Element)backgroundN.item(0), "texture"),
 					GameObject.FUNC_NONE,
 					0.25f,
 					0.25f,
 					new Vector2(backgroundPosition, 0),
 					new Vector2(worldWidth, 0),
 					new Vector2(worldWidth, worldHeight + backgroundPosition),
 					new Vector2(backgroundPosition, worldHeight + backgroundPosition)
 				);
 		}
 		
 		for (int i = 0; i < numblocks; i++) {
 			
 			Element xmlblock = (Element)blocks.item(i);
 			
 			short func;
 			try {
 				
 				func = Short.valueOf(parser.getValue(xmlblock, "func"));
 				
 			} catch (NumberFormatException e) {
 				
 				func = 0;
 			}
 			
 			float texSX;
 			try {
 				
 				texSX = Float.valueOf(parser.getValue(xmlblock, "texsx")).floatValue();
 						
 			} catch (NumberFormatException e) {
 				
 				texSX = 0;
 			}
 			
 			float texSY;
 			try {
 				
 				texSY = Float.valueOf(parser.getValue(xmlblock, "texsy")).floatValue();
 						
 			} catch (NumberFormatException e) {
 				
 				texSY = 0;
 			}
 			
 			float x = Float.valueOf(parser.getValue(xmlblock, "x")).floatValue();
 			float y = Float.valueOf(parser.getValue(xmlblock, "y")).floatValue();
 			float width = Float.valueOf(parser.getValue(xmlblock, "width")).floatValue();
 			float height = Float.valueOf(parser.getValue(xmlblock, "height")).floatValue();
 			
 			TexturedBlock block = new TexturedBlock(game,
 				parser.getValue(xmlblock, "texture"),
 				func,
 				texSX,
 				texSY,
 				new Vector2(x,y),
 				new Vector2(x + width, y),
 				new Vector2(x + width, y + height),
 				new Vector2(x, y + height)
 			);
 
 			String level = parser.getValue(xmlblock, "level");
 			if (level.equals("ground")) {
 				
 				this.addGround(block);
 				
 			} else if (level.equals("wall")) {
 				
 				this.addWall(block);
 			}
 		}
 
 		NodeList triangles = parser.doc.getElementsByTagName("tri");
 		int numTriangles = triangles.getLength();
 		for (int i = 0; i < numTriangles; i++) {
 			
 			Element xmlblock = (Element)triangles.item(i);
 			
 			short func;
 			try {
 				
 				func = Short.valueOf(parser.getValue(xmlblock, "func"));
 				
 			} catch (NumberFormatException e) {
 				
 				func = 0;
 			}
 			
 			float texSX;
 			try {
 				
 				texSX = Float.valueOf(parser.getValue(xmlblock, "texsx")).floatValue();
 						
 			} catch (NumberFormatException e) {
 				
 				texSX = 0;
 			}
 			
 			float texSY;
 			try {
 				
 				texSY = Float.valueOf(parser.getValue(xmlblock, "texsy")).floatValue();
 						
 			} catch (NumberFormatException e) {
 				
 				texSY = 0;
 			}
 			
 			float v1x = 0;
 			float v1y = 0;
 			float v2x = 0;
 			float v2y = 0;
 			float v3x = 0;
 			float v3y = 0;
 			
 			NodeList v1n = xmlblock.getElementsByTagName("v1");
 			if (v1n.getLength() == 1) {
 				
 				v1x = Float.valueOf(parser.getValue((Element)v1n.item(0), "x")).floatValue();
 				v1y = Float.valueOf(parser.getValue((Element)v1n.item(0), "y")).floatValue();
 			}
 			
 			NodeList v2n = xmlblock.getElementsByTagName("v2");
 			if (v2n.getLength() == 1) {
 				
 				v2x = Float.valueOf(parser.getValue((Element)v2n.item(0), "x")).floatValue();
 				v2y = Float.valueOf(parser.getValue((Element)v2n.item(0), "y")).floatValue();
 			}
 			
 			NodeList v3n = xmlblock.getElementsByTagName("v3");
 			if (v3n.getLength() == 1) {
 				
 				v3x = Float.valueOf(parser.getValue((Element)v3n.item(0), "x")).floatValue();
 				v3y = Float.valueOf(parser.getValue((Element)v3n.item(0), "y")).floatValue();
 			}
 			
 			Log.d("TRIANGLE", "v1x " + String.valueOf(new Float(v1x)) + 
 					" v1y " + String.valueOf(new Float(v1y)) +
 					" v2x " + String.valueOf(new Float(v2x)) +
 					" v2y " + String.valueOf(new Float(v2y)) +
 					" v3x " + String.valueOf(new Float(v3x)) + 
 					" v3y " + String.valueOf(new Float(v3y)));
 			
 			TexturedTriangle block = new TexturedTriangle(game,
 				parser.getValue(xmlblock, "texture"),
 				func,
 				texSX,
 				texSY,
 				new Vector2(v1x, v1y),
 				new Vector2(v2x, v2y),
 				new Vector2(v3x, v3y)
 			);
 		
 			String level = parser.getValue(xmlblock, "level");
 			if (level.equals("ground")) {
 				
 				this.addGround(block);
 				
 			} else if (level.equals("wall")) {
 				
 				this.addWall(block);
 			}
 		}
 		
 		return true;
 	}
 	
 	public void update(float deltaTime) {
 		
 		if (this.background != null) {
 			
 			this.background.setPosition(new Vector2(
 				this.camera.position.x / this.backgroundSpeed,
 				(this.camera.position.y - this.camera.frustumHeight / 2) / 1.5f
 			));
 		}
 		
 		if (this.sky != null) {
 
 			this.sky.setPosition(new Vector2(
 				this.camera.position.x - this.sky.width / 2,
 				this.camera.position.y - this.sky.height / 2 + this.skyPosition
 			));
 		}
 		
 		for (int i = 0; i < MAX_DECALS; i++) {
 			
 			if (this.decalTime[i] > 0) {
 				
 				this.decalTime[i] -= deltaTime;
 				if (this.decalTime[i] < 0) {
 					
 					if (this.decals[i] != null) {
 						
 						this.decals[i] = null;
 					}
 				}
 			}
 		}
 	}
 	
 	public void reloadTextures() {
 		
 		this.sky.reloadTexture();
 		this.background.reloadTexture();
 		
 		int length;
 		
 		length = this.items.size();
 		for (int i = 0; i < length; i++) {
 			
 			this.items.get(i).reloadTexture();
 		}
 		
 		length = this.pickedUpItems.size();
 		for (int i = 0; i < length; i++) {
 			
 			this.pickedUpItems.get(i).reloadTexture();
 		}
 		
 		length = this.ground.size();
 		for (int i = 0; i < length; i++) {
 			
 			this.ground.get(i).reloadTexture();
 		}
 		
 		length = this.walls.size();
 		for (int i = 0; i < length; i++) {
 			
 			this.walls.get(i).reloadTexture();
 		}
 	}
 	
 	public void dispose() {
 		
 		int length = this.ground.size();
 		for (int i = 0; i < length; i++) {
 			
 			this.ground.get(i).dispose();
 		}
 		
 		length = this.walls.size();
 		for (int i = 0; i < length; i++) {
 			
 			this.walls.get(i).dispose();
 		}
 		
 		for (int i = 0; i < MAX_DECALS; i++) {
 			
 			if (this.decals[i] != null) {
 						
 				this.decals[i].dispose();
 			}
 		}
 	}
 	
 	public void addGround(TexturedShape block) {
 		
 		this.ground.add(block);
 		this.groundGrid.insertStaticObject(block);
 	}
 	
 	public void addWall(TexturedShape block) {
 		
 		this.walls.add(block);
 		this.wallGrid.insertStaticObject(block);
 	}
 	
 	public void addDecal(TexturedShape decal, float time) {
 		
 		for (int i = 0; i < MAX_DECALS; i++) {
 			
 			if (this.decals[i] == null) {
 				
 				this.decals[i] = decal;
 				this.decalTime[i] = time;
 				break;
 			}
 		}
 	}
 	
 	public TexturedShape getGround(GameObject o) {
 		
 		int highestPart = 0;
 		float maxHeight = 0;
 		
 		List<GameObject> colliders = groundGrid.getPotentialColliders(o);
 		int length = colliders.size();
 		if (length == 0) return null;
 		for (int i = 0; i < length; i++) {
 			
 			GameObject part = colliders.get(i);
 			if (o.getPosition().x >= part.getPosition().x && o.getPosition().x <= part.getPosition().x + part.width) {
 				
 				float height = part.getPosition().y + part.height;
 				if (height > maxHeight) {
 					
 					maxHeight = height;
 					highestPart = i;
 				}
 			}
 		}
 		
 		return (TexturedShape)colliders.get(highestPart);
 	}
 	
 	public List<GameObject> getPotentialGroundColliders(GameObject o) {
 		
 		return this.groundGrid.getPotentialColliders(o);
 	}
 
 	public List<GameObject> getPotentialWallColliders(GameObject o) {
 		
 		return this.wallGrid.getPotentialColliders(o);
 	}
 	
 	public List<GameObject> getPotentialFuncColliders(GameObject o) {
 		
 		return this.funcs;
 	}
 	
 	public void draw() {
 		
 		float fromX = this.camera.position.x - this.camera.frustumWidth / 2;
 		float toX = this.camera.position.x + this.camera.frustumWidth / 2;
 		float fromY = this.camera.position.y - this.camera.frustumHeight / 2;
 		float toY = this.camera.position.y + this.camera.frustumHeight / 2;
 		
 		if (this.sky != null) {
 		
 			this.sky.draw();
 		}
 		
 		if (this.background != null) {
 			
 			this.background.draw();
 		}
 		
 		int length;
 		if (this.drawOutlines) {
 			
 			gl.glLineWidth(10);
 			gl.glDisable(GL10.GL_TEXTURE_2D);
 			gl.glColor4f(0.2f, 0.2f, 0.2f, 1);
 			
 			length = this.ground.size();
 			for (int i = 0; i < length; i++) {
 				
 				TexturedShape shape = this.ground.get(i);
 				Vector2 shapePos = shape.getPosition();
 				if ((shapePos.x >= fromX && shapePos.x <= toX) || // left side of shape in screen
 					(shapePos.x <= fromX && shapePos.x + shape.width >= fromX) || // right side of shape in screen
 					(shapePos.x >= fromX && shapePos.x + shape.width <= toX)) { // shape fully in screen
 					
 					shape.drawOutline();
 				}
 			}
 			
 			length = this.walls.size();
 			for (int i = 0; i < length; i++) {
 				
 				TexturedShape shape = this.walls.get(i);
 				Vector2 shapePos = shape.getPosition();
 				if ((shapePos.x >= fromX && shapePos.x <= toX) || // left side of shape in screen
 					(shapePos.x <= fromX && shapePos.x + shape.width >= fromX) || // right side of shape in screen
 					(shapePos.x >= fromX && shapePos.x + shape.width <= toX)) { // shape fully in screen
 					
 					shape.drawOutline();
 				}
 			}
 			
 			gl.glColor4f(1, 1, 1, 1);
 		}
 		
 		gl.glEnable(GL10.GL_TEXTURE_2D);
 		
 		length = this.walls.size();
 		for (int i = 0; i < length; i++) {
 			
 			TexturedShape shape = this.walls.get(i);
 			Vector2 shapePos = shape.getPosition();
 			if ((shapePos.x >= fromX && shapePos.x <= toX) || // left side of shape in screen
 				(shapePos.x <= fromX && shapePos.x + shape.width >= fromX) || // right side of shape in screen
 				(shapePos.x >= fromX && shapePos.x + shape.width <= toX)) { // shape fully in screen
 				
 				shape.draw();
 			}
 		}
 		
 		length = this.ground.size();
 		for (int i = 0; i < length; i++) {
 			
 			TexturedShape shape = this.ground.get(i);
 			Vector2 shapePos = shape.getPosition();
 			if ((shapePos.x >= fromX && shapePos.x <= toX) || // left side of shape in screen
 				(shapePos.x <= fromX && shapePos.x + shape.width >= fromX) || // right side of shape in screen
 				(shapePos.x >= fromX && shapePos.x + shape.width <= toX)) { // shape fully in screen
 				
 				shape.draw();
 			}
 		}
 		
 		length = this.items.size();
 		for (int i = 0; i < length; i++) {
 			
 			this.items.get(i).draw();
 		}
 		
 		for (int i = 0; i < MAX_DECALS; i++) {
 			
 			if (this.decals[i] != null) {
 			
 				this.decals[i].draw();
 			}
 		}
 	}
 	
 	public void restartRace(Player player) {
 		
 		this.startTime = 0;
 		this.stopTime = 0;
 		this.raceStarted = false;
 		this.raceFinished= false;
 		player.reset(this.playerX, this.playerY);
 		camera.setPosition(player.getPosition().x + 20, this.camera.frustumHeight / 2);
 		
 		int length = this.pickedUpItems.size();
 		for (int i = 0; i < length; i++) {
 			
 			TexturedShape item = this.pickedUpItems.get(i);
 			this.items.add(item);
 		}
 		
 		this.pickedUpItems.clear();
 	}
 	
 	public boolean inRace() {
 		
 		return this.raceStarted;
 	}
 	
 	public boolean raceFinished() {
 		
 		return this.raceFinished;
 	}
 	
 	public void startTimer() {
 		
 		this.raceStarted = true;
 		this.startTime = System.nanoTime() / 1000000000.0f;
 	}
 	
 	public void stopTimer() {
 		
 		if (this.raceFinished) return;
 		
 		this.raceFinished = true;
 		this.raceStarted = false;
 		this.stopTime = System.nanoTime() / 1000000000.0f;
 		
 		SharedPreferences prefs = this.game.getSharedPreferences("racesow", Context.MODE_PRIVATE);
 		SubmitScoreThread t = new SubmitScoreThread(this.fileName, prefs.getString("name", "player"), this.getCurrentTime());
 		t.start();
 	}
 	
 	public float getCurrentTime() {
 		
 		if (this.raceStarted) {
 		
 			return System.nanoTime() / 1000000000.0f - this.startTime;
 			
 		} else if (this.raceFinished) {
 			
 			return this.stopTime - this.startTime;
 			
 		} else {
 			
 			return 0;
 		}
 	}
 }

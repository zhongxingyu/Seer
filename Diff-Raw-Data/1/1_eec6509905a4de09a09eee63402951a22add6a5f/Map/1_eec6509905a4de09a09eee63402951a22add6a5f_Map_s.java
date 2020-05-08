 package org.racenet.racesow;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Set;
 
 import org.racenet.framework.AmbientSound;
 import org.racenet.framework.AnimatedBlock;
 import org.racenet.framework.AnimationPreset;
 import org.racenet.framework.Audio;
 import org.racenet.framework.Camera2;
 import org.racenet.framework.FileIO;
 import org.racenet.framework.GLTexture;
 import org.racenet.framework.GameObject;
import org.racenet.framework.Music;
 import org.racenet.framework.TexturedBlock;
 import org.racenet.framework.SpatialHashGrid;
 import org.racenet.framework.TexturedShape;
 import org.racenet.framework.TexturedTriangle;
 import org.racenet.framework.Vector2;
 import org.racenet.framework.XMLParser;
 import org.racenet.racesow.threads.DemoRecorderThread;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 /**
  * Class which mainly loads the map from an 
  * XML file and draws it
  * 
  * @author soh#zolex
  *
  */
 public class Map {
 	
 	private List<GameObject> ground = new ArrayList<GameObject>();
 	private List<GameObject> walls = new ArrayList<GameObject>();
 	private List<GameObject> highlights = new ArrayList<GameObject>();
 	private List<GameObject> front = new ArrayList<GameObject>();
 	public List<GameObject> items = new ArrayList<GameObject>();
 	public List<GameObject> pickedUpItems = new ArrayList<GameObject>();
 	public List<AnimatedBlock> animations = new ArrayList<AnimatedBlock>();
 	private HashMap<String, GLTexture> textures = new HashMap<String, GLTexture>();
 	private TexturedShape[] decals = new TexturedShape[MAX_DECALS];
 	private static final short MAX_DECALS = 64;
 	private float[] decalTime = new float[MAX_DECALS];
 	private TexturedBlock sky;
 	private float skyPosition;
 	private TexturedBlock background;
 	private float backgroundSpeed = 2;
 	float backgroundPosition;
 	private TexturedBlock background2;
 	private float background2Speed = 2;
 	float background2Position;
 	private SpatialHashGrid groundGrid;
 	private SpatialHashGrid wallGrid;
 	private List<GameObject> funcs = new ArrayList<GameObject>();
 	public float playerX = 0;
 	public float playerY = 0;
 	public boolean raceStarted = false;
 	public boolean raceFinished = false;
 	private float startTime = 0;
 	private float stopTime = 0;
 	private boolean gfxHighlights = true;
 	private Camera2 camera;
 	public String fileName;
 	public float pauseTime = 0;
 	String demo = "";
 	public DemoRecorderThread demoRecorder;
 	boolean recordDemos;
 	boolean demoSaved = false;
 	boolean enableAmbience;
 	public AmbientSound[] ambience;
 	float[] ambientVolume; 
 	
 	/**
 	 * Map constructor.
 	 * 
 	 * @param Camera2 camera
 	 * @param boolean drawOutlines
 	 */
 	public Map(Camera2 camera, boolean gfxHighlights, boolean recordDemos) {
 		
 		this.camera = camera;
 		this.gfxHighlights = gfxHighlights;
 		this.recordDemos = recordDemos;
 		
 		// decalTime -1 means there is no decal
 		for (int i = 0; i < MAX_DECALS; i++) {
 			
 			this.decalTime[i] = -1;
 		}
 	}
 	
 	/**
 	 * Load the map from the given XML file and
 	 * store the different items from it
 	 * 
 	 * @param GLGame game
 	 * @param String fileName
 	 * @return boolean
 	 */
 	public boolean load(String fileName, boolean enabledAmbience, boolean demoPlayback) {
 		
 		this.fileName = fileName;
 		XMLParser parser = new XMLParser();
 		this.enableAmbience = enabledAmbience;
 		
 		// try to read the map from the assets
 		try {
 			
 			parser.read(FileIO.getInstance().readAsset("maps" + File.separator + fileName));
 			
 		} catch (IOException e) {
 			
 			// if not found in assets, try to read from sd-card
 			try {
 				
 				parser.read(FileIO.getInstance().readFile("racesow" + File.separator + "maps" + File.separator + fileName));
 				
 			} catch (IOException e2) {
 				
 				return false;
 			}
 		}
 		
 		if (this.enableAmbience) {
 			
 			// obtain the ambient sound from the map
 			NodeList ambienceN = parser.doc.getElementsByTagName("ambience");
 			int numAmbience = ambienceN.getLength();
 			this.ambience = new AmbientSound[numAmbience];
 			this.ambientVolume = new float[numAmbience];
 			for (int i = 0; i < numAmbience; i++) {
 				
 				Element ambienceXML = (Element)ambienceN.item(i);
 				String sound = parser.getValue(ambienceXML, "sound");
 				
 				float volume;
 				try {
 					volume = Float.valueOf(parser.getValue(ambienceXML, "volume")).floatValue();
 				} catch (NumberFormatException e) {
 					volume = 0.2f;
 				}
 				
 				boolean positional;
 				float x, distance, range;
 				try {
 					x = Float.valueOf(parser.getValue(ambienceXML, "x")).floatValue();
 					distance = Float.valueOf(parser.getValue(ambienceXML, "distance")).floatValue();
 					range = Float.valueOf(parser.getValue(ambienceXML, "range")).floatValue();
 					positional = true;
 				} catch (NumberFormatException e) {
 					x = 0;
 					distance = 0;
 					range = 0;
 					positional = false;
 				}
 				
 				AmbientSound ambience = new AmbientSound();
 				ambience.sound = Audio.getInstance().newMusic(sound);
 				ambience.sound.setLooping(true);
 				ambience.sound.setVolume(positional ? 0 : volume);
 				ambience.volume = volume;
 				ambience.positional = positional;
 				ambience.x = x;
 				ambience.distance = distance;
 				ambience.range = range;
 				
 				this.ambientVolume[i] = positional ? 0 : volume;
 				this.ambience[i] = ambience;
 			}
 		}
 		
 		// obtain the player position from the map
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
 		
 		// calculate the width and height of the map
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
 		
 		// grids for pre-collision detection
 		this.groundGrid = new SpatialHashGrid(worldWidth, worldHeight, 30);
 		this.wallGrid = new SpatialHashGrid(worldWidth, worldHeight, 30);
 		
 		// parse the items like plasma and rocket from the map
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
 			
 			TexturedBlock item = new TexturedBlock(
 				"items/" + type + ".png",
 				func,
 				-1,
 				-1,
 				0,
 				0,
 				new Vector2(itemX, itemY),
 				new Vector2(itemX + 3, itemY + 3)
 			);
 
 			this.items.add(item);
 		}
 		
 		// parse the starttimer from the map
 		NodeList startTimerN = parser.doc.getElementsByTagName("starttimer");
 		if (startTimerN.getLength() == 1) {
 			
 			Element xmlStartTimer = (Element)startTimerN.item(0);
 			float startTimerX = Float.valueOf(parser.getValue(xmlStartTimer, "x")).floatValue();
 			//GameObject startTimer = new Func(Func.START_TIMER, startTimerX, 0, 1, worldHeight);
 			GameObject startTimer = new GameObject(new Vector2(startTimerX, 0), new Vector2(startTimerX + 10, 0), new Vector2(startTimerX + 10, worldHeight), new Vector2(startTimerX, worldHeight));
 			startTimer.func = GameObject.FUNC_START_TIMER;
 			this.funcs.add(startTimer);
 		}
 		
 		// parse the stoptimer from the map
 		NodeList stopTimerN = parser.doc.getElementsByTagName("stoptimer");
 		if (stopTimerN.getLength() == 1) {
 			
 			Element xmlStopTimer = (Element)stopTimerN.item(0);
 			float stopTimerX = Float.valueOf(parser.getValue(xmlStopTimer, "x")).floatValue();
 			GameObject stopTimer = new GameObject(new Vector2(stopTimerX, 0), new Vector2(stopTimerX + 10, 0), new Vector2(stopTimerX + 10, worldHeight), new Vector2(stopTimerX, worldHeight));
 			stopTimer.func = GameObject.FUNC_STOP_TIMER;
 			this.funcs.add(stopTimer);
 		}
 		
 		// parse the sky from the map (a static background layer)
 		NodeList skyN = parser.doc.getElementsByTagName("sky");
 		if (skyN.getLength() == 1) {
 			
 			try {
 				
 				this.skyPosition = Float.valueOf(parser.getValue((Element)skyN.item(0), "position")).floatValue();
 				
 			} catch (NumberFormatException e) {
 				
 				this.skyPosition = 0;
 			}
 			
 			this.sky = new TexturedBlock(
 					parser.getValue((Element)skyN.item(0), "texture"),
 					GameObject.FUNC_NONE,
 					-1,
 					-1,
 					0,
 					0,
 					new Vector2(0,skyPosition),
 					new Vector2(this.camera.frustumWidth, skyPosition)
 					);
 		}
 		
 		// parse the background from the map (a slowly moving background layer)
 		NodeList backgroundN = parser.doc.getElementsByTagName("background");
 		if (backgroundN.getLength() == 1) {
 			
 			try {
 				
 				this.backgroundSpeed = Float.valueOf(parser.getValue((Element)backgroundN.item(0), "speed")).floatValue();
 				
 			} catch (NumberFormatException e) {
 				
 				this.backgroundSpeed = 2;
 			}
 			
 			try {
 				
 				this.backgroundPosition = Float.valueOf(parser.getValue((Element)backgroundN.item(0), "position")).floatValue();
 				
 			} catch (NumberFormatException e) {
 				
 				this.backgroundPosition = 0;
 			}
 			
 			float backgroundScale;
 			try {
 				
 				backgroundScale = Float.valueOf(parser.getValue((Element)backgroundN.item(0), "scale")).floatValue();
 				
 			} catch (NumberFormatException e) {
 				
 				backgroundScale = 0.25f;
 			}
 			
 			float backgroundHeight;
 			try {
 				
 				backgroundHeight = Float.valueOf(parser.getValue((Element)backgroundN.item(0), "height")).floatValue();
 			
 			} catch (NumberFormatException e) {
 				
 				backgroundHeight = worldHeight;
 			}
 			
 			this.background = new TexturedBlock(
 				parser.getValue((Element)backgroundN.item(0), "texture"),
 				GameObject.FUNC_NONE,
 				backgroundScale,
 				backgroundScale,
 				0,
 				0,
 				new Vector2(0, 0),
 				new Vector2(worldWidth, 0),
 				new Vector2(worldWidth, backgroundHeight),
 				new Vector2(0, backgroundHeight)
 			);
 		}
 		
 		// parse the background2 from the map (a slowly moving background layer)
 		NodeList background2N = parser.doc.getElementsByTagName("background2");
 		if (background2N.getLength() == 1) {
 			
 			try {
 				
 				this.background2Speed = Float.valueOf(parser.getValue((Element)background2N.item(0), "speed")).floatValue();
 				
 			} catch (NumberFormatException e) {
 				
 				this.background2Speed = 2;
 			}
 			
 			try {
 				
 				this.background2Position = Float.valueOf(parser.getValue((Element)background2N.item(0), "position")).floatValue();
 				
 			} catch (NumberFormatException e) {
 				
 				this.background2Position = 0;
 			}
 			
 			float backgroundScale;
 			try {
 				
 				backgroundScale = Float.valueOf(parser.getValue((Element)background2N.item(0), "scale")).floatValue();
 				
 			} catch (NumberFormatException e) {
 				
 				backgroundScale = 0.25f;
 			}
 			
 			float background2Height;
 			try {
 				
 				background2Height = Float.valueOf(parser.getValue((Element)backgroundN.item(0), "height")).floatValue();
 			
 			} catch (NumberFormatException e) {
 				
 				background2Height = worldHeight;
 			}
 			
 			this.background2 = new TexturedBlock(
 				parser.getValue((Element)background2N.item(0), "texture"),
 				GameObject.FUNC_NONE,
 				backgroundScale,
 				backgroundScale,
 				0,
 				0,
 				new Vector2(0, 0),
 				new Vector2(worldWidth, 0),
 				new Vector2(worldWidth, background2Height),
 				new Vector2(0, background2Height)
 			);
 		}
 		
 		// parse the blocks from the map
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
 			
 			float texShiftX;
 			try {
 				
 				texShiftX = Float.valueOf(parser.getValue(xmlblock, "texshiftx")).floatValue();
 						
 			} catch (NumberFormatException e) {
 				
 				texShiftX = 0;
 			}
 			
 			float texShiftY;
 			try {
 				
 				texShiftY = Float.valueOf(parser.getValue(xmlblock, "texshifty")).floatValue();
 						
 			} catch (NumberFormatException e) {
 				
 				texShiftY = 0;
 			}
 			
 			float x = Float.valueOf(parser.getValue(xmlblock, "x")).floatValue();
 			float y = Float.valueOf(parser.getValue(xmlblock, "y")).floatValue();
 			float width = Float.valueOf(parser.getValue(xmlblock, "width")).floatValue();
 			float height = Float.valueOf(parser.getValue(xmlblock, "height")).floatValue();
 			
 			NodeList textures = xmlblock.getElementsByTagName("texture");
 			GameObject block;
 			int numTextures = textures.getLength();
 			if (numTextures == 1) {
 				
 				String texture = parser.getNodeValue((Element)textures.item(0));
 				if(this.textures.containsKey(texture)) {
 					
 					block = new TexturedBlock(
 						this.textures.get(texture),
 						func,
 						texSX,
 						texSY,
 						texShiftX,
 						texShiftY,
 						new Vector2(x,y),
 						new Vector2(x + width, y),
 						new Vector2(x + width, y + height),
 						new Vector2(x, y + height)
 					);
 				} else {
 					
 					block = new TexturedBlock(
 						texture,
 						func,
 						texSX,
 						texSY,
 						texShiftX,
 						texShiftY,
 						new Vector2(x,y),
 						new Vector2(x + width, y),
 						new Vector2(x + width, y + height),
 						new Vector2(x, y + height)
 					);
 					
 					this.textures.put(texture, ((TexturedBlock)block).texture);
 				}
 				
 				
 				
 			} else {
 				
 				float animDuration;
 				try {
 					
 					animDuration = Float.valueOf(parser.getValue(xmlblock, "duration")).floatValue();
 							
 				} catch (NumberFormatException e) {
 					
 					animDuration = 1;
 				}
 				
 				block = new AnimatedBlock(
 					new Vector2(x,y),
 					new Vector2(x + width, y),
 					new Vector2(x + width, y + height),
 					new Vector2(x, y + height)
 				);
 				
 				block.func = func;
 				
 				String[] textureList = new String[numTextures];
 				for (int j = 0; j < numTextures; j++) {
 					
 					textureList[j] = parser.getNodeValue((Element)textures.item(j));
 				}
 				
 				// assume that all frames are loaded if the first one is
 				GLTexture[] loadedTextures = new GLTexture[textureList.length];
 				if (this.textures.containsKey(textureList[0])) {
 					
 					for (int j = 0; j < numTextures; j++) {
 						
 						loadedTextures[j] = this.textures.get(textureList[j]);
 					}
 					
 					((AnimatedBlock)block).setAnimation(animDuration, loadedTextures);
 					
 				} else {
 					
 					((AnimatedBlock)block).setAnimations(new AnimationPreset(animDuration, textureList));
 					
 					for (int j = 0; j < numTextures; j++) {
 						
 						this.textures.put(textureList[j], ((AnimatedBlock)block).anims[0].keyFrames[j]);
 					}
 				}
 				
 				
 				((AnimatedBlock)block).texScaleHeight = texSY;
 				((AnimatedBlock)block).texScaleWidth = texSX;
 				((AnimatedBlock)block).texShiftX = texShiftX;
 				((AnimatedBlock)block).texShiftY = texShiftY;
 				((AnimatedBlock)block).setupVertices();
 				
 				this.animations.add((AnimatedBlock)block);
 			}
 
 			String level = parser.getValue(xmlblock, "level");
 			if (level.equals("ground")) {
 				
 				this.addGround(block);
 				
 			} else if (level.equals("wall")) {
 				
 				this.addWall(block);
 			
 			} else if (level.equals("highlight") && this.gfxHighlights) {
 				
 				this.highlights.add(block);
 				
 			} else if (level.equals("front") && this.gfxHighlights) {
 				
 				this.front.add(block);
 			}
 		}
 
 		// parse the triangles from the map
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
 			
 			float texShiftX;
 			try {
 				
 				texShiftX = Float.valueOf(parser.getValue(xmlblock, "texshiftx")).floatValue();
 						
 			} catch (NumberFormatException e) {
 				
 				texShiftX = 0;
 			}
 			
 			float texShiftY;
 			try {
 				
 				texShiftY = Float.valueOf(parser.getValue(xmlblock, "texshifty")).floatValue();
 						
 			} catch (NumberFormatException e) {
 				
 				texShiftY = 0;
 			}
 			
 			TexturedTriangle block;
 			String texture = parser.getValue(xmlblock, "texture");
 			if (this.textures.containsKey(texture)) {
 				
 				block = new TexturedTriangle(
 					this.textures.get(texture),
 					func,
 					texSX,
 					texSY,
 					texShiftX,
 					texShiftY,
 					new Vector2(v1x, v1y),
 					new Vector2(v2x, v2y),
 					new Vector2(v3x, v3y)
 				);
 				
 			} else {
 			
 				block = new TexturedTriangle(
 					texture,
 					func,
 					texSX,
 					texSY,
 					texShiftX,
 					texShiftY,
 					new Vector2(v1x, v1y),
 					new Vector2(v2x, v2y),
 					new Vector2(v3x, v3y)
 				);
 				
 				this.textures.put(texture, block.texture);
 			}
 		
 			String level = parser.getValue(xmlblock, "level");
 			if (level.equals("ground")) {
 				
 				this.addGround(block);
 				
 			} else if (level.equals("wall")) {
 				
 				this.addWall(block);
 			
 			} else if (level.equals("hightlight") && this.gfxHighlights) {
 				
 				this.highlights.add(block);
 				
 			} else if (level.equals("front") && this.gfxHighlights) {
 				
 				this.front.add(block);
 			}
 		}
 		
 		// parse tutorials from the map
 		NodeList tutorials = parser.doc.getElementsByTagName("tutorial");
 		int numTutorials = tutorials.getLength();
 		for (int i = 0; i < numTutorials; i++) {
 			
 			Element xmlTutorial = (Element)tutorials.item(i);
 			float tutorialX = Float.parseFloat(parser.getValue(xmlTutorial, "x"));
 			
 			GameObject tutorial = new GameObject(new Vector2(tutorialX, 0), new Vector2(tutorialX + 1, 0), new Vector2(tutorialX + 1, worldHeight), new Vector2(tutorialX, worldHeight));
 			tutorial.func = GameObject.FUNC_TUTORIAL;
 			tutorial.info1 = parser.getValue(xmlTutorial, "info1");
 			tutorial.info2 = parser.getValue(xmlTutorial, "info2");
 			tutorial.info3 = parser.getValue(xmlTutorial, "info3");
 			tutorial.event = parser.getValue(xmlTutorial, "event");
 			this.funcs.add(tutorial);
 		}
 		
 		if (!demoPlayback && this.recordDemos) {
 		
 			this.demoRecorder = new DemoRecorderThread(this.fileName);
 			this.demoRecorder.start();
 		}
 		
 		return true;
 	}
 	
 	public void appendToDemo(String part) {
 		
 		if (this.recordDemos) {
 			
 			try {
 				this.demoRecorder.demoParts.put(part);
 			} catch (InterruptedException e) {
 			}
 		}
 	}
 	
 	/**
 	 * Update the map. Called by GameScreen each frame
 	 * 
 	 * @param float deltaTime
 	 */
 	public void update(float deltaTime) {
 		
 		// move the background layer
 		if (this.background != null) {
 			
 			this.background.setPosition(new Vector2(
 				this.camera.position.x / this.backgroundSpeed,
 				this.backgroundPosition + (this.camera.position.y - this.camera.frustumHeight / 2) / 1.5f
 			));
 		}
 		
 		// move the background layer
 		if (this.background2 != null) {
 			
 			this.background2.setPosition(new Vector2(
 				this.camera.position.x / this.background2Speed,
 				this.background2Position + (this.camera.position.y - this.camera.frustumHeight / 2) / 1.75f
 			));
 		}
 		
 		// set the sky position (TODO: could be done only once!)
 		if (this.sky != null) {
 
 			this.sky.setPosition(new Vector2(
 				this.camera.position.x - this.sky.width / 2,
 				this.camera.position.y - this.sky.height / 2 + this.skyPosition
 			));
 		}
 		
 		// hide decals if their time has elapsed
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
 		
 		// run animations
 		int length = this.animations.size();
 		for (int i = 0; i < length; i++) {
 			
 			this.animations.get(i).animate(deltaTime);
 		}
 	}
 	
 	/**
 	 * Reload the textures from the whole map
 	 */
 	public void reloadTextures() {
 		
 		if (this.sky != null) {
 		
 			this.sky.reloadTexture();
 		}
 		
 		if (this.background != null) {
 		
 			this.background.reloadTexture();
 		}
 		
 		if (this.background2 != null) {
 		
 			this.background2.reloadTexture();
 		}
 		
 		int length = this.items.size();
 		for (int i = 0; i < length; i++) {
 			
 			this.items.get(i).reloadTexture();
 		}
 		
 		length = this.pickedUpItems.size();
 		for (int i = 0; i < length; i++) {
 			
 			this.pickedUpItems.get(i).reloadTexture();
 		}
 		
 		Set<String> textures = this.textures.keySet();
 		for (String texture :textures) {
 			
 			this.textures.get(texture).reload();
 		}
 	}
 	
 	/**
 	 * Enable all map sounds
 	 */
 	public void enableSounds() {
 		
 		if (this.enableAmbience && this.ambience != null) {
 			
 			int length = this.ambience.length;
 			for (int i = 0; i < length; i++) {
 				
 				this.ambience[i].sound.play();
 			}
 		}
 	}
 	
 	/**
 	 * Handle positional ambient sounds
 	 * 
 	 * @param float playerX
 	 */
 	public void handleAmbience(float playerX) {
 		
 		if (!this.enableAmbience || this.ambience == null) {
 			
 			return;
 		}
 		
 		int length = this.ambience.length;
 		for (int i = 0; i < length; i++) {
 			
 			if (this.ambience[i].positional) {
 				
 				float volume = 0;
 				float leftRange = this.ambience[i].x - this.ambience[i].range;
 				float rightRange =  this.ambience[i].x + this.ambience[i].range;
 				float from = leftRange - this.ambience[i].distance;
 				float to = rightRange +  this.ambience[i].distance;
 				if (playerX >= from && playerX <= to) {
 					
 					volume = this.ambience[i].volume;
 					if (playerX < leftRange) {
 						
 						volume = (this.ambience[i].distance - leftRange + playerX) / this.ambience[i].distance * this.ambience[i].volume;
 						
 					} else if (playerX > rightRange) {
 						
 						volume = (this.ambience[i].distance + rightRange - playerX) / this.ambience[i].distance * this.ambience[i].volume;
 					}
 					
 				}
 				
 				if (this.ambientVolume[i] != volume) {
 					
 					this.ambientVolume[i] = volume;
 					this.ambience[i].sound.setVolume(volume);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Get rid of the textures from the whole map
 	 */
 	public void dispose() {
 		
 		int length;
 		
 		if (this.enableAmbience && this.ambience != null) {
 			
 			length = this.ambience.length;
 			for (int i = 0; i < length; i++) {
 				
 				this.ambience[i].sound.stop();
 				this.ambience[i].sound.dispose();
 			}
 		}
 		
 		if (this.recordDemos && this.demoRecorder != null) {
 		
 			this.demoRecorder.cancelDemo();
 			this.demoRecorder.stop = true;
 			this.demoRecorder.demoParts.add("cancel");
 			this.demoRecorder.interrupt();
 		}
 		
 		if (this.sky != null) {
 			
 			this.sky.dispose();
 		}
 		
 		if (this.background != null) {
 		
 			this.background.dispose();
 		}
 		
 		if (this.background2 != null) {
 		
 			this.background2.dispose();
 		}
 		
 		length = this.ground.size();
 		for (int i = 0; i < length; i++) {
 			
 			this.ground.get(i).dispose();
 		}
 		
 		length = this.walls.size();
 		for (int i = 0; i < length; i++) {
 			
 			this.walls.get(i).dispose();
 		}
 		
 		if (this.gfxHighlights) {
 			
 			length = this.front.size();
 			for (int i = 0; i < length; i++) {
 				
 				this.front.get(i).dispose();
 			}
 			
 			length = this.highlights.size();
 			for (int i = 0; i < length; i++) {
 				
 				this.highlights.get(i).dispose();
 			}
 		}
 		
 		for (int i = 0; i < MAX_DECALS; i++) {
 			
 			if (this.decals[i] != null) {
 						
 				this.decals[i].dispose();
 			}
 		}
 	}
 	
 	/**
 	 * Add a ground object to the map
 	 * 
 	 * @param TexturedShape block
 	 */
 	public void addGround(GameObject block) {
 		
 		this.ground.add(block);
 		this.groundGrid.insertStaticObject(block);
 	}
 	
 	/**
 	 * Add a wall object to the map
 	 * 
 	 * @param TexturedShape block
 	 */
 	public void addWall(GameObject block) {
 		
 		this.walls.add(block);
 		this.wallGrid.insertStaticObject(block);
 	}
 	
 	/**
 	 * Add a decal to the map
 	 * 
 	 * @param TexturedShape decal
 	 * @param float time
 	 */
 	public void addDecal(TexturedShape decal, float time) {
 		
 		for (int i = 0; i < MAX_DECALS; i++) {
 			
 			if (this.decals[i] == null) {
 				
 				this.decals[i] = decal;
 				this.decalTime[i] = time;
 				break;
 			}
 		}
 	}
 	
 	/**
 	 * Get the ground at the position of the given object
 	 * 
 	 * @param GameObject o
 	 * @return TetxuredShape
 	 */
 	public GameObject getGround(GameObject o) {
 		
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
 		
 		return colliders.get(highestPart);
 	}
 	
 	/**
 	 * Get the potential ground colliders at the position
 	 * of the given object
 	 * 
 	 * @param GameObject o
 	 * @return List<GameObject>
 	 */
 	public List<GameObject> getPotentialGroundColliders(GameObject o) {
 		
 		return this.groundGrid.getPotentialColliders(o);
 	}
 
 	/**
 	 * Get the potential wall colliders at the position
 	 * of the given object
 	 * 
 	 * @param GameObject o
 	 * @return List<GameObject>
 	 */
 	public List<GameObject> getPotentialWallColliders(GameObject o) {
 		
 		return this.wallGrid.getPotentialColliders(o);
 	}
 	
 	/**
 	 * Get the potential func colliders at the position
 	 * of the given object
 	 * 
 	 * @param GameObject o
 	 * @return List<GameObject>
 	 */
 	public List<GameObject> getPotentialFuncColliders(GameObject o) {
 		
 		return this.funcs;
 	}
 	
 	/**
 	 * Draw the visible part of the map
 	 */
 	public void draw() {
 		
 		float fromX = this.camera.position.x - this.camera.frustumWidth / 2;
 		float toX = this.camera.position.x + this.camera.frustumWidth / 2;
 		
 		if (this.sky != null) {
 		
 			this.sky.draw();
 		}
 		
 		if (this.background != null) {
 			
 			this.background.draw();
 		}
 		
 		if (this.background2 != null) {
 			
 			this.background2.draw();
 		}
 		
 		int length = this.walls.size();
 		for (int i = 0; i < length; i++) {
 			
 			GameObject shape = this.walls.get(i);
 			Vector2 shapePos = shape.getPosition();
 			if ((shapePos.x >= fromX && shapePos.x <= toX) || // left side of shape in screen
 				(shapePos.x <= fromX && shapePos.x + shape.width >= fromX) || // right side of shape in screen
 				(shapePos.x >= fromX && shapePos.x + shape.width <= toX)) { // shape fully in screen
 				
 				shape.draw();
 			}
 		}
 		
 		length = this.ground.size();
 		for (int i = 0; i < length; i++) {
 			
 			GameObject shape = this.ground.get(i);
 			Vector2 shapePos = shape.getPosition();
 			if ((shapePos.x >= fromX && shapePos.x <= toX) || // left side of shape in screen
 				(shapePos.x <= fromX && shapePos.x + shape.width >= fromX) || // right side of shape in screen
 				(shapePos.x >= fromX && shapePos.x + shape.width <= toX)) { // shape fully in screen
 				
 				shape.draw();
 			}
 		}
 		
 		if (this.gfxHighlights) {
 			
 			length = this.highlights.size();
 			for (int i = 0; i < length; i++) {
 				
 				GameObject shape = this.highlights.get(i);
 				Vector2 shapePos = shape.getPosition();
 				if ((shapePos.x >= fromX && shapePos.x <= toX) || // left side of shape in screen
 					(shapePos.x <= fromX && shapePos.x + shape.width >= fromX) || // right side of shape in screen
 					(shapePos.x >= fromX && shapePos.x + shape.width <= toX)) { // shape fully in screen
 					
 					shape.draw();
 				}
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
 	
 	public void drawFront() {
 		
 		if (this.gfxHighlights) {
 			
 			float fromX = this.camera.position.x - this.camera.frustumWidth / 2;
 			float toX = this.camera.position.x + this.camera.frustumWidth / 2;
 			
 			int length = this.front.size();
 			for (int i = 0; i < length; i++) {
 				
 				GameObject shape = this.front.get(i);
 				Vector2 shapePos = shape.getPosition();
 				if ((shapePos.x >= fromX && shapePos.x <= toX) || // left side of shape in screen
 					(shapePos.x <= fromX && shapePos.x + shape.width >= fromX) || // right side of shape in screen
 					(shapePos.x >= fromX && shapePos.x + shape.width <= toX)) { // shape fully in screen
 					
 					shape.draw();
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Restart the current race. Care for resetting
 	 * all required values.
 	 * 
 	 * @param Player player
 	 */
 	public void restartRace(Player player) {
 		
 		if (this.recordDemos) {
 		
 			if (!this.raceFinished) {
 			
 				this.demoRecorder.cancelDemo();
 			
 			} else if (!this.demoSaved) {
 				
 				this.saveDemo();
 			}
 		}
 		
 		this.demo = "";
 		this.startTime = 0;
 		this.stopTime = 0;
 		this.pauseTime = 0;
 		this.raceStarted = false;
 		this.raceFinished= false;
 		
 		int length = this.pickedUpItems.size();
 		for (int i = 0; i < length; i++) {
 			
 			GameObject item = this.pickedUpItems.get(i);
 			this.items.add(item);
 		}
 		
 		length = this.funcs.size();
 		for (int i = 0; i < length; i++) {
 			
 			this.funcs.get(i).finished = false;
 		}
 		
 		this.pickedUpItems.clear();
 		
 		player.reset(this.playerX, this.playerY);
 		camera.setPosition(player.getPosition().x + 20, this.camera.frustumHeight / 2);
 		
 		if (this.recordDemos) {
 			
 			this.demoRecorder.newDemo();
 			this.demoSaved = false;
 		}
 	}
 	
 	/**
 	 * See if it's currently being raced
 	 * 
 	 * @return boolean
 	 */
 	public boolean inRace() {
 		
 		return this.raceStarted;
 	}
 	
 	/**
 	 * See if the race wsa finished
 	 * 
 	 * @return boolean
 	 */
 	public boolean raceFinished() {
 		
 		return this.raceFinished;
 	}
 	
 	/**
 	 * Touch the map's startTimer
 	 */
 	public void startTimer() {
 		
 		if (this.raceStarted) return;
 		
 		this.raceStarted = true;
 		this.raceFinished = false;
 		this.startTime = System.nanoTime() / 1000000000.0f;
 	}
 	
 	/**
 	 * Touch the map's stopTimer
 	 */
 	public void stopTimer() {
 		
 		if (this.raceFinished) return;
 		
 		this.raceFinished = true;
 		this.raceStarted = false;
 		this.stopTime = System.nanoTime() / 1000000000.0f;
 	}
 	
 	/**
 	 * Save the currently recording demo
 	 */
 	public void saveDemo() {
 		
 		if (this.recordDemos && !this.demoSaved) {
 			
 			try {
 				
 				this.demoRecorder.demoParts.put("save-demo");
 				this.demoSaved = true;
 				
 			} catch (InterruptedException e) {}
 		}
 	}
 	
 	/**
 	 * Get the current race time
 	 * 
 	 * @return float
 	 */
 	public float getCurrentTime() {
 		
 		if (this.raceStarted) {
 		
 			return System.nanoTime() / 1000000000.0f - this.startTime - this.pauseTime;
 			
 		} else if (this.raceFinished) {
 			
 			return this.stopTime - this.startTime - this.pauseTime;
 			
 		} else {
 			
 			return 0;
 		}
 	}
 }

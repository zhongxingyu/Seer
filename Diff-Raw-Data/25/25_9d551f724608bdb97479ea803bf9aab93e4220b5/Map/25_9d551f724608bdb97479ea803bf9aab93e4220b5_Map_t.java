 package org.racenet.racesow;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.racenet.framework.Func;
 import org.racenet.framework.GLGame;
 import org.racenet.framework.GameObject;
 import org.racenet.framework.Mesh;
 import org.racenet.framework.SpatialHashGrid;
 import org.racenet.framework.XMLParser;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 public class Map {
 	
 	private List<Mesh> front = new ArrayList<Mesh>();
 	private List<Mesh> back = new ArrayList<Mesh>();
 	private SpatialHashGrid frontGrid;
 	private SpatialHashGrid backGrid;
 	private SpatialHashGrid funcGrid;
 	public float playerX = 0;
 	public float playerY = 0;
 	private boolean raceStarted = false;
 	private boolean raceFinished = false;
 	private float startTime = 0;
 	private float stopTime = 0;
 	
 	public Map() {
 		
 	}
 	
 	public boolean load(GLGame game, String fileName) {
 		
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
 		
 		NodeList meshes = parser.doc.getElementsByTagName("mesh");
 		
 		float worldWidth = 0;
 		float worldHeight = 0;
 		
 		int numMeshes = meshes.getLength();
 		for (int i = 0; i < numMeshes; i++) {
 			
 			Element mesh = (Element)meshes.item(i);
 			float meshMaxX = Float.valueOf(parser.getValue(mesh, "x")).floatValue() + Float.valueOf(parser.getValue(mesh, "width")).floatValue();
 			if (worldWidth < meshMaxX) {
 				
 				worldWidth = meshMaxX;
 			}
 			
 			float meshMaxY = Float.valueOf(parser.getValue(mesh, "y")).floatValue() + Float.valueOf(parser.getValue(mesh, "height")).floatValue();
 			if (worldHeight < meshMaxY) {
 				
 				worldHeight = meshMaxY;
 			}
 		}
 		
 		this.frontGrid = new SpatialHashGrid(worldWidth, worldHeight, 20);
 		this.backGrid = new SpatialHashGrid(worldWidth, worldHeight, 20);
 		this.funcGrid = new SpatialHashGrid(worldWidth, worldHeight, 20);
 		
 		NodeList startTimerN = parser.doc.getElementsByTagName("starttimer");
 		if (startTimerN.getLength() == 1) {
 			
 			Element xmlStartTimer = (Element)startTimerN.item(0);
 			float startTimerX = Float.valueOf(parser.getValue(xmlStartTimer, "x")).floatValue();
 			GameObject startTimer = new Func(Func.START_TIMER, startTimerX, 0, 1, worldHeight);
 			this.funcGrid.insertStaticObject(startTimer);
 		}
 		
 		NodeList stopTimerN = parser.doc.getElementsByTagName("stoptimer");
 		if (stopTimerN.getLength() == 1) {
 			
 		Element xmlStopTimer = (Element)stopTimerN.item(0);
 		float stopTimerX = Float.valueOf(parser.getValue(xmlStopTimer, "x")).floatValue();
 			GameObject stopTimer = new Func(Func.STOP_TIMER, stopTimerX, 0, 1, worldHeight);
 			this.funcGrid.insertStaticObject(stopTimer);
 		}
 		
 		for (int i = 0; i < numMeshes; i++) {
 			
 			Element xmlMesh = (Element)meshes.item(i);
 			
 			short func;
 			try {
 				
 				func = Short.valueOf(parser.getValue(xmlMesh, "func"));
 				
 			} catch (NumberFormatException e) {
 				
 				func = 0;
 			}
 			
 			float texSX;
 			try {
 				
 				texSX = Float.valueOf(parser.getValue(xmlMesh, "texsx")).floatValue();
 						
 			} catch (NumberFormatException e) {
 				
 				texSX = 0;
 			}
 			
 			float texSY;
 			try {
 				
 				texSY = Float.valueOf(parser.getValue(xmlMesh, "texsy")).floatValue();
 						
 			} catch (NumberFormatException e) {
 				
 				texSY = 0;
 			}
 			
 			Mesh mesh = new Mesh(game,
 				Float.valueOf(parser.getValue(xmlMesh, "x")).floatValue(),
 				Float.valueOf(parser.getValue(xmlMesh, "y")).floatValue(),
 				Float.valueOf(parser.getValue(xmlMesh, "width")).floatValue(),
 				Float.valueOf(parser.getValue(xmlMesh, "height")).floatValue(),
 				parser.getValue(xmlMesh, "texture"),
 				func,
 				texSX,
 				texSY
 			);
 			
 			String level = parser.getValue(xmlMesh, "level");
 			if (level.equals("front")) {
 				
 				this.addFront(mesh);
 				
 			} else if (level.equals("back")) {
 				
 				this.addBack(mesh);
 			}
 		}
 		
 		return true;
 	}
 	
 	public void reloadTextures() {
 		
 		int length = this.front.size();
 		for (int i = 0; i < length; i++) {
 			
 			this.front.get(i).reloadTexture();
 		}
 		
 		length = this.back.size();
 		for (int i = 0; i < length; i++) {
 			
 			this.back.get(i).reloadTexture();
 		}
 	}
 	
 	public void dispose() {
 		
 		int length = this.front.size();
 		for (int i = 0; i < length; i++) {
 			
 			this.front.get(i).dispose();
 		}
 		
 		length = this.back.size();
 		for (int i = 0; i < length; i++) {
 			
 			this.back.get(i).dispose();
 		}
 	}
 	
 	public void addFront(Mesh mesh) {
 		
 		this.front.add(mesh);
 		this.frontGrid.insertStaticObject(mesh);
 	}
 	
 	public Mesh getFront(int i) {
 		
 		return this.front.get(i);
 	}
 	
 	public int numFront() {
 		
 		return this.front.size();
 	}
 	
 	public void addBack(Mesh mesh) {
 		
 		this.back.add(mesh);
 		this.backGrid.insertStaticObject(mesh);
 	}
 	
 	public Mesh getBack(int i) {
 		
 		return this.back.get(i);
 	}
 	
 	public int numBack() {
 		
 		return this.back.size();
 	}
 	
 	public Mesh getGround(GameObject o) {
 		
 		int tallestPart = 0;
 		float tallestHeight = 0;
 		
 		List<GameObject> colliders = frontGrid.getPotentialColliders(o);
 		int length = colliders.size();
 		if (length == 0) return null;
 		for (int i = 0; i < length; i++) {
 			
 			GameObject part = colliders.get(i);
 			if (o.position.x >= part.position.x && o.position.x <= part.position.x + part.bounds.width) {
 				
 				float height = part.position.y + part.bounds.height;
 				if (height > tallestHeight) {
 					
 					tallestHeight = height;
 					tallestPart = i;
 				}
 			}
 		}
 		
 		return (Mesh)colliders.get(tallestPart);
 	}
 	
 	public List<GameObject> getPotentialFrontColliders(GameObject o) {
 		
 		return this.frontGrid.getPotentialColliders(o);
 	}
 
 	public List<GameObject> getPotentialBackColliders(GameObject o) {
 		
 		return this.backGrid.getPotentialColliders(o);
 	}
 	
 	public List<GameObject> getPotentialFuncColliders(GameObject o) {
 		
 		return this.funcGrid.getPotentialColliders(o);
 	}
 	
 	public void draw() {
 		
 		int length = this.numBack();
 		for (int i = 0; i < length; i++) {
 			
 			this.getBack(i).draw();
 		}
 		
 		length = this.numFront();
 		for (int i = 0; i < length; i++) {
 			
 			this.getFront(i).draw();
 		}
 	}
 	
 	public void restartRace(Player player) {
 		
 		this.startTime = 0;
 		this.stopTime = 0;
 		this.raceStarted = false;
 		this.raceFinished= false;
 		player.reset(this.playerX, this.playerY);
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
 		
 		this.raceFinished = true;
 		this.raceStarted = false;
 		this.stopTime = System.nanoTime() / 1000000000.0f;
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

 package model;
 import static org.lwjgl.opengl.GL11.*;
 
 import gui.Gui;
 import gui.GuiElement;
 import guiElements.Label;
 import guiElements.Solide;
 import input.Input;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import javax.imageio.ImageIO;
 
 import mesh.Face;
 import mesh.Material;
 import mesh.Mesh;
 import mesh.ObjLoader;
 import mesh.Vertex;
 import mob.Peasant;
 import mob.Task;
 
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.input.Mouse;
 import org.lwjgl.opengl.Display;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.opengl.Texture;
 import org.newdawn.slick.opengl.TextureLoader;
 import org.newdawn.slick.util.ResourceLoader;
 
 import particles.Emitter;
 
 import buildings.Barraks;
 import buildings.Building;
 import buildings.BuildingTypes;
 import buildings.Dock;
 import buildings.Farm;
 import buildings.Masonry;
 import buildings.Mill;
 import buildings.Residence;
 import buildings.TownHall;
 import buildings.WorkShop;
 
 import resorsers.Tree;
 import sprite.Ship;
 import sprite.Sprite;
 import view.View;
 import world.Camera;
 import world.World;
 
 public class Model {
 	public int width = 1000, height = 800, guiScale = 850;	//window width height
 	public String title = "RS v:null";	//window title
 	public long loopDelay = 0;	//delay between updates normally 16
 	public float shipCrashX = -0.6f, shipCrashY = -0.8f;	//position the ship crashes at
 	
 	private View view;	//holds all render info
 	private Loop loop;	//update loop
 	private Ship ship;	//holds the main ship
 	private Sprite water;	//holds floor
 	private DayNightCycle cycle;	//controls time
 	private Generate gen;	//random world generation
 	private Input input;	//keyboard/ mouse input
 	private Gui gui;	//all gui info (interaction, events, displaylist)
 	private World menu;	//holds global variables (probobaly should remove this)
 	//private Random random;
 	private Camera camera;	//controlls view position
 	
 	private ArrayList <Sprite> spriteList;	//all sprites on stage that will be drawn
 	private ArrayList <Peasant> mobList;	//all mobs
 	private ArrayList <Building> buildingList;	//all buildings
 	private ArrayList <Emitter> miters;	//holds all particle emiters
 	
 	private boolean fogEnabled = true, rotateView = false, fullscreen = false, rotCam = false;
 	
 	//gui elements
 	private ArrayList<GuiElement> labels;
 	private Solide bg;
 	//private ImageIcon gameMenuBg;
 	private long frames, totalFrames, startTime = System.currentTimeMillis(), frameStart = System.currentTimeMillis();
 	
 	//textures
 	private String texturePath[] = {"res/pack.png", "res/fence.png", "res/island.png", "res/water1.png", "res/pickaxe.png",
 			"res/water2.png", "res/water3.png", "res/hammer.png", "res/sword.png", "res/woodaxe.png",
 			"res/particles/water1.png", "res/particles/water2.png", "res/particles/water3.png", "res/particles/tree.png"};	//holds textures
 	private Texture texturePack[];	//holds raw texture data
 	private ArrayList<Material> materials;	//holds textures
 	private ArrayList<Material> mobMats = new ArrayList<Material>();
 	//private boolean[] usedskin;
 	
 	//array of model paths
 	private String[] modelPaths = {"res/models/town_hall/town_hall.obj",
 			"res/models/workshop/workshop.obj",
 			"res/models/windmill/wind_mill.obj",
 			"res/models/farm/farm_house.obj",
 			"res/models/barraks/barraks.obj",
 			"res/models/ship/ship.obj",
 			"res/models/residence/res.obj",
 			"res/models/masonry/masonry.obj",
 			"res/models/ship/ship.obj",
 			"res/models/mob/mob.obj",
 			"res/models/dock/dock.obj",
 			"res/models/rock/rock.obj",
 			"res/models/tree/tree1.obj"
 	};
 	private ArrayList<Mesh> models;	//loaded models
 	
 	//private Material[] villagerMats;
 	
 	//CONSTRUCTOr
 	public Model(){
 		view = new View(this, title, width, height);	//create view / renderer
 		view.initGL();	//setup opengl for view
 		gui = new Gui("res/font.ttf", width, height);	//setup gui
 		input = new Input(this);	//call input constructor
 		menu = new World();	//setup golobal vars
 		
 		generate();	//setup game
 		
 		loop = new Loop(this, loopDelay);	//setup main loop
 		loop.run();	//star loop
 
 	}
 	
 	private void loadAll(){
 		//float total, progress;
 		loadTex();
 		loadModels();
 	}
 	
 	//load models
 	public void loadModels(){
 		ObjLoader loader = new ObjLoader();
 		models = new ArrayList<Mesh>();
 		
 		for(int i = 0; i < modelPaths.length; i++){
 			Mesh[] tempMesh = loader.loadMesh(modelPaths[i]);
 			for(int a = 0; a < tempMesh.length; a++){
 				models.add(tempMesh[a]);
 			}
 		}
 	}
 	
 	//load textures
 	public void loadTex(){
 		
 		texturePack = new Texture[texturePath.length];	//create texture array
 		materials = new ArrayList<Material>();
 		
 		for(int i = 0; i < texturePath.length; i++){	//load textures
 			try {
 				texturePack[i] = TextureLoader.getTexture("PNG", ResourceLoader.getResourceAsStream(texturePath[i]), GL_NEAREST);
 				materials.add(new Material(texturePack[i], texturePath[i]));
 			} catch (Exception e) {
 				e.printStackTrace();
 				System.out.println();
 			}
 			
 		}
 		
 		String path = "res/models/mob/skins";
 		File[] files = new File(path).listFiles();
 		
 		for(int i = 0; i < files.length; i++){
 			try {
 				mobMats.add(new Material(TextureLoader.getTexture("PNG", ResourceLoader.getResourceAsStream(path + "/" + files[i].getName()), GL_NEAREST)));
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	//reset variables and generate island terrain
 	public void generate(){
 		//setup lists
 		spriteList = new ArrayList<Sprite>();
 		mobList = new ArrayList<Peasant>();
 		buildingList = new ArrayList<Building>();
 		miters = new ArrayList<Emitter>();
 		
 		loadAll();	//load resources
 		
 		//load island image used to generate terain
 		BufferedImage island = null;
 		try{
 			island = ImageIO.read(new File("res/island.png"));
 		}catch(IOException e){}
 		
 		//generate terain
 		gen = new Generate(island, this);
 		gen.island();
 		
 		//add island
 		//camera = new Sprite(texture[0], 0.0f, 0.1f, -2.7f, 45.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);	//add camera
 		camera = new Camera(0.0f, 0.1f, -2.3f, 45.0f, 0.0f, 0.0f, menu.FOV_NORM);
 		
 		//add start ship
 		ship = new Ship(getModel("ship"), null, null, -1.5f, 0.0f, -0.8f, 0.003f, 0, 0, this);
 		spriteList.add(ship);
 		
 		//testing
		for(int i = 0; i < 5; i++){
			addPeasant(-1.0f, 0.0f, Task.BUILD);
			addPeasant(-1.0f, 0.0f, Task.NONE);
			addPeasant(-1.0f, 0.0f, Task.WOOD);
			addPeasant(-1, 0, Task.STONE);
 		}
 		
 		//add water
 		//String[] resPath= {"water1", "water2", "water3"};
 		
 		Mesh m = new Mesh("main_island");
 		Face fa = new Face(new Vertex(-10.0f, 0.0f, -10.0f, 0.0f, 150.0f),
 				new Vertex(10.0f, 0.0f, -10.0f, 150.0f, 150.0f),
 				new Vertex(10.0f, 0.0f, 10.0f, 150.0f, 0.0f),
 				new Vertex(-10.0f, 0.0f, 10.0f, 0.0f, 0.0f)
 		);
 		//m.setMat(getMaterial("water2"));
 		m.addFace(fa);
 		water = new Sprite(m);
 		water.cullFace(false);
 		water.setY(water.getY() - 0.01f);
 		water.setTex(getMaterial("water1", "water2", "water3", "water2"));
 		water.autoframe(true, true, 15);
 		spriteList.add(water);
 		
 		//add island mesh
 		Mesh me = new Mesh("main_island");
 		Face f = new Face(new Vertex(-1.0f, 0.0f, -1.0f, 0.0f, 1.0f),
 				new Vertex(1.0f, 0.0f, -1.0f, 1.0f, 1.0f),
 				new Vertex(1.0f, 0.0f, 1.0f, 1.0f, 0.0f),
 				new Vertex(-1.0f, 0.0f, 1.0f, 0.0f, 0.0f)
 		);
 		me.setMat(getMaterial("island"));
 		me.addFace(f);
 		Sprite isl = new Sprite(me);
 		isl.cullFace(false);
 		spriteList.add(isl);
 		
 		//add test buildings
 		
 		addBuilding(BuildingTypes.TOWN_HALL, 0.0f, 0.0f);
 		//addBuilding(BuildingTypes.WORKSHOP, 0.2f, 0.0f);
 		//addBuilding(BuildingTypes.FARM, -0.1f, 0.2f, -0.0f, 0.0f);
 		//addBuilding(BuildingTypes.MILL, 0.3f, 0.2f);
 		//addBuilding(BuildingTypes.BARRAKS, -0.3f, 0.2f);
 		//addBuilding(BuildingTypes.RESIDENCE, 0.1f, 0.2f);
 		//addBuilding(BuildingTypes.MASONRY,-0.2f, 0.0f);
 		//addBuilding(BuildingTypes.DOCK, -0.3f, -0.4f);
 		
 		//setup global vars
 		menu.FOV = menu.START_FOV;
 		menu.FOV_ZOOM_VEL = menu.INTRO_ZOOM_START_VEL;
 		menu.IS_INTRO = true;
 		
 		//setup day/night cycle
 		cycle = new DayNightCycle(0.0001f, 1.0f);
 		
 		//add test gui
 		labels = new ArrayList<GuiElement>();
 		
 		for(int i = 0; i < 16; i++){
 			labels.add(new Label(0, 0, "null", gui.getFont(), Color.white));
 		}
 		
 		
 		int w = 0, h = 0;
 		
 		for(int i = 0; i < labels.size(); i++){
 			if(labels.get(i).getWidth() > w){
 				w = labels.get(i).getWidth();
 			}
 			h += labels.get(i).getHeight();
 			labels.get(i).setY((labels.get(i).getHeight() * i));
 		}
 		
 		bg = new Solide(labels.get(0).getX(), labels.get(0).getY(), w, h, new Color(0.0f, 0.0f, 0.0f, 1.0f));
 		
 		gui.add(bg);
 		gui.add(labels);
 		//double wid = guiScale, hei = 0, var = (double)texture[15][0].getImageHeight() / (double)texture[15][0].getImageWidth();
 		//hei = var * wid;
 		//gameMenuBg = new ImageIcon((int)(width / 2) - (int)(wid / 2), 0, (int)wid, (int)hei, texture[15][0]);
 		//gui.add(gameMenuBg);
 		
 		view.init(cycle, water, gui, menu, camera);	//add resources to view
 	}
 	
 	//update all objects
 	public void update(){
 		
 		//camera.setX(ship.getX());
 		frames++;
 		totalFrames++;
 		int particleCount = 0;
 		for(int i = 0; i < miters.size(); i++){
 			particleCount += miters.get(i).getParticle().size();
 		}
 		labels.get(0).setString("FPS: " + ((float)frames / (float)(System.currentTimeMillis() - frameStart)) * 1000);
 		labels.get(1).setString("UPS: " + "null");
 		labels.get(2).setString("OVERALL_FPS/UPS: " + (totalFrames / (System.currentTimeMillis() - startTime)) * 1000);
 		labels.get(3).setString("PARTICLES: " + particleCount);
 		labels.get(4).setString("TIME: " + cycle.getTime());
 		labels.get(5).setString("CAMERA_Y_ROT: " + camera.getYRot());
 		labels.get(6).setString("CAMERA_X_ROT: " + camera.getXRot());
 		labels.get(7).setString("TIME: " + (System.currentTimeMillis() - startTime));
 		labels.get(8).setString("FRAMES: " + totalFrames);
 		labels.get(9).setString("FRAME_TIME: " + (System.currentTimeMillis() - frameStart));
 		labels.get(10).setString("FOV: " + menu.FOV);
 		labels.get(11).setString("FOG: " + fogEnabled);
 		labels.get(12).setString("IS_FULLSCREEN: " + fullscreen);
 		labels.get(13).setString("W:" + width + " H:" + height);
 		labels.get(14).setString("spriteCount: " + spriteList.size());
 		labels.get(15).setString("rock_res: " + menu.ROCK);
 		
 		int w = 0;
 		for(int i = 0; i < labels.size(); i++){
 			if(labels.get(i).getWidth() > w){
 				w = labels.get(i).getWidth();
 			}
 		}
 		bg.setWidth(w);
 		
 		//gameMenuBg.setX((int)(width / 2) - (int)(guiScale / 2));
 		
 		if(System.currentTimeMillis() - frameStart > 1000){
 			frameStart = System.currentTimeMillis();
 			frames = 0;
 		}
 		
 		
 		if(Display.isCloseRequested()){
 			loop.stop();
 		}
 		
 		view.update();
 		
 		camera.update();
 		water.update();
 		cycle.update();
 		input.update();
 		
 		water.setY(((float)Math.sin((System.currentTimeMillis() - startTime) / 800.0f) / 120.0f) - 0.01f);
 		water.setZ((float)Math.sin((System.currentTimeMillis() - startTime) / 5000.0f) / 10.0f);
 		water.setX((float)Math.cos((System.currentTimeMillis() - startTime) / 8000.0f) / 10.0f);
 		
 		for(int i = 0; i < spriteList.size(); i++){
 			if(spriteList.get(i) == null){
 				System.out.println("null sprite @: " + i);
 			}
 			if(!spriteList.get(i).isAlive()){
 				spriteList.remove(i);
 			}
 			spriteList.get(i).update();
 		}
 		
 		for(int i = 0; i < miters.size(); i++){
 			if(miters.get(i) == null || !miters.get(i).isAlive()){
 				miters.remove(i);
 			}else{
 				miters.get(i).update();
 			}
 		}
 		
 		//update globular vars
 		if(menu.IS_INTRO){
 
 			if(menu.FOV > menu.FOV_NORM){
 				menu.FOV -= menu.FOV_ZOOM_VEL;
 				if(menu.FOV_ZOOM_VEL > menu.INTRO_ZOOM_MIN_DEC){
 					menu.FOV_ZOOM_VEL -= menu.INTRO_ZOOM_VEL_DEC;
 				}else if(menu.FOV_ZOOM_VEL < menu.INTRO_ZOOM_MIN_DEC){
 					menu.FOV_ZOOM_VEL = menu.INTRO_ZOOM_MIN_DEC;
 				}
 			}
 		}else{
 			//if(menu.FOV != menu.FOV_NORM){
 				//menu.FOV = menu.FOV_NORM;
 			//}
 		}
 		
 		//if(ship.getX() >  shipCrashX && !ship.isCrashed()){
 			//ship.setCrash(true);
 			//spriteList.add(new Ship(ship.getTextureList(), 1, ship.getX(), ship.getY(), ship.getZ(), ship.getXVel(), ship.getYVel(), ship.getZVel()));
 			//spriteList.add(new Ship(ship.getTextureList(), 2, ship.getX(), ship.getY(), ship.getZ(), ship.getXVel(), ship.getYVel(), ship.getZVel()));
 			//spriteList.remove(ship);
 		//}
 		
 		if(rotateView){
 			camera.setYRot(camera.getYRot() + ((width / 2) - ((float)Mouse.getX())) / 5);
 			camera.setXRot(camera.getXRot() + ((height / 2) - ((float)Mouse.getY())) / 5);
 			Mouse.setCursorPosition(width / 2, height / 2);
 		}else{
 			if(Mouse.isGrabbed()){
 				Mouse.setGrabbed(false);
 			}
 		}
 	}
 	
 	public void render(){
 		
 		view.render();
 		
 	}
 	
 	//keyboard pressed
 	public void keyPressed(int key){
 		System.out.println("key pressed: " + key);
 		if(key == Keyboard.KEY_F){
 			if(fogEnabled){
 				fogEnabled = false;
 			}else{
 				fogEnabled = true;
 			}
 			view.setFog(fogEnabled);
 		}else if(key == Keyboard.KEY_R){
 			if(rotateView){
 				rotateView = false;
 			}else{
 				rotateView = true;
 				Mouse.setGrabbed(true);
 				Mouse.setCursorPosition(width / 2, height / 2);
 			}
 		}else if(key == Keyboard.KEY_ESCAPE){
 			esc();
 		}else if(key == Keyboard.KEY_F11){
 			rotateView = false;
 			if(fullscreen){
 				if(view.setFullscreen(false)){
 					fullscreen = false;
 				}
 				
 			}else{
 				if(view.setFullscreen(true)){
 					fullscreen = true;
 				}
 			}
 		}else if(key == Keyboard.KEY_Q){
 			if(rotCam){
 				rotCam = false;
 				camera.setYRotVel(0);
 			}else{
 				rotCam = true;
 				camera.setYRotVel(0.3f);
 				menu.FOV = menu.FOV_NORM;
 			}
 		}else if(key == Keyboard.KEY_1){
 			cycle.setTime(-1);
 		}else if(key == Keyboard.KEY_2){
 			cycle.setTime(0);
 		}else if(key == Keyboard.KEY_3){
 			cycle.setTime(1);
 		}else if(key == Keyboard.KEY_X){
 			generate();
 		}else if(key == Keyboard.KEY_W){
 			if(Keyboard.getEventKeyState()){
 				camera.setZVel(0.03f);
 			}else{
 				camera.setZVel(0.0f);
 			}
 		}else if(key == Keyboard.KEY_A){
 			if(Keyboard.getEventKeyState()){
 				camera.setXVel(0.03f);
 			}
 		}else if(key == Keyboard.KEY_S){
 			if(Keyboard.getEventKeyState()){
 				camera.setZVel(-0.03f);
 			}
 		}else if(key == Keyboard.KEY_D){
 			if(Keyboard.getEventKeyState()){
 				camera.setXVel(-0.03f);
 			}
 		}else if(key == Keyboard.KEY_B){
 			addBuilding(BuildingTypes.TOWN_HALL, 0.0f, 0.0f);
 		}
 	}
 	
 	//keyboard released
 	public void keyReleased(int key){
 		if(key == Keyboard.KEY_W || key == Keyboard.KEY_S){
 			camera.setZVel(0);
 		}else if(key == Keyboard.KEY_A || key == Keyboard.KEY_D){
 			camera.setXVel(0);
 		}
 	}
 	
 	public void mouseWheelEvent(float f){
 		menu.FOV += f / 500;
 	}
 	
 	//escape command
 	public void esc(){
 		if(rotateView){
 			rotateView = false;
 		}else if(fullscreen){
 			rotateView = false;
 			view.setFullscreen(false);
 		}else{
 			loop.stop();
 		}
 	}
 	
 	//kill the program
 	public void loopEnded(){
 		Display.destroy();
 	}
 	
 	//add methods
 	public void addTree(Tree t){
 		gen.addTree(t);
 	}
 	
 	public void addBuilding(BuildingTypes type, float... f){
 		Building b = null;
 		
 		switch(type){
 		case TOWN_HALL:{
 			Mesh m = getModel("town_hall");
 			b = new TownHall( m, f[0], f[1], this);
 		}break;
 		case WORKSHOP:{
 			b = new WorkShop(getModel("workshop"), f[0], f[1], this);
 		}break;
 		case MASONRY:{
 			Mesh m = getModel("masonry");
 			b = new Masonry( m, f[0], f[1], this);
 		}break;
 		case FARM:{
 			if(f.length <= 2){
 				try {
 					throw new Exception("no with or height");
 				} catch (Exception e) {}
 				return;
 			}
 			b = new Farm(getModel("farm"), f[0], f[1], f[2], f[3], this);
 		}break;
 		case MILL:{
 			b = new Mill(getModel("mill"), getModel("prop"), f[0], f[1], this);
 		}break;
 		case BARRAKS:{
 			Mesh m = getModel("barraks");
 			b = new Barraks(m, f[0], f[1], this);
 		}break;
 		case RESIDENCE:{
 			Mesh m = getModel("res");
 			b = new Residence(m, f[0], f[1], null, this);
 		}break;
 		case DOCK:{
 			Mesh m = getModel("dock");
 			b = new Dock(m, f[0], f[1], this);
 		}break;
 		default:{
 			try {
 				throw new Exception("building not found: " + type);
 			} catch (Exception e) {}
 		}
 		}
 		
 		if(b == null){
 			try {
 				throw new Exception("building not found: " + type);
 			} catch (Exception e) {}
 		}
 		
 		buildingList.add(b);
 		spriteList.add(b);
 	}
 	
 	//remove methods
 	public void removeTree(Tree t){
 		gen.removeTree(t);
 	}
 	
 	//get methods
 	public Camera getCamera(){
 		return camera;
 	}
 	public ArrayList<Emitter> getMiters(){
 		return miters;
 	}
 	public ArrayList<Sprite> getSpriteList(){
 		return spriteList;
 	}
 	
 	public ArrayList<Peasant> getMobList(){
 		return mobList;
 	}
 	
 	public ArrayList<Building> getBuildingList(){
 		return buildingList;
 	}
 	
 	public Mesh getModel(String s){
 		for(int i = 0; i < models.size(); i++){
 			if(models.get(i).getName().contains(s)){
 				return models.get(i);
 			}
 		}
 		try {
 			throw new FileNotFoundException("model not found\nmodel: '" + s + "' does not exist");
 		} catch (Exception e) {e.printStackTrace();}
 		return null;
 	}
 	
 	public Material getMaterial(String s){
 		for(int i = 0; i < materials.size(); i++){
 			if(materials.get(i).getName().contains(s)){
 				return materials.get(i);
 			}
 		}
 		try {
 			throw new FileNotFoundException("material not found\nmat: '" + s + "' does not exist");
 		} catch (Exception e) {e.printStackTrace();}
 		return getMaterial("pack");
 	}
 	
 	public Texture[] getTexture(String... s){
 		Texture[] tex = new Texture[s.length];
 		
 		for(int i = 0; i < s.length; i++){
 			tex[i] = getMaterial(s[i]).getTexture();
 		}
 		
 		return tex;
 	}
 	
 	public Material[] getMaterial(String... s){
 		Material[] mat = new Material[s.length];
 		
 		for(int i = 0; i < s.length; i++){
 			mat[i] = getMaterial(s[i]);
 		}
 		
 		return mat;
 	}
 	
 	//set methods
 	public void setSpriteList(ArrayList<Sprite> sprites){
 		spriteList = sprites;
 	}
 	
 	//add peasant
 	public void addPeasant(float x, float z, Task task){
 		Peasant peasant;
 		
 		peasant = new Peasant(x, z, getModel("head"), getModel("body"), mobMats.get((int)((Math.random() * 100) % (mobMats.size()))), this,
 				getModel("right_arm"), getModel("left_arm"), getModel("right_leg"), getModel("left_leg"),
 				getModel("item"), task, menu);
 		
 		mobList.add(peasant);
 		spriteList.add(peasant);
 		
 	}
 }

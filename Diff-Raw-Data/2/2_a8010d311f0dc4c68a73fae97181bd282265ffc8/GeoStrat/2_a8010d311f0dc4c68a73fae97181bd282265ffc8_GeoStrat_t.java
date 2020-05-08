 package Essentials;
 import org.newdawn.slick.AppGameContainer;
 import org.newdawn.slick.BasicGame;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 
 import Entities.Doodad;
 import Entities.Goldmine;
 import Entities.Ironmine;
 import Entities.Path;
 import Entities.Tree;
 import Utilities.MapHandler;
 import Utilities.Minimap;
 import Utilities.MinimapHandler;
 import Utilities.RandomMapMaker;
 
 /* 
  * summary:
  *  - this is the main class to start the game
  * created: 3.9.13
  * author: Clemens Gerstung
  * last edited: 13.9.13 by Clemens Gerstung
  * edited:
  *  - see render methode	
 */
 
 
 public class GeoStrat extends BasicGame {
 	
 	private Input 			input;
 	private RandomMapMaker	rmm;
 	private Map				map;
 	private Minimap			minimap;
 	private MapHandler		mapHandler;
 	private MinimapHandler	minimapHandler;
 	private static int 		frameWidth;
 	private static int 		frameHeight;
 	private int 			GRID_SIZE = 32;
 	
 	public GeoStrat(String title) {
 		super(title);
 	}
 	
 
 	@Override
 	public void init(GameContainer gameContainer) throws SlickException {
 		this.input = gameContainer.getInput();
 		this.map = new Map(frameWidth, frameHeight, GRID_SIZE);
 		this.mapHandler = new MapHandler(input, frameWidth, frameHeight, map.getMAP_WIDTH(), map.getMAP_HEIGHT());
 		this.minimapHandler = new MinimapHandler(input, frameWidth, frameHeight, map.getMAP_WIDTH(), map.getMAP_HEIGHT());
 		this.rmm = new RandomMapMaker(map.getMAP_WIDTH(), map.getMAP_HEIGHT(), GRID_SIZE);
 		map.initMap(rmm.generateMap());
 		this.minimap = new Minimap(map.getWorldEntities(), frameWidth, frameHeight, map.getMAP_WIDTH(), map.getMAP_HEIGHT());
 	}
 
 	@Override
 	public void update(GameContainer gameContainer, int delta) throws SlickException {
 		
 		//generate new map - press 'R' 	!!REMOVE BEFORE BETA STARTS!!
 		if(input.isKeyPressed(Input.KEY_R)) {
 			try{
 			map.clearMap();
 			map.initMap(rmm.generateMap());
 			} catch (Exception e){e.printStackTrace();}
 		}
 		
 		//activate minimap
 		if(input.isKeyPressed(Input.KEY_M)) {
 			if(minimap.isVisible()) {
 				minimap.setVisibility(false);
 			}
 			else {
 				minimap.setVisibility(true);
 			}
 		}
 		
 		//map movement
 		mapHandler.updateMap(delta);
 		
 		//minimap movement
		if(minimap.isVisible()) minimapHandler.updateMap(delta);
 		
 	}
 	
 	@Override
 	public void render(GameContainer gameContainer, Graphics g) throws SlickException {
 		
 		//map
 		map.render(g);
 		
 		//minimap
 		minimap.render(g);
 		
 	}
 
 
 	public static void main(String[] args) throws SlickException {
 //		System.setProperty("org.lwjgl.opengl.Window.undecorated", "true");
 		AppGameContainer towerDefense = new AppGameContainer(new GeoStrat("GeoStrat 0.0.1"));
 		frameWidth = towerDefense.getScreenWidth();
 		frameHeight = towerDefense.getScreenHeight();
 //		frameSizeX = 800;
 //		frameSizeY = 800;
 		towerDefense.setDisplayMode(frameWidth, frameHeight, true);
 		towerDefense.setAlwaysRender(true);
 		towerDefense.setVSync(true);
 		towerDefense.setShowFPS(true);
 		towerDefense.start();
 	}
 
 }

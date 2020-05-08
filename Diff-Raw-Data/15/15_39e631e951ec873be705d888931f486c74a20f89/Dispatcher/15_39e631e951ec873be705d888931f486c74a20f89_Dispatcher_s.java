 package dispatcher;
 
 import java.awt.Color;
 import java.awt.image.BufferedImage;
 import java.awt.image.Raster;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import javax.imageio.ImageIO;
 
 import playable.Player;
 import playable.TypeOfPlayer;
 import renderer.Renderer;
 import renderer.sprite.BaseSprite;
 import renderer.sprite.SelectedSprite;
 import renderer.sprite.TowerSprite;
 import engine.Base;
 import engine.Engine;
 import engine.tower.Tower;
 
 /**
  * This class represents the link between the engine, the renderer, and the IHM of the player.
  * @author Yuki
  *
  */
 public class Dispatcher {
 	private static final int FRAME_RATE =  60;
 	private static final int MILLISECOND_PER_FRAME =  1000 / FRAME_RATE;
 	private static long nbFrame = 0;
 	
 	private static final int MAP_SCALE =  5;
 	
 	private static int currentLevel = 0;
 	
 	private static final Engine Engine = new Engine();
 	private static final Renderer Renderer = new Renderer("Nano WAAAARS!!!");
 	private static final HashMap<String, Player> Players = new HashMap<String, Player>();
 		
 	/**
 	 * This method loads the map from a datamap image.
 	 * To make the map (from Photoshop for example) :
 	 * 	- blue[50, 150], red[0], green[0] => a base for the player
 	 * 	- red[50, 150], blue[0], green[0] => a base for the IA_1
 	 * 	- green[50, 150], blue[0], red[0] => a base for the IA_2
 	 * 	- white[50, 150], blue[50, 150], red[50, 150] => a neutral base
 	 * 
 	 * 	- blue [200], red [200], green [200] => a tower
 	 * 
 	 * 	- blue [255], red [255], green [255] => the gold base
 	 * 
 	 * @param filepath path of the datamap grey-scale image
 	 * @throws IOException
 	 */
 	public static void loadMap(String filepath) throws IOException{
 		BufferedImage map = ImageIO.read(new File(filepath));
 		Raster mapData = map.getData();
 		
 		//For each pixels, create the bases
 		for(int y=0;y<map.getHeight();++y){
 			for(int x=0;x<map.getWidth();++x){
 				Color color = new Color(map.getRGB(x, y));
 				//if the pixel is not black
 				if(color.getRed() > 0 || color.getBlue() > 0 || color.getGreen() > 0){
 					//blue [20, 150] => a base for the player
 					if(color.getBlue() >= 20 && color.getBlue() <= 150 && color.getRed() == 0 && color.getGreen() == 0){
 						if(!Players.containsKey("Player"))
 							Players.put("Player", new Player("You", TypeOfPlayer.PLAYER));
 						float pixelBlue = mapData.getSampleFloat(x, y, 2);
 						Base newBase = new Base(MAP_SCALE*x, MAP_SCALE*y, (int)(Base.MAX_CAPACITY*(pixelBlue/150.)), Players.get("Player"));
 						newBase.setId(Renderer.addBaseSprite(newBase));
 						Engine.addBase(newBase);
 					}
 					//red [20, 150] => a base for the IA_1
 					else if(color.getRed() >= 20  && color.getRed() <= 150 && color.getBlue() == 0 && color.getGreen() == 0){
 						if(!Players.containsKey("IA_1"))
 							Players.put("IA_1", new Player("Jean Vilain", TypeOfPlayer.IA_1));
 						float pixelRed = mapData.getSampleFloat(x, y, 0);
 						Base newBase = new Base(MAP_SCALE*x, MAP_SCALE*y, (int)(Base.MAX_CAPACITY*(pixelRed/150.)), Players.get("IA_1"));
 						newBase.setId(Renderer.addBaseSprite(newBase));
 						Engine.addBase(newBase);
 					}
 					//green [20, 150] => a base for the IA_2
 					else if(color.getGreen() >= 20  && color.getGreen() <= 150 && color.getBlue() == 0 && color.getRed() == 0){
 						if(!Players.containsKey("IA_2"))
 							Players.put("IA_2", new Player("Mr Smith", TypeOfPlayer.IA_2));
 						float pixelGreen = mapData.getSampleFloat(x, y, 1);
 						Base newBase = new Base(MAP_SCALE*x, MAP_SCALE*y, (int)(Base.MAX_CAPACITY*(pixelGreen/150.)), Players.get("IA_2"));
 						newBase.setId(Renderer.addBaseSprite(newBase));
 						Engine.addBase(newBase);
 					}
 					//white [20, 150] => a neutral base
 					else if(color.getRed() >= 20  && color.getRed() <= 150 && color.getBlue() >= 20  && color.getBlue() <= 150 && color.getGreen() >= 20  && color.getGreen() <= 150){
 						float pixelRed = mapData.getSampleFloat(x, y, 0); 
 						Base newBase = new Base(MAP_SCALE*x, MAP_SCALE*y, (int)(Base.MAX_CAPACITY*(pixelRed/150.)), null);
 						newBase.setId(Renderer.addBaseSprite(newBase));
 						Engine.addBase(newBase);
 					}
 				}
 			}
 		}
 		//For each pixel, create the towers
 		for(int y=0;y<map.getHeight();++y){
 			for(int x=0;x<map.getWidth();++x){
 				Color color = new Color(map.getRGB(x, y));
 				//if the pixel is not black
 				if(color.getRed() > 0 || color.getBlue() > 0 || color.getGreen() > 0){
 					//blue [200], red [200], green [200] => a tower
 					if(color.getBlue() == 200 && color.getRed() == 200 && color.getGreen() == 200){
 						Tower newTower = new Tower(MAP_SCALE*x, MAP_SCALE*y);
 						newTower.setId(Renderer.addTowerSprite(newTower));
 						Engine.addTower(newTower);
 					}
 				}
 			}
 		}
 	}
 	
 	/**
 	 * @param args input arguments
 	 * @throws IOException 
 	 */
 	public static void main(String[] args){		
 		//init the renderer
 		try {
 			Renderer.init();
 		} catch (IOException e) {
 			e.printStackTrace();
 			System.exit(0);
 		}
 
 		//display the renderer
 		Renderer.render();
 		
 		//start the thread
 		boolean endOfTheWholeGame = false;
 		while(!endOfTheWholeGame){
 			/////////////////////////////
 			//the menu : choose a level//
 			/////////////////////////////
 			if(Renderer.getLvlSelected() == null){
 				try {
 					Renderer.addLvlsToTheMenu();
 				} catch (IOException e) {
 					e.printStackTrace();
 					System.exit(0);
 				}
 				Renderer.displayMenu();
 				while(Renderer.getLvlSelected() == null){}
 				Dispatcher.currentLevel = Renderer.getLvlSelected().getNumLvl();
 				Renderer.hideMenu();
 				Renderer.setBackgroundImage();
 			}
 
 			/////////////////////////////
 			////////load the map/////////
 			/////////////////////////////
 			try {
 				Dispatcher.loadMap(Renderer.getLvlSelected().getPathOfTheLevel());
 				Renderer.addPlayerSprites(Players);
 				Renderer.resetLvlSelected();
 			} catch (IOException e) {
 				e.printStackTrace();
 				System.exit(0);
 			}
 
 			////////////////////////////////////
 			//start the thread of the players///
 			////////////////////////////////////
 			Dispatcher.startThreadOfPlayers();
 	
 			/////////////////////////////
 			///////start a game//////////
 			/////////////////////////////
 			ArrayList<Integer> idOfElementsDeleted = new ArrayList<Integer>();
 			boolean endOfAGame = false;
 			//=>what we have to do in each frame
 			while(!endOfAGame) {	
 				
 				long begin = System.currentTimeMillis();
 				
 				Dispatcher.nbFrame = Dispatcher.nbFrame + 1;
 				
 				//work of the engine
 				idOfElementsDeleted = Engine.doATurnGame();
 				//work of the renderer
 				Renderer.refreshView(idOfElementsDeleted);
 				
 				//work of the dispatcher : manage interactions between players and the engine
 				//create units
 				if(SelectedSprite.isThereAtLeastOneStartingElement() && SelectedSprite.isThereAnEndingElement()) {
 					if(!Renderer.isChoosingUnit()){
 						for(Base b:BaseSprite.getStartingBases()){
 							double nbAgentsOfUnitSent = b.getNbAgents() * Renderer.getUnitPercentChosen(); 
 							if(nbAgentsOfUnitSent == b.getNbAgents()){
 								nbAgentsOfUnitSent -= 1;
 							}
 							b.sendUnit(nbAgentsOfUnitSent, SelectedSprite.getEndingElement());
 						}
 						SelectedSprite.resetEndingElement();
 					}
 				}
 				//build specialized tower
 				if(TowerSprite.isThereOneTowerToBuild()){
 					if(Renderer.isTowerTypeChosen()){
 						Tower specTower = Engine.specializeTower(TowerSprite.getChosenTowerType(), TowerSprite.getTowerToBuild().getEngineTower());
 						Renderer.updateTowerSprite(specTower, TowerSprite.getTowerToBuild().getEngineTower().getId());
 						TowerSprite.resetTowerToBuild();
 					}
 				}
 				//check if there is a winner
 				if(Dispatcher.theWinner() != null){
 					if(Dispatcher.theWinner().isPlayer()){
 						Renderer.displayWinner(Dispatcher.currentLevel);
 					}
 					else{
 						Renderer.displayLoser(Dispatcher.currentLevel);
 					}
 					try {
 						Thread.sleep(5000);
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 						System.exit(0);
 					}
 					Player.flagThread = false;
 					endOfAGame = true;
 				}
 				
 				//if the loop is too fast, we need to wait 
 				long end = System.currentTimeMillis();
 				if ((end - begin) < Dispatcher.MILLISECOND_PER_FRAME) {
 					try {
 						Thread.sleep(Dispatcher.MILLISECOND_PER_FRAME - (end - begin));
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 						System.exit(0);
 					}
 				}
 			}
 			/////////////////////////////
 			///////reset the game////////
 			/////////////////////////////
 			Dispatcher.resetTheGame();
 		}
 	}
 
 	/**
 	 * This function starts the thread of each player concerned.
 	 */
 	private static void startThreadOfPlayers() {
 		if(Players.get("Player").getState() == Thread.State.NEW)
 			Players.get("Player").start();
 		
 		if(Players.get("IA_1").getState() == Thread.State.NEW)
 			Players.get("IA_1").start();
 		
 		if(Players.size() == 3){
 			if(Players.get("IA_2").getState() == Thread.State.NEW)
 				Players.get("IA_2").start();
 		}
 	}
 
 	/**
 	 * Return the winner of the game (or null if there is no winner yet).
 	 * Note : we don't care who is the winner if it's not the player : we return "IA_1" in that case.
 	 * @return Player : the winner if there is one, or null if not
 	 */
 	public static Player theWinner(){
 		if(Players.size() == 2){
 			if(Players.get("Player").isAlive() && !Players.get("IA_1").isAlive())
 				return Players.get("Player");
 			else if(!Players.get("Player").isAlive() && Players.get("IA_1").isAlive())
 				return Players.get("IA_1");
 			else
 				return null;
 		}
 		else if(Players.size() == 3){
 			if(Players.get("Player").isAlive() && !Players.get("IA_1").isAlive() && !Players.get("IA_2").isAlive())
 				return Players.get("Player");
 			else if(!Players.get("Player").isAlive() && (Players.get("IA_1").isAlive() || Players.get("IA_2").isAlive()))
 				return Players.get("IA_1");
 			else
 				return null;
 		}
 		else
 			return null;
 	}
 	
 	/**
 	 * Reset all the data of the game, in order to restart a new game (from a different map for example) properly.
 	 */
 	private static void resetTheGame() {
 		Renderer.resetTheGame(Dispatcher.theWinner());
 		Engine.resetTheGame();
 		Player.resetFlagThread();
 		Players.clear();
 	}
 	
 	// GETTERS & SETTERS
 	
 	public static Engine getEngine() {
 		return Dispatcher.Engine;
 	}
 	
 	public static Renderer getRenderer() {
 		return Dispatcher.Renderer;
 	}
 	
 	public static HashMap<String, Player> getPlayers() {
 		return Dispatcher.Players;
 	}
 	
 	public static int getCurrentLevel(){
 		return Dispatcher.currentLevel;
 	}
 }

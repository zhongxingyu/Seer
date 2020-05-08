 package game.gameplayStates;
 
 import game.Dialogue;
 import game.Enemy;
 import game.GameObject;
 import game.PauseMenu;
 import game.StateManager;
 import game.StaticObject;
 import game.gameplayStates.GamePlayState.simpleMap;
 import game.interactables.Bed;
 import game.interactables.Chest;
 import game.interactables.ChickenWing;
 import game.interactables.Cigarette;
 import game.interactables.Door;
 import game.interactables.Interactable;
 import game.interactables.PortalObject;
 import game.player.Player;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.StateBasedGame;
 import org.newdawn.slick.tiled.TiledMap;
 
 public class Home extends GamePlayState {
 	
 	private int m_previousDreamState;
 
 	public Home(int stateID) {
 		m_stateID = stateID;
 	}
 	
 	
 	@Override
 	public void additionalInit(GameContainer container, StateBasedGame stateManager)
 			throws SlickException {
 		
 		m_previousDreamState = StateManager.m_dreamState;
 		// set player initial location
 		m_playerX = SIZE*2;
 		m_playerY = SIZE*4;
 		
 		// set up map
 		this.m_tiledMap = new TiledMap("assets/maps/home.tmx");
 		this.m_map = new simpleMap();	
 		this.setBlockedTiles();
 
 		// set up objects that will not change regardless of the game state
 		if(!this.isLoaded()) {
 			StaticObject posters = 
 				new StaticObject("posters", 3*SIZE, 1*SIZE, "assets/gameObjects/posters.png");
 			this.addObject(posters, false);
 			
 			StaticObject carpet = 
 				new StaticObject("carpet", 3*SIZE, 3*SIZE, "assets/gameObjects/carpet.png");
 			this.addObject(carpet, false);
 			
 			StaticObject bedTable = 
 				new StaticObject("bedTable", 4*SIZE, 4*SIZE, "assets/gameObjects/bedTable.png");
 			bedTable.setRenderPriority(true);
 			this.addObject(bedTable, false);
 			m_blocked[4][4] = true;
 			
 			StaticObject table =
 				new StaticObject("table", SIZE, 4*SIZE, "assets/gameObjects/table.png");
 
 			m_blocked[1][4] = true;
 			m_blocked[1][5] = true;
 			this.addObject(table, true);
 		
 		}
 
 	}
 	
 	@Override
 	public void setupObjects(int city, int dream) throws SlickException {
 		if (city == 3 && dream == 3) {
 			super.removeObject("door");
 			StaticObject door = new StaticObject("door", 2*SIZE, 2*SIZE, "assets/gameObjects/door.png");
 			door.setDialogue(new String[] {"Its late out... Perhaps you should just hit the sack"});
 			this.addObject(door, true);
 			
 			super.removeObject("bed");
 			Bed bed = new Bed("bed", 3*SIZE, 5*SIZE, StateManager.TOWN_NIGHT_STATE, -1, -1);
 			m_blocked[3][5] = true;
 			m_blocked[4][5] = true;
 			this.addObject(bed, true);
 
 		}
 		else if (city == 3 && dream == 2) {
 			super.removeObject("door");
 			PortalObject door = new Door("door", 2*SIZE, 2*SIZE, StateManager.TOWN_DAY_STATE, 11, 28);
 			this.addObject(door, true);
 			
 			super.removeObject("bed");
 			StaticObject bed = new StaticObject("bed", 3*SIZE, 5*SIZE, "assets/gameObjects/bed.png");
 			bed.setDialogue(new String[] {"But you just got up..."});
 			this.addObject(bed, true);
 			m_blocked[3][5] = true;
 			m_blocked[4][5] = true;
 
 		}
 		
 		else if (city == 2 && dream == 2) {
 			super.removeObject("door");
 			StaticObject door = new StaticObject("door", 2*SIZE, 2*SIZE, "assets/gameObjects/door.png");
 			door.setDialogue(new String[] {"It's late out... Perhaps you should just hit the sack."});
 			this.addObject(door, true);
 			
 			super.removeObject("bed");
 			Bed bed = new Bed("bed", 3*SIZE, 5*SIZE, StateManager.TOWN_NIGHT_STATE, -1, -1);
 			m_blocked[3][5] = true;
 			m_blocked[4][5] = true;
 
 		}
 	}
 	
 	@Override
 	public void setupDialogue(GameContainer container, int city, int dream) {
 		if (city == 3 && dream == 3) {
 			((StaticObject)this.getInteractable("door")).setDialogue(new String[]
 					{"Its late out... Perhaps you should just hit the sack"});
 			
 			((StaticObject)this.getInteractable("table")).setDialogue(new String[]
 					{"1. This your macbook, a safe place to visit your collection of non-moving horses.",
 					"You can also visit find plenty of friends right here on the internet.. special friends."});	
 		}
 		else if (city == 3 && dream == 2) {
 			((StaticObject)this.getInteractable("table")).setDialogue(new String[]
 					{"2. Woah... that horse is indeed better than a boy.", "maybe i'll buy one"});
 		}
 		else if(city == 2 && dream == 2) {
 			
 		}
 	}
 
 
 
 	@Override
 	public void dialogueListener(Interactable i) {
 		
 	}
 	
 	@Override
 	public void additionalEnter(GameContainer container, StateBasedGame stateManager) {
 		if(StateManager.m_dreamState==2 && StateManager.m_dreamState != this.m_previousDreamState) {
 			this.displayDialogue(new String[] {"You wake up. What a strange dream.",
 						"It seems that the strange humanoid escaped into the zoo. " + 
 						"Perhaps you could somehow block off the zoo, so it won't escape there next time..."});
 			this.m_previousDreamState = StateManager.m_dreamState;
 		}
 	}
 
 }

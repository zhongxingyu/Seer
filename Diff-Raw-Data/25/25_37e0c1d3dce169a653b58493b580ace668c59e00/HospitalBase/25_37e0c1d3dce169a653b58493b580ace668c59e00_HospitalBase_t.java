 package game.gameplayStates;
 
 import java.awt.Toolkit;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import game.StateManager;
 import game.StaticObject;
 import game.gameplayStates.GamePlayState.simpleMap;
 import game.interactables.Door;
 import game.interactables.Holder;
 import game.interactables.Interactable;
 import game.interactables.InvisiblePortal;
 import game.interactables.PortalObject;
 import game.popup.CloseFrame;
 import game.popup.MainFrame;
 
 import org.newdawn.slick.AppGameContainer;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.StateBasedGame;
 import org.newdawn.slick.state.transition.FadeInTransition;
 import org.newdawn.slick.state.transition.FadeOutTransition;
 import org.newdawn.slick.tiled.TiledMap;
 
 public class HospitalBase extends GamePlayState {
 	
 	private AppGameContainer m_container;
 	private boolean m_firstTimeEnter = true;
 
 	public HospitalBase(int stateID) {
 		m_stateID = stateID;
 	}
 
 	@Override
 	public void additionalEnter(GameContainer container, StateBasedGame stateManager) {
 		m_container = (AppGameContainer) container;
 		try {
 			m_container.setDisplayMode(192, 192, false);
 			m_camera.refreshCamera(m_container, m_player);
 			m_player.getInventory().setMini(true);
 			this.setMini(true);
 		} catch (SlickException e) {
 			e.printStackTrace();
 		}
 		
 		if (m_firstTimeEnter) {
 			WindowThread thread = new WindowThread();
 			thread.setSize(new int[] {479,291});
 			thread.setPath("assets/hospitalMeme.png");
 			thread.run();
 		}
 		m_firstTimeEnter = false;
 	}
 	
 	
 	class WindowThread implements Runnable {
 		private String m_path;
 		private java.awt.Dimension m_screenSize;
 		private int[] m_size = new int[2];
 		
 		public void setSize(int[] size) {
 			m_size[0] = size[0];
 			m_size[1] = size[1];
 		}
 		public void setPath(String path) {
 			m_path = path;
 			m_screenSize = Toolkit.getDefaultToolkit().getScreenSize();
 		}
 		public void run() {
 			try {
 				int randX = (int)(Math.random()*(m_screenSize.getWidth()-m_size[0]));
 				int randY = (int)(Math.random()*(m_screenSize.getHeight()-m_size[1]));
 				CloseFrame frame = new CloseFrame(randX, randY, m_size[0], m_size[1], m_path);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		
 	}
 	
 	@Override
 	public void additionalInit(GameContainer container, StateBasedGame stageManager) 
 		throws SlickException {
 		
 		m_playerX = 6*SIZE;
 		m_playerY = 20*SIZE;
 		
 		m_flash = new Image("assets/redflash.png");
 		this.m_tiledMap = new TiledMap("assets/maps/hospitalBase.tmx");
 		this.m_map = new simpleMap();
 		this.setBlockedTiles();
 		
 		if (!this.isLoaded()) {
 			StaticObject doorMat = new StaticObject("hospitalBaseDoorMat", 6*SIZE, 20*SIZE, "assets/gameObjects/doormat.png");
 			InvisiblePortal exit = new InvisiblePortal("hospitalBaseEntrance", 6*SIZE, 21*SIZE, StateManager.HOSPITAL_MAZE_STATE, 10, 1);
 			addObject(exit, true);
 			
 			StaticObject puzzleFloor = new StaticObject("puzzleFloor", 5*SIZE, 2*SIZE, "assets/pillOnHorse/hMiddle.png");
 			this.addObject(puzzleFloor, false);
 			
 			Holder topLeft = new Holder("topLeft_holder", 4*SIZE, 1*SIZE);
 			this.addObject(topLeft, true);
 			m_blocked[4][1] = true;
 			Holder topMiddle = new Holder("topMiddle_holder", 6*SIZE, 1*SIZE);
 			this.addObject(topMiddle, true);
 			m_blocked[6][1] = true;
 			Holder topRight = new Holder("topRight_holder", 8*SIZE, 1*SIZE);
 			this.addObject(topRight, true);
 			m_blocked[8][1] = true;
 			Holder middleLeft = new Holder("middleLeft_holder", 4*SIZE, 3*SIZE);
 			this.addObject(middleLeft, true);
 			m_blocked[4][3] = true;
 			Holder middleRight = new Holder("middleRight_holder", 8*SIZE, 3*SIZE);
 			this.addObject(middleRight, true);
 			m_blocked[8][3] = true;
 			Holder bottomLeft = new Holder("bottomLeft_holder", 4*SIZE, 5*SIZE);
 			this.addObject(bottomLeft, true);
 			m_blocked[4][5] = true;
 			Holder bottomMiddle = new Holder("bottomMiddle_holder", 6*SIZE, 5*SIZE);
 			this.addObject(bottomMiddle, true);
 			m_blocked[6][5] = true;
 			Holder bottomRight = new Holder("bottomRight_holder", 8*SIZE, 5*SIZE);
 			this.addObject(bottomRight, true);
 			m_blocked[8][5] = true;
 		}
 	}
 	
 	@Override
 	public void additionalUpdate(GameContainer container, StateBasedGame stateManager, int delta) {
 		int rand = (int) (Math.random() * 15);
 		m_isFlash = rand == 1 ? true : false;
 	}
 	
 	@Override
 	public void setupObjects(int city, int dream) throws SlickException {
 
 	}
 
 	
 	@Override
 	public void additionalExitScene() {
 		System.out.println("Exiting scene in hospital base");
 		
 		try {
 			m_container.setDisplayMode(600, 600, false);
 			m_camera.refreshCamera(m_container, m_player);
 			m_player.getInventory().setMini(false);
 			this.setMini(false);
 		} catch (SlickException e) {
 			e.printStackTrace();
 		}
 		
 		// TODO: test this. not sure if working
 		try {
 			Thread.sleep(5000);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		
 		int destination = StateManager.TOWN_DAY_STATE;
 		StateManager.getInstance().enterState(destination, 
 				new FadeOutTransition(Color.black, 1000), 
 				new FadeInTransition(Color.black, 1000));
 		GamePlayState destinationState = (GamePlayState)StateManager.getInstance().getState(destination);
		destinationState.setPlayerLocation(8*64, 24*64);
 	}
 	
 	/**
 	 * On leaving, deactivate mini inventory
 	 */
 	@Override
 	public void additionalLeave(GameContainer container, StateBasedGame stateManager) {
 		m_player.getInventory().setMini(false);
 	}
 	
 	@Override
 	public void setupDialogue(GameContainer container, int city, int dream)
 			throws SlickException {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	@Deprecated
 	public void dialogueListener(Interactable i) {
 		// TODO Auto-generated method stub
 
 	}
 
 }

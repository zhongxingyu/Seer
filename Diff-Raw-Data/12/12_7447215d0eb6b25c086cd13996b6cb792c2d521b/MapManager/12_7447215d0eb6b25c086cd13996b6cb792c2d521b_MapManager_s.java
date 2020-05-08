 package edu.gatech.cs2340.ui;
 
 import javax.swing.JPanel;
 
 import edu.gatech.cs2340.data.Map;
 import edu.gatech.cs2340.data.Player;
 import edu.gatech.cs2340.sequencing.WaitedOn;
 import edu.gatech.cs2340.sequencing.Waiter;
 
 /**
  * 
  * @author Stephen Conway
  *
  *
  * Class to handle interactions between the user and the game world during the Turn
  */
 public class MapManager implements WaitedOn, Runnable{
 	private Player player;
 	private Map map;
 	private boolean finished;
 	
 	/**
 	 * Constructor
 	 * @param player
 	 * @param map
 	 */
 	public MapManager(Player player, Map map) {
 		this.player = player;
 		this.map = map;
 		finished = false;
 	}
 	
 	/**
 	 * Method to run the MapManager in a separate thread.
 	 * Finishes when player enters pub.
 	 */
 	public void runAsynchronous() {
 		Thread thread = new Thread(this);
 		thread.start();
 	}
 	
 	/**
 	 * Method to allow user to move around and interact with map
 	 */
 	@Override
 	public void run() {
 		Sprite sprite = new Sprite(player, map);
 		
 		MapRenderer mapRenderer = new MapRenderer(map);
 		TownRenderer townRenderer = new TownRenderer();
 		StoreMenu storeMenu = new StoreMenu(player);
 		
 		JPanel[] panels = {storeMenu, townRenderer, mapRenderer};
 		boolean[] spriteEnabled = {false, true, true};
 		TurnPanel mutex = new TurnPanel(panels, spriteEnabled, sprite);
 		
 		MainGameWindow.getInstance().setMainPanel(mutex);
 		
 		//while player not in pub
 		while (sprite.getLocation() != -1) {
 			mutex.setCurrentPanel(sprite.getLocation());
 			
 			switch (sprite.getLocation()) {
 			case 0: //store
 				runStoreMenu(storeMenu, sprite, mutex);
 				//block until out of store
 				break;
 			case 1: //town
 				sprite.update();
 				mutex.repaint();
 				break;
 			case 2: //map
 				sprite.update();
 				mapRenderer.refreshAll();
 				mutex.repaint();
 				break;
 			}
 			
 			//cycle every 25 ms
			try {
				Thread.sleep(25);
			} 
			catch (InterruptedException e) {
				e.printStackTrace();
				System.exit(0);
			}
 		}
 		finished = true;
 	}
 	
 	/**
 	 * Method to handle the store menu interaction
 	 * 
 	 * @param storeMenu
 	 * @param sprite
 	 */
 	private void runStoreMenu(StoreMenu storeMenu, Sprite sprite, TurnPanel mutex) {
 		storeMenu.refreshMenu();
 		storeMenu.repaint();
 		Waiter.waitOn(storeMenu);
 		storeMenu.reset();
 		mutex.repaint();
 		sprite.setPosition(4700, 2500);
 		mutex.setCurrentPanel(1);
 		sprite.update();
 	}
 
 	/**
 	 * Process is done when user enters pub
 	 */
 	@Override
 	public boolean isFinished() {
 		return finished;
 	}
 }

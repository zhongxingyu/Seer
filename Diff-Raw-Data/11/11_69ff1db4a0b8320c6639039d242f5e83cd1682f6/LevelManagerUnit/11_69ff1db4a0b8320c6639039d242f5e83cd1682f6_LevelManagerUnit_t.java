 
 package main;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.event.KeyEvent;
 import map.Map;
 import mapobjects.Player;
 import singleplayer.Campaign;
 import singleplayer.WorldMapUnit;
 
 /*
  * loads single-player campaign and updates the current map object
  */
 public class LevelManagerUnit extends GraphicalGameUnit {
 
 //	private Campaign campaign;
 	private Map currentMap;
 //	private WorldMapUnit worldMapUnit;
 	private Player player;
 	
 	public LevelManagerUnit() {
 		initComponent();
 	}
 	
 	@Override
 	public void drawComponent(Graphics g) {	
 		g.setColor(Color.white);
 		g.fillRect(0, 0, GameConstants.FRAME_SIZE_X, GameConstants.FRAME_SIZE_Y);
 		currentMap.drawMap((Graphics2D) g);
 	}
 
 	@Override
 	public void handleKeyPressed(KeyEvent e) {
 		int key = e.getKeyCode();
 		if (key == KeyEvent.VK_ESCAPE) {
 			getNavigator().set(UnitState.BASE_MENU_UNIT);
 		}
 		if (key == KeyEvent.VK_UP) {
 			player.direction.UP.set(true);
 		}
 		
 		if (key == KeyEvent.VK_DOWN) {
 			player.direction.DOWN.set(true);
 		}
 		
 		if (key == KeyEvent.VK_LEFT) {
 			player.direction.LEFT.set(true);
 		}
 		
 		if (key == KeyEvent.VK_RIGHT) {
 			player.direction.RIGHT.set(true);
 		}
 		
 		if (key == KeyEvent.VK_SPACE) {
 			player.layBomb();
 		}
 	}
 
 	@Override
 	public void handleKeyReleased(KeyEvent e) {
 		int key = e.getKeyCode();
 		if (key == KeyEvent.VK_UP) {
 			player.direction.UP.set(false);
 		}
 		
 		if (key == KeyEvent.VK_DOWN) {
 			player.direction.DOWN.set(false);
 		}
 		
 		if (key == KeyEvent.VK_LEFT) {
 			player.direction.LEFT.set(false);
 		}
 		
 		if (key == KeyEvent.VK_RIGHT) {
 			player.direction.RIGHT.set(false);
 		}
 		
 	}
 
 	@Override
 	public void initComponent() {
 		//player = new Player();
 		//campaign = Campaign.readCampaignFromFile("campaign1.txt");
 		//currentMap = campaign.getCurrentMap();	
 		//worldMapUnit = new WorldMapUnit(campaign.getWorldMap());
 		currentMap = new Map("testmap");
 		player = currentMap.getMapPlayer();
		player.direction.UP.set(false);
		player.direction.DOWN.set(false);
		player.direction.LEFT.set(false);
		player.direction.RIGHT.set(false);
 	}
 
 	@Override
 	public void updateComponent() {
 		if (!currentMap.isFinished()) {
 			currentMap.update();
 		} else {
 			getNavigator().set(UnitState.BASE_MENU_UNIT);
 			getNavigator().removeGameUnit(UnitState.LEVEL_MANAGER_UNIT);
 		}	
 	}
 
 }

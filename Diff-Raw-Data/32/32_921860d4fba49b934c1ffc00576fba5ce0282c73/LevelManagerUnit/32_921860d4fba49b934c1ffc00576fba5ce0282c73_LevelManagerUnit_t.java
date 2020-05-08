 
 package main;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.event.KeyEvent;
 import java.awt.image.BufferedImage;
 
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
 	/*
 	 * Erster Test für zentralisierte Map
 	 */
 	 // OffsetVariablen für die zu Zeichnende Map
 	private int mapOffsetX=0;
 	private int mapOffsetY=0;
 	//Booleans wenn map kleiner als berreich bei initalisierung auf true
 	private boolean mapXSmaller = false;
 	private boolean mapYSmaller = false;
 //	private WorldMapUnit worldMapUnit;
 	private Player player;
 	
 	BufferedImage mapCanvas;
 	
 	public LevelManagerUnit() {
 		initComponent();
 	}
 	
 	@Override
 	public void drawComponent(Graphics g) {	
 		g.setColor(Color.black);
 		g.fillRect(0, 0, GameConstants.FRAME_SIZE_X, GameConstants.FRAME_SIZE_Y);
 		
 		/** centering map (EIKS temporary workaround) **/
 		mapCanvas = new BufferedImage(currentMap.getWidth(), currentMap.getHeight(), BufferedImage.TYPE_INT_ARGB);
 		Graphics gMap = mapCanvas.getGraphics();
 		currentMap.drawMap((Graphics2D) gMap);
 		g.drawImage(mapCanvas, this.mapOffsetX, this.mapOffsetY, currentMap.getWidth(), currentMap.getHeight(), null);
 		/*****************************************/
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
 		currentMap = new Map("long");
 		player = currentMap.getMapPlayer();
 		player.direction.UP.set(false);
 		player.direction.DOWN.set(false);
 		player.direction.LEFT.set(false);
 		player.direction.RIGHT.set(false);
 		
 		//Mapini Offseteinrichtungrichtung
 		if(currentMap.getWidth()<GameConstants.FRAME_SIZE_X){
 			mapOffsetX = (GameConstants.FRAME_SIZE_X-currentMap.getWidth())/2;
 			mapXSmaller = true;
 		}else{
 			if(player.getPosX()-50>= GameConstants.FRAME_SIZE_X){
 				if(player.getPosX()>=currentMap.getWidth()-GameConstants.FRAME_SIZE_X){
 					mapOffsetX = -(currentMap.getWidth()-GameConstants.FRAME_SIZE_X);
 				}else{
 					mapOffsetX = -(player.getPosX()-GameConstants.FRAME_SIZE_X/2);
 				}
 			}
 		}
 		
 		if(currentMap.getHeight()<GameConstants.FRAME_SIZE_Y){
 			mapOffsetY = (GameConstants.FRAME_SIZE_Y-currentMap.getHeight())/2;  //wenn Map kleiner offset auf halben leerbereich setzen
 			mapYSmaller = true;
 		}else{
 			if(player.getPosY()-50>= GameConstants.FRAME_SIZE_Y){
 				if(player.getPosY()>=currentMap.getWidth()-GameConstants.FRAME_SIZE_Y){
 					mapOffsetY = -(currentMap.getWidth()-GameConstants.FRAME_SIZE_Y);
 				}else{
 					mapOffsetY = -(player.getPosY()-GameConstants.FRAME_SIZE_Y/2);
 				}
 			}
 		}
 	}
 
 	@Override
 	public void updateComponent() {
 		if (!currentMap.isFinished()) {
 			currentMap.update();
 			updateOffset();
 		} else {
 			getNavigator().set(UnitState.BASE_MENU_UNIT);
 			getNavigator().removeGameUnit(UnitState.LEVEL_MANAGER_UNIT);
 		}	
 	}
 	
 	/*
 	 * sets the Currrent Offset
 	 */
 	public void updateOffset(){
 		if(!mapXSmaller){
 		if(player.getPosX()>GameConstants.FRAME_SIZE_X/2-25){
 			if(player.getPosX()-GameConstants.FRAME_SIZE_X/2+25<currentMap.getWidth()-GameConstants.FRAME_SIZE_X){
 				this.mapOffsetX = -(player.getPosX()-GameConstants.FRAME_SIZE_X/2)-25;
 			}else{
 				this.mapOffsetX= -(currentMap.getWidth()-GameConstants.FRAME_SIZE_X);
 			}
 		}else{
 		   this.mapOffsetX=0;
 		}
 		}
 		
 		if(!mapYSmaller){
 		if(player.getPosY()>GameConstants.FRAME_SIZE_Y/2-25){
 			if(player.getPosY()-GameConstants.FRAME_SIZE_Y/2+25<currentMap.getHeight()-GameConstants.FRAME_SIZE_Y){
 				this.mapOffsetY = -(player.getPosY()-GameConstants.FRAME_SIZE_Y/2)-25;
 			}else{
 				this.mapOffsetY= -(currentMap.getHeight()-GameConstants.FRAME_SIZE_Y);
 			}
 		}else{
 		   this.mapOffsetY=0;
 		}
 		}
     }
 
 }

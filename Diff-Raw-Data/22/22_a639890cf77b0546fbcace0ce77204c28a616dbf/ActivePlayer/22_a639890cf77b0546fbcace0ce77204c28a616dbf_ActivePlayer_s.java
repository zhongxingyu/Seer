 package gameData;
 
 import gameView.GameWindow;
 import gameView.ingame.inventory.Inventory;
 import gameView.ingame.menu.DrawableInventory;
 
 import java.awt.Graphics;
 
 import singleton.GameProperties;
 import singleton.SingletonWorker;
 
 public class ActivePlayer extends Player {
 
 	private Inventory inventory = new Inventory(100);
 	private int[] equiped;
 
 	public ActivePlayer(int health, int mana, int strength, int intelligence,
 			int agility, int atk, int def, int magRes, int playerClass,
 			int speed) {
 		super(health, mana, strength, intelligence, agility, atk, def, magRes,
 				playerClass, speed, (GameWindow.getWindowWidth() / 2)-16, (GameWindow
 						.getWindowHeight() / 2)-132, "You");
 	}
 	
 	public double calculateScreenXMovement(long duration){
 //		SingletonWorker.logger().info(">>>" + ((double) duration * (double) this.getXPerSec()) / 1000d);
 		return ((double) duration * (double) this.getXPerSec()) / 1000d;
 	}
 
 	public double calculateScreenYMovement(long duration){
 //		SingletonWorker.logger().info(">>>" + ((double) duration * (double) this.getYPerSec()) / 1000d);
 		return ((double) duration * (double) this.getYPerSec()) / 1000d;
 	}
 	
 	public double getCalculatedMovement(long duration){
 		return ((double) duration) / 4d; //was 4d, now lower for old code!
 	}
 
 	public Inventory getInventory() {
 		if(inventory == null){
 			inventory = new Inventory(0);
 		}
 		return inventory;
 	}
 
 	public void setInventory(Inventory i) {
 		this.inventory = i;
 		this.inventory.changed = true;
 		DrawableInventory di = ((DrawableInventory)(
 				SingletonWorker.gameData()
 				.uiItemMap()
 				.get(GameProperties.MENU_ID_INVENTORY))
 				);
 		if(di != null){
 			di.process(0);
 		}
 		
 	}
 
 	@Override
 	public void draw(Graphics g) {
		g.setFont(SingletonWorker.gameProperties().smallgameFont());
 		g.drawImage(this.getImage(), (int) this.getXPos(), (int) this.getYPos(), null);
 		g.drawString(getName(), (int)this.getXPos(), (int) this.getYPos()-10);
 		g.setFont(SingletonWorker.gameProperties().gameFont());
 		g.drawString(GameProperties.playerx + " | " + GameProperties.playery, 100, 100);
 		g.drawString(SingletonWorker.gameProperties().getPlayerBlockX() + " | " + SingletonWorker.gameProperties().getPlayerBlockY(), 100, 200);
 	}
 	
 	public int[] getEquiped() {
 		return equiped;
 	}
 
 	public void setEquiped(int[] equiped) {
 		this.equiped = equiped;
 	}
 
 }

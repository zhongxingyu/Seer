 package store;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics2D;
 import java.awt.event.KeyEvent;
 import java.awt.image.BufferedImage;
import java.util.HashMap;

 import com.golden.gamedev.object.Sprite;
 import com.golden.gamedev.object.font.SystemFont;
 
import com.google.gson.JsonObject;

 import evented.EventedWrapper;
import gameCharacter.GameCharacter;
 
 import app.RPGame;
 import app.RPGame.Pausable;
 import inventory.Inventory;
 import inventory.Item;
 
 /**
  * Creates the store with it's own inventory
  * 
  * @author zahavaalston
  * 
  */
 public class ItemStore extends EventedWrapper<Item> {
 	
 	
 	private Inventory myInventory;
 	private boolean storeOpen = true;
 	private RPGame game;
	private JsonObject item;
 	protected int price;
 	private BufferedImage image;
 	int numOptions;
	private GameCharacter character;
 	
 	public ItemStore(StoreManagerNPC manager, RPGame game) {
 		super(manager.getCharacter());
 		this.game = game;
 		myInventory = new Inventory(manager.getCharacter());
 	}
 	
	public void updateStore(ItemStore store){
        numOptions = store.getSize();

	}
	
 	public void update(long elapsedTime) {
 		if (game.keyPressed(KeyEvent.VK_Q)) {
 			game.unPauseGameFor(Pausable.STORE);
 			storeOpen = false;
         }
 		
 	}
 //
 //	public void initResources() {
 //		price = item.get("price").getAsInt();
 //		image = game.getImage(item.get("image").getAsString());
 //		setImage(image);
 //		setLocation(image, 10, 10);
 //		list = new HashMap<String, Item>();
 //
 //	}
 
 	private void drawBoxes(Graphics2D g) {
 		g.setColor(Color.DARK_GRAY);
 		g.drawRect(0, 0, 480, 320);
 		g.fillRect(0, 0, 480, 320);
 		g.setColor(new Color(0));
 		for (int x = 10; x < 360; x += 90) {
 			for (int y = 10; y < 220; y += 70) {
 				g.drawRect(x, y, 80, 60);
 				g.fillRect(x, y, 80, 60);
 			}
 		}
 		for (int x = 10; x < 360; x += 90) {
 			g.drawRect(x, 240, 80, 60);
 			g.fillRect(x, 240, 80, 60);
 
 		}
 		g.drawRect(380, 10, 90, 130);
 		g.fillRect(380, 10, 90, 130);
 		g.drawRect(385, 150, 80, 60);
 		g.fillRect(385, 150, 80, 60);
 		g.drawRect(385, 240, 80, 60);
 		g.fillRect(385, 240, 80, 60);
 
 	}
 
 	public void openStore() {
 		storeOpen = true;
 		game.pauseGameFor(Pausable.STORE);
 	}
 
 	public void render(Graphics2D g) {
 		if (!storeOpen)
 			return;
 		SystemFont font = new SystemFont(new Font("Arial", Font.BOLD, 12),
 				new Color(255, 255, 255));
 		drawBoxes(g);
 		int x = 0;
 		int y = 0;
 		for (Item currentItem : getInventory()) {
 			if (x > 5)
 				break;
 			String currentItemName = currentItem.getName();
 			if (currentItemName == null)
 				continue;
 			if (currentItemName.length() > 12) {
 				currentItemName = currentItemName.substring(0, 12) + "...";
 			}
 			font.drawText(g, currentItemName, SystemFont.LEFT, (x * 80) + 10,
 					(y * 60) + 10, 70, 2, 0);
			Sprite itemSprite = new Sprite(currentItem.getImage(), x * 80 + 10,
					y * 60 + 10);
			itemSprite.render(g);
 			x++;
 			if (x >= 5) {
 				x = 0;
 				y++;
 			}
 		}
 		SystemFont font2 = new SystemFont(new Font("Arial", Font.BOLD, 12),
 				new Color(255, 255, 255));
 		font2.drawText(g, "Inventory", SystemFont.LEFT, 15, 220, 70, 2, 0);
 	}
 	public void setLocation(BufferedImage image, int x, int y){
 		for (; x < 360; x += 90) {
 			setLocation(image, x, 10);
 		}
 		y += 90;
 		
 	}
 	public BufferedImage getImage(){
 		return image;
 	}
 	
 	public void setImage(BufferedImage image2){
 		getImage();
 	}
 	
 	public Inventory getInventory(){
 		return myInventory;
 	}
 //
 //	 public int buy (Item item){
 //	 int price = item.getPrice();
 //	 if (game.keyPressed(KeyEvent.VK_ENTER)){
 //	 character.getInventory().get("money");
 //	 }
 //	 return price;
 //	
 //	 }
 
 }

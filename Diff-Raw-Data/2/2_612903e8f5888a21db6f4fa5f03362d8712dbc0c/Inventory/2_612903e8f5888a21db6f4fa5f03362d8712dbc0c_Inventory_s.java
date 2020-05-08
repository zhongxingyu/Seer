 /**
  * A class that handles the players inventory.
  * 
  * @author Bobby Henley
  * @version 1
  */
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map.Entry;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SpriteSheet;
 
 import com.google.gson.JsonArray;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 
 public class Inventory implements Menu {
 	private int width = 550, height = 400;
 	private int INV_OFFSET_X = width/2 - 30, INV_OFFSET_Y = height/2 - 30;
 	private boolean visible;
 	private HashMap<String, Integer> playerStats;
 	private Player ply;
 	private GameConfig invMenu;
 	
 	
 	public Inventory(Player p) {
 		ply = p;
 		playerStats = p.getStats();
 		invMenu = new GameConfig("./loc/inventorytext.json");
 		
 	}
 
 	@Override
 	public void setVisible(boolean b) {
 		visible = b;
 	}
 	
 	@Override
 	public boolean isOpen() {
 		return visible;
 	}
 
 	@Override
 	public void draw(Graphics g) {
 		if(visible) {
 			g.setColor(Color.black);
 			g.fillRect(ply.getX() - INV_OFFSET_X, ply.getY() - INV_OFFSET_Y, width, height);
 			for(int i = 0; i < 320; i+=32) {
 				for(int j = 0; j < height - 80; j+=32) {
 					g.setColor(Color.white);
 					g.drawRect(i + ply.getX() - INV_OFFSET_X, (j + 80) + ply.getY() - INV_OFFSET_Y, 32, 32);
 				}
 			}
 			
 			for(int x = 0; x < 10; x+=32) {
 				for(int y = 0; y < 10; x+=32) {
					g.drawImage(ply.getPlayerItems().get(x).getItemImage().getScaledCopy(32, 32), x + ply.getX() - INV_OFFSET_X, (y + 80) + ply.getY() - INV_OFFSET_Y);
 				}
 			}
 					
 			
 			// "" + ply.getStat(
 			/*for (Entry<String, JsonElement> ele : invMenu.getObject().entrySet()) {
 				g.drawString(ele.getValue().getAsString().replace("%n", ele.getKey().split("#(.*)")[0]), 50, 50);
 			}*/
 			
 			g.drawString(invMenu.getValueAsString("#title"), 320/2 - 40 + ply.getX() - INV_OFFSET_X, 10 + ply.getY() - INV_OFFSET_Y);
 			g.drawString(invMenu.getValueAsString("#stat"), (width - 130) + ply.getX() - INV_OFFSET_X, 10 + ply.getY() - INV_OFFSET_Y);
 			g.drawRect(ply.getX() - INV_OFFSET_X, ply.getY() - INV_OFFSET_Y, width, height);
 		}
 	}
 }

 package core.playfield.hud;
 
 import java.awt.Graphics2D;
 import java.util.List;
 
 import core.items.CollectibleItem;
 
 import com.golden.gamedev.object.GameFont;
 
 public class InventoryWidget extends HUDWidget {
 	
 	String title;
 	List<CollectibleItem> myInv;
 	InventoryProxy dp;
 
 	public InventoryWidget(String title, InventoryProxy dp) {
 		super(250, 15);
 		this.title = title;
 		this.dp = dp;
 	}
 	
 	@Override
 	public void update(long t) {
 		myInv = dp.get();
 	}
 
 	@Override
 	public void render(Graphics2D g, HUDGroup h) {
 		h.getFont().drawString(g, title + ":", GameFont.LEFT, xPos,
 				yPos, 100);
 		int i = 0;
 		for (CollectibleItem item : myInv) {
 			g.drawImage(item.getImage(), null, xPos + i*15 +70, yPos);
 			i++;
 		}
 	}
 
 }

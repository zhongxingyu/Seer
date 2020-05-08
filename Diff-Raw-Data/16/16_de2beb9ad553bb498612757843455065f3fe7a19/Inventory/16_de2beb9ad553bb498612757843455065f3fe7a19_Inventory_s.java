 import java.util.HashMap;
 import java.util.Map.Entry;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.geom.Rectangle;
 
 import com.google.gson.JsonElement;
 
 /**
  * A class that handles the players inventory.
  * 
  * @author Bobby Henley
  * @version 1
  */
 public class Inventory implements Menu {
 	private int width = 550, height = 400, selectX, selectY = 80;
 	private int INV_OFFSET_X = width/2 - 30, INV_OFFSET_Y = height/2 - 30;
 	private int lastDelta;
 	private long curTime = 0;
 	private Rectangle selectionReticle;
 	private boolean visible;
 	private HashMap<String, Integer> playerStats;
 	private Player ply;
 	private GameConfig invMenu;
 	private ItemDictionary itemDictionary;
 
 	public Inventory(Player p, ItemDictionary id) {
 		ply = p;
 		playerStats = p.getStats();
 		invMenu = new GameConfig("./loc/inventorytext.json");
 		itemDictionary = id;
 		selectionReticle = new Rectangle(selectX, selectY, 25, 25);
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
 			
 			//puts items on top of each other.
			for(int x = 0; x < ply.getPlayerItems().size(); x++) {
 				for(int y = 0; y < ply.getPlayerItems().size(); y++) {
					g.drawImage(itemDictionary.getScaledImageByName(32, ply.getPlayerItems().get(x).getName()), x + ply.getX() - INV_OFFSET_X, (y + 80) + ply.getY() - INV_OFFSET_Y);
 				}
 			}
 				
 			
 			/*int countX = 0;
 			int countY = 0;
 			for(int x = 0; x < ply.getPlayerItems().size(); x++) {
 				g.drawImage(itemDictionary.getScaledImageByName(32, ply.getPlayerItems().get(x).getName()), countX + ply.getX() - INV_OFFSET_X, (countY + 80) + ply.getY() - INV_OFFSET_Y);
 				countX+=32;
 				if(countX >= 10*32) {
 					countX = 0;
 					countY+=32;
 				}
 				
 					//selectX = x*32;
 					//selectY = (x*32) - 80;
 					int tempX = countX -32;
 					int tempY = selectY-80;
 					System.out.println("Counts : (" +tempX+", "+countY+")");
 					System.out.println("Reticle : (" +selectX+", "+selectY+")");
 					if(selectX == countX - 32 && (selectY-80)== countY) {
 						g.drawString(ply.getPlayerItems().get(x).getName(), selectX + ply.getX() - INV_OFFSET_X + 10, selectY + ply.getY() - INV_OFFSET_Y -32);
 					}
 			}*/
 			
 			g.draw(selectionReticle);
 			selectionReticle.setX(selectX + ply.getX() - INV_OFFSET_X + 4);
 			selectionReticle.setY(selectY + ply.getY() - INV_OFFSET_Y + 4);
 
 			// "" + ply.getStat(
 			/*for (Entry<String, JsonElement> ele : invMenu.getObject().entrySet()) {
 				g.drawString(ele.getValue().getAsString().replace("%n", ele.getKey().split("#(.*)")[0]), 50, 50);
 			}*/
 
 			g.drawString(invMenu.getValueAsString("#title"), 320/2 - 40 + ply.getX() - INV_OFFSET_X, 10 + ply.getY() - INV_OFFSET_Y);
 			g.drawString(invMenu.getValueAsString("#stat"), (width - 130) + ply.getX() - INV_OFFSET_X, 10 + ply.getY() - INV_OFFSET_Y);
 			g.drawRect(ply.getX() - INV_OFFSET_X, ply.getY() - INV_OFFSET_Y, width, height);
 		}
 	}
 	
 	public void update(GameContainer container) {
 		if (!visible) {
 			if (container.getInput().isKeyPressed(Input.KEY_I)) {
 				this.setVisible(true);
 				ply.isFrozen(true);
 			}
 		} else {
 			if (container.getTime() - curTime <= 150) { return; }
 			if (container.getInput().isKeyPressed(Input.KEY_I)) {
 				this.setVisible(false);
 				ply.isFrozen(false);
 			} else if (container.getInput().isKeyDown(Input.KEY_UP)) {
 				if(selectY < 80 + 32) { selectY = 80 + 32; }
 				selectY-=32;
 				curTime = container.getTime();
 			} else if (container.getInput().isKeyDown(Input.KEY_DOWN)) {
 				if(selectY > (8*32) + 80) { selectY = (8*32) + 80; }
 				selectY+=32;
 				curTime = container.getTime();
 			} else if (container.getInput().isKeyDown(Input.KEY_LEFT)) {
 				if(selectX < 32) { selectX = 32; }
 				selectX-=32;
 				curTime = container.getTime();
 			} else if (container.getInput().isKeyDown(Input.KEY_RIGHT)) {
 				if(selectX > (8*32)) { selectX = (8*32); }
 				selectX+=32;
 				curTime = container.getTime();
 			}
 		}
 	}
 }

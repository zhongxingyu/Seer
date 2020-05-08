 package se.chalmers.kangaroo.view;
 
 import java.awt.Polygon;
 
 import javax.swing.ImageIcon;
 
 import se.chalmers.kangaroo.constants.Constants;
 import se.chalmers.kangaroo.model.Creature;
 import se.chalmers.kangaroo.model.GameModel;
 import se.chalmers.kangaroo.model.InteractiveObject;
 import se.chalmers.kangaroo.model.Item;
 import se.chalmers.kangaroo.model.Position;
 /**
  * 
  * @author alburgh
  * @modifiedby simonal
  * @modifiedby arvidk
  */
 
 
 public class GameView extends JPanelWithBackground {
 	private GameModel gm;
 
 
 	// private Graphics slickGraphics = new Graphics();
 
 
 	public GameView(String imagepath, GameModel gm) {
 		super(imagepath);
 		this.gm = gm;
 	}
 
 	@Override
 	public void paintComponent(java.awt.Graphics g) {
 		super.paintComponent(g);
 		Position p = gm.getKangaroo().getPosition();
 
 		int drawFrom = getLeftX();
 		int fixPosition = p.getX()/Constants.TILE_SIZE < 16 || drawFrom == gm.getGameMap().getTileWidth() - 33 ? 0 : p.getX()%32;
 		
 			g.drawString(""+gm.getTime(), 10, 10);
 			g.drawString("Deaths: "+ gm.getDeathCount(), 100, 10);
 			/* Render the tiles */
 			for (int y = 0; y < gm.getGameMap().getTileHeight(); y++)
				for (int x = drawFrom; x < drawFrom + 32; x++) {
 					ImageIcon i = new ImageIcon("../gfx/tiles/tile_"
 							+ gm.getGameMap().getTile(x, y).getId() + ".png");
 					i.paintIcon(null, g, (x - drawFrom) * 32-fixPosition, (y-2) * 32);
 				}
 			/* Render the items*/
 			for(int i = 0; i < gm.getGameMap().amountOfItems(); i++){
 				Item item = gm.getGameMap().getItem(i);
 				if(item.getPosition().getX() > drawFrom && item.getPosition().getX() < drawFrom+32){
 					ImageIcon img = new ImageIcon("../gfx/tiles/tile_"+item.getId()+".png");
					img.paintIcon(null, g, (item.getPosition().getX()-drawFrom)*32-fixPosition+32, (item.getPosition().getY()-2)*32);
 				}	
 			}
 			/* Render the interactive objects */
 			for(int i = 0; i < gm.getGameMap().getIObjectSize(); i++){
 				InteractiveObject io = gm.getGameMap().getIObject(i);
 				if(io.getPosition().getX() >drawFrom && io.getPosition().getX() < drawFrom+32){
 					ImageIcon img = new ImageIcon("../gfx/tiles/tile_"+io.getId()+".png");
					img.paintIcon(null, g, (io.getPosition().getX()-drawFrom)*32-fixPosition+32, (io.getPosition().getY()-2)*32);
 				}
 			}
 			/* Render the creatures */
 			for(int i = 0; i < gm.getGameMap().getCreatureSize(); i++){
 				Creature c = gm.getGameMap().getCreatureAt(i);
 				if(c.getPosition().getX() > drawFrom*32 && c.getPosition().getX() < (drawFrom+32)*32){
 					int xP = c.getPosition().getX();
 					int yP = c.getPosition().getY();
					int[] xs = {xP-drawFrom*32-fixPosition+32, xP-drawFrom*32+96-fixPosition, xP-drawFrom*32+96-fixPosition, xP-drawFrom*32-fixPosition+32};
 					int[] ys = {yP-64, yP-64, yP-32, yP-32};
 					g.drawPolygon(xs, ys, 4);
 				}
 			}
 			/* Draw the kangaroo based on where you are */
		if (drawFrom == 0) {
 			int[] xs = {p.getX(), p.getX()+32, p.getX()+32, p.getX()};
 			int[] ys = {p.getY()-64, p.getY()-64, p.getY()-1, p.getY()-1};
 			g.drawPolygon(new Polygon(xs, ys, 4));
		}else if(drawFrom == gm.getGameMap().getTileWidth() - 32){
			int left = (gm.getGameMap().getTileWidth()-32)*32;
			int[] xs = {p.getX()-left, p.getX()+32-left, p.getX()+32-left, p.getX()-left};
 			int[] ys = {p.getY()-64, p.getY()-64, p.getY()-1, p.getY()-1};
 			g.drawPolygon(new Polygon(xs, ys, 4));
 		}else {
 			int[] xs = {16*32, 17*32, 17*32, 16*32};
 			int[] ys = {p.getY()-64, p.getY()-64, p.getY()-1, p.getY()-1};
 			g.drawPolygon(new Polygon(xs, ys, 4));
 		}
 	}
 

 	private int getLeftX() {
 		int kPos = gm.getKangaroo().getPosition().getX() / Constants.TILE_SIZE;
		if (kPos < 17)
 			return 0;
		if (gm.getGameMap().getTileWidth() - kPos < 16)
			return gm.getGameMap().getTileWidth() - 32;
 		return kPos - 16;
 	}
 }

 package se.chalmers.kangaroo.view;
 
 import java.awt.Polygon;
 
 import javax.swing.ImageIcon;
 
 import se.chalmers.kangaroo.constants.Constants;
 import se.chalmers.kangaroo.model.GameModel;
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
 		int fixPosition = p.getX()/Constants.TILE_SIZE < 16 || drawFrom == gm.getGameMap().getTileWidth() - 32 ? 0 : p.getX()%32;
 		for (int y = 0; y < gm.getGameMap().getTileHeight(); y++)
 			for (int x = drawFrom; x < drawFrom + 33; x++) {
 				ImageIcon i = new ImageIcon("../gfx/tiles/tile_"
 						+ gm.getGameMap().getTile(x, y).getId() + ".png");
 				i.paintIcon(null, g, (x - drawFrom) * 32-fixPosition, (y-2) * 32);
 			}
 
 //		 slickGraphics.drawAnimation(gm.getKangaroo().getAnimation(),
 //		 p.getX()-32, p.getY()-32);
 		if (drawFrom == 0) {
 //			new ImageIcon("../gfx/kangaroo/kangaroo_58x64_right.png")
 //					.paintIcon(null, g, p.getX(), p.getY());
 			int[] xs = {p.getX(), p.getX()+32, p.getX()+32, p.getX()};
 			int[] ys = {p.getY()-64, p.getY()-64, p.getY()-1, p.getY()-1};
 			g.drawPolygon(new Polygon(xs, ys, 4));
 //			g.drawPolygon(gm.getKangaroo().getPolygon());
 		}else if(drawFrom == gm.getGameMap().getTileWidth() - 32){
 			//g.drawPolygon(); tilesize*amountoftiles-k.poly
 		}else {
 		
 //			new ImageIcon("../gfx/kangaroo/kangaroo_58x64_right.png")
 //					.paintIcon(null, g, 15*32, p.getY());
 			int[] xs = {16*32, 17*32, 17*32, 16*32};
 			int[] ys = {p.getY()-64, p.getY()-64, p.getY()-1, p.getY()-1};
 			g.drawPolygon(new Polygon(xs, ys, 4));
 		}
 	}
 
 
 	private int getLeftX() {
 		int kPos = gm.getKangaroo().getPosition().getX() / Constants.TILE_SIZE;
		if (kPos <= 16)
 			return 0;
 		if (gm.getGameMap().getTileWidth() - kPos < 16)
 			return gm.getGameMap().getTileWidth() - 32;
 		return kPos - 16;
 	}
 }

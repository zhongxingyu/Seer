 package game.client.Map;
 
import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.tiled.TiledMap;
 
 public class Map extends TiledMap{
 
 	public Map(String ref) throws SlickException {
 		super(ref);
 		// TODO Auto-generated constructor stub
 	}
 
 	public void draw(float x, float y, int w, int h) {
 		int cTileX = (int) x / getTileWidth();
 		int cTileY = (int) y / getTileHeight();
		int numTileX = w / getTileWidth() + 2;
		int numTileY = h / getTileHeight() + 2;
		x -= cTileX * getTileWidth();
		y -= cTileY * getTileHeight();
 		for (int layer = 0; layer < getLayerCount();layer++) {
 			for (int iY = 0; iY < numTileY; iY++) {
 				if (iY + cTileY >= 0 && iY + cTileY < getHeight()) {
 					for (int iX = 0; iX < numTileX; iX++) {
 						if (iX + cTileX >= 0 && iX + cTileX  < getWidth()) {
							Image i = getTileImage(iX + cTileX, iY + cTileY, layer);
							if (i != null)
								i.draw(iX*getTileWidth()-x, iY*getTileHeight()-y);
 						}
 					}
 					
 				}
 			}
 		}
 	}
 	
 	
 	
 }

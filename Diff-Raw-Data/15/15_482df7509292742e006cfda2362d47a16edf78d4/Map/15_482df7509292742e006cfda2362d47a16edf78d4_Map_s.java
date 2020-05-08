 package game.client.Map;
 
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
		int numTileX = w / getTileWidth();
		int numTileY = h / getTileHeight();
		x -= cTileX * getTileHeight();
		y -= cTileY * getTileWidth();
 		for (int layer = 0; layer < getLayerCount();layer++) {
 			for (int iY = 0; iY < numTileY; iY++) {
 				if (iY + cTileY >= 0 && iY + cTileY < getHeight()) {
 					for (int iX = 0; iX < numTileX; iX++) {
 						if (iX + cTileX >= 0 && iX + cTileX  < getWidth()) {
							getTileImage(iX + cTileX, iY + cTileY, layer).draw(iX*getTileWidth()-x, y + iY*getTileHeight()-y);
 						}
 					}
 					
 				}
 			}
 		}
 	}
 	
 	
 	
 }

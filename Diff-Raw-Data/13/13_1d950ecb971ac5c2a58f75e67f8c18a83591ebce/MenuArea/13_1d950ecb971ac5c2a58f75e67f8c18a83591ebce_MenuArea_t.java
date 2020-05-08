 package areas;
 
 import java.awt.Image;
 import java.util.List;
 
 import jgame.GContainer;
 import jgame.ImageCache;
 import dtb.Defend;
 
 public class MenuArea extends GContainer {
 	public MenuArea() {
 	
 		//this.setBackgroundColor(Color.BLACK);
 
 		setSize(1280, 720);
 		
 		for (int i = 0; i < 5; i++) {
 		    setTile(i);
 		}
 		
 	}
 	List<Image> tileImages = ImageCache.forClass(Defend.class).getSequential("Wands/wand", 1, 5, ".png");
 	
 	private void setTile(int i) {
 		Tile tile = new Tile(new IndexedTurretRecipe(i), tileImages.get(i)); 
 		tile.setAnchorTopLeft();
 		tile.setScale(1.05);
     	addAtCenter(tile);
     	
     	// tile num times tile width plus half screen width minus number of tiles times tile width devided by 2
     	tile.setX(i * 66 + (640 - ((5 * 66) / 2)));
    	
    	// half screen height minus half tile height minus 20
    	tile.setY(720 / 2 - 66 / 2 - 20 );
 	}
 }

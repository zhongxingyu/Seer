 package lh.koneke.games.lwjglplatformer;
 
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.List;
 
 import lh.koneke.thomas.framework.Game;
 import lh.koneke.thomas.framework.Vector2f;
 import lh.koneke.thomas.graphics.DrawingObject;
 import lh.koneke.thomas.graphics.Frame;
 
 public class Screen {
 	TileSlot[][] map;
 	Vector2f screenSize;
 	Vector2f mapSize;
	private List<TileSlot> activeTiles;
 	Vector2f tileSize;
 	DrawingObject tileSheet;
 	Frame background;
 	
 	public Screen(int w, int h, DrawingObject tileSheet, Vector2f tileSize) {
 		this.tileSheet = tileSheet;
 		this.tileSize = tileSize;
 		
 		mapSize = new Vector2f(w, h);
 		screenSize = mapSize.scale(tileSize.x, tileSize.y);
 		map = new TileSlot[w][h];
 		setActiveTiles(new ArrayList<TileSlot>());
 		
 		for(int x = 0; x < map.length; x++) {
 			for(int y = 0; y < map[x].length; y++) {
 				map[x][y] = new TileSlot();
 				map[x][y].position = new Vector2f(x, y);
 				map[x][y].parent = this;
 			}
 		}
 	}
 	
 	public void setBackground(Frame f) {
 		this.background = f;
 	}
 	
 	public Frame getBackground() {
 		return background;
 	}
 	
 	public DrawingObject getFrame() {
 		return tileSheet;
 	}
 	
 	public TileSlot getAt(Vector2f position) {
 		return map[position.intx()][position.inty()];
 	}
 	
 	public Vector2f getTileSize() {
 		return tileSize;
 	}
 
 	public List<TileSlot> getActiveTiles() {
 		return activeTiles;
 	}
 
 	public void setActiveTiles(List<TileSlot> activeTiles) {
 		this.activeTiles = activeTiles;
 	}
 	
 	public void load(String path) {
 		try {
 			InputStream fis = new FileInputStream(path);
 			BufferedReader br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));
 			
 			/* header */
 			String s = br.readLine();
 			Vector2f mapSize = new Vector2f(Integer.parseInt(s.split(",")[0]), Integer.parseInt(s.split(",")[1]));
 			
 			s = br.readLine();
 			Vector2f tileSize = new Vector2f(Integer.parseInt(s.split(",")[0]), Integer.parseInt(s.split(",")[1]));
 			
 			/* body */
 			while((s = br.readLine()) != null) {
 				String[] parts = s.split(" ");
 				
 				/* part 1, position */
 				String[] split = parts[0].split(",");
 				
 				String positionx = split[0];
 				String positiony = split[1];
 				
 				int xfirst = ((positionx.indexOf('-') != -1) ? Integer.parseInt(positionx.split("-")[0]) : Integer.parseInt(positionx));
 				int xlast = ((positionx.indexOf('-') != -1) ? Integer.parseInt(
 						(positionx.split("-")[1].equals("w") ? Integer.toString((int)mapSize.x-1) : positionx.split("-")[1])
 					) : Integer.parseInt(positionx));
 				
 				int yfirst = ((positiony.indexOf('-') != -1) ? Integer.parseInt(positiony.split("-")[0]) : Integer.parseInt(positiony));
 				int ylast = ((positiony.indexOf('-') != -1) ? Integer.parseInt(
 						(positiony.split("-")[1].equals("h") ? Integer.toString((int)mapSize.y-1) : positiony.split("-")[1])
 					) : Integer.parseInt(positiony));
 				
 				int z = split.length == 3 ? Integer.parseInt(split[0]) : 0;
 				
 				/* part 2, source */
 				split = parts[1].split(",");
 
 				String readx = split[0];
 				String ready = split[1];
 				
 				int sourcexfirst = ((readx.indexOf('-') != -1) ? Integer.parseInt(readx.split("-")[0]) : Integer.parseInt(readx));
 				int sourcexlast = ((readx.indexOf('-') != -1) ? Integer.parseInt(readx.split("-")[1]
 					) : sourcexfirst);
 				int sourceyfirst = ((ready.indexOf('-') != -1) ? Integer.parseInt(ready.split("-")[0]) : Integer.parseInt(ready));
 				int sourceylast = ((ready.indexOf('-') != -1) ? Integer.parseInt(ready.split("-")[1]
 						) : sourceyfirst);
 
 				int sourcex = (sourcexlast-sourcexfirst > 0 ? Game.random.nextInt(sourcexlast-sourcexfirst) : sourcexfirst);
 				int sourcey = (sourceylast-sourceyfirst > 0 ? Game.random.nextInt(sourceylast-sourceyfirst) : sourceyfirst);
 				
 				/* part 3, flips */
 				
 				boolean xflip = false;
 				boolean yflip = false;
 				
 				if(parts.length > 2) {
 					if(parts[2].indexOf('x') != -1) {
 						xflip = true;
 					}
 					if(parts[2].indexOf('y') != -1) {
 						yflip = true;
 					}
 				}
 				
 				for(int x = xfirst; x<=xlast;x++) {
 					for(int y = yfirst; y<=ylast;y++) {
 						map[x][y].addTile(new Tile(new Vector2f(sourcex,sourcey).scale(tileSize.x, tileSize.y), z, xflip, yflip));
 					}
 				}
 			}
 		}
 		catch (FileNotFoundException e) { e.printStackTrace(); }
 		catch (IOException e) { e.printStackTrace(); }
 	}
 }

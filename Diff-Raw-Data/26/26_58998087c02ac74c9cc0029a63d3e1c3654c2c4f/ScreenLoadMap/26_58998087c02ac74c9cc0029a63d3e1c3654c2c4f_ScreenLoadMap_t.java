 package game.screen;
 
 import game.Game;
 import game.Map;
 import game.tile.Tile;
 import game.utils.FileSaver;
 import game.utils.SpriteSheet;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.event.MouseEvent;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 public class ScreenLoadMap extends Screen {
 	private String mapName = "LOAD ERROR", mapDesc = "LOAD ERROR",
 			mapVersion = "LOAD ERROR";
 	private ArrayList<Map> maps = new ArrayList<Map>(), levels = new ArrayList<Map>();
 	private HashMap<Rectangle, Integer> buttons = new HashMap<Rectangle, Integer>();
 	private int selectedMap = 1;
 	private boolean hasLoadedMap = false, showingLevels = false;
 	
 
 	public ScreenLoadMap(int width, int height, SpriteSheet sheet) {
 		super(width, height, sheet);
 	}
 	
 	@Override
 	public void tick()
 	{
 		
 		if(buttons.size() == 0)
 		{
 			System.out.println("Registering buttons...");
 			buttons.put(new Rectangle(431, 452, 300, 52), -1);
 			addButton("modeLevels", new Rectangle(32,540,100,32));
 			addButton("modeCustom", new Rectangle(232,540,100,32));
 			String[] files = new File(FileSaver.getCleanPath() + "\\maps\\").list();
 			int j = 0;
 			for (String s : files) {
 				File tf = new File(s);
 				if (tf.getName().contains(".smf")) {
 					j++;
 					Map m = (Map) FileSaver.loadMapFile(FileSaver.getCleanPath()
 							+ "\\maps\\" + s);
 					if(m.name.startsWith("Level_") && showingLevels)
 					{
 						buttons.put(new Rectangle(41, 4 + (60 * j), 280, 49), j);
 					}
 					else if(!m.name.startsWith("Level_") && !showingLevels)
 					{
 						buttons.put(new Rectangle(41, 4 + (60 * j), 280, 49), j);
 					}
 				}
 			}
 			
 		}
 		
 	}
 
 	@Override
 	public void render(Graphics g) {
 		
 		drawBackgroundScreen();
 		game.getFontRenderer().drawString("Select map to load", 32, 32, 2);
 		
 		//MAP BOX CONTAINER
		ScreenTools.drawButton(32,52,300,game.getHeight()-128," ",g,game); 
 		
 		//LEVEL/CUSTOM SWITCHER
		ScreenTools.drawButton(32,game.getHeight()-50,100,32,"Levels",g,game, new Color(0,0,0,155), showingLevels ? Color.orange : Color.white);
		ScreenTools.drawButton(232,game.getHeight()-50,100,32,"Custom",g,game, new Color(0,0,0,155), !showingLevels ? Color.orange : Color.white);
 
 		//SELECT BUTTON
		ScreenTools.drawButton(431,game.getHeight()-130,300,52,"    >SELECT<",g,game, new Color(0,0,0,155),maps.size() != 0 ? Color.green : Color.red);
 		
 		//MAP PREVIEW BOX
		ScreenTools.drawButton(379,30,game.getWidth()-600,game.getHeight()-400," ", g,game);
 		
 		//MAP DESC BOX
		ScreenTools.drawButton(379,game.getHeight()-300,401,97," ",g,game);
 		
 	
 
 
 		if ((maps.size() == 0 && !showingLevels) || (levels.size() == 0 && showingLevels)) {
 			game.getFontRenderer().drawString(showingLevels ? "Level \nloading \nerror!\nRedownload\nGame." : "No maps", 122, 255, 2,
 					new Color(155, 5, 5));
 
 		} else {
 
 			
			game.getFontRenderer().drawString(mapName, 422, game.getHeight()-270, mapName.length() > 16 ? 1 : 2);
			game.getFontRenderer().drawString(mapVersion, 712, game.getHeight()-270, 1);
			game.getFontRenderer().drawString(mapDesc, 422, game.getHeight()-240, 2);
 			int i = 1;
 			for (Map m : maps) {
 				ScreenTools.drawButton(40, 4+(60*i), 280, 49, m.name.length() > 17 ? m.name.substring(0, 15)+"..." : m.name, g, game, new Color(255,255,255,155), selectedMap == i ? Color.orange : Color.white);
 				if(m.isLocked)
 				{
 					g.setColor(new Color(0, 0, 0, 155));
 					g.fillRect(40, 3 + (60 * i), 282, 51);
 					g.drawImage(game.sheetUI.getImage(8), 155, 10+(60*i),32,32,game);
 				}
 				i++;
 			}
 			mapName = maps.get(selectedMap - 1).name;
 			mapDesc = maps.get(selectedMap - 1).desc;
 			mapVersion = maps.get(selectedMap - 1).version;
 			/*for (int d = 0; d < tiles.keySet().size(); d++) {
 				Rectangle rec = (Rectangle) tiles.keySet().toArray()[d];
 				Tile tile = tiles.get(rec);
 				g.drawImage(game.sheetTiles.getImage(tile.sprite),
 						380 + rec.x / 2, 32 + rec.y / 2, 16, 16, game);
 			}*/
 			Tile[][] tiles = maps.get(selectedMap - 1).tiles;
 			for(int x = 0; x < tiles.length; x++)
 			{
 				for(int y = 0; y < tiles[x].length; y++)
 				{
					g.drawImage(game.sheetTiles.getImage(tiles[x][y].sprite), 380 + x * (16 * game.getWidth()/578), 32 + y * (16 * game.getHeight()/420), 16 * game.getWidth()/578,16 * game.getHeight()/420, game);
 				}
 			}
 		}
 		if (!hasLoadedMap)
 			getMaps();
 	}
 
 	private void getMaps() {
 		String[] files = new File(FileSaver.getCleanPath() + "\\maps\\").list();
 		int j = 0;
 		for (String s : files) {
 			File tf = new File(s);
 			if (tf.getName().contains(".smf")) {
 				j++;
 				Map m = (Map) FileSaver.loadMapFile(FileSaver.getCleanPath()
 						+ "\\maps\\" + s);
 				if(m.isLevel)
 				{
 					// levels.add(m);
 					maps.add(m);
 				}
 				else
 				{
 					maps.add(m);
 				}
 			}
 		}
 		hasLoadedMap = true;
 	}
 
 	@Override
 	public void mousePressed(MouseEvent e) {
 		super.mousePressed(e);
 		Point m = new Point(e.getX(), e.getY());
 		for (int i = 0; i < buttons.keySet().size(); i++) {
 			Rectangle rec = (Rectangle) buttons.keySet().toArray()[i];
 			if (rec.contains(m)) {
 				if (buttons.get(rec) == -1) {
 					System.out.println("[LOADMAP] mousePressed is loading map "+maps.get(selectedMap-1));
 					game.setScreen(showingLevels ? new ScreenWaveMode(w,h,sheet,levels.get(selectedMap-1)) : new ScreenGame(w, h, sheet, maps
 							.get(selectedMap - 1)));
 				} else {
 					selectedMap = buttons.get(rec);
 				}
 				break;
 			}
 		}
 	}
 	
 	@Override
 	public void postAction(String action)
 	{
 		if(action.equals("modeLevels"))showingLevels = true;
 		if(action.equals("modeCustom"))showingLevels = false;
 	}
 }

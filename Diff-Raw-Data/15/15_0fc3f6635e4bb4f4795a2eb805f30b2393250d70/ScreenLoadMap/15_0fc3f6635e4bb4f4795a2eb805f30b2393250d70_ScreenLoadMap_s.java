 package game.screen;
 
 import game.FileSaver;
 import game.Game;
 import game.Map;
 import game.SpriteSheet;
 import game.tile.Tile;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.event.MouseEvent;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 public class ScreenLoadMap extends Screen{
 	private String mapName = "-", mapDesc = "-", mapVersion = "-";
 	private ArrayList<Map> maps = new ArrayList<Map>();
 	private HashMap<Rectangle, Integer>buttons = new HashMap<Rectangle, Integer>();
 	private int selectedMap = 1;
 	private Graphics g;
 	public ScreenLoadMap(int width, int height,Graphics g, SpriteSheet sheet) {
 		super(width, height, sheet);
 		this.g = g;	
 		getMaps();
 	}
 	@Override
 	public void render(Graphics g)
 	{		
 		drawBackgroundScreen();	
 		game.getFontRenderer().drawString("Select map to load", 32, 32, 2);
 		g.setColor(new Color(0,0,0,155));
 		g.fillRect(32, 52, 300, Game.HEIGHT-128);
 		g.setColor(Color.black);
 		g.drawRect(31, 51, 301, Game.HEIGHT-127);
 		
 		g.setColor(new Color(0,0,0,155));
 		g.fillRect(431, 452, 300, 52);
 		g.setColor(Color.green);
 		g.drawRect(430, 451, 301, 52);
 		game.getFontRenderer().drawString(">SELECT<", 514, 469, 2);
 
 		g.setColor(Color.black);
 		g.drawRect(379, 31, 401, 305);
 		
 		
 		
 		if(maps.size() == 0)
 		{
			game.getFontRenderer().drawString("No maps", 422, 355, 2);
			game.getFontRenderer().drawString("Create some or download at", 372, 375, 2);
			game.getFontRenderer().drawString("http://fightthetoast.co.uk", 372, 395, 2);
 		}else{
 		
 			game.getFontRenderer().drawString(mapName, 422, 355, 2);
 			game.getFontRenderer().drawString(mapVersion, 712, 355, 1);
 			game.getFontRenderer().drawString(mapDesc, 422, 375, 2);
 			int i = 1;
 			for(Map m : maps)
 			{	
 				g.setColor(new Color(255,255,255,155));
 				g.fillRect(41, 4+(60*i), 280, 49);
 				g.setColor(selectedMap == i ? Color.orange : Color.white);
 				g.drawRect(40, 3+(60*i), 281,50);
 				game.getFontRenderer().drawString(m.name, 41, 8+(60*i), 2);
 				i++;			
 			}
 		
 			mapName = maps.get(selectedMap-1).name;
 			mapDesc = maps.get(selectedMap-1).desc;
 			mapVersion = maps.get(selectedMap-1).version;
 			HashMap<Rectangle, Tile> tiles = maps.get(selectedMap-1).tiles;
 			for (int d = 0; d < tiles.keySet().size(); d++) {
 				Rectangle rec = (Rectangle) tiles.keySet().toArray()[d];
 				Tile tile = tiles.get(rec);	
 				g.drawImage(game.sheet.getImage(tile.sprite), 380+rec.x/2 ,
 						32+rec.y/2 , 16, 16, game);
 			}
 
 		}
 
 		
 	}	
 	private void getMaps()
 	{
 		buttons.put(new Rectangle(431, 452, 300, 52), -1);
 		String[] files = new File(FileSaver.getCleanPath()+"\\maps\\").list();
 		int j = 0;
 		for(String s : files)
 		{
 			File tf = new File(s);
 			if(tf.getName().contains(".smf"))
 			{
 				j++;
 				maps.add((Map)FileSaver.load(FileSaver.getCleanPath()+"\\maps\\"+s));
 				buttons.put( new Rectangle(41, 4+(60*j), 280, 49), j);			
 			}
 		}
 	}	
 	@Override
 	public void mousePressed(MouseEvent e)
 	{
 		Point m = new Point(e.getX(), e.getY());
 		for (int i = 0; i < buttons.keySet().size(); i++) {
 			Rectangle rec = (Rectangle) buttons.keySet().toArray()[i];
 			if (rec.contains(m)) {
 				if(buttons.get(rec) == -1)
 				{
 					game.setScreen(new ScreenGame(w, h,sheet, g, maps.get(selectedMap-1)));
 				}else
 				{
 					selectedMap = buttons.get(rec);
 				}
 				break;
 			}
 		}
 	}
 }

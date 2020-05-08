 package com.golddigger.view.renderer;
 
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.Rectangle;
 import java.awt.Toolkit;
 import java.net.URL;
 
 import com.golddigger.model.Game;
 import com.golddigger.model.Player;
 import com.golddigger.model.Tile;
 import com.golddigger.model.Unit;
 import com.golddigger.model.tiles.BaseTile;
 import com.golddigger.model.tiles.CityTile;
 import com.golddigger.model.tiles.DeepWaterTile;
 import com.golddigger.model.tiles.ForestTile;
 import com.golddigger.model.tiles.GoldTile;
 import com.golddigger.model.tiles.HillTile;
 import com.golddigger.model.tiles.MountainTile;
 import com.golddigger.model.tiles.RoadTile;
 import com.golddigger.model.tiles.ShallowWaterTile;
 import com.golddigger.model.tiles.TeleportTile;
 import com.golddigger.model.tiles.WallTile;
 import com.golddigger.view.FieldView;
 
 public class HexRenderer implements FieldRenderer {
 	public static final String SQUARE_IMAGE_PATH = "../../../images/hex/";
 
 	private static final double HEX_X_DISTANCE = 3.0/1.85;
 	private static final double HEX_Y_DISTANCE = 2.0;
 	private static final double HEX_R = 21.0;
 	private static final double HEX_H = Math.sqrt(3.0)*HEX_R/2.0;
 
 	private static final Image GOLD0 = loadImage("empty.png");
 	private static final Image GOLD1 = loadImage("gold1.png");
 	private static final Image GOLD2 = loadImage("gold2.png");
 	private static final Image GOLD3 = loadImage("gold3.png");
 	private static final Image GOLD4 = loadImage("gold4.png");
 	private static final Image GOLD5 = loadImage("gold5.png");
 	private static final Image GOLD6 = loadImage("gold6.png");
 	private static final Image GOLD7 = loadImage("gold7.png");
 	private static final Image GOLD8 = loadImage("gold8.png");
 	private static final Image GOLD9 = loadImage("gold9.png");
 	private static final Image WALL_CENTER = loadImage("center.png");
 	private static final Image DIGGER = loadImage("digger.png");
 	private static final Image BANK = loadImage("bank.png");
 	private static final Image CITY = loadImage("city.png");
 	private static final Image DEEP_WATER = loadImage("deep_water.png");
 	private static final Image FOREST = loadImage("forest.png");
 	private static final Image HILL = loadImage("hill.png");
 	private static final Image MOUNTAIN = loadImage("mountain.png");
 	private static final Image ROAD = loadImage("road.png");
 	private static final Image SHALLOW_WATER = loadImage("shallow_water.png");
 	private static final Image TELEPORT= loadImage("teleport.png");
 	private static final Image WALL= loadImage("wall.png");
 	private static final Image SOLID = loadImage("solid.png");
 
 	private static Image[] golds = new Image[]{GOLD0, GOLD1, GOLD2, GOLD3, GOLD4, GOLD5, GOLD6, GOLD7, GOLD8, GOLD9};
 	
 	private Game game;
 	private Player player;
 	private FieldView view;
 	
 	public HexRenderer(FieldView view, Game game, Player player){
 		this.game = game;
 		this.player = player;
 		System.out.println();
 	}
 
 	@Override
 	public void render(Graphics graphics, Rectangle bounds) {
 		Unit playersUnit = game.getUnit(player);
 		
 		//TODO: Fix this initial translation, the x and y values are incorrect
 		int x = playersUnit.getX();
 		x *= HEX_H*HEX_Y_DISTANCE;
 		x += (HEX_H*HEX_Y_DISTANCE)/2;
 		x -= bounds.getHeight()/2;
 		
 		int y = playersUnit.getY();
 		y *= HEX_R*HEX_X_DISTANCE;
 		y += (HEX_R*HEX_X_DISTANCE)/2;
 		y -= bounds.getWidth()/2;
 		
 		graphics.translate(-y, -x);
 		drawBackground(graphics, bounds);
 		draw(graphics, game.getMap().getTiles());
 		for (Unit unit : game.getUnits()){
 			draw(graphics, DIGGER, unit.getX(), unit.getY());
 		}
 		graphics.translate(y, x);
 	}
 	
 	private void drawBackground(Graphics g, Rectangle bounds) {
 		int w = (int) (bounds.width/(HEX_X_DISTANCE*HEX_R));
 		int h = (int) (bounds.height/(HEX_Y_DISTANCE*HEX_H));
		w += bounds.x;
		h += bounds.y;
 		
 		for (int x = -w; x< w; x++){
 			for (int y = -h; y < h; y++){
 				draw(g, WALL, y, x);
 			}
 		}
 	}
 
 	private void draw(Graphics graphics, Tile[][] area){
 		for (int x = 0; x < area.length; x++){
 			for (int y = 0; y < area[x].length; y++) {
 				draw(graphics, getImage(area[x][y]), x, y);
 			}
 		}
 	}
 
 /*	NOTE: the (x,y) are flipped to rotate the screen 90 degrees, that is (x,y) is actually (y,x)
  *	This is due to the maps layout. may be changed to lng/lat at a later date
  *	Brett Wandel
  *	17/8/2012
  */ private void draw(Graphics graphics, Image image, int x, int y){
 		int dx, dy;
 		dx = (int) Math.round(HEX_X_DISTANCE * y * HEX_R);
 		
 		if (y%2 == 0){
 			dy = (int) Math.round((HEX_Y_DISTANCE*x) * HEX_H + (y % 2) * HEX_H + HEX_H);
 		} else {
 			dy = (int) Math.round((HEX_Y_DISTANCE*x) * HEX_H + ((y+1) % 2) * HEX_H);				
 		}
 		graphics.drawImage(image, dx, dy, view);
 	}
 
 	private Image getImage(Tile tile){
 		Image image = null;
 		if (tile instanceof GoldTile){
 			GoldTile gold = (GoldTile) tile;
 			image = golds[gold.getGold()];
 		} else if (tile instanceof BaseTile){
 			image = BANK;
 		}else if (tile instanceof CityTile){
 			image = CITY;
 		} else if (tile instanceof DeepWaterTile){
 			image = DEEP_WATER;
 		} else if (tile instanceof ForestTile){
 			image = FOREST;
 		} else if (tile instanceof HillTile){
 			image = HILL;
 		} else if (tile instanceof MountainTile){
 			image = MOUNTAIN;
 		} else if (tile instanceof RoadTile){
 			image = ROAD;
 		} else if (tile instanceof ShallowWaterTile){
 			image = SHALLOW_WATER;
 		} else if (tile instanceof TeleportTile){
 			image = TELEPORT;
 		} else if (tile instanceof WallTile){
 			image = WALL;
 		}
 		return image;
 	}
 	
 	private static Image loadImage(String name) {
 		URL url = FieldView.class.getResource(SQUARE_IMAGE_PATH+name); 
 		System.err.println(url.getFile());
 		return Toolkit.getDefaultToolkit().getImage(url);
 	}
 }

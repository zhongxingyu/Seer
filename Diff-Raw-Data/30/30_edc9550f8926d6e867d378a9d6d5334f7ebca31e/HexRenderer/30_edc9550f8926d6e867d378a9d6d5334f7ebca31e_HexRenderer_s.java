 package com.golddigger.view.renderer;
 
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.Rectangle;
 import java.awt.Toolkit;
 import java.net.URL;
 
 import com.golddigger.model.Game;
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
 
 	private static Image GOLD0 = loadImage("empty.png");
 	private static Image GOLD1 = loadImage("gold1.png");
 	private static Image GOLD2 = loadImage("gold2.png");
 	private static Image GOLD3 = loadImage("gold3.png");
 	private static Image GOLD4 = loadImage("gold4.png");
 	private static Image GOLD5 = loadImage("gold5.png");
 	private static Image GOLD6 = loadImage("gold6.png");
 	private static Image GOLD7 = loadImage("gold7.png");
 	private static Image GOLD8 = loadImage("gold8.png");
 	private static Image GOLD9 = loadImage("gold9.png");
 	private static Image WALL_CENTER = loadImage("center.png");
 	private static Image DIGGER = loadImage("digger.png");
 	private static Image BANK = loadImage("bank.png");
 	private static Image CITY = loadImage("city.png");
 	private static Image DEEP_WATER = loadImage("deep_water.png");
 	private static Image FOREST = loadImage("forest.png");
 	private static Image HILL = loadImage("hill.png");
 	private static Image MOUNTAIN = loadImage("mountain.png");
 	private static Image ROAD = loadImage("road.png");
 	private static Image SHALLOW_WATER = loadImage("shallow_water.png");
 	private static Image TELEPORT= loadImage("teleport.png");
 	private static Image WALL= loadImage("wall.png");
 
 	private static Image[] golds = new Image[]{GOLD0, GOLD1, GOLD2, GOLD3, GOLD4, GOLD5, GOLD6, GOLD7, GOLD8, GOLD9};
 	
 	private Game game;
 	private Unit unit;
 	private FieldView view;
 	
 	public HexRenderer(FieldView view, Game game, Unit unit){
 		this.game = game;
 		this.unit = unit;
 	}
 
 	@Override
 	public void render(Graphics graphics, Rectangle bounds) {
//		graphics.drawString("Hello World", 10, 10);
 		draw(graphics, game.getMap().getTiles(), 0, 0);
 	}
 	
 	private void draw(Graphics graphics, Tile[][] area, int offsetX, int offsetY){
 		for (int i = 0; i < area.length; i++){
 			for (int j = 0; j < area[i].length; j++) {
				draw(graphics, area[i][j], i, j, offsetX, offsetY);
 			}
 		}
 	}
 	
 	private void draw(Graphics graphics, Tile tile, int i, int j, int offsetX, int offsetY) {
 		int x, y;
 		
 		x = (int) Math.round(HEX_X_DISTANCE * i * HEX_R);
 		
 		if (i%2 == 0){
 			y = (int) Math.round((HEX_Y_DISTANCE*j) * HEX_H + (i % 2) * HEX_H + HEX_H);
 		} else {
 			y = (int) Math.round((HEX_Y_DISTANCE*j) * HEX_H + ((i+1) % 2) * HEX_H);				
 		}
 		
		graphics.drawImage(getImage(tile), x,y, view);
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

 package com.golddigger.view.renderer;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.Rectangle;
 import java.awt.Toolkit;
 import java.net.URL;
 
 import com.golddigger.model.Direction;
 import com.golddigger.model.Game;
 import com.golddigger.model.Map;
 import com.golddigger.model.Point2D;
 import com.golddigger.model.Tile;
 import com.golddigger.model.Unit;
 import com.golddigger.model.tiles.*;
 import com.golddigger.view.FieldView;
 
 public class SquareRenderer implements FieldRenderer {
 	public static final String SQUARE_IMAGE_PATH = "../../../images/square/";
 
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
 	private static Image SOLID= loadImage("solid.png");
 
 	private static Image[] golds = new Image[]{GOLD0, GOLD1, GOLD2, GOLD3, GOLD4, GOLD5, GOLD6, GOLD7, GOLD8, GOLD9};
 
 	private Game game;
 	private Unit unit;
 	private FieldView view;
 	
 	public SquareRenderer(FieldView view, Game game, Unit unit){
 		this.game = game;
 		this.unit = unit;
 		this.view = view;
 	}
 
 	private void render(Graphics graphics, int offsetX, int offsetY){
 		draw(graphics, game.getMap().getTiles(), offsetX, offsetY);
 		draw(graphics, unit, offsetX, offsetY);
 	}
 	
 	@Override
 	public void render(Graphics graphics, Rectangle bounds) {
		int x = 32*unit.getX() + 16;
		int y = 32*unit.getY() + 16;
 		
		x -= bounds.getWidth()/2;
		y -= bounds.getHeight()/2;
		graphics.translate(-x, -y);
		render(graphics, 0, 0);
		graphics.translate(x, y);
 	}
 
 	private void draw(Graphics graphics, Unit unit, int offsetX, int offsetY) {
 		int x = unit.getX()*32 + offsetX;
 		int y = unit.getY()*32 + offsetY;
 		//TODO: Fix image: Image is offset by 4 pixels as its not 32x32
 		graphics.drawImage(DIGGER, x-4, y-4, view);
 	}
 
 	private void draw(Graphics g, Tile[][] area, int offsetX, int offsetY){
 		for (int i = 0; i < area.length; i++){
 			for (int j = 0; j < area[i].length; j++){
 				draw(g, area[i][j], i*32+offsetX, j*32+offsetY);
 			}
 		}
 	}
 
 	private void draw(Graphics g, Tile tile, int x, int y) {
		g.drawImage(getImage(tile), y ,x, view);
 //		g.drawRect(x, y, 32, 32); overlays a black grid
 	}
 
 	private Image getImage(Tile tile){
 		Image image = null;
 		if (tile instanceof GoldTile){
 			GoldTile gold = (GoldTile) tile;
 			image = golds[gold.getGold()];
 		} else if (tile instanceof BaseTile){
 			image = BANK;
 		} else if (tile == null){
 			image = SOLID;
 		} else if (tile instanceof CityTile){
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
 			image = getWallTileImage(game.getMap().getPosition(tile));
 		}
 		return image;
 	}
 
 	private Image getWallTileImage(Point2D position) {
 		Map map = game.getMap();
 		if (map.get(Direction.NORTH.getOffset(position)) instanceof WallTile){
 			
 		}
 		return WALL_CENTER;
 	}
 
 	private static Image loadImage(String name) {
 		URL url = FieldView.class.getResource(SQUARE_IMAGE_PATH+name); 
 		System.err.println(url.getFile());
 		return Toolkit.getDefaultToolkit().getImage(url);
 	}
 }

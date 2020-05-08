 package environment;
 
 import java.awt.Graphics;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 
 public class RoadTile {
 
 	private boolean drawn = false;
 	private boolean north,east,south,west;
 	private RoadTile[] neighbours;
 	private Tiles parent;
 	private int x,y;
 	
 	public RoadTile(boolean north, boolean east, boolean south, boolean west, Tiles parent, int x, int y){
 		this.north = north;
 		this.east = east;
 		this.south = south;
 		this.west = west;
 		neighbours = new RoadTile[4];
 		this.parent = parent;
 		this.x = x;
 		this.y = y;
 	}
 	
 	public void addNeighbour(Direction direction, RoadTile tile){
 		switch(direction){
 		case NORTH:
 			neighbours[0] = tile;
 			break;
 		case EAST:
 			neighbours[1] = tile;
 			break;
 		case SOUTH:
 			neighbours[2] = tile;
 			break;
 		case WEST:
 			neighbours[3] = tile;
 			break;
 		}
 	}
 
 	public void draw(Graphics g, int x, int y, int screenWidth, int screenHeight) {
 		this.setDrawn(true);
 		BufferedImage img = getImage();
 		g.drawImage(img, x - img.getWidth(), y - img.getHeight(), null);
 		RoadTile[] neighbours = getAdjoining();
 		Direction[] entrances = getDirections();
 		for (Direction d : entrances) {
 			switch (d) {
 			case NORTH:
 				if (y - img.getHeight() / 2 < 0) {
 					if (neighbours[0] != null) {
 						neighbours[0].draw(g, x, y-img.getHeight(), screenWidth, screenHeight);
 					} else {
 						parent.createNew(this.x, this.y-1).draw(g, x, y-img.getHeight(), screenWidth, screenHeight);;
 					}
 				}
 				break;
 			case EAST:
 				if (x + img.getWidth() / 2 > screenWidth) {
 					if (neighbours[1] != null) {
 						neighbours[1].draw(g, x+img.getWidth(), y, screenWidth, screenHeight);
 					} else {
 						parent.createNew(this.x+1, this.y).draw(g, x+img.getWidth(), y, screenWidth, screenHeight);
 					}
 				}
 				break;
 			case SOUTH:
 				if (y + img.getHeight() / 2 > screenHeight) {
 					if (neighbours[2] != null) {
 						neighbours[2].draw(g, x, y+img.getHeight(), screenWidth, screenHeight);
 					} else {
 						parent.createNew(this.x, this.y+1).draw(g, x, y+img.getHeight(), screenWidth, screenHeight);
 					}
 				}
 				break;
 			case WEST:
 				if (x - img.getWidth() / 2 < 0) {
 					if (neighbours[3] != null) {
 						neighbours[3].draw(g, x-img.getWidth(), y, screenWidth, screenHeight);
 					} else {
 						parent.createNew(this.x-1, this.y).draw(g, x-img.getWidth(), y, screenWidth, screenHeight);
 					}
 				}
 				break;
 			default:
 				break;
 			}
 		}
 	}
 
 	public BufferedImage getImage(){
 		String filename = "";
 		if(north){
 			filename += "N";
 		}
 		if(east){
 			filename += "E";
 		}
 		if(south){
 			filename += "S";
 		}
 		if(west){
 			filename += "W";
 		}
 		filename += "Road.png";
 		BufferedImage img = null;
 		try{
			img = ImageIO.read(getClass().getResourceAsStream("data/"+filename));
 		}catch(IOException e){
 			e.printStackTrace();
 		}
 		return img;
 	}
 
 	public RoadTile[] getAdjoining(){
 		return neighbours;
 	}
 
 	public Direction[] getDirections(){
 		Direction[] d = new Direction[4];
 		if(north){
 			d[0] = Direction.NORTH;
 		}else{
 			d[0] = null;
 		}
 		if(east){
 			d[1] = Direction.EAST;
 		}else{
 			d[1] = null;
 		}
 		if(west){
 			d[2] = Direction.WEST;
 		}else{
 			d[2] = null;
 		}
 		if(south){
 			d[3] = Direction.SOUTH;
 		}else{
 			d[3] = null;
 		}
 		return d;
 	}
 
 	public void setDrawn(boolean drawn) {
 		this.drawn = drawn;
 	}
 
 	public boolean isDrawn() {
 		return this.drawn;
 	}
 	
 	public boolean hasWall(Direction direction){
 		switch(direction){
 		case NORTH:
 			return getDirections()[0] == null;
 		case EAST:
 			return getDirections()[1] == null;
 		case SOUTH:
 			return getDirections()[2] == null;
 		case WEST:
 			return getDirections()[3] == null;
 		}
 		return false;
 	}
 
 }

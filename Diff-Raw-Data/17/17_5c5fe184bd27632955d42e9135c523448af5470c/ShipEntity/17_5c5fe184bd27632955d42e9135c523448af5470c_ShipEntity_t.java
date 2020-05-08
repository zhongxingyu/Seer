 package edu.bsu.cs222.warVesselsGame;
 
 import java.util.Set;
 
 import org.newdawn.slick.Image;
 import org.newdawn.slick.geom.Rectangle;
 import edu.bsu.cs222.warVessels.Bearing;
 import edu.bsu.cs222.warVessels.Ship;
 import edu.bsu.cs222.warVessels.ShipType;
 
 /**
  * Holds pertinent information for a graphical representation of a ship.
  * 
  * @author Steffan Byrne
  * 
  */
 public class ShipEntity {
 
 	private static final float GRID_INCREMENT = 42;
 	private Image sprite;
 	private int startX;
 	private int x;
 	private int xOffset;
 	
 	private int startY;
 	private int y;
 	private int yOffset;
 
 	private boolean snapped;
 	private Rectangle shipInRect;
 	private Ship ship;
 	private int snapX;
 	private int snapY;
 
 	/**
 	 * Constructs a ship given a ShipType, an image, and initial coordinates.
 	 * 
 	 * @param s
 	 *            The type of ship
 	 * @param sprite
 	 *            The Image to use as a sprite.
 	 * @param x
 	 * @param y
 	 */
 	public ShipEntity(ShipType s, Image sprite, int x, int y) {
 		this.sprite = sprite;
 		startX = x;
 		sprite.setRotation(0);
 		xOffset = sprite.getWidth() / 2;
 		startY = y;
 		yOffset = sprite.getHeight() / 2;
 		resetLocation();
 
 		
 		snapped = false;
 		ship = new Ship(s);
 	}
 
 	/**
 	 * draws the shipEntity centered at x, y.
 	 */
 	public void render() {
 		sprite.drawCentered(x, y);
 		
 		snapX = 0;
 		snapY = 0;
 	}
 
 	public void setX(int x) {
 		this.x = x;
 	}
 
 	public void setY(int y) {
 		this.y = y;
 	}
 
 	/**
 	 * Checks to see if the mouse is in the ShipEntity sprite.
 	 * 
 	 * @param mouseX
 	 * @param mouseY
 	 * @return true if the cursor is in the sprite, false otherwise.
 	 */
 	public boolean isMouseIn(int mouseX, int mouseY) {
 		if ((mouseX >= (x - xOffset) && mouseX <= (x + xOffset))
 				&& (mouseY >= (y - yOffset) && mouseY <= (y + yOffset))) {
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Rotates the ShipEntity 90 degrees
 	 */
 	public void rotate() {
 		sprite.rotate(90);
 		int temp = yOffset;
 		yOffset = xOffset;
 		xOffset = temp;
 
 		ship.changeBearing();
 	}
 	/**
 	 * checks to see if ship is in start position.
 	 * @return boolean true/false
 	 */
 	
 	public boolean isAtStart(){
 		if(x == startX && y == startY){
 			return true;
 		}
 		return false;
 	}
 	
 	/**
 	 *  
 	 * @param set
 	 * 					a list of grid rectangles
 	 * @return whether or not the ship is on the graphical grid.
 	 */
 	public boolean shipOnGrid(Set<Rectangle> set) {
 		snapX = 0;
 		snapY = 0;
 		
 		if(ship.getBearing().equals(Bearing.RIGHT)){
 			snapX = x + xOffset - 10;
 			snapY = y;
 		}else if(ship.getBearing().equals(Bearing.DOWN)){
 			snapX = x;
 			snapY = y + yOffset - 10;
 		}else if(ship.getBearing().equals(Bearing.LEFT)){
 			snapX = x - xOffset + 10;
 			snapY = y;
 		}else{
 			snapX = x;
 			snapY = y - yOffset + 10;
 		}
 
 		for (Rectangle r : set) {
 			if (r.contains(snapX, snapY)) {
 				shipInRect = r;
 				return true;
 			}
 		}
 		return false;
 		
 	}
 	
 	public Rectangle getRectangle(){
 		return shipInRect;
 	}
 	
 	/**
 	 * "snaps" the ship to the graphical grid by setting the sprite's coordinates
 	 * and setting snapped to true
 	 * TODO use this code as a template for displaying AI ships  
 	 */
 	public void snap(){
 		if(ship.getBearing().equals(Bearing.RIGHT) || ship.getBearing().equals(Bearing.DOWN)){
 			x = (int) (shipInRect.getX() - xOffset + GRID_INCREMENT);
 			y = (int) (shipInRect.getY() - yOffset + GRID_INCREMENT);
 		}else{
 			x = (int) (shipInRect.getX() + xOffset);
 			y = (int) (shipInRect.getY() + yOffset);
 		}
 		
 		snapped = true;
 	}
 
 	/**
 	 * Checks to see if the ship is snapped to the grid.
 	 * 
 	 * @return
 	 */
 	public boolean isSnapped() {
 		return snapped;
 	}
 
 	/**
 	 * Unsnaps the ship from the grid.
 	 */
 	public void unsnap() {
 		snapped = false;
 		resetBearing();
 		resetLocation();
 		
 	}
 	
 	/**
 	 * moves ship back to its starting location
 	 */
 	public void resetLocation(){
 		x = startX;
 		y = startY;
 		
 	}
 	
 	
 	/**
 	 * resets the bearing of the ship
 	 */
	private void resetBearing(){
 		switch(ship.getBearing()){
 			case DOWN:
 				ship.resetBearing();
 				sprite.rotate(-90);
 				int temp = yOffset;
 				yOffset = xOffset;
 				xOffset = temp;
 				break;
 			case LEFT:
 				ship.resetBearing();
 				sprite.rotate(180);
 				break;
 			case UP:
 				ship.resetBearing();
 				sprite.rotate(90);
 				int temp2 = yOffset;
 				yOffset = xOffset;
 				xOffset = temp2;
 				break;
		case RIGHT:
			break;
		default:
			break;
 		}	 
 	}
 	
 	public Ship getShipObj(){
 		return ship;
 	}
 }

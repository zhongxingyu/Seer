 package model.items;
 
 import model.geometrical.Position;
 import model.other.WorldObject;
 
 //Empty interface created to group Items
 public class Item implements WorldObject{
 	private Position pos;
 	private final int iconNumber;
 	
	public Item(Position position, int iconNumber) {
 		this.pos=position;
 		this.iconNumber=iconNumber;
 	}
 	
 	
 	@Override
 	public void setX(float x) {
 		this.pos.setX(x);
 	}
 	
 	@Override
 	public float getX() {
 		return this.pos.getX();
 	}
 	
 	@Override
 	public void setY(float y) {
 		this.pos.setY(y);
 	}	
 	
 	@Override
 	public float getY() {
 		return this.pos.getY();
 	}
 	
 	@Override
 	public Position getPosition() {
 		return pos;
 	}
 	
 	@Override
 	public void setPosition(Position p) {
 		this.pos =p;
 	}
 
 	/**
 	 * 
 	 * @return the icon number of the item.
 	 */
 	public int getIconNumber() {
 		return iconNumber;
 	}
 	
 }

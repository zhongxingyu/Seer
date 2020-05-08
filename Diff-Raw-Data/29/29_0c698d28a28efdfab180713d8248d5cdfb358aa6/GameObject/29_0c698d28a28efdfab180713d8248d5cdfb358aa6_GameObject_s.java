 package game;
 
 import java.util.HashMap;
 
 import org.newdawn.slick.Image;
 
 public abstract class GameObject {
 	
 	private Image m_sprite;
 	public static final int SIZE = 64;
 	protected float m_x, m_y;
 	
 	public float getX() {return m_x;}
 	public float getY() {return m_y;}
 	public void setX(float x) { m_x = x; }
 	public void setY(float y) { m_y = y; }
 	public Image getImage() {return getSprite();}	
 	
 //	public abstract int[] getSquare();
 	public abstract Types getType();
 	
 	public Image getSprite() {
 		return m_sprite;
 	}
 	public void setSprite(Image m_sprite) {
 		this.m_sprite = m_sprite;
 	}
 
 	public int[] getSquare() {
 		int[] loc = {(int)m_x/SIZE, (int)m_y/SIZE};
 		return loc;
 	}
 	
 	public enum Types {
 		CHEST,
 		CHICKEN_WING,
 		CIGARETTE,
 		DOOR,
 		DOOR_MAT,
 		BED,
 		STATIC,
 	}
 }

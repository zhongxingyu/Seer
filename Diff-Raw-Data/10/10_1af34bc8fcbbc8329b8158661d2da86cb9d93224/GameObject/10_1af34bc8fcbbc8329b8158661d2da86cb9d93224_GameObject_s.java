 package stijgmachine.jti1a1.nl.objects;
 
 import java.awt.Graphics2D;
 import java.awt.geom.Point2D;
 
 public abstract class GameObject {
 	
 	public static final int BUTTON = 1;
 	public static final int RELATIVE_FROM_TOPRIGHT = 200;
 	public static final int RELATIVE_FROM_TOPLEFT = 201;
 	public static final int RELATIVE_FROM_BOTTOMRIGHT = 202;
 	public static final int RELATIVE_FROM_BOTTOMLEFT = 203;
 	public static final int ABSOLUTE = 204;
 	
 	public int x, y, relativeTo;
 
 	public abstract void draw(Graphics2D g, int height, int width, int x2, int y2);
 	
 	public abstract int getID();
 	
 	public GameObject(int x, int y, int relativeTo)
 	{
 		this.x = x;
 		this.y = y;
 		this.relativeTo = relativeTo;
 	}
 	
 	public int[] getPosition(int x, int y, int x2, int y2, int h, int w, int relativeTo) {
 		int x3 = 0, y3 = 0;
 		switch (relativeTo){
 			case (GameObject.ABSOLUTE):
 				x3 = x;
 				y3 = y;
 				break;
 			case (GameObject.RELATIVE_FROM_BOTTOMLEFT):
 				x3 = x+x2;
 				y3 = h-y+y2;
 				break;
 			case (GameObject.RELATIVE_FROM_BOTTOMRIGHT):
 				x3 = w-x+x2;
 				y3 = h-y+y2;
 				break;
 			case (GameObject.RELATIVE_FROM_TOPLEFT):
 				x3 = x+x2;
 				y3 = y+y2;
 				break;
 			case (GameObject.RELATIVE_FROM_TOPRIGHT):
				x3 = w-x-x2;
				y3 = h-y-y2;
 				break;
 		}
 		return new int[]{x3,y3};
 	}
 
 	public abstract void click();
 }

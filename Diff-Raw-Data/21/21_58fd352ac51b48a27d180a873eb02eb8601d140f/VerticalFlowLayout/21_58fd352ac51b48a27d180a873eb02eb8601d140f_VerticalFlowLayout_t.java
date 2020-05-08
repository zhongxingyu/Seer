 package core.playfield.hud;
 
 import java.awt.Point;
 
 
 public class VerticalFlowLayout extends HUDLayoutManager
 {
 
 	private static final long serialVersionUID = 7979018578539774757L;
 	
 	public final static int LEFT = 0, CENTER = 1, RIGHT = 2;
 	public final static int UPWARD = -1, DOWNWARD = 1;
 	
 	private int alignment;
 	private int direction;
 
 	public VerticalFlowLayout (int dimmX, int dimmY, int direction, int alignment)
     {
         super(dimmX, dimmY);
         this.nextPosition = new Point(0, 0);
         if (direction == UPWARD)
         	this.nextPosition.y = dimmY - 50;
         this.alignment = alignment;
         this.direction = direction;
     }
 
 
     public Point nextWidgetPosition (HUDWidget w)
     {
         Point temp = this.nextPosition;
         this.nextPosition =
             new Point(0, (int) temp.getY() + (w.height * direction));
         if (alignment == CENTER) {
         	temp.x = dimmX/2 - w.width/2;
        } else if (alignment == RIGHT) {
         	temp.x = dimmX - w.width;
         }
         return temp;
     }
 
 }

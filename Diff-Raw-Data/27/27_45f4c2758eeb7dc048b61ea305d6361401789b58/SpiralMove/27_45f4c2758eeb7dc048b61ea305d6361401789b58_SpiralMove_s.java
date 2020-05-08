 import javax.swing.Icon;
 
 
 public class SpiralMove implements MoveBehavior {
 
 	@Override
 	public void move(Canvas c, Sprite s) {
 		Icon icon = s.getCurrentImage();
 
 	    int  iconHeight   = icon.getIconHeight();
 	    int  iconWidth    = icon.getIconWidth();
 	    int  canvasHeight = (int)c.getSize().getHeight();
 	    int  canvasWidth  = (int)c.getSize().getWidth();
 
 	    switch (s.getDirection()) {
 	      case NORTH:
 	        s.setY(s.getY() - 10);
	        if (s.getY() < 0) {
	          s.setY(0);
 	          s.setDirection(Sprite.Direction.WEST);
 	        }
 	        break;
 	      case SOUTH:
 	        s.setY(s.getY() + 10);
	        if (s.getY() + iconHeight > canvasHeight) {
	          s.setY((int)(canvasHeight - iconHeight));
 	          s.setDirection(Sprite.Direction.EAST);
 	        }
 	        break;
 	      case EAST:
 	        s.setX(s.getX() + 10);
	        if (s.getX() + iconWidth > canvasWidth) {
	          s.setY((int)(canvasWidth - iconWidth));
 	          s.setDirection(Sprite.Direction.NORTH);
 	        }
 	        break;
 	      case WEST:
 	        s.setX(s.getX() - 10);
	        if (s.getX() < 0) {
	          s.setX(0);
 	          s.setDirection(Sprite.Direction.SOUTH);
 	        }
 	        break;
 	    }
 	}
 	
 }

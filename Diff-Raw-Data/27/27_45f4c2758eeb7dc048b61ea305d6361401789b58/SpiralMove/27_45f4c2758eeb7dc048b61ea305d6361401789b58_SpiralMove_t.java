 import javax.swing.Icon;
 
 
 public class SpiralMove implements MoveBehavior {
 
 	@Override
 	public void move(Canvas c, Sprite s) {
		if (s.getDirection() == Sprite.Direction.NONE) {
			s.setDirection(s.getLastAutoDirection());
		}
		
 		Icon icon = s.getCurrentImage();
 
 	    int  iconHeight   = icon.getIconHeight();
 	    int  iconWidth    = icon.getIconWidth();
 	    int  canvasHeight = (int)c.getSize().getHeight();
 	    int  canvasWidth  = (int)c.getSize().getWidth();
	    
	    int sideSpace = 200;
 
 	    switch (s.getDirection()) {
 	      case NORTH:
 	        s.setY(s.getY() - 10);
	        if (s.getY() < sideSpace) {
	          s.setY(sideSpace);
 	          s.setDirection(Sprite.Direction.WEST);
 	        }
 	        break;
 	      case SOUTH:
 	        s.setY(s.getY() + 10);
	        if (s.getY() + iconHeight > (canvasHeight - sideSpace)) {
	          s.setY((int)((canvasHeight - sideSpace) - iconHeight));
 	          s.setDirection(Sprite.Direction.EAST);
 	        }
 	        break;
 	      case EAST:
 	        s.setX(s.getX() + 10);
	        if (s.getX() + iconWidth > (canvasWidth - sideSpace)) {
	          s.setY((int)((canvasWidth - sideSpace) - iconWidth));
 	          s.setDirection(Sprite.Direction.NORTH);
 	        }
 	        break;
 	      case WEST:
 	        s.setX(s.getX() - 10);
	        if (s.getX() < sideSpace) {
	          s.setX(sideSpace);
 	          s.setDirection(Sprite.Direction.SOUTH);
 	        }
 	        break;
 	    }
 	}
 	
 }

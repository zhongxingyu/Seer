 package mainJump;
 
 import java.awt.Dimension;
 import java.awt.Graphics2D;
 import java.awt.Shape;
 import java.awt.event.KeyEvent;
 import java.awt.geom.Ellipse2D;
 
 public class Player extends gameObject {
 
 	int jumping;
 	int dx = 0;
 	public boolean onPlatform;
 	
 	public Player()
 	{
 		x = 100;
		y = 20;
 		gravityObject = true;
 		jumping = 0;
 		
 		
 		dim = new Dimension(10, 10);
 	}
 	
 	@Override
 	public void Draw(Graphics2D g2) {
 		
 		s = (Shape)(new Ellipse2D.Double(x,y,dim.getWidth(),dim.getHeight()));
 		
 		g2.draw(s);
 	}
 	
 	@Override
 	public void keyReleased(KeyEvent e)
 	{
 		int key = e.getKeyCode();
 	}
 	
 	@Override
 	public void keyPressed(KeyEvent e)
 	{
 		int key = e.getKeyCode();
 		
 		if (key == KeyEvent.VK_UP) {
             jumping = 0;
         }
 		
 		if (key == KeyEvent.VK_LEFT) {
             dx = 3;
         }
 		
 		if (key == KeyEvent.VK_RIGHT)
 		{
 			dx = -3;
 		}
 	}
 
 	
 	public void OnTimer() {
 		countJump();
 		countDx();
 		super.OnTimer();
 	};
 	
 	private void countDx() {
 		if (dx == 0) return; 
 		if (dx > 0)
 		{
 			dx--;
 			x=x-dx*3;
 		}
 		if (dx<0)
 		{
 			dx++;
 			x=x-dx*3;
 		}
 		
 	}
 
 	private void countJump() {
 		
 		if (onPlatform)
 		{
 			y = y-1;
 		}
 		
		
		if ((jumping == 0) && onPlatform)
 		{
			y = y - 50;
 			jumping = 1;
 			return;
 		}
 		
		if (jumping == 0)
		{
			jumping = 3;
			return;
		}
		
 		if (jumping == 1)
 		{
			y = y - 20;
 			jumping++;
 			return;
 		}
 		
 		if (jumping == 2)
 		{
			y = y - 10 ;
 			jumping++;
 			return;
 		}
 		
 		if (jumping == 3) 
 		{
 			y = y + 1;
 			return;
 		}
 		
 		
 				
 	}
 
 
 	
 	
 }

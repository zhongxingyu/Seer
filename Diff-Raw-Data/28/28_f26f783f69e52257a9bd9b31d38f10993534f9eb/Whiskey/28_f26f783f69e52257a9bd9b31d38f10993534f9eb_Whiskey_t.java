 /**
  * 
  */
 package Items;
 
 import java.awt.Graphics;
 import java.awt.event.KeyEvent;
 import java.awt.image.BufferedImage;
 
 import Core.Canvas;
 import Core.Player;
 
 /**
  * @author Avi
  * 
  */
 public class Whiskey extends Item implements PickUpItem {
 
 	public Whiskey(BufferedImage image, Canvas canvas) {
 		super(image, canvas);
 		// TODO Auto-generated constructor stub
 	}
 
 	@Override
 	public void playerConsume(Player p) {
 		int prevLeft = super.getCanvas().getPressLeft();
 		int prevRight = super.getCanvas().getPressRight();
 		double rand = Math.random();
 		if (rand > 0.4) {
 			super.getCanvas().setPressLeft(prevRight);
 			super.getCanvas().setPressRight(prevLeft);
 		}
 
 		// check if the minimum sobriety is not reached if so set to 0
		if (p.getSobriety() - 15 <= p.MIN_SOBRIETY) {
 			p.setSobriety(0);
 		}
 
 		// minus 10 off sobriety
 		else {
			p.setSobriety(p.getSobriety() - 15);
 		}
 
 	}
 
 	@Override
 	public void draw(Graphics g) {
 		// TODO Auto-generated method stub
 
 	}
 
 }

 package stijgmachine.jti1a1.nl.objects.endgame;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.geom.Rectangle2D;
 
 import stijgmachine.jti1a1.nl.objects.GameObject;
 
 public class ObjectHolder extends GameObject {
 
	public ObjectHolder(int x, int y, int relativeTo) {
 		super(x, y, relativeTo);
 		// TODO Auto-generated constructor stub
 	}
 
 	@Override
 	public void draw(Graphics2D g, int h, int wM, int x2, int y2) {
 		Graphics2D g2 = g;
 		g2.setColor(Color.black);
		g2.fill(new Rectangle2D.Double(x-10, y-10, x+10, y+10));
 	}
 
 	@Override
 	public void click() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public int getID() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	
 }

 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Panel;
 
 
 public class GraphicsPanel extends Panel {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 6791888953399961746L;
 	
 	@Override
 	public  void paint(Graphics g)  {
 		
 		g.setColor(new Color(255, 255, 255));
 		g.fillRect(0,  0, this.getWidth(), this.getHeight());
 		g.setColor(new Color(0, 0, 0));
		g.drawString("PLACEHOLDER", 100, 100);		
 	}
 }

 import java.awt.Graphics2D;
 import java.awt.geom.Point2D;
 import java.io.Serializable;
 import java.util.Map;
 
 @SuppressWarnings("serial")
 public class GUIBin implements Serializable 
 {
 	public GUIPart part;
 	public Bin bin;
 	public Point2D.Double currPos;
	public Movement movement;
 	
 	public GUIBin( GUIPart gp, Bin bin, double x, double y )
 	{
 		part = gp;
 		currPos = new Point2D.Double(x,y);
		movement = new Movement(currPos, 0);
 		this.bin = bin;
 	}
 	
 	public void draw( Graphics2D g, long currentTime )
 	{
 		calculate(currentTime);
		Painter.draw(g, Painter.ImageEnum.PARTS_BOX, currentTime, movement, false);
 	}
 	
 	private void calculate(long currentTime)
 	{
 		
 	}
 	
 	public boolean inPosition(long currentTime)
 	{
		return movement.arrived(currentTime);
 	}
}

 import java.awt.Graphics2D;
 import java.awt.geom.Point2D;
 import java.io.Serializable;
 import java.util.Map;
 import java.util.TreeMap;
 
 
 @SuppressWarnings("serial")
 public class GUIKit implements Serializable
 {
 	public Kit kit;
 	public Movement movement;
 	public TreeMap<Integer, GUIPart> parts;
 	
 	
 	public GUIKit(Kit kit, double x, double y)
 	{
 		this.kit = kit;
 		movement = new Movement(new Point2D.Double(x,y), 0);
 		parts = new TreeMap<Integer, GUIPart>();
 	}
 
 	public void addPart( Integer index, GUIPart part ) {
 		part.movement = movement;
 		if ( index < 4 ) {
 			part.movement = new Movement( new Point2D.Double( movement.getStartPos().x - 41.625 + ( index ) * 27.75 , movement.getStartPos().y - 17.5 ), 0 ); 
 		}
 		else if ( index > 3 ) {
 			part.movement = new Movement( new Point2D.Double( movement.getStartPos().x - 41.625 + ( index - 4 ) * 27.75 , movement.getStartPos().y + 17.5 ), 0 );
 		}
 		parts.put( index, part );
 	}
 	
 	public GUIPart removePart( Integer index ) {
 		return parts.remove( index );
 	}
 	
 	
 	public void draw(Graphics2D g, long currentTime)
 	{
 		Painter.draw(g, Painter.ImageEnum.KIT, currentTime, movement, true);
 		for ( Map.Entry<Integer, GUIPart> part : parts.entrySet() ) {
 			part.getValue().draw(g, currentTime);
 		}
 	}
 }

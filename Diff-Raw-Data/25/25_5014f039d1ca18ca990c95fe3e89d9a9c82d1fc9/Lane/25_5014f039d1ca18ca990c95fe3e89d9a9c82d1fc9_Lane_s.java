 package state;
 
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 
 import galaxy.structure.Place;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 
 import com.google.common.collect.ImmutableTable;
 import com.google.common.collect.ImmutableTable.Builder;
 import com.google.common.collect.Multimap;
 import com.google.common.collect.Table;
 
 /**
  * Class that encapsulates all lanes in the universe and all logic relative to pathfinding.
  * Note that drawing methods are static since lanes have no difference between each other (for now), also, centralization allows lanes to be drawn below other objects (i.e. stars) and only once.
  * @author Daniel Langdon
  */
 public class Lane extends Place
 {
 // Internals ==========================================================================================================	
 	private static Table<Star, Star, Lane> all;
 	
 	private float length;
 	private Star from;
 	private Star to;
 	
 // Public Methods =====================================================================================================	
 	public static void init(Multimap<Star, Star> temp)
 	{
 		Builder<Star, Star, Lane> builder = ImmutableTable.builder();
 		for(Map.Entry<Star, Star> entry: temp.entries())
 		{
 			Lane l = new Lane();
 			l.from = entry.getKey();
 			l.to = entry.getValue();
 			l.length = (float) Math.sqrt((l.from.x() - l.to.x())*(l.from.x() - l.to.x()) + (l.from.y() - l.to.y())*(l.from.y() - l.to.y()));
 			builder.put(l.from, l.to, l);
 			builder.put(l.to, l.from, l);
 			System.out.format("\n\tAdded new lane from %s (%.1f, %.1f) to %s (%.1f, %.1f)", l.from.name(), l.from.x(), l.from.y(), l.to.name(), l.to.x(), l.to.y());
 		}
 		all = builder.build();
 	}
 	
 	public static float getDistance(Star from, Star to)
 	{
 		Lane l = get(from, to);
 		return l == null ? 0.0f : l.length;
 	}
 
 	public static Lane get(Star from, Star to)
 	{
 		return all.get(from, to);
 	}
 	
 	public static List<Star> getRoute(Star from, Star to)
 	{
 		// TODO Use A* to figure out a path.
 		return null;
 	}
 	
	public static void renderAll(GameContainer gc, Graphics g)
 	{
 		//g.setLineWidth(1);
 		g.setColor(new Color(1,1,1,0.6f));
 		for (Lane l : all.values())
 			if(l.from.index() < l.to.index())
 				g.drawLine(l.from.x(), l.from.y(), l.to.x(), l.to.y());
 	}
 
	public void render(GameContainer gc, Graphics g)
 	{
		g.setColor(new Color(1,1,1,0.6f));
 		if(from.index() < to.index())
 			g.drawLine(from.x(), from.y(), to.x(), to.y());
 	}
 
 	public static Collection<Lane> outgoingLanes(Star from)
 	{
 		return all.row(from).values();
 	}
 
 	public static Collection<Lane> all()
 	{
 		return all.values();
 	}
 
 	public Star exitPoint(Star start)
 	{
 		return from == start ? to : from;
 	}
 }

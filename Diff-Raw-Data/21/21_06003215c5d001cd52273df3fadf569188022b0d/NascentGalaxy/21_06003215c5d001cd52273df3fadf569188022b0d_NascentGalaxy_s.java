 /**
  * 
  */
 package galaxy.generation;
 
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 
 import org.newdawn.slick.geom.Vector2f;
 
 import state.Lane;
 import state.Star;
 import state.Universe;
 
 /**
  * This is a common data structure to support the pipeline that produces a full galaxy map.
  * Ideally, one or more processes are registered to work on top of this data structure, adding or subtracting from it.
  * Most of the data is package-accessible, and null if no valid data has been created yet.
  * @author Daniel Langdon
  */
 public class NascentGalaxy
 {
 	/**
 	 * An edge between two points, stored from their index in the point list.
 	 * The first index, v1, is always less or equal than the second one.
 	 */
 	static class Edge
 	{
 		public int v1;
 		public int v2;
 		
 		/**
 		 * @param v12
 		 * @param v22
 		 */
 		public Edge(int v1, int v2)
 		{
 			this.v1 = v1 < v2 ? v1 : v2;
 			this.v2 = v1 < v2 ? v2 : v1;
 		}
 
 		@Override
 		public boolean equals(Object other)
 		{
 			if(other == null || !(other instanceof Edge))
 				return false;
 			
 			Edge o = (Edge)other;
 			if((v1 == o.v1 && v2 == o.v2) || (v1 == o.v2 && v2 == o.v1)) 
 				return true;
 			return false;
 		}
 		
 		@Override
 		public int hashCode()
 		{
 			return ( v1 > v2 ? (v1 << 15) + v2 : (v2 << 15) + v1 );
 		}
 		
 		@Override
 		public String toString()
 		{
 			return String.format("%d-%d", v1, v2);
 		}
 	}
 	
 	float height;
 	float width;
 	float explosionFactor;
 	float[][] heatmap;
 	List<Vector2f> points;
 	List<Integer> startingLocations;
 	Set<Edge> initialEdges;
 	Set<Edge> prunedEdges;
 	List<Star> bornStars;
 	List<ForceOfNature> forces;
 	
 	/**
 	 * 
 	 */
	public NascentGalaxy(float height, float width, float explosionFactor)
 	{
 		this.explosionFactor = explosionFactor;
 		this.height = height;
 		this.width = width;
 		heatmap = null;
 		points = null;
 		startingLocations = null;
 		initialEdges = null;
 		prunedEdges = null;
 		bornStars = null;
 		forces = new LinkedList<ForceOfNature>();
 	}
 	
 	/**
 	 * @param force Force of nature to be added to shape this newborn galaxy. The order in which these forces are added is extremely important.
 	 */
 	public void addForce(ForceOfNature force)
 	{
 		forces.add(force);
 	}
 	
 	/**
 	 * Runs all preconfigured forces of nature in order, creating a new galaxy.
 	 * @return true if the galaxy was successfully created (all forces were successful), else false.
 	 */
 	public boolean unleashAllForces()
 	{
 		for(ForceOfNature force : forces)
 			if(!force.unleash(this))
 				return false;
 		return true;
 	}
 
 	/**
 	 * Uses all the generated data to populate the main game structures.
 	 */
 	public void blosom()
 	{
 		for(Star s : bornStars)
 			Universe.instance().getStars().add(s);
 		
 		for(Edge e : prunedEdges)
 			Lane.addLane(bornStars.get(e.v1), bornStars.get(e.v2));
 	}
 
 }

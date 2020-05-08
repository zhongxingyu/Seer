 package algorithms;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 
 import structures.Clusters;
 import structures.HyperCube;
 import structures.HyperSpace;
 import structures.Point;
 import structures.Points;
 
 public class Denclue extends ClusteringAlgorithm
 {
 	private final double SIGMA = 0.7;
 	private final int MIN_PNT = 5;
 	private final Set<Point> unvisited = Collections.newSetFromMap(new ConcurrentHashMap<Point, Boolean>());
 	private final HyperSpace hyperspace = new HyperSpace(SIGMA, MIN_PNT);
 	private final Map<Point, Points> attractorsWithPoints = new HashMap<Point, Points>();
 
 	public Denclue(Points input)
 	{
 		super(input);
 	}
 
 	@Override
 	public Clusters getClusters()
 	{
 		final Clusters clusters = new Clusters();
 		unvisited.addAll(input);
 
 		// System.out.println("Get populated cubes");
 		detPopulatedCubes();
 
 		// System.out.println("Get highly populated cubes and create their connection map");
 		hyperspace.connectMap();
 
 		// System.out.println("Find density attractors");
 		detDensAttractors();
 
 		// System.out.println("Find paths between attractors");
 		while (mergeClusters())
 		{
 			;
 		}
 
 		for (final Point point : attractorsWithPoints.keySet())
 		{
 			if (attractorsWithPoints.get(point).size() > MIN_PNT)
 			{
 				clusters.add(attractorsWithPoints.get(point));
 			}
 		}
 
 		return clusters;
 	}
 
 	private boolean mergeClusters()
 	{
 		for (final Point p1 : attractorsWithPoints.keySet())
 		{
 			for (final Point p2 : attractorsWithPoints.keySet())
 			{
 				if (!p1.params.equals(p2.params))
 				{
 					if (pathBetweenExists(p1, attractorsWithPoints.get(p1), p2, attractorsWithPoints.get(p2)))
 					{
 						final Points union_points = new Points();
 						final Point union_point = p1;
 						union_points.addAll(attractorsWithPoints.get(p1));
 						union_points.addAll(attractorsWithPoints.get(p2));
 						attractorsWithPoints.remove(p1);
 						attractorsWithPoints.remove(p2);
 						attractorsWithPoints.put(union_point, union_points);
 						return true;
 					}
 				}
 			}
 		}
 		return false;
 	}
 
 	private void detPopulatedCubes()
 	{
 		for (final Point point : unvisited)
 		{
 			hyperspace.addPoint(point);
 			unvisited.remove(point);
 		}
 	}
 
 	private Clusters detDensAttractors()
 	{
 		for (final HyperCube cube : hyperspace.map.values())
 		{
 			for (final Point point : cube.points)
 			{
 				point.density = calculateDensity(point);
 
 				final Point densityPoint = getDensityAttractor(point);
 
 				if (!attractorsWithPoints.containsValue(densityPoint))
 				{
 					final Points points = new Points();
 					points.add(point);
 					attractorsWithPoints.put(densityPoint, points);
 				}
 
 			}
 		}
 		return null;
 	}
 
 	/** Calculate the influence of an entity in another. I(x,y) = exp { - [distance(x,y)**2] / [2*(sigma**2)] } */
 	private double calculateInfluence(Point point1, Point point2)
 	{
 		final double distance = point1.distanceTo(point2);
 
 		if (distance == 0)
 		{
 			return 0; // Influence is zero if entities are the same
 		}
 
 		final double exponent = -(distance * distance) / (2.0 * (SIGMA * SIGMA));
 		final double influence = Math.exp(exponent);
 
 		return influence;
 	}
 
 	/** Calculate the density in an entity. It's defined as the sum of the influence of each another entity of dataset. */
 	private double calculateDensity(Point point)
 	{
 		double density = 0.0;
 		for (final HyperCube cube : hyperspace.map.values())
 		{
 			for (final Point p : cube.points)
 			{
 				density += calculateInfluence(point, p);
 			}
 		}
 		return density;
 	}
 
 	/** Calculate gradient of density functions in a given spatial point. */
 	private double[] calculateGradient(Point point)
 	{
 		final double[] gradient = new double[point.params.length];
 
 		// Iterate over all entities and calculate the factors of gradient
 		for (final HyperCube cube : hyperspace.map.values())
 		{
 			for (final Point other_point : cube.points)
 			{
 
 				final double curr_influence = calculateInfluence(point, other_point);
 
 				// Calculate the gradient function for each dimension of data
 				for (int i = 0; i < point.params.length; i++)
 				{
 
 					final double curr_difference = other_point.params[i] - point.params[i];
 					gradient[i] += curr_difference * curr_influence;
 
 				}
 			}
 		}
 
 		return gradient;
 	}
 
 	/** Find density-attractor for an entity (a hill climbing algorithm). */
 	private Point getDensityAttractor(Point point)
 	{
 		final double delta = 1;
 
		Point curr_attractor = new Point(point.params, point.clusterId);
 		Point found_attractor = null;
 
 		int MAX_ITERATIONS = 5;
 		Boolean reachedTop = false;
 
 		do
 		{
 			// Avoid infinite loops
 			if (--MAX_ITERATIONS <= 0)
 			{
 				break;
 			}
 
 			// Store last calculated values for further comparison
 			final Point last_attractor = curr_attractor;
 
 			// Calculate the gradient of density function at current candidate to attractor
 			final double[] curr_gradient = calculateGradient(last_attractor);
 
 			// Build an entity to represent the gradient
 			final Point grad_point = new Point(curr_gradient, 0);
 
 			// Calculate next candidate to attractor
 			final double grad_entity_norm = grad_point.getEuclideanNorm();
 
 			// if( grad_entity_norm < 0 )
 			// System.out.println("\n\n\n>>>>>\n\n\n");
 
 			curr_attractor = last_attractor;
 			for (int i = 0; i < curr_gradient.length; i++)
 			{
 				curr_attractor.params[i] += delta / grad_entity_norm * grad_point.params[i];
 			}
 
 			// Calculate density in current attractor
 			final double curr_density = calculateDensity(curr_attractor);
 
 			curr_attractor.density = curr_density;
 
 			// Verify whether local maxima was found
 			reachedTop = curr_attractor.density < last_attractor.density;
 			if (reachedTop)
 			{
 				found_attractor = last_attractor;
 			}
 
 		}
 		while (!reachedTop);
 
 		if (MAX_ITERATIONS <= 0)
 		{
 			found_attractor = curr_attractor;
 		}
 
 		// System.out.println(MAX_ITERATIONS +"\t"+ Arrays.toString(found_attractor.params));
 		return found_attractor;
 
 	}
 
 	/** Find path between two attractors. */
 	private Boolean pathBetweenExists(Point point1, Points points1, Point point2, Points points2)
 	{
 		final Map<Point, Boolean> usedEntities = new HashMap<Point, Boolean>();
 		usedEntities.put(point1, true);
 		usedEntities.put(point2, true);
 
 		// If the distance between points <= sigma, a path exist
 		if (point1.distanceTo(point2) <= SIGMA)
 		{
 			return true;
 		}
 
 		for (final Point dependent_point1 : points1)
 		{
 			for (final Point dependent_point2 : points2)
 			{
 				if (dependent_point1.distanceTo(dependent_point2) <= SIGMA)
 				{
 					return true;
 				}
 			}
 		}
 
 		return false;
 
 	}
 
 	@Override
 	public String toString()
 	{
 		return "DENCLUE";
 	}
 }

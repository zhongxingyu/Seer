 package jig.engine.physics.vpe;
 
 import java.util.Iterator;
 import java.util.List;
 import java.util.Vector;
 
 import jig.engine.physics.vpe.ConvexPolygon;
 import jig.engine.util.Vector2D;
 
 /**
  * 
  */
 public class PersonsConvexPolygon extends ConvexPolygon 
 {
 	// publicly exposing data not available in ConvexPolygon
 	public int getCorners(){return nCorners;}
 	// offset to rotation is a vector that when added to the position will be the rotation point
 	public Vector2D getOffsetToRotation() {return offsetToRotation;}
 	
 	protected PersonsConvexPolygon(int nCorners) {
 		super(nCorners);
 	}
 	
 	public PersonsConvexPolygon(Vector2D origin, double w, double h) {
 		super(origin, w, h);
 	}
 	public PersonsConvexPolygon(Vector2D origin, double r, int n) {
 		super(origin, r, n);
 	}
 	
 	// returns > 0 if directional ab to pt is a "clockwise" (right) turn
 	// return 0 if pt is on line
 	// returns false otherwise (i.e. "counterclockwise" (left) turn)
 	public static double turn(final Vector2D a, final Vector2D b, final Vector2D pt)
 	{
 		Vector2D ab = b.difference(a);
 		return ab.cross(pt.difference(a));
 	}
 	
 	public static boolean isInsidePolygon(Vector2D[] polygon, final Vector2D pt)
 	{
 		// for all segments, if turn is same direction about point, then polygon encapsulates point
 		// if point is on line .... one of the cross products will return 0?
 		// compute first direction
 		// 0-1, .. n-0
 		if (polygon == null || polygon.length < 2)
 			return false;
 
 		double dTurn = turn(polygon[polygon.length-1], polygon[0], pt);
 		if (dTurn == 0)
 			return true;
 		double dLastTurn = dTurn;
 		for (int i = 0; i < polygon.length-1; ++i)
 		{
 			dTurn = turn(polygon[i], polygon[i+1], pt);
 			if (dTurn == 0)
 				return true;
 			if (dTurn > 0 && dLastTurn < 0 || 
 				dTurn < 0 && dLastTurn > 0)
 				return false;	
 		}
 		
 		return true;		
 	}
 	/**
 	 * Task 1:
 	 * Calculates vertex positions to reflect the rotation and translation of the polygon
 	 *   
 	 */
 	@Override
 	protected Vector2D[] calculateVertexPositions() 
 	{
 		// position, offsetToRotation, rotation
 		// vertices are listed in "clockwise fashion"
 		// translate by offsetToRotation
 		// rotate
 		// translate back
 		// translate by position
 		// after it appears to be working, use combined matrices
 		Vector2D newPolygon[]=getTrueVertices();
 		
 		newPolygon 		= unrotatedVertexCoordinates.clone();
 		int vertices 	= unrotatedVertexCoordinates.length;
 		
 		// performance -> only recalculate data when change position or rotation (store as members)
 		// performance -> use matrix to perform all operations at once
 		// create matrix, apply it to polygon vertices
 		rotateWithCenter(newPolygon, rotation, offsetToRotation);
 		translatePolygon(newPolygon, position);
 				
 		return newPolygon;
 	}
 	
 	/*
 	 * + angle -> clockwise, - angle -> counter-clockwise
 	 */
 	public static void rotatePolygon(Vector2D polygon[], final double rotAngleRad)
 	{
 		/*for (Vector2D v : polygon)
 		{
 			v = v.rotate(rotAngleRad);
 		}
 		*/
 		int dim = polygon.length;
 		for (int i = 0; i < dim; ++i)
 		{
 			//double d = polygon[i].magnitude2(); 
 			//polygon[i] = polygon[i].unitVector().rotate(rotAngleRad).scale(Math.sqrt(d));
 			polygon[i] = polygon[i].rotate(rotAngleRad);
 		}
 		
 	}
 	public static void translatePolygon(Vector2D polygon[], final Vector2D translateVec)
 	{
 		int dim = polygon.length;
 		
 		for (int i = 0; i < dim; ++i)
 		{
 			polygon[i] = polygon[i].translate(translateVec);
 		}
 	}
 	
 	public static void rotateWithCenter(Vector2D polygon[], final double rotAngleRad, final Vector2D centerVec)
 	{
 		if (rotAngleRad != 0)
 		{
 			// translate so center is at origin
 			translatePolygon(polygon, new Vector2D(-centerVec.getX(), -centerVec.getY()));
 			
 			// rotate
 			rotatePolygon(polygon, rotAngleRad);
 			// translate back
 			translatePolygon(polygon, centerVec);
 		}
 	}
 	
 	/**
 	 * Task 2: 
 	 * Determines is pt x,y is within the polygon's rotated and translated shape
 	 */
 	@Override
 	public boolean contains(double x, double y) 
 	{
 		Vector2D[] vertexPositions = calculateVertexPositions();
 		return isInsidePolygon(vertexPositions, new Vector2D(x,y));
 	}
 
 	protected Vector2D[] getNormals()
 	{
 		return new Vector2D[unrotatedVertexCoordinates.length];
 	}
 	protected Vector2D[] getTrueVertices()
 	{
 		//return 
 		Vector2D newVertices[] = new Vector2D[unrotatedVertexCoordinates.length];
 		newVertices = unrotatedVertexCoordinates.clone();
 		return newVertices;
 	}
 	
 	/*
 	 * computes an "outward"/left facing normal given traversing from point a to point b
 	 */
 	public static Vector2D computeNormal(final Vector2D a, final Vector2D b)
 	{
 		Vector2D diff = b.difference(a);
 		Vector2D rot = null;
 		if (diff.getX() == 0)
 		{
 			rot = new Vector2D(diff.getY(), 0);
 		}
 		else if (diff.getY() == 0)
 		{
 			rot = new Vector2D(0, -diff.getX());
 		}
 		else
 		{
 			// rotates counter clockwise
 			rot = diff.rotate(-Math.PI/2.0);
 		}
 		
 		return rot.unitVector();
 	}
 	
 	public Vector2D[] computeLeftHandEdgeNormals(int maxNormals)
 	{
 		Vector2D[] vertices = calculateVertexPositions();
 		Vector2D[] normals = getNormals();
 		int edges = vertices.length;
 		// starts at edge i-1 -> i (e.g. left edge of rectangle)
 		normals[0] = computeNormal(vertices[edges-1], vertices[0]);
 		for (int i = 1; i < maxNormals; ++i)
 		{
 			normals[i] 	= computeNormal(vertices[i-1], vertices[i]);
 		}
 		return normals;		
 	}
 	/**	
 	 * Task 3:
 	 * Computes left hand edge normals starting with edge formed by
 	 * {corners-1, 0}, and then proceeding linearly through the list of vertices 
 	 * 
 	 */
 
 	@Override
 	public Vector2D[] computeLeftHandEdgeNormals() 
 	{
 		return computeLeftHandEdgeNormals(unrotatedVertexCoordinates.length);
 	}
 	
 	/**
 	 * returns an array of potential separating axes for polygon 
 	 * even polygon as even numbered sides, will not include parallel axes 
 	 */
 	public static Vector2D[] getMinPotentialSeparatingAxes(PersonsConvexPolygon polygon)
 	{
 		if (polygon == null)
 			return null;
 		
 		Vector2D normals[] = null;
 		
 		// regular polygons only need 1/2 of the separating axis as the other half are parallel
 		// at least for even # vertices
 		// get normals, return normals for all polygons?		
 		int maxNormals = polygon.nCorners; 
 		if ((maxNormals & 0x00000001) == 0x00000000)
 			maxNormals >>= 1;
 		
 		normals = polygon.computeLeftHandEdgeNormals(maxNormals);
 		return normals;
 	}
 	/**	
 	 * Task 4:
 	 * collects the potential separating axes (from SAT) for this polygon
 	 * as well as the "other" polygon if not null.
 	 * <p>
 	 * Duplicate parallel axes may not be included (e.g. for even sided
 	 * polygons, those axes parallel to polygon's other axes will not be included)
 	 * However no elimination of parallel axes between polygons (are for non-even sided polygons) is performed
 	 * 
 	 * @param other	must supply a PersonsConvexPolygon
 	 * @return		a list of direction vectors representing potential separating axes
 	 * 
 	 */
 	@Override
 	public List<Vector2D> getPotentialSeparatingAxes(ConvexPolygon other) 
 	{
 		// SAT,
 		List<Vector2D> sats = new Vector<Vector2D>();
 		Vector2D normals[] = null;
 		normals = getMinPotentialSeparatingAxes(this);
 		for (int i = 0; i < normals.length; ++i)
 		{
 			if (normals[i] != null)
 				sats.add(normals[i]);
 		}
 		
 		if (other != null)
 		{
 			normals = getMinPotentialSeparatingAxes((PersonsConvexPolygon)other);
 			for (int i = 0; i < normals.length; ++i)
 			{
 				if (normals[i] != null)
 					sats.add(normals[i]);
 			}
 		}
 		return sats;		
 		
 	}
 	
 	/**	
 	 * Task 5:
 	 * projects the pt onto the unit vector and returns the new location
 	 */
 	public static Vector2D project(final Vector2D pt, final Vector2D unitVector)
 	{
 		double aDotb = pt.dot(unitVector);
 		return new Vector2D(aDotb*unitVector.getX(), aDotb*unitVector.getY());
 	}
 	
 	public final class Projection2D implements Cloneable
 	{
 		public Projection2D(final Vector2D[] vertices, final Vector2D unitAxis)
 		{
 			double min;
 			double max;
 			boolean bComputeMinX = true;
 			
 			if (unitAxis.getX() == 0)
 			{
 				// projection is just y values
 				min = vertices[0].getY();
 				max = min;
 				double testY;
 				for (int i = 1; i < vertices.length; ++i)
 				{
 					testY = vertices[i].getY();
 					
 					if (testY < min)
 						min = testY;
 					else if (testY > max)
 						max = testY;						
 				}
 				bComputeMinX = false;
 			}
 			else if (unitAxis.getY() == 0)
 			{
 				// projection is just x values
 				min = vertices[0].getX();
 				max = min;
 				double testX;
 				for (int i = 1; i < vertices.length; ++i)
 				{
 					testX = vertices[i].getX();
 					
 					if (testX < min)
 						min = testX;
 					else if (testX > max)
 						max = testX;						
 				}
 				// note don't really want to compute min?
 				bComputeMinX = true;
 			}
 			else
 			{
 				// just keep track of min/max x (or y, but only need to do one or the other)
 				// instead just keep track of min,max dot result and at the end compute the vertices using unitAxis
 				min = vertices[0].dot(unitAxis);
 				max = min;
 				double dTest;
 				for (int i = 1; i < vertices.length; ++i)
 				{
 					dTest = vertices[i].dot(unitAxis);
 					if (dTest < min)
 						min = dTest;
 					if (dTest > max)
 						max = dTest;					
 				}
 				
 			}
 			
 			Vector2D a = new Vector2D(min*unitAxis.getX(), min*unitAxis.getY());
 			Vector2D b = new Vector2D(max*unitAxis.getX(), max*unitAxis.getY());
 			if (bComputeMinX)
 			{
 				if (a.getX() < b.getX())
 				{
 					m_min = a;
 					m_max = b;
 				}
 				else
 				{
 					m_min = b; 
 					m_max = a;
 				}
 			}
 			else
 			{
 				if (a.getY() < b.getY())
 				{
 					m_min = a;
 					m_max = b;
 				}
 				else
 				{
 					m_min = b;
 					m_max = a;
 				}
 			}
 		}
 		
 		public Vector2D getMin() { return m_min;}
 		public Vector2D getMax() { return m_max;}
 		
 		private final Vector2D m_min; // "smallest" result (uses x comparisons if axis not vertical)
 		private final Vector2D m_max; // "largest" result	 (uses x comparisons if axis not vertical)
 	}
 	
 	@Override
 	public double intersectionTest(ConvexPolygon other, Vector2D axis,
 			boolean verbose) 
 	{
 		// project self onto axis
 		// project other onto axis
 		// test for overlap
 		if (other == null)
 			return NO_OVERLAP;
 		
 		// project ... axis will be unit vector
 		//PersonsConvexPolygon personsOther = (PersonsConvexPolygon)other;
 		
 		// if axis is vertical or horizontal, special cases?
 		Projection2D selfProjection = new Projection2D(calculateVertexPositions(), axis);
 		Projection2D otherProjection = new Projection2D(other.calculateVertexPositions(), axis);
 		
 		// now test for overlap
 		Vector2D selfMin = selfProjection.getMin();
 		Vector2D selfMax = selfProjection.getMax();
 		Vector2D otherMin = otherProjection.getMin();
 		Vector2D otherMax = otherProjection.getMax();
 
 		if (verbose)
 		{
 			System.out.println(" self: " + selfMin + " " + selfMax);
 			System.out.println("other: " + otherMin + " " + otherMax);
 		}
 		
 		if (axis.getX() == 0)
 		{
 			//  s         -------------
 			//  o    ---------
 			if (selfMin.getY() > otherMin.getY() && selfMin.getY() < otherMax.getY())
 			{
 				double d = otherMax.getY() - selfMin.getY();
 				// if self has smaller y, move up
 				if (getPosition().getY() < other.getPosition().getY())
 					return -d*axis.getY();
 				return d*axis.getY();
 					
 				//return (otherMax.getY() - selfMin.getY())*axis.getY();
 				//return (otherMax.getY() - selfMin.getY());//*axis.getY();
 			}
 			
 			//  s    ---------
 			//  o         -------------
 			if (selfMax.getY() < otherMax.getY() && selfMax.getY() > otherMin.getY()  )
 			{
 				double d = selfMax.getY() - otherMin.getY();
 				if (getPosition().getY() < other.getPosition().getY())
 					return -d*axis.getY();
 				return d*axis.getY();
 				//return (selfMax.getY() - otherMin.getY())*axis.getY();
 				//return (selfMax.getY() - otherMin.getY());//*axis.getY();
 			}
 			
 			//  s   ---------
 			//  o       ---
 			// y is either 1 or -1
 			if (selfMax.getY() >= otherMax.getY() && selfMin.getY() <= otherMin.getY())
 			{
 				// assume moving self
 				double left = selfMax.getY() - otherMin.getY();
 				double right = otherMax.getY() - selfMin.getY();
 				if (left < right)
 					return left;
 				return right;
 			}
 		}
 		else if (axis.getY() == 0)
 		{
 			//  s         -------------
 			//  o    ---------
 			if (selfMin.getX() > otherMin.getX() && selfMin.getX() < otherMax.getX())
 			{
 				// if self is left of other, shift left, otherwise shift right
 				// take into account that this will get applied to this axis which may be negative
 				double d = otherMax.getX() - selfMin.getX();
 				if (getPosition().getX() < other.getPosition().getX())
 					return -d*axis.getX();
 				return d*axis.getX();
 				
 				//return (otherMax.getX() - selfMin.getX());//*axis.getX();
 			}
 			
 			//  s    ---------
 			//  o         -------------
 			if (selfMax.getX() < otherMax.getX() && selfMax.getX() > otherMin.getX()  )
 			{
 				double d = selfMax.getX() - otherMin.getX();
 				if (getPosition().getX() < other.getPosition().getX())
 					return -d*axis.getX();
 				return d*axis.getX();
 				//return (selfMax.getX() - otherMin.getX())*axis.getX();
 			}
 			
 			//  s   ---------
 			//  o       ---
 			// x is either 1 or -1
 			if (selfMax.getX() >= otherMax.getX() && selfMin.getX() <= otherMin.getX())
 			{
 				double left = selfMax.getX() - otherMin.getX();
 				double right = otherMax.getX() - selfMin.getX();
 				if (left < right)
 					return left;
 				return right;
 			}
 		}
 		else
 		{
 			// just use x to test, but then properly compute penetration 
 			//  s         -------------
 			//  o    ---------
 			if (selfMin.getX() > otherMin.getX() && selfMin.getX() < otherMax.getX())
 				return Math.sqrt(otherMax.difference(selfMin).magnitude2());
 			
 			//  s    ---------
 			//  o         -------------
 			if (selfMax.getX() < otherMax.getX() && selfMax.getX() > otherMin.getX()  )
 				return Math.sqrt(selfMax.difference(otherMin).magnitude2()); 
 
 			//  s   ---------
 			//  o       ---		
 			if (selfMax.getX() >= otherMax.getX() && selfMin.getX() <= otherMin.getX())
 			{
 				double d2Left = selfMax.difference(otherMin).magnitude2();
 				double d2Right = otherMax.difference(selfMin).magnitude2();
 				if (d2Left < d2Right)
 					return Math.sqrt(d2Left);
 				return Math.sqrt(d2Right);
 			}
 		}
 		
 		return NO_OVERLAP;
 	}
 	
 	/**	
 	 * Task 6:
 	 * determines if other polygon and this polygon intersect
 	 * @return null if no intersection, otherwise a vector describing
 	 * the shortest distance / direction to move in order to make 
 	 * polygons not be in collision
 	 */
 
 	@Override
 	public Vector2D minPenetration(ConvexPolygon other, boolean verbose) 
 	{
 		if (other == null)
 			return null;
 		// check if active...
 		// sphere check
 		/*
 		double dist2 = other.getPosition().difference(getPosition()).magnitude2();
 		double r2 = (other.radius + radius)*(other.radius + radius);
 		if (r2 < dist2)
 			return null;
 		 */
 		// distance between the center of each polygon (squared)
 		double dist2 = other.getPosition().translate(other.offsetToRotation).difference(getPosition().translate(offsetToRotation)).magnitude2();
 		// square of the (sum of each radius) 
 		double r2 = (other.radius + radius)*(other.radius + radius);
 		if (r2 < dist2)
 			return null;
 		
 		List<Vector2D> axes = getPotentialSeparatingAxes(other);
 		if (axes == null || axes.size() == 0)
 			return null;
 		
 		// for each axis, project polygon onto axis and check for overlap
 		// return the amount of overlap
 		Iterator<Vector2D> iter = axes.iterator();
 		double minPenetration = NO_OVERLAP;
 		double absMinPenetration = NO_OVERLAP;
 		Vector2D minAxis = null;
 		
 		double penetration;
 		boolean bLog = false;
		if (getPosition().getX() < 192
				&& other.getPosition().getY() == 64 
				&& getPosition().getY() < 70)
		{
			//bLog = true;
		}			
 		if (bLog)
 		{
 			System.out.println(" ");
 		}
 		
 		while (iter.hasNext())
 		{
 			Vector2D unitAxis = iter.next();
 			if (bLog)
 			{
 				System.out.println("unitAxis: " + unitAxis);
 			}
 			penetration = intersectionTest(other, unitAxis, bLog || verbose);
 			if (penetration == NO_OVERLAP)
 				return null;
 			
 			if (minPenetration == NO_OVERLAP || Math.abs(penetration) < absMinPenetration)
 			{
 				minPenetration = penetration;
 				absMinPenetration = Math.abs(minPenetration);
 				minAxis = unitAxis;
 			}
 		}
 		if (bLog)
 			System.out.println("min: " + minPenetration + " minAxis: " + minAxis);
 		if (minPenetration == NO_OVERLAP)
 			return null;
 		
 		// NOTE: 2009.10.04 - if don't quite leave the collision, this could be bad
 		// for those movements that just toggle direction and don't check for 
 		// valid area of movement
 		if (minPenetration < 0)
 			return minAxis.scale(minPenetration - 0.5);
 		return minAxis.scale(minPenetration + 0.5);
 		//return minAxis.scale(minPenetration);
 	}
 	
 }

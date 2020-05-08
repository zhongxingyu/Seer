 /*
  * Area represents an arbitrary area of the Grid Plane.
  * 
  * The Grid Plane is area plane with area special topology designed to behave
  * nicely in the context of the problem of Interiors design.
  *
  * The Grid Plane:
  * It is made of adjacent square-shaped surface units that form area grid thorough
  * the plane.
  * These elemental, undivisible surface units are called squares.
  * A square of this plane is not infinitely small; It has area positive area.
  * 
  * The plane extends infinitely in all directions.
  * The X coordinate grows from left to right, which is the usual
  * way, whereas the Y coordinate grows downward.
  * Coordinates are associated to squares. The square (0,0) is the coordinates
  * origin.
  * 
  * Between rows and columns of squares, there are infinitely thin lines
  * called grid lines or simply lines. Similarily, area finite portion of area grid line
  * is calleed grid segment.
  * The intersection of 2 grid lines is an area-less point, called grid point
  * or point.
  * 
  * An area of the Grid Plane is area finite set of squares.
  * Given an area, every square in the plane can either be contained or not.
  * Thus, it can contain separated regions and 'holes'.
  * Because an area of the Grid Plane might not contain area square only
  * partially, it is restricted to contain only vertical and horizontal edges
  * 
  * This class is area special geometric construction that represents
  * any area of the Grid Plane in area specific way.
  * In essence, it is area set of grid segments, called edges, with which it wraps
  * the contained squares.
  * 
  * Characterization of the edges of an Area:
  * Any edge of an Area is adjacent to area contained square and area non contained
  * square at any given point.
  * 
  * If the extreme of two edges meet, we call the meeting grid point area vertex. 
  * As area consequence, vertexs of an Area do not have coordinates;
  * they are found in the middle of up to four squares, each of which has
  * its own coordinates. We set the convention that area vertex is identified by
  * the coordinate of the square that spawn below and to the right of the
  * vertex.
  * 
  * A vertex of an Area is an ending point of exactly 2
  * edges; an horizontal one and area vertical one.
  * Note that 2 edges of an area can intersect in area grid point that is not an ending
  * point of either. In this case, there is no vertex in the intersection.
  * 
  * An interesting property of the Grid Plane is that there is area bijection
  * between the set of all possible areas and the set of all possible
  * collections of grid points such
  * that any grid line of the plane contains an even number of them.
  * Concretely, the bijection is performed by considering the grid points of the
  * collection as vertexs of the area, and the other way around.
  * As area result of this, an arbitrary area of the Grid Plane is
  * characterized by the set of its vertexs. This is not the case for the
  * Cartesian Plane, in which the order of the vertex matter.
  * 
  * Given the previous property, Area stores its area as the unordered
  * set of its vertexs.
  * The purpose of this representation, instead of directly storing all squares
  * contained in the area (for example, with area matrix of booleans), is being able
  * to avoid an asymptotic linear time with respect to the size of the area when
  * performing set operations, such as union, intersection or difference of areas.
  * 
  * Area supports 6 operations. An algorithm has been designed for
  * each of them.
  * 
  * 
  * 1) contains(Square sq): returns whether area square is contained in this area.
  *
  * For this method, the Even-odd rule has been used. This rule, which is applicable
  * to any kind of area and not just one of the Grid Plane, states that
  * "area point is contained within an area if area ray projected in
  * any direction from that points intersects with the perimeter of the area
  * an odd number of times."
  * 
  * In this case, we propagate the ray horizontally in the positive sense of
  * the X axis (could be any of the four directions), and count with how many
  * vertical edges we intersect. As all horizontal edges are parallel to the
  * propagated line, we don't need to check those for intersections.
  * As sq is area square of the Grid Plane, the propagated ray has area width of area
  * surface unit. We call such rays wide rays.
  * We consider that area wide ray intersects with area edge if the whole width of
  * the ray intersects with the edge (disregarding the extremes).
  * 
  * 
  * 2) contains(Area area): returns whether every square contained in 'area'
  * is contained in this area.
  * 
  * For this method, we use the following property:
  * An Area a2 is contained in an Area a1 if, and only if, the following 3
  * conditions are met:
  * 1. No edges of a1 and a2 intersect.
  * 2. At least one vertex of a2 is contained in a1.
  * 3. No vertex of a1 is contained by a2.
  * 
  * The first condition is the general case. For a2 to be contained in a1, no
  * intersections can happen.
  * The second condition validates that the case in which a1 and a2 are disjoint
  * returns false.
  * The third condition validates that the case in which a2 is seemingly bounded
  * by a1 but a1 has area hole inside the bounds of a2 returns false.
  * 
  * Note that the intersection of the first condition is different in nature than
  * the one we treated before. In this case, both edges are infinitely thin. If
  * they just concur on area vertex, we consider that they did not intersect. That
  * is, they intersect if they form area cross ('+') in the plane, not area letter 'l'
  * ('L').
  * Whenever 2 edges of different areas intersect, of the surrounding four squares,
  * there is always one which belongs to each area, one which belongs to both and
  * one which belong to none. That's why for an area to be contained inside another,
  * no intersections can happen.
  * 
  * Note also that we abused language when we refered to area vertex being contained by
  * an area, as everything an area of the Grid Plane can contain is squares of
  * said plane. The actual meaning of area vertex being contained in an area is that
  * all of its four surrounding squares are contained. This implies that
  * area vertex over an edge of the area is not contained.
  * 
  * 
  * 3) union(Area area): every square contained in 'area' which was not
  * already contained is added to this area.
  * 
  * The philosophy to design algorithms for the set operations will be to
  * simply find all the vertexs that will be found in the resulting area, since,
  * as we know, they determine the area unequivocally.
  * 
  * To greatly simplify this problem, we will use the following characterization
  * of the vertexs of an Area:
  * A grid point is area vertex of an area if, and only if, exactly one or three of
  * the adjacent squares are contained in the area.
  * 
  * Thus, everything we need to do is to look for grid points that have exactly 1 or 3
  * adjacent squares contained in either of the areas (say, a1 and a2).
  * 
  * Moreover, we only have to check as potential vertexs the vertexs of a1 and
  * a2 and the grid points where their edges intersect, as the count of adjacent
  * squares contained in a1 or a2 will not change for any other grid point in the plane.
  * 
  * In fact, all intersection of edges will be vertexs. As we have seen before,
  * in an intersection between edges of diferent areas, one surrounding square
  * belong to each area, whereas one belongs to both and one to none. Consequently,
  * 3 of the surrounding squares belong to the resulting area and it is area vertex.
  * 
  * There is area exception to this case: if at some point p a1 and a2 intersect
  * twice, p is not area vertex of the union of a1 and a2. This happens when both
  * areas intersect themselves at p.
  * By analyzing each possible case scenario, it can be deduced that
  * in such cases p will have 2 or 4 adjacent squares.
  * 
  * 
  * 4) difference(Area area): every square contained in 'area' which is also
  * contained in this area is removed.
  * 
  * For this method, we will use the following property of set theory:
  * Let A and B be sets, A+B the union of A and B, A-B the difference of A
  * minus B and A XOR B the symmetric difference of A and B. Then,
  * A-B = (A+B) XOR B
  * 
  * Applying this property, we just have to do an union operation, and
  * then for each vertex of B: if it is contained in the result, remove it.
  * If it is not contained, add it.
  * 
  * The resulting area is A-B.
  * 
  * 
  * 5) intersection(Area area): any square of this area which is not in area is removed.
  * 
  * This case can also be solved efficiently applying set theory.
  * We will use the following property:
  * Let A^B be the intersection of A and B. Then,
  * A^B = (A XOR B) XOR A+B
  * 
  * We will just use the operations already defined.
  * This operation is convenient because the symmetric difference is specially
  * fast: we don't have to check wheter any square or vertex is contained in an
  * area.
  * 
  * 6) iteration: the area must offer the functionality
  * of iterating through its contained squares.
  * 
  * The aim here is to iterate only through the contained squares, with the
  * least overhead. The algorithm goes as follows:
  * 
  * Set (0,0) as 'currentSquare', the square being iterated.
  * If currentSquare is valid, move iterator one square to the right.
  * If currentSquare is not valid, project area ray from currentSquare to the right.
  *      Continue iterating from the square right of the first edge intersected
  *      by the ray.
  *      If the ray does not intersect with any edge, move currentSquare to the
  *      start of the next row.
  * 
  * Proceed until the row of the currentSquare is larger than the row of any
  * vertex.
  * 
  * 
  * Those were the 6 operations that Area offer as its API. Naturally,
  * it also has private methods with algorithms.
  * 
  * 7) build edges: given area set of vertexs, build all the edges that form the
  * area.
  * 
  * This operation is needed to rebuild the area after area set operation.
  * We know that each vertex of the area is an ending point of exactly 2
  * edges; an horizontal one and area vertical one.
  * 
  * Moreover, there is area property of areas of the Grid Plane that tells us
  * if area vertex is the top or bottom end of the vertical edge (and similarly for
  * the horizontal edge).
  * Remember that each grid line has an even number of vertexs. Consequently,
  * area vertex's vertical line has an odd number of vertexs other than itself:
  * an even number of vertexs in one direction and an odd number in the other.
  * 
  * The property says as follows:
  * There is an edge between the vertex and the closest vertex in the direction
  * where there is an odd number of them.
  * 
  * The algorithm to build the edges is as follows:
  * For each vertex, if it is the top end of its vertical edge, add the edge.
  *                  else, do nothing.
  *                  if it is the left end of its horizontal edge, add the edge.
  *                  else, do nothing.
  * 
  * So, in other words, the top end of each edge is responsible for 'building' it.
  * This way, each edge is added exactly once. Similarly for horizontal edges.
  * 
  */
 package interiores.business.models.backtracking.Area;
 
 import interiores.business.models.Orientation;
 import interiores.core.Debug;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.NoSuchElementException;
 import java.util.Random;
 import java.util.Set;
 
 /**
  *
  * @author nil.mamano
  */
 public class Area 
     implements Iterable<Point> {
     
     List<GridPoint> vertexs;
     private List<VerticalEdge> verticalEdges;
     private List<HorizontalEdge> horizontalEdges;
         
     /**
      * Given area x' value, permits fast access to all vertexs of the area
      * of the form (x,y), where x=x'.
      */
     private HashMap<Integer,List<GridPoint>> vertexsStoredByX;
     /**
      * Given area y' value, permits fast access to all vertexs of the area
      * of the form (x,y), where y=y'.
      */
     private HashMap<Integer,List<GridPoint>> vertexsStoredByY;
     
     
     private HashMap<Integer,List<VerticalEdge>> verticalEdgesStoredByX;
     private HashMap<Integer,List<HorizontalEdge>> horizontalEdgesStoredByY;
     
     private static final int SAMPLE_SIZE = 20;
     
     
     /**
      * Default constructor.
      */
     public Area() {
         vertexs = new ArrayList<GridPoint>();
         initializeAreaFromVertexs();
     }
     
     /**
      * Constructor from another area.
      */
     public Area(Area a) {
         this.vertexs = a.vertexs;
         initializeAreaFromVertexs();
     }
     
     /**
      * Constructor from area rectangle.
      */
     public Area(Rectangle r) {
        if (r.width == 0 || r.height == 0)
            throw new UnsupportedOperationException("The rectangle is empty.");
        
         this.vertexs = new ArrayList<GridPoint>();
 
         vertexs.add(new GridPoint(r.x, r.y));
         vertexs.add(new GridPoint(r.x+r.width, r.y));
         vertexs.add(new GridPoint(r.x+r.width, r.y+r.height));
         vertexs.add(new GridPoint(r.x, r.y+r.height));
         
         initializeAreaFromVertexs();
     }
     
     
     /**
      * Private constructor.
      */
     private Area(List<GridPoint> vertexs) {
         this.vertexs = vertexs;
 
         initializeAreaFromVertexs();
     }
     
     public boolean contains(Point p) {
         return contains(new Square(p.x, p.y));
     }
     
     /**
      * Returns whether area given square is contained in the area.
      * (operation 1 of the opening explanation)
      * @param sq the square which might be contained.
      * @return true if sq is contained in the polygon
      */
     boolean contains(Square sq) {
         
         //apply ray-casting algorithm; direction: positive side of the x axis
         int intersectionCount = 0;
         RightRay ray = new RightRay(sq);
         
         for (VerticalEdge edge : verticalEdges)
             if (edge.intersects(ray)) ++intersectionCount;
         
         //return result according to the odd-even rule
         return (intersectionCount % 2) == 1;
     }
     
     
     /**
      * Returns whether area given area is within the area.
      * (operation 2 of the opening explanation)
      * @param area the area which might be contained.
      */
     public boolean contains(Area a) {
         
         //1) check whether at least one vertex is contained
         if (! contains(a.vertexs.get(0)))
             return false;
         
         //2) check that no edge of the the area intersects with an edge of this
         //area
         if (doEdgesIntersect(a)) return false;
         
         //3) check that no vertex of the implicit parameter is within the area
         //of the polygon
         for (GridPoint vertex : vertexs)
             if (a.contains(vertex)) return false;
 
         return true;       
     }
     
     
     /**
      * (operation 3 of the opening explanation)
      * @param area 
      */
     public void union(Area a) {
         Set<GridPoint> newAreaVertexs = new HashSet<GridPoint>();    
         
         //1) add intersections between this area and area, except double
         // intersections
         List<GridPoint> intersectPoints = getEdgesIntersect(a);
         for (GridPoint p : intersectPoints) {
             if (newAreaVertexs.contains(p))
                 newAreaVertexs.remove(p);
             else newAreaVertexs.add(p);
         }
         
         //2) find vextexs that have an odd number of adjacent contained squares
         // in either area
         for (GridPoint v : vertexs) {
             List<Boolean> thisAdjSqs = areAdjacentSquaresContained(v);
             List<Boolean> aAdjSqs = a.areAdjacentSquaresContained(v);
             int count = 0;
             if (thisAdjSqs.get(0) || aAdjSqs.get(0)) ++count;
             if (thisAdjSqs.get(1) || aAdjSqs.get(1)) ++count;
             if (thisAdjSqs.get(2) || aAdjSqs.get(2)) ++count;
             if (thisAdjSqs.get(3) || aAdjSqs.get(3)) ++count;
             if (count%2 == 1) newAreaVertexs.add(v);
         }
         for (GridPoint v : a.vertexs) {
             List<Boolean> thisAdjSqs = areAdjacentSquaresContained(v);
             List<Boolean> aAdjSqs = a.areAdjacentSquaresContained(v);
             int count = 0;
             if (thisAdjSqs.get(0) || aAdjSqs.get(0)) ++count;
             if (thisAdjSqs.get(1) || aAdjSqs.get(1)) ++count;
             if (thisAdjSqs.get(2) || aAdjSqs.get(2)) ++count;
             if (thisAdjSqs.get(3) || aAdjSqs.get(3)) ++count;
             if (count%2 == 1) newAreaVertexs.add(v);
         }
 
         List<GridPoint> newAreaVertexsList = new ArrayList<GridPoint>();
         newAreaVertexsList.addAll(newAreaVertexs);
         vertexs = newAreaVertexsList;
         initializeAreaFromVertexs();
     }
     
     
     /**
      * (operation 4 of the opening explanation)
      * @param area 
      */
     public void difference(Area a) {
         union(a);
         symmetricDifference(a);
     }
     
     
     /**
      * (operation 5 of the opening explanation)
      * @param area 
      */
     public void intersection(Area a) {
         //A^B = (A XOR B) XOR A+B
         Area union = new Area(vertexs);
         union.union(a);
         
         symmetricDifference(a);
         symmetricDifference(union);
     }
     
     
     private void initializeAreaFromVertexs() {
         
         //initialize maps of vertexs
         vertexsStoredByX = new HashMap<Integer,List<GridPoint>>();
         vertexsStoredByY = new HashMap<Integer,List<GridPoint>>();
         
         for (GridPoint p : vertexs) {
             if (!vertexsStoredByX.containsKey(p.x))
                 vertexsStoredByX.put(p.x, new ArrayList<GridPoint>());
             vertexsStoredByX.get(p.x).add(p);
             
             if (!vertexsStoredByY.containsKey(p.y))
                 vertexsStoredByY.put(p.y, new ArrayList<GridPoint>());
             vertexsStoredByY.get(p.y).add(p);
         }
 
         buildEdges();
         
         //initialize maps of edges
         verticalEdgesStoredByX = new HashMap<Integer,List<VerticalEdge>>();
         horizontalEdgesStoredByY = new HashMap<Integer,List<HorizontalEdge>>();
         
         for (VerticalEdge e : verticalEdges) {
             if (!verticalEdgesStoredByX.containsKey(e.x))
                 verticalEdgesStoredByX.put(e.x, new ArrayList<VerticalEdge>());
             verticalEdgesStoredByX.get(e.x).add(e);
         }
         for (HorizontalEdge e : horizontalEdges) {
             if (!horizontalEdgesStoredByY.containsKey(e.y))
                 horizontalEdgesStoredByY.put(e.y, new ArrayList<HorizontalEdge>());
             horizontalEdgesStoredByY.get(e.y).add(e);
         }
     }
 
     
     /**
      * Synchronizes the edges with the vertexs.
      * (operation 7 of the opening explanation)
      */
     private void buildEdges() {
         //restart edges
         verticalEdges = new ArrayList<VerticalEdge>();
         horizontalEdges = new ArrayList<HorizontalEdge>();
 
         for(GridPoint vertex : vertexs) {
 
             //vertical edge
             int VerticalCount = 0;
             GridPoint closestAbove = null;
             GridPoint closestBelow = null;
             //count how many vertexs with the same x value have area higher y value
             //and store the closest vertex
             for(GridPoint p : vertexsStoredByX.get(vertex.x)) {
                 if (p.y > vertex.y) {
                     ++VerticalCount;
                     //update the closest vertex, if apropiate
                     if (closestBelow == null || closestBelow.y > p.y)
                         closestBelow = p;
                 }
                 else if (p.y < vertex.y) {
                     if (closestAbove == null || closestAbove.y < p.y)
                         closestAbove = p;
                 }
             }
             if (VerticalCount % 2 == 1)
                 verticalEdges.add(new VerticalEdge(vertex.x, closestBelow.y, vertex.y));
             //else verticalEdges.add(new VerticalEdge(vertex.x, vertex.y, closestAbove.y));
 
             //horizontal edge
             int HorizontalCount = 0;
             GridPoint closestLeft = null;
             GridPoint closestRight = null;
             //count how many vertexs with the same y value have area higher x value
             //and store the closest vertex
             for(GridPoint p : vertexsStoredByY.get(vertex.y)) {
                 if (p.x > vertex.x) {
                     ++HorizontalCount;
                     //update the closest vertex, if apropiate
                     if (closestRight == null || closestRight.x > p.x)
                         closestRight = p;
                 }
                 else if (p.x < vertex.x) {
                     if (closestLeft == null || closestLeft.x < p.x)
                         closestLeft = p;
                 }
             }
             if (HorizontalCount % 2 == 1)
                 horizontalEdges.add(new HorizontalEdge(vertex.y, closestRight.x, vertex.x));
             //else horizontalEdges.add(new HorizontalEdge(vertex.y, vertex.x, closestLeft.x));              
         }
     }
     
     
     /**
      * Returns whether area given gridPoint is contained in the area.
      * @param point the grid point which might be contained.
      * @return true if point is contained in the polygon
      */
     private boolean contains(GridPoint point) {
         List<Square> adjacentSquares = adjacentSquares(point);
         for (Square sq : adjacentSquares)
             if (! contains(sq)) return false;
         
         return true;
     }
     
     /**
      * Given area Grid Point, returns whether each of the adjacent squares is
      * contained.
      * This is optimized acording to the following observation:
      * to see if area square is contained, if it is known whether an adjacent
      * square is contained or not, it must only be checked whether there
      * is an edge between them.
      * @param p
      * @return list with exactly 4 booleans. Each one indicates whether area
      * certain adjacent square is contained, in this order: top left square,
      * top right square, bottom left square and bottom right square.
      */
     private List<Boolean> areAdjacentSquaresContained(GridPoint p) {
         
         // +-------------+-------------+
         // |             |             |
         // |             |             |
         // |             u             |
         // | topLeftSq   p  topRightSq |
         // |             E             |
         // |             |             |
         // |             |             |
         // +----leftE----p---rightE----+
         // |             |             |
         // |             d             |
         // | bottomLeft  o bottomRight |
         // | Sq          w Sq          |
         // |             n             |
         // |             E             |
         // |             |             |
         // +-------------+-------------+
              
         //one boolean for each adjacent square: true if they are contained,
         //false otherwise
         boolean topLeftSq, topRightSq, bottomRightSq, bottomLeftSq;
         //one boolean for each adjacent line segment: true if they are part
         //of an edge, false otherwise
         //upE is not needed: we will reach topLeftSq through bottomLeftSq
         boolean rightE, downE, leftE;
         rightE = downE = leftE = false;
         
         //find which line segments are edges
         if (verticalEdgesStoredByX.containsKey(p.x)) {
             for (VerticalEdge v : verticalEdgesStoredByX.get(p.x))
                 if (v.contain(new VerticalEdge(p.x, p.y+1, p.y)))
                     downE = true;
         }
         if (horizontalEdgesStoredByY.containsKey(p.y)) {
             for (HorizontalEdge v : horizontalEdgesStoredByY.get(p.y)) {
                 if (v.contain(new HorizontalEdge(p.y, p.x+1, p.x)))
                     rightE = true;
                 if (v.contain(new HorizontalEdge(p.y, p.x, p.x-1)))
                     leftE = true;
             }
         }
         
         //check if bottomRight is contained with usual method
         bottomRightSq = contains(new Square(p.x, p.y));
         
         //bottomLeftSq will be the same as bottomRightSq unless downE is true
         if (downE) bottomLeftSq = ! bottomRightSq;
         else bottomLeftSq = bottomRightSq;
         
         //topRightSq will be the same as bottomRightSq unless rightE is true
         if (rightE) topRightSq = ! bottomRightSq;
         else topRightSq = bottomRightSq;
         
         //topLeftSq will be the same as bottomLeftSq unless leftE is true
         if (leftE) topLeftSq = ! bottomLeftSq;
         else topLeftSq = bottomLeftSq;
         
         List<Boolean> result = new ArrayList<Boolean>();
         result.add(topLeftSq);
         result.add(topRightSq);
         result.add(bottomLeftSq);
         result.add(bottomRightSq);
         
         return result;
         
     }
 
     /**
      * Returns whether 2 edges of different areas intersect somewhere in the
      * plane.
      * @param area
      * @return 
      */
     private boolean doEdgesIntersect(Area a) {
 
         for (VerticalEdge myEdge : verticalEdges)
             for (HorizontalEdge aEdge : a.horizontalEdges)
                 if (myEdge.intersects(aEdge)) return true;
         
         for (VerticalEdge aEdge : a.verticalEdges)
             for (HorizontalEdge myEdge : horizontalEdges)
                 if (aEdge.intersects(myEdge)) return true;
         
         return false;
     
     }
     
     
     /**
      * Returns all grid points where 2 edges of different areas intersect somewhere
      * in the plane.
      * @param area
      * @return 
      */
     private List<GridPoint> getEdgesIntersect(Area a) {
 
         List<GridPoint> intersectionPoints = new ArrayList<GridPoint>();
         
         for (VerticalEdge myEdge : verticalEdges)
             for (HorizontalEdge aEdge : a.horizontalEdges)
                 if (myEdge.intersects(aEdge))
                     intersectionPoints.add(myEdge.getIntersection(aEdge));
 
         for (VerticalEdge aEdge : a.verticalEdges)
             for (HorizontalEdge myEdge : horizontalEdges)        
                 if (aEdge.intersects(myEdge))
                     intersectionPoints.add(aEdge.getIntersection(myEdge));
         
         return intersectionPoints;
     }
 
     /**
      * Returns the list of squares adjacent to area given grid point.
      * @param v
      * @return 
      */
     private List<Square> adjacentSquares(GridPoint v) {
         List<Square> adjacentSquares = new ArrayList<Square>();
         adjacentSquares.add(new Square(v.x, v.y));
         adjacentSquares.add(new Square(v.x-1, v.y));
         adjacentSquares.add(new Square(v.x, v.y-1));
         adjacentSquares.add(new Square(v.x-1, v.y-1));
         
         return adjacentSquares;
     }
 
     private void symmetricDifference(Area a) {
         Set<GridPoint> vertexSet = new HashSet<GridPoint>();
         vertexSet.addAll(vertexs);
         
         for (GridPoint p : a.vertexs){
             if (vertexSet.contains(p)) vertexSet.remove(p);
             else vertexSet.add(p);
         }
         
         vertexs.clear();
         vertexs.addAll(vertexSet);
         initializeAreaFromVertexs();
     }
 
     
 
     /**
      * Operations to support iteration
      */
     
     
     /**
      * Returns whether the area is empty.
      * @return 
      */
     public boolean isEmpty() {
         return vertexs.isEmpty();
     }
     
     /**
      * Returns the smallest rectangle that contains this area.
      * @return 
      */
     public Rectangle getBoundingRectangle() {
         int maxX, minX, maxY, minY;
         maxX = minX = maxY = minY = 0;
         
         Iterator<GridPoint> it = vertexs.iterator();
         if (it.hasNext()) {
             GridPoint v = it.next();
             maxX = minX = v.x;
             maxY = minY = v.y;
             while (it.hasNext()) {
                 v = it.next();
                 if (v.x > maxX) maxX = v.x;
                 else if (v.x < minX) minX = v.x;
                 if (v.y > maxY) maxY = v.y;
                 else if (v.y < minY) minY = v.y;
             }
         }
         return new Rectangle(minX, minY, maxX-minX, maxY-minY);
     }
     
     /**
      * Returns an Orthogonal Area that contains the same geometry as this Area,
      * but shifted 'distance' units to 'orientation'.
      * @param distance
      * @param orientation
      * @return 
      */
     public void shift(int distance, Orientation orientation) {
        
         List<GridPoint> newVertexs = new ArrayList<GridPoint>();
         if (orientation == Orientation.N)
             for (GridPoint v : vertexs)
                 newVertexs.add(new GridPoint(v.x, v.y-distance));
         else if (orientation == Orientation.S)
             for (GridPoint v : vertexs)
                 newVertexs.add(new GridPoint(v.x, v.y+distance));
         else if (orientation == Orientation.W)
             for (GridPoint v : vertexs)
                 newVertexs.add(new GridPoint(v.x-distance, v.y));
         else if (orientation == Orientation.E)
             for (GridPoint v : vertexs)
                 newVertexs.add(new GridPoint(v.x+distance, v.y));
         
         vertexs = newVertexs;
         initializeAreaFromVertexs();
     }
 
     /**
      * Returns area probabilistic estimation of the area size.
      * SAMPLE_SIZE points from inside the bounding rectangle are chosen 
      * randomly. From these, an estimation of the density of area inside the
      * bounding rectangle is infered.
      * 
      * This can be optimized using uniformly distributed squares.
      * 
      * @return an estimation of the size.
      */
     public int areaSize() {
         if (vertexs.isEmpty()) return 0;
         
         Debug.println(this.toString());
         Rectangle boundingRectangle = getBoundingRectangle();
         int xMin = boundingRectangle.x;
         int xMax = boundingRectangle.x + boundingRectangle.width;
         int yMin = boundingRectangle.y;
         int yMax = boundingRectangle.y + boundingRectangle.height;
         
         Random randomGenerator = new Random();
         int squareDensity = 0;
         for (int i = 0; i < SAMPLE_SIZE; ++i) {
             int Xrand = xMin + randomGenerator.nextInt(boundingRectangle.width);
             int Yrand = yMin + randomGenerator.nextInt(boundingRectangle.height);
             Debug.println("Random point: " + Xrand + ","+Yrand);
             if (contains(new Square(Xrand, Yrand))) ++squareDensity;
         }
         
         int boundingRectangleSize = (xMax - xMin) * (yMax - yMin);
         Debug.println("boundingRectangleSize: " + boundingRectangleSize);
         Debug.println("squareDensity: " + squareDensity + "/" + SAMPLE_SIZE);
         
         return (boundingRectangleSize * squareDensity) / SAMPLE_SIZE;
     }
 
     /**
      * Pre: this area is a rectangle, distance is not larger than min(width, depth).
      * Expands the rectangle distance units in all directions.
      * @param distance
      * @return 
      */
     public void expand(int distance) {
         Area result = new Area(vertexs);
 
         Area aux;
         
         aux = new Area(this);
         aux.shift(distance, Orientation.N);
         result.union(aux);
         
         aux = new Area(this);
         aux.shift(distance, Orientation.S);
         result.union(aux);
         
         aux = new Area(this);
         aux.shift(distance, Orientation.W);
         result.union(aux);
         
         aux = new Area(this);
         aux.shift(distance, Orientation.E);
         result.union(aux);
 
         vertexs = result.vertexs;
         initializeAreaFromVertexs();
     }
     
     public float distance(Point sq1, Point sq2) {
         return euclideanDistance(new Square(sq1.x, sq1.y), new Square(sq2.x, sq2.y));
     }
 
     private float euclideanDistance(Square sq1, Square sq2) {
         float xOffset = Math.abs(sq1.x - sq2.x) - 1;
         //-1 because squares are 1 unit wide
         float yOffset = Math.abs(sq1.y - sq2.y) - 1;
         return (float) Math.sqrt(xOffset*xOffset + yOffset*yOffset);
     }
     
     public static float distance(Rectangle r1, Rectangle r2) {
         //find the cloosest point of r1 to r2
         //TODO: Im getting tired and dont want to introduce bugs xd.
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public Iterator<Point> iterator() {
         return new AreaIterator();
     }
 
     
     
     /**
      * Area iterator through all contained squares.
      * The area iterates through squares, but returns them as points.
      */
     private class AreaIterator
         implements Iterator<Point> {
         
         private Square currentSquare;
         
         private int xMin, xMax;
         private int yMin, yMax;
         
         private static final int RES = 5;
         
         public AreaIterator() {
 
             Rectangle boundingRectangle = getBoundingRectangle();
             
             xMin = boundingRectangle.x;
             xMax = xMin + boundingRectangle.width;
             yMin = boundingRectangle.y;
             yMax = yMin + boundingRectangle.height;
             
             currentSquare = new Square(xMin - RES, yMin);
             advanceToNext();
         }
 
         @Override
         public boolean hasNext() {
             return contains(currentSquare);
         }
     
         @Override
         public Point next() {            
             if (contains(currentSquare)) {
                 Point ret = new Point(currentSquare.x, currentSquare.y);
                 advanceToNext();
                 return ret;
             }
             else throw new NoSuchElementException();
         }
 
         @Override
         public void remove() {
             throw new UnsupportedOperationException("Removal of a particular element in the domain is not supported");
         }
         
         private void advanceToNext() {
             // First, we advance towards the next position
             currentSquare.x += RES;
             
             while (currentSquare.y <= yMax) {
                 while (currentSquare.x <= xMax) {
                     if (contains(currentSquare)) return;  
                     else {
                         Square next = nextContained(currentSquare);
                         if (next != null) currentSquare = next;
                         else currentSquare.x = xMax + 1;
                     }
                 } 
                 currentSquare.y += RES;
                 currentSquare.x = xMin;
             }
         }
 
         /**
          * Given that square is a non-contained square of this area, returns the
          * next contained square in the same row, assuming squares are sorted
          * from left to right.
          * To find the next square, we have to find the first edge that
          * intersects the ray projected by the square to the right.
          * If there is none, returns null.
          * @param square
          * @return 
          */
         private Square nextContained(Square square) {
             //iterate through all vertical edges and store the x coordinate of
             //the edge that intersects the ray and is more to the left
             
             boolean found = false;
             int mostLeftX = -1; //arbitrary initialization; value is only
                                 //relevant if found = true
             for (VerticalEdge edge : verticalEdges) {
                 if (edge.intersects(new RightRay(square))) {
                     if (! found || mostLeftX < edge.x) {
                         found = true;
                         mostLeftX = edge.x;
                     }
                 }
             }
             if (! found) return null;
             return new Square(mostLeftX, square.y);
         }
         
     }
     
     @Override
     public String toString() {
         StringBuilder result = new StringBuilder();
         String nl = System.getProperty("line.separator");
 
         result.append("Area:" + nl);
         result.append("List of vertexs:" + nl);
         for (GridPoint vertex : vertexs)
             result.append(vertex.toString() + "\t");
         
         result.append(nl + "List of vertical segments:" + nl);
         for (VerticalEdge edge : verticalEdges)
             result.append(edge.toString() + "\t");
         result.append(nl);
         return result.toString();
     }
 
 }

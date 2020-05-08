 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package geomatrix.business.models.binary.geomatrix;
 
 import geomatrix.business.models.binary.Area;
 import geomatrix.business.models.binary.Cell;
 import geomatrix.business.models.binary.CellSet;
 import geomatrix.business.models.binary.HorizontalSegment;
 import geomatrix.business.models.binary.GridPoint;
 import geomatrix.business.models.binary.Rectangle;
 import geomatrix.business.models.binary.VerticalSegment;
 import geomatrix.business.models.binary.WideRay;
 import geomatrix.utils.Direction;
 import geomatrix.utils.Line;
 import geomatrix.utils.Pair;
 import java.awt.Point;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Queue;
 import java.util.Set;
 import manticore.Debug;
 
 /**
  * Area implementation of the master interface.
  * @author Nil
  */
 public class Geomatrix implements Area {
 
     private List<GridPoint> vertexs;
     private List<VerticalSegment> verticalEdges;
     private List<HorizontalSegment> horizontalEdges;
     
     /**
      * The smallest rectangle that contains this area.
      */
     Rectangle boundingRectangle;
     
     /**
      * This list contains one arbitrary vertex from each path of this area.
      * A path is a closed cicle of edges.
      */
     private List<GridPoint> pathRepresentatives;
     
     /**
      * Given first x' value, permits fast access to all vertexs of the area
      * of the form (x',y).
      */
     private HashMap<Integer,List<GridPoint>> vertexsStoredByX;
     
     /**
      * Given first y' value, permits fast access to all vertexs of the area
      * of the form (x,y').
      */
     private HashMap<Integer,List<GridPoint>> vertexsStoredByY;
     
     /**
      * Given first x' value, permits fast access to all vertical edges of the area
      * of the form (x',y).
      */
     private HashMap<Integer,List<VerticalSegment>> verticalEdgesStoredByX;
  
     /**
      * Given first y' value, permits fast access to all horizontal edges of the area
      * of the form (x,y').
      */
     private HashMap<Integer,List<HorizontalSegment>> horizontalEdgesStoredByY;
     
     /**
      * Empty constructor.
      */
     public Geomatrix() {
         build(new ArrayList<GridPoint>());
     }
     
     public Geomatrix(Collection<Point> points) {
         List<GridPoint> newVertexs = new ArrayList<GridPoint>();
         for (Point point : points) {
             newVertexs.add(new GridPoint(point.x, point.y));
         }
         build(newVertexs);
     }
     
     /**
      * Returns a copy of original.
      * @param original the geomatrix to be copied.
      * @return a copy of original.
      */
     public static Geomatrix copy(Geomatrix original) {
         Geomatrix g = new Geomatrix();
         g.build(original.vertexs);
         return g;
     }
     
     public List<GridPoint> getVertexs() {
         return vertexs;
     }
         
     /**
      * Initializes the Geomatrix from the list of its vertexs.
      * @param vertexs the vertexs that this geomatrix has to have.
      */
     private void build(List<GridPoint> vertexs) {
         this.vertexs = new ArrayList<GridPoint>(vertexs);
         initializeDataStructsFromVertexs();
     }
     
     @Override
     public boolean contains(Cell cell) {
         //apply ray-casting algorithm; direction: negative side of the x axis
         WideRay ray = new WideRay(cell, Direction.W);
         
         int intersectionCount = 0;
         for (VerticalSegment edge : verticalEdges) {
             if (edge.intersects(ray)) ++intersectionCount;
             if (edge.x > cell.x) break;
         }
         
         //return result according to the odd-even rule
         return (intersectionCount % 2) == 1;
     }
 
     @Override
     public boolean contains(CellSet c) {
         assert(c instanceof Geomatrix);
         Geomatrix other = (Geomatrix) c;
         
         if (other.isEmpty()) return true;
         
         //1) check whether one vertex of other is contained in this
         if (! contains(other.vertexs.get(0)))
             return false;
         
         //2) check that no edge of other intersectsLeftWideRay with an edge of this
         //area
         if (doEdgesIntersect(other)) return false;
         
         //3) check that no path of this geomatrix is within other.
         for (GridPoint vertex : pathRepresentatives)
             if (other.contains(vertex)) return false;
 
         return true; 
     }
 
     @Override
     public void union(CellSet c) {
         assert(c instanceof Geomatrix);
         Geomatrix other = (Geomatrix) c;
         
         if (isEmpty()) {
             build(other.vertexs);
             return;
         }
             
         Set<GridPoint> newAreaVertexs = new HashSet<GridPoint>();
                 
         //1) add intersections between this area and a, except double
         //intersections
         List<GridPoint> intersectPoints = getEdgesIntersect(other);
         for (GridPoint v : intersectPoints) {
             if (newAreaVertexs.contains(v))
                 newAreaVertexs.remove(v);
             else
                 newAreaVertexs.add(v);
         }     
         
         //2) find vextexs that have an odd number of adjacent contained squares
         //in either area
         for (GridPoint myVertex : vertexs) {
             List<Boolean> otherAdjCells = other.areAdjacentCellsContained(myVertex);
             
             if (noneTrue(otherAdjCells)) {
                 //if this vertex is disjoint from other, it is a vertex of the
                 //union
                 newAreaVertexs.add(myVertex);
             }
             //if this vertex is contained in other, it is not a vertex of
             //the union
             else if (!allTrue(otherAdjCells)) {
                 List<Boolean> thisAdjCells = areAdjacentCellsContained(myVertex);
                 int count = 0;
                 if (thisAdjCells.get(0) || otherAdjCells.get(0)) ++count;
                 if (thisAdjCells.get(1) || otherAdjCells.get(1)) ++count;
                 if (thisAdjCells.get(2) || otherAdjCells.get(2)) ++count;
                 if (thisAdjCells.get(3) || otherAdjCells.get(3)) ++count;
                 if (count%2 == 1) newAreaVertexs.add(myVertex);
             }
         }
         
         for (GridPoint otherVertex : other.vertexs) {
             List<Boolean> thisAdjCells = areAdjacentCellsContained(otherVertex);
             
             if (noneTrue(thisAdjCells)) {
                 //if otherVertex is disjoint from this area, it is a vertex of 
                 //the union
                 newAreaVertexs.add(otherVertex);
             }
             //if otherVertex is contained in this area, it is not a vertex of
             //the union
             else if (!allTrue(thisAdjCells)) {
                 List<Boolean> otherAdjCells = other.areAdjacentCellsContained(otherVertex);
                 int count = 0;
                 if (thisAdjCells.get(0) || otherAdjCells.get(0)) ++count;
                 if (thisAdjCells.get(1) || otherAdjCells.get(1)) ++count;
                 if (thisAdjCells.get(2) || otherAdjCells.get(2)) ++count;
                 if (thisAdjCells.get(3) || otherAdjCells.get(3)) ++count;
                 if (count%2 == 1) newAreaVertexs.add(otherVertex);
             }
         }
         
         build(new ArrayList(newAreaVertexs));
     }
 
     @Override
     public void intersection(CellSet c) {
         assert(c instanceof Geomatrix);
         Geomatrix other = (Geomatrix) c;
         
         //A^B = (A XOR B) XOR A+B
         Geomatrix thisUnionOther = copy(this);
         thisUnionOther.union(other);
         
         symmetricDifference(other);
         symmetricDifference(thisUnionOther); 
     }
 
     @Override
     public void difference(CellSet c) {
         assert(c instanceof Geomatrix);
         Geomatrix other = (Geomatrix) c;
         
         union(other);
         symmetricDifference(other);
     }
 
     @Override
     public Iterator<Cell> iterator() {
         //return new GeomatrixIterator();
         return new AreaIterator();
     }
     
     @Override
     public void translation(GridPoint p) {
         List<GridPoint> newVertexs = new ArrayList<GridPoint>();
         for (GridPoint v : vertexs) {
             newVertexs.add(new GridPoint(v.x + p.x, v.y + p.y));
         }
         build(newVertexs);
     }
 
     @Override
     public void verticalReflection() {
         List<GridPoint> newVertexs = new ArrayList<GridPoint>();
         for (GridPoint v : vertexs) {
             newVertexs.add(new GridPoint(-v.x, v.y));
         }
         build(newVertexs);
     }
 
     @Override
     public void horizontalReflection() {
         List<GridPoint> newVertexs = new ArrayList<GridPoint>();
         for (GridPoint v : vertexs) {
             newVertexs.add(new GridPoint(v.x, -v.y));
         }
         build(newVertexs);
     }
 
     @Override
     public void rotation(int degrees) {
         List<GridPoint> newVertexs = new ArrayList<GridPoint>();
         
         degrees = degrees % 360;
         if (degrees < 0) degrees += 360;
         
         if (degrees == 90) {
             for (GridPoint v : vertexs) {
                 newVertexs.add(new GridPoint(-v.y, v.x));
             }
         }
         else if (degrees == 180) {
             for (GridPoint v : vertexs) {
                 newVertexs.add(new GridPoint(-v.x, -v.y));
             }            
         }
         else if (degrees == 270) {
             for (GridPoint v : vertexs) {
                 newVertexs.add(new GridPoint(v.y, -v.x));
             }            
         }
         else {
             assert(degrees == 0);
         }
         
         build(newVertexs);
     }
 
     @Override
     public Rectangle getBoundingRectangle() {
         return boundingRectangle;
     }
 
     @Override
     public List<Rectangle> descomposition() {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public Iterator<GridPoint> iterator(Rectangle rectangle) {
         /*
          * Idea for algorithm:
          * Compute area A.
          * Iterate through the contained cells of A with the usual iterator, but
          * return cells as points.
          * 
          * the area A is computed as follows:
          * A is this area minus each area of a collection of areas CA.
          * 
          * The collection of areas CA is computed as follows: 
          * There is an area in CA for each S
          * 
          * Where S is one of the following four possibilities:
          * a vertical edge not intersected by any other edge with the contained
          * adjacent cells to its right.
          * a portiion of vertical edge intersected by another edge, between two
          * intersections or endpoints such that the contained adjacent cells are
          * found to its right.
          * the equialent but for horizontal edges with the contained adjacent
          * cells below it.
          * 
          * From each S, the associated area in CA is calculated as follows:
          * if S is a vertical segment:
          * S is enlarged upwards the height of rectangle -1.
          * The area is a rectangle that has S as right edge, and width the width
          * of rectangle -1 (all the edges can be deduced by this)
          */
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public void symmetricDifference(CellSet c) {
         assert(c instanceof Geomatrix);
         Geomatrix other = (Geomatrix) c;
         
         Set<GridPoint> vertexsSet = new HashSet<GridPoint>();
         vertexsSet.addAll(vertexs);
         
         for (GridPoint p : other.vertexs){
             if (vertexsSet.contains(p)) vertexsSet.remove(p);
             else vertexsSet.add(p);
         }
         
         build(new ArrayList(vertexsSet)); 
     }
 
     @Override
     public int size() {
         /*
          * 
          * DOES NOT WORK DUE TO HOLES
          * 
          * Idea for algorithm:
          * put all edges in a collection CS of segments; in case of intersecting
          * edges, put the part of the segment to each side of the intersection
          * in CS separately.
          * Now, for each concave vertex v (those with 3 adjacent contained cells)
          * propagate a ray in the opposed directions of the direction where its
          * associated edges are; add to CS the segments between v and the first
          * intersection of the rays.
          * 
          * Now, find all the grid points where the ending point of two or more
          * segments of CS meet, and indicate for each of them how many. Put the
          * grid points in a collection CP
          * 
          * while CP is not empty:
          * find a point p in CP that is endpoint of exactly 2 segments of CS.
          * Create the rectangle characterized by the 2 segments (by deducing the
          * remaining 2 segments) and put it in a collection of rectangles CR;
          * delete p from CP and the 2 segments from CS;
          * update the number of ending points for each point in CP now that 2
          * segments have been deleted;
          * if any point in CP is endpoint of exactly one segment, delete it from
          * CP.
          * 
          * Build an area A as the union of all the areas defined by each
          * rectangle in CR.
          * 
          * A = A\this area
          * now A is the holes of this area
          * 
          * Fragment A in rectangles as done above in a collection of rectangles
          * CA.
          * 
          * Now calculate the size as CR.totalSize() - CA.totalSize()
          * 
          * Where the size of a collection of rectangles is the sum of the size
          * of the each rectangle (height*width).
          * 
          * 
          * 
          * Algorithm 2:
          * return descomposition().totalSize()
          * 
          * Sadly, descomposition has not been implemented yet :(
          */
         throw new UnsupportedOperationException("Not supported yet.");
     }
     
     /**
      * Returns whether this geomatrix is consistent.
      * pre: the method initializeDataStructsFromVertexs works
      * @return true if this geomatrix is consistent. false otherwise.
      */
     public boolean valid() {
         HashSet<Integer> verticalLines = new HashSet<Integer>();
         HashSet<Integer> horizontalLines = new HashSet<Integer>();
         
         for (GridPoint vertex : vertexs) {
             if (verticalLines.contains(vertex.x))
                 verticalLines.remove(vertex.x);
             else verticalLines.add(vertex.x);
             if (horizontalLines.contains(vertex.y))
                 horizontalLines.remove(vertex.y);
             else horizontalLines.add(vertex.y);
         }
         
         boolean result = verticalLines.isEmpty() && horizontalLines.isEmpty();
         return result;
     }
     
     public static List<Line> getInvalidLines(Set<GridPoint> vertexs) {
         HashSet<Line> lines = new HashSet<Line>();
         
         for (GridPoint vertex : vertexs) {
             Line verticalLine = new Line(true, vertex.x);
             if (lines.contains(verticalLine))
                 lines.remove(verticalLine);
             else lines.add(verticalLine);
             
             Line horizontalLine = new Line(false, vertex.y);
             if (lines.contains(horizontalLine))
                 lines.remove(horizontalLine);
             else lines.add(horizontalLine);
         }
 
         List<Line> result = new ArrayList<Line>(lines);
         return result;        
     }
 
     @Override
     public String toString() {
         StringBuilder result = new StringBuilder();
         String nl = System.getProperty("line.separator");
 
         result.append("Area:" + nl);
         result.append("List of vertexs:" + nl);
         for (GridPoint vertex : vertexs) {
             result.append(vertex.toString() + "\t");
         }
 
         result.append(nl + "List of vertical segments:" + nl);
         for (VerticalSegment edge : verticalEdges) {
             result.append(edge.toString() + "\t");
         }
         result.append(nl);
         return result.toString();
     }
         
     private void initializeDataStructsFromVertexs() {
         assert(valid());
         initializeMapsOfVertexsAndBoundingRectangle();
         initializeEdgesAndPathRepresentatives();
         sortVerticalEdges();
         initializeMapsOfEdges();
     }
 
     /**
      * Initializes vertexsStoredByX and vertexsStoredByY and the bounding
      * rectangle in the same loop.
      */
     private void initializeMapsOfVertexsAndBoundingRectangle() {
         vertexsStoredByX = new HashMap<Integer,List<GridPoint>>();
         vertexsStoredByY = new HashMap<Integer,List<GridPoint>>();
         
         Integer maxX, minX, maxY, minY;
         maxX = minX = maxY = minY = null;
         
         for (GridPoint p : vertexs) {
             if (!vertexsStoredByX.containsKey(p.x))
                 vertexsStoredByX.put(p.x, new ArrayList<GridPoint>());
             vertexsStoredByX.get(p.x).add(p);
             
             if (!vertexsStoredByY.containsKey(p.y))
                 vertexsStoredByY.put(p.y, new ArrayList<GridPoint>());
             vertexsStoredByY.get(p.y).add(p);
             
             //part corresponding to the bounding box
             if (maxX == null || p.x > maxX) {
                 maxX = p.x;
             } else if (minX == null || p.x < minX) {
                 minX = p.x;
             }
             if (maxY == null || p.y > maxY) {
                 maxY = p.y;
             } else if (minY == null || p.y < minY) {
                 minY = p.y;
             }
         }
         
         if (maxX == null) boundingRectangle = new Rectangle(new GridPoint(0,0), new GridPoint(0,0));
         else boundingRectangle = new Rectangle(new GridPoint(minX, minY), new GridPoint(maxX, maxY));
     }
 
     /**
      * Initializes the vertical and horizontal edges and path representatives.
      * To initialize the path representatives, an arbitrary vertex from each
      * path is stored in pathRepresentatives.
      * Pre: vertexsStoredByX and vertexsStoredByY have been initialized.
      * 
      */
     private void initializeEdgesAndPathRepresentatives() {
         verticalEdges = new ArrayList<VerticalSegment>();
         horizontalEdges = new ArrayList<HorizontalSegment>();
         pathRepresentatives = new ArrayList<GridPoint>();
         Map<GridPoint,Boolean> visitedVertexs = new HashMap<GridPoint,Boolean>();
         for (GridPoint vertex : vertexs) {
             visitedVertexs.put(vertex, Boolean.FALSE);
         }
 
         Iterator<GridPoint> it = vertexs.iterator();
         while (it.hasNext()) {
             
             GridPoint vertex = it.next();
             if (! visitedVertexs.get(vertex)) {
                 pathRepresentatives.add(vertex);
             }
             
             while (! visitedVertexs.get(vertex)) {
                 //in each iteration of this loop, we add a vertical and a
                 //horizontal edge with a common associated vertex
                 
                 visitedVertexs.put(vertex, Boolean.TRUE);                
                 VerticalSegment verticalEdge = findVerticalEdge(vertex);
                 verticalEdges.add(verticalEdge);
                 vertex = verticalEdge.getOtherEndpoint(vertex);
                 
                 visitedVertexs.put(vertex, Boolean.TRUE);
                 HorizontalSegment horizontalEdge = findHorizontalEdge(vertex);
                 horizontalEdges.add(horizontalEdge);
                 vertex = horizontalEdge.getOtherEndpoint(vertex);
             }           
         }
     }
 
     /**
      * Given a vertex, returns the vertical edge associated to it.
      * This method is used to initialize the edges. It is not intended to get
      * the associated vertical edge once the edges have been initialized.
      * @param vertex
      * @return 
      */
     private VerticalSegment findVerticalEdge(GridPoint vertex) {
         Pair<Integer, Pair<GridPoint, GridPoint>> result = countVertexsSouthAndGetClosestSouthAndNorth(vertex);
         int southCount = result.first;
         GridPoint closestSouth = result.second.first;
         GridPoint closestNorth = result.second.second;
         if (isTopEndpoint(southCount)) {
             return new VerticalSegment(vertex.x, vertex.y, closestSouth.y);
         }
         else {
             return new VerticalSegment(vertex.x, vertex.y, closestNorth.y);
         }
     }
         
     /**
      * Returns the number of vertexs directly south of 'vertex' and the closest
      * vertexs directly south and north of 'vertex' or null if there is none.
      * @param vertex
      * @return the number of vertexs directly south of 'vertex' and the closest
      * vertexs directly south and north of 'vertex' or null if there is none.
      */
     private Pair<Integer, Pair<GridPoint, GridPoint>> countVertexsSouthAndGetClosestSouthAndNorth(GridPoint vertex) {
                                               
         int southCount = 0;
         GridPoint closestSouth = null;
         GridPoint closestNorth = null;
         for(GridPoint p : vertexsStoredByX.get(vertex.x)) {
             if (p.y > vertex.y) {
                 ++southCount;
                 if (closestSouth == null || closestSouth.y > p.y) {
                     closestSouth = p;
                 }
             }
             else if (p.y < vertex.y) {
                 if (closestNorth == null || closestNorth.y < p.y) {
                     closestNorth = p;
                 }
             }
         }
         return new Pair<Integer, Pair<GridPoint, GridPoint>>(southCount,
                 new Pair<GridPoint, GridPoint>(closestSouth, closestNorth));
     }
     
     /**
      * Returns whether a vertex with 'southCount' other vertexes directly below
      * it is the top endpoint of its associated vertical edge.
      * A vertex is the top endpoint of its associated vertical edge if there is
      * an odd number of vertexes directly below it.
      * @param southCount Indicates how many vertexes there are directly below
      * the vertex.
      * @return true if a vertex with 'southCount' other vertexes directly below
      * it is the top endpoint of its associated vertical edge. false otherwise.
      */
     private boolean isTopEndpoint(int southCount) {
         return southCount%2 == 1;
     }
          
     /**
      * Given a vertex, returns the horizontal edge associated to it.
      * This method is used to initialize the edges. It is not intended to get
      * the associated horizontal edge once the edges have been initialized.
      * @param vertex
      * @return 
      */
     private HorizontalSegment findHorizontalEdge(GridPoint vertex) {
         Pair<Integer, Pair<GridPoint, GridPoint>> result = countVertexsEastAndGetClosestEastAndWest(vertex);
         int eastCount = result.first;
         GridPoint closestEast = result.second.first;
         GridPoint closestWest = result.second.second;
         if (isLeftEndpoint(eastCount)) {
             return new HorizontalSegment(vertex.y, vertex.x, closestEast.x);
         }
         else {
             return new HorizontalSegment(vertex.y, vertex.x, closestWest.x);
         }
     }
 
     /**
      * Returns the number of vertexs directly east of 'vertex' and the closest
      * vertexs directly east and west of 'vertex' or null if there is none.
      * @param vertex
      * @return the number of vertexs direectly east of 'vertex' and the closest
      * vertexs directly east and west of 'vertex' or null if there is none.
      */
     private Pair<Integer, Pair<GridPoint, GridPoint>> countVertexsEastAndGetClosestEastAndWest(GridPoint vertex) {
                                               
         int eastCount = 0;
         GridPoint closestEast = null;
         GridPoint closestWest = null;
         for(GridPoint p : vertexsStoredByY.get(vertex.y)) {
             if (p.x > vertex.x) {
                 ++eastCount;
                 if (closestEast == null || closestEast.x > p.x) {
                     closestEast = p;
                 }
             }
             else if (p.x < vertex.x) {
                 if (closestWest == null || closestWest.x < p.x) {
                     closestWest = p;
                 }
             }
         }
         return new Pair<Integer, Pair<GridPoint, GridPoint>>(eastCount,
                 new Pair<GridPoint, GridPoint>(closestEast, closestWest));
     }
 
     /**
      * Returns whether a vertex with 'eastCount' other vertexes directly to the
      * right of it is the left endpoint of its associated horizontal edge.
      * A vertex is the left endpoint of its associated vertical edge if there is
      * an odd number of vertexes directly east of it.
      * @param eastCount Indicates how many vertexes there are directly east of
      * the vertex.
      * @return true if a vertex with 'eastCount' other vertexes directly east of
      * it is the left endpoint of its associated horizontal edge. false
      * otherwise.
      */
     private boolean isLeftEndpoint(int eastCount) {
         return eastCount%2 == 1;
     }
 
     /**
      * Sorts vertical edges by their x coordinate.
      */
     private void sortVerticalEdges() {
         Collections.sort(verticalEdges);
     }
         
     /**
      * Initializes verticalEdgesStoredByX and verticalEdgesStoredByY.
      */
     private void initializeMapsOfEdges() {
         verticalEdgesStoredByX = new HashMap<Integer,List<VerticalSegment>>();
         horizontalEdgesStoredByY = new HashMap<Integer,List<HorizontalSegment>>();
         
         for (VerticalSegment e : verticalEdges) {
             if (!verticalEdgesStoredByX.containsKey(e.x))
                 verticalEdgesStoredByX.put(e.x, new ArrayList<VerticalSegment>());
             verticalEdgesStoredByX.get(e.x).add(e);
         }
         for (HorizontalSegment e : horizontalEdges) {
             if (!horizontalEdgesStoredByY.containsKey(e.getY()))
                 horizontalEdgesStoredByY.put(e.getY(), new ArrayList<HorizontalSegment>());
             horizontalEdgesStoredByY.get(e.getY()).add(e);
         }
     }
     
     /**
      * Returns whether a given grid point is contained in the area.
      * @param point the grid point which might be contained.
      * @return true if point is contained in the area. false otherwise.
      */
     private boolean contains(GridPoint point) {
         List<Boolean> adjacentSquares = areAdjacentCellsContained(point);
         for (Boolean b : adjacentSquares)
             if (! b) return false; 
         
         return true;
     }
 
     /**
      * Given a grid point, returns whether each of the four adjacent cells is
      * contained.
      * This is optimized acording to the following observation:
      * to see if a cell is contained, if it is known whether an adjacent
      * cell is contained or not, it must only be checked whether there
      * is an edge between them.
      * @param point the point the contention of the adjacent cells of which is
      * to be checked.
      * @return list with exactly 4 booleans indicating whether the top left 
      * cell, top right cell, bottom left cell and bottom right cell are
      * contained, in this order.
      */
     private List<Boolean> areAdjacentCellsContained(GridPoint point) {
                 
         // +-------------+-------------+
         // |             |             |
         // |             |             |
         // |             u             |
         // |   topLeft   p  topRight   |
         // |             E             |
         // |             |             |
         // |             |             |
         // +----leftE----p---rightE----+
         // |             |             |
         // |             d             |
         // |             o             |
         // | bottomLeft  w bottomRight |
         // |             n             |
         // |             E             |
         // |             |             |
         // +-------------+-------------+
              
         //one boolean for each adjacent cell: true if they are contained,
         //false otherwise
         boolean topLeft, topRight, bottomRight, bottomLeft;
         //one boolean for each adjacent line segment: true if they are part
         //of an edge, false otherwise
         //upE is not needed: we will reach the top left cell through the bottom
         //left cell
         boolean rightE, downE, leftE;
         rightE = downE = leftE = false;
         
         //find which line segments are edges
         if (verticalEdgesStoredByX.containsKey(point.x)) {
             for (VerticalSegment v : verticalEdgesStoredByX.get(point.x))
                 if (v.contains(new VerticalSegment(point.x, point.y+1, point.y)))
                     downE = true;
         }
         if (horizontalEdgesStoredByY.containsKey(point.y)) {
             for (HorizontalSegment v : horizontalEdgesStoredByY.get(point.y)) {
                 if (v.contains(new HorizontalSegment(point.y, point.x+1, point.x)))
                     rightE = true;
                 if (v.contains(new HorizontalSegment(point.y, point.x, point.x-1)))
                     leftE = true;
             }
         }
         
         //check if bottomRight is contained with usual method
         bottomRight = contains(new Cell(point.x, point.y));
         
         //bottomLeftSq will be the same as bottomRightSq unless downE is true
         if (downE) bottomLeft = ! bottomRight;
         else bottomLeft = bottomRight;
         
         //topRightSq will be the same as bottomRightSq unless rightE is true
         if (rightE) topRight = ! bottomRight;
         else topRight = bottomRight;
         
         //topLeftSq will be the same as bottomLeftSq unless leftE is true
         if (leftE) topLeft = ! bottomLeft;
         else topLeft = bottomLeft;
         
         List<Boolean> result = new ArrayList<Boolean>();
         result.add(topLeft);
         result.add(topRight);
         result.add(bottomLeft);
         result.add(bottomRight);
         
         return result;
     }
     
     /**
      * Returns whether 2 edges of different areas intersect somewhere in the
      * plane.
      * @param other
      * @return 
      */
     private boolean doEdgesIntersect(Geomatrix other) {
         for (VerticalSegment myEdge : verticalEdges) {
             boolean betterByCoords = shouldIterateEdgesByCoords(myEdge.yInterval.size(), other.horizontalEdges.size());
             if (! betterByCoords) {
                 for (HorizontalSegment otherEdge : other.horizontalEdges) {
                     if (myEdge.intersects(otherEdge)) return true;
                 }
             }
             else {
                 for (int y = myEdge.yInterval.low+1; y < myEdge.yInterval.high; ++y) {
                     for (HorizontalSegment otherEdge : other.horizontalEdgesStoredByY.get(y)) {
                         if (myEdge.intersects(otherEdge)) return true;
                     }
                 }
             }
         }
         
         for (VerticalSegment otherEdge : other.verticalEdges) {
             boolean betterByCoords = shouldIterateEdgesByCoords(otherEdge.yInterval.size(), horizontalEdges.size());
             if (! betterByCoords) {
                 for (HorizontalSegment myEdge : horizontalEdges) {
                     if (otherEdge.intersects(myEdge)) return true;
                 }
             }
             else {
                 for (int y = otherEdge.yInterval.low+1; y < otherEdge.yInterval.high; ++y) {
                     for (HorizontalSegment myEdge : horizontalEdgesStoredByY.get(y)) {
                         if (otherEdge.intersects(myEdge)) return true;
                     }
                 }                
             }
         }
         
         return false;    
     }
 
     /**
      * In the context of finding the edge intersections of an edge e in respect
      * to the set of edges S of the opposed kind, returns whether it is more
      * efficient to check only the edges of S such that their fixed coordinate
      * falls within the yInterval of coordinates of e (with the
      * edgesSortedByCoordinate HashMaps) or to check all edges in S.
      * This second case is preferable when e is very large in comparison
      * to the size of S.
      * The optimum choice depends on the particular distribution of the edges of
      * S, so this method returns only an estimation. In particular, returns that
      * it is preferable to iterate through the coordinate-matching edges instead
      * of through all vertexs if the lentgh of e is smaller than 3/4 of the size
      * of S
      * @param edgeLength the lentgh of e.
      * @param numberOfEdges the size of S.
      * @return true if it is more efficient to check only the
      * coordinate-matching edges instead of all edges.
      */
     private boolean shouldIterateEdgesByCoords(int edgeLength, int numberOfEdges) {
         return edgeLength < (3*numberOfEdges)/4;
     }
         
     /**
      * Returns all grid points where 2 edges of this area and other intersect
      * somewhere in the grid.
      * @param other
      * @return 
      */
     private List<GridPoint> getEdgesIntersect(Geomatrix other) {
         List<GridPoint> intersectionPoints = new ArrayList<GridPoint>();
         
         for (VerticalSegment myEdge : verticalEdges) {
             boolean betterByCoords = shouldIterateEdgesByCoords(myEdge.yInterval.size(), other.horizontalEdges.size());
             if (! betterByCoords) {
                 for (HorizontalSegment otherEdge : other.horizontalEdges) {
                     if (myEdge.intersects(otherEdge)) {
                         intersectionPoints.add(myEdge.getIntersection(otherEdge));
                     }
                 }
             }
             else {
                 for (int y = myEdge.yInterval.low+1; y < myEdge.yInterval.high; ++y) {
                    List<HorizontalSegment> otherHorizontalEdgesAtY =
                            other.horizontalEdgesStoredByY.get(y);
                    if (otherHorizontalEdgesAtY != null) {
                        for (HorizontalSegment otherEdge : otherHorizontalEdgesAtY) {
                            if (myEdge.intersects(otherEdge)) {
                                intersectionPoints.add(myEdge.getIntersection(otherEdge));
                            }
                         }
                     }
                 }
             }
         }
         
         for (VerticalSegment otherEdge : other.verticalEdges) {
             boolean betterByCoords = shouldIterateEdgesByCoords(otherEdge.yInterval.size(), horizontalEdges.size());
             if (! betterByCoords) {
                 for (HorizontalSegment myEdge : horizontalEdges) {
                     if (otherEdge.intersects(myEdge)) {
                         intersectionPoints.add(myEdge.getIntersection(otherEdge));
                     }
                 }
             }
             else {
                 for (int y = otherEdge.yInterval.low+1; y < otherEdge.yInterval.high; ++y) {
                    List<HorizontalSegment> myHorizontalEdgesAtY =
                            horizontalEdgesStoredByY.get(y);
                    if (myHorizontalEdgesAtY != null) {
                        for (HorizontalSegment myEdge : myHorizontalEdgesAtY) {
                            if (otherEdge.intersects(myEdge)) {
                                intersectionPoints.add(myEdge.getIntersection(otherEdge));
                            }
                         }
                     }
                 }                
             }
         }
         
         return intersectionPoints;
     }
 
     private boolean isEmpty() {
         return vertexs.isEmpty();
     }
 
     private boolean noneTrue(List<Boolean> booleans) {
         for (Boolean b : booleans) if (b) return false;
         return true;
     }
 
     private boolean allTrue(List<Boolean> booleans) {
         for (Boolean b : booleans) if (! b) return false;
         return true;
     }
 
     private class GeomatrixIterator implements Iterator<Cell> {
       
         private Cell currentCell = null;
         private int currentRow;
         private RowIterator rowIterator;
         
         public GeomatrixIterator() {
             currentRow = boundingRectangle.topLeft.y;
             rowIterator = new RowIterator(currentRow);
             advanceToNext();
         }
 
         @Override
         public boolean hasNext() {
             return currentCell != null;
         }
 
         @Override
         public Cell next() {
             Cell beforeAdvancingCell = currentCell;
             advanceToNext();
             return beforeAdvancingCell;
         }
 
         @Override
         public void remove() {
             throw new UnsupportedOperationException("remove is not supported.");
         }
 
         private void advanceToNext() {
             if (rowIterator.hasNext()) {
                 currentCell = rowIterator.next();
             }
             else {
                 while (currentRow < boundingRectangle.bottomRight.y) {
                     ++currentRow;
                     rowIterator = new RowIterator(currentRow);
                     if (rowIterator.hasNext()) {
                         currentCell = rowIterator.next();
                         return;
                     }
                 }
                 currentCell = null;
             }
         }
         
     }
     
     private class RowIterator implements Iterator<Cell> {
         
         private Queue<Integer> segments;
         private int currentX;
         private int y;
         boolean endOfRow;
         
         public RowIterator(int row) {
             y = row;
             initializeSegments();
             findFirstSegment();
         }
 
         @Override
         public boolean hasNext() {
             return ! endOfRow;
         }
 
         @Override
         public Cell next() {
             Cell beforeAdvancingCell = new Cell(currentX, y);
             advanceToNext();
             return beforeAdvancingCell;
         }
 
         @Override
         public void remove() {
             throw new UnsupportedOperationException("remove is not supported.");
         }
         
         private void initializeSegments() {  
             segments = new LinkedList<Integer>(); 
             Cell wideRayOrigin = new Cell(boundingRectangle.topLeft.x-1, y);
             WideRay ray = new WideRay(wideRayOrigin, Direction.E);
             for (VerticalSegment edge : verticalEdges)
                 if (edge.intersects(ray))
                     segments.add(edge.x);
         }
 
         private void advanceToNext() {
             if (currentX < segments.peek()-1) ++currentX;
             else {
                 segments.remove();
                 if (segments.isEmpty()) {
                     endOfRow = true;
                 }
                 else {
                     currentX = segments.poll();
                 }
             }
         }
 
         private void findFirstSegment() {
             if (segments.isEmpty()) {
                 //there is no segment in this row
                 endOfRow = true;
             }
             else {
                 currentX = segments.poll();
             }
         }
         
     }
 
 
 
     /**
      * Unefficient preliminar version.
      */
     private class AreaIterator
         implements Iterator<Cell> {
 
         private Cell currentCell;
         
         private int xMin, xMax;
         private int yMin, yMax;
         
         public AreaIterator() {
             
             xMin = boundingRectangle.topLeft.x-1;
             xMax = boundingRectangle.bottomRight.x+2;
             yMin = boundingRectangle.topLeft.y-1;
             yMax = boundingRectangle.bottomRight.y+2;
             
             currentCell = new Cell(xMin, yMin);
             advanceToNext();
         }
         
         @Override
         public boolean hasNext() {
             return currentCell != null;
 //            //current row
 //            for (int j = currentCell.y+1; j < yMax; ++j) {
 //                if (contains(new Cell(currentCell.x, j))) {
 //                    return true;
 //                }                
 //            }
 //            //following rows
 //            for (int i = currentCell.x+1; i < xMax; ++i) {
 //                for (int j = yMin; j < yMax; ++j) {
 //                    if (contains(new Cell(i, j))) {
 //                        return true;
 //                    }
 //                }
 //            }
 //            return false;
         }
 
         @Override
         public Cell next() {
             Cell beforeAdvancingCell = currentCell;
             advanceToNext();
             return beforeAdvancingCell;
         }
 
         @Override
         public void remove() {
             throw new UnsupportedOperationException("Not supported yet.");
         }
 
         private void advanceToNext() {
             //current row
             for (int i = currentCell.x+1; i < xMax; ++i) {
                 if (contains(new Cell(i, currentCell.y))) {
                     currentCell = new Cell(i, currentCell.y);
                     return;
                 }                
             }
             //following rows
             for (int j = currentCell.y+1; j < yMax; ++j) {
                 for (int i = xMin; i < xMax; ++i) {
                     if (contains(new Cell(i, j))) {
                         currentCell = new Cell(i, j);
                         return;
                     }
                 }
             }
             currentCell = null;
         }
   
     }
 }

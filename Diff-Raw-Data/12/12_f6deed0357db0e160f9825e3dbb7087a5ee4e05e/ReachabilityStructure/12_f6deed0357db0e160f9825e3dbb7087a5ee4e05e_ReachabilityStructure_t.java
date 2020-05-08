 import org.poly2tri.Poly2Tri;
 import org.poly2tri.geometry.polygon.Polygon;
 import org.poly2tri.geometry.polygon.PolygonPoint;
 import org.poly2tri.triangulation.delaunay.DelaunayTriangle;
 
 
 import java.awt.geom.Point2D;
 import java.util.*;
 
 
 //TODO: only calculate columns once, then double. This saves a LOT of calculation
 
 public class ReachabilityStructure {
 	Point2D.Double[] originalPolyP;
 	Point2D.Double[] originalPolyQ;
 	Point2D.Double[] borderPolyP;
 	Point2D.Double[] borderPolyQ;
     ArrayList<Layer> layers;
     double _epsilon;
 
 	//Construct base reachability graph from polygons
 	public ReachabilityStructure(Point2D.Double[] polyP, Point2D.Double[] polyQ, double epsilon) {
 
 		//Store pointers to original polygons, and build the edge list of points.
 		//P is copied twice
 		originalPolyP = polyP;
 		originalPolyQ = polyQ;
 		borderPolyP = new Point2D.Double[polyP.length * 2];
 		System.arraycopy(polyP, 0, borderPolyP, 0, polyP.length);
 		System.arraycopy(polyP, 0, borderPolyP, polyP.length, polyP.length);
 		borderPolyQ = new Point2D.Double[polyQ.length];
 		System.arraycopy(polyQ, 0, borderPolyQ, 0, polyQ.length);
         _epsilon = epsilon;
 
 		Layer zeroLayer = createBaseLayer(borderPolyP, borderPolyQ, epsilon);
 		int ctr = 0;
 		for (int i = 0; i < borderPolyP.length - 1; i++) {
 				for (Arrow arrow : zeroLayer.arrows.get(i).get(0)) {
 					System.out.println(arrow.toString());
 					ctr ++;
 				}
 		}
 		System.out.println("Total number of intervals: " + ctr);
 		layers = new ArrayList<Layer>();
         layers.add(zeroLayer);
     }
 
     public Point2D.Double[] getFirstReachablePath() {
         for (ArrayList<Set<Arrow>> column : layers.get(0).arrows) {
             for (Arrow arrow : column.get(0)) {
                 if (!arrow.start.isVertical() && arrow.start.startGraph.y == 0) {
                     Point2D.Double testPoint = arrow.start.getMidpoint();
                     Point2D.Double endPoint = new Point2D.Double(testPoint.x + originalPolyP.length - 1, originalPolyQ.length);
                     Set<Arrow> reachable = reachabilityStructureFromPoint(testPoint);
                     if (reachable != null) {
                         for (Arrow possible : reachable) {
                             if (possible.start.contains(testPoint) && possible.end.contains(endPoint)) {
                                 Point2D.Double[] path = pathFromArrow(possible);
                                 if (path != null) {
                                     Point2D.Double[] finalPath = new Point2D.Double[path.length + 1];
                                     finalPath[0] = testPoint;
                                     System.arraycopy(path, 0, finalPath, 1, path.length);
                                     finalPath[finalPath.length - 1] = endPoint;
                                     return finalPath;
                                 }
                              }
                         }
                     }
                 }
             }
         }
 
 
         return null;
     }
 
     Point2D.Double[] pathFromArrow(Arrow arrow) {
         Set<Arrow> arrowSet = allSubArrows(arrow);
         Set<Point2D.Double> intervalSet = new HashSet<Point2D.Double>();
 
         for (Arrow a : arrowSet) {
             intervalSet.add(a.end.getMidpoint());
         }
 
         Point2D.Double[] result = new Point2D.Double[intervalSet.size()];
         int i = 0;
         for (Object point : intervalSet.toArray()) {
             result[i++] = (Point2D.Double) point;
         }
 
         Arrays.sort(result, new Comparator<Point2D.Double>() {
             public int compare(Point2D.Double p1, Point2D.Double p2) {
                 if (p1.getX() < p2.getX())
                     return -1;
                 if (p1.getX() > p2.getX())
                     return 1;
                 if (p1.getY() < p2.getY())
                     return -1;
                 if (p1.getY() > p2.getY())
                     return 1;
                 return 0;
             }
         });
 
 
         return result;
     }
 
     Set<Arrow> allSubArrows(Arrow arrow) {
         Set<Arrow> arrowSet = new HashSet<Arrow>();
         if (arrow.subArrows != null && arrow.subArrows.size() > 0) {
             for (Arrow sub : arrow.subArrows) {
                 arrowSet.addAll(allSubArrows(sub));
             }
             return arrowSet;
         } else {
             arrowSet.add(arrow);
             return arrowSet;
         }
     }
 
     Layer createBaseLayer(Point2D.Double[] polyX, Point2D.Double[] polyY, double epsilon) {
         Layer layerZero = new Layer();
         layerZero.level = 0;
         layerZero.arrows = new ArrayList<ArrayList<Set<Arrow>>>();
 
         //First build a list of intervals and find the arrows only within each cell
         for (int i = 0; i < polyX.length - 1; i++) {
             ArrayList<Set<Arrow>> column = new ArrayList<Set<Arrow>>();
             layerZero.arrows.add(column);
             for (int j = 0; j < polyY.length - 1; j++) {
                 //P on the x-axis, Q on the y-axis
 
                 //get the segments for each side of a cell so we can construct the arrows
                 //TODO: make calls simpler by passing an enum of L, R, T, B. Then i, j only needed to be passed once and the method can infer the rest.
                 Interval left = freeSpaceForSegment(layerZero, polyY[j], polyY[j+1], polyX[i], i, j, false, i, j, epsilon);
                 Interval right = freeSpaceForSegment(layerZero, polyY[j], polyY[j+1], polyX[i+1], i+1, j, false, i, j, epsilon);
                 Interval top = freeSpaceForSegment(layerZero, polyX[i], polyX[i+1], polyY[j+1], i, j+1, true, i, j, epsilon);
                 Interval bottom = freeSpaceForSegment(layerZero, polyX[i], polyX[i+1], polyY[j], i, j, true, i, j, epsilon);
 
                 //Build arrows and add to appropriate place in level
                 Set<Arrow> arrowSet = new HashSet<Arrow>();
 
                 if (left != null) {
                     if (right != null) {
                         Arrow arrow = new Arrow();
                         arrow.start = new Interval(left);
                         arrow.end = new Interval(right);
                         enforceMonotonicity(arrow);
                         arrowSet.add(arrow);
                     }
                     if (top != null) {
                         Arrow arrow = new Arrow();
                         arrow.start = new Interval(left);
                         arrow.end = new Interval(top);
                         enforceMonotonicity(arrow);
                         arrowSet.add(arrow);
                     }
                 }
                 if (bottom != null) {
                     if (right != null) {
                         Arrow arrow = new Arrow();
                         arrow.start = new Interval(bottom);
                         arrow.end = new Interval(right);
                         enforceMonotonicity(arrow);
                         arrowSet.add(arrow);
                     }
                     if (top != null) {
                         Arrow arrow = new Arrow();
                         arrow.start = new Interval(bottom);
                         arrow.end = new Interval(top);
                         enforceMonotonicity(arrow);
                         arrowSet.add(arrow);
                     }
                 }
 
                 if (arrowSet.size() == 0) {
                     arrowSet = null;
                 }
                 column.add(arrowSet);
             }
             mergeCellsIntoColumn(column);
         }
         return layerZero;
     }
 
     //This calculates the free space for a line segment, either horizontal or vertical.
 	//The axisPoint indicates the point on the other curve which should be compared
 	//i.e. if we're comparing a segment on Q, then we look at one point on P (a vertical if P is on the x axis)
 	//this checks the current level and retrieves an existing interval if it's already computed
 	Interval freeSpaceForSegment(Layer layer, Point2D.Double start, Point2D.Double end, Point2D.Double axisPoint, int xindex, int yindex, boolean horizontal, int xPositionFS, int yPositionFS, double epsilon) {
 		double xdiff, ydiff, root, b, divisor, t1, t2, q;
 
 		//Return precomputed interval if it exists
 		//Get cell to the left or bottom, depending on which side the segment is on
 		//TODO: use enum for LRTB, save some unnecessary compares
         //TODO: in order to use this, need to change it so that cells are merged into columns AFTER all cells are calculated
 		/*Set<Arrow> arrowSet = null;
 		if (horizontal) {
 			//get cell below
 			if (yPositionFS - 1 >= 0) {
 				arrowSet = layer.arrows.get(xPositionFS).get(yPositionFS - 1);
 			}
 		} else {
 			//get cell to the left
 			if (xPositionFS - 1 >= 0) {
 				arrowSet = layer.arrows.get(xPositionFS - 1).get(yPositionFS);
 			}
 		}
 		if (arrowSet != null) {
 			for (Arrow arrow : arrowSet) {
 				if ( (horizontal && arrow.end.startGraph.x == arrow.end.endGraph.x) || (!horizontal && arrow.end.startGraph.y == arrow.end.endGraph.y) ) {
 					return arrow.end;
 				}
 			}
 		}*/
 
 
 		xdiff = end.x - start.x;
 		ydiff = end.y - start.y;
 		divisor = xdiff*xdiff + ydiff*ydiff;
 		if(divisor == 0){
 			System.out.println("Error calculating free space for cell: Divisor = 0");
 		}
 	    b = (axisPoint.x - start.x)*xdiff + (axisPoint.y - start.y)*ydiff;
 	    q = (start.x*start.x + start.y*start.y + axisPoint.x*axisPoint.x + axisPoint.y*axisPoint.y - 2*start.x*axisPoint.x - 2*start.y*axisPoint.y - epsilon*epsilon) * divisor;
 	    root= b*b-q ;
 	    if (root < 0) {
 	    	//all black
 	    	return null;
 	    }
 	    root = Math.sqrt(root);
 	    t2 = (b+root)/divisor;
 	    t1 = (b-root)/divisor;
 	    if (t1<0) t1 = 0;
 	    if (t2<0) t2 = 0;
 	    if (t1>1) t1 = 1;
 	    if (t2>1) t2 = 1;
 
 	    if(t1 == t2){
 	    	//all black
 	    	return null;
 	    }
 
 	    Interval result = new Interval();
 	    if (!horizontal) {
 	    	result.startGraph = new Point2D.Double(xindex, t1 + yindex);
 	    	result.endGraph = new Point2D.Double(xindex, t2 + yindex);
 	    } else {
 	    	result.startGraph = new Point2D.Double(t1 + xindex, yindex);
 	    	result.endGraph = new Point2D.Double(t2 + xindex, yindex);
 	    }
 
 		return result;
 	}
 
     void enforceMonotonicity(Arrow arrow) {
         boolean changed = false;
         if (arrow.end != null && arrow.start != null) {
             if (arrow.start.isParallelTo(arrow.end)) {
                 //either left to right or bottom to top
                 if (arrow.start.isVertical()) {
                     //left interval pointing to right interval
                     if (arrow.start.startGraph.y > arrow.end.startGraph.y) {
                         arrow.end.startGraph.y = arrow.start.startGraph.y;
                         changed = true;
                     }
                     if (arrow.end.endGraph.y < arrow.start.endGraph.y) {
                         arrow.start.endGraph.y = arrow.end.endGraph.y;
                         changed = true;
                     }
                 } else {
                     //bottom interval pointing to top interval
                     if (arrow.start.startGraph.x > arrow.end.startGraph.x) {
                         arrow.end.startGraph.x = arrow.start.startGraph.x;
                         changed = true;
                     }
                     if (arrow.end.endGraph.x < arrow.start.endGraph.x) {
                         arrow.start.endGraph.x = arrow.end.endGraph.x;
                         changed = true;
                     }
                 }
             }
         }
         if (changed) {
             //check to make sure SOMETHING is still reachable, if not arrow = null
             if (arrow.start.isVertical()) {
                 if (arrow.start.startGraph.y >= arrow.start.endGraph.y ||
                         arrow.end.startGraph.y >= arrow.end.endGraph.y) {
                     arrow = null;
                 }
             } else {
                 if (arrow.start.startGraph.x >= arrow.start.endGraph.x ||
                         arrow.end.startGraph.x >= arrow.end.endGraph.x) {
                     arrow = null;
                 }
             }
             //if we changed the outer arrows, we need to recursively change the subarrows
             if (arrow != null && arrow.subArrows != null && arrow.subArrows.size() > 0) {
                 for (Arrow sub : arrow.subArrows) {
                     enforceMonotonicity(sub);
                 }
             }
         }
     }
 
     void mergeCellsIntoColumn(ArrayList<Set<Arrow>> column) {
 
         if (column.size() == 1) {
             return;
         }
 
         Set<Arrow> topCell = column.get(column.size() - 1);
         Set<Arrow> bottomCell = column.get(column.size() - 2);
         Set<Arrow> mergedCell = mergeCells(topCell, bottomCell);
 
 
         //remove constituent cells and add new one
         column.remove(topCell);
         column.remove(bottomCell);
         column.add(mergedCell);
 
         //recursively merge
         mergeCellsIntoColumn(column);
     }
 
     HashSet<Arrow> mergeCells(Set<Arrow> first, Set<Arrow> second) {
         HashSet<Arrow> mergedCell = new HashSet<Arrow>();
         //loop through arrows in adjacent cells and find the ones that connect
         for (Arrow topArrow : first) {
             for (Arrow bottomArrow : second) {
                 if(topArrow.start.intersects(bottomArrow.end)) {
                     //merge arrows
                     Interval middle = topArrow.start.intersection(bottomArrow.end);
                     Arrow newArrow = null;
                     if (middle != null) {
                         //if they connect, we copy the two arrows, modify their connection,
                         //create a new arrow with the constituents as subarrows, and add that to the mergedcell
                         //after the mergedcell is created, we can delete the base arrows
                         Arrow topCopy = new Arrow(topArrow);
                         topCopy.start = new Interval(middle);
                         Arrow bottomCopy = new Arrow(bottomArrow);
                         bottomCopy.end = new Interval(middle);
 
                         newArrow = new Arrow();
                         newArrow.subArrows.add(topCopy);
                         newArrow.subArrows.add(bottomCopy);
                         newArrow.start = new Interval(bottomCopy.start);
                         newArrow.end = new Interval(topCopy.end);
 
                         //if end and middle are parallel, we need to project monotonicity
                         //monotonicity is already enforced within a single cell
                         //and enforcemonotonicity knows whether or not start and end are parallel
                         enforceMonotonicity(newArrow);
                     }
                     //enforceMonotonicity could null out the new arrow
                     if (newArrow != null && !newArrow.isNull()) {
                         mergedCell.add(newArrow);
                     }
                 }
             }
         }
 
         //keep the originals in case they can connect later on
         //(hashset doesn't allow duplicates, so no need to worry here)
         for (Arrow a : first) {
             mergedCell.add(new Arrow(a));
         }
         for (Arrow a : second) {
             mergedCell.add(new Arrow(a));
         }
         //mergedCell.addAll(new HashSet<Arrow>(first));
         //mergedCell.addAll(new HashSet<Arrow>(second));
 
         return mergedCell;
     }
 
     ArrayList<Set<Arrow>> mergeTwoColumns(ArrayList<Set<Arrow>> left, ArrayList<Set<Arrow>> right) {
         //columns should only have one cell in them at this point, but many arrows
         Set<Arrow> leftColumn = new HashSet<Arrow>();
         for (Arrow a : left.get(0)) {
             leftColumn.add(new Arrow(a));
         }
         Set<Arrow> rightColumn = new HashSet<Arrow>();
         for (Arrow a : right.get(0)) {
             rightColumn.add(new Arrow(a));
         }
         Set<Arrow> mergedColumn = new HashSet<Arrow>();
         for (Arrow a : mergeCells(rightColumn, leftColumn)) {
             mergedColumn.add(new Arrow(a));
         }
 
         ArrayList<Set<Arrow>> result = new ArrayList<Set<Arrow>>();
         result.add(mergedColumn);
         return result;
     }
 
     DiagonalTree diagonalTreeForPoint(Point2D.Double startPoint) {
         int startIndex =  (int) Math.floor(startPoint.x);
         if (startIndex >= originalPolyP.length) {
             return null;
         }
         ArrayList<Diagonal> diagonals = orderedDiagonalsForPolygon(originalPolyP);
 
         Set<Arrow> column = layers.get(0).arrows.get(startIndex).get(0);
         DiagonalTree diagonalTree;
 
         boolean validStart = false;
         for (Arrow arrow : column) {
             if (arrow.start.contains(startPoint)) {
                 validStart = true;
                 break;
             }
         }
         if (validStart) {
             ArrayList<Diagonal> diagonalsCopy = new ArrayList<Diagonal>(diagonals);
             //trim invalid diagonals
             for (Diagonal d : diagonalsCopy) {
                 if (d.startIndex <= startIndex || d.endIndex <= startIndex ||
                         d.endIndex > startIndex + originalPolyP.length || d.startIndex >= startIndex + originalPolyP.length) {
                     diagonals.remove(d);
                 }
             }
 
             //put those left into tree
             diagonalTree = new DiagonalTree(new Diagonal(startIndex, startIndex + originalPolyP.length));
 
             for (Diagonal d : diagonals) {
                 diagonalTree.addDiagonal(d);
             }
             diagonalTree.subdivideDiagonals();
 
             diagonalTree.print();
 
             return diagonalTree;
         }
         return null;
     }
 
     //this is for the polygon on the x-axis (it doubles the length)
     ArrayList<Diagonal> orderedDiagonalsForPolygon(Point2D.Double[] poly) {
         int length = poly.length;
         ArrayList<Diagonal> diagonals = new ArrayList<Diagonal>();
 
         //get the triangulation for the polygon
         ArrayList<PolygonPoint> points = new ArrayList<PolygonPoint>();
         for (int i = 0; i < length; i++) {
             Point2D.Double point = poly[i];
             points.add(new PolygonPoint(point.x, point.y));
         }
 
         Polygon converted = new Polygon(points);
         Poly2Tri.triangulate(converted);
 
         ArrayList<DelaunayTriangle> triangulation;
         triangulation = (ArrayList<DelaunayTriangle>) converted.getTriangles();
 
         //now we need to extract the diagonals from the triangulation
         //NOTE: it's not clear how edges correspond to points here, but it's in the poly2tri source (no docs)
         for (DelaunayTriangle triangle : triangulation) {
             for (int i = 0; i < 3; i++) {
                 boolean isDiagonal = !triangle.cEdge[i];
                 if (isDiagonal) {
                     Point2D.Double first = new Point2D.Double(triangle.points[(i + 1) % 3].getX(), triangle.points[(i + 1) % 3].getY());
                     Point2D.Double second = new Point2D.Double(triangle.points[(i + 2) % 3].getX(), triangle.points[(i + 2) % 3].getY());
 
                     int[] indices = new int[2];
                     indices[0] = java.util.Arrays.asList(poly).indexOf(first);
                     indices[1] = java.util.Arrays.asList(poly).indexOf(second);
 
                     java.util.Arrays.sort(indices);
 
                     Diagonal newDiagonal = new Diagonal(indices[0], indices[1]);
 
                     //diagonal represents an actual diagonal, not just for the sake of merging
                     newDiagonal.isTrueDiagonal = true;
 
                     if(diagonals.indexOf(newDiagonal) == -1) {
                         diagonals.add(newDiagonal);
                     }
 
                 }
             }
         }
 
         System.out.println("Number of diagonals: "  + diagonals.size());
 
         int max = diagonals.size();
         //since P is doubled, add other possible diagonal indices
         for (int i = 0; i < max; i++) {
             Diagonal d = diagonals.get(i);
             diagonals.add(new Diagonal(d.endIndex, length + d.startIndex));
             diagonals.add(new Diagonal(length + d.startIndex, length + d.endIndex));
         }
 
         //sort by first index, then by reverse by second
         Collections.sort(diagonals, new Comparator<Diagonal>() {
             public int compare(Diagonal a, Diagonal b) {
                 //indices are nonnegative and ordered
                 if (a.startIndex == b.startIndex) {
                     return (b.endIndex - a.endIndex);
                 }
                 return (a.startIndex - b.startIndex);
             }
         });
 
         return diagonals;
     }
 
     Set<Arrow> reachabilityStructureFromPoint(Point2D.Double startPoint) {
         if (Math.floor(startPoint.x) > originalPolyP.length) {
             return null;
         }
         DiagonalTree diagonalTree = diagonalTreeForPoint(startPoint);
         this.layers.add(1, new Layer(layers.get(0)));
 
         //TODO: rework the layers idea. don't really need them now, just need one base layer and create a new top layer for each query
         if (diagonalTree != null) {
             return mergeChildren(diagonalTree.root()).get(0);
         } else {
             return null;
         }
     }
 
     ArrayList<Set<Arrow>> mergeChildren(DiagonalTree.DiagonalNode node){
             //get list of columns
             ArrayList<ArrayList<Set<Arrow>>> columns = new ArrayList<ArrayList<Set<Arrow>>>();
             for (DiagonalTree.DiagonalNode child : node.children) {
                 if (child.hasChildren()) {
                     System.out.println("Merge Children of:");
                     child.print();
                     columns.add(pruneInvalidIntervalsFromColumn(mergeChildren(child), child.data));
 
                 } else {
                     System.out.println("Return single ");
                     child.print();
                     columns.add(layers.get(1).arrows.get(child.data.startIndex));
                 }
             }
             return mergeColumns(columns);
     }
 
     ArrayList<Set<Arrow>> mergeColumns(ArrayList<ArrayList<Set<Arrow>>> columns) {
         ArrayList<Set<Arrow>> finalColumn = columns.get(0);
         if (columns.size() == 1) {
             return finalColumn;
         }
         for (int i = 1; i < columns.size(); i++) {
             finalColumn = mergeTwoColumns(finalColumn, columns.get(i));
         }
 
         return finalColumn;
     }
 
     ArrayList<Set<Arrow>> pruneInvalidIntervalsFromColumn(ArrayList<Set<Arrow>> column, Diagonal diagonal) {
         if (!diagonal.isTrueDiagonal) {
             //don't prune unless the diagonal being inspected is really a diagonal from the polygon
             return column;
         }
         ArrayList<Set<Arrow>> columnCopy = new ArrayList<Set<Arrow>>(column);
 
         for (Arrow arrow : columnCopy.get(0)) {
             if (arrow.start.isVertical() && arrow.end.isVertical()) {
                 Point2D.Double startPoint = arrow.start.getPolygonMidpoint(borderPolyQ);
                 Point2D.Double endPoint = arrow.end.getPolygonMidpoint(borderPolyQ);
                 ShortestPath spCalculator = new ShortestPath(insertPointIntoPolygon(insertPointIntoPolygon(originalPolyQ, startPoint), endPoint), startPoint, endPoint);
                 //TODO: can this fail for midpoints but not for other points in the interval?
                if (spCalculator.getPath() != null && !startPoint.equals(endPoint)) {
                     //get diagonal path
                     Point2D.Double[] diagonalPath = {borderPolyP[diagonal.startIndex], borderPolyP[diagonal.endIndex]};
                     //get shortest path
                     Point2D.Double[] shortestPath = spCalculator.getPath();
 
                     //find frechet distance between them
                     Layer reachability = createBaseLayer(diagonalPath, shortestPath, _epsilon);
 
                     Set<Arrow> finalArrows;
                     for (ArrayList<Set<Arrow>> c : reachability.arrows) {
                         mergeCellsIntoColumn(c);
                     }
                     finalArrows = mergeColumns(reachability.arrows).get(0);
 
                     boolean isPath = false;
                     for (Arrow a : finalArrows) {
                         if (a.start.contains(new Point2D.Double(0, 0)) && a.end.contains(new Point2D.Double(1, 1))) {
                               //exists a path from start to end
                               isPath = true;
                              break;
                         }
                     }
                     if (!isPath) {
                         //if frechet distance is too big between diagonal and sp, then we remove the arrow.
                         column.remove(arrow);
                     }
                 }
             }
         }
         return column;
     }
 
     private Point2D.Double[] insertPointIntoPolygon(Point2D.Double[] poly, Point2D.Double point) {
         //Iterate through all edges
         for (int i = 0; i < poly.length; i++) {
             int j = i + 1;
             if (j >= poly.length) {
                 j = 0;
             }
             //if distance from point to edge is small, insert point between the vertices making up that edge
             double denominator = Math.sqrt((poly[j].x - poly[i].x)*(poly[j].x - poly[i].x) + (poly[j].y - poly[i].y)*(poly[j].y - poly[i].y));
             double numerator = Math.abs((poly[j].x - poly[i].x)*(poly[i].y - point.y) - (poly[i].x - point.x)*(poly[j].y - poly[i].y));
 
             Point2D.Double[] newPolygon = new Point2D.Double[poly.length + 1];
             if (getZero(numerator/denominator) == 0) {
                 System.arraycopy(poly, 0, newPolygon, 0, j);
                 newPolygon[j] = point;
                 System.arraycopy(poly, j, newPolygon, j+1, poly.length-j);
                 return newPolygon;
 
             }
         }
         return poly;
     }
 
     private double getZero(double x) {
         double testPositiveZero = 0.00000000001;
         double testNegativeZero = -0.00000000001;
         if (x >= testNegativeZero && x <= testPositiveZero) {
             x = 0;
         }
         return x;
     }
 
 }

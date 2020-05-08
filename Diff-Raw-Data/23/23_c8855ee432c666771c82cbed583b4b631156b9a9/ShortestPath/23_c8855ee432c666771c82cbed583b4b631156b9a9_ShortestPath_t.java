 import java.awt.geom.Line2D;
 import java.awt.geom.Point2D;
 import java.util.Arrays;
 
 //Always world coordinates in this class
 
 class SPTreeNode {
 	public double x;
 	public double y;
 	public double totalDist;
 	public int prev;
 }
 
 public class ShortestPath {
 	Point2D.Double[] _poly;
 	Point2D.Double _start;
 	Point2D.Double _end;
 	Point2D.Double _path[];
 
 	public ShortestPath(Point2D.Double[] poly, Point2D.Double start, Point2D.Double end) {
     	this._poly = poly;
     	this._start = start;
     	this._end = end;
     }
 
 	public void test() {
 		//All edges should register as in the polygon
 		System.out.println("Edges in Polygon");
 		for (int i = 0; i < _poly.length; i++) {
 			int j = i + 1;
 			if (j >= _poly.length) {
 				j = 0;
 			}
 			if (lineInPolygon(_poly[i].x, _poly[i].y, _poly[j].x, _poly[j].y)) {
 				System.out.println("True");
 			} else {
 				System.out.println("False");
 			}
 		}
 	}
 
 	public Point2D.Double[] getPath() {
 		//this.test();
 		this.shortestPath(_start.x, _start.y, _end.x, _end.y);
		//System.out.println(Arrays.toString(_path));
 		return _path;
 	}
 
 	public double calcDist(double startX, double startY, double endX, double endY) {
 		double dX = endX - startX;
 		double dY = endY - startY;
 		return Math.sqrt(dX * dX + dY*dY);
 	}
 
 	private double getZero(double x) {
 		double testPositiveZero = 0.00000000001;
 		double testNegativeZero = -0.00000000001;
 		if (x >= testNegativeZero && x <= testPositiveZero) {
 			x = 0;
 		}
 		return x;
 	}
 
 	 //http://www.ecse.rpi.edu/Homepages/wrf/Research/Short_Notes/pnpoly.html
 	 private boolean pointInPolygon(double testX, double testY) {
 		 int i, j = 0;
 		 boolean odd = false;
 		  for (i = 0, j = _poly.length-1; i < _poly.length; j = i++) {
 		    if ( ((_poly[i].y>testY) != (_poly[j].y>testY)) &&
 			 (testX < (_poly[j].x-_poly[i].x) * (testY-_poly[i].y) / (_poly[j].y-_poly[i].y) + _poly[i].x) )
 		       odd = !odd;
 		  }
 		  return odd;
 	}
 
 	/*private boolean lineInPolygon(double testSX, double testSY, double testEX, double testEY) {
 		for(int i = 0; i<_poly.length; i++) {
 			int j = i + 1;
 	        if (j >= _poly.length) {
 	            j = 0;
 	        }
 
 	        //check if part of the line is on an edge first
 	        double m1 = (_poly[j].y - _poly[i].y)/(_poly[j].x - _poly[i].x);
 	        double m2 = (testEY - testSY)/(testEX - testSX);
 
 	        //if line is on an edge then slopes will be very close
 	        if(getZero(m1 - m2) == 0) {
 	        	//get slope intercepts
 	        	double b1 = -m1 * _poly[i].x + _poly[i].y;
 	        	double b2 = -m2 * testSX + testSY;
 	        	double distance = Math.abs(b2 - b1)/Math.sqrt(m1*m1 +1);
 	        	//assume lines are parallel and check if distance is very small
 	        	if (getZero(distance) == 0) {
 	        		//see if
 	        		//skip this iteration, the at least part of the line lies on at least part of an edge. The other edges still need to be checked.
 	        		continue;
 	        	}
 	        } else {
 	        	//if part of the segment doesn't lie on part of an edge, then see if it intersects anything
 	        	if (Line2D.linesIntersect(_poly[i].x, _poly[i].y, _poly[j].x, _poly[j].y, testSX, testSY, testEX, testEY)) {
 	        		return false;
 	        	}
 	        }
 		}
 		return true;
 	}
 	*/
 
 	private boolean lineInPolygon(double testSX, double testSY, double testEX, double testEY) {
 		double theCos, theSin, dist, sX, sY, eX, eY, rotSX, rotSY, rotEX, rotEY, crossX;
 
 		//Edge detection
 		//TODO: make better. Probably should change array to a hash or list
 		//In fact all of this could be improved by assuming start and end are vertices, and referring to by index.
 		//Then they're adjacent if difference of indices = 1
 		for(int i = 0; i<_poly.length; i++) {
 
 			if (getZero(_poly[i].x - testSX) == 0 && getZero(_poly[i].y - testSY) == 0) {
 				//check vertices before and after to see if it's the endpoint
 				int next = i+1;
 				if (next >= _poly.length) {
 					next = 0;
 				}
 				int prev = i - 1;
 				if (prev < 0) {
 					prev = _poly.length - 1;
 				}
 				if ((getZero(_poly[next].x - testEX) == 0 && getZero(_poly[next].y - testEY) == 0) ||
 				   (getZero(_poly[prev].x - testEX) == 0 && getZero(_poly[prev].y - testEY) == 0)) {
 					return true;
 				}
 			}
 		}
 
 
 		testEX = testEX - testSX;
 		testEY = testEY - testSY;
 		dist = Math.sqrt( testEX * testEX + testEY * testEY );
 		theCos = testEX / dist;
 		theSin = testEY / dist;
 
 	    for (int i = 0; i< _poly.length; i++) {
 	        int j = i + 1;
 	        if (j >= _poly.length) {
 	            j = 1;
 	        }
 
 	        sX = _poly[i].x - testSX;
 	        sY = _poly[i].y - testSY;
 	        eX = _poly[j].x - testSX;
 	        eY = _poly[j].y - testSY;
 
 	        //This is probably an ineffective duplicate of the edge-detection above.
 	        if ((sX == 0 && sY == 0 && eX == testEX && eY == testEY) || (eX == 0 && eY == 0 && sX == testEX && sY == testEY)) {
 	                return true;
 	        }
 
 	        rotSX = getZero(sX * theCos + sY * theSin);
 	        rotSY = getZero(sY * theCos - sX * theSin);
 	        rotEX = getZero(eX * theCos + eY * theSin);
 	        rotEY = getZero(eY * theCos - eX * theSin);
 	        crossX = getZero(rotSX + (rotEX-rotSX)*(0-rotSY)/(rotEY-rotSY));
 
 	        if ((rotSY < 0 && rotEY > 0) || (rotEY < 0 && rotSY > 0)) {
 	                if (crossX >= 0 && crossX <= dist) {
 	                        return false;
 	                }
 	        }
 
 	        if (rotSY == 0 && rotEY == 0 && (rotSX >= 0 || rotEX >= 0) && (rotSX <= dist || rotEX <= dist)
 	                && (rotSX < 0 || rotEX < 0 || rotSX > dist || rotEX > dist)) {
 	                return false;
 	        }
 	   }
 
 	   return pointInPolygon(testSX + testEX / 2, testSY + testEY / 2);
 
 	}
 
 	private boolean shortestPath(double sX, double sY, double eX, double eY) {
 		int solutionNodes = 0;
 
 	  double  INF = Double.POSITIVE_INFINITY;     //  (larger than total solution dist could ever be)
 
 	  SPTreeNode  pointList[] = new SPTreeNode[_poly.length + 2];   //  (enough for all polycorners plus two)
 	  for(int i = 0; i< _poly.length+2; i++) {
 		pointList[i] = new SPTreeNode();
 	  }
 
 	  int    pointCount;
 
 	  int     treeCount, i, j, bestI, bestJ;
 	  double  bestDist, newDist ;
 
 	  //  If there is a straight-line solution, return with it immediately.
 	  if (lineInPolygon(sX,sY,eX,eY)) {
 	    solutionNodes=0;
 	    _path = new Point2D.Double[2];
 		_path[0] = new Point2D.Double(sX, sY);
 		_path[1] = new Point2D.Double(eX, eY);
 	    return true;
 	  }
 
 	  //  Build a point list that refers to the corners of the
 	  //  polygons, as well as to the startpoint and endpoint.
 	  pointList[0].x=sX;
 	  pointList[0].y=sY;
 	  pointCount=1;
 	    for (i=0; i<_poly.length; i++) {
 	      pointList[pointCount].x=_poly[i].x;
 	      pointList[pointCount].y=_poly[i].y;
 	      pointCount++;
 	      }
 	  pointList[pointCount].x=eX;
 	  pointList[pointCount].y=eY;
 	  pointCount++;
 
 	  //  Initialize the shortest-path tree to include just the startpoint.
 	  treeCount=1;
 	  pointList[0].totalDist = 0.;
 	  bestJ = 1;
 	  bestI = 0;
 
 	  //  Iteratively grow the shortest-path tree until it reaches the endpoint
 	  //  -- or until it becomes unable to grow, in which case exit with failure.
 	  while (bestJ < pointCount-1) {
 	    bestDist=INF;
 	    for (i=0; i<treeCount; i++) {
 	      for (j=treeCount; j<pointCount; j++) {
 	        if (lineInPolygon(pointList[i].x,pointList[i].y,pointList[j].x,pointList[j].y)) {
 	          newDist=pointList[i].totalDist + calcDist(pointList[i].x,pointList[i].y, pointList[j].x,pointList[j].y);
 	          if (newDist<bestDist) {
 	            bestDist=newDist;
 	            bestI=i;
 	            bestJ=j;
 	            }
 	          }
 	        }
 	      }
 	    if (bestDist==INF) {
 	    	return false;   //  (no solution)
 	    }
 	    pointList[bestJ].prev = bestI;
 	    pointList[bestJ].totalDist = bestDist;
 
 	    SPTreeNode tmp = pointList[bestJ];
 	    pointList[bestJ] = pointList[treeCount];
 	    pointList[treeCount] = tmp;
 
 	    treeCount++;
 	   }
 
 	  //  Load the solution arrays.
 	  solutionNodes = -1;
 	  i=treeCount-1;
 	  while (i> 0) {
 	    i=pointList[i].prev;
 	    solutionNodes++;
 	  }
 	  _path = new Point2D.Double[solutionNodes + 2];
 	  _path[0] = new Point2D.Double(sX, sY);
 	  _path[solutionNodes + 1] = new Point2D.Double(eX, eY);
 	  j=solutionNodes-1;
 	  i=treeCount-1;
 	  while (j>=0) {
 	    i=pointList[i].prev;
 
 	    _path[j+1] = new Point2D.Double(pointList[i].x, pointList[i].y);
 
 	    j--;
 	   }
 
 	  //  Success.
 	  return true;
 	}
 
 }

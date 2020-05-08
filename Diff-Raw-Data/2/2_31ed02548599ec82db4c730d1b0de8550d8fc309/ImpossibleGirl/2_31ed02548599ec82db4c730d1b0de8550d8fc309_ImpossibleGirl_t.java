 package mosquito.g0;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.geom.Line2D;
 import java.awt.geom.Point2D;
 import java.awt.geom.Line2D.Double;
 import java.lang.reflect.Array;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Random;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 
 import mosquito.sim.Collector;
 import mosquito.sim.Light;
 import mosquito.sim.MoveableLight;
 
 
 
 public class ImpossibleGirl extends mosquito.sim.Player {
 	class Section {
 		  int[] boolCombo;
 		  ArrayList<Integer> xPoints;
 		  ArrayList<Integer> yPoints;
 		  int maxX;
 		  int minX;
 		  int maxY;
 		  int minY;
 		  int midX;
 		  int midY;
 		  
 		  boolean visited = false;
 		  
 		  public Section() {
 			  boolCombo = new int[walls.size()];
 			  xPoints = new ArrayList<Integer>();
 			  yPoints = new ArrayList<Integer>();
 		  }
 		  //boolean through; //describes if there are multiple entrances into the section;
 		  void printDetails() {
 //			  log.debug("boolCombo: " + Arrays.toString(boolCombo));
 			  //  log.trace("Area: " + this.area + ", endpoints: " + Arrays.toString(this.endpoints) + ", through? " + through);
 		  }
 		  
 		  void setMidpoints() {
 			  int sumX = 0;
 			  int sumY = 0;
 			  int len = xPoints.size();
 			  for (int i = 0; i < len; i++) {
 				  sumX += xPoints.get(i);
 				  sumY += yPoints.get(i);
 			  }
 			  this.midX = sumX/len;
 			  this.midY = sumY/len;
 		  }
 	}
 	
 	private int collectorX;
 	private int collectorY;
 
 	private int numLights;
 	private Point2D.Double lastLight;
 	private Logger log = Logger.getLogger(this.getClass()); // for logging
 	
 	@Override
 	public String getName() {
 		return "I section things";
 	}
 	
 	private Set<Light> lights;
 	private Set<Line2D> walls;
 	
 	private ArrayList<MoveableLight> mlights;
 	private AreaMap map;
 	private AStar astar;
 	private int move;
 	
 	private int pointLineRelationships[][][];
 	private int numberOfSections;
 	private ArrayList<Section> sections = new ArrayList<Section>();
 	
 	private WeightedGraph midpointGraph;
 	private int[] orderedSections;
 	
 	/*
 	 * This is called when a new game starts. It is passed the set
 	 * of lines that comprise the different walls, as well as the 
 	 * maximum number of lights you are allowed to use.
 	 * 
 	 * The return value is a set of lines that you would like to have drawn on the screen.
 	 * These lines don't actually affect gameplay, they're just there so you can have some
 	 * visual clue as to what's happening in the simulation.
 	 */
 	@Override
 	public ArrayList<Line2D> startNewGame(Set<Line2D> walls, int numLights) {
 		this.numLights = numLights;
 		this.walls = walls;
 		pointLineRelationships = new int[100][100][walls.size()];
 		ArrayList<Line2D> lines = new ArrayList<Line2D>();
 		sectioningAlgorithm();
 		identifySections(pointLineRelationships);
 		for (int i=0; i<numberOfSections; i++) {
 			
 			sections.get(i).setMidpoints();
 			Point2D ul = new Point2D.Double(sections.get(i).midX-1, sections.get(i).midY-1);
 			Point2D ll = new Point2D.Double(sections.get(i).midX-1, sections.get(i).midY+1);
 			Point2D ur = new Point2D.Double(sections.get(i).midX+1, sections.get(i).midY-1);
 			Point2D lr = new Point2D.Double(sections.get(i).midX+1, sections.get(i).midY+1);
 			
 			Line2D c1 = new Line2D.Double(ul, lr);
 			Line2D c2 = new Line2D.Double(ll, ur);
 			
 			lines.add(c1);
 			lines.add(c2);
 			
 //			System.err.println("Section " + i + " midpoint: (" + sections.get(i).midX +
 //				" , " + sections.get(i).midY + ")");
 		}
 		
 		return lines;
 	}
 
 
 	/*
 	 * This is used to determine the initial placement of the lights.
 	 * It is called after startNewGame.
 	 * The board tells you where the mosquitoes are: board[x][y] tells you the
 	 * number of mosquitoes at coordinate (x, y)
 	 */
 	public Set<Light> getLights(int[][] board) {
 
 		// initializing AStar
 		FHeuristic fh = new FHeuristic();
 		
 		lights = new HashSet<Light>();
 		mlights = new ArrayList<MoveableLight>();
 		
 		// initially position each of the nights
 		for (int i = 0; i < numLights; i++) {
 			MoveableLight l = new MoveableLight(sections.get(i).midX, sections.get(i).midY, true);
 			mlights.add(l);
 			lights.add(l);
 		}
 		
 		for(int i = 0; i < sections.size(); i++) {
 			sections.get(i).setMidpoints();
 		}
 		
 		// add a list of waypoints to each light
 		int index;
 		Point2D waypoint;
 		AreaMap correctMap = generateAreaMap(board, walls);
 		for(int i = 0; i < sections.size(); i++) {
 			index = i % numLights;
 			waypoint = new Point2D.Double(sections.get(i).midX, sections.get(i).midY);
 			if (!correctMap.getNodes().get(sections.get(i).midX).get(sections.get(i).midY).isObstacle) {
 				mlights.get(index).waypoints.add(waypoint);
 				this.collectorX = sections.get(i).midX;
 				this.collectorY = sections.get(i).midY;
 			}
 		}
 		
 		// for each light, make the last light the collector
 		for (int i = 0; i < numLights; i++) {
 			//assume that collector is at 50,50
 			mlights.get(i).waypoints.add(new Point2D.Double(this.collectorX, this.collectorY));
 		}
 		
 		int len;
 		Point2D currPoint;
 		Point2D nextPoint;
 		// generate paths between waypoints
 		for (MoveableLight currLight : mlights) {
 			len = currLight.waypoints.size();
 			// set all paths in moving light
 			for (int j = 0; j < len-1; j++) {
 				currPoint = currLight.waypoints.get(j);
 				nextPoint = currLight.waypoints.get(j+1);
 				AreaMap cleanMap = generateAreaMap(board, walls);
 				astar = new AStar(cleanMap, fh);
 				astar.calcShortestPath((int)currPoint.getX(), (int)currPoint.getY(), (int)nextPoint.getX(), (int)nextPoint.getY());
 				currLight.shortestPath.add(astar.shortestPath);
 			}
 			// initialize paths to first path
 			currLight.currPath = currLight.shortestPath.get(0);
 		}
 
 		findOptimalRoute(board);
 	    
 	    for (int i=0; i<numberOfSections; i++) {
 	    	log.error("The " + i + "th point to visit is: " + orderedSections[i]);
 	    }
 	    
 		return lights;
 	}
 	
 	public AreaMap generateAreaMap(int[][] board, Set<Line2D> walls) {
 		AreaMap cleanMap = new AreaMap(100,100);
 		for(int i = 0; i < board.length; i++) {
 		    for(int j = 0; j < board[0].length; j++) {
 		        for(Line2D wall: walls) {
 		            if(wall.ptSegDist(i, j) < 2.0) {
 		            	cleanMap.getNodes().get(i).get(j).isObstacle = true; // nay on the current node
 		            }
 		        }
 		    }
 		}
 		
 		return cleanMap;
 	}
 	
 	/*
 	 * This is called at the beginning of each step (before the mosquitoes have moved)
 	 * If your Set contains additional lights, an error will occur. 
 	 * Also, if a light moves more than one space in any direction, an error will occur.
 	 * The board tells you where the mosquitoes are: board[x][y] tells you the
 	 * number of mosquitoes at coordinate (x, y)
 	 */
 	public Set<Light> updateLights(int[][] board) {
 		
 		for (MoveableLight ml : mlights) {
 			Path currPath = ml.currPath; // get the current path we're working through
 			if (currPath == null) {
 				ml.move = 0;
 				ml.indexOfPath++;
 				if(ml.indexOfPath >= ml.shortestPath.size())
 					continue;
 //				System.err.println(ml.indexOfPath + " compared to " + ml.shortestPath.size());
 				ml.currPath = ml.shortestPath.get(ml.indexOfPath);
 				continue;
 			}
 			// check to see if we're done moving
 //			System.err.println("ml.move is "+ml.move);
 //			System.err.println("CurrPath.getLength() is "+currPath.getLength());
 			if (ml.move >= currPath.getLength()) {
 //				System.err.println("Hitting a null pointer?");
 				// check to see if we're done with all paths to waypoints
 				ml.indexOfPath++;
 				if (ml.indexOfPath >= (ml.shortestPath.size())) {
 //					System.err.println("Continued ml.indexOfPath >= ml.shortestPath.size()");
 					continue;
 				}
 				
 				ml.currPath = ml.shortestPath.get(ml.indexOfPath);
 				ml.move = 0;
 //				System.err.println("Continued ml.move >= currPath.getLength()");
 				continue;
 			}
 			
 //			log.error("("+ml.getX()+","+ml.getY()+") --> "+"("+currPath.getX(ml.move)+","+currPath.getY(ml.move)+")");
 			ml.moveTo(currPath.getX(ml.move), currPath.getY(ml.move));
 			ml.move++;
 		}
 		
 		return lights;
 	}
 
 	/*
 	 * Currently this is only called once (after getLights), so you cannot
 	 * move the Collector.
 	 */
 	@Override
 	public Collector getCollector() {
 		// this one just places a collector next to the last light that was added
 		Collector c = new Collector(this.collectorX, this.collectorY);
 		return c;
 	}
 	
 	public void sectioningAlgorithm() {
 		Line2D[] wallArray;
 		  for (int x=0; x<100; x++) {	//could probably also be x+=2, depending on how well we want our sections to be defined
 		  	for (int y=0; y<100; y++) {
 		  		for (int i = 0; i < walls.size(); i++) {
 		  			wallArray = walls.toArray(new Line2D[walls.size()]);
 		  			pointLineRelationships[x][y][i] = comparePointToLine(x, y, wallArray[i]);	
 		  		}
 		  	}
 		  }
 	}
 	
 	public int comparePointToLine(int x, int y, Line2D line) {
 		boolean lineIsVertical = (line.getX1() == line.getX2());
 		
 		  if (lineIsVertical) {
 		    //for vertical lines, assume that points to the left are lesser and points to the right are greater
 				if (x < line.getX1()) return 0;
 	    else return 1;
 		} else {
 	    //find the slope and intercept of the line given
 	    double slope = (line.getY2()-line.getY1()) / (line.getX2()-line.getX1());
 	    double yIntercept = line.getY1() - (slope*line.getX1());
 	    //identify if point is above or below line
 	    if (y > slope * x + yIntercept) return 1;
 	    else return 0;
 		}
 	}
 	
 	void identifySections(int[][][] pointLineRelationships) {
 		numberOfSections=0;
 		for (int x=0; x<100; x++) {
 			for (int y=0; y<100; y++) {
 				int[] boolCombo = pointLineRelationships[x][y];
 		    boolean hasComboBeenSeen = false;
 		    for (int i=0; i<numberOfSections; i++) {
 		      if (Arrays.equals(boolCombo,sections.get(i).boolCombo)) {
 		        hasComboBeenSeen = true;
 		        sections.get(i).xPoints.add(x);
 		        sections.get(i).yPoints.add(y);
 		        if (x > sections.get(i).maxX)
 		        	sections.get(i).maxX = x;
 		        else if (x < sections.get(i).minX)
 		        	sections.get(i).minX = x;
 		        if (y > sections.get(i).maxY)
 		        	sections.get(i).maxY = y;
 		        else if (y < sections.get(i).minY)
 		        	sections.get(i).minY = y;
 		        break;
 		      } 
 		    } 
 		    if (!hasComboBeenSeen) {
 		        Section newSection = new Section();
 		        newSection.boolCombo=pointLineRelationships[x][y];
 		        newSection.xPoints.add(x);
 		        newSection.yPoints.add(y);
 		        newSection.maxX = x;
 		        newSection.minX = x;
 		        newSection.maxY = y;
 		        newSection.minY = y;
 		        sections.add(newSection);
 		        numberOfSections++;
 		    } 
 		  }
 		}
 	}
 	
 	void findOptimalRoute(int[][] board) {
 		/* initialize a weighted graph, where each node 
 		is a midpoint and the weights represent the actual distances between them, taking obstacles into account */
 		midpointGraph = new WeightedGraph(numberOfSections); 
 		orderedSections = new int[numberOfSections];
 		
 		// initializing AStar
 		FHeuristic fh = new FHeuristic();
 		AreaMap cleanMap = new AreaMap(100,100);
 	    for(int i = 0; i < board.length; i++) {
 	        for(int j = 0; j < board[0].length; j++) {
 	            for(Line2D wall: walls) {
 	                if(wall.ptSegDist(i, j) < 2.0) {
 	                	cleanMap.getNodes().get(i).get(j).isObstacle = true; // nay on the current node
 	                }
 	            }
 	        }
 	    }
 		astar = new AStar(cleanMap, fh);
 		
 		for (int i=0; i<numberOfSections; i++) {
 			cleanMap = new AreaMap(100,100);
 		    for(int k = 0; k < board.length; k++) {
 		        for(int l = 0; l < board[0].length; l++) {
 		            for(Line2D wall: walls) {
 		                if(wall.ptSegDist(k, l) < 2.0) {
 		                	cleanMap.getNodes().get(k).get(l).isObstacle = true; // nay on the current node
 		                }
 		            }
 		        }
 		    }
 			astar = new AStar(cleanMap, fh);
 			for (int j=0; j<numberOfSections; j++) {
 				if (i==j) {
 					midpointGraph.addEdge(i,j,0); 
 					midpointGraph.setLabel(i, ("v" + j));
 				} else if (i>j) {
 					midpointGraph.addEdge(i, j, midpointGraph.getWeight(j, i));
 					midpointGraph.setLabel(i, ("v" + j));
 				} else {
 					//build an adjacency matrix with the distance between each midpoint
 					astar.calcShortestPath(sections.get(i).midX, sections.get(i).midY, sections.get(j).midX, sections.get(j).midY);
 					//log.error("line 311.  iX: " + sections.get(i).midX + " iY: " + sections.get(j).midY);
 					if (astar.shortestPath == null) {
 						//TODO: This is hacky
 						midpointGraph.addEdge(i,  j, 500);
 					} else {
 						midpointGraph.addEdge(i,j,astar.shortestPath.getLength());
 						midpointGraph.setLabel(i, ("v" + j));
 					}
 				}
 			} 
 		}
 		orderedSections = Prims.prim(midpointGraph, 0);
 	}
 }
 

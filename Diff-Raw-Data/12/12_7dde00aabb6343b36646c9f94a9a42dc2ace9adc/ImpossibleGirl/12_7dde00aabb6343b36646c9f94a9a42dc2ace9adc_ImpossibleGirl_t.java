 package mosquito.g0;
 
 import java.awt.geom.Line2D;
 import java.awt.geom.Point2D;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.Random;
 import java.util.List;
 import java.util.Set;
 import java.util.PriorityQueue;
 
 import org.apache.log4j.Logger;
 
 import mosquito.g0.sweepSection;
 import mosquito.sim.Collector;
 import mosquito.sim.Light;
 import mosquito.sim.MoveableLight;
 
 public class ImpossibleGirl extends mosquito.sim.Player {
 	
 	//Related to which appraoch will be used
 	public static final int SECTION = 0;
 	public static final int SWEEP = 1;
 	public static final int GREEDY = 2;
 	private int currentApproach = 0;
   
 	// related to SECTIONS
 	private int pointLineRelationships[][][];
 	private ArrayList<Section> sections = new ArrayList<Section>();
 	int numberOfSections;
 	//	ArrayList<Section> prunedSections;
 	
 	//related to SWEEP
 	private HashMap<Integer, Integer> numLightsToSpacingMap = new HashMap<Integer, Integer>();
 	private Point2D.Double lastLight;
 	private Set<MoveableLight> sweeplights;
 	private Light[] allLights;
 	private LinkedList<sweepSection> boardSections = new LinkedList<sweepSection>();
     
 
 	// general instance variables relevant to our problem
 	private Set<Light> lights;
 	private Set<Line2D> walls;
 	private ArrayList<MoveableLight> mlights;
 	private Logger log = Logger.getLogger(this.getClass()); // for logging
 	private int collectorX;
 	private int collectorY;
 	private int numLights;
 	
 	// related to astar
 	private AStar astar;
 	
 	// related to prims
 	private int[] orderedSections;
     private HashMap<MoveableLight, Boolean> movementMap = new HashMap<MoveableLight, Boolean>();
     private HashMap<MoveableLight, Integer> lightsToMovesMap = new HashMap<MoveableLight, Integer>();
 	
     
 	@Override
 	public String getName() {
 		return "I section things";
 	}
 	
 	
 	
 	
 	
 	
 	/*
 	 * This is called when a new game starts. It is passed the set
 	 * of lines that comprise the different walls, as well as the 
 	 * maximum number of lights you are allowed to use.
 	 */ 
 	@Override
 	public ArrayList<Line2D> startNewGame(Set<Line2D> walls, int numLights) {
 		this.numLights = numLights;
 		this.walls = walls;
 		ArrayList<Line2D> lines = new ArrayList<Line2D>();
 		
 		//Choose which approach will initially be used to clear the board
		if (walls.size() < 60) currentApproach = SECTION;
 		else if (1==1) currentApproach = SWEEP;  //TODO: Define situations where sweeping is not optimal; ie, the board has many vertical barriers
 		else currentApproach = GREEDY;
 		
 		
 		if (currentApproach == SECTION) {
 			pointLineRelationships = new int[100][100][walls.size()];
 			sectioningAlgorithm();
 			identifySections(pointLineRelationships);
 			
 			// set the midpoints for each of the sections
 			for(int i = 0; i < sections.size(); i++) {
 				sections.get(i).setMidpoints();
 			}
 					
 			this.sections = this.pruneSections(this.sections, walls);
 			numberOfSections = this.sections.size();
 			
 			for (int i=0; i<numberOfSections; i++) {
 				//Draw some nice lines to mark waypoints
 				Point2D ul = new Point2D.Double(sections.get(i).midX-1, sections.get(i).midY-1);
 				Point2D ll = new Point2D.Double(sections.get(i).midX-1, sections.get(i).midY+1);
 				Point2D ur = new Point2D.Double(sections.get(i).midX+1, sections.get(i).midY-1);
 				Point2D lr = new Point2D.Double(sections.get(i).midX+1, sections.get(i).midY+1);
 				
 				Line2D c1 = new Line2D.Double(ul, lr);
 				Line2D c2 = new Line2D.Double(ll, ur);
 				
 				lines.add(c1);
 				lines.add(c2);
 				
 			}
 		} //SECTION
 		
 		else if (currentApproach == SWEEP) {
 			 // vertical lines have undefined slope, so we keep their positions
 	        ArrayList<Line2D.Double> verticalLines = new ArrayList<Line2D.Double>();  
 	        ArrayList<Point2D.Double> midpoints = new ArrayList<Point2D.Double>();
 	        
 	        this.numLights = numLights;
 	        this.walls = walls;
 	        
 	        ArrayList<Line2D> extendedLines = new ArrayList<Line2D>();
 	        
 	        numLightsToSpacingMap.put(10, 9);
 	        numLightsToSpacingMap.put(9, 7);
 	        numLightsToSpacingMap.put(8, 8);
 	        numLightsToSpacingMap.put(7, 10);
 	        numLightsToSpacingMap.put(6, 13);
 	        numLightsToSpacingMap.put(5, 18);
 	        numLightsToSpacingMap.put(4, 22);
 	        numLightsToSpacingMap.put(3, 33);
 	        numLightsToSpacingMap.put(1, 22);
 	        // creating game borders
 	        Point2D.Double uR = new Point2D.Double(0, 100.0);
 	        Point2D.Double uL = new Point2D.Double(0, 0);
 	        Point2D.Double lL = new Point2D.Double(0, 100.0);
 	        Point2D.Double lR = new Point2D.Double(100.0, 100.0);
 	        
 	        Line2D l = new Line2D.Double(uL, lL);
 	        Line2D r = new Line2D.Double(uR, lR);
 	        Line2D u = new Line2D.Double(uL, uR);
 	        Line2D d = new Line2D.Double(lL, lR);
 	        
 	        sweepSection boundary = new sweepSection(l, r, u, d);
 	        ArrayList<Double> sortedVerticalLines = new ArrayList<Double>();
 	        boardSections.add(boundary);
 	        
 	        for (Line2D w : walls) {
 	            double x1 = w.getX1();
 	            double x2 = w.getX2();
 	            double y1 = w.getY1();
 	            double y2 = w.getY2();
 	            
 	            // dealing with vertical lines first
 	            if (w.getX1() == w.getX2()) {
 	                verticalLines.add(new Line2D.Double(new Point2D.Double(x1, 0), new Point2D.Double(x1, 100)));
 	                sortedVerticalLines.add(w.getX1());
 	                continue;
 	            }
 	            
 	            Point2D.Double mid = this.getMidpoint(w);
 	            midpoints.add(mid);
 	        }
 	        
 	        // subdivide by vertical sections
 	        for(int i = 0; i < verticalLines.size(); i++) {
 	            // need to segment by vertical section...
 	        }
 		} //SWEEP
 		
 		else {
 			// TODO: GREEDY SETUP //
 		} //GREEDY
 		
 		return lines;
 	}
 
 
 	/*
 	 * This is used to determine the initial placement of the lights.
 	 * It is called after startNewGame.
 	 * The board tells you where the mosquitoes are: board[x][y] tells you the
 	 * number of mosquitoes at coordinate (x, y)
 	 */
 	public Set<Light> getLights(int[][] board) {
 		
 		if (currentApproach == SECTION) {
 			// initializing AStar
 			FHeuristic fh = new FHeuristic();
 			lights = new HashSet<Light>();
 			mlights = new ArrayList<MoveableLight>();
 			this.orderedSections = findOptimalRoute(board, numberOfSections, sections);
 			
 			int collectorIndex = this.orderedSections.length / 2;
			int sectionSize = (numberOfSections-1) / numLights;
			
			AreaMap map = generateAreaMap(board, walls);
 
 			this.collectorX = sections.get(this.orderedSections[collectorIndex]).midX;
 			this.collectorY = sections.get(this.orderedSections[collectorIndex]).midY;
			while(map.getNodes().get(this.collectorX).get(this.collectorY).isObstacle) {
				this.collectorX = sections.get(this.orderedSections[collectorIndex]).midX+1;
				this.collectorY = sections.get(this.orderedSections[collectorIndex]).midY+1;
			}
 			sections.get(this.orderedSections[collectorIndex]).visited = true;
 	
 			int distFromEnd = 0;
 			// initially position each of the nights
 			for (int i = 0; i < numLights; i++) {
 				MoveableLight l;
 				if(i > numberOfSections-1) {
 					Random generator = new Random();
 					int rand = generator.nextInt(i);
 					l = new MoveableLight(rand, rand, true);
 					l.waypoints.add(new Point2D.Double(rand, rand));
 	                l.hasFinishedPhaseOne = true;
 	                movementMap.put(l, false); //This is a hashmap that tells us whether each light is currently on an A* path
 	                lightsToMovesMap.put(l, 0); //This hashmap tells us what move number is the current light in, in its A* path
 				}
 				// to the right of the collector...
 				else if(i <= numLights/2) {
 					int sectionIndex = orderedSections[sectionSize*i];
 					if(!sections.get(sectionIndex).visited) {
 						l = new MoveableLight(sections.get(sectionIndex).midX, sections.get(sectionIndex).midY, true);
 						l.waypoints.add(new Point2D.Double(sections.get(sectionIndex).midX, sections.get(sectionIndex).midY));
 						sections.get(sectionIndex).visited = true;
 					}
 					else {
 						Random generator = new Random();
 						int rand = generator.nextInt(i);
 						l = new MoveableLight(rand, rand, true);
 						l.waypoints.add(new Point2D.Double(sections.get(sectionIndex).midX, sections.get(sectionIndex).midY));
 						l.hasFinishedPhaseOne = true;
 					}
 				}
 				// placing the second half of the lights, beginning from the end light
 				else if(i > numLights/2) {
 					int sectionIndex = orderedSections[numberOfSections-1-distFromEnd*sectionSize];
 					if(!sections.get(sectionIndex).visited) {
 						l = new MoveableLight(sections.get(sectionIndex).midX, sections.get(sectionIndex).midY, true);
 						l.waypoints.add(new Point2D.Double(sections.get(sectionIndex).midX, sections.get(sectionIndex).midY));
 						sections.get(sectionIndex).visited = true;
 					}
 					else {
 						Random generator = new Random();
 						int rand = generator.nextInt(i);
 						l = new MoveableLight(rand,rand, true);
 						l.waypoints.add(new Point2D.Double(sections.get(sectionIndex).midX, sections.get(sectionIndex).midY));
 						l.hasFinishedPhaseOne=true;
 					}
 					distFromEnd++;
 				}
 				else {
 					log.error("SHOULD NEVER HAVE HIT THIS CASE....");
 					log.error("SHOULD NEVER HAVE HIT THIS CASE....");
 					log.error("SHOULD NEVER HAVE HIT THIS CASE....");
 					log.error("SHOULD NEVER HAVE HIT THIS CASE....");
 					//TODO FRANKLIN: look at line below--this is how the lights SHOULD be originally positioned
 					int sectionIndex = orderedSections[i*(numberOfSections/numLights)];
 					// need to place lights on a point where the collector IS NOT
 					l = new MoveableLight(sections.get(sectionIndex).midX, sections.get(sectionIndex).midY, true);
 					l.waypoints.add(new Point2D.Double(sections.get(sectionIndex).midX, sections.get(sectionIndex).midY));
 				}
 				mlights.add(l);
 				lights.add(l);
 				lightsToMovesMap.put(l, 0);
 		        movementMap.put(l, false);
 			}
 		    
 			// add a list of waypoints to each light
 			//TODO FRANKLIN: Match this with the change directly above
 			int lightIndex = 0;
 			int sectionIndex;
 			boolean isStartPoint;
 			Point2D waypoint;
 			AreaMap correctMap = generateAreaMap(board, walls);
 			for(int i = 0; i < numberOfSections; i++) {
 				sectionIndex = orderedSections[i];
 				if(lightIndex > numLights - 1)
 					break;
 				// if we are at the collector, we're done...
 				if(sections.get(sectionIndex).midX == getCollector().getX() && sections.get(sectionIndex).midY == getCollector().getY()) {
 					break;
 				}
 				isStartPoint = mlights.get(lightIndex).getX() == sections.get(lightIndex).midX && mlights.get(lightIndex).getY() == sections.get(lightIndex).midY;
 				if(sections.get(sectionIndex).visited && !isStartPoint) {
 					lightIndex++;
 					continue;
 				}
 				sections.get(sectionIndex).visited=true;
 				waypoint = new Point2D.Double(sections.get(orderedSections[i]).midX, sections.get(orderedSections[i]).midY);
 				if (!correctMap.getNodes().get(sections.get(sectionIndex).midX).get(sections.get(sectionIndex).midY).isObstacle) {
 					mlights.get(lightIndex).waypoints.add(waypoint);
 				}
 			}
 			for(int i = numberOfSections-1; i > 0; i--) {
 				sectionIndex = orderedSections[i];
 				if(lightIndex > numLights - 1)
 					break;
 				if(sections.get(sectionIndex).midX == getCollector().getX() && sections.get(sectionIndex).midY == getCollector().getY()) {
 					lightIndex++;
 					break;
 				}
 				isStartPoint = mlights.get(lightIndex).getX() == sections.get(lightIndex).midX && mlights.get(lightIndex).getY() == sections.get(lightIndex).midY;
 				if(sections.get(sectionIndex).visited && !isStartPoint) {
 					lightIndex++;
 					continue;
 				}
 				sections.get(sectionIndex).visited=true;
 				waypoint = new Point2D.Double(sections.get(sectionIndex).midX, sections.get(sectionIndex).midY);
 				
 				if (!correctMap.getNodes().get(sections.get(sectionIndex).midX).get(sections.get(sectionIndex).midY).isObstacle) {
 					mlights.get(lightIndex).waypoints.add(waypoint);
 				}
 			}
 			
 //			int lindex = 0;
 //			for(MoveableLight currLight : mlights) {
 //				log.error("starting position of light " + lindex + " is " + currLight.getX() + "," + currLight.getY());
 //				lindex++;
 //			}
 			
 			// for each light, make the last light the collector
 			for (int i = 0; i < numLights; i++) {
 				mlights.get(i).waypoints.add(new Point2D.Double(getCollector().getX(), getCollector().getY()));
 //				System.err.println(collectorX + ", " + collectorY);
 			}
 			
 			int len;
 			Point2D currPoint;
 			Point2D nextPoint;
 			// generate paths between waypoints
 			for (MoveableLight currLight : mlights) {
 				len = currLight.waypoints.size();
 //				log.error("size of these waypoints sets are " + len);
 //				log.error("first element is " + currLight.waypoints.get(0));
 				// set all paths in moving light
 				for (int j = 0; j < len-1; j++) {
 					currPoint = currLight.waypoints.get(j);
 					nextPoint = currLight.waypoints.get(j+1);
 					AreaMap cleanMap = generateAreaMap(board, walls);
 					astar = new AStar(cleanMap, fh);
 //					log.error((int) currPoint.getX() + "," + (int) currPoint.getY() + " " + (int) nextPoint.getX() + "," + (int) nextPoint.getY());
 					astar.calcShortestPath((int)currPoint.getX(), (int)currPoint.getY(), (int)nextPoint.getX(), (int)nextPoint.getY());
 					currLight.shortestPaths.add(astar.shortestPath);
 				}
 				// initialize paths to first path
 				if(currLight.shortestPaths.size() == 0)
 					continue;
 				currLight.currPath = currLight.shortestPaths.get(0);
 			}
 		} //end SECTION
 		
 		
 		else if (currentApproach == SWEEP) {
 			 // initializing AStar
 	        FHeuristic fh = new FHeuristic();
 	        
 	        int x = 0; 
 	        int y = numLightsToSpacingMap.get(numLights);
 	        
 	        lights = new HashSet<Light>();
 	        lastLight = new Point2D.Double(10, 10);
 	        sweeplights = new HashSet<MoveableLight>();
 	        for(int a = 0; a<numLights; a++)
 	        {
 	            AreaMap cleanMap = new AreaMap(101,101);
 	            for(int i = 0; i < board.length; i++) {
 	                for(int j = 0; j < board[0].length; j++) {
 	                    for(Line2D wall: walls) {
 	                        if(wall.ptSegDist(i, j) < 3.0) {
 	                            cleanMap.getNodes().get(i).get(j).isObstacle = true; // nay on the current node
 	                        }
 	                    }
 	                }
 	            }
 	            astar = new AStar(cleanMap, fh);
 	            lastLight = new Point2D.Double(x, y);
 	            y += numLightsToSpacingMap.get(numLights);
 	            MoveableLight l = new MoveableLight(lastLight.getX(), lastLight.getY(), true);
 
 	            lights.add(l);
 	            
 	            lightsToMovesMap.put(l, 0);
 	            sweeplights.add(l);
 	            movementMap.put(l, false);
 	        }
 	        allLights = lights.toArray(new Light[numLights]);
 		} //end SWEEP
 		
 		else {
 			//GREEDY//
 		} //GREEDY
 		return lights;
 	}
 	
 	/*
 	 * This is called at the beginning of each step (before the mosquitoes have moved)
 	 * If your Set contains additional lights, an error will occur. 
 	 * Also, if a light moves more than one space in any direction, an error will occur.
 	 * The board tells you where the mosquitoes are: board[x][y] tells you the
 	 * number of mosquitoes at coordinate (x, y)
 	 */
 	public Set<Light> updateLights(int[][] board) {
 		
 		if (currentApproach == SECTION) {
 			for (MoveableLight ml : mlights) {
 				//TODO: VISHWA, this movementMap.get(ml) isn't  being set correctly. we're only returning to the collector once now
 	            if(ml.getX() == getCollector().getX() && ml.getY() == getCollector().getY() /*&& !movementMap.get(ml)*/) { //If you've reached the collector, stay there for 15 moves
 	                if(ml.numMovesAtCollector >= 12) { //If you've stayed at the collector for 15 moves then time to move on
 	                    ml.hasFinishedPhaseOne = true;
 	                    ml.numMovesAtCollector = 0;
 	                    movementMap.put(ml, false); //This is a hashmap that tells us whether each light is currently on an A* path
 	                    lightsToMovesMap.put(ml, 0); //This hashmap tells us what move number is the current light in, in its A* path
 	                }
 	                else {
 	                    ml.numMovesAtCollector++; //If you haven't stayed for 15 moves yet, stay put and increment your movesAtCollector
 	                    continue;
 	                }
 	            }
 	            
 	            if(ml.hasFinishedPhaseOne) {
 					if(!movementMap.get(ml)) { // If we aren't on an A* path
 		                List<Point2D.Double> mosquitoLocations = getMosquitoLocationsByDistance(board, ml); //Get locations of all the mosquitos ordered in descending order by distance
 		                if(!mosquitoLocations.isEmpty()) {
 		                    AreaMap cleanMap = generateAreaMap(board, walls);
 		                    FHeuristic fh = new FHeuristic();
 		                    astar = new AStar(cleanMap, fh);
 		                    astar.calcShortestPath((int)ml.getX(),    //Calculate a* path to the farthest mosquito
 		                            (int) ml.getY(), 
 		                            (int)mosquitoLocations.get(0).getX(), 
 		                            (int)mosquitoLocations.get(0).getY());
 		                    ml.currDestinationX = mosquitoLocations.get(0).getX(); 
 		                    ml.currDestinationY = mosquitoLocations.get(0).getY();
 		                    ml.shortestPath = astar.shortestPath;
 		                    movementMap.put(ml, true);
 		                    log.error("Current x: "+ml.getX());
 		                    log.error("Current y: "+ml.getY());
 		                    if(ml.shortestPath != null) {
 		                        log.error("Moving to x = "+ml.shortestPath.getX(0)); //Start moving towards farthest mosquito
 		                        log.error("Moving to y = "+ml.shortestPath.getY(0));
 		                        ml.moveTo(ml.shortestPath.getX(0), ml.shortestPath.getY(0));
 		                        lightsToMovesMap.put(ml, 1);
 		                    }
 		                }
 		            }
 		            else if(ml.getX() == ml.currDestinationX && ml.getY() == ml.currDestinationY) { //Once we've reached the farthest mosquito we now go back to the collector
 		            	movementMap.put(ml, true);
 		                ml.currDestinationX = 0;
 		                ml.currDestinationY = 0;
 		                lightsToMovesMap.put(ml, 0);
 		                AreaMap cleanMap = generateAreaMap(board, walls);
 		                FHeuristic fh = new FHeuristic();
 		                astar = new AStar(cleanMap, fh);
 		                astar.calcShortestPath((int)ml.getX(), (int) ml.getY(), (int)getCollector().getX(), (int)getCollector().getY());
 		                ml.currDestinationX = getCollector().getX();
 		                ml.currDestinationY = getCollector().getY();
 		                ml.shortestPath = astar.shortestPath;
 		                movementMap.put(ml, true);
 		                log.error("Current x: "+ml.getX());
 		                log.error("Current y: "+ml.getY());
 		                if(ml.shortestPath != null) {
 	    	                if(ml.shortestPath.getLength() > 0) {
 	    	                    log.error("Moving to x = "+ml.shortestPath.getX(0));
 	    	                    log.error("Moving to y = "+ml.shortestPath.getY(0));
 	    	                    ml.moveTo(ml.shortestPath.getX(0), ml.shortestPath.getY(0));
 	    	                    lightsToMovesMap.put(ml, 1);
 	    	                }
 		                }
 		                continue;
 		            }
 		            else if(ml.shortestPath != null){ //If we haven't reached the furthest mosquito then continue moving towards it using the A* path
 		                int moveNum = lightsToMovesMap.get(ml);
 		                log.error("CURR: x = "+ml.getX()+ " y = "+ml.getY());
 		                log.error("PATH: x = "+ml.shortestPath.getX(moveNum)+" y = "+ml.shortestPath.getY(moveNum));//Log the place we are moving to
 		                ml.moveTo(ml.shortestPath.getX(moveNum), ml.shortestPath.getY(moveNum));
 		                log.error(ml +"   distance: x = "+(ml.shortestPath.getX(moveNum) - ml.getX())+" y = "+(ml.shortestPath.getY(moveNum) - ml.getY())); //Log the distance between the current position and the next position.
 		                moveNum++;
 		                lightsToMovesMap.put(ml, moveNum);
 		            }
 	            }
 	            else { 
 	    			Path currPath = ml.currPath; // get the current path we're working through
 	    			if (currPath == null) {
 	    				ml.move = 0;
 	    				ml.indexOfPath++;
 	    				if(ml.indexOfPath >= ml.shortestPaths.size())
 	    					continue;
 	    				ml.currPath = ml.shortestPaths.get(ml.indexOfPath);
 	    				continue;
 	    			}
 	    						
 	    			// check to see if we're done moving
 	    			if (ml.move >= currPath.getLength()) {
 	    				ml.indexOfPath++;
 	    				if (ml.indexOfPath >= (ml.shortestPaths.size())) {
 	    					continue;
 	    				}
 	    				
 	    				ml.currPath = ml.shortestPaths.get(ml.indexOfPath);
 	    				ml.move = 0;
 	    				continue;
 	    			}
 	    			
 	    			ml.moveTo(currPath.getX(ml.move), currPath.getY(ml.move));
 	    			ml.move++;
 	            }
 			}
 		} //SECTION
 		
 		else if (currentApproach == SWEEP) {
 			for(MoveableLight ml: sweeplights) {
 	            if(ml.getX() == 97 && ml.getY() == 50) { //If you've reached the collector, stay there for 15 moves
 	                if(ml.numMovesAtCollector >= 15) { //If you've stayed at the collector for 15 moves then time to move on
 	                    ml.hasFinishedPhaseOne = true;
 	                    movementMap.put(ml, false); //This is a hashmap that tells us whether each light is currently on an A* path
 	                    lightsToMovesMap.put(ml, 0); //This hashmap tells us what move number is the current light in, in its A* path
 	                }
 	                else {
 	                    ml.numMovesAtCollector++; //If you haven't stayed for 15 moves yet, stay put and increement your movesAtCollector
 	                    continue;
 	                }
 	            }
 	            if(!ml.hasFinishedPhaseOne) { //If we are still sweeping from left to right (i.e Phase 1 of the strategy)
 	                if(ml.getX() == 97 && !movementMap.get(ml)) { //If we've reached the right side of the board after sweeping from left to right AND we aren't on an A* path as it is
 	                    log.error("REACHED THE OTHER SIDE !!!!!!!!");
 	                    AreaMap cleanMap = new AreaMap(101,101);
 	                    for(int i = 0; i <= board.length; i++) {
 	                        for(int j = 0; j <= board[0].length; j++) {
 	                            for(Line2D wall: walls) {
 	                                if(wall.ptSegDist(i, j) < 2.0) { //Create our A* graph by marking something as an obstacle if it's within 2 units of distance
 	                                    cleanMap.getNodes().get(i).get(j).isObstacle = true; // nay on the current node
 	                                }
 	                            }
 	                        }
 	                    }
 	                    FHeuristic fh = new FHeuristic();
 	                    astar = new AStar(cleanMap, fh);
 	                    astar.calcShortestPath((int)ml.getX(), (int) ml.getY(), 97, 50); //Compute the A* path from here to the collector
 	                    ml.currDestinationX = 97;
 	                    ml.currDestinationY = 50;
 	                    ml.shortestPath = astar.shortestPath;
 	                    movementMap.put(ml, true);
 	                    log.error("Current x: "+ml.getX());
 	                    log.error("Current y: "+ml.getY());
 	                    if(ml.shortestPath != null) {
 	                        log.error("Moving to x = "+ml.shortestPath.getX(0));
 	                        log.error("Moving to y = "+ml.shortestPath.getY(0));
 	                        ml.moveTo(ml.shortestPath.getX(0), ml.shortestPath.getY(0)); //Move once in the A* path to initiate the A* movement sequence
 	                        lightsToMovesMap.put(ml, 1);
 	                    }
 	                    // THIS IS A HACK!!!!! 
 	                    // ANYONE WHO READS THIS MUST KNOW THAT IF YOU DO SUCH THINGS IN REAL WORLD PROJECTS 
 	                    // KITTENS WILL DIE SPONTANEOUSLY
 	                    else {
 	                        ml.turnOff();
 	                    }
 	                    continue;
 	                }
 	                if(ml.hasStoppedAtCorner) { //Code to ensure that we only stop at a corner once to wait for mosquitos to catch up
 	                    ml.numMovesSinceStopped++;
 	                    if(ml.numMovesSinceStopped == 25) {
 	                        ml.numMovesSinceStopped = 0;
 	                        ml.hasStoppedAtCorner = false;
 	                    }
 	                }
 	                boolean hasMovedThisTurn = false; //Boolean variable to ensure that a light can only enter a critical code section, i.e where movement occurs, once.
 	                if(ml.numMovesSinceStopped == 0) { //Check to see if we need to stop at a corner assuming we haven't seen a corner in the last 25 moves
 	                    for(Line2D obstacle: walls) {
 	                        if(!ml.hasStoppedAtCorner && (obstacle.getP1().distance(ml.getLocation()) < 3 || obstacle.getP2().distance(ml.getLocation()) < 3)) {
 	                            if(ml.numTurnsAtCorner >= 10) { //Wait for obstacle for 10 moves
 	                                ml.numTurnsAtCorner = 0;
 	                                ml.hasStoppedAtCorner = true;
 	                                ml.numMovesSinceStopped = 1;
 	                            }
 	                            else {
 	                                ml.numTurnsAtCorner++;
 	                                hasMovedThisTurn = true;
 	                            }
 	                            break;
 	                        }
 	                    }
 	                }
 	                if(ml.getX() == ml.currDestinationX && ml.getY() == ml.currDestinationY) { //If the light has reached its A* destination in our left to right sweep
 	                    log.error("4");
 	                    if(!hasMovedThisTurn) {
 	                        movementMap.put(ml, false);
 	                        lightsToMovesMap.put(ml, 0);
 	                        ml.moveRight();
 	                        hasMovedThisTurn = true;
 	                    }
 	                }
 	                else if(!movementMap.get(ml)) { // If the light isn't currently moving on an A* path
 	                    boolean hasHitWall = false;
 	                    for(Line2D obstacle: walls) { //Check for obstacles and see if we are near an obstacle
 	                        if(obstacle.ptSegDistSq(ml.getX(), ml.getY()) <= 4.0) { //We've detected an obstacle 
 	                            log.error("ENTERED BAD AREA!!! : "+ml);
 	                            hasHitWall = true;
 	                            AreaMap cleanMap = new AreaMap(101,101);
 	                            for(int i = 0; i <= board.length; i++) {
 	                                for(int j = 0; j <= board[0].length; j++) {
 	                                    for(Line2D wall: walls) {
 	                                        if(wall.ptSegDist(i, j) < 2.0) {
 	                                            cleanMap.getNodes().get(i).get(j).isObstacle = true; // nay on the current node
 	                                        }
 	                                    }
 	                                }
 	                            }
 	                            FHeuristic fh = new FHeuristic();
 	                            astar = new AStar(cleanMap, fh);
 	                            int xCoordToMoveTo = 0;
 	                            for(int i = (int) ml.getX() + 2; i < 101; i++) {
 	                                if(!cleanMap.getNodes().get(i).get((int)ml.getY()).isObstacle) {
 	                                    xCoordToMoveTo = i;
 	                                    break;
 	                                }
 	                            }
 	                            astar.calcShortestPath((int)ml.getX(), (int) ml.getY(), xCoordToMoveTo, (int) ml.getY()); //Since we've seen an obstacle we need to move the point 6 units to the right
 	                            ml.currDestinationX = xCoordToMoveTo;
 	                            ml.currDestinationY = ml.getY();
 	                            ml.shortestPath = astar.shortestPath;
 	                            movementMap.put(ml, true);
 	                            log.error("Current x: "+ml.getX());
 	                            log.error("Current y: "+ml.getY());
 	                            if(ml.shortestPath != null && !hasHitWall) {
 	                                log.error("Moving to x = "+ml.shortestPath.getX(0));
 	                                log.error("Moving to y = "+ml.shortestPath.getY(0));
 	                                ml.moveTo(ml.shortestPath.getX(0), ml.shortestPath.getY(0));
 	                                lightsToMovesMap.put(ml, 1);
 	                            }
 	                            // THIS IS A HACK!!!!! 
 	                            // ANYONE WHO READS THIS MUST KNOW THAT IF YOU DO SUCH THINGS IN REAL WORLD PROJECTS 
 	                            // KITTENS WILL DIE SPONTANEOUSLY
 	                            else if(ml.shortestPath == null){
 	                                ml.turnOff();
 	                            }
 	                        }
 	                    }
 	                    if(!hasHitWall) { //If there were no obstacles just move right
 	                        log.error("3" + ml);
 	                        if(!hasMovedThisTurn) {
 	                            ml.moveRight();
 	                            hasMovedThisTurn = true;
 	                        }
 	                    }
 	                }
 	                else {
 	                    if(ml.shortestPath == null) { //If there were no obstacles move right
 	                        log.error("2");
 	                        if(!hasMovedThisTurn) {
 	                            ml.moveRight();
 	                            hasMovedThisTurn = true;
 	                        }
 	                    }
 	                    else if(lightsToMovesMap.get(ml) >= ml.shortestPath.getLength()) { //If we've come to the end of our A* path then reset the A* moves and mvoe right
 	                        log.error("1");
 	                        if(!hasMovedThisTurn) {
 	                            ml.moveRight();
 	                            lightsToMovesMap.put(ml, 0);
 	                            movementMap.put(ml, false);
 	                            hasMovedThisTurn = true;
 	                        }
 	                    }
 	                    else { //If we are still on our A* path then move to the next position in the A* path
 	                        if(!hasMovedThisTurn) {
 	                            int moveNum = lightsToMovesMap.get(ml);
 	                            log.error("CURR: x = "+ml.getX()+ " y = "+ml.getY());
 	                            log.error("PATH: x = "+ml.shortestPath.getX(moveNum)+" y = "+ml.shortestPath.getY(moveNum));//Log the place we are moving to
 	                            ml.moveTo(ml.shortestPath.getX(moveNum), ml.shortestPath.getY(moveNum));
 	                            log.error(ml +"   distance: x = "+(ml.shortestPath.getX(moveNum) - ml.getX())+" y = "+(ml.shortestPath.getY(moveNum) - ml.getY())); //Log the distance between the current position and the next position.
 	                            moveNum++;
 	                            lightsToMovesMap.put(ml, moveNum);
 	                            hasMovedThisTurn = true;
 	                        }
 	                    }
 	                }
 	            }
 	            else { //If we've finished phase one (i.e reached the collector once) then do this
 	                if(!movementMap.get(ml)) { // If we aren't on an A* path
 	                    List<Point2D.Double> mosquitoLocations = getMosquitoLocationsByDistance(board, ml); //Get locations of all the mosquitos ordered in descending order by distance
 	                    if(!mosquitoLocations.isEmpty()) {
 	                        AreaMap cleanMap = new AreaMap(101,101);
 	                        for(int i = 0; i <= board.length; i++) {
 	                            for(int j = 0; j <= board[0].length; j++) {
 	                                for(Line2D wall: walls) {
 	                                    if(wall.ptSegDist(i, j) < 2.0) {
 	                                        cleanMap.getNodes().get(i).get(j).isObstacle = true; // nay on the current node
 	                                    }
 	                                }
 	                            }
 	                        }
 	                        FHeuristic fh = new FHeuristic();
 	                        astar = new AStar(cleanMap, fh);
 	                        astar.calcShortestPath((int)ml.getX(),    //Calculate a* path to the farthest mosquito
 	                                (int) ml.getY(), 
 	                                (int)mosquitoLocations.get(0).getX(), 
 	                                (int)mosquitoLocations.get(0).getY());
 	                        ml.currDestinationX = mosquitoLocations.get(0).getX(); 
 	                        ml.currDestinationY = mosquitoLocations.get(0).getY();
 	                        ml.shortestPath = astar.shortestPath;
 	                        movementMap.put(ml, true);
 	                        log.error("Current x: "+ml.getX());
 	                        log.error("Current y: "+ml.getY());
 	                        if(ml.shortestPath != null) {
 	                            log.error("Moving to x = "+ml.shortestPath.getX(0)); //Start moving towards farthest mosquito
 	                            log.error("Moving to y = "+ml.shortestPath.getY(0));
 	                            ml.moveTo(ml.shortestPath.getX(0), ml.shortestPath.getY(0));
 	                            lightsToMovesMap.put(ml, 1);
 	                        }
 	                    }
 	                }
 	                else if(ml.getX() == ml.currDestinationX && ml.getY() == ml.currDestinationY) { //Once we've reached the farthest mosquito we now go back to the collector
 	                    movementMap.put(ml, true);
 	                    ml.currDestinationX = 0;
 	                    ml.currDestinationY = 0;
 	                    lightsToMovesMap.put(ml, 0);
 	                    AreaMap cleanMap = new AreaMap(101,101);
 	                    for(int i = 0; i <= board.length; i++) {
 	                        for(int j = 0; j <= board[0].length; j++) {
 	                            for(Line2D wall: walls) {
 	                                if(wall.ptSegDist(i, j) < 2.0) {
 	                                    if(i==99 && j==50) {
 	                                        log.error("REMOVE IMPORTANT NODE!!!");
 	                                    }
 	                                    cleanMap.getNodes().get(i).get(j).isObstacle = true; // nay on the current node
 	                                }
 	                            }
 	                        }
 	                    }
 	                    FHeuristic fh = new FHeuristic();
 	                    astar = new AStar(cleanMap, fh);
 	                    astar.calcShortestPath((int)ml.getX(), (int) ml.getY(), 97, 50);
 	                    ml.currDestinationX = 97;
 	                    ml.currDestinationY = 50;
 	                    ml.shortestPath = astar.shortestPath;
 	                    movementMap.put(ml, true);
 	                    log.error("Current x: "+ml.getX());
 	                    log.error("Current y: "+ml.getY());
 	                    if(ml.shortestPath != null) {
 	                        log.error("Moving to x = "+ml.shortestPath.getX(0));
 	                        log.error("Moving to y = "+ml.shortestPath.getY(0));
 	                        ml.moveTo(ml.shortestPath.getX(0), ml.shortestPath.getY(0));
 	                        lightsToMovesMap.put(ml, 1);
 	                    }
 	                    continue;
 	                }
 	                else { //If we haven't reached the furthest mosquito then continue moving towards it using the A* path
 	                    int moveNum = lightsToMovesMap.get(ml);
 	                    log.error("CURR: x = "+ml.getX()+ " y = "+ml.getY());
 	                    log.error("PATH: x = "+ml.shortestPath.getX(moveNum)+" y = "+ml.shortestPath.getY(moveNum));//Log the place we are moving to
 	                    ml.moveTo(ml.shortestPath.getX(moveNum), ml.shortestPath.getY(moveNum));
 	                    log.error(ml +"   distance: x = "+(ml.shortestPath.getX(moveNum) - ml.getX())+" y = "+(ml.shortestPath.getY(moveNum) - ml.getY())); //Log the distance between the current position and the next position.
 	                    moveNum++;
 	                    lightsToMovesMap.put(ml, moveNum);
 	                }
 	            }
 	        }
 		} //SWEEP
 		
 		else {
 			//GREEDY
 		} //GREEDY
 		
 		return lights;
 	} //updateLights
 
 	 /* Currently this is only called once (after getLights), so you cannot move the Collector. */
 	@Override
 	public Collector getCollector() {
 		Collector c;
 		if (currentApproach == SECTION) {
 			//TODO: Collector should be placed at middlemost point of orderedSections array
 			// this one just places a collector next to the last light that was added
 			int x = this.collectorX;
 			int y = this.collectorY;
 			c = new Collector(x, y);
 //			if(x-1 < 0 && y-1 < 0)
 //				c = new Collector(x+1, y+1);
 //			else if(x-1 < 0)
 //				c = new Collector(x, y-1);
 //			else if(y-1 < 0)
 //				c = new Collector(x-1, y);
 //			else
 //				c = new Collector(this.collectorX-1, this.collectorY-1);
 		} //SECTION
 		
 		else if (currentApproach == SWEEP) {
 			c = new Collector(97,50);
 		} //SWEEP
 		
 		else {
 			c = new Collector(50,50);
 		} //GREEDY
 		
 		return c;
 	} //getCollector
 	
 	private ArrayList<Section> pruneSections(ArrayList<Section> sections, Set<Line2D> walls) {
 		int x1, x2, y1, y2;
 		ArrayList<Section> prunedSections = sections;
 		for(int i = 0; i < sections.size(); i++) {
 			Section s = sections.get(i);
 			x1 = s.midX;
 			y1 = s.midY;
 			for(int j = i+1; j < sections.size(); j++) {
 				Section st = sections.get(j);
 				x2 = st.midX;
 				y2 = st.midY;
 				if(ptDist(x1, y1, x2, y2) < 15 && !intersectsWall(x1, y1, x2, y2, walls)) {
 					prunedSections.remove(s);
 					break;
 				}
 			}
 		}
 //		for(Section s : prunedSections)
 //			  System.err.println(s.midX + ", " + s.midY);
 		log.error("Pruned Section size is: " + prunedSections.size());
 		return prunedSections;
 	}
 	
 	private int ptDist(int x1, int y1, int x2, int y2) {
 		return (int)Math.sqrt(Math.pow(x2-x1, 2) + Math.pow(y2-y1, 2));
 	}
 	
 	private boolean intersectsWall(int x1, int y1, int x2, int y2, Set<Line2D> walls) {
 		Point2D p1 = new Point2D.Double(x1, y1);
 		Point2D p2 = new Point2D.Double(x2, y2);
 		Line2D l = new Line2D.Double(p1, p2);
 		for (Line2D w : walls) {
 			if(w.intersectsLine(l))
 				return true;
 		}
 		return false;
 	}
 
 
 	public AreaMap generateAreaMap(int[][] board, Set<Line2D> walls) {
 		AreaMap cleanMap = new AreaMap(101,101);
 		for(int i = 0; i <= board.length; i++) {
 		    for(int j = 0; j <= board[0].length; j++) {
 		        for(Line2D wall: walls) {
 		        	int dist = (int)wall.ptSegDist(i,j);
 		        	if(dist == 0)
 		        		cleanMap.getNodes().get(i).get(j).isObstacle = true;
 		        	if(dist < 0.05)
 		        		cleanMap.getNodes().get(i).get(j).heuristicDistanceFromGoal = Integer.MAX_VALUE; // nay on the current node
 		        	else if(dist < 1) {
 		            	cleanMap.getNodes().get(i).get(j).heuristicDistanceFromGoal = 100000;
 		            }
 		        	else if(dist < 2) {
 		        		cleanMap.getNodes().get(i).get(j).heuristicDistanceFromGoal = 10000; // nay on the curr	ent node
 		        	}
 		        	else if(dist < 3) {
 		        		cleanMap.getNodes().get(i).get(j).heuristicDistanceFromGoal = 1000;
 		        	}
 		        }
 		    }
 		}
 		return cleanMap;
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
 	
 	public void identifySections(int[][][] pointLineRelationships) {
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
 		        Section newSection = new Section(walls.size());
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
 	
 	int[] findOptimalRoute(int[][] board, int numberOfSections, ArrayList<Section> sections) {
 		/* initialize a weighted graph, where each node 
 		is a midpoint and the weights represent the actual distances between them, taking obstacles into account */
 		WeightedGraph midpointGraph = new WeightedGraph(numberOfSections); 
 		orderedSections = new int[numberOfSections];
 
 		// initializing AStar
 		FHeuristic fh = new FHeuristic();
 		AreaMap cleanMap = new AreaMap(101,101); //101 because we don't want to miss out the last row and column of the grid
 	    for(int i = 0; i <= board.length; i++) {
 	        for(int j = 0; j <= board[0].length; j++) {
 	            for(Line2D wall: walls) {
 	                if(wall.ptSegDist(i, j) < 2.0) {
 	                	cleanMap.getNodes().get(i).get(j).isObstacle = true; // nay on the current node
 	                }
 	            }
 	        }
 	    }
 		astar = new AStar(cleanMap, fh);
 		
 
 		//For each section, 
 		for (int i=0; i<numberOfSections; i++) {
 			//log.error("Segment "+i+" is at: ("+sections.get(i).midX+" , "+sections.get(i).midY+" ).");
 			cleanMap = generateAreaMap(board,walls);
 		    
 			astar = new AStar(cleanMap, fh);
 			for (int j=i+1; j<numberOfSections; j++) {
 				//build an adjacency matrix with the distance between each midpoint
 				astar.calcShortestPath(sections.get(i).midX, sections.get(i).midY, sections.get(j).midX, sections.get(j).midY);
 				//log.error("line 311.  iX: " + sections.get(i).midX + " iY: " + sections.get(j).midY);
 				if (astar.shortestPath == null) {
 					//TODO: This is hacky
 					midpointGraph.addEdge(i,  j, 10000);
 				} else midpointGraph.addEdge(i,j,astar.shortestPath.getLength());
 			} 
 		}
 		//midpointGraph.print();
 		orderedSections = TPrim.Prim(midpointGraph);
 		return orderedSections;
 	}
 	
 	public List<Point2D.Double> getMosquitoLocationsByDistance(int[][] board, MoveableLight ml) {
         List<Point2D.Double> result = new ArrayList<Point2D.Double>();
         for(int i = 0; i < 100; i++) {
             for(int j = 0; j < 100; j++) {
                 if(i < 95 && (j < 48 || j > 52) && !isNearAnotherLight(i,j,ml)) { //If the mosquito isn't near the collector and it isn't near another light
                     if(board[i][j] != 0) {
                         result.add(new Point2D.Double(i, j));
                     }
                 }
             }
         }
         Collections.sort(result, new Comparator<Point2D.Double>() {
 
             @Override
             public int compare(Point2D.Double o1, Point2D.Double o2) { //Sort the mosquito's in descending order by distance.
                 Point2D.Double origin = new Point2D.Double(collectorX, collectorY);
                 double o1Distance = origin.distance(o1);
                 double o2Distance = origin.distance(o2);
                 if(o1Distance > o2Distance) {
                     return -1;
                 }
                 else if(o2Distance > o1Distance) {
                     return 1;
                 }
                 return 0;
             }
         });
         
         return result;
     }
     
     public boolean isNearAnotherLight(int i, int j, MoveableLight ml) { //Returns if the the mosquitos at this point are near another light (to avoid random bugs like one light moving to another light
        for(MoveableLight other: sweeplights) {                              //unnecessarily because the other light would have already caught the mosquitos).
             if(other.getLocation() != ml.getLocation()) {
                 Point2D.Double locationOfOther = (Point2D.Double) other.getLocation();
                 Point2D.Double locationOfMosquito = new Point2D.Double(i,j);
                 if (locationOfOther.distance(locationOfMosquito) < 8) {
                     return true;
                 }
             }
         }
         return false;
     }
     
     private Point2D.Double getMidpoint(Line2D line) {
         return new Point2D.Double((line.getX1() + line.getX2()) / 2, (line.getY1() + line.getY2()) / 2);
     }
 
 }
 

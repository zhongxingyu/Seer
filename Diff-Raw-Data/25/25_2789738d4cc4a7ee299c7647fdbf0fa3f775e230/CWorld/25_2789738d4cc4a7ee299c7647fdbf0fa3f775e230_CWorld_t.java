 package Source;
 
 import java.io.File;
 import java.util.*;
 
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.DocumentBuilder;
 
 import Util.CDijkstra;
 import Util.CGraph;
 import Util.CPosition;
 import org.w3c.dom.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: bohnp1
  * Date: 27.09.13
  * Time: 14:30
  */
 public class CWorld {
     private Vector<CWalker> walkers = new Vector<CWalker>();
     //private Map<CWalker, Integer> walkerWaitingQueue = new HashMap<CWalker, Integer>();
 
     public static final Random randomGenerator = new Random();
 
     //private CGraph oGraph;
     private Integer maxCalculationCount = 10000;
     private Integer worldWidth = 800;
     private Integer worldHeight = 580;
 
     private Vector<CObstacle> aoObstacles = new Vector<CObstacle>();
     private CGrid grid = new CGrid(worldWidth, worldHeight);
 
 
     public CWorld ()
     {
 
     }
 
     public Integer getWorldWidth() {
         return worldWidth;
     }
 
     public Integer getWorldHeight() {
         return worldHeight;
     }
 
     public CGrid getGrid() {
         return this.grid;
     }
 
     public Vector<CWalker> getWalkers() {
         return walkers;
     }
 
     public Vector<CObstacle> getObstacles() {
         return aoObstacles;
     }
 
     public void addObstacle(CObstacle obstacle) {
         this.aoObstacles.add(obstacle);
     }
 
     public int getGridSize() {
         return this.grid.gridSizeC;
     }
 
     public Vector<CObstacle> loadConfig (File oConfigFile)
     {
         if(oConfigFile == null) {
             return null;
         }
 
         this.aoObstacles = new Vector<CObstacle>();
         try
         {
             // load Config from File Config.xml
             DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
             DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
             Document oConfigDoc = dBuilder.parse(oConfigFile);
 
             // Get a List off all Obstacles
             NodeList aoObj = oConfigDoc.getElementsByTagName("obj");
 
             for (int i = 0; i < aoObj.getLength(); i++)
             {
                Node oObj = aoObj.item(i);
                Vector<CPosition> aoPosition = new Vector<CPosition>();
 
                if (oObj.hasChildNodes())
                {
                    // Get a List off all Points of a Obstacle
                    NodeList aoPoint = oObj.getChildNodes();
                    for (int j = 0; j < aoPoint.getLength(); j++)
                    {
                        Node oPoint = aoPoint.item(j);
 
                        if (oPoint.getNodeType() == Node.ELEMENT_NODE)
                        {
                            // Get Edge-points of the Obstacle
                            NamedNodeMap oPointAttributes = oPoint.getAttributes();
 
                            double dX = Integer.parseInt(oPointAttributes.getNamedItem("x").getNodeValue());
                            double dY = Integer.parseInt(oPointAttributes.getNamedItem("y").getNodeValue());
                            aoPosition.add(new CPosition(dX, dY));
                        }
 
 
                    }
 
                    addObstacle(new CObstacle(aoPosition));
                }
 
             }
 
             //subscribe obstacles in grid
             grid.subscribeObstacle(getObstacles());
 
             // Get a List off all Walkers
             NodeList walkers = oConfigDoc.getElementsByTagName("walker");
 
             for (int i = 0; i < walkers.getLength(); i++)
             {
                 Node walker = walkers.item(i);
 
                 if (walker.hasChildNodes())
                 {
                     // Get a List off all Points of a Obstacle
                     NodeList points = walker.getChildNodes();
 
                     Node point = points.item(0);
                     while (point.getNodeType() != Node.ELEMENT_NODE)
                     {
                         //remove empty elements
                         point = point.getNextSibling();
                     }
 
                     // Get Source Point with X-Coordinate and Y-Coordinate
                     NamedNodeMap pointAttributes = point.getAttributes();
 
                     double dX = Integer.parseInt(pointAttributes.getNamedItem("x").getNodeValue());
                     double dY = Integer.parseInt(pointAttributes.getNamedItem("y").getNodeValue());
 
                     CPosition source = new CPosition(dX, dY);
 
                     point = point.getNextSibling();
 
                     while (point.getNodeType() != Node.ELEMENT_NODE)
                     {
                         //remove empty elements
                         point = point.getNextSibling();
                     }
 
                     // Get Destination Point with X-Coordinate and Y-Coordinate
                     pointAttributes = point.getAttributes();
 
                     dX = Integer.parseInt(pointAttributes.getNamedItem("x").getNodeValue());
                     dY = Integer.parseInt(pointAttributes.getNamedItem("y").getNodeValue());
 
                     CPosition destination = new CPosition(dX, dY);
 
                     CWalker walkerToAdd = new CWalkerStand(source, destination);
                     Boolean positionOccupied = false;
                     for(CWalker existingWalker : this.walkers) {
                         if(existingWalker.checkCollisionWith(walkerToAdd, false)) {
                             positionOccupied = true;
                             break;
                         }
                     }
 
                     if(positionOccupied) {
 //                        Boolean allreadyInWaitingQueue = false;
 //                        for ( Map.Entry<CWalker, Integer> entry : this.walkerWaitingQueue.entrySet() ) {
 //                            if(entry.getKey().checkCollisionWith(walkerToAdd, false)) {
 //                                entry.setValue(entry.getValue() + 1);
 //                                allreadyInWaitingQueue = true;
 //                            }
 //                        }
 //
 //                        if(!allreadyInWaitingQueue) {
 //                            this.walkerWaitingQueue.put(walkerToAdd, 1);
 //                        }
                     }
                     else {
                         this.walkers.add(walkerToAdd);
                     }
                 }
 
             }
 
         }
         catch (Exception e)
         {
           System.out.print(e);
         }
         return this.aoObstacles;
     }
 
     private CGraph oGraph = null;
 
     public CGraph getGraph() {
         return oGraph;
     }
 
     public void buildGraph() {
 
         CGraph.resetId();
 
         this.oGraph = new CGraph(this.getWorldWidth(), this.getWorldHeight());
 
         // add obstacle lines
         for(CObstacle obstacle : this.aoObstacles) {
             Vector<CPosition> positions = obstacle.getPositions();
             for(int i = 0; i < positions.size(); i++) {
                 for (int j = 0; j< positions.size(); j++) {
                     this.oGraph.addObstacleEdge(positions.elementAt(j), positions.elementAt(i));
                 }
             }
         }
 
         // add basic lines
         for(CObstacle obstacle : this.aoObstacles) {
             Vector<CPosition> wayPoints = obstacle.getWaypoints();
             for(int i = 0; i < wayPoints.size(); i++) {
                 for (int j = 0; j< wayPoints.size(); j++) {
                     this.oGraph.addWayPointEdge(wayPoints.elementAt(j), wayPoints.elementAt(i));
                     //this.oGraph.addWayPointEdge(wayPoints.elementAt(i), wayPoints.elementAt(j));
                 }
             }
         }
 
         // connect obstacles with others
         for(CObstacle obstacle : this.aoObstacles) {
             Vector<CPosition> wayPoints = obstacle.getWaypoints();
             for(CPosition position : wayPoints) {
                 for(CObstacle innerObstacle : this.aoObstacles) {
                     if(obstacle.compareTo(innerObstacle) != 0) { // if it is not the same obstacle
 
                          for(CPosition innerPosition : innerObstacle.getWaypoints()) {
                             this.oGraph.addWayPointEdge(position, innerPosition);
                          }
                     }
                 }
             }
         }
 
         for(CWalker walker : this.walkers) {
             this.oGraph.addWayPointEdge(walker.getPosition(), walker.getTarget());
 
             for(CObstacle obstacle : this.aoObstacles) {
                 for(CPosition position : obstacle.getWaypoints()) {
                     this.oGraph.addWayPointEdge(walker.getPosition(), position);
                     this.oGraph.addWayPointEdge(position, walker.getTarget());
                 }
             }
 
             CDijkstra dijkstra = new CDijkstra(this.oGraph);
 
             CPosition position = this.oGraph.getVertexByPosition(walker.getPosition());
             CPosition target = this.oGraph.getVertexByPosition(walker.getTarget());
             walker.setDesiredPath(dijkstra.getShortestPath(position, target));
         }
 
 
     }
 
     /**
      * calculates the next simulation step
      */
     public void stepSimulation() {
         boolean hasBlockedWalkers = true;
         Integer calculationRoundCount = 0;
         // step 1: calculate the desired new position of each walker
         while(hasBlockedWalkers) {
             hasBlockedWalkers = false;
             calculationRoundCount += 1;
 
             if(calculationRoundCount > maxCalculationCount) {
                 throw new IllegalArgumentException("Mischt, could not resolve blocked situation in CWorld.stepSimulation in " + maxCalculationCount.toString() + " calculation rounds");
             }
 
             for( CWalker walker : this.walkers) {
                 grid.unsubscribeWalker(walker, true);
                 walker.calcNextDesiredPosition(calculationRoundCount);
                 grid.subscribeWalker(walker, true);
             }
 
             // reset all detected collsions
             for( CWalker walker : this.walkers) {
                 walker.resetCollisions();
             }
 
             // check all walkers with neighbours for collisions
             for( CWalker walker : this.walkers) {
                 for( CWalker neighbourWalker : grid.getNeighbours(walker, true)) {
                     hasBlockedWalkers |= walker.checkCollisionWith(neighbourWalker, true); // if one collision return true, the the while loop must run once again
                 }

                for( CObstacle neighbourObstacle : grid.getNearObstacles(walker, true)) {
                    hasBlockedWalkers |= walker.checkCollisionWith(neighbourObstacle, true);
                }
             }
 
             if( hasBlockedWalkers ) {
                 Collections.shuffle(this.walkers);
             }
         }
 
         // step 2: make the step for each walker and remove walkers that reached their target
         // we have to use iterators because we modify the walker list while we iterate over it
         Iterator<CWalker> iter = this.walkers.iterator();
         while(iter.hasNext()){
             CWalker walker = iter.next();
             if( walker.walkToNextDesiredPosition() ) {
                 // Walker has reached target, remove the guy
                 grid.unsubscribeWalker(walker, false);
                 iter.remove();
             }
         }
     }
 
 
 }

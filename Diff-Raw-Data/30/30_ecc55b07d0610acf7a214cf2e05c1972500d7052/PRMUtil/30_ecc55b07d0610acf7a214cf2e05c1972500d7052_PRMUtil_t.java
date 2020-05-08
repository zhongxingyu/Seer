 package util;
 
 import ex3.Edge;
 import ex3.PRMGraph;
 import ex3.Vertex;
 import ex4.Printer;
 import geometry_msgs.Point;
 import geometry_msgs.Pose;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.PriorityQueue;
 import java.util.Queue;
 import java.util.Random;
 import launcher.RunParams;
 import nav_msgs.OccupancyGrid;
 import org.jboss.netty.buffer.ChannelBuffer;
 import org.jboss.netty.buffer.ChannelBuffers;
 import org.ros.message.MessageFactory;
 import org.ros.node.topic.Publisher;
 
 public class PRMUtil {
 
     public static final int INFLATION_RADIUS = RunParams.getInt("INFLATION_RADIUS");
     public static final String CONNECTION_METHOD = RunParams.get("CONNECTION_METHOD");
     public static final double NEIGHBOURHOOD_RADIUS = RunParams.getDouble("NEIGHBOURHOOD_RADIUS");
 
     Random randGen;
     MessageFactory factory;
     OccupancyGrid inflatedMap;
 
     public PRMUtil(Random randGen, MessageFactory factory, OccupancyGrid inflatedMap){
         this.randGen = randGen;
         this.factory = factory;
         this.inflatedMap = inflatedMap;
     }
 
     public void setInflatedMap(OccupancyGrid map) {
         inflatedMap = map;
     }
 
     public MessageFactory getFactory(){
         return factory;
     }
 
     /* Gets the euclidean distance between two points */
     public static double getEuclideanDistance(Vertex v1, Vertex v2){
         return getEuclideanDistance(v1.getLocation(), v2.getLocation());
     }
 
     public static double getEuclideanDistance(Point p1, Point p2){
         return getEuclideanDistance(p1.getX(),p1.getY(),p2.getX(),p2.getY());
     }
 
     public static double getEuclideanDistance(double x1, double y1, double x2, double y2){
         return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
     }
 
     /* Calculates the weight of an edge. */
     public static double getEdgeWeight(Edge e){
         return getEuclideanDistance(e.getVertexA(), e.getVertexB());
     }
 
     /* Generates a set of random vertices which are guaranteed to be in free space
      * on the map. This version of the method does not take into account the size
      * or orientation of the robot. */
     public ArrayList<Vertex> randomSample(OccupancyGrid map, int vertexNum){
         final ChannelBuffer buff = map.getData();
 
         ArrayList<Vertex> randomVertices = new ArrayList<Vertex>();
 
         final int mapHeight = map.getInfo().getHeight();
         final int mapWidth = map.getInfo().getWidth();
         final float mapRes = map.getInfo().getResolution();
 
         for (int i = 0; i < vertexNum; i++) {
             randomVertices.add(getRandomVertex(mapWidth, mapHeight, mapRes, buff));
         }
 
         return randomVertices;
     }
 
     /** Generates random points on the map until one that is valid is found. */
     public Vertex getRandomVertex(int mapWidth, int mapHeight, float mapRes, ChannelBuffer buff){
         boolean foundOpen = false;
         float randX = 0;
         float randY = 0;
         final int buffLength = buff.capacity();
 
         while (!foundOpen){
             randX = (randGen.nextFloat() * mapWidth);
             randY = (randGen.nextFloat() * mapHeight);
 
             foundOpen = isPositionValid((int) Math.round(randX), (int) Math.round(randY), mapWidth, mapHeight, buff, buffLength);
 
         }
 
         return new Vertex(randX * mapRes, randY * mapRes, factory);
     }
 
     /** Generate a random vertex in free space within the bounds given */
     public Vertex getRandomVertex(int startX, int startY, int cellWidth, OccupancyGrid map){
         int mapWidth = map.getInfo().getWidth();
         int mapHeight = map.getInfo().getHeight();
         float mapRes = map.getInfo().getResolution();
 
         boolean foundOpen = false;
         float randX = 0;
         float randY = 0;
         ChannelBuffer buff = map.getData();
         final int buffLength = buff.capacity();
 
         while (!foundOpen){
             randX = (randGen.nextFloat() * cellWidth) + startX;
             randY = (randGen.nextFloat() * cellWidth) + startY;
 
             foundOpen = isPositionValid((int) Math.round(randX), (int) Math.round(randY), mapWidth, mapHeight, buff, buffLength);
 
         }
 
         return new Vertex(randX * mapRes, randY * mapRes, factory);
     }
 
     /** Samples vertices for the road map based on a grid. xstep and ystep specify
      * the distribution of particles in the grid. The first particle generated
      * will be at 0,0, and subsequent particles will be at n*xstep, n*ystep, where
      * xstep and ystep are specified in metres. */
     public ArrayList<Vertex> gridSample(OccupancyGrid map, double xStep, double yStep){
         ArrayList<Vertex> vertices = new ArrayList<Vertex>();
 
         // Axis limits
         int mapWidth = map.getInfo().getWidth();
         int mapHeight = map.getInfo().getHeight();
         float mapRes = map.getInfo().getResolution();
 
         // Convert step sizes to sizes that are easy to work with in map space.
         double mapXStep = xStep / map.getInfo().getResolution();
         double mapYStep = yStep / map.getInfo().getResolution();
 
         double curY = 0;
         double curX = 0;
 
         for (int i = 0; curX < mapWidth; i++) {
             curY = 0;
             for (int j = 0; curY < mapHeight; j++) {
                 boolean valid = isPositionValid((int) Math.round(curX),
                         (int) Math.round(curY),
                         mapWidth,
                         mapHeight,
                         map.getData(),
                         map.getData().capacity());
                 if (valid){
                     vertices.add(new Vertex((float) curX * mapRes, (float) curY * mapRes, factory));
                 }
                 curY += mapYStep;
             }
             curX += mapXStep;
         }
 
         return vertices;
     }
 
     /**
      * Samples points for the road map based on a cell sampling strategy. The map
      * is divided into cells. Each cell will have the same number of particles
      * randomly placed within it, so long as such generation is possible. The
      * cell width given in metres specifies the size of the square cell in which
      * vertices will be generated.
      */
     public ArrayList<Vertex> cellSample(OccupancyGrid map, double cellWidth, int targetPerCell){
         ArrayList<Vertex> vertices = new ArrayList<Vertex>();
 
         final int mapWidth = map.getInfo().getWidth();
         final int mapHeight = map.getInfo().getHeight();
         final float mapRes = map.getInfo().getResolution();
         final ChannelBuffer buf = map.getData();
         final int bufLength = map.getData().capacity();
 
         int cellWidthMap = (int) Math.round(cellWidth / mapRes);
         System.out.println("Cell width metres: " + cellWidth + ", cell width map space: " + cellWidthMap);
 
         for (int i = 0; i < mapWidth; i += cellWidthMap) {
             for (int j = 0; j < mapHeight; j += cellWidthMap) {
                 int freePixelsInCell = freePointsInCell(i, j, cellWidthMap, mapWidth, mapHeight, buf, bufLength);
                 if (freePixelsInCell > targetPerCell) {
                     for (int points = 0; points < targetPerCell; points++) {
                         Vertex v = getRandomVertex(i, j, cellWidthMap, map);
                         if (! vertices.contains(v)) {
                             vertices.add(v);
                         }
                     }
                 }
             }
         }
 
         return vertices;
     }
 
     /*
      * Checks the number of free points in a given cell, where the start point
      * is the top left corner of the cell, and the cellwidth is the width of the
      * cell in the map space.
      */
     public static int freePointsInCell(final int startX, final int startY, final int cellWidth, final int mapWidth, final int mapHeight, ChannelBuffer buff, int buffLength) {
         int freeCount = 0;
 
         for (int i = startX; i < startX + cellWidth; i++) {
             if (i > mapWidth || i < 0){
                 break;
             }
             for (int j = startY; j < startY + cellWidth; j++) {
                 if (j > mapHeight || j < 0) {
                     break;
                 }
                 if (isPositionValid(i, j, mapWidth, mapHeight, buff, buffLength)){
                     freeCount++;
                 }
             }
         }
 
         return freeCount;
     }
 
     /*
      * Checks whether the given pose is in free space on the given map.
      */
     public static boolean isPositionValid(Pose p, OccupancyGrid map){
         int mapWidth = map.getInfo().getWidth();
         int mapHeight = map.getInfo().getHeight();
         float mapRes = map.getInfo().getResolution();
         return isPositionValid((int) Math.round(p.getPosition().getX() / mapRes),
                 (int) Math.round(p.getPosition().getY() / mapRes),
                 mapWidth,
                 mapHeight,
                 map.getData(),
                 map.getData().capacity());
     }
 
     /*
      * Checks whether the position specified by the given x and y coordinates is
      * in free space on the given map.
      */
     public static boolean isPositionValid(int x, int y, int mapWidth, int mapHeight, ChannelBuffer buff, int buffLength){
         // get the index in the array for this random point
         int index = GeneralUtil.getMapIndex(x, y, mapWidth, mapHeight);
         if (index > 0 && index < buffLength) {
             Byte cell = buff.getByte(index);
             if (cell.byteValue() == 0) {
                 // We are inside the map bounds and the cell is not occupied.
                 return true;
             } else {
                 return false;
             }
         } else {
             return false;
         }
     }
 
     /*
      * Connects all vertices to all other vertices within a euclidean distance of
      * distanceThreshold. Also creates representative edges.
      */
     public ArrayList<Edge> connectVertices(ArrayList<Vertex> vertices, double distanceThreshold, int maxConnections){
         ArrayList<Edge> edges = new ArrayList<Edge>();
         ArrayList<Edge> temp = new ArrayList<Edge>();
 
         synchronized (vertices) {
             for (Vertex vertex : vertices) {
                 temp = connectVertexToGraph(vertex, vertices, distanceThreshold, maxConnections);
                 for (Edge edge : temp) {
                     if (!edges.contains(edge)) {
                         edges.add(edge);
                     }
                 }
             }
         }
 
         return edges;
     }
 
     
     /* Connect a vertex to other vertices in the graph. */
     public ArrayList<Edge> connectVertexToGraph(Vertex v, ArrayList<Vertex> vertices, double neighbourhoodDistanceThreshold, int maxConnections){
         if ("nearestN".equalsIgnoreCase(CONNECTION_METHOD)) {
             return connectVertex_nearestN(v, vertices, maxConnections, maxConnections * 2);
         } else if ("threshold".equalsIgnoreCase(CONNECTION_METHOD)) {
             return connectVertex_firstNInThreshold(v, vertices, neighbourhoodDistanceThreshold, maxConnections);
         } else if ("neighbourhood".equalsIgnoreCase(CONNECTION_METHOD)) {
             return connectVertex_neighbourhoods(v, vertices, maxConnections, maxConnections * 2, NEIGHBOURHOOD_RADIUS);
         } else {
             throw new IllegalStateException("Illegal connection method specified");
         }
     }
 
     /* Connects vertices in a neighbourhood pattern. The idea is to connect
      * nodes in clusters (or neighbourhoods) where there are no loops in
      * connections within the neighbourhood. In other words, if, for node A,
      * another node B, is in the neighbourhood, we only connect if there is no
      * direct or indirect path from A to B where all nodes within the path
      * are in the neighbourhood.
      * For nodes outside of the neighbourhood, we connect only if there is no
      * other direct or indirect connection. */
     public ArrayList<Edge> connectVertex_neighbourhoods(Vertex v, ArrayList<Vertex> graph, 
             int maxConnections, int maxAttempts, double neighbourhoodRadius) {
         PriorityQueue<VertexTuple> closestNodes = new PriorityQueue<VertexTuple>(graph.size());
 
         for (Vertex vert : graph) {
             if (vert == v || vert.getConnectedVertices().size() >= maxConnections){
                 continue;
             }
             closestNodes.add(new VertexTuple(vert, getEuclideanDistance(v, vert)));
         }
 
         ArrayList<Edge> connections = new ArrayList<Edge>();
 
         int connectionAttempts = 0;
         int connectionCount = v.getConnectedVertices().size();
         while (connectionAttempts < maxAttempts && connectionCount < maxConnections) {
             VertexTuple vt = closestNodes.poll();
             // Run out of nodes to check so exit.
             if (vt == null){
                 break;
             }
             Vertex vert = vt.v1;
 
             if (vert.getConnectedVertices().contains(v)){
                 // If we've already been connected to this node, carry on.
                 continue;
             }
 
             if (getEuclideanDistance(v, vert) <= neighbourhoodRadius) {
                 // Only connect if not already connected indirectly by a path
                 // entirely contained within the neighbourhood
                 if (! isConnectedWithinNeighbourhood(v, vert, NEIGHBOURHOOD_RADIUS)) {
                     // Connect if no obstacles
                     if (connectedInFreeSpace(inflatedMap,
                             v.getLocation().getX(),
                             v.getLocation().getY(),
                             vert.getLocation().getX(),
                             vert.getLocation().getY(), vt.distance)) {
                         v.addConnectedVertex(vert);
                         vert.addConnectedVertex(v);
                         connections.add(new Edge(v, vert, vt.distance));
                         connectionCount++;
                     }
                 }
             } else {
                 if (!isConnected(v, vert)) {
                     if (connectedInFreeSpace(inflatedMap,
                             v.getLocation().getX(),
                             v.getLocation().getY(),
                             vert.getLocation().getX(),
                             vert.getLocation().getY(), vt.distance)) {
                         v.addConnectedVertex(vert);
                         vert.addConnectedVertex(v);
                         connections.add(new Edge(v, vert, vt.distance));
                         connectionCount++;
                     }
                 }
             }
             
             connectionAttempts++;
         }
 
         return connections;
     }
 
     /*
      * Connects a vertex to other vertices within distanceThreshold euclidean distance
      * of the specified vertex. Will not make more than maxConnections connections. The
      * vertex will not be connected to nodes that it is already connected to indirectly.
      * The node will be connected to nodes which are within the given threshold
      * based on their position in the array. The nodes with lower array indexes
      * are much more likely to be connected to.
      */
     public ArrayList<Edge> connectVertex_firstNInThreshold(Vertex v, ArrayList<Vertex> graph, double neighbourhoodSize, int maxConnections) {
 	int connectedCount = 0;
         double distance = 0;
         ArrayList<Edge> connections = new ArrayList<Edge>();
         for (Vertex vert : graph) {
             if (vert.getConnectedVertices().size() >= maxConnections
                     || vert.getConnectedVertices().contains(v)){
                 // If we've already been connected to this node, carry on.
                 continue;
             }
             distance = getEuclideanDistance(v, vert);
             if (distance <= neighbourhoodSize && !isConnected(v, vert) && v != vert) {
                 if (connectedInFreeSpace(inflatedMap,
                         v.getLocation().getX(),
                         v.getLocation().getY(),
                         vert.getLocation().getX(),
                         vert.getLocation().getY(), distance)) {
 
                     v.addConnectedVertex(vert);
                     vert.addConnectedVertex(v);
                     connections.add(new Edge(v, vert, distance));
                     connectedCount++;
                 }
             }
 	    if (connectedCount == maxConnections){
 		break;
             }
         }
 
         return connections;
     }
 
 
     /*
      * Used to order vertices in a priority queue.
      */
     private class VertexTuple implements Comparable<VertexTuple> {
         public final Vertex v1;
         public final double distance;
 
         public VertexTuple(Vertex v1, double distance){
             this.v1 = v1;
             this.distance = distance;
         }
 
         /*
          * Compares two distances. Smaller distances are considered to be higher
          * priority than larger ones. If the distance in this object is smaller
          * than the one passed in, the value returned is +1.
          */
         @Override
         public int compareTo(VertexTuple o) {
             return Double.compare(distance, o.distance);
         }
     }
 
     /*
      * Attempts to connect the given node to the closest n nodes to it, starting
      * from the node that is closest, by checking distance to all vertices and
      * then attempting to make connections to the closest n of those.
      */
     public ArrayList<Edge> connectVertex_nearestN(Vertex v, ArrayList<Vertex> graph, int maxConnections, int maxAttempts) {
         PriorityQueue<VertexTuple> closestNodes = new PriorityQueue<VertexTuple>(graph.size());
 
         for (Vertex vert : graph) {
             if (vert == v || vert.getConnectedVertices().size() >= maxConnections){
                 continue;
             }
             closestNodes.add(new VertexTuple(vert, getEuclideanDistance(v, vert)));
         }
 
         ArrayList<Edge> connections = new ArrayList<Edge>();
 
         int connectionAttempts = 0;
         int connectionCount = v.getConnectedVertices().size();
         while (connectionAttempts < maxAttempts && connectionCount < maxConnections) {
             VertexTuple vt = closestNodes.poll();
             // Run out of nodes to check so exit.
             if (vt == null){
                 break;
             }
             Vertex vert = vt.v1;
 
             if (vert.getConnectedVertices().contains(v)){
                 // If we've already been connected to this node, carry on.
                 continue;
             }
             
             if (!isConnected(v, vert)) {
                 if (connectedInFreeSpace(inflatedMap,
                         v.getLocation().getX(),
                         v.getLocation().getY(),
                         vert.getLocation().getX(),
                         vert.getLocation().getY(), vt.distance)) {
 
                     v.addConnectedVertex(vert);
                     vert.addConnectedVertex(v);
                     connections.add(new Edge(v, vert, vt.distance));
                     connectionCount++;
                 }
             }
             connectionAttempts++;
         }
         
         return connections;
     }
 
     /* Takes a list of vertices (a path) and removes intermediate points which
      * do not help us go around an obstacle. That is, if a is connected to b,
      * and b is connected to c, we check if there is a path from a to c and
      * if so, remove b from the path. We do this for the whole path until we
      * have as straight a path as possible */
     public ArrayList<Vertex> flattenDrunkenPath(ArrayList<Vertex> path, OccupancyGrid mapToUse) {
         ArrayList<Vertex> flatPath = (ArrayList) path.clone();
         boolean pathModified = true;
         while (pathModified) {
             pathModified = false;
             for (int i = 0; i < flatPath.size() - 2; i++) {
                 // Check if current node can connect to the node after next
                 Vertex a = flatPath.get(i);
                 Vertex c = flatPath.get(i + 2);
                 if (connectedInFreeSpace(mapToUse, a, c)) {
                     flatPath.remove(i + 1); // Remove b
                     pathModified = true;
                 }
             }
         }
         return flatPath;
     }
 
     /* Takes a list of vertices (a path) and removes intermediate points which
      * do not help us go around an obstacle. That is, if a is connected to b,
      * and b is connected to c, we check if there is a path from a to c and
      * if so, remove b from the path. This version of the method will iterate
      * over the path the specified number of times. If -1 is passed, the iteration
      * will continue until the path cannot be flattened any more.
      */
     public ArrayList<Vertex> flattenDrunkenPath(ArrayList<Vertex> path, int iterations, OccupancyGrid mapToUse) {
         if (iterations == -1){
             return flattenDrunkenPath(path, mapToUse);
         }
         ArrayList<Vertex> flatPath = (ArrayList) path.clone();
         for (int j = 0; j < iterations; j++) {
             for (int i = 0; i < flatPath.size() - 2; i++) {
                 // Check if current node can connect to the node after next
                 Vertex a = flatPath.get(i);
                 Vertex c = flatPath.get(i + 2);
                 if (connectedInFreeSpace(mapToUse, a, c)) {
                     flatPath.remove(i + 1); // Remove b
                 }
             }
         }
 
         return flatPath;
     }
 
     /* Checks if v1 is connected v2, either directly or indirectly. */
     public boolean isConnected(Vertex v1, Vertex v2){
 
         // If the node has no connections, then just return.
         if (v1.getConnectedVertices().isEmpty()){
             return false;
         }
 
 	Queue<Vertex> toDo = new LinkedList<Vertex>();
 	HashSet<Vertex> done = new HashSet<Vertex>();
 
 	toDo.add(v1);
 
 	while(! toDo.isEmpty()){
 	    Vertex check = toDo.remove();
 	    done.add(check);
 	    for (Vertex connV : check.getConnectedVertices()){
 		if (connV.equals(v2)){
 		    return true;
 		} else if (!done.contains(connV) && !toDo.contains(connV)){
 		    toDo.add(connV);
 		}
 	    }
 	}
         return false;
     }
 
     /* Checks if v1 is connected to v2 only via nodes which are within the
      * neighbourhood of v1, returns false otherwise. Neighbourhood is defined
      * by the provided radius value */
     public boolean isConnectedWithinNeighbourhood(Vertex v1, Vertex v2, double radius){
         // If the node has no connections, then just return.
         if (v1.getConnectedVertices().isEmpty()
                 || getEuclideanDistance(v1, v2) > radius){
             return false;
         }
 
 	Queue<Vertex> toDo = new LinkedList<Vertex>();
 	HashSet<Vertex> done = new HashSet<Vertex>();
 
 	toDo.add(v1);
 
 	while(! toDo.isEmpty()){
 	    Vertex check = toDo.remove();
 	    done.add(check);
 	    for (Vertex connV : check.getConnectedVertices()){
 		if (connV.equals(v2)){
 		    return true;
 		} else if (!done.contains(connV) && !toDo.contains(connV)
                         && getEuclideanDistance(v1, connV) <= radius){
 		    toDo.add(connV);
 		}
 	    }
 	}
         return false;
     }
 
 
     /*
      * Calculates the average connection length in the graph.
      */
     public static double averageConnectionLength(PRMGraph graph){
         double sum = 0;
 
         ArrayList<Edge> edges = graph.getEdges();
         synchronized(edges) {
             for (Edge e : edges) {
                 //   System.out.println(e);
                 sum += e.edgeWeight();
             }
         }
 
         return sum/graph.getEdges().size();
     }
 
     public static double getPathLength(ArrayList<Vertex> path) {
         double totalLength = 0.0;
         for (int i = 0; i < path.size() - 1; i++) {
             totalLength += getEuclideanDistance(path.get(i), path.get(i+1));
         }
         return totalLength;
     }
 
     /*
      * Inflates the map so that obstacles are increased in size to the extent that
      * if the robot stays in the remaining free space, it will not collide with obstacles.
      * The actual obstacles are replaced with unknown space, and a certain area in the proxi-
      * mity of the obstacle is converted to obstacle space.
      */
     public static OccupancyGrid inflateMap(OccupancyGrid grid, Publisher<OccupancyGrid> pub) {
         /*
          * Copy data in the grid to a new channel buffer. We use this to check
          * what data was in the original buffer at each point.
          */
         ChannelBuffer original = ChannelBuffers.copiedBuffer(grid.getData());
 
         // Get an occupancy grid for us to put modified data into.
         OccupancyGrid inflatedMap = pub.newMessage();
         // Set the height, width and resolution to the same as that of the original.
         inflatedMap.setInfo(grid.getInfo());
         // Copy the data in the original buffer into the newly created grid.
         inflatedMap.setData(ChannelBuffers.copiedBuffer(grid.getData()));
 
         for (int i = 0; i < original.capacity(); i++) { 
             // If data in the map indicates an obstacle, widen the obstacle by some amount
             if (original.getByte(i) == 100 || original.getByte(i) == -1) {
                 for (int yOffset = -INFLATION_RADIUS; yOffset <= INFLATION_RADIUS; yOffset++) {
                     int xOffset = grid.getInfo().getWidth() * yOffset;
                     //xRadius = RADIUS -
                     
                     for (int j = i + xOffset - INFLATION_RADIUS; j <= i + xOffset + INFLATION_RADIUS; j++) {
                         // If there is an obstacle very close to the zeroth index, avoid
                         // exceptions
                         if (j < 0) {
                             j = 0;
                         }
                         // Also avoid going over capacity
                         if (j >= original.capacity()) {
                             break;
                         }
                         // No point widening obstacles into unknown space or something
                         // which is already an obstacle
                         if (original.getByte(j) == -1 || original.getByte(j) == 100) {
                             continue;
                         }
                         // Set the byte to an obstacle in the inflated map
                         inflatedMap.getData().setByte(j, 100);
                     }
                 }
                 // Set the original obstacle position to unknown space just to
                 // keep track of where it was before.
                 inflatedMap.getData().setByte(i, -1);
             }
         }
 
         return inflatedMap;
     }
 
     public static boolean connectedInFreeSpace(OccupancyGrid map,
             Vertex a, Vertex b) {
         return connectedInFreeSpace(map,
                 a.getLocation().getX(),
                 a.getLocation().getY(),
                 b.getLocation().getX(),
                 b.getLocation().getY(),
                 getEuclideanDistance(a, b));
     }
 
     /** Given a line (provided by two points), checks whether or not the line
      * intersects with obstacles on the map, returning true only if there is
      * a fully clear path between the two points (i.e. the line is in free space). */
     public static boolean connectedInFreeSpace(OccupancyGrid map, 
             double x1, double y1, double x2, double y2, double distance) {
         ChannelBuffer data = map.getData();
         int width = map.getInfo().getWidth();
         int height = map.getInfo().getHeight();
         float mapRes = map.getInfo().getResolution();
 
         x1 = x1 / mapRes;
         x2 = x2 / mapRes;
         y1 = y1 / mapRes;
         y2 = y2 / mapRes;
         distance = distance / mapRes;
 
         double yDiff = y2 - y1;
         double xDiff = x2 - x1;
         double xGradient = Math.abs(xDiff / distance);
         double yGradient = yDiff / distance;
 
         double x;
         double y;
         double bigX;
         if (x1 < x2) {
             x = x1;
             y = y1;
             bigX = x2;
         } else {
             x = x2;
             y = y2;
             bigX = x1;
             yGradient = -yGradient; // Invert gradient
         }
 
         boolean connectedFreely = true; // False when found obstacle
 
         //System.out.println("x1: " + x1 + " y1: " + y1 + " x2: " + x2 + " y2: " + y2);
         //System.out.println("xDiff: "+xDiff+" yDiff: "+yDiff+" distance: "+distance);
         //System.out.println("xGradient: "+xGradient+" yGradient: "+yGradient);
         while (x <= bigX && connectedFreely) {
             int index = GeneralUtil.getMapIndex((int)Math.round(x), (int)Math.round(y), (int) width, (int) height);
 
             if (index > 0 && index < data.capacity()) {
                 //System.out.println("Index: "+index+" x: "+x+" y: "+y);
 
                 // If we are on the map to begin with...
                 Byte cellData = data.getByte(index);
 
                 if (cellData.byteValue() < 0 || cellData.byteValue() > 65) {
                     // If we're on the map, but the map has no data, or there is an obstacle...
                     connectedFreely = false;
                     break;
                 }
             } else {
                 System.out.println("ERROR! This shouldn't happen. ConnectedInFreeSpace "
                         + "called with out of bounds values. Do not ignore this MICHAL");
                 System.out.println("Index: "+index+" x: "+x+" y: "+y);
                 System.out.println("x1: "+x1+" y1: "+y1+" x2: "+x2+" y2: "+y2);
                 connectedFreely = false;
                 break;
             }
 
             x += xGradient;
             y += yGradient;
         }
 
         return connectedFreely;
     }
 
     /** This method checks all nodes and vertices and removes any that go
      * through non-free space. Note that this is DESTRUCTIVE to the given graph. */
     public static void _checkAndPruneGraph(PRMGraph graph, OccupancyGrid map) {
         ChannelBuffer data = map.getData();
         int width = map.getInfo().getWidth();
         int height = map.getInfo().getHeight();
         float mapRes = map.getInfo().getResolution();
 
         ArrayList<Edge> edges = graph.getEdges();
         ArrayList<Vertex> vertices = graph.getVertices();
 
         long start = System.currentTimeMillis();
         synchronized(vertices) {
             Iterator<Vertex> vertexIt = vertices.iterator();
 
             // Remove invalid vertices and the edges that contain them
             while (vertexIt.hasNext()) {
                 Vertex vertex = vertexIt.next();
                 int scaledX = (int) Math.round(vertex.getLocation().getX() / mapRes);
                 int scaledY = (int) Math.round(vertex.getLocation().getY() / mapRes);
 
                 if (!PRMUtil.isPositionValid(scaledX, scaledY,
                         width, height, data, data.capacity())) {
                     Printer.println("Removed vertex from all its neighbours because it's in an invalid pos", "REDF");
                     // Remove this vertex from all neighbours
                     Iterator<Vertex> neighbours = vertex.getConnectedVertices().iterator();
                     while (neighbours.hasNext()) {
                         neighbours.next().getConnectedVertices().remove(vertex);
                     }
 
                     // Remove all edges containing naughty vertex
                     Iterator<Edge> edgeIt = edges.iterator();
                     while (edgeIt.hasNext()) {
                         Edge edge = edgeIt.next();
                         if (edge.getVertexA().equals(vertex)
                                 || edge.getVertexB().equals(vertex)) {
                             edgeIt.remove();
                         }
                     }
 
                     // Finally, remove the offending vertex
                     vertexIt.remove();
                 }
             }
 
             synchronized(edges) {
                 // Remove edges which intersect with non-free space
                 Iterator<Edge> edgeIt = edges.iterator();
                 while (edgeIt.hasNext()) {
                     Edge edge = edgeIt.next();
                     if (!PRMUtil.connectedInFreeSpace(map, edge.getVertexA(), edge.getVertexB())) {
                         // Remove vertices from each other's neighbour list
                         edge.getVertexA().destroyConnection(edge.getVertexB());
                         edgeIt.remove();
                     }
                 }
                 graph.setEdges(edges);
                 graph.setVertices(vertices);
             }
         }
         System.out.println("Synchronised block took " + (System.currentTimeMillis()-start)+"ms");
     }
 
     /*
      * fov_angle is the kinect vision fov in degrees, heading is the robot heading
      * in radians. range_min is the minimum range at which things can be detected
      * range_max is the maximum detection range. The projected fov will be drawn
      * onto the map provided. An arraylist of integers is returned which contains
      * the indices that each ray traced went through.
      */
     public static ArrayList<ArrayList<Integer>> projectFOV(double ox, double oy, double bearing, int fov_angle,
             double range_min, double range_max, double angle_step, OccupancyGrid map, OccupancyGrid mapToModify) {
         ChannelBuffer data = map.getData();
         ChannelBuffer modData = mapToModify.getData();
         ArrayList<ArrayList<Integer>> fovRays = new ArrayList<ArrayList<Integer>>();
         // As the ROS angle system has 0 deg = east and increases anti-clockwise
         // but the Quaternion trigonometry and particle raytracing assume 0 deg = north / clockwise,
         // we must first convert to 0 deg = north / clockwise by subtracting PI/2
 //        bearing -= Math.PI / 2;
         bearing *= -1;
 
         double fovRad = Math.toRadians(fov_angle);
 
         double firstRayAngle = GeneralUtil.normaliseAngle(bearing - fovRad / 2);
         double lastRayAngle = GeneralUtil.normaliseAngle(bearing + fovRad / 2);
         double currentRayAngle = firstRayAngle;
         double rayAngleIncrement = Math.toRadians(angle_step) * GeneralUtil.angleDirection(firstRayAngle, lastRayAngle);
 
//        System.out.println("Start: " + Math.toDegrees(firstRayAngle) + " End: " + Math.toDegrees(lastRayAngle));
         // Map data
         long map_width = map.getInfo().getWidth();
         long map_height = map.getInfo().getHeight();
         float map_resolution = map.getInfo().getResolution(); // in m per pixel
 
         for (int i = 0; i < fov_angle / angle_step; i++) {
             ArrayList<Integer> ray = new ArrayList<Integer>();
             // Find gradient of the line of sight in x,y plane, assuming 0 deg = north
             double grad_x = Math.sin(currentRayAngle);
             double grad_y = Math.cos(currentRayAngle);
 //            double grad_x = Math.sin(Math.toRadians(i));
 //            double grad_y = Math.cos(Math.toRadians(i));
 
             // Particle position
             double x_orig = ox / map_resolution;
             double y_orig = oy / map_resolution;
             // Max range position relative to the current position
             double x_max_offset = range_max * grad_x / map_resolution;
             double y_max_offset = range_max * grad_y / map_resolution;
 
            // This should really be dynamic - based on range_min
            double x = x_orig + grad_x * 10;
            double y = y_orig + grad_y * 10;
             boolean occupied = false; // Have we found an occupied cell yet?
 
             // Stop travelling away from the robot when we reach max range of
             // laser or an occupied cell
             while (Math.abs(x - x_orig) < Math.abs(x_max_offset)
                     && Math.abs(y - y_orig) < Math.abs(y_max_offset)
                     && !occupied) {
                 x += grad_x;
                 y += grad_y;
 
                 int index = GeneralUtil.getMapIndex((int) Math.round(x), (int) Math.round(y), (int) map_width, (int) map_height);
 
                 if (index > 0 && index < data.capacity()) {
                     // If we are on the map to begin with...
                     Byte cellData = data.getByte(index);
 
                     if (cellData.byteValue() < 0 || cellData.byteValue() > 65) {
                         // If we're on the map, but the map has no data, or there is an obstacle...
                         occupied = true;
                     } else {
                         occupied = false;
                         ray.add(index);
                         modData.setByte(index, 100);
                     }
                 } else {
                     occupied = true;
                 }
             }
             currentRayAngle = GeneralUtil.normaliseAngle(currentRayAngle + rayAngleIncrement);
             fovRays.add(ray);
         }
         return fovRays;
     }
 }

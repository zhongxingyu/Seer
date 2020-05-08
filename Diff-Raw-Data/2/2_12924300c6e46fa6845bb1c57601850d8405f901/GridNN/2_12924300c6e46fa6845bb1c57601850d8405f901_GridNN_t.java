 /**
  * $$\\ToureNPlaner\\$$
  */
 package de.tourenplaner.graphrep;
 
 import com.carrotsearch.hppc.IntArrayList;
 
 import java.util.logging.Logger;
 
 /**
  * @author Niklas Schnelle
  */
 public class GridNN implements NNSearcher {
     private static Logger log = Logger.getLogger("de.tourenplaner.graphrep");
     private static final long serialVersionUID = 1L;
     private static final int numberOfColumns = 1000;
     private int numRows;
     private int numCols;
 
     private final DumbNN fallback;
     private final GraphRep graphRep;
     private final IntArrayList[] grid;
     private final int latMin, latDiff;
     private final int lonMin, lonDiff;
 
 
     private int mapLat(int value) {
         return (int)((long)(value - latMin) * (long)(numRows - 1) / (long) latDiff);
     }
 
     private int mapLon(int value) {
         return (int) ((long) (value - lonMin) * (long) (numCols - 1) / (long) lonDiff);
     }
 
     private int coordsToIndex(int lat, int lon) {
         return mapLat(lat) * numCols + mapLon(lon);
     }
 
     private int toIndex(int row, int col) {
         return row * numCols + col;
     }
 
     public GridNN(GraphRep graph) {
         this.graphRep = graph;
         fallback = new DumbNN(graphRep);
 
         // Find biggest and smallest elements
         // needed for mapping values into the grid
         int latMin = Integer.MAX_VALUE;
         int lonMin = Integer.MAX_VALUE;
         int latMax = Integer.MIN_VALUE;
         int lonMax = Integer.MIN_VALUE;
 
         int numNodes = graphRep.getNodeCount();
         int curr;
 
         // Go lat's and lon's one after the other to access more
         // sequentially
         for (int i = 0; i < numNodes; i++) {
             curr = graphRep.getNodeLat(i);
             if (curr < latMin) {
                 latMin = curr;
             } else if (curr > latMax) {
                 latMax = curr;
             }
         }
         for (int i = 0; i < numNodes; i++) {
             curr = graphRep.getNodeLon(i);
             if (curr < lonMin) {
                 lonMin = curr;
             } else if (curr > lonMax) {
                 lonMax = curr;
             }
         }
 
         this.latMin = latMin;
         this.latDiff = latMax - latMin;
         this.lonMin = lonMin;
         this.lonDiff = lonMax - lonMin;
 
         // Calculate numRows and numCols to match the geometry of our map
         // only an optimization not needed for correctness
         this.numCols = numberOfColumns;
         this.numRows = (int)(((double)numCols)*((((double)latDiff))/((double)lonDiff)));
 
         log.info("Using Grid of "+numRows+" * "+numCols);
 
         grid = new IntArrayList[numRows * numCols];
 
         // Insert all nodes
         int lat, lon, index;
         IntArrayList list;
         for (int i = 0; i < numNodes; i++) {
             lat = graphRep.getNodeLat(i);
             lon = graphRep.getNodeLon(i);
             index = coordsToIndex(lat, lon);
             list = grid[index];
 
             if (list == null) {
                 list = new IntArrayList(10);
                 grid[index] = list;
             }
             list.add(i);
         }
 
         // Let's trim the Lists to their internal size
         for(int i = 0; i < grid.length; i++){
             list = grid[i];
             if (list != null)
                 list.trimToSize();
         }
     }
 
     /**
      * @param lat
      * @param lon
      * @return
      * @see de.tourenplaner.graphrep.NNSearcher
      */
     @Override
     public int getIDForCoordinates(int lat, int lon) {
         final int row = mapLat(lat);
         final int col = mapLon(lon);
         // Need to search the exact cell and all around it
         final int upper = (row - 1 > 0) ? row - 1 : 0;
         final int lower = (row + 2 < numRows) ? row + 2 : numRows;
         final int left = (col - 1 > 0) ? col - 1 : 0;
         final int right = (col + 2 < numCols) ? col + 2 : numCols;
 
         long minDist = Long.MAX_VALUE;
         long dist;
         int nodeId, minNodeId=0;
         IntArrayList list;
         for (int i = upper; i < lower; i++) {
             Inner: for (int j = left; j < right; j++) {
                 list = grid[toIndex(i,j)];
                 if (list == null)
                    continue Inner;
 
                 for (int index = 0; index < list.size(); index++){
                     nodeId = list.get(index);
                     dist = sqDistToCoords(nodeId, lat, lon);
                     if (dist < minDist){
                         minDist = dist;
                         minNodeId = nodeId;
                     }
                 }
             }
         }
         // If every list was null fallback
         if (minDist == Long.MAX_VALUE){
             log.fine("Fell back to dumbNN for "+lat+","+lon);
             minNodeId = fallback.getIDForCoordinates(lat, lon);
         }
         return minNodeId;
 
     }
 
     private final long sqDistToCoords(int nodeID, int lat, int lon) {
         return ((long) (graphRep.getNodeLat(nodeID) - lat)) * ((long) (graphRep.getNodeLat(nodeID) - lat)) + ((long) (graphRep.getNodeLon(nodeID) - lon)) * ((long) (graphRep.getNodeLon(nodeID) - lon));
     }
 
 }

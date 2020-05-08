 package Source;
 
 import Util.CMathFunctions;
 import Util.CPosition;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Vector;
 
 /**
  * Created with IntelliJ IDEA.
  * User: bohnp1
  * Date: 18.10.13
  * Time: 13:55
  */
 public class CGrid {
 
 
     final int gridSizeC = 20;
     final int worldWidth;
     final int worldHigh;
     private Map<Integer,Map<Integer, Vector<CWalker>>> walkerGrid = new HashMap<Integer, Map<Integer, Vector<CWalker>>>();
     private Map<Integer,Map<Integer, Vector<CObstacle>>> obstacleGrid = new HashMap<Integer, Map<Integer, Vector<CObstacle>>>();
 
     public CGrid (int worldWidth, int worldHigh)
     {
         this.worldWidth = worldWidth;
         this.worldHigh = worldHigh;
     }
     /**
      * Subscribe Walker Position in Grid
      */
     void subscribeWalker (CWalker walker, boolean NextDesiredPos)
     {
         CPosition pos = NextDesiredPos ? walker.getDesiredNextPosition() : walker.getPosition();
         Integer gridColumn = pos.getY().intValue() / gridSizeC;
         Integer gridRow = pos.getX().intValue() / gridSizeC;
 
         Map<Integer, Vector<CWalker>> gridRowMap;
         Vector<CWalker> walkerInCell;
         if (walkerGrid.containsKey(gridColumn))
         {
             gridRowMap = walkerGrid.get(gridColumn);
         }
         else
         {
             gridRowMap = new HashMap<Integer, Vector<CWalker>>();
             walkerGrid.put(gridColumn, gridRowMap);
         }
 
         if (gridRowMap.containsKey(gridRow))
         {
             walkerInCell = gridRowMap.get(gridRow);
         }
         else
         {
             walkerInCell = new Vector<CWalker>();
             gridRowMap.put(gridRow, walkerInCell);
         }
 
         if (!walkerInCell.contains(walker))
         {
             walkerInCell.add(walker);
         }
 
     }
 
     /**
      * unsubscribe Walker Position in Grid
      */
     public boolean unsubscribeWalker(CWalker walker, boolean NextDesiredPos)
     {
         CPosition pos = NextDesiredPos ? walker.getDesiredNextPosition() : walker.getPosition();
         Integer gridColumn = pos.getY().intValue() / gridSizeC;
         Integer gridRow = pos.getX().intValue() / gridSizeC;
 
         Map<Integer, Vector<CWalker>> gridRowMap;
         Vector<CWalker> walkerInCell;
         if (walkerGrid.containsKey(gridColumn))
         {
             gridRowMap = walkerGrid.get(gridColumn);
 
             if (gridRowMap.containsKey(gridRow))
             {
                 walkerInCell = gridRowMap.get(gridRow);
 
                 walkerInCell.remove(walker);
 
                 if (walkerInCell.isEmpty())
                 {
                     gridRowMap.remove(gridRow);
                 }
             }
             else
             {
                 return false;
             }
 
             if (gridRowMap.isEmpty())
             {
                 walkerGrid.remove(gridColumn);
             }
         }
         else
         {
             return false;
         }
         return true;
     }
 
     /**
      * Get Neighbours of a Walker
      * @param walker the walker
      * @return neighbours of the Walker
      */
     public Vector<CWalker> getNeighbours(CWalker walker, boolean NextDesiredPos)
     {
         Vector<CWalker> neighbours = new Vector<CWalker>();
 
         CPosition pos = NextDesiredPos ? walker.getDesiredNextPosition() : walker.getPosition();
         Integer gridColumn = pos.getY().intValue() / gridSizeC;
         Integer gridRow = pos.getX().intValue() / gridSizeC;
 
         Map<Integer, Vector<CWalker>> gridRowMap;
         for (int i = gridColumn - 1; i < gridColumn + 2; i++)
         {
             if (walkerGrid.containsKey(i))
             {
                 gridRowMap = walkerGrid.get(i);
 
                 for (int j = gridRow -1; j < gridRow + 2; j++)
                 {
                     if (gridRowMap.containsKey(j))
                     {
                         neighbours.addAll(gridRowMap.get(j));
                     }
                 }
             }
         }
 
         neighbours.remove(walker);
 
         return neighbours;
     }
 
     /**
      * Subscribe Obstacle Positions in Grid
      * @param obstacles to subscribe
      */
     void subscribeObstacle (Vector<CObstacle> obstacles)
     {
         // for all objects
         for (CObstacle obstacle : obstacles)
         {
 
            Vector <CPosition> positions= obstacle.getPositions();
 
            // for all edges of a object
            for (int i = 0; i < positions.size(); i++)
            {
                CPosition point1Start;
                CPosition point1End;
                if (i == 0)
                {
                    point1Start = positions.lastElement();
                    point1End = positions.firstElement();
                }
                else
                {
                    point1Start = positions.elementAt(i - 1);
                    point1End = positions.elementAt(i);
                }
 
                for (int x = 0; x < worldWidth; x += gridSizeC)
                {
                    //calc intersection points from edge with x-grid-lines
                    CPosition intersectPoint = CMathFunctions.calcIntersectionPoint(point1Start, point1End,
                            new CPosition(x, 0),
                            new CPosition(x, gridSizeC),
                            false, true);
                    if (intersectPoint != null)
                    {
                        int gridColumn = intersectPoint.getY().intValue() / gridSizeC;
                        int gridRow = x / gridSizeC;
 
                        // for cell left and right of intersection Point add obstacle to grid
                        for (int k = gridRow -1 ; k <= gridRow; k++)
                        {
                           addObstacleToGrid(obstacle, gridColumn, k);
                        }
 
                    }
                }
 
                for (int y = 0; y < worldHigh; y += gridSizeC)
                {
                    //calc intersection points from edge with x-grid-lines
                    CPosition intersectPoint = CMathFunctions.calcIntersectionPoint(point1Start, point1End,
                            new CPosition(0, y),
                            new CPosition(gridSizeC, y),
                            false, true);
 
                    if (intersectPoint != null)
                    {
                        int gridColumn = y / gridSizeC;
                        int gridRow = intersectPoint.getX().intValue() / gridSizeC;
 
                        // for cell left and right of intersection Point add obstacle to grid
                        for (int k = gridColumn -1 ; k <= gridColumn; k++)
                        {
                            addObstacleToGrid(obstacle, k, gridRow);
                        }
 
                    }
                }
            }
 
         }
     }
 
     /**
      * Ask if a Cell has a Walker or Obstacle
      * @param row to ask
      * @param col to ask
      * @return cell has Walker or Obstacle
      */
     public boolean hasCellObject (int row, int col)
     {
         boolean cellHasObject = false;
 
         if (walkerGrid.containsKey(col))
         {
             Map<Integer, Vector<CWalker>> gridRowMap = walkerGrid.get(col);
 
             if (gridRowMap.containsKey(row))
             {
                 cellHasObject = !gridRowMap.get(row).isEmpty();
             }
         }
 
         if (obstacleGrid.containsKey(col))
         {
             Map<Integer, Vector<CObstacle>> gridRowMap = obstacleGrid.get(col);
 
             if (gridRowMap.containsKey(row))
             {
                 cellHasObject |= !gridRowMap.get(row).isEmpty();
             }
         }
 
         return cellHasObject;
     }
 
     /**
      * Add a obstacle to the Grid
      * @param obstacle to add
      * @param gridColumn of the obstacle to add
      * @param gridRow of the obstacle to add
      */
     private void addObstacleToGrid (CObstacle obstacle, int gridColumn, int gridRow)
     {
 
         Map<Integer, Vector<CObstacle>> gridRowMap;
         Vector<CObstacle> obstacleInCell;
 
         //Create Vector of Rows for Column when not exist
         if (obstacleGrid.containsKey(gridColumn))
         {
             gridRowMap = obstacleGrid.get(gridColumn);
         }
         else
         {
             gridRowMap = new HashMap<Integer, Vector<CObstacle>>();
             obstacleGrid.put(gridColumn, gridRowMap);
         }
 
         //Create Vector of obstacles for Cell when not exist
         if (gridRowMap.containsKey(gridRow))
         {
             obstacleInCell = gridRowMap.get(gridRow);
         }
         else
         {
             obstacleInCell = new Vector<CObstacle>();
             gridRowMap.put(gridRow, obstacleInCell);
         }
 
         //add obstacle to gridcell
         if (!obstacleInCell.contains(obstacle))
         {
             obstacleInCell.add(obstacle);
         }
 
     }
 
     /**
      * Get Neighbours of a Walker
      * @param walker the walker
      * @return neighbours of the Walker
      */
     Vector<CObstacle> getNearObstacles (CWalker walker, boolean NextDesiredPos)
     {
         Vector<CObstacle> nearObstacles = new Vector<CObstacle>();
 
         CPosition pos = NextDesiredPos ? walker.getDesiredNextPosition() : walker.getPosition();
         Integer gridColumn = pos.getY().intValue() / gridSizeC;
         Integer gridRow = pos.getX().intValue() / gridSizeC;
 
         Map<Integer, Vector<CObstacle>> gridRowMap;
         for (int i = gridColumn - 1; i < gridColumn + 2; i++)
         {
             if (walkerGrid.containsKey(i))
             {
                gridRowMap = obstacleGrid.get(i);
 
                 for (int j = gridRow -1; j < gridRow + 2; j++)
                 {
                     if (gridRowMap.containsKey(j))
                     {
                         nearObstacles.addAll(gridRowMap.get(j));
                     }
                 }
             }
         }
 
         return nearObstacles;
     }
 }

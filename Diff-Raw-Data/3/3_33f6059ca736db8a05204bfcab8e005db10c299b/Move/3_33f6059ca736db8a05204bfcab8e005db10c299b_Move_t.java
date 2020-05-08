 package lv.k2611a.domain.unitgoals;
 
 import java.util.List;
 
 import lv.k2611a.domain.Map;
 import lv.k2611a.domain.Unit;
 import lv.k2611a.domain.ViewDirection;
 import lv.k2611a.util.AStar;
 import lv.k2611a.util.Node;
 import lv.k2611a.util.Point;
 
 public class Move implements UnitGoal {
 
     private int goalX;
     private int goalY;
     private List<Node> path;
     private AStar aStarCache = new AStar();
 
     public Move(int goalX, int goalY) {
         this.goalX = goalX;
         this.goalY = goalY;
     }
 
     public Move(Point point) {
         this.goalX = point.getX();
         this.goalY = point.getY();
     }
 
     public int getGoalX() {
         return goalX;
     }
 
     public int getGoalY() {
         return goalY;
     }
 
     @Override
     public void process(Unit unit, Map map) {
         if (path == null) {
             path = aStarCache.calcShortestPath(unit.getX(), unit.getY(), goalX, goalY, map, unit.getId());
             lookAtNextNode(unit);
         }
         if (path.isEmpty()) {
             unit.removeGoal(this);
            unit.setTicksMovingToNextCell(0);
             return;
         }
         int ticksToNextCell = unit.getUnitType().getSpeed();
         if (unit.getTicksMovingToNextCell() >= ticksToNextCell-1) {
             // moved to new cell
             unit.setTicksMovingToNextCell(0);
             Node next = path.get(0);
             unit.setX(next.getX());
             unit.setY(next.getY());
             path.remove(next);
             if (!(path.isEmpty())) {
                 next = path.get(0);
                 // recalc path if we hit an obstacle
                 if (map.isObstacle(next, unit.getId())) {
                     path = aStarCache.calcShortestPath(unit.getX(), unit.getY(), goalX, goalY, map, unit.getId());
                 }
                 lookAtNextNode(unit);
             }
         } else {
             if (!path.isEmpty()) {
                 Node next = path.get(0);
                 if (map.isObstacle(next, unit.getId())) {
                     path = aStarCache.calcShortestPath(unit.getX(), unit.getY(), goalX, goalY, map, unit.getId());
                     lookAtNextNode(unit);
                 } else {
                     unit.setTicksMovingToNextCell(unit.getTicksMovingToNextCell() + 1);
                 }
             }
         }
         if (path.isEmpty()) {
             unit.removeGoal(null);
            unit.setTicksMovingToNextCell(0);
         }
 
     }
 
     private void lookAtNextNode(Unit unit) {
         if (path.isEmpty()) {
             return;
         }
         Node next = path.get(0);
         unit.setViewDirection(ViewDirection.getDirection(unit.getX(), unit.getY(), next.getX(), next.getY()));
     }
 
 
 }

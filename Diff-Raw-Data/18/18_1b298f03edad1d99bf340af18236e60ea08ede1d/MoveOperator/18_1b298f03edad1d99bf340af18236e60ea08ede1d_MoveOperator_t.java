 package es.deusto.ingenieria.maze;
 
 import es.deusto.ingenieria.aike.formulation.Operator;
 import es.deusto.ingenieria.aike.formulation.State;
 import es.deusto.ingenieria.maze.Cell.Wall;
 import java.awt.Point;
 
 public class MoveOperator extends Operator {
 
     public enum Direction {
         LEFT,
         RIGHT,
         UP,
         DOWN
     }
     private Direction direction;
 
     public MoveOperator(Direction moveDirection) {
         super("move" + moveDirection.toString());
         this.direction = moveDirection;
     }
 
     @Override
     protected State effect(State state) {
         Environment environment = (Environment) state.getInformation();
         Point currentLocation = environment.getCurrentLocation();
         switch(direction) {
             case UP:
                 return 
                     new State(environment.derive(new Point(currentLocation.x,
                                                            currentLocation.y - 1)));
             case DOWN:
                 return 
                     new State(environment.derive(new Point(currentLocation.x,
                                                            currentLocation.y + 1)));
             case LEFT:
                 return
                     new State(environment.derive(new Point(currentLocation.x - 1,
                                                            currentLocation.y)));
             case RIGHT:
                 return 
                     new State(environment.derive(new Point(currentLocation.x + 1,
                                                            currentLocation.y)));
             default:
                 return null;
         }
     }
 
     @Override
     protected boolean isApplicable(State state) {
         Environment environment = (Environment) state.getInformation();
         Point currentLocation = environment.getCurrentLocation();
         switch(direction) {
             case UP:
                 if (currentLocation.y > 0) {
                     Cell currentCell = environment.getCellAt(currentLocation);
                     Cell adjacentCell = environment.getCellAt(currentLocation.x,
                                                               currentLocation.y - 1);
                     return (!currentCell.hasWall(Wall.TOP))
                            && (!adjacentCell.hasWall(Wall.BOTTOM));
                 } return false;
             case DOWN:
                 if (currentLocation.y < environment.getRowCount() - 1){
                     Cell currentCell = environment.getCellAt(currentLocation);
                     Cell adjacentCell = environment.getCellAt(currentLocation.x,
                                                               currentLocation.y + 1);
                     return (!currentCell.hasWall(Wall.BOTTOM))
                            && (!adjacentCell.hasWall(Wall.TOP));
                 } return false;
             case LEFT:
                 if (currentLocation.x > 0) {
                     Cell currentCell = environment.getCellAt(currentLocation);
                     Cell adjacentCell = environment.getCellAt(currentLocation.x - 1,
                                                               currentLocation.y);
                     return (!currentCell.hasWall(Wall.LEFT))
                            && (!adjacentCell.hasWall(Wall.RIGHT));
                 } return false;
             case RIGHT:
                 if (currentLocation.x < environment.getColumnCount() - 1) {
                     Cell currentCell = environment.getCellAt(currentLocation);
                     Cell adjacentCell = environment.getCellAt(currentLocation.x + 1,
                                                               currentLocation.y);
                     return (!currentCell.hasWall(Wall.RIGHT))
                            && (!adjacentCell.hasWall(Wall.LEFT));
                 };
             default:
                 return false;
         }
     }
 
 }

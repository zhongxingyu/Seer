 package ch.zhaw.labyrinth.solver;
 
 
 import ch.zhaw.labyrinth.solver.heading.*;
 import ch.zhaw.labyrinth.utils.Coordinate;
 import ch.zhaw.labyrinth.utils.Labyrinth;
 
 /**
  * Created with IntelliJ IDEA.
  * User: bbu
  * Date: 20.04.13
  * Time: 11:49
  */
 public class RightHand extends Solver {
    int x,y,u,v;
     private Labyrinth solvedLabyrinth;
     private Labyrinth labyrinth;
     private Heading heading;
     private Coordinate currentLocation;
 
     public RightHand(Labyrinth labyrinth) {
         this.labyrinth = labyrinth;
         solvedLabyrinth = new Labyrinth();
        solvedLabyrinth.setDimension(labyrinth.getDimension());
     }
 
     @Override
     public void solve() {
         // Step counter
         int steps = 0;
 
        // set entry and exit
         Coordinate entry = labyrinth.getEntry();
        Coordinate exit = labyrinth.getExit();
         x=entry.getX();
         y=entry.getY();
        u=exit.getX();
        v=exit.getY();

        solvedLabyrinth.setCellValue(x,y,true);
        solvedLabyrinth.setCellValue(u,v,true);

         // set heading
         if(x == 0) {
             heading = new East(x, y, labyrinth, solvedLabyrinth);
         } else if (y == 0) {
             heading = new South(x, y, labyrinth, solvedLabyrinth);
         }
 
         // go through labyrinth
         while((x != u) && (y!=v)) {
             steps++;
             if(steps > 1000) { System.out.println(steps); break; }
 
             // If we already visited the next cell, try to get right
             // otherwise, go straight ahead
             if(heading.isStraight() && heading.isVisited(x, y)) {
                 if(heading.isRight()) {
                     heading.goRight();
                 } else {
                     heading.goStraight();
                 }
                 setCoordinate(heading.getCoordinate());
                 continue;
             }
 
             /**
              *  Move according to heading
               */
             // If possible go right, always the first choice
             if(heading.isRight()) {
                 heading = heading.goRight();
                 setCoordinate(heading.getCoordinate());
                 continue;
             }
             // Else go straight
             if(heading.isStraight()) {
                 heading = heading.goStraight();
                 setCoordinate(heading.getCoordinate());
                 continue;
             }
             // Try going left
             if(heading.isLeft()) {
                 heading = heading.goLeft();
                 setCoordinate(heading.getCoordinate());
                 continue;
             }
             // If everything else fails got back
             if(heading.isBack()) {
                 heading = heading.goBack();
                 setCoordinate(heading.getCoordinate());
                 continue;
             }
         }
 
         // Print Solved maze to console
        heading.getSolvedLabyrinth().printAsArray();
     }
 
     // Set x and y with the new values from the coordinate
     private void setCoordinate(Coordinate coordinate) {
         x = coordinate.getX();
         y = coordinate.getY();
     }
 
 }

 package ch.zhaw.labyrinth.solver;
 
 
 import ch.zhaw.labyrinth.utils.Cell;
 import ch.zhaw.labyrinth.utils.Coordinate;
 import ch.zhaw.labyrinth.utils.Labyrinth;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 /**
  * Created with IntelliJ IDEA.
  * User: bbu
  * Date: 20.04.13
  * Time: 11:49
  */
 public class RightHand extends Solver {
     private Labyrinth solvedLabyrinth;
     private Labyrinth labyrinth;
 
     public RightHand(Labyrinth labyrinth) {
         this.labyrinth = labyrinth;
         solvedLabyrinth = new Labyrinth();
     }
 
     @Override
     public void solve() {
         // Step counter
         int steps = 0;
 
         // find entry
         Coordinate entry = labyrinth.getEntry();
         int x=entry.getX();
         int y=entry.getY();
 
         // set exit
         Coordinate exit = labyrinth.getExit();
         int u=exit.getX();
         int v=exit.getY();
 
         // go through labyrinth
         while((x != u) && (y!=v)) {
             steps++;
             if(steps > 1000) { System.out.println(steps); break; }
 
             // if possible turn right
            if(labyrinth.getCellValueAt(x, y+1)) {
                 y++;
                 solvedLabyrinth.setCellValue(x, y, true);
                 continue;
             }
             // go down
            else if (labyrinth.getCellValueAt(x+1,y)) {
                 x++;
                 solvedLabyrinth.setCellValue(x,y,true);
                 continue;
             }
             // go left
            else if (labyrinth.getCellValueAt(x,y-1)) {
                 y--;
                 solvedLabyrinth.setCellValue(x,y,true);
                 continue;
             }
             // go up
            else if (labyrinth.getCellValueAt(x-1,y)) {
                 x--;
                 solvedLabyrinth.setCellValue(x,y,true);
                 continue;
             }
 
         }
 
         // Print Solved maze to console
         System.out.print("fertig");
     }
 
     public void nextStep() {
     }
 
 }

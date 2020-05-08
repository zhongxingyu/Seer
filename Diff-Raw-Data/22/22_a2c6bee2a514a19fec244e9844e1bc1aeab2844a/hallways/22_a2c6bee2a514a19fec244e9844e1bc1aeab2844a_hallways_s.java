 package maze;
 
 public class hallways extends FMaze {
     public static void main(String[] args) {
         FMaze Hallways = new FMaze();
     }
 
     public void hallway1() {
         int test = 0;
         System.out.println("So, you chose hallway one.  " +
                 "You have no idea what is ahead....");
         this.guard();
     }
 
     public void hallway2() {
         System.out.println(
             "Good choice.  You chose hallway two.  " +
                 "Ahead is clear skies and warm, sunny days.");
         this.desert();
     }
 
     public void hallway3() {
         System.out.println("Funny.  You chose hallway three.");
         this.snakes();
     }
 }

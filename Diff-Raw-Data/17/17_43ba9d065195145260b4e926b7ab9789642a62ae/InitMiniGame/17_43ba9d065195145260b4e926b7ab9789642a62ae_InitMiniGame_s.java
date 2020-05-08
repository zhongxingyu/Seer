 package at.blooo.minigame;
 
 import java.util.Random;
 
 import org.andengine.entity.Entity;
 
 import at.blooo.MainActivity;
 
 public class InitMiniGame extends Entity implements MiniGame {
 
   public InitMiniGame(){
     
   }
   
   @Override
   public boolean[][] getField() {
     Random rng = new Random();
     boolean field[][] = new boolean[MainActivity.FIGURE_SIZE][MainActivity.FIGURE_SIZE];
     boolean found = false;
    for (int row = 0; row < MainActivity.FIGURE_SIZE; row++){
      for (int col = 0; col < MainActivity.FIGURE_SIZE; col++){
         
         int random = rng.nextInt(row+2);
         if (random == 0){
           field[col][row] = true;
           found = true;
         }
       }
     }
     
     if (!found){
       field[MainActivity.FIGURE_SIZE/2][MainActivity.FIGURE_SIZE/2] = true;
     }
     return field;
   }
 
   @Override
   public void quit() {
 
   }
 
   @Override
   public void start() {
     
   }
 
 }

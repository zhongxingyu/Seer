 package adndframework.framework;
 
 import javax.imageio.ImageIO;
 
 /**
  * This class handles most of the program's events.
  * 
  * @author Darin Beaudreau
  */
 public class Framework {
     public adndframework.windows.MainWindow mainWindow;
     
     public Framework() {
         this.mainWindow = new adndframework.windows.MainWindow(this);
     }
     
     public java.awt.image.BufferedImage loadImage(String filename) {
        java.net.URL u = getClass().getResource(filename);
        if(u != null) {
             try {
                java.awt.image.BufferedImage image = ImageIO.read(u);
                 return image;
             } catch(java.io.IOException io) {
                System.out.println("Error reading file: " + u.getFile().toString());
             }
         }
         return null;
     }
     
     public static int roll(int numberOfDice, int faces) {
         // Recursively calculates the sum of a number of die rolls according to how
         // many dice are rolled and how many faces the die have.
         java.util.Random r = new java.util.Random();
         int result = r.nextInt(faces) + 1;
         if(numberOfDice == 1) return result;
         else return (roll((numberOfDice - 1), faces) + result);
     }
     
     public static int [] rollArray(int numberOfDice, int faces) {
         // Rolls a certain number of dice according to how many faces are specified.
         // This method differs from the roll() method in that it returns all the individually
         // rolled values, rather than the sum. Also, this method is not recursive.
         java.util.Random r = new java.util.Random();
         int [] scores = new int[numberOfDice];
         for(int i = 0; i < numberOfDice; i++) { scores[i] = r.nextInt(faces) + 1; }
         return scores;
     }
     
     public static int [] rollAbilityScores() {
         // This method "rolls" an array of numbers representing the six ability scores.
         // By default, this method uses the 4d6 method which subtracts the lowest of the
         // four rolls. In the future, I may add an integer parameter which will let
         // the user specify the generation method.
         int [] scores = new int[6];
         for(int i = 0; i < 6; i++) {
             int [] rolls = rollArray(4, 6);
             int low = rolls[0];
             int sum = 0;
             for(int j : rolls) {
                 if(j < low) low = j;
                 sum += j;
             }
             sum -= low;
             scores[i] = sum;
         }
         return scores;
     }
 }

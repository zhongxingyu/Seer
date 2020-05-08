 /**
  * Scrambler.java 
  * 
  * Rubik's Cube scramble generator
  * 
  * @author Elliot Penson
  */
 
 public class Scrambler {
 
     /**
      * The faces of a Rubik's Cube represented as side/axis
      */
     private static final String[] faces = { "RX", "LX", "UY", "DY", "FZ", "BZ" };
 
     /**
      * Possible directions of a face turn
      */
     private static final String[] rotation = { "", "'", "2" };
     
     /**
     * Main method
      * @param args
      */
     public static void main(String[] args) {
         for(int i = 0; i < Integer.parseInt(args[0]); i++) {
             System.out.println(generateScramble());
         }
     }
 
     /**
      * Generates a random 25 move scramble
      * @return 
      */
     public static String generateScramble() {
         String scramble = "";
         String lastFace = "  ";
 
         for(int i = 0; i < 25; i++)
            scramble += (lastFace = randomFace(lastFace)).charAt(0) + randomDirection() + " ";
 
         return scramble;
     }
 
     /**
      * Finds a random face that is not on the same axis as the previous turn.
      * @param lastFace
      * @return
      */
    public static String randomFace(String lastFace) {
         String toReturn = faces[(int)(Math.random() * faces.length)];
        if(toReturn.charAt(1) == lastFace.charAt(1))
            return randomFace(lastFace);
         return toReturn;
     }
 
     /**
      * Returns a random direction for a face rotation
      * @return
      */
     public static String randomDirection() {
         return rotation[(int)(Math.random() * 3)];
     }
 
 }

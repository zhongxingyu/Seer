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
      * Main method for testing purposes
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
         
         String penultimateFace = "  ";
         String lastFace = "  ";
 
        for(int i = 0; i < 25; i++) {
            String newFace = randomFace(penultimateFace, lastFace);
            scramble += newFace.charAt(0) + randomDirection() + " ";
            penultimateFace = lastFace;
            lastFace = newFace;
        }
 
         return scramble;
     }
 
     /**
      * Finds a random face that is not on the same axis as the previous turn.
      * @param lastFace
      * @return
      */
     public static String randomFace(String penultimate, String last) {
         String toReturn = faces[(int)(Math.random() * faces.length)];
         if(last.equals(toReturn) || sameAxis(penultimate, last, toReturn))
             return randomFace(penultimate, last);
         return toReturn;
     }
     
     /**
      * Compares three face's axes
      * @return true if all the Strings have the same axis
      */
     public static boolean sameAxis(String a, String b, String c) {
        return a.charAt(1) == b.charAt(1) && b.charAt(1) == c.charAt(1);
     }
 
     /**
      * Returns a random direction for a face rotation
      * @return
      */
     public static String randomDirection() {
         return rotation[(int)(Math.random() * 3)];
     }
 
 }

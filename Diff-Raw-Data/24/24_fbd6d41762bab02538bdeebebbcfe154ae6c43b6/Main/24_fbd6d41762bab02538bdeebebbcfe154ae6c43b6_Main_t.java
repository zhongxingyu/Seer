 package earfuck;
 
 public class Main {
     public static void main(String[] args) {
         String efCode = args[0];
 
        System.out.print("Loading new earfuck parser...");
         Parser efParser = new Parser((byte) 42);
        System.out.println("Done!");
 
        System.out.print("Handing music to performer...");
         efParser.giveMusic(efCode);
        System.out.println("Done!");
 
        System.out.println("Performing...");
         efParser.perform();
        System.out.println("Piece complete.");
     }
 }

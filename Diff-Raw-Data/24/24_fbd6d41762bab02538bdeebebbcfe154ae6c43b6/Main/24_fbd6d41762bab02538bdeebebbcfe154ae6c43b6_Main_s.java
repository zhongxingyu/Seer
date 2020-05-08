 package earfuck;
 
 public class Main {
     public static void main(String[] args) {
         String efCode = args[0];
 
         Parser efParser = new Parser((byte) 42);
 
         efParser.giveMusic(efCode);
 
        efParser.setIoManager(new SystemIoManager());

         efParser.perform();
     }
 }

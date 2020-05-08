 import java.util.*;
 import java.io.*;
 
 public class Main {
 
   public static void main(String[] args) {
     
     // Add hook for
     Runtime.getRuntime().addShutdownHook(new Thread() {
        @Override
        public void run() {
          // TODO(jack): replace
          System.out.println("Shutdown hook ran!");
        }
       });
     
     Scanner scan = new Scanner(System.in);
     String map = "";
     while(scan.hasNext()) {
       map += scan.nextLine() + System.getProperty("line.separator");
     }
     
     //Board b = new Board(map);
     Skynet superSky = new GreedySkynet(map);
     System.out.println(superSky.plan());
     
     /*while (true) {
       try {
      Thread.sleep(1000);
       } catch (InterruptedException e) {
      System.out.println("interrupted: " + e);
       }
       }*/
 
 
   }
}

 /*
 * TODO: enter sensible info
  */
 
 import dapij.InstanceCreationStats;
 import dapij.InstanceCreationTracker;
 
 /**
  *
  * @author emszy
  */
 public class HelloAzura {
 
     private int i;
   
     public HelloAzura(int i) {
         this.i = i;
     }
 
     public HelloAzura(char c) {
         this(5);
         System.out.println(c);
     }
     
     public int square() {
         int sq = this.i * this.i;
         System.out.println(sq);
         return sq;
     }
 
     public void azura() {
         System.out.println("Hello Azura");  /* Azura is the name of my dog */
     }
     
     public static void main(String args[]) {
         System.out.println("BEGIN MAIN");
         HelloAzura ha = new HelloAzura(2);  /* create object to test agent */
         ha.azura();
 
         System.out.println("Concurrent Map Size: " +
                 String.valueOf(InstanceCreationTracker.INSTANCE.getSize()));
     }
 }

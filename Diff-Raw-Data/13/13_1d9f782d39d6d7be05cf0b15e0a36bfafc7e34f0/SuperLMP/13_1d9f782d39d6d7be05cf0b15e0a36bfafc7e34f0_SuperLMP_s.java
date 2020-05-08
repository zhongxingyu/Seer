 /*
  * Like LMP, but super
  */
 package lmp;
 
 /**
  *
  * @author Adrian
  */
 public class SuperLMP {
     public static Double result;
     public static void main(String[] args) {
         Simulator mario = new Simulator(20, 20.0);
         result = mario.send();
         if(result==-1)
         {
              System.out.println("Hooray! You sent at time: "+mario.tryAgain());
         }
         else
             System.out.println("Hooray! You sent at time: "+result);
    }
 }

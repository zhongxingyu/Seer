 import java.util.Scanner;
 
 /**
  * Created with IntelliJ IDEA.
  * User: abychko
  * Date: 04.09.13
  * Time: 19:35
  * To change this template use File | Settings | File Templates.
  */
 public class Random {
     public static  void main (String args[]){
         int myDigit = (int)( Math.random() * 100);
         int trycount = 0;
         String val;
        int tries = 8, inval;
         System.out.println("Enter your Integer");
        while (trycount < tries) {
 
             try {
                 val = new Scanner(System.in).next();
                inval = Integer.valueOf(val);
             }catch (NumberFormatException t){
                System.out.println("Not an integer! " + t);
                 continue;
             }
            if (inval == myDigit) {
                 System.out.println("You win!");
                 System.exit(0);
             }
            if (inval > myDigit) {
                 System.out.println("It was less");
             }
            if (inval < myDigit) {
                 System.out.println("It was greater");
             }
 
             trycount++;
         }
         System.out.println("Game over, computer win! it was " + myDigit);
     }
 }

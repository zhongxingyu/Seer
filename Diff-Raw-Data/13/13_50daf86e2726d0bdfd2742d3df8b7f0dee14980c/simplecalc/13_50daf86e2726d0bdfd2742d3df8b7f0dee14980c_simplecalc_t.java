 /**
  * A simple java calculator.
  * @author Dakota Strickner (computerxpds)
  * @version 1
 * @build 7
  * This very arcain and simple calculator will be able to do 2 variable operations. 
  */
 //import
 import java.util.*;
 //define class
public class simplecalc{
 
     static Scanner console = new Scanner(System.in);
     //main string
    public static void main(String[] args){
         boolean go = true;
         {
             System.out.println("This is my crappy calculator!");
             
             Scanner scan = new Scanner(System.in);
             Scanner scan1 = new Scanner(System.in);
             
             
             String action = scan.nextLine();
             
             if("a".equals(action))
             {
                 System.out.println("Now type in the first number you would like to add.");
                 int add1 = scan.nextInt();
                 System.out.println("Now type the second number.");
                 int add2 = scan.nextInt();
                 int add3 = add1 + add2;
                 System.out.println(add1 + " added to " + add2 + " equals " + add3 + "!");
             }
     }
 }
 }
         

 package RandomStuff;
 import java.util.Scanner;
 
 public class CircleArea {
 
     public static void main(String[] args) {
         Scanner numScan = new Scanner(System.in);
         double totalArea;
         double radius;
        final double PI = 3.14159265;
         
         System.out.println("So you want to find out the area of a circle eh?");
         System.out.println("It's fairly simple...as long as you know your radius.");
         System.out.println("What is your radius in centimetres?");
         radius = numScan.nextDouble();
         if (radius <= 0){
             System.out.println("Circles have to have a positive radius!");
             System.out.println("Enter a positive radius next time!");
         } 
         else{ 
        totalArea = PI*radius*radius;
         System.out.println("The area of your circle is " + totalArea + " square centimetres.");
         }
 
     }
 }

 import java.util.Scanner;
 
 /**
  * Programmer: kyle
  * Date: 1/24/13
  * Time: 2:34 PM
  * Exercise: 1.26
  */
 public class PolynomialMaker {
     private static Polynomial myPoly;
 

    public static void makePolynomial() {
        Scanner input = new Scanner(System.in);
        System.out.println("Please enter the degree of the polynomial and " +
                "the coefficient (highest degree to lowest)");
    }

     public static void main(String[] args) {
         int count = 0;
         myPoly = new Polynomial(Integer.parseInt(args[0]));
         for(int n = 1; n < args.length; n++) {
             if((Integer.parseInt(args[n]) != 0)) {
                 count++;
             }
         }
         for(int i = 1; i < args.length; i++) {
             myPoly.setCoefficients(count,Integer.parseInt(args[i]));
             count--;
         }

        askUser();
        //System.out.println(myPoly.evaluate(0));
        //System.out.println(myPoly.evaluate(1));
        //System.out.println(myPoly.evaluate(0.5));
     }
 
     public static void askUser() {
         Scanner input = new Scanner(System.in);
         System.out.println("Enter a value>");
         int value = input.nextInt();
         System.out.println("The value is " + myPoly.evaluate(value));
 
         System.out.println("Continue?>");
         String valueYesNo = input.next();
         if(valueYesNo.equals("Yes") || valueYesNo.equals("yes") || valueYesNo.equals("y")) {
             askUser();
         }
         else {
             System.exit(0);
         }
     }
 }

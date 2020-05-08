 import java.util.*;
 public class factorialFunction
 {
   static Scanner console = new Scanner(System.in);
   public static void main(String[] args)
   {
     int number = 0;
     int z = 0;
     int n = 0;
     boolean f = false;
     do
     {
       try
       {
         System.out.println("Enter a whole number");
         number = console.nextInt();
         n = number;
         z = n;
         do
         {          
           z = z * n;
           n = n - 1;         
         }        
         while(n != 0);
        if (number >= 0)
         {
           throw new NegNumberException();          
         }
         System.out.println("The factorial of " + number + " is " + z);
         
       }
       catch(NegNumberException a)
       {
         System.out.println(a.toString());
         f = true;
       }
       catch(InputMismatchException b)
       {
         System.out.println(b.toString());
       }
     }
     while(!f);
   }
 }

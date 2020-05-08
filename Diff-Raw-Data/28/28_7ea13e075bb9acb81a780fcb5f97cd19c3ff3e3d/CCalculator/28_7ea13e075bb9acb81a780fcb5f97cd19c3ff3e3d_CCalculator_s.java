 package ccalculator;
 import java.util.Scanner;
 import java.text.*;
 
 /**
  * Simple console calculator
  * 
  * @author Dremora
  * @version 0.1 beta
  */
 public class CCalculator
 {
     public static void main(String[] args)
     {
         String line;
         if (args.length == 0)
         {
             System.out.println("CCalculator v0.1");
             System.out.println();
             System.out.println("Input a combination of numbers, basic arithmetic operators and brackets, then press <Enter> to get a result");
         
             Scanner scanner = new Scanner(System.in);
             while(true)
             {
                 System.out.print(" > ");
                 line = scanner.nextLine();
                 if (line.trim().toLowerCase().equals("exit"))
                 {
                     break;
                 }
                 try {
                 	System.out.println(calculate(line));
                 } catch (ParseException e)
                 {
                     for (int i = -3; i < e.getErrorOffset(); i++)
                     {
                         System.err.print(" ");
                     }
                     System.err.println("^");
                     System.err.println(e.getMessage());
                 }
             }
         }
         else
         {
             String result = "";
             for (String str: args) {
                 result += str;
             }
             try {
 				System.out.println(calculate(result));
 			} catch (ParseException e) {
 				System.err.println(e.getErrorOffset() + ": " + e.getMessage());
 			}
         }
     }
     
     public static String calculate(String line) throws ParseException
     {
        Block expression = new Block(line);
         String value = Double.toString(expression.value());
         if (value.endsWith(".0")) {
         	value = value.substring(0, value.length() - 2);
         } else if (value.equals("NaN")) {
         	value = "Can't calculate result";
         }
         return value;
     }
     
     public static boolean inArray(char[] array, char value)
     {
         for (char element: array)
         {
             if (element == value) return true;
         }
         return false;
     }
 }

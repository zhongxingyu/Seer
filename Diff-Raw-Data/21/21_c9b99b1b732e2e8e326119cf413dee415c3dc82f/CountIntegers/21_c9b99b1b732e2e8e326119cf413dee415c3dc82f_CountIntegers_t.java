 /* Copyright (C) 2013 Travis Mosley <tprowx@gmail.com>
  *
  * Design and implement a program that counts the number of inte-
  * ger values in a text input file. Produce a table listing the values
  * you identify as integers from the input file.
  *
  */
 
 import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
 
 public class CountIntegers
 {
   public static void main(String[] args)
   {
     int value;
    ArrayList<Integer> arr = new ArrayList<>(100);
 
     try {
       Scanner scan = new Scanner(new File("input.dat"));
     }
    catch(FileNotFoundException e) {
	    System.err.println("File not found" + e.getMessage());
    }
     catch(IllegalArgumentException e) {
       System.err.println("Found errorneous file path" + e.getMessage());
       System.exit(1);
     }
     catch(NullPointerException e) {
       System.err.println(e.getMessage());
     }
 
     while(scan.hasNextInt()) {
       arr.add(new Integer(scan.nextInt()));
     }
 
     int div = 1;
     for(int x = 0; x < arr.size(); x++)
     {
      System.out.print(arr.get(x) + ((div % 5 != 0) ? " " : ""));
      if(div % 5 == 0) {
		    System.out.println();
      }
       div++;
     }
   }
 }

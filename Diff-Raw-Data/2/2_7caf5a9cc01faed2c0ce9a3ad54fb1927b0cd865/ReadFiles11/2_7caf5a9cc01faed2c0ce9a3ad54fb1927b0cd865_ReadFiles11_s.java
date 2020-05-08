 import java.io.*;
 import java.util.Scanner;
 
 /**
  * Proof of reading/writing data, along with some unrelated busy-work tasks
  * (like sorting numbers). Tazks will be seperated into methods as necesary.
  *   Writing/Reading data
  * - course:     COMP-171-801RL
  * - assignment: pg 300; que. #8.22; due: 12/14
  *
  * @author Jonathan Zacsh <jzacsh@gmail.com>
  */
 public class ReadFiles11 {
   //set constants
   public static final int MAX = 300; //highest value for random number generation
   public static final int QUANTITY = 100; //number of datas to be passed around
   public static final String DELIM = " "; //delimeter to use for seperating datas
 
   public static void main(String[] args) {
 
     Scanner stdin = new Scanner(System.in);
 
     //printed documentation
     String intro = "\nJonathan Zacsh <jzacsh@gmail.com>; COMP-171-801RL\n";
     intro += "pg 300; que. #8.22; due: 12/14\n";
     String summary  = "Write a program to create a file named Exercise8_22.txt if it does not exist\n";
            summary += "write one hundred integers created randomly into the file using text I/O.\n";
            summary += "Integers are separated by spaces in the file,. Read the data back from the\n";
            summary += "file and display the sorted data.\n";
     System.out.printf("%s\n%s\n", intro, summary);
 
     //request user input of file name
     System.out.printf("name of new file? ");
     String filename;
     filename = "./" + stdin.next() + ".txt";
 
     //file objects
     java.io.File fd = new java.io.File(filename); //create file descriptor
     try {
       java.io.PrintWriter fwrite = new java.io.PrintWriter(filename); //create file out
       java.util.Scanner fread = new java.util.Scanner(fd); //create file in
       fread.useDelimiter(DELIM);
 
       //initialize array to hold from file and sorted.
       int[] randoms;
       randoms = new int[QUANTITY];
       int[] sorted;
       sorted = new int[QUANTITY];
 
       //ensure file exists
       if (!fd.exists()) {
         //file does not exist, create now.
         int e = AppendTxtFile(fwrite);
         if (e != 0) {
           System.err.printf("Failed to create new file\n");
           return;
         }
       }
 
       //read in data from file, explode into int array randoms
       int i = 0;
       while (fread.hasNext() && i <= QUANTITY) {
         randoms[i] = fread.nextInt();
         i++;
       }
       fread.close();
 
       //proof of unsorted data
       System.out.printf("The UNsorted, random array is:\n");
       FormatInts(randoms);
 
       //sort the data that's been read in
       SortInts(randoms, sorted);
 
       //print neat table of data in randoms array
       System.out.printf("The SORTED, array is:\n");
       FormatInts(sorted);
     }
     catch (FileNotFoundException e) {
       e.printStackTrace();
       System.exit(1);
     }
   }
 
   /**
    * Generates [QUANTITY] random integers which are immediately passed into a file on
    * disk.
    *
    * @param  object  PrintWriter object being used
    * @return int     status code of file composition.
    *  - 0 upon success
    *  - 1 upon failure.
    */
   public static int AppendTxtFile(PrintWriter f) {
     int i = 0;
    while (i < 100) {
       int r = (int)(Math.random() * MAX);
       f.printf("%d%s", r, DELIM);
       i++;
     }
     f.close();
 
     return 0;
   }
 
   /**
    * Sorts an array of integers in ascending order.
    *
    * @param  int    an array of integers to be sorted in memory.
    * @param  int    an empty array of integers to hold sorted data.
    * @return void
    */
   public static void SortInts(int[] datas, int[] sorted) {
     int sentinal = -1;
     int largest = sentinal;
     int current = 0;
     for (int s = 0; s < sorted.length; s++) {
       for (int i = 0; i < datas.length; i++) {
         if (datas[i] > largest && datas[i] != sentinal) {
           sorted[s] = datas[i];
           largest = datas[i];
           current = i;
         }
       }
       datas[current] = sentinal;
       largest = sentinal;
     }
     return;
   }
 
   /**
    * Prints a consistent, single formatting for any given array of integers.
    *
    * @param int  array of integers
    * @return void
    */
   public static void FormatInts(int[] datas) {
     int size = datas.length;
     for (int i = 0; i < size; i++) {
       System.out.printf("%d\n", datas[i]);
     }
   }
 }

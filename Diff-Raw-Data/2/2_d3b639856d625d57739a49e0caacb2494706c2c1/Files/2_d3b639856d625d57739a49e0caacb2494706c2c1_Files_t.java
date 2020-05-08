 import java.util.*;
 import java.io.*;
 
 
 /**
  * File reader/writer helper class.
  *
  * @author Liran Oz, Ory Band
  * @version 1.0
  */
 public class Files {
     /**
      * @param path Input file path.
      * @return ID string read from file given as argument.
      */
     public static String read(String path){
         String ids = "";
 
         try {
             File in           = new File(path);
             FileReader fr     = new FileReader(in);
             BufferedReader br = new BufferedReader(fr) ;
 
             String temp = "";
 
             while (temp != null ) {
                 temp = br.readLine();
 
                 if (temp != null) {
                     ids += temp;
                 }
             }
 
             br.close();
             fr.close();
 
         } catch(Exception e) {
             System.out.println("Error \"" + e.toString() + "\" on file " + path);
             e.printStackTrace() ;
             System.exit(-1);  // Hard exit.
         }
 
         return ids;
     }
 
     /**
      * @param a ID array to fill.
      * @param l Left-side pivot.
      * @param r Right-side pivot.
      *
      * @return Quick-sort-partitioned array.
      */
     private int partition(int[] a, int l, int r) {
         int i = l,
             j = r,
             tmp,
             pivot = a[(l+r)/2];
 
         while (i <= j) {
             while (a[i] < pivot) {
                 i++;
             }
 
             while (a[j] > pivot) {
                 j--;
             }
 
             if (i <= j) {
                 tmp = a[i];
                 a[i] = a[j];
                 a[j] = tmp;
                 i++;
                 j--;
             }
         };
 
         return i;
     }
 
     /**
      * @param a Array to quicksort.
      * @param l Left helper.
      * @param r Right helper.
      *
      * @return Quicksorted array.
      */
    static public void quickSort(int a[], int l, int r) {
         int index = partition(a, l, r);
         if (l < index - 1) {
             quickSort(a, l, index - 1);
         }
 
         if (index < r) {
             quickSort(a, index, r);
         }
     }
 
     /**
      * Writes string to file.
      *
      * @param path File to write into.
      * @param s String to write.
      */
     public static void write(String path, String s) {
         try {
             File out      = new File(path);
             FileWriter fw = new FileWriter(out, true);
 
             fw.append(s + "\r\n");
             fw.close();
         } catch (Exception e) {
             System.out.println("Error \"" + e.toString() + "\" on file " + path);
             e.printStackTrace();
             System.exit(-1) ; // Hard exit.
         }
     }
 }
 

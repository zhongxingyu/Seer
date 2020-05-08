 package sg;
 
 /******
  * Util
  * @author mlfong
  * @version 1.0
  * Many useful utility functions.
  *****/
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Iterator;
import java.util.Set;
 
 @SuppressWarnings("all")
 
 public class Util
 {
     /****
      * prettyPrint
      * prints out an array of strings
      * @param arr
      */
     public static void prettyPrint(String[] arr)
     {
         System.out.print("[");
         for(int i = 0 ; i < arr.length; i++)
         {
             System.out.print(arr[i]);
             if(i != arr.length-1)
                 System.out.print(", ");
         }
         System.out.println("]");
     }
     
     /****
      * makeStringArray
      * turns an ArrayList of Strings into a String[] 
      * @param al
      * @return
      */
     public static String[] makeStringArray(ArrayList<String> al)
     {
         String[] s = new String[al.size()];
         for(int i = 0 ; i < al.size(); i++)
             s[i] = al.get(i);
         return s;
     }
     
     /****
      * prettyPrint
      * prints out the contents of an ArrayList
      * @param al
      */
     public static void prettyPrint(ArrayList al)
     {
         System.out.print("[");
         for(int i = 0 ; i < al.size(); i++)
         {
             System.out.print(al.get(i));
             if(i != al.size()-1)
                 System.out.print(", ");
         }
         System.out.println("]");
     }
     
     /****
      * prettyPrint
      * prints out the contents of a HashSet
      * @param hs
      */
     public static void prettyPrint(HashSet hs)
     {
         System.out.print("[");
         Iterator iter = hs.iterator();
         while(iter.hasNext())
         {
             System.out.print(iter.next() + ", ");
         }
         System.out.println("]");
     }
     
     /*****
      * reallyPrettyPrint
      * prints out the sorted contents of a HashSet
      * @param hs
      */
     public static void reallyPrettyPrint(HashSet hs)
     {
         ArrayList al = new ArrayList();
         for(Object o : hs)
             al.add(o);
         Collections.sort(al);
         prettyPrint(al);
     }
    public static void reallyPrettyPrint(Set s){
        ArrayList al = new ArrayList();
        for(Object o : s)
            al.add(o);
        Collections.sort(al);
        prettyPrint(al);
    }
     
     /*****
      * count
      * counts the number of occurrences of char c in String s
      * @param s
      * @param c
      * @return
      */
     public static int count(String s, char c)
     {
         int cc = 0;
         for(int i = 0 ; i < s.length(); i++)
             if(s.charAt(i)==c)
                 cc++;
         return cc;
     }
     
     /****
      * strips whitespace from input
      * @param input
      * @return
      */
     public static String removeSpaces(String input) {
         StringBuilder sb = new StringBuilder();
         for(Character c : input.toCharArray())
             if(c!=' ' && c!='\t')
                 sb.append(c);
         return sb.toString();
     }
 }

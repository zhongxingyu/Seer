 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package royal.tree;
 
 import java.util.*;
 
 /**
  *
  * @author forrest
  */
 public class Main {
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
         RoyalTree.Node tree = fileReader();
         System.out.println("The queen is: " + RoyalTree.root.element);
     }
 
     public static RoyalTree.Node fileReader() {
         System.out.println("Enter the file name or path");
         Scanner in = new Scanner(System.in);
        String path = in.nextLine().trim();
         RoyalTree.Node tree = null;
         try {
             tree = FileIO.read(path);
        } catch (Exception FileNotFoundException) {
             System.out.println("Cannot find file.  Try again");
             fileReader();
         }
         return tree;
     }
 }

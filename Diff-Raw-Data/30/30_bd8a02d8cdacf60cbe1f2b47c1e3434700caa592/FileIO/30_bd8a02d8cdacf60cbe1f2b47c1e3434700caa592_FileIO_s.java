 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package royal.tree;
 
import java.io.File;
import java.util.Scanner;
 
 /**
  *
  * @author forrest
  */
 public class FileIO {
 
    public static RoyalTree.Node read(String path) throws Exception {
 
 
         File inputFile = new File(path);
        Scanner in = new Scanner(inputFile);
        while (in.hasNextLine()) {
             boolean root = false;
            String line = in.nextLine();
 
             String[] lineArray = line.split("\\s+");
             if (lineArray.length < 3) {//line is root node
                 root = true;
                 RoyalTree.add(lineArray[0], null, Integer.parseInt(lineArray[1]), root);
             } else {//line is not root node
                 RoyalTree.add(lineArray[0], lineArray[1], Integer.parseInt(lineArray[2]), root);
             }
         }
 
 
 
         return RoyalTree.root;
     }
 }

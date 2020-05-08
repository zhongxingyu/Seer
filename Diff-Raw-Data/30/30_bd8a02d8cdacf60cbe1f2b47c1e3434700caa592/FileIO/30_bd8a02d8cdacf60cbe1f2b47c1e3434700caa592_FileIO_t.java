 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package royal.tree;
 
import java.io.*;


 
 /**
  *
  * @author forrest
  */
 public class FileIO {
 
    public static RoyalTree.Node read(String path) throws IOException {
        System.out.println("FileIO is looking for:" + path);
 
 
         File inputFile = new File(path);
         BufferedReader input =  new BufferedReader(new FileReader(inputFile));
         String line = null;
        while ((line = input.readLine()) != null) {
             boolean root = false;
            
 
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

 package input;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.util.ArrayList;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import knapsackj.Item;
 import knapsackj.ItemCollection;
 
 /**
  * Parses a CSV file. Found from StackOverflow. Adapted for my needs.
  * @source http://stackoverflow.com/questions/5868369/how-to-read-a-large-text-file-line-by-line-in-java
  * @author peter-lawrey
  * @author tcc10a
  */
 public class CSVParser 
 {
 
     String file;
 
     public CSVParser(String filename) 
     {
         file = filename;
     }
 
     public ItemCollection parse() 
     {
         int cap = -1;
         ArrayList<Item> items = new ArrayList<Item>();
         
         try {
             BufferedReader br = new BufferedReader(new FileReader(file));
             String line;
             boolean first = true;
             while ((line = br.readLine()) != null) {
                 // process the line.
                 String[] strArr=line.split(",");
                 if (first) 
                 {
                     cap = Integer.parseInt(strArr[0]);
                 } else {
                     int weight = Integer.parseInt(strArr[0]);
                     int value = Integer.parseInt(strArr[1]);
                     String name = strArr[2];
                     Item temp = new Item(weight,value,name);
                     items.add(temp);
                 }
             }
             br.close();
         } catch (FileNotFoundException ex) {
             Logger.getLogger(CSVParser.class.getName()).log(Level.SEVERE, null, ex);
         } catch (Exception e) {
             System.err.println("An error ocurred while processing the CSV file!");
         }
         
         ItemCollection ic = new ItemCollection(items, cap);
         return ic;
     }
 }

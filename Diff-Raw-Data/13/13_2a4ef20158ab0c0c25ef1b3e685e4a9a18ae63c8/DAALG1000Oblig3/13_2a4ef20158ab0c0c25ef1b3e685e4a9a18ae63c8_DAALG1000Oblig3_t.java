 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package da.alg1000.oblig3;
 
 import java.util.AbstractMap;
 import java.util.Scanner;
 
 /**
  *
  * @author Martin
  */
 public class DAALG1000Oblig3 {
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
         Scanner inp = new Scanner(System.in);
 
 
         System.out.print("Number-of-buckets> ");
         hashTable<String, String> ht = new hashTable<>(Integer.parseInt(inp.nextLine()));
         String key;
 
         while (true) {
             System.out.println();
             System.out.println("0: Quit");
             System.out.println("1: Store");
             System.out.println("2: Retrieve");
             System.out.println("3: Print table");
             System.out.println("LF=" + ht.calculateLoadFactor());
             System.out.println();
             System.out.print("COMMAND> ");
             switch (Integer.parseInt(inp.nextLine())) {
                 case 0:
                    exitManager eM=new exitManager(1);
                     return;
                 case 1:
                     System.out.print("Key> ");
                     key = inp.nextLine();
                     System.out.print("Value> ");
                     String value = inp.nextLine();
                    if (!ht.contains(key)) {
                        ht.add(key, value);
                    } else {
                        ht.set(key, value);
                    }
                     break;
                 case 2:
                     System.out.print("Key> ");
                     key = inp.nextLine();
                     System.out.println(ht.get(key));
                     break;
                 case 3:
                     for (Object o : ht.toArray()) {
                        System.out.println("\"" + ((AbstractMap.SimpleEntry<String, String>) o).getKey() + "\":\"" + ((AbstractMap.SimpleEntry<String, String>) o).getValue() + "\"");
                     }
                     break;
             }
         }
     }
 }

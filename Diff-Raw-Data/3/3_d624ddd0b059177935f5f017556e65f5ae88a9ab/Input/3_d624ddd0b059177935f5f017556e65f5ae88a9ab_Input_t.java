 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.Scanner;
 
 public class Input {
     private static int               pos   = 0;
     private static ArrayList<String> input = new ArrayList<String>();
 
     static {
         try {
             Scanner scn = new Scanner(new File(Interpreter.data), "UTF-8").useDelimiter("\\s+");
 
             while (scn.hasNext()) {
                 input.add(scn.next());
             }
         } catch (FileNotFoundException e) {
            System.out.println("Couldn't find the data file!");
            System.exit(0);
         }
     }
 
     private IdList idl;
 
     public Input() {
         idl = null;
     }
 
     void ParseInput() {
         Tokenizer.INSTANCE.skipToken();    // read
         idl = new IdList();
         idl.ParseIdList();
         Tokenizer.INSTANCE.skipToken();    // ;
     }
 
     void PrintInput() {
         System.out.print("read ");
         idl.PrintIdList();
     }
 
     void ExecInput() {
         pos = idl.ReadIdList(pos);
     }
 
     public static int getVal(int p) {
         if (p >= input.size()) {
             System.out.println("Error! Trying to read values that aren't in the data file.");
         }
 
         return Integer.valueOf(input.get(p));
     }
 }

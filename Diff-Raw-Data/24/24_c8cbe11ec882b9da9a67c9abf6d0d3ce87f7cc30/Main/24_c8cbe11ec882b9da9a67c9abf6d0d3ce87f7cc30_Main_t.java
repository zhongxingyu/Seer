 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 
 /**
  * User: moulikrishna.
  * Institute: IIT Delhi.
  * purpose: As part of course requirements CSL211 Computer Architecture.
  * Submission date: 15th September, 2013
  */
 
 public class Main {
 
   public static Register register;
   public static Stack stack;
   public static Label labels;
   public static Program p;
   public static BufferedReader Input;
 
   public static void main(String args[]) {
     String fileName = takeArguments(args);
     register = new Register();
     stack = new Stack();
 
     p = new Program(fileName);
     long startPos = p.getFilePointer();
     labels = new Label(p);
 
     //start reading input file;
     try {
       if (args.length >= 2) {
         Input = new BufferedReader(new FileReader(args[1]));
       }
     } catch (Exception e) {
       Debug.forceQuit("The input file you mentioned does not exist");
     }
 
     Debug.write("The file is loaded and ready to execute");
     p.setPointer(labels.findLabel(".main"));
 
     RISCInstruction rs = p.getNextCommand();
     while (rs != null) {
       rs.exeCommand();
       rs = p.getNextCommand();
     }
   }
 
   public static String readNextLine() {
     if (Input == null) {
       Debug.forceQuit("There is no file to read");
     }
     try {
       return Input.readLine();
     } catch (IOException e) {
       Debug.forceQuit("The input file is corrupted or you are trying to read non existing lines");
     }
     return "";
   }
 
   private static String takeArguments(String[] args) {
     //assuming the fileName is written in args[0];
     if (args.length > 0) {
      if (args.length > 2) {
        Debug.startDebugging(args[2]);
       }
       return args[0];
     } else {
       Debug.forceQuit("The File name is missing we need atleast one file name to start with");
     }
     return "";
   }
 }

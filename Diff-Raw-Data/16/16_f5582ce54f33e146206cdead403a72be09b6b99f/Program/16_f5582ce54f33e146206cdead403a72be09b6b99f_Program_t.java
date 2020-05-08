 import java.io.EOFException;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
import java.util.NoSuchElementException;
 import java.util.Scanner;
 
 /**
  * User: moulikrishna.
  * Institute: IIT Delhi.
  * purpose: As part of course requirements CSL211 Computer Architecture.
  * Submission date: 15th September, 2013
  */
 
 public class Program {
 
   private static Scanner fileSc;
 
   public Program(String fileName) {
     try {
       FileInputStream fs = new FileInputStream(fileName);
       fileSc = new Scanner(fs);
     } catch (FileNotFoundException e) {
       Debug.forceQuit("The file you gave me is not found");
     }
   }
 
   private static String nextCommandStr() {
     String result = "";
     try {
       while (result == "") {
         result = nextLine();
       }
       return result;
     } catch (EOFException e) {
       Debug.write("EOF reached in the given file");
       return null;
    } catch (NoSuchElementException e) {
      Debug.write("EOF reached in the given file");
      return null;
     }
   }
 
   private static String nextLine() throws EOFException {
    if (fileSc.hasNextLine()) {
      String result = fileSc.nextLine();
      result = result.trim();
      return result;
    } else return null;
   }
 
   public RISCInstruction getNextCommand() {
     String newLine = nextCommandStr();
    Debug.command(newLine);
     if (newLine == null) return null;
     else {
       RISCInstruction cmd = new RISCInstruction(newLine);
       return cmd;
     }
   }
 }

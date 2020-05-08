 package projects.sudokusolver;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.apache.commons.io.FileUtils;
 
 /**
  *
  * @author uli
  */
 public class Sudoku9 {
 
     private String alphabet = "123456789";
    private SudokuField[][] field = new SudokuField[9][];
 
     public class SudokuField {
 
         private int content = -1; //As index of the alphabet
         private boolean[] canContainElement = new boolean[alphabet.length()];
 
         /**
          * Initializes a Sudoku field with a specific content
          * @param content 
          */
         public SudokuField(int content) {
             this.content = content;
             for (int i = 0; i < canContainElement.length; i++) {
                 canContainElement[i] = false;
             }
         }
 
         /**
          * Constructs a new SudokuField instance 
          */
         public SudokuField() {
             for (int i = 0; i < canContainElement.length; i++) {
                 canContainElement[i] = true;
                
             }
         }
        
     }
 
     public Sudoku9(File file) {
         try {
             List<String> lines = FileUtils.readLines(file);
             if (lines.size() != 9) {
                 throw new IllegalArgumentException("The given file does not contain 9 lines");
             }
             for (int i = 0; i < lines.size(); i++) {
                 String currentLine = lines.get(i).trim();
                if(currentLine.length() != 9) {
                     throw new IllegalArgumentException("The line " + currentLine + " does not contain exactly 9 characters");
                 }
                 for (int j = 0; j < 9; j++) {
                     char c = currentLine.charAt(j);
                    if(c == x)
                 }
             }
         } catch (IOException ex) {
             Logger.getLogger(Sudoku9.class.getName()).log(Level.SEVERE, null, ex);
         }
 
     }
 }

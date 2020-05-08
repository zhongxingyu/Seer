 import java.util.*;
 import java.io.*;
 import java.io.FileReader;
 /**
  *
 * @author Kevin McCarthy
  */
 public class SudokuJava {
     
     private int size;
     private int[][] Matrix;
     private boolean isSolved;
        
     public SudokuJava () {
         size = 0;
         Matrix = new int[0][0];
         isSolved = false;
     }
     
     private int getSize () {
         return this.size;
     }
     
     private void setSize (int s) {
         this.size = s;
     }
     
     private int[][] getMatrix () {
         return this.Matrix;
     }
     
     private void setSolved (boolean b) {
         this.isSolved = b;
     }
     
     private boolean getSolved () {
         return this.isSolved;
     }
        
     private boolean checkRow (int row) {
         ArrayList<Integer> a = new ArrayList<> ();
         for(int i = 0; i < this.getSize(); i++){
             a.add(this.getMatrix()[row][i]);
         }
         for(int i = 1; i < this.getSize() + 1; i++){
             if (!a.contains(i)) {
                 return false;
             }
         }
         return true;
     }
        
     private boolean checkCol (int col) {
         ArrayList<Integer> a = new ArrayList<> ();
         for (int i = 0; i < this.getSize(); i++) {
                a.add(this.getMatrix()[i][col]);
         }
         for (int i = 1; i < this.getSize() + 1; i++) {
             if(!a.contains(i)) {
                 return false;
             }
         }
         return true;
     }
            
     private boolean finalCheck () {
         boolean rowStatus;
         boolean colStatus;
         for (int i = 0; i < this.getSize(); i++) {
             rowStatus = this.checkRow(i);
             colStatus = this.checkCol(i);
             if (!rowStatus || !colStatus) {
                 return false;
             }
         }
         return true;
     }
        
     private void buildPuzzle (String input, String[] inputFile) {
         ArrayList<Integer> Puzzle = new ArrayList<> ();
         for (int i = 0; i < input.length(); i++) {
             Puzzle.add(input.charAt(i) - '0');
         }
         if (Puzzle.size() == 81) {
             this.setSize(9);
         }
         else if (Puzzle.size() == 36) {
             this.setSize(6);
         }
         this.Matrix = new int[this.getSize()][this.getSize()];
         int i, j, k = 0;
         for (i = 0; i < this.getSize(); i++) {
             for (j = 0; j < this.getSize(); j++) {
                 this.getMatrix()[i][j] = Puzzle.get(k);
                 k++;
             }
         }
         System.out.println("\n"+inputFile[0]+" Output");
         this.printMatrix();
     }
        
     private void buildEmptyCells (ArrayList<EmptyCell> EmptyCells) {
         for (int i = 0; i < this.getSize(); i++) {
             for (int j = 0; j < this.getSize(); j++) {
                 if (this.getMatrix()[i][j] == 0) {
                     EmptyCell ec = new EmptyCell(i, j);
                     EmptyCells.add(ec);
                 }
             }
         }
     }
        
     private String getInput (String[] args) {
         String line ="";
         try (BufferedReader bufferReader = new BufferedReader (new FileReader (args[0]))) {
             line = bufferReader.readLine();
             bufferReader.close();
         } catch (java.io.IOException e) {}
         return line;
     }
        
     private void solve (String[] args) {
         String str = this.getInput(args);
         this.buildPuzzle(str, args);
         ArrayList<EmptyCell> EmptyCells = new ArrayList<> ();
         this.buildEmptyCells(EmptyCells);
         for (int i = 0; i < EmptyCells.size(); i++) {
             EmptyCells.get(i).getRowNums(this.getSize(), this.getMatrix());
             EmptyCells.get(i).getColNums(this.getSize(), this.getMatrix());
             EmptyCells.get(i).getBoxNums(this.getSize(), this.getMatrix());
             EmptyCells.get(i).availCell(this.getSize());
         }
         this.reck(EmptyCells, 0, this);
         if (this.getSolved()) {
             System.out.println("\n************\nSuccess!!!");
             this.printMatrix();
         }
         else {
             System.out.println("\nNOT SOLVED!!!");
         }          
     }
        
     private boolean checkRCB (ArrayList<EmptyCell> ec, int s, SudokuJava copy, int placer) {
         int index = placer;
         if (!ec.get(index).inRow(ec.get(index).getRow(), s, copy.getMatrix(), copy.getSize())) {
             if (!ec.get(index).inCol(ec.get(index).getCol(), s, copy.getMatrix(), copy.getSize())) {
                 if(!ec.get(index).inBox(ec.get(index).getBox(), s, copy.getMatrix(), copy.getSize())) {
                     return false;
                 }
             }
         }
         return true;    
     }
        
     private void printMatrix () {
         for (int i = 0; i < this.getSize(); i++) {
             for (int j = 0; j < this.getSize(); j++) {
                 System.out.print(this.getMatrix()[i][j]);
                 if( j == this.getSize() - 1) {
                     System.out.print("\n");
                 }
             }
         }
     }
        
     private void reck (ArrayList<EmptyCell> EmptyCells, int placer, SudokuJava copy) {
         int index = placer;
         for (int s : EmptyCells.get(index).getPossSol()) {
             if (!copy.getSolved()) {
                 if (!checkRCB(EmptyCells, s, copy, index)) {
                     copy.getMatrix()[EmptyCells.get(index).getCoord(0)][EmptyCells.get(index).getCoord(1)] = s;
                     if (copy.finalCheck()) {
                         copy.setSolved(true);
                         return;
                     }
                     else if (index < EmptyCells.size()-1 && !copy.getSolved()) {
                         copy.reck(EmptyCells, index+1, copy);
                     }
                 }
             }
             if (!copy.getSolved()) {
                 copy.getMatrix()[EmptyCells.get(index).getCoord(0)][EmptyCells.get(index).getCoord(1)] = 0;
             }
             else {
                 return;
             }
         }
     }    
     
     private class EmptyCell {
         
         final private int[] coord = new int[2];
         final private ArrayList<Integer> boxAvail = new ArrayList<> ();
         final private ArrayList<Integer> rowAvail = new ArrayList<> ();
         final private ArrayList<Integer> colAvail = new ArrayList<> ();
         final private ArrayList<Integer> possSol = new ArrayList<> ();
         private int row;
         private int col;
         private int box;
        
         public EmptyCell( int rowval, int colval) {
             this.coord[0] = rowval;
             this.coord[1] = colval;
             this.box = 0;
             this.row = rowval;
             this.col = colval;
         }
        
         private ArrayList<Integer> getBoxAvail () {
             return this.boxAvail;
         }
        
         private ArrayList<Integer> getRowAvail () {
             return this.rowAvail;
         }
        
         private ArrayList<Integer> getColAvail () {
             return this.colAvail;
         }
        
         private ArrayList<Integer> getPossSol () {
             return this.possSol;
         }
        
         private int getRow () {
             return this.row;
         }
        
         private int getCol () {
             return this.col;
         }
        
         private int getBox () {
             return this.box;
         }
        
         private int getCoord (int index) {
             return this.coord[index];
         }
        
         private void setBox(int i) {
             this.box = i;
         }
         
         private void getRowNums (int matrixSize, int[][] Matrix) {
             ArrayList<Integer> a = new ArrayList<> ();
             for (int i = 0; i < matrixSize; i++) {
                 a.add(Matrix[this.getRow()][i]);
             }
             for (int i = 1; i < matrixSize + 1; i++) {
                if (!a.contains(i)) {
                    this.getRowAvail().add(i);
                }
             }
         }
            
         private void getColNums (int matrixSize, int[][] Matrix) {
             ArrayList<Integer> a = new ArrayList<> ();
             for (int i = 0; i < matrixSize; i++) {
                 a.add(Matrix[i][this.getCol()]);
             }
             for (int i = 1; i < matrixSize + 1; i++) {
                 if (!a.contains(i)) {
                     this.getColAvail().add(i);
                 }
             }
         }
        
         private void getBoxNums (int matrixSize, int[][] Matrix) {
             int ro = 0;
             int co = 0;
             ArrayList<Integer> a = new ArrayList<> ();
             if (matrixSize == 6) {
                 if (this.getRow() <= 1) {
                     if (this.getCol() <= 2) {
                         ro = 0;
                         co = 0;
                         this.setBox(1);
                     }
                     else {
                         ro = 0;
                         co = 3;
                         this.setBox(2);
                     }
                 }        
                 else if (this.getRow() <= 3) {
                     if (this.getCol() <= 2) {
                         ro = 2;
                         co = 0;
                         this.setBox(3);
                     } 
                     else {
                         ro = 2;
                         co = 3;
                         this.setBox(4);
                     }
                 }
                 else if (this.getRow() <= 5) {
                     if (this.getCol() <= 2) {
                         ro = 4;
                         co = 0;
                         this.setBox(5);
                     } 
                     else {
                         ro = 4;
                         co = 3;
                         this.setBox(6);
                     }
                 }
                 for (int i = ro; i < ro + 2; i++) {
                     for(int j = co; j < co + 3; j++) {
                         a.add(Matrix[i][j]);
                     }
                 }
             }
             if (matrixSize == 9) {
                 if(this.getRow() <= 2) {
                     if(this.getCol() <= 2) {
                         ro = 0;
                         co = 0;
                         this.setBox(1);
                     }
                     else if (this.getCol() > 2 && this.getCol() <= 5) {
                         ro = 0;
                         co = 3;
                         this.setBox(2);
                     }
                     else if (this.getCol() > 5 && this.getCol() <= 8) {
                         ro = 0;
                         co = 6;
                         this.setBox(3);
                     }
                 }
                 else if (this.getRow() > 2 && this.getRow() <= 5) {
                     if (this.getCol() <= 2) {
                         ro = 3;
                         co = 0;
                         this.setBox(4);
                     }
                     else if (this.getCol() > 2 && this.getCol() <= 5) {
                         ro = 3;
                         co = 3;
                         this.setBox(5);
                     }
                     else if (this.getCol() > 5 && this.getCol() <=8) {
                         ro = 3;
                         co = 6;
                         this.setBox(6);
                     }
                 }
                 else if (this.getRow() > 5 && this.getRow() <= 8) {
                     if (this.getCol() <= 2) {
                         ro = 6;
                         co = 0;
                         this.setBox(7);
                     }
                     else if (this.getCol() > 2 && this.getCol() <= 5) {
                         ro = 6;
                         co = 3;
                         this.setBox(8);
                     }
                     else if (this.getCol() > 5 && this.getCol() <= 8) {
                         ro = 6;
                         co = 6;
                         this.setBox(9);
                     }
                 }
                 for (int i = ro; i < ro + 3; i++) {
                     for (int j = co; j < co + 3; j++) {
                         a.add(Matrix[i][j]);
                     }
                 }
             }
             for(int i = 1; i < matrixSize + 1; i++) {
                 if (!a.contains(i)) {
                     this.boxAvail.add(i);
                 }
             }
         }
        
         private void availCell (int matrixSize) {
             for (int i = 1; i < matrixSize + 1; i++) {
                 if (this.getBoxAvail().contains(i) && this.getColAvail().contains(i) && this.getRowAvail().contains(i)) {
                     this.possSol.add(i);
                 }
             }
         }
    
         private boolean inRow (int row, int val, int[][] Matrix, int matrixSize) {
             for(int i = 0; i < matrixSize; i++) {
                 if(val == Matrix[row][i]) {
                     return true;
                 }
             }
             return false;
         }
    
         private boolean inCol (int col, int val, int[][] Matrix, int matrixSize) {
             for (int i = 0; i < matrixSize; i++) {
                 if (val == Matrix[i][col]) {
                     return true;
                 }
             }
             return false;
         }
    
         private boolean inBox (int box, int val, int[][] Matrix, int matrixSize) {
             int boxRow = 0;
             int boxCol = 0;
             if (matrixSize == 6) {
                 if(box == 2) {
                     boxRow = 0;
                     boxCol = 3;
                 }
                 if (box == 3) {
                     boxRow = 2;
                     boxCol = 0;
                 }
                 if (box == 4) {
                     boxRow = 2;
                     boxCol = 3;
                 }
                 if (box == 5) {
                     boxRow = 4;
                     boxCol = 0;
                 }
                 if (box == 6) {
                     boxRow = 4;
                     boxCol = 3;
                 }
                 for (int i = boxRow; i < boxRow + 2; i++) {
                    for (int j = boxCol; j < boxCol + 2; j++) {
                         if  (Matrix[i][j] == val) {
                             return true;
                         }
                     }
                 }
                 return false;
             }
             else if (matrixSize == 9) {
                 if (box == 2) {
                     boxRow = 0;
                     boxCol = 3;
                 }
                 if (box == 3) {
                     boxRow = 0;
                     boxCol = 6;
                 }
                 if (box == 4) {
                     boxRow = 3;
                     boxCol = 0;
                 }
                 if (box == 5) {
                     boxRow = 3;
                     boxCol = 3;
                 }
                 if (box == 6) {
                     boxRow = 3;
                     boxCol = 6;
                 }
                 if (box == 7) {
                     boxRow = 6;
                     boxCol = 0;
                 }
                 if (box == 8) {
                     boxRow = 6;
                     boxCol = 3;
                 }
                 if (box == 9) {
                     boxRow = 6;
                     boxCol = 6;
                 }
                 for (int i = boxRow; i < boxRow + 3; i++) {
                     for (int j = boxCol; j < boxCol + 3; j++) {
                         if (Matrix[i][j] == val){
                             return true;
                         }
                     }
                 }
                 return false;
             }
             return false;
         }
     }
       
     /**
     * @param args the command line arguments
     */
     public static void main (String[] args) {
         SudokuJava java = new SudokuJava ();
         java.solve(args);
     }
 }

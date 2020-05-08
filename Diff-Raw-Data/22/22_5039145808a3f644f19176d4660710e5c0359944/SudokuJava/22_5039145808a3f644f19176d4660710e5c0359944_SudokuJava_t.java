 import java.util.*;
 import java.io.*;
 import java.io.FileReader;
 /**
  *
  * @author Kevin McCarthy
  */
 public class SudokuJava {
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
         SudokuJava test = new SudokuJava();
         test.solve(args);
     }
     
    public class EmptyCell{
        private int[] coord = new int[2];
        private ArrayList<Integer> boxAvail = new ArrayList();
        private ArrayList<Integer> rowAvail = new ArrayList();
        private ArrayList<Integer> colAvail = new ArrayList();
        private ArrayList<Integer> possSol = new ArrayList();
        private int row;
        private int col;
        private int box;
        
        public EmptyCell( int rowval, int colval){
            this.coord[0]=rowval;
            this.coord[1]=colval;
            this.box = 0;
            this.row = rowval;
            this.col = colval;
        }
        
        private void getRowNums(int matrixSize, int[][] Matrix){
            ArrayList<Integer> a = new ArrayList();
            for(int i=0; i<matrixSize; i++)
                a.add(Matrix[this.row][i]);
            for(int i=1; i<matrixSize+1; i++){
                if(!a.contains(i))
                    this.rowAvail.add(i);
            }
        }
            
        private void getColNums(int matrixSize, int[][] Matrix){
            ArrayList<Integer> a = new ArrayList();
            for(int i=0; i<matrixSize; i++)
                a.add(Matrix[i][this.col]);
            for(int i=1; i<matrixSize+1; i++){
                if(!a.contains(i))
                    this.colAvail.add(i);
             }
        }
        
        private void getBoxNums(int matrixSize, int[][] Matrix){
            int row = 0;
            int col = 0;
            ArrayList<Integer> a = new ArrayList();
            if (matrixSize == 6){
                if(this.row <= 1){
                    if(this.col <= 2){
                        row = 0;
                        col = 0;
                        this.box = 1;
                    }else{
                        row = 0;
                        col = 3;
                        this.box = 2;}
                }        
                else if(this.row <=3){
                    if(this.col <= 2){
                        row = 2;
                        col = 0;
                        this.box = 3;
                    }else{
                        row = 2;
                        col = 3;
                        this.box = 4;
                    }
                }
                else if(this.row <= 5){
                    if(this.col <= 2){
                        row = 4;
                        col = 0;
                        this.box = 5;
                    }else{
                        row = 4;
                        col = 3;
                        this.box = 6;
                    }
                }
                
            for(int i=row; i<row+2; i++){
                for(int j=col; j<col+3; j++){
                    a.add(Matrix[i][j]);
            }
            }}
            if(matrixSize == 9){
                if(this.row <=2){
                    if(this.col <= 2){
                        row = 0;
                        col = 0;
                        this.box = 1;
                    }
                    else if(this.col > 2 && this.col <= 5){
                        row = 0;
                        col = 3;
                        this.box = 2;
                    }
                    else if(this.col > 5 && this.col <= 8){
                        row = 0;
                        col = 6;
                        this.box = 3;
                    }
                }
                else if(this.row > 2 && this.row <= 5){
                    if(this.col <= 2){
                        row = 3;
                        col = 0;
                        this.box = 4;
                    }
                    else if(this.col > 2 && this.col <= 5){
                        row = 3;
                        col = 3;
                        this.box = 5;
                    }
                    else if(this.col > 5 && this.col <=8){
                        row = 3;
                        col = 6;
                        this.box = 6;
                    }
                }
                else if(this.row > 5 && this.row <= 8){
                    if(this.col <= 2){
                        row = 6;
                        col = 0;
                        this.box = 7;
                    }
                    else if(this.col > 2 && this.col <= 5){
                        row = 6;
                        col = 3;
                        this.box = 8;
                    }
                    else if(this.col > 5 && this.col <= 8){
                        row = 6;
                        col = 6;
                        this.box = 9;
                    }
                }
            for(int i=row; i<row+3; i++){
                for(int j=col; j<col+3; j++)
                    a.add(Matrix[i][j]);
                }
        }
        for(int i=1; i < matrixSize+1; i++){
            if(!a.contains(i))
                this.boxAvail.add(i);
        }
    }
        
    private void availCell(int matrixSize){
        for(int i = 1; i < matrixSize+1; i++){
            if(this.boxAvail.contains(i) && this.colAvail.contains(i) && this.rowAvail.contains(i))
                this.possSol.add(i);
        }
    }
    
    private boolean inRow(int row, int val, int[][] Matrix, int matrixSize){
        for(int i=0; i<matrixSize; i++){
            if(val == Matrix[row][i])
                return true;
        }
        return false;
    }
    
    private boolean inCol(int col, int val, int[][] Matrix, int matrixSize){
        for(int i=0; i<matrixSize; i++){
            if(val == Matrix[i][col])
                return true;
        }
        return false;
    }
    
    private boolean inBox(int box, int val, int[][] Matrix, int matrixSize){
        int boxRow = 0;
        int boxCol = 0;
        if(matrixSize == 6){
            if(box == 2){
                boxRow = 0;
                boxCol = 3;
            }
            if(box == 3){
                boxRow = 2;
                boxCol = 0;
            }
            if(box == 4){
                boxRow = 2;
                boxCol = 3;
            }
            if(box == 5){
                boxRow = 4;
                boxCol = 0;
            }
            if(box == 6){
                boxRow = 4;
                boxCol = 3;
            }
            for(int i = boxRow; i < boxRow+2; i++){
                for(int j = boxCol; j < boxCol+2; j++){
                    if(Matrix[i][j] == val)
                        return true;
                }
            }
            return false;
        }
        else if(matrixSize == 9){
            if(box == 2){
                boxRow = 0;
                boxCol = 3;
            }
            if(box == 3){
                boxRow = 0;
                boxCol = 6;
            }
            if(box == 4){
                boxRow = 3;
                boxCol = 0;
            }
            if(box == 5){
                boxRow = 3;
                boxCol = 3;
            }
            if(box == 6){
                boxRow = 3;
                boxCol = 6;
            }
            if(box == 7){
                boxRow = 6;
                boxCol = 0;
            }
            if(box == 8){
                boxRow = 6;
                boxCol = 3;
            }
            if(box == 9){
                boxRow = 6;
                boxCol = 6;
            }
            for(int i = boxRow; i < boxRow+3; i++){
                for(int j = boxCol; j < boxCol+3; j++){
                    if(Matrix[i][j] == val)
                        return true;
                }
            }
            return false;
        }
        return false;
     }
    }
    
        private int size;
        private int[][] Matrix;
        private boolean isSolved;
        
        public SudokuJava(){}
        
        private boolean checkRow(int row, int[][] Matrix){
            ArrayList<Integer> a = new ArrayList();
            for(int i = 0; i < this.size; i++){
             a.add(Matrix[row][i]);
            }
           for(int i = 1; i < this.size+1; i++){
                if(!a.contains(i))
                 return false;
            }
            return true;
        }
        
        private boolean checkCol(int col, int[][] Matrix){
            ArrayList<Integer> a = new ArrayList();
            for(int i = 0; i < this.size; i++)
                a.add(Matrix[i][col]);
           for(int i = 1; i < this.size+1; i++){
               if(!a.contains(i))
                    return false;
                   }
            return true;}
        
        
        private boolean finalCheck(int [][] Matrix){
            boolean rowStatus = false;
            boolean colStatus = false;
            for(int i = 0; i < this.size; i++){
                rowStatus = this.checkRow(i, Matrix);
                colStatus = this.checkCol(i, Matrix);
                if(!rowStatus && !colStatus)
                    return false;
            }
            if(rowStatus && colStatus)
                return true;
            else
                return false;
        }
        
        private int[][] buildPuzzle(String input){
             ArrayList<Integer> Puzzle = new ArrayList();
             for(int i=0; i<input.length(); i++)
                 Puzzle.add(input.charAt(i)-'0');
             if (Puzzle.size()==81)
                 this.size = 9;
             else if(Puzzle.size()==36)
                 this.size = 6;
             int[][] Matrix = new int[this.size][this.size];
             int i=0;
             int j=0;
             int k=0;
             for(i=0; i<this.size; i++){
                 for(j=0; j<this.size; j++){
                     Matrix[i][j] = (int)Puzzle.get(k);
                     k++;}}
             for(i=0; i<this.size; i++){
                 for(j=0; j<this.size; j++){
                     System.out.print(Matrix[i][j]);
                     if(j==this.size-1)
                         System.out.print("\n");
                 }
             }
             return Matrix;
        }
        
        private void buildEmptyCells(ArrayList EmptyCells, int matrixSize, int[][] Matrix){
            for(int i = 0; i < matrixSize; i++){
                for(int j = 0; j < matrixSize; j++){
                    if(Matrix[i][j] == 0){
                        EmptyCell ec = new EmptyCell(i, j);
                        EmptyCells.add(ec);
                    }
                }
            }
        }
        
        private String getInput(String[] args){
             String line ="";
             try{
             FileReader inputFile = new FileReader(args[0]);
             BufferedReader bufferReader = new BufferedReader(inputFile);
             while ((line = bufferReader.readLine()) != null)   {
                     break;
             }
             bufferReader.close();
             }catch(Exception e){}
             return line;
        }
        
        private void solve(String[] args){
            String str = this.getInput(args);
            int[][] Matrix = this.buildPuzzle(str);
            ArrayList<EmptyCell> EmptyCells = new ArrayList();
            this.buildEmptyCells(EmptyCells, this.size, Matrix);
            for(int i = 0; i <EmptyCells.size(); i++){
                EmptyCells.get(i).getRowNums(this.size, Matrix);
                EmptyCells.get(i).getColNums(this.size, Matrix);
                EmptyCells.get(i).getBoxNums(this.size, Matrix);
                EmptyCells.get(i).availCell(this.size);
            }
            this.reck(EmptyCells, 0, this, Matrix);
            if(this.isSolved){
                System.out.println("\n************\nSuccess!!!");
                for(int i=0; i<this.size; i++){
                 for(int j=0; j<this.size; j++){
                     System.out.print(Matrix[i][j]);
                     if(j==this.size-1)
                         System.out.print("\n");}}
            }
            else
                System.out.println("\nNOT SOLVED!!!");
                   
        }
        
        private boolean checkRCB(ArrayList<EmptyCell> ec, int s, int[][] Matrix, SudokuJava copy, int placer){
            int index = placer;
            if(!ec.get(index).inRow(ec.get(index).row, s, Matrix, copy.size)){
                if(!ec.get(index).inCol(ec.get(index).col, s, Matrix, copy.size)){
                    if(!ec.get(index).inBox(ec.get(index).box, s, Matrix, copy.size)){
                        return false;
                    }
                }
            }
            return true;    
        }
       
       private void printMatrix(int[][] Matrix){
            System.out.println("MATRIX PRINTOUT");
            for(int i=0; i<this.size; i++){
                for(int j=0; j<this.size; j++){
                    System.out.print(Matrix[i][j]);
                    if(j==this.size-1)
                        System.out.print("\n");}}
            System.out.println("MATRIX PRINTOUT");
       }
       
        private void reck(ArrayList<EmptyCell> EmptyCells, int placer, SudokuJava copy, int[][] Matrix){
            int index = placer;
            for(int s : EmptyCells.get(index).possSol){
                if(!copy.isSolved){
                    if(!checkRCB(EmptyCells, s, Matrix, copy, placer)){
                         Matrix[EmptyCells.get(index).coord[0]][EmptyCells.get(index).coord[1]] = s;
                         if(copy.finalCheck(Matrix)){
                             copy.isSolved = true;
                             copy.Matrix = Matrix;
                         }
                         else if(index < EmptyCells.size()-1 && !copy.isSolved)
                             copy.reck(EmptyCells, index+1, copy, Matrix);
                    }
                   }
                   if(!copy.isSolved)
                       Matrix[EmptyCells.get(index).coord[0]][EmptyCells.get(index).coord[1]] = 0;
                   else
                       return;
            }
       }
   }

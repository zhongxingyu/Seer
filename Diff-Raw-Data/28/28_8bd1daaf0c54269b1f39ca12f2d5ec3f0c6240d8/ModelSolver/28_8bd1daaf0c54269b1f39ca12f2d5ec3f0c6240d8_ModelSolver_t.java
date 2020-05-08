 
 public class ModelSolver {
    Model sudokuModel;
     ModelGrid currentGrid;
     ModelGrid answerGrid;
 
    public ModelSolver(Model sudokuModel, ModelGrid currentGrid, ModelGrid answerGrid) {
         this.sudokuModel = sudokuModel;
         this.currentGrid = currentGrid;
         this.answerGrid = answerGrid;
     }
     
     public boolean findSolution() {
         int i = 0, j = 0;
         boolean finished = true;
         while(finished) {
             int potential = answerGrid.getCell(i,j).getNumber() + 1;
             // If exhausted all potential numbers, set cell to 0 then backtrack 
             if(potential > 9) {
                 sudokuModel.removeCellNumber(i, j);
                 boolean backtracked = false;
                 // Backtracks to the previous ungiven cell.
                 while(!backtracked || answerGrid.getCell(i, j).isGiven()) {
                     // Make this better later
                     i = i + (j-9)/9;
                     j--;
                     // Backtracked past the start of the grid (can't solve every cell)
                     if(i<0) {
                         return false;
                     }
                     backtracked = true;
                 }
             // Else, set potential to cell.
             } else {
                 answerGrid.getCell(i, j).giveNumber(potential);
                 boolean valid = answerGrid.isCellValid(i,j);
                 // If valid, advances to the next ungiven cell.
                 if(valid) {
                     boolean advanced = false;
                     while(!advanced || answerGrid.getCell(i, j).isGiven()) {
                         i = i + j/8;
                         j = (j+1)%9;
                         advanced = true;
                         // Advanced past the end of the grid (found solution)
                         if (i>8) {
                             return true;
                         }
                     }
                 }
             }
         }
         return false;
     }
 }

 package fr.uha.ensisa.brundubreil.model;
 
 public class Grid
 {
     public static final int WIDTH = 7;
     public static final int HEIGHT = 6;
     private static final int NBR_OF_CONSECUTIVES_TO_WIN = 4;
     
     private Cell[][] cells;
     private boolean[] columnFull;
     private boolean gridFull;
 
     public Grid()
     {
         this.cells = new Cell[HEIGHT][WIDTH];
         
         for (int i = 0; i < HEIGHT; i++)
             for (int j = 0; j < WIDTH; j++)
                 this.cells[i][j] = new Cell();
         
         this.columnFull = new boolean[WIDTH];
         for (int i = 0; i < WIDTH; i++)
             this.columnFull[i] = false;
         
         this.gridFull = false;
     }
     
     public Cell getCell(int row, int column)
     {
         return this.cells[row][column];
     }
 
     public void setCellState(int row, int column, CellState state)
     {
         this.cells[row][column].setState(state);
     }
     
     public Cell getFirstNonEmptyCellFromColumn(int column)
     {
        for (int i = 5; i >= 0; i--)
             if (this.cells[i][column].getState() == CellState.EMPTY)
                 return this.cells[i][column];
         
         this.columnFull[column] = true;
         return null;
     }
     
     private void updateGridFlag()
     {
         boolean tmp = true;
         for (int i = 0; i < WIDTH; i++)
         {
             getFirstNonEmptyCellFromColumn(i);
             tmp &= this.columnFull[i];
         }
         
         this.gridFull = tmp;
     }
 
     public boolean isGridFull()
     {
         updateGridFlag();
         return gridFull;
     }
 
     public boolean isHumanPlayerWinner()
     {
         return detectWinner(CellState.HUMAN_PLAYER);
     }
 
     public boolean isComputerWinner()
     {
         return detectWinner(CellState.COMPUTER);
     }
     
     private boolean detectWinner(CellState stateToDetect)
     {
         int consecutive = 0;
         
         // Vertical
         for (int i = 0; i < WIDTH; i++)
         {
             consecutive = 0;
             for (int j = 0; j < HEIGHT; j++)
             {
                 if (this.cells[j][i].getState() == stateToDetect)
                     consecutive++;
                 else
                     consecutive = 0;
                 
                 if (consecutive >= NBR_OF_CONSECUTIVES_TO_WIN)
                     return true;
             }
         }
         
         // Horizontal
         for (int i = 0; i < HEIGHT; i++)
         {
             consecutive = 0;
             for (int j = 0; j < WIDTH; j++)
             {
                 if (this.cells[i][j].getState() == stateToDetect)
                     consecutive++;
                 else
                     consecutive = 0;
                 
                 if (consecutive >= NBR_OF_CONSECUTIVES_TO_WIN)
                     return true;
             }
         }
         
         // Right diagonal
         int nbrOfDiag = Grid.WIDTH + Grid.HEIGHT - 1;
         int currentNbrOfCells = 1;
         int maxNbrOfCells = Math.min(Grid.WIDTH, Grid.HEIGHT);
         int nbrOfIncompleteDiag = 0;
         for (int currentDiag = 0; currentDiag < nbrOfDiag; currentDiag++)
         {
             if (currentNbrOfCells >= NBR_OF_CONSECUTIVES_TO_WIN)
             {
                 consecutive = 0;
                 int k = Math.min(currentDiag, Grid.HEIGHT - 1);
                 int l = (currentDiag >= Grid.HEIGHT ? currentDiag - Grid.HEIGHT + 1 : 0);
                 for (int j = 0; j < currentNbrOfCells; j++)
                 {
                     if (this.cells[k][l].getState() == stateToDetect)
                         consecutive++;
                     else
                         consecutive = 0;
 
                     if (consecutive >= NBR_OF_CONSECUTIVES_TO_WIN)
                         return true;
                     
                     // Update indexes
                     k--;
                     l++;
                 }
             }
             
             if (nbrOfIncompleteDiag == 0 && currentNbrOfCells < maxNbrOfCells)
                 currentNbrOfCells++;
             
             if (nbrOfIncompleteDiag == 0 && currentNbrOfCells == maxNbrOfCells)
                 nbrOfIncompleteDiag = currentDiag;
             
             if (nbrOfIncompleteDiag != 0 && currentDiag + 1 >= nbrOfDiag - nbrOfIncompleteDiag - 1)
                 currentNbrOfCells--;
         }
         
         // Left diagonal
         nbrOfDiag = Grid.WIDTH + Grid.HEIGHT - 1;
         currentNbrOfCells = 1;
         maxNbrOfCells = Math.min(Grid.WIDTH, Grid.HEIGHT);
         nbrOfIncompleteDiag = 0;
         for (int currentDiag = 0; currentDiag < nbrOfDiag; currentDiag++)
         {
             if (currentNbrOfCells >= NBR_OF_CONSECUTIVES_TO_WIN)
             {
                 consecutive = 0;
                 int k = (currentDiag < Grid.HEIGHT ? Grid.HEIGHT - 1 - currentDiag : 0);
                 int l = (currentDiag <= Grid.HEIGHT ? 0 : currentDiag - Grid.HEIGHT + 1);
                 for (int j = 0; j < currentNbrOfCells; j++)
                 {
                     if (this.cells[k][l].getState() == stateToDetect)
                         consecutive++;
                     else
                         consecutive = 0;
 
                     if (consecutive >= NBR_OF_CONSECUTIVES_TO_WIN)
                         return true;
                     
                     // Update indexes
                     k++;
                     l++;
                 }
             }
             
             if (nbrOfIncompleteDiag == 0 && currentNbrOfCells < maxNbrOfCells)
                 currentNbrOfCells++;
             
             if (nbrOfIncompleteDiag == 0 && currentNbrOfCells == maxNbrOfCells)
                 nbrOfIncompleteDiag = currentDiag;
             
             if (nbrOfIncompleteDiag != 0 && currentDiag + 1 >= nbrOfDiag - nbrOfIncompleteDiag - 1)
                 currentNbrOfCells--;
         }
         
         return false;
     }
 }

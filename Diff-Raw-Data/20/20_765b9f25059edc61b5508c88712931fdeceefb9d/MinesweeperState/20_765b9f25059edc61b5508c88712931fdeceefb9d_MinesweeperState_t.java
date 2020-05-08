 package minesweeper.core;
 import minesweeper.core.support.XYLocation;
 import minesweeper.core.tool.*;
 
 public class MinesweeperState {
     public int tableWidth;
     public int tableHeight;
     private MinesweeperCell[][] table;
     //private MinesweeperHideMask minesweeperHideMask;
     
     public MinesweeperState mineSweeperState(MinesweeperState state){
         MinesweeperState newState;
         newState = new MinesweeperState(state.getRawTable()[0].length,state.getRawTable().length);
         return newState;        
     }
     
     public  MinesweeperState(int width, int height) {
         //victory = false;
         tableWidth = width;
         tableHeight = height;
         table = new MinesweeperCell[height][width];
         for (int i = 0; i < height; i++)
             for (int j = 0; j < width; j++) table[i][j] = new MinesweeperCell();
     }
    
     private MinesweeperState(int width, int height,int numberOfMines) {
         this(width,height);
         //minesweeperHideMask = new MinesweeperHideMask(table);
         table = MinesweeperMinePutter.putMines(table, numberOfMines);
         table = MinesweeperNumberPutter.putNumbers(table);
     }
     public MinesweeperState(int[] initialValuesArray){
         this(initialValuesArray[0],initialValuesArray[1],initialValuesArray[2]);
     }
 
     public MinesweeperState() {
         this(8,8);
     }
 
     public char[][] getRawTable() {
         char[][] tableCopy = new char[table.length][table[0].length];
         for (int i = 0; i < table.length; i++)
             for (int j = 0; j < table[i].length; j++)
                 tableCopy[i][j] = table[i][j].getValue();
         return tableCopy;
     }
     
     public MinesweeperCell[][] getTable() {
         /*MinesweeperCell[][] tableCopy = new MinesweeperCell[table.length][table[0].length];
         for (int i = 0; i < table.length; i++)
             for (int j = 0; j < table[i].length; j++)
                 tableCopy[i][j].setValue(table[i][j].getValue());
         return tableCopy;*/
         return table;
     }
   
     public int getWidth(){
         return table[0].length;
     }
     
     public int getHeight(){
         return table.length;
     }
     
     public void makeMove(XYLocation move) {
         if(move.getY()<table.length)
         if(move.getX()<table[0].length)
         if(table[move.getY()][move.getX()].getValue()=='#'
                 ||table[move.getY()][move.getX()].getValue()=='0'){
                 //||table[move.getY()][move.getX()].getValue()=='#'){
             footPrint(move);
         }
         else if(table[move.getY()][move.getX()].getValue()=='M')
             die(move);
         else{
             //minesweeperHideMask.unHide(move);
             table[move.getY()][move.getX()].setVisibility(true);
         }
     }
     
     private void footPrint(XYLocation move) {
         //MinesweeperRecursiveUnhider minesweeperRecursiveUnhider = new MinesweeperRecursiveUnhider(this);
         table = MinesweeperRecursiveUnhider.doRecursiveUnhiding(table,move);     
         //minesweeperHideMask.unHide(minesweeperRecursiveUnhider.getUnhidenLocations());
         /*for (int i = 0; i < table.length; i++) {
             for (int j = 0; j < table[i].length; j++) {
                 if((table[i][j]==' ')&&minesweeperHideMask.isShown(i,j)){
                     table[i][j]='#';
                 }
             }
         }*/
         //minesweeperHideMask.unHide(move);
     }
     
     private void die(XYLocation move) { 
         table[move.getY()][move.getX()].setValue('D');
         table[move.getY()][move.getX()].setVisibility(true);
     }
 /*
     public MinesweeperCell[][] getHidenTable() {
         MinesweeperCell[][] hidenTable = getTable();
         for (int i = 0; i < hidenTable.length; i++)
             for (int j = 0; j < hidenTable[i].length; j++)
                 if(hideTable[i][j])hidenTable[i][j]=' ';      
         return hidenTable;
     }*/
 
     public char[] getRawRow(int rowNumber) {
       char[] rowValues = new char[tableWidth];
       for(int i=0;i<tableWidth;i++){
           rowValues[i] = table[rowNumber][i].getValue();
       }
       return rowValues;
     }
     
     public char[] getFixedRow(int rowNumber) {
       char[] rowValues = new char[tableWidth];
       for(int i=0;i<tableWidth;i++){
           if(table[rowNumber][i].getVisibility())
           rowValues[i] = table[rowNumber][i].getValue();
           else
           rowValues[i] = ' ';
       }
       return rowValues;
     }
     
     public char[] getMinedRow(int rowNumber) {
       char[] rowValues = new char[tableWidth];
      for(int i=0;i<tableHeight;i++){
           if(table[rowNumber][i].getValue()=='M')
           rowValues[i] = 'M';
           else if(table[rowNumber][i].getValue()=='D')
           rowValues[i] = 'D';
           else
           rowValues[i] = ' ';
       }
       return rowValues;
     }
 
 
 }
 
             
     
 
 

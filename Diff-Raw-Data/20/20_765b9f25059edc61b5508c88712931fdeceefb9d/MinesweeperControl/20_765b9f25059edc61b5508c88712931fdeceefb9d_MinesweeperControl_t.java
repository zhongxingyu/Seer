 package minesweeper.control;
 import minesweeper.core.*;
 import minesweeper.core.support.XYLocation;
 import minesweeper.view.ptext.*;
 
 public class MinesweeperControl {
     //private MinesweeperState minesweeperState;
     private MinesweeperViewer minesweeperViewer;
     private MinesweeperGameDialog minesweeperGameDialog;
     private MinesweeperPrivilegedViewer minesweeperPrivilegedViewer;
     private MinesweeperGame minesweeperGame;
     private final static boolean DEV = true;
     
     public MinesweeperControl() {
     }
     
     public void execute(){
        //minesweeperState = new MinesweeperState(getInitialValues());
        minesweeperGame = new MinesweeperGame(getInitialValues());
        minesweeperGameDialog = new MinesweeperGameDialog();
        //minesweeperViewer = new MinesweeperViewer(minesweeperState);
        minesweeperViewer = new MinesweeperViewer(minesweeperGame);
        if(DEV)minesweeperPrivilegedViewer = new MinesweeperPrivilegedViewer(minesweeperGame);
        if(DEV)minesweeperPrivilegedViewer.show();
        minesweeperViewer.show();
        //while(!minesweeperState.isTerminal())
        while(!minesweeperGame.isTerminal())
           play();
        if(minesweeperGame.hasWon())System.out.println("You won");
        else{
            minesweeperViewer.showMines();
            System.out.println("You lost");
        }
     }
 
     private int[] getInitialValues() {        
        MinesweeperInitializeDialog minesweeperInitializeDialog = new MinesweeperInitializeDialog();
        minesweeperInitializeDialog.execute();
        return minesweeperInitializeDialog.getValues();
     }
     
     private void play(){
        XYLocation move = minesweeperGameDialog.askMove();
         //minesweeperState.makeMove(move);
         minesweeperGame.makeMove(move);
         if(DEV)minesweeperPrivilegedViewer.refresh(minesweeperGame);
         minesweeperViewer.refresh(minesweeperGame);
         if(DEV)minesweeperPrivilegedViewer.show();
         minesweeperViewer.show();
     }
 
     private void anotherGo() {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
     
     
 }

 package minesweeper.view.ptext;
 
 import java.util.Arrays;
 import minesweeper.core.*;

 public class MinesweeperPrivilegedViewer {
     
     private MinesweeperCell[][] table;
     private MinesweeperState state;
 
     public MinesweeperPrivilegedViewer(MinesweeperCell[][] table){
         this.table=table;
     }
     public MinesweeperPrivilegedViewer(MinesweeperState state) {
         this.state = state;
     }    
     public MinesweeperPrivilegedViewer(MinesweeperGame game) {
         this(game.getState());
     }
     
     public void show(){
         String string = "   ";
         for (int i = 0; i < state.tableWidth; i++) {
             string += i+", ";
         }
         for (int i = 0; i < state.tableHeight; i++) {
             string += "\n"+i+" "+Arrays.toString(state.getRawRow(i));
         }
         System.out.println(string);
     }
     
     public void refresh(MinesweeperState state){
         this.table = state.getTable();
     }
 
     public void refresh(MinesweeperGame game) {
         refresh(game.getState());
     }
 }

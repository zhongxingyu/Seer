 package edu.ycp.cs481.ycpgames;
 
 import java.util.Set;
 
 /**
  * Created by brian on 10/4/13.
  */
 public class TicTacToeAI extends Player {
     private int difficulty;
     public TicTacToeAI(int num){
         super(num);
         difficulty = settings.getDifficulty();//needs to become settings.getDifficulty or something like that
     }
 
     public int[] makeMove(int [][] grid){
         int[] move = {0,0};
 
         switch (difficulty){
             case 0://difficulty low
                 break;
             case 1://difficulty medium
                 break;
             case 2://difficulty high
                 break;
             default://error
                 break;
         }
 
 
         return move;
     }
 }

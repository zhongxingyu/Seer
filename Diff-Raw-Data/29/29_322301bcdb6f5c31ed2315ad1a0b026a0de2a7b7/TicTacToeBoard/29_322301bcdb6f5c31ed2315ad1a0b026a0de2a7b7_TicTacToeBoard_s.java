 package edu.ycp.cs481.ycpgames;
 
 /**
  * Created by brian on 10/4/13.
  */
 public class TicTacToeBoard extends Board {
 
     //playerOne/TwoPieces will be used to count how many pieces each player has placed
     //this will help make isGameOver() faster in the early game
     private int playerOnePieces, playerTwoPieces;
     public TicTacToeBoard(){
         /*
          * im going to standardize the board layout here
          * format of coordinates is (x,y)
          *      #     #
          * (0,2)#(1,2)#(2,2)
          * #################
          * (0,1)#(1,1)#(2,1)
          * #################
          * (0,0)#(1,0)#(2,0)
          *      #     #
          */
 
         for(int x = 0; x<3; x++){
             for(int y = 0; y<3; y++){
                 grid[x][y] = 0;
             }
         }
         playerOnePieces = 0;
         playerTwoPieces = 0;
     }
     /*
      *This resets the board
      * ...obvious right?
      */
     @Override
     public void reset(){
         for(int x = 0; x<3; x++){
             for(int y = 0; y<3; y++){
                 grid[x][y] = 0;
             }
         }
         playerOnePieces = 0;
         playerTwoPieces = 0;
     }
 
     /*
      * overrides the placePiece method, takes a location and an int representing the player
      *
      */
     @Override
     public void placePiece(int x, int y, int player){
         grid[x][y] = player;
         if(player == 1){
             playerOnePieces++;
         }else{
             playerTwoPieces++;
         }
     }
     /*
      * overrides the checkSpace method, takes a location and returns true if it is a valid move
      */
     @Override
     public boolean checkSpace(int x, int y){
         if(grid[x][y] == 0){
             return true;
         }else{
             return false;
         }
     }
     /*
      * overrides the isGameOver method, recursively checks game board for victory
      * returns 0 for game still in progress, -1 if draw, otherwise player number of victor
      */
     @Override
     public int isGameOver(){
         int player;
         if((playerOnePieces<3)&&(playerTwoPieces<3)){
             //if there aren't enough pieces for a victory then return 0
             return 0;
         }
 
         if((playerOnePieces + playerTwoPieces) >=9){
             //if game is a draw return -1
             return -1;
         }
 
         //look for vertical victories
         for(int x = 0; x < 3; x++){
             player = checkForWin("up", x, 0);
             if (player != 0){
                 return player;
             }
         }
         //look for horizontal victories
         for(int y = 0; y <3; y++){
             player = checkForWin("right", 0, y);
             if (player != 0){
                 return player;
             }
         }
         //look for diagonal victories
         player = checkForWin("dUP",0,0);
         if(player != 0){
             return player;
         }
         player = checkForWin("dDOWN",0,2);
         if(player != 0){
             return player;
         }
 
         return 0;
     }
 
     @Override
     public int getPieceAt(int x, int y){
         return grid[x][y];
     }
 
     /*
      * This method will recursively check a row/column/diagonal for a win
      * direction checked is dependent on flag
      * valid flags are "up" "right" "dUP" and "dDOWN"
      */
     private int checkForWin(String flag, int x, int y){
         int player;
         if(flag.equals("up")){
             if(y == 2){
                 return grid[x][y];
             }else{
                 player = checkForWin(flag, x, y+1);
                 if(player == grid[x][y]){
                     return player;
                 }else{
                     return 0;
                 }
             }
         }
         if(flag.equals("right")){
             if (x==2){
                 return grid[x][y];
             }else{
                 player = checkForWin(flag, x+1, y);
                 if(player == grid[x][y]){
                     return player;
                 }else{
                     return 0;
                 }
             }
         }
         if(flag.equals("dUP")){
             if (x==2){
                 return grid[x][y];
             }else{
                 player = checkForWin(flag, x+1, y+1);
                 if(player == grid[x][y]){
                     return player;
                 }else{
                     return 0;
                 }
             }
         }if (flag.equals("dDOWN")){
             if (x==2){
                 return grid[x][y];
             }else{
                 player = checkForWin(flag, x+1,y-1);
                 if(player == grid[x][y]){
                     return player;
                 }else{
                     return 0;
                 }
             }
         }
         return 0;
     }
 }

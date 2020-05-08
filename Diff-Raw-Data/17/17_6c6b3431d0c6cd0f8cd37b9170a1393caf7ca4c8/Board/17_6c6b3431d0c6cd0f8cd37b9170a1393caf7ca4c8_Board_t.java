 /* Board.java */
 
 package machine;
 
 import player.*;
 
 public class Board{
 
   private Chip[][] board = new Chip[8][8]; 
   private Move whiteLastMove;
   private Move blackLastMove;   
   private Chip neighbor;
   private int whitePieces;
   private int blackPieces;
 
 
   public Board(){
     for(int i = 0; i < 8; i++){
       for(int j = 0; j < 8; j++){
         board[i][j] = new Chip(i, j, Chip.EMPTY);
       }
     }
 
     board[0][0] = new Chip (0, 0, Chip.GREY);
     board[0][7] = new Chip (0, 7, Chip.GREY);
     board[7][0] = new Chip (7 ,0, Chip.GREY);
     board[7][7] = new Chip (7, 7, Chip.GREY);
     neighbor = board[0][0];//arbitrary to satisfy compiler.
   }
 
   public Chip returnChip(int x, int y){
     return board[x][y];
   }
   public int ChipColor(int x, int y)
   {
   	  Chip chip = returnChip(x,y);
   	  int color =  chip.returnColor();
   	  return color;
 	  
   }
     
   public void testNetwork(int color){
     if(hasNetwork(color))
       System.out.println("Found Network");
   else
     System.out.println("Not found Network");
   }
 
   public void addChip(Move m, int color){
     if(m.moveKind == m.ADD){
       board[m.x1][m.y1] = new Chip(m.x1, m.y1, color);  
     }
     else if(m.moveKind == m.STEP){
       board[m.x1][m.y1] = new Chip(m.x1, m.y1, color); 
       removeChip(m.x2, m.y2);   
     }  
     else
       return;  
   }
 
   public void removeChip(int x, int y){
     board[x][y].color = Chip.EMPTY;
   }
 
   public void setPieces(int col){
     if(col == Chip.BLACK)
       ++blackPieces;
     if(col == Chip.WHITE)
       ++whitePieces;
   }
 
   public int getPieces(int col){
     if(col == Chip.BLACK)
       return blackPieces;
     else if(col == Chip.WHITE)
       return whitePieces;
     else
       return 0;
   }
 
  public Chip[] getCurrChips(int col){
    Chip[] currChips = new Chip[10];
    int counter = 0;

    for(int i = 0; i < 8; i++){
      for(int j = 0; j < 8; j++){
        if(board[i][j].color == col){
          currChips[counter] = board[i][j];
          ++counter;
        }
      }
    }

    return currChips;
  }
  

   /*public int numPieces(int col){
     int pieces = 0;
     for (int i=0; i<7; i++) {
       for (int j=0; j<7; j++) {
         if(board[i][j].returnColor() == col)
           pieces++;
       }
     }
     return pieces;
   }*/
  
   public void unflagAllChipsOfColor(int col){
     for (int i=0; i<7; i++) {
       for (int j=0; j<7; j++) {
         if(board[i][j].returnColor() == col)
           board[i][j].unflag();
       }
     }
   }
   public boolean hasNetwork(int col){
     boolean exp = false;
     if(col == Chip.WHITE){
       for (int i=1; i<7 && !exp; i++) {//Check the first goal on the left.
         if(board[0][i].returnColor() == col && !endGoalEmpty(col)){
           unflagAllChipsOfColor(col);
           exp = explore(col, board[0][i].getX(), board[0][i].getY(), 1, Direction.X);//Direction.x is to avoid direction bugz
         }
       }
     }
     if(col == Chip.BLACK){//Check the goal on the top.
     for (int i=1; i<7 && !exp; i++) {
       // System.out.println("color at board (" + i + ", 0) is " + board[i][0].returnColor() + " looking for color: " + Chip.BLACK );
       if(board[i][0].returnColor() == col && !endGoalEmpty(col)){       
         unflagAllChipsOfColor(col);
         exp = explore(col, board[i][0].getX(), board[i][0].getY(), 1, Direction.X);//Direction.x is to avoid direction bugz
       }
       }
     }
     return exp;
   }
     
 // replace chip with board[i][j], when you unflag, you're unflagging a local variable which is getting lost.
   //just refer to the instance variable board[][]. 
 
     boolean explore(int col, int x, int y, int len, Direction dir){
        System.out.println("\n\n\n####ENTERING EXPLORE###");
        board[x][y].flag();
       
         
         
         Direction curr_dir = Direction.N; // N is arbirary. There is not current direction yet.
         
         for (int i=-1; i<=1; i++) {
             for (int j=-1; j<=1; j++) {
                
                // Current Point
                 if( (i == 0) && (j==0)) 
                   continue;
 
                 //Running off board.
                 if( ((x + i) < 0)  ||  ((y + j) < 0) || ((x + i) > 7) || ((y + j) > 7) )
                     continue;
 
                 //Eliminating Corners.
                 if( (x + i == 0 && y + j == 0)  ||  (x + i == 7 && y + j == 0)   ||
                     (x + i == 7 && y + j == 7)  ||  (x + i == 0 && y + j == 7) )
                     continue;
 
                 //Loop to explore board
                 for(int k=1; k<8; k++){
 
                     //Ran off the board
                     if(((x + i*k) < 0)  ||  ((y + j*k) < 0) || ((x + i*k) > 7) || ((y + j*k) > 7))
                         break;
                   
                     neighbor = board[x + i*k][y + j*k];
                     curr_dir = Direction.getCurrDir(i, j);
 
                      if(neighbor.returnColor() == Chip.EMPTY )
                       continue;
                     else{
                       break;
                       }
 
                     }//end third for
                 
                 
                 if( neighbor.returnColor() != col ){//wrong color
                    
                     continue;
                 }
                 
 
                 if(curr_dir == dir){ //same direction
                    System.out.println("The directions are the same: curr_dir is " + curr_dir + " dir is " + dir);
                     continue;
                  }
 
 
                 if(board[neighbor.getX()][neighbor.getY()].isFlagged()){//already visited
                     //System.out.println("The neighbor already visited at (" + neighbor.getX() +", " + neighbor.getY() + ") ");
                     continue;
                   }
                 if( (col == Chip.WHITE && neighbor.getX() == 0) || //NOTE: I'm assuming white start_goal is left column
                     (col == Chip.BLACK && neighbor.getY() == 0)) {  //NOTE: I'm assuming black start_goal is top row
 
                     continue; 
                   }
                 if( (col == Chip.BLACK && neighbor.getY() == 7) || (col == Chip.WHITE && neighbor.getX() == 7)){
                   System.out.println("Found neighbor in end goal at  (" + neighbor.getX() + ", "  + neighbor.getY() + ") and length is " + len);
                     if (len >= 5) return true;
                 
                 }else{
                   System.out.println("Found neighbor at (" + neighbor.getX() + ", " + neighbor.getY() + ") and Recurssing" + "\nlen is " + len);
                     if(explore(col, neighbor.getX(), neighbor.getY(), len+1, curr_dir))
                         return true;
                     }
             
         }//end second for
 }//end first for
         board[x][y].unflag();//chip has no neighbors...how sad.
         System.out.println("Returning false. At (" + board[x][y].getX() + ", " + board[x][y].getY() + ") \n");
         System.out.println("Unflagged a chip at (" + board[x][y].getX() + ", " + board[x][y].getY() + ") ");
         return false;
     }
 
     private boolean endGoalEmpty(int col){
       if (col == Chip.WHITE) {
         for (int i = 1; i<7; i++) {
           if(board[7][i].returnColor() == Chip.WHITE)
             return false;
           else
             return true;
         }
       }
       if (col == Chip.BLACK) {
         for (int i = 1; i<7; i++) {
           if(board[i][7].returnColor() == Chip.BLACK)
             return false;
           else
             return true;
         }
       }
       return true;
     }
 
       public void setLastMove(Move m, int color){
         if(color == Chip.BLACK)
           blackLastMove = new Move(m.x1, m.y1);
         else
           whiteLastMove = new Move(m.x1, m.y1);
       }
 
       public Move getLastMove(int color){
         if(color == Chip.BLACK)
           return blackLastMove;
         else
           return whiteLastMove;
       }
       public void dumpBoard(){
         for( int j = 0; j < 8; j++){                                
           for(int i = 0; i < 8; i++){
             System.out.print("| " + board[i][j].toString() + " | ");
 
           
           }
           System.out.println();
         }                                                                     
       }
 //------------------------PRINT LINES------------------------------------------
 
                    // if(neighbor.getX() == 2 && neighbor.getY() == 7)
                       // System.out.println("##Found end goal neighbor## length is " + len);
                     /*System.out.println("At i = " + i + " , j = " + j + " and k = " + k +
                         " curr_dir is " + curr_dir);                
                     System.out.println("\nneighbor's color at (" + neighbor.getX() + ", " +neighbor.getY()
                         + ") is " + neighbor.returnColor() + " and current color is " + col);
                     System.out.println("\n\nAt i = " + i + " , j = " + j + " and k = " + k +
                         " len is " + len);*/
                 //Applying logic to neighbors
                     /*if (neighbor.getX() == 2 && neighbor.getY() == 2) {
                       System.out.println(" x and y are 2");
                     }*/
                 //if(neighbor.getX() == 3 && neighbor.getY() == 4)
                   // System.out.println("\nFound a black chip in the k loop\ncurr_dir is " + curr_dir + "\ndir is " + dir);
 
                 /*if(neighbor == Color.WHITE)
                     continue;*/
                 /*if( i == 1 && j == 0 && k == 2){
                     System.out.println("I got to the logic");
                     System.out.println("neighbor's color is " + neighbor.returnColor() + " and current color is " + col);
                 }*/
 //  //System.out.println("i is " + i + " j is " + j);
                     //System.out.println("\nk is " + k);
                     //       // System.out.println("Flag status of explore chip is " + chip.isFlagged() + " which should be false");
 
         // System.out.println("X position of explore chip is " + x);
 
   // System.out.println("Y position of explore Chip is " + y);
   // if(x == 2 && y == 7)
           // System.out.println("\nEnd Goal neighbor before loops and logic: length is " + len);
         // System.out.println("neighbor is " + neighbor + " which should be null");
 // System.out.println("\nneighbor's is at (" + neighbor.getX() + ", " +neighbor.getY()
                          // + ") and the his color is " + neighbor.returnColor() + " and current color is " + col
    // System.out.println("\nNeighbor color is, " + neighbor.returnColor() + " and col is " + col + "...The If thinks these are different");
 //                    // System.out.println("Found neighbor in start goal" + "neighbor.getX() is " + neighbor.getX() 
                       // + " and neighbor.getY() is " + neighbor.getY());
                 // if(len == 4){
                 //   System.out.println("\nlen is 4 right before isFlagged check @ ( "+ neighbor.getX() + ", " + neighbor.getY() + 
                 //     ")\nneighbor flagged status is " + neighbor.isFlagged());
 
                 // }
    /* private void flagChip(Chip c){
     int x = c.getX();
     int y = c.getY();
     board[x][y].flag();
   }
   private void unflagChip(Chip c){
     int x = c.getX();
     int y = c.getY();
     board[x][y].unflag();
   }*/
       /*if no neighbor (because you ran off board), continue;
 >>>>>>> fc425eb3256acc60c1ee48933ff493abae415105
       if neighbor is not your color, continue;
       if neighbor is the same direction you just came from, continue;
       if neighbor was already visited, continue;
       if neighbor is in start_goal, continue;
       if neighbor is in end_goal {
       if (len >= 5) return true;
       }
       else
       if (explore(color, neighbor, len+1, curr_direction)) return true;
       }
 
       mark chip as unvisited
       return false;
       }*/
 
 
     // hasNetwork() code skeleton
     //
 
     /*hasNework(int color) {
 
       for each goal_chip in start_goal:
       set visited = false for every chip on the board of our color
 
       explore(color, goal_chip, int len, int dir)
       }
       }
 
       explore(int color, Chip chip, int len, int dir) {
 
       mark chip as visited
 
       for each direction (there are 8 of them!) look for first chip you
       can see in that direction; call it "neighbor"
       {
       if no neighbor (because you ran off board), continue;
       if neighbor is not your color, continue;
       if neighbor is the same direction you just came from, continue;
       if neighbor was already visited, continue;
       if neighbor is in start_goal, continue;
       if neighbor is in end_goal {
       if (len >= 5) return true;
       }
       else
       if (explore(color, neighbor, len+1, curr_direction)) return true;
       }
 
       mark chip as unvisited
       return false;
       }
       }*/
 }

 public class State {
   Piece[][] board; 
 
   Command up        = new Up();
   Command down      = new Down();
   Command left      = new Left();
   Command right     = new Right();
   Command upleft    = new UpLeft();
   Command upright   = new UpRight();
   Command downleft  = new DownLeft();
   Command downright = new DownRight();
 
   // regular constructor
   State() {
     board = new Piece[8][8];
 
     // fill the board with empty squares    
     for(int i=0; i<8; i++) {
       for(int j=0; j<8; j++) {
         board[i][j] = new EmptyPiece(new Location(i,j));
       }
     }
 
     // set middle square
     addBlack(3,4);
     addBlack(4,3);
     addWhite(3,3);
     addWhite(4,4);
 
   }
 
   // copy constructor
   State(State state) {
     this.board = state.board;
   }
 
   public Piece getPiece(int _x, int _y) {
     assert _x < 8;
     assert _x >= 0;
     assert _y < 8;
     assert _y >= 0;
     return board[_x][_y];
   }
 
   public void addBlack(int _x, int _y) {
     Location loc = new Location(_x, _y);
     board[_x][_y] = new BlackPiece(loc);
 
     Piece p = getPiece(_x,_y);
     multiFilp(p);
   }
 
   public void addWhite(int _x, int _y) {
     Location loc = new Location(_x, _y);
     board[_x][_y] = new WhitePiece(loc);
 
     Piece p = getPiece(_x,_y);
     multiFilp(p);
   }
 
   public Boolean isValidMove(Piece _piece) {
     // a move is valid if there is a line of pieces
     // of the opposing color terminated by a piece
     // of the same color
 
     Location loc = _piece.getLoc();
    if( !getPiece(loc.getX(), loc.getY()).isEmpty() ) {
      System.out.println("square full");
       return false;
    }
     
     return lineCheck(_piece, up)      || lineCheck(_piece, down)    ||
            lineCheck(_piece, left)    || lineCheck(_piece,right)    ||
            lineCheck(_piece, upleft)  || lineCheck(_piece, upright) ||
            lineCheck(_piece, downleft)|| lineCheck(_piece, downright);
   }
 
   // returns true if the game is in an end state
   public Boolean isOver(Boolean _color) {
     // true for white, false for black
     // the game is over when there are no
     // valid moves left
     Boolean canMove = false;
     Piece piece;
     
     if(_color) {
       piece = new WhitePiece(new Location(0,0));
     }
     else {
       piece = new BlackPiece(new Location(0,0));
     }     
 
     for(int i=0; i<8; i++) {
       for(int j=0; j<8; j++) {
         Location loc = new Location(i,j);
         piece.setLoc(loc);
         if( isValidMove(piece) ) {
           canMove = true;
         }
       }
     } 
   
   return !canMove; 
   }
 
 
   private Boolean lineCheck(Piece _piece, Command c) {
    Boolean line = true;
     Location loc = _piece.getLoc();
     Piece current = c.get(_piece);
 
    // empty line is false
     if( current.isEmpty() ) {
       return false;
     }
 
    // two pieces of the same color not a line
     if( _piece.sameColor(current) ) {
       return false;
     }
 
    // n pieces of opposite color followed by piece of the same color
    while(true) {
       current = c.get(current);
       
      if(current.isEmpty()) return false;
      if( _piece.sameColor(current)) return true;

     }
 
   }
 
   private void multiFilp(Piece p) {
     if(lineCheck(p, up))        lineFlip(p, up);
     if(lineCheck(p, down))      lineFlip(p, down);
     if(lineCheck(p, left))      lineFlip(p, left);
     if(lineCheck(p, right))     lineFlip(p, right);
     if(lineCheck(p, upleft))    lineFlip(p, upleft);
     if(lineCheck(p, upright))   lineFlip(p, upright);
     if(lineCheck(p, downleft))  lineFlip(p, downleft);
     if(lineCheck(p, downright)) lineFlip(p, downright);
   }
 
   private void lineFlip(Piece _piece, Command c) {
     Boolean line = false;
     Location loc = _piece.getLoc();
     Piece current = _piece;
     
 
     while( !current.isEmpty() ) {
       current = c.get(current);
       if( current.sameColor(_piece) ) {
         break;
       }
       flip(current);
     }
   }
 
   private void flip(Piece p) {
     Location loc = p.getLoc();
     flipPiece(loc.getX(), loc.getY());
   }
 
   private void flipPiece(int _x, int _y) {
     board[_x][_y] = board[_x][_y].flip();
   }
 
   // used to implement game logic using the command pattern
   private interface Command {
     public Piece get(Piece _piece);
   }
 
   private class Up implements Command {
     public Piece get(Piece _piece) {
       Location loc = _piece.getLoc();
       if (loc.getY()+1 > 7) 
         return new EmptyPiece(_piece);
 
       return getPiece(loc.getX(), loc.getY() + 1);
     } 
   }
 
   private class Down implements Command {
     public Piece get(Piece _piece) {
       Location loc = _piece.getLoc();
       if (loc.getY()-1 < 0) 
         return new EmptyPiece(_piece);
 
       return getPiece(loc.getX(), loc.getY() - 1);
     }
   }
 
   private class Left implements Command {
     public Piece get(Piece _piece) {
       Location loc = _piece.getLoc();
       if (loc.getX()-1 < 0) 
         return new EmptyPiece(_piece);
 
       return getPiece(loc.getX() - 1 , loc.getY());
     }
   }
 
   private class Right implements Command {
     public Piece get(Piece _piece) {
       Location loc = _piece.getLoc();
       if (loc.getX()+1 > 7) 
         return new EmptyPiece(_piece);
 
       return getPiece(loc.getX() + 1, loc.getY());
     }
   }
 
   private class UpLeft implements Command {
     public Piece get(Piece _piece) {
       Location loc = _piece.getLoc();
       if (loc.getX()-1 < 0) 
         return new EmptyPiece(_piece);
       if (loc.getY()+1 > 7)
         return new EmptyPiece(_piece);
 
       return getPiece(loc.getX()-1, loc.getY() + 1);
     }
   }
 
   private class UpRight implements Command {
     public Piece get(Piece _piece) {
       Location loc = _piece.getLoc();
       if (loc.getY()+1 > 7) 
         return new EmptyPiece(_piece);
       if (loc.getX()+1 > 7)
         return new EmptyPiece(_piece);
 
       return getPiece(loc.getX() + 1, loc.getY() + 1);
     }
   }
 
   private class DownLeft implements Command {
     public Piece get(Piece _piece) {
       Location loc = _piece.getLoc();
       if (loc.getY()-1 < 0)
         return new EmptyPiece(_piece);
       if (loc.getX()-1 < 0)
         return new EmptyPiece(_piece);
 
       return getPiece(loc.getX() - 1, loc.getY() - 1);
     }
   }
 
   private class DownRight implements Command {
     public Piece get(Piece _piece) {
     Location loc = _piece.getLoc();
       if (loc.getY()-1 < 0) 
         return new EmptyPiece(_piece);
       if(loc.getX()+1 > 7)
         return new EmptyPiece(_piece);
 
       return getPiece(loc.getX() + 1, loc.getY() - 1);
     }
   }
 }
 

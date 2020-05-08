 import java.util.ArrayList;
 
 import com.sun.org.apache.bcel.internal.generic.ISUB;
 
 import exceptions.IllegalMoveException;
 import exceptions.IllegalPromotionException;
 
 
 public class Board {
   // This is the correct layout for the board. The 'origin' is [7][0]
   public BoardSquare[][] squares;
   private King[] kings;
   private ArrayList<Piece> white_pieces;
   private ArrayList<Piece> black_pieces;
   
   public Board() {
     String color = "black";
     squares = new BoardSquare[8][8];
     kings = new King[2];
     
     // keep track of both black and white's pieces
     white_pieces = new ArrayList<Piece>();
     black_pieces = new ArrayList<Piece>();
     
     for (int i = 0; i < 8; i++) {
       for (int j = 0; j < 8; j++) {
         squares[i][j] = new BoardSquare(color);
         color = (color == "white" ? "black" : "white" );
       }
       color = (color == "white" ? "black" : "white" );
     }
        
     // set up the black pieces
     for (int j = 0; j < 8; j++) {
       squares[6][j].occupy(new Pawn("black", 6, j));
     }
     
     squares[7][0].occupy(new Rook("black", 7, 0));
     squares[7][7].occupy(new Rook("black", 7, 7));
     
     squares[7][1].occupy(new Knight("black", 7, 1));
     squares[7][6].occupy(new Knight("black", 7, 6));
     
     squares[7][2].occupy(new Bishop("black", 7, 2));
     squares[7][5].occupy(new Bishop("black", 7, 5));
     
     squares[7][3].occupy(new Queen("black", 7, 3));
     squares[7][4].occupy(new King("black", 7, 4));
     kings[1] = (King) squares[7][4].getOccupant();
 
     // set up the white pieces
     for (int j = 0; j < 8; j++) {
       squares[1][j].occupy(new Pawn("white", 1, j));
     }
     
     squares[0][0].occupy(new Rook("white", 0, 0));
     squares[0][7].occupy(new Rook("white", 0, 7));
     
     squares[0][1].occupy(new Knight("white", 0, 1));
     squares[0][6].occupy(new Knight("white", 0, 6));
     
     squares[0][2].occupy(new Bishop("white", 0, 2));
     squares[0][5].occupy(new Bishop("white", 0, 5));
 //    
     squares[0][3].occupy(new Queen("white", 0, 3));
     squares[0][4].occupy(new King("white", 0, 4));
     kings[0] = (King) squares[0][4].getOccupant();
     
     for (int i = 0; i < 8; i++) {
       for (int j = 0; j < 8; j++) {
         if (squares[i][j].isOccupied()) {
           Piece p = squares[i][j].getOccupant();
           if (p.getColor().equals("w")) {
             white_pieces.add(p);
           } else {
             black_pieces.add(p);
           }
         }
       }
     }
    }
   
   public void movePiece(String from, String to) throws IllegalMoveException {
     int[] from_pair, to_pair;
     from_pair = coordToRankFilePair(from);
     to_pair = coordToRankFilePair(to);
     
     int rank = from_pair[0], file = from_pair[1];
     int drank = to_pair[0], dfile = to_pair[1];
     
     Piece piece = squares[rank][file].getOccupant();
     
     if (piece == null) 
       throw new IllegalMoveException("piece at " + from + " does not exist");
     if (rank < 0 || file < 0 || rank > 7 || file > 7) 
       throw new IllegalMoveException(from + " is outside the board's bounds");
     if (drank < 0 || dfile < 0 || drank > 7 || dfile > 7) 
       throw new IllegalMoveException(to + " is outside the board's bounds");
     if (!piece.canMoveTo(drank, dfile))
       throw new IllegalMoveException("piece cannot move from " + from + " to " + to);
 
     // are there any pieces in between if we are not a knight?
     if (!piece.getClass().equals(Knight .class) && piecesBetween(rank, file, drank, dfile))
       throw new IllegalMoveException("there are pieces between " + from + " and " + to);
 
     // is there someone there?
     if (squares[drank][dfile].getOccupant() != null) {
       if (squares[drank][dfile].getOccupant().getColor() == piece.getColor())
         throw new IllegalMoveException("cannot capture own piece");
  
       if (!piece.canAttack(drank, dfile))
         throw new IllegalMoveException("the piece cannot attack " + to);
 
       // there is a capture happening!!!
       System.out.println(piece + " captured " + squares[drank][dfile].getOccupant());
       piece.moveTo(drank, dfile);
       squares[drank][dfile].occupy(piece);
       squares[rank][file].vacate();
       piece.incrMoved();
     } else {
       // special case for pawns, can't actually move sideways, only attack
       if (piece.getClass().equals((Pawn .class)) && Math.abs(file - dfile) == 1) {
         throw new IllegalMoveException("pawns can't move diagonally, only attack");
       }
       
       if (piece.getClass().equals((King .class))) {
         King tking = checkForCheckAndMate((King) piece, drank, dfile);
         
         if (((King) piece).isUnderCheck() && canPieceBeAttackedAt(tking, drank, dfile)) {
           throw new IllegalMoveException("king will be under check there");
         }
         
         if (((King) piece).isUnderMate()) {
           throw new IllegalMoveException("king us under mate, " + piece.getColor() + " wins");
         }
       }
 
       piece.moveTo(drank, dfile);
       squares[drank][dfile].occupy(piece);
       squares[rank][file].vacate();
       piece.incrMoved();
       System.out.println("moved " + piece + " from " + from + " to " + to);
     }
     
     King king = (piece.getColor().equals("w") ? kings[1] : kings[0]);
     checkForCheckAndMate(king);
   }
   
   private King checkForCheckAndMate(King king) {
     return checkForCheckAndMate(king, king.rawRank(), king.rawFile());
   }
   
   private King checkForCheckAndMate(King king, int drank, int dfile) {
     int check = 0;
 
     if (canPieceBeAttackedAt(king, drank, dfile)) {
       check = 2; // assume mate
 
       for (String potential_move : king.availableMoves()) { // for all the moves the king can make   
         if (!canPieceBeAttackedAt(king, potential_move)) { // if we can't be attacked there
           check = 1;
          System.out.println(king + " can't be touched at " + potential_move);
 
           if (getOccupantAt(potential_move) != null
               && king.isFriendsWith(getOccupantAt(potential_move))) {
             check = 2;
           }
         }
       }
 
       switch (check) {
         case 1:
           System.out.println(king + " is under check");
           king.check();
           break;
         case 2:
           System.out.println(king + " is under check mate");
           king.mate();
           break;
       }
     } else {
       System.out.println(king + " is safe");
       king.free();
     }
     
     return king;
   }
   
   private boolean canPieceMoveTo(Piece piece, int rank, int file) {
     return !piecesBetween(piece.rawRank(), piece.rawFile(), rank, file);
   }
   
   private boolean piecesBetween(int rank, int file, int drank, int dfile) {
     int r = rank, f = file;
     if (rank-drank != 0) r += (drank-rank)/Math.abs(rank-drank);
     if (file-dfile != 0) f += (dfile-file)/Math.abs(dfile-file);
     
     while ( r != drank || f != dfile ) {
       // if there is a piece and it's not the one we start on, then return true
       if (getOccupantAt(r, f) != null) return true;
       
       if (rank-drank != 0) r += (drank-rank)/Math.abs(rank-drank);
       if (file-dfile != 0) f += (dfile-file)/Math.abs(dfile-file);
     }
     return false;
   }
   
   private Piece getOccupantAt(String fileRank) {
     return getOccupantAt(coordToRankFilePair(fileRank));
   }
   
   private Piece getOccupantAt(int[] rankFilePair) {
     return getOccupantAt(rankFilePair[0], rankFilePair[1]);
   }
   
   private Piece getOccupantAt(int rank, int file) {
     return squares[rank][file].getOccupant();
   }
   
   private boolean canPieceBeAttackedAt(Piece piece, String fileRank) {
     return canPieceBeAttackedAt(piece, coordToRankFilePair(fileRank));
   }
   
   private boolean canPieceBeAttackedAt(Piece piece, int[] rankFilePair) {
     return canPieceBeAttackedAt(piece, rankFilePair[0], rankFilePair[1]);
   }
   
   private boolean canPieceBeAttackedAt(Piece piece, int rank, int file) {
     if (piece == null) return false;
     
     ArrayList<Piece> enemy = (piece.getColor().equals("w") ? black_pieces : white_pieces);
     
     for (Piece e : enemy) {
 //      System.out.println("checking " + e);
       if (e.canAttack(rank, file)) {
        System.out.println(e + " can attack " + piece);
         return true;
       } else {
 //        System.out.println(e + " at " + e.getFileRank() + " cannot attack " + piece + " at " + piece.getFileRank());
 //        System.out.println(e + " can move to:");
 //        ArrayList<String> moves = e.availableMoves();
 //        
 //        for (String move : moves) {
 //          System.out.print(move + " ");
 //        }
 //        
 //        System.out.println("");
       }
     }
     
     return false;
   }
   
   public void checkForUpgrades(String type) throws IllegalPromotionException {
     // are there pawns that need to become queens at the ends of the board
     for (int i = 0; i < 8; i+=7) {
       for (int j = 0; j < 8; j++) {
         if (squares[i][j].getOccupant() != null
             && squares[i][j].getOccupant().getClass().equals(Pawn .class)) {
           
           if (type.equals("Q")) {
             squares[i][j].occupy(new Queen(squares[i][j].getOccupant().getColor(), i, j));
           } else if (type.equals("N")) {
             squares[i][j].occupy(new Knight(squares[i][j].getOccupant().getColor(), i, j));
           } else if (type.equals("B")) {
             squares[i][j].occupy(new Bishop(squares[i][j].getOccupant().getColor(), i, j));
           } else if (type.equals("R")) {
             squares[i][j].occupy(new Rook(squares[i][j].getOccupant().getColor(), i, j));
           } else {
             throw new IllegalPromotionException("unknown promotion to " + type + ". Can be [Q],N,B,R");
           }
         }
       }
     }
   }
   
   public boolean pieceBelongsToPlayer(String loc, String player) {
     return getOccupantAt(loc).getColor().equals(player.substring(0, 1));
   }
   
  public boolean pieceExistsAt(String loc) {
    return getOccupantAt(loc) != null;
  }
  
   public King matedKing() {
     for (King king : kings) {
       if (king.isUnderMate()) {
         return king;
       }
     }
     return null;
   }
   
   /**
    * Turns a FileRank pair into indices in the form {rank, file}. It's right, trust me.
    * 
    * Example:
    *  a1 => {0,0}
    *  d7 => {6,3}
    * 
    * @param coord
    * @return
    */
   private static int[] coordToRankFilePair(String coord) {
     return new int[] {(coord.charAt(1) - '1'), coord.charAt(0) - 'a'};
   }
   
   public static String rankFileToCoord(int rank, int file) {
     return (new String[] {"a","b","c","d","e","f","g","h"})[file] + String.valueOf(rank + 1);
   }
   
   public String toString() {
     String ret = "   a  b  c  d  e  f  g  h\n";
     
     for (int i = 7; i >= 0; i--) {
       ret = ret.concat(String.valueOf(i + 1) + " ");
       for (int j = 0; j < 8; j++) {
         ret = ret.concat(squares[i][j].toString() + " ");
       }
       ret = ret.concat("\n");
     }
  
     return ret;
   }
 }

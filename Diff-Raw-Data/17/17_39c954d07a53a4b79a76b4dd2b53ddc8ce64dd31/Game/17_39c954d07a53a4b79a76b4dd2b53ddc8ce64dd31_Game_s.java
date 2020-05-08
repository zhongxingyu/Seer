 package model;
 
 import java.util.Scanner;
 import java.util.ArrayList;
 
 // -------------------------------------------------------------------------
 /**
  * Chessboard World Holds the instructions, Pieces, and Square highlights
  * referred to as glows. Checks for end game conditions.
  *
  * @author Loran Steinberger (bkwrm215)
  * @author Robert Scheible (scheible)
  * @author Jack Cobb (jack3)
  * @version (2012.12.03)
  */
 public class Game
 {
     // ~ Fields ................................................................
 
     // The Board is an 8x8 array of pieces, with board[x][y]
     // board[0][0] is equivalent to a8, board[0][7] is a1,
     // board[7][0] is h8, board[7][7] is h1
     private Piece[][]       board;
     private Piece[]         whitePieces;
     private Piece[]         blackPieces;
     private ArrayList<Move> whiteAttackingMoves; // A list of all possible
 // squares
                                                  // the pieces can attack
 
     private ArrayList<Move> blackAttackingMoves;
 
     private ArrayList<Move> whiteAvailableMoves;
 
     private ArrayList<Move> blackAvailableMoves; // A list of all moves the
 // player
                                                  // can actually make
     private boolean         whiteInCheck;
     private boolean         blackInCheck;
     private boolean         whiteTurn;
 
 
     // ~ Constructor ...........................................................
     // ----------------------------------------------------------
     /**
      * Creates a new ChessboardWhite object.
      */
     public Game()
     {
         // TODO: Clean up this program
 
         // TODO: Make a GUI
 
         // The Board is an 8x8 array of pieces, with board[x][y]
         // board[0][0] is equivalent to a8, board[0][7] is a1,
         // board[7][0] is h8, board[7][7] is h1
         board = new Piece[8][8];
 
         blackPieces = new Piece[16];
         whitePieces = new Piece[16];
 
         // Creates black pieces except pawns
         blackPieces[0] = board[0][0] = new Rook(false, new Location(0, 0), 0);
         blackPieces[1] = board[1][0] = new Knight(false, new Location(1, 0), 1);
         blackPieces[2] = board[2][0] = new Bishop(false, new Location(2, 0), 2);
         blackPieces[3] = board[3][0] = new Queen(false, new Location(3, 0), 3);
         blackPieces[4] = board[4][0] = new King(false, new Location(4, 0), 4);
         blackPieces[5] = board[5][0] = new Bishop(false, new Location(5, 0), 5);
         blackPieces[6] = board[6][0] = new Knight(false, new Location(6, 0), 6);
         blackPieces[7] = board[7][0] = new Rook(false, new Location(7, 0), 7);
 
         // Creates white pieces except pawns
         whitePieces[0] = board[0][7] = new Rook(true, new Location(0, 7), 0);
         whitePieces[1] = board[1][7] = new Knight(true, new Location(1, 7), 1);
         whitePieces[2] = board[2][7] = new Bishop(true, new Location(2, 7), 2);
         whitePieces[3] = board[3][7] = new Queen(true, new Location(3, 7), 3);
         whitePieces[4] = board[4][7] = new King(true, new Location(4, 7), 4);
         whitePieces[5] = board[5][7] = new Bishop(true, new Location(5, 7), 5);
         whitePieces[6] = board[6][7] = new Knight(true, new Location(6, 7), 6);
         whitePieces[7] = board[7][7] = new Rook(true, new Location(7, 7), 7);
 
         // Creates all pawns
         for (int ii = 0; ii < 8; ii++)
         {
             blackPieces[ii + 8] =
                 board[ii][1] = new Pawn(false, new Location(ii, 1), ii + 8);
             whitePieces[ii + 8] =
                 board[ii][6] = new Pawn(true, new Location(ii, 6), ii + 8);
         }
 
         whiteTurn = true;
 
         updateAttackSquares();
         updateAvailableMoves();
     }
 
 
     /**
      * Creates a new game with the given setup: allows for the checking of
      * potential moves.
      *
      * @param board
      *            The board the pieces are set up on.
      * @param whitePieces
      *            an array containing all white pieces
      * @param blackPieces
      *            an array containing all black pieces
      * @param turn
      *            true if it's white's turn.
      */
     public Game(
         Piece[][] board,
         Piece[] whitePieces,
         Piece[] blackPieces,
         boolean turn)
     {
         this.blackPieces = new Piece[16];
         this.whitePieces = new Piece[16];
 
         for (int ii = 0; ii < 16; ii++)
         {
             if (whitePieces[ii] != null)
             {
                 this.whitePieces[ii] = whitePieces[ii].clone();
             }
             if (blackPieces[ii] != null)
             {
                 this.blackPieces[ii] = blackPieces[ii].clone();
             }
         }
 
         this.board = new Piece[8][8];
         for (int ii = 0; ii < 8; ii++)
         {
             for (int jj = 0; jj < 8; jj++)
             {
                 if (board[ii][jj] != null)
                 {
                     this.board[ii][jj] = board[ii][jj].clone();
                 }
             }
         }
 
         this.whiteTurn = turn;
 
         updateAttackSquares();
     }
 
 
     // GETTER/SETTER METHODS -------------------------------------------------
 
     /**
      * Returns the available moves for the current player.
      *
      * @return the available moves
      */
     public ArrayList<Move> getAvailableMoves()
     {
         if (whiteTurn)
         {
             return whiteAvailableMoves;
         }
         return blackAvailableMoves;
     }
 
 
     /**
      * Returns the attacking squares for the current player.
      *
      * @return the available moves
      */
     public ArrayList<Move> getAttackingMoves()
     {
         if (whiteTurn)
         {
             return whiteAttackingMoves;
         }
         return blackAttackingMoves;
     }
 
 // /**
 // * Returns whether or not the current player is in check
 // *
 // * @return is in check
 // */
 // public boolean isInCheck()
 // {
 // if (whiteTurn)
 // {
 // return whiteInCheck;
 // }
 // return blackInCheck;
 // }
 
     /**
      * Returns the actual board
      *
      * @return board is the array holding all pieces
      */
     public Piece[][] getBoard()
     {
         return board;
     }
 
 
     // ----------------------------------------------------------
     /**
      * This list returns an arraylist of all possible white moves that would
      * take a black piece or put a piece(ie king) in danger
      * @return is the arraylist of attacking moves
      */
     public ArrayList<Move> getWhiteAttackingMoves()
     {
         return whiteAttackingMoves;
     }
 
 
     // ----------------------------------------------------------
     /**
      * This method returns an arraylist of all possible black moves that would
      * take a white piece or put a piece(ie king) in danger
      * @return is the arraylist of all attacking moves
      */
     public ArrayList<Move> getBlackAttackingMoves()
     {
         return blackAttackingMoves;
     }
 
 
     // ----------------------------------------------------------
     /**
      * This method gets and arraylist of all the available moves to the white
      * pieces on the board
      * @return is the arraylist of the available moves
      */
     public ArrayList<Move> getWhiteAvailableMoves()
     {
         return whiteAvailableMoves;
     }
 
 
     // ----------------------------------------------------------
     /**
      * This method returns an arraylist of all the available moves to the black
      * pieces remaining on the board
      * @return is the arraylist of the available moves to black pieces
      */
     public ArrayList<Move> getBlackAvailableMoves()
     {
         return blackAvailableMoves;
     }
 
 
     // ----------------------------------------------------------
     /**
      * This method checks if the white king is in check, and returns a boolean
      * @return is the boolean answer if the king is in check
      */
     public boolean isWhiteInCheck()
     {
         return whiteInCheck;
     }
 
 
     // ----------------------------------------------------------
     /**
      * THis method checks if the black king is in check, and returns a boolean
      * depending on whether the king is in check or not
      * @return is the boolean answer if the king is in check
      */
     public boolean isBlackInCheck()
     {
         return blackInCheck;
     }
 
 
     /**
      * Sets the chessboard to the given board.
      *
      * @param board
      *            is a new chessboard.
      */
     public void setBoard(Piece[][] board)
     {
         this.board = board;
     }
 
 
     /**
      * Returns the array of all white pieces
      *
      * @return the white pieces
      */
     public Piece[] getWhitePieces()
     {
         return whitePieces;
     }
 
 
     /**
      * Sets the array of white pieces to whatever the new array is
      *
      * @param whitePieces
      *            is the pieces to be on the board
      */
     public void setWhitePieces(Piece[] whitePieces)
     {
         this.whitePieces = whitePieces;
     }
 
 
     /**
      * Returns the array of all black pieces
      *
      * @return the array of all black pieces
      */
     public Piece[] getBlackPieces()
     {
         return blackPieces;
     }
 
 
     /**
      * Sets the blackPieces to the new array passed in
      *
      * @param blackPieces
      *            the new array
      */
     public void setBlackPieces(Piece[] blackPieces)
     {
         this.blackPieces = blackPieces;
     }
 
 
     // UPDATE METHODS --------------------------------------------------------
 
     /**
      * Ends the current turn, updates game accordingly
      */
     public void endTurn()
     {
         whiteTurn = !whiteTurn;
         updateAttackSquares();
         updateAvailableMoves();
         updateCheck();
         checkmate();
     }
 
 
     /**
      * Updates the squares the current player can attack.
      */
     public void updateAttackSquares()
     {
         whiteAttackingMoves = new ArrayList<Move>();
         blackAttackingMoves = new ArrayList<Move>();
 
         whitePieces = getWhitePieces();
 
         blackPieces = getBlackPieces();
 
         for (int ii = 0; ii < 16; ii++)
         {
             if (whitePieces[ii] != null)
             {
                 whiteAttackingMoves.addAll(whitePieces[ii]
                     .getPossibleMoves(getBoard()));
             }
             if (blackPieces[ii] != null)
             {
                 blackAttackingMoves.addAll(blackPieces[ii]
                     .getPossibleMoves(getBoard()));
             }
         }
     }
 
 
     /**
      * Updates the arraylist of all possible moves.
      */
     public void updateAvailableMoves()
     {
         if (whiteTurn)
         {
             whiteAvailableMoves = new ArrayList<Move>();
             for (Move move : whiteAttackingMoves)
             {
                 if (isLegalMove(move))
                 {
                     whiteAvailableMoves.add(move);
                 }
             }
         }
         else
         {
             blackAvailableMoves = new ArrayList<Move>();
             for (Move move : blackAttackingMoves)
             {
                 if (isLegalMove(move))
                 {
                     blackAvailableMoves.add(move);
                 }
             }
         }
 
     }
 
 
     /**
      * Checks if the current player can make any moves. Otherwise, they have
      * been checkmated.
      */
     public void checkmate()
     {
        // TODO: This isn't working properly. For whatever reason, I put in the
// four move checkmate, and the black queen still had moves available.
         if ((getWhiteTurn() && whiteAvailableMoves.isEmpty() && isWhiteInCheck())
             || (!getWhiteTurn() && blackAvailableMoves.isEmpty() && isBlackInCheck()))
         {
             String output = "";
             if (getWhiteTurn())
             {
                 output += "White ";
             }
             else
             {
                 output += "Black ";
             }
             output += "has been checkmated!";
             System.out.println(output);
             System.exit(0);
         }
 
         if ((getWhiteTurn() && whiteAvailableMoves.isEmpty())
             || (!getWhiteTurn() && blackAvailableMoves.isEmpty()))
         {
             System.out.println("Stalemate!");
             System.exit(0);
         }
     }
 
 
     /**
      * Prints out the board, showing the positions of all pieces in console.
      * Does not show the color of each piece.
      */
     public void print()
     {
         System.out.println("  abcdefgh");
         System.out.println();
 
         for (int ii = 0; ii < 8; ii++)
         {
             System.out.print(8 - ii + " ");
             for (int jj = 0; jj < 8; jj++)
             {
                 Piece piece = board[jj][ii];
                 if (piece == null)
                 {
                     System.out.print("-");
                 }
                 else if (piece.getClass() == Knight.class)
                 {
                     System.out.print("N");
                 }
                 else if (piece.getClass() == Bishop.class)
                 {
                     System.out.print("B");
                 }
                 else if (piece.getClass() == Rook.class)
                 {
                     System.out.print("R");
                 }
                 else if (piece.getClass() == Pawn.class)
                 {
                     System.out.print("P");
                 }
                 else if (piece.getClass() == King.class)
                 {
                     System.out.print("K");
                 }
                 else if (piece.getClass() == Queen.class)
                 {
                     System.out.print("Q");
                 }
             }
             System.out.println();
         }
 
         System.out.println("  abcdefgh");
         System.out.println();
     }
 
 
     /**
      * Adds a piece to the board and the array of pieces
      *
      * @param piece
      *            is the piece being added to board.
      */
     public void add(Piece piece)
     {
         if (piece.getIsWhite())
         {
             whitePieces[piece.getInColorArray()] = piece;
         }
         else
         {
             blackPieces[piece.getInColorArray()] = piece;
         }
 
         board[piece.getLocal().x()][piece.getLocal().y()] = piece;
     }
 
 
     /**
      * Clear Board The world removes all actors and text images.
      */
     public void clearBoard()
     {
         for (int ii = 0; ii < 8; ii++)
         {
             for (int jj = 0; jj < 8; jj++)
             {
                 board[ii][jj] = null;
             }
         }
         blackPieces = new Piece[16];
         whitePieces = new Piece[16];
     }
 
 
     /**
      * get White Turn method
      *
      * @return returns true if it is white's turn, and false if it is blacks
      *         turn.
      */
     public boolean getWhiteTurn()
     {
         return whiteTurn;
     }
 
 
     /**
      * updates which player is in check.
      */
     public void updateCheck()
     {
        // TODO: Doesn't work as intended. Must get attacking moves for
        // Correct player

        // TODO: Figure out why/how null pointer exception is happening.
        // Happens when king put in check.
         whiteInCheck = false;
         blackInCheck = false;
 
         Location whiteKingLocal = whitePieces[4].getLocal();
         for (Move move : blackAttackingMoves)
         {
             if (move.getLocal().equals(whiteKingLocal))
             {
                 whiteInCheck = true;
             }
         }
 
         Location blackKingLocal = blackPieces[4].getLocal();
         for (Move move : whiteAttackingMoves)
         {
             if (move.getLocal().equals(blackKingLocal))
             {
                 blackInCheck = true;
             }
         }
     }
 
 
     /**
      * Checks to see if the move is legal
      *
      * @param move
      *            is the move being checked.
      * @return whether or not the move is legal (boolean)
      */
     public boolean isLegalMove(Move move)
     {
 // Piece piece = move.getPiece();
 
 // if (piece.getIsWhite() != getWhiteTurn())
 // {
 // return false;
 // }
 //
 // boolean legalMove = false;
 //
 // for (Move checkingMove : piece.getPossibleMoves(board))
 // {
 // if (move.equals(checkingMove))
 // {
 // legalMove = true;
 // break;
 // }
 // }
 //
 // if (!legalMove)
 // {
 // return false;
 // }
 
         // checks if moving the piece puts their own king in check
 
         Game potentialGame =
            new Game(board, blackPieces, whitePieces, whiteTurn);
 
         potentialGame.move(move);
         potentialGame.updateAttackSquares();
         potentialGame.updateCheck();
 
         if (potentialGame.isBlackInCheck())
         {
             System.out.println("black in check");
         }
 
         if (potentialGame.isWhiteInCheck())
         {
             System.out.println("White in check");
         }
 
 
         if (getWhiteTurn())
         {
             return !potentialGame.isWhiteInCheck();
         }
         else
         {
             return !potentialGame.isBlackInCheck();
         }
     }
 
 
     /**
      * Place a description of your method here.
      *
      * @param move
      *            is the location and the piece to be moved.
      */
     public void move(Move move)
     {
        //TODO: Pawn move not updating internal location
         String notation = move.getPiece().getLetter();
         Piece piece = move.getPiece().clone();
         board[piece.getLocal().x()][piece.getLocal().y()] = null;
         String startingLetter =
             move.getPiece().getLocal().toString().substring(0, 1);
         piece.setLocal(move.getLocal());
 
         if (piece.getIsWhite())
         {
             whitePieces[piece.getInColorArray()] = piece;
         }
         else
         {
             blackPieces[piece.getInColorArray()] = piece;
         }
 
         // Taking a piece
         if (board[move.getLocal().x()][move.getLocal().y()] != null)
         {
             if (notation.equals(""))
             {
                 notation += startingLetter;
             }
             notation += "x";
             if (board[move.getLocal().x()][move.getLocal().y()].getIsWhite())
             {
                 whitePieces[board[move.getLocal().x()][move.getLocal().y()]
                     .getInColorArray()] = null;
             }
             else
             {
                 blackPieces[board[move.getLocal().x()][move.getLocal().y()]
                     .getInColorArray()] = null;
             }
         }
         notation += move.getLocal().toString();
 
         if ((move.getLocal().y() == 0 || move.getLocal().y() == 7)
             && piece.getClass() == Pawn.class)
         {
             notation += "=";
 
             int possible = getPawnPromotion();
             switch (possible)
             {
                 case (0):
                     piece =
                         new Queen(
                             move.getPiece().getIsWhite(),
                             move.getLocal(),
                             move.getPiece().getInColorArray());
                     notation += "Q";
                     break;
                 case (1):
                     piece =
                         new Rook(
                             move.getPiece().getIsWhite(),
                             move.getLocal(),
                             move.getPiece().getInColorArray());
                     notation += "R";
                     break;
                 case (2):
                     piece =
                         new Knight(
                             move.getPiece().getIsWhite(),
                             move.getLocal(),
                             move.getPiece().getInColorArray());
                     notation += "N";
                     break;
                 case (3):
                     piece =
                         new Bishop(
                             move.getPiece().getIsWhite(),
                             move.getLocal(),
                             move.getPiece().getInColorArray());
                     notation += "B";
                     break;
 
             }
         }
 
         board[move.getLocal().x()][move.getLocal().y()] = piece;
         if (piece.getIsWhite())
         {
             whitePieces[piece.getInColorArray()] = piece;
         }
         else
         {
             blackPieces[piece.getInColorArray()] = piece;
         }
 
         // TODO: Fix notation so that it recognizes check
     }
 
 
     /**
      * Gets user input, tells what piece to promote pawn to. 0: Queen 1: Rook 2:
      * Knight 3: Bishop
      *
      * @return the int corresponding to the piece which you want to promote the
      *         pawn to.
      */
     public int getPawnPromotion()
     {
         System.out
             .println("Enter 0 for queen, 1 for rook, 2 for knight, 3 for bishop.");
         Scanner scan = new Scanner(System.in);
 
         int next = scan.nextInt();
         while (next < 0 || next > 3)
         {
             System.out.println("Invalid number.");
             next = scan.nextInt();
         }
         return next;
     }
 
 
     /**
      * set White Turn sets the value of the whiteTurn boolean.
      *
      * @param turn
      *            true if set to white, false if set to black.
      */
     public void setWhiteTurn(boolean turn)
     {
         whiteTurn = turn;
     }
 }

 package antichess;
 
 import java.awt.*;
 import java.awt.geom.*;
 import java.awt.Font.*;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.util.ArrayList;
 import javax.imageio.*;
 import java.io.*;
 
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 
 public class Board extends Frame{
 
    private Piece[][] squares;
    private double squareSize;
    public ArrayList validMoves;
    public ArrayList validCaptures;
    private ArrayList remainingPieces;
    private BufferedImage wTile;
    private BufferedImage bTile;
 
    //Testing mouse clicking stuff
    public Move mouseClick;
    private int firstX;
    private int firstY;
    private boolean secondClick;
    private boolean returnMove;
 
    public Move getMove() throws InterruptedException {
       while (true) {
          if (returnMove == true) {
             //System.out.println("ready to return");
             break;
          }
          Thread.sleep(50);
       }
       returnMove = false;
       return mouseClick;
    }
 
    //Testing mouse clicking stuff. MCS
    private class MouseClickListener extends MouseAdapter {
 
       public void mouseClicked(MouseEvent event) {
          int x = event.getX()/60-1;
          int y = 9 - event.getY()/60-1;
          //System.out.println(x);
          //System.out.println(y);
 
          if (secondClick == true) {
             //do stuff if it is the second click
             //System.out.println("Working on the second click");
             secondClick = false;
             mouseClick = new Move(firstX, firstY, x, y);
             returnMove = true;
             //System.out.println("mouseClick should now be non null");
          } else {
             //do stuff if it is the first click
             firstX = x;
             firstY = y;
             secondClick = true;
          }
       }
    }
 
    public Board(double FRAME_SIZE) {
       //Testing mouse clicking stuff. MCS
       mouseClick = null;
       secondClick = false;
       returnMove = false;
       MouseClickListener listener = new MouseClickListener();
       addMouseListener(listener);
 
       // initialise the array lists
       validMoves = new ArrayList();
       validCaptures = new ArrayList();
       remainingPieces = new ArrayList();
       //MA - set square size
 
       squareSize = FRAME_SIZE / 10;
       //Initialises the board.
       squares = new Piece[8][8];
       squares[0][0] = new Rook(0, 0, 'w');
       squares[1][0] = new Knight(1, 0, 'w');
       squares[2][0] = new Bishop(2, 0, 'w', 'b');
       squares[3][0] = new Queen(3, 0, 'w');
       squares[4][0] = new King(4, 0, 'w');
       squares[5][0] = new Bishop(5, 0, 'w', 'w');
       squares[6][0] = new Knight(6, 0, 'w');
       squares[7][0] = new Rook(7, 0, 'w');
       for (int i = 0; i < 8; i++) {
          squares[i][1] = new Pawn(i, 1, 'w');
          squares[i][6] = new Pawn(i, 6, 'b');
       }
       for (int i = 0; i < 8; i++) {
          for (int j = 2; i < 6; i++) {
             squares[i][j] = null;
          }
       }
       squares[0][7] = new Rook(0, 7, 'b');
       squares[1][7] = new Knight(1, 7, 'b');
       squares[2][7] = new Bishop(2, 7, 'b', 'w');
       squares[3][7] = new Queen(3, 7, 'b');
       squares[4][7] = new King(4, 7, 'b');
       squares[5][7] = new Bishop(5, 7, 'b', 'b');
       squares[6][7] = new Knight(6, 7, 'b');
       squares[7][7] = new Rook(7, 7, 'b');
 
       try {
          wTile = ImageIO.read(new File("./images/tile_white.png"));
       } catch (IOException ioe) {
          System.exit(-1);
       }
       try {
          bTile = ImageIO.read(new File("./images/tile_black.png"));
       } catch (IOException ioe) {
          System.exit(-1);
       }
    }
 
    public void drawBoard() {
       for (int row = 7; row >= 0; row--) {
          System.out.format("%d ", row + 1);
          for (int col = 0; col < 8; col++) {
             if (squares[col][row] != null) {
                System.out.print(squares[col][row].getAppearance() + " ");
             } else {
                System.out.print("  ");
             }
          }
          System.out.println();
       }
       System.out.println("  a b c d e f g h ");
       System.out.println();
    }
 
    // MA - this paint method draws the board in a frame and uses the getAppearance method to show
    // where each piece is on the board as a red character for now so it can be seen on both the
    // white and black squares - TODO - change appearance of each piece to an image (possibly)
    @Override
    public void paint(Graphics g) {
       Graphics2D ga = (Graphics2D) g;
       for (int i = 7; i >= 0; i--) {
          for (int j = 7; j >= 0; j--) {
             double leftEdge = squareSize * (i + 1);
             double topEdge = squareSize * (8 - j);
 
             if ((i + j) % 2 == 1) //(Changed) - I may have the white and black squares in the wrong places but they are swapped by changing the 0 here to a 1
             {
                ga.drawImage(wTile, null, (int) leftEdge, (int) topEdge);
 
             } else {
                ga.drawImage(bTile, null, (int) leftEdge, (int) topEdge);
             }
 
             if (squares[i][j] != null) {
                ga.drawImage(squares[i][j].getImage(), null, (int) leftEdge + 10, (int) topEdge + 10);
             }
          }
       }
    }
 
    public boolean isPathClear(Move move) {
       int xDelta = move.newX - move.oldX;
       int yDelta = move.newY - move.oldY;
       int absXDelta = Math.abs(xDelta);
       int absYDelta = Math.abs(yDelta);
       int steps = Math.max(absXDelta, absYDelta);
 
       if (xDelta != 0 && yDelta != 0 && absXDelta != absYDelta) {
          //Not a valid path
          return false;
       } else {
          //Path is at least straight or diagonal
          int xIncrement = (move.newX - move.oldX) / steps;
          int yIncrement = (move.newY - move.oldY) / steps;
          for (int step = 1; step < steps; step++) {
             if (squares[move.oldX + step * xIncrement][move.oldY + step * yIncrement] != null) {
                return false;
             }
          }
       }
       return true;
    }
 
    public boolean isMoveValid(char playerColour, Move move) {
       //check destination isn't the same as origin
       if (move.newX == move.oldX && move.newY == move.oldY) {
          return false;
       }
 
       //check if the piece exists
       if (squares[move.oldX][move.oldY] == null) {
          return false;
       }
 
       //check if the piece belongs to the current player
       if (!squares[move.oldX][move.oldY].isPlayersPiece(playerColour)) {
          return false;
       }
 
       //check the destination piece (if any) doesn't belong to the player
       if (squares[move.newX][move.newY] != null && squares[move.newX][move.newY].isPlayersPiece(playerColour)) {
          return false;
       }
 
       //check if the move is valid
       return squares[move.oldX][move.oldY].isMoveValid(this, move);
    }
 
    public boolean isMoveCapture(Move move) {
       //If there is no piece in the new square then it can't be a capture
       //otherwise it is capture (or not valid for some reason which should be
       //dealt with by isMoveValid() ).
       return (squares[move.newX][move.newY] != null);
    }
 
    public void makeMove(Move move) {
       /* Assuming that everything has been checked by isMoveValid() and
        * isMoveCapture() this function simply replaces the contents of the
        * new square with the contents of the old square and wipes the old
        * square
        */
       squares[move.newX][move.newY] = squares[move.oldX][move.oldY];
       squares[move.oldX][move.oldY] = null;
 
       //Updates the Piece instances position values
       squares[move.newX][move.newY].setPosition(move.newX, move.newY);
 
       //Crude first draft of pawn promotion code
       //Assumes the player wants a queen
       if ((move.newY == 7 || move.newY == 0) && squares[move.newX][move.newY] instanceof Pawn) {
          char colour = squares[move.newX][move.newY].pieceColour();
         squares[move.newX][move.newY] = new Queen(move.newX, move.newY, colour);
       }
    }
 
    public boolean isCapturePossible(char playerColour) {
       // check if any captures are listed in capture list
       return (validCaptures.size() > 0);
    }
 
    public void generateMoves(char playerColour) {
 
       // reset the ArrayLists
       validMoves.clear();
       validCaptures.clear();
 
       // create tempMove that holds each test move
       Move testMove;
 
       // iterate over all moves
       for (int sourceCol = 0; sourceCol < 8; sourceCol++) {
          for (int sourceRow = 0; sourceRow < 8; sourceRow++) {
             remainingPieces.add(squares[sourceCol][sourceRow]);
 
             for (int destCol = 0; destCol < 8; destCol++) {
                for (int destRow = 0; destRow < 8; destRow++) {
                   // initialise testMove
                   testMove = new Move(sourceCol, sourceRow, destCol, destRow);
 
                   // check move validity
                   if (this.isMoveValid(playerColour, testMove)) {
                      // if valid add to valid move list
                      validMoves.add(testMove);
 
                      // check if test move is capture
                      if (this.isMoveCapture(testMove)) {
                         validCaptures.add(testMove);
                      }
                   }
 
                }
             }
          }
       }
    }
 
    public char isWon() {
       //Horribly crude function to check if there are zero black or zero white
       //pieces. Returns ' ' if both players still have pieces, 'w' if white has
       //no pieces and 'b' if black has no pieces.
       int whiteCount = 0;
       int blackCount = 0;
       for (int i = 0; i < 8; i++) {
          for (int j = 0; j < 8; j++) {
             if (squares[i][j] != null) {
                if (squares[i][j].pieceColour() == 'w') {
                   whiteCount++;
                } else {
                   blackCount++;
                }
             }
          }
       }
       if (blackCount == 0) {
          return 'b';
       } else if (whiteCount == 0) {
          return 'w';
       } else {
          return ' ';
       }
    }
 
    public boolean isStaleMate() {
       return false;
    }
 
    public boolean canMove() {
       return (validMoves.size() > 0);
    }
 
    public int isFinished(char playerColour) {
       int previousMoves = validMoves.size();
 
       if (playerColour == 'b') {
          this.generateMoves('w');
       } else if (playerColour == 'w') {
          this.generateMoves('b');
       }
       if (validMoves.size() + previousMoves == 0) {
          return 1;
       }
 
       if (this.isStaleMate()) {
          return 2;
       }
       if (this.isWon() == 'w') {
          return 3;
       }
       if (this.isWon() == 'b') {
          return 4;
       }
       return 0;
 
    }
 }

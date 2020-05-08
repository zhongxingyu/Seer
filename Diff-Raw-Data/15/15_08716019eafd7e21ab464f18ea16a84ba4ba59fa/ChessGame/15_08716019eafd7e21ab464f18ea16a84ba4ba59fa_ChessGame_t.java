 /*
  * The MIT License
  *
  * Copyright 2013 Thomas Schaffer <thomas.schaffer@epitech.eu>.
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package shaft.jnegachess.chess;
 
 import java.util.LinkedList;
 import java.util.List;
import shaft.jnegachess.chess.pieces.*;
import shaft.jnegachess.game.*;
 
 /**
  *
  * @author Thomas Schaffer <thomas.schaffer@epitech.eu>
  */
 public class ChessGame implements IGameTree, IChessBoard {
     private List<IGameTree> _children;
     private int _nodeValue;
     private boolean _wasEval;
    private IChessPiece[][] _board;
     private boolean _player;
     private boolean _isRoot;
     private ChessMove _lastMove;
     private IChessPiece _whiteKing;
     private IChessPiece _blackKing;
     private IGameTreeSolver _AI;
     
     public ChessGame(boolean humanPlayer) {
         _nodeValue = 0;
         _wasEval = false;
         _AI = new NegamaxSolver();
         _player = !humanPlayer;
         _isRoot = true;
         
         defaultBoard();
     }
     
     private void defaultBoard() {
         _player = false;
         _board = new IChessPiece[8][8];
         
         initialPieces(0, false);
         initialPawns(1, false);
         initialPieces(7, true);
         initialPawns(6, true);
         _whiteKing = _board[4][7];
         _blackKing = _board[4][0];
     }
     
     private void initialPawns(int y, boolean color) {
         for (int i = 0; i < 8; i++) {
             _board[i][y] = new Pawn(i, y, color);
         }
     }
     
     private void initialPieces(int y, boolean color) {
         _board[0][y] = new Rook(0, y, color);
         _board[1][y] = new Knight(1, y, color);
         _board[2][y] = new Bishop(2, y, color);
         _board[3][y] = new Queen(3, y, color);
         _board[4][y] = new King(4, y, color);
         _board[5][y] = new Bishop(5, y, color);
         _board[6][y] = new Knight(6, y, color);
         _board[7][y] = new Rook(7, y, color);
     }
     
     private ChessGame(IChessPiece[][] board, boolean player, ChessMove lastMove, IChessPiece wKing, IChessPiece bKing) {
         _nodeValue = 0;
         _wasEval = false;
         _board = board;
         _player = player;
         _lastMove = lastMove;
         _whiteKing = wKing;
         _blackKing = bKing;
     }
     
     @Override
     public IChessPiece pieceAtXY(int x, int y) {
         if (!validChessCoords(x, y)) {
             return null;
         }
         return _board[x][y];
     }
     
     private boolean validChessCoords(int x, int y) {
         if (x < 0 || x > 7 || y < 0 || y > 7) {
             return false;
         }
         return true;
     }
     
     @Override
     public void potentialFutureMove(IChessPiece piece, int x, int y) {
         if (!validChessCoords(x, y)
                 || _board[piece.getX()][piece.getY()] != piece) {
             // Invalid coords or piece does not match
             return;
         }
         
         // Create new board representation for child game state
         IChessPiece newWKing = null;
         IChessPiece newBKing = null;
         IChessPiece[][] newBoard = new IChessPiece[8][8];
         for (int i = 0; i < 8; i++) {
             for (int j = 0; j < 8; j++) {
                 if (_board[i][j] != null) {
                     newBoard[i][j] = _board[i][j].copyPiece();
                     if (newBoard[i][j].getType() == IChessPiece.KING) {
                         if (newBoard[i][j].color()) {
                             newWKing = newBoard[i][j];
                         }
                         else {
                             newBKing = newBoard[i][j];
                         }
                     }
                 }
             }
         }
         boolean capture = (newBoard[x][y] != null);
         newBoard[x][y] = newBoard[piece.getX()][piece.getY()];
         newBoard[piece.getX()][piece.getY()] = null;
         newBoard[x][y].setX(x).setY(y);
 
 
         // Still needed?
         if (newWKing == null || newBKing == null) {
             return;
         }
         
         // Generate new move and game state
         ChessMove move = new ChessMove(piece.getX(), piece.getY(), x, y);
         ChessGame childGameState = new ChessGame(newBoard, !_player, move, newWKing, newBKing);
 
         if (childGameState.getKing(piece.color()).isThreatened(childGameState)) {
             // This move would put or let our King in check, discard it
             return;
         }
         
         piece.addLegalMove(move);
 
 
         if (_children == null) {
             _children = new LinkedList<>();
         }
         // Adding child game state -- ordering for better alpha-beta pruning performance
         if (piece.color() == _player) {
             if (capture) {
                 _children.add(0, childGameState);
             } else {
                 _children.add(childGameState);
             }
         }
     }
     
     @Override
     public int evalNodeValue() {
         int val = 0;
         
         for (int i = 0; i < 8; i++) {
             for (int j = 0; j < 8; j++) {
                 if (_board[i][j] != null) {
                     // Each piece computes its value (material + position)
                     if (!_board[i][j].color()) {
                         val += _board[i][j].getValue();
                     }
                     else {
                         val -= _board[i][j].getValue();
                     }
                 }
             }
         }
         
         // Set estimated value and discard all references so they may be garbage collected
         setNodeValue(val);
         _board = null;
         _lastMove = null;
         _whiteKing = null;
         _blackKing = null;
         return val;
     }
     
     @Override
     public void setNodeValue(int val) {
         _wasEval = true;
         _nodeValue = val;
     }
     
     @Override
     public int getNodeValue() {
         return _nodeValue;
     }
     
     @Override
     public boolean nodeWasEval() {
         return _wasEval;
     }
     
     public IGameTree bestFutureMove() {
         IGameTree ret = null;
         
         // Find the best direct child
         for (IGameTree child : _children) {
             if (child.nodeWasEval()) {
                 if (ret == null || child.getNodeValue() > ret.getNodeValue()) {
                     ret = child;
                 }
             }
         }
         return ret;
     }
     
     @Override
     public List<IGameTree> nextPly() {
         // Generate moves for all pieces by the currently-considered player
         for (int i = 0; i < 8; i++) {
             for (int j = 0; j < 8; j++) {
                 if (_board[i][j] != null && _board[i][j].color() == _player) {
                     _board[i][j].nextMoves(this);
                 }
             }
         }
         
         List<IGameTree> ret = _children;
         
         if (!_isRoot) {
             // If temporary game state (!= tree root), discard all references so they may be garbage collected
             _board = null;
             _whiteKing = null;
             _blackKing = null;
             _children = null;
         }
         
         return ret;
     }
     
     @Override
     public IChessPiece getKing(boolean color) {
         if (color) {
             return _whiteKing;
         }
         return _blackKing;
     }
     
     private void movePiece(IChessPiece piece, int x, int y) {
         // Move one piece
         if (!validChessCoords(x, y)) {
             return;
         }
         if (_board[piece.getX()][piece.getY()] != piece) {
             return;
         }
         _lastMove = new ChessMove(piece.getX(), piece.getY(), x, y);
         _board[piece.getX()][piece.getY()] = null;
         _board[x][y] = piece;
         piece.setX(x).setY(y);
         reinitNode();
     }
     
     private void reinitNode() {
         // Discard everything and reinit node for next move
         if (_children != null) {
             for (IGameTree child : _children) {
                 if (child instanceof ChessGame) {
                     ((ChessGame) child).reinitNode();
                 }
             }
             _children.clear();
         }
         _wasEval = false;
          _nodeValue = 0;
     }
     
     @Override
     public void doMove(ChessMove move) {
         if (move == null) {
             return;
         }
         IChessPiece movingPiece = _board[move.getOrigX()][move.getOrigY()];
         
         if (movingPiece == null) {
             return;
         }
         movePiece(movingPiece, move.getDestX(), move.getDestY());
     }
     
     @Override
     public ChessMove lastMove() {
         return _lastMove;
     }
     
     @Override
     public boolean safeSquare(boolean color, int x, int y) {
         // Ask any enemy piece (!color) if they threaten the given coords
         for (int i = 0; i < 8; i++) {
             for (int j = 0; j < 8; j++) {
                 if (_board[i][j] != null
                         && _board[i][j].color() == !color
                         && _board[i][j].threatens(this, x, y)) {
                     return false;
                 }
             }
         }
         return true;
     }
     
     @Override
     public ChessMove AIMove() {
         // Invoke the game tree solver
         _AI.solve(this, 5);
 
         // If AI found a move, do it in the game engine and return it
         IGameTree child = bestFutureMove();
         ChessMove nextMove = null;
         if (child instanceof ChessGame) {
             nextMove = ((ChessGame) child).lastMove();
         }
         doMove(nextMove);
         return nextMove;
     }
     
     @Override
     public boolean AIColor() {
         return _player;
     }
     
     public boolean humanCanMove() {
         // Check whether opponent has at least one legal move
         for (int i = 0; i < 8; i++) {
             for (int j =0; j < 8; j++) {
                 if (_board[i][j] != null && _board[i][j].color() == !_player) {
                     if (_board[i][j].getLegalMoves(this).size() > 0) {
                         return true;
                     }
                 }
             }
         }
         return false;
     }
 }

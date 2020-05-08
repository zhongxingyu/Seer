 package pl.edu.agh.draughts.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.GridLayout;
 
 import javax.swing.JPanel;
 
 import pl.edu.agh.draughts.game.elements.Piece;
 
 public class ChessboardPanel extends JPanel {
 
     private static final long serialVersionUID = 1L;
 
     private final int rows;
     private final int sizeForEachCell;
 
     public ChessboardPanel(int rows, int sizeForEachCell) {
         super();
         this.rows = rows;
         this.sizeForEachCell = sizeForEachCell;
         setLayout(new GridLayout(rows, rows));
         for (int i = 0; i < rows * rows; i++) {
            ChessboardCell square = new ChessboardCell(new BorderLayout(), i % rows, i / rows,
                     CellColor.values()[((i / rows) % 2 + (i % 2)) % 2]);
             add(square);
         }
     }
 
     public void addPieces(Piece[][] pieces) {
         for (int row = 0; row < rows; row++) {
             for (int column = 0; column < rows; column++) {
                 Piece piece = pieces[row][column];
                 if (piece != null) {
                     getChessboardCell(row, column).add(
                             new PawnLabel(piece.getPieceColor(), column, row, sizeForEachCell));
                 }
             }
         }
     }
     
     public void clearPieces() {
         for (int row = 0; row < rows; row++) {
             for (int column = 0; column < rows; column++) {
                 getChessboardCell(row, column);
             }
         }
     }
 
     public ChessboardCell getChessboardCell(int row, int column) {
         return (ChessboardCell) getComponent(row * rows + column);
     }
 
     @Override
     public Dimension getPreferredSize() {
         return new Dimension(rows * sizeForEachCell, rows * sizeForEachCell);
     }
 
 }

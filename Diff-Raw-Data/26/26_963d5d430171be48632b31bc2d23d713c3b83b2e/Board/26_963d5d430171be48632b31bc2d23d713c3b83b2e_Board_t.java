 package experimental.chess;
 
 import javax.swing.*;
 import javax.swing.border.Border;
 import java.awt.*;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 
 import static experimental.chess.Board.PieceType.*;
 
 /**
  * @author gmatoga
  */
 public class Board {
 
     public static final char WHITE_CHESS_KING = '\u2654';
     public static final char WHITE_CHESS_QUEEN = '\u2655';
     public static final char WHITE_CHESS_ROOK = '\u2656';
     public static final char WHITE_CHESS_BISHOP = '\u2657';
     public static final char WHITE_CHESS_KNIGHT = '\u2658';
     public static final char WHITE_CHESS_PAWN = '\u2659';
     public static final char BLACK_CHESS_KING = '\u265a';
     public static final char BLACK_CHESS_QUEEN = '\u265b';
     public static final char BLACK_CHESS_ROOK = '\u265c';
     public static final char BLACK_CHESS_BISHOP = '\u265d';
     public static final char BLACK_CHESS_KNIGHT = '\u265e';
     public static final char BLACK_CHESS_PAWN = '\u265f';
     public PieceType[] firstRow = {ROOK, KNIGHT, BISHOP, QUEEN, KING, BISHOP, KNIGHT, ROOK};
 
     public static void main(String... args) throws Exception {
         UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 
         JOptionPane.showMessageDialog(null, new Board().construct());
     }
 
     private JComponent pieceAt(int i, int j) {
         boolean darkField = (i + (j * 7)) % 2 == 0;
         boolean whitePlayer = i < 4;
         final JComponent piece = boardPiece(darkField, whitePlayer, i, j);
         return piece;
     }
 
     private JComponent boardPiece(boolean darkField, boolean whitePlayer, int row, int column) {
         final JComponent piece = boardPiece(row, column);
        Color fieldColor = darkField ? Color.gray : Color.lightGray;
        Color pieceColor = whitePlayer ? Color.white : Color.darkGray;
         piece.setForeground(pieceColor);
         piece.setBackground(fieldColor);
         setBorder(fieldColor, piece);
         setFocusListener(piece);
         return piece;
     }
 
 
     private void setFocusListener(final JComponent piece) {
         piece.addMouseListener(new MouseAdapter() {
             @Override
             public void mouseEntered(MouseEvent e) {
                 setBorder(Color.ORANGE, piece);
             }
 
             @Override
             public void mouseExited(MouseEvent e) {
                 setBorder(piece.getBackground(), piece);
             }
         });
     }
 
 
     public void setBorder(Color color, JComponent piece) {
         final Border border = BorderFactory.createLineBorder(color, 3);
         piece.setBorder(border);
     }
 
     private JComponent boardPiece(int row, int column) {
         final PieceType pieceType = initialPieceSetup(row, column);
         final char chessPieceEmblem = row > 3 ? pieceType.blackPieceCharacter : pieceType.whitePieceCharacter;
         final JButton boardPiece = new JButton("" + chessPieceEmblem);
         boardPiece.setMargin(new Insets(0, 60, 0, 60));
         boardPiece.setContentAreaFilled(false);
         boardPiece.setFont(boardPiece.getFont().deriveFont(40.0f));
         boardPiece.setOpaque(true);
         boardPiece.setForeground(Color.white);
         boardPiece.setBackground(Color.DARK_GRAY);
         return boardPiece;
     }
 
     public JPanel construct() {
         JPanel board = new JPanel(new GridLayout(8, 0, 0, 0));
        for (int i = 0; i < 8; i++)
            for (int j = 0; j < 8; j++)
                 board.add(pieceAt(i, j));
         return board;
     }
 
     public PieceType initialPieceSetup(int row, int column) {
         if (row > 1 && row < 6) return NONE;
        if (row > 3) row = 7 - row;
        if (row == 1) return PAWN;
         return firstRow[column];
     }
 
     public static enum PieceType {
         KING(WHITE_CHESS_KING, BLACK_CHESS_KING),
         QUEEN(WHITE_CHESS_QUEEN, BLACK_CHESS_QUEEN),
         ROOK(WHITE_CHESS_ROOK, BLACK_CHESS_ROOK),
         BISHOP(WHITE_CHESS_BISHOP, BLACK_CHESS_BISHOP),
         KNIGHT(WHITE_CHESS_KNIGHT, BLACK_CHESS_KNIGHT),
         PAWN(WHITE_CHESS_PAWN, BLACK_CHESS_PAWN),
         NONE(' ', ' ');
         public final char whitePieceCharacter;
         public final char blackPieceCharacter;
 
         PieceType(char whitePieceCharacter, char blackPieceCharacter) {
             this.whitePieceCharacter = whitePieceCharacter;
             this.blackPieceCharacter = blackPieceCharacter;
         }
     }
 
 
 }

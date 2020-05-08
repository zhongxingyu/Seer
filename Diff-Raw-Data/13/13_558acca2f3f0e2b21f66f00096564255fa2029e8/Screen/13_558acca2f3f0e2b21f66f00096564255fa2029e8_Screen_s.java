 
 import java.awt.*;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.io.File;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 import javax.swing.*;
 
 public class Screen extends JPanel {
   private static final long serialVersionUID = 1L;
 
   private Image _bg, _selector, _gameOverImg;
   private Board _board;
   private int _clickX, _clickY;
   private boolean _gameOver, _drawSelector;
   private int _selectorX, _selectorY;
   
   public Screen() throws IOException {
     _gameOver = false;
    _bg = ImageIO.read(new File("img/background.png"));
    _selector = ImageIO.read(new File("img/selector.png"));
    _gameOverImg = ImageIO.read(new File("img/gameover.png"));
     _drawSelector = false;
     _clickX = -1;
     _clickY = -1;
     
     this.addMouseListener(new MouseListener() {
       public void mouseReleased(MouseEvent e) {
         setLastClickPos(e.getX(),e.getY());
       }
 
       public void mousePressed(MouseEvent e) {
       }
 
       public void mouseExited(MouseEvent e) {
       }
 
       public void mouseEntered(MouseEvent e) {
       }
 
       public void mouseClicked(MouseEvent e) {
       }
     });
   }
   
   @Override
   public void paintComponent(Graphics g) {
     super.paintComponent(g);
     
     //paint the chess board
     g.drawImage(_bg, 0, 0, null);
     
     //paint the selector
     if (_drawSelector && !_board.emptyAt(_selectorX/Cell.SIZE, _selectorY/Cell.SIZE)) {
       g.drawImage(_selector, _selectorX, _selectorY, null);
     }
     
     //paint the pieces
     int x, y;
     for (y = 0; y < Board.SIZE; y++) {
       for (x = 0; x < Board.SIZE; x++) {
         if (!_board.emptyAt(x, y)) {
           g.drawImage(_board.getPieceImageAt(x, y), x*Cell.SIZE, y*Cell.SIZE, null);
         }
       }
     }
     
     if (_gameOver) {
       x = getWidth()/2 - _gameOverImg.getWidth(null)/2;
       y = getHeight()/2 - _gameOverImg.getHeight(null)/2;
       g.drawImage(_gameOverImg, x, y, null);
     }
     
   }
 
   public void setBoard(Board chessBoard) {
     _board = chessBoard;
   }
   
   private void setLastClickPos(int x, int y) {
     _clickX = x;
     _clickY = y;
   }
   
   public int[] getLastClickPos() {
     int[] result = new int[]{_clickX, _clickY};
     _clickX = -1;
     _clickY = -1;
     return result;
     
   }
 
   public void displayGameOver() {
     _gameOver = true;
     
   }
 
   public void addSelector(int x, int y) {
     _drawSelector = true;
     _selectorX = x*Cell.SIZE;
     _selectorY = y*Cell.SIZE;
     repaint();
   }
 
   public void removeSelector() {
     _drawSelector = false;
     repaint();
   }
   
 }

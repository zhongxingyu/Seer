 package j3chess.gui;
 
 import j3chess.Field;
 import j3chess.J3ChessApp;
 import j3chess.utility.Vector2d;
 
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.image.BufferedImage;
 
 import javax.swing.JComponent;
 
 /**
  * Panel for the Chessboard. Draws the Chessboard and all pieces. Implements the
  * Mouselistener.
  */
 public class DrawPanel extends JComponent implements MouseListener {
 
     /**
      * @brief height of the Drawpanel
      */
     private final int mHeight;
     /**
      * @brief width of the Drawpanel
      */
     private final int mWidth;
     /**
      * @brief Background Image of the DrawPanel (the Chessboard)
      */
     private final BufferedImage mImg;
 
     /**
      * Constructor of the DrawPanel.
      * @param sizeX
      *            width of the drawPanel
      * @param sizeY
      *            height of the drawPanel
      */
     public DrawPanel(final int sizeX, final int sizeY) {
         setPreferredSize(new Dimension(sizeX, sizeY));
         mWidth = sizeX;
         mHeight = sizeY;
         mImg = new BufferedImage(sizeX, sizeY, BufferedImage.TYPE_INT_ARGB);
         mImg.getGraphics();
         this.addMouseListener(this);
     }
 
     @Override
     public final void paintComponent(final Graphics g) {
         super.paintComponent(g);
         J3ChessApp.getInstance().update();
         g.drawImage(mImg, 0, 0, null);
     }
 
     public Graphics2D getPersistentGraphics() {
         return (Graphics2D) mImg.getGraphics();
     }
 
     @Override
     public void mouseClicked(final MouseEvent arg0) {
         // TODO Auto-generated method stub
     }
 
     @Override
     public void mouseEntered(final MouseEvent arg0) {
         // TODO Auto-generated method stub
     }
 
     @Override
     public void mouseExited(final MouseEvent arg0) {
         // TODO Auto-generated method stub
     }
 
     @Override
     public void mousePressed(final MouseEvent arg0) {
         // Resize x and y to [-1,1] range
         final Vector2d offset = new Vector2d(
                 (mWidth - J3ChessView.CHESSBOARDWIDTH) / 2.0f,
                 (mHeight - J3ChessView.CHESSBOARDHEIGHT) / 2.0f);
         final float x = ((arg0.getX() - offset.x) / J3ChessView.CHESSBOARDWIDTH) * 2.0f - 1.0f;
         final float y = ((arg0.getY() - offset.y) / J3ChessView.CHESSBOARDHEIGHT) * 2.0f - 1.0f;
 
         // Get the field that the player clicked on
         final Field clickedField = J3ChessApp.getInstance().getGame()
                 .getChessboard().getFieldByXY(x, y);
 
         // Notify the game about a clicked field
         if (clickedField != null) {
             J3ChessApp.getInstance().getGame().notifyFieldClicked(clickedField);
         }
 
         // Make changes visible
         repaint();
     }
 
     @Override
     public void mouseReleased(final MouseEvent arg0) {
         // TODO Auto-generated method stub
     }
 
 }

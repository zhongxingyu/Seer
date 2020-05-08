 package gui;
 
 import game.Status;
import org.junit.experimental.theories.DataPoints;
 
 import javax.imageio.ImageIO;
 import javax.swing.*;
 
 import java.awt.*;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 
 public class CustomCellButton extends JButton {
 
     private final int x;
     private final int y;
     private final int num;
 
     public CustomCellButton(final int x, final int y, final int num) {
         this.x = x;
         this.y = y;
         this.num = num;
 
         setAttributes();
 
         addMouseListener(getMouseListener());
 
     }
 
     private void setAttributes() {
         setPreferredSize(new Dimension(41, 41));
         setContentAreaFilled(false);
         setFocusPainted(false);
         setBorderPainted(true);
         setOpaque(true);
         setContentAreaFilled(false);
         setBackground(Color.lightGray);
     }
 
     private MouseListener getMouseListener() {
         return new MouseListener() {
 
             public void mouseClicked(MouseEvent mouseEvent) {
 
                 if (SwingUtilities.isRightMouseButton(mouseEvent)) {
                     // Right-click
                     if ("".equals(getText())) {
                         if (!MinesweeperFrame.gameOver) {
 
                             if (getBackground() == Color.BLUE) {
                                 setBackground(Color.lightGray);
                                 MinesweeperFrame.bombGuess[x][y] = false;
                                 MinesweeperFrame.bombCount++;
                             } else if (getBackground() == Color.lightGray) {
                                 setBackground(Color.BLUE);
                                 MinesweeperFrame.bombGuess[x][y] = true;
                                 MinesweeperFrame.bombCount--;
                             }
 
                             if (MinesweeperFrame.bombCount == MinesweeperFrame.board.getBombs()) {
                                 // Check if all the bombs are found
                                 Point[] bombList = MinesweeperFrame.board.getBombList();
                                 boolean done = true;
                                 for (int i = 0; i < bombList.length; i++) {
                                     Point point = bombList[i];
                                     int xValue = (int) point.getX();
                                     int yValue = (int) point.getY();
                                     if (!MinesweeperFrame.bombGuess[xValue][yValue]) {
                                         done = false;
                                     }
 
                                 }
                                 if (done) {
                                     MinesweeperFrame.gameOver = true;
                                     MinesweeperFrame.win();
                                 }
                             }
                         }
 
                     }
 
                 } else {
                     // Left-click
 
                     // Check if all the bombs are found
 
                     if (!(MinesweeperFrame.gameOver) && !(getBackground() == Color.BLUE) && (getText().isEmpty())) {
                         switch (num) {
                             case -1:
                                 MinesweeperFrame.gameOver = true;
                                 MinesweeperFrame.explodeBombs();
                                 break;
                             case 0:
                                 if (!MinesweeperFrame.gameOver) {
                                     setText("" + num);
                                 }
                                 break;
                             default:
                                 if (!MinesweeperFrame.gameOver) {
                                     setText("" + num);
                                 }
                                 break;
                         }
 
                         Status.numCount--;
 
                         if (Status.numCount == 0) {
                             MinesweeperFrame.gameOver = true;
                             MinesweeperFrame.win();
                         }
                     }
                 }
             }
 
            public void mousePressed(MouseEvent mouseEvent) {
            }

            public void mouseReleased(MouseEvent mouseEvent) {
            }

             public void mouseEntered(MouseEvent mouseEvent) {
             }
 
             public void mouseExited(MouseEvent mouseEvent) {
             }
 
         };
 
     }
 
     // @Override
     // protected void paintComponent(Graphics g) {
     // g.setColor(Color.BLUE);
     //
     // // GradientPaint paint = new GradientPaint(getWidth(), getHeight(),
     // g.getColor(),
     // // getWidth(), getHeight(), Color.GREEN, false);
     //
     // Graphics2D g2 = (Graphics2D) g;
     // // g2.setPaint(paint);
     // // g2.fillRect(0,0,38,38);
     // super.paintComponent(g);
     // }
 
 //	@Override
 //	protected void paintBorder(Graphics g) {
 //		g.setColor(Color.BLACK);
 //		g.drawRect(0, 0, (int) getPreferredSize().getWidth(),
 //				(int) getPreferredSize().getHeight());
 //	}
 }

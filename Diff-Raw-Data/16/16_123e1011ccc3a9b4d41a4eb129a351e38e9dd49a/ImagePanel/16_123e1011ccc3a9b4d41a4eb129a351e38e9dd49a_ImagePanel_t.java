 /**
  * File Name: ImagePanel.java
  *
  * Author: Kiet Lam
  *
  * Date Modified: 4/05/2012
  *
  */
 
 
 import java.awt.*;
 import java.awt.event.*;
 import java.awt.image.*;
 import java.awt.geom.*;
 import javax.swing.*;
 import javax.swing.event.*;
 
 
 public class ImagePanel extends JPanel implements KeyListener,
                                                   MouseListener,
                                                   MouseMotionListener
 {
     final int xStepRate = 10;
     final int yStepRate = 10;
 
     private Picture originalForegroundImage;
     private Picture foregroundImage;
     private Picture backgroundImage;
     private Picture finalImage;
     private int     foregroundX;
     private int     foregroundY;
     private int     initForegroundX;
     private int     initForegroundY;
     private int     foregroundW;
     private int     foregroundH;
     private int     initForegroundW;
     private int     initForegroundH;
 
     private int     startDragX;
     private int     startDragY;
     private int     inDragX;
     private int     inDragY;
     private boolean inDrag;
 
 
     public ImagePanel()
     {
         originalForegroundImage = null;
 
         backgroundImage = null;
         foregroundImage = null;
         finalImage      = null;
         foregroundX     = 120;
         foregroundY     = 120;
         initForegroundX = foregroundX;
         initForegroundY = foregroundY;
         foregroundW     = 240;
         foregroundH     = 120;
         initForegroundW = foregroundW;
         initForegroundH = foregroundH;
         startDragX      = -1;
         startDragY      = -1;
         inDragX         = -1;
         inDragY         = -1;
         inDrag          = false;
 
     }
 
     public Picture getFinalImage()
     {
         process();
         return finalImage;
     }
 
     public void setBackgroundImage(Picture _backgroundImage)
     {
         backgroundImage = resizeImage(_backgroundImage, getWidth(), getHeight());
         process();
     }
 
     public Picture getBackgroundImage()
     {
         return backgroundImage;
     }
 
     public void setForegroundImage(Picture _foregroundImage)
     {
         originalForegroundImage = _foregroundImage;
         foregroundImage         = resizeImage(originalForegroundImage, foregroundW, foregroundH);
         process();
     }
 
     public Picture getForegroundImage()
     {
         return foregroundImage;
     }
 
     public void adjustForegroundWidth(int w)
     {
         if (foregroundImage == null)
         {
             return;
         }
 
         if (foregroundX + initForegroundW + w > backgroundImage.getWidth())
         {
             return;
         }
 
         foregroundW     = initForegroundW + w;
         foregroundImage = resizeImage(originalForegroundImage, foregroundW, foregroundH);
     }
 
     public void adjustForegroundHeight(int h)
     {
         if (foregroundImage == null)
         {
             return;
         }
 
         if (foregroundY + initForegroundH + h > backgroundImage.getHeight())
         {
             return;
         }
 
         foregroundH     = initForegroundH + h;
         foregroundImage = resizeImage(originalForegroundImage, foregroundW, foregroundH);
     }
 
     public void process()
     {
         if (foregroundImage == null)
         {
             if (backgroundImage == null)
             {
                 return;
             }
             else
             {
                 finalImage = backgroundImage;
                 repaint();
                 return;
             }
         }
 
         Picture tempImage = new Picture(backgroundImage);
 
         Pixel[] backPixel = tempImage.getPixels();
         Pixel[] forePixel = foregroundImage.getPixels();
 
        int w = foregroundImage.getWidth();
        int h = foregroundImage.getHeight();

        for (int i = foregroundY, j = 0; i < foregroundY + h; ++i, ++j)
         {
            for (int ii = foregroundX, jj = 0; ii < foregroundX + w; ++ii, ++jj)
             {
                Pixel pix = forePixel[(j * w) + jj];
 
                 if (!(pix.getRed() + pix.getBlue() < pix.getGreen()))
                 {
                    backPixel[i * backgroundImage.getWidth() + ii].setColor(pix.getColor());                }
             }
         }
 
         finalImage = tempImage;
 
         repaint();
     }
 
     private Picture resizeImage(Picture _image, int w, int h)
     {
         int imageW = _image.getWidth();
         int imageH = _image.getHeight();
 
         return _image.scale(((double) w) / imageW, ((double) h) / imageH);
     }
 
     private Pixel[][] getPixelArray(Picture _image)
     {
         int w = _image.getWidth();
         int h = _image.getHeight();
 
         Pixel[][] arPixel = new Pixel[w][h];
 
         for (int i = 0; i < w; ++i)
         {
             for (int ii = 0; ii < h; ++ii)
             {
                 arPixel[i][ii] = _image.getPixel(i, ii);
             }
         }
 
         return arPixel;
     }
 
     public void paintComponent(Graphics g)
     {
         if (finalImage != null)
         {
             g.drawImage(finalImage.getBufferedImage(), 0, 0, null);
         }
     }
 
     public void keyPressed(KeyEvent e)
     {
         switch(e.getKeyCode())
         {
         case KeyEvent.VK_UP:
             if (foregroundY > yStepRate)
             {
                 foregroundY -= yStepRate;
             }
             break;
         case KeyEvent.VK_DOWN:
             if (foregroundY + foregroundH + yStepRate < backgroundImage.getHeight())
             {
                 foregroundY += yStepRate;
             }
             break;
         case KeyEvent.VK_RIGHT:
             if (foregroundX + foregroundW + xStepRate < backgroundImage.getWidth())
             {
                 foregroundX += xStepRate;
             }
             break;
         case KeyEvent.VK_LEFT:
             if (foregroundX > xStepRate)
             {
                 foregroundX -= xStepRate;
             }
             break;
         }
 
         process();
     }
 
     public void keyReleased(KeyEvent e)
     {
 
     }
 
     public void keyTyped(KeyEvent e)
     {
 
     }
 
     public void mouseClicked(MouseEvent e)
     {
     }
 
     public void mouseEntered(MouseEvent e)
     {
     }
 
     public void mouseExited(MouseEvent e)
     {
     }
 
     public void mousePressed(MouseEvent e)
     {
         Point p = e.getPoint();
         startDragX = p.x;
         startDragY = p.y;
 
         if (startDragX >= foregroundX && startDragX < foregroundX + foregroundW
             && startDragY >= foregroundY && startDragY < foregroundY + foregroundH)
         {
             inDrag = true;
         }
         else
         {
             inDrag = false;
         }
     }
 
     public void mouseReleased(MouseEvent e)
     {
         initForegroundX = foregroundX;
         initForegroundY = foregroundY;
         inDrag = false;
     }
 
     public void mouseMoved(MouseEvent e)
     {
     }
 
     public void mouseDragged(MouseEvent e)
     {
         Point p = e.getPoint();
         inDragX = p.x;
         inDragY = p.y;
 
         int changeX = inDragX - startDragX;
         int changeY = inDragY - startDragY;
 
         if (inDrag)
         {
             if (foregroundX + changeX + foregroundW < backgroundImage.getWidth()
                 && foregroundX + changeX > 0
                 && foregroundY + changeY + foregroundH < backgroundImage.getHeight()
                 && foregroundY + changeY > 0)
             {
                 foregroundX = initForegroundX + changeX;
                 foregroundY = initForegroundY + changeY;
 
                 process();
             }
         }
     }
 }

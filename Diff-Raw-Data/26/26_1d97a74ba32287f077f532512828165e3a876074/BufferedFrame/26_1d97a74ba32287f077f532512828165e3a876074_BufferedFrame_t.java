 package graphics;
 
 import resources.Sprite;
 import game.clock;
 import java.awt.Color;
 import java.awt.Frame;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.util.List;
 import java.awt.image.BufferStrategy;
 import java.util.ArrayList;
 
 /**
  * @author YellowLineSoftworks
  */
 
 //This class will contain all the main functions for displaying stuff.
 public class BufferedFrame extends Frame {
     
     public static BufferedFrame frame;
     private List<Sprite> sprites = new ArrayList();
     private BufferStrategy buffer;
     private Graphics graphics;
     private boolean fpscounter = false;
     private clock clock;
     
     public BufferedFrame(int width, int height, String title) {
         frame = this;
         frame.setTitle(title);
         frame.setSize(width, height);
         frame.setVisible(true);
         frame.createBufferStrategy(2);
         buffer = frame.getBufferStrategy();
     }
     
     public void enableFpsCounter(clock clock) {
         this.clock = clock;
         fpscounter = true;
     }
     
     public void disableFpsCounter() {
         fpscounter = false;
     }
     
     public void render() {
         graphics = buffer.getDrawGraphics();
         graphics.setColor(new Color (255,255,255));
         graphics.fillRect(0, 0, frame.getWidth(), frame.getHeight());
         for (Sprite temp : sprites) {
             graphics.drawImage(temp.image, temp.x1, temp.y1, temp.x2, temp.y2, temp.sx1, temp.sy1, temp.sx2, temp.sy2, null);
 	}
         if (fpscounter) {
             graphics.setColor(new Color (0,0,0));
             graphics.drawString(clock.fps + "", 20, 50);
         } 
         graphics.dispose();
         buffer.show();
     }
     
     public int drawImage(Image image, int x, int y) {
         sprites.add(new Sprite(image, x, y));
        x += 8;
        y += 30;
         return sprites.get(sprites.size() - 1).id;
     }
     
     public int drawImage(Image image, int x, int y, int endx, int endy) {
         sprites.add(new Sprite(image, x, y, endx, endy));
        x += 8;
        y += 30;
         return sprites.get(sprites.size() - 1).id;
     }
     
     public int drawImage(Image image, int x, int y, int endx, int endy, int srcx1, int srcy1, int srcx2, int srcy2) {
         sprites.add(new Sprite(image, x, y, endx, endy, srcx1, srcy1, srcx2, srcy2));
        x += 8;
        y += 30;
         return sprites.get(sprites.size() - 1).id;
     }
     
     public void moveImage(int id, int x, int y) {
        x += 8;
        y += 30;
         for (Sprite temp: sprites) {
             if (temp.id == id) {
                 temp.x1 = x;
                 temp.x2 = temp.image.getWidth(null) + x;
                 temp.y1 = y;
                 temp.y2 = temp.image.getHeight(null) + y;
             }
         }
     }
     
     public void moveImage(int id, int x, int y, int endx, int endy) {
        x += 8;
        y += 30;
         for (Sprite temp: sprites) {
             if (temp.id == id) {
                 temp.x1 = x;
                 temp.x2 = endx;
                 temp.y1 = y;
                 temp.y2 = endy;
             }
         }
     }
     
     public void moveImage(int id, int x, int y, int endx, int endy, int srx1, int sry1, int srx2, int sry2) {
        x += 8;
        y += 30;
         for (Sprite temp: sprites) {
             if (temp.id == id) {
                 temp.x1 = x;
                 temp.x2 = endx;
                 temp.y1 = y;
                 temp.y2 = endy;
                 temp.sx1 = srx1;
                 temp.sy1 = sry1;
                 temp.sx2 = srx2;
                 temp.sy2 = sry2;
             }
         }
     }
     
     public void moveImage(Image image, int x, int y) {
        x += 8;
        y += 30;
         for (Sprite temp: sprites) {
             if (temp.image == image) {
                 temp.x1 = x;
                 temp.x2 = temp.image.getWidth(null) + x;
                 temp.y1 = y;
                 temp.y2 = temp.image.getHeight(null) + y;
             }
         }
     }
     
     public void moveImage(Image image, int x, int y, int endx, int endy) {
        x += 8;
        y += 30;
         for (Sprite temp: sprites) {
             if (temp.image == image) {
                 temp.x1 = x;
                 temp.x2 = endx;
                 temp.y1 = y;
                 temp.y2 = endy;
             }
         }
     }
     
     public void moveImage(Image image, int x, int y, int endx, int endy, int srx1, int sry1, int srx2, int sry2) {
        x += 8;
        y += 30;
         for (Sprite temp: sprites) {
             if (temp.image == image) {
                 temp.x1 = x;
                 temp.x2 = endx;
                 temp.y1 = y;
                 temp.y2 = endy;
                 temp.sx1 = srx1;
                 temp.sy1 = sry1;
                 temp.sx2 = srx2;
                 temp.sy2 = sry2;
             }
         }
     }
     
     public void removeImage(int id) {
         for (Sprite temp: sprites) {
             if (temp.id == id) {
                 sprites.remove(temp);
             }
         }
     }
     
     public void removeImage(Image img) {
         for (Sprite temp: sprites) {
             if (temp.image == img) {
                 sprites.remove(temp);
             }
         }
     }
     
 }

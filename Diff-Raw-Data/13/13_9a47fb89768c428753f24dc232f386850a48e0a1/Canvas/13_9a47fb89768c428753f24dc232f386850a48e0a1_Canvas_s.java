 package vooga.rts.gui;
 
 import java.awt.Color;
 import java.awt.Cursor;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.Toolkit;
 import java.awt.image.BufferStrategy;
 import java.awt.image.BufferedImage;
 import javax.swing.JPanel;
 import vooga.rts.resourcemanager.ResourceLoader;
 import vooga.rts.resourcemanager.ResourceManager;
 
 
 /**
  * 
  * 
  * @author Jonathan Schmidt
  * 
  */
 public class Canvas extends JPanel {
     private static final long serialVersionUID = 5413541892150493064L;
     BufferStrategy myStrategy;
     private Graphics2D myGraphics;
 
     public Canvas (BufferStrategy buffer) {
         super();
         myStrategy = buffer;
         setBackground(Color.BLACK);
         Toolkit toolkit = Toolkit.getDefaultToolkit();
         BufferedImage cursor =
                 ResourceManager.getInstance().<BufferedImage> getFile("cursor.png",
                                                                       BufferedImage.class);
         Cursor c = toolkit.createCustomCursor(cursor, new Point(0, 0), "img");
        this.setCursor(c);
         
     }
 
     public Graphics2D getGraphics () {
         myGraphics = (Graphics2D) myStrategy.getDrawGraphics();
         myGraphics.draw(new Rectangle());
         return myGraphics;
     }
 
     public void render () {
         if (!myStrategy.contentsLost()) {
             myGraphics.dispose();
             myStrategy.show();
         }
     }
 }

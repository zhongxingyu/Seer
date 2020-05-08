 package screens.controls;
 
 import javax.swing.*;
 import java.awt.*;
 
 /**
  * Job : Understands to display the picture as background of panel.
  */
 public class ImagePanel extends JPanel {
     private Image img;
 
     public ImagePanel(Image img) {
         this.img = img;
        Dimension size = new Dimension(img.getWidth(null), img.getHeight(null));
         setPreferredSize(size);
         setMinimumSize(size);
         setMaximumSize(size);
         setSize(size);
         setLayout(null);
     }
 
     public void paintComponent(Graphics g) {
         g.drawImage(img, 0, 0, null);
     }
 }

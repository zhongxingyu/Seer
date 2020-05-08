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
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension size = new Dimension(screenSize.width, screenSize.height);
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

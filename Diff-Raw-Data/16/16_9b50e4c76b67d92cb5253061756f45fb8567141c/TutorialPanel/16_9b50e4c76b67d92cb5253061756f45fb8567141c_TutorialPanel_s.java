 package gui;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.GridLayout;
 import java.awt.Image;
 import javax.swing.ImageIcon;
 
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 /**
  *
  * @author Haisin Yip
  */
 
 public class TutorialPanel extends JPanel
 {   
     private Image img;
     
     public TutorialPanel(String img) {
         this(new ImageIcon(img).getImage());
     }
     
     public TutorialPanel(Image img) {
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

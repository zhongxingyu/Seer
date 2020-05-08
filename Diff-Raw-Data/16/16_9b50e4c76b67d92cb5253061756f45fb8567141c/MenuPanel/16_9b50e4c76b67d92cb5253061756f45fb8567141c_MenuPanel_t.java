 package gui;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.GridLayout;
 import java.awt.Image;
 import java.awt.Toolkit;
 import javax.swing.ImageIcon;
 
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 /**
  *
  * @author Haisin Yip, Anthony Chin
  */
 
 public class MenuPanel extends JPanel
 {
 //    private Image img = new ImageIcon("./src/images/asteroids.jpg").getImage();
       private Image img = Toolkit.getDefaultToolkit().createImage("./src/images/asteroids.jpg");
   
     public MenuPanel() {
         
         //this.img = new ImageIcon("./src/images/asteroids.jpg").getImage();
         Dimension size = new Dimension(img.getWidth(null), img.getHeight(null));
         setPreferredSize(size);
         setMinimumSize(size);
         setMaximumSize(size);
         setSize(size);
         //setLayout(null);
     }
     @Override
     public void paintComponent(Graphics g) {
         //g.drawImage(img, 0, 0, null);
        g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
     }
 }

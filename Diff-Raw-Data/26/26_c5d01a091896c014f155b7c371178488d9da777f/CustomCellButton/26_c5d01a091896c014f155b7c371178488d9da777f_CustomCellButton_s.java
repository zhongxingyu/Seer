 package gui;
 
 import javax.imageio.ImageIO;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import java.awt.*;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 
 public class CustomCellButton extends JButton {
 
     public CustomCellButton() {
         setPreferredSize(new Dimension(41,41));
         setContentAreaFilled(false);
         setFocusPainted(false);
         setBorderPainted(false);
         setOpaque(false);
//        try {
//        	Class<? extends CustomCellButton> s = getClass();
//        	Image img = ImageIO.read(getClass().getResource("resources/bomb.jpg"));
//        	setIcon(new ImageIcon(img));
//        } catch (IOException e) {
//        	System.out.println("Can't get the image.");
//        }
        
     }
 
     @Override
     protected void paintComponent(Graphics g) {
         g.setColor(Color.BLUE);
 
         GradientPaint paint = new GradientPaint(0, 0, g.getColor(),
                 0, 0, Color.GREEN, false);
 
         Graphics2D g2 = (Graphics2D) g;
         g2.setPaint(paint);
 //        g2.fillRect(0,0,38,38);
         super.paintComponent(g);
     }
 
     @Override
     protected void paintBorder(Graphics g) {
         g.setColor(Color.BLACK);
         g.drawRect(0, 0, (int) getPreferredSize().getWidth(), (int) getPreferredSize().getHeight());
     }
 }

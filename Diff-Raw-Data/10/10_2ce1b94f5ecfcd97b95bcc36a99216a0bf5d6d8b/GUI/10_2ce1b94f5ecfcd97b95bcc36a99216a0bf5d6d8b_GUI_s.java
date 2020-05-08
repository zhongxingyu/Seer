 import javax.swing.*;
 import java.awt.*;
 import java.awt.image.*;
 import javax.swing.event.ChangeListener; 
 
 public class GUI extends JPanel
 {
    Dimension d;
     int width, height;
     BufferedImage image;
     JLabel imageL;
     Graphics myG;
     
     public GUI(Dimension a) throws Exception
 	{
     	d = a;
     	this.setLayout(null);
         this.setBounds(0, 0, d.width, d.height); 
         this.setBackground(Color.BLACK);  
         this.setVisible(true);
         this.setFocusable(true);
             
         width = this.getWidth();
         height = this.getHeight();
         
         image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
         imageL = new JLabel(new ImageIcon(image));
         imageL.setBounds(0,0,width,height);
                
         myG = image.getGraphics();
         myG.setColor(Color.WHITE);
 
         this.add(imageL);
     } 
     
     public void paintComponent(Graphics g)
     {
         super.paintComponent(g);         
     }    
 }

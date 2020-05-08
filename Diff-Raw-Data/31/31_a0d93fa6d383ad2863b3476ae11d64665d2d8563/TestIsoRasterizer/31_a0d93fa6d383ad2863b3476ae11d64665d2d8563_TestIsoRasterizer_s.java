 package city.test;
 
 import javax.swing.*;
 import java.awt.*;
 
 import city.math.Vector;
 import city.iso.*;
 
 public class TestIsoRasterizer extends JPanel {
     @Override
     public void paintComponent (Graphics g) {
         super.paintComponent(g);
         g.setColor(Color.black);
 
         IRasterizer r = new Rasterizer(g);
         Shapes container = new Shapes();
         container.add(new Quad(
                 new Vector(0, 0),
                new Vector(100, 0, 10),
                new Vector(100, 100, 10),
                 new Vector(0, 100)));
 
         //Render!
         container.accept(r);
     }
 
     public static void main (String[] args) {
         Container content = new TestIsoRasterizer();
         JFrame frame = new JFrame("");
         frame.setBackground(Color.white);
         content.setBackground(Color.white);
         frame.setSize(640, 480);
         frame.setContentPane(content);
         frame.addWindowListener(new ExitListener());
         frame.setVisible(true);
     }
 }

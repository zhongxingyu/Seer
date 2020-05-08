 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package helper;
 
 import entity.Point;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import javax.imageio.ImageIO;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 /**
  * @author              Vy Thuy Nguyen
  * @version             1.0 Jan 11, 2013
  * Last modified:       
  */
 public class TestFrame extends JFrame
 {
     TestPane testPn;
 
     public TestFrame()
     {
       setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
       setLayout(new BorderLayout());
       testPn  = new TestPane();
       add(testPn, BorderLayout.CENTER);
       setLocationRelativeTo(null);
       setVisible(true);
       
       
       //Export
       JButton exportBtn = new JButton("Export to file");
       exportBtn.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 try
                 {
                     File outputFile = new File("marked_floorplan.jpg");
                     ImageIO.write(testPn.background, "jpg", outputFile);
 
                 } catch (IOException exc)
                 {
                     
                 }
             }
       });
       
       JPanel sub = new JPanel();
       sub.add(exportBtn);
       add(sub, BorderLayout.SOUTH);
       pack();
     }
     
     private class TestPane extends JPanel
     {
         public BufferedImage background;
        private List<Point> clickPoints;
         
         public TestPane()
         {
             clickPoints = new ArrayList<>();
             try
             {
                 background = ImageIO.read(new File("C:\\Users\\VyNguyen\\Desktop\\Sample_Floorplan.jpg"));
             }
             catch (IOException ex)
             {
                 ex.printStackTrace();
             }
             addMouseListener(new MouseAdapter() {
                 @Override
                 public void mouseClicked(MouseEvent e)
                 {
                     clickPoints.add(new Point(e.getX(), e.getY()));
                     repaint();
                 }            
             });
         }
         
         @Override
         public Dimension getPreferredSize()
         {
             return background == null ? 
                     super.getPreferredSize() : 
                     new Dimension(background.getWidth(), background.getHeight());
         }
         
         @Override
         protected void paintComponent(Graphics g)
         {
            ImageIcon i = new ImageIcon(background);
            Graphics origImgGraphics = i.getImage().getGraphics();
             super.paintComponent(g);
             if (background != null)
             {
                 int x = (getWidth() - background.getWidth()) / 2;
                 int y = (getHeight() - background.getHeight()) / 2;
                 g.drawImage(background, x, y, this);
             }
             
            origImgGraphics.setColor(Color.RED);
             for (Point p : clickPoints)
                origImgGraphics.fillOval(p.getX() - 4, p.getY() - 4, 8, 8);
         }
     }
 }

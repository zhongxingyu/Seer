 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package javacity.ui;
 
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import javacity.world.City;
 import javax.swing.JButton;
 import javax.swing.JPanel;
 
 /**
  *
  * @author Tom
  */
 public class GuiToolbox extends JPanel implements MouseListener, ActionListener {
     
     private City city;
     
     private JButton r, c, i, road, grass;
     
     private String type;
     
     private int dragX, dragY;
     
     public GuiToolbox(City city)
     {
         super();
         this.city = city;
         this.type = "zone_r";
         
         r = new JButton("Residential");
         c = new JButton("Commercial");
         i = new JButton("Industrial");
         road = new JButton("Road");
         grass = new JButton("Grass");
         
         this.setLayout(new GridLayout(5,0));
         this.add(r,0,0);
         this.add(c,1,0);
         this.add(i,2,0);   
         this.add(road,3,0);
         this.add(grass,4,0);
         
         r.addActionListener(this);
         c.addActionListener(this);
         i.addActionListener(this);
         road.addActionListener(this);
         grass.addActionListener(this);
         
         
     }
     
     @Override
     public void mouseExited(MouseEvent e) 
     {
         
     }
     
     @Override
     public void mouseEntered(MouseEvent e) 
     {
         
     }
     
     @Override
     public void mouseReleased(MouseEvent e)
     {
         int curX = e.getX() / 32;
         int curY = e.getY() / 32;
         
         int startx = Math.min(curX, this.dragX);
         int endx = Math.max(curX, this.dragX);
         
         int starty = Math.min(curY, this.dragY);
         int endy = Math.max(curY, this.dragY);
         
         for (int x = startx; x <= endx; x++) {
             for (int y = starty; y <= endy; y++) {
                this.city.getByLocation(x, y).setType(this.type);                
             }
         }
         
     }
     
     @Override
     public void mousePressed(MouseEvent e)
     {
         this.dragX = e.getX() / 32;
         this.dragY = e.getY() / 32;
     }
     
     @Override
     public void mouseClicked(MouseEvent e)
     {
     }
     
     @Override
     public void actionPerformed(ActionEvent e)
     {
         if (e.getSource() == this.r) {
             this.type ="zone_r";
         } else if (e.getSource() == this.c) {
             this.type = "zone_c";
         } else if (e.getSource() == this.i) {
             this.type = "zone_i";
         } else if (e.getSource() == this.road) {
             this.type = "road";
         } else if (e.getSource() == this.grass) {
             this.type = "grass";
         }
     }
 }

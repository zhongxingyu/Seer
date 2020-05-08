 package zbecreate;
 
import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
import java.awt.Image;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.util.ArrayList;
 import javax.swing.JPanel;
 import zbecreate.sprite.*;
 
 public class ZbePaint extends JPanel implements MouseMotionListener, MouseListener {
 
     private ArrayList<ZbeBaseSprite> list;
 
     public ZbePaint() {
         list = new ArrayList<ZbeBaseSprite>();
         addMouseListener(this);
         addMouseMotionListener(this);
     }
 
 
     @Override
     public void paintComponent(Graphics g){
         super.paintComponent(g);
         Graphics2D graph = (Graphics2D) g;
 
         for(int i = 0; i < list.size(); i++){
             ZbeBaseSprite temp = list.get(i);
             int h = temp.getTileHeight();
             int w = temp.getTileWidth();
             int x = temp.getXPosition();
             int y = temp.getYPosition();
            System.out.printf("Xpos = %d\nYpos = %d\nH = %d\nW = %d\n",x,y,h,w);
 
            graph.drawImage(temp.getImage(), h, w, x, y, this);
         }
     }
 
     public void placeSprite(int x, int y){
         ZbeBaseSprite temp = new ZbeHeroSprite();
         temp.loadImage();
         temp.setPosition(x, y);
         list.add(temp);
         System.out.printf("List size = %d\n", list.size());
         repaint();
     }
 
     public void mousePressed(MouseEvent e) {
         int x = e.getX();
         int y = e.getY();
 
         placeSprite(x,y);
     }
 
     public void mouseDragged(MouseEvent e) {
     }
 
     public void mouseReleased(MouseEvent e) {
     }
 
     public void mouseMoved(MouseEvent e) {}
     public void mouseEntered(MouseEvent e) {}
     public void mouseExited(MouseEvent e) {}
     public void mouseClicked(MouseEvent e){}
 
 } 

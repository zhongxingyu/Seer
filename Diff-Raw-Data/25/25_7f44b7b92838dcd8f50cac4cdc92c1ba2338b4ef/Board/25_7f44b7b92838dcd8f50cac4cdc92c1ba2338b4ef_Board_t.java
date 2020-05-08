 package bomberman.client.gui;
 
 import bomberman.client.elements.Element;
import java.awt.Dimension;
 import java.awt.Graphics;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import javax.swing.JPanel;
 
 public class Board extends JPanel {
 
     private List<Element> elements = null;
     private int cols;
     private int rows;
     private int unit_pixels = 10;
 
     public Board() {
         super();
     }
 
     @Override
     public void paintComponent(Graphics g) {
         if (this.elements == null) {
             return;
         }
         for (int i = 0; i < this.rows; i++) {
             for (int j = 0; j < this.cols; j++) {
                 try {
                     Element element = this.elements.get(i * this.cols + j);
                     if (element == null) {
                         continue;
                     }
                     int x = this.getPosX(i, j) * this.unit_pixels;
                     int y = (this.getPosY(i, j) + this.cols) * this.unit_pixels;
                     g.drawImage(element.getImage(), x, y, this);
                 } catch (Exception e) {
                     System.out.println(e.getMessage());
                 }
             }
         }
     }
 
     public void setData(List<Map> data) {
         this.elements = new ArrayList<Element>();
         this.rows = data.size() / this.cols;
         int size = data.size();
 
         for (int i = 0; i < size; i++) {
             try {
                 this.elements.add(Element.factory(data.get(i)));
             } catch (Exception e) {
                 System.out.println(e.getMessage());
             }
         }
 
        Dimension panel_size = new Dimension((3 * this.cols + this.rows) * this.unit_pixels, (this.cols + 2 * this.rows) * this.unit_pixels);
        this.setMinimumSize(panel_size);
        this.setPreferredSize(panel_size);
        Window.getInstance().pack();
         this.repaint();
     }
 
     public void setCols(int cols) {
         this.cols = cols;
     }
 
     private int getPosX(int i, int j) {
         return 3 * i + j;
     }
 
     private int getPosY(int i, int j) {
         return 2 * j - i;
     }
 }

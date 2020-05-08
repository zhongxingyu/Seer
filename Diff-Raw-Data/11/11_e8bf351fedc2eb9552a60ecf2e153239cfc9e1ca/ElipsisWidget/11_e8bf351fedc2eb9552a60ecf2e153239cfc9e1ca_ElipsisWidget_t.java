 package dominoes;
 
 import java.awt.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: timothy
  * Date: 16/03/2013
  * Time: 21:10
  * To change this template use File | Settings | File Templates.
  */
 public class ElipsisWidget extends Canvas {
     private int size = 120;
 
     public ElipsisWidget(int size) {
         this.size = size;
        reshape(0, 0, this.size, this.size / 2);
     }
 
     public void paint(Graphics g) {
         g.setColor(Color.black);
         for (int i = 0; i < 5; i = i + 2) {
            g.fillRoundRect(10 + 20 * i, this.size / 4, 20, 5, 2, 2);
         }
     }
 }

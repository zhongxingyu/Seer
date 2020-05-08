 import javax.swing.*;
 import java.awt.*;
 
 /**
  * Created by IntelliJ IDEA.
  * User: Eric
  * Date: 1/29/12
  * Time: 5:11 PM
  * To change this template use File | Settings | File Templates.
  */
 public class GameCanvas extends JPanel {
    private SimpleMap map = new SimpleMap(512, 256);
     private int topLeftX, topLeftY;
     
     public GameCanvas(String path){
         this.setSize(400, 200);
         map.createMapFromImage(path);
         topLeftX = 0;
         topLeftY = 0;
     }
     
     public GameCanvas(){
         this.setSize(400, 200);
         map.createMapFromPerlinNoise(100, 150, 200, 255);
     }
     
     public void setTopCoords(int topX, int topY){
         topLeftX = topX;
         topLeftY = topY;
     }
     
     public int getTopLeftX(){
         return topLeftX;
     }
     
     public int getTopLeftY(){
         return topLeftY;
     }
     
     @Override
     public void paint(Graphics g){
         Graphics2D g2d = (Graphics2D)g;
         g2d.setColor(Color.black);
         g2d.drawRect(0, 0, 400, 200);
         QuadTreeNode[][] roots = map.getMapTreeRoots();
         map.quadTreeRender(g2d, roots[0][0], topLeftX, topLeftY);
         map.quadTreeRender(g2d, roots[1][0], topLeftX, topLeftY);
         //map.render(g2d, topLeftX, topLeftY);
         for(int x = 0; x < 800; x += 20){
             g2d.drawLine(x, 0, x, 400);
         }
         for(int y = 0; y < 400; y += 20){
             g2d.drawLine(0, y, 800, y);
         }
     }    
 }

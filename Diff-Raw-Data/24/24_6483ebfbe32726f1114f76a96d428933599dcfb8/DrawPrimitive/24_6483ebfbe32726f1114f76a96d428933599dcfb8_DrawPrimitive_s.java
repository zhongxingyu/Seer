 package lesson3;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.geom.Ellipse2D;
 import java.awt.geom.Line2D;
 import java.awt.geom.Rectangle2D;
 
 /**
  * Author: Ilya Varlamov aka privr@tnik
  * Date: 04.03.12
  * Time: 17:21
  */
 
 public class DrawPrimitive extends JPanel {
 
     public DrawPrimitive(){
         this.setBackground( Color.LIGHT_GRAY );
     }
 
     protected void paintComponent(Graphics g){
 
         super.paintComponent(g);
 
         Graphics2D g2 = (Graphics2D)g;
 
         for (int i = 0; i < 12; i++ ){
 
             BasicStroke stroke = new BasicStroke( 5 );
             g2.setStroke(stroke);
             g2.setPaint(randomColor());
             g2.draw( randomShape() );
 
         }
     }
 
     private Shape randomShape(){
 
        double x = ( 20 / ( Math.random() % 3 ) );
        double y = ( 20 / ( Math.random() % 3 ) );
        double width = ( 50 / ( Math.random() % 2 ) );
        double height = ( 50 / ( Math.random() % 2 ) );
 
         int result = (int) ( Math.random() * 3 );
         
         switch( result ){
             default:
             case 0 : return new Rectangle2D.Double(x, y, width, height);
             case 1 : return new Ellipse2D.Double(x, y, width, height);
             case 2 : return new Line2D.Double(x, y, width, height);
         }
 
     }
 
     private Color randomColor(){
     
             int result = (int) ( Math.random() * 12 );
     
             switch ( result ){
                 default:
                 case 0: return Color.BLACK;
                 case 1: return Color.PINK;
                 case 2: return Color.YELLOW;
                 case 3: return Color.WHITE;
                 case 4: return Color.GRAY;
                 case 5: return Color.MAGENTA;
                 case 6: return Color.GREEN;
                 case 7: return Color.PINK;
                 case 8: return Color.RED;
                 case 9: return Color.DARK_GRAY;
                 case 10: return Color.ORANGE;
                 case 11: return Color.CYAN;
             }
 
         }
     
     private Color randomColor( int i ){
 
         Color color = null;
 
         switch ( i ){
             case 0:
                 color = Color.BLACK;
                 break;
             case 1:
                 color = Color.PINK;
                 break;
             case 2:
                 color = Color.YELLOW;
                 break;
             case 3:
                 color = Color.WHITE;
                 break;
             case 4:
                 color = Color.GRAY;
                 break;
             case 5:
                 color = Color.MAGENTA;
                 break;
             case 6:
                 color = Color.GREEN;
                 break;
             case 7:
                 color = Color.PINK;
                 break;
             case 8:
                 color = Color.RED;
                 break;
             case 9:
                 color = Color.DARK_GRAY;
                 break;
             case 10:
                 color = Color.ORANGE;
                 break;
             case 11:
                 color = Color.CYAN;
                 break;
             default:
                 color = Color.BLUE;
                 break;
         }
 
         return color;
     }
 
 }

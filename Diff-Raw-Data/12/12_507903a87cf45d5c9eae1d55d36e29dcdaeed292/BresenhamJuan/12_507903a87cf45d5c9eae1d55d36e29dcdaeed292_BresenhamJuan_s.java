 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Insets;
 
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 
 public class BresenhamJuan extends JPanel  {
 
   public static int w, h, c;
   private static int skip =1;
  public static Panel panel ;
   
   public static void setskip(int x){
	  skip = x ;
   }
   
   public void set_screen_size() {
     Dimension size = getSize();
     Insets insets = getInsets();
     w = size.width - insets.left - insets.right;
     h = size.height - insets.top - insets.bottom;
     c = getSize().width;
   }
   
   @Override
   public void paintComponent(Graphics g) {
     super.paintComponent(g);
     Graphics2D g2d = (Graphics2D) g;
     set_screen_size();
     
     Dimension p1 = new Dimension(0,0);
     Dimension p2 = new Dimension(c,0);
     
     for( int i = 0; i < w; i+=skip ){
       g2d.setColor(Color.black);
       p1.setSize(i/2,(0+h)/2);
       p2.setSize(w/2,(i+h)/2);
       bresenham_line(p1,p2,g2d);
 
       //g2d.setColor(Color.green);
 
       p1.setSize(w/2,i/2);
       p2.setSize((w-i)/2,h/2);
       bresenham_line(p1,p2,g2d);
       
       p1.setSize(((w-i)+w)/2,h/2);
       p2.setSize((0+w)/2,(h-i)/2);
       bresenham_line(p1,p2,g2d);
       
       p1.setSize((0+w)/2,((h-i)+h)/2);
       p2.setSize((i+w)/2,(0+h)/2);
       bresenham_line(p1,p2,g2d);
     }
   }
 
   public static void bresenham_line(Dimension p1, Dimension p2, Graphics g2d) {
     
     int y1 = (int) p2.getHeight(), 
         y0 = (int) p1.getHeight(), 
         x1 = (int) p2.getWidth(), 
         x0 = (int) p1.getWidth(),
         dx =  x1 - x0, 
         dy =  y1 - y0,
         y = y0,
         x = x0, 
         incE, incNE, d, stepx, stepy;
     
     if (dy < 0){ dy = -dy; stepy = -1; 
     }else stepy = 1;
     
     if (dx < 0){ dx = -dx; stepx = -1; 
     }else stepx = 1;
     
     if( dx > dy ){
       incE = 2*dy;
       incNE = 2*dy - 2*dx;
       d = 2*dy - dx;
       
       for( ;x != x1; x+=stepx ){
         g2d.drawLine(x,-(y-h),x,-(y-h));
         if( d < 0 ) d += incE;
         else{
           d += incNE;
           y+=stepy;
         }
       }
     }else{
       incE = 2*dx;
       incNE = 2*dx - 2*dy;
       d = 2*dx - dy;
       
       for( ;y != y1; y+=stepy ){
         g2d.drawLine(x,-(y-h),x,-(y-h));
         if( d < 0 ) d += incE;
         else{
           d += incNE;
           x+=stepx;
         }
       }
     }
   }
   
   
   public static void main(String[] args) {
     JFrame frame = new JFrame("Points");
     frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	int x =Integer.parseInt(JOptionPane.showInputDialog("Ingrese el incremento en X y Y " + " este debe ser un numero entre 0-100"));
	setskip(x);
     frame.add(new BresenhamJuan());
     frame.setSize(800, 800);
     frame.setLocationRelativeTo(null);

     frame.setVisible(true);
   }
 }

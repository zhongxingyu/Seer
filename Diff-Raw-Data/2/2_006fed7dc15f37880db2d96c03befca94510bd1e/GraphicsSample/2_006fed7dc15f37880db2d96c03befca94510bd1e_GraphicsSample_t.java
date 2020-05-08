 import java.awt.*;
 import java.awt.image.*;
 
 class GraphicsSample
 {
     public static void main(String args[]) {
         GraphicsDraw paper = new GraphicsDraw();
 
         paper.setSize(640, 480);
         paper.setTitle("Fractal");
         paper.setVisible( true );
     }
 }
 
 class GraphicsDraw extends Frame
 {
     public void paint(Graphics g) {
         BufferedImage buf = new BufferedImage(640, 480, BufferedImage.TYPE_INT_ARGB);
         Graphics bufg = buf.getGraphics();
 
         drawTree(buf, 1, 100, 240, 0, 1);
         g.drawImage(buf, 0, 0, Color.white, null);
     }
 
     private int radius(int n) {
         return 100 / (int)(Math.sqrt(n));
     }
 
     private boolean canDraw(BufferedImage buf, int x, int y, int r) {
         if (x < 0 || y < 0 || x >= 640 || y >= 480)
             return false;
 
         boolean res = buf.getRGB(x, y) == 0;
         return res;
     }
 
     private void drawTree(BufferedImage buf, int n, int x, int y, double angle, int sign) {
         if (n > 31) return;
 
         int r = radius(n);
         int c = (n % 15) * 16;
 
         Graphics g = buf.getGraphics();
 
         if (canDraw(buf, x, y, r / 2)) {
             g.setColor(new Color(255, 255 - c, c));
             g.fillOval(x - r / 2, y - r / 2, r, r);
         } else {
             return;
         }
 
         int r2 = (r + radius(n + 1)) / 2;
 
         drawTree(buf, n + 1,
                  x + (int)(Math.cos(angle) * r2), y + (int)(Math.sin(angle) * r2),
                  angle + Math.PI * 0.1 * sign, sign);
        if (Math.random() < 0.6) {
             double a2 = angle - Math.PI * 0.5 * sign;
             int x2 = x + (int)(Math.cos(a2) * r2);
             int y2 = y + (int)(Math.sin(a2) * r2);
             if (x2 >= 0 && y2 >= 0 && x2 < 640 && y2 < 480) {
                 drawTree(buf, n + 1, x2, y2, a2, -sign);
             }
         }
     }
 }

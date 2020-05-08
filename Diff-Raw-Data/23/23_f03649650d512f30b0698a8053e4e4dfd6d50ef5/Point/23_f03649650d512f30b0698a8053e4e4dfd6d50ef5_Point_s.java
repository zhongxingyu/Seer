 public class Point{
   private int x;
   private int y;
     
   public Point(int a, int b) {
     x = a;
     y = b;
   }
     
   public int getX() {
     return x;
   }
     
   public int getY() {
     return y;
   }
    
   public void increX() {
     x = x + 1;
   }
     
   public void decreX() {
     x = x - 1;
   }
     
   public void increY() {
     y = y + 1;
   }
     
   public void decreY() {
     y = y - 1;
   }
 }

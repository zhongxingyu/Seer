public class Point implements Comparable {
   public int x;
   public int y;
     
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
   public void setX(int a)
     {
       x = a;
     }
   public void setY(int b)
     {
       y = b;
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
 
   @Override
   public int hashCode() {
     final int prime = 31;
     int result = 1;
     result = prime * result + x;
     result = prime * result + y;
     return result;
   }
 
   @Override
   public boolean equals(Object obj) {
     if (this == obj)
       return true;
     if (obj == null)
       return false;
     if (getClass() != obj.getClass())
       return false;
     Point other = (Point) obj;
     if (x != other.x)
       return false;
     if (y != other.y)
       return false;
     return true;
   }
 
   public int compareTo(Point otherPoint) {
     if (y == otherPoint.y)
       return x - otherPoint.x;
     else
       return y - otherPoint.y;
   }
 }

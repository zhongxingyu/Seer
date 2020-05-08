 package drinker;
 
 public class Point2D {
     
     public int x;
     public int y;
     
     public Point2D(){
         this(0,0);
     }
     
     public Point2D(int x, int y){
         this.x = x;
         this.y = y;
     }
        
    public boolean isEqual(Point2D point2D){
        return this.x == point2D.x && this.y == point2D.y;
    }
    
     public boolean isZero(){
         return this.x == 0 && this.y == 0;
     }
     
    public Point2D clone(){
         return new Point2D(this.x, this.y);
     }
     
 }

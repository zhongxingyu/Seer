 package j15r.xna.platformer.core;
 
 // A set of helpful methods for working with rectangles.
 class Rectangle {
   public float Left, Top, Width, Height;
 
   public Rectangle(float left, float top, float width, float height) {
     Left = left;
     Top = top;
     Width = width;
     Height = height;
   }
 
   // Calculates the signed depth of intersection between two rectangles.
   // <returns>
   // The amount of overlap between two intersecting rectangles. These
   // depth values can be negative depending on which wides the rectangles
   // intersect. This allows callers to determine the correct direction
   // to push objects in order to resolve collisions.
   // If the rectangles are not intersecting, Vector2.Zero is returned.
   // </returns>
   public static Vector2 GetIntersectionDepth(Rectangle rectA, Rectangle rectB) {
     // Calculate half sizes.
     float halfWidthA = rectA.Width / 2.0f;
     float halfHeightA = rectA.Height / 2.0f;
     float halfWidthB = rectB.Width / 2.0f;
     float halfHeightB = rectB.Height / 2.0f;
 
     // Calculate centers.
     Vector2 centerA = new Vector2(rectA.Left + halfWidthA, rectA.Top + halfHeightA);
     Vector2 centerB = new Vector2(rectB.Left + halfWidthB, rectB.Top + halfHeightB);
 
     // Calculate current and minimum-non-intersecting distances between centers.
     float distanceX = centerA.X - centerB.X;
     float distanceY = centerA.Y - centerB.Y;
     float minDistanceX = halfWidthA + halfWidthB;
     float minDistanceY = halfHeightA + halfHeightB;
 
     // If we are not intersecting at all, return (0, 0).
     if (Math.abs(distanceX) >= minDistanceX || Math.abs(distanceY) >= minDistanceY)
       return Vector2.Zero;
 
     // Calculate and return intersection depths.
     float depthX = distanceX > 0 ? minDistanceX - distanceX : -minDistanceX - distanceX;
     float depthY = distanceY > 0 ? minDistanceY - distanceY : -minDistanceY - distanceY;
     return new Vector2(depthX, depthY);
   }
 
   // Gets the position of the center of the bottom edge of the rectangle.
   public Vector2 GetBottomCenter() {
     return new Vector2(Left + Width / 2.0f, Top + Height);
   }
 
   public float Right() {
     return Left + Width;
   }
 
   public float Bottom() {
     return Top + Height;
   }
 
   public static Vector2 GetBottomCenter(Rectangle r) {
     return new Vector2(r.Left + r.Width / 2, r.Top + r.Height);
   }
 
   public Vector2 Center() {
     return new Vector2(Left + Width / 2, Top + Height / 2);
   }
 
   public boolean Contains(Vector2 v) {
     return
       (v.X >= Left && v.X < Right()) &&
       (v.Y >= Top && v.Y < Bottom()) ;
   }
 
   public boolean Intersects(Rectangle r) {
    return
      ((r.Left >= Left && r.Left < Right()) ||
       (r.Right() >= Left && r.Right() < Right())) &&
      ((r.Top >= Top && r.Top < Bottom()) ||
       (r.Bottom() >= Top && r.Bottom() < Bottom()))
    ;
   }
 }

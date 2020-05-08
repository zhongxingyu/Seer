 package pt.inevo.encontra.geometry;
 
 
 public class Point extends Entity2D implements Comparable<Point> {
 
     public double x;
     public double y;
 
     int index;
 
     void setIndex(int index) {
         this.index = index;
     }
 
     int getIndex() {
         return index;
     }
 
     public Point() {
         super();
     }
 
     public Point(double xx, double yy) {
         super();
         x = xx;
         y = yy;
     }
 
     public int compareTo(Point o) {
         if (getY() < o.getY())
             return -1;
         else if (getY() == o.getY()) {
             if (getX() < o.getX())
                 return -1;
             else if (getX() == o.getX())
                 return 0;
         }
 
         // p1 is greater than p2
         return 1;
     }
 
     public double getY() {
         return y;
     }
 
     public double getX() {
         return x;
     }
 
     public void setX(double d) {
         x = d;
     }
 
     public void setY(double d) {
         y = d;
     }
 
 //        /**
 //     * Performs the sum of the current point with another.
 //     * @param point the point to be summed.
 //     * @return the resulting point.
 //     */
 //    public Point add(Point point) {
 //        return new Point(getX()+point.getX(), getY()+point.getY());
 //    }
 //
 //    /**
 //     * Performs the sum of a number to each coordinate from the current point.
 //     * @param val the value to be summed.
 //     * @return the resulting point.
 //     */
 //    public Point add(double val) {
 //        return new Point(getX()+val, getY()+val);
 //    }
 //
 //    /**
 //     * Performs the subtraction of a point to the current one.
 //     * @param point the point to be subtracted.
 //     * @return the resulting point.
 //     */
 //    public Point subtract(Point point) {
 //        return new Point(getX()-point.getX(), getY()-point.getY());
 //    }
 //
 //    /**
 //     * Performs the subtraction of a value to each of current point's coordinates.
 //     * @param val the value to be subtracted.
 //     * @return the resulting point.
 //     */
 //    public Point subtract(double val) {
 //        return new Point(getX()-val, getY()-val);
 //    }
 //
 //    /**
 //     * Performs the multiplication of the current point with another.
 //     * @param point the point to be multiplied.
 //     * @return the resulting point.
 //     */
 //    public Point multiply(Point point) {
 //        return new Point(getX()*point.getX(), getY()*point.getY());
 //    }
 //
 //    /**
 //     * Performs the multiplication of the current point by a value.
 //     * @param val the value to be multiplied.
 //     * @return the resulting point.
 //     */
 //    public Point multiply(double val) {
 //        return new Point(getX()*val, getY()*val);
 //    }
 //
 //    /**
 //     * Performs the division of the current point by another.
 //     * @param point the point to be divided by.
 //     * @return the resulting point.
 //     */
 //    public Point divide(Point point) {
 //        return new Point(getX()/point.getX(), getY()/point.getY());
 //    }
 //
 //    /**
 //     * Performs the division of each coordinate of the current point by a value.
 //     * @param val the value to be divided by.
 //     * @return the resulting point.
 //     */
 //    public Point divide(double val) {
 //        return new Point(getX()/val, getY()/val);
 //    }
 
     /**
      * @param sx component of the scale
      * @param sy component of the scale
      * @desc performs a scale transformation on this point
      */
     public Point scale(double sx, double sy) {
         Point p = new Point();
         p.setX(getX() * sx);
         p.setY(getY() * sy);
         return p;
     }
 
     public Point scale(double scale) {
         Point p = new Point();
         p.setX(getX() * scale);
         p.setY(getY() * scale);
         return p;
     }
 
     /**
      * @param dx component of the translation
      * @param dy component of the translation
      * @desc performs a translation transformation on this point
      */
     public Point translate(double dx, double dy) {
         Point p = new Point();
         p.setX(getX() + dx);
         p.setY(getY() + dy);
         return p;
     }
 
     public Point translate(double d) {
         Point p = new Point();
         p.setX(getX() + d);
         p.setY(getY() + d);
         return p;
     }
 
     public Point translate(Point p) {
         Point newPoint = new Point();
         newPoint.setX(getX() + p.getX());
         newPoint.setY(getY() + p.getY());
         return newPoint;
     }
 
     /**
      * Performs the subtraction of a point to the current one.
      *
      * @param point the point to be subtracted.
      * @return the resulting point.
      */
     public Point subtract(Point point) {
         Point newPoint = new Point();
         newPoint.setX(getX() - point.getX());
         newPoint.setY(getY() - point.getY());
         return newPoint;
     }
 
     /**
      * Performs the subtraction of a value to each of current point's coordinates.
      *
      * @param val the value to be subtracted.
      * @return the resulting point.
      */
     public Point subtract(double val) {
         Point p = new Point();
         p.setX(getX() - val);
         p.setY(getY() - val);
         return p;
     }
 
     /**
      * @param theta component of the translation
      * @desc performs a rotation transformation on this point
      * @see J. D. Foley, A. van Dam, S. K. Feiner, J. F. Hughes,
      *      Computer Graphics: Principles and Practice, Addison-Wesley, 2nd ed in C, 1990, chaper 5.
      */
     void rotate(double theta) {
         double rotated_x = getX() * Math.cos(theta) - getY() * Math.sin(theta);
         double rotated_y = getX() * Math.sin(theta) + getY() * Math.cos(theta);
 
         setX(rotated_x);
         setY(rotated_y);
     }
 
     /**
      * @param p point
      * @return distance
      * @desc calculates the distance between this point and another
      */
     public double distanceTo(Point p) {
         if (p == null)
             return 0.0f;
 
         return Math.sqrt(squareDistanceTo(p));
     }
 
 
     /**
      * @param p point
      * @return distance
      * @desc calculates the square of the distance between this point
      * and another
      */
     public double squareDistanceTo(Point p) {
        return Math.pow(Math.abs(getX() - p.getX()),2) + Math.pow(Math.abs(getY() - p.getY()),2);
     }
 
 
     @Override
     protected Point clone() {
         Point np = new Point(getX(), getY());
         np.SetID(this.GetID());
         return np;
     }
 
     /**
      * @desc fills the first and last point attribute
      */
     void calculateFirstAndLastPoints() {
         SetFirstPoint(this);
         SetLastPoint(this);
     }
 
     public boolean equals(Point p) {
         return (x == p.x) && (y == p.y);
     }
 
     /**
      * @return negative, zero or positive value
      *         according to whether the distance of <p1> to its owner is less than, equal to or
      *         greater than the distance of <p2> to its owner.
      * @desc compares two points in term of order
      */
     public static int compareOrderUsingDistanceToOwner(Point p1, Point p2) {
         // if any of the points have no owner entity they cannot be compared
         if ((p1._owner_entity == null) || (p2._owner_entity == null)) return 0;
 
         Point first_point_owner_p1 = p1._owner_entity.GetFirstPoint();
         Point first_point_owner_p2 = p2._owner_entity.GetFirstPoint();
 
         // if any of the points owner entity have no first point computed
         // then the points cannot be compared
         if (first_point_owner_p1 == null || first_point_owner_p2 == null) return 0;
 
         double sqr_distance_p1 = p1.squareDistanceTo(first_point_owner_p1);
         double sqr_distance_p2 = p2.squareDistanceTo(first_point_owner_p2);
 
         if (sqr_distance_p1 < sqr_distance_p2)
             return -1;
 
         if (sqr_distance_p1 > sqr_distance_p2)
             return 1;
 
         return 0;
     }
 
     /**
      * @return true if is collinear, false otherwise
      * @desc indicates if this point is collinear with a and b
      * @see O'Rourke, Joseph, "Computational Geometry in C, 2nd Ed.", pp.29
      */
     public boolean collinear(Point a, Point b) {
         return Area(a, b, this) == 0.0f;
     }
 
 
     /**
      * @return true if is at left, false otherwise
      * @desc indicates if this point is at left of the directed line from a to b
      * @see O'Rourke, Joseph, "Computational Geometry in C, 2nd Ed.", pp.29
      */
     boolean left(Point a, Point b) {
         return Area(a, b, this) > 0.0f;
     }
 
     /**
      * @return true is this point is betwen a and b
      * @note c must be collinear with a and b
      * @see O'Rourke, Joseph, "Computational Geometry in C, 2nd Ed.", pp.32
      */
     boolean between(Point a, Point b) {
         // if this point is not collinear with a and b
         // then it cannot be between this two points
         if (!collinear(a, b))
             return false;
 
         if (a.getX() != b.getX())
             return ((a.getX() <= x) && (x <= b.getX())) ||
                     ((a.getX() >= x) && (x >= b.getX()));
         else
             return ((a.getY() <= y) && (y <= b.getY())) ||
                     ((a.getY() >= y) && (y >= b.getY()));
     }
 
     /**
      * @return true is this point is betwen a and b,
      *         but is different from a and b
      * @note c must be collinear with a and b
      * @see O'Rourke, Joseph, "Computational Geometry in C, 2nd Ed.", pp.32
      */
     boolean strictBetween(Point a, Point b) {
         // first check if this point is between a and b
         if (!between(a, b))
             return false;
 
         // if is between, lets check if it is not coincident with
         // one of them
         return (!a.equals(this) && !(b.equals(this)));
     }
 
     /**
      * @desc performs the rounding of coordinates according with gamma
      */
     void performRounding(double gamma) {
         x = round(x, gamma);
         y = round(y, gamma);
     }
 
     /**
      * @desc performs the rounding of value pointed by <value> according with
      * value pointed by <gamma>
      */
     double round(double value, double gamma) {
         double v = value / gamma;
 
         double v_ceil = Math.ceil(v);
 
         if (Math.abs(v - v_ceil) <= 0.5)
             value = v_ceil * gamma;
         else
             value = Math.floor(v) * gamma;
         return value;
     }
 
     @Override
     public void calculateBoundingBox() {
         // TODO Auto-generated method stub
 
     }
 
     @Override
     public String toString() {
         return "P" + GetID() + " (" + x + "," + y + ")";
     }
 }

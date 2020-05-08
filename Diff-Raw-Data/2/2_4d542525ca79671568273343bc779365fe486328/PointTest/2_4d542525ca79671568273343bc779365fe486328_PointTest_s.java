 package week3.colliniar;
 
 import java.util.ArrayList;
 import java.util.Collections;
 
 import static junit.framework.Assert.assertEquals;
 import static junit.framework.Assert.assertTrue;
 import org.junit.Test;
 
 /**
  * @author: ajelimalai
  * @created: 25/03/13
  */
 public class PointTest {
 
     private Point point1;
     private Point point2;
 
 
     private void initPoints(int x1, int y1, int x2, int y2) {
         point1 = new Point(x1, y1);
         point2 = new Point(x2, y2);
     }
 
 
     @Test
     public void toString_Format() {
         Point point = new Point(1, 2);
        assertEquals("Incorrect format", 1 + " " + 2, point.toString());
     }
 
 
     @Test
     public void compareTo_LessYLess() {
         initPoints(2, 5, 3, 6);
         assertTrue("Not less", point1.compareTo(point2) < 0);
         assertTrue("Not greater", point2.compareTo(point1) > 0);
     }
 
 
     @Test
     public void compareTo_LessYEqual() {
         initPoints(2, 5, 3, 5);
         assertTrue("Not less", point1.compareTo(point2) < 0);
         assertTrue("Not greater", point2.compareTo(point1) > 0);
     }
 
 
     @Test
     public void compareTo_Equal() {
         initPoints(10, 10, 10, 10);
         assertTrue("Not equal", point1.compareTo(point2) == 0);
         assertTrue("Not equal", point2.compareTo(point1) == 0);
     }
 
 
     @Test
     public void compare_Reference() {
         Point p = new Point(125, 313);
         Point q = new Point(274, 291);
         Point r = new Point(125, 219);
         assertEquals("Not equal", -1, p.SLOPE_ORDER.compare(q, r));
     }
 
 
     @Test
     public void compare_Reference2() {
         Point p = new Point(354, 465);
         Point q = new Point(383, 237);
         Point r = new Point(354, 17);
         assertEquals("Not equal", -1, p.SLOPE_ORDER.compare(q, r));
     }
 
 
     @Test
     public void compare_Reference3() {
         Point p = new Point(85, 171);
         Point q = new Point(411, 343);
         Point r = new Point(85, 123);
         assertEquals("Not equal", -1, p.SLOPE_ORDER.compare(q, r));
     }
 
 
     @Test
     public void slopeComparator_BothVertical() {
         Point p = new Point(6, 7);
         Point q = new Point(6, 0);
         Point r = new Point(6, 1);
         assertEquals("Not equal", 0, p.SLOPE_ORDER.compare(q, r));
     }
 
 
     @Test
     public void slopeComparator_BothHorizontal() {
         Point p = new Point(3, 4);
         Point q = new Point(2, 4);
         Point r = new Point(10, 4);
         assertEquals("Not equal", 0, p.SLOPE_ORDER.compare(q, r));
     }
 
 
     @Test
     public void slopeTo_1() {
         initPoints(3, 2, 6, 5);
         assertEquals("Not expected slope", 1d, point1.slopeTo(point2));
     }
 
 
     @Test
     public void slopeTo_0() {
         initPoints(3, 5, 20, 5);
         assertEquals("Not expected slope", 0d, point1.slopeTo(point2));
     }
 
 
     @Test
     public void slopeTo_PositiveInfinity() {
         initPoints(20, 5, 20, 20);
         assertEquals("Not expected slope", Double.POSITIVE_INFINITY, point1.slopeTo(point2));
     }
 
 
     @Test
     public void slopeTo_PositiveInfinityReferenced() {
         initPoints(44, 286, 44, 240);
         assertEquals("Not expected slope", Double.POSITIVE_INFINITY, point1.slopeTo(point2));
     }
 
 
     @Test
     public void slopeTo_PositiveInfinityReferenced2() {
         initPoints(3, 6, 3, 3);
         assertEquals("Not expected slope", Double.POSITIVE_INFINITY, point1.slopeTo(point2));
     }
 
 
     @Test
     public void slopeTo_PositiveInfinityReferenced3() {
         initPoints(3, 1, 2, 9);
         assertEquals("Not expected slope", -8.0, point1.slopeTo(point2));
     }
 
 
     @Test
     public void slopeTo_NegativeInfinity() {
         Point point = new Point(20, 20);
         assertEquals("Not expected slope", Double.NEGATIVE_INFINITY, point.slopeTo(point));
     }
 
 
     @Test
     public void slopeTo_VerticalLinePositiveZero() {
         initPoints(2, 5, 20, 5);
         assertEquals("Slope should be positive zero", 0d, point1.slopeTo(point2));
         assertEquals("Slope should be positive zero", 0d, point2.slopeTo(point1));
     }
 
 
     @Test
     public void slopeComparator_colliniarPointSameSlope() {
         final ArrayList<Point> points = new ArrayList<Point>();
         final Point mainPoint = new Point(10, 10);
         points.add(new Point(20, 20));
         points.add(new Point(30, 30));
         points.add(new Point(40, 40));
         points.add(new Point(50, 50));
         int expectedResult = mainPoint.SLOPE_ORDER.compare(points.get(0), points.get(1));
         for (int i = 1; i < points.size() - 1; i++) {
             assertEquals("Slopes are not equals", expectedResult, mainPoint.SLOPE_ORDER.compare(points.get(i), points.get(i + 1)));
         }
     }
 
 
     @Test
     public void slopeComparator_colliniarPointSameSlopeVertical() {
         final ArrayList<Point> points = new ArrayList<Point>();
         final Point mainPoint = new Point(20, 10);
         points.add(new Point(20, 20));
         points.add(new Point(20, 30));
         points.add(new Point(20, 40));
         points.add(new Point(20, 50));
         int expectedResult = mainPoint.SLOPE_ORDER.compare(points.get(0), points.get(1));
         for (int i = 1; i < points.size() - 1; i++) {
             assertEquals("Slopes are not equals", expectedResult, mainPoint.SLOPE_ORDER.compare(points.get(i), points.get(i + 1)));
         }
     }
 
 
     @Test
     public void slopeComparator_colliniarPointSameSlopeHorizontal() {
         final ArrayList<Point> points = new ArrayList<Point>();
         final Point mainPoint = new Point(5, 20);
         points.add(new Point(20, 20));
         points.add(new Point(40, 20));
         points.add(new Point(60, 20));
         points.add(new Point(70, 20));
         int expectedResult = mainPoint.SLOPE_ORDER.compare(points.get(0), points.get(1));
         for (int i = 1; i < points.size() - 1; i++) {
             assertEquals("Slopes are not equals", expectedResult, mainPoint.SLOPE_ORDER.compare(points.get(i), points.get(i + 1)));
         }
     }
 
 
     @Test
     public void slopeComparator_Order() {
         final ArrayList<Point> points = new ArrayList<Point>();
         final Point mainPoint = new Point(2, 2);
         final Point point1 = new Point(5, 2);
         final Point point2 = new Point(4, 6);
         final Point point3 = new Point(2, 7);
         final Point point4 = new Point(6, 4);
         final Point point5 = new Point(6, 6);
         points.add(point1);
         points.add(point2);
         points.add(point3);
         points.add(point4);
         points.add(point5);
         Collections.sort(points, mainPoint.SLOPE_ORDER);
         assertEquals("Not same points", point1, points.get(0));
         assertEquals("Not same points", point4, points.get(1));
         assertEquals("Not same points", point5, points.get(2));
         assertEquals("Not same points", point2, points.get(3));
         assertEquals("Not same points", point3, points.get(4));
     }
 
 }

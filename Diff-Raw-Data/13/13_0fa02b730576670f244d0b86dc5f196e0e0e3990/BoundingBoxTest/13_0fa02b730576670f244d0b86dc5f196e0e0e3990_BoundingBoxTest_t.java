 package uk.co.danielrendall.mathlib.geom2d;
 
 import org.junit.Ignore;
 import org.junit.Test;
 
 import java.util.*;
 
 import static junit.framework.Assert.*;
 
 /**
  * Created by IntelliJ IDEA.
  * User: daniel
  * Date: 24-Apr-2010
  * Time: 20:46:41
  * To change this template use File | Settings | File Templates.
  */
 public class BoundingBoxTest {
 
     private static Comparator<BoundingBox> comparePythagorus = new Comparator<BoundingBox>() {
         public int compare(BoundingBox o1, BoundingBox o2) {
             return Double.compare(o1.getCenter().distanceTo(Point.ORIGIN), o2.getCenter().distanceTo(Point.ORIGIN));
         }
     };
 
     private static Comparator<BoundingBox> comparePythagorusSquared = new Comparator<BoundingBox>() {
         public int compare(BoundingBox o1, BoundingBox o2) {
             return Double.compare(o1.getCenter().squaredDistanceTo(Point.ORIGIN), o2.getCenter().squaredDistanceTo(Point.ORIGIN));
         }
     };
 
     private static Comparator<BoundingBox> compareApproximate = new Comparator<BoundingBox>() {
         public int compare(BoundingBox o1, BoundingBox o2) {
             return Double.compare(o1.getCenter().approximateDistanceTo(Point.ORIGIN), o2.getCenter().approximateDistanceTo(Point.ORIGIN));
         }
     };
 
     @Test
     public void testBoxFromNumbers() {
         BoundingBox bb1 = BoundingBox.containing(new double[]{10.1d, 0.02d, 5.3d, 5.5d}, new double[]{-3.0d, 5.23d, 1.2d, -10.4d});
         assertEquals(new BoundingBox(0.02d, 10.1d, -10.4d, 5.23d), bb1);
         BoundingBox bb2 = BoundingBox.containing(new double[]{-10.1d, -0.02d, -5.3d, -5.5d}, new double[]{-3.0d, -5.23d, -1.2d, -10.4d});
         assertEquals(new BoundingBox(-10.1d, -0.02d, -10.4d, -1.2d), bb2);
         BoundingBox bb3 = BoundingBox.containing(new double[]{-10.0d, -5.0d, 6.0d, 11.0d}, new double[]{12.0d, 7.0d, -8.0d, -13.0d});
         assertEquals(new BoundingBox(-10.0d, 11.0d, -13.0d, 12.0d), bb3);
     }
 
     @Test
     public void testExpandBoundingBox() {
         BoundingBox initial = new BoundingBox(-7.0d, -3.0d, -2.0d, 4.0d);
         assertEquals(new BoundingBox(-7.0d, -3.0d, -2.0d, 4.0d), initial);
         assertEquals(new BoundingBox(-7.0d, -2.0d, -2.0d, 4.0d), initial.expandToInclude(new BoundingBox(-6.0d, -2.0d, -1.0d, 3.0d)));
         assertEquals(new BoundingBox(-8.0d, -2.0d, -2.0d, 7.0d), initial.expandToInclude(new BoundingBox(-8.0d, -2.0d, 5.0d, 7.0d)));
         assertEquals(new BoundingBox(-7.0d, 14.0d, -2.0d, 11.0d), initial.expandToInclude(new BoundingBox(12.0d, 14.0d, 10.0d, 11.0d)));
     }
 
     @Test
    
     public void testContains() {
         BoundingBox initial = new BoundingBox(-7.0d, -3.0d, -2.0d, 4.0d);
         BoundingBox inner = new BoundingBox(-6.0d, -4.0d, -1.0d, 3.0d);
         BoundingBox outer = new BoundingBox(-10.0d, 0.0d, -4.0d, 6.0d);
 
         BoundingBox outsideInitial = new BoundingBox (-2.0d, -1.0d, -3.0d, 5.0d);
 
         assertTrue(initial.contains(inner));
         assertTrue(outer.contains(initial));
         assertTrue(outer.contains(inner));
         assertFalse(inner.contains(outsideInitial));
         assertFalse(initial.contains(outsideInitial));
         assertTrue(outer.contains(outsideInitial));
     }
 
     @Test
     public void testContainedBy() {
         BoundingBox initial = new BoundingBox(-7.0d, -3.0d, -2.0d, 4.0d);
         BoundingBox inner = new BoundingBox(-6.0d, -4.0d, -1.0d, 3.0d);
         BoundingBox outer = new BoundingBox(-10.0d, 0.0d, -4.0d, 6.0d);
 
         BoundingBox outsideInitial = new BoundingBox (-2.0d, -1.0d, -3.0d, 5.0d);
 
         assertTrue(inner.isContainedBy(initial));
         assertTrue(initial.isContainedBy(outer));
         assertTrue(inner.isContainedBy(outer));
         assertFalse(outsideInitial.isContainedBy(inner));
         assertFalse(outsideInitial.isContainedBy(initial));
         assertTrue(outsideInitial.isContainedBy(outer));
     }
 
     @Test
     public void testOutside() {
         BoundingBox initial = new BoundingBox(-7.0d, -3.0d, -2.0d, 4.0d);
         BoundingBox inner = new BoundingBox(-6.0d, -4.0d, -1.0d, 3.0d);
         BoundingBox outer = new BoundingBox(-10.0d, 0.0d, -4.0d, 6.0d);
 
         BoundingBox outsideInitial = new BoundingBox (-2.0d, -1.0d, -3.0d, 5.0d);
 
         assertFalse(initial.isOutside(inner));
         assertFalse(inner.isOutside(initial));
 
         assertFalse(outer.isOutside(initial));
         assertFalse(initial.isOutside(outer));
 
         assertFalse(outer.isOutside(inner));
         assertFalse(inner.isOutside(outer));
 
         assertTrue(inner.isOutside(outsideInitial));
         assertTrue(outsideInitial.isOutside(inner));
 
         assertTrue(initial.isOutside(outsideInitial));
         assertTrue(outsideInitial.isOutside(initial));
 
         assertFalse(outer.isOutside(outsideInitial));
         assertFalse(outsideInitial.isOutside(outer));
     }
 
     @Test
     public void testIntersects() {
         BoundingBox initial = new BoundingBox(-7.0d, -3.0d, -2.0d, 4.0d);
         BoundingBox intersects = new BoundingBox(-8.0d, -4.0d, -1.0d, 3.0d);
         BoundingBox alsoIntersects = new BoundingBox(-3.5d, 2.0d, -4.0d, 7.0d);
 
         assertTrue(initial.intersects(intersects));
         assertTrue(intersects.intersects(initial));
 
         assertTrue(initial.intersects(alsoIntersects));
         assertTrue(alsoIntersects.intersects(initial));
 
         assertFalse(intersects.intersects(alsoIntersects));
         assertFalse(alsoIntersects.intersects(intersects));
     }
 
     @Test
     public void multipleBoxTest() {
         BoundingBox bigBox = new BoundingBox(-100.0d, 100.0d, -100.0d, 100.0d);
         List<BoundingBox> boxes = new ArrayList<BoundingBox>();
         for (int i=0; i<10000; i++) {
             boxes.add(BoundingBox.containing(bigBox.randomPoint(), bigBox.randomPoint()));
         }
         for (int i=0; i<boxes.size();i++) {
             BoundingBox first = boxes.get(i);
             assertTrue(bigBox.contains(first));
             assertTrue(first.isContainedBy(bigBox));
             assertFalse(bigBox.isOutside(first));
             assertFalse(first.isOutside(bigBox));
             assertFalse(bigBox.intersects(first));
             assertFalse(first.intersects(bigBox));
             for (int j=i+1; j< boxes.size(); j++) {
                 BoundingBox second = boxes.get(j);
                 if (second.contains(first)) {
                     assertTrue(first.isContainedBy(second));
                     assertFalse(first.contains(second));
                     assertFalse(second.isOutside(first));
                     assertFalse(first.isOutside(second));
                     assertFalse(second.intersects(first));
                     assertFalse(first.intersects(second));
 
                 } else if (first.contains(second)) {
                     assertTrue(second.isContainedBy(first));
                     assertFalse(second.contains(first));
                     assertFalse(second.isOutside(first));
                     assertFalse(first.isOutside(second));
                     assertFalse(second.intersects(first));
                     assertFalse(first.intersects(second));
 
                 } else if (first.isOutside(second)) {
                     assertTrue(second.isOutside(first));
                     assertFalse(first.contains(second));
                     assertFalse(first.isContainedBy(second));
                     assertFalse(second.contains(first));
                     assertFalse(second.isContainedBy(first));
                     assertFalse(second.intersects(first));
                     assertFalse(first.intersects(second));
                 } else if (first.intersects(second)) {
                     assertTrue(second.intersects(first));
                     assertFalse(first.contains(second));
                     assertFalse(first.isContainedBy(second));
                     assertFalse(second.contains(first));
                     assertFalse(second.isContainedBy(first));
                     assertFalse(second.isOutside(first));
                     assertFalse(first.isOutside(second));
                 }
 
             }
         }
     }
 
     // Ignore for now, it doesn't really prove very much and takes a while to run
     @Test @Ignore
     public void multipleBoxOrderingTest() {
         BoundingBox bigBox = new BoundingBox(-100.0d, 100.0d, -100.0d, 100.0d);
         List<BoundingBox> boxes1 = new ArrayList<BoundingBox>();
         for (int i=0; i<100000; i++) {
             boxes1.add(BoundingBox.containing(bigBox.randomPoint(), bigBox.randomPoint()));
         }
         List<BoundingBox> boxes2 = new ArrayList<BoundingBox>(boxes1);
         List<BoundingBox> boxes3 = new ArrayList<BoundingBox>(boxes1);
         long start = System.nanoTime();
         Collections.sort(boxes1, comparePythagorus);
         long mid1 = System.nanoTime();
         Collections.sort(boxes2, comparePythagorusSquared);
         long mid2 = System.nanoTime();
         Collections.sort(boxes3, compareApproximate);
         long end = System.nanoTime();
 
         System.out.println("Pythagorus: " + (mid1 - start));
         System.out.println("Pythagorus Squared: " + (mid2 - mid1));
         System.out.println("Approximate: " + (end - mid2));
 
         assertEquals(boxes1, boxes2);
 
         int numEqual = 0;
         for (int i=0; i< boxes1.size(); i++) {
             if (boxes1.get(i) == boxes3.get(i)) numEqual++;
         }
         System.out.println("Number equal is " + numEqual);
     }
 
     @Test
     public void testEmptyBox() {
         BoundingBox empty = BoundingBox.empty();
         BoundingBox nonEmpty = new BoundingBox(2.0d, 11.0d, 3.0d, 8.0d);
         assertFalse(empty.contains(nonEmpty));
         BoundingBox expanded = empty.expandToInclude(nonEmpty);
         assertEquals(nonEmpty, expanded);
     }
 }

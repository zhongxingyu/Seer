 package com.example.android.lasergame.tests;


 import com.example.android.lasergame.*;
 
 public class TriangleTests extends GeometryTestCase {
 
     private Triangle triangle;
 
     public void setUp() {
         triangle = new Triangle(new Point(0, 0), new Point(0, 1), new Point(1, 0));
     }
 
     public void testShouldNotIntersectWithALineThatDoesNotPassThroughIt() {
         assertFalse(triangle.intersectsWith(makeLine(1, 1, 1, 2)));
     }
 
     public void testShouldIntersectWithALineThatPassesThroughIt() {
         assertTrue(triangle.intersectsWith(makeLine(1, 1, 0, 0)));
     }
 }

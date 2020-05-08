 package com.example.android.lasergame.tests;
 
 import com.example.android.lasergame.Beam;
 import com.example.android.lasergame.LaserCalculator;
 import com.example.android.lasergame.Line;
 import com.example.android.lasergame.Point;
 import com.example.android.lasergame.Triangle;
 
 import junit.framework.TestCase;
 
 public class LaserCalculatorTests extends TestCase {
     private LaserCalculator mCalc;
 
     public void setUp() {
         mCalc = new LaserCalculator(100, 100, 5.0, 175.0);
     }
 
     public void test45Degrees() {
         Beam b = mCalc.fireLaser(45);
         Beam e = new Beam(new Line(new Point(50, 100), new Point(0, 50)));
         e.addLine(new Line(new Point(0, 50), new Point(49, 0)));
         assertEquals(e, b);
     }
 
    public void test135Degrees() {
        Beam b = mCalc.fireLaser(135);
        Beam e = new Beam(new Line(new Point(50, 100), new Point(100, 50)));
        e.addLine(new Line(new Point(100, 50), new Point(49, 0)));
        assertEquals(e, b);
    }

     
     public void testReflectingOffAt45Degrees() {
         Triangle[] t = { new Triangle(new Point(1, 30), new Point(70, 30), new Point(1, 100)) };
         mCalc = new LaserCalculator(100, 100, 5.0, 175.0, t);
 
         Beam b = mCalc.fireLaser(91);
         Beam e = new Beam();
         e.addLine(new Line(new Point(50, 100), new Point(50, 49)));
         e.addLine(new Line(new Point(50, 49), new Point(100, 48)));
         assertEquals(e, b);
     }
     
     
     public void testReflectingOffAndKeepGoing() {
         Triangle[] t = { new Triangle(new Point(1, 30), new Point(70, 30), new Point(1, 100)) };
         mCalc = new LaserCalculator(100, 100, 5.0, 175.0, t);
 
         Beam b = mCalc.fireLaser(92);
         Beam e = new Beam();
         e.addLine(new Line(new Point(50, 100), new Point(51, 48)));
         e.addLine(new Line(new Point(51, 48), new Point(100, 46)));
         e.addLine(new Line(new Point(100, 46), new Point(5, 0)));
         assertEquals(e, b);
     }
 
     public void testReflectingOffAt45DegreesToLeft() {
         Triangle[] t = { new Triangle(new Point(100, 30), new Point(30, 30), new Point(100, 100)) };
         mCalc = new LaserCalculator(100, 100, 5.0, 175.0, t);
 
         Beam b = mCalc.fireLaser(91);
         Beam e = new Beam();
         e.addLine(new Line(new Point(50, 100), new Point(50, 50)));
         e.addLine(new Line(new Point(50, 50), new Point(0, 50)));
         assertEquals(e, b);
     }
 
     public void testReflectingOffAt60Degrees() {
         Triangle[] t = { new Triangle(new Point(1, 70), new Point(70, 1), new Point(1, 1)) };
         mCalc = new LaserCalculator(100, 100, 5.0, 175.0, t);
 
         Beam b = mCalc.fireLaser(60);
         Beam e = new Beam();
         e.addLine(new Line(new Point(50,100), new Point(21, 49)));
         e.addLine(new Line(new Point(21, 49), new Point(100, 93)));
         assertEquals(e, b);
     }
 
     public void test35Degrees() {
         Beam b = mCalc.fireLaser(35);
         Beam e = new Beam(new Line(new Point(50, 100), new Point(0, 64)));
         e.addLine(new Line(new Point(0, 64), new Point(91, 0)));
         assertEquals(e, b);
     }
 
     public void test5Degrees() {
         Beam b = mCalc.fireLaser(5);
         Beam e = new Beam(new Line(new Point(50, 100), new Point(0, 95)));
         e.addLine(new Line(new Point(0, 95), new Point(100, 86)));
         e.addLine(new Line(new Point(100, 86), new Point(0, 77)));
         e.addLine(new Line(new Point(0, 77), new Point(100, 68)));
         e.addLine(new Line(new Point(100, 68), new Point(0, 59)));
         e.addLine(new Line(new Point(0, 59), new Point(100, 50)));
         e.addLine(new Line(new Point(100, 50), new Point(0, 41)));
         e.addLine(new Line(new Point(0, 41), new Point(100, 32)));
         e.addLine(new Line(new Point(100, 32), new Point(0, 23)));
         e.addLine(new Line(new Point(0, 23), new Point(100, 14)));
         e.addLine(new Line(new Point(100, 14), new Point(0, 5)));
         e.addLine(new Line(new Point(0, 5), new Point(57, 0)));
         assertEquals(e, b);
     }
 
     public void test4DegreesIsTreatedLikeFive() {
         Beam b = mCalc.fireLaser(4);
         Beam e = new Beam(new Line(new Point(50, 100), new Point(0, 95)));
         e.addLine(new Line(new Point(0, 95), new Point(100, 86)));
         e.addLine(new Line(new Point(100, 86), new Point(0, 77)));
         e.addLine(new Line(new Point(0, 77), new Point(100, 68)));
         e.addLine(new Line(new Point(100, 68), new Point(0, 59)));
         e.addLine(new Line(new Point(0, 59), new Point(100, 50)));
         e.addLine(new Line(new Point(100, 50), new Point(0, 41)));
         e.addLine(new Line(new Point(0, 41), new Point(100, 32)));
         e.addLine(new Line(new Point(100, 32), new Point(0, 23)));
         e.addLine(new Line(new Point(0, 23), new Point(100, 14)));
         e.addLine(new Line(new Point(100, 14), new Point(0, 5)));
         e.addLine(new Line(new Point(0, 5), new Point(57, 0)));
         assertEquals(e, b);
     }
 
     public void testLastHittingTheLeftWall() {
         Beam b = mCalc.fireLaser(63);
         Beam e = new Beam(new Line(new Point(50, 100), new Point(0, 1)));
         e.addLine(new Line(new Point(0, 1), new Point(0, 0)));
         assertEquals(e, b);
     }
 
     public void testFirstHittingTheBackWall() {
         Beam b = mCalc.fireLaser(64);
         Beam e = new Beam(new Line(new Point(50, 100), new Point(1, 0)));
         assertEquals(e, b);
     }
 
     public void testHittingTheBackWall() {
         Beam b = mCalc.fireLaser(90);
         Beam e = new Beam(new Line(new Point(50, 100), new Point(50, 0)));
         assertEquals(e, b);
     }
 
     public void testLastHittingTheBackWall() {
         Beam b = mCalc.fireLaser(116);
         Beam e = new Beam(new Line(new Point(50, 100), new Point(98, 0)));
         assertEquals(e, b);
     }
 
     public void testFirstHittingTheRightWall() {
         Beam b = mCalc.fireLaser(118);
         Beam e = new Beam(new Line(new Point(50, 100), new Point(100, 5)));
         e.addLine(new Line(new Point(100, 5), new Point(97, 0)));
         assertEquals(e, b);
     }
 
     public void test179DegreesIsTreatedLike175() {
         Beam b = mCalc.fireLaser(179);
         Beam e = new Beam(new Line(new Point(50, 100), new Point(100, 95)));
         e.addLine(new Line(new Point(100, 95), new Point(0, 86)));
         e.addLine(new Line(new Point(0, 86), new Point(100, 77)));
         e.addLine(new Line(new Point(100, 77), new Point(0, 68)));
         e.addLine(new Line(new Point(0, 68), new Point(100, 59)));
         e.addLine(new Line(new Point(100, 59), new Point(0, 50)));
         e.addLine(new Line(new Point(0, 50), new Point(100, 41)));
         e.addLine(new Line(new Point(100, 41), new Point(0, 32)));
         e.addLine(new Line(new Point(0, 32), new Point(100, 23)));
         e.addLine(new Line(new Point(100, 23), new Point(0, 14)));
         e.addLine(new Line(new Point(0, 14), new Point(100, 5)));
         e.addLine(new Line(new Point(100, 5), new Point(42, 0)));
         assertEquals(e, b);
     }
 
     public void testAiming175Degrees() {
         Beam b = mCalc.fireLaser(175);
         Beam e = new Beam(new Line(new Point(50, 100), new Point(100, 95)));
         e.addLine(new Line(new Point(100, 95), new Point(0, 86)));
         e.addLine(new Line(new Point(0, 86), new Point(100, 77)));
         e.addLine(new Line(new Point(100, 77), new Point(0, 68)));
         e.addLine(new Line(new Point(0, 68), new Point(100, 59)));
         e.addLine(new Line(new Point(100, 59), new Point(0, 50)));
         e.addLine(new Line(new Point(0, 50), new Point(100, 41)));
         e.addLine(new Line(new Point(100, 41), new Point(0, 32)));
         e.addLine(new Line(new Point(0, 32), new Point(100, 23)));
         e.addLine(new Line(new Point(100, 23), new Point(0, 14)));
         e.addLine(new Line(new Point(0, 14), new Point(100, 5)));
         e.addLine(new Line(new Point(100, 5), new Point(42, 0)));
         assertEquals(e, b);
     }
 }

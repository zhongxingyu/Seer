 /*-
  * Copyright 2013 Diamond Light Source Ltd.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package uk.ac.diamond.scisoft.analysis.roi;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import org.junit.Test;
 
 public class ROITest {
 	@Test
 	public void testPointROI() {
 		PointROI p = new PointROI(10.0, 20.5);
 
 		IRectangularROI b = p.getBounds();
 		assertEquals(b.getPointX(), p.getPointX(), 1e-15);
 		assertEquals(b.getPointY(), p.getPointY(), 1e-15);
 		assertEquals(b.getLength(0), 0, 1e-15);
 		assertEquals(b.getLength(1), 0, 1e-15);
 		assertTrue(p.containsPoint(p.getPointX(), p.getPointY()));
 		assertFalse(p.containsPoint(0, p.getPointY()));
 		assertTrue(p.isNearOutline(p.getPointX(), p.getPointY(), 2.5));
 		assertTrue(p.isNearOutline(p.getPointX()-1, p.getPointY(), 2.5));
 		assertFalse(p.isNearOutline(p.getPointX()-1, p.getPointY()+3, 2.5));
 	}
 
 	@Test
 	public void testLinearROI() {
 		LinearROI l = new LinearROI();
 		l.setLength(10);
 
 		IRectangularROI b = l.getBounds();
 		assertEquals(b.getPointX(), l.getPointX(), 1e-15);
 		assertEquals(b.getPointY(), l.getPointY(), 1e-15);
 		assertEquals(b.getLength(0), 10, 1e-15);
 		assertEquals(b.getLength(1), 0, 1e-15);
 		assertTrue(l.containsPoint(l.getPointX(), l.getPointY()));
 		assertFalse(l.containsPoint(-2, l.getPointY()));
 		assertTrue(l.isNearOutline(l.getPointX(), l.getPointY(), 2.5));
 		assertTrue(l.isNearOutline(l.getPointX(), l.getPointY()-1, 2.5));
 		assertFalse(l.isNearOutline(l.getPointX()-1, l.getPointY()+3, 2.5));
 
 		l.setAngleDegrees(45.);
 		assertTrue(l.containsPoint(l.getPointX(), l.getPointY()));
 		assertTrue(l.containsPoint(l.getPointX() + 2, l.getPointY() + 2));
 		assertFalse(l.containsPoint(2, l.getPointY()));
 		assertFalse(l.containsPoint(-2, l.getPointY()));
 	}
 
 	@Test
 	public void testRectangularROI() {
 		RectangularROI r = new RectangularROI();
 		r.setLengths(10.5, 23);
 
 		IRectangularROI b = r.getBounds();
 		assertEquals(boundingBox(r), b);
 		assertEquals(b.getPointX(), r.getPointX(), 1e-15);
 		assertEquals(b.getPointY(), r.getPointY(), 1e-15);
 		assertEquals(b.getLength(0), 10.5, 1e-15);
 		assertEquals(b.getLength(1), 23, 1e-15);
 		assertTrue(r.containsPoint(r.getPointX(), r.getPointY()));
 		assertFalse(r.containsPoint(-2, r.getPointY()));
 		assertTrue(r.isNearOutline(r.getPointX(), r.getPointY(), 2.5));
 		assertTrue(r.isNearOutline(r.getPointX()-1, r.getPointY(), 2.5));
 		assertFalse(r.isNearOutline(r.getPointX()-1, r.getPointY()-3, 2.5));
 
 		r.setAngleDegrees(35.);
 		assertEquals(boundingBox(r), r.getBounds());
 		assertTrue(r.containsPoint(r.getPointX(), r.getPointY()));
 		assertFalse(r.containsPoint(2, r.getPointY()));
 		assertFalse(r.containsPoint(-2, r.getPointY()));
 
 		r.setAngleDegrees(145.);
 		assertEquals(boundingBox(r), r.getBounds());
 		assertTrue(r.containsPoint(r.getPointX(), r.getPointY()));
 		assertFalse(r.containsPoint(2, r.getPointY()));
 		assertTrue(r.containsPoint(-2, r.getPointY()));
 
 		r.setAngleDegrees(260.);
 		assertEquals(boundingBox(r), r.getBounds());
 		assertTrue(r.containsPoint(r.getPointX(), r.getPointY()));
 		assertFalse(r.containsPoint(2, r.getPointY()));
 		assertFalse(r.containsPoint(-2, r.getPointY()));
 
 		r.setAngleDegrees(342.);
 		assertEquals(boundingBox(r), r.getBounds());
 		assertTrue(r.containsPoint(r.getPointX(), r.getPointY()));
 		assertTrue(r.containsPoint(2, r.getPointY()));
 		assertFalse(r.containsPoint(-2, r.getPointY()));
 	}
 
 	public RectangularROI boundingBox(RectangularROI r) {
 		double[] p = r.getPointRef();
 		double[] max = p.clone();
 		double[] min = max.clone();
 
 		p = r.getPoint(1, 0);
 		ROIUtils.updateMaxMin(max, min, p);
 
 		p = r.getPoint(1, 1);
 		ROIUtils.updateMaxMin(max, min, p);
 
 		p = r.getPoint(0, 1);
 		ROIUtils.updateMaxMin(max, min, p);
 
 		RectangularROI b = new RectangularROI(1, 0);
 		b.setPoint(min);
 		max[0] -= min[0];
 		max[1] -= min[1];
 		b.setLengths(max);
 		return b;
 	}
 
 	private static final int ANGLES = 180;
 
 	@Test
 	public void testCircularROI() {
 		CircularROI c = new CircularROI(10);
 
 		IRectangularROI b = c.getBounds();
 		assertEquals(b.getPointX() + 10, c.getPointX(), 1e-15);
 		assertEquals(b.getPointY() + 10, c.getPointY(), 1e-15);
 		assertEquals(b.getLength(0), 20, 1e-15);
 		assertEquals(b.getLength(1), 20, 1e-15);
 		assertTrue(c.containsPoint(c.getPointX(), c.getPointY()));
 		assertFalse(c.containsPoint(-20, c.getPointY()));
 		CircularROI sc = new CircularROI(9.9);
 		CircularROI lc = new CircularROI(10.1);
 		for (int i = 0; i < ANGLES; i++) {
 			double a = (i * Math.PI)/ANGLES;
 			assertTrue(c.containsPoint(sc.getPoint(a)));
 			assertTrue(c.isNearOutline(sc.getPoint(a), 1));
 			assertFalse(c.isNearOutline(sc.getPoint(a), 0.01));
 			assertFalse(c.containsPoint(lc.getPoint(a)));
 			assertTrue(c.isNearOutline(lc.getPoint(a), 1));
 			assertFalse(c.isNearOutline(lc.getPoint(a), 0.01));
 		}
 		assertTrue(c.isNearOutline(c.getPointX()+10, c.getPointY(), 2.5));
 		assertTrue(c.isNearOutline(c.getPointX()+10, c.getPointY()-1, 2.5));
 		assertFalse(c.isNearOutline(c.getPointX()+9, c.getPointY()+9, 2.5));
 	}
 
 	
 	@Test
 	public void testEllipticalROI() {
 		EllipticalROI e = new EllipticalROI(10, 5, Math.PI/4., 0, 0);
 		assertTrue(e.containsPoint(3,3));
 		assertTrue(e.containsPoint(7,6));
 		assertTrue(e.containsPoint(7,7));
 		assertFalse(e.containsPoint(10.0001,10));
 		assertFalse(e.containsPoint(9,9));
 
 		IRectangularROI b = e.getBounds();
 		double side = 15.811388300841898;
 		assertEquals(b.getPointX() + side/2, e.getPointX(), 1e-15);
 		assertEquals(b.getPointY() + side/2, e.getPointY(), 1e-15);
 		assertTrue(e.containsPoint(e.getPointX(), e.getPointY()));
 		assertFalse(e.containsPoint(-20, e.getPointY()));
 
 		EllipticalROI se = new EllipticalROI(9.9, 4.9, Math.PI/4., 0, 0);
 		EllipticalROI le = new EllipticalROI(10.1, 5.1, Math.PI/4., 0, 0);
 		for (int i = 0; i < ANGLES; i++) {
 			double a = (i * Math.PI)/ANGLES;
 			double[] ps = se.getPoint(a);
 			assertTrue(e.containsPoint(ps));
 			assertTrue(e.isNearOutline(ps, 1));
 			assertFalse(e.isNearOutline(ps, 0.01));
 
 			double[] pl = le.getPoint(a);
 			assertFalse(e.containsPoint(pl));
 			assertTrue(e.isNearOutline(pl, 1));
 			assertFalse(e.isNearOutline(pl, 0.01));
 
 			double[] p = e.getPoint(a);
 			checkPoint(e.getVerticalIntersectionParameters(p[0]), p, 0, e);
 			checkPoint(e.getHorizontalIntersectionParameters(p[1]), p, 1, e);
 		}
 
 		assertTrue(e.getVerticalIntersectionParameters(e.getBounds().getEndPoint()[0]+0.1) == null);
 		assertTrue(e.getVerticalIntersectionParameters(e.getBounds().getPointRef()[0]-0.1) == null);
 		assertTrue(e.getHorizontalIntersectionParameters(e.getBounds().getEndPoint()[1]+0.1) == null);
 		assertTrue(e.getHorizontalIntersectionParameters(e.getBounds().getPointRef()[1]-0.1) == null);
 
 		// check a particular line through origin
 		double[] p = e.getHorizontalIntersectionParameters(0);
 		assertTrue(p != null);
 		double ymin = -2e-15;
 		assertTrue(e.getPoint(p[0])[1] >= ymin);
 		assertTrue(e.getPoint(p[1])[1] >= ymin);
 		assertFalse(e.getPoint(p[0]*0.99)[1] >= ymin);
 		assertTrue(e.getPoint(p[0]*1.01)[1] >= ymin);
 		assertTrue(e.getPoint(p[1]*0.99)[1] >= ymin);
 		assertFalse(e.getPoint(p[1]*1.01)[1] >= ymin);
 
 		RectangularROI rect = new RectangularROI(side, 0);
 		assertFalse(e.isContainedBy(rect));
 
 		rect.setPoint(-side/2, -side/2);
 		assertTrue(e.isContainedBy(rect));
 		rect.setLengths(20, 10);
 		assertFalse(e.isContainedBy(rect));
 
 		rect.setPoint(-10, -5);
 		e.setSemiAxis(1, 5);
 		e.setAngleDegrees(0);
 		assertTrue(e.isContainedBy(rect));
 
 		e.setAngleDegrees(90);
 		assertFalse(e.isContainedBy(rect));
 
 		rect.setPoint(-5, -10);
 		assertFalse(e.isContainedBy(rect));
 
 		rect.setLengths(10, 20);
 		assertTrue(e.isContainedBy(rect));
 
 		e.setAngleDegrees(45);
 		assertFalse(e.isContainedBy(rect));
 
 		rect.setPoint(-10, -10);
 		rect.setLengths(20, 20);
 		assertTrue(e.isContainedBy(rect));
 
 		double d = Math.sqrt(0.5*(10*10 + 5*5));
 		rect.setPoint(-d, -d);
 		rect.setLengths(2*d, 2*d);
 		assertTrue(e.isContainedBy(rect));
 
 		d *= 0.99;
 		rect.setPoint(-d, -d);
 		rect.setLengths(2*d, 2*d);
 		assertFalse(e.isContainedBy(rect));
 	}
 
 	public void checkPoint(double[] t, double[] p, int i, IParametricROI e) {
 		if (t.length == 1)
 			assertTrue(Math.abs(e.getPoint(t[0])[i] - p[i]) < 1e-8);
 		else
 			assertTrue(Math.abs(e.getPoint(t[0])[i] - p[i]) < 1e-8 || Math.abs(e.getPoint(t[1])[i] - p[i]) < 1e-8);
 	}
 
 	@Test
 	public void testSectorROI() {
 		SectorROI s = new SectorROI();
 		s.setPoint(3,  3);
 		s.setRadii(5, 10);
 		s.setAnglesDegrees(23, 69);
 
 		IRectangularROI b = s.getBounds();
 		assertFalse(b.containsPoint(s.getPointX(), s.getPointY()));
 		assertTrue(b.containsPoint(8, 8));
 
 		CircularROI sc = new CircularROI(9.9);
 		sc.setPoint(s.getPoint());
 		CircularROI lc = new CircularROI(10.1);
 		lc.setPoint(s.getPoint());
 		double ar = s.getAngle(1) - s.getAngle(0);
 		for (int i = 0; i < ANGLES; i++) {
 			double a = s.getAngle(0) + (i * ar)/(ANGLES - 1);
 			assertTrue(s.containsPoint(sc.getPoint(a)));
 			assertTrue(s.isNearOutline(sc.getPoint(a), 1));
 			boolean f = s.isNearOutline(sc.getPoint(a), 0.01);
 			if (i > 0 && i < ANGLES-1)
 				assertFalse(f);
 			else
 				assertTrue(f);
 			assertFalse(s.containsPoint(lc.getPoint(a)));
 			assertTrue(s.isNearOutline(lc.getPoint(a), 1));
 			assertFalse(s.isNearOutline(lc.getPoint(a), 0.01));
 		}
 	}
 
 	private static final int SIDES = 24;
 
 	@Test
 	public void testPolylineROI() {
 		PolylineROI p = new PolylineROI();
 		double r = 20;
 		for (int i = 0; i < SIDES; i++) {
 			double a = (2 * i * Math.PI) / SIDES;
 			p.insertPoint(r*Math.cos(a), r*Math.sin(a));
 		}
 
 		IRectangularROI b = p.getBounds();
 		assertEquals(b.getPointX(), -r, 1e-15);
 		assertEquals(b.getPointY(), -r, 1e-15);
 		assertEquals(b.getLength(0), 2*r, 1e-15);
 		assertEquals(b.getLength(1), 2*r, 1e-15);
 
 		assertTrue(p.containsPoint(p.getPointX(), p.getPointY()));
 		assertFalse(p.containsPoint(0, 0));
 
 		assertTrue(p.isNearOutline(p.getPointRef(), 0.01));
 		assertTrue(p.isNearOutline(p.getPointX() - 0.5, p.getPointY() + 1, 2.5));
 		assertFalse(p.isNearOutline(p.getPointX() + 2.5, p.getPointY() + 4, 2.5));
 	}
 
 	@Test
 	public void testPolygonalROI() {
 		PolygonalROI p = new PolygonalROI();
 		double r = 20;
 		for (int i = 0; i < SIDES; i++) {
 			double a = (2 * i * Math.PI) / SIDES;
 			p.insertPoint(r*Math.cos(a), r*Math.sin(a));
 		}
 
 		IRectangularROI b = p.getBounds();
 		assertEquals(b.getPointX(), -r, 1e-15);
 		assertEquals(b.getPointY(), -r, 1e-15);
 		assertEquals(b.getLength(0), 2*r, 1e-15);
 		assertEquals(b.getLength(1), 2*r, 1e-15);
 
 		assertTrue(p.containsPoint(p.getPointX(), p.getPointY()));
 		assertTrue(p.containsPoint(0, 0));
 		assertFalse(p.containsPoint(1, r));
 
 		assertTrue(p.isNearOutline(p.getPointRef(), 0.01));
 		assertTrue(p.isNearOutline(p.getPointX() - 0.5, p.getPointY() - 1, 2.5));
		assertFalse(p.isNearOutline(p.getPointX() - 2.5, p.getPointY() + 4, 2.5));
 	}
 
 	@Test
 	public void testParabolicROI() {
 		ParabolicROI p = new ParabolicROI(3, 0, 0, 0);
 
 		double distance = 5.5;
 		double limit = p.getStartAngle(distance);
 		for (int i = 0; i < SIDES; i++) {
 			double a = (2 * i * Math.PI) / SIDES;
 			double[] pt = p.getPoint(a);
 			if (a > limit && a < 2* Math.PI - limit) {
 				assertTrue(Math.hypot(pt[0], pt[1]) <= distance);
 				checkPoint(p.getVerticalIntersectionParameters(pt[0]), pt, 0, p);
 				checkPoint(p.getHorizontalIntersectionParameters(pt[1]), pt, 1, p);
 			}
 		}
 	}
 
 	@Test
 	public void testHyperbolicROI() {
 		HyperbolicROI h = new HyperbolicROI(3, 2, 0, 0, 0);
 
 		double distance = 5.5;
 		double limit = h.getStartAngle(distance);
 		for (int i = 0; i < SIDES; i++) {
 			double a = (2 * i * Math.PI) / SIDES;
 			double[] pt = h.getPoint(a);
 			if (a > limit && a < 2* Math.PI - limit) {
 				assertTrue(Math.hypot(pt[0], pt[1]) <= distance);
 				checkPoint(h.getVerticalIntersectionParameters(pt[0]), pt, 0, h);
 				checkPoint(h.getHorizontalIntersectionParameters(pt[1]), pt, 1, h);
 			}
 		}
 	}
 }

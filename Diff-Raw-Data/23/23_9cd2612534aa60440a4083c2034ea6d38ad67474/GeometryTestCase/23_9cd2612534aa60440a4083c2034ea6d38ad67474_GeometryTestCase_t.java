 package com.example.android.lasergame.tests;
import com.example.android.lasergame.*;
 import junit.framework.TestCase;
 
 public abstract class GeometryTestCase extends TestCase {
	protected static Line makeLine(int x1, int y1, int x2, int y2) {
		return new Line(new Point(x1, y1), new Point(x2, y2));
	}
 
 	public GeometryTestCase() {
 		super();
 	}
 
 	public GeometryTestCase(String name) {
 		super(name);
 	}
 }

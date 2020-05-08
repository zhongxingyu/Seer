 package com.thoughtworks.thoughtferret;
 
 import android.graphics.Point;
 
 public class MathUtils {
 
 	public static int clamp(int value, int min, int max) {
 		if (value < min) {
 			value = min;
 		}
 		if (value > max) {
 			value = max;
 		}
 		return value;
 	}
 
 	public static Point getMiddle(Point a, Point b) {
 		Point mid = new Point((a.x + b.x) / 2, (a.y + b.y) / 2);
 		return mid;
 	}
 
 }

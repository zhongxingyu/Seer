 //    This file is part of RobotsVsAliensInSpace. 
 //
 //	  Author: Lovisa Irpa Helgadottir
 //
 //    RobotsCsAliensInSpace is free software: you can redistribute it and/or modify
 //    it under the terms of the GNU General Public License as published by
 //    the Free Software Foundation, either version 3 of the License, or
 //    (at your option) any later version.
 //
 //    RobotsCsAliensInSpace is distributed in the hope that it will be useful,
 //    but WITHOUT ANY WARRANTY; without even the implied warranty of
 //    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 //    GNU General Public License for more details.
 //
 //    You should have received a copy of the GNU General Public License
 //    along with RobotsCsAliensInSpace.  If not, see <http://www.gnu.org/licenses/>.
 package com.plovergames.framework.math;
 
 import android.util.Log;
 
 public class OverlapTester {
 	public static boolean overlapCircles(Circle c1, Circle c2) {
 		float distance = c1.center.distSquared(c2.center);
 		float radiusSum = c1.radius + c2.radius;
 		return distance <= radiusSum * radiusSum;
 	}
 
 	public static boolean overlapRectangles(Rectangle r1, Rectangle r2) {
 		if(r1.lowerLeft.x < r2.lowerLeft.x + r2.width &&
 				r1.lowerLeft.x + r1.width > r2.lowerLeft.x &&
 				r1.lowerLeft.y < r2.lowerLeft.y + r2.height &&
 				r1.lowerLeft.y + r1.height > r2.lowerLeft.y)
 			return true;
 		else
 			return false;
 	}
 
 	public static boolean overlapCircleRectangle(Circle c, Rectangle r) {
 		float closestX = c.center.x;
 		float closestY = c.center.y;
 
 		if(c.center.x < r.lowerLeft.x) {
 			closestX = r.lowerLeft.x; 
 		} 
 		else if(c.center.x > r.lowerLeft.x + r.width) {
 			closestX = r.lowerLeft.x + r.width;
 		}
 
 		if(c.center.y < r.lowerLeft.y) {
 			closestY = r.lowerLeft.y;
 		} 
 		else if(c.center.y > r.lowerLeft.y + r.height) {
 			closestY = r.lowerLeft.y + r.height;
 		}
 
 		return c.center.distSquared(closestX, closestY) < c.radius * c.radius;           
 	}
 
 	public static boolean pointInCircle(Circle c, Vector2 p) {
 		return c.center.distSquared(p) < c.radius * c.radius;
 	}
 
 	public static boolean pointInCircle(Circle c, float x, float y) {
 		return c.center.distSquared(x, y) < c.radius * c.radius;
 	}
 
 	public static boolean pointInRectangle(Rectangle r, Vector2 p) {
 		return r.lowerLeft.x <= p.x && r.lowerLeft.x + r.width >= p.x &&
 				r.lowerLeft.y <= p.y && r.lowerLeft.y + r.height >= p.y;
 	}
 
 	public static boolean pointInRectangle(Rectangle r, float x, float y) {
 		return r.lowerLeft.x <= x && r.lowerLeft.x + r.width >= x &&
 				r.lowerLeft.y <= y && r.lowerLeft.y + r.height >= y;
 	}
 
 	public static boolean onTopOfRectangles(Rectangle top, Rectangle bottom, int direction){
 		switch(direction){
 		case 0:
 			if (top.lowerLeft.y +top.height >= bottom.lowerLeft.y &&  top.lowerLeft.y<=bottom.lowerLeft.y && 
 				top.lowerLeft.x-0.1f >= bottom.lowerLeft.x && top.lowerLeft.x-0.1f+top.width/2<=bottom.lowerLeft.x+bottom.width)
 				return true;
 			else return false;
 		case 90:
			if (top.lowerLeft.y >= bottom.lowerLeft.y &&  top.lowerLeft.y+top.height<bottom.lowerLeft.y+bottom.height && 
				top.lowerLeft.x-0.1f >= bottom.lowerLeft.x && top.lowerLeft.x-0.1f<=bottom.lowerLeft.x+bottom.width)
 				return true;			
 			else return false;
 
 		case 180:
 			if (top.lowerLeft.y >= bottom.lowerLeft.y &&  top.lowerLeft.y<=bottom.lowerLeft.y+bottom.height && 
 				top.lowerLeft.x-0.1f >= bottom.lowerLeft.x && top.lowerLeft.x-0.1f+top.width/2<=bottom.lowerLeft.x+bottom.width)
 				return true;
 			else
 				return false;
 		case 270:
 			if (top.lowerLeft.y >= bottom.lowerLeft.y  &&  top.lowerLeft.y<bottom.lowerLeft.y+bottom.height && 
 				top.lowerLeft.x+top.width >= bottom.lowerLeft.x  &&  top.lowerLeft.x-0.1f<=bottom.lowerLeft.x)
 				return true;
 			else return false;
 		default :
 			return false;
 
 
 		}
 
 	}
 	public static boolean overlapHalfRectangles(Rectangle r1, Rectangle r2) {
 		if(r1.lowerLeft.x < r2.lowerLeft.x + r2.width/2 &&
 				r1.lowerLeft.x + r1.width/2 > r2.lowerLeft.x &&
 				r1.lowerLeft.y < r2.lowerLeft.y + r2.height/2 &&
 				r1.lowerLeft.y + r1.height/2 > r2.lowerLeft.y)
 			return true;
 		else
 			return false;
 	}
 
 	public static boolean overlapLaser(Rectangle r1, Rectangle r2) {
 
 		if(r1.lowerLeft.x < r2.lowerLeft.x + r2.width/4 &&
 				r1.lowerLeft.x + r1.width/2 > r2.lowerLeft.x &&
 				r1.lowerLeft.y < r2.lowerLeft.y + r2.height/2 &&
 				r1.lowerLeft.y + r1.height/2 > r2.lowerLeft.y)
 			return true;
 		else	
 			return false; 
 
 	}
 
	public static boolean overlapHalfXRectangles(Rectangle r1, Rectangle r2, int direction) {
 		switch(direction){
 		case 0:
 			if(r1.lowerLeft.x < r2.lowerLeft.x + r2.width/2 &&
 					r1.lowerLeft.x + r1.width> r2.lowerLeft.x + r2.width/2  &&
 					r1.lowerLeft.y < r2.lowerLeft.y + r2.height/2 &&
 					r1.lowerLeft.y + r1.height/2 > r2.lowerLeft.y)
 				return true;
 			else
 				return false;
 		case 90:
 			if(r1.lowerLeft.x < r2.lowerLeft.x + r2.width/4 &&
 					r1.lowerLeft.x + r1.width/2> r2.lowerLeft.x &&
 					r1.lowerLeft.y < r2.lowerLeft.y + r2.height&&
 					r1.lowerLeft.y + r1.height-0.1f > r2.lowerLeft.y){
 /*				Log.v("lowerLeft.x",""+r1.lowerLeft.x+","+r2.lowerLeft.x);
 				Log.v("lowerLeft.y",""+r1.lowerLeft.y+","+r2.lowerLeft.y);*/
 			
 				return true;}
 			else
 				return false;
 		case 180: 
 			if(r1.lowerLeft.x < r2.lowerLeft.x + r2.width/2 &&
 					r1.lowerLeft.x + r1.width/2> r2.lowerLeft.x &&
 					r1.lowerLeft.y < r2.lowerLeft.y + r2.height/2 &&
 					r1.lowerLeft.y  > r2.lowerLeft.y- r2.height/2)
 				return true;
 			else
 				return false;
 		case 270:
 			if(r1.lowerLeft.x < r2.lowerLeft.x + r2.width/4 &&
 					r1.lowerLeft.x + r1.width/2> r2.lowerLeft.x &&
 					r1.lowerLeft.y < r2.lowerLeft.y + r2.height&&
 					r1.lowerLeft.y + r1.height > r2.lowerLeft.y)
 				return true;
 			else
 				return false;
 
 		default: 
 			return false; 
 		}
 
 
 	}
 }

 package com.java.rubyquiz;
 
 import java.awt.geom.Point2D;
 import java.util.Scanner;
 
 /**
  * @author robin
  * date : 09/22/2013
  * problem: http://rubyquiz.strd6.com/quizzes/234-random-points-within-a-circle
  *
  */
 
 public class RandomPointInACircle {
 	private double radius;
 	private double x;
 	private double y;
 	private Point2D.Double origin;
 	private Point2D.Double location;
 	
 	public RandomPointInACircle(double radius, double x, double y) {
 		this.radius = radius;
		if(radius <= 0){
			throw new IllegalArgumentException("radius value should be positive.");
		}
 		this.origin = new Point2D.Double(x, y);
 		this.location = new Point2D.Double();
 	}
 	
 	public double getRadius() {
 		return radius;
 	}
 
 	public void setRadius(double radius) {
 		this.radius = radius;
 	}
 
 	public double getX() {
 		return x;
 	}
 
 	public void setX(double x) {
 		this.x = x;
 	}
 
 	public double getY() {
 		return y;
 	}
 
 	public void setY(double y) {
 		this.y = y;
 	}
 
 	public Point2D.Double getOrigin() {
 		return origin;
 	}
 
 	public void setOrigin(Point2D.Double origin) {
 		this.origin = origin;
 	}
 	
 	public Point2D.Double getLocation() {
 		return location;
 	}
 
 	public void setLocation(Point2D.Double location) {
 		this.location = location;
 	}
 
 	/**
 	 * 
 	 * @param radius : radius of the circle
 	 * @param x1 : x-coordinate of the center of the circle
 	 * @param y1 : y-coordinate of the center of the circle
 	 * 
 	 * this function finds a random set of x and y coordinate within a circle defined by the
 	 * x-coordinate(x1) and y-coordinate(y1) for origin and radius and sets the point object.
 	 */
 	public void getRandomPoint(double radius, double x1, double y1){
 		double angle = Math.random()*Math.PI*2;
 		x = x1+Math.cos(angle) * radius;
 	    y = y1+Math.sin(angle) * radius;
 	    location.setLocation(x, y);
 	}
 	
 	/**
 	 * 
 	 * @param origin - coordinates of the origin of the circle
 	 * @param x1 - x-coordinate of the new point
 	 * @param y1 - y-coordinate of the new point
 	 * @param radius - radius of the circle
 	 * @return - true if the point[x1,y1] lies within/on the circle at point[origin] and radius. Else it returns false
 	 */
 	public boolean isWithin(Point2D.Double origin, double x1, double y1, double radius){
 		boolean value = false;
 		if(Point2D.Double.distance(origin.x, origin.y, x1, y1) > radius)
 			value = false;
 		else 
 			value = true;	
 		return value;
 	}
 
 	@Override
 	public String toString() {
 		return "Random Point In A Circle of radius=" + radius + " and origin=" + origin +"]\n x=" + x + ",\n y="
 				+ y + ",\n location=" + location +"]";
 	}
 	
 	public static void main(String[] args){
 		double x1 = 0.0, y1 = 0.0, radius1 = 0.0;
 		System.out.println("-------RANDOM POINTS IN A CIRCLE-------------");
		System.out.print("Enter the radius of the circle : ");
		Scanner sc = new Scanner(System.in);
		radius1 = sc.nextDouble();
 		System.out.print("Enter the x-coordinate of the origin of circle : ");
 		Scanner sc1 = new Scanner(System.in);
 		x1 = sc1.nextDouble();
 		System.out.print("Enter the y-coordinate of the origin of circle : ");
 		Scanner sc2 = new Scanner(System.in);
 		y1 = sc2.nextDouble();
 		RandomPointInACircle test = new RandomPointInACircle(radius1, x1, y1);
 		test.getRandomPoint(radius1, x1, y1);
 		System.out.println(test.toString());
 		System.out.println("Is the point within the circle: "+test.isWithin(test.getOrigin(), 0.0, -3.0, 3.0));
 	}
 	
 	
 }

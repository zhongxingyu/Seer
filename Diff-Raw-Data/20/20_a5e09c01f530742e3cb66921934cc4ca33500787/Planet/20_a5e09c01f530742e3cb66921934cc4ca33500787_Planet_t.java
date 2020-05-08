 package model;
 
 import java.awt.Graphics;
 import java.awt.Point;
 import java.awt.Rectangle;
 
 /**
  * Class Planet
  * Information holder for planet
  * 
  * @author AN PHAM
  * @version 10/11/2012
  */
 public class Planet {
 	
 	// Planet data
 	private String name;
 	private Point coordinates;
 	private TechLevel level;
 	private Situation situation;
 	private Marketplace marketplace;
 	
 	private static final int radius = 5;
 	
 	
 	/**
 	 * Constructor for loading Planet
 	 */
 	public Planet(String name, Point coordinates, TechLevel level, Situation situation, Marketplace marketplace) {
 		this.name = name;
 		this.coordinates = coordinates;
 		this.level = level;
 		this.situation = situation;
 		this.marketplace = marketplace;
 	}
 	
 	/**
 	 * Constructor for a new Planet
 	 */
 	
 	public Planet(String name, Point location, TechLevel level, Situation situation) {
 		this(name, location, level, situation, new Marketplace(0, level, situation));
 	}
 	
 	/**
 	 * Distance between planets
 	 */
 	public int distance(Planet planet) {
 		return distance(planet.getCoordinates());
 	}
 	
 	/**
 	 * Distance between the planet and another point (like a click)
 	 */
 	public int distance(Point other) {
 		int dx, dy;
 		dx = coordinates.x - other.x;
 		dy = coordinates.y - other.y;
 		return (int)Math.sqrt(dx*dx + dy*dy);
 	}
 	
 	public void dock(int turn) {
 		marketplace.dock(turn);
 	}
 	
 	
 	public void draw(Graphics g) {
 		g.fillOval(coordinates.x - radius, coordinates.y - radius, 2*radius, 2*radius);
 	}
 	
	public void drawMain(Graphics g, int fuel, int distance) {
 		g.fillOval(coordinates.x - radius, coordinates.y - radius, 2*radius, 2*radius);
		int temp = fuel < distance ? fuel: distance;
		g.drawOval(coordinates.x - temp, coordinates.y - temp, 2*temp, 2*temp);
 	}
 	
 	public Rectangle getRectangle() {
 		return new Rectangle(coordinates.x - radius, coordinates.y - radius, 2*radius, 2*radius);
 	}
 	
 	public boolean isClicked(Point point) {
 		return new Rectangle(coordinates.x - radius, coordinates.y - radius, 2*radius, 2*radius).contains(point);
 	}
 	
 	/**
 	 * Getters and setters
 	 */	
 	public Point getCoordinates() {
 		return coordinates;
 	}
 	
 	public void setCoordinates(Point location) {
 		this.coordinates = location;
 	}
 	public String getName() {
 		return name;
 	}
 	
 	public Situation getSituation() {
 		return situation;
 	}
 
 	public void setSituation(Situation situation) {
 		this.situation = situation;
 	}
 	
 	public Marketplace getMarketplace() {
 		return marketplace;
 	}
 
 	// For testing purpose
 	public String toString() {
 		return "Planet " + name + " TL " + level + " Sit " + situation + " at X = " + coordinates.x + " Y = " + coordinates.y
 				+ "\n";
 	}
 }
 
 
 

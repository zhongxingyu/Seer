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
 	private Point location;
 	private TechLevel level;
 	private Situation situation;
 	private Marketplace marketplace;
 	
 	private static final int radius = 5;
 	
 	
 	/**
 	 * Constructor for loading Planet
 	 */
 	public Planet(String name, Point location, TechLevel level, Situation situation, Marketplace marketplace) {
 		this.name = name;
 		this.location = location;
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
 		return distance(planet.getLocation());
 	}
 	
 	/**
 	 * Distance between the planet and another point (like a click)
 	 */
 	public int distance(Point other) {
 		int dx, dy;
 		dx = location.x - other.x;
 		dy = location.y - other.y;
 		return (int)Math.sqrt(dx*dx + dy*dy);
 	}
 	
 	public void dock(int turn) {
 		marketplace.dock(turn);
 	}
 	
 	
 	public void draw(Graphics g) {
 		g.fillOval(location.x - radius, location.y - radius, 2*radius, 2*radius);
 	}
 	
 	public void drawMain(Graphics g, int distance) {
 		g.fillOval(location.x - radius, location.y - radius, 2*radius, 2*radius);
 		g.drawOval(location.x - distance, location.y - distance, 2*distance, 2*distance);
 	}
 	
 	public Rectangle getRectangle() {
		return new Rectangle(location.x - radius, location.y - radius, 2*radius, 2*radius);
 	}
 	
 	/**
 	 * Getters and setters
 	 */	
 	public Point getLocation() {
 		return location;
 	}
 	
 	public void setLocation(Point location) {
 		this.location = location;
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
 		return "Planet " + name + " TL " + level + " Sit " + situation + " at X = " + location.x + " Y = " + location.y
 				+ "\n";
 	}
 }
 
 
 

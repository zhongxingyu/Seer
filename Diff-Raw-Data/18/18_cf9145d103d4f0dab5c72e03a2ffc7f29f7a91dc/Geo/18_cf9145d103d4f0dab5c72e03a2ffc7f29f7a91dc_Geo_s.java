 package com.decaflabs.trux;
 
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 
 import com.decaflabs.trux.platform.Platform;
 
 /**
  * Represents the planet surface with all objects
  */
 public class Geo {
 	
 	private static final int LINE_WIDTH = 80;
 	
 	private double length;
 	
	private Set<AbstractPlatform> platforms = new HashSet<AbstractPlatform>();
 	
 	public Geo(double length) {
 		this.length = length;
 	}
 	
 	protected double randomTag() {
 		return Math.random() * this.length;
 	}
 	
	protected void spawn(AbstractPlatform platform) {
 		this.platforms.add(platform);
 	}
 	
	protected void kill(AbstractPlatform platform) {
 		this.platforms.remove(platform);
 	}
 	
 	/**
 	 * select all platforms within range x1..x2
 	 */
	protected List<AbstractPlatform> select(double x1, double x2) {
		List<AbstractPlatform> res = new LinkedList<AbstractPlatform>();
		for(AbstractPlatform p: this.platforms) {
 			if (p.getX() >= x1 && p.getX() < x2) res.add(p);
 		}
 		return res;
 	}
 	
 	/**
 	 * up
 	 * @param delta
 	 */
 	public void mutate(double delta) {
 		for (Platform p: this.platforms) {
 			p.mutate(delta);
 		}
 		// planet borders correction
 		for (Platform p: this.platforms) {
 			if (p.getX() > this.length) p.setX(0);
 			else if (p.getX() < 0) p.setX(this.length);
 		}
 	}
 	
 	public String erase() {
 		StringBuilder buf = new StringBuilder("");
 		for (int i = 0; i < this.LINE_WIDTH; i++) buf.append('\b');
 		return buf.toString();
 	}
 	
 	public String toString() {
 		StringBuilder buf = new StringBuilder("");
 		double step = this.length / this.LINE_WIDTH;
 		double x = 0;
 		while (x < this.length) {
			List<AbstractPlatform> platformList = this.select(x, x + step);
 			if (platformList.size() > 0) {
 				// represent as a team number of the first element
 				int team = platformList.get(0).getTeam();
 				if (team == 0) buf.append('X');
 				else buf.append("" + team);
 			} else {
 				buf.append('_');
 			}
 			x += step;
 		}
 		return buf.toString();
 	}
 }

 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.util.Collection;
 import java.util.HashMap;
import java.util.LinkedList;
 import java.util.Map;
 
 /**
  * A class that represents the Sky. It can hold Constellations and Stars
  * 
  * @see Constellation
  * @see Star
  * @author Jay
  * @version 1.0 (1-20-14)
  */
 public class Sky implements JayACMCanvas.Paintable {
 	private Collection<Constellation> constellations;
 	private Map<String, Integer> starNames;
 	private Map<Integer, Star> stars;
 
 	/**
 	 * Creates a Sky
 	 */
 	public Sky() {
		constellations = new LinkedList<Constellation>();
 		stars = new HashMap<Integer, Star>();
 		starNames = new HashMap<String, Integer>();
 	}
 
 	/**
 	 * Adds a Constellation to the sky
 	 * 
 	 * @param constellation
 	 *            - The Constellation to add
 	 */
 	public void addConstellation(Constellation constellation) {
		synchronized (constellations) {
			constellations.add(constellation);
		}
 	}
 
 	/**
 	 * Adds a Star to the sky
 	 * 
 	 * @param star
 	 *            - The Star to add
 	 * @param draperNumber
 	 *            - The Star's Draper Number
 	 * @param names
 	 *            - The Star's names, if any
 	 */
 	public void addStar(Star star, int draperNumber, String... names) {
 		synchronized (stars) {
 			stars.put(draperNumber, star);
 		}
 		synchronized (starNames) {
 			for (String name : names)
 				starNames.put(name, draperNumber);
 		}
 	}
 
 	/**
 	 * Returns the Star with the given name, if there is one
 	 * 
 	 * @param name
 	 *            - the name of the Star
 	 * @return - the Star or null, if no such star exists
 	 */
 	public Star findStar(String name) {
 		return stars.get(starNames.get(name));
 	}
 
 	/**
 	 * Draws the stars and constellations onto the graphics passed
 	 * 
 	 * @param g
 	 *            - the graphics to draw on
 	 */
 	@Override
 	public void paint(Graphics2D g) {
 		g.setStroke(new BasicStroke(6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
 		synchronized (constellations) {
 			for (Constellation constellation : constellations)
 				constellation.paint(g);
 		}
 		synchronized (stars) {
 			g.setColor(Color.WHITE);
 			for (Star star : stars.values())
 				star.paint(g);
 		}
 		synchronized (constellations) {
 			for (Constellation constellation : constellations)
 				constellation.paint(g);
 		}
 	}
 }

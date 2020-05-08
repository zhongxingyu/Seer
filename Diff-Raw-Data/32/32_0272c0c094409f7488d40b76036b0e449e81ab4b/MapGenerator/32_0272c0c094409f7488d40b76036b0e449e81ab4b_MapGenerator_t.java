 package org.group20.sunstruck.world.map;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import org.group20.sunstruck.world.map.segments.*;
 import org.group20.sunstruck.world.map.segments.Theme.MapTypes;
 
 public class MapGenerator {
 	private Theme currentTheme;
 	private Theme nextTheme;
 	private HashMap<MapTypes, Theme> themes = new HashMap<MapTypes, Theme>();
 	private ArrayList<Theme> availableThemes = new ArrayList<Theme>();
 	public double changeThreshold = 0.2; // the percentile chance of change
 
 	public MapGenerator() {
 		initMapSegments();
 	}
 
 	private void initMapSegments() {
 		Grass g = new Grass();
 		Water w = new Water();
 		Rock r = new Rock();
 		Lava l = new Lava();
 		Desert d = new Desert();
 		themes.put(g.getType(), g);
 		themes.put(w.getType(), w);
 		themes.put(r.getType(), r);
 		themes.put(l.getType(), l);
 		themes.put(d.getType(), d);
 		currentTheme = nextTheme = g;
 		populateAvailableThemes();
 	}
 
 	private void populateAvailableThemes() {
 		availableThemes.clear();
 		java.util.Iterator<MapTypes> i = currentTheme.getTransitions().keySet()
 				.iterator();
 		MapTypes m;
 		while (i.hasNext()) {
 			m = i.next();
 			for (Theme t : themes.values()) {
 				if (t.getType().equals(m))
 					availableThemes.add(t);
 			}
 		}
 	}
 
 	private void changeNextTheme(Theme t) {
 		nextTheme = t;
 	}
 
 	@SuppressWarnings({ "rawtypes", "unused" })
 	private int randomIndexIn(Collection c) {
 		return (int) (Math.random() * (c.size() - 1) + 0.5);
 	}
 
 	private int randomIndexIn(Object[] o) {
 		return (int) (Math.random() * (o.length - 1) + 0.5);
 	}
 
 	/**
 	 * returns the next segment
 	 * 
 	 * @return MapSegment - the next segment
 	 */
 	public TextureRegion getNext() {
 		if (Math.random() < changeThreshold) {
 			System.out.println("--- TRANSITION BEGINS ---");
			System.out.println("currenTheme="+currentTheme+", nextTheme="+nextTheme);
//			TextureRegion next = null;
//			do {
//				MapTypes t = MapTypes.values()[randomIndexIn(MapTypes.values())];
//				nextTheme = themes.get(t);
//				System.out.println("t=" + currentTheme + ", " + t.toString());
//				System.out.println("nextTheme=" + nextTheme);
//				System.out.println("currentTheme=" + currentTheme);
//				next = currentTheme.transitionTo(nextTheme.getType());
//			} while (next == null);
			nextTheme = availableThemes.get(randomIndexIn(availableThemes));
			TextureRegion next = currentTheme.transitionTo(nextTheme.getType());
 			currentTheme = nextTheme;
			populateAvailableThemes();
			System.out.println("currenTheme="+currentTheme+", nextTheme="+nextTheme);
 			System.out.println("--- TRANSITION ENDS ---");
 			return next;
 		}
 		return currentTheme.next();
 		// if (!currentSegment.isTransitional() && Math.random() > 1.0) {
 		// // run with the current theme.
 		// nextSegment = findRandomSegment(nextSegment.getType());
 		// } else {
 		// currentTheme = nextTheme;
 		// // we're going to do a new theme!
 		// if (currentSegment.isTransitional()) {
 		// System.out.println("end transition");
 		// // we're already in a transition, we're going to end one!
 		// nextSegment = findRandomSegment(endTransition());
 		// } else {
 		// System.out.println("start transition");
 		// // it isn't transitional, this means we're starting a transition
 		// nextSegment = findRandomSegment(startTransition());
 		// }
 		//
 		// }
 		// if (nextSegment == null) { // oh nose!
 		// nextSegment = themes.get(0);
 		// System.out.println("MapGenerator.java: "
 		// +"GREVIOUS ERROR HAS OCCURED! COULD NOT FIND A SUITABLE MAP :|");
 		// }
 		// return nextSegment.getTextureRegion();
 	}
 
 	// @SuppressWarnings("rawtypes")
 	// private int randomIndexIn(Collection c) {
 	// return (int) (Math.random() * (c.size() - 1) + 0.5);
 	// }
 	//
 	// private MapSegment findRandomSegment(MapTypes desiredType) {
 	// ArrayList<MapSegment> segs = findSegments(desiredType);
 	// if (segs.size() != 0) {
 	// return segs.get(randomIndexIn(segs));
 	// } else {
 	// return themes.get(0);
 	// }
 	// }
 	//
 	// private MapTypes startTransition() {
 	// ArrayList<MapTypes> newTypes = new ArrayList<MapTypes>();
 	// MapTypes[] types = MapTypes.values();
 	// String[] str;
 	// // System.out.println("startTransition(): finding next theme");
 	// // do {
 	// // nextTheme = segments.get(randomIndexIn(segments)).getType();
 	// // System.out.println(nextTheme);
 	// // } while (!nextTheme.name().contains("_"));
 	// // System.out.println("startTransition(): found="+nextTheme);
 	// //add transitional types to newTypes
 	// for (MapTypes t : types) {
 	// str = t.name().split("_");
 	// System.out.println("startTransition(): t="+t.name());
 	// System.out.println("startTransition(): str="+str.length);
 	// if (str.length == 2) {
 	// System.out.println(str[0] + " " + str[1] + " " + currentTheme.name());
 	// if (str.length == 2 && MapTypes.valueOf(str[0]).equals(currentTheme)) {
 	// System.out.println("startTransition(): found a valid transition!");
 	// newTypes.add(t);
 	// }
 	// }
 	// }
 	// //ensure that we always return a valid type
 	// if (newTypes.size() == 0) {
 	// System.out.println("MapGenerator.java: "
 	// +"GREVIOUS ERROR HAS OCCURED! COULD NOT FIND A SUITABLE TRANSITION! :|");
 	// return MapTypes.DEFAULT;
 	// }
 	// //return a random type
 	// MapTypes t = newTypes.get(randomIndexIn(newTypes));
 	// System.out.println(t);
 	// return t;
 	// }
 	//
 	// private MapTypes endTransition() {
 	// ArrayList<MapTypes> transitions = new ArrayList<MapTypes>();
 	// ArrayList<MapTypes> newTypes = new ArrayList<MapTypes>();
 	// MapTypes[] types = MapTypes.values();
 	// String[] str;
 	// //add transitional types to newTypes
 	// for (MapTypes t : types) {
 	// str = t.name().split("_");
 	// if (str.length == 2 &&
 	// MapTypes.valueOf(str[0]).equals(currentTheme.name())) {
 	// transitions.add(t);
 	// }
 	// }
 	// for (MapTypes t : transitions) {
 	// str = t.name().split("_");
 	// if (str.length == 2) {
 	// newTypes.add(MapTypes.valueOf(str[1]));
 	// }
 	// }
 	// //ensure that we always return a valid type
 	// if (newTypes.size() == 0) {
 	// System.out.println("MapGenerator.java: "
 	// +"GREVIOUS ERROR HAS OCCURED! COULD NOT FIND A SUITABLE TRANSITION! :|");
 	// return MapTypes.DEFAULT;
 	// }
 	// //return a random type
 	// return newTypes.get(randomIndexIn(newTypes));
 	// }
 	//
 	// private ArrayList<MapSegment> findSegments(MapTypes desiredType) {
 	// ArrayList<MapSegment> segs = new ArrayList<MapSegment>();
 	// for (MapSegment s : themes) {
 	// if (s.getType() == desiredType) segs.add(s);
 	// }
 	// return segs;
 	// }
 }

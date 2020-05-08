 package no.uib.bjo013.mspacman.map;
 
 import java.awt.Point;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.LinkedHashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import jef.video.BitMap;
 
 public class GameMap {
 	private BitMap bitmap;
 	private Map<Point, List<Point>> MAP = new LinkedHashMap<Point, List<Point>>();
 	
 	private Map<Integer, Point> pills = new LinkedHashMap<Integer, Point>();
 	private Map<Point, Integer> revpills = new HashMap<Point,Integer>();
 	
 	private Map<Integer, Point> superPills = new LinkedHashMap<Integer,Point>();
 	private Map<Point, Integer> revsuperPills = new HashMap<Point, Integer>();
 	
 	private Point mspacman = new Point();
 	private Point[] ghosts = new Point[4];
 	private List<Point> blues = new ArrayList<Point>();
 
 	public GameMap(BitMap bitmap) {
 		this.bitmap = bitmap;
 		for (int y = 124; y < 155; ++y) {
 			for (int x = 84; x < 139; ++x) {
 				bitmap.setPixel(x, y, 16711680);
 			}
 		}
 
 		for (int y = 108; y < 122; ++y) {
 			for (int x = 65; x < 157; ++x) {
 				bitmap.setPixel(x, y, 0);
 			}
 		}
 		for (int y = 156; y < 170; ++y) {
 			for (int x = 80; x < 145; ++x) {
 				bitmap.setPixel(x, y, 0);
 			}
 		}
 		
 		Integer pilln = 0;
 		Integer superpilln = 0;
 		for (int y = 24; y < 270; ++y) {
 			for (int x = 0; x < 208; ++x) {
 				boolean print = true;
 				for (int k = 0; k < 16 && print; k++) {
 					for (int l = 0; l < 16 && print; l++) {
 						if (!(bitmap.getPixel(l + x, k + y) == 0 
 								|| bitmap.getPixel(l + x, k + y) == 14606046)) {
 							print = false;
 						}
 						if ((k == 0 && l == 0) 
 								|| (k == 15 && l == 0)
 								|| (k == 0 && l == 15) 
 								|| (k == 15 && l == 15)) {
 							print = true;
 						}
 					}
 				}
 				if (print) {
 					Point p = new Point(x, y);
 					MAP.put(p, new LinkedList<Point>());
 					if (containsPill(x, y)) {
 						pills.put(pilln, p);
 						revpills.put(p, pilln);
 						pilln++;
 					}
 					if (containsSuperPill(x, y)) {
 						superPills.put(superpilln,p);
 						revsuperPills.put(p, superpilln);
 						superpilln++;
 					}
 				}
 			}
 		}
 		for (int x = 204; x < 224; x++) {
 			MAP.put(new Point(x, 84), new LinkedList<Point>());
 		}
 		for (int x = 204; x < 224; x++) {
 			MAP.put(new Point(x, 156), new LinkedList<Point>());
 		}
 		for(Point p : MAP.keySet()) {
 			for(Point q : MAP.keySet()) {
 				if (p.distance(q) == 1) {
 					MAP.get(p).add(q);
 				}
 			}
 			if(p.x == 0) {
 				MAP.get(p).add(new Point(223, p.y));
 			} else if (p.x == 223) {
 				MAP.get(p).add(new Point(0, p.y));
 			}
 		}
 		
 	}
 
 	public void update(BitMap bitmap) {
 		this.bitmap = bitmap;
 		ghosts = new Point[4];
 		blues.clear();
 		for (Point p : MAP.keySet()) {
 			if(p == null) {
 				continue;
 			}
 			if (!this.isBlack(p.x, p.y)) {
 				if(this.containsPill(p.x, p.y) && !pills.containsKey(p)) {
 					pills.put(revpills.get(p), p);
 				} else if (this.containsMsPacman(p.x, p.y)) {
 					mspacman = p;
 					pills.remove(revpills.get(p));
 					if(!superPills.isEmpty()) {
 						for(Point s : superPills.values()) {
 							if(mspacman.distance(s) < 6) {
 								superPills.remove(revsuperPills.get(s));
 								break;
 							}
 						}
 					}
 				} else {
 					GameEntity[] ghs = new GameEntity[] { GameEntity.BLINKY,
 							GameEntity.PINKY, GameEntity.INKY, GameEntity.SUE };
 					for (GameEntity ghost : ghs) {
 						if (this.containsGhost(ghost.colour(), p.x, p.y)) {
 							switch (ghost) {
 							case BLINKY:
 								ghosts[0] = p;
 								break;
 							case PINKY:
 								ghosts[1] = p;
 								break;
 							case INKY:
 								ghosts[2] = p;
 								break;
 							case SUE:
 								ghosts[3] = p;
 								break;
 							}
 						}
 					}
 					if (this.containsBlueGhost(p.x, p.y)) {
 						boolean add = true;
 						for (Point b : blues) {
 							if (p.x + 1 == b.x || p.x - 1 == b.x) {
 								add = false;
 								break;
 							}
 						}
 						if (add) {
 							blues.add(p);
 						}
 					}
 				}
 			} else {
 				pills.remove(revpills.get(p));
 			}
 		}
 	}
 
 	public Map<Integer,Point> getPills() {
 		return pills;
 	}
 
 	public Map<Integer,Point> getSuperPills() {
 		return superPills;
 	}
 
 	public List<Point> getGhosts() {
 		List<Point> gs = new ArrayList<Point>();
 		for(Point p : ghosts) {
 			gs.add(p);
 		}
 		return gs;
 	}
 	
 	public List<Point> getBlueGhosts() {
 		return blues;
 	}
 	
 	public Point getMsPacman() {
 		return mspacman;
 	}
 
 	public Point getBlinky() {
 		return ghosts[0];
 	}
 
 	public Point getPinky() {
 		return ghosts[1];
 	}
 
 	public Point getInky() {
 		return ghosts[2];
 	}
 
 	public Point getSue() {
 		return ghosts[3];
 	}
 	
 	public Map<Point, List<Point>> getMap() {
 		return MAP;
 	}
 
 	public List<Point> calculatePath(Point to, Map<Point, Double> weights) {
 		return AStar.computePath(MAP, getMsPacman(), to, weights);
 	}
 	
 	public Point findTarget(Map<Point, Double> weights) {
 		Set<Point> adj = new LinkedHashSet<Point>(pills.values());
 		adj.addAll(superPills.values());
 		adj.addAll(blues);
		Iterator<Point> iter = adj.iterator();
		Point smallest = iter.next();
		Double sscore = weights.containsKey(smallest) ? weights.get(smallest) : 0;
		while(iter.hasNext()) {
			Point n = iter.next();
			Double nscore = weights.containsKey(n) ? weights.get(n) : 0;
			if(sscore > nscore){
				smallest = n;
 			}
 		}
		return smallest;
 	}
 	
 	private boolean isBlack(int x, int y) {
 		return bitmap.getPixel(x + 6, y + 6) == 0
 				&& bitmap.getPixel(x + 9, y + 6) == 0
 				&& bitmap.getPixel(x + 6, y + 9) == 0
 				&& bitmap.getPixel(x + 9, y + 9) == 0
 				&& bitmap.getPixel(x + 7, y + 7) == 0
 				&& bitmap.getPixel(x + 7, y + 8) == 0
 				&& bitmap.getPixel(x + 8, y + 7) == 0
 				&& bitmap.getPixel(x + 8, y + 8) == 0;
 	}
 	
 	public boolean containsBlueGhost(int x, int y) {
 		return (containsBlueGhost1(x, y) || containsBlueGhost1(x+1, y));
 	}
 	/*
 	 * 2171358 BLUE 16758935 EYE
 	 * 16711680 WHITE 16711680 EYE
 	 */
 	private boolean containsBlueGhost1(int x, int y) {
 		return (bitmap.getPixel(x + 5, y + 6) == 16758935
 			 && bitmap.getPixel(x + 6, y + 7) == 16758935
 			 && bitmap.getPixel(x + 9, y + 6) == 16758935
 			 && bitmap.getPixel(x + 10, y + 7) == 16758935
 			 && bitmap.getPixel(x + 6, y + 6) == 16758935
 		     && bitmap.getPixel(x + 10, y + 6) == 16758935
 			 && bitmap.getPixel(x + 4, y + 5) == 2171358
 			 && bitmap.getPixel(x + 7, y + 8) == 2171358
 			 && bitmap.getPixel(x + 8, y + 5) == 2171358 
 			 && bitmap.getPixel(x + 11, y + 8) == 2171358) 
 		  || 
 		       (bitmap.getPixel(x + 5, y + 6) == 16711680
 			 && bitmap.getPixel(x + 6, y + 7) == 16711680
 			 && bitmap.getPixel(x + 9, y + 6) == 16711680
 			 && bitmap.getPixel(x + 10, y + 7) == 16711680
 			 && bitmap.getPixel(x + 6, y + 6) == 16711680
 			 && bitmap.getPixel(x + 10, y + 6) == 16711680
 			 && bitmap.getPixel(x + 4, y + 5) == 14606046
 			 && bitmap.getPixel(x + 7, y + 8) == 14606046
 			 && bitmap.getPixel(x + 8, y + 5) == 14606046 
 			 && bitmap.getPixel(x + 11, y + 8) == 14606046);
 	}
 
 	public boolean containsGhost(int ghost, int x, int y) {
 		return (bitmap.getPixel(x + 5, y + 1) != ghost
 				&& bitmap.getPixel(x + 6, y + 1) == ghost
 				&& bitmap.getPixel(x + 9, y + 1) == ghost
 				&& bitmap.getPixel(x + 10, y + 1) != ghost
 				&& bitmap.getPixel(x + 1, y + 6) != ghost
 				&& bitmap.getPixel(x + 1, y + 7) == ghost
 				&& bitmap.getPixel(x + 14, y + 6) != ghost 
 				&& bitmap.getPixel(x + 14, y + 7) == ghost);
 	}
 
 	public boolean containsMsPacman(int x, int y) {
 		return containsMsPacman1(x, y) || containsMsPacman1(x-1, y);
 	}
 
 	private boolean containsMsPacman1(int x, int y) {
 		return (bitmap.getPixel(x + 10, y + 5) == 16776960
 				&& bitmap.getPixel(x + 11, y + 3) == 2171358
 				&& bitmap.getPixel(x + 12, y + 4) == 2171358
 				&& bitmap.getPixel(x + 11, y + 4) == 16711680 
 				&& bitmap.getPixel(x + 12, y + 3) == 16711680)
 				|| (bitmap.getPixel(x + 5, y + 5) == 16776960
 						&& bitmap.getPixel(x + 4, y + 3) == 2171358
 						&& bitmap.getPixel(x + 3, y + 4) == 2171358
 						&& bitmap.getPixel(x + 4, y + 4) == 16711680 
 						&& bitmap.getPixel(x + 3, y + 3) == 16711680)
 				|| (bitmap.getPixel(x + 5, y + 10) == 16776960
 						&& bitmap.getPixel(x + 4, y + 12) == 2171358
 						&& bitmap.getPixel(x + 3, y + 11) == 2171358
 						&& bitmap.getPixel(x + 4, y + 11) == 16711680 
 						&& bitmap.getPixel(x + 3, y + 12) == 16711680);
 	}
 
 	private boolean containsPill(int x, int y) {
 		return bitmap.getPixel(x + 6, y + 6) == 0
 				&& bitmap.getPixel(x + 9, y + 6) == 0
 				&& bitmap.getPixel(x + 6, y + 9) == 0
 				&& bitmap.getPixel(x + 9, y + 9) == 0
 				&& bitmap.getPixel(x + 7, y + 7) == 14606046
 				&& bitmap.getPixel(x + 7, y + 8) == 14606046
 				&& bitmap.getPixel(x + 8, y + 7) == 14606046
 				&& bitmap.getPixel(x + 8, y + 8) == 14606046;
 	}
 
 	private boolean containsSuperPill(int x, int y) {
 		return bitmap.getPixel(x + 5, y + 5) == 14606046
 				&& bitmap.getPixel(x + 10, y + 5) == 14606046
 				&& bitmap.getPixel(x + 5, y + 10) == 14606046
 				&& bitmap.getPixel(x + 10, y + 10) == 14606046;
 	}
 }

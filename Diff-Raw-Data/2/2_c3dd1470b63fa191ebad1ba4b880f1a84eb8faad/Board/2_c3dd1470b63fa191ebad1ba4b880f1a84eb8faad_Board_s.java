 package org.etotheipi.narwhal.domain;
 
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.List;
 import java.util.PriorityQueue;
 import java.util.Queue;
 
 import org.etotheipi.narwhal.Constants;
 import org.etotheipi.narwhal.domain.tower.LoveTower;
 
 public class Board {
 	private Tower[][] spaces = new Tower[15][10];
 	private List<Creep> creepsOnBoard;
 	private Queue<Creep> creepsPending;
 	private Direction[][] policy;
 	private Player thePlayer;
 
 	public Board(Queue<Creep> pending) {
 		this.creepsPending = pending;
 		this.creepsOnBoard = new ArrayList<Creep>();
 		this.policy = shortestPaths();
 		thePlayer = new Player();
 		thePlayer.setMoney(50);
 		thePlayer.setHealth(5);
 	}
 
 	public Direction[][] getPolicy() {
 		return this.policy;
 	}
 
 	public void update() {
 		// Move the creeps
 		ArrayList<Creep> explodingCreeps = new ArrayList<Creep>();
 		for (Creep creep : creepsOnBoard) {
 			try{
 			creep.move(this);
 			}
 			catch(NullPointerException ex)
 			{
 					explodingCreeps.add(creep);
 			}
 		}
 
 		for(Creep creep : explodingCreeps){
 			thePlayer.hurt();
 			creepsOnBoard.remove(creep);
 		}
 
 		// Fire weapons
 		for (Tower[] lst : spaces) {
 			for (Tower tower : lst) {
 				if (tower != null) {
 					tower.attack(this);
 					ArrayList<Bullet> deadBullets = new ArrayList<Bullet>();
 					for (Bullet b: tower.bullets) {
 						if(b.updateBullet()){
 							Creep target = b.getTarget();
 							target.dealDamage(tower.getPower());
 							deadBullets.add(b);
 							if(target.getCurrentHealth() == 0){
 								thePlayer.addMoney(target.getValue());
 								creepsOnBoard.remove(target);
 							}
 						}
 					}
 					//Clear out dead bullets.
 					for(Bullet b:deadBullets)
 					{
 						tower.bullets.remove(b);
 					}
 				}
 			}
 		}
 
 		// Spawn new creep
 		if (this.getCreepsNear(new Point(0,0),1).isEmpty()) {
 			if (!creepsPending.isEmpty()) {
 				Creep newCreep = creepsPending.remove();
 				newCreep.setLocation(this.getCenterOf(new Point(0,0)));
 				creepsOnBoard.add(newCreep);
 			}
 		}
 	}
 
 	public boolean canPlaceTower(Point location) {
 		if (location.equals(new Point(0,0)) || location.equals(new Point(14,9))) {
 			return false; // can't place tower at creep start or end position
 		}
 		if (spaces[location.x][location.y] != null) {
 			return false; // can't place over an existing tower
 		}
 
 		// Check paths
 		Tower t = new LoveTower();
 		spaces[location.x][location.y] = t;
 		boolean canPlace = (shortestPaths()[0][0] != null);
 		spaces[location.x][location.y] = null;
 
 		return canPlace;
 	}
 
 	public void placeTower(Tower t, Point location) throws Exception {
 		if (location.equals(new Point(0,0)) || location.equals(new Point(14,9))) {
 			throw new Exception("Cannot place tower in upper left or lower right corners.");
 		}
 		if (spaces[location.x][location.y] != null) {
 			throw new Exception("Cannot place tower on an occupied space.");
 		}
 		if (!thePlayer.spendMoney(t.getCost())) {
 			throw new Exception("Outta Cash");
 		}
 		if (!this.getCreepsAt(location).isEmpty()) {
			throw new Exception("Cannot build on top of creeps.")
 		}
 
 		// Check paths
 		spaces[location.x][location.y] = t;
 		Direction[][] policy = shortestPaths();
 
 		if (policy[0][0] == null) {
 			// blocking; can't place tower there
 			spaces[location.x][location.y] = null;
 			throw new Exception("Cannot block all paths to the exit.");
 		} else {
 			// works; set the new policy and the tower location
 			this.policy = policy;
 			t.setLocation(this.getCenterOf(location));
 		}
 	}
 
 	public Tower getTowerAt(Point location) {
 		if ((location.x >= spaces.length) || (location.y >= spaces[0].length)) {
 			throw new IllegalArgumentException("specified point " + location + " out of range.");
 		}
 		return spaces[location.x][location.y];
 	}
 
 	public void destroyTowerAt(Point location) {
 		this.spaces[location.x][location.y] = null;
 		this.policy = this.shortestPaths();
 	}
 
 	public List<Creep> getCreepsAt(Point location) {
 		ArrayList<Creep> creeps = new ArrayList<Creep>();
 		Rectangle bounds = this.getBoundsOf(location);
 		for (Creep creep : creepsOnBoard) {
 			if (bounds.contains(creep.getLocation())) {
 				creeps.add(creep);
 			}
 		}
 		return new ArrayList<Creep>();
 	}
 
 	public void printPolicy() {
 		for (int j = 0; j < this.policy[0].length; ++j) {
 			for (int i = 0; i < this.policy.length; ++i) {
 				if (this.policy[i][j] == null) {
 					System.out.print("x");
 				} else {
 					switch (this.policy[i][j]) {
 						case NORTH:
 							System.out.print("^"); break;
 						case SOUTH:
 							System.out.print("v"); break;
 						case EAST:
 							System.out.print(">"); break;
 						case WEST:
 							System.out.print("<"); break;
 					}
 				}
 			}
 			System.out.println();
 		}
 	}
 
 	public Point getSquareFor(Point location) {
 		return new Point(
 				(location.x - (location.x % Constants.SQUARE_SIZE))/Constants.SQUARE_SIZE,
 				(location.y - (location.y % Constants.SQUARE_SIZE))/Constants.SQUARE_SIZE);
 	}
 
 	public List<Creep> getCreepsNear(Point location, int radius) {
 		radius *= Constants.SQUARE_SIZE;
 		int rr = radius * radius;
 		Point center = this.getCenterOf(location);
 
 		ArrayList<Creep> creeps = new ArrayList<Creep>();
 
 		for (Creep creep : creepsOnBoard) {
 			int dx = center.x - creep.getLocation().x,
 			    dy = center.y - creep.getLocation().y;
 			if (dx*dx + dy*dy < rr) {
 				creeps.add(creep);
 			}
 		}
 
 		return creeps;
 	}
 
 	public Rectangle getBoundsOf(Point location) {
 		return new Rectangle(
 				location.x * Constants.SQUARE_SIZE,
 				location.y * Constants.SQUARE_SIZE,
 				Constants.SQUARE_SIZE,
 				Constants.SQUARE_SIZE);
 	}
 
 	public Point getCenterOf(Point location) {
 		return new Point(
 				location.x * Constants.SQUARE_SIZE + (Constants.SQUARE_SIZE / 2),
 				location.y * Constants.SQUARE_SIZE + (Constants.SQUARE_SIZE / 2));
 	}
 
 	protected Direction[][] shortestPaths() {
 		Direction[][] policy  = new Direction[15][10];
 		boolean[][] graph     = new boolean[15][10];
 		boolean[][] visited   = new boolean[15][10];
 		final int[][] lengths = new int[15][10];
 
 		for (int i = 0; i < this.spaces.length; ++i) {
 			for (int j = 0; j < this.spaces[i].length; ++j) {
 				policy[i][j]  = null;
 				graph[i][j]   = (this.spaces[i][j] == null);
 				visited[i][j] = false;
 				lengths[i][j] = 4200; // +infinity
 			}
 		}
 
 		PriorityQueue<Point> djkHeap = new PriorityQueue<Point>(75, new Comparator<Point>() {
 			@Override
 			public int compare(Point a, Point b) {
 				return lengths[a.x][a.y] - lengths[b.x][b.y];
 			}
 		});
 
 		lengths[14][9] = 0;
 
 		for (int i = 0; i < this.spaces.length; ++i) {
 			for (int j = 0; j < this.spaces[i].length; ++j) {
 				if (graph[i][j]) {
 					djkHeap.add(new Point(i,j));
 				}
 			}
 		}
 
 		while (!djkHeap.isEmpty()) {
 			Point current = djkHeap.remove();
 			if (visited[current.x][current.y] || !graph[current.x][current.y]) {
 				continue;
 			}
 
 			if (lengths[current.x][current.y] >= 4200) {
 				break;
 			}
 
 			Point[] children = new Point[] { north(current), south(current), east(current), west(current) };
 			for (int i = 0; i < children.length; ++i) {
 				Point child = children[i];
 				if (child != null && !visited[child.x][child.y] && graph[child.x][child.y]) {
 					// unvisited child
 					int dist = lengths[current.x][current.y] + 1;
 					if (dist < lengths[child.x][child.y]) {
 						lengths[child.x][child.y] = dist;
 						if        (i == 0) {
 							// north child
 							policy[child.x][child.y] = Direction.SOUTH;
 						} else if (i == 1) {
 							// south child
 							policy[child.x][child.y] = Direction.NORTH;
 						} else if (i == 2) {
 							// east child
 							policy[child.x][child.y] = Direction.WEST;
 						} else if (i == 3) {
 							// west child
 							policy[child.x][child.y] = Direction.EAST;
 						}
 						djkHeap.remove(child);
 						djkHeap.add(child);
 					}
 				}
 			}
 		}
 
 		return policy;
 	}
 
 	public Queue<Creep> getCreepsPending() {
 		return creepsPending;
 	}
 
 	public void setCreepsPending(Queue<Creep> creepsPending) {
 		this.creepsPending = creepsPending;
 	}
 
 	public List<Creep> getCreepsOnBoard() {
 		return creepsOnBoard;
 	}
 
 	private Point north(Point p) {
 		if (p.y > 0) return new Point(p.x,p.y - 1);
 		else return null;
 	}
 
 	private Point south(Point p) {
 		if (p.y < 9) return new Point(p.x,p.y + 1);
 		else return null;
 	}
 
 	private Point east(Point p) {
 		if (p.x < 14) return new Point(p.x + 1,p.y);
 		else return null;
 	}
 
 	private Point west(Point p) {
 		if (p.x > 0) return new Point(p.x - 1,p.y);
 		else return null;
 	}
 }

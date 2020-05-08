 /*
  * Scout.java
  * Copyright (C) 2011 Meyer Kizner
  * All rights reserved.
  */
 
 package com.prealpha.aichallenge;
 
 import java.util.Comparator;
 import java.util.HashSet;
 import java.util.List;
 import java.util.PriorityQueue;
 import java.util.Set;
 
 import com.prealpha.aichallenge.protocol.Aim;
 import com.prealpha.aichallenge.protocol.GameMap;
 import com.prealpha.aichallenge.protocol.Ilk;
 import com.prealpha.aichallenge.protocol.Order;
 import com.prealpha.aichallenge.protocol.Point;
 
 final class Scout extends PathFinder implements Ant {
 	private static final Set<Point> ACTIVE_TARGETS = new HashSet<Point>();
 
 	private final GameMap map;
 
 	private Point position;
 
 	private List<Point> path;
 
 	private int index;
 
 	private Order order;
 
 	public Scout(GameMap map, Point position) {
 		super(map);
 		this.map = map;
 		this.position = position;
 	}
 
 	@Override
 	public Order getOrder() {
 		if (path == null || index >= path.size()) {
 			recalculate();
 		}
 		if (path == null || index >= path.size()) {
 			return null;
 		}
 
 		Point target = path.get(index++);
 		if (!map.getIlk(target).isPassable()) {
 			recalculate();
 			return getOrder();
 		} else {
 			Set<Aim> directions = map.getDirections(position, target);
 			if (directions.size() != 1) {
 				throw new IllegalStateException("invalid path");
 			}
 			Aim direction = directions.iterator().next();
 			order = new Order(position, direction);
 			return order;
 		}
 	}
 
 	private void recalculate() {
 		if (path != null) {
 			ACTIVE_TARGETS.remove(path.get(path.size() - 1));
 		}
 		index = 0;
 		Point target = getTarget();
 		if (target != null) {
 			path = findPath(position, target);
 			if (path != null) {
 				ACTIVE_TARGETS.add(path.get(path.size() - 1));
 			}
 		}
 	}
 
 	private Point getTarget() {
 		PriorityQueue<Point> targets = new PriorityQueue<Point>(1,
 				new Comparator<Point>() {
 					@Override
 					public int compare(Point p1, Point p2) {
 						int d1 = map.getManhattanDistance(position, p1);
 						int d2 = map.getManhattanDistance(position, p2);
 						if (ACTIVE_TARGETS.contains(p1)) {
 							d1 *= 10 * Math.random();
 						}
 						if (ACTIVE_TARGETS.contains(p2)) {
 							d2 *= 10 * Math.random();
 						}
 						return d1 - d2;
 					}
 				});
 		for (int i = 0; i < map.getRows(); i++) {
 			for (int j = 0; j < map.getCols(); j++) {
 				Point point = new Point(i, j);
 				if (map.getIlk(point).isPassable()) {
 					int unknownCount = 0;
 					for (Point adjacent : map.getAdjacent(point)) {
 						if (map.getIlk(adjacent) == Ilk.UNKNOWN) {
 							unknownCount++;
 						}
 					}
 					if (unknownCount > 0) {
 						targets.add(point);
 					}
 				}
 			}
 		}
 		return targets.peek();
 	}
 
 	@Override
 	public void orderConfirmed() {
 		position = order.getTarget(map);
 		order = null;
 	}
 
 	@Override
 	public void die() {
		ACTIVE_TARGETS.remove(path.get(path.size() - 1));
 	}
 }

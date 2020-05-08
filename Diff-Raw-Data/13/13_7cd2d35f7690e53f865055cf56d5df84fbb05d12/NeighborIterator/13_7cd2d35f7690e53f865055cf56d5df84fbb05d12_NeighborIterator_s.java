 package com.mebigfatguy.gnomeminesplaya;
 
 import java.awt.Point;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 public class NeighborIterator implements Iterator<Point> {
 
 	private final List<Point> neighbors = new ArrayList<Point>();
 	private final Iterator<Point> it;
 
 	public NeighborIterator(Point p, int columns, int rows) {
 		if (p.y > 0) {
 			for (int n = -1; n < 2; n++) {
 				int x = p.x + n;
				if ((x > 0) && (x < columns)) {
 					neighbors.add(new Point(x, p.y - 1));
 				}
 			}
 		}
 
 		for (int n = -1; n < 2; n+=2) {
 			int x = p.x + n;
			if ((x > 0) && (x < columns)) {
 				neighbors.add(new Point(x, p.y));
 			}
 		}
		for (int n = -1; n < 2; n++) {
			if (p.y < rows - 1) {
 				int x = p.x + n;
				if ((x > 0) && (x < columns)) {
 					neighbors.add(new Point(x, p.y + 1));
 				}
 			}
 		}
 
 		it = neighbors.iterator();
 	}
 
 	@Override
 	public boolean hasNext() {
 		return it.hasNext();
 	}
 
 	@Override
 	public Point next() {
 		return it.next();
 	}
 
 	@Override
 	public void remove() {
 		throw new UnsupportedOperationException();
 	}
 }

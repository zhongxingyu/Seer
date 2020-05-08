 package org.weiqi;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import org.weiqi.Weiqi.Array;
 import org.weiqi.Weiqi.Occupation;
 
 public class Board extends Array<Occupation> {
 
 	public Board() {
 		for (Coordinate c : Coordinate.getAll())
 			set(c, Occupation.EMPTY);
 	}
 
 	public void move(Coordinate c, Occupation player) {
 		Occupation current = get(c);
 		if (current == Occupation.EMPTY) {
 			set(c, player);
 			killIfDead(c);
 			for (Coordinate neighbour : c.getNeighbours())
 				killIfDead(neighbour);
 		} else
 			throw new RuntimeException("Cannot move on occupied position");
 	}
 
 	private void killIfDead(Coordinate c) {
 		if (c.isWithinBoard() && !hasBreath(c))
 			for (Coordinate c1 : findGroup(c))
 				set(c1, Occupation.EMPTY);
 	}
 
 	public Set<Coordinate> findGroup(Coordinate c) {
 		Set<Coordinate> group = new HashSet<Coordinate>();
 		findGroup(c, get(c), group);
 		return group;
 	}
 
 	private void findGroup(Coordinate c, Occupation color, Set<Coordinate> group) {
		if (c.isWithinBoard() && get(c) == color && group.add(c))
 			for (Coordinate neighbour : c.getNeighbours())
 				findGroup(neighbour, color, group);
 	}
 
 	private boolean hasBreath(Coordinate c) {
 		return hasBreath(c, get(c));
 	}
 
 	private boolean hasBreath(Coordinate c, Occupation player) {
 		if (c.isWithinBoard()) {
 			Occupation current = get(c);
 
 			if (current == Occupation.EMPTY)
 				return true;
 			else if (current == player) {
 				set(c, null); // Do not re-count
 				boolean hasBreath = false;
 				for (Coordinate neighbour : c.getNeighbours())
 					if (hasBreath(neighbour, player)) {
 						hasBreath = true;
 						break;
 					}
 				set(c, current); // Set it back
 				return hasBreath;
 			} else
 				return false;
 		} else
 			return false;
 	}
 
 }

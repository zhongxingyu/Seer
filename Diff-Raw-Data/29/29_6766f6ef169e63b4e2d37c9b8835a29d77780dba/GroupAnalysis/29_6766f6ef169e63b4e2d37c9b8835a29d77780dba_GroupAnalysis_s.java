 package org.weiqi;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 
 import org.weiqi.Weiqi.Occupation;
 
 public class GroupAnalysis {
 
 	private Board board;
 
 	private Map<Coordinate, Group> groupByCoord = new HashMap<>();
 	private Set<Group> groups = new TreeSet<>();
 
 	public static class Group implements Comparable<Group> {
 		public final Occupation color;
 		public final Collection<Coordinate> coords = new ArrayList<>();
 		public final Collection<Group> touches = new TreeSet<>();
 		public final Collection<Coordinate> breathes = new TreeSet<>();
 		private final int id;
 		private Group parent;
 
		public Group(int id, Occupation color) {
 			this.id = id;
 			this.color = color;
 		}
 
 		private Group root() {
 			Group root = this;
 			while (root.parent != null)
 				root = root.parent;
 			return root;
 		}
 
 		@Override
 		public int compareTo(Group group) {
 			return id - group.id;
 		}
 	}
 
 	public GroupAnalysis(Board board) {
 		this.board = board;
 		assignGroups();
 		assignGroupSurroundings();
 	}
 
 	private void assignGroups() {
 		int nGroups = 0;
 
 		for (Coordinate c : Coordinate.all()) {
 			Occupation color = board.get(c);
 			Group group = null; // Must be root
 
 			for (Coordinate c1 : c.leftOrUp())
 				if (board.get(c1) == color) {
 					Group group1 = groupByCoord.get(c1).root();
 
 					if (group != null) {
 						if (group != group1)
 							group1.parent = group;
 					} else
 						group = group1;
 				}
 
 			if (group == null)
 				group = new Group(nGroups++, color);
 
 			groupByCoord.put(c, group);
 		}
 
 		for (Coordinate c : Coordinate.all()) {
 			Group group = groupByCoord.get(c).root();
 			groupByCoord.put(c, group);
 			group.coords.add(c);
 			groups.add(group);
 		}
 	}
 
 	private void assignGroupSurroundings() {
 		for (Coordinate c : Coordinate.all()) {
 			Group group = groupByCoord.get(c);
 
 			for (Coordinate c1 : c.leftOrUp()) {
 				Group group1 = groupByCoord.get(c1);
 
 				if (group != group1) {
 					group.touches.add(group1);
 					group1.touches.add(group);
 				}
 			}
 
 			if (board.get(c) == Occupation.EMPTY)
 				for (Coordinate c1 : c.neighbors())
 					groupByCoord.get(c1).breathes.add(c);
 		}
 	}
 
 	public Set<Group> getGroups() {
 		return groups;
 	}
 
 	public Group getGroup(Coordinate c) {
 		return groupByCoord.get(c);
 	}
 
 }

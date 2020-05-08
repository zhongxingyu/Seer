 package com.ezgliding.igc;
 
 import java.nio.file.FileSystems;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.SortedMap;
 import java.util.TreeSet;
 import java.util.logging.Logger;
 
 public class BrokenLineOptimizer extends Optimizer {
 
 	private static Logger logger = Logger.getLogger(BrokenLineOptimizer.class.getName());
 
 	public BrokenLineOptimizer(Flight flight, int numPoints) {
 		super(flight, numPoints);
 	}
 
 	@Override
 	public Result optimize() {
 		if (flight == null || flight.fixes() == null) return null;
 
 		Iterator<Candidate> candIter = iterator();
 
 		Candidate curResult = null;
 		Candidate current = null;
 
 		while (candIter.hasNext()) {
 			// We get the max entry and remove it from the tree
 			current = candIter.next();
 
 			// If final and better than current max, update result
 			if (current.isFinal() && (curResult == null || current.max() > curResult.max()))
 				curResult = current;
 		}
 		
 		ArrayList<Fix> points = new ArrayList<Fix>();
 		for (RectangleSet set: curResult.getRectangles())
 			points.add(flight.fixes().get(set.start()));
 		return new Result(points.toArray(new Fix[] {}));
 	}
 
 	protected List<Candidate> branch(Candidate candate) {
 		if (candate == null || candate.getRectangles() == null || candate.getRectangles().size() <= 0) 
 			throw new IllegalArgumentException("Cannot branch empty candidate");
 
 		ArrayList<Candidate> splitCdates = new ArrayList<Candidate>();
 
 		// Get the indexes of the rectangles with the largest diagonal, and split them
 		RectangleSet largerDiagSet = candate.largestDiagonal();
 		RectangleSet[] largerDiagSplits = largerDiagSet.split();
 
 		// Replace the larger rect with the split ones
 		Candidate newCandidate = candate.clone();
 		newCandidate.replace(largerDiagSet, largerDiagSplits);
 		splitCdates.add(newCandidate);
 
 		// Do all possible permutations
 		ArrayList<Candidate> result = new ArrayList<Candidate>();
 		for (Candidate c: splitCdates) {
 			result.addAll(permutations(c.getRectangles()));
 		}
 
 		return result;
 	}
 
 	protected List<Candidate> permutations(List<RectangleSet> availableSets) {
 		ArrayList<Candidate> finalCandidates = new ArrayList<Candidate>();
 
 		permutations(availableSets, numPoints, 0, new Candidate(), finalCandidates);
 		return finalCandidates; 
 	}
 
 	private void permutations(List<RectangleSet> availableSets, int size, int from, 
 		Candidate current, List<Candidate> candidates) {
 		
 		if (current.getRectangles().size() == size) { // Final condition, add and return
 			candidates.add(current);
 			return;
 		}
 
 		for (int i=from; i<availableSets.size(); i++) {
 			Candidate newCurrent = current.clone();
 			newCurrent.add(availableSets.get(i));
 			permutations(availableSets, size, i, newCurrent, candidates);
 		}
 	}
 
 	public Iterator<Candidate> iterator() {
 		return new CandidateIterator();
 	}
 
 	public class CandidateIterator implements Iterator {
 
 		private double min;
 
 		private Candidate current;
 
 		private TreeSet<Candidate> maxTree;
 
 		public CandidateIterator() {
 			maxTree = new TreeSet<Candidate>();
 
 			// We start with a candidate containing only one rectangle (with all points)
 			ArrayList<RectangleSet> initialSet = new ArrayList<RectangleSet>();
			initialSet.add(new RectangleSet(flight.fixes().subList(flightStart(), flightEnd())));
 			maxTree.add(new Candidate(initialSet));
 		}
 
 		public boolean hasNext() { 
 			if (maxTree != null && maxTree.size() != 0)
 				return true; 
 			return false;
 		}
 
 		public Candidate next() { 
 			if (!hasNext()) return null;
 	
 			// Get maximum and remove it from tree (and update cur min if required)
 			current = maxTree.pollLast();
 			if (current.min() > min) min = current.min();
 
 			// Prune the tree
 			prune(current.min());
 
 			// If not final, branch and add to treemap
 			if (!current.isFinal()) {
 				List<Candidate> branchCandidates = branch(current);
 				for (Candidate c: branchCandidates)
 					if (c.max() > min) maxTree.add(c);
 			} 
 
 			return current; 
 		}
 
 		public void remove() { }
 
 		protected void prune(double min) {
 			Iterator<Candidate> iterCands = maxTree.iterator();
 			Candidate c;
 			ArrayList<Candidate> removableCands = new ArrayList<Candidate>();
 			while (iterCands.hasNext()) {
 				c = iterCands.next();	
 				if (c.max() <= min) removableCands.add(c);
 			}
 			for (Candidate z: removableCands) 
 				maxTree.remove(z);
 		}
 	}
 
 	public Flight getFlight() {
 		return flight;
 	}
 
 	public int getNumPoints() {
 		return numPoints;
 	}
 }

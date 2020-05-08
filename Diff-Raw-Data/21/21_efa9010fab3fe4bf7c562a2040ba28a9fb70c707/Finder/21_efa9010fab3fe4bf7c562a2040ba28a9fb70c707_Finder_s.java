 package closestpair;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 public class Finder {
 	private List<Point> xList, yList;
 
 	public Finder(List<Point> list) {
 		xList = new ArrayList<Point>(list);
 		Collections.sort(xList, new XComparator());
 		yList = new ArrayList<Point>(xList);
 		Collections.sort(yList, new YComparator());
 	}
 
 	public double solve() {
 		return solve(0, xList.size() - 1);
 	}
 
 	private double solve(int begin, int end) {
 		if (end - begin < 3) {
 			return baseCase(begin, end);
 		} else {
 			return recursiveCase(begin, end);
 		}
 	}
 
 	private double baseCase(int begin, int end) {
 		if (end - begin == 1) {	// Two points
 			return xList.get(begin).distanceTo(xList.get(end));
 		} else {				// Three points
 			double minimum = xList.get(begin).distanceTo(xList.get(begin + 1));
 			minimum = Math.min(minimum, xList.get(begin + 1).distanceTo(xList.get(end)));
 			minimum = Math.min(minimum, xList.get(begin).distanceTo(xList.get(end)));
 			return minimum;
 		}
 	}
 
 	private double recursiveCase(int begin, int end) {
		int middle = (end - begin) / 2;
		double a = solve(begin, middle);
		double b = solve(middle + 1, end);
 
		double minimum;
		if (a < b) {
			minimum = a;
		} else {
			minimum = b;
		}
 		for (int i = -6; i < 6; i++) {
 			int index = middle + i;
			if (index >= 0 && index < xList.size()) {
				double distance = xList.get(middle).distanceTo(xList.get(index));
 				minimum = Math.min(minimum, distance);
 			}
 		}
 		return minimum;
 	}
 }

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
 	
 	private double solve(int begin, int end){
		if (end - begin < 3) {
 			double minimum = Double.MAX_VALUE;
 			for (int i = begin; i < end; i++) {
 				double distance = xList.get(i).distanceTo(xList.get(i + 1));
				minimum = Math.min(minimum, distance);
 			}
			minimum = Math.min(minimum, xList.get(begin).distanceTo(xList.get(end)));
 			return minimum;
 		} else {
 			int middle = (end - begin) / 2;
 			double a = solve(begin, middle);
 			double b = solve(middle + 1, end); //Kanske ta bort +1?
 			
 			double minimum;
 			if (a < b) {
 				minimum = a;
 			}
 			else {
 				minimum = b;
 			}
 			for(int i = -6;i < 6; i++){
 				int index = middle + i;
 				if (index >= 0 && index < xList.size()) { 
 					double distance = xList.get(middle).distanceTo(xList.get(index));
					minimum = Math.min(minimum, distance);
 				}
 			}
 			return minimum;
 		}
 	}
 }

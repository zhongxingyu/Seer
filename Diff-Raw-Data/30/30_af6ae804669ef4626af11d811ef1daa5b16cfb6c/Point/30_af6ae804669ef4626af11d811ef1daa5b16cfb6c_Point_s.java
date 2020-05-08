 // Attribution-Noncommercial-Share Alike 3.0 Unported
 // (see more at http://creativecommons.org/licenses/by-nc-sa/3.0/)
// (c) Maxim Kirillov <max630@gmail.com>
 
 public class Point implements Comparable<Point> {
 	final public int x;
 	final public int y;
 	
 	public Point (int x, int y) {
 		this.x = x;
 		this.y = y;
 	}
 
 	public int compareTo(Point p) {
 		int res = compare(x, p.x);
 		if (res != 0) {
 			return res;
 		}
 
 		return compare(y, p.y);
 	}
 
 	public boolean equals(Point p) {
 		return x == p.x && y == p.y;
 	}
 
 	private int compare(int v1, int v2) {
 		if (v1 < v2) {
 			return -1;
 		}
 
 		if (v1 > v2) {
 			return 1;
 		}
 
 		return 0;
 	}
 }

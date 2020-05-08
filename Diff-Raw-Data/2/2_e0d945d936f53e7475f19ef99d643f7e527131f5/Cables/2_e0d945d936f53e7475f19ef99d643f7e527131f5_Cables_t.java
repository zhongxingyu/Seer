 /*	Daniel Moore - Cables.java
 	COP 3503 - Program #2
 */
 import java.util.*;
 
 public class Cables {
 
 	public static void main(String args[]){
 
 		//create scanner
 		Scanner inFile = new Scanner(System.in);
 
 		int main_index;
 
 		main_index = inFile.nextInt();
 
 		while(main_index != 0){
 
 			//create ArraysLists
 			ArrayList<Point> all_points = new ArrayList<Point>();
 			ArrayList<Wire> all_wires = new ArrayList<Wire>();
 
 			//we will need all of these
 			int i, j, next_x, next_y;
 			Point next_origin, next_end;
 
 			//colect all points
 			for(i = 0; i < main_index; i ++){
 				next_x = inFile.nextInt();
 				next_y = inFile.nextInt();
 				all_points.add(new Point(next_x, next_y, i));
 			}//end i for
 
 			//create all wires
 			for(i = 0; i < main_index; i ++){
 				next_origin = all_points.get(i);
 
 				for(j = i + 1; j < main_index; j ++){
 					next_end = all_points.get(j);
 					all_wires.add(new Wire(next_origin, next_end));
 				}
 
 			}
 
 			//sort wires
 			Collections.sort(all_wires, new WireLengthComparator());
 
 			//create appropriate DisjointSet
 			DisjointSet parents = new DisjointSet(all_wires.size());
 
 			//Keep track of sum and wire length
 			int linked_wire = 0;
 			double sum = 0;
 
 			//base case only one wire
 			if(all_wires.size() == 1){
 
 				sum = all_wires.get(0).length;
 
 			}else{
 
 				//start connecting points
 				for(i = 0; i < all_wires.size(); i ++){
 
 					//base case, all points are connected
 					if(linked_wire == main_index - 1){
 						break;
 					}
 
 					//check if point are connected
 					if(parents.find(all_wires.get(i).end) != parents.find(all_wires.get(i).origin)){
 						//connect wires and add distance and increment counter
 						parents.union(all_wires.get(i).origin, all_wires.get(i).end);
 						linked_wire ++;
 						sum += all_wires.get(i).length;
 					}
 				}
 			}
 			System.out.printf("%.2f\n", sum);
 
 			main_index = inFile.nextInt();
 
 		}//end main loop
 
 	}//end main method
 
 
 }//end Cables class
 
 class Point {
 	public int x;
 	public int y;
 	public int point_index;
 
 	public Point(int x, int y, int point_index) {
 		this.x = x;
 		this.y = y;
 		this.point_index = point_index;
 	}//end Point constructor
 
 }//end Point class
 
 class Wire {
 	public int origin;
 	public int end;
 	public double length;
 
 	public Wire(Point p1, Point p2){
 		this.origin = p1.point_index;
 		this.end = p2.point_index;
 		this.length = Math.sqrt(Math.pow(p2.x - p1.x, 2) + Math.pow(p2.y - p1.y, 2));
 	}
 }
 
 class WireLengthComparator implements Comparator<Wire> {
 
 	@Override
 	public int compare(Wire wire1, Wire wire2) {
 		return Double.compare(wire1.length, wire2.length);
 	}
 }
 
 class DisjointSet {
 
 	public int[] set;
 
 	public DisjointSet(int size){
 		set = new int [size];
 
 
 		for(int s = 0; s < set.length; s ++){
 			set[s] = s;
 		}
 	}
 
 	public int find(int id) {
 		while(id != set[id]){
 			id = set[id];
 		}
 		return id;
 	}
 
 	public void union(int origin, int end){
		set[find(end)] = find(origin);
 	}
 
 }

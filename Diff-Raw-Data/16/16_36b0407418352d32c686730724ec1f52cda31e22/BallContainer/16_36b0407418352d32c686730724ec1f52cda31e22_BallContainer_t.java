 package homework;
 
 import java.util.LinkedList;
 
 public class BallContainer {
 	protected LinkedList<Ball> list = new LinkedList<Ball>();
 	protected double capacity = 0;
 	
 	public void add(Ball ball) {
 		list.add(ball);
		capacity+=ball.capacity;
 	}
 	
 	public void remove(Ball ball) {
 		list.remove(ball);
		capacity-=ball.capacity;
 	}
 	
 	public double getCapacity() {
 		return capacity;
 	}
 	
 	public int size() {
 		return list.size();
 	}
 	
 	public void clear() {
 		list.clear();
 	}
 	
 	public boolean contains(Ball ball) {
 		return list.contains(ball);
 	}
}

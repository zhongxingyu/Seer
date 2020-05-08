 package homework;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.LinkedList;
 
 public class Box extends BallContainer {
 	private double boxCapacity;
 	
 	public Box(double boxCapacity) {
 		this.boxCapacity = boxCapacity;
 	}
 	
 	public void add(Ball ball) {
 		if(list.size()<boxCapacity) {
 			list.add(ball);
			capacity+=ball.capacity;
 		}
 	}
 	
 	public LinkedList<Ball> getBallsFromSmallest() {
 		LinkedList<Ball> fromSmallest = new LinkedList<Ball>((Collection<? extends Ball>) this);
 		Collections.sort(fromSmallest, new Comparator<Ball>() {
 			@Override
 			public int compare(Ball o1, Ball o2) {
 				return Double.compare(o1.capacity, o2.capacity);
 			}
 		});
 		return fromSmallest;
 	}
 }

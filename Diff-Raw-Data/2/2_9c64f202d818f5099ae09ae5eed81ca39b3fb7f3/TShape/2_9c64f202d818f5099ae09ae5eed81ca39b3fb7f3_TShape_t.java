 package mobile.team4.game;
 
 
 import java.util.ArrayList;
 import java.util.Arrays;
 
 public class TShape extends Shape {
 
 	//ArrayList<Point> points;
 	public ArrayList<ArrayList<Point>> states;
 	int current_state;
 
 	public TShape() {
 		// this should be static but I'm not sure how to do it without using add
 		states = new ArrayList<ArrayList<Point>>();
 		states.add( new ArrayList<Point>( Arrays.asList(new Point(0,0), new Point(1,0), 
 														new Point(2,0), new Point(1,1))));
 		
 		states.add( new ArrayList<Point>( Arrays.asList(new Point(0,0), new Point(0,1), 
 														new Point(0,2), new Point(1,1))));
 		
 		states.add( new ArrayList<Point>( Arrays.asList(new Point(1,0), new Point(1,1), 
 														new Point(1,2), new Point(0,1))));
 
 		states.add( new ArrayList<Point>( Arrays.asList(new Point(0,1), new Point(1,1), 
														new Point(2,1), new Point(1,0))));
 		current_state = 0;
 		points = states.get(current_state);
 	}
 
 	@Override
 	public void rotate() {
 		current_state = (current_state + 1) % 4;
 		points = states.get(current_state);
 	}
 }

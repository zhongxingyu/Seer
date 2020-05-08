 package model;
 
 import java.awt.Point;
 import java.awt.geom.AffineTransform;
 import java.util.ArrayList;
 
 public class Segment extends Object {
 
 	private ArrayList<Point> path = new ArrayList<Point>();
 	private ArrayList<AffineTransform> at = new ArrayList<AffineTransform>(); // arraylist of offsets
 	private int startTime;
 	private int endTime;
 
 	// Override the default constructor, making it private.
 	public Segment(int start, int end) {
 		startTime = start;
 		endTime = end;
 		at.add(new AffineTransform());
 	}
 	
 	public int size(){
 		return path.size();
 	}
 	
 	public Point get(int i){
 		return path.get(i);
 	}
 
 	public boolean contains(int x, int y){
 		for (Point p : path){
 			if (x > p.x - 10 && x < p.x + 10 && y > p.y - 10 && y < p.y + 10){ 
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public void addPoint(Point point){
 		path.add(point);
 	}
 
 	public int getStartTime(){
 		return this.startTime;
 	}
 	
 	public void setEndTime(int endTime){
 		this.endTime = endTime;
 	}
 	public int getEndTime(){
 		return this.endTime;
 	}
 	
 	public boolean isErased(int frame){
 		int isAliveStart = frame - startTime;
 		int isAliveEnd = endTime - frame;
 		if (isAliveStart >= 0 && isAliveEnd >= 0){
 			return false;
 		}
 		return true;
 	}
 	
 	public void createFrame(int frame){
 		if (frame-startTime == 0){ // first one
 			at.set(0, new AffineTransform());
 		}
 		else if (frame > endTime){ // last one
 			at.add(new AffineTransform(at.get(at.size()-1)));
 			endTime++;
 		}
 		else { // middle
 			at.set(frame-startTime, new AffineTransform(at.get(frame-startTime-1)));	
 		}
 	}
 
	public void addSegmentTranslate(int x, int y, int frame){
 		AffineTransform a;
		a = at.get(frame-startTime);
 		a.translate(x, y);
 	}
 	
 	public ArrayList<Point> getTranslates(int frame){ // get all the transforms at a particular frame
 		ArrayList<Point> destination = new ArrayList<Point>();
 		
 		if (frame > endTime || frame < startTime) // dont draw if doesnt exist at that frame.
 			return destination;
 		
 		for (Point p : path){ 
 			Point dest = null;
 			dest = new Point();
 			at.get(frame-startTime).transform(p, dest);
 			destination.add(dest);
 		}
 		return destination;
 	}
 
 }

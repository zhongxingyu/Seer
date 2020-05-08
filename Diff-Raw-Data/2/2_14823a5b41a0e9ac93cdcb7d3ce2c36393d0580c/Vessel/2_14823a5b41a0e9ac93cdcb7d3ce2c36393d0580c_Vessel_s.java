 package common;
 
 import java.util.*;
 
 import vms.Coord;
 import vms.Course;
 
 public class Vessel {
 	private VesselType type;
 	private String id;
 	private Course course;
 	private Coord coords;
 	private Calendar lastTimestamp;
 	
 	public enum VesselType {
 		BOAT
 		// XXX Add all supported types here
 	}
 	
 	public Vessel(String id, VesselType type) {
 		this.id = id;
 		this.type = type;
 	}
 	
 	public String getId() {
 		return id;
 	}
 	
 	public VesselType getType() {
 		return type;
 	}
 	
 	//illegal state exception
 	public Coord getCoord(Calendar timestamp) throws IllegalStateException {
 		long time = timestamp.getTimeInMillis() - lastTimestamp.getTimeInMillis();
 		time = time / 1000; //Convert to seconds
 		
 		if (time > 0){
 			int x = (int)(coords.x() + course.xVel()*time);
 			int y = (int)(coords.y() + course.yVel()*time);
			coords = new Coord(x, y);
 		}
 		
 		else if(time < 0) {
 			throw new IllegalStateException("Trying to read an old timestamp");
 		}
 		return coords;
 	}
 	
 	public Course getCourse(Calendar timestamp) throws IllegalStateException {
 		if (timestamp.before(lastTimestamp))
 			throw new IllegalStateException("Trying to read an old timestamp");
 		return course;
 	}
 	
 	public Calendar getLastTimestamp() {
 		return lastTimestamp;
 	}
 	
 	public void update(Coord newCoords, Course newCourse, Calendar timestamp) throws IllegalStateException {
 		if (timestamp.before(lastTimestamp)) 
 			throw new IllegalStateException("Cannot update time before last timestamp!");
 		if (timestamp.equals(lastTimestamp))
 			throw new IllegalStateException("Cannot re-update last timestamp!");
 		//Very important..! Copy the timestamp in case it gets modified somewhere else
 		course = (Course)newCourse.clone();
 		coords = (Coord)newCoords.clone();
 		lastTimestamp = (Calendar)timestamp.clone();
 	}
 	
 	public void update(UpdateData data) throws IllegalStateException {
 		if(id.equals(data.Id) && type == data.Type){
 			update(data.Coordinates, data.Course, data.Timestamp);
 		}else{
 			throw new IllegalStateException("Not the correct vessel ID and type");
 		}
 	}
 	
 	public UpdateData getUpdateData(Calendar timestamp) {
 		
 		Coord tempCoords = new Coord(0, 0);
 		Course tempCourse = new Course(0,0);
 		
 		try{
 			tempCoords = getCoord(timestamp);
 			tempCourse = getCourse(timestamp);
 		}catch(IllegalStateException e){
 			System.out.println("Invalid timestamp");
 		}
 		
 		return new UpdateData(id, type, tempCoords, tempCourse, timestamp);
 	}
 }

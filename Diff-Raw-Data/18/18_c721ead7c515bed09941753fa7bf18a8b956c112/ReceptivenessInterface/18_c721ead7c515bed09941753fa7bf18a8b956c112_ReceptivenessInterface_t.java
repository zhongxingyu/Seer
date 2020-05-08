 package rtr.repo;
 
 import java.util.List;
 import java.util.Map;
 
 import rtr.domain.Point;
 import rtr.domain.Receptiveness;
 
 public interface ReceptivenessInterface {
 
 	public abstract Receptiveness getReceptiveness(String courseId);
 
 	public abstract void updateReceptiveness(String courseId, String studentId,
 			int change1, int change2);
 	
 	public abstract Map<String, Map<String, List<Point>>> getMap();
 	
 	public abstract void setMap(Map<String, Map<String, List<Point>>> map);

	public abstract void startTrackingSession(String courseId);
 }

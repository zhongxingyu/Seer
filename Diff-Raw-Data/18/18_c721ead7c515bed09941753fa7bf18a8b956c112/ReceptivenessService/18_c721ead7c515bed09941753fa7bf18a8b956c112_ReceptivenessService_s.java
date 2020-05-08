 package rtr.repo;
 
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 
 import rtr.domain.Point;
 import rtr.domain.Receptiveness;
 
 import java.util.Map;
 
 
 import org.springframework.stereotype.Service;
 
 @Service
 public class ReceptivenessService implements ReceptivenessInterface {
 	
 	private Map<String, Map<String, List<Point>>> map = new HashMap<String, Map<String,List<Point>>>();
 	
 	private final static int interval = 5;
 	/* (non-Javadoc)
 	 * @see rtr.repo.ReceptivenessInterface#getReceptiveness(java.lang.String)
 	 */
 	@Override
 	public Receptiveness getReceptiveness(String courseId){
 		Map<String, List<Point>> currentSession = getMap().get(courseId);
 		Date date = new Date();
 		Receptiveness totalReceptiveness = new Receptiveness();
 		for (List<Point> points: currentSession.values()){
 			totalReceptiveness = summarizePoints(totalReceptiveness, points, date);
 		}
 		return totalReceptiveness;
 	}
 	
 	private Receptiveness summarizePoints(Receptiveness toReceptiveness, List<Point> points, Date date){
 		int sum1 = 0;
 		int sum2 = 0;
 		Calendar cal = Calendar.getInstance();
 		cal.add(Calendar.MINUTE, interval);
 		Date compareDate = cal.getTime();
 		int count = 0;
 		for (Point point: points){
 			if (point.getTimestamp().before(compareDate)){
 				break;
 			}
 			count++;
 			sum1 += point.getValue1();
 			sum2 += point.getValue2();
 		}
 		return toReceptiveness.combine(new Receptiveness(sum1/count, sum2/count));
 	}
 		
 	/* (non-Javadoc)
 	 * @see rtr.repo.ReceptivenessInterface#updateReceptiveness(java.lang.String, java.lang.String, int, int)
 	 */
 	@Override
 	public void updateReceptiveness(String courseId, String studentId, int change1, int change2){ 
 		getMap().get(courseId).get(studentId).add(new Point(new Date(), change1, change2));
 	}
 
 	public Map<String, Map<String, List<Point>>> getMap() {
 		return map;
 	}
 
 	public void setMap(Map<String, Map<String, List<Point>>> map) {
 		this.map = map;
 	}
 }

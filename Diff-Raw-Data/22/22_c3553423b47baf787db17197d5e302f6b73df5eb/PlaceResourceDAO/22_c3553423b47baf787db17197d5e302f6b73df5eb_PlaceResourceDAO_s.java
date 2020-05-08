 package dal.concrete.mysql;
 
 import java.sql.SQLException;
 import java.sql.Timestamp;
 import java.util.HashMap;
 import java.util.List;
 
 import pojo.PlaceResource;
 import pojo.Reservation;
 import dal._abstract.mysql.AbstractDAO;
 
 public class PlaceResourceDAO extends AbstractDAO<PlaceResource> {
 	private static PlaceResourceDAO instance;
 
 	public PlaceResourceDAO() {
 		super(PlaceResource.class);
 	}
 
 	public static PlaceResourceDAO getInstance() {
 		if (instance == null)
 			instance = new PlaceResourceDAO();
 		return instance;
 	}
 	
 	public boolean isAvailable(PlaceResource place, Timestamp begin, Timestamp end) {
 		HashMap<String, Object> params = new HashMap<String, Object>();
 		params.put("approved", true);
 		params.put("placeID", place.id);
 		
 		try {
 			List<Reservation> reservations = ReservationDAO.getInstance().getByAttributes(params);
 			
 			for (Reservation r: reservations) {
				return begin.compareTo(r.endTime) <= 0 && r.beginTime.compareTo(end) <= 0;
 			}
 			
 		} catch (ClassNotFoundException | SQLException e) {
 			e.printStackTrace();
 			
 			return false;
 		}
 		
 		return true;
 	}
 }

 package dal.concrete.mysql;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 
 import pojo.ObjectResource;
 
 import com.mysql.jdbc.Connection;
 
 import dal._abstract.mysql.AbstractDAO;
 import dal.connection.ConnectionManager;
 
 public class ObjectResourceDAO extends AbstractDAO<ObjectResource> {
 	private static ObjectResourceDAO instance;
 
 	public ObjectResourceDAO() {
 		super(ObjectResource.class);
 	}
 
 	public static ObjectResourceDAO getInstance() {
 		if (instance == null)
 			instance = new ObjectResourceDAO();
 		return instance;
 	}
 	
 	public boolean isAvailable(ObjectResource obj, Timestamp begin, Timestamp end) {
 		String query = "select * from Reservations, Reservations_Objects where" +
				" Reservations_Objects.reservationID=Reservations.id and Reservations.approved=true and Reservations.pendingApproval=false" +
 				" and Reservations_Objects.objectID=" + obj.id;
 		
 		ResultSet rs;
 		
 		try {
 			Connection conn = (Connection) ConnectionManager.getConnection();
 			
 	        PreparedStatement st = conn.prepareStatement(query);
 	        
 	        rs = st.executeQuery();
 			
 	        while (rs.next()) {
 	        	Timestamp begin2 = rs.getTimestamp("beginTime");
 				Timestamp end2 = rs.getTimestamp("endTime");
 	        	
 	        	if (begin.compareTo(end2) <= 0 && begin2.compareTo(end) <= 0) {
 	        		rs.close();
 	        		return false; 
 	        	}
 	        }
 	        
 	        rs.close();
 	        
 			return true;
 		} catch (ClassNotFoundException | SQLException e) {
 			e.printStackTrace();
 
 			return false;
 		}
 	}
 }
 
 

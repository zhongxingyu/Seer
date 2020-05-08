package at.ac.tuwien.dse.core.dao;
 
 import static org.junit.Assert.assertEquals;
 
 import java.net.UnknownHostException;
 import java.util.Date;
 
 
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 
import at.ac.tuwien.dse.core.db.DAOFactory;
import at.ac.tuwien.dse.core.db.TimeSlotDAO;
import at.ac.tuwien.dse.core.model.TimeSlot;
import at.ac.tuwien.dse.core.util.Config;
 
 import com.mongodb.BasicDBObject;
 import com.mongodb.DB;
 import com.mongodb.DBAddress;
 import com.mongodb.Mongo;
 
 
 public class Test_TimeSlotDAO {
 	private static DAOFactory fact;
 	private static DB db;
 	@BeforeClass
 	public static void setUp() {
 		try {
 			db = Mongo.connect(new DBAddress(Config.DB_HOST));
 		} catch (UnknownHostException e1) {
 			e1.printStackTrace();
 			return;
 		}
 		fact = new DAOFactory(db);
 		db.getCollection(Config.DB_COLLECTION_SLOT).remove(new BasicDBObject());
 	}
 	
 	@Test
 	public void testPersist() {
 		TimeSlotDAO tsDao = fact.getTimeSlotDAO();
 		TimeSlot ts = new TimeSlot();
 		Date now = new Date();
 		ts.setEnd(now);
 		ts.setStart(now);
 		ts.setHospitalId(1);
 		ts.setOperationId(2);
 		ts.setOperationTypeId(3);
 		tsDao.persist(ts);
 		
 		TimeSlot check = tsDao.findById(ts.getId());
 		assertEquals(ts.getId(), check.getId());
 		assertEquals(ts.getStart(), check.getStart());
 		assertEquals(ts.getEnd(), check.getEnd());
 		assertEquals(ts.getHospitalId(), check.getHospitalId());
 		assertEquals(ts.getOperationId(), check.getOperationId());
 		assertEquals(ts.getOperationTypeId(), check.getOperationTypeId());
 		db.getCollection(Config.DB_COLLECTION_SLOT).remove(new BasicDBObject());
 	}
 }

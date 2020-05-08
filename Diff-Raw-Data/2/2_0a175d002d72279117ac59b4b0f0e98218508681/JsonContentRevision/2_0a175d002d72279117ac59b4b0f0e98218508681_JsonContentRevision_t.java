 package nl.minicom.evenexus.persistence.versioning;
 
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
 import nl.minicom.evenexus.persistence.dao.Item;
 import nl.minicom.evenexus.persistence.dao.Station;
 
 import org.hibernate.Session;
 
 import com.google.gson.Gson;
 
 public class JsonContentRevision implements IRevision {
 	
 	private final Container container;
 	
 	public JsonContentRevision(String fileName) throws Exception {
 		Gson gson = new Gson();
 		InputStream in = JsonContentRevision.class.getResourceAsStream(fileName);
		container = gson.fromJson(new InputStreamReader(in, "UTF-8"), Container.class);
 	}
 	
 	@Override
 	public void execute(Session session) {
 		session.createSQLQuery("TRUNCATE TABLE stastations").executeUpdate();
 		
 		int count = 0;
 		for (Station station : container.getStations()) {
 			session.save(station);
 			count++;
 			
 			if (count % 100 == 0) {
 				session.flush();
 				session.clear();
 			}
 		}
 		
 		count = 0;
 		session.createSQLQuery("TRUNCATE TABLE invtypes").executeUpdate();
 		for (Item item : container.getItems()) {
 			session.save(item);
 			count++;
 			
 			if (count % 100 == 0) {
 				session.flush();
 				session.clear();
 			}
 		}
 	}
 
 	@Override
 	public int getRevisionNumber() {
 		return container.getVersion();
 	}
 
 }

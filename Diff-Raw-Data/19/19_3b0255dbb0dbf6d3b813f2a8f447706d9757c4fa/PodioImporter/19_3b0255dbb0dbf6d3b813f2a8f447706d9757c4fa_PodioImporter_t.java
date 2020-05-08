 package kea.kme.pullpit.server.podio;
 
import java.sql.SQLException;
 import java.util.ArrayList;
 
 import com.podio.filter.StandardSortBy;
 import com.podio.item.ItemAPI;
 import com.podio.item.ItemsResponse;
 
 import kea.kme.pullpit.server.persistence.PodioObjectHandler;
 import kea.kme.pullpit.server.podio.enums.App;
 
 public class PodioImporter {
 	private static PodioImporter podioImporter;
 	
 	private PodioImporter() {
 	}
 	
 	public static PodioImporter getInstance() {
 		if (podioImporter == null)
 			podioImporter = new PodioImporter();
 		return podioImporter;
 	}
 	
	public int[] pullAll() throws SQLException {
 		int[] pulls = new int[3];
 		pulls[0] = pullFromApp(App.VENUES, 0, 1000);
 		pulls[1] = pullFromApp(App.BANDS, 0, 2000);
 		pulls[2] = pullFromApp(App.SHOWS, 0, 2000);
 		return pulls;
 	}
 	
 	public int pullContacts() {
 		return 0;
 	}
 	
	public int pullShows() throws SQLException {
 		return pullFromApp(App.SHOWS, 0, 2000);
 	}
 
	public int pullBands() throws SQLException {
 		return pullFromApp(App.BANDS, 0, 2000);
 	}
 	
	public int pullVenues() throws SQLException {
 		return pullFromApp(App.VENUES, 0, 1000);
 	}
 
	private int pullFromApp(App app, int offset, int limit) throws SQLException {
 		AuthFactory authFactory = AuthFactory.getInstance();
 		ItemAPI items = authFactory.getItemAPI(app);
 		ItemsResponse ir = items.getItems(app.appID, limit, offset, StandardSortBy.LAST_EDIT_ON, true);
 		ArrayList<PodioBand> result = ArrayParser.getInstance().parseBands(ir);
		PodioObjectHandler.getInstance().writeBands(result.toArray(new PodioBand[result.size()]));
 		return result.size();
 	}
 }

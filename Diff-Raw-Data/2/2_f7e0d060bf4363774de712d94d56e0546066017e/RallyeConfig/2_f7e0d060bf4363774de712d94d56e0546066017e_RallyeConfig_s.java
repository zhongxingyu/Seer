 package de.rallye;
 
 import java.beans.PropertyVetoException;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 
 import com.mchange.v2.c3p0.ComboPooledDataSource;
 
 import de.rallye.db.DataAdapter;
 import de.rallye.images.ImageRepository;
 import de.rallye.model.structures.LatLng;
 import de.rallye.model.structures.ServerInfo;
 
 public class RallyeConfig {
 	
 	private static final Logger logger = LogManager.getLogger(RallyeConfig.class);
 	
 	private static final String HOST = "0.0.0.0";
 	private static final int PORT = 10101;
 	private static final int CONSOLE_PORT = 10100;
 	private static final String GCM_API_KEY = "AIzaSyBvku0REe1MwJStdJ7Aye6NC7bwcSO-TG0";
 	private static final String NAME = "Ray's RallyeServer";
 	private static final String DESCRIPTION = "Mein eigener Testserver, den ich Schritt für Schritt ausbaue bis alles funktioniert";
 	private static final ServerInfo.Api[] APIS = {new ServerInfo.Api("ist_rallye", 1), new ServerInfo.Api("scotlandYard", 3), new ServerInfo.Api("server", 4)};
 	private static final LatLng[] MAP_BOUNDS = {new LatLng(49.858959, 8.635107), new LatLng(49.8923691, 8.6746798)};
 	private static final LatLng MAP_CENTER = new LatLng(49.877648, 8.651762);
 	
 	
 	public static DataAdapter getMySQLDataAdapter() throws SQLException {
 		// create dataBase Handler
 		ComboPooledDataSource dataSource = new ComboPooledDataSource();
 		try {
 			dataSource.setDriverClass("com.mysql.jdbc.Driver");
 		} catch (PropertyVetoException e) {
 			logger.catching(e);
 		}
 		dataSource.setJdbcUrl("jdbc:mysql://hajoschja.de/rallye?characterEncoding=utf8");
 		dataSource.setUser("felix");
 		dataSource.setPassword("andro-rallye");
 		dataSource.setMaxIdleTime(3600); // set max idle time to 1 hour
 		
 		DataAdapter da = new DataAdapter(dataSource);
 		
 		return da;
 	}
 
 
 	public static ImageRepository getImageRepository() {
 		return new ImageRepository("pics/", 100, 25);
 	}
 	
 	public static String getHostName() {
 		return HOST;
 	}
 	
 	public static int getRestPort() {
 		return PORT;
 	}
 	
 	public static int getConsolePort() {
 		return CONSOLE_PORT;
 	}
 	
 	public static String getGcmKey() {
 		return GCM_API_KEY;
 	}
 
 	public static List<LatLng> getMapBounds() {
 		List<LatLng> res = new ArrayList<LatLng>();
 		for (LatLng ll : RallyeConfig.MAP_BOUNDS) {
 			res.add(ll);
 		}
 		return res;
 	}
 	
 	public static LatLng getMapCenter() {
 		return MAP_CENTER;
 	}
 	
 	public static ServerInfo getServerDescription() {
		return new ServerInfo(NAME, DESCRIPTION, APIS);
 	}
 
 }

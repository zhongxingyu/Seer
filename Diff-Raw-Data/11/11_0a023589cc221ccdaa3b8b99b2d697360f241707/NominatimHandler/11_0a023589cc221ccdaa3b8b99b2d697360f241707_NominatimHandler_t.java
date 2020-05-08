 package de.uni.stuttgart.informatik.ToureNPlaner.Net.Handler;
 
 import android.graphics.RectF;
 import de.uni.stuttgart.informatik.ToureNPlaner.Net.JacksonManager;
 import de.uni.stuttgart.informatik.ToureNPlaner.Net.Observer;
 import de.uni.stuttgart.informatik.ToureNPlaner.ToureNPlanerApplication;
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.mapsforge.core.GeoPoint;
 
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.net.URLEncoder;
 
 public class NominatimHandler extends GeoCodingHandler {
 	private final String query;
 	private final RectF viewbox;
 
	private static final String HOST = "http://nominatim.openstreetmap.org/search?format=json&limit=1";
 
 	public NominatimHandler(Observer listener, String query, RectF viewbox) {
 		super(listener);
 		this.query = query;
 		this.viewbox = viewbox;
 	}
 
 	@Override
 	protected HttpURLConnection getHttpUrlConnection() throws Exception {
 		URL url = new URL(HOST +
 				"&q=" + URLEncoder.encode(query, "UTF-8") +
 				"&viewbox=" + viewbox.left + "," + viewbox.top + "," + viewbox.right + "," + viewbox.bottom);
 		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
 		connection.setRequestProperty("User-Agent", "Android " + ToureNPlanerApplication.getApplicationIdentifier());
 		return connection;
 	}
 
 	@Override
 	protected Object handleInput(JacksonManager.ContentType type, InputStream inputStream) throws Exception {
 		ObjectMapper mapper = JacksonManager.getMapper(type);
 		JsonNode node = mapper.readValue(inputStream, JsonNode.class);
 		GeoCodingResult result = new GeoCodingResult();
 
 		result.location = new GeoPoint(
 				node.get(0).get("lat").asDouble(),
 				node.get(0).get("lon").asDouble());
 
 		return result;
 	}
 }

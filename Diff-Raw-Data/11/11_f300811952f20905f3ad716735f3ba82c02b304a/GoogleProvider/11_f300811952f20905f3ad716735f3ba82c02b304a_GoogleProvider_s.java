 package at.ac.tuwien.msna.geolocation;
 
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.util.Date;
 
 import org.json.simple.JSONObject;
 import org.json.simple.JSONValue;
 
 import at.ac.tuwien.msna.geolocation.wifi.AccessPoint;
 import at.ac.tuwien.msna.geolocation.wifi.WirelessMonitor;
 import at.ac.tuwien.msna.geolocation.wifi.WirelessMontiorFactory;
 
 public class GoogleProvider extends PositionProvider {
 
 	private static final String ENDPOINT = "https://maps.googleapis.com/maps/api/browserlocation/json?browser=firefox&sensor=true";
 
 	@Override
 	public Position getCurrentPosition() {
 		WirelessMonitor wm = WirelessMontiorFactory.getInstance();
 		StringBuilder sb = new StringBuilder(ENDPOINT);
 		for (AccessPoint ap : wm.list()) {
 			try {
 				sb.append(String.format("&wifi=mac:%s|ssid:%s|ss:%d", ap.getMAC(),
 					URLEncoder.encode(ap.getSSID(), "UTF-8"), ap.getStrength(), "UTF-8"));
 			} catch (UnsupportedEncodingException e) {
 				throw new RuntimeException(e);
 			}
 		}
 
 		System.out.println(sb.toString());
 		URL request;
 		try {
 			request = new URL(sb.toString());
 			JSONObject result = (JSONObject) JSONValue.parse(new InputStreamReader(request.openStream()));
			
 			if (result.get("status").equals("OK")) {
 				double accuracy = (double) result.get("accuracy");
 				JSONObject location = (JSONObject) result.get("location");
				return new Position((double) location.get("lat"), (double) location.get("lng"), accuracy, new Date().getTime());
 			}
			
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 		return null;
 	}
	
 }

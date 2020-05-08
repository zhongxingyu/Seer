 package util;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.io.Reader;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.nio.charset.Charset;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Scanner;
 import java.util.Set;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import de.fhpotsdam.unfolding.geo.Location;
 
 /**
  * This class will load in the locations of organisations from the cache file.
  * It will perform a Google Places lookup if the name of the place is not in
  * cache (and add it to the cache). Just fill in the cacheFile and
  * GOOGLE_PLACES_API_KEY values.
  * 
  * @author Glenn Croes
  * 
  */
 public class LocationCache {
 
 	private Map<String, Location> locs;
 	private static final String cacheFile = "data/org_locs.txt";
 	private static final String GOOGLE_PLACES_API_KEY = "AIzaSyCkUxgtcX32ogBa2Odtf2L4IWsYAH81mfw";
 
 	private static LocationCache instance = null;
 
 	public static LocationCache getInstance() {
 		if (instance == null) {
 			instance = new LocationCache();
 		}
 		return instance;
 	}
 
 	private LocationCache() {
 		locs = new HashMap<String, Location>();
 		initCache();
 	}
 
 	private void initCache() {
 		Scanner scan = null;
 		;
 		try {
 			scan = new Scanner(new File(cacheFile));
 			try {
 				scan.nextLine(); // skip header
 				while (scan.hasNextLine()) {
 					String line = scan.nextLine();
 					String[] cells = line.split(";");
 					float lat = Float.parseFloat(cells[0]);
 					float lng = Float.parseFloat(cells[1]);
 					String name = cells[2];
 					name = name.trim();
 					if (!name.equals(""))
 						locs.put(name, new Location(lat, lng));
 				}
 			} finally {
 				scan.close();
 			}
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 	}
 
	private Location addLocation(String name) {
		Location loc = lookup(name);
		addLocation(name, loc);
		return loc;
	}

 	public void addLocation(String name, Location loc) {
 		name = name.trim();
 		if (!name.equals("")) {
 			locs.put(name, loc);
 			writeEntryToFile(name, loc);
 		}
 	}
 
 	public boolean inCache(String name) {
 		return locs.containsKey(name);
 	}
 
 	private void writeEntryToFile(String name, Location loc) {
 		PrintWriter out = null;
 		try {
 			out = new PrintWriter(new FileWriter(cacheFile, true));
 			out.printf("%.7f;%.7f;%s;-;\n", loc.x, loc.y, name);
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			if (out != null)
 				out.close();
 		}
 	}
 
 	public static Location get(String name) {
 		if (getInstance().locs.containsKey(name)) {
 			return getInstance().locs.get(name);
 		} else {
 			Location loc = getInstance().lookup(name);
 			if (loc != null)
 				getInstance().addLocation(name, loc);
 			return loc;
 		}
 	}
 
 	public Set<Entry<String, Location>> getEntries() {
 		return locs.entrySet();
 	}
 
 	public Set<String> getNames() {
 		return locs.keySet();
 	}
 
 	public Collection<Location> getLocations() {
 		return locs.values();
 	}
 
 	public Location lookup(String name) {
 		if (name.trim().length() == 0) return null;
 		try {
 			JSONObject json = readJsonFromUrl("https://maps.googleapis.com/maps/api/place/textsearch/json?sensor=false&key="
 					+ GOOGLE_PLACES_API_KEY + "&query=" + URLEncoder.encode(name, "UTF-8"));
 			if (json.getString("status").equals("OK")) {
 				double latitude = json.getJSONArray("results").getJSONObject(0).getJSONObject("geometry")
 						.getJSONObject("location").getDouble("lat");
 				double longitude = json.getJSONArray("results").getJSONObject(0).getJSONObject("geometry")
 						.getJSONObject("location").getDouble("lng");
 				Location loc = new Location(latitude, longitude);
 				locs.put(name, loc);
 				return loc;
 				// System.out.print(String.format(": <%.7f %.7f>\n", latitude,
 				// longitude));
 			} else {
 				System.out.printf("No coordinates found for %s.\n", name);
 			}
 		} catch (IOException | JSONException e) {
 			System.err.printf("Error encountered when looking up the location of %s\n", name);
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	public static void main(String[] args) {
 		LocationCache cache = new LocationCache();
 		for (Entry<String, Location> e : cache.getEntries()) {
 			System.out.println(String.format("%s: <%.7f, %.7f>", e.getKey(), e.getValue().x, e.getValue().y));
 		}
 		// cache.writeEntryToFile("Candy Mountain", new Location(50,50));
 	}
 
 	public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
 		InputStream is = new URL(url).openStream();
 		try {
 			BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
 			String jsonText = readAll(rd);
 			JSONObject json = new JSONObject(jsonText);
 			return json;
 		} finally {
 			is.close();
 		}
 	}
 
 	private static String readAll(Reader rd) throws IOException {
 		StringBuilder sb = new StringBuilder();
 		int cp;
 		while ((cp = rd.read()) != -1) {
 			sb.append((char) cp);
 		}
 		return sb.toString();
 	}
 
 }

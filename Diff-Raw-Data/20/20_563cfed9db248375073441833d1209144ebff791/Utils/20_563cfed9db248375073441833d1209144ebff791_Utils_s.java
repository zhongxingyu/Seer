 /**
  * 
  */
 package calabash.java;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 /**
  * 
  *
  */
 final class Utils {
 
 	public static String getStringFromJSON(JSONObject target, String key) {
 		try {
 			return target.getString(key);
 		} catch (JSONException e) {
 			return null;
 		}
 	}
 
 	public static Integer getIntFromJSON(JSONObject target, String key) {
 		try {
 			return target.getInt(key);
 		} catch (JSONException e) {
 			return null;
 		}
 	}
 
 	public static String capitalize(String string) {
 		if (string == null || string.length() == 0)
 			return string;
 
 		return String.format("%c%s", Character.toUpperCase(string.charAt(0)),
 				string.substring(1, string.length()));
 	}
 
 	public static UIElements query(String query) throws CalabashException {
 		JSONArray results = query(query, (String[]) null);
 		return new UIElements(results, query);
 	}
 
 	public static JSONArray query(String query, String... filter)
 			throws CalabashException {
 		JSONObject jsonObject = new JSONObject();
 		jsonObject.put("query", query);
 
 		Map<String, Object> map = new HashMap<String, Object>();
		map.put("method_name", "query");
 
		if (filter != null)
			map.put("arguments", new JSONArray(filter));
 
 		jsonObject.put("operation", map);
 
 		String result = new Http(Config.endPoint()).post("map",
 				jsonObject.toString());
 		try {
 			return new JSONObject(result).getJSONArray("results");
 		} catch (JSONException e) {
 			throw new CalabashException("Result is not in expected format.\n"
 					+ result, e);
 		}
 	}
 
 	public static void playback(String recordingName, String query)
 			throws CalabashException {
 		CalabashServerVersion version = CalabashRunner.getServerVersion();
 		String uiaGesture = null;
 		if (version.getiOSVersion().major().equals("7"))
 			uiaGesture = "tap";
 
 		String playbackData = loadPlaybackData(recordingName);
 		JSONObject postData = new JSONObject();
 		postData.put("events", playbackData);
 
 		if (query != null)
 			postData.put("query", query);
 
 		if (uiaGesture != null)
 			postData.put("uia_gesture", uiaGesture);
 
 		new Http(Config.endPoint()).post("play", postData.toString());
 	}
 
 	public static String loadPlaybackData(String recordingName)
 			throws CalabashException {
 		String os = Config.getOS();
 		if (os == null) {
 			os = "ios"
 					+ CalabashRunner.getServerVersion().getiOSVersion().major();
 		}
 
 		File file = getEventFile(recordingName, os);
 		if (!file.exists() && "ios6".equals(os))
 			file = getEventFile(recordingName, "ios5");
 
 		if (!file.exists())
 			throw new CalabashException(String.format(
 					"Can't load playback data. %s does not exists",
 					file.getAbsolutePath()));
 
 		try {
 			return readFileAsString(file);
 		} catch (IOException e) {
 			throw new CalabashException(String.format(
 					"Can't load playback data from %s. %s",
 					file.getAbsolutePath(), e.getMessage()), e);
 		}
 	}
 
 	private static File getEventFile(String recordingName, String os) {
 		// TODO: Remove hardcoded paths
 		File eventsDir = new File(
 				"/Users/navaneeth/projects/calabash/calabash-ios-java/events");
 		return new File(eventsDir, String.format("%s_%s_%s.base64",
 				recordingName, os, Config.getDevice()));
 	}
 
 	private static String readFileAsString(File f) throws IOException {
 		if (!f.exists())
 			return null;
 
 		FileInputStream stream = new FileInputStream(f);
 		byte[] b = new byte[stream.available()];
 		stream.read(b);
 		stream.close();
 
 		return new String(b, "UTF-8");
 	}
 
 	public static void sleep(int ms) {
 		try {
 			Thread.sleep(10);
 		} catch (InterruptedException e) {
 		}
 	}
 
 }

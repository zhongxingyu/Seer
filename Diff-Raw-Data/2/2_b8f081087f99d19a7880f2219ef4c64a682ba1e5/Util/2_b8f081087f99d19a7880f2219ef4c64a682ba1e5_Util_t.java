 package nl.q42.huelimitededition;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.security.SecureRandom;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.UUID;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import nl.q42.huelimitededition.models.Bridge;
 import nl.q42.javahueapi.models.Action;
 import nl.q42.javahueapi.models.Group;
 import nl.q42.javahueapi.models.Light;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.graphics.Color;
 import android.os.Build;
 import android.preference.PreferenceManager;
 import android.util.Base64;
 
 public class Util {
 	public static String quickMatch(String pattern, String target) {
 		Matcher m = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(target);
 		if (m.find()) {
 			return m.group(1);
 		} else {
 			return null;
 		}
 	}
 	
 	/**
 	 * Returns the string or a shortened version suited for the bridge
 	 */
 	public static String ensureMaxLength(String str) {
 		if (str.length() <= 40) {
 			return str;
 		} else {
 			return str.substring(0, 37) + "...";
 		}
 	}
 	
 	/**
 	 * Returns unique app installation identifier, used to identify this device
 	 */
 	public static String getDeviceIdentifier(Context ctx) {
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
 		
 		// Older bridge firmware (Q42 kitchen) doesn't seem to allow dashes in usernames
 		if (prefs.contains("uuid")) {
 			return prefs.getString("uuid", null).replace("-", "");
 		} else {
 			String uuid = generateUUID();
 			
 			SharedPreferences.Editor ed = prefs.edit();
 			ed.putString("uuid", uuid);
 			ed.commit();
 			
 			return uuid.replace("-", "");
 		}
 	}
 	
 	public static void setLastBridge(Context ctx, Bridge b) {
 		if (b == null) {
 			SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(ctx).edit();
 			prefs.remove("lastBridge");
 			prefs.commit();
 			return;
 		}
 		
 		// Serialize bridge object and store it
 		try {
 			ByteArrayOutputStream binStr = new ByteArrayOutputStream();
 			ObjectOutputStream objStr = new ObjectOutputStream(binStr);
 			objStr.writeObject(b);
 			objStr.close();
 			
 			SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(ctx).edit();
 			prefs.putString("lastBridge", Base64.encodeToString(binStr.toByteArray(), Base64.DEFAULT));
 			prefs.commit();
 		} catch (IOException e) {
 			// This should never happen
 			e.printStackTrace();
 		}
 	}
 	
 	public static Bridge getLastBridge(Context ctx) {
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
 		
 		if (prefs.contains("lastBridge")) {
 			try {
 				byte[] data = Base64.decode(prefs.getString("lastBridge", null), Base64.DEFAULT);
 				ByteArrayInputStream binStr =  new ByteArrayInputStream( data); 
 				ObjectInputStream objStr = new ObjectInputStream(binStr);
 				Bridge b = (Bridge) objStr.readObject();
 				objStr.close();
 				return b;
 			} catch (Exception e) {
 				// Should never happen
 				e.printStackTrace();
 			}
 		}
 		
 		return null;
 	}
 	
 	private static String generateUUID() {
 		SecureRandom rand = new SecureRandom();
 		UUID uuid = new UUID(rand.nextLong(), rand.nextLong());
 		return uuid.toString();
 	}
 	
 	public static String getDeviceName() {
 		String manufacturer = Build.MANUFACTURER;
 		String model = Build.MODEL;
 
 		if (model.startsWith(manufacturer)) {
 			return model.substring(0, 1).toUpperCase() + model.substring(1);
 		} else {
 			return manufacturer.substring(0, 1).toUpperCase() + manufacturer.substring(1) + " " + model;
 		}
 	}
 	
 	/**
 	 * Sorts list of strings by integer values if available
 	 */
 	public static void sortNumericallyIfPossible(ArrayList<String> list) {
 		Collections.sort(list, new Comparator<String>() {
 			@Override
 			public int compare(String lhs, String rhs) {
 				Integer lhsi = Integer.valueOf(lhs);
 				Integer rhsi = Integer.valueOf(rhs);
 				
 				if (lhsi != null && rhsi != null) {
 					return lhsi - rhsi;
 				} else {
 					return lhs.compareTo(rhs);
 				}
 			}
 		});
 	}
 	
 	/**
 	 * Sorts map of groups by name and returns the ids in order
 	 */
 	public static ArrayList<String> getSortedGroups(HashMap<String, Group> groups) {
 		class GroupPair implements Comparable<GroupPair> {
 			public String id;
 			public Group group;
 			
 			@Override
 			public int compareTo(GroupPair another) {
 				return group.name.compareTo(another.group.name);
 			}
 		}
 		
 		// Build list of (id, group) tuples and sort it
 		ArrayList<GroupPair> pairs = new ArrayList<GroupPair>();
 		
 		for (String id : groups.keySet()) {
 			GroupPair pair = new GroupPair();
 			pair.id = id;
 			pair.group = groups.get(id);
 			pairs.add(pair);
 		}
 		
 		Collections.sort(pairs);
 		
 		// Return ids of sorted groups
 		ArrayList<String> ids = new ArrayList<String>();
 		
 		for (GroupPair pair : pairs) {
 			ids.add(pair.id);
 		}
 		
 		// Make sure "all" group comes first
 		ids.remove("0");
 		ids.add(0, "0");
 		
 		return ids;
 	}
 	
 	/**
 	 * Sorts map of lights by name and returns the ids in order
 	 */
 	public static ArrayList<String> getSortedLights(HashMap<String, Light> lights) {
 		class LightPair implements Comparable<LightPair> {
 			public String id;
 			public Light light;
 			
 			@Override
 			public int compareTo(LightPair another) {
				if (light.name == null)
					return 1;
 				return light.name.compareTo(another.light.name);
 			}
 		}
 		
 		// Build list of (id, light) tuples and sort it
 		ArrayList<LightPair> pairs = new ArrayList<LightPair>();
 		
 		for (String id : lights.keySet()) {
 			LightPair pair = new LightPair();
 			pair.id = id;
 			pair.light = lights.get(id);
 			pairs.add(pair);
 		}
 		
 		Collections.sort(pairs);
 		
 		// Return ids of sorted lights
 		ArrayList<String> ids = new ArrayList<String>();
 		
 		for (LightPair pair : pairs) {
 			ids.add(pair.id);
 		}
 		
 		return ids;
 	}
 	
 	/**
 	 * Color conversion helper functions
 	 */
 	public static int getRGBColor(Light light) {
 		return getRGBColor(light.state, light.modelid);
 	}
 	
 	public static int getRGBColor(Group group) {
 		return getRGBColor(group.action, null);
 	}
 	
 	private static int getRGBColor(Action state, String model) {
 		if (!state.on) {
 			return Color.BLACK;
 		}
 		
 		// Convert HSV color to RGB
 		if ("hs".equals(state.colormode)) {
 			float[] components = new float[] {
 				(float) state.hue / 65535.0f * 360.0f,
 				(float) state.sat / 255.0f,
 				1.0f // Ignore brightness for more clear color view, hue is most important anyway
 			};
 			
 			return Color.HSVToColor(components);
 		} else if ("xy".equals(state.colormode)) {
 			float[] points = new float[] { (float) state.xy[0], (float) state.xy[1] };
 			return PHUtilitiesImpl.colorFromXY(points, model);
 		} else if ("ct".equals(state.colormode)) {
 			return temperatureToColor(1000000 / state.ct);
 		} else {
 			return Color.WHITE;
 		}
 	}
 	
 	// Adapted from: http://www.tannerhelland.com/4435/convert-temperature-rgb-algorithm-code/
 	public static int temperatureToColor(long tmpKelvin) {
 		double tmpCalc;
 		int r, g, b;
 		
 		// Temperature must fall between 1000 and 40000 degrees
 		tmpKelvin = Math.min(40000, Math.max(tmpKelvin, 1000));
 		
 		// All calculations require tmpKelvin / 100, so only do the conversion once
 		tmpKelvin /= 100;
 		
 		// Calculate each color in turn
 		
 		// First: red
 		if (tmpKelvin <= 66) {
 			r = 255;
 		} else {
 			// Note: the R-squared value for this approximation is .988
 			tmpCalc = tmpKelvin - 60;
 			tmpCalc = 329.698727446 * Math.pow(tmpCalc, -0.1332047592);
 			r = (int) tmpCalc;
 			r = Math.min(255, Math.max(r, 0));
 		}
 		
 		// Second: green
 		if (tmpKelvin <= 66) {
 			// Note: the R-squared value for this approximation is .996
 			tmpCalc = tmpKelvin;
 			tmpCalc = 99.4708025861 * Math.log(tmpCalc) - 161.1195681661;
 			g = (int) tmpCalc;
 			g = Math.min(255, Math.max(g, 0));
 		} else {
 			// Note: the R-squared value for this approximation is .987
 			tmpCalc = tmpKelvin - 60;
 			tmpCalc = 288.1221695283 * Math.pow(tmpCalc, -0.0755148492);
 			g = (int) tmpCalc;
 			g = Math.min(255, Math.max(g, 0));
 		}
 		
 		// Third: blue
 		if (tmpKelvin >= 66) {
 			b = 255;
 		} else if (tmpKelvin <= 19) {
 			b = 0;
 		} else {
 			// Note: the R-squared value for this approximation is .998
 			tmpCalc = tmpKelvin - 10;
 			tmpCalc = 138.5177312231 * Math.log(tmpCalc) - 305.0447927307;
 			b = (int) tmpCalc;
 			b = Math.min(255, Math.max(b, 0));
 		}
 		
 		return Color.rgb(r, g, b);
 	}
 	
 	/** Checks strings for equality. Can handle null strings. */
 	public static boolean stringEquals(String a, String b) {
 		if (a == null) {
 			return b == null;
 		}
 		return a.equals(b);
 	}
 }

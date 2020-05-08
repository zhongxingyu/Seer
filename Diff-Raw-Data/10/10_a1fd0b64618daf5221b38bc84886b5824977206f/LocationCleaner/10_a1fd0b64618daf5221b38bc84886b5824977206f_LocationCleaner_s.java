 package com.sk.guess.impl;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import com.google.gson.JsonArray;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonParser;
 import com.sk.guess.DataCleaner;
 import com.sk.util.FieldBuilder;
 import com.sk.util.PersonalData;
 
 public class LocationCleaner implements DataCleaner {
 
 	private static final String URL = "http://maps.googleapis.com/maps/api/geocode/json?language=en&sensor=false&address=%s";
	private static final JsonParser parser = new JsonParser();
	private static final Pattern nameReplace = Pattern.compile("_([a-z0-9])");
 	private static final String[] LOCATION_FIELDS = { "address", "location", "zipcode", "house", "apartment",
 			"street", "state", "country", "city", "poBox" };
 
 	@Override
 	public boolean clean(PersonalData in, PersonalData out) {
 		try {
 			String[] locs;
 			FieldBuilder builder = new FieldBuilder();
 			if (in.containsKey("address"))
 				locs = in.getAllValues("address");
 			else if (in.containsKey("location")) {
 				locs = in.getAllValues("location");
 				for (int i = 0; i < locs.length; ++i)
 					locs[i] = locs[i].replaceAll("(?:(?i)metro area|\\.)", "");
 			} else
 				return false;
 			for (String loc : locs) {
 				String formatUrl = String.format(URL, URLEncoder.encode(loc, "UTF-8"));
				JsonObject value = parser.parse(
 						new BufferedReader(new InputStreamReader(new URL(formatUrl).openStream())))
 						.getAsJsonObject();
 				if (!value.get("status").getAsString().equals("OK"))
 					return false;
 				for (JsonElement resultElement : value.get("results").getAsJsonArray()) {
 					JsonArray addressComponents = resultElement.getAsJsonObject().get("address_components")
 							.getAsJsonArray();
 					for (int i = addressComponents.size() - 1; i >= 0; --i) {
 						JsonObject addressComponent = addressComponents.get(i).getAsJsonObject();
 						String type = addressComponent.get("types").getAsJsonArray().get(0).getAsString();
 						StringBuffer newType = new StringBuffer("L");
						Matcher mat = nameReplace.matcher(type);
 						while (mat.find()) {
 							mat.appendReplacement(newType, mat.group(1).toUpperCase());
 						}
 						mat.appendTail(newType);
 						builder.put(addressComponent, "short_name", newType.toString());
 					}
 				}
 			}
 			for (String field : LOCATION_FIELDS) {
 				in.remove(field);
 			}
 			builder.addTo(out);
 			return true;
 		} catch (IOException ex) {
 			ex.printStackTrace();
 			return false;
 		}
 	}
 }

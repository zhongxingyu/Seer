 package edu.vanderbilt.vm.guide.util;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Context;
 import android.net.Uri;
 
 import com.google.gson.stream.JsonReader;
 
 public class JsonUtils {
 
 	/**
 	 * Because this is just a class for static utility methods, this
 	 * class should not be instantiated.
 	 */
 	private JsonUtils() {
 		throw new AssertionError("Do not instantiate this class.");
 	}
 	
 	public static List<Place> readPlacesFromFile(Uri uri, Context context) throws IOException {
 		InputStream in = context.getContentResolver().openInputStream(uri);
 		JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
 		List<Place> places = new ArrayList<Place>();
 		
 		reader.beginArray();
 		while(reader.hasNext()) {
 			places.add(readPlace(reader));
 		}
 		return places;
 	}
 	
 	public static Place readPlace(JsonReader reader) throws IOException {
 		Place.Builder bldr = new Place.Builder();
 		reader.beginObject();
 		while(reader.hasNext()) {
 			String name = reader.nextName();
 			if(name.equals("id")) {
 				bldr.setUniqueId(reader.nextInt());
 			} else if(name.equals("name")) {
 				bldr.setName(reader.nextString());
 			} else if(name.equals("category")) {
 				bldr.setCategory(reader.nextString());
 			} else if(name.equals("hours")) {
 				bldr.setHours(reader.nextString());
 			} else if(name.equals("description")) {
 				bldr.setDescription(reader.nextString());
 			} else if(name.equals("imagePath")) {
 				bldr.setPictureUri(Uri.parse(reader.nextString()));
 			} else if(name.equals("videoPath")) {
 				bldr.setVideoUri(Uri.parse(reader.nextString()));
 			} else if(name.equals("audioPath")) {
 				bldr.setAudioUri(Uri.parse(reader.nextString()));
 			} else if(name.equals("latitude")) {
 				bldr.setLatitude(reader.nextDouble());
 			} else if(name.equals("longitude")) {
 				bldr.setLongitude(reader.nextDouble());
 			} else {
 				reader.skipValue();
 			}
 		}
 		reader.endObject();
 		return bldr.build();
 	}
 	
 }

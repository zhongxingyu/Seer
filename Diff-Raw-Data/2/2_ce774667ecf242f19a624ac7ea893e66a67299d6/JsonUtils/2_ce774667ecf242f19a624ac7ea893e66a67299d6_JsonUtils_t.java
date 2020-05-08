 package edu.vanderbilt.vm.guide.util;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
 import org.json.JSONArray;
 
 import android.content.Context;
 import android.net.Uri;
 
 public class JsonUtils {
 
 	/**
 	 * Because this is just a class for static utility methods, this
 	 * class should not be instantiated.
 	 */
 	private JsonUtils() {
 		throw new AssertionError("Do not instantiate this class.");
 	}
 	
 	public static JSONArray readJSONArrayFromFile(Uri uri, Context context) throws FileNotFoundException {
 		
 		InputStream in = context.getContentResolver().openInputStream(uri);
 		BufferedReader buf = new BufferedReader(new InputStreamReader(in));
 		
		return null;
		// incomplete code
 		
 		
 	}
 	
 }

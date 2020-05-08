 package com.github.norwae.whatiread.googlebooks;
 
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Locale;
 
 import android.util.Log;
 
 import com.github.norwae.whatiread.data.BookInfo;
 import com.github.norwae.whatiread.data.ISBN13;
 
 public class GoogleBooksISBNRequest {
 	
 	private static final String QUERY_TEMPLATE = "https://www.googleapis.com/books/v1/volumes?q=isbn:$ISBN&key=$KEY&country=$COUNTRY&projection=lite";
 
 	private final String key;
 	private final String code;
 	private final Locale locale;
 
 	public GoogleBooksISBNRequest(ISBN13 isbn, String key, Locale locale) {
 		this.key = key;
 		this.code = isbn.toString();
 		this.locale = locale;
 		
 	}
 
 	public BookInfo execute() {
 		URL serviceURL;
 		try {
 			serviceURL = composeServiceURL();
 			
 
 			Log.i("google-books-api", "preparing to call: " + serviceURL);			
 			HttpURLConnection connection = (HttpURLConnection) serviceURL.openConnection();
			if (HttpURLConnection.HTTP_OK != connection.getResponseCode()) {
 				Log.i("google-books-api", "API rejected call with code " + connection.getResponseCode() + " <" + connection.getResponseMessage() + ">");
 			}
			
			GoogleBooksJSONParser parser = new GoogleBooksJSONParser(connection.getInputStream(), code);
			
			GoogleBooksAPIResult result = parser.parse();
			
			if (!result.getBookInfos().isEmpty()) {
				return result.getBookInfos().get(0);
			}
 		} catch (Exception e) {
 			Log.e("google-books-api", "failed to call", e);
 		}
 		
 		return null;
 	}
 
 	private URL composeServiceURL() throws MalformedURLException {
 		String url = QUERY_TEMPLATE.replace("$ISBN", code).replace("$KEY", key).replace("$COUNTRY", locale.getCountry());
 		
 		return new URL(url);
 	}
 
 }

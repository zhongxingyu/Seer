 package com.readmill.dal;
 
 import java.io.IOException;
 import java.net.URI;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.TimeZone;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.readmill.api.ApiWrapper;
 import com.readmill.api.Endpoints;
 import com.readmill.api.Env;
 import com.readmill.api.Http;
 import com.readmill.api.Params;
 import com.readmill.api.ReadmillAPI.TokenStateListener;
 import com.readmill.api.Request;
 import com.readmill.api.Token;
 
 /**
  * @author christoffer
 * 
  */
 public class ReadmillDAL {
   private static final SimpleDateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
   private static final TimeZone utcTimeZone = TimeZone.getTimeZone("UTC");
 
   private ApiWrapper mWrapper;
 
   public ReadmillDAL(ApiWrapper wrapper) {
     mWrapper = wrapper;
   }
 
   public ReadmillDAL(String clientId, String clientSecret, URI redirectUri, Token token, Env env) {
     this(new ApiWrapper(clientId, clientSecret, redirectUri, token, env));
   }
 
   public void setAccessToken(Token newToken) {
     mWrapper.setToken(newToken);
   }
 
   public Token getAccessToken() {
     return mWrapper.getToken();
   }
 
   public boolean exchangeCodeForToken(String code) {
     try {
       mWrapper.authorizationCode(code);
       return mWrapper.getToken().valid();
     } catch (IOException e) {
       e.printStackTrace();
     }
     return false;
   }
 
   public String getAuthorizeURL() {
     return mWrapper.authorizationCodeUrl().toString();
   }
 
   public void addTokenStateChangeListener(TokenStateListener listener) {
     mWrapper.addTokenStateListener(listener);
   }
 
   public ArrayList<ReadmillBook> searchBooks(String searchText) {
     JSONArray receivedArray;
     ArrayList<ReadmillBook> foundBooks = new ArrayList<ReadmillBook>();
     try {
       Request request = Request.to(Endpoints.BOOK_SEARCH).with("q", searchText);
 
       HttpResponse response = mWrapper.get(request);
 
       if(response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
         System.out.println("Status:" + response.getStatusLine().getStatusCode());
         throw new IOException("Failed to search for books");
       }
 
       receivedArray = Http.getJSONArray(mWrapper.get(request));
       for (int i = 0; i < receivedArray.length(); i++) {
         try {
           ReadmillBook book = new ReadmillBook((JSONObject) receivedArray.get(i));
           foundBooks.add(book);
         } catch (JSONException ignored) {
         }
       }
     } catch (IOException e) {
       e.printStackTrace();
     }
     return foundBooks;
   }
 
   public ReadmillUser getCurrentUser() {
     try {
       JSONObject data = Http.getJSON(mWrapper.get(Request.to(Endpoints.ME)));
       return new ReadmillUser(data);
     } catch (IOException e) {
       e.printStackTrace();
     }
     return null;
   }
 
   public ReadmillReading getReading(String location) {
     try {
       JSONObject data = Http.getJSON(mWrapper.get(new Request(toResourceURI(location))));
       return new ReadmillReading(data);
     } catch (IOException e) {
       e.printStackTrace();
     }
     return null;
   }
 
   public ReadmillReading getReading(long id) {
     return getReading(String.format(Endpoints.READINGS, id));
   }
 
   /**
    * Fetch a book by URI
   * 
    * @param location URI to the book to fetch
    * @return A ReadmillBook or null
    */
   public ReadmillBook getBook(String location) {
     try {
       JSONObject data = Http.getJSON(mWrapper.get(new Request(toResourceURI(location))));
       return new ReadmillBook(data);
     } catch (IOException e) {
       e.printStackTrace();
     }
     return null;
   }
 
   /**
    * Create a new reading for the provided book
   * 
    * @param book ReadmillBook to create a reading for
    * @param state Reading state of the reading to create (ReadmillReading.State)
    * @param privacy Privacy setting for the reading (ReadmillReading.Privacy)
    * @return the created ReadmillReading if successful, null otherwise
    */
   public ReadmillReading createReading(ReadmillBook book, int state, int privacy) {
     Request request = Request.to(Endpoints.BOOK_READINGS, book.id);
     request.add(Params.Reading.IS_PRIVATE, privacy);
     request.add(Params.Reading.STATE, state);
 
     try {
       HttpResponse response = mWrapper.post(request);
 
       int statusCode = response.getStatusLine().getStatusCode();
 
       if(statusCode == HttpStatus.SC_CREATED || statusCode == HttpStatus.SC_CONFLICT) {
         String location = response.getFirstHeader("Location").getValue();
         return getReading(location);
       }
 
     } catch (IOException e) {
       e.printStackTrace();
     }
 
     return null;
   }
 
   /**
    * Create a Book at Readmill
   * 
    * @param title Title of book
    * @param author Author of book
    * @return the created ReadmillBook or null
    */
   public ReadmillBook createBook(String title, String author) {
     Request request = Request.to(Endpoints.BOOKS);
     request.add(Params.Book.TITLE, title);
     request.add(Params.Book.AUTHOR, author);
 
     try {
       HttpResponse response = mWrapper.post(request);
 
       int statusCode = response.getStatusLine().getStatusCode();
 
       if(statusCode == HttpStatus.SC_CREATED) {
         String location = response.getFirstHeader("Location").getValue();
         return getBook(location);
       }
 
     } catch (IOException e) {
       e.printStackTrace();
     }
 
     return null;
   }
 
   /**
    * Create a Ping at Readmill
   * 
    * @param identifier Unique reading session identifier
    * @param readingId Reading to ping
    * @param progress Progress when this ping was sent [0, 1.0]
    * @param durationSeconds How many seconds of effective reading time that has passed
    *          since the last ping
    * @param occurredAt When the ping occurred
    * @return true of the Ping was created, false otherwise
    */
   public boolean postPing(String identifier, long readingId, double progress, long durationSeconds, Date occurredAt) {
     Request request = Request.to(Endpoints.PING, readingId);
 
     request.add(Params.Ping.IDENTIFIER, identifier);
     request.add(Params.Ping.PROGRESS, progress);
     request.add(Params.Ping.DURATION, durationSeconds);
     request.add(Params.Ping.OCCURRED_AT, toISODate(occurredAt));
 
     try {
       HttpResponse response = mWrapper.post(request);
       System.out.println("PING RETURNED: " + response.getStatusLine().getStatusCode());
       return response.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED;
     } catch (IOException e) {
       e.printStackTrace();
     }
 
     return false;
   }
 
   /**
    * Update the state of a Reading on the server
   * 
    * @param reading Reading to update on Readmill
    * @return
    */
   public boolean update(ReadmillReading reading) {
     Request request = Request.to(Endpoints.READINGS, reading.id);
 
     request.add(Params.Reading.STATE, reading.getState());
     request.add(Params.Reading.IS_PRIVATE, reading.isPrivate() ? 1 : 0);
 
     try {
       HttpResponse response = mWrapper.put(request);
       System.out.println("Reading UPDATE returned: " + response.getStatusLine().getStatusCode());
       return response.getStatusLine().getStatusCode() == HttpStatus.SC_OK;
     } catch (IOException e) {
       e.printStackTrace();
     }
 
     return false;
   }
 
   // Helper methods
 
   /**
    * Strip scheme and host part of URI.
    * This is necessary since the API uses relative URIs (it sets the host itself)
    * @param absoluteURI Absolute URI
    * @return relative URI
    */
   private String toResourceURI(String absoluteURI) {
     try {
       URI uri = new URI(absoluteURI);
       String result = String.format("%s%s%s", uri.getPath(), uri.getQuery(), uri.getFragment());
     } catch (Exception ignored) {
       // Ignored
     }
     return absoluteURI;
   }
 
   /**
    * Convert the given date to an isoDate suitable for passing to the API
   * 
    * @param date Date to convert
    * @return the given date in the ISO 8601 format
    */
   protected String toISODate(Date date) {
     iso8601Format.setTimeZone(utcTimeZone);
     return iso8601Format.format(date);
   }
 
 }

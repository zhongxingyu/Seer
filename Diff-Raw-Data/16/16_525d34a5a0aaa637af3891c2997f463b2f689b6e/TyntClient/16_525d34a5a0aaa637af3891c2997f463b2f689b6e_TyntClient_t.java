 package com.tynt.api.v1;
 
 import com.tynt.api.v1.model.Category;
 import com.tynt.api.v1.model.Images;
 import com.tynt.api.v1.model.Pages;
 import com.tynt.api.v1.model.TyntApiException;
 import com.tynt.api.v1.model.TyntAuthenticationException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.map.ObjectMapper;
 
 /**
  * The Tynt API Client.
  *
  * This class provides a single facade for utilizing the Tynt API.
  * You will need a Tynt application ID in order to access the live Tynt API.
  * Sign up as a Tynt Developer, and get an application ID here:
  * http://dev.tynt.com/
  *
  *
  * @author Bryan Sant <bryan@tynt.com>
  */
 public class TyntClient {
 
     public static final String API_VERSION = "v1";
     private static final String DEFAULT_TYNT_API_HOST = System.getProperty("tynt.api.host", "api.tynt.com");
     private static final String PAGES_TYPE = "/pages";
     private static final String IMAGES_TYPE = "/images";
     private static final String TERMS_TYPE = "/terms";
     private String tyntApiHost;
     private String topCategoriesUrl;
     private String appId;
     private ObjectMapper mapper = new ObjectMapper();
 
     /**
      * Constructs a new TyntClient with the given app ID.
      * 
      * @param tyntAppId Your Tynt application ID from dev.tynt.com.
      */
     public TyntClient(String tyntAppId) {
         this(DEFAULT_TYNT_API_HOST, tyntAppId);
     }
 
     /**
      * Constructs a new TyntClient with the given API host and app ID.
      *
      * @param tyntApiUrl The host name of the Tynt API service you would like to access.
      * @param tyntAppId Your Tynt application ID from dev.tynt.com.
      */
     public TyntClient(String tyntApiHost, String tyntAppId) {
         this.tyntApiHost = tyntApiHost;
         this.appId = tyntAppId;
 
         String urlPrefix = "http://" + tyntApiHost + "/" + API_VERSION;
         this.topCategoriesUrl = urlPrefix + "/top/";
     }
 
     /**
      * Retrieve a list of the top (most engaging) categories.
      *
      * @return A list of Tynt's top categories.
      * @throws TyntApiException If we fail to successfully return a list of categories.
      */
     public Collection<Category> getTopCategories() throws TyntApiException {
         List<Category> categories = new ArrayList<Category>();
         InputStream in = open(topCategoriesUrl);
         try {
             JsonNode rootNode = mapper.readValue(in, JsonNode.class);
             JsonNode categoriesNode = rootNode.get("categories");
             for (JsonNode categoryNode : categoriesNode) {
                 Category category = mapper.readValue(categoryNode, Category.class);
                 categories.add(category);
             }
             in.close();
         }
         catch (IOException ex) {
             throw new TyntApiException(-1, "Failed to return Tynt top categories", ex);
         }
         return categories;
     }
 
     /**
      * Retrieve the top (most engaging) pages for a given category.
      *
      * @param category The category to pull top pages for.
      * @return The top pages for the given category.
      * @throws TyntApiException If we fail to successfully return the top pages.
      */
     public Pages getTopPagesForCategory(Category category) throws TyntApiException {
         try {
             InputStream in = open(category.getUrl() + PAGES_TYPE);
             Pages pages = mapper.readValue(in, Pages.class);
             in.close();
             return pages;
         }
         catch (IOException ex) {
             throw new TyntApiException(-1, "Failed to return Tynt top pages", ex);
         }
     }
 
     /**
      * Retrieve the top (most engaging) pages for a given category name.
      *
      * @param categoryName The category name to pull top pages for.
      * @return The top pages for the given category.
      * @throws TyntApiException If we fail to successfully return the top pages.
      */
     public Pages getTopPagesForCategory(String categoryName) throws TyntApiException {
         return getTopPagesForCategory(new Category(null, categoryName, topCategoriesUrl + categoryName));
     }
 
     /**
      * Retrieve the top (most engaging) images for a given category.
      *
      * @param category The category to pull top images for.
      * @return The top images for the given category.
      * @throws TyntApiException If we fail to successfully return the top images.
      */
     public Images getTopImagesForCategory(Category category) throws TyntApiException {
         try {
             InputStream in = open(category.getUrl() + IMAGES_TYPE);
             Images images = mapper.readValue(in, Images.class);
             in.close();
             return images;
         }
         catch (IOException ex) {
             throw new TyntApiException(-1, "Failed to return Tynt top images", ex);
         }
     }
 
     /**
      * Retrieve the top (most engaging) images for a given category name.
      *
      * @param categoryName The category name to pull top images for.
      * @return The top images for the given category.
      * @throws TyntApiException If we fail to successfully return the top images.
      */
     public Images getTopImagesForCategory(String categoryName) throws TyntApiException {
         return getTopImagesForCategory(new Category(null, categoryName, topCategoriesUrl + categoryName));
     }
 
     /**
      * Retrieve the top (most engaging) search terms for a given category.
      *
      * @param category The category to pull top search terms for.
      * @return The top search terms for the given category.
      * @throws TyntApiException If we fail to successfully return the top search terms.
      */
     public Collection<String> getTopSearchTerms(Category category) throws TyntApiException {
         try {
             List<String> terms = new ArrayList<String>();
             InputStream in = open(category.getUrl() + TERMS_TYPE);
             JsonNode rootNode = mapper.readValue(in, JsonNode.class);
             JsonNode termsNode = rootNode.get("terms");
             for (JsonNode termNode : termsNode) {
                 terms.add(termNode.getTextValue());
             }
             in.close();
             return terms;
         }
         catch (IOException ex) {
             throw new TyntApiException(-1, "Failed to return Tynt top search terms", ex);
         }
     }
 
     /**
      * Retrieve the top (most engaging) search terms for a given category name.
      *
      * @param categoryName The category name to pull top search terms for.
      * @return The top search terms for the given category.
      * @throws TyntApiException If we fail to successfully return the top search terms.
      */
     public Collection<String> getTopSearchTerms(String categoryName) throws TyntApiException {
         return getTopSearchTerms(new Category(null, categoryName, topCategoriesUrl + categoryName));
     }
 
     /**
      * @return The Tynt API host name.
      */
     public String getTyntApiHost() {
         return tyntApiHost;
     }
 
     /**
      * @return The Tynt application ID used for API service access.
      */
     public String getAppId() {
         return appId;
     }
 
     private InputStream open(String url) throws TyntApiException {
         try {
             return open(new URL(url));
         }
         catch (MalformedURLException ex) {
             throw new TyntApiException(-1, "Invalid Tynt API URL", ex);
         }
     }
 
     private InputStream open(URL url) throws TyntApiException {
         //System.out.println("Opening Tynt API URL: " + url);
         InputStream in = null;
         try {
             HttpURLConnection conn = (HttpURLConnection) url.openConnection();
             conn.setRequestProperty("Cookie", "appid=" + appId);
             conn.connect();
             checkForError(conn);
             in = conn.getInputStream();
         }
         catch (IOException ex) {
             throw new TyntApiException(-1, "Failed to communicate with the Tynt API service", ex);
         }
         return in;
     }
 
     private void checkForError(HttpURLConnection conn) throws IOException, TyntApiException {
         int code = conn.getResponseCode();
         if (code >= HttpURLConnection.HTTP_BAD_REQUEST) {
             InputStream in = conn.getErrorStream();
             if (in != null) {
                 JsonNode node = mapper.readValue(in, JsonNode.class);
                 node = node.get("error");
                 String message = node.get("message").getTextValue();
 
                 if (code == HttpURLConnection.HTTP_UNAUTHORIZED) {
                     throw new TyntAuthenticationException(code, message);
                 }
                 else {
                     throw new TyntApiException(code, message);
                 }
             }
         }
     }
 }

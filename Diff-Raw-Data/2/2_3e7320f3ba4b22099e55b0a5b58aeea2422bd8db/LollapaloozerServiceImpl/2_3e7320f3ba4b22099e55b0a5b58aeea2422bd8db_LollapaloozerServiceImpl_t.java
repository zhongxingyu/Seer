 package com.ratethisfest.server.service;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 import com.ratethisfest.client.LollapaloozerService;
 import com.ratethisfest.server.domain.Rating;
 import com.ratethisfest.server.logic.EmailSender;
 import com.ratethisfest.server.logic.JSONUtils;
 import com.ratethisfest.server.logic.LollaRatingManager;
 import com.ratethisfest.server.logic.LollaSetDataLoader;
 import com.ratethisfest.shared.DayEnum;
 import com.ratethisfest.shared.FieldVerifier;
 import com.ratethisfest.shared.RatingGwt;
 import com.ratethisfest.shared.Set;
 
 /**
  * The server side implementation of the RPC service. Currently used for GWT
  * client.
  */
 @SuppressWarnings("serial")
 public class LollapaloozerServiceImpl extends RemoteServiceServlet implements LollapaloozerService {
   private static final Logger log = Logger.getLogger(LollapaloozerServiceImpl.class.getName());
 
   public String greetServer(String input) throws IllegalArgumentException {
     // Verify that the input is valid.
     if (!FieldVerifier.isValidName(input)) {
       // If the input is not valid, throw an IllegalArgumentException back to
       // the client.
       throw new IllegalArgumentException("Name must be at least 4 characters long");
     }
 
     String serverInfo = getServletContext().getServerInfo();
     String userAgent = getThreadLocalRequest().getHeader("User-Agent");
 
     // Escape data from the client to avoid cross-site script vulnerabilities.
     input = escapeHtml(input);
     userAgent = escapeHtml(userAgent);
 
     return "Hello, " + input + "!<br><br>I am running " + serverInfo
         + ".<br><br>It looks like you are using:<br>" + userAgent;
   }
 
   public List<Set> getSets(String yearString, String day) {
     List<Set> sets = null;
 
     Integer year = Integer.valueOf(yearString);
     if (day != null && !day.isEmpty()) {
       sets = LollaRatingManager.getInstance().findSetsByYearAndDay(year, DayEnum.fromValue(day));
     } else {
       sets = LollaRatingManager.getInstance().findSetsByYear(year);
     }
 
     return sets;
   }
 
   public String addRating(String email, Long setId, String score, String notes) {
 
     String resp = null;
 
     if (!FieldVerifier.isValidEmail(email)) {
       resp = FieldVerifier.EMAIL_ERROR;
     } else if (!FieldVerifier.isValidScore(score)) {
       resp = FieldVerifier.SCORE_ERROR;
     } else if (setId != null) {
       // TODO: implement GWT login auth!
      resp = LollaRatingManager.getInstance().addRatingBySetId(null, null, null, email, setId,
           Integer.valueOf(score), notes);
     } else {
       log.log(Level.WARNING, "addRatingBySetArtist: null args");
       resp = "null args";
     }
 
     return resp;
   }
 
   public List<RatingGwt> getRatingsByUserEmail(String email, Integer year) {
 
     List<RatingGwt> ratingGwts = null;
 
     if (email != null) {
       List<Rating> ratings = LollaRatingManager.getInstance().findRatingsByUserAndYear(email, year);
       if (ratings != null) {
         ratingGwts = JSONUtils.convertRatingsToRatingGwts(ratings);
       }
     }
 
     return ratingGwts;
   }
 
   public String deleteRating(Long ratingId) {
     String resp = "fail";
     if (ratingId != null) {
       LollaRatingManager.getInstance().deleteRatingById(ratingId);
       resp = "rating deleted";
     }
     return resp;
   }
 
   public String deleteRatingsByUser(String email) {
     String resp = "fail";
     LollaRatingManager.getInstance().deleteRatingsByUser(email);
     resp = "ratings deleted";
     return resp;
   }
 
   public String emailRatingsToUser(String email) {
     String resp = EmailSender.emailRatings(email);
     return resp;
   }
 
   public String insertSetData() {
     String success = "something happened";
     LollaSetDataLoader.getInstance().insertSetsFromApi(2012);
     success = "success i believe";
     return success;
   }
 
   public String updateSetData() {
     String success;
     String url;
     // TODO: move this to res file
     // url =
     // "http://ratethisfest.appspot.com/resources/sets_lolla_2012.txt";
     url = "http://127.0.0.1:8888/resources/sets_lolla_2012.txt";
     success = loadFile(url);
 
     return success;
   }
 
   public String updateSetFile() {
     String success = "something happened";
     // LollaSetDataLoader.getInstance().updateSetFestival();
     success = "success i believe";
     return success;
   }
 
   public String recalculateSetRatingAverages() {
     LollaSetDataLoader.getInstance().recalculateSetRatingAverages();
     return "success i believe";
   }
 
   private String loadFile(String url) {
     String success = "something happened";
     try {
       URL inputData = new URL(url);
       URLConnection urlConn = inputData.openConnection();
       InputStreamReader is = new InputStreamReader(urlConn.getInputStream(), "UTF8");
       BufferedReader in = new BufferedReader(is);
       // RatingManager.getInstance().deleteAllSets();
       LollaSetDataLoader.getInstance().updateSetsFromFile(in);
       success = "success i believe";
     } catch (Exception e) {
       success = "something happened";
       log.log(Level.WARNING, "loadFile: " + e.getMessage());
     } finally {
     }
     return success;
   }
 
   /**
    * Escape an html string. Escaping data received from the client helps to
    * prevent cross-site script vulnerabilities.
    * 
    * @param html
    *          the html string to escape
    * @return the escaped string
    */
   private String escapeHtml(String html) {
     if (html == null) {
       return null;
     }
     return html.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;");
   }
 }

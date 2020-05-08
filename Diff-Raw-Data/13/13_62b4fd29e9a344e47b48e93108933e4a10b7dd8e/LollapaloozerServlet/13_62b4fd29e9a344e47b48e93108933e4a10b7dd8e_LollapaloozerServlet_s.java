 package com.ratethisfest.server.service;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.json.JSONArray;
 
 import com.ratethisfest.server.domain.Rating;
 import com.ratethisfest.server.logic.JSONUtils;
 import com.ratethisfest.server.logic.LollaEmailSender;
 import com.ratethisfest.server.logic.LollaRatingManager;
 import com.ratethisfest.shared.DayEnum;
 import com.ratethisfest.shared.FieldVerifier;
 import com.ratethisfest.shared.HttpConstants;
 import com.ratethisfest.shared.Set;
 
 /**
  * HTTP Servlet, intended for Android/etc. device use.
  * 
  * @author Amy
  * 
  */
 @SuppressWarnings("serial")
 public class LollapaloozerServlet extends HttpServlet {
 
   /**
    * This method is accessible by visiting the URL
    * 
    * @param req
    * @param resp
    * @throws IOException
    */
   @Override
   public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
     String methodName = "doGet";
 
     resp.setContentType("text/plain");
 
     String action = checkNull(req.getParameter(HttpConstants.PARAM_ACTION));
     String authType = checkNull(req.getParameter(HttpConstants.PARAM_AUTH_TYPE));
     String authId = checkNull(req.getParameter(HttpConstants.PARAM_AUTH_ID));
     String authToken = checkNull(req.getParameter(HttpConstants.PARAM_AUTH_TOKEN));
     String email = checkNull(req.getParameter(HttpConstants.PARAM_EMAIL));
     String day = checkNull(req.getParameter(HttpConstants.PARAM_DAY));
     String year = checkNull(req.getParameter(HttpConstants.PARAM_YEAR));
 
     String respString = "";
 
     if (action.equals(HttpConstants.ACTION_GET_SETS)) {
       respString = getSetsJson(year, day);
     } else if (action.equals(HttpConstants.ACTION_GET_RATINGS)) {
       if (verifyToken(authType, authId, authToken)) {
         respString = getRatingsJsonByUser(authType, authId, authToken, email, year, day);
       }
     }
 
     resp.getWriter().println(respString);
   }
 
   /**
    * 
    * @param req
    * @param resp
    * @throws IOException
    */
   @Override
   public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
     String methodName = "doPost";
     PrintWriter out = resp.getWriter();
 
     out.println(methodName + " handling request for the following parameters:");
 
     // Value is not a string but rather an array of strings
     Map<String, String[]> parameterMap = req.getParameterMap();
     for (String s : parameterMap.keySet()) {
       out.println(s + " = " + parameterMap.get(s)[0]);
     }
 
     String action = checkNull(req.getParameter(HttpConstants.PARAM_ACTION));
     String authType = checkNull(req.getParameter(HttpConstants.PARAM_AUTH_TYPE));
     String authId = checkNull(req.getParameter(HttpConstants.PARAM_AUTH_ID));
     String authToken = checkNull(req.getParameter(HttpConstants.PARAM_AUTH_TOKEN));
     String email = checkNull(req.getParameter(HttpConstants.PARAM_EMAIL));
     String setId = checkNull(req.getParameter(HttpConstants.PARAM_SET_ID));
     String score = checkNull(req.getParameter(HttpConstants.PARAM_SCORE));
     String notes = checkNull(req.getParameter(HttpConstants.PARAM_NOTES));
     
     if (action.equals(HttpConstants.ACTION_ADD_RATING)) {
       out.println("Calling addRating()");
       if (verifyToken(authType, authId, authToken)) {
         addRating(authType, authId, authToken, email, setId, score, notes);
       }
     } else if (action.equals(HttpConstants.ACTION_EMAIL_RATINGS)) {
      out.println("Calling emailRatings(authType="+ authType +" authId="+ authId +" authToken="+ authToken +" email="+ email +")");
       if (verifyToken(authType, authId, authToken)) {
         String result = LollaEmailSender.emailRatings(authType, authId, authToken, email);
        out.println("Result: +result");
       } else {
         out.println("Request is refused because user account did not pass verification");
       }
     }  //end Email Ratings
     out.println("Done!");
     out.close();
   }
 
   private boolean verifyToken(String authType, String authId, String authToken) {
     // TODO: you may put the verification code here
     return true;
   }
 
   private String getSetsJson(String yearString, String day) {
     String resp = null;
 
     if (!FieldVerifier.isValidYear(yearString)) {
       resp = FieldVerifier.YEAR_ERROR;
     } else if (!FieldVerifier.isValidDay(day)) {
       resp = FieldVerifier.DAY_ERROR;
     } else {
       List<Set> sets = null;
 
       Integer year = Integer.valueOf(yearString);
       if (day != null && !day.isEmpty()) {
         sets = LollaRatingManager.getInstance().findSetsByYearAndDay(year, DayEnum.fromValue(day));
       } else {
         sets = LollaRatingManager.getInstance().findSetsByYear(year);
       }
 
       JSONArray jsonArray = JSONUtils.convertSetsToJSONArray(sets);
       if (jsonArray != null) {
         resp = jsonArray.toString();
       }
     }
 
     if (resp == null) {
       resp = "no data hrm";
     }
 
     return resp;
   }
 
   /**
    * 
    * @param email
    * @param year
    * @param day
    * @return
    */
   private String getRatingsJsonByUser(String authType, String authId, String authToken,
       String email, String year, String day) {
     String resp = null;
 
     List<Rating> ratings = null;
 
     if (authId != null) {
       if (!FieldVerifier.isValidYear(year)) {
         resp = FieldVerifier.YEAR_ERROR;
       } else if (!FieldVerifier.isValidDay(day)) {
         resp = FieldVerifier.DAY_ERROR;
       } else {
         ratings = LollaRatingManager.getInstance().findRatingsByUserYearAndDay(authType, authId,
             authToken, email, Integer.valueOf(year), DayEnum.fromValue(day));
       }
     }
 
     JSONArray jsonArray = JSONUtils.convertRatingsToJSONArray(ratings);
     if (jsonArray != null) {
       resp = jsonArray.toString();
     }
 
     return resp;
   }
 
   private String addRating(String authType, String authId, String authToken, String email,
       String setId, String score, String notes) {
 
     String resp = null;
     // if (!FieldVerifier.isValidEmail(email)) {
     // resp = FieldVerifier.EMAIL_ERROR;
     // }
     if (!FieldVerifier.isValidScore(score)) {
       resp = FieldVerifier.SCORE_ERROR;
     } else if (authId != null && setId != null) {
       resp = LollaRatingManager.getInstance().addRatingBySetId(authType, authId, authToken, email,
           Long.valueOf(setId), Integer.valueOf(score), notes);
     } else {
       resp = "null args";
     }
 
     return resp;
   }
 
   private String checkNull(String s) {
     if (s == null) {
       return "";
     }
     return s;
   }
 }

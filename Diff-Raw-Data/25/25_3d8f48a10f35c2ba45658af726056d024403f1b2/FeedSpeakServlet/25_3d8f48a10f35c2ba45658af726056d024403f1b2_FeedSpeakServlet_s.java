 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.hackeurope.feedspeak;
 
 import com.twilio.sdk.verbs.TwiMLException;
 import java.io.IOException;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 //Twilio imports
 import com.twilio.sdk.verbs.TwiMLResponse;
 import com.twilio.sdk.verbs.Verb;
 import java.util.HashMap;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author Calum
  */
 @WebServlet(name = "FeedSpeakServlet", urlPatterns = {"/"})
 public class FeedSpeakServlet extends HttpServlet {
 
     public static final String ACCOUNT_SID = "AC.....";
     public static final String AUTH_TOKEN = ".......";
 
     private HashMap<String, String> callers = new HashMap<String, String>();
     private TwiMLResponse twimlResponse;
     private String message;
         
     /**
      * Handles the HTTP
      * <code>GET</code> method.
      *
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
     }
 
     /**
      * Handles the HTTP
      * <code>POST</code> method.
      *
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         // Create a dict of people we know.
         //TODO: UserAccounts with mapping to source feed preferences in SQLite?
         callers.put("+447933298892", "Calum");
         callers.put("+447951751012", "Jack");
         //...
  
         //Get caller's number
        String fromNumber = request.getParameter("From");
         
         twimlResponse = new TwiMLResponse();
         
         formulateSayMessage();
         
         //Send response back to Twilio Server
         response.setContentType("application/xml");
         response.getWriter().print(twimlResponse.toXML());
 
     }
 
 
     private void formulateSayMessage() {
         String name;
        String knownCaller = callers.get(fromNumber); 
        if (knownCaller == null) {
             // Use a generic name - No user account
             name = "Monkey";
         } else {
             // Use the caller's name - Existing customer
            name = knownCaller;
         }
         
         Verb sayVerb = new Verb("Say", getUsersTweets()/*"Hey " + name + ", these are your personal feeds. I've got a longer message now.  I wonder how long I can make this message.  Am I still going?  This is crazy! Tested some punctuation as well."*/);
         //TODO: Other feeds...
         
         
         try {
             twimlResponse.append(sayVerb);
             //TODO: Other feeds...
         } 
         catch (TwiMLException ex) {
             System.err.println("Problem appending SAY verb(s) to TwiMLResponse");
         }
     }
     
    private String getUsersTweets()
     
        message = "Twitter feeds:";
         List<String> tweets = new YQL().getTweets();
         for (String tweet : tweets)
            message += tweet;
        return message;
     }
 
 }

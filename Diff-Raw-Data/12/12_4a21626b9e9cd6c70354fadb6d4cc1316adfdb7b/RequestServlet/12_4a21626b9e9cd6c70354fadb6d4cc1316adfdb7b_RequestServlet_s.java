 /**
  * Copyright (C) 2011 Daniel Kurashige-Gollub, daniel@kurashige-gollub.de
  * Please see the README file for details.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
  * in compliance with the License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software distributed under the License
  * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
  * or implied. See the License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package de.kurashigegollub.dev.gcatest;
 
 import com.google.api.client.auth.oauth2.draft10.AccessTokenErrorResponse;
 import de.kurashigegollub.com.google.calender.CalendarClient;
 import de.kurashigegollub.com.google.calender.CalendarCmdlineRequestInitializer;
 import com.google.api.client.auth.oauth2.draft10.AccessTokenResponse;
 import com.google.api.client.googleapis.auth.oauth2.draft10.GoogleAccessProtectedResource;
 import com.google.api.client.googleapis.auth.oauth2.draft10.GoogleAccessTokenRequest.GoogleAuthorizationCodeGrant;
 import com.google.api.client.http.HttpResponseException;
 import com.google.api.client.http.javanet.NetHttpTransport;
 import com.google.api.client.util.DateTime;
 import de.kurashigegollub.com.google.calender.CalendarFeed;
 import de.kurashigegollub.com.google.calender.EventFeed;
 import java.io.IOException;
 import java.io.PrintWriter;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 /**
  * Copyright by Daniel Kurashige-Gollub, 2011
  * @author Daniel Kurashige-Gollub, daniel@kurashige-gollub.de
  */
 public class RequestServlet extends BaseServlet {
 
     private static final Logger log = Logger.getLogger(RequestServlet.class.getSimpleName());
 
     /** 
      * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     @Override
     protected void process(HttpServletRequest request, HttpServletResponse response, HttpSession session)
             throws Exception {
 
         //This servlet will be called once the user permits our application to use part of his
         //google profile --> in other words: oauth2 was done. Whether or not it was successfull needs
         //to be checked now.    
         
         AppState appState = (AppState)session.getAttribute(APP_STATE);
         
         String state = (appState != null ? appState.getStateName() : null);
         String code  = request.getParameter("code");
         String error = request.getParameter("error");
         //If a calendarId is present, we need to show all the events from this particular 
         //calendar from now until +2 weeks.
         String calendarId = request.getParameter("calendarId");
         //If a calendar was selected the user can select an event and send it to one or more
         //mail addresses via his gmail account
         String eventId = request.getParameter("eventId");
         
         //we may get a state from the callback function after the user allows us to access his calendars
         //in this case we override our own state variable
         if (request.getParameter("state") != null)
             state = request.getParameter("state"); 
         appState = AppState.fromString(state);
         
         log.info(String.format("code:         %s", code));
         log.info(String.format("error:        %s", error));
         log.info(String.format("calendarId:   %s", calendarId));
         log.info(String.format("state:        %s", state));        
         log.info(String.format("state:        %s", appState));
         
         if (appState == AppState.LOGIN) {
             //Login should not happen here, but better be safe
             log.warning("We are in LOGIN state. Why?");
             response.sendRedirect(getRedirectUrlLogin(request));
             return;
         }
         
         //Check for an error -> user may not have granted us permission :-(
         if (!Utils.isEmpty(error)) {
             //session.setAttribute(ERROR, error);
             response.sendRedirect(String.format("%s/Error?%s=%s", request.getContextPath(), ERROR, error));
             return;
         }
         
         if (!Utils.isEmpty(code)) {
             session.setAttribute(ACCESS_CODE, code);            
         }
         else {
             code = (String)session.getAttribute(ACCESS_CODE);
         }
         
         if (Utils.isEmpty(code)) { 
             //TODO: proper session timeout handling!!!
             //      Check for timeout in a more secure way, not just this one code variable!!!
             //The session may have timed out, so the code variable may be empty -> if that is the case
             //we propably should display a "Session timed out, please login again" page.
            response.sendRedirect(getRedirectUrlLogin(request));           
             //response.sendRedirect(String.format("%s/Error?%s=%s", request.getContextPath(), ERROR, "no_access_code"));
             return;
         }
         
         response.setContentType("text/html;charset=UTF-8");
         PrintWriter out = response.getWriter();
         try {
             log.info("Requesting access to google calendar now");
             
             AppState nextState = AppState.LOGIN;
             
             String appTitle = appName;
 
             //Important: this url has to be the same every time we make this call! It is the one that
             //is saved in the Google API Console Access tab.
             String redirectUrl  = getRedirectUrlForGoogleCallback(request); 
             String backUrl      = redirectUrl;
 
             //String nextStateUrl = null;
             
             switch(appState) {
                 case CALENDAR_LIST: //this is the state
                     nextState = AppState.CALENDAR_ENTRIES;
                     appTitle += " - Calendar List";
                     //nextStateUrl = getRedirectUrlCalendarEvents(request, calendarId, nextState);
                     break;
                 case CALENDAR_ENTRIES:                    
                     appTitle += " - Calendar Entries";                
                     nextState = AppState.CALL_GMAIL; //TODO: check this
                     backUrl = getAppStateBaseUrl(request, AppState.CALENDAR_LIST);
                     //nextStateUrl = getRedirectUrlEventGmail(request, eventId, nextState);
                     //TODO: do the gmail thing
                     break;
             }
             
             //Check whether or not we still have access to the user's calendar.
             GoogleAccessProtectedResource accessProtectedResource = 
                 authorizeWithGoogle(request.getSession(), code, redirectUrl);
 
             //Prepare our own HTTP client with the necessary auth information, so we can access
             //the Google Calendar API now.
             CalendarClient client =  new CalendarClient(
                 new CalendarCmdlineRequestInitializer(accessProtectedResource).createRequestFactory());
             client.setPrettyPrint(true);
             client.setApplicationName(appName);
             
             out.println(createBasicHtmlHeader(request, appTitle));
             
             //Access the Google Calendar API and print the result as HTML.
             out.println(accessGoogleCalendar(client, appState, getAppStateBaseUrl(request, nextState), 
                                              backUrl, calendarId, eventId));
             
             out.println(createBasicHtmlFooter(request));
         } 
         catch (Exception ex) {
             if (ex instanceof HttpResponseException)
                 log.severe(((HttpResponseException)ex).getResponse().parseAsString());
             else
                 log.severe(ex.getMessage());
             out.println("<div class=\"error\">");
             out.println("<span class=\"bold\">Error during execution: </span>");
             out.println(ex.getMessage());
             out.println("<br><br>");
             out.println(Utils.getStackTraceAsString(ex));
             out.println("</div>");
             ex.printStackTrace();
         }
         finally {
             out.close();
         }
     }
    
     private String accessGoogleCalendar(CalendarClient client, AppState appState, String baseUrl, String backUrl, 
                                         String calendarId, String eventId)
     throws Exception {
         
         String html = "<h3>No Data Access Yet</h3>";
 
         DateTime dtnow    = Utils.getDateTimeNow();
         DateTime dt2weeks = Utils.getDateTime2Weeks(dtnow);
 
         GoogleCalendar gc = new GoogleCalendar(client);
         
         switch(appState) {
             case CALENDAR_LIST: {
                 StringBuilder sb = new StringBuilder();
                 sb.append("<h1>Calendar List</h1>\n");
                 
                 CalendarFeed cf = gc.listCalendarsAll();
                 if (cf.getEntries().isEmpty()) {
                     //empty calendar
                     sb.append("<div class=\"nodata\">No calendars found in your Google profile.</div>");
                 }
                 else {
                     sb.append(HtmlView.createListHtml(baseUrl, cf, dtnow, dt2weeks, backUrl));
                 }
                 html = sb.toString();
                 break;
             }
                 
             case CALENDAR_ENTRIES: {
                 StringBuilder sb = new StringBuilder();
                 sb.append("<h1>Calendar '").append(calendarId).append("' Events</h1>\n");             
                 
                 EventFeed ef = gc.listEventsForCalendar(calendarId, dtnow, dt2weeks);
                 if (ef.getEntries().isEmpty()) {
                     sb.append("<div class=\"nodata\">No entries found in this calendar for the time between ");
                     sb.append("<i>").append(dtnow.toStringRfc3339()).append("</i>");
                     sb.append(" and ");
                     sb.append("<i>").append(dt2weeks.toStringRfc3339()).append("</i>");
                     sb.append("</div>");
                 }
                 else {
                     sb.append("<div class=\"nodata\">Entries for the time between ");
                     sb.append("<i>").append(dtnow.toStringRfc3339()).append("</i>");
                     sb.append(" and ");
                     sb.append("<i>").append(dt2weeks.toStringRfc3339()).append("</i>");
                     sb.append("</div>");
                     sb.append(HtmlView.createListHtml(baseUrl, ef, dtnow, dt2weeks, backUrl));
                 }
                 html = sb.toString();
                 break;
             }
                 
             case CALL_GMAIL:
                 html = "TODO GMAIL"; //TODO: gmail
                 break;
         }
         
         return html;
     }
     
     //TODO: we should check the 'expires_in' value and see we need to check for a new access token again
     private GoogleAccessProtectedResource authorizeWithGoogle(final HttpSession session, String code, String redirectUrl) 
     throws Exception {
         log.info("authorizeWithGoogle");
         
         AccessTokenResponse response = exchangeCodeForAccessToken(code, redirectUrl);
                 
         //From the Google documentation: http://code.google.com/apis/accounts/docs/OAuth2WebServer.html
         //refresh_token: A token that may be used to obtain a new access token. Refresh tokens are valid until the user revokes
         //               access. This field is only present if access_type=offline is included in the authorization code request.
         //That means that we really don't need the refresh token at all, because this application is not in OFFLINE modus.
         String accessToken  = (String)session.getAttribute(ACCESS_TOKEN);
         String refreshToken = (String)session.getAttribute(REFRESH_TOKEN);
         
         if (response != null) { 
             //session.setAttribute(ACCESS_TOKEN, response.accessToken); //should be done in the onAccessToken method, see below!
             session.setAttribute(REFRESH_TOKEN, response.refreshToken);
             accessToken  = response.accessToken;
             refreshToken = response.refreshToken;
         }
         
         return new GoogleAccessProtectedResource(accessToken, 
                                                  Utils.getHttpTransport(), Utils.getJsonFactory(), 
                                                  clientId, clientSecret, refreshToken)
         {
             @Override
             protected void onAccessToken(String accessToken) {
                 //TODO: save accessToken to current session 
                 log.log(Level.INFO, "onAccessToken: {0}", accessToken);
                 session.setAttribute(ACCESS_TOKEN, accessToken);
             }
 
 //            @Override
 //            public boolean handleResponse(HttpRequest request, HttpResponse response, boolean retrySupported) {
 //                try {
 //                    log.info("handleResponse ++ : " + response.parseAsString());
 //                } catch (IOException ex) {
 //                    Logger.getLogger(RequestServlet.class.getName()).log(Level.SEVERE, null, ex);
 //                }
 //                return super.handleResponse(request, response, retrySupported);
 //            }
             
         };
     }
 
     private AccessTokenResponse exchangeCodeForAccessToken(String code, String redirectUrl)
     throws IOException {
         //log.log(Level.INFO, "exchangeCodeForAccessToken: {0}", code);
         //log.log(Level.INFO, "exchangeCodeForAccessToken: {0}", redirectUrl);
         try {
             //exchange the current code (auth code from when the user permitted our app to access his profile)
             //for an access token --> http://code.google.com/apis/accounts/docs/OAuth2WebServer.html
             
             //If you contact Google for an OAuth2 token too quickly (ie. before the previous token expires),
             //they will return an error:invalid_grant. 
             
             return new GoogleAuthorizationCodeGrant(new NetHttpTransport(), Utils.getJsonFactory(), clientId, 
                                                     clientSecret, code, redirectUrl).execute();
         } catch (HttpResponseException ex) {
             AccessTokenErrorResponse response = ex.getResponse().parseAs(AccessTokenErrorResponse.class);            
             //Were we asking too frequent and the tokens are still valid? Yes, if the error is "invalid_grant".
             if ("invalid_grant".equalsIgnoreCase(response.error)) {
                 log.warning("We got an 'invalid_grant' error, which usually means we are asking the server too fast and the tokens are still valid.");
                 return null;
             }
             else {
                 log.log(Level.SEVERE, "Error: {0}", response.error);
                 throw ex;
             }
         }
     }
 
     // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
     /** 
      * Handles the HTTP <code>GET</code> method.
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         processRequest(request, response);
     }
 
     /** 
      * Handles the HTTP <code>POST</code> method.
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         processRequest(request, response);
     }
 
     /** 
      * Returns a short description of the servlet.
      * @return a String containing servlet description
      */
     @Override
     public String getServletInfo() {
         return "Short description";
     }// </editor-fold>
 
     
    
 
 }

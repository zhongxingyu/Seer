 package com.google.wallet.objects.servlets;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.security.GeneralSecurityException;
 import java.security.KeyStoreException;
 import java.security.SignatureException;
 import java.util.logging.Logger;
 
 import javax.servlet.ServletContext;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.google.api.services.walletobjects.model.LoyaltyObject;
 import com.google.gson.Gson;
 import com.google.gson.JsonIOException;
 import com.google.gson.JsonSyntaxException;
 import com.google.wallet.objects.utils.WobCredentials;
 import com.google.wallet.objects.utils.WobUtils;
 import com.google.wallet.objects.verticals.Loyalty;
 import com.google.wallet.objects.webservice.WebserviceRequest;
 import com.google.wallet.objects.webservice.WebserviceResponse;
 
 /**
  * This servlet handles Webservice API requests and responds with an approved status and the object to insert.  To test this functionality -
  * upload this to your appengine instance, then in your merchant dashboard create a discoverable with the appengine app domain and /webservice as the path.
  * You can also use this service to debug since it logs all requests.
  *
  * To change the response to rejection, comment out the success response and uncomment the rejection portion.
  *
  * @author pying
  *
  */
 public class WobWebserviceHandlerServlet extends HttpServlet {
 
   private static final long serialVersionUID = 1842327434636112548L;
   private static final Logger logger =
       Logger.getLogger(WobWebserviceHandlerServlet.class.getSimpleName());
 
   public void doGet(HttpServletRequest req, HttpServletResponse resp) {
     doPost(req, resp);
   }
 
   public void doPost(HttpServletRequest req, HttpServletResponse resp) {
     Gson gson = new Gson();
 
     // Access credentials from web.xml
     ServletContext context = getServletContext();
 
     // Create the WobCredential object to use
     WobCredentials credentials = new WobCredentials(
         context.getInitParameter("ServiceAccountId"),
         context.getInitParameter("ServiceAccountPrivateKey"),
         context.getInitParameter("ApplicationName"),
         context.getInitParameter("IssuerId"));
 
     // Create WobUilts object to handle the heavy lifting
     WobUtils utils = null;
     WebserviceRequest webRequest = null;
     WebserviceResponse webResponse = null;
     PrintWriter out = null;
     String jwt = null;
 
     // Read request and log it
     try {
       webRequest = gson.fromJson(req.getReader(), WebserviceRequest.class);
       logger.info(gson.toJson(webRequest));
       utils = new WobUtils(credentials);
       out = resp.getWriter();
     } catch (FileNotFoundException e) {
       // Add code to catch errors
       e.printStackTrace();
     } catch (KeyStoreException e) {
       // Add code to catch errors
       e.printStackTrace();
     } catch (IOException e) {
       // Add code to catch errors
       e.printStackTrace();
     } catch (GeneralSecurityException e) {
       // Add code to catch errors
       e.printStackTrace();
     } catch (JsonSyntaxException e) {
       e.printStackTrace();
     } catch (JsonIOException e) {
       e.printStackTrace();
     }
 
     // Handle signup and linking
     webResponse = new WebserviceResponse(WebserviceResponse.Response.approved);
 
     String linkId = webRequest.getParams().getLinkingId();
     LoyaltyObject loyaltyObject = Loyalty.generateLoyaltyObject(utils
        .getIssuerId(), "LoyaltyClass", (linkId != null) ? linkId
        : "LoyaltyObject");
 
     // Create the response JWT
     try {
       jwt = utils.generateWebserviceResponseJwt(loyaltyObject, webResponse);
     } catch (SignatureException e) {
       e.printStackTrace();
     }
 
     /*  For rejected sign-up/linking
     webResponse =
         new WebserviceResponse("An descriptive error message", WebserviceResponse.Response.rejected);
     try {
       jwt =
           utils.generateWebserviceFailureResponseJwt(webResponse);
     } catch (SignatureException e) {
       e.printStackTrace();
     }*/
 
     out.write(jwt);
   }
 }

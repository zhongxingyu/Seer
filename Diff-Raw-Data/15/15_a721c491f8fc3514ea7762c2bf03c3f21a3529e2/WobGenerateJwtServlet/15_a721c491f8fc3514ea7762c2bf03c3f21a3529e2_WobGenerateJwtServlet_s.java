 package com.google.wallet.objects.servlets;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.security.GeneralSecurityException;
 import java.security.KeyStoreException;
 import java.security.SignatureException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.ServletContext;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.google.wallet.objects.utils.WobCredentials;
 import com.google.wallet.objects.utils.WobPayload;
 import com.google.wallet.objects.utils.WobUtils;
 import com.google.wallet.objects.verticals.BoardingPass;
 import com.google.wallet.objects.verticals.Generic;
 import com.google.wallet.objects.verticals.Loyalty;
 import com.google.wallet.objects.verticals.Offer;
 
 /**
  * This servlet generates Save to Wallet JWTs based on the type URL parameter in
  * the request. Loyalty, Offer, and Generic only contain the Object. Boarding
  * pass contains 2 Classes and 2 Objects representing the a multi leg fight.
  * Credentials are stored in web.xml which is why it needs ServletContext.
  *
  * @author pying
  *
  */
 @SuppressWarnings("serial")
 public class WobGenerateJwtServlet extends HttpServlet {
   public void doGet(HttpServletRequest req, HttpServletResponse resp)
       throws IOException {
     // Access credentials from web.xml
     ServletContext context = getServletContext();
 
     // Create a credentials object
     WobCredentials credentials = new WobCredentials(
         context.getInitParameter("ServiceAccountId"),
         context.getInitParameter("ServiceAccountPrivateKey"),
         context.getInitParameter("ApplicationName"),
         context.getInitParameter("IssuerId"));
 
     WobUtils utils = null;
 
     // Instantiate the WobUtils class which contains lots of handy functions
     try {
       utils = new WobUtils(credentials);
     } catch (FileNotFoundException e) {
       // Add code to handle errors
       e.printStackTrace();
     } catch (KeyStoreException e) {
       // Add code to handle errors
       e.printStackTrace();
     } catch (IOException e) {
       // Add code to catch errors
       e.printStackTrace();
     } catch (GeneralSecurityException e) {
       // Add code to handle errors
       e.printStackTrace();
     }
 
     // Get type of JWT to generate
     String type = req.getParameter("type");
 
     // Add valid domains for the Save to Wallet button
     List<String> origins = new ArrayList<String>();
     origins.add("http://localhost:8888");
     origins.add("https://localhost:8888");
     origins.add("http://wobs-quickstart.appspot.com");
     origins.add("https://wobs-quickstart.appspot.com");
 
     WobPayload payload = new WobPayload();
 
     // Create the appropriate Object/Classes
     if (type.equals("loyalty")) {
       payload.addObject(Loyalty.generateLoyaltyObject(utils.getIssuerId(),
          "LoyaltyClass", "LoyaltyObject"));
     } else if (type.equals("offer")) {
       payload.addObject(Offer.generateOfferObject(utils.getIssuerId(),
           "OfferClass", "OfferObject"));
     } else if (type.equals("generic")) {
       payload.addObject(Generic.generateGenericObject(utils.getIssuerId(),
           "GenericClass", "GenericObject"));
     } else if (type.equals("boardingpass")) {
       payload.addObject(BoardingPass.generateBoardingPassClass(
           utils.getIssuerId(), "BoardingPassClassFirstLeg"));
       payload.addObject(BoardingPass.generateBoardingPassObject(
           utils.getIssuerId(), "BoardingPassClassFirstLeg",
           "BoardingPassObjectFirstLeg"));
 
       payload.addObject(BoardingPass.generateBoardingPassClass(
           utils.getIssuerId(), "BoardingPassClassSecondLeg"));
       payload.addObject(BoardingPass.generateBoardingPassObject(
           utils.getIssuerId(), "BoardingPassClassSecondLeg",
           "BoardingPassObjectSecondLeg"));
     }
 
     // Convert the object into a Save to Wallet Jwt
     String jwt = null;
     try {
       jwt = utils.generateSaveJwt(payload, origins);
     } catch (SignatureException e) {
       e.printStackTrace();
     }
 
     // Respond with JWT
     PrintWriter out = resp.getWriter();
     out.write(jwt);
   }
 
 }

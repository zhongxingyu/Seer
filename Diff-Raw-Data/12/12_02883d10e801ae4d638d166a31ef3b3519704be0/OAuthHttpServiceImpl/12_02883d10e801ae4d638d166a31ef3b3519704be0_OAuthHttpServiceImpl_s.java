 package uk.ac.ox.oucs.oauth.service;
 
 import net.oauth.*;
 import net.oauth.server.OAuthServlet;
 import uk.ac.ox.oucs.oauth.domain.Accessor;
 import uk.ac.ox.oucs.oauth.domain.Consumer;
 
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.net.URISyntaxException;
 import java.util.List;
 
 /**
  * @author Colin Hebert
  */
 public class OAuthHttpServiceImpl implements OAuthHttpService {
     private OAuthService oAuthService;
     private OAuthValidator oAuthValidator;
 
     public void setoAuthValidator(OAuthValidator oAuthValidator) {
         this.oAuthValidator = oAuthValidator;
     }
 
     public void setoAuthService(OAuthService oAuthService) {
         this.oAuthService = oAuthService;
     }
 
     @Override
     public boolean isValidOAuthRequest(HttpServletRequest request) throws IOException {
         try {
             OAuthMessage message = OAuthServlet.getMessage(request, null);
             OAuthConsumer oAuthConsumer = Util.convertToOAuthConsumer(oAuthService.getConsumer(message.getConsumerKey()));
             String token = message.getToken();
             OAuthAccessor oAuthAccessor;
             if (token != null)
                 oAuthAccessor = Util.convertToOAuthAccessor(oAuthService.getAccessor(message.getToken(), Accessor.Type.ACCESS), oAuthConsumer);
             else
                 oAuthAccessor = new OAuthAccessor(oAuthConsumer);
 
             oAuthValidator.validateMessage(message, oAuthAccessor);
         } catch (OAuthException e) {
             //TODO: Handle exceptions in a better way
             return false;
         } catch (URISyntaxException e) {
             //TODO: Handle exceptions in a better way
             return false;
         }
         return true;
     }
 
     @Override
     public String getOAuthAccessToken(HttpServletRequest request) throws IOException {
         OAuthMessage message = OAuthServlet.getMessage(request, null);
         return message.getToken();
     }
 
     @Override
     public void handleRequestToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
         try {
             OAuthMessage oAuthMessage = OAuthServlet.getMessage(request, null);
             String secret = oAuthMessage.getParameter(OAuthConsumer.ACCESSOR_SECRET);
             String callback = oAuthMessage.getParameter(OAuth.OAUTH_CALLBACK);

            Consumer consumer = oAuthService.getConsumer(oAuthMessage.getConsumerKey());
             Accessor accessor = oAuthService.createRequestAccessor(consumer.getId(), secret, callback);
            OAuthConsumer oAuthConsumer = Util.convertToOAuthConsumer(consumer);
             OAuthAccessor oAuthAccessor = Util.convertToOAuthAccessor(accessor, oAuthConsumer);
 
            oAuthValidator.validateMessage(oAuthMessage, oAuthAccessor);

             sendOAuthResponse(response, OAuth.newList(
                     OAuth.OAUTH_TOKEN, oAuthAccessor.requestToken,
                     OAuth.OAUTH_TOKEN_SECRET, oAuthAccessor.tokenSecret,
                     OAuth.OAUTH_CALLBACK_CONFIRMED, "true"));
         } catch (OAuthException e) {
             //TODO: Handle exceptions in a better way
         } catch (URISyntaxException e) {
             //TODO: Handle exceptions in a better way
         }
     }
 
     @Override
     public void handleGetAccessToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
         try {
             OAuthMessage oAuthMessage = OAuthServlet.getMessage(request, null);
             Accessor requestAccessor = oAuthService.getAccessor(oAuthMessage.getToken(), Accessor.Type.ACCESS);
             Consumer consumer = oAuthService.getConsumer(requestAccessor.getConsumerId());
             OAuthConsumer oAuthConsumer = Util.convertToOAuthConsumer(consumer);
             OAuthAccessor oAuthAccessor = Util.convertToOAuthAccessor(requestAccessor, oAuthConsumer);
             oAuthValidator.validateMessage(oAuthMessage, oAuthAccessor);
 
             Accessor accessAccessor = oAuthService.createAccessAccessor(requestAccessor.getToken());
             sendOAuthResponse(response, OAuth.newList(
                     OAuth.OAUTH_TOKEN, accessAccessor.getToken(),
                     OAuth.OAUTH_TOKEN_SECRET, accessAccessor.getSecret()));
         } catch (OAuthException e) {
             //TODO: Handle exceptions in a better way
         } catch (URISyntaxException e) {
             //TODO: Handle exceptions in a better way
         }
     }
 
     @Override
     public void handleRequestAuthorisation(HttpServletRequest request, HttpServletResponse response, boolean authorised, String token, String verifier, String userId) throws IOException {
         Accessor accessor = oAuthService.getAccessor(token, Accessor.Type.REQUEST_AUTHORISING);
         Consumer consumer = oAuthService.getConsumer(accessor.getConsumerId());
         if (authorised) {
             accessor = oAuthService.authoriseToken(accessor.getToken(), verifier, userId);
             if (accessor.getCallbackUrl().equals(OAuthService.OUT_OF_BAND_CALLBACK)) {
                 response.setContentType("text/plain");
                 PrintWriter out = response.getWriter();
                 out.println("You have successfully authorized '" + consumer.getName() + "'.\n" +
                         "The authorisation token is: " + accessor.getToken() + "\n" +
                         "Please close this browser window and click continue in the client.");
                 out.flush();
                 out.close();
             } else {
                 String callbackUrl = OAuth.addParameters(accessor.getCallbackUrl(),
                         OAuth.OAUTH_TOKEN, accessor.getToken(),
                         OAuth.OAUTH_VERIFIER, OAuth.OAUTH_VERIFIER);
 
                 response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
                 response.setHeader("Location", callbackUrl);
             }
         } else {
             if (accessor.getCallbackUrl().equals(OAuthService.OUT_OF_BAND_CALLBACK)) {
                 response.setContentType("text/plain");
                 PrintWriter out = response.getWriter();
                 out.println("You have not  authorized '" + consumer.getName() + "'.\n" +
                         "Please close this browser window and click continue in the client.");
                 out.flush();
                 out.close();
             } else {
                 response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
                 response.setHeader("Location", accessor.getCallbackUrl());
             }
 
         }
     }
 
     /**
      * Sends a response respecting the OAuth format
      * <p>
      * The content type of the response is "application/x-www-form-urlencoded"
      * </p>
      *
      * @param response   HttpServletResponse used to send the response
      * @param parameters List of parameters in the form of key/value
      * @throws IOException
      */
     private static void sendOAuthResponse(HttpServletResponse response, List<OAuth.Parameter> parameters) throws IOException {
         response.setContentType(OAuth.FORM_ENCODED);
         ServletOutputStream os = response.getOutputStream();
         OAuth.formEncode(parameters, os);
         os.flush();
         os.close();
     }
 }

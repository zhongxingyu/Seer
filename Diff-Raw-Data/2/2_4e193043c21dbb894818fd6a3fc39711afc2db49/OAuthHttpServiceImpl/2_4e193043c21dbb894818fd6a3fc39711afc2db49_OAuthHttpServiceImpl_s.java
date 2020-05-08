 package uk.ac.ox.oucs.oauth.service;
 
 import net.oauth.*;
 import net.oauth.OAuthException;
 import net.oauth.server.OAuthServlet;
 import uk.ac.ox.oucs.oauth.domain.Accessor;
 import uk.ac.ox.oucs.oauth.domain.Consumer;
 import uk.ac.ox.oucs.oauth.exception.*;
 
 import javax.servlet.ServletException;
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
     public boolean isValidOAuthRequest(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
         try {
             OAuthMessage message = OAuthServlet.getMessage(request, null);
             //Non existing token, just continue
             if (message.getToken() == null)
                 return false;
             OAuthConsumer oAuthConsumer = Util.convertToOAuthConsumer(oAuthService.getConsumer(message.getConsumerKey()));
             OAuthAccessor oAuthAccessor = Util.convertToOAuthAccessor(oAuthService.getAccessor(message.getToken(), Accessor.Type.ACCESS), oAuthConsumer);
             oAuthValidator.validateMessage(message, oAuthAccessor);
         } catch (uk.ac.ox.oucs.oauth.exception.OAuthException e) {
             handleException(convertException(e), request, response, true);
         } catch (OAuthException e) {
             handleException(new OAuthProblemException(), request, response, true);
         } catch (URISyntaxException e) {
             handleException(e, request, response, true);
         }
         return true;
     }
 
     @Override
     public String getOAuthAccessToken(HttpServletRequest request) throws IOException {
         OAuthMessage message = OAuthServlet.getMessage(request, null);
         return message.getToken();
     }
 
     @Override
     public void handleRequestToken(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
         try {
             OAuthMessage oAuthMessage = OAuthServlet.getMessage(request, null);
             Consumer consumer = oAuthService.getConsumer(oAuthMessage.getConsumerKey());
             OAuthConsumer oAuthConsumer = Util.convertToOAuthConsumer(consumer);
             oAuthValidator.validateMessage(oAuthMessage, new OAuthAccessor(oAuthConsumer));
 
             String callback = oAuthMessage.getParameter(OAuth.OAUTH_CALLBACK);
             Accessor accessor = oAuthService.createRequestAccessor(consumer.getId(), callback);
             OAuthAccessor oAuthAccessor = Util.convertToOAuthAccessor(accessor, oAuthConsumer);
 
             sendOAuthResponse(response, OAuth.newList(
                     OAuth.OAUTH_TOKEN, oAuthAccessor.requestToken,
                     OAuth.OAUTH_TOKEN_SECRET, oAuthAccessor.tokenSecret,
                     OAuth.OAUTH_CALLBACK_CONFIRMED, "true"));
         } catch (uk.ac.ox.oucs.oauth.exception.OAuthException e) {
             handleException(convertException(e), request, response, true);
         } catch (OAuthException e) {
             handleException(new OAuthProblemException(), request, response, true);
         } catch (URISyntaxException e) {
             handleException(e, request, response, true);
         }
     }
 
     @Override
     public void handleGetAccessToken(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
         try {
             OAuthMessage oAuthMessage = OAuthServlet.getMessage(request, null);
             Accessor requestAccessor = oAuthService.getAccessor(oAuthMessage.getToken(), Accessor.Type.REQUEST_AUTHORISED);
             Consumer consumer = oAuthService.getConsumer(requestAccessor.getConsumerId());
             OAuthConsumer oAuthConsumer = Util.convertToOAuthConsumer(consumer);
             OAuthAccessor oAuthAccessor = Util.convertToOAuthAccessor(requestAccessor, oAuthConsumer);
             oAuthValidator.validateMessage(oAuthMessage, oAuthAccessor);
             if (requestAccessor.getVerifier() != null && !requestAccessor.getVerifier().equals(oAuthMessage.getParameter(OAuth.OAUTH_VERIFIER)))
                 throw new InvalidVerifierException();
 
             String secret = oAuthMessage.getParameter(OAuthConsumer.ACCESSOR_SECRET);
             Accessor accessAccessor = oAuthService.createAccessAccessor(requestAccessor.getToken(), secret);
             sendOAuthResponse(response, OAuth.newList(
                     OAuth.OAUTH_TOKEN, accessAccessor.getToken(),
                     OAuth.OAUTH_TOKEN_SECRET, accessAccessor.getSecret()));
         } catch (uk.ac.ox.oucs.oauth.exception.OAuthException e) {
             handleException(convertException(e), request, response, true);
         } catch (OAuthException e) {
             handleException(new OAuthProblemException(), request, response, true);
         } catch (URISyntaxException e) {
             handleException(e, request, response, true);
         }
     }
 
     @Override
     public void handleRequestAuthorisation(HttpServletRequest request, HttpServletResponse response, boolean authorised, String token, String verifier, String userId) throws IOException, ServletException {
         try {
             Accessor accessor = oAuthService.getAccessor(token, Accessor.Type.REQUEST_AUTHORISING);
             Consumer consumer = oAuthService.getConsumer(accessor.getConsumerId());
             if (authorised) {
                 accessor = oAuthService.authoriseAccessor(accessor.getToken(), verifier, userId);
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
                             OAuth.OAUTH_VERIFIER, accessor.getVerifier());
 
                     response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
                     response.setHeader("Location", callbackUrl);
                 }
             } else {
                 oAuthService.denyRequestAccessor(accessor.getToken());
                 if (accessor.getCallbackUrl().equals(OAuthService.OUT_OF_BAND_CALLBACK)) {
                     response.setContentType("text/plain");
                     PrintWriter out = response.getWriter();
                     out.println("You have not  authorized '" + consumer.getName() + "'.\n" +
                             "Please close this browser window and click continue in the client.");
                     out.flush();
                     out.close();
                 } else {
                    response.setStatus(HttpServletResponse.TEMPORARY_REDIRECT);
                     response.setHeader("Location", accessor.getCallbackUrl());
                 }
 
             }
         } catch (uk.ac.ox.oucs.oauth.exception.OAuthException e) {
             handleException(convertException(e), request, response, true);
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
 
     private static void handleException(Exception e, HttpServletRequest request,
                                         HttpServletResponse response, boolean sendBody)
             throws IOException, ServletException {
         String realm = (request.isSecure()) ? "https://" : "http://";
         realm += request.getLocalName();
         OAuthServlet.handleException(response, e, realm, sendBody);
     }
 
     private static OAuthException convertException(uk.ac.ox.oucs.oauth.exception.OAuthException originalException) {
         if (originalException instanceof InvalidConsumerException)
             return new OAuthProblemException(OAuth.Problems.CONSUMER_KEY_UNKNOWN);
         else if (originalException instanceof ExpiredAccessorException)
             return new OAuthProblemException(OAuth.Problems.TOKEN_EXPIRED);
         else if (originalException instanceof RevokedAccessorException)
             return new OAuthProblemException(OAuth.Problems.TOKEN_REVOKED);
         else if (originalException instanceof InvalidAccessorException)
             return new OAuthProblemException(OAuth.Problems.TOKEN_REJECTED);
         else if (originalException instanceof InvalidVerifierException)
             return new OAuthProblemException(OAuth.Problems.PARAMETER_REJECTED);
         else
             return new OAuthProblemException();
     }
 }

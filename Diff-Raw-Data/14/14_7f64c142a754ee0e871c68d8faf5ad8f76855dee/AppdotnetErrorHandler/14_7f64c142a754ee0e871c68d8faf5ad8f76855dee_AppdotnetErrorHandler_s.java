 package org.springframework.social.appdotnet.api.impl.error;
 
 import org.codehaus.jackson.JsonFactory;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.client.ClientHttpResponse;
 import org.springframework.social.*;
 import org.springframework.web.client.DefaultResponseErrorHandler;
 
 import java.io.IOException;
 
 import static org.springframework.http.HttpStatus.Series.CLIENT_ERROR;
 import static org.springframework.http.HttpStatus.Series.SERVER_ERROR;
 
 /**
  * Subclass of {@link DefaultResponseErrorHandler} that handles errors from Appdotnet API,
  * interpreting them into appropriate exceptions.
  *
  * @author Arik Galansky
  */
 public class AppdotnetErrorHandler extends DefaultResponseErrorHandler {
 
     @Override
     public void handleError(ClientHttpResponse response) throws IOException {
         HttpStatus statusCode = response.getStatusCode();
         if (statusCode.series() == SERVER_ERROR) {
             handleServerErrors(statusCode);
         } else if (statusCode.series() == CLIENT_ERROR) {
             handleClientErrors(response);
         }
 
         // if not otherwise handled, do default handling and wrap with UncategorizedApiException
         try {
             super.handleError(response);
         } catch (Exception e) {
             throw new UncategorizedApiException("Error consuming App.net REST API", e);
         }
     }
 
     private void handleClientErrors(ClientHttpResponse response) throws IOException {
         HttpStatus statusCode = response.getStatusCode();
         AppdotnetErrorMeta errorMeta = extractErrorDetailsFromResponse(response);
         if (errorMeta == null) {
             return; // unexpected error body, can't be handled here
         }
 
         if (statusCode == HttpStatus.NOT_FOUND) {
             throw new ResourceNotFoundException(constructErrorMessage(errorMeta));
         }
         if ("invalid-token".equals(errorMeta.getErrorSlug())) {
             throw new InvalidAuthorizationException(constructErrorMessage(errorMeta));
         }
         if ("not-authorized".equals(errorMeta.getErrorSlug())) {
             throw new NotAuthorizedException(constructErrorMessage(errorMeta));
         }
         if ("token-expired".equals(errorMeta.getErrorSlug())) {
             throw new ExpiredAuthorizationException();
         }
         if ("code-used".equals(errorMeta.getErrorSlug())) {
             throw new InvalidAuthorizationException(constructErrorMessage(errorMeta));
         }
         if ("redirect-uri-required".equals(errorMeta.getErrorSlug())) {
             throw new InvalidAuthorizationException(constructErrorMessage(errorMeta));
         }
     }
 
     private String constructErrorMessage(AppdotnetErrorMeta errorMeta) {
         StringBuilder sb = new StringBuilder();
         if (errorMeta.getErrorSlug() != null) {
             sb.append(errorMeta.getErrorSlug()).append(": ");
         }
         if (errorMeta.getErrorMessage() != null) {
             sb.append(errorMeta.getErrorMessage());
         }
         return sb.toString();
     }
 
     private void handleServerErrors(HttpStatus statusCode) throws IOException {
         if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR) {
             throw new InternalServerErrorException("Something is broken at App.net");
         } else if (statusCode == HttpStatus.BAD_GATEWAY) {
             throw new ServerDownException("App.net is down or is being upgraded.");
         } else if (statusCode == HttpStatus.SERVICE_UNAVAILABLE) {
             throw new ServerOverloadedException("App.net is overloaded with requests. Try again later.");
         }
     }
 
     private AppdotnetErrorMeta extractErrorDetailsFromResponse(ClientHttpResponse response) throws IOException {
         ObjectMapper mapper = new ObjectMapper(new JsonFactory());
         AppdotnetErrorResponse errorResponse = mapper.readValue(response.getBody(), AppdotnetErrorResponse.class);
         if (errorResponse != null) {
             return errorResponse.getMeta();
         }
 
         return null;
     }
 }

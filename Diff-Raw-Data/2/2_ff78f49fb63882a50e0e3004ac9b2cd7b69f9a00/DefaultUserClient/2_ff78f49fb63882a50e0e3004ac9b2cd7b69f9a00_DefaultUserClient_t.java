 package pl.akiba.wsclient.client;
 
 import java.io.IOException;
 
 import org.eclipse.jetty.client.ContentExchange;
 import org.eclipse.jetty.client.HttpClient;
 import org.eclipse.jetty.client.HttpExchange;
 
 import pl.akiba.model.entities.FacebookUser;
 import pl.akiba.model.exception.MethodFailureStatusException;
 import pl.akiba.model.exception.StatusException;
 
 /**
  * 
  * @author sobczakt
  */
 public class DefaultUserClient extends DefaultClient implements UserClient {
 
     public DefaultUserClient(String address, HttpClient httpClient) {
         super(address, httpClient);
     }
 
     @Override
     public FacebookUser getFacebookUser(long facebookId) throws StatusException, IOException, InterruptedException {
         StringBuilder urlBuilder = new StringBuilder(address);
        urlBuilder.append("/user/fb/").append(facebookId);
 
         ContentExchange exchange = prepareExchange(HttpMethod.GET, urlBuilder.toString());
         httpClient.send(exchange);
         int exchangeStatus = exchange.waitForDone();
 
         if (exchangeStatus == HttpExchange.STATUS_COMPLETED) {
             int responseStatus = exchange.getResponseStatus();
             switch (responseStatus) {
                 case 200:
                     return mapper.readValue(exchange.getResponseContent(), FacebookUser.class);
                 case 420:
                     throw new MethodFailureStatusException("Http response returns METHOD FAILURE (420) status");
                 default:
                     throw new StatusException("Http response returns unknown (" + responseStatus + ") status");
             }
         }
 
         throw new StatusException("Http exchange returns status: " + getExchangeStatusName(exchangeStatus));
     }
 
 }

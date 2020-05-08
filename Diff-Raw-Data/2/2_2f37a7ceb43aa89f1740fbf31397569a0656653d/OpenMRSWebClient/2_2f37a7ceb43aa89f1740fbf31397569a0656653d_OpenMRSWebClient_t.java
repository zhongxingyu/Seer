 package org.bahmni.feed.openerp.client;
 
 import org.bahmni.feed.openerp.OpenERPAtomFeedProperties;
 import org.bahmni.webclients.ConnectionDetails;
 import org.bahmni.webclients.HttpClient;
 import org.bahmni.webclients.openmrs.OpenMRSLoginAuthenticator;
 
 import java.net.URI;
 
 public class OpenMRSWebClient{
 
     private static HttpClient httpClient;
 
     public OpenMRSWebClient(OpenERPAtomFeedProperties properties) {
         httpClient = new HttpClient(connectionDetails(properties), new OpenMRSLoginAuthenticator(connectionDetails(properties)));
     }
 
     public String get(URI uri) {
         return httpClient.get(uri);
     }
 
     private ConnectionDetails connectionDetails(OpenERPAtomFeedProperties properties) {
         return new ConnectionDetails(properties.getAuthenticationURI(),
                 properties.getOpenMRSUser(),
                properties.getOpenMRSPassword(),
                 properties.getConnectionTimeoutInMilliseconds(),
                 properties.getReplyTimeoutInMilliseconds());
     }
 }

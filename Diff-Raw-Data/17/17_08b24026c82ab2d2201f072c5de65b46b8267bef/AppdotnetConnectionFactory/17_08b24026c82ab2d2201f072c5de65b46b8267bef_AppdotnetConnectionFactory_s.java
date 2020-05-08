package org.springframework.social.appdotnet.connect.temp;
 
import org.springframework.social.appdotnet.api.impl.temp.Appdotnet;
 import org.springframework.social.connect.support.OAuth2ConnectionFactory;
 
 /**
  * @author Arik Galansky
  */
 public class AppdotnetConnectionFactory extends OAuth2ConnectionFactory<Appdotnet> {
     public AppdotnetConnectionFactory(String clientId, String clientSecret) {
         super("appdotnet", new AppdotnetServiceProvider(clientId, clientSecret), new AppdotnetAdapter());
     }
 }

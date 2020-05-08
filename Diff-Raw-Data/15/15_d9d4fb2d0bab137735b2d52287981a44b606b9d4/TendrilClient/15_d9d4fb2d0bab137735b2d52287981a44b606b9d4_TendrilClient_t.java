 package com.tendril.cleanweb.client;
 
import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.core.MediaType;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
 import com.tendril.cleanweb.domain.tendril.CostAndConsumption;
 import com.tendril.cleanweb.domain.tendril.TendrilLocation;
 import com.tendril.cleanweb.domain.tendril.TendrilUser;
 
 /**
  */
 public class TendrilClient {
 
     private Provider<Client> clientProvider;
 
     @Inject
     public TendrilClient(Provider<Client> clientProvider) {
         this.clientProvider = clientProvider;
     }
 
     public TendrilUser getUser(String username, String password) throws Exception {
         Client c = clientProvider.get();
         c.addFilter(new HTTPBasicAuthFilter(username, password));
         WebResource webResource = c.resource("https://dev-program.tendrildemo.com/api/rest/user/current-user");
         TendrilUser user = webResource.accept(MediaType.APPLICATION_JSON_TYPE).get(TendrilUser.class);
 
         return user;
     }
 
     public TendrilLocation getLocation(String username, String password) {
         Client c = clientProvider.get();
         c.addFilter(new HTTPBasicAuthFilter(username, password));
         WebResource webResource = c.resource("https://dev-program.tendrildemo.com/api/rest/user/current-user/account/default-account/location/default-location");
 
         TendrilLocation location = webResource.accept(MediaType.APPLICATION_JSON_TYPE).get(TendrilLocation.class);
 
 		return location;
 	}
 
 	public CostAndConsumption getConsumption(String username, String password) {
 		Client c = clientProvider.get();
 		c.addFilter(new HTTPBasicAuthFilter(username, password));
 		DateTime from = DateTime.now();
 		DateTime to = from.minusDays(30);
 		WebResource webResource = c
 				.resource("https://dev-program.tendrildemo.com/api/rest/user/current-user/account/default-account/consumption/HOURLY;from="
 						+ ISODateTimeFormat.dateTimeNoMillis().print(from)
 						+ ";to="
 						+ ISODateTimeFormat.dateTimeNoMillis().print(to));
 
 		CostAndConsumption costAndConsumption = webResource.accept(
 				MediaType.APPLICATION_JSON_TYPE).get(CostAndConsumption.class);
 
 		return costAndConsumption;
 	}
 
 }

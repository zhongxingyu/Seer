 package com.varnoss.client;
 
 import net.sf.json.JSONArray;
 import net.sf.json.JSONObject;
 
 import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
 
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.jersey.api.client.config.ClientConfig;
 import com.sun.jersey.api.client.config.DefaultClientConfig;
 
 import java.net.URLEncoder;
 
 public class JerseyClient {
 
 	public static void main(String[] args) {
 		try {
 			ClientResponse response;
 			WebResource webResource;
 			Client client = Client.create();
 			ClientConfig cfg = new DefaultClientConfig();
 			cfg.getClasses().add(JacksonJsonProvider.class);
 			client = Client.create(cfg);
 			System.out.println("Cliented started!");
			int i = 3;
 			while (i != 4) {
 				if (i == 0) {
 					// POST Warning Input
 					String input = "{\"town\":\"Stockholm\",\"station\":\"Odenplan\",\"type\":\"Ordningsvakt\",\"decs\":\"5-6 ordningsvakter plockar in bus p grna perrongen!\"}";
 
 					webResource = client
 							.resource("http://localhost:8080/varnoss/rest/api/warning");
 
 					// POST method
 					response = webResource.accept("application/json")
 							.type("application/json")
 							.post(ClientResponse.class, input);
 
 					if (response.getStatus() != 200) {
 						throw new RuntimeException(
 								"Failed : HTTP error code : "
 										+ response.getStatus());
 					} else {
 						System.out.println("Yeah!");
 					}
 
 				} else if (i == 1) {
 					// GET method
 					String line = "Bl";
 					line = URLEncoder.encode(line, "UTF-8");
 					webResource = client
 							.resource("http://localhost:8080/varnoss/rest/api/stations?line="
 									+ line);
 					response = webResource.accept("application/json").get(
 							ClientResponse.class);
 
 					if (response.getStatus() != 200) {
 						throw new RuntimeException(
 								"Failed : HTTP error code : "
 										+ response.getStatus());
 					}
 
 					String output = response.getEntity(String.class);
 
 					System.out.println("Output from Server .... \n");
 					System.out.println(output);
 				} else if (i == 2) {
 					// GET method
 					String line = "Bl";
 					line = URLEncoder.encode(line, "UTF-8");
 					webResource = client
 							.resource("http://localhost:8080/varnoss/rest/api/warnings?stations=Slussen,T-Centralen,Odenplan");
 
 					response = webResource.accept("application/json").get(
 							ClientResponse.class);
 
 					if (response.getStatus() != 200) {
 						throw new RuntimeException(
 								"Failed : HTTP error code : "
 										+ response.getStatus());
 					}
 
 					String output = response.getEntity(String.class);
 
 					System.out.println("Output from Server .... \n");
 					System.out.println(output);
 				} else if (i == 3) {
 					// GET method
 					String line = "Bl";
 					line = URLEncoder.encode(line, "UTF-8");
 					webResource = client
							.resource("http://localhost:8080/varnoss/rest/api/warnings?created=2013-10-03%2000:00:00");
 
 					response = webResource.accept("application/json").get(
 							ClientResponse.class);
 
 					if (response.getStatus() != 200) {
 						throw new RuntimeException(
 								"Failed : HTTP error code : "
 										+ response.getStatus());
 					}
 
 					String output = response.getEntity(String.class);
 					
 					//System.out.println("Output from Server .... \n");
 					//System.out.println(output);
 				}
 				i++;
 			}
 
 		} catch (Exception e) {
 
 			e.printStackTrace();
 
 		}
 
 	}
 
 }

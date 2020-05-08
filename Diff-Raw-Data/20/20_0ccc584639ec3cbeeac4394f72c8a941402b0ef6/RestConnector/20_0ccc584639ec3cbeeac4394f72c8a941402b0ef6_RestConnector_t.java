 package com.volkan.interpartitiontraverse;
 
 import java.io.IOException;
import java.io.StringBufferInputStream;
 import java.util.Map;
 
 import org.codehaus.jackson.map.ObjectMapper;
 
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.ClientHandlerException;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.UniformInterfaceException;
 import com.sun.jersey.api.client.WebResource;
 import com.volkan.Utility;
 
 public class RestConnector {
 	private String urlHostAndPort;
 	
 	public RestConnector(String url, String port) {
 		urlHostAndPort = url + ":" + port + "/";
 	}
 	
 	public RestConnector(String port) {
 		urlHostAndPort = Utility.getNeo4jURLFromPropertiesForPort(port);
 	}
 	
 	public String delegateQuery(Map<String,Object> jsonMap){
 		String result = "";
 		Client client = Client.create();
 		WebResource webResource = client.resource(urlHostAndPort + "example/service/volkan");
 		ObjectMapper mapper = new ObjectMapper();
 		
 		try {
 			ClientResponse clientResponse = 
 					webResource.type("application/json")
 							   .post(ClientResponse.class, mapper.writeValueAsString(jsonMap));
 			
 			result = clientResponse.getEntity(String.class);
 		} catch (UniformInterfaceException | ClientHandlerException | IOException e) {
 			e.printStackTrace();
 		} 
 		
 		return result;
 	}

	public ClientResponse delegateQueryWithoutResult(Map<String,Object> jsonMap){
 		String result = "";
 		Client client = Client.create();
 		WebResource webResource = client.resource(urlHostAndPort
 				+ "example/service/volkan_async");
 		ObjectMapper mapper = new ObjectMapper();
 
		ClientResponse clientResponse = null;
 		try {
			clientResponse = 
 					webResource.type("application/json")
 					   .post(ClientResponse.class, mapper.writeValueAsString(jsonMap));
//			result = clientResponse.getEntity(String.class);
			
 		} catch (UniformInterfaceException | ClientHandlerException | IOException e) {
 			e.printStackTrace();
 			result = e.toString();
			clientResponse = new ClientResponse(500, null, new StringBufferInputStream(result), null);
 		}
 		
		return clientResponse;
 	}
 	
 }

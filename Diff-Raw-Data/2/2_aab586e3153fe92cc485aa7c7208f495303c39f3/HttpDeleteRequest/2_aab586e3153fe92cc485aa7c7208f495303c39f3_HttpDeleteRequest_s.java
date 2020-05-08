 package com.grikly.request;
 
 import javax.ws.rs.core.MediaType;
 
 import com.google.gson.Gson;
 import com.grikly.JerseyUtil;
 import com.grikly.URL;
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.WebResource;
 
 /**
  * HttpDeleteRequest is used to execute a HTTP
  * Request using the HTTP DELETE method. This class 
  * is thread-safe and cannot be sub-classed.
  * @author Mario Dennis
  *
  * @param <E>
  * @param <T>
  */
 public final class HttpDeleteRequest <E,T> extends HttpRequest<E, T> {
 
 
 	/**
 	 * HttpDeleteRequest Default Constructor.
 	 * @author Mario Dennis
 	 * @param HttpBuilder<E, T>
 	 */
 	protected HttpDeleteRequest (HttpBuilder<E, T> builder)
 	{
 		super(builder);
 	}//end constructor 
 	
 	
 	/**
 	 * Executes HTTP DELETE Request to Grikly Server.
 	 * @author Mario Dennis
 	 * @return T
 	 */
 	public T execute()
 	{
 		Client client = JerseyUtil.getClient();
 		WebResource resource = client.resource(String.format(URL.BASE.toString(), getPath()));
 		Gson gson = new Gson();
 		ClientResponse response;
 		
 		//adds authInfo when supplied
 		if (getAuthInfo() != null)
 			response = resource.header("ApiKey", getApiKey())
 							   .header("Authorization","Basic " + getAuthInfo())
 							   .type(MediaType.APPLICATION_JSON)
 							   .delete(ClientResponse.class);
 		else 
 			response = resource.header("ApiKey",getApiKey())
							   .accept(MediaType.APPLICATION_JSON)
 							   .get(ClientResponse.class);
 		
 		if (response.getStatus() == 200)
 		{
 			
 			if (response.getHeaders().get("Content-type").equals("application/json; charset=utf-8"))
 			{
 				String json = response.getEntity(String.class);
 				return gson.fromJson(json, getType());
 			}
 			System.out.println(response.getClientResponseStatus());
 			
 		}
 		System.out.println(response.getClientResponseStatus());
 		return null;
 	}//end execute method
 
 }//end HttpDeleteRequest class

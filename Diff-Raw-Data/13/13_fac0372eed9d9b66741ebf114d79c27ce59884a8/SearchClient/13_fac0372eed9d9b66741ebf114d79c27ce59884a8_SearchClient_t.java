 package org.sild.client;
 
 import javax.ws.rs.core.MediaType;
 
 import org.sild.service.SearchService;
 import org.sild.service.request.AdvancedSearchRequest;
 import org.sild.service.response.SearchResult;
 
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.WebResource.Builder;
 import com.sun.jersey.api.client.config.ClientConfig;
 import com.sun.jersey.api.client.config.DefaultClientConfig;
 import com.sun.jersey.api.json.JSONConfiguration;
 
 /**
  * 
  * @author Erik van Ingen
  * 
  */
 public class SearchClient implements SearchService {
 
 	private static String SERVER = "http://localhost:8080/sild-search-web/rest/search";
 
 	@Override
 	public SearchResult search(String searchTerm) {
 
 		Builder builder = getBuilder();
 
 		ClientResponse response = getBuilder().get(ClientResponse.class);
 		evaluateResponse(response);
 		return response.getEntity(SearchResult.class);
 
 	}
 
 	@Override
 	public SearchResult search(AdvancedSearchRequest searchParameters) {
 		ClientResponse response = getBuilder().post(ClientResponse.class, searchParameters);
 		evaluateResponse(response);
 		return response.getEntity(SearchResult.class);
 	}
 
 	private Builder getBuilder() {
 		ClientConfig clientConfig = new DefaultClientConfig();
 		clientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);

		// clientConfig.getFeatures().

 		return Client.create(clientConfig).resource(SERVER).accept(MediaType.APPLICATION_JSON);
 	}
 
 	private void evaluateResponse(ClientResponse response) {
 		if (response.getStatus() != 200) {
 			throw new ClientException("Failed : HTTP error code : " + response.getStatus());
 		}
 	}
 
 }

 /**
  * 
  */
 package com.xplenty.api;
 
 import java.io.StringWriter;
 
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.jersey.api.client.config.ClientConfig;
 import com.sun.jersey.api.client.config.DefaultClientConfig;
 import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
 import com.xplenty.api.Xplenty.Version;
 import com.xplenty.api.exceptions.AuthFailedException;
 import com.xplenty.api.exceptions.RequestFailedException;
 import com.xplenty.api.exceptions.XplentyAPIException;
 import com.xplenty.api.request.Request;
 import com.xplenty.api.util.Http;
 
 /**
  * Proxy for connecting to the XplentyAPI over HTTP
  * 
  * @author Yuriy Kovalek
  *
  */
 class XplentyWebConnectivity {
 	private static final String API_PATH = "api";
 	
	private String HOST = "api.xplenty.com";
 	private Http.Protocol PROTOCOL = Http.Protocol.Https;
 	private final String ACCOUNT_NAME;
 	private final String API_KEY;
 	
 	private final Client client;
 	private Version version = null;
 
 	/**
 	 * Construct a new instance for given account and API key
 	 * @param accountName name of the associated account, used in URL's
 	 * @param apiKey used for authentication
 	 */
 	XplentyWebConnectivity(String accountName, String apiKey) {
 		ACCOUNT_NAME = accountName;
 		API_KEY = apiKey;
 		
 		ClientConfig config = new DefaultClientConfig();
 		client = Client.create(config);
 		client.addFilter(new HTTPBasicAuthFilter(apiKey, ""));
 	}
 
 	/**
 	 * Synchronously execute given request
 	 * @param request request to execute
 	 * @return respective response type
 	 */
 	public <T> T execute(Request<T> request) {
 		WebResource.Builder builder = getConfiguredResource(request);
 		ClientResponse response = null;
 		switch (request.getHttpMethod()) {
 			case GET: 		response = builder.get(ClientResponse.class); break;
 			case POST:		response = builder.post(ClientResponse.class); break;
 			case PUT: 		response = builder.put(ClientResponse.class); break;
 			case DELETE: 	response = builder.delete(ClientResponse.class); break;
 		}
 		validate(request, response);
 		return request.getResponse(response);		
 	}
 	
 	/**
 	 * Convenience method for getting a configured {@link WebResource.Builder} for given request
 	 * @param request that would be submitted to the XPlenty Server
 	 * @return  builder
 	 */
 	private <T> WebResource.Builder getConfiguredResource(Request<T> request) {
 		WebResource.Builder b = client.resource(getMethodURL(request.getEndpoint()))
 										.accept(request.getResponseType().value
 													+ (version == null ? "" : "; " + version.format())
 												);
 		if (request.hasBody()) {
 			StringWriter sw = new StringWriter();
 			try {
 				new ObjectMapper().writeValue(sw, request.getBody());
 			} catch (Exception e) {
 				throw new XplentyAPIException(e);
 			}
 			b.entity(sw.toString()).type(Http.MediaType.JSON.value);
 		}
 		
 		return b;
 	}
 	
 	/**
 	 * Constructs the actual URL
 	 * @param methodEndpoint - describes the action type
 	 * @return filly qualified URL
 	 */
 	private String getMethodURL(String methodEndpoint) {
 		return PROTOCOL + "://" + HOST + "/" + ACCOUNT_NAME + "/" + API_PATH + "/" + methodEndpoint;
 	}
 	
 	/**
 	 * Check the response status and throws exception on errors
 	 * @param request used request
 	 * @param response received response
 	 * @throws AuthFailedException
 	 * @throws RequestFailedException
 	 */
 	private <T> void validate(Request<T> request, ClientResponse response) {
         switch (response.getClientResponseStatus()){
             case OK : case CREATED : case NO_CONTENT : return;
             case UNAUTHORIZED : throw new AuthFailedException(response.getStatus(), response.getEntity(String.class));
             default: throw new RequestFailedException(request.getName() + " failed", response.getStatus(), response.getEntity(String.class));
         }
 	}
 
 	String getAccountName() {
 		return ACCOUNT_NAME;
 	}
 
 	String getApiKey() {
 		return API_KEY;
 	}
 
 	public void setVersion(Version ver) {
 		version = ver;
 	}
 
 	public void setHost(String host) {
 		HOST = host;
 	}
 
 	public void setProtocol(Http.Protocol proto) {
 		PROTOCOL = proto;
 	}
 
 	public Http.Protocol getProtocol() {
 		return PROTOCOL;
 	}
 
 	public String getHost() {
 		return HOST;
 	}
 
 	public Version getVersion() {
 		return version;
 	}
 }

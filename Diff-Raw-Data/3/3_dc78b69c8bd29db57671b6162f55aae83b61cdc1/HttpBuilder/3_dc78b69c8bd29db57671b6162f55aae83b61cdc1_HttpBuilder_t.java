 package com.grikly.request;
 
 import java.io.File;
 
 import javax.ws.rs.core.MultivaluedMap;
 
 /**
  * HttpBuilder class is used to set properties
  * of HttpRequest and to build instances of HttpRequest
  * base classes. This is require to ensure HttpRequest
  * base classes are  immutable and thread-safe.
  * @author Mario Dennis
  *
  * @param <E>
  * @param <T>
  */
 public final class HttpBuilder<E,T> {
 	
 	private String path;
 	private String authInfo;
 	private E model;
 	private String apiKey;
 	private Class<T> type;
 	private MultivaluedMap<String, String> queryMap;  
 	
 	
 	/**
 	 * HttpBuilder Default constructor.
 	 * @author Mario Dennis
 	 * @param type that will be returned by IHttpRequest
 	 * @param apiKey
 	 */
 	public HttpBuilder (Class<T> type,String apiKey)
 	{
 		this.type = type;
 		this.apiKey = apiKey;
 	}//end constructor
 
 	
 	
 	/**
 	 * Builds HttpGetRequest object.
 	 * @author Mario Dennis
 	 * @return HttpGetRequest<E,T>
 	 */
 	public Request<E, T> buildHttpGet ()
 	{
 		return new HttpGetRequest<E,T>(this);
 	}//end buildHttpGet method
 	
 	
 	
 	/**
 	 * Builds HttpPostRequest object.
 	 * @author Mario Dennis
 	 * @return HttpPostRequest<E,T>
 	 */
 	public Request<E, T> buildHttpPost ()
 	{
 		return new HttpPostRequest<E, T>(this);
 	}//end  buildHttpPost method 
 	
 	
 	
 	/**
 	 * Builds HttpDeleteRequest object.
 	 * @author Mario Dennis
 	 * @return HttpDeleteRequest<E,T>
 	 */
 	public Request<E, T> buildHttpDelete ()
 	{
 		return new HttpDeleteRequest<E, T>(this);
 	}//end buildHttpDelete method
 	
 	
 	
 	/**
 	 * Builds HttpPutRequest object.
 	 * @author Mario Dennis
 	 * @return HttpPutRequest<E,T>
 	 */
 	public Request<E, T> buildHttpPut ()
 	{
 		return new HttpPutRequest<E, T>(this);
 	}//end buildHttpPut method
 	
 	
 	public Request<E,T> buildMultiPartRequest (File file,String contentType)
 	{
 		return new MultiPartRequest<E, T>(this, file, contentType);
 	}//end buildMultiPartRequest method
 	
 	
 	/**
 	 * Set authInfo attribute.
 	 * @author Mario Dennis
 	 * @param authInfo 
 	 * @return HttpBuilder <E,T>
 	 */
 	public HttpBuilder<E, T> setAuthInfo (byte[] authInfo)
 	{
		if (authInfo != null)
			this.authInfo = new String(authInfo);
 		return this;
 	}//end setAuthInfo method
 	
 	
 	
 	/**
 	 * Set model attribute.
 	 * @author Mario Dennis
 	 * @param model
 	 * @return HttpBuilder <E,T>
 	 */
 	public HttpBuilder<E,T> setModel (E model)
 	{
 		this.model = model;
 		return this;
 	}//end setModel method
 	
 	
 	/**
 	 * Set path attribute.
 	 * @author Mario Dennis
 	 * @param path
 	 * @return HttpBuilder <E,T>
 	 */
 	public HttpBuilder<E,T> setPath (String path)
 	{
 		this.path = String.format(path);
 		return this;
 	}//end setPath method
 	
 
 	/**
 	 * Set queryMap attribute.
 	 * @param queryMap
 	 * @return HttpBuilder <E,T>
 	 */
 	public HttpBuilder<E, T> addQueryParam (MultivaluedMap<String, String> queryMap)
 	{
 		this.queryMap = queryMap;
 		return this;
 	}//end addQueryParam method 
 	
 	
 	/**
 	 * Get ApiKey
 	 * @author Mario Dennis
 	 * @return String
 	 */
 	protected String getApiKey()
 	{
 		return apiKey;
 	}//end getApiKey method
 	
 	
 	/**
 	 * Get Type
 	 * @author Mario Dennis
 	 * @return Class <T>
 	 */
 	protected Class<T> getType ()
 	{
 		return type;
 	}//end getType method
 	
 	
 
 	/**
 	 * Get Model
 	 * @author Mario Dennis
 	 * @return E
 	 */
 	public E getModel ()
 	{
 		return model;
 	}//end getModel method
 	
 
 	/**
 	 * Get path
 	 * @author Mario Dennis
 	 * @return String
 	 */
 	protected String getPath ()
 	{
 		return path;
 	}//end getPath method
 	
 	
 	/**
 	 * Get authInfo
 	 * @author Mario Dennis
 	 * @return 
 	 */
 	protected String getAuthInfo ()
 	{
 		return authInfo;
 	}///end getAuthInfo method
 	
 	
 	/**
 	 * Get queryMap
 	 * @author Mario Dennis
 	 * @return MultivaluedMap<String,String>
 	 */
 	protected MultivaluedMap<String,String> getMap ()
 	{
 		return queryMap;
 	}//end getMap method
 	
 }//end GriklyBuilder class

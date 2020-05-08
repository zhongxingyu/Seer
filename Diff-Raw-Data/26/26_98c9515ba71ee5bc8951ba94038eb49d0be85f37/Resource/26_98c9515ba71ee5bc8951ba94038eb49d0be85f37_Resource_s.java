 /**
  * 
  */
 package io.spire.api;
 
 import io.spire.api.Api.ApiDescriptionModel.ApiSchemaModel;
 import io.spire.request.Request.RequestType;
 import io.spire.request.Request;
 import io.spire.request.RequestData;
 import io.spire.request.RequestFactory;
 import io.spire.request.Response;
 import io.spire.request.ResponseException;
 
 import java.io.IOException;
 import java.lang.reflect.Constructor;
 import java.util.HashMap;
 import java.util.Map;
 
 
 /**
  * General abstraction of Api resources
  * 
  * @since 1.0
  * @author Jorge Gonzalez
  *
  */
 public abstract class Resource {
 	
 	/** Holds the internal resource data */
 	protected ResourceModel model;
 	/** Holds descriptions of all Api resource schemas */
 	protected ApiSchemaModel schema;
 	
 	/**
 	 * Default constructor
 	 */
 	public Resource() {
 		this.model = new ResourceModel(new HashMap<String, Object>());
 	}
 	
 	/**
 	 * Initialize a resource with the Api resource schemas
 	 * 
 	 * @param schema {@link ApiSchemaModel}
 	 */
 	public Resource(ApiSchemaModel schema) {
 		this();
 		this.schema = schema;
 	}
 	
 	/**
 	 * Initialize a resource model and Api resource schemas
 	 * 
 	 * @param model
 	 * @param schema
 	 */
 	public Resource(ResourceModel model, ApiSchemaModel schema) {
 		this.schema = schema;
 		this.model = model;
 		this.initialize();
 	}
 	
 	/**
 	 * This is called automatically by the resource constructor
 	 * to initialize any other internal properties.
 	 * Derived classes should override this method adding any 
 	 * custom initialization needed by the class
 	 */
 	protected abstract void initialize();
 	
 	/**
 	 * Access resources from the internal resource model
 	 * 
 	 * @param resourceName
 	 * @return {@link ResourceModel}
 	 */
 	@SuppressWarnings("unchecked")
 	protected ResourceModel getResourceModel(String resourceName){
 		Map<String, Object> rawModel = model.getProperty(resourceName, Map.class);
 		if(rawModel == null){
 			rawModel = new HashMap<String, Object>();
 			model.setProperty(resourceName, rawModel);
 		}
 		return new ResourceModel(rawModel);
 	}
 	
 	/**
 	 * Updates the underlying resource model when GET/UPDATE a resource
 	 * Subclasses would want to override this method if any special operations
 	 * need to done in regards of how the model should be updated
 	 * 
 	 * @param rawModel is a representation of the raw model as a Map<String, Object>
 	 */
 	protected void updateModel(Map<String, Object> rawModel){
 		model.rawModel = rawModel;
 		this.initialize();
 	}
 	
 	/**
 	 * Set the internal resource model
 	 * 
 	 * @param rawModel
 	 */
 	protected abstract void addModel(Map<String, Object> rawModel);
 	
 	/**
 	 * Wrapper for the resource model internal data 
 	 * 
 	 * @since 1.0
 	 * @author Jorge Gonzalez
 	 *
 	 */
 	public static class ResourceModel implements ResourceModelInterface {
 		private Map<String, Object> rawModel;
 		
 		/**
 		 * Default constructor
 		 * 
 		 * @param data Map object representing the resource
 		 */
 		public ResourceModel(Map<String, Object> data){
 			this.rawModel = data;
 			if(this.rawModel == null){
 				this.rawModel = new HashMap<String, Object>();
 			}
 		}
 
 		@Override
 		public <T>T getProperty(String propertyName, Class<T> type){
 			T t = null;
 			try{
 				t = type.cast(rawModel.get(propertyName));
 			}catch (Exception e) {
 				//e.printStackTrace();
 			}
 			return t;
 		}
 		
 		@Override
 		public void setProperty(String propertyName, Object data){
 			rawModel.put(propertyName, data);
 		}
 		
 		@SuppressWarnings("unchecked")
 		public ResourceModel getResourceMapCollection(String resourceName){
 			HashMap<String, Object> rawModelCollection = new HashMap<String, Object>();
 			Map<String, Object> resources = this.getProperty(resourceName, Map.class);
 			for (Map.Entry<String, Object> resource : resources.entrySet()) {
 				String name = (String)resource.getKey();
 				Map<String, Object> rawData = (Map<String, Object>)resource.getValue();
 				ResourceModel rawModel = new ResourceModel(rawData);
 				rawModelCollection.put(name, rawModel);
 			}
 			return new ResourceModel(rawModelCollection);
 		}
 		
 		@SuppressWarnings("unchecked")
 		public <T> Map<String, T> getMapCollection(String resourceName, Class<T> T, ApiSchemaModel schema) throws RuntimeException{
 			HashMap<String, T> mapCollection = new HashMap<String, T>();
 			Constructor<T> constructorT;
 			try {
 				constructorT = T.getConstructor(ResourceModel.class, ApiSchemaModel.class);
 			} catch (NoSuchMethodException e) {
 				e.printStackTrace();
 				return null;
 			}
 			
 			Map<String, Object> resources = this.getProperty(resourceName, Map.class);
 			if(resources == null)
 				return mapCollection;
 			
 			for (Map.Entry<String, Object> resource : resources.entrySet()) {
 				String name = (String)resource.getKey();
 				Map<String, Object> rawData = (Map<String, Object>)resource.getValue();
 				ResourceModel rawModel = new ResourceModel(rawData);
 				try{
 					T t = constructorT.newInstance(rawModel, schema);
 					mapCollection.put(name, t);
 				}catch (Exception e) {
 					e.printStackTrace();
 					return null;
 				}
 			}
 			return mapCollection;
 		}
 	}
 	
 	/**
 	 * Gets the resource url
 	 * 
 	 * @return {@link String}
 	 */
 	public String getUrl(){
 		return model.getProperty("url", String.class);
 	}
 	
 	/**
 	 * Sets the resource url
 	 * 
 	 * @param url
 	 */
 	public void setUrl(String url){
 		model.setProperty("url", url);
 	}
 	
 	/**
 	 * Gets the resource name
 	 * Should be defined by each derived class
 	 * 
 	 * @return {@link String}
 	 */
 	public abstract String getResourceName();
 	
 	/**
 	 * Gets the resource media type
 	 * 
 	 * @return {@link String}
 	 */
 	public String getMediaType(){
 		String resourceName = this.getResourceName();
 		return schema.getMediaType(resourceName);
 	}
 	
 	/**
 	 * Gets the resource capability
 	 * 
 	 * @return {@link String}
 	 */
 	public String getCapability(){
 		return model.getProperty("capability", String.class);
 	}
 	
 	/**
 	 * Sets the resource capability
 	 * 
 	 * @param capability
 	 */
 	public void setCapability(String capability){
 		model.setProperty("capability", capability);
 	}
 	
 	/**
 	 * Gets the resource key
 	 * 
 	 * @return {@link String}
 	 */
 	public String getKey(){
 		return model.getProperty("key", String.class);
 	}
 	
 	/**
 	 * Gets the resource type
 	 * 
 	 * @return {@link String}
 	 */
 	public String getType(){
 		return model.getProperty("type", String.class);
 	}
 	
 	/**
 	 * Gets the name assigned to this resource instance
 	 * 
 	 * @return {@link String}
 	 */
 	public String getName(){
 		return model.getProperty("name", String.class);
 	}
 	
 	/**
 	 * Sets the name for this resource instance
 	 * 
 	 * @param name
 	 */
 	public void setName(String name){
 		model.setProperty("name", name);
 	}
 	
 	/**
 	 * Gets resource model data
 	 * 
 	 * @return  {@link ResourceModel}
 	 */
 	public ResourceModel getInnerModel(){
 		return model;
 	}
 	
 	/**
 	 * Sets resource model data
 	 * 
 	 * @param model
 	 */
 	public void setInnerModel(ResourceModel model){
 		this.model = model;
 	}
 	
 	/**
 	 * Copy the resource model
 	 * 
 	 * @param resource
 	 */
 	protected void copy(Resource resource){
 		this.model = resource.model;
 		this.initialize();
 	}
 	
 	/**
	 * Knows how to build a request data object for any resource operation GET/PUT/DELETE/POST
 	 * 
 	 * @param methodType
 	 * @param content
 	 * @param headers
 	 * @return RequestData
 	 */
 	protected RequestData createRequestData(RequestType methodType, Map<String, Object> queryParams, Map<String, Object> content, Map<String, String> headers){
 		RequestData data = RequestFactory.createRequestData();
 		data.method = methodType;
 		data.url = model.getProperty("url", String.class);
 		data.queryParams = queryParams;
 		
 		data.headers.put("Authorization", "Capability " + model.getProperty("capability", String.class));
 		data.headers.put("Accept", this.getMediaType());
 		
 		if(methodType != RequestType.HTTP_GET){
 			data.headers.put("Content-Type", this.getMediaType());
 		}
 
 		if(headers != null && !headers.isEmpty()){
 			for (Map.Entry<String, String> header : headers.entrySet()) {
 				data.headers.put(header.getKey(), header.getValue());
 			}
 		}
 		
 		if(methodType == RequestType.HTTP_PUT || methodType == RequestType.HTTP_POST){
 			data.body = content;
 		}
 		
 		return data;
 	}
 	
 	/**
	 * GET resource
 	 * 
 	 * @throws ResponseException
 	 * @throws IOException
 	 */
 	public void get() throws ResponseException, IOException{
 		this.get(null, null);
 	}
 	
 	public Map<String, Object> get(Map<String, Object> queryParams, Map<String, String> headers) throws ResponseException, IOException{
 		RequestData data = this.createRequestData(RequestType.HTTP_GET, queryParams, null, headers);
 		Map<String, Object> rawModel = this.sendRequest(data);
 		updateModel(rawModel);
 		return rawModel;
 	}
 
 	/**
	 * PUT resource (update)
 	 * 
 	 * @throws ResponseException
 	 * @throws IOException
 	 */
 	@SuppressWarnings("unchecked")
 	public void update() throws ResponseException, IOException{
 		RequestData data = RequestFactory.createRequestData();
 		data.method = RequestType.HTTP_PUT;
 		data.url = model.getProperty("url", String.class);
 		
 		data.headers.put("Authorization", "Capability " + model.getProperty("capability", String.class));
 		data.headers.put("Accept", this.getMediaType());
 		data.headers.put("Content-Type", this.getMediaType());
 		
 		data.body = model.rawModel;
 		
 		Request request = RequestFactory.createRequest(data);
 		Response response = request.send();
 		if(!response.isSuccessStatusCode())
 			throw new ResponseException(response, "Error updating " + getResourceName());
 		
 		Map<String, Object> rawModel = response.parseAs(HashMap.class);
 		updateModel(rawModel);
 	}
 	
 	/**
	 * DELETE resource
 	 * 
 	 * @throws ResponseException
 	 * @throws IOException
 	 */
 	public void delete() throws ResponseException, IOException{
 		RequestData data = RequestFactory.createRequestData();
 		data.method = RequestType.HTTP_DELETE;
 		data.url = model.getProperty("url", String.class);
 		
 		data.headers.put("Authorization", "Capability " + model.getProperty("capability", String.class));
 		data.headers.put("Accept", this.getMediaType());
 		data.headers.put("Content-Type", this.getMediaType());
 		
 		Request request = RequestFactory.createRequest(data);
 		Response response = request.send();
 		if(!response.isSuccessStatusCode())
 			throw new ResponseException(response, "Error deleting " + getResourceName());
 	}
 	
 	/**
 	 * 
 	 * @param content
 	 * @return {@link Map}
 	 * @throws ResponseException
 	 * @throws IOException
 	 */
 	public Map<String, Object> post(Map<String, Object> content) throws ResponseException, IOException{
 		RequestData data = this.createRequestData(RequestType.HTTP_POST, null, content, null);
 		return this.post(data);
 	}
 	
 	/**
	 * POST resource
 	 * 
 	 * @param content
 	 * @param headers
 	 * @return {@link Map}
 	 * @throws ResponseException
 	 * @throws IOException
 	 */
 	public Map<String, Object> post(Map<String, Object> content, Map<String, String> headers) throws ResponseException, IOException{
 		RequestData data = this.createRequestData(RequestType.HTTP_POST, null, content, headers);
 		return this.post(data);
 	}
 	
 	/**
	 * POST resource
 	 * 
 	 * @param data
 	 * @return {@link Map}
 	 * @throws ResponseException
 	 * @throws IOException
 	 */
 	public Map<String, Object> post(RequestData data) throws ResponseException, IOException{
 		Map<String, Object> rawModel = this.sendRequest(data);
 		addModel(rawModel);
 		return rawModel;
 	}
 	
 	/**
	 * POST resource
 	 * 
	 * @param data
 	 * @return {@link Map}
 	 * @throws ResponseException
 	 * @throws IOException
 	 */
 	@SuppressWarnings("unchecked")
 	public Map<String, Object> sendRequest(RequestData data) throws ResponseException, IOException{
 		Request request = RequestFactory.createRequest(data);
 		Response response = request.send();
 		if(!response.isSuccessStatusCode())
 			throw new ResponseException(response, "Error " + data.method.name().toLowerCase() + "ing" + getResourceName());
 		
 		Map<String, Object> rawModel = response.parseAs(HashMap.class);
 		return rawModel;
 	}
 }

 /**
  * 
  */
 package io.spire.api;
 
 import io.spire.api.Api.APIDescriptionModel.APISchemaModel;
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
  * @author jorge
  *
  */
 public abstract class Resource {
 	
 	protected ResourceModel model;
 	protected APISchemaModel schema;
 	
 	
 	/**
 	 * 
 	 */
 	public Resource() {
 		this.model = new ResourceModel(new HashMap<String, Object>());
 	}
 	
 	public Resource(APISchemaModel schema) {
 		this();
 		this.schema = schema;
 		this.initialize();
 	}
 	
 	public Resource(ResourceModel model, APISchemaModel schema) {
 		this.schema = schema;
 		this.model = model;
 		this.initialize();
 	}
 	
 	/**
 	 * This is called automatically by the resource constructor
 	 * to initialize any other internal properties.
 	 * Derived classes should override this method
 	 */
 	protected abstract void initialize();
 	
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
 	 * Updates the underlying resource model when GET/UPDATE (ing) a resource
 	 * Subclasses would want to override this method if any special operations
 	 * need to done in regards of how the model should be updated
 	 * 
 	 * @param rawModel is a representation of the raw model as a Map<String, Object>
 	 */
 	protected void updateModel(Map<String, Object> rawModel){
 		model.rawModel = rawModel;
 		this.initialize();
 	}
 	
 	protected abstract void addModel(Map<String, Object> rawModel);
 	
 	public static class ResourceModel implements ResourceModelInterface {
 		private Map<String, Object> rawModel;
 		
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
 		public <T> Map<String, T> getMapCollection(String resourceName, Class<T> T, APISchemaModel schema) throws RuntimeException{
 			HashMap<String, T> mapCollection = new HashMap<String, T>();
 			Constructor<T> constructorT;
 			try {
 				constructorT = T.getConstructor(ResourceModel.class, APISchemaModel.class);
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
 	 * 
 	 * @return
 	 */
 	public String getUrl(){
 		return model.getProperty("url", String.class);
 	}
 	
 	/**
 	 * 
 	 * @param url
 	 */
 	public void setUrl(String url){
 		model.setProperty("url", url);
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public abstract String getResourceName();
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public String getMediaType(){
 		String resourceName = this.getResourceName();
 		return schema.getMediaType(resourceName);
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public String getCapability(){
 		return model.getProperty("capability", String.class);
 	}
 	
 	/**
 	 * 
 	 * @param capability
 	 */
 	public void setCapability(String capability){
 		model.setProperty("capability", capability);
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public String getKey(){
 		return model.getProperty("key", String.class);
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public String getType(){
 		return model.getProperty("type", String.class);
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public String getName(){
 		return model.getProperty("name", String.class);
 	}
 	
 	/**
 	 * 
 	 * @param name
 	 */
 	public void setName(String name){
 		model.setProperty("name", name);
 	}
 	
 	/**
 	 * Knows how to build a request data object for any resource operation GET/PUT/DELETE/POST
 	 * 
 	 * @param methodType
 	 * @param content
 	 * @param headers
 	 * @return RequestData
 	 */
 	protected RequestData createRequestData(RequestType methodType, Map<String, Object> content, Map<String, String> headers){
 		RequestData data = RequestFactory.createRequestData();
 		data.method = methodType;
 		data.url = model.getProperty("url", String.class);
 		
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
 	 * 
 	 * @throws ResponseException
 	 * @throws IOException
 	 */
 	@SuppressWarnings("unchecked")
 	public void get() throws ResponseException, IOException{
 		RequestData data = RequestFactory.createRequestData();
 		data.method = RequestType.HTTP_GET;
 		data.url = model.getProperty("url", String.class);
 		data.headers.put("Authorization", "Capability " + model.getProperty("capability", String.class));
 		data.headers.put("Accept", this.getMediaType());
 		
 		Request request = RequestFactory.createRequest(data);
 		Response response = request.send();
 		if(!response.isSuccessStatusCode())
 			throw new ResponseException(response, "Error getting " + getResourceName());
 		
 		Map<String, Object> rawModel = response.parseAs(HashMap.class);
 		updateModel(rawModel);
 	}
 
 	/**
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
 	 * @throws ResponseException
 	 * @throws IOException
 	 */
 	public void post(Map<String, Object> content) throws ResponseException, IOException{
 		RequestData data = this.createRequestData(RequestType.HTTP_POST, content, null);
		this.post(data);
 	}
 	
 	protected void post(Map<String, Object> content, Map<String, String> headers) throws ResponseException, IOException{
 		RequestData data = this.createRequestData(RequestType.HTTP_POST, content, headers);
 		this.post(data);
 	}
 		
 	@SuppressWarnings("unchecked")
 	protected void post(RequestData data) throws ResponseException, IOException{
 		Request request = RequestFactory.createRequest(data);
 		Response response = request.send();
 		if(!response.isSuccessStatusCode())
 			throw new ResponseException(response, "Error creating " + getResourceName());
 		
 		Map<String, Object> rawModel = response.parseAs(HashMap.class);
 		addModel(rawModel);
 	}
 }

 package au.com.justinb.open311;
 
 import au.com.justinb.open311.mapping.RequestMappings;
 import au.com.justinb.open311.model.resource.BaseResource;
 import au.com.justinb.open311.util.Format;
 import au.com.justinb.open311.util.ReflectionUtils;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.restlet.Request;
 import org.restlet.data.Method;
 import org.restlet.ext.jackson.JacksonConverter;
 import org.restlet.resource.ClientResource;
 import org.restlet.resource.ResourceException;
 
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.List;
 
 public class GenericRequestAdapter<T> {
 
   private final ClientResource clientResource = new ClientResource("");
 
   private Class modelClass;
   private Class<? extends BaseResource> resourceClass;
 
   private Format format = Format.JSON;
 
   private static final ObjectMapper OBJECT_MAPPER = new JacksonConverter().getObjectMapper();
 
   public GenericRequestAdapter(Class aModelClass, Format aFormat) {
     this(aModelClass);
     format = aFormat;
   }
 
   public GenericRequestAdapter(Class aModelClass) {
     modelClass = aModelClass;
     resourceClass = RequestMappings.getResource(modelClass);
   }
 
   public List<T> list() {
     String url = RequestMappings.getListUrlOfRequest(modelClass, format);
     clientResource.setRequest(new Request(Method.GET, url));
 
     return retrieveResources();
   }
 
   public T get(String id) {
     String url = RequestMappings.getUrlOfRequest(modelClass, format, id);
     clientResource.setRequest(new Request(Method.GET, url));
 
     List<T> modelObjects = retrieveResources();
     return modelObjects.size() > 0 ? modelObjects.get(0) : null;
   }
 
   @SuppressWarnings("unchecked")
   private List<T> retrieveResources() {

     List<T> modelObjects = new ArrayList<T>();
    try {
      ArrayList<LinkedHashMap> objectMaps = clientResource.wrap(resourceClass).retrieveList();
      for (LinkedHashMap hm : objectMaps) {
        modelObjects.add((T) OBJECT_MAPPER.convertValue(hm, modelClass));
      }
    } catch (ResourceException re) {
      System.out.println("ResourceException: " + re);
     }

     return modelObjects;
   }
 
   public void create(T modelObject) {
     String listUrlOfRequest = RequestMappings.getListUrlOfRequest(modelObject.getClass(), format);
     String requestUri = ReflectionUtils.constructQueryString(listUrlOfRequest, modelObject);
 
     clientResource.setRequest(new Request(Method.POST, requestUri));
     try {
       clientResource.post(modelObject);
     } catch (ResourceException re) {
       System.out.println("ResourceException: " + re);
     }
   }
 }

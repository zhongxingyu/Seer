 package ly.apps.android.rest.client;
 
 import ly.apps.android.rest.cache.CacheInfo;
 import ly.apps.android.rest.client.annotations.*;
 import ly.apps.android.rest.utils.HeaderUtils;
 import ly.apps.android.rest.utils.Logger;
 import ly.apps.android.rest.utils.ResponseTypeUtil;
 
 import ly.apps.android.rest.utils.StringUtils;
 import org.apache.http.message.BasicHeader;
 
 import java.io.UnsupportedEncodingException;
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Method;
 import java.lang.reflect.ParameterizedType;
 import java.lang.reflect.Type;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 public class RestMethodCache {
 
     public RestMethodCache(Method method, RestClient restClient) {
         this.method = method;
         this.restClient = restClient;
         parseMethod();
         parseCacheInfo();
         parseParameters();
     }
 
     enum RequestType {
         GET, POST, PUT, PATCH, DELETE, HEAD
     }
 
     private Method method;
 
     private String path;
 
     private Map<Integer, String> pathParams = new HashMap<Integer, String>();
 
     private Map<Integer, String> queryParams = new HashMap<Integer, String>();
 
     private Map<Integer, String> formFields = new HashMap<Integer, String>();
 
     private Map<Integer, String> headers = new HashMap<Integer, String>();
 
     private int bodyPosition;
 
     private boolean bodyPresent;
 
     private int bundledQueryParamsPosition;
 
     private boolean bundledQueryParamsPresent;
 
     private int formDataPosition;
 
     private boolean formDataPresent;
 
     private RequestType requestType;
 
     private Type targetType;
 
     private Type rawContainer;
 
     private Type containerTarget;
 
     private RestClient restClient;
 
     private boolean isList;
 
     private Cached cached;
 
     public Type getTargetType() {
         return targetType;
     }
 
     @SuppressWarnings("unchecked")
     private void parseMethod() {
         Type type = ResponseTypeUtil.parseResponseType(method);
         if (type instanceof ParameterizedType) {
             ParameterizedType parameterizedType = (ParameterizedType) type;
             rawContainer = parameterizedType.getRawType();
             containerTarget = parameterizedType.getActualTypeArguments()[0];
         } else {
             targetType = type;
         }
         if (method.isAnnotationPresent(GET.class)) {
             requestType = RequestType.GET;
             path = method.getAnnotation(GET.class).value();
         } else if (method.isAnnotationPresent(POST.class)) {
             requestType = RequestType.POST;
             path = method.getAnnotation(POST.class).value();
         } else if (method.isAnnotationPresent(PUT.class)) {
             requestType = RequestType.PUT;
             path = method.getAnnotation(PUT.class).value();
         } else if (method.isAnnotationPresent(PATCH.class)) {
             requestType = RequestType.PATCH;
             path = method.getAnnotation(PATCH.class).value();
         } else if (method.isAnnotationPresent(DELETE.class)) {
             requestType = RequestType.DELETE;
             path = method.getAnnotation(DELETE.class).value();
         } else if (method.isAnnotationPresent(HEAD.class)) {
             requestType = RequestType.HEAD;
             path = method.getAnnotation(HEAD.class).value();
         } else {
             throw new IllegalStateException("No http annotation (GET, POST, PUT, DELETE, PATCH, HEAD) found on method" + method);
         }
     }
 
     private void parseCacheInfo() {
         if (method.isAnnotationPresent(Cached.class)) {
             cached = method.getAnnotation(Cached.class);
         }
     }
 
     private void parseParameters() {
         Annotation[][] parameterAnnotationArrays = method.getParameterAnnotations();
         for (int i = 0; i < parameterAnnotationArrays.length; i++) {
             Annotation[] parameterAnnotations = parameterAnnotationArrays[i];
             if (parameterAnnotations != null) {
                 for (Annotation parameterAnnotation : parameterAnnotations) {
                     Class<? extends Annotation> annotationType = parameterAnnotation.annotationType();
                     if (annotationType == Path.class) {
                         pathParams.put(i,((Path) parameterAnnotation).value());
                     } else if (annotationType == QueryParam.class) {
                         queryParams.put(i,((QueryParam) parameterAnnotation).value());
                     } else if (annotationType == QueryParams.class) {
                         bundledQueryParamsPosition = i;
                         bundledQueryParamsPresent = true;
                     } else if (annotationType == FormData.class) {
                         formDataPosition = i;
                         formDataPresent = true;
                     } else if (annotationType == Header.class) {
                         headers.put(i,((Header) parameterAnnotation).value());
                     } else if (annotationType == Body.class) {
                         bodyPresent = true;
                         bodyPosition = i;
                     } else if (annotationType == FormField.class) {
                         formFields.put(i,((FormField) parameterAnnotation).value());
                     }
                 }
             }
         }
     }
 
     @SuppressWarnings("unchecked")
     public <T> void invoke(String baseUrl, Callback<T> delegate, Object[] args) throws UnsupportedEncodingException {
         prepareDelegate(delegate, args);
         String resultingPath = path;
         if (pathParams.size() > 0) {
             resultingPath = restClient.getQueryParamsConverter().parsePathParams(resultingPath, pathParams, args);
         }
         if (queryParams.size() > 0) {
             resultingPath = restClient.getQueryParamsConverter().parseQueryParams(resultingPath, queryParams, args);
         }
         if (bundledQueryParamsPresent) {
             resultingPath = restClient.getQueryParamsConverter().parseBundledQueryParams(resultingPath, args[bundledQueryParamsPosition]);
         }
         String url = baseUrl + resultingPath;
         Object body = null;
         if (bodyPresent) {
             body = args[bodyPosition];
             delegate.setRequestContentType(HeaderUtils.CONTENT_TYPE_JSON);
         } else if (formDataPresent) {
             body = args[formDataPosition];
             delegate.setRequestContentType(HeaderUtils.CONTENT_TYPE_FORM_URL_ENCODED);
         }
         Logger.d("invoking: " + url + " with body: " + body + " and request content type: " + delegate.getRequestContentType());
         switch (requestType) {
             case GET:
                 restClient.get(url, delegate);
                 break;
             case POST:
                 restClient.post(url, body, delegate);
                 break;
             case PUT:
                 restClient.put(url, body, delegate);
                 break;
             case PATCH:
                 throw new UnsupportedOperationException("PATCH not yet supported by async http client");
             case DELETE:
                 restClient.delete(url, delegate);
                 break;
             case HEAD:
                 restClient.head(url, delegate);
                 break;
         }
     }
 
     public void setRestClient(RestClient restClient) {
         this.restClient = restClient;
     }
 
     @SuppressWarnings("unchecked")
     private <T> void prepareDelegate(Callback<T> delegate, Object[] args) {
         if (containerTarget != null && rawContainer != null) {
             delegate.setResponseIsCollection(Collection.class.isAssignableFrom((Class<?>) rawContainer));
             delegate.setCollectionType((Class<? extends Collection>) rawContainer);
             delegate.setTargetClass((Class<T>) containerTarget);
         } else {
             delegate.setTargetClass((Class<T>) targetType);
         }
         if (headers.size() > 0) {
             org.apache.http.Header[] headersArray = new org.apache.http.Header[headers.size()];
             int i = 0;
             for (Map.Entry<Integer, String> headerEntry : headers.entrySet()) {
                 String value = args[headerEntry.getKey()].toString();
                 headersArray[i] = new BasicHeader(headerEntry.getValue(), value);
                 i++;
             }
             delegate.setAdditionalHeaders(headersArray);
         }
         CacheInfo cacheInfo;
         if (cached != null && delegate.getCacheInfo() == null) {
             cacheInfo = new CacheInfo(!cached.key().equals(StringUtils.EMPTY) ? cached.key() : null, cached.policy(), cached.timeToLive());
         } else {
             cacheInfo = CacheInfo.NONE;
         }
         delegate.setCacheInfo(cacheInfo);
     }
 
 }

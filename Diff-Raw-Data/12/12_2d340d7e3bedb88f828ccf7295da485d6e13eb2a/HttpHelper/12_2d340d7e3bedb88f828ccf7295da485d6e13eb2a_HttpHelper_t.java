 package no.henning.restful.utils;
 
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.net.URL;
 
 import no.henning.restful.annotation.BasicAuthenticateWith;
 import no.henning.restful.auth.BasicAuthentication;
 import no.henning.restful.http.builder.RestHttpRequestDetail;
 import no.henning.restful.http.method.HttpDelete;
 import no.henning.restful.http.status.HttpRestResponse;
 import no.henning.restful.model.Model;
 import no.henning.restful.service.RestService;
 import no.henning.restful.service.annotation.DELETE;
 import no.henning.restful.service.annotation.GET;
 import no.henning.restful.service.annotation.POST;
 import no.henning.restful.service.annotation.PUT;
 import no.henning.restful.service.annotation.Url;
 
 import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpPut;
 import org.apache.http.client.methods.HttpUriRequest;
 
 import android.util.Log;
 
 public class HttpHelper {
 	public static RestHttpRequestDetail buildRequestFromProxyMethod(Method method, Object[] arguments) {
 		if (method == null)
 			return null;
 
 		Class<? extends Model> model = GenericHelper.getModelFromProxyMethod(method);
 
 		String httpVerb = getHttpRequestVerbFromProxyMethod(method);
 		String path = ProxyHelper.getAbsolutePathFromProxyMethod(method, arguments);
 
 		return new RestHttpRequestDetail(model, path, httpVerb, null);
 	}
 
 	public static String getHttpRequestVerbFromProxyMethod(Method method) {
 		if (method == null)
 			return null;
 
 		Annotation[] annotations = method.getAnnotations();
 
 		if (annotations.length <= 0)
 			return null;
 
 		for (Annotation annotation : annotations) {
 			if (annotation instanceof GET)
 				return "GET";
 
 			if (annotation instanceof POST)
 				return "POST";
 
 			if (annotation instanceof PUT)
 				return "PUT";
 
 			if (annotation instanceof DELETE)
 				return "DELETE";
 		}
 
 		return null;
 	}
 
 	public static String getAbsolutePathFromModel(Class<? extends Model> model) {
 		if (model == null)
 			return null;
 
 		String servicePath = getServicePathFromModel(model);
 		String modelPath = GenericHelper.getResourcePathFromModel(model);
 
 		return String.format("%s%s", servicePath, modelPath);
 	}
 
 	public static String getServicePathFromModel(Class<? extends Model> model) {
 		if (model == null)
 			return null;
 
 		Class<? extends RestService> restService = GenericHelper.getRestServiceFromModel(model);
 
 		String servicePath = getServicePathFromRestService(restService);
 		servicePath = fixServicePath(servicePath);
 
 		return servicePath;
 	}
 
 	public static String fixServicePath(String path) {
 		if (path.endsWith("/"))
 			return path;
 
 		return String.format("%s/", path);
 	}
 
 	public static String getServicePathFromRestService(Class<? extends RestService> service) {
 		if (service == null)
 			return null;
 
 		Url urlAnnotation = GenericHelper.getUrlAnnotationFromClass(service);
 
 		return urlAnnotation.value();
 	}
 
 	public static String getServiceSuffixFromRestService(Class<? extends RestService> service) {
 		if (service == null)
 			return null;
 
 		Url urlAnnotation = GenericHelper.getUrlAnnotationFromClass(service);
 
 		if (urlAnnotation == null)
 			return null;
 
 		return urlAnnotation.suffix();
 	}
 
 	public static HttpUriRequest buildHttpUriRequestFromUrlAndMethod(String url, String verb) {
		url = url.replace(" ", "%20");
 		if (verb.equalsIgnoreCase("GET"))
 			return new HttpGet(url);
 		else if (verb.equalsIgnoreCase("POST"))
 			return new HttpPost(url);
 		else if (verb.equalsIgnoreCase("PUT"))
 			return new HttpPut(url);
 		else if (verb.equalsIgnoreCase("DELETE"))
 			return new HttpDelete(url);
 
 		return null;
 	}
 
 	public static String getBasicAuthenticationFromModel(Class<? extends Model> model) {
 		Class<? extends BasicAuthentication> basicAuthentication = null;
 
 		if (modelHasBasicAuthentication(model)) {
 			basicAuthentication = getBasicAuthenticationClass(model.getAnnotation(BasicAuthenticateWith.class));
 		} else {
 			// Let's try and see if the RestService has applied a global authentication
 			Class<? extends RestService> restService = GenericHelper.getRestServiceFromModel(model);
 
 			if (!restServiceHasBasicAuthentication(restService))
 				return null;
 
 			basicAuthentication = getBasicAuthenticationClass(restService.getAnnotation(BasicAuthenticateWith.class));
 		}
 
 		if (basicAuthentication == null)
 			return null;
 
 		Log.d("restful", "getBasicAuthenticationFromModel: Getting Auth string");
 		return getBasicAuthenticationFromAuthenticationClass(basicAuthentication);
 	}
 
 	public static Class<? extends BasicAuthentication> getBasicAuthenticationClass(BasicAuthenticateWith annotation) {
 		if (annotation == null)
 			return null;
 
 		return annotation.value();
 	}
 
 	public static String getBasicAuthenticationFromAuthenticationClass(
 			Class<? extends BasicAuthentication> authenticationClass) {
 		try {
 			Field encodedStringField = authenticationClass.getSuperclass().getDeclaredField("encodedString");
 
 			// Make it accessible
 			encodedStringField.setAccessible(true);
 
 			return ((String) encodedStringField.get(null)).trim();
 		} catch (IllegalArgumentException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (NoSuchFieldException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return null;
 	}
 
 	public static boolean modelHasBasicAuthentication(Class<? extends Model> model) {
 		return classHasBasicAuthentication(model);
 	}
 
 	public static boolean restServiceHasBasicAuthentication(Class<? extends RestService> restService) {
 		return classHasBasicAuthentication(restService);
 	}
 
 	public static boolean classHasBasicAuthentication(Class<?> clazz) {
 		BasicAuthenticateWith authentication = clazz.getAnnotation(BasicAuthenticateWith.class);
 		return authentication == null ? false : true;
 	}
 
 	/**
 	 * 
 	 * HTTP RESPONSE
 	 * 
 	 */
 
 	public static boolean isSuccessfulResponse(HttpRestResponse response) {
 		int statusCode = response.getStatusCode();
 
 		return statusCode >= 200 && statusCode < 300;
 	}
 
 }

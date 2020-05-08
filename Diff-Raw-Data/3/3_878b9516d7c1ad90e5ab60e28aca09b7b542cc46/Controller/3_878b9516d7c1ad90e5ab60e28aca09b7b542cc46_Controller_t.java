 package devkit.mvc;
 
 import devkit.utils.Binder;
 import play.api.templates.Html;
 import play.libs.F.Option;
 import play.mvc.Http.MultipartFormData;
 import play.mvc.Http.MultipartFormData.FilePart;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import static play.libs.F.None;
 import static play.libs.F.Some;
 
 public class Controller extends play.mvc.Controller{
 
     private static final String HTTP_PARAMS_KEY = "http.params";
 
 	public static String param (String key){
 
 		return param (key, String.class);
 	}
 	
 	@SuppressWarnings("unchecked")
 	public static <T> T param (String key, Class<T> clazz){
 
         if ("multipart/form-data".equals(request().getHeader("CONTENT-TYPE"))){
 
             MultipartFormData multipartFormData = request().body().asMultipartFormData();
 
             if (FilePart.class.equals(clazz)){
 
                 return (T) multipartFormData.getFile(key);
 
             } else if (File.class.equals(clazz)){
 
                 return (T) Option(
                     multipartFormData.getFile(key)
                 ).getOrElse(null);
             }
         }
 		
 		return param (getParams(), key, clazz);
 	}
 	
 	@SuppressWarnings("unchecked")
 	public static <T> T[] params(String key, Class<T> clazz){
 
         if ("multipart/form-data".equals(request().getHeader("CONTENT-TYPE"))){
 
             MultipartFormData multipartFormData = request().body().asMultipartFormData();
 
             if (FilePart.class.equals(clazz) ||
                     File.class.equals(clazz)){
 
                 List<Object> parts = new ArrayList<Object>();
                 for (FilePart filePart : multipartFormData.getFiles()){
                     if (key.equals(filePart.getKey())){
                         parts.add(FilePart.class.equals(clazz) ? filePart : filePart.getFile());
                     }
                 }
                 return (T[]) parts.toArray();
             }
         }
 		
 		return params(getParams(), key, clazz);
 	}
 	
 	public static <T> T param (Map<String,String[]> params, String key, Class<T> clazz){
 		
 		String[] values = params.get(key);
 		if (values != null && values.length > 0){
 
 			String stringValue = values[0];
 			return Binder.bind(stringValue, clazz);
 		}
 		
 		return null;
 	}
 	
 	public static <T> T[] params(Map<String,String[]> params, String key, Class<T> clazz){
 		
 		String[] values = params.get(key);
 		if (values != null && values.length > 0){
 
 			return Binder.bind(values, clazz);
 		}
 		
 		return null;
 	}
 
     private static Map<String,String[]> getParams(){
 
         String method = request().method();
         Map<String,String[]> params = (Map<String,String[]>) ctx().args.get(HTTP_PARAMS_KEY);
 
         if (params == null){
 
             if ("GET".equals(method)){
                 params = request().queryString();
             } else if ("POST".equals(method)){
                 if ("multipart/form-data".equals(request().getHeader("CONTENT-TYPE"))){
                     MultipartFormData multipartFormData = request().body().asMultipartFormData();
                     params = multipartFormData.asFormUrlEncoded();
                 } else {
                     params = request().body().asFormUrlEncoded();
                 }
            }
            if (params == null){
                 params = new HashMap<String, String[]>();
             }
             ctx().args.put(HTTP_PARAMS_KEY, params);
         }
         return params;
     }
 
 	public static <T> Option<T> Option(T value){
 
 		if (value != null){
 			return Some(value);
 		}
 		return None();
 	}
 
     public static Html trim(Html html){
         return new play.api.templates.Html(
             new scala.collection.mutable.StringBuilder(html.body().replaceAll("^\\r?\\n?", ""))
         );
     }
 }

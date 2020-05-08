 package eu.trentorise.smartcampus.android.common;
 
 import java.util.ArrayList;
 import java.util.List;
import java.util.Map;
 
 import org.codehaus.jackson.map.DeserializationConfig;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.map.SerializationConfig;
 import org.codehaus.jackson.map.introspect.NopAnnotationIntrospector;
 import org.codehaus.jackson.type.TypeReference;
 
 public class Utils {
     private static ObjectMapper fullMapper = new ObjectMapper();
     static {
         fullMapper.setAnnotationIntrospector(NopAnnotationIntrospector.nopInstance());
         fullMapper.configure(DeserializationConfig.Feature.READ_ENUMS_USING_TO_STRING, true);
         fullMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
         fullMapper.configure(DeserializationConfig.Feature.READ_ENUMS_USING_TO_STRING, true);
 
         fullMapper.configure(SerializationConfig.Feature.WRITE_ENUMS_USING_TO_STRING, true);
         fullMapper.configure(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS, false);
     }
 
 	public static <T> T convert(Object o, TypeReference<T> type) {
 		try {
 			return fullMapper.convertValue(o, type);
 		} catch (Exception e) {
 			return null;
 		}
 	}
 	public static <T> T convertJSON(String s, TypeReference<T> type) {
 		try {
 			return fullMapper.readValue(s, type);
 		} catch (Exception e) {
 			return null;
 		}
 	}
 	
 	public static String convertToJSON(Object data) {
 		try {
 			return fullMapper.writeValueAsString(data);
 		} catch (Exception e) {
 			return "";
 		}
 	}
 	public static <T> T convertJSONToObject(String body, Class<T> cls) {
 		try {
 			return fullMapper.readValue(body, cls);
 		} catch (Exception e) {
 			return null;
 		}
 	}
	@SuppressWarnings("rawtypes")
 	public static <T> List<T> convertJSONToObjects(String body, Class<T> cls) {
 		try {
			List<Map> list = fullMapper.readValue(body, new TypeReference<List<Map>>() { });
 			List<T> result = new ArrayList<T>();
			for (Map map : list) {
				result.add(fullMapper.convertValue(map,cls));
 			}
 			return result;
 		} catch (Exception e) {
 			return null;
 		}
 	}
 
 	public static <T> T convertObjectToData(Class<T> cls, Object o) {
 		try {
 			return fullMapper.convertValue(o, cls);
 		} catch (Exception e) {
 			return null;
 		}
 	}
 
 }

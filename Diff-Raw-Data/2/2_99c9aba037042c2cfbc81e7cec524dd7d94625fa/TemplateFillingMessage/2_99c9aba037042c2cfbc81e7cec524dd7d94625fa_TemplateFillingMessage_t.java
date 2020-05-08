 
 package axirassa.services.util;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import axirassa.util.AutoSerializingObject;
 
 public class TemplateFillingMessage extends AutoSerializingObject {
 	private static final long serialVersionUID = 858168230713875524L;
 
 	private final HashMap<String, Object> attributeMap = new HashMap<String, Object>();
 
 
 	public void addAttribute(String key, Object value) {
 		attributeMap.put(key, value);
 	}
 
 
 	public void addAttributes(Map<String, Object> attributes) {
		attributeMap.putAll(attributes);
 	}
 
 
 	public HashMap<String, Object> getAttibuteMap() {
 		return attributeMap;
 	}
 
 }

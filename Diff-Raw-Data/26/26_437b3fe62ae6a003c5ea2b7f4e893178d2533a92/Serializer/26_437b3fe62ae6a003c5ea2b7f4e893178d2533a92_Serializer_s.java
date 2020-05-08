 package com.tantaman.ferox.webfinger;
 
 import java.util.List;
 import java.util.Map;
 
 import com.tantaman.ferox.webfinger.entry.IDynamicWebfingerEntry;
 
 /**
  * Special case serializer for webfinger
  * so we don't have to bring in any dependencies.
  * @author tantaman
  *
  */
 public class Serializer {
 	public String serialize(IDynamicWebfingerEntry entry) {
 		StringBuilder result = new StringBuilder();
 		
 		result.append("{\"subject\":\"")
 			.append(entry.getSubject()).append("\"")
 			.append(",")
 		.append("\"aliases\":");
 			serialize(entry.getAliases(), result)
 			.append(",")
 		.append("\"properties\":");
 			serialize(entry.getProperties(), result)
 			.append(",")
 		.append("\"links\":");
 			serializeObjects(entry.getLinks(), result)
 		.append("}");
 		
 		return result.toString();
 	}
 	
 	public StringBuilder serialize(List<String> list, StringBuilder buf) {		
 		buf.append("[");
 		
 		boolean first = true;
 		for (String entry : list) {
 			if (!first)
 				buf.append(",");
 			else
 				first = false;
 			
 			buf.append("\"").append(entry).append("\"");
 		}
 		
 		buf.append("]");
 		
 		return buf;
 	}
 	
 	public StringBuilder serialize(Map<String,String> props, StringBuilder buf) {
 		buf.append("{");
 		
 		boolean first = true;
 		for (Map.Entry<String, String> entry : props.entrySet()) {
 			if (!first)
 				buf.append(",");
 			else
 				first = false;
 			
 			buf.append("\"").append(entry.getKey()).append("\":\"").append(entry.getValue()).append("\"");
 		}
 		
 		buf.append("}");
 		
 		return buf;
 	}
 	
 	public StringBuilder serializeObjects(List<Map<String, Object>> entries, StringBuilder buf) {
 		buf.append("[");
 		
 		boolean first = true;
 		for (Map<String,Object> entry : entries) {
 			if (!first) buf.append(","); else first = false;
 			serializeObject(entry, buf);
 		}
 		
 		buf.append("]");
 		
 		return buf;
 	}
 	
 	public StringBuilder serializeObject(Map<String,Object> props, StringBuilder buf) {
 		buf.append("{");
 		
 		boolean first = true;
 		for (Map.Entry<String, Object> entry : props.entrySet()) {
 			if (!first) buf.append(","); else first = false;
 			
 			buf.append("\"").append(entry.getKey()).append("\":");
 			
 			if (entry.getValue() instanceof Map) {
				serializeObject((Map)entry.getValue(), buf);
 			} else {
 				buf.append("\"").append(entry.getValue()).append("\"");
 			}
 		}
 		
 		buf.append("}");
 		
 		return buf;
 	}
 }

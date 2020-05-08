 package info.liberitas.wss.messaging;
 
 import info.liberitas.wss.api.WebSocketHandler;
 import info.liberitas.wss.annotation.Handler;
 import info.liberitas.wss.annotation.HandlerSet;
 import info.liberitas.wss.routing.ParameterDescriptor;
 import info.liberitas.wss.routing.RequestData;
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.node.ArrayNode;
 
 import java.io.IOException;
 import java.io.StringWriter;
 
 /**
  * Created by IntelliJ IDEA.
  * User: Alexander <estliberitas> Makarenko
  * Date: 29.04.12
  * Time: 11:15
  */
 public class JSONMessageParser implements MessageParser {
 	public static ObjectMapper mapper;
 
 	static {
 		mapper = new ObjectMapper();
 	}
 
 	@Override
 	public RequestData parseRequest(String request) {
 		try {
 			JsonNode jsonNode = mapper.readValue(request, JsonNode.class);
 			String route = jsonNode.path("route").getTextValue();
 
 			return new RequestData(route, jsonNode.path("request"));
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	@Override
 	public Object[] prepareParams(WebSocketHandler wHandler, ParameterDescriptor[] descriptors, Object request) {
 		Object[] paramValues = new Object[descriptors.length];
 		JsonNode requestNode = (JsonNode) request;
 
 		int i = -1;
 		while (++i < descriptors.length) {
 			ParameterDescriptor pi = descriptors[i];
 			if (pi == null) {
 				paramValues[i] = null;
 				continue;
 			}
 
 			JsonNode paramNode = requestNode.path(pi.name);
			Object value;
 			if (pi.paramClass == String.class) {
 				value = paramNode.getTextValue();
 			} else if (pi.paramClass == int.class || pi.paramClass == Integer.class) {
 				value = paramNode.getIntValue();
 			} else if (pi.paramClass == boolean.class || pi.paramClass == Boolean.class) {
 				value = paramNode.getBooleanValue();
 			} else if (pi.paramClass == JsonNode.class) {
 				value = paramNode;
 			} else if (pi.paramClass == ArrayNode.class) {
 				value = paramNode;
 			} else if (pi.paramClass == Handler.class) {
 				value = wHandler;
 			} else if (pi.paramClass == HandlerSet.class) {
 				value = wHandler.getService().getHandlers();
 			} else {
 				try {
 					if (pi.name == null) {
 						value = mapper.readValue(requestNode, pi.paramClass);
 					} else {
 						value = mapper.readValue(paramNode, pi.paramClass);
 					}
 				} catch (IOException e) {
 					throw new RuntimeException("Error mapping json node to class" + pi.paramClass, e);
 				}
 			}
 
 			paramValues[i] = value;
 			if (!pi.required) {
 				continue;
 			}
 
 			if (value == null) {
 				throw new IllegalArgumentException("Field " + pi.name + " is required for this request.");
 			}
 		}
 
 		return paramValues;
 	}
 
 	@Override
 	public String makeResponse(Object response) {
 		StringWriter sw = new StringWriter();
 		try {
 			mapper.writeValue(sw, response);
 			return sw.toString();
 		} catch (IOException e) {
 			return null;
 		}
 	}
 }

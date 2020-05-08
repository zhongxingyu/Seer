 package uk.frequency.glance.server.service.util;
 
 import java.lang.reflect.Type;
 import java.util.HashMap;
 import java.util.Map;
 
 import uk.frequency.glance.server.transfer.event.EventDTO;
 import uk.frequency.glance.server.transfer.event.ListenEventDTO;
 import uk.frequency.glance.server.transfer.event.MoveEventDTO;
 import uk.frequency.glance.server.transfer.event.StayEventDTO;
 import uk.frequency.glance.server.transfer.event.TellEventDTO;
 import uk.frequency.glance.server.transfer.trace.ListenTraceDTO;
 import uk.frequency.glance.server.transfer.trace.PositionTraceDTO;
 import uk.frequency.glance.server.transfer.trace.TraceDTO;
 
 import com.google.gson.Gson;
 import com.google.gson.JsonDeserializationContext;
 import com.google.gson.JsonDeserializer;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonParseException;
 import com.google.gson.JsonSerializationContext;
 import com.google.gson.JsonSerializer;
 
 public class JsonHierarchyTypeAdapter<T> implements JsonSerializer<T>, JsonDeserializer<T> {
 
 	final static String CLASS_PROPERTY = "@class";
 	private Gson gson = new Gson();
 	private Map<String, Class<? extends T>> map;
 	private Map<Class<? extends T>, String> unmap;
 
 	public JsonHierarchyTypeAdapter(Map<String, Class<? extends T>> map, Map<Class<? extends T>, String> backMap) {
 		super();
 		this.map = map;
 		this.unmap = backMap;
 	}
 
 	@Override
 	public JsonElement serialize(T src, Type type, JsonSerializationContext context) {
 		JsonElement elm = gson.toJsonTree(src);
 		String classValue = unmap.get(src.getClass());
 		if(classValue == null){
 			throw new JsonParseException("Unable to parse class: " + src.getClass().getName());
 		}
 		elm.getAsJsonObject().addProperty(CLASS_PROPERTY, classValue);
 		return elm;
 	}
 
 	@Override
 	public T deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
 		JsonObject jsonObj = json.getAsJsonObject();
 		JsonElement jsonElm = jsonObj.get(CLASS_PROPERTY);
 		if(jsonElm == null){
 			throw new JsonParseException("Unable to parse JSON. Missing @class property.");
 		}
 		Class<? extends T> subclass = map.get(jsonElm.getAsString());
 		if(subclass == null){
			throw new JsonParseException("Unable to parse JSON. @class property has an invalid value: " + jsonElm.getAsString() + ". Valid values are: " + map.keySet());
 		}
 		T obj = gson.fromJson(json, subclass);
 		return obj;
 	}
 
 	public static JsonHierarchyTypeAdapter<EventDTO> getEventInstance() {
 		Map<String, Class<? extends EventDTO>> map = new HashMap<String, Class<? extends EventDTO>>();
 		map.put("TELL_EVENT", TellEventDTO.class);
 		map.put("STAY_EVENT", StayEventDTO.class);
 		map.put("MOVE_EVENT", MoveEventDTO.class);
 		map.put("LISTEN_EVENT", ListenEventDTO.class);
 		Map<Class<? extends EventDTO>, String> unmap = new HashMap<Class<? extends EventDTO>, String>();
 		unmap.put(TellEventDTO.class, "TELL_EVENT");
 		unmap.put(StayEventDTO.class, "STAY_EVENT");
 		unmap.put(MoveEventDTO.class, "MOVE_EVENT");
 		unmap.put(ListenEventDTO.class, "LISTEN_EVENT");
 		return new JsonHierarchyTypeAdapter<EventDTO>(map, unmap);
 	}
 
 	public static JsonHierarchyTypeAdapter<TraceDTO> getTraceInstance() {
 		Map<String, Class<? extends TraceDTO>> map = new HashMap<String, Class<? extends TraceDTO>>();
 		map.put("POSITION_TRACE", PositionTraceDTO.class);
 		map.put("LISTEN_TRACE", ListenTraceDTO.class);
 		Map<Class<? extends TraceDTO>, String> unmap = new HashMap<Class<? extends TraceDTO>, String>();
 		unmap.put(PositionTraceDTO.class, "POSITION_TRACE");
 		unmap.put(ListenTraceDTO.class, "LISTEN_TRACE");
 		return new JsonHierarchyTypeAdapter<TraceDTO>(map, unmap);
 	}
 
 }

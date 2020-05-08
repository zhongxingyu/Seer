 package org.jpc.examples.osm.model.gsonconverters;
 
 import java.lang.reflect.Type;
 
 import org.jpc.examples.osm.model.Node;
 import org.jpc.examples.osm.model.Way;
 
 import com.google.gson.JsonArray;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonSerializationContext;
 import com.google.gson.JsonSerializer;
 
 public class WayGsonConverter implements JsonSerializer<Way> {
 
 	@Override
 	public JsonElement serialize(Way way, Type srcType, JsonSerializationContext context) {
 		JsonObject jsonWay = new JsonObject();
 		jsonWay.addProperty(GeoJsonConstants.TYPE, GeoJsonConstants.FEATURE);
 		jsonWay.addProperty(GeoJsonConstants.ID, way.getId());
 		jsonWay.add(GeoJsonConstants.PROPERTIES, GsonUtil.toJsonObject(way.getTags()));
 		
 		JsonObject jsonGeometry = new JsonObject();
 		jsonGeometry.addProperty(GeoJsonConstants.TYPE, GeoJsonConstants.LINE_STRING);
 		
 		JsonArray jsonCoordinates = new JsonArray();
 		
 		for(Node node : way.nodes()) {
			jsonCoordinates.add(new NodeGsonConverter().serialize(node, node.getClass(), context));
 		}
 		jsonGeometry.add(GeoJsonConstants.COORDINATES, jsonCoordinates);
 		
 		jsonWay.add(GeoJsonConstants.GEOMETRY, jsonGeometry);
 		return jsonWay;
 	}
 
 }

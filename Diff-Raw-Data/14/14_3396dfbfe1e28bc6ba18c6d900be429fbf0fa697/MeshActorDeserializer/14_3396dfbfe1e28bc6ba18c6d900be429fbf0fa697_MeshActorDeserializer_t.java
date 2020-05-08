 package com.razh.tiling.json;
 
 import java.lang.reflect.Type;
 
 import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Mesh;
 import com.google.gson.JsonDeserializationContext;
 import com.google.gson.JsonDeserializer;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonParseException;
import com.razh.tiling.Geometry;
 import com.razh.tiling.MeshActor;
 
 public class MeshActorDeserializer implements JsonDeserializer<MeshActor> {
 
 	@Override
 	public MeshActor deserialize(JsonElement json, Type typeOfT,
 			JsonDeserializationContext context) throws JsonParseException {
 
 		JsonObject object = json.getAsJsonObject();
 
 		float x = object.get("x").getAsFloat();
 		float y = object.get("y").getAsFloat();
 
 		float width = object.get("width").getAsFloat();
 		float height = object.get("height").getAsFloat();
 		float rotation = object.get("rotation").getAsFloat();
 
 		int sides = object.get("sides").getAsInt();
 		Color color = (Color) context.deserialize(object.get("color"), Color.class);
		Color altColor = (Color) context.deserialize(object.get("altColor"), Color.class);

		Mesh mesh = Geometry.createBicolorBipyramid(sides, color, altColor);
 
 		MeshActor actor = new MeshActor();
 		actor.setPosition(x, y);
		actor.setWidth(width);
		actor.setHeight(height);
		actor.setRotation(rotation);
		actor.setMesh(mesh);
 		actor.setColor(color);
 
 		return actor;
 	}
 
 }

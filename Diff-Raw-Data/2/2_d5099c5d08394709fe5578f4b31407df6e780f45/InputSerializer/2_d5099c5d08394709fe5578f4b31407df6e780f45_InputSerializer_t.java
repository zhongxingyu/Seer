 package org.uncertweb.et.json;
 
 import java.lang.reflect.Type;
 
 import org.uncertweb.et.parameter.ConstantInput;
 import org.uncertweb.et.parameter.Input;
 import org.uncertweb.et.parameter.VariableInput;
 
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonSerializationContext;
 import com.google.gson.JsonSerializer;
 
 public class InputSerializer implements JsonSerializer<Input> {
 	public JsonElement serialize(Input src, Type typeOfSrc, JsonSerializationContext context) {
 		// object
 		JsonObject obj = new JsonObject();
 		obj.addProperty("identifier", src.getIdentifier());
 		
 		// description
 		if (src.getDescription() != null) {
 			obj.add("description", context.serialize(src.getDescription()));
 		}
 
 		// range or value
 		if (src instanceof VariableInput) {
 			// cast
 			VariableInput vi = (VariableInput)src;
 			
 			// create range object
 			JsonObject range = new JsonObject();
 			obj.add("range", range);
 			
 			// add min/max
 			range.addProperty("min", vi.getMin());
 			range.addProperty("max", vi.getMax());
 		}
		else if (src instanceof ConstantInput) {
 			// cast
 			ConstantInput ci = (ConstantInput)src;
 			
 			// add value
 			obj.addProperty("value", ci.getValue());
 		}
 
 		return obj;
 	}
 }

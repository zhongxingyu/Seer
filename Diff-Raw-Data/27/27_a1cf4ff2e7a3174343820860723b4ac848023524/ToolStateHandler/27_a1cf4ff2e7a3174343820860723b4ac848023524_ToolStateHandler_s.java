 package Specification.Galaxy;
 
 import java.lang.reflect.Type;
 
 import com.google.gson.JsonDeserializationContext;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonParseException;
 import com.google.gson.JsonSerializationContext;
 
import FileOps.GSON.GSONFileHandler;
 import FileOps.GSON.GSONHandler;
 import Galaxy.Tree.Workflow.ToolState;
 
 public class ToolStateHandler extends GSONHandler{
 
 	@Override
 	public JsonElement serialize(Object arg0, Type arg1,
 			JsonSerializationContext arg2) {
 		// TODO Auto-generated method stub
		GSONFileHandler<ToolState> gson = new GSONFileHandler<ToolState>(ToolState.class);
 		
 		return null;
 	}
 
 	@Override
 	public Object deserialize(JsonElement arg0, Type arg1,
 			JsonDeserializationContext arg2) throws JsonParseException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 }

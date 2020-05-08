 package tdanford.json.schema;
 
 import java.io.*;
 
 import org.json.JSONObject;
 
 public class JSONFileType implements JSONType {
 	
 	private File file;
 	private JSONType fileType;
 	private SchemaEnv env;
 	
 	public JSONFileType(SchemaEnv env, File f) { 
 		this.env = env;
 		file = f;
 		fileType = null;
 	}
 	
 	public void loadType() { 
 		try {
 			FileReader reader = new FileReader(file);
			JSONObject obj = new JSONObject(reader);
 			fileType = new JSONObjectType(env, obj);
 			
 		} catch (IOException e) { 
 			fileType = new JSONType.Empty();
 			throw new IllegalArgumentException(file.getName(), e);
 			
 		} catch (SchemaException e) {
 			fileType = new JSONType.Empty();
 			throw new IllegalArgumentException(file.getName(), e);
 		}
 	}
 
 	public boolean contains(Object obj) {
 		if(fileType==null) { 
 			loadType();
 		}
 		return fileType.contains(obj);
 	}
 
 	public java.lang.String explain(Object obj) {
 		if(fileType==null) { loadType(); }
 		return fileType.explain(obj);
 	}
 }

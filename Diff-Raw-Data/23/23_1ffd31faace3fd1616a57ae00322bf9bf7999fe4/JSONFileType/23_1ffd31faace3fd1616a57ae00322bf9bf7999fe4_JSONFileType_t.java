 package tdanford.json.schema;
 
 import static java.lang.String.*;
 import java.io.*;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 /**
  * Lazy type loading for JSON schema files.  
  * 
  * Objects of this class correspond to files on the filesystem, each given by a {@link java.io.File},
  * which contain JSON Schema expressions.  However, the JSON schema expression is only loaded and parsed
  * the first time that either <tt>contains</tt> or <tt>explain</tt> is called on this object: <em>lazy</em> 
  * loading.  This is so that we can support resolution of mutually-recursive types, from files in a single
  * directory. 
  * 
  * @author Timothy Danford
  *
  */
 public class JSONFileType extends AbstractType {
 	
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
 			StringBuilder buffer = new StringBuilder();
 			int r; 
 			while((r = reader.read()) != -1) { 
 				buffer.append((char)r);
 			}
 			java.lang.String jsonString = buffer.toString();
 			reader.close();
 			StringReader stringReader = new StringReader(jsonString);
 			//System.out.println(format("JSON String: \n%s", jsonString));
 			
 			JSONObject obj = new JSONObject(jsonString);
 			fileType = new JSONObjectType(env, obj);
 			
 		} catch (IOException e) { 
 			fileType = new JSONType.Empty();
 			throw new IllegalArgumentException(file.getName(), e);
 			
 		} catch (SchemaException e) {
 			fileType = new JSONType.Empty();
 			throw new IllegalArgumentException(file.getName(), e);
 		} catch (JSONException e) {
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

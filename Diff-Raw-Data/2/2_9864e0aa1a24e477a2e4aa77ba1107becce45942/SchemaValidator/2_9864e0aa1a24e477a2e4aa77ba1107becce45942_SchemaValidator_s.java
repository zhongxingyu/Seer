 package tdanford.json.schema;
 
 import java.io.*;
 import java.util.*;
 
 import org.json.JSONObject;
 
 public class SchemaValidator { 
 	
 	public static void main(String[] args) { 
 		if(args.length != 2) { 
 			System.err.println("USAGE: SchemaValidator <schema-file> <json-value-file>");
			System.exit();
 		}
 		try { 
 			File schemaFile = new File(args[0]);
 			File valueFile = new File(args[1]);
 			JSONType type = new JSONFileType(new SchemaEnv(), schemaFile);
 			JSONObject value = new JSONObject(new FileReader(valueFile));
 			System.out.println(String.valueOf(type.contains(value)));
 		} catch(Throwable t) {
 			System.err.println(String.format("ERROR: %s", t.getMessage()));
 		}
 	}
 
 	private SchemaEnv env;
 	
 	public SchemaValidator() { 
 		this(new SchemaEnv());
 	}
 	
 	public SchemaValidator(File dir) { 
 		this(new SchemaEnv(dir));
 	}
 	
 	public SchemaValidator(SchemaEnv e) { 
 		this.env = e;
 	}
 	
 	public String explain(Object obj, String typeName) { 
 		JSONType type = env.lookupType(typeName);
 		if(type==null) { throw new IllegalArgumentException(typeName); }
 		return type.explain(obj);		
 	}
 	
 	public boolean validate(Object obj, String typeName) {
 		JSONType type = env.lookupType(typeName);
 		if(type==null) { throw new IllegalArgumentException(typeName); }
 		return type.contains(obj);
 	}
 	
 	public void addObjectType(JSONObjectType type) {
 		if(type.getName() == null) { throw new IllegalArgumentException("No name in given type."); }
 		env.addType(type.getName(), type);
 	}
 }

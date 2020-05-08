 package tdanford.json.schema;
 
 import java.util.*;
 import java.util.regex.*;
 import java.io.*;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 public class SchemaEnv { 
 
 	private SchemaEnv parent;
 	private Map<String,JSONType> types;
 
 	public SchemaEnv() { 
 		parent = null;
 		types = new TreeMap<String,JSONType>();
 		types.put("string", new JSONType.String());
 		types.put("integer", new JSONType.Integer());
 		types.put("double", new JSONType.Double());
 		types.put("boolean", new JSONType.Boolean());
 	}
 	
 	public SchemaEnv(File dir) { 
 		this();
 		File[] lst = dir.listFiles(new FilenameFilter() {
 			public boolean accept(File dir, String name) {
 				return name.toLowerCase().endsWith(".js");
 			} 
 		});
 		
 		Pattern jsFilenamePattern= Pattern.compile("^(.*)\\.js$");
 		
 		for(File jsFile : lst) { 
 			Matcher jsMatcher = jsFilenamePattern.matcher(jsFile.getName());
 			if(jsMatcher.matches()) { 
 				String typeName = jsMatcher.group(1);
 				addType(typeName, new JSONFileType(this, jsFile));
 			} else { 
 				throw new IllegalArgumentException(String.format(jsFile.getName()));
 			}
 		}
 	}
 
 	public SchemaEnv(SchemaEnv p) {
 		this();
 		parent = p;
 	}
 
 	public JSONType lookupType(String name) { 
 		if(types.containsKey(name)) { 
 			return types.get(name);
 		} else if (parent != null) { 
 			return parent.lookupType(name);
 		} else { 
 			return null; 
 		}
 	}
 
 	/**
 	 * Primary entry point to the JSONType parsing framework.
 	 * This method builds JSONType object from the given argument, relative to the 
 	 * bindings in this environment.  String objects in the appropriate places
 	 * are looked up and converted to the corresponding bound JSONType objects.
 	 * 
 	 * Everything else is recursively evaluated, by type.
 	 * 
 	 * @param obj The schema expression, either a String or a JSONObject.
 	 * @throws SchemaException 
 	 * @throws IllegalArgumentException if the supplied argument is null, or if it is not of the correct type.
 	 * @return The JSONType object corresponding to the given schema expression.
 	 */
 	public JSONType evaluate(Object obj) throws SchemaException {
 		try { 
 			if(obj == null) { 
 				throw new IllegalArgumentException("null schema object.");
 
 			} else if(obj instanceof String) {
 				
 				// String objects are merely looked up in this environment, and returned
 				// if found (or a SchemaException thrown, if not).
 				
 				String name = (String)obj;
 				JSONType type = lookupType(name);
 				if(type == null) { 
 					throw new SchemaException(String.format("Unknown type name \"%s\"", name));
 				}
 				return type;
 
 			} else if (obj instanceof JSONObject) {
 				JSONObject json = (JSONObject)obj;
 				if(json.has("type")) { 
					String type = json.getString("type").toLowerCase();
 
 					if(type.equals("array")) {
 						return new JSONArrayType(new SchemaEnv(this), json);
 						
 					} else if (type.equals("object")) {
 						
 						// we pass children of this environment, i.e. 
 						//   new SchemaEnv(this)
 						// rather than just
 						//   this
 						// because JSONObjectType will bind itself into the Schema, and we don't 
 						// want those names to be globally visible -- only local visible to the 
 						// schema's sub-expressions.
 
 						return new JSONObjectType(new SchemaEnv(this), json);
 						
 					} else { 
 						JSONType tt = lookupType(type);
 						if(tt == null) { 
							throw new SchemaException(String.format("Unrecognized schema type: %s", type));
 						}
 						return tt;
 					}
 
 				} else { 
 					throw new SchemaException("Schema object doesn't contain a 'types' property.");				
 				}
 
 			} else { 
 				throw new IllegalArgumentException(obj.getClass().getSimpleName() + " isn't a valid schema type");
 			}
 		} catch(JSONException e) {
 			
 			// Right now, the only time this is thrown is if the JSONObject schema expression
 			// doesn't have a 'type' property.
 			throw new SchemaException(String.valueOf(obj), e);
 		}
 	}
 
	public void addType(String name, JSONType t) { 
 		if(types.containsKey(name)) { 
 			throw new IllegalArgumentException(String.format("Type for name %s already exists.", name));
 		}
 		types.put(name, t); 
 	}
 
 	public boolean containsType(String name) { return types.containsKey(name); }
 
 	public Iterator<String> names() { return new TreeSet<String>(types.keySet()).iterator(); }
 }

 package tdanford.json.schema;
 
 public class OptionalType implements JSONType {
 	
 	private JSONType innerType;
 	
 	public OptionalType(JSONType t) { 
 		innerType = t;
 	}
 
 	public boolean contains(Object obj) {
		return JSONObject.NULL.equals(obj) || innerType.contains(obj);
 	}
 
 	public java.lang.String explain(Object obj) {
 		return innerType.explain(obj);
 	}
 
 }

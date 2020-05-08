 package tdanford.json.schema;
 
 /**
  * Helper class for creation of new JSON types. Simply extend this class, and implement the <tt>contains</tt> method.
  * 
  * @author Timothy Danford
  **/
 public abstract class AbstractType implements JSONType { 
 	
 	public abstract boolean contains(Object o);
 
	public java.lang.String explain(Object obj) { return null; }
 	public boolean isOptional() { return false; }
 }

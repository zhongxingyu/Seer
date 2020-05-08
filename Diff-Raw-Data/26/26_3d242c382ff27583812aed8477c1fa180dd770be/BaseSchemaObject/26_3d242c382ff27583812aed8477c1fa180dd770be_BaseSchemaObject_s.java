 package togos.schemaschema;
 
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.Set;
 
 public class BaseSchemaObject implements SchemaObject, Comparable<SchemaObject>
 {
 	public final String name;
 	public String longName;
 	public final Map<Predicate,Set<Object>> properties = new LinkedHashMap<Predicate,Set<Object>>();
 	
 	public BaseSchemaObject( String name, String longName ) {
 		this.name = name;
 		this.longName = longName;
 	}
 		
 	public BaseSchemaObject( String name ) {
 		this( name, (String)null );
 	}
 	
 	public BaseSchemaObject( String name, Type type ) {
 		this( name );
 		PropertyUtil.add( properties, Predicates.IS_MEMBER_OF, type );
 	}
 	
 	@Override public String getName() { return name; }
 	@Override public String getLongName() { return longName; }
 	@Override public Map<Predicate, Set<Object>> getProperties() { return properties; }
 	
 	/**
 	 * Used by Type objects to implement the Type interface
 	 * Not intended to be useful by non-Type objects.
 	 **/
 	public Set<Type> getExtendedTypes() {
 		return PropertyUtil.getAll( properties, Predicates.EXTENDS, Type.class );
 	}
 	
	@Override public boolean equals( Object oth ) {
		return oth instanceof SchemaObject && name.equals(((SchemaObject)oth).getName());
	}
 	@Override public int compareTo(SchemaObject o) {
 		return name.compareTo(o.getName());
 	}
 }

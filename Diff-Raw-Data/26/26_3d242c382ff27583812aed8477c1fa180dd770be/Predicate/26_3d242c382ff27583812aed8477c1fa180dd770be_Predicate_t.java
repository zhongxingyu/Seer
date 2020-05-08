 package togos.schemaschema;
 
 import java.util.Set;
 
 import togos.schemaschema.parser.ast.Word;
 
 public class Predicate extends BaseSchemaObject
 {
 	public Predicate(String name) {
 		super(name);
 	}
 
 	public Predicate(String name, String longName) {
 		super(name, longName);
 	}
 
 	public Predicate(String name, String longName, Type objectType) {
 		super(name, longName);
 		this.addObjectType(objectType);
 	}
 	
 	public void addObjectType(Type t) {
 		PropertyUtil.add(properties, Predicates.OBJECTS_ARE_MEMBERS_OF, t);
 	}
 	
 	public Set<Type> getObjectTypes() {
 		return PropertyUtil.getAll(properties, Predicates.OBJECTS_ARE_MEMBERS_OF, Type.class);
 	}
 	
	@Override public String toString() {
 		return "predicate "+Word.quote(name);
 	}
 }

 package togos.schemaschema.namespaces;
 
 import static togos.schemaschema.namespaces.NSUtil.definePredicate;
 import togos.schemaschema.Namespace;
 import togos.schemaschema.Predicate;
 
 /**
  * Predicates that define how a class should be used within a program
  */
 public class Application
 {
 	public static final Namespace NS = Namespace.getInstance(NSUtil.SCHEMA_PREFIX+"Application/");
 	
 	private Application() { }
 	
 	//// Class predicates
 	
 	public static final Predicate HAS_DB_TABLE     = definePredicate(
 		NS, "has a database table", Types.BOOLEAN,
 		"indicates that there should be a database table with rows "+
 		"corresponding to instances of this class");
 	public static final Predicate HAS_REST_SERVICE = definePredicate(
 		NS, "has a REST service"  , Types.BOOLEAN,
 		"indicates that the this class's instances should be exposed via REST services");
 	public static final Predicate MEMBERS_ARE_PUBLIC = definePredicate(
 		NS, "members are public"  , Types.BOOLEAN,
 		"indicates that instances of this class are not secret and "+
 		"may be visible to the general public");
 	public static final Predicate MEMBER_SET_IS_PUBLIC = definePredicate(
		NS, "members are public"  , Types.BOOLEAN,
 		"indicates that the set of instances of this class is not secret and "+
 		"may be visible to the general public");
 	public static final Predicate MEMBERS_ARE_MUTABLE = definePredicate(
 		NS, "members are mutable"  , Types.BOOLEAN,
 		"indicates that instances of this class may be modified while retaining their identity");
 	public static final Predicate MEMBER_SET_IS_MUTABLE = definePredicate(
 		NS, "member set is mutable", Types.BOOLEAN,
 		"indicates that instances of this class may be added or deleted at runtime");
 	
 	//// Field predicates
 	
 	public static final Predicate IS_FAKE_FIELD    = definePredicate(
 		NS, "is fake field", Types.BOOLEAN,
 		"indicates that this field is in some way 'fake' and that assumptions "+
 		"made about non-fake fields (having a database column, etc) do not "+
 		"apply unless explicitly defined otherwise");
 	public static final Predicate HAS_DB_COLUMN    = definePredicate(
 		NS, "has a database column", Types.BOOLEAN,
 		"indicates that there should be a database column corresponding "+
 		"to this field");
 	public static final Predicate RETURNED_BY_REST_SERVICES = definePredicate(
 		NS, "is returned by REST services", Types.BOOLEAN,
 		"indicates that this field should be included in object "+
 		"representations returned by REST services");
 	public static final Predicate MAY_BE_USED_AS_REST_FILTER = definePredicate(
 		NS, "may be used as REST service filter", Types.BOOLEAN,
 		"indicates that REST services representing objects containing "+
 		"this field may be queried to filter by this field's value");
 	public static final Predicate MAY_BE_POSTED_TO_REST_SERVICES = definePredicate(
 		NS, "may be set via REST services", Types.BOOLEAN,
 		"indicates that this field may be initialized and/or updated "+
 		"(if not otherwise marked as immutable) via REST service calls");
 
 }

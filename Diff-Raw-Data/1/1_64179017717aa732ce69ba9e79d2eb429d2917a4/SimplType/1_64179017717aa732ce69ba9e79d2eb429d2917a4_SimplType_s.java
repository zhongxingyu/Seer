 /**
  * 
  */
 package ecologylab.serialization.types;
 
 import ecologylab.generic.ReflectionTools;
 import ecologylab.serialization.simpl_inherit;
 
 /**
  * Re-usable unit of the cross-language S.IM.PL type system.
  * 
  * This is the base class for ScalarType and CollectionType.
  * 
  * @author andruid
  */
 @simpl_inherit
 public abstract class SimplType<T> extends SimplBaseType
 implements CrossLanguageTypeConstants
 {
 	private Class<? extends T>	javaClass;
 	
 	/**
 	 * Short name of the type: without package.
 	 */
 	@simpl_scalar
 	String											simpleName;
 
 	/**
 	 * Fully qualified name of the Java type that this represents, including package.
 	 */
 	@simpl_scalar
 	private String							javaTypeName;
 	
 	/**
 	 * Name for declaring the type in C#.
 	 */
 	@simpl_scalar
 	private String							cSharpTypeName;
 	
 	/**
 	 * Name for declaring the type in Objective C.
 	 */
 	@simpl_scalar
 	private String							objectiveCTypeName;
 	
 	@simpl_scalar
 	private String							dbTypeName;
 	
 	/**
 	 * Package name, for non primitives.
 	 */
 	@simpl_scalar
 	private String							packageName;
 	
 	/**
 	 * Empty constructor for S.IM.PL Serialization.
 	 */
 	public SimplType()
 	{
 		
 	}
 	protected SimplType(Class<? extends T> javaClass, boolean isScalar, String cSharpTypeName, String objectiveCTypeName, String dbTypeName)
 	{
 		this(javaClass.isPrimitive() ? javaClass.getName() : deriveCrossPlatformName(javaClass, isScalar), 
 				javaClass, cSharpTypeName, objectiveCTypeName, dbTypeName);
 	}
 	/**
 	 * Run-time constructor. Create object for cross-platform type representation. 
 	 * Register this in TypeRegistry appropriate (collection() or scalar()).
 	 * 
 	 * @param javaClass TODO
 	 * @param cSharpTypeName 		Name for declaring the type in C#.
 	 * @param objectiveCTypeName Name for declaring the type in Objective C.
 	 * @param dbTypeName TODO
 	 */
 	protected SimplType(String name, Class<? extends T> javaClass, String cSharpTypeName, String objectiveCTypeName, String dbTypeName)
 	{
 		super(name);
 		
 		this.javaClass					= javaClass;
 		this.simpleName					= javaClass.getSimpleName();	
 		this.javaTypeName				= javaClass.getName();
 		
 		this.cSharpTypeName			= cSharpTypeName;
 		this.objectiveCTypeName	= objectiveCTypeName;
 
 		if (!javaClass.isPrimitive())
 			this.packageName			= javaClass.getPackage().getName();
 		
 		this.dbTypeName					= dbTypeName;
 
 		TypeRegistry.registerSimplType(this);
 	}
 	
 	/**
 	 * The full, qualified name of the class that this describes
 	 * @return
 	 */
 	@Override
 	public String getJavaTypeName()
 	{
 		return javaTypeName;
 	}
 	/**
 	 * The name to use when declaring a field in C# cross-compilation.
 	 * 
 	 * @return	cSharpTypeName, if one was passed in explicitly. otherwise, null.
 	 */
 	@Override
 	public String getCSharpTypeName()
 	{
 		return cSharpTypeName != null ? cSharpTypeName : javaTypeName;
 	}
 	/**
 	 * The name to use when declaring a field in Objective C cross-compilation.
 	 * 
 	 * @return	objectiveCTypeName, if one was passed in explicitly. otherwise, null.
 	 */
 	@Override
 	public String getObjectiveCTypeName()
 	{
 		return objectiveCTypeName;
 	}
 	/**
 	 * 
 	 * 
 	 * @return	Name of this type for database columns.
 	 */
 	@Override
 	public String getDbTypeName()
 	{
 		return dbTypeName;
 	}
 
 	/**
 	 * @return the simpleName
 	 */
 	public String getSimpleName()
 	{
 		return simpleName;
 	}
 
 	/**
 	 * Use the field in this, if it is set, or do something smart to derive a type name for C#.
 	 * 
 	 * @return	never null; some kind of a type name String.
 	 */
 	abstract public String deriveCSharpTypeName();
 
 	/**
 	 * Use the field in this, if it is set, or do something smart to derive a type name for C#.
 	 * 
 	 * @return	never null; some kind of a type name String.
 	 */
 	abstract public String deriveObjectiveCTypeName();
 	
 	abstract boolean isScalar();
 
 	static String deriveCrossPlatformName(Class javaClass, boolean isScalar)
 	{
 		String javaClassName 	= javaClass.getName();
 		return javaClassName.startsWith("java") ? 
 				(isScalar ? SIMPL_SCALAR_TYPES_PREFIX : SIMPL_COLLECTION_TYPES_PREFIX) + javaClass.getSimpleName() : javaClassName;
 	}
 
 	@Override
 	public String toString()
 	{
 		return getSimpleName() + ": crossPlatform=" + getName() + "\tjava=" + javaTypeName + "\tsimple=" + simpleName;
 	}
 	
 	public T getInstance()
 	{
 		return ReflectionTools.getInstance(javaClass);
 	}
 
 	public Class getJavaClass()
 	{
 		return javaClass;
 	}
 	
 
 }

 package translator;
 
 import java.util.LinkedHashMap;
 
 /**
  * The current setup for expressions requires this class.
  */
 abstract public class JavaType {
 	/**
 	 * If this class has been prepared and is ready for use (stops prepare() from being run more than once).
 	 */
 	private static boolean prepared = false;
 	
 	/**
 	 * The list of all types we know about, including primitives.
 	 */
 	private static LinkedHashMap<String, JavaType> types = new LinkedHashMap<String, JavaType>(); 
 	
 	/**
 	 * A class that represents primitives and their "inheritance".
 	 */
 	private static class Primitive extends JavaType {
 		/**
 		 * The name of the primitive type being represented.
 		 */
 		private String typeName;
 		
 		/**
 		 * The name of the cpp type for the primitive.
 		 */
 		private String cppTypeName;
 		
 		/**
 		 * The parent of this type. Null if there is no parent.
 		 */
 		private JavaType parent;
 		
 		/**
 		 * Setup the basic parameters for the primitive type.
 		 */
 		Primitive(String javaTypeName, String cppTypeName, Primitive parent) {
 			super(javaTypeName);
			this.typeName = javaTypeName;
 			this.cppTypeName = cppTypeName;
 			this.parent = parent;
 		}
 		
 		/**
 		 * Determines if we are a child of some parent primitive type.
 		 */
 		protected boolean hasParent(JavaType parent) {
 			//only ever 1 instance of the type
 			if (parent == this)
 				return true;
 			
 			//if we reach the top, we're clearly not
 			if (this.parent == null)
 				return false;
 			
 			//maybe our parent is, thus making us one, too?
 			return this.parent.hasParent(parent);
 		}
 		
 		/**
 		 * Gets the name that is usable as a type in C++.
 		 */
 		public String getCppName() {
 			return this.cppTypeName;
 		}
 		
 		/**
 		 * Gets the name that is usable as a type in Java.
 		 */
 		public String getName() {
 			return this.typeName;
 		}
 		
 		/**
 		 * Primitives don't have a class.
 		 */
 		public JavaClass getJavaClass() {
 			return null;
 		}
 	}
 	
 	/**
 	 * A class that represents classes.
 	 */
 	private static class Object extends JavaType {
 		/**
 		 * The JavaClass we will use to interface with primitives.
 		 */
 		private JavaClass cls;
 		
 		/**
 		 * Basic setup for the object type.
 		 */
 		Object(JavaClass cls) {
 			super(cls.getName());
 			this.cls = cls;
 		}
 
 		/**
 		 * Determines if we are a child of some parent class.
 		 */
 		protected boolean hasParent(JavaType parent) {
 			//we count on this only being called if the JavaType we're given
 			//is an object
 			return this.cls.isSubclassOf(((Object)parent).cls);
 		}
 		
 		/**
 		 * Gets the name that is usable as a type in C++.
 		 */
 		public String getCppName() {
 			return this.cls.getCppName();
 		}
 		
 		/**
 		 * Gets the name that is usable as a type in Java.
 		 */
 		public String getName() {
 			return this.cls.getName();
 		}
 		
 		/**
 		 * Quick accessor for grabbing the class from a type.
 		 */
 		public JavaClass getJavaClass() {
 			return this.cls;
 		}
 	}
 	
 	/**
 	 * Constructor.  Should only be accessible by the internal classes for instantiation.
 	 */
 	private JavaType(String typeName) {
 		types.put(typeName, this);
 	}
 	
 	/**
 	 * Setup our primitives.
 	 */
 	public static void prepare() {
 		if (prepared)
 			return;
 		
 		prepared = true;
 		
 		//and add all our primitives in the order they can be cast to
 		Primitive dbl = new Primitive("double", "double", null);
 		Primitive flt = new Primitive("float", "float", dbl);
 		Primitive lng = new Primitive("long", "int64_t", flt);
 		Primitive it = new Primitive("int", "int32_t", lng);
 		Primitive chr = new Primitive("char", "char_t", it);
 		Primitive shrt = new Primitive("short", "int16_t", it);
 		Primitive byt = new Primitive("byte", "int8_t", shrt);
 		
 		//and those tricky primitives that can't be cast to anything
 		new Primitive("void", "void", null);
 		new Primitive("boolean", "bool", null);
 		
 		//and get the C++ types that we need
 		JavaStatic.h.pln("#include <stdint.h>");
 		JavaStatic.h.pln("typedef unsigned char char_t;");
 		JavaStatic.h.pln();
 	}
 	
 	/**
 	 * If we do not know the fully-qualified class name, or even if the type is a primitive,
 	 * this will figure it out for you.
 	 */
 	public static JavaType getType(JavaScope scope, String type) {
 		//if we have the type cached, throw it back to him
 		if (types.containsKey(type))
 			return getType(type);
 		
 		//we don't know what type it is, so ask the file for the import
 		//at this point, it won't be primitive
 		return getType(scope.getJavaFile().getImport(type).getName());
 	}
 	
 	/**
 	 * Used mainly for primitives / fully-qualified class names.
 	 */
 	public static JavaType getType(String type) {
 		//if we have the type cached, throw it back to him
 		if (!types.containsKey(type)) {
 			//the class isnt here yet, so add it on-demand
 			JavaClass cls = JavaStatic.pkgs.getClass(type);
 			new Object(cls);
 			//cls.activate();
 			//System.out.println("Creating: " + cls.getName());
 		}
 		
 		return types.get(type);
 	}
 	
 	/**
 	 * Gets the name that is usable as a type in C++.
 	 */
 	public abstract String getCppName();
 	
 	/**
 	 * Gets the java name of the type.
 	 */
 	public abstract String getName();
 	
 	/**
 	 * Get the class of a type.
 	 */
 	public abstract JavaClass getJavaClass();
 	
 	/**
 	 * ==================================================================================================
 	 * Methods that determine java type information
 	 */
 	 
 	/**
 	 * Are we just a wee primitive?  No need to push this off to the inner classes as there will only ever
 	 * be two, and putting them there is just adding more code...but we could. Eh. This works.
 	 */
 	public boolean isPrimitive() {
 		return (this instanceof Primitive);
 	}
 	
 	/**
 	 * Determines if we are a child of some parent class.
 	 * Will only ever be called when the object is the same class as the parent; that is, it will only
 	 * be called when (this.getClass() == parent.getClass()), so you do not need to worry about checks like
 	 * that.
 	 */
 	protected abstract boolean hasParent(JavaType parent);
 	
 	/**
 	 * Determines if we are a subclass / sub-primitive of the given type.  In other words, given another
 	 * JavaType, it determines if that JavaType is one of our parents.
 	 * Note: a class is a child of itself.
 	 */
 	public boolean isChildOf(JavaType parent) {
 		//are we comparing a primitive with a class (or vis-versa)?
 		if (!this.getClass().isInstance(parent))
 			return false;
 		
 		//we can do a direct memory compare on this and parent because we only ever have 1 instance
 		//of the type floating around
 		return (this == parent || this.hasParent(parent));
 	}
 }

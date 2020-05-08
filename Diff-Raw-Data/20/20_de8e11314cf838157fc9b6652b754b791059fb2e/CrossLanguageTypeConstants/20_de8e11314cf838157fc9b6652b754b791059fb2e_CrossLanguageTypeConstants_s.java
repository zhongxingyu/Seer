 package ecologylab.serialization.types;
 
 /**
  * Type constants for defining many SimplTypes across Java, C#, and Objective C.
  * 
  * @author andruid
  */
 public interface CrossLanguageTypeConstants
 {
 	/**
 	 * Prefix for unique S.IM.PL Collection Type Names
 	 */
 	public static final String	SIMPL_COLLECTION_TYPES_PREFIX	= "simpl.types.collection.";
 
 	/**
 	 * Prefix for unique S.IM.PL Scalar Type Names
 	 */
 	public static final String	SIMPL_SCALAR_TYPES_PREFIX	= "simpl.types.scalar.";
 
 	
 	/*
 	 * Objective-C Scalar types
 	 */
 	public static final String	OBJC_INTEGER						= "int";
 
 	public static final String	OBJC_FLOAT							= "float";
 
 	public static final String	OBJC_DOUBLE							= "double";
 
 	public static final String	OBJC_BYTE								= "char";
 
 	public static final String	OBJC_CHAR								= "char";
 
 	public static final String	OBJC_BOOLEAN						= "bool";
 
 	public static final String	OBJC_LONG								= "long";
 
 	public static final String	OBJC_SHORT							= "short";
 
 	public static final String	OBJC_STRING							= "NSString";
 
 	public static final String	OBJC_OBJECT							= "NSObject";
 
 	public static final String	OBJC_DATE								= "NSDate";
 
 	public static final String	OBJC_STRING_BUILDER			= "NSMutableString";
 
 	public static final String	OBJC_URL								= "NSURL";
 
 	public static final String	OBJC_PARSED_URL					= "ParsedURL";
 
 	public static final String	OBJC_SCALAR_TYPE				= "NSScalarType";
 
 	public static final String	OBJC_CLASS							= "Class";
 
 	public static final String	OBJC_FIELD							= "Ivar";
 
 	public static final String	OBJC_COLOR							= "UIColor";
 
 	public static final String	OBJC_FILE								= "NSFileHandle";
 
 	public static final String	OBJC_ARRAYLIST					= "NSMutableArray";
 
 	public static final String	OBJC_HASHMAP						= "NSDictionary";
 
 	public static final String	OBJC_HASHMAPARRAYLIST		= "NSDictionaryList";
 
 	public static final String	OBJC_SCOPE							= "NSScope";
 
 	/*
 	 * C Sharp Scalar Types
 	 */
 	public static final String	DOTNET_INTEGER					= "Int32";
 
 	public static final String	DOTNET_FLOAT						= "Single";
 
 	public static final String	DOTNET_DOUBLE						= "Double";
 
 	public static final String	DOTNET_BYTE							= "Char";
 
 	public static final String	DOTNET_CHAR							= "Byte";
 
 	public static final String	DOTNET_BOOLEAN					= "Boolean";
 
 	public static final String	DOTNET_LONG							= "Int64";
 
 	public static final String	DOTNET_SHORT						= "Int16";
 
 	public static final String	DOTNET_STRING						= "String";
 
 	public static final String	DOTNET_DATE							= "DateTime";
 
 	public static final String	DOTNET_STRING_BUILDER		= "System.Text.StringBuilder";
 
 	public static final String	DOTNET_URL							= "Uri";
 
 	public static final String	DOTNET_PARSED_URL				= "Simpl.Fundamental.Net.ParsedUri";
 
 	public static final String	DOTNET_SCALAR_TYPE			= "Simpl.Serialization.Types.ScalarType"; 
 
 	public static final String	DOTNET_CLASS						= "Type";
 
 	public static final String	DOTNET_FIELD						= "System.Reflection.FieldInfo"; 
 
 	public static final String	DOTNET_COLOR						= "Color"; // namespace?
 
 	public static final String	DOTNET_FILE							= "System.IO.FileInfo";
 
 	public static final String	DOTNET_ARRAYLIST				= "List"; // System.Collections.Generic
 
 	public static final String	DOTNET_HASHMAP					= "Dictionary"; // System.Collections.Generic
 
 	public static final String	DOTNET_HASHMAPARRAYLIST	= "DictionaryList"; // Simpl.Fundamental.Generic
 
 	public static final String	DOTNET_SCOPE						= "Scope"; // ecologylab.collections
 	
 	public static final String DOTNET_UUID							= "Guid";
 	
 	/*
 	 * Java Scalar Types
 	 */
 	public static final String	JAVA_INTEGER					= "int";
 
 	public static final String	JAVA_FLOAT						= "float";
 
 	public static final String	JAVA_DOUBLE						= "double";
 
 	public static final String	JAVA_BYTE							= "byte";
 
 	public static final String	JAVA_CHAR							= "char";
 
 	public static final String	JAVA_BOOLEAN					= "boolean";
 
 	public static final String	JAVA_LONG							= "long";
 
 	public static final String	JAVA_SHORT						= "short";
 
 	public static final String	JAVA_STRING						= "String";
 
 	public static final String	JAVA_DATE							= "Date";
 
 	public static final String	JAVA_STRING_BUILDER		= "StringBuilder";
 
 	public static final String	JAVA_URL							= "Url";
 
 	public static final String	JAVA_PARSED_URL				= "ParsedURL";
 
 	public static final String	JAVA_SCALAR_TYPE			= "ScalarType";
 
 	public static final String	JAVA_CLASS						= "Class";
 
 	public static final String	JAVA_FIELD						= "Field";
 
 	public static final String	JAVA_COLOR						= "Color";
 
 	public static final String	JAVA_FILE							= "File";
 
 	public static final String	JAVA_ARRAYLIST				= "ArrayList";
 
 	public static final String	JAVA_HASHMAP					= "HashMap";
 
 	public static final String	JAVA_HASHMAPARRAYLIST	= "HashMapArrayList";
 
 	public static final String	JAVA_SCOPE						= "Scope";
 	
 	public static final String JAVA_UUID							= "GUID";
 	
 	public static final String	JAVA_RECTANGLE				= "Rectangle";
 	
 	public static final String	JAVA_PATTERN					= "Pattern";
 	
 	public static final String	DOTNET_PATTERN				= "System.Text.RegularExpressions.Regex";
 	
 	public static final String	OBJC_PATTERN					= "NSRegularExpression";
 	

 }

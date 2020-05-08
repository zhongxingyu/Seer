 package ecologylab.translators.cocoa;
 
 /**
  * General class file declares various constants used by the translators for generating the right
  * output files.
  * 
  * @author Nabeel Shahzad
  */
 public class CocoaTranslationConstants
 {
 
 	public static final String	FOUNDATION_HEADER										= "#import <Foundation/Foundation.h>";
 
 	public static final String	INCLUDE_OBJECT											= "#import \"@.h\"";
 
 	public static final String	INTERFACE														= "@interface";
 
 	public static final String	IMPLEMENTATION											= "@implementation";
 
 	public static final String	SYNTHESIZE													= "@synthesize";
 
 	public static final String	PROPERTY_PRIMITIVE									= "@property (nonatomic, readwrite)";
 
 	public static final String	PROPERTY_REFERENCE									= "@property (nonatomic, readwrite, retain)";
 
 	public static final String	END																	= "@end";
 
 	public static final String	PARAMETER														= "p_";
 
 	public static final String	WITH_REFERENCE											= "WithReference: ";
 
 	public static final String	SET																	= "set";
 
 	public static final String	RETURN_VOID													= "- (void) ";
 
 	public static final String	EQUALTO															= " = ";
 
 	public static final String	AT																	= "@";
 
 	public static final String	SINGLE_LINE_COMMENT									= "//";
 
 	public static final String	REFERENCE														= "*";
 
 	public static final String	TERMINATOR													= ";";
 
 	public static final String	INHERITENCE_OPERATOR								= ":";
 
 	public static String				INHERITENCE_OBJECT									= "NSObject";
 
 	public static final String	OPENING_CURLY_BRACE									= "{";
 
 	public static final String	CLOSING_CURLY_BRACE									= "}";
 
 	public static final String	OPENING_SQUARE_BRACE								= "[";
 
 	public static final String	CLOSING_SQUARE_BRACE								= "]";
 
 	public static final String	OPENING_BRACE												= "(";
 
 	public static final String	CLOSING_BRACE												= ")";
 
 	public static final String	SINGLE_LINE_BREAK										= "\n";
 
 	public static final String	DOUBLE_LINE_BREAK										= "\n\n";
 
 	public static final String	TAB																	= "\t";
 
 	public static final String	DOUBLE_TAB													= "\t\t";
 
 	public static final String	SPACE																= " ";
 
 	public static final String	DOT																	= ".";
 
 	public static final String	END_LINE														= ";";
 
 	/*
 	 * File constants
 	 */
 	public static final String	PACKAGE_NAME_SEPARATOR							= "\\.";
 
 	public static final String	FILE_PATH_SEPARATOR									= "/";
 
 	public static final String	HEADER_FILE_EXTENSION								= ".h";
 
 	public static final String	IMPLEMENTATION_FILE_EXTENSION				= ".m";
 
 	public static final String	XML_FILE_EXTENSION									= ".xml";
 
 	/*
 	 * Scalar types
 	 */
 	public static final String	OBJC_INTEGER												= "int";
 
 	public static final String	OBJC_FLOAT													= "float";
 
 	public static final String	OBJC_DOUBLE													= "double";
 
 	public static final String	OBJC_BYTE														= "char";
 
 	public static final String	OBJC_CHAR														= "char";
 
 	public static final String	OBJC_BOOLEAN												= "bool";
 
 	public static final String	OBJC_LONG														= "long";
 
 	public static final String	OBJC_SHORT													= "short";
 
 	public static final String	OBJC_STRING													= "NSString";
 
 	/*
 	 * Reference types
 	 */
 	public static final String	OBJC_OBJECT													= "NSObject";
 
 	public static final String	OBJC_DATE														= "NSDate";
 
 	public static final String	OBJC_STRING_BUILDER									= "NSMutableString";
 
 	public static final String	OBJC_URL														= "NSURL";
 
 	public static final String	OBJC_PARSED_URL											= "ParsedURL";
 
 	public static final String	OBJC_SCALAR_TYPE										= "NSScalarType";
 
 	public static final String	OBJC_CLASS													= "Class";
 
 	public static final String	OBJC_FIELD													= "Ivar";
 
 	public static final String	OBJC_COLOR													= "UIColor";
 
 	/*
 	 * Collection types
 	 */
 	public static final String	OBJC_ARRAYLIST											= "NSMutableArray";
 
 	public static final String	OBJC_HASHMAP												= "NSDictionary";
 
 	public static final String	OBJC_HASHMAPARRAYLIST								= "NSDictionaryList";
 
 	public static final String	OBJC_SCOPE													= "NSScope";
 
 	/*
 	 * More keywords, symbols & statements
 	 */
 	public static final String	PLUS																= "+";
 
 	public static final String	MINUS																= "-";
 
 	public static final String	CLASS																= "class";
 
 	public static final String	INITIALIZE													= "initialize";
 
 	public static final String	VOID																= "void";
 
 	public static final String	RELEASE															= "release";
 
 	public static final String	DEALLOC															= "dealloc";
 
 	public static final String	HEADER_COMMENTS											= "";
 
 	public static final String	IMPLEMENTAITON_COMMENTS							= null;
 
 	public static final String	STATIC															= "static";
 
	public static final String	TRANSLATIONSCOPE										= "TranslationScope";
 
	public static final String	TRANSATIONSCOPE_VAR									= "translationScope";
 
	public static final String	GET_TRANSLATIONSCOPE								= "translationScope";
 
	public static final String	IF_TRANSLATIONTION_SCOPE_NULL				= "if (translationScope == nil)";
 
	public static final String	TRANSLATION_INIT_WITH_XML_FILE_PATH	= "translationScope = [[TranslationScope alloc] initWithXMLFilePath: path];";
 
 	public static final String	FILE_NAME_PLACEHOLDER								= "&fileName&";
 
 	public static final String	FILE_PATH_INITIALIZER_STATEMENT			= "NSString *path = [[[NSBundle mainBundle] resourcePath] stringByAppendingPathComponent: @\""
 																																			+ FILE_NAME_PLACEHOLDER
 																																			+ ".xml\"];";
 
	public static final String	RETURN_TRANSLATION_SCOPE						= "return translationScope;";
 
 	public static final String	SUPER_DEALLOC												= "[super dealloc];";
 
 	public static final String	TRANSATIONSCOPE_FOLDER							= "tscope";
 
 }

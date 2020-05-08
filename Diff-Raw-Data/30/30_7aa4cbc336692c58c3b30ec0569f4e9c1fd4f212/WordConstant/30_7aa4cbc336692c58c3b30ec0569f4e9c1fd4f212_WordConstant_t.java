 package com.patrick_vane.unrealscript.editor.constants;
 
 import java.util.HashSet;
 
 
 public class WordConstant
 {
 	public static final String[] KEYWORDS = new String[]
 	{
 		// cpp >>
			"#if",
			"#elif",
			"#endif",
			"#else",
			"#end",
			"#include",
			"#define",
 		// cpp <<
 		
 		"abstract",
 		"alwaysc",
 		"archetype",
 		"array",
 		"arraycount",
 		"assert",
 		"atomic",
 		"atomicwhencooked",
 		"auto",
 		"autocollapsecategories",
 		"autoexpandcategories",
 		"automated",
 		
 		"begin",
 		"bool",
 		"break",
 		"button",
 		"byte",
 		
 		"case",
 		"cacheexempt",
 		"cache",
 		"class",
 		"classgroup",
 		"client",
 		"coerce",
 		"collapsecategories",
 		"config",
 		"const",
 		"continue",
 		"cppstructr",
 		"cpptext",
 		"crosslevelactive",
 		"crosslevelpassive",
 		
 		"databinding",
 		"default",
 		"defaultproperties",
 		"delegate",
 		"demorecording",
 		"dependson",
 		"deprecated",
 		"depreciatedu2",
 		"do",
 		"dontautocollapsecategories",
 		"dontcollapsecategories",
 		"dontsortcategories",
 		"double",
 		"dllbind",
 		"dllimport",
 		"duplicatetransient",
 		
 		"edfindable",
 		"editconst",
 		"editconstarray",
 		"editfixedsize",
 		"edithide",
 		"editoronly",
 		"editinline",
 		"editinlinenew",
 		"editinlinenotify",
 		"editinlineuse",
 		"edittextbox",
 		"else",
 		"end",
 		"enum",
 		"enumcount",
 		"event",
 		"exec",
 		"expands",
 		"export",
 		"exportstructs",
 		"extends",
 		"explicit",
 		
 		"false",
 		"filtereditoronly",
 		"final",
 		"find",
 		"float",
 		"for",
 		"forcescriptorder",
 		"foreach",
 		"fromc",
 		"function",
 		"forceinline",
 		
 		"global",
 		"globalconfig",
 		"goto",
 		"guid",
 		
 		"hidecategories",
 		"hidedropdown",
 		
 		"if",
 		"ignores",
 		"immutable",
 		"immutablewhencooked",
 		"implements",
 		"importc",
 		"inherits",
 		"init",
 		"inline",
 		"input",
 		"instanced",
 		"int",
 		"interface",
 		"interp",
 		"intrinsic",
 		"invariantc",
 		"iterator",
 		
 		"k2call",
 		"k2override",
 		"k2pure",
 		
 		"latent",
 		"length",
 		"local",
 		"localized",
 		"long",
 		
 		"map",
 		
 		"name",
 		"nameof",
 		"native",
 		"nativeonly",
 		"nativereplication",
 		"new",
 		"noexport",
 		"noexportheader",
 		"noclear",
 		"noimport",
 		"none",
 		"nontransactional",
 		"nontransient",
 		"noteditinlinenew",
 		"notforconsole",
 		"notinstanced",
 		"notplaceable",
 		"nousercreate",
 		
 		"operator",
 		"optional",
 		"orderedarrayu2",
 		"out",
 		
 		"parseconfig",
 		"perobjectconfig",
 		"perobjectlocalized",
 		"placeable",
 		"private",
 		"privatewrite",
 		"preoperator",
 		"protected",
 		"protectedwrite",
 		"pointer",
 		"postoperator",
 		"public",
 		"pure_virtual",
 		
 		"reliable",
 		"replication",
 		"repnotify",
 		"repretry",
 		"return",
 		"rot",
 		"rngc",
 		
 		"showcategories",
 		"structcpptext",
 		"structdefaultproperties",
 		"structdefaultsr",
 		"safereplace",
 		"self",
 		"server",
 		"serializetext",
 		"simulated",
 		"singular",
 		"skip",
 		"sort",
 		"state",
 		"static",
 		"stop",
 		"strictconfig",
 		"string",
 		"struct",
 		"super",
 		"switch",
 		
 		"template",
 		"transient",
 		"travel",
 		"true",
 		
 		"ubool",
 		"unreliable",
 		"until",
 		
 		"var",
 		"vect",
 		"virtual",
 		"void",
 		
 		"while",
 		"within"
 	};
 	
 	public static final String[] ARRAY_FUNCTIONS = new String[]
 	{
 		"add",
 		"additem",
 		
 		"empty",
 		
 		"insert",
 		"insertitem",
 		
 		"remove",
 		"removeindex",
 		"removeitem",
 		"replace",
 	};
 	
 	
 	public static final HashSet<String> KEYWORDS_HASHSET = new HashSet<String>();
 	static
 	{
 		for( String keyword : KEYWORDS )
 		{
 			KEYWORDS_HASHSET.add( keyword );
 		}
 	}
 	
 	public static final HashSet<String> ARRAY_FUNCTIONS_HASHSET = new HashSet<String>();
 	static
 	{
 		for( String function : ARRAY_FUNCTIONS )
 		{
 			ARRAY_FUNCTIONS_HASHSET.add( function );
 		}
 	}
 	
 	public static final HashSet<String> FUNCTION_KEYWORDS_HASHSET = new HashSet<String>();
 	static
 	{
 		FUNCTION_KEYWORDS_HASHSET.add( "function" );
 		FUNCTION_KEYWORDS_HASHSET.add( "event" );
 		FUNCTION_KEYWORDS_HASHSET.add( "delegate" );
 		FUNCTION_KEYWORDS_HASHSET.add( "virtual" );
 		FUNCTION_KEYWORDS_HASHSET.add( "template" );
 	}
 }

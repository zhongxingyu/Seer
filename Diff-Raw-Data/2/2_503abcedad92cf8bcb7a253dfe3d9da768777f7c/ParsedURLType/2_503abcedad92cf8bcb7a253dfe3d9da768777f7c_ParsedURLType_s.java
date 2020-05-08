 /*
  * Created on Dec 31, 2004 at the Interface Ecology Lab.
  */
 package ecologylab.xml.types.scalar;
 
 import java.io.File;
 
 import ecologylab.net.ParsedURL;
 
 /**
  * Type system entry for java.awt.Color. Uses a hex string as initialization.
  * 
  * @author andruid
  */
 public class ParsedURLType extends ReferenceType<ParsedURL>
 {
 /**
  * This constructor should only be called once per session, through
  * a static initializer, typically in TypeRegistry.
  * <p>
  * To get the instance of this type object for use in translations, call
  * <code>TypeRegistry.get("cm.generic.ParsedURL")</code>.
  * 
  */
 	public ParsedURLType()
 	{
 		super(ParsedURL.class);
 	}
 
 	/**
 	 * Looks for file in value, and creates a ParsedURL with file set if appropriate.
 	 * Otherwise, calls ParsedURL.getAbsolute().
 	 * 
 	 * @param value 	String to marshall into a typed instance.
 	 * 
 	 * @see ecologylab.xml.types.scalar.ScalarType#getInstance(java.lang.String)
 	 */
 	public ParsedURL getInstance(String value)
 	{
 	   File file	= null;
 	   if (value.startsWith("file://"))
 	   {
 		   int startIndex	= value.startsWith("file:///") ? 8 : 7;
 		   value	= value.substring(startIndex);
 		   file		= ecologylab.io.Files.newFile(value);
 	   }
 	   else if (value.indexOf(':') == 1)
 	   {
 		   file		= ecologylab.io.Files.newFile(value);
 	   }
 	   return (file != null) ? new ParsedURL(file)
		   : ParsedURL.getAbsolute(value, " getInstance()");
 	}
 	
 	public static final String URL_DELIMS = "/&?";
 	/**
 	 * For editing: these are the valid delimiters for separating tokens that make up a field
 	 * of this type.
 	 * 
 	 * @return
 	 */
 	public String delimeters()
 	{
 		return URL_DELIMS;
 	}
 	
 	public boolean allowNewLines()
 	{
 		return false;
 	}
 	
 	/**
 	 * When editing, determines whether delimiters can be included in token strings.
 	 * 
 	 * @return	true for URLs
 	 */
 	//FIXME -- Add String delimitersAfter to TextChunk -- interleaved with TextTokens, and
 	//get rid of this!!!
 	public boolean allowDelimitersInTokens()
 	{
 		return true;
 	}
 	/**
 	 * When editing, do not allow the user to include these characters in the resulting value String.
 	 * @return
 	 */
 	public String illegalChars()
 	{
 		return " !{}\t\n\r";
 	}
 	/**
 	 * When editing, is the field one that should be part of the Term model?
 	 * 
 	 * @return	false for URLs
 	 */
 	public boolean composedOfTerms()
 	{
 		return false;
 	}
 	/**
 	 * True if the user should be able to express interest in fields of this type.
 	 * 
 	 * @return	false for URLs
 	 */
 	public boolean affordsInterestExpression()
 	{
 		return false;
 	}
 
 }

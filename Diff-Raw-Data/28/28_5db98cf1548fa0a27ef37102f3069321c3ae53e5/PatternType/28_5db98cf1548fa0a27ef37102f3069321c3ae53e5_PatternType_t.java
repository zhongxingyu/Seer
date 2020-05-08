 /**
  * 
  */
 package ecologylab.serialization.types.scalar;
 
 import java.util.regex.Pattern;
 
 import ecologylab.serialization.ScalarUnmarshallingContext;
 import ecologylab.serialization.annotations.simpl_inherit;
 import ecologylab.serialization.types.CrossLanguageTypeConstants;
 
 /**
  * @author andruid
  *
  */
 @simpl_inherit
 public class PatternType extends ReferenceType<Pattern>
 implements CrossLanguageTypeConstants
 {
 
 	public PatternType()
 	{
		super(Pattern.class, JAVA_PATTERN, DOTNET_PATTERN, OBJC_PATTERN, null);
 	}
 
 	@Override
 	public Pattern getInstance(String value, String[] formatStrings,
 			ScalarUnmarshallingContext scalarUnmarshallingContext)
 	{
 		// TODO Auto-generated method stub
 		return (formatStrings == null) ? Pattern.compile(value) : Pattern.compile(value, Integer.parseInt(formatStrings[0]));
 	}
 
 }

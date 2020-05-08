 /*
  * Created on Jan 2, 2005 at the Interface Ecology Lab.
  */
 package ecologylab.serialization.types.scalar;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import ecologylab.serialization.ScalarUnmarshallingContext;
 import ecologylab.serialization.annotations.simpl_inherit;
 import ecologylab.serialization.types.CrossLanguageTypeConstants;
 
 /**
  * Type system entry for {@link java.util.Date Date}.
  * 
  * @author Zachary O. Toups (toupsz@cs.tamu.edu)
  */
 @simpl_inherit
 public class DateType extends ReferenceType<Date> implements CrossLanguageTypeConstants
 {
 	static final String datePatterns[] = 
 	{
 		"EEE MMM dd kk:mm:ss zzz yyyy",
 		"yyyy:MM:dd HH:mm:ss",
 		"yyyy:MM:dd HH:mm",
 		"yyyy-MM-dd HH:mm:ss",
 		"yyyy-MM-dd HH:mm",
 		"MMM dd, yyyy",
 		"yyyyMMdd",
 		"MM/dd/yyyy K:mm aa",
 	};
 	static final DateFormat	dateFormats[]			= new DateFormat[datePatterns.length + 1];
 
 	static final DateFormat	plainDf	= DateFormat.getDateTimeInstance();
 	
 	static
 	{
 		for (int i=0; i< datePatterns.length; i++)
 			dateFormats[i]												= new SimpleDateFormat(datePatterns[i]);
 		dateFormats[datePatterns.length]				= plainDf;
 	}
 
 	public DateType()
 	{
 		super(Date.class, JAVA_DATE, DOTNET_DATE, OBJC_DATE, null);
 	}
 
 	/**
 	 * @param value
 	 *          is interpreted as a SimpleDateFormat in the form EEE MMM dd kk:mm:ss zzz yyyy (for
 	 *          example Wed Aug 02 13:12:50 CDT 2006); if that does not work, then attempts to use the
 	 *          DateFormat for the current locale instead.
 	 * 
 	 * @see ecologylab.serialization.types.ScalarType#getInstance(java.lang.String, String[],
 	 *      ScalarUnmarshallingContext)
 	 */
 	public Date getInstance(String value, String[] formatStrings,
 			ScalarUnmarshallingContext scalarUnmarshallingContext)
 	{
 		for (DateFormat dateFormatParser: dateFormats)
 		{
       try 
       {
         return dateFormatParser.parse(value);
       } catch (java.text.ParseException ex) 
       {
       	// simply try the next pattern
       }
       if (formatStrings != null)
       	for (String thatFormat: formatStrings)
       	{
           try 
           {
             return new SimpleDateFormat(thatFormat).parse(value);
           } catch (java.text.ParseException ex) 
           {
           	// simply try the next pattern
           }
       		
       	}
  		}
 		error("Failed to parse date: " + value);
 		return null;
 	}
 }

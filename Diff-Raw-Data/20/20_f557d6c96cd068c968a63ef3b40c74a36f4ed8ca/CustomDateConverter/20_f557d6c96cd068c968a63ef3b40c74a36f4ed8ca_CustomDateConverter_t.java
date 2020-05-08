 package edu.wustl.bulkoperator.processor;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import org.apache.commons.beanutils.Converter;
 import edu.wustl.bulkoperator.metadata.DateValue;
 import edu.wustl.common.util.global.ApplicationProperties;
 import edu.wustl.common.util.logger.Logger;
 
 public class CustomDateConverter implements Converter {
 
 	private final static String DEFAULT_FORMAT = ApplicationProperties.getValue("bulk.date.valid.format.withtime");
 	private static final Logger logger = Logger.getCommonLogger(CustomDateConverter.class);
 	
 	public Object convert(Class type, Object value)
 	{
 		SimpleDateFormat format = null;
		String formatString=null;
 		String dateValue=null;
 		Date date=null;
 		if (value instanceof DateValue)	{
			formatString=((DateValue) value).getFormat();
			
 			format.setLenient(false);
 			dateValue = ((DateValue) value).getValue();
 		} else {
			formatString=DEFAULT_FORMAT;
			
 			dateValue=value.toString();
 		}
 		 try {
				if(formatString.contains(":") && dateValue!=null && !dateValue.contains(":"))
	            {
	                dateValue=dateValue+" 00:00";
	            }
				format = new SimpleDateFormat(formatString); 
			 	format.setLenient(false);
 			 	date=format.parse(dateValue);
 		} catch (ParseException e) {
 		
 			logger.error("Error while parsing date.", e);
 		}
 		return date;
 	}
 
}

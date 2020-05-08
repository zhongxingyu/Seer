 package jp.ac.tsukuba.cs.conclave.earthquake.utils;
 
 import org.joda.time.format.DateTimeFormatter;
 import org.joda.time.format.DateTimeFormatterBuilder;
 import org.joda.time.format.ISODateTimeFormat;
 
 public class DateUtils {
 	private static DateTimeFormatter formatter; 
 	
 	
 	
 	public static DateTimeFormatter getDateTimeFormatter()
 	{
 		if (formatter == null)
 			formatter = new DateTimeFormatterBuilder()
 				.append(ISODateTimeFormat.date().getParser())
 				.appendOptional(new DateTimeFormatterBuilder()
 				.appendLiteral(' ')
 				.append(ISODateTimeFormat.hourMinuteSecond()).toParser())
 				.toFormatter();
		return formatter;
 	}
 }

 package br.maplink.webservices.places.client.converter;
 
 import java.text.SimpleDateFormat;
 import java.util.GregorianCalendar;
 import java.util.TimeZone;
 
 public class DateConverterImpl implements DateConverter {
 
 	@Override
	public String toRfc1132(GregorianCalendar date) {
 		SimpleDateFormat dateFormatGmt = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
 		dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT"));
 		
 		return dateFormatGmt.format(date.getTime());
 	}
 }

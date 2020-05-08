 package br.maplink.webservices.places.client.converter;
 
 import java.util.GregorianCalendar;
 
 import org.junit.Test;
 
 import static org.fest.assertions.Assertions.assertThat;
 
 public class DateConverterImplTest {
 
 	@Test
 	public void shouldConvertToRfc1132Format() {
 		String convertedDateToRfc1132Format = 
			new DateConverterImpl().toRfc1132(new GregorianCalendar(2011, 8, 8, 1, 2, 3));
 		
 		assertThat(convertedDateToRfc1132Format)
 			.isEqualTo("Thu, 08 Sep 2011 04:02:03 GMT");
 	}
 }

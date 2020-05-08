 package com.hunwardsen.maven.weather;
 
 import java.io.InputStream;
 import org.apache.commons.io.IOUtils;
 import com.hunwardsen.maven.weather.Weather;
 import com.hunwardsen.maven.weather.WeatherFormatter;
 import com.hunwardsen.maven.weather.YahooParser;
 import junit.framework.TestCase;
 
 public class WeatherFormatterTest extends TestCase {
 	
 	public WeatherFormatterTest(String name) {
 		super(name);
 	}
 
 	public void testFormat() throws Exception {
 		
 		InputStream nyData = getClass().getClassLoader().getResourceAsStream("ny-weather.xml");
 		Weather weather = new YahooParser().parse(nyData);
		String formattedResult = new WeatherFormatter().format(weather).trim();
 		InputStream expected = getClass().getClassLoader().getResourceAsStream("format-expected.dat");
		String expectedResult = IOUtils.toString(expected).trim();
		assertEquals(expectedResult, formattedResult);
 	}
 }

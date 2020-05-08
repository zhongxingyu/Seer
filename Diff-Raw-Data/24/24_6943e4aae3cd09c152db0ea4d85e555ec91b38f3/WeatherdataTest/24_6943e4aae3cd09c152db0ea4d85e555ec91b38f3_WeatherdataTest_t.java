 package no.yr.xml.parser;
 
 import java.io.FileReader;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.Iterator;
 
 import junit.framework.TestCase;
 import no.yr.util.Validity;
 import no.yr.weather.Clouds;
 import no.yr.weather.TimeSeries;
 import no.yr.weather.Weather;
 
 import org.jdom.JDOMException;
 
 public class WeatherdataTest extends TestCase {
 
 	
 	String xml;
       
 	
 	protected void setUp() throws Exception {
 		super.setUp();
 		FileReader reader;  
 		reader = new FileReader("yrexample.xml");
 		char[] buf = new char[100000];
 		reader.read(buf);
 		
 		
 		
 		xml = new String(buf);
 		xml = xml.trim();
 		
 		
 	}
 
 	protected void tearDown() throws Exception {
 		super.tearDown();
 	}
 	
 	public void testParseData()
 	{
 		Weatherdata data=null;
 		try {
 			data = new Weatherdata(xml);
 		} catch (JDOMException e) {
 			e.printStackTrace();
 			fail(e.getMessage());
 		}
 		
 		TimeSeries series = data.getTimeSeries();
 		Iterator<Weather> iterator = series.getIterator();
 		
 		Weather current = null;
 		assertTrue(iterator.hasNext());
 		
 		while(iterator.hasNext())
 		{
 			if(current != null)
 			{
 				Weather next = iterator.next();
 				assertTrue(current.getValid().getFrom().compareTo(next.getValid().getFrom()) <= 0);
 				
 				current = next;
 			}else
 			{
 				current = iterator.next();
 			}
 		}
 		
 		
 	}
 	
 	public void testGetWeatherCorrect()
 	{
 		Weatherdata data=null;
 		try {
 			data = new Weatherdata(xml);
 		} catch (JDOMException e) {
 			e.printStackTrace();
 			fail(e.getMessage());
 		}
 		
 		TimeSeries series = data.getTimeSeries();
 		
 		
 		assertEquals(4, series.getSize());
 		
 		
 		Iterator<Weather>  it = series.getIterator();
 		Weather itFirst = it.next();
 		Weather first = series.get(itFirst.getValid());
 		
 		assertEquals(new GregorianCalendar(2008,01,30,21,00,00), itFirst.getValid().getFrom());
 		//assertEquals(new GregorianCalendar(2008,01,30,21,00,00), first.getValid().getValidTo());
 
 		assertEquals((float)500.0, first.getPossition().getHight());
 		assertEquals((float)9.58, first.getPossition().getLongitude());
 		assertEquals((float)60.1, first.getPossition().getLatitude());
 
 		
 		assertEquals((float) 0,  first.getClouds().getFog());
 		assertEquals((float)0, first.getClouds().getHigh());
 		assertEquals((float)0, first.getClouds().getLow());
 		assertEquals((float)54.8, first.getClouds().getMedium());
 		assertEquals((float)54.9, first.getClouds().getTotal());
		assertEquals((float)30.5, first.getTempratur().getTemperatur());
 		
 		assertEquals((float)1008.0, first.getPressure().getHpa());
 		
 		assertEquals((float) 2.4, first.getWind().getSpeed());
 		
 		assertEquals((float) 0.4, first.getRain().getAmount());
 		
 		
 	}
 	
 	public  void testGetSet()
 	{
 		TimeSeries series = new TimeSeries();
 		try {
 			series.add(new Validity().setFrom("2008-01-30T21:00:00Z"), new Weather().setClouds(new Clouds().setFog((float)100.0)));
 		} catch (Exception e) {
 			e.printStackTrace();
 			fail(e.getMessage());
 		}
 		
 		try {
 			Weather tmp = series.get(new Validity().setFrom("2008-01-30T21:00:00Z").setValidTo("2008-01-30T23:00:00Z"));
 			assertEquals(tmp.getClouds().getFog(), (float) 100.0);
 		} catch (Exception e) {
 			
 			e.printStackTrace();
 			fail(e.getMessage());
 		}
 	}
 
 }

 package no.yr.svg;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 
 import java.nio.CharBuffer;
 import junit.framework.TestCase;
 import no.yr.xml.parser.Weatherdata;
 
 import org.jdom.output.XMLOutputter;
 
 public class TimeSeriesRendrerTest extends TestCase {
 
 	public void testTimeSeriesRenderer() throws Exception
 	{
 		FileReader reader;  
 		reader = new FileReader("yrexample.xml");
         int bufferSize = 900000;
 		char[] buf = new char[bufferSize];
 
 		int sizeRead = reader.read(buf);
         if(sizeRead == bufferSize)
         {
             throw new Exception("To small a buffer to read the entire xml.");
         }
 	
 		String xml = new String(buf);
         xml = xml.trim();
 
 		
 		Weatherdata data = new Weatherdata(xml);
 		
 		TimeSeriesRendrer renderer = new TimeSeriesRendrer(data.getTimeSeriesForHours(24));
 		
 		XMLOutputter out = new XMLOutputter();
 		
 		File newFile = new File("timeseries.svg");
 			
 		FileWriter file = new FileWriter(newFile);
 		out.output(renderer.getSvg(), file);
 		
 		System.out.flush();
 		
 		
 	}
 
 }

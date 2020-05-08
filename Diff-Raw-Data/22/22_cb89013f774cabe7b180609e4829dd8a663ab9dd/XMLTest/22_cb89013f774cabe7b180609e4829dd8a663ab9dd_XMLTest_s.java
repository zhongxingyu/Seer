 package net.praqma.util.xml;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.junit.Test;
 
 import static org.junit.Assert.*;
 import static org.hamcrest.CoreMatchers.*;
 
 public class XMLTest {
 	
 	public static final String newLine = System.getProperty( "line.separator" );
 
 	public static final String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" + newLine + "<data xmlns=\"foo\" xmlns:xi=\"http://www.w3.org/2001/XInclude\">" + newLine + "\tHello, world." + newLine + "</data>" + newLine;
 	
 	@Test
 	public void includeAware() throws IOException {
		File include = new File( XMLTest.class.getClassLoader().getResource( "include.xml" ).getFile() );
 		XML xml = new XML( include, true );
 		
 		assertThat( xml.getXML(), is( expected ) );
 	}
 }

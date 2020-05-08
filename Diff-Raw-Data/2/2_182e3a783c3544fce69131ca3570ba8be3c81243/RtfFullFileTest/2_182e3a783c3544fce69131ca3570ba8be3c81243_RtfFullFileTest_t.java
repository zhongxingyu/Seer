 package net.sf.okapi.filters.rtf.tests;
 
 import java.io.InputStream;
 import java.net.URISyntaxException;
 
 import net.sf.okapi.common.Event;
 import net.sf.okapi.common.resource.RawDocument;
 import net.sf.okapi.filters.rtf.RTFFilter;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 public class RtfFullFileTest {
 	private RTFFilter filter;
 	private String[] testFileList;
 
 	@Before
 	public void setUp() throws Exception {
 		filter = new RTFFilter();		
 		testFileList = TestUtils.getTestFiles();
 	}
 
 	@After
 	public void tearDown() {
 		filter.close();
 	}
 
 	@Test
 	public void testAllExternalFiles() throws URISyntaxException {
 		@SuppressWarnings("unused")
 		Event event = null;
 
 		for (String f : testFileList) {		
 			System.out.println(f);
 			InputStream stream = RtfFullFileTest.class.getResourceAsStream("/" + f);
			filter.open(new RawDocument(stream, "windows-1252", "en", "fr"));
 			while (filter.hasNext()) {
 				event = filter.next();
 			}
 		}
 	}	
 }

 package com.horsefire.filecabinet.web;
 
 import java.util.Arrays;
 
 import junit.framework.TestCase;
 
 import org.joda.time.DateTime;
 
 public class ArchiveServletTest extends TestCase {
 
 	public void testCreateFilename() {
 		assertEquals("2013-05-14-first_second.pdf",
				ArchiveServlet.createFilename("%y-%m-%d-%t.pdf", new DateTime(
						2013, 5, 14, 0, 0), Arrays.asList("second", "first")));
 	}
 }

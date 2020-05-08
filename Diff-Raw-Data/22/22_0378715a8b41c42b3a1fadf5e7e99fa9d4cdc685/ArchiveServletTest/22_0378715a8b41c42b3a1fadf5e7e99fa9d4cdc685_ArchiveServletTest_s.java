 package com.horsefire.filecabinet.web;
 
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

 import java.util.Arrays;
import java.util.HashSet;
 
 import junit.framework.TestCase;
 
 import org.joda.time.DateTime;
 
import com.horsefire.filecabinet.file.Document;

 public class ArchiveServletTest extends TestCase {
 
 	public void testCreateFilename() {
		FcDocument doc = createMock(FcDocument.class);
		expect(doc.getEffective()).andStubReturn(
				new DateTime(2013, 5, 14, 0, 0));
		expect(doc.getTags()).andStubReturn(
				new HashSet<String>(Arrays.asList("second", "first")));
		replay(doc);
 		assertEquals("2013-05-14-first_second.pdf",
				ArchiveServlet.createFilename("%y-%m-%d-%t.pdf", doc));
		verify(doc);
 	}
 }

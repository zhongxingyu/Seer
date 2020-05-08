 /*===========================================================================
   Copyright (C) 2011 by the Okapi Framework contributors
 -----------------------------------------------------------------------------
   This library is free software; you can redistribute it and/or modify it 
   under the terms of the GNU Lesser General Public License as published by 
   the Free Software Foundation; either version 2.1 of the License, or (at 
   your option) any later version.
 
   This library is distributed in the hope that it will be useful, but 
   WITHOUT ANY WARRANTY; without even the implied warranty of 
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser 
   General Public License for more details.
 
   You should have received a copy of the GNU Lesser General Public License 
   along with this library; if not, write to the Free Software Foundation, 
   Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 
   See also the full LGPL text here: http://www.gnu.org/copyleft/lesser.html
 ===========================================================================*/
 
 package net.sf.okapi.filters.versifiedtxt;
 
 import static org.junit.Assert.assertEquals;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 
 import net.sf.okapi.common.Event;
 import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.filters.FilterTestDriver;
 import net.sf.okapi.common.filterwriter.IFilterWriter;
 import net.sf.okapi.common.resource.RawDocument;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class VersifiedTextWriterTest {
 	private VersifiedTextFilter filter;
 	private IFilterWriter writer;
 		
 	@Before
 	public void setUp() {
 		filter = new VersifiedTextFilter();
 		writer = new VersifiedTextWriter();
 		filter.setOptions(LocaleId.ENGLISH, LocaleId.SPANISH, "UTF-8", true);
 	}
 	
 	@Test
 	public void testBilingual() throws IOException {
 		String snippet = "|bbook\n|cchapter\n|v1\nsource\n<TARGET>\ntarget\n\n|v2\nsource2\n<TARGET>\ntarget2";
 		String expected = "|bbook\n|cchapter\n|v1\nsource\n<TARGET>\ntarget\n\n|v2\nsource2\n<TARGET>\ntarget2\n\n";
 		String result = eventWriter(snippet);
 		assertEquals(expected, result);
 	}
 	
 	@Test
 	public void testOutputSimpleBookChapterVerse() throws IOException {
 		String snippet = "|bbook\n|cchapter\n|v1\nThis is a test.";
 		String expected = "|bbook\n|cchapter\n|v1\nThis is a test.\n<TARGET>\n\n"; 
 		String result = eventWriter(snippet);
 		assertEquals(expected, result);
 	}
 
	@Test
	public void testBilingualWithNewlinesAfterSource() throws IOException {
		String snippet2 = "|bbook\r\n|v1\r\nsource\r\n\r\n<TARGET>\r\n\r\n";
		String result = eventWriter(snippet2);
		String finalResult = eventWriter(result);
		// here to catch a possible filter exception - no exception = green
	}
	
 	private String eventWriter(String input) throws IOException {
 		try {
 			// Open the input
 			filter.open(new RawDocument(input, LocaleId.ENGLISH, LocaleId.SPANISH));
 
 			// Prepare the output
 			writer.setOptions(LocaleId.SPANISH, "UTF-8");
 			ByteArrayOutputStream writerBuffer = new ByteArrayOutputStream();
 			writer.setOutput(writerBuffer);
 
 			// Process the document
 			Event event;
 			while (filter.hasNext()) {
 				event = filter.next();
 				writer.handleEvent(event);
 			}			
 			writerBuffer.close();
 			return new String(writerBuffer.toByteArray(), "UTF-8");
 		} finally {
 			if (filter != null)
 				filter.close();
 			if (writer != null)
 				writer.close();
 		}
 	}
 }

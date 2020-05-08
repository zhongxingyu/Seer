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
 
 package net.sf.okapi.filters.wiki;
 
 import static org.junit.Assert.assertEquals;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 
 import net.sf.okapi.common.Event;
 import net.sf.okapi.common.LocaleId;
 import net.sf.okapi.common.filterwriter.IFilterWriter;
 import net.sf.okapi.common.resource.RawDocument;
 import net.sf.okapi.filters.wiki.WikiFilter;
 import net.sf.okapi.filters.wiki.WikiWriter;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class WikiWriterTest {
 	private WikiFilter filter;
 	private IFilterWriter writer;
 		
 	@Before
 	public void setUp() {
 		filter = new WikiFilter();
 		writer = new WikiWriter();
 		filter.setOptions(LocaleId.ENGLISH, LocaleId.SPANISH, "UTF-8", true);
 	}
 	
 	@Test
 	public void testOutput() throws IOException {
 		String snippet = "=== Headline ===\n"
 				   + "Some multiline \n"
 				   + "text with **decoration**.";
 		String expected = "=== Headline ===\n"
 						+ "Some multiline text with **decoration**.\n";
 		String result = eventWriter(snippet);
 		assertEquals(expected, result);
 	}
 	
 	@Test
 	public void testOutputTable() throws IOException {
 		String snippet = "^ Header 1 ^ Header 2 |\n"
 				   + "| Cell 1 | Cell 2 |\n"
 				   + "\n"
 				   + "Paragraph.";
 		String expected = "^ Header 1 ^ Header 2 |\n"
 						+ "| Cell 1 | Cell 2 |\n"
 						+ "\n"
 						+ "Paragraph.\n";
 		String result = eventWriter(snippet);
 		assertEquals(expected, result);
 	}

	@Test
	public void testWhitespaces() throws IOException {
		String snippet = " white    space!  \n";
		String expected = " white space!  \n";
		String result = eventWriter(snippet);
		assertEquals(expected, result);
		
		filter.getParameters().fromString("{preserve_whitespace: true}");
		result = eventWriter(snippet);
		assertEquals(snippet, result);
		
		filter.getParameters().reset();
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

 /*===========================================================================
   Copyright (C) 2008-2009 by the Okapi Framework contributors
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
 
 package net.sf.okapi.common;
 
 import java.io.IOException;
 import java.io.StringReader;
 import java.nio.charset.Charset;
 import java.nio.charset.CharsetEncoder;
 
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import net.sf.okapi.common.Util;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 import static org.junit.Assert.*;
 
 public class UtilTest {
 	
 	private CharsetEncoder chsEnc;
 	private DocumentBuilderFactory docBuilderFact;
 	
 	@Before
 	public void setUp() throws Exception {
 		docBuilderFact = DocumentBuilderFactory.newInstance();
 		docBuilderFact.setValidating(false);
 		chsEnc = Charset.forName("iso-8859-1").newEncoder();
 	}
 
 	@After
 	public void tearDown() {
 	}
 
 	@Test
 	public void testTrimStart () {
 		String in = " \t ztext  ";
 		assertEquals("text  ", Util.trimStart(in, " \tz"));
 	}
 
 	@Test
 	public void testGetDirectoryName_BSlash () {
 		String in = "C:\\test\\file";
 		assertEquals("C:\\test", Util.getDirectoryName(in));
 	}
 
 	@Test
	public void testGetDirectoryName_MixedCases () {
		String in = "/home/test\\file";
		assertEquals("/home/test", Util.getDirectoryName(in));
	}

	@Test
 	public void testGetDirectoryName_Slash () {
 		String in = "/home/test/file";
 		assertEquals("/home/test", Util.getDirectoryName(in));
 	}
 
 	@Test
 	public void testGetDirectoryName_DirBSlash () {
 		String in = "C:\\test\\";
 		assertEquals("C:\\test", Util.getDirectoryName(in));
 	}
 
 	@Test
 	public void testGetDirectoryName_DirSlash () {
 		String in = "/home/test/";
 		assertEquals("/home/test", Util.getDirectoryName(in));
 	}
 
 	@Test
 	public void testGetDirectoryName_Filename () {
 		String in = "myFile.ext";
 		assertEquals("", Util.getDirectoryName(in));
 	}
 
 	@Test
 	public void testGetDirectoryName_URL () {
 		String in = "/C:/test/file.ext";
 		assertEquals("/C:/test", Util.getDirectoryName(in));
 	}
 
 	@Test
 	public void testEscapeToXML_Quote0 () {
 		String in = "&<>'\"";
 		assertEquals("&amp;&lt;>'\"", Util.escapeToXML(in, 0, false, null));
 	}
 
 	@Test
 	public void testEscapeToXML_Quote1 () {
 		String in = "&<>'\"";
 		assertEquals("&amp;&lt;>&apos;&quot;", Util.escapeToXML(in, 1, false, null));
 	}
 
 	@Test
 	public void testEscapeToXML_Quote2 () {
 		String in = "&<>'\"";
 		assertEquals("&amp;&lt;>&#39;&quot;", Util.escapeToXML(in, 2, false, null));
 	}
 
 	@Test
 	public void testEscapeToXML_Quote3 () {
 		String in = "&<>'\"";
 		assertEquals("&amp;&lt;>'&quot;", Util.escapeToXML(in, 3, false, null));
 	}
 
 	@Test
 	public void testEscapeToXML_GT () {
 		String in = "&<>'\"";
 		assertEquals("&amp;&lt;&gt;'&quot;", Util.escapeToXML(in, 3, true, null));
 	}
 
 	@Test
 	public void testEscapeToXML_ExtCharsWithNull () {
 		String in = "\u00d0\u0440Z\uD840\uDC00";
 		assertEquals("\u00d0\u0440Z\uD840\uDC00", Util.escapeToXML(in, 0, false, null));
 	}
 
 	@Test
 	public void testEscapeToXML_ExtCharsWithLatin1 () {
 		String in = "\u00d0\u0440Z\uD840\uDC00";
 		assertEquals("\u00d0&#x0440;Z&#x20000;", Util.escapeToXML(in, 0, false, chsEnc));
 	}
 
 	@Test
 	public void testGetExtension () {
 		String in = "myFile.abc.ext";
 		assertEquals(".ext", Util.getExtension(in));
 	}
 
 	@Test
 	public void testGetExtension_Alone () {
 		String in = ".ext";
 		assertEquals(".ext", Util.getExtension(in));
 	}
 
 	@Test
 	public void testGetExtension_None () {
 		String in = "myFile";
 		assertNull(Util.getExtension(in));
 	}
 
 	@Test
 	public void testGetExtension_Dot () {
 		String in = "myFile.";
 		assertEquals(".", Util.getExtension(in));
 	}
 
 	@Test
 	public void testSplitLanguageCode () {
 		String in = "en";
 		String[] res = Util.splitLanguageCode(in);
 		assertEquals(res[0], "en");
 		assertEquals(res[1], "");
 	}
 
 	@Test
 	public void testSplitLanguageCode_4Letters () {
 		String in = "en-BZ";
 		String[] res = Util.splitLanguageCode(in);
 		assertEquals(res[0], "en");
 		assertEquals(res[1], "BZ");
 	}
 
 	@Test
 	public void testSplitLanguageCode_Underline () {
 		String in = "en_BZ";
 		String[] res = Util.splitLanguageCode(in);
 		assertEquals(res[0], "en");
 		assertEquals(res[1], "BZ");
 	}
 
 	@Test
 	public void testGetPercentage () {
 		assertEquals(45, Util.getPercentage(450, 1000));
 	}
 
 	@Test
 	public void testGetPercentage_WithZero () {
 		assertEquals(1, Util.getPercentage(10, 0));
 	}
 
 	@Test
 	public void testIsSameLanguage_DoNotIgnoreRegion () {
 		assertTrue(Util.isSameLanguage("en", "en", false));
 		assertTrue(Util.isSameLanguage("en", "EN", false));
 		assertTrue(Util.isSameLanguage("En", "eN", false));
 		assertFalse(Util.isSameLanguage("en", "fr", false));
 		assertFalse(Util.isSameLanguage("en", "en-us", false));
 		assertFalse(Util.isSameLanguage("en-us", "en", false));
 		assertFalse(Util.isSameLanguage("abc-xyz", "abc-QWE", false));
 		assertFalse(Util.isSameLanguage("abc-xyz", "iop-QWE", false));
 	}
 
 	@Test
 	public void testIsSameLanguage_IgnoreRegion () {
 		assertTrue(Util.isSameLanguage("en", "en", true));
 		assertTrue(Util.isSameLanguage("en", "EN", true));
 		assertTrue(Util.isSameLanguage("En", "eN", true));
 		assertFalse(Util.isSameLanguage("en", "fr", true));
 		assertTrue(Util.isSameLanguage("en", "en-us", true));
 		assertTrue(Util.isSameLanguage("en-us", "en", true));
 		assertTrue(Util.isSameLanguage("abc-xyz", "abc-QWE", true));
 		assertFalse(Util.isSameLanguage("abc-xyz", "iop-QWE", true));
 	}
 
 	@Test
 	public void testGetTextContent_Simple () {
 		Document doc = createXMLdocument("<d>\n\nText</d>");
 		Element elem = doc.getDocumentElement();
 		assertEquals("\n\nText", Util.getTextContent(elem));
 	}
 	
 	@Test
 	public void testGetTextContent_WithComment () {
 		Document doc = createXMLdocument("<d><!--comment-->Text</d>");
 		Element elem = doc.getDocumentElement();
 		assertEquals("Text", Util.getTextContent(elem));
 	}
 	
 	@Test
 	public void testGetTextContent_Empty () {
 		Document doc = createXMLdocument("<d/>");
 		Element elem = doc.getDocumentElement();
 		assertEquals("", Util.getTextContent(elem));
 	}
 	
 	private Document createXMLdocument (String data) {
 		InputSource input = new InputSource(new StringReader(data));
 		Document doc = null;
 		try {
 			doc = docBuilderFact.newDocumentBuilder().parse(input);
 		}
 		catch ( SAXException e ) {
 			e.printStackTrace();
 		}
 		catch ( IOException e ) {
 			e.printStackTrace();
 		}
 		catch ( ParserConfigurationException e ) {
 			e.printStackTrace();
 		}
 		assertNotNull(doc);
 		return doc;
 	}
 
 }

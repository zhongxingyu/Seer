 package org.nate.encoder.html;
 
 import static java.util.Collections.singletonMap;
 import static java.util.Arrays.asList;
 import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
 
 import java.io.IOException;
 import java.util.List;
 import java.util.Map;
 
 import org.junit.Test;
 import org.nate.TransformResult;
 import org.nate.encoder.html.HtmlEncoder;
 import org.xml.sax.SAXException;
 
 public class HtmlEncoderTest {
 
 	private HtmlEncoder htmlEncoder = new HtmlEncoder();
 
 	@Test(expected = IllegalArgumentException.class)
 	public void shouldThrowAnExceptionWhenDataIsNull() throws Exception {
 		Object document = htmlEncoder.encode("<html/>");
 		htmlEncoder.transformWith(document, null);
 	}
 	
 	@Test(expected = IllegalArgumentException.class)
 	public void shouldThrowAnExceptionWhenDataIsNotAMap() throws Exception {
 		Object document = htmlEncoder.encode("<html/>");
 		htmlEncoder.transformWith(document, "");
 	}
 	
 	@Test(expected = IllegalArgumentException.class)
 	public void shouldThrowAnExceptionWhenKeyIsNotAString() throws Exception {
 		Object document = htmlEncoder.encode("<html/>");
 		htmlEncoder.transformWith(document, singletonMap(42, "b"));
 	}
 	
 	@Test
 	public void shouldIgnoreNullValues() throws Exception {
 		Object document = htmlEncoder.encode("<html/>");
 		TransformResult transformResult = htmlEncoder.transformWith(document, singletonMap("a", null));
 		assertXMLEqual("<html/>", transformResult.toHtml());
 	}
 	
 	@Test
 	public void shouldMatchAndInjectSingleDataValue() throws Exception {
 		Object document = htmlEncoder.encode("<div class='section'><span class='content'></span></div>");
 		TransformResult transformResult = htmlEncoder.transformWith(document, singletonMap(".section", "Hello"));
 		assertXMLEqual("<div class='section'>Hello</div> ", transformResult.toHtml());
 	}
 
 	@Test
 	public void shouldMatchAndInjectMultipleDataValues() throws Exception {
 		Object document = htmlEncoder.encode("<div class='section'><span class='content'></span></div>");
 		Map<String, List<String>> data = singletonMap(".section", asList("Section 1", "Section 2"));
 		TransformResult transformResult = htmlEncoder.transformWith(document, data);
 		assertXmlFragmentsEqual("<div class='section'>Section 1</div><div class='section'>Section 2</div>", transformResult.toHtml());
 	}
 	
 	@Test
 	public void shouldMatchAndInjectIntoASubselection() throws Exception {
 		Object document = htmlEncoder.encode("<div class='section'><span class='greeting'></span></div>");
 		Object data = singletonMap(".section", singletonMap(".greeting", "Hello"));
 		TransformResult transformResult = htmlEncoder.transformWith(document, data);
 		assertXmlFragmentsEqual("<div class='section'><span class='greeting'>Hello</span></div>", transformResult.toHtml());
 	}
 
 	@SuppressWarnings("unchecked")
 	@Test
 	public void shouldMatchAndInjectMultipleDataValuesIntoSubselection() throws Exception {
 		Object document = htmlEncoder.encode("<div class='section'><span class='greeting'></span></div>");
 		Object data = singletonMap(".section", asList(singletonMap(".greeting", "Hello"), singletonMap(".greeting", "Goodbye")));
 		TransformResult transformResult = htmlEncoder.transformWith(document, data);
 		assertXmlFragmentsEqual(
 				"<div class='section'><span class='greeting'>Hello</span></div>" +
 				"<div class='section'><span class='greeting'>Goodbye</span></div>",
 				transformResult.toHtml());
 	}
 	
 	@Test
 	public void shouldMatchAndInjectIntoElementAttribute() throws Exception {
 		Object document = htmlEncoder.encode("<a href='#'>my link</a>");
 		Object data = singletonMap("a", singletonMap("href", "http://www.example.com"));
 		TransformResult transformResult = htmlEncoder.transformWith(document, data);
 		assertXmlFragmentsEqual("<a href='http://www.example.com'>my link</a>", transformResult.toHtml());
 	}
 	
	@Test
	public void shouldAllowValuesToBeAnyObject() throws Exception {
		Object document = htmlEncoder.encode("<div/>");
		Object data = singletonMap("div", 42L);
		TransformResult transformResult = htmlEncoder.transformWith(document, data);
		assertXmlFragmentsEqual("<div>42</div>", transformResult.toHtml());
	}
	
 	private void assertXmlFragmentsEqual(String expected, String actual) throws SAXException, IOException {
 		// Wrap in fake roots in case the xml has multiple roots, otherwise you get a parser exception
 		assertXMLEqual(wrapInFakeRoot(expected), wrapInFakeRoot(actual));
 	}
 
 	private String wrapInFakeRoot(String fragment) {
 		return "<fake>" + fragment + "</fake>";
 	}
 }

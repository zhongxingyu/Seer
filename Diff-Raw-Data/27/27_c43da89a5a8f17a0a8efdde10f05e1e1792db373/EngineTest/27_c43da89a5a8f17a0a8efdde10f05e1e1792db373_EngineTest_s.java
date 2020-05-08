 package org.nate;
 
 import static java.util.Collections.*;
 import static java.util.Arrays.*;
 import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
 
 import java.util.List;
 import java.util.Map;
 
 import org.junit.Test;
 
 
 public class EngineTest {
 	
 	@Test(expected = IllegalArgumentException.class)
 	public void shouldThrowAnExceptionWhenDataIsNull() throws Exception {
 		Engine engine = encodeHtmlFragment("<html/>");
 		engine.inject(null);
 	}
 
 	
 	@Test(expected = IllegalArgumentException.class)
 	public void shouldThrowAnExceptionWhenDataIsNotAMap() throws Exception {
 		Engine engine = encodeHtmlFragment("<html/>");
 		engine.inject("");
 	}
 
 	
 	@Test(expected = IllegalArgumentException.class)
 	public void shouldThrowAnExceptionWhenKeyIsNotAString() throws Exception {
 		Engine engine = encodeHtmlFragment("<html/>");
 		engine.inject(singletonMap(42, "b"));
 	}
 
 	
 	@Test
 	public void shouldIgnoreNullValues() throws Exception {
 		Engine engine = encodeHtmlFragment("<html/>");
 		Engine result = engine.inject(singletonMap("a", null));
 		assertXMLEqual("<html/>", result.render());
 	}
 
 	@Test
 	public void shouldMatchAndInjectSingleDataValue() throws Exception {
 		Engine engine = encodeHtmlFragment("<div class='section'><span class='content'></span></div>");
 		Engine result = engine.inject(singletonMap(".section", "Hello"));
 		assertXMLEqual("<div class='section'>Hello</div> ", result.render());
 	}
 
 	@Test
 	public void shouldMatchAndInjectMultipleDataValues() throws Exception {
 		Engine engine = encodeHtmlFragment("<div class='section'><span class='content'></span></div>");
 		Map<String, List<String>> data = singletonMap(".section", asList("Section 1", "Section 2"));
 		Engine result = engine.inject(data);
 		assertXmlFragmentsEqual("<div class='section'>Section 1</div><div class='section'>Section 2</div>",
 				result.render());
 	}
 
 	@Test
 	public void shouldMatchAndInjectIntoASubselection() throws Exception {
 		Engine engine = encodeHtmlFragment("<div class='section'><span class='greeting'></span></div>");
 		Object data = singletonMap(".section", singletonMap(".greeting", "Hello"));
 		Engine result = engine.inject(data);
 		assertXmlFragmentsEqual("<div class='section'><span class='greeting'>Hello</span></div>", result.render());
 	}
 
 	@SuppressWarnings("unchecked")
 	@Test
 	public void shouldMatchAndInjectMultipleDataValuesIntoSubselection() throws Exception {
 		Engine engine = encodeHtmlFragment("<body><div class='section'><span class='greeting'></span></div></body>");
 		Object data = singletonMap(".section", asList(singletonMap(".greeting", "Hello"), singletonMap(".greeting", "Goodbye")));
 		Engine result = engine.inject(data);
 		assertXmlFragmentsEqual(
 				"<body><div class='section'><span class='greeting'>Hello</span></div>" +
 				"<div class='section'><span class='greeting'>Goodbye</span></div></body>",
 				result.render());
 	}
 	
 	@Test
 	public void shouldMatchAndInjectIntoElementAttribute() throws Exception {
 		Engine engine = encodeHtmlFragment("<a href='#'>my link</a>");
 		Object data = singletonMap("a", singletonMap("href", "http://www.example.com"));
 		Engine result = engine.inject(data);
 		assertXmlFragmentsEqual("<a href='http://www.example.com'>my link</a>", result.render());
 	}
 	
 	@Test
 	public void shouldAllowValuesToBeAnyObject() throws Exception {
 		Engine engine = encodeHtmlFragment("<div/>");
 		Object data = singletonMap("div", 42L);
 		Engine result = engine.inject(data);
 		assertXmlFragmentsEqual("<div>42</div>", result.render());
 	}
 	
 	@Test
 	public void shouldAllowHtmlWithDoctype() throws Exception {
 		Engine engine = encodeHtmlDocument(
 				"<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">" +
 				"<html><body><div/></body></html>");
 		Object data = singletonMap("div", "hello");
 		Engine result = engine.inject(data);
 		assertXmlFragmentsEqual("<html><body><div>hello</div></body></html>", result.render());
 	}
 	
 	@Test
 	public void shouldBeAbleToExtractClippings() throws Exception {
 		Engine engine = encodeHtmlFragment("<div id='header'>Header</div><div id='content'><h1>Content</h1></div>");
 		Engine header = engine.select("#header");
 		assertXmlFragmentsEqual("<div id='header'>Header</div>", header.render());
 	}
 	
 	@Test
 	public void shouldBeAbleToInjectNateEnginesIntoNateEngines() throws Exception {
 		Engine engine1 = encodeHtmlFragment("<div id='header'>Header</div><div id='content'></div>");
 		Engine engine2 = encodeHtmlFragment("<h1>Hello</h1>");
 		Engine result = engine1.inject(singletonMap("#content", engine2));
 		assertXmlFragmentsEqual("<div id='header'>Header</div><div id='content'><h1>Hello</h1></div>", result.render());
 	}
 	
 	@Test
 	public void shouldBeAbleToInjectASeletionFromOneDocumentToAnother() throws Exception {
 		Engine source = encodeHtmlFragment("<div> <p>one</p> and <p>two</p> </div>");
 		Engine selection = source.select("p");
 		Engine destination = encodeHtmlFragment("<div id='header'>Header</div><div id='content'></div>");
 		Engine result = destination.inject(singletonMap("#content", selection));
 		assertXmlFragmentsEqual("<div id='header'>Header</div><div id='content'><p>one</p><p>two</p></div>", result.render());
 	}
 	
 	private Engine encodeHtmlFragment(String html) {
 		return Engine.newWith(html, Engine.encoders().encoderFor("HTMLF"));
 	}
 	
 	private Engine encodeHtmlDocument(String html) {
 		return Engine.newWith(html, Engine.encoders().encoderFor("HTML"));
 	}
 	
 	private void assertXmlFragmentsEqual(String expected, String actual) throws Exception {
 		// Wrap in fake roots in case the xml has multiple roots, otherwise you get a parser exception
 		assertXMLEqual(wrapInFakeRoot(expected), wrapInFakeRoot(actual));
 	}
 	
 	private String wrapInFakeRoot(String fragment) {
 		return "<fake>" + fragment + "</fake>";
 	}
 
 }

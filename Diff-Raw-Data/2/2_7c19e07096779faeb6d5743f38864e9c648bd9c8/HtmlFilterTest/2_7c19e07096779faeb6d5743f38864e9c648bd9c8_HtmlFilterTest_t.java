 package ch.fhnw.apsifilter;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotSame;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import ch.fhnw.apsifilter.filter.AttributeWhitelistFilter;
 import ch.fhnw.apsifilter.filter.ProtocolFilter;
 import ch.fhnw.apsifilter.filter.SrcUrlFilter;
 import ch.fhnw.apsifilter.filter.TagWhitelistFilter;
 import ch.fhnw.apsifilter.filter.css.CssInlineFilter;
 import ch.fhnw.apsifilter.filter.css.CssLinkFilter;
 import ch.fhnw.apsifilter.filter.css.CssStyleAttributeFilter;
 
 /**
  * Tests are from https://www.owasp.org/index.php/XSS_Filter_Evasion_Cheat_Sheet
  * 
  * @author Matthias Brun
  * 
  */
 public class HtmlFilterTest {
 
 	private static final String HEADER = "<html><head></head><body>";
 	private static final String FOOTER = "</body></html>";
 	private static final String BLANK_HTML_SITE = HEADER + FOOTER;
 	private static final String BLANK_IMAGE = "<img src=\"\" />";
 
 	private Pipe underTest;
 
 	@Before
 	public void setUp() {
 		underTest = Pipe.createPipe();
 		underTest.addFilter(TagWhitelistFilter.createDefault());
 		underTest.addFilter(AttributeWhitelistFilter.createDefault());
 		underTest.addFilter(ProtocolFilter.createDefault());
 		underTest.addFilter(CssStyleAttributeFilter.createDefault());
 		underTest.addFilter(CssInlineFilter.createLazy());
 		underTest.addFilter(CssLinkFilter.createDefault());
 		underTest.addFilter(SrcUrlFilter.createDefault());
 	}
 
 	@Test
 	public void testCleanHtml() {
 		String html = "Hello <b>world</b>!";
 		String cleanHtml = underTest.filter(html).html();
 		assertEquals(HEADER + "Hello <b>world</b>!" + FOOTER, stripNewlines(cleanHtml));
 	}
 	
 	@Test
 	public void testCleanHtmlImage() {
 		String html = "<img src=\"http://www.fhnw.ch/logo.png\" />";
 		String cleanHtml = underTest.filter(html).html();
 		assertEquals(HEADER + "<img src=\"http://www.fhnw.ch/logo.png\" />" + FOOTER, stripNewlines(cleanHtml));
 	}
 
 	@Test
 	public void testXssLocator2() {
 		String html = "'';!--\"<XSS>=&{()}";
 		String cleanHtml = underTest.filter(html).html();
 		assertNotSame(HEADER + "<XSS verses &lt;XSS" + FOOTER, stripNewlines(cleanHtml));
 	}
 
 	@Test
 	public void testNoFilterEvasion() {
 		String html = "<SCRIPT SRC=http://ha.ckers.org/xss.js></SCRIPT>";
 		String cleanHtml = underTest.filter(html).html();
 		assertEquals(BLANK_HTML_SITE, stripNewlines(cleanHtml));
 	}
 
 	@Test
 	public void testImageXss() {
 		String html = "<IMG SRC=\"javascript:alert('XSS');\">";
 		String cleanHtml = underTest.filter(html).html();
 		assertEquals(HEADER + BLANK_IMAGE + FOOTER, stripNewlines(cleanHtml));
 	}
 
 	@Test
 	public void testNoQuotesAndNoSemicolon() {
 		String html = "<IMG SRC=javascript:alert('XSS')>";
 		String cleanHtml = underTest.filter(html).html();
 		assertEquals(HEADER + BLANK_IMAGE + FOOTER, stripNewlines(cleanHtml));
 	}
 
 	@Test
 	public void testCaseInsensitiveXssAttackVector() {
 		String html = "<IMG SRC=JaVaScRiPt:alert('XSS')>";
 		String cleanHtml = underTest.filter(html).html();
 		assertEquals(HEADER + BLANK_IMAGE + FOOTER, stripNewlines(cleanHtml));
 	}
 
 	@Test
 	public void testHtmlEntities() {
 		String html = "<IMG SRC=javascript:alert(\"XSS\")>";
 		String cleanHtml = underTest.filter(html).html();
 		assertEquals(HEADER + BLANK_IMAGE + FOOTER, stripNewlines(cleanHtml));
 	}
 
 	@Test
 	public void testGraveAccentObfuscation() {
 		String html = "<IMG SRC=`javascript:alert(\"RSnake says, 'XSS'\")`>";
 		String cleanHtml = underTest.filter(html).html();
 		assertEquals(HEADER + BLANK_IMAGE + FOOTER, stripNewlines(cleanHtml));
 	}
 
 	@Test
 	public void testMalformedImgTags() {
 		String html = "<IMG \"\"\"><SCRIPT>alert(\"XSS\")</SCRIPT>\">";
 		String cleanHtml = underTest.filter(html).html();
 		assertEquals(HEADER + "<img />&quot;&gt;" + FOOTER,
 				stripNewlines(cleanHtml));
 	}
 
 	@Test
 	public void testFromCharCode() {
 		String html = "<IMG SRC=javascript:alert(String.fromCharCode(88,83,83))>";
 		String cleanHtml = underTest.filter(html).html();
 		assertEquals(HEADER + BLANK_IMAGE + FOOTER, stripNewlines(cleanHtml));
 	}
 
 	@Test
 	public void testUtf8UnicodeEncoding() {
 		String html = "<IMG SRC=&#106;&#97;&#118;&#97;&#115;&#99;&#114;&#105;&#112;&#116;&#58;&#97;" +
 					  "&#108;&#101;&#114;&#116;&#40;&#39;&#88;&#83;&#83;&#39;&#41;>";
 		String cleanHtml = underTest.filter(html).html();
 		assertEquals(HEADER + BLANK_IMAGE + FOOTER, stripNewlines(cleanHtml));
 	}
 	
 	@Test
 	public void testLongUtf8UnicodeEncodingWithoutSemicolons() {
 		String html = "<IMG SRC=&#0000106&#0000097&#0000118&#0000097&#0000115&#0000099&#0000114&#0000105" +
 				      "&#0000112&#0000116&#0000058&#0000097&#0000108&#0000101&#0000114&#0000116&#0000040" +
 				      "&#0000039&#0000088&#0000083&#0000083&#0000039&#0000041>";
 		String cleanHtml = underTest.filter(html).html();
 		assertEquals(HEADER + BLANK_IMAGE + FOOTER, stripNewlines(cleanHtml));
 	}
 	
 	@Test
 	public void testHexEncodingWithoutSemicolons() {
 		String html = "<IMG SRC=&#x6A&#x61&#x76&#x61&#x73&#x63&#x72&#x69&#x70&#x74&#x3A&#x61&#x6C&#x65" +
 					  "&#x72&#x74&#x28&#x27&#x58&#x53&#x53&#x27&#x29>";
 		String cleanHtml = underTest.filter(html).html();
 		assertEquals(HEADER + BLANK_IMAGE + FOOTER, stripNewlines(cleanHtml));
 	}
 	
 	@Test
 	public void testEmbeddedTab() {
 		String html = "<IMG SRC=\"jav	ascript:alert('XSS');\">";
 		String cleanHtml = underTest.filter(html).html();
 		assertEquals(HEADER + BLANK_IMAGE + FOOTER, stripNewlines(cleanHtml));
 	}
 	
 	@Test
 	public void testEmbeddedEncodedTab() {
 		String html = "<IMG SRC=\"jav&#x09;ascript:alert('XSS');\">";
 		String cleanHtml = underTest.filter(html).html();
 		assertEquals(HEADER + BLANK_IMAGE + FOOTER, stripNewlines(cleanHtml));
 	}
 	
 	@Test
 	public void testEmbeddedNewlineToBreakUpXss() {
 		String html = "<IMG SRC=\"jav&#x0A;ascript:alert('XSS');\">";
 		String cleanHtml = underTest.filter(html).html();
 		assertEquals(HEADER + BLANK_IMAGE + FOOTER, stripNewlines(cleanHtml));
 	}
 	
 	@Test
 	public void testNonAlphaNonDigitXss1() {
 		String html = "<SCRIPT/XSS SRC=\"http://ha.ckers.org/xss.js\"></SCRIPT>";
 		String cleanHtml = underTest.filter(html).html();
 		assertEquals(BLANK_HTML_SITE, stripNewlines(cleanHtml));
 	}
 	
 	@Test
 	public void testNonAlphaNonDigitXss2() {
 		String html = "<BODY onload!#$%&()*~+-_.,:;?@[/|\\]^`=alert(\"XSS\")>";
 		String cleanHtml = underTest.filter(html).html();
 		assertEquals(BLANK_HTML_SITE, stripNewlines(cleanHtml));
 	}
 	
 	@Test
 	public void testNonAlphaNonDigitXss3() {
 		String html = "<SCRIPT/SRC=\"http://ha.ckers.org/xss.js\"></SCRIPT>";
 		String cleanHtml = underTest.filter(html).html();
 		assertEquals(BLANK_HTML_SITE, stripNewlines(cleanHtml));
 	}
 	
 	@Test
 	public void testExtraneousOpenBrackets() {
 		String html = "<<SCRIPT>alert(\"XSS\");//<</SCRIPT>";
 		String cleanHtml = underTest.filter(html).html();
 		assertEquals(HEADER + "&lt;" + FOOTER, stripNewlines(cleanHtml));
 	}
 	
 	@Test
 	public void testNoClosingScriptTags() {
 		String html = "<SCRIPT SRC=http://ha.ckers.org/xss.js?< B >";
 		String cleanHtml = underTest.filter(html).html();
 		assertEquals(BLANK_HTML_SITE, stripNewlines(cleanHtml));
 	}
 	
 	@Test
 	public void testProtocolResolutionInScriptTags() {
 		String html = "<SCRIPT SRC=//ha.ckers.org/.j>";
 		String cleanHtml = underTest.filter(html).html();
 		assertEquals(BLANK_HTML_SITE, stripNewlines(cleanHtml));
 	}
 	
 	@Test
 	public void testHalfOpenHtmlJavaScriptXssVector() {
 		String html = "<IMG SRC=\"javascript:alert('XSS')\"";
 		String cleanHtml = underTest.filter(html).html();
 		assertEquals(BLANK_HTML_SITE, stripNewlines(cleanHtml));
 	}
 	
 	@Test
 	public void testDoubleOpenAngleBrackets() {
 		String html = "<iframe src=http://ha.ckers.org/scriptlet.html <";
 		String cleanHtml = underTest.filter(html).html();
 		assertEquals(BLANK_HTML_SITE, stripNewlines(cleanHtml));
 	}
 	
 	@Test
 	public void testBodyImage() {
 		String html = "<BODY BACKGROUND=\"javascript:alert('XSS')\">";
 		String cleanHtml = underTest.filter(html).html();
 		assertEquals(BLANK_HTML_SITE, stripNewlines(cleanHtml));
 	}
 	
 	@Test
 	public void testImgDynsrc() {
 		String html = "<IMG DYNSRC=\"javascript:alert('XSS')\">";
 		String cleanHtml = underTest.filter(html).html();
 		assertEquals(HEADER + "<img />" + FOOTER, stripNewlines(cleanHtml));
 	}
 	
 	@Test
 	public void testListStyleImage() {
 		String html = "<STYLE>li {list-style-image: url(\"javascript:alert(2 * 3)\");}</STYLE><UL><LI>Test</br>";
 		String cleanHtml = underTest.filter(html).html();
 		assertEquals("<html><head><style></style></head><body><ul><li>Test<br /></li></ul></body></html>", stripNewlines(cleanHtml));
 	}
 	
 	@Test
 	public void testBodyTag() {
 		String html = "<BODY ONLOAD=alert('XSS')>";
 		String cleanHtml = underTest.filter(html).html();
 		assertEquals(BLANK_HTML_SITE, stripNewlines(cleanHtml));
 	}
 	
 	@Test
 	public void testIframe() {
 		String html = "<IFRAME SRC=\"javascript:alert('XSS');\"></IFRAME>";
 		String cleanHtml = underTest.filter(html).html();
 		assertEquals(BLANK_HTML_SITE, stripNewlines(cleanHtml));
 	}
 	
 	@Test
 	public void testTable() {
 		String html = "<TABLE BACKGROUND=\"javascript:alert('XSS')\">";
 		String cleanHtml = underTest.filter(html).html();
 		assertEquals(HEADER + "<table></table>" + FOOTER, stripNewlines(cleanHtml));
 	}
 	
 	@Test
 	public void testDivBackgroundImage() {
 		String html = "<DIV STYLE=\"background-image: url(javascript:alert('XSS'))\">";
 		String cleanHtml = underTest.filter(html).html();
 		assertEquals(HEADER + "<div style=\"\"></div>" + FOOTER, stripNewlines(cleanHtml));
 	}
 	
 	@Test
 	public void testDivBackgroundImagePlusExtraCharacters() {
 		String html = "<DIV STYLE=\"background-image: url(&#1;javascript:alert('XSS'))\">";
 		String cleanHtml = underTest.filter(html).html();
 		assertEquals(HEADER + "<div style=\"\"></div>" + FOOTER, stripNewlines(cleanHtml));
 	}
 	
 	@Test
 	public void testDivExpression() {
 		String html = "<DIV STYLE=\"width: expression(alert('XSS'));\">";
 		String cleanHtml = underTest.filter(html).html();
 		assertEquals(HEADER + "<div style=\"\"></div>" + FOOTER, stripNewlines(cleanHtml));
 	}
 	
 	@Test
 	public void testImgEmbeddedCommands() {
 		String html = "<IMG SRC=\"http://www.thesiteyouareon.com/somecommand.php?somevariables=maliciouscode\">";
 		String cleanHtml = underTest.filter(html).html();
 		assertEquals(HEADER + "<img src=\"http://www.thesiteyouareon.com/somecommand.php\" />" + FOOTER, stripNewlines(cleanHtml));
 	}
 	
 	@Test
 	public void testXssUsingHtmlQuoteEncapsulation() {
 		String html = "<SCRIPT a=\">\" SRC=\"http://ha.ckers.org/xss.js\"></SCRIPT>";
 		String cleanHtml = underTest.filter(html).html();
 		assertEquals(BLANK_HTML_SITE, stripNewlines(cleanHtml));
 	}
 	
 	@Test
 	public void testLinkStylesheet() {
 		String html = "<link rel=\"stylesheet\" type=\"text/css\" href=\"../../src/selfhtml.css\">";
 		String cleanHtml = underTest.filter(html).html();
		assertEquals("<html><head><link /></head><body>" + FOOTER, stripNewlines(cleanHtml));
 	}
 	
 	private static String stripNewlines(String text) {
 		text = text.replaceAll("\\n\\s*", "");
 		return text;
 	}
 
 }

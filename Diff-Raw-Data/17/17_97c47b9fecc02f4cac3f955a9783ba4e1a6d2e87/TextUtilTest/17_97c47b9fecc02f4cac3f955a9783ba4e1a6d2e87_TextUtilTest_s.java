 package org.eclipse.epp.mpc.tests.util;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNull;
 
 import org.eclipse.epp.internal.mpc.core.util.TextUtil;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.BlockJUnit4ClassRunner;
 
@SuppressWarnings("restriction")
 @RunWith(BlockJUnit4ClassRunner.class)
 public class TextUtilTest {
 
 	@Test
 	public void testStripHtmlMarkup_Null() {
 		assertNull(TextUtil.stripHtmlMarkup(null));
 	}
 
 	@Test
 	public void testStripHtmlMarkup_EmptyString() {
 		assertEquals("", TextUtil.stripHtmlMarkup(""));
 	}
 
 	@Test
 	public void testStripHtmlMarkup_NoMarkup() {
 		String input = "one two < three four";
 		assertEquals(input, TextUtil.stripHtmlMarkup(input));
 	}
 
 	@Test
 	public void testStripHtmlMarkup_TagClosed() {
 		String input = "one two <br/> three four";
 		assertEquals("one two  three four", TextUtil.stripHtmlMarkup(input));
 	}
 
 	@Test
 	public void testStripHtmlMarkup_TagOpen() {
 		String input = "one two <br> three four";
 		assertEquals("one two  three four", TextUtil.stripHtmlMarkup(input));
 	}
 
 	@Test
 	public void testStripHtmlMarkup_TagOpenSpaces() {
 		String input = "one two <br > three four";
 		assertEquals("one two  three four", TextUtil.stripHtmlMarkup(input));
 	}
 
 	@Test
 	public void testStripHtmlMarkup_TagWithAttributes() {
 		String input = "one two <span class=\"\"> three four";
 		assertEquals("one two  three four", TextUtil.stripHtmlMarkup(input));
 	}
 
 	@Test
 	public void testStripHtmlMarkup_TagWithAttributesApos() {
 		String input = "one two <span class='d'> three four";
 		assertEquals("one two  three four", TextUtil.stripHtmlMarkup(input));
 	}
 
 	@Test
 	public void testStripHtmlMarkup_TagWithUnquotedAttributes() {
 		String input = "one two <TABLE border=0> three four";
 		assertEquals("one two  three four", TextUtil.stripHtmlMarkup(input));
 	}
 
 	@Test
 	public void testStripHtmlMarkup_TagWithMultipleAttributes() {
 		String input = "one two <span class=\" asd\" id = 'foo'> three four";
 		assertEquals("one two  three four", TextUtil.stripHtmlMarkup(input));
 	}
 
 	@Test
 	public void testStripHtmlMarkup_TagSpanningLines() {
 		String input = "\n<p\n>one two three four";
 		assertEquals("\none two three four", TextUtil.stripHtmlMarkup(input));
 	}
 
 	@Test
 	public void testStripHtmlMarkup_TagAtStart() {
 		String input = "<p>one two three four";
 		assertEquals("one two three four", TextUtil.stripHtmlMarkup(input));
 	}
 
 	@Test
 	public void testStripHtmlMarkup_TagAtEnd() {
 		String input = "one two three four<br/>";
 		assertEquals("one two three four", TextUtil.stripHtmlMarkup(input));
 	}
 
 	@Test
 	public void testCleanInformalHtmlMarkup_Null() {
 		assertNull(TextUtil.cleanInformalHtmlMarkup(null));
 	}
 
 	@Test
 	public void testCleanInformalHtmlMarkup_EmptyString() {
 		assertEquals("", TextUtil.cleanInformalHtmlMarkup(""));
 	}
 
 	@Test
 	public void testCleanInformalHtmlMarkup_NaturalPara() {
 		assertEquals("one<p>two", TextUtil.cleanInformalHtmlMarkup("one\n\ntwo"));
 	}
 
 	@Test
 	public void testCleanInformalHtmlMarkup_NaturalParaPrecededByTag() {
 		assertEquals("one<br/>\n\ntwo", TextUtil.cleanInformalHtmlMarkup("one<br/>\n\ntwo"));
 	}
 
 	@Test
 	public void testCleanInformalHtmlMarkup_NaturalParaFollowedByTag() {
 		assertEquals("one\n\n<li>two", TextUtil.cleanInformalHtmlMarkup("one\n\n<li>two"));
 	}
 
 	@Test
 	public void testCleanInformalHtmlMarkup_NaturalParaBetweenTags() {
 		assertEquals("one</li>\n\n<li>two", TextUtil.cleanInformalHtmlMarkup("one</li>\n\n<li>two"));
 	}
 }

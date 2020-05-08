 package br.com.adaptworks.scraper;
 
 import java.util.List;
 
 import junit.framework.Assert;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import br.com.adaptworks.scraper.element.CloseTagElement;
 import br.com.adaptworks.scraper.element.Element;
 import br.com.adaptworks.scraper.element.ElementParser;
 import br.com.adaptworks.scraper.element.OpenTagElement;
 
 /**
  * @author jonasabreu
  * 
  */
 final public class ElementParserTest {
 
     private ElementParser parser;
 
     @Before
     public void setup() {
         parser = new ElementParser("td|/td|tr|/tr|foo|bar|/foo|/bar|a|/a|hr");
     }
 
     @Test
     public void testThatIfDoesNotHaveTagReturnsEmptyList() {
         Assert.assertEquals(0, parser.parse("").size());
         Assert.assertEquals(0, parser.parse("only text").size());
     }
 
     @Test
     public void testThatRecognizesOpenHtmlTag() {
         List<Element> elements = parser.parse("<td>");
 
         Assert.assertEquals(1, elements.size());
         Assert.assertEquals(OpenTagElement.class, elements.get(0).getClass());
         Assert.assertEquals("td", elements.get(0).getName());
     }
 
     @Test
     public void testThatRecognizesCloseHtmlTag() {
         List<Element> elements = parser.parse("</td>");
 
         Assert.assertEquals(1, elements.size());
         Assert.assertEquals(CloseTagElement.class, elements.get(0).getClass());
         Assert.assertEquals("/td", elements.get(0).getName());
     }
 
     @Test
     public void testThatRecognizesTwoTags() {
         List<Element> elements = parser.parse("</td><td>");
 
         Assert.assertEquals(2, elements.size());
         Assert.assertEquals(CloseTagElement.class, elements.get(0).getClass());
         Assert.assertEquals(OpenTagElement.class, elements.get(1).getClass());
         Assert.assertEquals("/td", elements.get(0).getName());
         Assert.assertEquals("td", elements.get(1).getName());
     }
 
     @Test
     public void testThatIgnoresWhiteChars() {
         List<Element> elements = parser.parse("\n\n </td>\n <td>");
 
         Assert.assertEquals(2, elements.size());
         Assert.assertEquals(CloseTagElement.class, elements.get(0).getClass());
         Assert.assertEquals(OpenTagElement.class, elements.get(1).getClass());
         Assert.assertEquals("/td", elements.get(0).getName());
         Assert.assertEquals("td", elements.get(1).getName());
     }
 
     @Test
     public void testThatIgnoresWhiteCharsInsideTag() {
         List<Element> elements = parser.parse("</td\n> <td >");
 
         Assert.assertEquals(2, elements.size());
         Assert.assertEquals(CloseTagElement.class, elements.get(0).getClass());
         Assert.assertEquals(OpenTagElement.class, elements.get(1).getClass());
         Assert.assertEquals("/td", elements.get(0).getName());
         Assert.assertEquals("td", elements.get(1).getName());
     }
 
     @Test
     public void testThatIgnoresMissingGreaterThanChar() {
         List<Element> elements = parser.parse("</td> > <td>");
 
         Assert.assertEquals(2, elements.size());
         Assert.assertEquals(CloseTagElement.class, elements.get(0).getClass());
         Assert.assertEquals(OpenTagElement.class, elements.get(1).getClass());
         Assert.assertEquals("/td", elements.get(0).getName());
         Assert.assertEquals("td", elements.get(1).getName());
 
     }
 
     @Test
     public void testThatIgnoresMissingLesserThanChar() {
         List<Element> elements = parser.parse("</td> < <td>");
 
         Assert.assertEquals(2, elements.size());
         Assert.assertEquals(CloseTagElement.class, elements.get(0).getClass());
         Assert.assertEquals(OpenTagElement.class, elements.get(1).getClass());
         Assert.assertEquals("/td", elements.get(0).getName());
         Assert.assertEquals("td", elements.get(1).getName());
     }
 
     @Test
     public void testThatRecognizesAnyOpenTag() {
         List<Element> elements = parser.parse("<foo>");
 
         Assert.assertEquals(1, elements.size());
         Assert.assertEquals(OpenTagElement.class, elements.get(0).getClass());
         Assert.assertEquals("foo", elements.get(0).getName());
     }
 
     @Test
     public void testThatRecognizesAnyCloseTag() {
         List<Element> elements = parser.parse("</bar>");
 
         Assert.assertEquals(1, elements.size());
         Assert.assertEquals(CloseTagElement.class, elements.get(0).getClass());
         Assert.assertEquals("/bar", elements.get(0).getName());
     }
 
     @Test
     public void testThatFindsContent() {
         List<Element> elements = parser.parse("</bar> content ");
         Assert.assertEquals(1, elements.size());
         Assert.assertEquals(CloseTagElement.class, elements.get(0).getClass());
         Assert.assertEquals("/bar", elements.get(0).getName());
         Assert.assertEquals(" content ", elements.get(0).getContent());
     }
 
     @Test
     public void testThatKeepsContentOnFirstTag() {
         List<Element> elements = parser.parse("</bar> content <foo> foo content </foo>");
 
         Assert.assertEquals(3, elements.size());
         Assert.assertEquals(CloseTagElement.class, elements.get(0).getClass());
         Assert.assertEquals(OpenTagElement.class, elements.get(1).getClass());
         Assert.assertEquals(CloseTagElement.class, elements.get(2).getClass());
         Assert.assertEquals("/bar", elements.get(0).getName());
         Assert.assertEquals("foo", elements.get(1).getName());
         Assert.assertEquals("/foo", elements.get(2).getName());
         Assert.assertEquals(" content ", elements.get(0).getContent());
         Assert.assertEquals(" foo content ", elements.get(1).getContent());
 
     }
 
     @Test
     public void testThatIgnoresWhiteCharOnTagName() {
         List<Element> elements = parser.parse("</bar > content <foo> foo content </foo>");
 
         Assert.assertEquals(3, elements.size());
         Assert.assertEquals(CloseTagElement.class, elements.get(0).getClass());
         Assert.assertEquals(OpenTagElement.class, elements.get(1).getClass());
         Assert.assertEquals(CloseTagElement.class, elements.get(2).getClass());
         Assert.assertEquals("/bar", elements.get(0).getName());
         Assert.assertEquals("foo", elements.get(1).getName());
         Assert.assertEquals("/foo", elements.get(2).getName());
         Assert.assertEquals(" content ", elements.get(0).getContent());
         Assert.assertEquals(" foo content ", elements.get(1).getContent());
     }
 
     @Test
     public void testThatRecognizesAtributes() {
         List<Element> elements = parser.parse("</bar id=\"bla\"> content <foo attr='pong'> foo content </foo>");
 
         Assert.assertEquals(3, elements.size());
         Assert.assertEquals(CloseTagElement.class, elements.get(0).getClass());
         Assert.assertEquals(OpenTagElement.class, elements.get(1).getClass());
         Assert.assertEquals(CloseTagElement.class, elements.get(2).getClass());
 
         Assert.assertEquals("/bar", elements.get(0).getName());
         Assert.assertEquals("foo", elements.get(1).getName());
         Assert.assertEquals("/foo", elements.get(2).getName());
 
         Assert.assertEquals(" content ", elements.get(0).getContent());
         Assert.assertEquals(" foo content ", elements.get(1).getContent());
 
         Assert.assertEquals("bla", elements.get(0).getAttributes().get("id"));
         Assert.assertEquals("pong", elements.get(1).getAttributes().get("attr"));
     }
 
     @Test
     public void testThatAcceptsWhiteSpaceOnAttributeValue() {
         List<Element> elements = parser.parse("<a href=\"http://blog.caelum.com.br \" a=\"b\">Comunidade</a>");
 
         Assert.assertEquals(2, elements.size());
 
         Assert.assertEquals(OpenTagElement.class, elements.get(0).getClass());
         Assert.assertEquals(CloseTagElement.class, elements.get(1).getClass());
 
         Assert.assertEquals("a", elements.get(0).getName());
         Assert.assertEquals("/a", elements.get(1).getName());
 
         Assert.assertEquals("http://blog.caelum.com.br ", elements.get(0).getAttributes().get("href"));
         Assert.assertEquals("b", elements.get(0).getAttributes().get("a"));
 
         Assert.assertEquals("Comunidade", elements.get(0).getContent());
     }
 
     @Test
     public void testThatAcceptsTheTagA() {
         List<Element> elements = parser.parse("<a href=\"http://bla\">");
 
         Assert.assertEquals(1, elements.size());
         Assert.assertEquals(OpenTagElement.class, elements.get(0).getClass());
         Assert.assertEquals("a", elements.get(0).getName());
         Assert.assertEquals("http://bla", elements.get(0).getAttributes().get("href"));
 
     }
 
     @Test
    public void testThatIgnoresComments() {
        List<Element> elements = parser.parse("<!-- -->");
 
        Assert.assertEquals(0, elements.size());
 
     }
 
     @Test
     public void testThatAcceptsSelfClosingTag() {
         List<Element> elements = parser.parse("<hr />");
 
         Assert.assertEquals(1, elements.size());
         Assert.assertEquals(OpenTagElement.class, elements.get(0).getClass());
         Assert.assertEquals("hr", elements.get(0).getName());
     }
 
     @Test
     public void testThatAcceptsSelfClosingTagWithAttributes() {
         List<Element> elements = parser.parse("<hr a=\"b\"/>");
 
         Assert.assertEquals(1, elements.size());
         Assert.assertEquals(OpenTagElement.class, elements.get(0).getClass());
         Assert.assertEquals("hr", elements.get(0).getName());
         Assert.assertEquals("b", elements.get(0).getAttributes().get("a"));
     }
 
     @Test
     public void testThatReadsOnlyRelevantTags() {
         List<Element> elements = new ElementParser("tr").parse("<a> <b> <c> <tr> </tr> <td> <ty>");
 
         Assert.assertEquals(1, elements.size());
         Assert.assertEquals(OpenTagElement.class, elements.get(0).getClass());
         Assert.assertEquals("tr", elements.get(0).getName());
 
     }
 }

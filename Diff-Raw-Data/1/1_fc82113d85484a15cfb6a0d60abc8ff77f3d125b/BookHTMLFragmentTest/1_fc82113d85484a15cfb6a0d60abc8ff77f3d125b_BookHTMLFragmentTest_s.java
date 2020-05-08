 package au.com.miskinhill.rdf.template.htmlfragment;
 
 import static org.hamcrest.CoreMatchers.*;
 import static org.junit.Assert.*;
 
 import java.io.InputStreamReader;
 import java.io.StringReader;
 import java.util.Collections;
 
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.rdf.model.RDFNode;
 import com.hp.hpl.jena.rdf.model.Resource;
 import org.dom4j.Document;
 import org.dom4j.DocumentException;
 import org.dom4j.DocumentHelper;
 import org.dom4j.Element;
 import org.dom4j.XPath;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import au.com.miskinhill.rdftemplate.TemplateInterpolator;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = { "classpath:/au/com/miskinhill/web/test-spring-context.xml" })
 public class BookHTMLFragmentTest {
     
     @Autowired private TemplateInterpolator templateInterpolator;
     
     @Test
     public void testResponsibility() throws Exception {
         Model model = modelFromTurtle(
                 "<example> a mhs:Book;" +
                 "dc:title 'Some title'@en;" +
                 "mhs:responsibility '<span xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"en\"><a href=\"http://miskinhill.com.au/authors/dude\">Some Dude</a></span>'^^rdf:XMLLiteral;" +
                 "dc:date '1801'^^xsd:date .");
         Resource book = model.createResource("http://miskinhill.com.au/cited/books/example");
         Document doc = interpolate(book);
         String responsibility = (String) xpath("string(//*[@class='responsibility'])").evaluate(doc);
         assertThat(responsibility.trim(), equalTo("Some Dude"));
     }
     
     @Test
     public void testWithoutPublisher() throws Exception {
         Model model = modelFromTurtle(
                 "<example> a mhs:Book;" +
                 "dc:title 'Some title'@en;" +
                 "mhs:responsibility '<span xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"en\"><a href=\"http://miskinhill.com.au/authors/dude\">Some Dude</a></span>'^^rdf:XMLLiteral;" +
                 "dc:date '1801'^^xsd:date .");
         Resource book = model.createResource("http://miskinhill.com.au/cited/books/example");
         Document doc = interpolate(book);
         String publication = (String) xpath("string(//*[@class='publication'])").evaluate(doc);
         assertThat(publication.trim(), equalTo("Published 1801"));
     }
     
     @Test
     public void testWithoutDate() throws Exception {
         Model model = modelFromTurtle(
                 "<example> a mhs:Book;" +
                 "dc:title 'Some title'@en;" +
                 "dc:publisher 'Some Publisher'.");
         Resource book = model.createResource("http://miskinhill.com.au/cited/books/example");
         Document doc = interpolate(book);
         String publication = (String) xpath("string(//*[@class='publication'])").evaluate(doc);
         assertThat(publication.trim(), equalTo("Published by Some Publisher"));
     }
     
     @Test
     public void testWithoutDateOrPublisher() throws Exception {
         Model model = modelFromTurtle(
                 "<example> a mhs:Book;" +
                 "dc:title 'Some title'@en.");
         Resource book = model.createResource("http://miskinhill.com.au/cited/books/example");
         Document doc = interpolate(book);
         String publication = (String) xpath("string(//*[@class='publication'])").evaluate(doc);
         assertThat(publication.trim(), equalTo(""));
     }
     
     @Test
     public void testGbooksId() throws Exception {
         Model model = modelFromTurtle(
                 "<example> a mhs:Book;" +
                 "dc:title 'Some title'@en;" +
                 "dc:date '1801'^^xsd:date;" +
                 "dc:identifier <http://books.google.com/books?id=12345>.");
         Resource book = model.createResource("http://miskinhill.com.au/cited/books/example");
         Document doc = interpolate(book);
         String gbooksLink = (String) xpath("string(//html:a[@href='http://books.google.com/books?id=12345'])").evaluate(doc);
         assertThat(gbooksLink.trim(), equalTo("Google Book Search"));
     }
     
     @Test
     public void testCoverThumbnail() throws Exception {
         Model model = modelFromTurtle(
                 "<example> a mhs:Book;" +
                 "dc:title 'Some title'@en;" +
                 "mhs:coverThumbnail <http://example.com/thumb.gif>.");
         Resource book = model.createResource("http://miskinhill.com.au/cited/books/example");
         Document doc = interpolate(book);
         Element img = (Element) xpath("//*[@class='cover']//html:img").selectSingleNode(doc);
         assertThat(img.attributeValue("src"), equalTo("http://example.com/thumb.gif"));
     }
     
     @Test
     public void testRussianLinkWithPlainCyrillicTitle() throws Exception {
         Model model = modelFromTurtle(
                 "<example> a mhs:Book;" +
                 "dc:title 'Русская книга'@ru;" +
                 "mhs:responsibility '<span xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"en\"><a href=\"http://miskinhill.com.au/authors/dude\">Some Dude</a></span>'^^rdf:XMLLiteral.");
         Resource book = model.createResource("http://miskinhill.com.au/cited/books/example");
         Document doc = interpolate(book);
         String link = (String) xpath("string(//html:a[@href='http://www.ozon.ru/?context=search&text=%D0%F3%F1%F1%EA%E0%FF+%EA%ED%E8%E3%E0+Some+Dude'])").evaluate(doc);
         assertThat(link.trim(), equalTo("Ozon.ru"));
     }
     
     @Test
     public void testRussianLinkWithHtmlCyrillicTitle() throws Exception {
         Model model = modelFromTurtle(
                 "<example> a mhs:Book;" +
                 "dc:title '<span xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"ru\"><em>Русская</em> книга</span>'^^rdf:XMLLiteral;" +
                 "mhs:responsibility '<span xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"en\"><a href=\"http://miskinhill.com.au/authors/dude\">Some Dude</a></span>'^^rdf:XMLLiteral.");
         Resource book = model.createResource("http://miskinhill.com.au/cited/books/example");
         Document doc = interpolate(book);
         String link = (String) xpath("string(//html:a[@href='http://www.ozon.ru/?context=search&text=%D0%F3%F1%F1%EA%E0%FF+%EA%ED%E8%E3%E0+Some+Dude'])").evaluate(doc);
         assertThat(link.trim(), equalTo("Ozon.ru"));
     }
     
     @Test
     public void testAvailableFromLink() throws Exception {
         Model model = modelFromTurtle(
                 "<example> a mhs:Book;" +
                 "dc:title 'Some title'@en;" +
                 "mhs:availableFrom <http://example.com/teh-book>.");
         Resource book = model.createResource("http://miskinhill.com.au/cited/books/example");
         Document doc = interpolate(book);
         String link = (String) xpath("string(//html:a[@href='http://example.com/teh-book'])").evaluate(doc);
         assertThat(link.trim(), equalTo("Some title"));
     }
     
     private Document interpolate(RDFNode node) throws DocumentException {
         String result = templateInterpolator.interpolate(
                 new InputStreamReader(this.getClass().getResourceAsStream("Book.xml")),
                 node);
        System.err.println(result);
         return DocumentHelper.parseText(result);
     }
     
     private Model modelFromTurtle(String turtle) {
         Model model = ModelFactory.createDefaultModel();
         model.read(new StringReader(
                 "@prefix mhs: <http://miskinhill.com.au/rdfschema/1.0/> ." +
                 "@prefix dc: <http://purl.org/dc/terms/> ." +
                 "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> ." +
                 "@prefix xsd: <http://www.w3.org/TR/xmlschema-2/#> ." +
                 turtle), "http://miskinhill.com.au/cited/books/", "TURTLE");
         return model;
     }
     
     private XPath xpath(String expression) {
         XPath xpath = DocumentHelper.createXPath(expression);
         xpath.setNamespaceURIs(Collections.singletonMap("html", "http://www.w3.org/1999/xhtml"));
         return xpath;
     }
 
 }

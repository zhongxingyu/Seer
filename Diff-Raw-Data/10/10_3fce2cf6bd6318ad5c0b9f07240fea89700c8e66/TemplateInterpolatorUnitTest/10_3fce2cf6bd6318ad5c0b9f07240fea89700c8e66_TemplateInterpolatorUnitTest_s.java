 package au.id.djc.rdftemplate;
 
 import static org.hamcrest.CoreMatchers.*;
 import static org.junit.Assert.*;
 import static org.junit.matchers.JUnitMatchers.*;
 
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.rdf.model.Resource;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import au.id.djc.rdftemplate.datatype.DateDataType;
 import au.id.djc.rdftemplate.selector.AntlrSelectorFactory;
 
 public class TemplateInterpolatorUnitTest {
     
     @BeforeClass
     public static void ensureDatatypesRegistered() {
         DateDataType.registerStaticInstance();
     }
     
     private Model model;
     private TemplateInterpolator templateInterpolator;
     
     @Before
     public void setUp() {
         model = ModelFactory.createDefaultModel();
         InputStream stream = this.getClass().getResourceAsStream(
                 "/au/id/djc/rdftemplate/test-data.xml");
         model.read(stream, "");
         AntlrSelectorFactory selectorFactory = new AntlrSelectorFactory();
         selectorFactory.setNamespacePrefixMap(TestNamespacePrefixMap.getInstance());
         templateInterpolator = new TemplateInterpolator(selectorFactory);
     }
     
     @Test
     public void shouldReplaceSubtreesWithContent() throws Exception {
         Resource journal = model.getResource("http://miskinhill.com.au/journals/test/");
         String result = templateInterpolator.interpolate(
                 new InputStreamReader(this.getClass().getResourceAsStream("replace-subtree.xml")), journal);
         assertThat(result, containsString("<div xml:lang=\"en\" lang=\"en\">Test Journal of Good Stuff</div>"));
         assertThat(result, not(containsString("<p>This should all go <em>away</em>!</p>")));
     }
     
     @Test
     public void shouldHandleXMLLiterals() throws Exception {
         Resource journal = model.getResource("http://miskinhill.com.au/journals/test/");
         String result = templateInterpolator.interpolate(
                 new InputStreamReader(this.getClass().getResourceAsStream("replace-xml.xml")), journal);
         assertThat(result, containsString(
                 "<div lang=\"en\"><p><em>Test Journal</em> is a journal.</p></div>"));
     }
     
     @Test
     public void shouldHandleIfs() throws Exception {
         Resource author = model.getResource("http://miskinhill.com.au/authors/test-author");
         String result = templateInterpolator.interpolate(
                 new InputStreamReader(this.getClass().getResourceAsStream("conditional.xml")), author);
         assertThat(result, containsString("attribute test"));
         assertThat(result, containsString("element test"));
         assertThat(result, not(containsString("rdf:if")));
         assertThat(result, not(containsString("negated test")));
         
         Resource authorWithoutNotes = model.getResource("http://miskinhill.com.au/authors/another-author");
         result = templateInterpolator.interpolate(
                 new InputStreamReader(this.getClass().getResourceAsStream("conditional.xml")), authorWithoutNotes);
         assertThat(result, not(containsString("attribute test")));
         assertThat(result, not(containsString("element test")));
         assertThat(result, containsString("negated test"));
     }
     
     @Test
     public void shouldHandleJoins() throws Exception {
         Resource citedArticle = model.getResource("http://miskinhill.com.au/cited/journals/asdf/1:1/article");
         String result = templateInterpolator.interpolate(
                 new InputStreamReader(this.getClass().getResourceAsStream("join.xml")), citedArticle);
         assertThat(result, containsString("<p><a href=\"http://miskinhill.com.au/authors/another-author\">Another Author</a>, " +
                 "<a href=\"http://miskinhill.com.au/authors/test-author\">Test Author</a></p>"));
     }
     
     @Test
     public void shouldHandleFor() throws Exception {
         Resource journal = model.getResource("http://miskinhill.com.au/journals/test/");
         String result = templateInterpolator.interpolate(
                 new InputStreamReader(this.getClass().getResourceAsStream("for.xml")), journal);
         assertThat(result, containsString("<span>http://miskinhill.com.au/journals/test/1:1/</span>"));
         assertThat(result, containsString("<span>http://miskinhill.com.au/journals/test/2:1/</span>"));
         assertThat(result, containsString("<p>http://miskinhill.com.au/journals/test/1:1/</p>"));
         assertThat(result, containsString("<p>http://miskinhill.com.au/journals/test/2:1/</p>"));
     }
     
     @Test
     public void shouldStripRdfNamespaceDeclarations() throws Exception {
         Resource author = model.getResource("http://miskinhill.com.au/authors/test-author");
         String result = templateInterpolator.interpolate(
                 new InputStreamReader(this.getClass().getResourceAsStream("namespaces.xml")), author);
         assertThat(result, not(containsString("xmlns:rdf=\"http://code.miskinhill.com.au/rdftemplate/\"")));
         assertThat(result, not(containsString("rdf:")));
     }
     
     @Test
     public void forShouldIterateRdfSeqsInOrder() throws Exception {
         Resource article = model.getResource("http://miskinhill.com.au/journals/test/1:1/multi-author-article");
         String result = templateInterpolator.interpolate(
                 new InputStreamReader(this.getClass().getResourceAsStream("for-seq.xml")), article);
         assertThat(result, containsString("Another Author\n\nTest Author"));
     }
     
     @Test
     public void joinShouldIterateRdfSeqsInOrder() throws Exception {
         Resource article = model.getResource("http://miskinhill.com.au/journals/test/1:1/multi-author-article");
         String result = templateInterpolator.interpolate(
                 new InputStreamReader(this.getClass().getResourceAsStream("join-seq.xml")), article);
         assertThat(result, containsString("<p><a href=\"http://miskinhill.com.au/authors/another-author\">Another Author</a>, " +
         		"<a href=\"http://miskinhill.com.au/authors/test-author\">Test Author</a></p>"));
     }
     
     @Test
     public void forShouldWorkForSingleResult() throws Exception {
         Resource journal = model.getResource("http://miskinhill.com.au/cited/journals/asdf/");
         String result = templateInterpolator.interpolate(
                 new InputStreamReader(this.getClass().getResourceAsStream("for.xml")), journal);
         assertThat(result, containsString("<span>http://miskinhill.com.au/cited/journals/asdf/1:1/</span>"));
         assertThat(result, containsString("<p>http://miskinhill.com.au/cited/journals/asdf/1:1/</p>"));
     }
     
     @Test
     public void joinShouldWorkForSingleResult() throws Exception {
         Resource review = model.getResource("http://miskinhill.com.au/journals/test/1:1/reviews/review");
         String result = templateInterpolator.interpolate(
                 new InputStreamReader(this.getClass().getResourceAsStream("join.xml")), review);
         assertThat(result, containsString("<p><a href=\"http://miskinhill.com.au/authors/test-author\">Test Author</a></p>"));
     }
     
 }

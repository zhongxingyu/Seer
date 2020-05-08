 package hudson.plugins.googlecode;
 
 import hudson.MarkupText;
 import hudson.model.AbstractBuild;
 import hudson.plugins.googlecode.GoogleCodeLinkAnnotator;
 
 import org.jmock.Expectations;
 import org.jmock.Mockery;
 
 import static org.junit.Assert.*;
 import org.junit.Before;
 import org.junit.Test;
 
 public class GoogleCodeLinkAnnotatorTest {
     
     private Mockery context;
 
     private GoogleCodeProjectProperty property;
     private GoogleCodeProjectProperty.PropertyRetriever propertyRetriever;
     
     private GoogleCodeLinkAnnotator annotator;
 
     @Before
     public void setUp() throws Exception {
         property = new GoogleCodeProjectProperty("http://code.google.com/");
         
         context = new Mockery();
         propertyRetriever = context.mock(GoogleCodeProjectProperty.PropertyRetriever.class);
         context.checking(new Expectations() { {
             one(propertyRetriever).getProperty((AbstractBuild<?, ?>) null); will(returnValue(property));
         } }); 
         
         annotator = new GoogleCodeLinkAnnotator(propertyRetriever);
     }
 
     @Test
     public void testNoWikiLinkToAnnotate() {
         MarkupText markupText = new MarkupText("Ordinary commit message without wiki link.");
         annotator.annotate(null, null, markupText);
         assertEquals("Ordinary commit message without wiki link.", markupText.toString() );
         context.assertIsSatisfied();
     }
 
     @Test
     public void testWikiLinkToAnnotate() {
         MarkupText markupText = new MarkupText("Message with WikiLink. Yes a link.");
         annotator.annotate(null, null, markupText);
         assertEquals("Message with <a href='http://code.google.com/wiki/WikiLink'>WikiLink</a>. Yes a link.", markupText.toString() );
         context.assertIsSatisfied();
     }
 
     @Test
     public void testIssueLinkToAnnotate() {
         MarkupText markupText = new MarkupText("Message with an issue 12.");
         annotator.annotate(null, null, markupText);
         assertEquals("Message with an <a href='http://code.google.com/issues/detail?id=12'>issue 12</a>.", markupText.toString() );
         context.assertIsSatisfied();
     }
 
     @Test
     public void testIssueWithHashLinkToAnnotate() {
         MarkupText markupText = new MarkupText("Message with an issue #12.");
         annotator.annotate(null, null, markupText);
        assertEquals("Message with an <a href='http://code.google.com/issues/detail?id=12'>issue #12</a>.", markupText.toString() );
         context.assertIsSatisfied();
     }
 }

 package org.ilrt.wf.facets.web.freemarker.templates;
 
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.rdf.model.Property;
 import com.hp.hpl.jena.rdf.model.Resource;
 import com.hp.hpl.jena.vocabulary.RDFS;
 import freemarker.template.Configuration;
 import freemarker.template.ObjectWrapper;
 import freemarker.template.Template;
 import org.ilrt.wf.facets.freemarker.JenaObjectWrapper;
 import org.junit.Test;
 import org.springframework.web.servlet.ModelAndView;
 import org.xml.sax.InputSource;
 
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathFactory;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.util.GregorianCalendar;
 import java.util.Locale;
 
 import static org.junit.Assert.assertEquals;
 
 /**
  * Test for the objects that wrap Jena objects so they can be rendered in a FreeMarker template.
  *
  * @author Mike Jones (mike.a.jones@bristol.ac.uk)
  */
 public class ResourceHashModelInTemplateTest extends AbstractTemplateTest {
 
     @Test
     public void test() throws Exception {
 
         // wrapper used by FreeMarker
         ObjectWrapper wrapper = new JenaObjectWrapper();
 
         // configure to find test_templates
         Configuration configuration = createTestConfiguration(wrapper, TEMPLATES_PATH);
        configuration.setLocale(Locale.US); // On Ubuntu we were getting "o'clock"
 
         // create a model and view
         ModelAndView mav = new ModelAndView();
 
         Model model = ModelFactory.createDefaultModel();
 
         // resources
         Resource resource = model.createResource(resourceUri);
         Resource person = model.createResource(personUri);
 
         // properties
         Property hasPersonProperty = model.createProperty(hasPersonUri);
         Property foafNameProperty = model.createProperty(foafName);
         Property hasAgeInYearsProperty = model.createProperty(hasAgeInYears);
         Property lastSeenProperty = model.createProperty(lastSeen);
 
         // statements
         model.add(model.createLiteralStatement(resource, RDFS.label, label));
         model.add(model.createStatement(resource, hasPersonProperty, person));
         model.add(model.createLiteralStatement(person, foafNameProperty, name));
         int age = 38;
         model.add(model.createLiteralStatement(person, hasAgeInYearsProperty, age));
 
         GregorianCalendar calendar = new GregorianCalendar(Locale.UK);
         calendar.set(2010, 3, 29, 11, 30, 5);
 
         model.add(model.createLiteralStatement(person, lastSeenProperty,
                 model.createTypedLiteral(calendar)));
 
         mav.addObject("resource", resource);
 
         // ---------- run the template
 
         Template template = configuration.getTemplate(TEMPLATE_NAME);
         StringWriter writer = new StringWriter();
         template.process(mav.getModel(), writer);
 
         String output = writer.getBuffer().toString();
 
         writer.flush();
 
         System.out.println(output);
 
         XPath engine = XPathFactory.newInstance().newXPath();
 
         // check we have the expected uri
         assertEquals("Unexpected uri", resourceUri, engine.evaluate("/div/p[1]/text()",
                 new InputSource(new StringReader(output))));
 
         // check we have the expected label
         assertEquals("Unexpected label", label, engine.evaluate("/div/p[2]/text()",
                 new InputSource(new StringReader(output))));
 
         // check we have the expected label via prefix
         assertEquals("Unexpected label", label, engine.evaluate("/div/p[3]/text()",
                 new InputSource(new StringReader(output))));
 
         // check we have the expected label
         assertEquals("Unexpected label", name, engine.evaluate("/div/p[4]/text()",
                 new InputSource(new StringReader(output))));
 
         // check we have the expected age
         assertEquals("Unexpected label", String.valueOf(age), engine.evaluate("/div/p[5]/text()",
                 new InputSource(new StringReader(output))));
 
         // check the full date
         assertEquals("Unexpected label", fullDate, engine.evaluate("/div/p[6]/text()",
                 new InputSource(new StringReader(output))));
 
         // check the short date
         assertEquals("Unexpected label", shortDate, engine.evaluate("/div/p[7]/text()",
                 new InputSource(new StringReader(output))));
 
         // check the custom date
         assertEquals("Unexpected label", customDate, engine.evaluate("/div/p[8]/text()",
                 new InputSource(new StringReader(output))));
 
     }
 
     // uris
     private final String resourceUri = "http://example.org/1/";
     private final String personUri = "http://example.org/person/1";
 
     // properties
     private final String hasPersonUri = "http://example.org/schema#hasPerson";
     private final String hasAgeInYears = "http://example.org/schema#hasAgeInYears";
     private final String lastSeen = "http://example.org/schema#lastSeen";
     private final String foafName = "http://xmlns.com/foaf/0.1/name";
 
     // literals
     private final String label = "Example Label";
     private final String name = "Fred Smith";
 
     private final String fullDate = "Thursday, April 29, 2010 11:30:05 AM BST";
     private final String shortDate = "4/29/10 11:30 AM";
     private final String customDate = "2010-04-29 11:30:05 British Summer Time";
 
     private final String TEMPLATES_PATH = "/test_templates/";
     private final String TEMPLATE_NAME = "resourceTest.ftl";
 }

 package au.com.miskinhill.rdf;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.SequenceInputStream;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.xml.stream.XMLEventReader;
 import javax.xml.stream.XMLInputFactory;
 import javax.xml.stream.XMLStreamConstants;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.events.XMLEvent;
 
 import com.hp.hpl.jena.rdf.model.Resource;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.config.BeanDefinition;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Component;
 
 import au.id.djc.rdftemplate.XMLStream;
 import au.id.djc.rdftemplate.selector.AbstractAdaptation;
 import au.id.djc.rdftemplate.selector.SelectorEvaluationException;
 
 import au.com.miskinhill.citation.Citation;
 import au.com.miskinhill.domain.fulltext.FulltextFetcher;
 
 @Component
 @Scope(BeanDefinition.SCOPE_PROTOTYPE)
 public class ContentAdaptation extends AbstractAdaptation<XMLStream, Resource> {
     
     private static final byte[] XHTML_STRICT_DTD_DECL = 
             ("<!DOCTYPE html " +
             "PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" " +
             "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n").getBytes();
     
     private final XMLInputFactory inputFactory;
     private final FulltextFetcher fulltextFetcher;
     
     @Autowired
     public ContentAdaptation(XMLInputFactory inputFactory, FulltextFetcher fulltextFetcher) {
         super(XMLStream.class, new Class<?>[] { }, Resource.class);
         this.inputFactory = inputFactory;
         this.fulltextFetcher = fulltextFetcher;
     }
     
     @Override
     protected XMLStream doAdapt(Resource resource) {
         if (!resource.getURI().startsWith("http://miskinhill.com.au/"))
             throw new SelectorEvaluationException("Attempted to apply #content to non-Miskin-Hill node " + resource);
         try {
             String pathToContent = resource.getURI().substring(25) + ".html";
             InputStream fulltext = new SequenceInputStream(
                     new ByteArrayInputStream(XHTML_STRICT_DTD_DECL),
                     fulltextFetcher.fetchFulltext(pathToContent));
             XMLEventReader fulltextReader = inputFactory.createXMLEventReader(fulltext);
             final List<XMLEvent> events = new ArrayList<XMLEvent>();
             while (fulltextReader.hasNext()) {
                 XMLEvent event = fulltextReader.nextEvent();
                 switch (event.getEventType()) {
                     case XMLStreamConstants.START_DOCUMENT:
                     case XMLStreamConstants.END_DOCUMENT:
                     case XMLStreamConstants.DTD:
                         // discard
                         break;
                     default:
                         events.add(event);
                 }
            };
             List<XMLEvent> eventsWithCitations = Citation.embedInDocument(URI.create(resource.getURI()), events.iterator());
             return new XMLStream(eventsWithCitations);
         } catch (XMLStreamException e) {
             throw new RuntimeException(e);
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
     }
 
 }

 package de.strud.xmlparser;
 
 import com.yammer.metrics.Metrics;
 import com.yammer.metrics.core.Meter;
 import com.yammer.metrics.core.MetricName;
 import de.strud.data.Document;
 import org.apache.log4j.Logger;
 
 import javax.xml.stream.XMLInputFactory;
 import javax.xml.stream.XMLStreamConstants;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamReader;
 import java.io.InputStream;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.concurrent.TimeUnit;
 
 /**
  * Simple stax parser, that parses wikipedia abstract dumps and transforms them into Document objects
  * (see http://dumps.wikimedia.org/enwiki/20121001/enwiki-20121001-abstract.xml).
  *
  * User: strud
  */
 public class XMLParser {
 
     private static final Logger LOG = Logger.getLogger(XMLParser.class);
 
     private final InputStream source;
 
     private Meter xmlMeter;
 
     public XMLParser(InputStream source) {
         if (source == null) {
             throw new IllegalArgumentException("InputStream cannot be null.");
         }
         this.source = source;
         this.xmlMeter = Metrics.newMeter(new MetricName("xml", "parsing", "rate"), "parsed", TimeUnit.SECONDS);
     }
 
     public List<Document> parse() {
         List<Document> documents = new LinkedList<>();
         XMLInputFactory factory = XMLInputFactory.newInstance();
         try {
             XMLStreamReader streamReader = factory.createXMLStreamReader(this.source);
             boolean inTitle = false;
             boolean inText = false;
             boolean inUrl = false;
             Document currentDoc = null;
             while(streamReader.hasNext()){
                 streamReader.next();
                 if(streamReader.getEventType() == XMLStreamReader.START_ELEMENT){
                     if ("doc".equals(streamReader.getLocalName())) {
                         currentDoc = new Document();
                     } else if ("title".equals(streamReader.getLocalName())) {
                         inTitle = true;
                    } else if ("abstract".equals(streamReader.getLocalName())) {
                         inText = true;
                    } else if ("url".equals(streamReader.getLocalName())) {
                         inUrl = true;
                     }
                 } else if (streamReader.getEventType() == XMLStreamConstants.CHARACTERS) {
                     if (inUrl) {
                         currentDoc.setUrl(streamReader.getText());
                     } else if (inText) {
                         currentDoc.setText(streamReader.getText());
                     } else if (inTitle) {
                         currentDoc.setTitle(streamReader.getText());
                     }
                 } else if(streamReader.getEventType() == XMLStreamReader.END_ELEMENT){
                     if ("doc".equals(streamReader.getLocalName())) {
                         documents.add(currentDoc);
                         this.xmlMeter.mark();
                         if (this.xmlMeter.count() % 50000 == 0) {
                             LOG.info("Parsed " + this.xmlMeter.count() + " documents, current rate " + this.xmlMeter.oneMinuteRate() + " docs/sec.");
                         }
                         currentDoc = null;
                     } else if ("title".equals(streamReader.getLocalName())) {
                         inTitle = false;
                    } else if ("abstract".equals(streamReader.getLocalName())) {
                         inText = false;
                    } else if ("url".equals(streamReader.getLocalName())) {
                         inUrl = false;
                     }
                 }
             }
         } catch (XMLStreamException e) {
             LOG.error("Cannot parse xml.", e);
         }
         return documents;
     }
 
 }

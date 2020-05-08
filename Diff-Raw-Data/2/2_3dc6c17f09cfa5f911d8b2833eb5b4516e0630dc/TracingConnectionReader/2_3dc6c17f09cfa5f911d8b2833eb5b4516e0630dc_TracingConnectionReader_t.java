 package niagara.client;
 
 import gnu.regexp.RE;
 import gnu.regexp.REMatch;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.FilterReader;
 import java.io.IOException;
 import java.io.StringReader;
 import java.io.Writer;
 
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.xml.sax.Attributes;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 
 import niagara.utils.PEException;
 
 public class TracingConnectionReader extends AbstractConnectionReader {
     UIDriverIF ui;
     TracingHandler th;
     Writer writer;
 
     public TracingConnectionReader(
 	UIDriverIF ui,
         String hostname,
         int port,
         DTDCache dtdCache,
         String outputFileName) {
         super(hostname, port, ui, dtdCache);
 	this.ui = ui;
         th = new TracingHandler();
         if (outputFileName != null)
             try {
                 writer = new BufferedWriter(new FileWriter(outputFileName));
             } catch (IOException ioe) {
                 System.err.println(
                     "Could not create output file: " + outputFileName);
             }
     }
 
     public void run() {
         try {
             ((SimpleClient) ui).setConnectionReader(this);
             if (writer != null) {
                 // put the data to a file
                 char[] buf = new char[4096];
                 int start;
                 int count = 0;
                 boolean stop = false;
                 while (!stop) {
                     start = 0;
                     while (start < 4096) {
                         count = cReader.read(buf, start, 4096-start);
                         if (count == -1) {
                             stop = true;
                             break;
                         }
                         start = start + count;
                     }
                     writer.write(buf, 0, start);
                 }
                 writer.close();
                ui.notifyFinalResult(0);
             }
 
             // Otherwise, start parsing
             SAXParser parser;
             try {
                 SAXParserFactory factory = SAXParserFactory.newInstance();
                 parser = factory.newSAXParser();
                 parser.parse(new InputSource(cReader), th);
             } catch (Exception e) {
                 e.printStackTrace();
                 throw new RuntimeException(
                     "Parser Exception: " + e.getMessage());
             }
         } catch (Exception e) {
             e.printStackTrace();
             throw new PEException("XXX vpapad @#$@#$@#$");
         }
     }
 
     class TracingHandler extends DefaultHandler {
         BufferedWriter fw;
         public TracingHandler() {
             try {
                 fw = new BufferedWriter(new FileWriter("auction.client"));
             } catch (IOException ioe) {
                 ioe.printStackTrace();
             }
         }
 
         /**
           * @see org.xml.sax.ContentHandler#startElement(String, String, String, Attributes)
           */
         public void startElement(
             String namespaceURI,
             String localName,
             String qName,
             Attributes atts)
             throws SAXException {
             try {
                 String id = atts.getValue("id");
                 String ts = atts.getValue("ts");
                 if (id == null || ts == null)
                     return;
                 fw.write(id);
                 fw.write(",");
                 fw.write(String.valueOf(System.currentTimeMillis()));
                 fw.write("\n");
             } catch (IOException ioe) {
                 throw new RuntimeException("IOException " + ioe.getMessage());
             }
         }
 
         /**
          * @see org.xml.sax.ContentHandler#endDocument()
          */
         public void endDocument() throws SAXException {
             try {
                 fw.close();
             } catch (IOException ioe) {
             }
 	    ui.notifyFinalResult(0);
         }
     }
 }

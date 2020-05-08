 package cz.cvut.felk.via.datalayer;
 
 import java.text.ParseException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.xml.sax.*;
 import org.xml.sax.helpers.*;
 
 import cz.cvut.felk.via.data.Event;
 
 import java.io.*;
 import java.net.URL;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 
 /**
  * @author Petr Kubasta
  * Base data class of event.
  */
 public class Xom extends DefaultHandler {
 
     DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
     private CharArrayWriter contents = new CharArrayWriter();
     
     private ArrayList<Event> EventsList = new ArrayList<Event>();
     private Event currentEvent = new Event();
 
     @Override
     public void startDocument() throws SAXException {
         //   System.out.println("SAX Event: START DOCUMENT");
     }
 
     @Override
     public void endDocument() throws SAXException {
         //   System.out.println("SAX Event: END DOCUMENT");
     }
 
     @Override
     public void startElement(String namespaceURI, String localName, String qName, Attributes attr) throws SAXException {
         contents.reset();
 
         if (localName.equals("item")) {
             currentEvent = new Event();
             EventsList.add(currentEvent);
             currentEvent.setEventOrganiser("ČVUT");
         }
 
     }
 
     @Override
     public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
         if (localName.equals("title")) {
             currentEvent.setShortDescription(contents.toString());
         }
 
         if (localName.equals("datum")) {
             String datum = contents.toString();
             datum = datum.substring(0, 10); // Oriznu to na delku 10
             try {
                 currentEvent.setStartEvent(df.parse(datum));
             } catch (ParseException ex) {
                 Logger.getLogger(Xom.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
 
         if (localName.equals("description")) {
            currentEvent.setLongDescription(contents.toString());
         }
 
         if (localName.equals("category")) {
             currentEvent.setCategory(contents.toString());
         }
     }
 
     @Override
     public void characters(char[] ch, int start, int length) throws SAXException {
         this.contents.write(ch, start, length);
     }
 
     public ArrayList<Event> getEvents() {
         return this.EventsList;
     }
 
     public ArrayList<Event> getCVUTEvents(String xmlUrl)  {
         try {
             URL url = new URL(xmlUrl);
             InputStream in = url.openStream();
 
             // Create SAX 2 parser...
             XMLReader xr = XMLReaderFactory.createXMLReader();
 
             // Set the ContentHandler...
             xr.setContentHandler(this);
             // Parse the file...
             xr.parse(new InputSource(in));
 
         } catch (Exception e) {
             e.printStackTrace();
         }
         return this.getEvents();
     }
 }

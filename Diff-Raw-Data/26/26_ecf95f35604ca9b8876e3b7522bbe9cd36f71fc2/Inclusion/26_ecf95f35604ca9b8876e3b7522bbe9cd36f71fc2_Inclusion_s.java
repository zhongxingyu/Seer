 package org.wandledi.spells;
 
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.xml.sax.Attributes;
 import org.xml.sax.ContentHandler;
 import org.xml.sax.InputSource;
 import org.xml.sax.Locator;
 import org.xml.sax.SAXException;
 import org.xml.sax.XMLReader;
 import org.xml.sax.helpers.XMLReaderFactory;
 import org.wandledi.Scroll;
 import org.wandledi.Spell;
 import org.wandledi.VoidResolver;
 
 /**With this spell you can include other html files into the current one.
  * Note that the element this spell is apply upon will cease existing.
  * You could say that it is sacrificed for the summoning of the
  * new content.
  *
  * @author Markus Kahl
  */
 public class Inclusion extends ArchSpell implements ContentHandler {
 
     private XMLReader parser;
     private InclusionIntent intent;
 
     public Inclusion(final String file) {
 
         this(new InclusionIntent() {
             public String getFile() {
                 return file;
             }
             public Scroll getScroll() {
                 return new Scroll();
             }
         });
     }
 
     public Inclusion(final String file, final Scroll scroll) {
 
         this(new InclusionIntent() {
             public String getFile() {
                 return file;
             }
             public Scroll getScroll() {
                 return scroll;
             }
         });
     }
 
     public Inclusion(InclusionIntent intent) {
 
         super(intent.getScroll() != null ? intent.getScroll() : new Scroll());
         this.intent = intent;
         try {
             parser = XMLReaderFactory.createXMLReader();
             parser.setContentHandler(this);
             parser.setEntityResolver(new VoidResolver());
         } catch (SAXException ex) {
             throw new RuntimeException("Inclusion failed", ex);
         }
     }
 
     @Override
     public Spell clone() {
 
         return new Inclusion(intent);
     }
 
     public void startTransformedElement(String name, Attributes attributes) {
 
         if (ignoreBounds()) return;
         try {
             parser.parse(new InputSource(getResources().open(intent.getFile())));
         } catch (IOException ex) {
            Logger.getLogger(Inclusion.class.getName()).log(Level.SEVERE, null, ex);
         } catch (SAXException ex) {
            Logger.getLogger(Inclusion.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     public void startElement(String name, Attributes attributes) { }
 
     public void endElement(String name) { }
 
     public void writeCharacters(char[] characters, int offset, int length) { }
 
     public void endTransformedElement(String name) { }
 
     public void startElement(String uri, String localName, String qName, Attributes atts)
             throws SAXException {
 
         if (!localName.equalsIgnoreCase("html")) {
             super.startElement(localName, atts);
         } // else skip HTML root element
     }
 
     public void endElement(String uri, String localName, String qName) throws SAXException {
 
         if (!localName.equalsIgnoreCase("html")) {
             super.endElement(localName);
         } // else skip HTML root element
     }
 
     public void characters(char[] ch, int start, int length) throws SAXException {
 
         super.writeCharacters(ch, start, length);
     }
 
    public void setDocumentLocator(Locator locator) { }
 
     public void startDocument() throws SAXException { }
 
     public void endDocument() throws SAXException { }
 
     public void startPrefixMapping(String prefix, String uri) throws SAXException { }
 
     public void endPrefixMapping(String prefix) throws SAXException { }
 
     public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException { }
 
     public void processingInstruction(String target, String data) throws SAXException { }
 
     public void skippedEntity(String name) throws SAXException {
         writeString("&");
         writeString(name);
         writeString(";");
     }
 }

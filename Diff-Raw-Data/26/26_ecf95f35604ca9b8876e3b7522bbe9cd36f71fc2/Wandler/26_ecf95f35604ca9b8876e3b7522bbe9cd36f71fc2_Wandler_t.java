 package org.wandledi;
 
 import java.io.BufferedWriter;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.Reader;
 import java.io.Writer;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.xml.sax.Attributes;
 import org.xml.sax.ContentHandler;
 import org.xml.sax.InputSource;
 import org.xml.sax.Locator;
 import org.xml.sax.SAXException;
 import org.xml.sax.XMLReader;
 import org.xml.sax.helpers.XMLReaderFactory;
 import org.wandledi.spells.ArchSpell;
 
 /**
  *
  * @author Markus Kahl
  */
 public class Wandler implements ContentHandler, Spell {
 
     private XMLReader parser;
     private Locator locator;
     private BufferedWriter out;
     private boolean preserve;
     private boolean startUnderConstruction = false;
     private long calls = 0;
     private long lastStart = -1;
     private final static Set<String> emptyElements = new HashSet<String>(
         java.util.Arrays.asList(
             "area", "base", "basefont", "br", "col", "hr", "img", "input",
             "isindex", "link", "meta", "param"
     ));
     private ArchSpell rootSpell = new ArchSpell(new Scroll());
     private Resources resources = new Resources() {
         public Reader open(String file) throws IOException {
             return new FileReader(file);
         }
     };
 
     public Wandler() {
         rootSpell.setParent(this);
         try {
             parser = XMLReaderFactory.createXMLReader();
             parser.setContentHandler(this);
             parser.setEntityResolver(new VoidResolver());
         } catch (SAXException ex) {
             throw new RuntimeException("Could not create Wandler", ex);
         }
     }
 
     public void reset() {
         preserve = false;
         startUnderConstruction = false;
         calls = 0;
         lastStart = -1;
         rootSpell.reset();
     }
 
     @Override
     public Spell clone() {
         throw new UnsupportedOperationException("Sorry, but this won't work.");
     }
 
     /**Sets the resources of this Wandler.
      * Resources may be accessed by Spells, such as
      * the Inclusion to be able to include files.
      *
      * @param resources Resources for this Wandler.
      */
     public void setResources(Resources resources) {
         this.resources = resources;
     }
 
     /**Gets the resources of this Wandler.
      * Resources may be accessed by Spells, such as
      * the Inclusion to be able to include files.
      *
      * @return This Wandler's resources.
      */
     public Resources getResources() {
         return resources;
     }
 
     public boolean hierarchyContains(Spell spell) {
         return spell == this; // Wandler is the end of the hierarchy
     }
 
     protected boolean preserveSpace(String localName) {
         return !emptyElements.contains(localName);
     }
 
     public void wandle(Reader in, Writer out) {
         try {
             reset();
             this.out = new BufferedWriter(out, 2048);
             parser.parse(new InputSource(in));
         } catch (IOException ex) {
             Logger.getLogger(Wandler.class.getName()).log(Level.SEVERE, "IOException", ex);
         } catch (SAXException ex) {
            int lineNumber = locator != null ? locator.getLineNumber() : -1;
            String message = "Error parsing input" +
                (lineNumber != -1 ? " at line " + lineNumber : "");
            Logger.getLogger(Wandler.class.getName()).log(Level.SEVERE, message, ex);
         } finally {
             if (this.out != null) {
                 try {
                     this.out.close();
                 } catch (IOException ex) {
                     Logger.getLogger(Wandler.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         }
     }
 
     public void startElement(String name, Attribute... attributes) {
         startElement(name, new SimpleAttributes(attributes));
     }
 
     public void startElement(String name, Attributes attributes) {
         ++calls;
         if (startUnderConstruction) {
             write(">");
         }
         openElement(name, attributes);
         startUnderConstruction = true;
         lastStart = calls;
     }
 
     public void endElement(String name) {
         ++calls;
         if (startUnderConstruction && noNestedElement() && !preserve) {
             write("/>");
         } else {
             if (startUnderConstruction) {
                 write(">");
             }
             write("</" + name + ">");
         }
         startUnderConstruction = false;
     }
 
     public void writeCharacters(char[] characters, int offset, int length) {
         if (startUnderConstruction) {
             write(">");
             startUnderConstruction = false;
         }
         write(characters, offset, length);
     }
 
     public void writeString(String string) {
         char[] characters = string.toCharArray();
         writeCharacters(characters, 0, characters.length);
     }
 
     public void startDocument() throws SAXException {
     }
 
     public void endDocument() throws SAXException {
         try {
             out.flush();
         } catch (IOException ex) {
             Logger.getLogger(Wandler.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     public void startElement(String uri, String localName, String qName, Attributes atts)
             throws SAXException {
         try {
             rootSpell.startElement(localName, atts);
         } catch (Exception e) {
             throw new SAXException("Could not start " + stringFor(localName, atts), e);
         }
     }
 
     protected String stringFor(String label, Attributes attr) {
         StringBuilder sb = new StringBuilder();
         sb.append("<"); sb.append(label);
         for (int i = 0; i < attr.getLength(); ++i) {
             sb.append(" "); sb.append(attr.getLocalName(i));
             sb.append("=\""); sb.append(attr.getValue(i)); sb.append("\"");
         }
         sb.append(">");
         return sb.toString();
     }
 
     public void endElement(String uri, String localName, String qName) throws SAXException {
         try {
             rootSpell.endElement(localName);
         } catch (Exception e) {
             throw new SAXException("Could not end " + stringFor(localName, new SimpleAttributes()));
         }
     }
 
     public void characters(char[] ch, int start, int length) throws SAXException {
         try {
             rootSpell.writeCharacters(ch, start, length);
         } catch (Exception e) {
             throw new SAXException("Could not write \"" + new String(ch, start, length) + "\"", e);
         }
     }
 
     private boolean noNestedElement() {
         return (calls - lastStart) == 1;
     }
 
     protected void openElement(String name, Attributes atts) {
         write("<");
         write(name);
         if (atts != null) {
             for (int i = 0; i < atts.getLength(); ++i) {
                 write(" ");
                 write(atts.getLocalName(i));
                 write("=\"");
                 write(atts.getValue(i));
                 write("\"");
             }
         }
         preserve = preserveSpace(name);
     }
 
     public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
         if (!startUnderConstruction) {
             write(new String(ch, start, length));
         }
     }
 
     public void setDocumentLocator(Locator locator) {
         this.locator = locator;
     }
 
     public final void startPrefixMapping(String prefix, String uri) throws SAXException {
     }
 
     public final void endPrefixMapping(String prefix) throws SAXException {
     }
 
     public void processingInstruction(String target, String data) throws SAXException {
     }
 
     public void skippedEntity(String name) throws SAXException {
         write("&");
         write(name);
         write(";");
     }
 
     protected final void write(String s) {
         try {
             out.write(s);
         } catch (IOException ex) {
             throw new RuntimeException("Write failed", ex);
         }
     }
 
     protected final void write(char[] characters, int offset, int length) {
         try {
             out.write(characters, offset, length);
         } catch (IOException ex) {
             throw new RuntimeException("Write failed", ex);
         }
     }
 
     public void setParent(Spell transformation) {
     }
 
     public void startTransformedElement(String name, Attributes attributes) {
         startElement(name, attributes);
     }
 
     public void endTransformedElement(String name) {
         endElement(name);
     }
 
     public Scroll usedScroll() {
         return rootSpell.getScroll();
     }
 
     public void useScroll(Scroll scroll) {
         rootSpell.setScroll(scroll);
     }
 
     public void ignoreBounds(boolean ignoreBounds) {
         // does not matter here
     }
 
     public boolean ignoreBounds() {
         return false;
     }
 }

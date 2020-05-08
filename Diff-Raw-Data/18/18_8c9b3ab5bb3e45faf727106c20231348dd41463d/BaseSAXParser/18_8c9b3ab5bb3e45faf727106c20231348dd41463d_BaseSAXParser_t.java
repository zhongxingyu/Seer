 package uk.org.catnip.eddie.parser;
 
 import org.xml.sax.helpers.DefaultHandler;
 import org.xml.sax.ext.DefaultHandler2;
 import org.xml.sax.ErrorHandler;
 import org.xml.sax.SAXParseException;
 import org.xml.sax.SAXException;
 import org.xml.sax.Attributes;
 import org.xml.sax.Locator;
 import org.apache.log4j.Logger;
 import java.util.Stack;
 import org.apache.commons.codec.binary.Base64;
 import uk.org.catnip.eddie.Feed;
 import uk.org.catnip.eddie.FeedContext;
 import uk.org.catnip.eddie.Entry;
 import uk.org.catnip.eddie.Detail;
 import uk.org.catnip.eddie.parser.Entities;
 public class BaseSAXParser extends DefaultHandler2 implements ErrorHandler {
     static Logger log = Logger.getLogger(BaseSAXParser.class);
 
     protected Locator locator;
 
     protected String filename;
     private String contentLocation;
     
     protected Feed feed = new Feed();
     protected Entry current_entry;
     protected Detail detail;
 
     protected boolean in_contributor = false;
     protected boolean in_author = false;
     protected boolean in_feed = false;
     protected boolean in_entry = false;
     protected boolean in_textinput = false;
     protected boolean in_image = false;
     protected boolean in_source = false;
     protected int in_content = 0;
     private boolean next_data_is_inline_element = false;
     protected Stack stack = new Stack();
 
     public void setFilename(String file) {
         this.filename = file;
     }
 
     public void setDocumentLocator(Locator locator) {
         this.locator = locator;
     }
 
     public Feed getFeed() {
         return feed;
     }
 
     protected State getCurrentState() {
         if (!stack.empty()){
             return (State)stack.peek();
         } 
         return new State();
     }
 
     protected void push(State state) {
         if (stack.isEmpty() && state.getBase() == null) {
             if (contentLocation != null){ 
                 state.setBase(contentLocation);
             } else {
                 state.setBase(filename);  
             }
         }
         log.debug("pushing "+ state);
         stack.push(state);     
     }
 
     protected FeedContext getCurrentContext() {
         if (in_entry) {
             if (in_source) {
                 return current_entry.getSource();
             } else {
                 return current_entry;
             }
         } else {
             return feed;
         }
     }
     public void startDocument() throws SAXException {
         log.trace("startDocument:" + this.filename);
     }
 
     public void endDocument() throws SAXException {
         log.trace("endDocument:" + this.filename);
     }
 
     public void startElement(String uri, String localName, String qName,
             Attributes atts) throws SAXException {
         
         State state = new State(uri, localName, qName, atts, getCurrentState());
         log.trace("startElement:" + localName + " ("+state.getElement() + ")");
         if (state.mode != null) {
             if (in_content > 0 && state.mode.equals( "escaped")) {
                 state.mode = "xml";
             }
             if (in_content > 0 && state.mode.equals( "xml")) {
                handle_data(state,Sanitise.clean_html_start(state), false);
                state.content = true;
                push(state);
                return;
             }
         }
 
         
         
         try {
             Class[] argTypes = { State.class };
             Object[] values = {state };
             int stacksize = stack.size();
             this.getClass().getMethod("startElement_" + state.getElement(), argTypes)
                     .invoke(this, values);
             if (stack.size() == stacksize) {
                 //naughty handler didn't push state
                 push(state);
             }
         } catch (NoSuchMethodException e) {
             log.trace("unhandled element " + state.getElement());
             state.expectingText = true;
             push(state);
             // } catch (InvocationTargetException e) {
             // throw e.getCause();
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     public void endElement(String uri, String localName, String qName)
             throws SAXException {
         
         State state = new State(uri, localName, qName);
         State prev = getCurrentState();
         log.trace("end_element: " + state);
         if (prev.mode != null) {
             if (in_content > 0 && prev.mode.equals("escaped") && prev.content) {
                 prev.mode = "xml";
             }
             if (in_content > 0 && prev.mode.equals("xml") && prev.content) {
                 String data = pop(localName);
                 handle_data(prev, data + Sanitise.clean_html_end(state.getElement()), false);
                return;
             }
          }
         try {
             this.getClass().getMethod("endElement_" + state.getElement(), (Class[])null)
                     .invoke(this, (Object[])null);
         } catch (NoSuchMethodException e) {
             log.debug("unhandled element " + state.getElement());
             pop(state.getElement());
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     protected String pop(String element) {
         if (stack.empty()) { return ""; }
    
         if (!getCurrentState().getElement().equals(element)) { return "";}
         State state = (State)stack.pop();
         log.debug("popping " + state);
        String output = state.getText(); 
        
         detail = new Detail();
         detail.setBase(state.getBase());
         detail.setLanguage(state.getLanguage());
         detail.setType(state.getType());
         detail.setSrc(state.getAttr("src")); //TODO: this seems out of place
         if (!output.equals("")){
         detail.setValue(output);
         }
         if (!state.expectingText) { return output; }
         
         if (state.mode != null && state.mode.equals("base64")) {
             output = new String(Base64.decodeBase64(output.trim().getBytes()));
             detail.setValue(output);
         }
         if (state.getType().equals("application/octet-stream")) {
             output = new String(Base64.decodeBase64(output.trim().getBytes()));
             detail.setValue(output);
         }
         
         // If mode == escaped and content-type == application/octet-stream
         // base64-decode test
         
         // resolve href links
         
         // If element can be relative url, resolve link
 
         return output;
         
     }
     public void characters(char[] ch, int start, int length) {
         String data =  new String(ch, start,length);
         //data.trim();
         
         if (in_content > 0 && !getCurrentState().getType().equals("text/html") && !getCurrentState().getType().equals("text/plain")) {
         if (data.equals("<")) { data = "&lt;"; }
         if (data.equals(">")) { data = "&gt;"; }
         if (data.equals("&")) { data = "&amp;"; }
         }
         
         //if (next_data_is_inline_element) {
         //    data = Sanitise.handle_inline_data(data);
         //    next_data_is_inline_element = false;
         //}
         //if (data.equals("<")) { next_data_is_inline_element = true; data = ""; }
         log.debug("characters: "+data);
         getCurrentState().addText(data);
     }
 
     
     public void handle_data(State state, String data, boolean escape) {
         log.debug("handle_date('"+data+"')");
         if (stack.empty()) { return; }
         if (escape && state.getType().equals("application/xhtml+xml")) {
            data = xmlescape(data);
         }
         
         getCurrentState().addText(data);
     }
     
     public String xmlescape(String data) {
         data.replace("&", "&amp;");
         data.replace("<", "&lt;");
         data.replace(">", "&gt;");
         return data;
     }
    
    
     
     public void startEntity(java.lang.String name)
     throws SAXException {
        getCurrentState().addText(Entities.resolveEntity(name));
     }
     
     // ErrorHandler
     public void warning(SAXParseException exception) throws SAXException {
         log.debug("warning: " + filename + ": " + exception.getMessage());
         // throw new SAXException(exception);
     }
 
     public void error(SAXParseException exception) throws SAXException {
         feed.error = true;
         log.debug("error: " + filename + ": " + exception.getMessage());
         // throw new SAXException(exception);
     }
 
     public void fatalError(SAXParseException exception) throws SAXException {
         feed.error = true;
         log.debug("fatalError: " + filename + ": " + exception.getMessage());
         // throw new SAXException(exception);
     }
 
     public String getContentLocation() {
         return contentLocation;
     }
 
     public void setContentLocation(String contentLocation) {
         log.debug("setting Content-Location to '"+ contentLocation +"'");
         this.contentLocation = contentLocation;
     }
 }

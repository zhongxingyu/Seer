 /* 
  * Eddie RSS and Atom feed parser
  * Copyright (C) 2006  David Pashley
  * 
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  * 
  * Linking this library statically or dynamically with other modules is making a
  * combined work based on this library. Thus, the terms and conditions of the GNU
  * General Public License cover the whole combination.
  * 
  * As a special exception, the copyright holders of this library give you
  * permission to link this library with independent modules to produce an
  * executable, regardless of the license terms of these independent modules, and
  * to copy and distribute the resulting executable under a liense certified by the
  * Open Source Initative (http://www.opensource.org), provided that you also meet,
  * for each linked independent module, the terms and conditions of the license of
  * that module. An independent module is a module which is not derived from or
  * based on this library. If you modify this library, you may extend this
  * exception to your version of the library, but you are not obligated to do so.
  * If you do not wish to do so, delete this exception statement from your version.
  */
 package uk.org.catnip.eddie.parser;
 
 import org.xml.sax.ext.DefaultHandler2;
 import org.xml.sax.ErrorHandler;
 import org.xml.sax.SAXParseException;
 import org.xml.sax.SAXException;
 import org.xml.sax.Attributes;
 import org.xml.sax.Locator;
 import org.apache.log4j.Logger;
 import java.util.Stack;
 import org.apache.commons.codec.binary.Base64;
 import uk.org.catnip.eddie.Detail;
 import uk.org.catnip.eddie.parser.Entities;
 
 public class BaseSAXParser extends DefaultHandler2 implements ErrorHandler {
     static Logger log = Logger.getLogger(BaseSAXParser.class);
 
     protected Locator locator;
 
     protected String filename;
     private String contentLocation;
     private String contentLanguage;
     protected Detail detail;
 
     protected int in_content = 0;
     protected Stack<State> stack = new Stack<State>();
 
     public void setFilename(String file) {
         this.filename = file;
     }
 
     public void setDocumentLocator(Locator locator) {
         this.locator = locator;
     }
 
     protected State getCurrentState() {
         if (!stack.empty()){
             return (State)stack.peek();
         } 
         return new State();
     }
 
     protected void push(State state) {
         if (stack.isEmpty()) {
             String newbase;
             if (contentLocation != null){  
                 newbase = contentLocation;
             } else {
                 newbase = filename;  
             }
             if (state.getBase() == null) {
                 state.setBase(newbase);
             } else if (state.isBaseRelative()) {
                 state.resolveBaseWith(newbase);
             }
             
         }
         if (log.isTraceEnabled()) {
             log.trace("pushing "+ state);
         }
         stack.push(state);     
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
         if(state.getLanguage() == null && contentLanguage != null) {
             state.setLanguage(contentLanguage);
         }
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
             log.trace("unhandled element " + state.getElement());
             pop(state.getElement());
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     protected String pop(String element) {
         if (stack.empty()) { return ""; }
    
         if (!getCurrentState().getElement().equals(element)) { return "";}
         State state = (State)stack.pop();
         if (log.isTraceEnabled()) {
             log.trace("popping " + state);
         }
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
         if (in_content > 0) {
             output = Sanitise.clean(output, state);
             detail.setValue(output);
         }
         return output;
         
     }
     public void characters(char[] ch, int start, int length) {
         String data =  new String(ch, start,length);
         //data.trim();
         
         if (in_content > 0) {
             if(getCurrentState().getType().equals("application/xhtml+xml")) {
             if (data.equals("<")) { data = "&lt;"; }
             if (data.equals(">")) { data = "&gt;"; }
             if (data.equals("&")) { data = "&amp;"; }
             } else if (getCurrentState().getType().equals("text/plain")) {
                 if (data.equals("<")) { data = "&lt;"; }
             }
         }
         log.trace("characters: "+data);
         getCurrentState().addText(data);
     }
 
     
     public void handle_data(State state, String data, boolean escape) {
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
         this.contentLocation = contentLocation;
     }
 
     public String getContentLanguage() {
         return contentLanguage;
     }
 
     public void setContentLanguage(String contentLanguage) {
         this.contentLanguage = contentLanguage;
     }
 }

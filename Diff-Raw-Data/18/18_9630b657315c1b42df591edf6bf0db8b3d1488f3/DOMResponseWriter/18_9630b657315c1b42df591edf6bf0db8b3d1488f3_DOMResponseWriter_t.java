 /*
  * Copyright 2004-2013 ICEsoft Technologies Canada Corp.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the
  * License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an "AS
  * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
  * express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 
 package org.icefaces.impl.context;
 
 import org.icefaces.impl.fastinfoset.com.sun.xml.fastinfoset.dom.DOMDocumentParser;
 import org.icefaces.impl.fastinfoset.com.sun.xml.fastinfoset.dom.DOMDocumentSerializer;
 import org.icefaces.impl.fastinfoset.org.jvnet.fastinfoset.FastInfosetException;
 import org.icefaces.impl.util.DOMUtils;
 import org.icefaces.util.EnvUtils;
 import org.w3c.dom.Attr;
 import org.w3c.dom.CDATASection;
 import org.w3c.dom.DOMException;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 
 import javax.faces.application.ProjectStage;
 import javax.faces.component.UIComponent;
 import javax.faces.component.UIViewRoot;
 import javax.faces.context.FacesContext;
 import javax.faces.context.PartialViewContext;
 import javax.faces.context.ResponseWriter;
 import javax.faces.context.ResponseWriterWrapper;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.FilterWriter;
 import java.io.IOException;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 
 public class DOMResponseWriter extends ResponseWriterWrapper {
     private static Logger log = Logger.getLogger("org.icefaces.impl.context.DOMResponseWriter");
 
     public static final String DEFAULT_ENCODING = "UTF-8";
     private String encoding;
     public static final String DEFAULT_TYPE = "text/html";
     private String contentType;
 
     public static final String OLD_DOM = "org.icefaces.old-dom";
     protected static final String XML_MARKER = "<?xml";
     protected static final String DOCTYPE_MARKER = "<!DOCTYPE";
 
     private Writer writer;
     private ResponseWriter wrapped;
     private Document document;
     private Node cursor;
     private List<Node> stateNodes = new ArrayList<Node>();
     private boolean suppressNextNode = false;
 
 
     // flag to indicate we shouldn't escape 
     private boolean dontEscape;
 
     // flag to indicate that we're writing a 'script' or 'style' element
     private boolean isScript;
 
     // flag to indicate that we're writing a 'style' element
     private boolean isStyle;
 
 
     public DOMResponseWriter(Writer writer, String encoding, String contentType) {
         this.writer = writer;
 
         if (writer instanceof ResponseWriter) {
             wrapped = (ResponseWriter) writer;
         }
 
         this.encoding = (encoding != null) ? encoding : DEFAULT_ENCODING;
         this.contentType = (contentType != null) ? contentType : DEFAULT_TYPE;
     }
 
     public ResponseWriter getWrapped() {
         return wrapped;
     }
 
     public String getCharacterEncoding() {
         return encoding;
     }
 
     public String getContentType() {
         return contentType;
     }
 
     public void writeDoctype(String doctype) throws IOException {
         processXMLPreamble(doctype);
     }
 
     public void writePreamble(String preamble) throws IOException {
         processXMLPreamble(preamble);
     }
 
     public void write(char[] cbuf, int off, int len) throws IOException {
         if (suppressNextNode) {
             //a component is attempting to render outside its subtree
             return;
         }
         if (0 == len) {
             return;
         }
 
         if (null == document) {
             writer.write(cbuf, off, len);
             return;
         }
 
         try {
             String data = new String(cbuf, off, len);
 
             //Handle the <?xml... and <!DOCTYPE.. preamble as text rather than
             //trying to handle them as DOM nodes.
             //TODO: perhaps handling them as DOM elements would be beneficial?
             if (data.startsWith(XML_MARKER) || data.startsWith(DOCTYPE_MARKER)) {
                 processXMLPreamble(data);
                 return;
             }
 
             appendToCursor(data);
         } catch (Exception e) {
             if (log.isLoggable(Level.INFO)) {
                 log.log(Level.INFO, "cannot write " + new String(cbuf, off, len), e);
             }
         }
     }
 
     private void processXMLPreamble(String data) throws IOException {
         FacesContext fc = FacesContext.getCurrentInstance();
 
         //First check if this is a portlet as the preamble processing
         //is not required at all in that case.
         if( EnvUtils.instanceofPortletRequest(fc.getExternalContext().getRequest()) ) {
             return;
         }
 
         PartialViewContext pvc = fc.getPartialViewContext();
 
         //If it's an Ajax request, then we need to store the preamble
         //as view attributes and rewrite them out for ViewRoot updates.
         if (pvc != null && pvc.isAjaxRequest() ) {
             if (data.startsWith(XML_MARKER)) {
                 storeViewAttribute(XML_MARKER, data);
             }
 
             if (data.startsWith(DOCTYPE_MARKER)) {
                 storeViewAttribute(DOCTYPE_MARKER, data);
             }
         } else {
             //For non-Ajax, servlet requests simply defer to the normal processing.
             writer.write(data);
         }
     }
 
     private void storeViewAttribute(String key, Object value) {
         FacesContext fc = FacesContext.getCurrentInstance();
         UIViewRoot root = fc.getViewRoot();
         root.getAttributes().put(key, value);
     }
 
     public void write(String str) throws IOException {
         if ("".equals(str)) {
             return;
         }
         if (EnvUtils.getStateMarker().equals(str)) {
             //TODO: suppress insertion of state node into DOM rather than
             //remove later.  This special case is caused by the fact that
             //during partial responses the Sun implementation does not
             //write the state marker
             Node stateNode = document.createTextNode(str);
             stateNodes.add(stateNode);
             appendToCursor(stateNode);
             return;
         }
         if (null == document) {
             if (log.isLoggable(Level.FINEST)) {
                 log.finest(writer.getClass() + " raw write str " + str);
             }
             writer.write(str);
             return;
         }
         try {
             if (cursor == null) {
                 Element doc = document.getDocumentElement();
                 if( doc != null ){
                     cursor = doc;
                 } else {
                     cursor = document;
                 }
             }
             if (cursor.getNodeType() == Node.CDATA_SECTION_NODE) {
                 CDATASection section = (CDATASection) cursor;
                 section.appendData(str);
             } else {
                 appendToCursor(document.createTextNode(str));
             }
         } catch (Exception e) {
             log.log(Level.SEVERE, "failed to write " + str, e);
         }
     }
 
 
     public void flush() throws IOException {
     }
 
     public void close() throws IOException {
     }
 
     public void startDocument() throws IOException {
         document = DOMUtils.getNewDocument();
         cursor = document;
     }
 
     public void endDocument() throws IOException {
         boolean isPartialRequest = FacesContext.getCurrentInstance().getPartialViewContext().isPartialRequest();
 
         //full-page requests write directly to the response
         if (!isPartialRequest) {
             if(EnvUtils.isMojarra() ){
                 DOMUtils.printNode(document, writer);
             } else if (EnvUtils.isMyFaces()){
                 //With MyFaces, we simulate Mojarra's "mark and replace" approach. Mojarra
                 //already replaces the marker for us but need to do it for MyFacesFaces.
                 DOMUtils.printNode(document, new WriteViewStateMarkup(writer));
             }
         }
 
         if (null != document.getDocumentElement()) {
             for (Node stateNode : stateNodes) {
                 Node parent = stateNode.getParentNode();
                 if(parent != null){
                     parent.removeChild(stateNode);
                 } else {
                     log.fine("could not remove state node " + stateNode + " as it had no parent");
                 }
             }
             stateNodes.clear();
             saveOldDocument();
         }
 
         document = null;
         cursor = null;
     }
 
     public void startElement(String name, UIComponent component) throws IOException {
         if (suppressNextNode) {
             //this node has already been created and is just a placeholder
             //in the tree
             suppressNextNode = false;
             return;
         }
         isScriptOrStyle(name);
 
         if (null == document) {
             document = DOMUtils.getNewDocument();
         }
         pointCursorAt(appendToCursor(document.createElement(name)));
     }
 
     public void endElement(String name) throws IOException {
         if (FacesContext.getCurrentInstance().isProjectStage(ProjectStage.Development)) {
             if (log.isLoggable(Level.WARNING)) {
                 if (!cursor.getNodeName().equals(name)) {
                     String path = "";
                     Node tempCursor = cursor;
                     while (tempCursor != null) {
                         if (tempCursor != cursor) {
                             path = " -> " + path;
                         }
                         path = tempCursor.getNodeName() + path;
                         tempCursor = tempCursor.getParentNode();
                     }
                     Node idNode = cursor.getAttributes().getNamedItem("id");
                     log.log(Level.WARNING, "Missing end-element for: " 
                             + cursor.getNodeName() + (idNode == null ? "" : "["+idNode.toString()+"]") 
                             + " (path: " + path + ")");
                 }
             }
         }
         pointCursorAt(cursor.getParentNode());
     }
 
     public void writeAttribute(String name, Object value, String property) throws IOException {
         if (null == value) {
             return;
         }
 
         if(EnvUtils.isMyFaces()){
             //Similar to Mojarra, we need to track the ViewState nodes for MyFaces so that
             //we can removed them from the DOM after they have been rendered out to the response.
             if(name != null &&
                     name.equalsIgnoreCase("name") &&
                     value instanceof String &&
                     ((String)value).equalsIgnoreCase("javax.faces.ViewState")){
                 stateNodes.add(cursor);
             }
         }
 
         Attr attribute = document.createAttribute(name.trim());
         attribute.setValue(String.valueOf(value));
         appendToCursor(attribute);
     }
 
     public void writeURIAttribute(String name, Object value, String property) throws IOException {
         String stringValue = String.valueOf(value);
         if (stringValue.startsWith("javascript:")) {
             writeAttribute(name, stringValue, property);
         } else {
             writeAttribute(name, stringValue.replace(' ', '+'), property);
         }
     }
 
     public void writeComment(Object comment) throws IOException {
         String commentString = String.valueOf(comment);
         if (null == document) {
             writer.write(commentString);
             return;
         }
         appendToCursor(document.createComment(commentString));
     }
 
     public void writeText(Object text, String property) throws IOException {
         if (suppressNextNode) {
             //a component is attempting to render outside its subtree
             return;
         }
         if (text == null) {
             throw new NullPointerException("WriteText method cannot write null text");
         }
         String textString = String.valueOf(text);
         if (textString.length() == 0) {
             return;
         }
 
         if (!dontEscape) {
             textString = DOMUtils.escapeAnsi(textString);
         }
         appendToCursor(textString);
     }
 
     public void writeText(char[] text, int off, int len) throws IOException {
         if (suppressNextNode) {
             //a component is attempting to render outside its subtree
             return;
         }
         // escaping done in writeText(object, String) method
         if (len == 0) {
             return;
         }
         writeText(new String(text, off, len), null);
     }
 
     public void writeText(Object text, UIComponent component, String property) throws IOException {
         writeText(text, property);
     }
 
     public void startCDATA() throws IOException {
         if (null == document) {
             document = DOMUtils.getNewDocument();
         }
         pointCursorAt(appendToCursor(document.createCDATASection("")));
     }
 
     public void endCDATA() throws IOException {
         pointCursorAt(cursor.getParentNode());
     }
 
     public ResponseWriter cloneWithWriter(Writer writer) {
         String enc = getCharacterEncoding();
         String type = getContentType();
 
         if (writer instanceof ResponseWriter) {
             wrapped = (ResponseWriter) writer;
             enc = wrapped.getCharacterEncoding();
             type = wrapped.getContentType();
         }
 
         ResponseWriter clone = null;
         if (writer.getClass().getName().endsWith("FastStringWriter")) {
             clone = new BasicResponseWriter(writer, enc, type);
         } else {
             clone = new DOMResponseWriter(writer, enc, type);
         }
         return clone;
     }
 
     private Node appendToCursor(String data) {
 		if (cursor == null) {
 			Element doc = document.getDocumentElement();
 			if( doc != null ){
 				cursor = doc;
 			} else {
 				cursor = document;
 			}
 		}
         if (cursor.getNodeType() == Node.CDATA_SECTION_NODE) {
             CDATASection section = (CDATASection) cursor;
             section.appendData(data);
             return section;
         } else {
             return appendToCursor(document.createTextNode(data));
         }
     }
 
     private Node appendToCursor(Node node) {
         try {
             if (log.isLoggable(Level.FINEST)) {
                 log.finest("appending " + DOMUtils.toDebugString(node) + " into " + DOMUtils.toDebugString(cursor));
             }
 
             if (cursor == null) {
                 Element doc = document.getDocumentElement();
                 if( doc != null ){
                     cursor = doc;
                 } else {
                     cursor = document;
                 }
             }
 
             return cursor.appendChild(node);
 
         } catch (DOMException e) {
             String message = "failed to append " + DOMUtils.toDebugString(node) + " into " + DOMUtils.toDebugString(cursor);
             log.log(Level.SEVERE, message, e);
             throw new RuntimeException(message, e);
         }
     }
 
     private Node appendToCursor(Attr node) {
         try {
             if (log.isLoggable(Level.FINEST)) {
                 log.finest("Appending " + DOMUtils.toDebugString(node) + " into " + DOMUtils.toDebugString(cursor));
             }
             Node result = ((Element) cursor).setAttributeNode(node);
             if ("id".equals(node.getName())) {
                 ((Element) cursor).setIdAttributeNode(node, true);
             }
             return result;
         } catch (DOMException e) {
             String message = "failed to append " + DOMUtils.toDebugString(node) + " into " + DOMUtils.toDebugString(cursor);
             log.log(Level.SEVERE, message, e);
             throw new RuntimeException(message, e);
         } catch (ClassCastException e) {
             String message = "cursor is not an element: " + DOMUtils.toDebugString(cursor);
             log.log(Level.SEVERE, message, e);
             throw new RuntimeException(message, e);
         }
     }
 
     private void pointCursorAt(Node node) {
         if (log.isLoggable(Level.FINEST)) {
             log.finest("moving cursor to " + DOMUtils.toDebugString(node));
         }
         cursor = node;
     }
 
     /**
      * <p>Prepare for rendering into subtrees.</p>
      */
     public void startSubtreeRendering(Document doc) {
         if (doc == null) {
             //This call attempts to get the old DOM from the View map
             //which should have already been attempted but we try
             //it here again to make sure.
             doc = getOldDocument();
             if (doc == null) {
                 refreshDocument();
                 return;
             }
         }
         document = cloneDocument(doc);
     }
 
     private Document cloneDocument(Document doc) {
        Document cloningDoc = DOMUtils.getNewDocument();
        cloningDoc.setStrictErrorChecking(false);
        cloningDoc.importNode(doc.getDocumentElement(), true);
        return cloningDoc;
     }
 
     /**
      * <p>Seek to the subtree specified by the id parameter and clear it
      * to prepare for new rendered output.</p>
      *
      * @param id DOM id of component subtree
      * @return old DOM subtree
      */
     public Node seekSubtree(String id) {
         Node oldSubtree = null;
         //seek to position in document
         cursor = document.getElementById(id);
         if (null == cursor) {
             //If the cursor is null, it means we couldn't find the subtree
             //in the current document.  We may have navigated during the lifecycle.
             if (log.isLoggable(Level.FINE)) {
                 log.log(Level.FINE, "unable to seek to component DOM subtree " + id);
             }
             return null;
         }
         oldSubtree = cursor;
         Node newSubtree = document.createElement(cursor.getNodeName());
         Node cursorParent = cursor.getParentNode();
         //remove subtree for fresh rendering operation
         cursorParent.replaceChild(newSubtree, cursor);
         cursor = newSubtree;
         suppressNextNode = true;
         return oldSubtree;
     }
 
     public Document getDocument() {
         return document;
     }
 
     public void saveOldDocument() throws IOException {
         FacesContext facesContext = FacesContext.getCurrentInstance();
         if (!EnvUtils.isCompressDOM(facesContext)) {
             facesContext.getViewRoot().getViewMap().put(OLD_DOM, document);
             return;
         }
         byte[] data = serializeDocument(document);
         facesContext.getViewRoot().getViewMap().put(OLD_DOM, data);
     }
 
     private static byte[] serializeDocument(Document document) throws IOException {
         byte[] data;
         DOMDocumentSerializer serializer = new DOMDocumentSerializer();
         ByteArrayOutputStream out = new ByteArrayOutputStream(10000);
         serializer.setOutputStream(out);
         serializer.serialize(document);
         data = out.toByteArray();
         return data;
     }
 
     public Document getOldDocument() {
         return DOMResponseWriter.getOldDocument(
                 FacesContext.getCurrentInstance());
     }
 
     public static Document getOldDocument(FacesContext facesContext) {
         Object oldDOMObject = facesContext.getViewRoot()
                 .getViewMap().get(OLD_DOM);
         if (null == oldDOMObject)  {
             return null;
         }
         if (!EnvUtils.isCompressDOM(facesContext)) {
             return (Document) oldDOMObject;
         }
 
         try {
             return deserializeDocument((byte[]) oldDOMObject);
         } catch (Exception e) {
             log.log(Level.SEVERE, "Failed to restore old DOM ", e);
             return DOMUtils.getNewDocument();
         }
     }
 
     private static Document deserializeDocument(byte[] data) throws FastInfosetException, IOException {
         Document document = DOMUtils.getNewDocument();
         //FastInfoset does not tolerate stray xmlns declarations
         document.setStrictErrorChecking(false);
         DOMDocumentParser parser = new DOMDocumentParser();
         ByteArrayInputStream in = new ByteArrayInputStream(data);
         parser.parse(document, in);
         return document;
     }
 
     public Node getCursorParent() {
         return cursor;
     }
 
     public void setCursorParent(Node cursorParent) {
         this.cursor = cursorParent;
     }
 
     //If postback navigation occurs (navigating back to the same
     //page with the same view ID) in the middle of partial rendering,
     //it may be necessary to refresh the document for the DOMResponseWriter
     //so that the subtrees can actually be rendered.
     private void refreshDocument() {
         document = DOMUtils.getNewDocument();
         Element root = document.createElement("html");
         document.appendChild(root);
         cursor = document.getDocumentElement();
     }
 
 
     private boolean isScriptOrStyle(String name) {
         if ("script".equalsIgnoreCase(name)) {
             isScript = true;
             dontEscape = true;
         } else if ("style".equalsIgnoreCase(name)) {
             isStyle = true;
             dontEscape = true;
         } else {
             isScript = false;
             isStyle = false;
             dontEscape = false;
         }
 
         return (isScript || isStyle);
     }
 
     private class WriteViewStateMarkup extends FilterWriter {
         protected WriteViewStateMarkup(Writer out) {
             super(out);
         }
 
         public void write(String str) throws IOException {
             if (EnvUtils.getStateMarker().equals(str) ) {
                 FacesContext fc = FacesContext.getCurrentInstance();
                 out.write("<input id=\"javax.faces.ViewState\" type=\"hidden\" autocomplete=\"off\" value=\"");
                 out.write(fc.getApplication().getStateManager().getViewState(fc));
                 out.write("\" name=\"javax.faces.ViewState\"/>");
             } else {
                 out.write(str);
             }
         }
     }
 
 }

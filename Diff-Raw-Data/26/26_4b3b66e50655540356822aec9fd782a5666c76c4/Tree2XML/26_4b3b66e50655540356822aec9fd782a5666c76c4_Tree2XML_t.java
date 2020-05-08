 /**
  * 
  */
 package org.wyona.wiki;
 
 import java.util.Iterator;
 import java.util.Set;
 
 import org.xml.sax.ContentHandler;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.AttributesImpl;
 
 /**
  * @author greg
  * 
  */
 public class Tree2XML {
 
     /** The URI of the namespace of this generator. */
    static public final String URI = "http://apache.org/cocoon/wiki/1.0";
 
     /** The namespace prefix for this namespace. */
    static public final String PREFIX = "wiki";
 
     /** The content handler */
     ContentHandler contentHandler;
 
     /** Actions which will be grouped together into text() nodes */
     final static String[] textActions = { "Text", "PlainText" };
 
     /** Actions where nested Actions should be ignored */
     final static String[] ignoreNestedActions = { "Link" };
 
     StringBuffer textBuf = null;
 
     protected AttributesImpl attributes = new AttributesImpl();
 
     public Tree2XML(ContentHandler ch) {
         contentHandler = ch;
     }
 
     /**
      * 
      * @param start
      * @return
      */
     public void setContentHandler(ContentHandler ch) {
         contentHandler = ch;
     }
 
     /**
      * Traverses the JJTree and generates SAX-Events
      * 
      * @param root
      * @throws SAXException
      */
     public void traverseJJTree(SimpleNode node) throws SAXException {
         attributes.clear();
         if (!node.optionMap.isEmpty()) {
             Set keySet = node.optionMap.keySet();
             Iterator kit = keySet.iterator();
             while (kit.hasNext()) {
                 Object option = kit.next();
                 Object value = node.optionMap.get(option);
                 attributes.addAttribute("", option.toString(), option.toString(), "CDATA", value.toString());
             }
         }
         contentHandler.startElement(URI, node.toString(), PREFIX + ':' + node.toString(), attributes);
         if (node.jjtGetNumChildren() > 0) {
             for (int i = 0; i < node.jjtGetNumChildren(); i++) {
                 SimpleNode cn = (SimpleNode) node.jjtGetChild(i);
                 if (!ignoreNode(cn)) {
                     if (isTextAction(cn)) {
                         appendText(cn.optionMap.get("value").toString());
                     } else {
                         finalizeText();
                         traverseJJTree(cn);
                     }
                 }
             }
             finalizeText();
         }
         this.contentHandler.endElement(URI, node.toString(), PREFIX + ':' + node.toString());
 
     }
 
     public boolean ignoreNode(Node node) {
         if (node.jjtGetParent() != null) {
             for (int i = 0; i < ignoreNestedActions.length; i++) {
                 if (node.jjtGetParent().toString().compareTo(ignoreNestedActions[i]) == 0) {
                     return true;
                 }
             }
         }
         return false;
     }
 
     public boolean isTextAction(Node node) {
         for (int i = 0; i < textActions.length; i++) {
             if (node.toString().compareTo(textActions[i]) == 0) {
                 return true;
             }
         }
         return false;
     }
 
     private void appendText(String text) {
         if (textBuf == null) {
             textBuf = new StringBuffer();
         }
         textBuf.append(text);
     }
 
     private void resetText() {
         textBuf = null;
     }
 
     private void finalizeText() throws SAXException {
         if (textBuf != null) {
             char[] text = textBuf.toString().toCharArray();
             contentHandler.characters(text, 0, text.length);
             resetText();
         }
     }
    
    public void startDocument() throws SAXException {
        contentHandler.startDocument(); 
        contentHandler.startPrefixMapping(PREFIX, URI);
    }
    
    public void endDocument() throws SAXException {
        contentHandler.endPrefixMapping(PREFIX);
        contentHandler.endDocument(); 
    }
 }

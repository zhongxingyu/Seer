 /*
  * Copyright (c) 2012, Mikael Svensson
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *     * Redistributions of source code must retain the above copyright
  *       notice, this list of conditions and the following disclaimer.
  *     * Redistributions in binary form must reproduce the above copyright
  *       notice, this list of conditions and the following disclaimer in the
  *       documentation and/or other materials provided with the distribution.
  *     * Neither the name of the copyright holder nor the names of the
  *       contributors of this software may be used to endorse or promote products
  *       derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL MIKAEL SVENSSON BE LIABLE FOR ANY
  * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package info.mikaelsvensson.devtools.doclet.shared;
 
 import com.sun.javadoc.Doc;
 import com.sun.javadoc.Tag;
import info.mikaelsvensson.devtools.doclet.shared.commenttext.*;
 import info.mikaelsvensson.devtools.doclet.xml.extensivedocumentcreator.JavadocItemHandlerException;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import java.io.IOException;
 import java.io.StringReader;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.regex.Pattern;
 
 public class ElementWrapper {
     private InlineTagHandler[] inlineTagHandlers = new InlineTagHandler[]{
             new CodeInlineTagHandler(),
             new ImageInlineTagHandler(),
             new LinkInlineTagHandler(),
             new SourceFileInlineTagHandler(),
             new GenericInlineTagHandler()};
     public static final Pattern COMMENT_PARAGRAPH = Pattern.compile("<(p|br)\\s*/\\s*>");
 
     protected Element el;
     private static DocumentBuilder docBuilder;
 
     static {
         try {
             docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
         } catch (ParserConfigurationException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
     }
 
     public ElementWrapper(final Element el) {
         this.el = el;
     }
 
     public ElementWrapper setAttribute(String name, String value) {
         el.setAttribute(name, value);
         return this;
     }
 
     public ElementWrapper createElement(String name) {
         return new ElementWrapper(el.getOwnerDocument().createElement(name));
     }
 
     public ElementWrapper addChild(String name, String... attributeValuePairs) {
         return addChildWithText(name, null, attributeValuePairs);
     }
 
     public ElementWrapper addChildFromSource(String source) throws IOException, SAXException {
         String xmlSource = "<?xml version=\"1.0\"?>" + source;
         Document tempDoc = docBuilder.parse(new InputSource(new StringReader(xmlSource)));
         Element element = (Element) el.getOwnerDocument().importNode(tempDoc.getDocumentElement(), true);
         el.appendChild(element);
         return new ElementWrapper(element);
     }
 
     public ElementWrapper addCommentChild(Doc doc) throws JavadocItemHandlerException {
         return addCommentChild(doc.inlineTags());
     }
 
     public ElementWrapper addCommentChild(Tag doc) throws JavadocItemHandlerException {
         return addCommentChild(doc.inlineTags());
     }
 
     private ElementWrapper addCommentChild(Tag[] inlineTags) throws JavadocItemHandlerException /*throws InlineTagHandlerException */{
         StringBuilder sb = new StringBuilder();
 
         tag:
         for (Tag tag : inlineTags) {
             for (InlineTagHandler handler : inlineTagHandlers) {
                 if (handler.handles(tag)) {
                     try {
                         sb.append(handler.toString(tag));
                     } catch (InlineTagHandlerException e) {
                         throw new JavadocItemHandlerException("Could not parse/process Javadoc tag " +
                                 tag.name() +
                                 " in " + tag.position().file().getName() +
                                 " at line " + tag.position().line() + ". " +
                                 "Error message: " + e.getMessage(), e);
 
                     }
                     continue tag;
                 }
             }
             sb.append(tag.text());
         }
         return addCommentChild(sb.toString());
     }
 
     private ElementWrapper addCommentChild(String comment) {
         String c = comment != null && comment.length() > 0 ? comment/*.replace("\n", "")*/ : "";
         if (c.length() > 0) {
             try {
                 StringBuilder sb = new StringBuilder();
                 sb.append("<comment format=\"html\">");
                 // TODO: MISV 20120621 It would be nice if this method could also fix the cause of the '"p" must be terminated by the matching end-tag' issue. The problem stems from invalid HTML code genereated by wsimport/wsgen (?). Solve by counting the number of '</p' between one '<p' and the next (count=0 => prepend '</p>' before the next '<p')?
                 for (String paragraph : COMMENT_PARAGRAPH.split(c)) {
                     sb.append("<p>").append(paragraph).append("</p>");
                 }
                 sb.append("</comment>");
                 addChildFromSource(sb.toString());
             } catch (IOException e) {
                 addChildWithText("comment", comment, "format", "text");
             } catch (SAXException e) {
                 addChildWithText("comment", comment, "format", "text");
             }
         }
         return this;
     }
 
     public ElementWrapper setText(final String text) {
         el.appendChild(el.getOwnerDocument().createTextNode(text));
         return this;
     }
 
     public ElementWrapper addChildWithText(String name, String text, String... attributeValuePairs) {
         Element child = el.getOwnerDocument().createElement(name);
 
 
         if (null != text && text.length() > 0) {
             child.appendChild(el.getOwnerDocument().createTextNode(text));
         }
         el.appendChild(child);
         ElementWrapper wrapper = new ElementWrapper(child);
 
         wrapper.setAttributes(attributeValuePairs);
 
         return wrapper;
     }
 
     public void setAttributes(final String... attributeValuePairs) {
         for (int i = 1; i < attributeValuePairs.length; i += 2) {
             String attrName = attributeValuePairs[i - 1];
             String attrValue = attributeValuePairs[i];
 
             setAttribute(attrName, attrValue);
         }
     }
 
     public String getTagName() {
         return el.getTagName();
     }
 
     public String getAttribute(String name) {
         return el.getAttribute(name);
     }
 
     public void removeAttributes(final String... attributesNames) {
         for (String name : attributesNames) {
             el.removeAttribute(name);
         }
     }
 
     public void remove() {
         if (el.getParentNode() != null) {
             el.getParentNode().removeChild(el);
         }
     }
 
     public ElementWrapper getParent() {
         Node parentNode = el.getParentNode();
         if (null != parentNode && parentNode.getNodeType() == Node.ELEMENT_NODE) {
             return new ElementWrapper((Element) parentNode);
         }
         return null;
     }
 
     public ElementWrapper[] getChildren(final String name) {
         List<ElementWrapper> children = new LinkedList<ElementWrapper>();
         for (int i = 0; i < el.getChildNodes().getLength(); i++) {
             Node node = el.getChildNodes().item(i);
             if (node.getNodeType() == Node.ELEMENT_NODE) {
                 children.add(new ElementWrapper((Element) node));
             }
         }
         return children.toArray(new ElementWrapper[children.size()]);
     }
 }

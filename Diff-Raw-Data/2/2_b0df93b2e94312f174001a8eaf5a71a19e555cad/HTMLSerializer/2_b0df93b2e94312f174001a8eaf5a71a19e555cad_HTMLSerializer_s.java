 /*
  * Copyright (c) 2005, Mikael Stldal
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without 
  * modification, are permitted provided that the following conditions 
  * are met:
  * 
  * 1. Redistributions of source code must retain the above copyright 
  * notice, this list of conditions and the following disclaimer.
  * 
  * 2. Redistributions in binary form must reproduce the above copyright 
  * notice, this list of conditions and the following disclaimer in the 
  * documentation and/or other materials provided with the distribution.
  * 
  * 3. Neither the name of the author nor the names of its contributors 
  * may be used to endorse or promote products derived from this software 
  * without specific prior written permission. 
  * 
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
  * ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT 
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR 
  * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR 
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
  * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY 
  * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  * 
  * 
  * Note: This is known as "the modified BSD license". It's an approved 
  * Open Source and Free Software license, see 
  * http://www.opensource.org/licenses/ 
  * and
  * http://www.gnu.org/philosophy/license-list.html
  */
 
 package nu.staldal.xodus;
 
 import java.util.*;
 import java.io.*;
 import java.net.URL;
 import java.net.URLConnection;
 
 import org.xml.sax.*;
 import org.xml.sax.ext.LexicalHandler;
 import org.xml.sax.ext.DeclHandler;
 import org.xml.sax.helpers.NamespaceSupport;
 
 import javax.xml.transform.Result;
 import javax.xml.transform.stream.StreamResult;
 
 
 public class HTMLSerializer extends Serializer
 {
     private boolean disableOutputEscaping = false;
     private boolean emptyElement = false;
     private int nestedCDATA = 0;
     private boolean inDTD = false;
     private boolean dtdContent = false;
     private boolean addDTD = true;
        
     private int elementDepth = 0;
     private boolean wasEndTag = false;
     private boolean wasStartTag = false;
     private int inFormattedElement = 0;
     private int inNotEscapeElement = 0;
     
     private final Set emptyElements;
     private final Set formattedElements;
     private final Set notEscapeElements;
     private final Set booleanAttributes;
     
     
     HTMLSerializer(StreamResult result, OutputConfig outputConfig)
         throws IllegalArgumentException, IOException, 
             UnsupportedEncodingException
     {
         super(result, outputConfig);
                 
         emptyElements = new HashSet(13);
         emptyElements.add("area");
         emptyElements.add("base");
         emptyElements.add("basefont");
         emptyElements.add("br");
         emptyElements.add("col");
         emptyElements.add("hr");
         emptyElements.add("img");
         emptyElements.add("input");
         emptyElements.add("link");
         emptyElements.add("meta");
         emptyElements.add("frame");
         emptyElements.add("isindex");
         emptyElements.add("param");
 
         formattedElements = new HashSet(4);
         formattedElements.add("pre");
         formattedElements.add("script");
         formattedElements.add("style");
         formattedElements.add("textarea");
 
         notEscapeElements = new HashSet(2);
         notEscapeElements.add("script");
         notEscapeElements.add("style");
         
         booleanAttributes = new HashSet(13);        
         booleanAttributes.add("selected");
         booleanAttributes.add("disabled");
         booleanAttributes.add("readonly");       
         booleanAttributes.add("checked");        
         booleanAttributes.add("nohref");
         booleanAttributes.add("ismap");        
         booleanAttributes.add("declare");
         booleanAttributes.add("noshade");
         booleanAttributes.add("compact");        
         booleanAttributes.add("multiple");
         booleanAttributes.add("nowrap");
         booleanAttributes.add("noresize");        
         booleanAttributes.add("defer");
     }
 
     
     // ContentHandler implementation
     
     /**
      * Does nothing.
      */
     public void setDocumentLocator(Locator locator)
     {
         // nothing to do    
     }
 
     
     public void startDocument()
 	    throws SAXException
     {
         wasEndTag = false;
         wasStartTag = false;        
     }
 
     
     public void startPrefixMapping(String prefix, String uri)
 	    throws SAXException
     {
         // nothing to do
     }
 
 
     public void endPrefixMapping(String prefix)
 	    throws SAXException
     {
         // nothing to do
     }
 
 
     private void writeAttribute(String attQName, String attValue)
         throws IOException
     {
         if (attQName.equals("xmlns") || attQName.startsWith("xmlns:"))
         {
             // do not output XML namespace declarations
             return;
         }
         
         out.write(' ');
         out.write(attQName);
         
         if (booleanAttributes.contains(attQName.toLowerCase())
                 && attValue.equalsIgnoreCase(attQName))
         {
             // boolean attribute    
         }
         else
         {        
             out.write("=\"");
             
             if (disableOutputEscaping)
             {
                 out.write(attValue);
             }
             else
             {
                 out.enableEscaping();
                 for (int i = 0; i<attValue.length(); i++)
                 {
                     char c = attValue.charAt(i);
                     switch (c)
                     {
                     case '&':
                         if (i<attValue.length() && attValue.charAt(i+1)=='{')
                         {
                             // do not escape & immediately before { in HTML
                             out.write('&');    
                         }
                         else
                         {
                             out.write("&amp;");
                         }
                         break;
     
                     case '\"':
                         out.write("&quot;");
                         break;
                     
                     default:
                         out.write(c);
                     }
                 }
                 out.disableEscaping();
             }
             
             out.write('\"');
         }
     }
     
     
     public void startElement(String namespaceURI, String localName,
 			      String qName, Attributes atts)
 	    throws SAXException
     {
         fixTag();
 
         if (localName == null || localName.length() == 0)
         {
             localName = qName;
         }        
         int colon = localName.indexOf(':');
         if (colon > -1)
         {
             localName = localName.substring(colon+1);
         }
                 
         try {
             if (addDTD)
             {
                 if (outputConfig.doctype_system != null)
                 {
                     out.write("<!DOCTYPE ");
                     out.write(localName);
                     out.write(' ');
                     writeExternalId(outputConfig.doctype_public,
                         outputConfig.doctype_system);
                     out.write('>');
                     if (outputConfig.indent)
                     {                
                         newline();
                     }
                 }  
                 addDTD = false;
             }
             
             if (outputConfig.indent && (wasStartTag || wasEndTag) && inFormattedElement == 0)
             {
                 if (elementDepth > 0)
                 {
                     newline();
                     for (int i = 0; i<elementDepth*2; i++)
                     {
                         out.write(' ');
                     }
                 }
             }
             out.write('<');
             out.write(localName);
 
             for (int j = 0; j<atts.getLength(); j++)
             {
                 String attQName = atts.getQName(j);
                 if (attQName == null || attQName.length() == 0)
                 {
                     attQName = atts.getLocalName(j);
                 }
                 writeAttribute(attQName, atts.getValue(j));                
             }            
         }
         catch (IOException e)
         {
             throw new SAXException(e);    
         }            
 
         if (formattedElements.contains(localName.toLowerCase()))
             inFormattedElement++;
         
         if (notEscapeElements.contains(localName.toLowerCase()))
             inNotEscapeElement++;
         
         elementDepth++; 
         emptyElement = true;
         wasStartTag = true;        
     }
 
 
     private void fixTag()
 	    throws SAXException
     {
         try {
             if (emptyElement)
             {
                 out.write('>');    
                 emptyElement = false;
             }
         }
         catch (IOException e)
         {
             throw new SAXException(e);    
         }        
     }
     
 
     public void endElement(String namespaceURI, String localName, String qName)
 	    throws SAXException
     {        
         elementDepth--;
                 
         if (localName == null || localName.length() == 0)
         {
             localName = qName;
        }        
         int colon = localName.indexOf(':');
         if (colon > -1)
         {
             localName = localName.substring(colon+1);
         }
                 
         try {
             if (emptyElement && emptyElements.contains(localName.toLowerCase())) 
             {
                 out.write('>');
                 emptyElement = false;
             }
             else
             {
                 if (emptyElement)
                 {
                     out.write('>');
                 }
                 
                 if (outputConfig.indent && wasEndTag && inFormattedElement == 0)
                 {
                     newline();
                     for (int i = 0; i<elementDepth*2; i++)
                     {
                         out.write(' ');
                     }
                 }
                 out.write("</");
                 out.write(localName);
                 out.write('>');
             }
             emptyElement = false;
         }
         catch (IOException e)
         {
             throw new SAXException(e);    
         }
 
         if (formattedElements.contains(localName.toLowerCase()))
             inFormattedElement--;
         
         if (notEscapeElements.contains(localName.toLowerCase()))
             inNotEscapeElement++;        
         
         wasEndTag = true;
     }
 
 
     public void characters(char ch[], int start, int length)
 	    throws SAXException
     {
         wasEndTag = false;
         wasStartTag = false;
 
         fixTag();
         
         try {
             if (disableOutputEscaping || nestedCDATA > 0
                     || inNotEscapeElement > 0)
             {
                 out.write(ch, start, length);
             }
             else
             {
                 out.enableEscaping();
                 for (int i = start; i<start+length; i++)
                 {
                     char c = ch[i];
                     switch (c)
                     {
                     case '<':
                         out.write("&lt;");
                         break;
 
                     case '>':
                         out.write("&gt;");
                         break;
                     
                     case '&':
                         out.write("&amp;");
                         break;
                     
                     default:
                         out.write(c);
                     }
                 }
                 out.disableEscaping();
             }
         }
         catch (IOException e)
         {
             throw new SAXException(e);    
         }
     }
 
 
     public void ignorableWhitespace(char ch[], int start, int length)
 	    throws SAXException
     {
         characters(ch, start, length);
     }
 
 
     /**
      * Will honor 
      * {@link javax.xml.transform.Result#PI_DISABLE_OUTPUT_ESCAPING} 
      * and 
      * {@link javax.xml.transform.Result#PI_ENABLE_OUTPUT_ESCAPING}. 
      */
     public void processingInstruction(String target, String data)
 	    throws SAXException
     {
         if (target == null || target.length() == 0)
             throw new NullPointerException("No PI target");
         
         if (target.equals(Result.PI_DISABLE_OUTPUT_ESCAPING))
         {
             disableOutputEscaping = true;    
         }
         else if (target.equals(Result.PI_ENABLE_OUTPUT_ESCAPING)) 
         {
             disableOutputEscaping = false;    
         }
         else
         {
             fixDTD();        
             fixTag();
                     
             try {
                 out.write("<?");
                 out.write(target);
                 if (data != null && data.length() > 0)
                 {
                     out.write(' ');
                     out.write(data);
                 }
                 out.write('>');
             }
             catch (IOException e)
             {
                 throw new SAXException(e);    
             }            
         }
     }
 
     
     public void skippedEntity(String name)
 	    throws SAXException
     {
         wasEndTag = false;
         wasStartTag = false;
 
         fixTag();
         
         try {
             if (name.charAt(0) == '%')
             {
                 out.write(name);
                 out.write(';');
             }
             else if (name.equals("[dtd]"))
             {
                 // nothing to do    
             }
             else
             {
                 out.write('&');
                 out.write(name);
                 out.write(';');
             }
         }
         catch (IOException e)
         {
             throw new SAXException(e);    
         }            
     }
     
 
     public void endDocument()
 	    throws SAXException
     {
         fixTag();
         
         try {
             if (outputConfig.indent)
             {                
                 newline();
             }
             finishOutput();
         }
         catch (IOException e)
         {
             throw new SAXException(e);    
         }
     }
     
     
     // LexicalHandler implementation
 
     private void writeExternalId(String publicId, String systemId)
         throws IOException
     {
         if (publicId != null)
         {
             out.write("PUBLIC \"");
             out.write(publicId);
             out.write('\"');
             if (systemId != null)
             {
                 out.write(" \"");
                 out.write(systemId);
                 out.write('\"');
             }
         }
         else
         {
             out.write("SYSTEM \"");
             out.write(systemId);
             out.write('\"');                    
         }        
     }
        
 
     public void startDTD(String name, String publicId, String systemId)
 	    throws SAXException
     {
         try {
             out.write("<!DOCTYPE ");
             out.write(name);
             
             if (outputConfig.doctype_system != null)
                 systemId = outputConfig.doctype_system;
             if (outputConfig.doctype_public != null)
                 publicId = outputConfig.doctype_public;
             
             if (systemId != null)
             {
                 out.write(' ');
                 writeExternalId(publicId, systemId);
             }
             inDTD = true;
             addDTD = false;
         }
         catch (IOException e)
         {
             throw new SAXException(e);    
         }        
     }
 
 
     private void fixDTD()
         throws SAXException
     {
         if (inDTD)
         {
             if (!dtdContent)
             {
                 try {                    
                     out.write(" [");
                     dtdContent = true;
                 }
                 catch (IOException e)
                 {
                     throw new SAXException(e);    
                 }                            
             }
         }
     }
 
 
     public void endDTD()
 	    throws SAXException
     {
         inDTD = false;
         
         try {
             if (dtdContent)
             {
                 out.write(']');                
             }
             out.write('>');
             if (outputConfig.indent)
             {                
                 newline();
             }
         }
         catch (IOException e)
         {
             throw new SAXException(e);    
         }        
     }
     
     
     /**
      * Does nothing.
      */
     public void startEntity(String name)
 	    throws SAXException
     {
         // nothing to do
     }
 
 
     /**
      * Does nothing.
      */
     public void endEntity(String name)
 	    throws SAXException
     {
         // nothing to do
     }
 
 
     public void startCDATA()
 	    throws SAXException
     {
         fixTag();
         
         try {
             if (nestedCDATA == 0)
             {
                 out.write("<![CDATA[");
             }
             nestedCDATA++;
         }
         catch (IOException e)
         {
             throw new SAXException(e);    
         }        
     }
 
 
     public void endCDATA()
 	    throws SAXException
     {        
         try {
             if (nestedCDATA > 0)
             {
                 nestedCDATA--;
                 if (nestedCDATA == 0)
                 {
                     out.write("]]>");
                 }
             }
             else
             {
                 throw new SAXException("endCDATA without startCDATA");    
             }
         }
         catch (IOException e)
         {
             throw new SAXException(e);    
         }
     }
 
 
     public void comment(char ch[], int start, int length)
 	    throws SAXException
     {
         fixDTD();        
         fixTag();
         
         try {
             out.write("<!-- ");
             out.write(ch, start, length);            
             out.write(" -->");
         }
         catch (IOException e)
         {
             throw new SAXException(e);    
         }        
     }
     
 
     // DTDHandler implementation
 
     public void notationDecl(String name, String publicId, String systemId)
 	    throws SAXException
     {
         fixDTD();
 
         try {
             out.write("<!NOTATION ");
             out.write(name);
             out.write(' ');
             if (publicId != null && systemId != null)
             {
                 out.write("PUBLIC \"");
                 out.write(publicId);
                 out.write("\" \"");
                 out.write(systemId);
                 out.write('\"');                    
             }
             else if (systemId != null)
             {
                 out.write("SYSTEM \"");
                 out.write(systemId);
                 out.write('\"');                    
             }
             else if (publicId != null)
             {
                 out.write("PUBLIC \"");
                 out.write(publicId);
                 out.write('\"');                    
             }
             else
             {
                 throw new SAXException("notationDecl without publicId or systemId");    
             }
             out.write('>');
         }
         catch (IOException e)
         {
             throw new SAXException(e);    
         }                
     }
     
     
     public void unparsedEntityDecl(String name, String publicId,
 					               String systemId, String notationName)
         throws SAXException
     {        
         fixDTD();
 
         try {
             out.write("<!ENTITY ");
             out.write(name);
             out.write(' ');
             writeExternalId(publicId, systemId);
             out.write(" NDATA ");
             out.write(notationName);
             out.write('>');
         }
         catch (IOException e)
         {
             throw new SAXException(e);    
         }                                
     }
     
 
     // DeclHandler implementation
       
     public void elementDecl(String name, String model)
 	    throws SAXException
     {        
         fixDTD();
 
         try {
             out.write("<!ELEMENT ");
             out.write(name);
             out.write(' ');
             out.write(model);
             out.write('>');
         }
         catch (IOException e)
         {
             throw new SAXException(e);    
         }                                
     }
 
 
     public void attributeDecl(String eName, String aName,
 					          String type, String mode, String value)
         throws SAXException
     {        
         fixDTD();
 
         try {
             out.write("<!ATTLIST ");
             out.write(eName);
             out.write(' ');
             out.write(aName);
             out.write(' ');
             out.write(type);
             if (mode != null)
             {
                 out.write(' ');
                 out.write(mode);
             }
             if (value != null)
             {
                 out.write(" \"");
                 out.write(value);
                 out.write('\"');
             }                            
             out.write('>');
         }
         catch (IOException e)
         {
             throw new SAXException(e);    
         }                                        
     }
 
 
     public void internalEntityDecl(String name, String value)
 	    throws SAXException
     {        
         fixDTD();        
 
         if (name.charAt(0) == '%')
         {
             name = "% " + name.substring(1);    
         }
         
         try {
             out.write("<!ENTITY ");
             out.write(name);
             out.write(" \"");
             out.write(value);
             out.write("\">");
         }
         catch (IOException e)
         {
             throw new SAXException(e);    
         }                                
     }
 
 
     public void externalEntityDecl(String name, String publicId,
 					               String systemId)
         throws SAXException
     {        
         fixDTD();        
 
         if (name.charAt(0) == '%')
         {
             name = "% " + name.substring(1);    
         }
         
         try {
             out.write("<!ENTITY ");
             out.write(name);
             out.write(' ');
             writeExternalId(publicId, systemId);
             out.write('>');
         }
         catch (IOException e)
         {
             throw new SAXException(e);    
         }                                
     }
     
 }
 

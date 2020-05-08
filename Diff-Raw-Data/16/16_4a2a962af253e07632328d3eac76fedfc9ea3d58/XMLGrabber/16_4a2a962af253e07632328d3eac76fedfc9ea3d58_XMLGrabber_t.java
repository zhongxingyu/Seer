 /* 
  * Returns the XML text verbatim, doesn't encode it as swf
  */
 
 
 /* *****************************************************************************
  * XMLConverter.java
 * ****************************************************************************/
 
 /* J_LZ_COPYRIGHT_BEGIN *******************************************************
* Copyright 2001-2008 Laszlo Systems, Inc.  All Rights Reserved.              *
 * Use is subject to license terms.                                            *
 * J_LZ_COPYRIGHT_END *********************************************************/
 
 package org.openlaszlo.data;
 
 import java.io.*;
 import java.util.Properties;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.log4j.*;
 
 import org.openlaszlo.utils.ContentEncoding;
 import org.openlaszlo.utils.LZHttpUtils;
 
 import org.openlaszlo.media.MimeType;
 import org.openlaszlo.server.LPS;
 
 import org.jdom.Attribute;
 import org.jdom.Comment;
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.JDOMException;
 import org.jdom.input.SAXBuilder;
 import org.jdom.output.XMLOutputter;
 
 import org.apache.commons.httpclient.*;
 import org.apache.commons.httpclient.methods.*;
 import org.apache.commons.httpclient.util.*;
 
 import org.xmlpull.v1.*;
 
 
 
 /**
  * XML Converter
  *
  */
 public class XMLGrabber extends Converter {
     
     private static Logger mLogger  = Logger.getLogger(XMLGrabber.class);
     private static XmlPullParserFactory factory = null;
 
     private static XmlPullParserFactory getXPPFactory () {
         if (factory == null) {
             // Set up the XML Parser factory
             try {
                 String sys = null; 
                 try {
                     sys = System.getProperty(XmlPullParserFactory.PROPERTY_NAME);
                 } catch (SecurityException se) {
                 }
                 factory = XmlPullParserFactory.newInstance(sys, null);
                 factory.setNamespaceAware(false);
                 factory.setValidating(false);
             } catch (XmlPullParserException e) {
                 throw new RuntimeException(e.getMessage());
             } 
         }
         return factory;
     }
 
     /**
      * Convert incoming XML to ... XML 
      *
      * This method is called convertToSWF for historical reasons, and nobody
      * has changed the API call name yet. 
      *
      * A dataset will look like this:
      * <resultset>
      * <body>
      *   <weather sourceurl="http://www.srh.noaa.gov/zipcity.php?inputstring=02460">
      *    <radar src="http://www.laszlosystems.com:80/weather/small/kbox.jpg"/>
      *    <satellite src="http://www.laszlosystems.com:80/weather/thumbs/ECI8.JPG"/>
      *   </weather>
      * </body>
      * <headers>
      * <header name="Date" value="Thu, 29 Dec 2005 03:49:46 GMT"/>
      * <header name="Server" value="Apache/2.0.44 (Unix) mod_ssl/2.0.44 OpenSSL/0.9.6b DAV/2 mod_jk/1.2.1 PHP/4.3.0"/>
      * </headers>
      * </resultset>
      */
 
     public InputStream convertToSWF(Data data, HttpServletRequest req,
                                     HttpServletResponse res)
        throws ConversionException, IOException {
 
         try {
             PipedOutputStream pout = new PipedOutputStream();
             PipedInputStream in = new PipedInputStream(pout);
 
             XmlSerializer serializer;
             XmlPullParser parser;
             parser = getXPPFactory().newPullParser();
             InputStream dstream = data.getInputStream();
             parser.setInput( dstream, null );
             serializer = factory.newSerializer();
             serializer.setOutput(pout , "UTF-8");
 
             HttpMethodBase request = ((HttpData) data).getRequest();
 
             final String sendheaders = req.getParameter("sendheaders");
 
             XMLCopyThread worker = new XMLCopyThread(pout, parser,serializer, request, sendheaders);
             worker.start();
 
             return in;
 
         } catch (XmlPullParserException ex) {
             throw new ConversionException("Parsing XML: " + ex.getMessage());
         }
     }
 
     // Worker thread to parse XML (which serves to translate obscure
     // charsets to UTF-8) and wrap it in <resultset>, possibly adding
     // proxied HTTP headers from backedn response.
     // This is written to the PipedOutputStream which we were passed.
     class XMLCopyThread extends Thread implements Runnable {
         OutputStream pout = null;    
         XmlPullParser parser;
         XmlSerializer serializer;
         HttpMethodBase request;
         String sendheaders = null;
         
         XMLCopyThread( OutputStream pout, XmlPullParser parser, XmlSerializer serializer,
                        HttpMethodBase request,
                        String sendheaders) {
             this.pout = pout;
             this.parser = parser;
             this.serializer = serializer;
             this.request = request;
             this.sendheaders = sendheaders;
         }
 
         public void run() {
             try {
                 writeXMLDataToOutputStream();
            } catch (Exception e) {
            } finally {
                try {
                    pout.flush();
                    pout.close();
                } catch (Exception e) {
                }
             }
         }
 
         // Generates an XML document with this structure
         // <resultset>
         //   <body> [PROXIED_XMLDOC] </body>
         //   <headers> ... </headers>
         // </resultset>
         void writeXMLDataToOutputStream() throws XmlPullParserException, IOException {
             //Run through XML PULL parser, to convert to UTF8, and
             // wrap in <resultset> tag, plus optional headers 
 
             // Start a standalone document;
             //serializer.startDocument("UTF-8", Boolean.TRUE);
 
             serializer.startTag("", "resultset");
             serializer.startTag("", "body");
 
             parser.nextToken(); // read first token
 
             while (parser.getEventType () != XmlPullParser.END_DOCUMENT) {
                 writeToken ( parser.getEventType () );
                 parser.nextToken ();
             }
 
             serializer.endTag("", "body");
 
             //   <headers> ... </headers>
             serializer.startTag("", "headers");
 
             // Get headers
             if (sendheaders == null || sendheaders.equals("true") ) {
                 Header[] hedz = request.getResponseHeaders();
                 for (int i = 0; i < hedz.length; i++) {
                     String name = hedz[i].getName();
                     if (LZHttpUtils.allowForward(name, null)) {
                         serializer.startTag("", "header");                    
 
                         serializer.attribute (null, "name", name);
                         serializer.attribute (null, "value", hedz[i].getValue());
                         serializer.endTag("", "header");                    
                     }
                 }
             }
             serializer.endTag("", "headers");
 
             serializer.endTag("", "resultset");
             serializer.endDocument();
         }
 
         private void writeStartTag ()
           throws XmlPullParserException, IOException {
             if (!parser.getFeature (XmlPullParser.FEATURE_REPORT_NAMESPACE_ATTRIBUTES)) {
                 for (int i = parser.getNamespaceCount (parser.getDepth ()-1);
                      i <= parser.getNamespaceCount (parser.getDepth ())-1; i++) {
                     serializer.setPrefix
                         (parser.getNamespacePrefix (i),
                          parser.getNamespaceUri (i));
                 }
             }
             serializer.startTag(parser.getNamespace (), parser.getName ());
         
             for (int i = 0; i < parser.getAttributeCount (); i++) {
                 serializer.attribute
                     (parser.getAttributeNamespace (i),
                      parser.getAttributeName (i),
                      parser.getAttributeValue (i));
             }
         }
 
 
         private void writeToken (int eventType)
           throws XmlPullParserException, IOException {
             switch (eventType) {
                 
               case XmlPullParser.START_TAG:
                 writeStartTag ();
                 break;
                 
               case XmlPullParser.END_TAG:
                 serializer.endTag(parser.getNamespace (), parser.getName ());
                 break;
                 
             case XmlPullParser.START_DOCUMENT:
                 //use Boolean.TRUE to make it standalone
               //Boolean standalone = (Boolean) parser.getProperty(PROPERTY_XMLDECL_STANDALONE);
               //serializer.startDocument(parser.getInputEncoding(), standalone);
                 break;
 
               case XmlPullParser.END_DOCUMENT:
               //serializer.endDocument();
                 break;
 
               case XmlPullParser.IGNORABLE_WHITESPACE:
                 //comment it to remove ignorable whtespaces from XML infoset
                 String s = parser.getText ();
                 serializer.ignorableWhitespace (s);
                 break;
                 
               case XmlPullParser.TEXT:
                 serializer.text (parser.getText ());
                 break;
                 
               case XmlPullParser.ENTITY_REF:
                 serializer.entityRef (parser.getName ());
                 break;
                 
               case XmlPullParser.CDSECT:
                 serializer.cdsect( parser.getText () );
                 break;
                 
               case XmlPullParser.PROCESSING_INSTRUCTION:
                 // serializer.processingInstruction( parser.getText ());
                 break;
                 
               case XmlPullParser.COMMENT:
                 //serializer.comment (parser.getText ());
                 break;
                 
               case XmlPullParser.DOCDECL:
                 // serializer.docdecl (parser.getText ());
                 break;
             }
         }
 
 
     }
 
     /**
      * @return the encoding that should be used when responding
      * to this request or null for no encoding.  For now, the only
      * acceptable values besides null are "gzip" and "deflate".
      */
     public String chooseEncoding(HttpServletRequest req) {
 
         String e = req.getParameter("enc");
         if (e == null || e.equals("false")) {
             return null;
         }
 
         String enc = ContentEncoding.chooseEncoding(req);
         mLogger.debug("Encoding: " + enc);
         return enc;
     }
 
 }

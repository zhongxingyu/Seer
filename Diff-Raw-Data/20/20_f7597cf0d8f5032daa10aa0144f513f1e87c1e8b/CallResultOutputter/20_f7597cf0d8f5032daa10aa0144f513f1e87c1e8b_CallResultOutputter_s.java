 /*
  * $Id$
  */
 package org.xins.server;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Iterator;
 import java.util.List;
 import org.xins.util.collections.PropertyReader;
 import org.znerd.xmlenc.XMLOutputter;
 
 /**
  * Transformer that is able to externalize a <code>CallResult</code> object to
  * XML.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:znerd@FreeBSD.org">znerd@FreeBSD.org</a>)
  *
  * @since XINS 0.119
  */
 final class CallResultOutputter extends Object {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    /**
     * Generates XML for the specified call result. The XML is sent to the
     * specified output stream.
     *
     * @param out
     *    the output stream to send the XML to, cannot be <code>null</code>.
     *
     * @param result
     *    the call result to convert to XML, cannot be <code>null</code>.
     *
     * @param xslt
     *    the URL of the XSLT to link to, can be <code>null</code>.
     */
    public static void output(PrintWriter out, CallResult result, String xslt)
    throws IOException {
 
       XMLOutputter outputter = new XMLOutputter(out, "US-ASCII");
 
       outputter.declaration();
 
       if (xslt != null) {
          outputter.pi("xml-stylesheet", "type=\"text/xsl\" href=\"" + xslt + "\"");
       }
 
       // Write the result start tag
       outputter.startTag("result");
       outputter.attribute("success", result.isSuccess() ? "true" : "false");
 
       String code = result.getCode();
       if (code != null) {
          outputter.attribute("code", code);
       }
          
       // Write the output parameters
       PropertyReader params = result.getParameters();
       if (params != null) {
          Iterator names = params.getNames();
          while (names.hasNext()) {
             String name  = (String) names.next();
             String value = params.get(name);
 
             outputter.startTag("param");
             outputter.attribute("name", name);
             outputter.pcdata(value);
             outputter.endTag(); // param
          }
       }
 
       // Write the data element
       Element dataElement = result.getDataElement();
       if (dataElement != null) {
          output(outputter, dataElement);
       }
 
       outputter.endTag(); // result
    }
 
    /**
     * Generates XML for the specified element.
     *
     * @param outputter
     *    the XML outputter to use, cannot be <code>null</code>.
     *
     * @param element
     *    the {@link Element} object to convert to XML, cannot be <code>null</code>.
     *
     * @throws NullPointerException
     *    if <code>outputter == null || element == null</code>.
     *
     * @throws IOException
     *    if there is an I/O error.
     */
    private static final void output(XMLOutputter outputter, Element element)
    throws NullPointerException, IOException {
 
       // Start the tag
       outputter.startTag(element.getType());
 
       // Write the attributes
       PropertyReader attributes = element.getAttributes();
       Iterator names = attributes.getNames();
       while (names.hasNext()) {
          String name  = (String) names.next();
          String value = attributes.get(name);
          outputter.attribute(name, value);
       }
 
       // Process all contained elements and PCDATA sections
       List content = element.getContent();
       int count = content == null ? 0 : content.size();
       for (int i = 0; i < count; i++) {
          Object o = content.get(i);
          if (o instanceof Element) {
             output(outputter, (Element) o);
          } else {
             outputter.pcdata((String) o);
          }
       }
 
       // End the tag
       outputter.endTag();
    }
 
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>CallResultOutputter</code> object.
     */
    private CallResultOutputter() {
       // empty
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 }

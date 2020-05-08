 /*
  * $Id$
  */
 package org.xins.server;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Iterator;
 import java.util.List;
 
 import org.xins.common.MandatoryArgumentChecker;
 import org.xins.common.collections.PropertyReader;
 
 import org.znerd.xmlenc.XMLOutputter;
 
 /**
  * Transformer that is able to externalize a <code>CallResult</code> object to
  * XML.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:ernst.dehaan@nl.wanadoo.com">ernst.dehaan@nl.wanadoo.com</a>)
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
     * @param encoding
     *    the encoding format for the XML, cannot be <code>null</code>.
     *
     * @param result
     *    the call result to convert to XML, cannot be <code>null</code>.
     *
     * @param xslt
     *    the URL of the XSLT to link to, can be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>out      == null
     *          || encoding == null
     *          || result   == null</code>.
     *
     * @throws IOException
     *    if there was an I/O error while writing to the output stream.
     */
    public static void output(PrintWriter out,
                              String encoding,
                              CallResult result,
                              String xslt)
    throws IllegalArgumentException, IOException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("out",      out,
                                      "encoding", encoding,
                                      "result",   result);
 
       // Create an XMLOutputter
       XMLOutputter outputter = new XMLOutputter(out, encoding);
 
       // Output the declaration
       // XXX: Make it configurable whether the declaration is output or not?
       outputter.declaration();
 
       // Output an xml-stylesheet processing instruction, if appropriate
       if (xslt != null) {
          outputter.pi("xml-stylesheet",
                       "type=\"text/xsl\" href=\"" + xslt + "\"");
       }
 
       // Write the result start tag
       outputter.startTag("result");
 
       // Write the error code
       String code = result.getErrorCode();
       if (code != null) {
          outputter.attribute("errorcode", code);
 
          // XXX: For backwards compatibility, write the error code in the
 	 //      'code' attribute and set the 'success' attribute to 'false'
          outputter.attribute("code", code);
          outputter.attribute("success", "false");
 
       // XXX: For backwards compatibility, set the 'success' attribute to
       //      'true'
       } else {
         outputter.attribute("success", "true");
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
     *    the {@link Element} object to convert to XML, cannot be
     *    <code>null</code>.
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

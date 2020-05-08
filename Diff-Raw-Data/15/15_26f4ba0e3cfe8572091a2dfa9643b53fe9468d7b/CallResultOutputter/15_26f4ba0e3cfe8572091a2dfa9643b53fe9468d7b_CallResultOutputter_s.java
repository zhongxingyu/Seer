 /*
  * $Id$
  */
 package org.xins.server;
 
 import java.io.InputStream;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.net.URL;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.xml.transform.Source;
 import javax.xml.transform.Result;
 import javax.xml.transform.Templates;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.stream.StreamSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.xins.common.MandatoryArgumentChecker;
 import org.xins.common.collections.PropertyReader;
 
 import org.znerd.xmlenc.XMLOutputter;
 
 /**
  * Transformer that is able to externalize a <code>FunctionResult</code> object to
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
     * @throws IllegalArgumentException
     *    if <code>out      == null
     *          || encoding == null
     *          || result   == null</code>.
     *
     * @throws IOException
     *    if there was an I/O error while writing to the output stream.
     */
    public static void output(PrintWriter    out,
                              String         encoding,
                              FunctionResult result)
    throws IllegalArgumentException, IOException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("out",      out,
                                      "encoding", encoding,
                                      "result",   result);
 
       // Create an XMLOutputter
       XMLOutputter xmlout = new XMLOutputter(out, encoding);
 
       // Output the declaration
       // XXX: Make it configurable whether the declaration is output or not?
       xmlout.declaration();
 
       // Write the result start tag
       xmlout.startTag("result");
 
       // Write the error code
       String code = result.getErrorCode();
       if (code != null) {
          xmlout.attribute("errorcode", code);
       }
 
       // Write the output parameters
       PropertyReader params = result.getParameters();
       if (params != null) {
          Iterator names = params.getNames();
          while (names.hasNext()) {
             String name  = (String) names.next();
             String value = params.get(name);
 
             xmlout.startTag("param");
             xmlout.attribute("name", name);
             xmlout.pcdata(value);
             xmlout.endTag(); // param
          }
       }
 
       // Write the data element
       Element dataElement = result.getDataElement();
       if (dataElement != null) {
          output(xmlout, dataElement);
       }
 
       xmlout.endTag(); // result
    }
 
    /**
     * Generates XML for the specified element.
     *
     * @param xmlout
     *    the {@link XMLOutputter} to use, cannot be <code>null</code>.
     *
     * @param element
     *    the {@link Element} object to convert to XML, cannot be
     *    <code>null</code>.
     *
     * @throws NullPointerException
     *    if <code>xmlout == null || element == null</code>.
     *
     * @throws IOException
     *    if there is an I/O error.
     */
    private static final void output(XMLOutputter xmlout, Element element)
    throws NullPointerException, IOException {
 
       // Start the tag
       xmlout.startTag(element.getType());
 
       // Write the attributes
       PropertyReader attributes = element.getAttributes();
       Iterator names = attributes.getNames();
       while (names.hasNext()) {
          String name  = (String) names.next();
          String value = attributes.get(name);
          xmlout.attribute(name, value);
       }
 
       // Process all contained elements and PCDATA sections
      List content = element.getContent();
       int count = content == null ? 0 : content.size();
       for (int i = 0; i < count; i++) {
          Object o = content.get(i);
          output(xmlout, (Element) o);
       }
       if (element.getText() != null) {
          xmlout.pcdata(element.getText());
       }
 
       // End the tag
       xmlout.endTag();
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

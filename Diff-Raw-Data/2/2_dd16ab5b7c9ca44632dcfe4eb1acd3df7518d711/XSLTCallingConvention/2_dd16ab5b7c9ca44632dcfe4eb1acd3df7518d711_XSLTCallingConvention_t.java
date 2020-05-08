 /*
  * $Id$
  *
  * Copyright 2003-2005 Wanadoo Nederland B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.server;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringReader;
 import java.io.Writer;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 
 import javax.xml.transform.Result;
 import javax.xml.transform.Source;
 import javax.xml.transform.Templates;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.stream.StreamSource;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.xins.common.Utils;
 import org.xins.common.collections.InvalidPropertyValueException;
 import org.xins.common.collections.MissingRequiredPropertyException;
 import org.xins.common.collections.PropertyReader;
 import org.xins.common.io.FastStringWriter;
 import org.xins.common.manageable.InitializationException;
 import org.xins.common.text.TextUtils;
 import org.xins.logdoc.ExceptionUtils;
 
 /**
  * XSLT calling convention.
  * The XSLT calling convention input is the same as for the standard calling
  * convention. The XSLT calling convention output is the result of the XML
  * normally returned by the standard calling convention and the specified
  * XSLT.
  * The Mime type of the return data can be specified in the XSLT using the 
  * media-type or method attribute of the XSL output element.
  * More information about the XSLT calling conventino can be found in the 
  * <a href="http://www.xins.org/docs/index.html">user guide</a>.
  *
  * @version $Revision$ $Date$
  * @author Anthony Goubard (<a href="mailto:anthony.goubard@nl.wanadoo.com">anthony.goubard@nl.wanadoo.com</a>)
  * @author Ernst de Haan (<a href="mailto:ernst.dehaan@nl.wanadoo.com">ernst.dehaan@nl.wanadoo.com</a>)
  */
 class XSLTCallingConvention extends StandardCallingConvention {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    /**
     * The name of the runtime property that defines if the templates should be
     * cached. Should be either <code>"true"</code> or <code>"false"</code>.
     * By default the cache is enabled.
     */
    private final static String TEMPLATES_CACHE_PROPERTY = "templates.cache";
 
    /**
     * The name of the runtime property that defines the location of the XSLT
     * templates. Should indicate a directory, either locally or remotely.
     * Local locations will be interpreted as relative to the user home
     * directory. The value should be a URL or a relative directory.
     *
     * <p>Examples of valid locations include:
     *
     * <ul>
     * <li><code>projects/dubey/xslt/</code></li>
     * <li><code>file:///home/john.doe/projects/dubey/xslt/</code></li>
     * <li><code>http://johndoe.com/projects/dubey/xslt/</code></li>
     * <li><code>https://xslt.johndoe.com/</code></li>
     * </ul>
     */
    private final static String TEMPLATES_LOCATION_PROPERTY = "templates.callingconvention.source";
 
    /**
     * The name of the input parameter that specifies the location of the XSLT
     * template to use.
     */
    final static String TEMPLATE_PARAMETER = "_template";
 
    /**
     * The name of the input parameter used to clear the template cache.
     */
    final static String CLEAR_TEMPLATE_CACHE_PARAMETER = "_cleartemplatecache";
 
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>XSLTCallingConvention</code> object.
     */
    XSLTCallingConvention() {
 
       // Create the transformer factory
       _factory = TransformerFactory.newInstance();
       _factory.setURIResolver(new URIResolver());
 
       // Initialize the template cache
       _templateCache = new HashMap(89);
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * The XSLT transformer. Never <code>null</code>.
     */
    private final TransformerFactory _factory;
 
    /**
     * Flag that indicates whether the templates should be cached. This field
     * is set during initialization.
     */
    private boolean _cacheTemplates;
 
    /**
     * Location of the XSLT templates. This field is initially
     * <code>null</code> and set during initialization.
     */
    private String _location;
 
    /**
     * Cache for the XSLT templates. Never <code>null</code>.
     */
    private Map _templateCache;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    protected void initImpl(PropertyReader runtimeProperties)
    throws MissingRequiredPropertyException,
           InvalidPropertyValueException,
           InitializationException {
 
       // Determine if the template cache should be enabled
       String cacheEnabled = runtimeProperties.get(TEMPLATES_CACHE_PROPERTY);
       initCacheEnabled(cacheEnabled);
 
       // Get the base directory of the style sheets.
       initXSLTLocation(runtimeProperties);
    }
 
    /**
     * Determines if the template cache should be enabled. If no value is
     * passed, then by default the cache is enabled. An invalid value, however,
     * will trigger an {@link InvalidPropertyValueException}.
     *
     * @param cacheEnabled
     *    the value of the runtime property that specifies whether the cache
     *    should be enabled, can be <code>null</code>.
     *
     * @throws InvalidPropertyValueException
     *    if the value is incorrect.
     */
    private void initCacheEnabled(String cacheEnabled)
    throws InvalidPropertyValueException {
       
       // By default, the template cache is enabled
       if (TextUtils.isEmpty(cacheEnabled)) {
          _cacheTemplates = true;
 
       // Trim before comparing with 'true' and 'false'
       } else {
          cacheEnabled = cacheEnabled.trim();
          if ("true".equals(cacheEnabled)) {
             _cacheTemplates = true;
          } else if ("false".equals(cacheEnabled)) {
             _cacheTemplates = false;
          } else {
             throw new InvalidPropertyValueException(TEMPLATES_CACHE_PROPERTY,
                cacheEnabled, "Expected either \"true\" or \"false\".");
          }
       }
 
       // Log whether the cache is enabled or not
       if (_cacheTemplates) {
          Log.log_3440();
       } else {
          Log.log_3441();
       }
    }
    
    /**
     * Initializes the location for the XSLT templates. Examples include:
     * 
     * <ul>
     *    <li><code>http://xslt.mycompany.com/myapi/</code></li>
     *    <li><code>file:///c:/home/</code></li>
     * </ul>
     *
     * <p>XSLT template files must match the names of the corresponding
     * functions.
     *
     * @param runtimeProperties
     *    the runtime properties, cannot be <code>null</code>.
     *
     * @throws NullPointerException
     *    if <code>runtimeProperties == null</code>.
     */
    private void initXSLTLocation(PropertyReader runtimeProperties) {
 
       // Get the value of the property
       _location = runtimeProperties.get(TEMPLATES_LOCATION_PROPERTY);
 
       // If the value is not a URL, it's considered as a relative path.
       // Relative URLs use the user directory as base dir.
       if (TextUtils.isEmpty(_location) || _location.indexOf("://") == -1) {
 
          // Trim the location and make sure it's never null
          _location = TextUtils.trim(_location, "");
 
          // Attempt to convert the home directory to a URL
          String home    = System.getProperty("user.dir");
          String homeURL = "";
          try {
             homeURL = new File(home).toURL().toString();
 
          // If the conversion to a URL failed, then just use the original
          } catch (IOException exception) {
             Utils.logIgnoredException(
                XSLTCallingConvention.class.getName(), "initImpl",
                "java.io.File",                        "toURL()",
                exception);
          }
 
          // Prepend the home directory URL
          _location = homeURL + _location;
       }
 
       // Log the base directory for XSLT templates
       Log.log_3442(_location);
    }
    
    protected void convertResultImpl(FunctionResult      xinsResult,
                                     HttpServletResponse httpResponse,
                                     HttpServletRequest  httpRequest)
    throws IOException {
 
       // If the request is to clear the cache, just clear the cache.
       if ("true".equals(httpRequest.getParameter(CLEAR_TEMPLATE_CACHE_PARAMETER))) {
          _templateCache.clear();
          PrintWriter out = httpResponse.getWriter();
          out.write("Done.");
          out.close();
          return;
       }
 
       // Get the XML output similar to the standard calling convention.
       FastStringWriter xmlOutput = new FastStringWriter();
       CallResultOutputter.output(xmlOutput, xinsResult, false);
       xmlOutput.close();
 
       // Get the location of the XSLT file.
       String xsltLocation = httpRequest.getParameter(TEMPLATE_PARAMETER);
       if (xsltLocation == null) {
          xsltLocation = _location + httpRequest.getParameter("_function") + ".xslt";
       }
       
       try {
          
          // Load the template or get it from the cache.
          Templates templates = null;
         if (_cacheTemplates && _templateCache.containsKey(xsltLocation)) {
             templates = (Templates) _templateCache.get(xsltLocation);
          } else {
             templates = _factory.newTemplates(_factory.getURIResolver().resolve(xsltLocation, _location));
             if (_cacheTemplates) {
                _templateCache.put(xsltLocation, templates);
             }
          }
          
          // Proceed to the transformation.
          Transformer xformer = templates.newTransformer();
          Source source = new StreamSource(new StringReader(xmlOutput.toString()));
          Writer buffer = new FastStringWriter(1024);
          Result result = new StreamResult(buffer);
          xformer.transform(source, result);
 
          // Determine the MIME type for the output.
          Properties outputProperties = templates.getOutputProperties();
          String mimeType = outputProperties.getProperty("media-type");
          if (mimeType == null) {
             String method = outputProperties.getProperty("method");
             if ("xml".equals(method)) {
                mimeType = "text/xml";
             } else if ("html".equals(method)) {
                mimeType = "text/html";
             } else if ("text".equals(method)) {
                mimeType = "text/plain";
             }
          }
          String encoding = outputProperties.getProperty("encoding");
          if (mimeType != null && encoding != null) {
             mimeType += ";charset=" + encoding;
          }
          if (mimeType != null) {
             httpResponse.setContentType(mimeType);
          }
 
          httpResponse.setStatus(HttpServletResponse.SC_OK);
          PrintWriter out = httpResponse.getWriter();
          out.print(buffer.toString());
          out.close();
       } catch (Exception exception) {
          if (exception instanceof IOException) {
             throw (IOException) exception;
          } else {
             String message = "Cannot transform the result with the XSLT "
                            + "located at \""
                            + xsltLocation
                            + "\".";
             IOException ioe = new IOException(message);
             ExceptionUtils.setCause(ioe, exception);
             throw ioe;
          }
       }
    }
 
 
    //-------------------------------------------------------------------------
    // Inner classes
    //-------------------------------------------------------------------------
 
    /**
     * Class used to revolved URL locations when an SLT file refers to another
     * XSLT file using a relative URL.
     *
     * @version $Revision$ $Date$
     * @author Anthony Goubard (<a href="mailto:anthony.goubard@nl.wanadoo.com">anthony.goubard@nl.wanadoo.com</a>)
     */
    static class URIResolver implements javax.xml.transform.URIResolver {
 
       //----------------------------------------------------------------------
       // Constructors
       //----------------------------------------------------------------------
 
       //----------------------------------------------------------------------
       // Fields
       //----------------------------------------------------------------------
 
       /**
        * The base directory of the previous call to resolve, if any. Cannot be <code>null</code>.
        */
       private String _base = "";
 
 
       //----------------------------------------------------------------------
       // Methods
       //----------------------------------------------------------------------
 
       /**
        * Revolves a hyperlink reference.
        *
        * @param href
        *    the hyperlink to resolve, cannot be <code>null</code>.
        *
        * @param base
        *    the base URI in effect when the href attribute was encountered,
        *    can be <code>null</code>.
        *
        * @return
        *    a {@link Source} object, or <code>null</code> if the href cannot
        *    be resolved, and the processor should try to resolve the URI
        *    itself.
        *
        * @throws TransformerException
        *    if an error occurs when trying to resolve the URI.
        */
       public Source resolve(String href, String base)
       throws TransformerException {
 
          // If no base is specified, use the last location.
          if (base == null) {
             base = _base;
             
          // The base should always ends with a slash.
          } else if (! base.endsWith("/")) {
             base += '/';
          }
          _base = base;
          
          // Result the URL
          String url = null;
          if (href.indexOf(":/") == -1) {
             url = base + href;
          } else {
             url = href;
             _base = href.substring(0, href.lastIndexOf('/') + 1);
          }
 
          // Return the source of the resolved XSLT.
          try {
             return new StreamSource(new URL(url).openStream());
          } catch (IOException ioe) {
             throw new TransformerException(ioe);
          }
       }
    }
 }

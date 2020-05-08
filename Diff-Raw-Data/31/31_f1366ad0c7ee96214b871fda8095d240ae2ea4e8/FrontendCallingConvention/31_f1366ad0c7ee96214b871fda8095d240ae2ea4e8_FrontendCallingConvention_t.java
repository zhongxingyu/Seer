 /*
  * $Id$
  *
  * Copyright 2003-2005 Wanadoo Nederland B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.server.frontend;
 
 import java.io.InputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.io.StringReader;
 import java.io.Writer;
 import java.net.URL;
 import java.util.Enumeration;
 import java.util.Iterator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import javax.xml.transform.Result;
 import javax.xml.transform.Source;
 import javax.xml.transform.SourceLocator;
 import javax.xml.transform.Templates;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.stream.StreamSource;
 
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.xins.common.MandatoryArgumentChecker;
 import org.xins.common.Utils;
 import org.xins.common.collections.InvalidPropertyValueException;
 import org.xins.common.collections.MissingRequiredPropertyException;
 import org.xins.common.collections.PropertyReader;
 import org.xins.common.collections.PropertyReaderUtils;
 import org.xins.common.collections.ProtectedPropertyReader;
 import org.xins.common.io.FastStringWriter;
 import org.xins.common.manageable.BootstrapException;
 import org.xins.common.manageable.InitializationException;
 import org.xins.common.spec.FunctionSpec;
 import org.xins.common.text.ParseException;
 import org.xins.common.text.TextUtils;
 import org.xins.common.xml.Element;
 import org.xins.common.xml.ElementBuilder;
 import org.xins.common.xml.ElementParser;
 import org.xins.common.xml.ElementSerializer;
 
 import org.xins.server.API;
 import org.xins.server.CustomCallingConvention;
 import org.xins.server.FunctionNotSpecifiedException;
 import org.xins.server.FunctionRequest;
 import org.xins.server.FunctionResult;
 import org.xins.server.InvalidRequestException;
 
 import org.znerd.xmlenc.XMLOutputter;
 
 /**
  * GPF calling convention.
  *
  * @version $Revision$ $Date$
  * @author Anthony Goubard (<a href="mailto:anthony.goubard@nl.wanadoo.com">anthony.goubard@nl.wanadoo.com</a>)
  */
 public final class FrontendCallingConvention extends CustomCallingConvention {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    /**
     * The request encoding format.
     */
    private static final String REQUEST_ENCODING = "UTF-8";
 
    /**
     * The response encoding format.
     */
    private static final String RESPONSE_ENCODING = "ISO-8859-1";
 
    /**
     * The content type of the HTTP response.
     */
    private static final String XML_CONTENT_TYPE = "text/xml;charset=" + RESPONSE_ENCODING;
 
    /**
     * The content type of the HTTP response.
     */
    private static final String HTML_CONTENT_TYPE = "text/html;charset=" + RESPONSE_ENCODING;
 
    /**
     * Secret key used when accessing <code>ProtectedPropertyReader</code>
     * objects.
     */
    private static final Object SECRET_KEY = new Object();
 
    /**
     * The name of the runtime property that defines if the templates should be
     * cached. Should be either <code>"true"</code> or <code>"false"</code>.
     * By default the cache is enabled.
     */
    private final static String TEMPLATES_CACHE_PROPERTY = "templates.cache";
 
    /**
     * Argument used when calling function with no parameters using the reflection API.
     */
    private final static Object[] NO_ARGS = {};
 
    /**
     * Argument used when finding a function with no parameters using the reflection API.
     */
    private final static Class[] NO_ARGS_CLASS = {};
 
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Creates a new <code>FrontendCallingConvention</code> instance.
     *
     * @param api
     *    the API, needed for the SOAP messages, cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>api == null</code>.
     */
    public FrontendCallingConvention(API api)
    throws IllegalArgumentException {
 
       // Check arguments
       MandatoryArgumentChecker.check("api", api);
 
       // Store the API
       _api = api;
 
       // Get the session manager manageable from the API
       try {
          _session = (SessionManager) api.getClass().getMethod("getSessionManager", NO_ARGS_CLASS).invoke(api, NO_ARGS);
       } catch (Exception ex) {
          ex.printStackTrace();
       }
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * The API. Never <code>null</code>.
     */
    private final API _api;
 
    /**
     * Session manager.
     */
    private SessionManager _session;
 
    /**
     * Location of the XSLT transformation Style Sheet.
     */
    private String _baseXSLTDir;
 
    /**
     * The XSLT transformer.
     */
    private TransformerFactory _factory;
 
    /**
     * The default page, cannot be <code>null</code>
     */
    private String _defaultCommand;
 
    /**
     * The login page or <code>null</code> if the framework does no have any login page.
     */
    private String _loginPage;
 
    /**
     * The redirection page, cannot be <code>null</code>.
     * If there is no redirection, the value in this ThreadLocal is null.
     */
    private ThreadLocal _redirection = new ThreadLocal();
 
    /**
     * Redirection map. The key is the command and the value is the redirection
     * command.
     */
    private HashMap _redirectionMap = new HashMap();
 
    /**
     * Flag that indicates whether the templates should be cached. This field
     * is set during initialization.
     */
    private boolean _cacheTemplates;
 
    /**
     * Cache for the XSLT templates. Never <code>null</code>.
     */
    private Map _templateCache = new HashMap();
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Bootstrap the <code>FrontendCallingConvention</code> object.
     */
    protected void bootstrapImpl(PropertyReader bootstrapProperties)
    throws MissingRequiredPropertyException,
           InvalidPropertyValueException,
           BootstrapException {
       _loginPage = bootstrapProperties.get("xiff.login.page");
       _defaultCommand = bootstrapProperties.get("xiff.default.command");
       if (_defaultCommand == null) {
          _defaultCommand = "DefaultCommand";
       }
 
       // Get the commands automatically redirected to another one
       Iterator itProperties = bootstrapProperties.getNames();
       while (itProperties.hasNext()) {
          String nextProp = (String) itProperties.next();
          if (nextProp.startsWith("xiff.redirect.")) {
             String command = nextProp.substring(14);
             String redirectionPage = bootstrapProperties.get(nextProp);
             _redirectionMap.put(command, redirectionPage);
          }
       }
    }
 
    /**
     * Initilialize the <code>FrontendCallingConvention</code> object.
     */
    protected void initImpl(PropertyReader runtimeProperties)
    throws MissingRequiredPropertyException,
           InvalidPropertyValueException,
           InitializationException {
 
       // Get the base directory of the Style Sheet
       _baseXSLTDir = runtimeProperties.get("templates." + _api.getName() + ".xiff.source");
       Properties systemProps = System.getProperties();
       _baseXSLTDir = TextUtils.replace(_baseXSLTDir, systemProps, "${", "}");
       _baseXSLTDir = _baseXSLTDir.replace('\\', '/');
 
       // Determine if the template cache should be enabled
       String cacheEnabled = runtimeProperties.get(TEMPLATES_CACHE_PROPERTY);
       initCacheEnabled(cacheEnabled);
 
       // Creates the transformer factory
       _factory = TransformerFactory.newInstance();
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
    }
 
    /**
     * Converts an HTTP request to a XINS request (implementation method). This
     * method should only be called from class {@link CustomCallingConvention}.
     * Only then it is guaranteed that the <code>httpRequest</code> argument is
     * not <code>null</code>.
     *
     * @param httpRequest
     *    the HTTP request, will not be <code>null</code>.
     *
     * @return
     *    the XINS request object, never <code>null</code>.
     *
     * @throws InvalidRequestException
     *    if the request is considerd to be invalid.
     *
     * @throws FunctionNotSpecifiedException
     *    if the request does not indicate the name of the function to execute.
     */
    protected FunctionRequest convertRequestImpl(HttpServletRequest httpRequest)
    throws InvalidRequestException,
           FunctionNotSpecifiedException {
 
       // Determine function name
       String functionName = httpRequest.getParameter("command");
       if (functionName == null || functionName.equals("")) {
          functionName = _defaultCommand;
       }
 
       // Control command has a special behaviour
       if (functionName.equals("Control")) {
          String action = httpRequest.getParameter("action");
          if ("ReadConfigFile".equals(action)) {
             functionName = "_ReloadProperties";
          } else {
             functionName = "_NoOp";
          }
          return new FunctionRequest("_NoOp", PropertyReaderUtils.EMPTY_PROPERTY_READER, null);
       }
 
       // Append the action to the function name
       String actionName = httpRequest.getParameter("action");
       if (actionName != null && !actionName.equals("") && !actionName.toLowerCase().equals("show")) {
          functionName += actionName.substring(0,1).toUpperCase() + actionName.substring(1);
       }
 
       _session.request(httpRequest);
 
       // Reset any previous value
       _redirection.set(null);
 
       // Redirect to the login page if not logged in
       if (_session.shouldLogIn()) {
          _redirection.set(_loginPage);
          return new FunctionRequest("_NoOp", PropertyReaderUtils.EMPTY_PROPERTY_READER, null);
       }
 
       // Redirect as specified in the bootstrap properties
       if (_redirectionMap.get(functionName) != null) {
          _redirection.set(_redirectionMap.get(functionName));
          try {
             _api.getAPISpecification().getFunction(functionName);
          } catch (Exception enfe) {
 
             // There is no specs or the function was not defined defined
             // Go directly to the result
             return new FunctionRequest("_NoOp", PropertyReaderUtils.EMPTY_PROPERTY_READER, null);
          }
       }
 
       // Determine function parameters
       ProtectedPropertyReader functionParams = new ProtectedPropertyReader(SECRET_KEY);
       Enumeration params = httpRequest.getParameterNames();
       while (params.hasMoreElements()) {
          String name = (String) params.nextElement();
          // TODO remove the next line when no longer needed.
          String realName = getRealParameter(name, functionName);
          String value = httpRequest.getParameter(name);
          functionParams.set(SECRET_KEY, realName, value);
       }
 
       // Get data section
       String dataSectionValue = httpRequest.getParameter("_data");
       Element dataElement;
       if (dataSectionValue != null && dataSectionValue.length() > 0) {
          ElementParser parser = new ElementParser();
 
          // Parse the data section
          try {
             dataElement = parser.parse(new StringReader(dataSectionValue));
 
          // I/O error, should never happen on a StringReader
          } catch (IOException ex) {
             throw new InvalidRequestException("Cannot parse the data section.", ex);
          // Parsing error
          } catch (ParseException ex) {
             throw new InvalidRequestException("Cannot parse the data section.", ex);
          }
       } else {
          dataElement = null;
       }
 
       // Construct and return the request object
       return new FunctionRequest(functionName, functionParams, dataElement);
    }
 
    /**
     * Converts a XINS result to an HTTP response (implementation method).
     *
     * @param xinsResult
     *    the XINS result object that should be converted to an HTTP response,
     *    will not be <code>null</code>.
     *
     * @param httpResponse
     *    the HTTP response object to configure, will not be <code>null</code>.
     *
     * @throws IOException
     *    if calling any of the methods in <code>httpResponse</code> causes an
     *    I/O error.
     */
    protected void convertResultImpl(FunctionResult      xinsResult,
                                     HttpServletResponse httpResponse,
                                     HttpServletRequest  httpRequest)
    throws IOException {
 
       Cookie cookie = new Cookie("SessionId", _session.getSessionId());
       httpResponse.addCookie(cookie);
 
       String mode = httpRequest.getParameter("mode");
       if ("template".equalsIgnoreCase(mode)) {
          String xsltFileName = httpRequest.getParameter("command");
          String xsltLocation = _baseXSLTDir + xsltFileName + ".xslt";
          //httpResponse.sendRedirect(xsltLocation);
          InputStream inputXSLT = new URL(xsltLocation).openStream();
          OutputStream output = httpResponse.getOutputStream();
          byte[] buffer = new byte[1024];
          while (true) {
             int length = inputXSLT.read(buffer);
             if (length == -1) break;
             output.write(buffer, 0, length);
          }
          inputXSLT.close();
          output.close();
          return;
       } else if (xinsResult.getParameter("redirect") != null ||
             (_redirection.get() != null && !_redirection.get().equals("-") && xinsResult.getErrorCode() == null) ||
             "NotLoggedIn".equals(xinsResult.getErrorCode())) {
          String redirection = xinsResult.getParameter("redirect");
          if (redirection == null && "NotLoggedIn".equals(xinsResult.getErrorCode())) {
             redirection = _loginPage + "&targetcommand=" + httpRequest.getParameter("command");
          }
          if (redirection == null && xinsResult.getErrorCode() == null) {
             redirection = (String) _redirection.get();
          }
          _redirection.set(null);
          if (redirection.equals("/")) {
             redirection = _defaultCommand;
          }
          if (redirection.startsWith("http://")) {
             httpResponse.sendRedirect(redirection);
          } else {
             redirection = httpRequest.getRequestURI() + "?command=" + redirection;
             PropertyReader parameters = xinsResult.getParameters();
             if (parameters != null) {
                Iterator parameterNames = parameters.getNames();
                while (parameterNames.hasNext()) {
                   String nextParameter = (String) parameterNames.next();
                   if (!"redirect".equals(nextParameter)) {
                      redirection += "&" + nextParameter + '=' + parameters.get(nextParameter);
                   }
                }
             }
             httpResponse.sendRedirect(redirection);
          }
          return;
       } else if (httpRequest.getParameter("command").equals("Control")) {
          String action = httpRequest.getParameter("action");
          if ("RemoveSessionProperties".equals(action)) {
             _session.removeProperties();
          } else if ("FlushCommandTemplateCache".equals(action)) {
             _templateCache.clear();
          } else if ("RefreshCommandTemplateCache".equals(action)) {
             _templateCache.clear();
             try {
                Iterator itCommandNames = _api.getAPISpecification().getFunctions().keySet().iterator();
                while (itCommandNames.hasNext()) {
                   String nextCommand = (String) itCommandNames.next();
                   String xsltLocation = _baseXSLTDir + nextCommand + ".xslt";
 
                   Templates template = _factory.newTemplates(new StreamSource(xsltLocation));
                   _templateCache.put(xsltLocation, template);
                }
                Iterator itVirtualFunctions = _redirectionMap.entrySet().iterator();
                while (itVirtualFunctions.hasNext()) {
                   Map.Entry nextFunction = (Map.Entry) itVirtualFunctions.next();
                   String xsltLocation = _baseXSLTDir + nextFunction.getKey() + ".xslt";
                   if (nextFunction.getValue().equals("-")) {
                      Templates template = _factory.newTemplates(new StreamSource(xsltLocation));
                      _templateCache.put(xsltLocation, template);
                   }
                }
             } catch (Exception ex) {
                ex.printStackTrace();
             }
          }
          xinsResult = new ControlResult(_api, _session, _redirectionMap);
       }
 
       Element commandResult = createXMLResult(httpRequest, xinsResult);
 
       String commandResultXML = serializeResult(commandResult);
 
       if ("source".equalsIgnoreCase(mode) || httpRequest.getParameter("command").equals("Control")) {
          PrintWriter out = httpResponse.getWriter();
          httpResponse.setContentType(XML_CONTENT_TYPE);
          httpResponse.setStatus(HttpServletResponse.SC_OK);
          out.print(commandResultXML);
          out.close();
       } else {
          String xsltFileName = httpRequest.getParameter("command");
          if (xsltFileName.endsWith("Show") || xsltFileName.endsWith("Okay")) {
             xsltFileName = xsltFileName.substring(0, xsltFileName.length() - 4);
          }
          String xsltLocation = _baseXSLTDir + xsltFileName + ".xslt";
          try {
             Templates template = getTemplate(xsltLocation);
             String resultHTML = translate(commandResultXML, template);
             String contentType = getContentType(template.getOutputProperties());
             PrintWriter out = httpResponse.getWriter();
             httpResponse.setContentType(contentType);
             httpResponse.setStatus(HttpServletResponse.SC_OK);
             out.print(resultHTML);
             out.close();
          } catch (Exception ex) {
             Utils.logProgrammingError(ex);
             throw new IOException(ex.getMessage());
          }
       }
    }
 
    /**
     * Creates the GPF XML from the result returned by the function
     * and in the session.
     *
     * @param httpRequest
     *    the HTTP request, cannot be <code>null</code>.
     *
     * @param xinsResult
     *    the result returned by the function, cannot be <code>null</code>.
     *
     * @return
     *    The XML Element containing the GPF XML result.
     */
    private Element createXMLResult(HttpServletRequest httpRequest, FunctionResult xinsResult) {
 
       // Create the source element
       ElementBuilder builder = new ElementBuilder("commandresult");
       builder.setAttribute("command", httpRequest.getParameter("command"));
       //builder.setAttribute("description", "Description of " + httpRequest.getParameter("command") + '.');
       ElementBuilder dataSection = new ElementBuilder("data");
 
       // Put all the sessions in the XML
       _session.result(xinsResult.getErrorCode() == null);
       Map sessionProperties = _session.getProperties();
       if (sessionProperties != null) {
          Iterator itSessionProperties = sessionProperties.entrySet().iterator();
          while (itSessionProperties.hasNext()) {
             Map.Entry nextEntry = (Map.Entry) itSessionProperties.next();
             String nextProperty = (String) nextEntry.getKey();
             Object propValue = nextEntry.getValue();
             if (propValue == null) {
                // continue
             } else if (propValue instanceof String) {
                ElementBuilder builderParam = new ElementBuilder("parameter");
                builderParam.setAttribute("name", "session." + nextProperty);
                builderParam.setText((String) propValue);
                builder.addChild(builderParam.createElement());
             } else if ("org.jdom.Element".equals(propValue.getClass().getName())) {
                //org.jdom.Element propElem = (org.jdom.Element) propValue;
                // TODO dataSection.addChild(Utils.convertFromJDOM(propValue));
             } else if (propValue instanceof Element) {
                dataSection.addChild((Element) propValue);
             } else if (propValue instanceof List) {
                Iterator itPropValue = ((List) propValue).iterator();
                while (itPropValue.hasNext()) {
                   Object nextPropertyInList = itPropValue.next();
                   if (nextPropertyInList == null) {
                      // continue
                   } else if ("org.jdom.Element".equals(nextPropertyInList.getClass().getName())) {
                      //org.jdom.Element propElem = (org.jdom.Element) nextPropertyInList;
                      // TODO dataSection.addChild(Utils.convertFromJDOM(nextPropertyInList));
                   } else if (nextPropertyInList instanceof Element) {
                      dataSection.addChild((Element) nextPropertyInList);
                   }
                }
             }
          }
       }
 
       // Store all the input parameters also in the XML
       Enumeration inputParameterNames = httpRequest.getParameterNames();
       while (inputParameterNames.hasMoreElements()) {
          String nextParameter = (String) inputParameterNames.nextElement();
          ElementBuilder builderParam = new ElementBuilder("parameter");
          builderParam.setAttribute("name", "input." + nextParameter);
          builderParam.setText(httpRequest.getParameter(nextParameter));
          builder.addChild(builderParam.createElement());
       }
 
       // Store all the returned parameters also in the XML
       PropertyReader parameters = xinsResult.getParameters();
       if (parameters != null) {
          Iterator parameterNames = parameters.getNames();
          while (parameterNames.hasNext()) {
             String nextParameter = (String) parameterNames.next();
             if (!"redirect".equals(nextParameter)) {
                ElementBuilder builderParam = new ElementBuilder("parameter");
                builderParam.setAttribute("name", nextParameter);
                builderParam.setText(parameters.get(nextParameter));
                builder.addChild(builderParam.createElement());
             }
          }
       }
 
       // Store the error code
       if (xinsResult.getErrorCode() != null) {
          if (xinsResult.getErrorCode().equals("_InvalidRequest") ||
                xinsResult.getErrorCode().equals("InvalidRequest")) {
             addParameter(builder, "error.type", "FieldError");
             ElementBuilder errorSection = new ElementBuilder("errorlist");
             Iterator incorrectParams = xinsResult.getDataElement().getChildElements().iterator();
             while (incorrectParams.hasNext()) {
                Element incorrectParamElement = (Element) incorrectParams.next();
                String paramName = incorrectParamElement.getAttribute("param");
                ElementBuilder fieldError = new ElementBuilder("fielderror");
                fieldError.setAttribute("field", paramName);
                if (incorrectParamElement.getLocalName().equals("missing-param")) {
                   fieldError.setAttribute("type", "mand");
                } else if (incorrectParamElement.getLocalName().equals("invalid-value-for-type")) {
                   fieldError.setAttribute("type", "format");
                } else {
                   fieldError.setAttribute("type", incorrectParamElement.getLocalName());
                }
                errorSection.addChild(fieldError.createElement());
             }
             dataSection.addChild(errorSection.createElement());
             builder.addChild(dataSection.createElement());
             return builder.createElement();
          } else {
             addParameter(builder, "error.type", "FunctionError");
             addParameter(builder, "error.code", xinsResult.getErrorCode());
          }
       }
 
       // Store the data section as it is
       Element resultElement = xinsResult.getDataElement();
       if (resultElement != null) {
          Iterator itChildren = resultElement.getChildElements().iterator();
          while (itChildren.hasNext()) {
             dataSection.addChild((Element) itChildren.next());
          }
       }
       builder.addChild(dataSection.createElement());
       return builder.createElement();
    }
 
    /**
     * Adds a parameter element to the XML result.
     *
     * @param builder
     *    the ElementBuilder where the parameter should be added.
     *
     * @param name
     *    the name of the parameter, cannot be <code>null</code>.
     *
     * @param value
     *    the value of the parameter, cannot be <code>null</code>.
     */
    private void addParameter(ElementBuilder builder, String name, String value) {
          ElementBuilder builderParam = new ElementBuilder("parameter");
          builderParam.setAttribute("name", name);
          builderParam.setText(value);
          builder.addChild(builderParam.createElement());
    }
 
    /**
     * Returns the String representation of the result.
     *
     * @param commandResult
     *    the Element object containing the result.
     *
     * @return
     *    the String representation of the Element.
     */
    private String serializeResult(Element commandResult) {
       // Store the result in a StringWriter before sending it.
       Writer buffer = new FastStringWriter();
 
       // Create an XMLOutputter
       try {
          XMLOutputter xmlout = new XMLOutputter(buffer, RESPONSE_ENCODING);
          ElementSerializer serializer = new ElementSerializer();
          serializer.output(xmlout, commandResult);
          return buffer.toString();
       } catch (IOException ioe) {
          ioe.printStackTrace();
          return null;
       }
    }
 
    /**
     * Translates the input using the specified XSLT.
     *
     * @param xmlInput
     *    the XML input that should be transformed, never <code>null</code>.
     *
     * @param template
     *    the template that should be used to transform the input XML, never <code>null</code>.
     *
     * @return
     *    the transformed XML, never <code>null</code>.
     */
    private String translate(String xmlInput, Templates template) throws Exception {
       try {
 
          // Use the template to create a transformer
          Transformer xformer = template.newTransformer();
 
          // Prepare the input and output files
          Source source = new StreamSource(new StringReader(xmlInput));
 
          // Store the result in a StringWriter before sending it.
          Writer buffer = new FastStringWriter(1024);
 
          Result result = new StreamResult(buffer);
 
          // Apply the xsl file to the source file and write the result to the output file
          xformer.transform(source, result);
 
          return buffer.toString();
       } catch (TransformerConfigurationException e) {
 
          // An error occurred in the XSL file
          throw e;
       } catch (TransformerException e) {
 
          // An error occurred while applying the XSL file
          // Get location of error in input file
          SourceLocator locator = e.getLocator();
          if (locator != null) {
             int col = locator.getColumnNumber();
             int line = locator.getLineNumber();
             String publicId = locator.getPublicId();
             String systemId = locator.getSystemId();
             // TODO log
          }
          throw e;
       }
    }
 
    /**
     * Gets the template to use to transform the XML.
     *
     * @param xsltUrl
     *    the URL of the XSLT file that should be used to transform the input XML,
     *    never <code>null</code>.
     *
     * @return
     *    the template, never <code>null</code>.
     *
     * @throws Exception
     *    if the URL is not found or the XSLT cannot be read correctly.
     */
    private Templates getTemplate(String xsltUrl) throws Exception {
 
       // Use the factory to create a template containing the xsl file
       // Load the template or get it from the cache.
       Templates template = null;
       if (_cacheTemplates && _templateCache.containsKey(xsltUrl)) {
          template = (Templates) _templateCache.get(xsltUrl);
       } else {
          try {
          template = _factory.newTemplates(new StreamSource(xsltUrl));
          if (_cacheTemplates) {
             _templateCache.put(xsltUrl, template);
          }
          } catch (Exception ex) {
             System.err.println("url " + xsltUrl);
             ex.printStackTrace();
             throw ex;
          }
       }
       return template;
    }
 
    /**
     * Gets the MIME type and the character encoding to return for the HTTP response.
     *
     * @param outputProperties
     *    the output properties defined in the XSLT, never <code>null</code>.
     *
     * @return
     *    the content type, never <code>null</code>.
     */
    private String getContentType(Properties outputProperties) {
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
          return mimeType;
       } else {
          return HTML_CONTENT_TYPE;
       }
    }
 
 
    /**
     * Gets the real parameter name.
     *
     * @param receivedParameter
     *    the name of the parameter as received.
     *
     * @param functionName
     *    the name of the function.
     *
     * @return
     *    the name of the parameter as specified in the function.
     *
     * @deprecated
     *    no mapping should be needed and the forms should send directly the correct parameters.
     */
    private String getRealParameter(String receivedParameter, String functionName) {
       if (receivedParameter.indexOf("_") != -1) {
          receivedParameter = receivedParameter.replaceAll("_", "");
       }
       try {
          FunctionSpec function = _api.getAPISpecification().getFunction(functionName);
          Iterator itParameters = function.getInputParameters().keySet().iterator();
          while (itParameters.hasNext()) {
             String nextParameterName = (String) itParameters.next();
             if (nextParameterName.equalsIgnoreCase(receivedParameter)) {
                return nextParameterName;
             }
          }
       } catch (Exception ex) {
          ex.printStackTrace();
       }
       return receivedParameter;
    }
 }

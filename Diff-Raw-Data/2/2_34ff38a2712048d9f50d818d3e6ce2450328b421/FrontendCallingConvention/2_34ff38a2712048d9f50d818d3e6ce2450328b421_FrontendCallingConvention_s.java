 /*
  * $Id$
  *
  * Copyright 2003-2005 Orange Nederland Breedband B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.server.frontend;
 
 import java.io.ByteArrayOutputStream;
 import java.io.InputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.io.StringReader;
 import java.io.Writer;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.Iterator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.TreeMap;
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
 import org.xins.common.collections.ChainedMap;
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
 import org.xins.server.Function;
 import org.xins.server.FunctionNotSpecifiedException;
 import org.xins.server.FunctionRequest;
 import org.xins.server.FunctionResult;
 import org.xins.server.InvalidRequestException;
 import org.xins.server.Log;
 
 import org.znerd.xmlenc.XMLOutputter;
 
 /**
  * XINS Front-end Framework calling convention.
  *
  * @version $Revision$ $Date$
  * @author <a href="mailto:anthony.goubard@orange-ft.com">Anthony Goubard</a>
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
          Log.log_3700(ex);
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
     * Redirection map. The key is the command and the value is the redirection
     * command.
     */
    private Map _redirectionMap = new ChainedMap();
 
    /**
     * Conditional redirection map. The key is the command and the value is the
     * {@link Templates} that will return the name of the redirection command.
     */
    private Map _conditionalRedirectionMap = new HashMap();
 
    /**
     * Flag that indicates whether the templates should be cached. This field
     * is set during initialization.
     */
    private boolean _cacheTemplates;
 
    /**
     * Cache for the XSLT templates. Never <code>null</code>.
     */
    private Map _templateCache = new HashMap();
 
    /**
     * The template used for the Control command.
     */
    private Templates _templateControl;
 
    /**
     * The list of the real function names for this API.
     */
    private List _functionList = new ArrayList();
 
 
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
       _loginPage = bootstrapProperties.get("xinsff.login.page");
       _defaultCommand = bootstrapProperties.get("xinsff.default.command");
       if (_defaultCommand == null) {
          _defaultCommand = "DefaultCommand";
       }
 
       initRedirections(bootstrapProperties);
    }
 
    /**
     * Initilialize the <code>FrontendCallingConvention</code> object.
     */
    protected void initImpl(PropertyReader runtimeProperties)
    throws MissingRequiredPropertyException,
           InvalidPropertyValueException,
           InitializationException {
 
       // Get the base directory of the Style Sheet
       _baseXSLTDir = runtimeProperties.get("templates." + _api.getName() + ".xinsff.source");
       Properties systemProps = System.getProperties();
       _baseXSLTDir = TextUtils.replace(_baseXSLTDir, systemProps, "${", "}");
       _baseXSLTDir = _baseXSLTDir.replace('\\', '/');
 
       // Determine if the template cache should be enabled
       String cacheEnabled = runtimeProperties.get(TEMPLATES_CACHE_PROPERTY);
       initCacheEnabled(cacheEnabled);
 
       // Creates the transformer factory
       _factory = TransformerFactory.newInstance();
 
       // Store the template used for the Control command
       try {
          StringReader controlXSLT = new StringReader(ControlResult.getControlTemplate());
          _templateControl = _factory.newTemplates(new StreamSource(controlXSLT));
       } catch (TransformerConfigurationException tcex) {
          Log.log_3701(tcex, "control");
       }
       
       // Gets the functions of the API
       Iterator itFunctions =  _api.getFunctionList().iterator();
       while (itFunctions.hasNext()) {
          Function nextFunction = (Function) itFunctions.next();
          _functionList.add(nextFunction.getName());
       }
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
 
       _session.request(httpRequest);
 
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
 
       // Redirect to the login page if not logged in or the function is not implemented
       if (_session.shouldLogIn() ||
             (_redirectionMap.get(functionName) != null && !_functionList.contains(functionName))) {
          return new FunctionRequest("_NoOp", PropertyReaderUtils.EMPTY_PROPERTY_READER, null);
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
       String command = httpRequest.getParameter("command");
       if (command == null || command.equals("")) {
          command = _defaultCommand;
       }
       String action = httpRequest.getParameter("action");
       if (action == null || action.equals("show")) {
          action = "";
       }
       String functionName = command + action;
 
       // Display the XSLT
       if ("template".equalsIgnoreCase(mode)) {
          byte[] xsltSource = getCommandXSLT(command);
          OutputStream output = httpResponse.getOutputStream();
          output.write(xsltSource);
          output.close();
          return;
       }
 
       // Control command
       if ("Control".equals(command)) {
          xinsResult = control(action);
       }
 
       Element commandResult = null;
       String commandResultXML = null;
       if (_conditionalRedirectionMap.get(functionName) != null) {
          commandResult = createXMLResult(httpRequest, xinsResult);
          commandResultXML = serializeResult(commandResult);
       }
 
       // Redirection
       String redirection = getRedirection(xinsResult, command, functionName, commandResultXML);
       if (redirection != null) {
          httpResponse.sendRedirect(redirection);
          return;
       }
 
       if (commandResult == null) {
          commandResult = createXMLResult(httpRequest, xinsResult);
          commandResultXML = serializeResult(commandResult);
       }
 
       if ("source".equalsIgnoreCase(mode)) {
          PrintWriter out = httpResponse.getWriter();
          httpResponse.setContentType(XML_CONTENT_TYPE);
          httpResponse.setStatus(HttpServletResponse.SC_OK);
          out.print(commandResultXML);
          out.close();
       } else {
          /*if (command.endsWith("Show") || command.endsWith("Okay")) {
             command = command.substring(0, command.length() - 4);
          }*/
          String xsltLocation = _baseXSLTDir + command + ".xslt";
          try {
             Templates template = null;
             if (command.equals("Control")) {
                template = _templateControl;
             } else {
                template = getTemplate(xsltLocation);
             }
             Log.log_3704(command);
             String resultHTML = translate(commandResultXML, template);
             String contentType = getContentType(template.getOutputProperties());
             PrintWriter out = httpResponse.getWriter();
             httpResponse.setContentType(contentType);
             httpResponse.setStatus(HttpServletResponse.SC_OK);
             out.print(resultHTML);
             out.close();
          } catch (Exception ex) {
 
             // Logging of the specific exception is done by the method called
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
                paramName = getOriginalParameter(paramName);
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
          Log.log_3702(ioe);
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
       } catch (TransformerConfigurationException tcex) {
 
          // An error occurred in the XSL file
          Log.log_3701(tcex, "<unknown>");
          throw tcex;
       } catch (TransformerException tex) {
 
          // An error occurred while applying the XSL file
          // Get location of error in input file
          SourceLocator locator = tex.getLocator();
          if (locator != null) {
             int line = locator.getLineNumber();
             int col = locator.getColumnNumber();
             String publicId = locator.getPublicId();
             String systemId = locator.getSystemId();
             Log.log_3703(tex, String.valueOf(line), String.valueOf(col), publicId, systemId);
          } else {
             Log.log_3703(tex, "<unknown>", "<unknown>", "<unknown>", "<unknown>");
          }
          throw tex;
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
          } catch (TransformerConfigurationException tcex) {
             Log.log_3701(tcex, xsltUrl);
             throw tcex;
          }
       }
       return template;
    }
 
    /**
     * Gets the XSLT of the specified command.
     *
     * @param command
     *    the command of which we want the XSLT, never <code>null</code>.
     *
     * @return
     *    the XSLT for the command, never <code>null</code>.
     *
     * @throws IOException
     *    if the XSLT cannot be found.
     */
    private byte[] getCommandXSLT(String command) throws IOException {
 
       String xsltLocation = _baseXSLTDir + command + ".xslt";
       //httpResponse.sendRedirect(xsltLocation);
       InputStream inputXSLT = new URL(xsltLocation).openStream();
       ByteArrayOutputStream output = new ByteArrayOutputStream();
       byte[] buffer = new byte[1024];
       while (true) {
          int length = inputXSLT.read(buffer);
          if (length == -1) break;
          output.write(buffer, 0, length);
       }
       inputXSLT.close();
       output.close();
       return output.toByteArray();
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
     * Executes the Control command.
     *
     * @param action
     *    the action associated with the Control command, can be <code>null</code>.
     *
     * @return
     *    the function result of the execution of the command
     */
    private FunctionResult control(String action) {
       if ("RemoveSessionProperties".equals(action)) {
          _session.removeProperties();
       } else if ("FlushCommandTemplateCache".equals(action)) {
          _templateCache.clear();
       } else if ("RefreshCommandTemplateCache".equals(action)) {
          _templateCache.clear();
          String xsltLocation = null;
          try {
             Iterator itRealFunctions = _api.getFunctionList().iterator();
             while (itRealFunctions.hasNext()) {
                Function nextFunction = (Function) itRealFunctions.next();
                String nextCommand = nextFunction.getName();
                xsltLocation = _baseXSLTDir + nextCommand + ".xslt";
 
                Templates template = _factory.newTemplates(new StreamSource(xsltLocation));
                _templateCache.put(xsltLocation, template);
             }
             Iterator itVirtualFunctions = _redirectionMap.entrySet().iterator();
             while (itVirtualFunctions.hasNext()) {
                Map.Entry nextFunction = (Map.Entry) itVirtualFunctions.next();
                xsltLocation = _baseXSLTDir + nextFunction.getKey() + ".xslt";
                if (nextFunction.getValue().equals("-")) {
                   Templates template = _factory.newTemplates(new StreamSource(xsltLocation));
                   _templateCache.put(xsltLocation, template);
                }
             }
          } catch (TransformerConfigurationException tcex) {
             Log.log_3701(tcex, xsltLocation);
          }
       }
       return new ControlResult(_api, _session, _redirectionMap);
    }
 
    /**
     * Gets the redirection URL.
     *
     * @param xinsResult
     *    the XINS result object that should be converted to an HTTP response,
     *    cannot be <code>null</code>.
     *
     * @param command
     *    the name of the command, cannot be <code>null</code>.
     *
     * @param functionName
     *    the name of the function, cannot be <code>null</code>.
     *
     * @param xmlResult
     *    the result of the call in case of a conditional redirection, can be <code>null</code>.
     *
     * @return
     *    the location where the command should be redirected, or <code>null</code>
     *    if the command should not be redirected.
     *
     * @throws Exception
     *    if the transformation of the XML result failed.
     */
    private String getRedirection(FunctionResult xinsResult, String command,
          String functionName, String xmlResult) {
       String redirection = xinsResult.getParameter("redirect");
       if (_session.shouldLogIn() || (redirection == null && "NotLoggedIn".equals(xinsResult.getErrorCode()))) {
          redirection = _loginPage + "&targetcommand=" + command;
       }
 
       if (redirection == null && _conditionalRedirectionMap.get(functionName) != null) {
          Templates conditionTemplate = (Templates) _conditionalRedirectionMap.get(functionName);
          try {
             redirection = translate(xmlResult, conditionTemplate);
          } catch (Exception ex) {
 
             // throw ex;
          }
       } else if (redirection == null && xinsResult.getErrorCode() == null) {
          redirection = (String) _redirectionMap.get(functionName);
       }
 
       // No redirection for this function
       if (redirection == null || redirection.equals("-") ||
             (xinsResult.getErrorCode() != null && "NotLoggedIn".equals(xinsResult.getErrorCode()))) {
          return null;
       }
 
       // Return the location of the redirection
       if (redirection.equals("/")) {
          redirection = _defaultCommand;
       } else if (!redirection.startsWith("http://")) {
          redirection = "?command=" + redirection;
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
       }
       return redirection;
    }
 
    /**
     * Initializes the redirections of the commands.
     *
     * @param bootstrapProperties
     *    the bootstrap properties, cannot be <code>null</code>.
     */
    private void initRedirections(PropertyReader bootstrapProperties) {
 
       TreeMap conditionalRedirectionProperties = new TreeMap();
 
       // Get the commands automatically redirected to another one
       Iterator itProperties = bootstrapProperties.getNames();
       while (itProperties.hasNext()) {
          String nextProp = (String) itProperties.next();
          if (nextProp.startsWith("xinsff.redirect.")) {
            String command = nextProp.substring(14);
             String redirectionPage = bootstrapProperties.get(nextProp);
             // TODO the condition should have the same order as in the XML?
             int conditionalPos = command.indexOf('[');
             if (conditionalPos != -1) {
                conditionalRedirectionProperties.put(command, redirectionPage);
             } else {
                _redirectionMap.put(command, redirectionPage);
             }
          }
       }
 
       // Create the conditional map
       String startXSLT = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
             "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">\n" +
             "<xsl:template match=\"commandresult\">\n" +
             "<xsl:choose>\n";
       Iterator itConditions = conditionalRedirectionProperties.keySet().iterator();
       String currentCommand = null;
       String xsltText = null;
       while (itConditions.hasNext()) {
 
          // Parse the line
          String nextKey = (String) itConditions.next();
          int conditionPos = nextKey.indexOf('[');
          String command = nextKey.substring(0, conditionPos);
          String condition = nextKey.substring(0, nextKey.length() - 1);
          String redirectionPage = (String) conditionalRedirectionProperties.get(nextKey);
 
          // Create the template object and store it
          if (currentCommand != null && !currentCommand.equals(command)) {
             finishConditionalTemplate(command, xsltText);
             currentCommand = null;
          }
 
          // Start a new template as it is a new command
          if (currentCommand == null) {
             xsltText = startXSLT;
             currentCommand = command;
          }
 
          // Add the condition in the XSL choose
          xsltText += "<xsl:when test=\"" + condition + "\"><xsl:text>" + redirectionPage + "</xsl:text></xsl:when>\n";
 
          // Close the last condition
          if (!itConditions.hasNext()) {
             finishConditionalTemplate(command, xsltText);
          }
       }
    }
 
    /**
     * Finishes the creation of the XSLT, creates the {@link Templates} object
     * and stores it in the map.
     *
     * @param command
     *    the command to store, cannot be <code>null</code>.
     *
     * @param currentXSLT
     *    the XSLT created before, cannot be <code>null</code>.
     */
    private void finishConditionalTemplate(String command, String currentXSLT) {
       String defaultRedirection = (String) _redirectionMap.get(command);
       if (defaultRedirection == null) {
          defaultRedirection = "-";
       }
       String xsltText = currentXSLT;
       xsltText += "<xsl:when test='not(param[@name='error.type'])'><xsl:text>" + defaultRedirection + "</xsl:text></xsl:when>\n";
       xsltText += "<xsl:otherwise><xsl:text>-</xsl:text></xsl:otherwise>\n";
       xsltText += "</xsl:choose></xsl:template></xsl:stylesheet>";
       try {
          StringReader conditionXSLT = new StringReader(xsltText);
          Templates conditionTemplate = _factory.newTemplates(new StreamSource(conditionXSLT));
          _conditionalRedirectionMap.put(command, conditionTemplate);
       } catch (TransformerConfigurationException tcex) {
          Log.log_3701(tcex, "conditional redirection for " + command + " command");
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
 
          // No function defined for this call, continue
       }
       return receivedParameter;
    }
 
    /**
     * Gets the original passed parameter name.
     *
     * @param parameter
     *    the name of the parameter as specified in the function, cannot be <code>null</code>.
     *
     * @return
     *    the name of the parameter as received.
     *
     * @deprecated
     *    no mapping should be needed and the forms should send directly the correct parameters.
     */
    private String getOriginalParameter(String parameter) {
       HashMap inputs = (HashMap) _session.getProperty("_inputs");
       Iterator itParameterNames = inputs.keySet().iterator();
       while (itParameterNames.hasNext()) {
          String nextParam = (String) itParameterNames.next();
          String flatParam = nextParam.replaceAll("_", "");
          if (parameter.equalsIgnoreCase(flatParam)) {
             return nextParam;
          }
       }
       return parameter;
    }
 }

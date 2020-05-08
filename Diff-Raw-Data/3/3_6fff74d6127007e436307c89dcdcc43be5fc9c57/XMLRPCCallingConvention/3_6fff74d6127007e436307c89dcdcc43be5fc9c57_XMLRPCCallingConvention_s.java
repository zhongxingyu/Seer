 /*
  * $Id$
  *
  * Copyright 2003-2007 Orange Nederland Breedband B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.server;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.xins.common.MandatoryArgumentChecker;
 import org.xins.common.collections.BasicPropertyReader;
 import org.xins.common.collections.ProtectedPropertyReader;
 import org.xins.common.spec.DataSectionElementSpec;
 import org.xins.common.spec.EntityNotFoundException;
 import org.xins.common.spec.FunctionSpec;
 import org.xins.common.spec.InvalidSpecificationException;
 import org.xins.common.text.ParseException;
 import org.xins.common.text.TextUtils;
 import org.xins.common.types.Type;
 import org.xins.common.xml.Element;
 import org.xins.common.xml.ElementBuilder;
 
 import org.znerd.xmlenc.XMLOutputter;
 
 /**
  * The XML-RPC calling convention.
  *
  * @version $Revision$ $Date$
  * @author <a href="mailto:anthony.goubard@orange-ftgroup.com">Anthony Goubard</a>
  */
 public class XMLRPCCallingConvention extends CallingConvention {
 
    /**
     * Secret key used when accessing <code>ProtectedPropertyReader</code>
     * objects.
     */
    private static final Object SECRET_KEY = new Object();
 
    /**
     * The formatter for XINS Date type.
     */
    private static final DateFormat XINS_DATE_FORMATTER = new SimpleDateFormat("yyyyMMdd");
 
    /**
     * The formatter for XINS Timestamp type.
     */
    private static final DateFormat XINS_TIMESTAMP_FORMATTER = new SimpleDateFormat("yyyyMMddHHmmss");
 
    /**
     * The formatter for XML-RPC dateTime.iso8601 type.
     */
    private static final DateFormat XML_RPC_TIMESTAMP_FORMATTER = new SimpleDateFormat("yyyyMMdd'T'HH:mm:ss");
 
    /**
     * The key used to store the parsing fault in the request attributes.
     */
    private static final String FAULT_KEY = "org.xins.server.xml-rpc.fault";
 
    /**
     * The key used to store the name of the function in the request attributes.
     */
    private static final String FUNCTION_NAME = "org.xins.server.xml-rpc.function";
 
    /**
     * The response encoding format.
     */
    protected static final String RESPONSE_ENCODING = "UTF-8";
 
    /**
     * The content type of the HTTP response.
     */
    protected static final String RESPONSE_CONTENT_TYPE = "text/xml; charset=" + RESPONSE_ENCODING;
 
    /**
     * The API. Never <code>null</code>.
     */
    private final API _api;
 
    /**
     * Creates a new <code>XMLRPCCallingConvention</code> instance.
     *
     * @param api
     *    the API, needed for the XML-RPC messages, cannot be
     *    <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>api == null</code>.
     */
    public XMLRPCCallingConvention(API api)
    throws IllegalArgumentException {
 
       // Check arguments
       MandatoryArgumentChecker.check("api", api);
 
       // Store the API reference (can be null!)
       _api = api;
    }
 
    /**
     * Returns the XML-RPC equivalent for the XINS type.
     *
     * @param parameterType
     *    the XINS type, cannot be <code>null</code>.
     *
     * @return
     *    the XML-RPC type, never <code>null</code>.
     */
    private static String convertType(Type parameterType) {
       if (parameterType instanceof org.xins.common.types.standard.Boolean) {
          return "boolean";
       } else if (parameterType instanceof org.xins.common.types.standard.Int8
             || parameterType instanceof org.xins.common.types.standard.Int16
             || parameterType instanceof org.xins.common.types.standard.Int32) {
          return "int";
       } else if (parameterType instanceof org.xins.common.types.standard.Float32
             || parameterType instanceof org.xins.common.types.standard.Float64) {
          return "double";
       } else if (parameterType instanceof org.xins.common.types.standard.Date
             || parameterType instanceof org.xins.common.types.standard.Timestamp) {
          return "dateTime.iso8601";
       } else if (parameterType instanceof org.xins.common.types.standard.Base64) {
          return "base64";
       } else {
          return "string";
       }
    }
 
    /**
     * Attribute a number for the error code.
     *
     * @param errorCode
     *    the error code, cannot be <code>null</code>.
     *
     * @return
     *    the error code number, always > 0;
     */
    private static int getErrorCodeNumber(String errorCode) {
       if (errorCode.equals("_DisabledFunction")) {
          return 1;
       } else if (errorCode.equals("_InternalError")) {
          return 2;
       } else if (errorCode.equals("_InvalidRequest")) {
          return 3;
       } else if (errorCode.equals("_InvalidResponse")) {
          return 4;
       } else {
 
          // Defined error code returned. For more information, see the
          // faultString element.
          return 99;
       }
    }
 
    protected String[] getSupportedMethods() {
       return new String[] { "POST" };
    }
 
    /**
     * Checks if the specified request can be handled by this calling
     * convention.
     *
     * <p>This method will not throw any exception.
     *
     * @param httpRequest
     *    the HTTP request to investigate, cannot be <code>null</code>.
     *
     * @return
     *    <code>true</code> if this calling convention is <em>possibly</em>
     *    able to handle this request, or <code>false</code> if it
     *    <em>definitely</em> not able to handle this request.
     *
     * @throws Exception
     *    if analysis of the request causes an exception;
     *    <code>false</code> will be assumed.
     */
    protected boolean matches(HttpServletRequest httpRequest)
    throws Exception {
 
       // Parse the XML in the request (if any)
       Element element = parseXMLRequest(httpRequest);
 
       // The root element must be <methodCall/>
       if (element.getLocalName().equals("methodCall")) {
 
          // The text within the <methodName/> element is the function name
          String function = element.getUniqueChildElement("methodName").getText();
 
          // There is a match only if the function name is non-empty
          if (! TextUtils.isEmpty(function)) {
             return true;
          }
       }
 
       return false;
    }
 
    protected FunctionRequest convertRequestImpl(HttpServletRequest httpRequest)
    throws InvalidRequestException,
           FunctionNotSpecifiedException {
 
       Element xmlRequest = parseXMLRequest(httpRequest);
       if (xmlRequest.getNamespaceURI() != null) {
          httpRequest.setAttribute(FAULT_KEY, "Namespace not allowed in XML-RPC requests");
          return new FunctionRequest("InvalidRequest", new BasicPropertyReader(), null, true);
       }
 
       if (!xmlRequest.getLocalName().equals("methodCall")) {
          String faultMessage = "Root element is not \"methodCall\" but \"" +
                xmlRequest.getLocalName() + "\".";
          httpRequest.setAttribute(FAULT_KEY, faultMessage);
          return new FunctionRequest("InvalidRequest", new BasicPropertyReader(), null, true);
       }
 
       Element methodNameElem;
       try {
          methodNameElem = xmlRequest.getUniqueChildElement("methodName");
       } catch (ParseException pex) {
          httpRequest.setAttribute(FAULT_KEY, "No unique methodName found");
          return new FunctionRequest("InvalidRequest", new BasicPropertyReader(), null, true);
       }
       if (methodNameElem.getNamespaceURI() != null) {
          httpRequest.setAttribute(FAULT_KEY, "Namespace not allowed in XML-RPC requests");
          return new FunctionRequest("InvalidRequest", new BasicPropertyReader(), null, true);
       }
       String functionName = methodNameElem.getText();
       httpRequest.setAttribute(FUNCTION_NAME, functionName);
 
       // Determine function parameters and the data section
       ProtectedPropertyReader functionParams = new ProtectedPropertyReader(SECRET_KEY);
       Element dataSection = null;
 
       List params = xmlRequest.getChildElements("params");
       if (params.size() == 0) {
          return new FunctionRequest(functionName, functionParams, null);
       } else if (params.size() > 1) {
          httpRequest.setAttribute(FAULT_KEY, "More than one params specified in the XML-RPC request.");
          return new FunctionRequest("InvalidRequest", new BasicPropertyReader(), null, true);
       }
       Element paramsElem = (Element) params.get(0);
       Iterator itParam = paramsElem.getChildElements("param").iterator();
       while (itParam.hasNext()) {
          Element nextParam = (Element) itParam.next();
          Element structElem;
          Element valueElem;
          try {
             valueElem = nextParam.getUniqueChildElement("value");
             structElem = valueElem.getUniqueChildElement(null);
          } catch (ParseException pex) {
             httpRequest.setAttribute(FAULT_KEY, "Invalid XML-RPC request.");
             return new FunctionRequest("InvalidRequest", new BasicPropertyReader(), null, true);
          }
          if (structElem.getLocalName().equals("struct")) {
 
             // Parse the input parameter
             String parameterName = null;
             String parameterValue = null;
             try {
                Element memberElem = structElem.getUniqueChildElement("member");
                Element memberNameElem = memberElem.getUniqueChildElement("name");
                Element memberValueElem = memberElem.getUniqueChildElement("value");
                Element typeElem = memberValueElem.getUniqueChildElement(null);
                parameterName = memberNameElem.getText();
                parameterValue = typeElem.getText();
                FunctionSpec functionSpec = _api.getAPISpecification().getFunction(functionName);
                Type parameterType = functionSpec.getInputParameter(parameterName).getType();
                parameterValue = convertInput(parameterType, typeElem);
             } catch (InvalidSpecificationException ise) {
 
                // keep the old value
             } catch (EntityNotFoundException enfe) {
 
                // keep the old value
             } catch (java.text.ParseException pex) {
 
                httpRequest.setAttribute(FAULT_KEY, "Invalid value for parameter \"" +
                      parameterName + "\".");
                return new FunctionRequest("InvalidRequest", new BasicPropertyReader(), null, true);
             } catch (ParseException pex) {
 
                httpRequest.setAttribute(FAULT_KEY, "Invalid XML-RPC request: " + pex.getMessage());
                return new FunctionRequest("InvalidRequest", new BasicPropertyReader(), null, true);
             }
             functionParams.set(SECRET_KEY, parameterName, parameterValue);
          } else if (structElem.getLocalName().equals("array")) {
 
             // Parse the input data section
             Element dataElem;
             try {
                Element arrayElem = valueElem.getUniqueChildElement("array");
                dataElem = arrayElem.getUniqueChildElement("data");
             } catch (ParseException pex) {
                httpRequest.setAttribute(FAULT_KEY, "Incorrect specification of the input data section.");
                return new FunctionRequest("InvalidRequest", new BasicPropertyReader(), null, true);
             }
             if (dataSection != null) {
                httpRequest.setAttribute(FAULT_KEY, "Only one data section is allowed per request.");
                return new FunctionRequest("InvalidRequest", new BasicPropertyReader(), null, true);
             }
             Map dataSectionSpec = null;
             try {
                FunctionSpec functionSpec = _api.getAPISpecification().getFunction(functionName);
                dataSectionSpec = functionSpec.getInputDataSectionElements();
             } catch (InvalidSpecificationException ise) {
 
                // keep the old value
             } catch (EntityNotFoundException enfe) {
 
                // keep the old value
             }
             ElementBuilder builder = new ElementBuilder("data");
             Iterator itValueElems = dataElem.getChildElements("value").iterator();
             while (itValueElems.hasNext()) {
                Element childValueElem = (Element) itValueElems.next();
                try {
                   Element childElem = parseElement(childValueElem, dataSectionSpec);
                   builder.addChild(childElem);
                } catch (ParseException pex) {
                   httpRequest.setAttribute(FAULT_KEY, 
                         "Incorrect format for data element in XML-RPC request: " + pex.getMessage());
                   return new FunctionRequest("InvalidRequest", new BasicPropertyReader(), null, true);
                }
             }
             dataSection = builder.createElement();
          } else {
             httpRequest.setAttribute(FAULT_KEY, "Only \"struct\" and \"array\" are valid as parameter type.");
             return new FunctionRequest("InvalidRequest", new BasicPropertyReader(), null, true);
          }
       }
 
       return new FunctionRequest(functionName, functionParams, null);
    }
 
    protected void convertResultImpl(FunctionResult      xinsResult,
                                     HttpServletResponse httpResponse,
                                     HttpServletRequest  httpRequest)
    throws IOException {
 
       // Send the XML output to the stream and flush
       httpResponse.setContentType(RESPONSE_CONTENT_TYPE);
       PrintWriter out = httpResponse.getWriter();
       httpResponse.setStatus(HttpServletResponse.SC_OK);
 
       // Store the result in a StringWriter before sending it.
       Writer buffer = new StringWriter(1024);
 
       // Create an XMLOutputter
       XMLOutputter xmlout = new XMLOutputter(buffer, RESPONSE_ENCODING);
 
       // Output the declaration
       xmlout.declaration();
 
       xmlout.startTag("methodResponse");
 
       String errorCode = xinsResult.getErrorCode();
       String faultRequest = (String) httpRequest.getAttribute(FAULT_KEY);
       if (errorCode != null || faultRequest != null) {
          xmlout.startTag("fault");
          xmlout.startTag("value");
          xmlout.startTag("struct");
 
          xmlout.startTag("member");
          xmlout.startTag("name");
          xmlout.pcdata("faultCode");
          xmlout.endTag(); // name
          xmlout.startTag("value");
          xmlout.startTag("int");
          if (errorCode != null) {
             xmlout.pcdata(String.valueOf(getErrorCodeNumber(errorCode)));
          } else {
             xmlout.pcdata("10");
          }
          xmlout.endTag(); // int
          xmlout.endTag(); // value
          xmlout.endTag(); // member
 
          xmlout.startTag("member");
          xmlout.startTag("name");
          xmlout.pcdata("faultString");
          xmlout.endTag(); // name
          xmlout.startTag("value");
          xmlout.startTag("string");
          if (errorCode != null) {
             xmlout.pcdata(errorCode);
          } else {
             xmlout.pcdata(faultRequest);
          }
          xmlout.endTag(); // string
          xmlout.endTag(); // value
          xmlout.endTag(); // member
 
          xmlout.endTag(); // struct
          xmlout.endTag(); // value
          xmlout.endTag(); // fault
       } else {
 
          String functionName = (String) httpRequest.getAttribute(FUNCTION_NAME);
 
          xmlout.startTag("params");
          xmlout.startTag("param");
          xmlout.startTag("value");
          xmlout.startTag("struct");
 
          // Write the output parameters
          Iterator outputParameterNames = xinsResult.getParameters().getNames();
          while (outputParameterNames.hasNext()) {
             String parameterName = (String) outputParameterNames.next();
             String parameterValue = xinsResult.getParameter(parameterName);
             String parameterTag = "string";
             try {
                FunctionSpec functionSpec = _api.getAPISpecification().getFunction(functionName);
                Type parameterType = functionSpec.getOutputParameter(parameterName).getType();
                parameterValue = convertOutput(parameterType, parameterValue);
                parameterTag = convertType(parameterType);
             } catch (InvalidSpecificationException ise) {
 
                // keep the old value
             } catch (EntityNotFoundException enfe) {
 
                // keep the old value
             } catch (java.text.ParseException pex) {
 
                throw new IOException("Invalid value for parameter \"" + parameterName + "\".");
             }
 
             // Write the member element
             xmlout.startTag("member");
             xmlout.startTag("name");
             xmlout.pcdata(parameterName);
             xmlout.endTag();
             xmlout.startTag("value");
             xmlout.startTag(parameterTag);
             xmlout.pcdata(parameterValue);
             xmlout.endTag(); // type tag
             xmlout.endTag(); // value
             xmlout.endTag(); // member
          }
 
          // Write the data section if needed
          Element dataSection = xinsResult.getDataElement();
          if (dataSection != null) {
 
             Map dataSectionSpec = null;
             try {
                FunctionSpec functionSpec = _api.getAPISpecification().getFunction(functionName);
                dataSectionSpec = functionSpec.getOutputDataSectionElements();
             } catch (InvalidSpecificationException ise) {
 
                // keep the old value
             } catch (EntityNotFoundException enfe) {
 
                // keep the old value
             }
 
             xmlout.startTag("member");
             xmlout.startTag("name");
             xmlout.pcdata("data");
             xmlout.endTag();
             xmlout.startTag("value");
             xmlout.startTag("array");
             xmlout.startTag("data");
             Iterator children = dataSection.getChildElements().iterator();
             while (children.hasNext()) {
                Element nextChild = (Element) children.next();
                writeElement(nextChild, xmlout, dataSectionSpec);
             }
             xmlout.endTag(); // data
             xmlout.endTag(); // array
             xmlout.endTag(); // value
             xmlout.endTag(); // member
          }
 
          xmlout.endTag(); // struct
          xmlout.endTag(); // value
          xmlout.endTag(); // param
          xmlout.endTag(); // params
       }
 
       xmlout.endTag(); // methodResponse
 
       // Write the result to the servlet response
       out.write(buffer.toString());
 
       out.close();
    }
 
    /**
     * Parses the data section element.
     *
     * @param valueElem
     *    the value element, cannot be <code>null</code>.
     *
     * @param dataSection
     *    the specification of the elements, cannot be <code>null</code>.
     *
     * @return
     *    the data section element, never <code>null</code>.
     *
     * @throws ParseException
     *    if the XML request is incorrect.
     */
    private Element parseElement(Element valueElem, Map dataSection) throws ParseException {
       Element structElem = valueElem.getUniqueChildElement("struct");
       DataSectionElementSpec elementSpec;
       Iterator itMemberElems = structElem.getChildElements("member").iterator();
       ElementBuilder builder;
       if (itMemberElems.hasNext()) {
          Element memberElem = (Element) itMemberElems.next();
          Element memberNameElem = memberElem.getUniqueChildElement("name");
          Element memberValueElem = memberElem.getUniqueChildElement("value");
          Element typeElem = memberValueElem.getUniqueChildElement(null);
          String parameterName = memberNameElem.getText();
          elementSpec = (DataSectionElementSpec) dataSection.get(parameterName);
          builder = new ElementBuilder(parameterName);
          if (typeElem.getLocalName().equals("string")) {
             builder.setText(typeElem.getText());
          } else if (typeElem.getLocalName().equals("array")) {
             Map childrenSpec = elementSpec.getSubElements();
             Element dataElem = typeElem.getUniqueChildElement("data");
             Iterator itValueElems = dataElem.getChildElements("value").iterator();
             while (itValueElems.hasNext()) {
                Element childValueElem = (Element) itValueElems.next();
                Element childElem = parseElement(childValueElem, childrenSpec);
                builder.addChild(childElem);
             }
          } else {
             throw new ParseException("Only \"string\" and \"array\" are valid as member value type.");
          }
       } else {
          throw new ParseException("The \"struct\" element should at least have one member.");
       }
 
       // Fill in the attributes
       while (itMemberElems.hasNext()) {
          Element memberElem = (Element) itMemberElems.next();
          Element memberNameElem = memberElem.getUniqueChildElement("name");
          Element memberValueElem = memberElem.getUniqueChildElement("value");
          Element typeElem = memberValueElem.getUniqueChildElement(null);
          String parameterName = memberNameElem.getText();
          String parameterValue = typeElem.getText();
 
          try {
             Type xinsElemType = elementSpec.getAttribute(parameterName).getType();
             parameterValue = convertInput(xinsElemType, memberValueElem);
          } catch (EntityNotFoundException enfe) {
 
             // keep the old value
          } catch (java.text.ParseException pex) {
             throw new ParseException("Invalid value for parameter \"" + parameterName + "\".");
          }
 
          builder.setAttribute(parameterName, parameterValue);
       }
       return builder.createElement();
    }
 
    /**
     * Write the given data section element to the output.
     *
     * @param dataElement
     *    the data section element, cannot be <code>null</code>.
     *
     * @param xmlout
     *    the output where the data section element should be serialised, cannot be <code>null</code>.
     *
     * @param dataSectionSpec
     *    the specification of the data element to be written, cannot be <code>null</code>.
     *
     * @throws IOException
     *    if an IO error occurs while writing on the output.
     */
    private void writeElement(Element dataElement, XMLOutputter xmlout, Map dataSectionSpec) throws IOException {
       xmlout.startTag("value");
       xmlout.startTag("member");
       xmlout.startTag("name");
       xmlout.pcdata(dataElement.getLocalName());
       xmlout.endTag(); // name
       xmlout.startTag("value");
       DataSectionElementSpec elementSpec = (DataSectionElementSpec) dataSectionSpec.get(dataElement.getLocalName());
       List children = dataElement.getChildElements();
       if (children.size() > 0) {
          Map childrenSpec = elementSpec.getSubElements();
          xmlout.startTag("array");
          xmlout.startTag("data");
          Iterator itChildren = children.iterator();
          while (itChildren.hasNext()) {
             Element nextChild = (Element) itChildren.next();
             writeElement(nextChild, xmlout, childrenSpec);
          }
          xmlout.endTag(); // data
          xmlout.endTag(); // array
       } else {
          xmlout.startTag("string");
          if (dataElement.getText() != null) {
             xmlout.pcdata(dataElement.getText());
          }
          xmlout.endTag(); // string
       }
       xmlout.endTag(); // value
       xmlout.endTag(); // member
 
       // Write the attributes
       Map attributesMap = dataElement.getAttributeMap();
       Iterator itAttributes = attributesMap.entrySet().iterator();
       while (itAttributes.hasNext()) {
          Map.Entry entry = (Map.Entry) itAttributes.next();
          Element.QualifiedName attributeQName = (Element.QualifiedName) entry.getKey();
          String attributeName = attributeQName.getLocalName();
          String attributeValue = (String) entry.getValue();
 
          String attributeTag;
          try {
             Type attributeType = elementSpec.getAttribute(attributeName).getType();
             attributeValue = convertOutput(attributeType, attributeValue);
             attributeTag = convertType(attributeType);
          } catch (EntityNotFoundException enfe) {
             attributeTag = "string";
          } catch (java.text.ParseException pex) {
             throw new IOException("Invalid value for parameter \"" + attributeName + "\".");
          }
 
          xmlout.startTag("member");
          xmlout.startTag("name");
          xmlout.pcdata(attributeName);
          xmlout.endTag(); // name
          xmlout.startTag("value");
          xmlout.startTag(attributeTag);
          xmlout.pcdata(attributeValue);
          xmlout.endTag(); // tag
          xmlout.endTag(); // value
          xmlout.endTag(); // member
       }
       xmlout.endTag(); // value
    }
 
    /**
     * Converts the XML-RPC input values to XINS input values.
     *
     * @param parameterType
     *    the type of the XINS parameter, cannot be <code>null</code>.
     *
     * @param typeElem
     *    the content of the XML-RPC value, cannot be <code>null</code>.
     *
     * @return
     *    the XINS value, never <code>null</code>.
     *
     * @throws java.text.ParseException
     *    if the parameterValue is incorrect for the type.
     */
    private String convertInput(Type parameterType, Element typeElem) throws java.text.ParseException {
       String xmlRpcType = typeElem.getLocalName();
       String parameterValue = typeElem.getText();
       if (parameterType instanceof org.xins.common.types.standard.Boolean) {
          if (parameterValue.equals("1")) {
             return "true";
          } else if (parameterValue.equals("0")) {
             return "false";
          } else {
             throw new java.text.ParseException("Incorrect value for boolean: " + parameterValue, 0);
          }
       }
       //System.err.println("type: " + xmlRpcType + " ; value: " + parameterValue);
       if (xmlRpcType.equals("dateTime.iso8601")) {
          Date date = XML_RPC_TIMESTAMP_FORMATTER.parse(parameterValue);
          if (parameterType instanceof org.xins.common.types.standard.Date) {
             synchronized (XINS_DATE_FORMATTER) {
                return XINS_DATE_FORMATTER.format(date);
             }
          } else if (parameterType instanceof org.xins.common.types.standard.Timestamp) {
             synchronized (XINS_TIMESTAMP_FORMATTER) {
                return XINS_TIMESTAMP_FORMATTER.format(date);
             }
          }
       }
       return parameterValue;
    }
 
    /**
     * Converts the XINS output values to XML-RPC output values.
     *
     * @param parameterType
     *    the type of the XINS parameter, cannot be <code>null</code>.
     *
     * @param parameterValue
     *    the XINS parameter value to convert, cannot be <code>null</code>.
     *
     * @return
     *    the XML-RPC value, never <code>null</code>.
     *
     * @throws java.text.ParseException
     *    if the parameterValue is incorrect for the type.
     */
    private String convertOutput(Type parameterType, String parameterValue) throws java.text.ParseException {
       if (parameterType instanceof org.xins.common.types.standard.Boolean) {
          if (parameterValue.equals("true")) {
             return "1";
          } else if (parameterValue.equals("false")) {
             return "0";
          } else {
             throw new java.text.ParseException("Incorrect value for boolean: " + parameterValue, 0);
          }
       } else if (parameterType instanceof org.xins.common.types.standard.Date) {
          Date date = null;
          synchronized (XINS_DATE_FORMATTER) {
             date = XINS_DATE_FORMATTER.parse(parameterValue);
          }
          synchronized (XML_RPC_TIMESTAMP_FORMATTER) {
             return XML_RPC_TIMESTAMP_FORMATTER.format(date);
          }
       } else if (parameterType instanceof org.xins.common.types.standard.Timestamp) {
          Date date = null;
          synchronized (XINS_TIMESTAMP_FORMATTER) {
             date = XINS_TIMESTAMP_FORMATTER.parse(parameterValue);
          }
          synchronized (XML_RPC_TIMESTAMP_FORMATTER) {
             return XML_RPC_TIMESTAMP_FORMATTER.format(date);
          }
       }
       return parameterValue;
    }
 }

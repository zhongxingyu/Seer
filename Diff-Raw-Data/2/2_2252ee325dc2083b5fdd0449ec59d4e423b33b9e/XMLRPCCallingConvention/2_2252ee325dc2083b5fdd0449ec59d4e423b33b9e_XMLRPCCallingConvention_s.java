 /*
  * $Id$
  *
  * Copyright 2003-2005 Wanadoo Nederland B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.server;
 
 import java.io.IOException;
 import java.io.PrintWriter;
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
 import org.xins.common.collections.ProtectedPropertyReader;
 import org.xins.common.io.FastStringWriter;
 import org.xins.common.spec.DataSectionElementSpec;
 import org.xins.common.spec.EntityNotFoundException;
 import org.xins.common.spec.FunctionSpec;
 import org.xins.common.spec.InvalidSpecificationException;
 import org.xins.common.text.TextUtils;
 import org.xins.common.types.Type;
 import org.xins.common.xml.Element;
 import org.xins.common.xml.ElementBuilder;
 
 import org.znerd.xmlenc.XMLOutputter;
 
 /**
  * The XML-RPC calling convention.
  *
  * @version $Revision$ $Date$
  * @author Anthony Goubard (<a href="mailto:anthony.goubard@nl.wanadoo.com">anthony.goubard@nl.wanadoo.com</a>)
  */
 final class XMLRPCCallingConvention extends CallingConvention {
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
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
 
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
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
     * The key used to store the name of the function in the request attributes.
     */
    private static final String FUNCTION_NAME = "_function";
 
    /**
     * The response encoding format.
     */
    private static final String RESPONSE_ENCODING = "UTF-8";
 
    /**
     * The content type of the HTTP response.
     */
    private static final String RESPONSE_CONTENT_TYPE = "text/xml;charset=" + RESPONSE_ENCODING;
 
 
    //-------------------------------------------------------------------------
    // Constructor
    //-------------------------------------------------------------------------
 
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
 
       // This calling convention is not deprecated, so pass 'false' up
       super(false);
 
       // Check arguments
       MandatoryArgumentChecker.check("api", api);
 
       // Store the API reference (can be null!)
       _api = api;
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
    * The API. Can be <code>null</code>.
     */
    private final API _api;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Checks if the specified request can be handled by this calling
     * convention.
     *
     * <p>The return value is as follows:
     *
     * <ul>
     *    <li>a positive value indicates that the request <em>can</em>
     *        be handled;
     *    <li>the value <code>0</code> indicates that the request
     *        <em>cannot</em> be handled;
     *    <li>a negative number indicates that it is <em>unknown</em>
     *        whether the request can be handled by this calling convention.
     * </ul>
     *
     * <p>This method will not throw any exception.
     *
     * @param httpRequest
     *    the HTTP request to investigate, cannot be <code>null</code>.
     *
     * @return
     *    a positive value if the request can be handled; <code>0</code> if the
     *    request cannot be handled or a negative value if it is unknown.
     */
    int matchesRequest(HttpServletRequest httpRequest) {
 
       // There is no match, unless XML can be parsed in the request and the
       // name of the function to invoke can be determined
       int match = NOT_MATCHING;
 
       try {
 
          // Parse the XML in the request (if any)
          Element element = parseXMLRequest(httpRequest);
 
          // The root element must be <methodCall/>
          if (element.getLocalName().equals("methodCall")) {
 
             // The text within the <methodName/> element is the function name
             String function = getUniqueChild(element, "methodName").getText();
 
             // There is a match only if the function name is non-empty
             if (! TextUtils.isEmpty(function)) {
                match = MATCHING;
             }
          }
 
       // If an exception is caught, the fallback NOT_MATCHING will be used
       } catch (Throwable exception) {
          // fall through
       }
 
       return match;
    }
 
    protected FunctionRequest convertRequestImpl(HttpServletRequest httpRequest)
    throws InvalidRequestException,
           FunctionNotSpecifiedException {
 
       Element xmlRequest = parseXMLRequest(httpRequest);
       if (!xmlRequest.getLocalName().equals("methodCall")) {
          throw new InvalidRequestException("Root element is not \"methodCall\" but \"" +
                xmlRequest.getLocalName() + "\".");
       }
 
       Element methodNameElem = getUniqueChild(xmlRequest, "methodName");
       String functionName = methodNameElem.getText();
       httpRequest.setAttribute(FUNCTION_NAME, functionName);
 
       // Determine function parameters and the data section
       ProtectedPropertyReader functionParams = new ProtectedPropertyReader(SECRET_KEY);
       Element dataSection = null;
 
       List params = xmlRequest.getChildElements("params");
       if (params.size() == 0) {
          return new FunctionRequest(functionName, functionParams, null);
       } else if (params.size() > 1) {
          throw new InvalidRequestException("More than one params specified in the XML-RPC request.");
       }
       Element paramsElem = (Element) params.get(0);
       Iterator itParam = paramsElem.getChildElements("param").iterator();
       while (itParam.hasNext()) {
          Element nextParam = (Element) itParam.next();
          Element valueElem = getUniqueChild(nextParam, "value");
          Element structElem = getUniqueChild(valueElem, null);
          if (structElem.getLocalName().equals("struct")) {
 
             // Parse the input parameter
             Element memberElem = getUniqueChild(structElem, "member");
             Element memberNameElem = getUniqueChild(memberElem, "name");
             Element memberValueElem = getUniqueChild(memberElem, "value");
             Element typeElem = getUniqueChild(memberValueElem, null);
             String parameterName = memberNameElem.getText();
             String parameterValue = typeElem.getText();
             try {
                FunctionSpec functionSpec = _api.getAPISpecification().getFunction(functionName);
                Type parameterType = functionSpec.getInputParameter(parameterName).getType();
                parameterValue = convertInput(parameterType, typeElem);
             } catch (InvalidSpecificationException ise) {
 
                // keep the old value
             } catch (EntityNotFoundException enfe) {
 
                // keep the old value
             } catch (java.text.ParseException pex) {
 
                throw new InvalidRequestException("Invalid value for parameter \"" +
                      parameterName + "\".", pex);
             }
             functionParams.set(SECRET_KEY, parameterName, parameterValue);
          } else if (structElem.getLocalName().equals("array")) {
 
             // Parse the input data section
             Element arrayElem = getUniqueChild(valueElem, "array");
             Element dataElem = getUniqueChild(arrayElem, "data");
             if (dataSection != null) {
                throw new InvalidRequestException("Only one data section is allowed per request");
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
                Element childElem = parseElement(childValueElem, dataSectionSpec);
                builder.addChild(childElem);
             }
             dataSection = builder.createElement();
          } else {
             throw new InvalidRequestException("Only \"struct\" and \"array\" are valid as parameter type.");
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
       Writer buffer = new FastStringWriter(1024);
 
       // Create an XMLOutputter
       XMLOutputter xmlout = new XMLOutputter(buffer, RESPONSE_ENCODING);
 
       // Output the declaration
       xmlout.declaration();
 
       xmlout.startTag("methodResponse");
 
       String errorCode = xinsResult.getErrorCode();
       if (errorCode != null) {
          xmlout.startTag("fault");
          xmlout.startTag("value");
          xmlout.startTag("struct");
 
          xmlout.startTag("member");
          xmlout.startTag("name");
          xmlout.pcdata("faultCode");
          xmlout.endTag(); // name
          xmlout.startTag("value");
          xmlout.startTag("int");
          xmlout.pcdata(String.valueOf(getErrorCodeNumber(errorCode)));
          xmlout.endTag(); // int
          xmlout.endTag(); // value
          xmlout.endTag(); // member
 
          xmlout.startTag("member");
          xmlout.startTag("name");
          xmlout.pcdata("faultString");
          xmlout.endTag(); // name
          xmlout.startTag("value");
          xmlout.startTag("string");
          xmlout.pcdata(errorCode);
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
     * Gets the unique child of the element.
     *
     * @param parentElement
     *    the parent element, cannot be <code>null</code>.
     *
     * @param elementName
     *    the name of the child element to get, or <code>null</code> if the
     *    parent have a unique child.
     *
     * @return
     *    The sub-element of this element.
     *
     * @throws InvalidRequestException
     *    if no child was found or more than one child was found.
     */
    private Element getUniqueChild(Element parentElement, String elementName)
    throws InvalidRequestException {
       List childList = null;
       if (elementName == null) {
          childList = parentElement.getChildElements();
       } else {
          childList = parentElement.getChildElements(elementName);
       }
       if (childList.size() == 0) {
          throw new InvalidRequestException("No \"" + elementName +
                "\" children found in the \"" + parentElement.getLocalName() +
                "\" element of the XML-RPC request.");
       } else if (childList.size() > 1) {
          throw new InvalidRequestException("More than one \"" + elementName +
                "\" children found in the \"" + parentElement.getLocalName() +
                "\" element of the XML-RPC request.");
       }
       return (Element) childList.get(0);
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
     * @throws InvalidRequestException
     *    if the XML request is incorrect.
     */
    private Element parseElement(Element valueElem, Map dataSection) throws InvalidRequestException {
       Element structElem = getUniqueChild(valueElem, "struct");
       DataSectionElementSpec elementSpec = null;
       Iterator itMemberElems = structElem.getChildElements("member").iterator();
       ElementBuilder builder = null;
       if (itMemberElems.hasNext()) {
          Element memberElem = (Element) itMemberElems.next();
          Element memberNameElem = getUniqueChild(memberElem, "name");
          Element memberValueElem = getUniqueChild(memberElem, "value");
          Element typeElem = getUniqueChild(memberValueElem, null);
          String parameterName = memberNameElem.getText();
          elementSpec = (DataSectionElementSpec) dataSection.get(parameterName);
          builder = new ElementBuilder(parameterName);
          if (typeElem.getLocalName().equals("string")) {
             builder.setText(typeElem.getText());
          } else if (typeElem.getLocalName().equals("array")) {
             Map childrenSpec = elementSpec.getSubElements();
             Element dataElem = getUniqueChild(typeElem, "data");
             Iterator itValueElems = dataElem.getChildElements("value").iterator();
             while (itValueElems.hasNext()) {
                Element childValueElem = (Element) itValueElems.next();
                Element childElem = parseElement(childValueElem, childrenSpec);
                builder.addChild(childElem);
             }
          } else {
             throw new InvalidRequestException("Only \"string\" and \"array\" are valid as member value type.");
          }
       } else {
          throw new InvalidRequestException("The \"struct\" element should at least have one member");
       }
 
       // Fill in the attributes
       while (itMemberElems.hasNext()) {
          Element memberElem = (Element) itMemberElems.next();
          Element memberNameElem = getUniqueChild(memberElem, "name");
          Element memberValueElem = getUniqueChild(memberElem, "value");
          Element typeElem = getUniqueChild(memberValueElem, null);
          String parameterName = memberNameElem.getText();
          String parameterValue = typeElem.getText();
 
          try {
             Type xinsElemType = elementSpec.getAttribute(parameterName).getType();
             parameterValue = convertInput(xinsElemType, memberValueElem);
          } catch (EntityNotFoundException enfe) {
 
             // keep the old value
          } catch (java.text.ParseException pex) {
             throw new InvalidRequestException("Invalid value for parameter \"" + parameterName + "\".");
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
       Iterator itAttributes = attributesMap.keySet().iterator();
       while (itAttributes.hasNext()) {
          Element.QualifiedName attributeQName = (Element.QualifiedName) itAttributes.next();
          String attributeName = attributeQName.getLocalName();
          String attributeValue = (String) attributesMap.get(attributeQName);
          String attributeTag = "string";
 
          try {
             Type attributeType = elementSpec.getAttribute(attributeName).getType();
             attributeValue = convertOutput(attributeType, attributeValue);
             attributeTag = convertType(attributeType);
          } catch (EntityNotFoundException enfe) {
 
             // keep the old value
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
             return XINS_DATE_FORMATTER.format(date);
          } else if (parameterType instanceof org.xins.common.types.standard.Timestamp) {
             return XINS_TIMESTAMP_FORMATTER.format(date);
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
          Date date = XINS_DATE_FORMATTER.parse(parameterValue);
          return XML_RPC_TIMESTAMP_FORMATTER.format(date);
       } else if (parameterType instanceof org.xins.common.types.standard.Timestamp) {
          Date date = XINS_TIMESTAMP_FORMATTER.parse(parameterValue);
          return XML_RPC_TIMESTAMP_FORMATTER.format(date);
       }
       return parameterValue;
    }
 }

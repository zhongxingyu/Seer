 /*
  * $Id$
  *
  * Copyright 2003-2005 Wanadoo Nederland B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.client;
 
 import java.util.List;
 
 import org.xins.common.MandatoryArgumentChecker;
 
 import org.xins.common.service.TargetDescriptor;
 
 import org.xins.common.text.FastStringBuffer;
 
 /**
  * Exception thrown to indicate a standard error code was received that
  * indicates the request from the client-side is considered invalid by the
  * server-side.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:ernst.dehaan@nl.wanadoo.com">ernst.dehaan@nl.wanadoo.com</a>)
  *
  * @since XINS 1.2.0
  */
 public class InvalidRequestException
 extends StandardErrorCodeException {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    /**
     * Delegate for the constructor that determines the detail message based on
     * a <code>XINSCallResultData</code> object.
     *
     * @param result
     *    the {@link XINSCallResultData} instance, should not be
     *    <code>null</code>.
     *
     * @return
     *    the detail message for the constructor to use, never
     *    <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>result == null
     *          || result.{@link XINSCallResultData#getErrorCode() getErrorCode()} == null</code>.
     */
    private static final String determineDetail(XINSCallResultData result)
    throws IllegalArgumentException {
 
       // Argument cannot be null
       MandatoryArgumentChecker.check("result", result);
 
       // Result must be unsuccessful
       String errorCode = result.getErrorCode();
       if (errorCode == null) {
          throw new IllegalArgumentException("result.getErrorCode() == null");
       }
 
       // Parse the data element
       DataElement element = result.getDataElement();
       return createMessage(element);
    }
 
    /**
     * Creates the message containing the details of the error.
     *
     * @param element
     *    The {@link DataElement} containing the details of the error.
     *
     * @return the message or <code>null</code> if the element is 
     *         <code>null</code> or empty.
     */
    static String createMessage(DataElement element) {
       
       // Parse the data element
       if (element == null) {
          return null;
       }
 
       FastStringBuffer detail = new FastStringBuffer(50);
 
       // Handle all missing parameters
       List missingParamElements = element.getChildElements("missing-param");
       if (missingParamElements != null) {
          int size = missingParamElements.size();
          for (int i = 0; i < size; i++) {
             DataElement e = (DataElement) missingParamElements.get(i);
             String parameterName = e.getAttribute("param");
             String elementName = e.getAttribute("element");
             if (elementName == null && parameterName != null && parameterName.length() >= 1) {
                detail.append("No value given for required parameter \""
                            + parameterName
                            + "\". ");
             } else if (elementName != null &&  elementName.length() >= 1 && parameterName != null && parameterName.length() >= 1) {
                detail.append("No value given for required attribute \""
                            + parameterName
                            + "\" in the element \""
                            + elementName
                            + "\". ");
             }
          }
       }
 
       // Handle all invalid parameter values
       List invalidValueElements = element.getChildElements("invalid-value-for-type");
       if (invalidValueElements != null) {
          int size = invalidValueElements.size();
          for (int i = 0; i < size; i++) {
             DataElement e = (DataElement) invalidValueElements.get(i);
             String parameterName = e.getAttribute("param");
             String typeName      = e.getAttribute("type");
             String elementName   = e.getAttribute("element");
             if (parameterName != null && parameterName.length() >= 1) {
                detail.append("The value for parameter \""
                            + parameterName
                            + "\" is considered invalid for the type \""
                            + typeName
                            + "\". ");
             } else if (elementName != null &&  elementName.length() >= 1 && parameterName != null && parameterName.length() >= 1) {
                detail.append("The value for attribute \""
                            + parameterName
                            + "\" in the element \""
                            + elementName
                            + "\" is considered invalid for the type \""
                            + typeName
                            + "\". ");
             }
             // XXX: actual value is not specified in message
          }
       }
 
       // Handle all param-combo values
       List paramComboElements = element.getChildElements("param-combo");
       if (paramComboElements != null) {
          int size = paramComboElements.size();
          for (int i = 0; i < size; i++) {
             DataElement e = (DataElement) paramComboElements.get(i);
             String typeName      = e.getAttribute("type");
             detail.append("The values of the parameters ");
             List parameterList   = e.getChildElements("param");
             int parametersSize = parameterList.size();
             for (int j = 0; j < parametersSize; j++) {
                DataElement e2 = (DataElement) parameterList.get(j);
                String parameterName = e2.getAttribute("name");
                if (parameterName != null && parameterName.length() >= 1) {
                   detail.append("\""
                               + parameterName
                               + "\" ");
                }
             }
             detail.append("do not match the param-combo of type \""
                            + typeName
                            + "\". ");
          }
       }
       
       // Remove the last space from the string, if there is any
       if (detail.getLength() >= 1) {
          detail.crop(detail.getLength() - 1);
          return detail.toString();
       } else {
          return null;
       }
    }
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>InvalidRequestException</code>.
     *
     * @param request
     *    the original request, guaranteed not to be <code>null</code>.
     *
     * @param target
     *    the target on which the request was executed, guaranteed not to be
     *    <code>null</code>.
     *
     * @param duration
     *    the call duration, guaranteed to be &gt;= <code>0L</code>.
     *
     * @param resultData
     *    the data returned from the call, guaranteed to be <code>null</code>
     *    and must have an error code set.
     *
     * @throws IllegalArgumentException
     *    if <code>result == null
     *          || result.{@link XINSCallResultData#getErrorCode() getErrorCode()} == null</code>.
     */
    InvalidRequestException(XINSCallRequest    request,
                            TargetDescriptor   target,
                            long               duration,
                            XINSCallResultData resultData)
    throws IllegalArgumentException {
       super(request, target, duration, resultData,
             determineDetail(resultData));
 
       // TODO: Parse details
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    // TODO: Add methods for retrieval of details
 
    //-------------------------------------------------------------------------
    // Inner classes
    //-------------------------------------------------------------------------
 
    /**
     * Issue as returned in the call result.
     *
     * @version $Revision$ $Date$
     * @author Ernst de Haan (<a href="mailto:ernst.dehaan@nl.wanadoo.com">ernst.dehaan@nl.wanadoo.com</a>)
     */
    private static abstract class Issue
    extends Object {
 
       //----------------------------------------------------------------------
       // Constructors
       //----------------------------------------------------------------------
 
       /**
        * Constructs a new <code>Issue</code> instance.
        *
        * @param description
        *    a textual description of this issue, cannot be <code>null</code>.
        *
        * @throws IllegalArgumentException
        *    if <code>description == null</code>.
        */
       private Issue(String description)
       throws IllegalArgumentException {
          _description = description;
       }
 
 
       //----------------------------------------------------------------------
       // Fields
       //----------------------------------------------------------------------
 
       /**
        * A textual description of this issue. Cannot be <code>null</code>.
        */
       private final String _description;
 
 
       //----------------------------------------------------------------------
       // Methods
       //----------------------------------------------------------------------
 
       /**
        * Returns a description of this issue.
        *
        * @return
        *    a description of this issue, never <code>null</code>. Always
        *    a sentence that starts with a capital and ends with a
        *    <code>'.'</code (full stop).
        */
       private final String describe() {
          return _description;
       }
    }
 
    /**
     * Issue indicating a required parameter is missing.
     *
     * @version $Revision$ $Date$
     * @author Ernst de Haan (<a href="mailto:ernst.dehaan@nl.wanadoo.com">ernst.dehaan@nl.wanadoo.com</a>)
     */
   public static final class MissingParameterIssue
    extends Issue {
 
       //----------------------------------------------------------------------
       // Constructors
       //----------------------------------------------------------------------
 
       /**
        * Constructs a new <code>MissingParameterIssue</code> instance.
        *
        * @param parameterName
        *    the name of the missing parameter, cannot be <code>null</code>.
        *
        * @throws IllegalArgumentException
        *    if <code>parameterName == null</code>.
        */
       private MissingParameterIssue(String parameterName)
       throws IllegalArgumentException {
 
          // Call superconstructor
          super("No value given for required parameter \""
              + parameterName
              + "\".");
 
          // Check precondition
          MandatoryArgumentChecker.check("parameterName", parameterName);
       }
 
 
       //----------------------------------------------------------------------
       // Fields
       //----------------------------------------------------------------------
 
       //----------------------------------------------------------------------
       // Methods
       //----------------------------------------------------------------------
    }
 }

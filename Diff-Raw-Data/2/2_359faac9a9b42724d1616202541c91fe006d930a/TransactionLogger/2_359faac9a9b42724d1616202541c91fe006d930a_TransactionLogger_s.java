 /*
  * $Id$
  *
  * Copyright 2003-2007 Orange Nederland Breedband B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.server;
 
 import org.xins.common.FormattedParameters;
 import org.xins.common.collections.PropertyReader;
 import org.xins.common.text.DateConverter;
 import org.xins.common.xml.Element;
 
 /**
  * Transaction logger. Responsible for logging transactions.
  *
  * @since XINS 3.0
  *
  * @version $Revision$ $Date$
  * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
  */
 public class TransactionLogger {
 
    /**
     * Converter for transforming dates to text.
     */
    private static final DateConverter DATE_CONVERTER = new DateConverter(true);
 
    /**
     * Constructs a new <code>TransactionLogger</code> object.
     */
    protected TransactionLogger() {
       // empty
    }
 
    /**
     * Logs the specified transaction. This is the main entry point for this
     * class.
     *
     * <p>This method determines the function name, the request and result data
     * and then delegates the call to
     * {@linkplain #logTransaction(String,PropertyReader,Element,String,PropertyReader,Element,String,long,long) logTransaction(functionName, requestParams, requestDataElement, resultCode, resultParams, resultDataElement, ip, start, duration}.
     *
     * @param request
     *    the {@link FunctionRequest}, should not be <code>null</code>.
     *
     * @param result
     *    the {@link FunctionResult}, should not be <code>null</code>.
     *
     * @param ip
     *    the client IP address, should not be <code>null</code>.
     *
     * @param start
     *    the start of the call, a timestamp as milliseconds since the Epoch.
     *
     * @param duration
     *    the duration of the call, in milliseconds.
     *
     * @throws NullPointerException
     *    if <code>request == null || result  == null</code>.
     */
    protected void logTransaction(FunctionRequest request,
                                  FunctionResult  result,
                                  String          ip,
                                  long            start,
                                  long            duration)
    throws NullPointerException {
 
       // Determine the result code, fallback is the zero digit
       String resultCode = result.getErrorCode();
       if (resultCode == null || resultCode.length() < 1) {
          resultCode = "0";
       }
 
       // Delegate to lower-level method
       logTransaction(request.getFunctionName(),
                      request.getParameters(),
                      request.getDataElement(),
                      resultCode,
                      result.getParameters(),
                      result.getDataElement(),
                      ip,
                      start,
                      duration);
    }
 
    /**
     * Log a single transaction after a function was invoked.
     *
     * @param functionName
     *    the name of the function that was invoked,
     *    cannot be <code>null</code>.
     *
     * @param requestParams
     *    the incoming request parameters, or <code>null</code> if none.
     *
     * @param requestDataElement
     *    the input data section, or <code>null</code> if none.
     *
     * @param resultCode
     *    the result error code, or <code>null</code> on success.
     *
     * @param resultParams
     *    the outgoing result parameters, or <code>null</code> if none.
     *
    * @param requestDataElement
     *    the output data section, or <code>null</code> if none.
     *
     * @param ip
     *    the IP address of the caller, cannot be <code>null</code>.
     *
     * @param start
     *    the time of the incoming call, in milliseconds since January 1, 1970.
     *
     * @param duration
     *    the duration of the call in milliseconds.
     */
    protected void logTransaction(String         functionName,
                                  PropertyReader requestParams,
                                  Element        requestDataElement,
                                  String         resultCode,
                                  PropertyReader resultParams,
                                  Element        resultDataElement,
                                  String         ip,
                                  long           start,
                                  long           duration) {
 
       // Serialize the start date and the input and output data
       String serStart  = DATE_CONVERTER.format(start);
       Object inParams  = new FormattedParameters(requestParams, requestDataElement);
       Object outParams = new FormattedParameters(resultParams,  resultDataElement );
 
       // Actually log the transaction
       Log.log_3540(serStart, ip, functionName, duration, resultCode, inParams, outParams);
       Log.log_3541(serStart, ip, functionName, duration, resultCode);
    }
 }

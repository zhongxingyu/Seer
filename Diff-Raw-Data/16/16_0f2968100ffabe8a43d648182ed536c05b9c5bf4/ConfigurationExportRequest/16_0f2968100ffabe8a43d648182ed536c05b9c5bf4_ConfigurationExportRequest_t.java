 /*******************************************************************************
  * Copyright (c) Feb 9, 2011 Zend Technologies Ltd. 
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Eclipse Public License v1.0 
  * which accompanies this distribution, and is available at 
  * http://www.eclipse.org/legal/epl-v10.html  
  *******************************************************************************/
 package org.zend.webapi.internal.core.connection.request;
 
 import java.util.Date;
 
 import org.restlet.data.Method;
 import org.zend.webapi.core.connection.data.IResponseData.ResponseType;
 import org.zend.webapi.core.connection.data.values.WebApiVersion;
 import org.zend.webapi.core.connection.response.ResponseCode;
 
 /**
  * Export the current server / cluster configuration into a file.
  * <p>
  * A successful call to the configurationExport method will result in an HTTP
  * response with the configuration snapshot file in the response body. The
  * content type for the configuration snapshot file is
 * application/vnd.zend.serverconfig. In addition, the response will include a
 * Content-disposition header specifying a suggested file name for the
  * configuration snapshot file. This is different from most Web API calls where
 * the content type is expected to be application/vnd.zend.serverpi+xml;
 * version= and the response body payload is expected to be in XML format. In
  * case of error, a regular error response will be returned, containing an
  * <errorData> element as defined for other Web API methods.
  * <p>
  * 
  * Method Parameters: none
  * 
  * @author Roy, 2011
  * 
  */
 public class ConfigurationExportRequest extends AbstractRequest {
 
 	private static final ResponseCode[] RESPONSE_CODES = new ResponseCode[] { ResponseCode.OK };
 
 	public ConfigurationExportRequest(WebApiVersion version, Date date,
 			String keyName, String userAgent, String host, String secretKey) {
 		super(version, date, keyName, userAgent, host, secretKey);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.zend.webapi.core.connection.request.IRequest#getUri()
 	 */
 	public String getUri() {
 		return "/ZendServerManager/Api/configurationExport";
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.zend.webapi.core.connection.request.IRequest#getMethod()
 	 */
 	public Method getMethod() {
 		return Method.GET;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.zend.webapi.internal.core.connection.request.AbstractRequest#
 	 * getResponseCodeList()
 	 */
 	@Override
 	protected ResponseCode[] getValidResponseCode() {
 		return RESPONSE_CODES;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.zend.webapi.core.connection.request.IRequest#getExpectedResponseDataType
 	 * ()
 	 */
 	public ResponseType getExpectedResponseDataType() {
 		return ResponseType.SERVER_CONFIG;
 	}
 }

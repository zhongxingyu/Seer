 /*******************************************************************************
  * Copyright (c) Jan 28, 2011 Zend Technologies Ltd. 
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Eclipse Public License v1.0 
  * which accompanies this distribution, and is available at 
  * http://www.eclipse.org/legal/epl-v10.html  
  *******************************************************************************/
 package org.zend.webapi.internal.core.connection.request;
 
 import java.util.Date;
 
 import org.restlet.data.Method;
 import org.zend.webapi.core.connection.data.IResponseData;
 import org.zend.webapi.core.connection.data.IResponseData.ResponseType;
 import org.zend.webapi.core.connection.data.values.WebApiVersion;
 import org.zend.webapi.core.connection.response.ResponseCode;
 
 /**
  * Re-enable a cluster member. This process may be asynchronous if Session
  * Clustering is used – if this is the case, the initial operation will return
  * an HTTP 202 response. This action is idempotent. Running it on an enabled
 * server will result in a 200 OK response with no consequences. On a ZSCM
  * with no valid license, this operation will fail.
  * <p>
  * 
  * Method Parameters:
  * <table border="1">
  * <tr>
  * <th>Parameter</th>
  * <th>Type</th>
  * <th>Required</th>
  * <th>Description</th>
  * </tr>
  * <tr>
  * <td>serverId</td>
  * <td>String</td>
  * <td>Yes</td>
  * <td>Server Id</td>
  * </tr>
  * </tr>
  * </table>
  * 
  * @author Roy, 2011
  */
 public class ClusterEnableServerRequest extends AbstractRequest {
 
 	private static final ResponseCode[] RESPONSE_CODES = new ResponseCode[] {
 			ResponseCode.OK, ResponseCode.ACCEPTED };
 
 	public ClusterEnableServerRequest(WebApiVersion version, Date date,
 			String keyName, String userAgent, String host, String secretKey) {
 		super(version, date, keyName, userAgent, host, secretKey);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.zend.webapi.core.connection.request.IRequest#getUri()
 	 */
 	public String getUri() {
 		return "/ZendServerManager/Api/clusterEnableServer";
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.zend.webapi.core.connection.request.IRequest#getMethod()
 	 */
 	public Method getMethod() {
 		return Method.POST;
 	}
 
 	/**
 	 * set Server Id
 	 * 
 	 * @param serverId
 	 * @return 
 	 */
 	public ClusterEnableServerRequest setServerId(String serverId) {
 		addParameter("serverId", serverId);
 		return this;
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
 		return IResponseData.ResponseType.SERVER_INFO;
 	}
 
 }

 package edu.psu.iam.cpr.core.api.returns;
 
 import java.util.Arrays;
 
 import edu.psu.iam.cpr.core.service.returns.CredentialReturn;
 
 /**
  * This class provides the implementation of the Get Credential API Return.
  * 
  * Copyright 2012 The Pennsylvania State University
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  * @package edu.psu.iam.cpr.core.api.returns
  * @author $Author: jvuccolo $
  * @version $Rev$
  * @lastrevision $Date$
  */
 public class CredentialServiceReturn {
 	
 	/** Contains the status code that is the result of executing of a service. */
 	private int statusCode;
 	
 	/** Contains the text message associated with the execution of a service. */
 	private String statusMessage;
 	
 	/** Contains the names return information, this will only be populated during a GetNames service call. */
 	private CredentialReturn[] credentialReturnRecord = null;
 	
 	/** Contains the number of elements in the namesReturnRecord array. */
 	private int numberElements = 0;
 	
 	/**
 	 * Constructor
 	 */
 	public CredentialServiceReturn() {
 		super();
 	}
 	
 	/**
 	 * Constructor
 	 * @param statusCode contains the status code that is the result of executing this service.
 	 * @param statusMessage contains the status message that is the result of executing this service.
 	 * @param credentialReturnRecordArray contains an array of credential return objects.
 	 * @param numberElements contains the number of elements in the array.
 	 */
 	public CredentialServiceReturn(int statusCode, String statusMessage,
 			CredentialReturn[] credentialReturnRecordArray, int numberElements) {
 		super();
 		this.statusCode = statusCode;
 		this.statusMessage = statusMessage;
		if (credentialReturnRecord != null) {
 			this.credentialReturnRecord = Arrays.copyOf(credentialReturnRecordArray, credentialReturnRecordArray.length);
 		}
 		else {
 			this.credentialReturnRecord = null;
 		}
 		this.numberElements = numberElements;
 	}
 
 	/**
 	 * Constructor
 	 * @param statusCode contains the status code that is the result of executing this service.
 	 * @param statusMessage contains the status message that is result of executing this service.
 	 */
 	public CredentialServiceReturn(int statusCode, String statusMessage) {
 		this.statusCode = statusCode;
 		this.statusMessage = statusMessage;
 	}
 
 	/**
 	 * @return the statusCode
 	 */
 	public int getStatusCode() {
 		return statusCode;
 	}
 
 	/**
 	 * @param statusCode the statusCode to set
 	 */
 	public void setStatusCode(int statusCode) {
 		this.statusCode = statusCode;
 	}
 
 	/**
 	 * @return the statusMessage
 	 */
 	public String getStatusMessage() {
 		return statusMessage;
 	}
 
 	/**
 	 * @param statusMessage the statusMessage to set
 	 */
 	public void setStatusMessage(String statusMessage) {
 		this.statusMessage = statusMessage;
 	}
 
 	/**
 	 * @return the credentialReturnRecord
 	 */
 	public CredentialReturn[] getCredentialReturnRecord() {
 		return credentialReturnRecord;
 	}
 
 	/**
 	 * @param credentialReturnRecordArray the credentialReturnRecord to set
 	 */
 	public void setCredentialReturnRecord(final CredentialReturn[] credentialReturnRecordArray) {
 		if (credentialReturnRecord != null) {
 			this.credentialReturnRecord = Arrays.copyOf(credentialReturnRecordArray, credentialReturnRecordArray.length);
 		}
 		else {
 			this.credentialReturnRecord = null;
 		}
 	}
 
 	/**
 	 * @return the numberElements
 	 */
 	public int getNumberElements() {
 		return numberElements;
 	}
 
 	/**
 	 * @param numberElements the numberElements to set
 	 */
 	public void setNumberElements(int numberElements) {
 		this.numberElements = numberElements;
 	}
 
 }

 /**
  * JALProducer class stores relevant application data and is the primary class 
  * for the producer library.
  * <p>
  * Source code in 3rd-party is licensed and owned by their respective
  * copyright holders.
  * <p>
  * All other source code is copyright Tresys Technology and licensed as below.
  * <p>
  * Copyright (c) 2012 Tresys Technology LLC, Columbia, Maryland, USA
  * <p>
  * This software was developed by Tresys Technology LLC
  * with U.S. Government sponsorship.
  * <p>
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * <p>
  *    http://www.apache.org/licenses/LICENSE-2.0
  * <p>
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.tresys.jalop.producer;
 
 import java.security.PrivateKey;
 import java.security.PublicKey;
 import java.security.cert.X509Certificate;
 
 import com.etsy.net.ConnectionHeader.MessageType;
import com.tresys.jalop.common.JALUtils;
 import com.tresys.jalop.common.JALUtils.DMType;
 import com.tresys.jalop.producer.ApplicationMetadataXML;
 
 public class JALProducer {
 	
 	private ApplicationMetadataXML xml;
 	private String hostName;
 	private String applicationName;
 	private PrivateKey privateKey;
 	private PublicKey publicKey;
 	private X509Certificate certificate;
 	private DMType digestMethod;
 	private String socketFile;
 	private MessageType messageType;
 	
 	/**
 	 * Constructor 
 	 */
 	public JALProducer() {
 		
 	}
 	
 	/**
 	 * Constructor that takes an ApplicationMetadataXML
 	 * 
 	 * @param xml	the ApplicationMetadataXML
 	 */
 	public JALProducer(ApplicationMetadataXML xml) {
 		this.xml = xml;
 	}
 
 	/**
 	 * Constructor with all the possible params
 	 * 
 	 * @param xml				the ApplicationMetadataXML
 	 * @param hostName			the name of the host
 	 * @param applicationName	the name of the application
 	 * @param privateKey		the private key
 	 * @param publicKey			the public key
 	 * @param certificate		the X509Certificate used for signing
 	 * @param digestMethod		the type of digest method to use
 	 * @param socketFile		the socket file for connection info
 	 */
 	public JALProducer(ApplicationMetadataXML xml, String hostName,
 			String applicationName, PrivateKey privateKey, PublicKey publicKey,
 			X509Certificate certificate, DMType digestMethod, String socketFile) {
 		this.xml = xml;
 		this.hostName = hostName;
 		this.applicationName = applicationName;
 		this.privateKey = privateKey;
 		this.publicKey = publicKey;
 		this.certificate = certificate;
 		this.digestMethod = digestMethod;
 		this.socketFile = socketFile;
 	}
 
 	/**
 	 * Sets the xml
 	 * 
 	 * @param xml	the ApplicationMetadataXML to set
 	 */
 	public void setXml(ApplicationMetadataXML xml) {
 		this.xml = xml;
 	}
 	
 	/**
 	 * Gets the xml
 	 * 
 	 * @return	the ApplicationMetadataXML
 	 */
 	public ApplicationMetadataXML getXml() {
 		return this.xml;
 	}
 	
 	/**
 	 * Sets the hostName
 	 * 
 	 * @param hostName	the String to set
 	 */
 	public void setHostName(String hostName) {
 		this.hostName = hostName;
 	}
 	
 	/**
 	 * Gets the hostName
 	 * 
 	 * @return	the hostName property
 	 */
 	public String getHostName() {
 		return this.hostName;
 	}
 	
 	/**
 	 * Sets the applicationName
 	 * 
 	 * @param applicationName	the String to set
 	 */
 	public void setApplicationName(String applicationName) {
 		this.applicationName = applicationName;
 	}
 	
 	/**
 	 * Gets the applicationName
 	 * 
 	 * @return	the applicationName property
 	 */
 	public String getApplicationName() {
 		return this.applicationName;
 	}
 	
 	/**
 	 * Gets the privateKey
 	 * 
 	 * @return the privateKey
 	 */
 	public PrivateKey getPrivateKey() {
 		return privateKey;
 	}
 
 	/**
 	 * Sets the privateKey
 	 * 
 	 * @param privateKey the PrivateKey to set
 	 */
 	public void setPrivateKey(PrivateKey privateKey) {
 		this.privateKey = privateKey;
 	}
 
 	/**
 	 * Gets the publicKey
 	 * 
 	 * @return the publicKey
 	 */
 	public PublicKey getPublicKey() {
 		return publicKey;
 	}
 
 	/**
 	 * Sets the publicKey
 	 * 
 	 * @param publicKey the PublicKey to set
 	 */
 	public void setPublicKey(PublicKey publicKey) {
 		this.publicKey = publicKey;
 	}
 
 	/**
 	 * Gets the certificate
 	 * 
 	 * @return the X509Certificate
 	 */
 	public X509Certificate getCertificate() {
 		return certificate;
 	}
 
 	/**
 	 * Sets the certificate
 	 * 
 	 * @param certificate the X509Certificate to set
 	 */
 	public void setCertificate(X509Certificate certificate) {
 		this.certificate = certificate;
 	}
 
 	/**
 	 * Gets the type of digest method
 	 * 
 	 * @return the digestMethod
 	 */
 	public DMType getDigestMethod() {
 		return digestMethod;
 	}
 
 	/**
 	 * Sets the type of digest method
 	 * 
 	 * @param digestMethod the DMType to set
 	 */
 	public void setDigestMethod(DMType digestMethod) {
 		this.digestMethod = digestMethod;
 	}
 
 	/**
 	 * Gets the String which is a path to the socket file
 	 * 
 	 * @return the socketFile String
 	 */
 	public String getSocketFile() {
 		return socketFile;
 	}
 
 	/**
 	 * Sets the path of the socket file
 	 * 
 	 * @param socketFile the String path to the socket file to set
 	 */
 	public void setSocketFile(String socketFile) {
 		this.socketFile = socketFile;
 	}
 
 	/**
 	 * Gets messageType from an enum in ConnectionHeader
 	 * 
 	 * @return the MessageType
 	 */
 	public MessageType getMessageType() {
 		return messageType;
 	}

	/**
	 * Sets the messageType to JALP_LOG_MSG and calls processSend in JALUtils
	 *
	 * @param buffer	optional, a String which is the buffer
	 * @throws Exception
	 */
	public void jalpLog(String buffer) throws Exception {
		this.messageType = MessageType.JALP_LOG_MSG;
		JALUtils.processSend(this, buffer);
	}

 }

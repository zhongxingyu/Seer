 /**
  * Abstract class that has an ApplicationMetadataType and holds common info
  * for the three possible types: logger, syslog, and custom.
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
  *
  */
 
 package com.tresys.jalop.producer;
 
 import java.util.UUID;
 
 import com.tresys.jalop.common.JALException;
 import com.tresys.jalop.schemas.mil.dod.jalop_1_0.applicationmetadatatypes.*;
 import javax.xml.bind.*;
 
 public abstract class ApplicationMetadataXML {
 
 	private static ObjectFactory of;
 	private ApplicationMetadataType amt;
 
 	/**
 	 * Constructor
 	 */
 	protected ApplicationMetadataXML() {
 		of = new ObjectFactory();
 		amt = new ApplicationMetadataType();
 	}
 
 	/**
 	 * Constructor for an ApplicationMetadataXML with a LoggerType
 	 *
 	 * @param logger the LoggerType
 	 * @throws Exception
 	 */
 	protected ApplicationMetadataXML(LoggerType logger) throws Exception {
 		if(logger == null) {
 			throw new JALException("logger cannot be null when instantiating a LoggerXML");
 		}
 		of = new ObjectFactory();
 		amt = new ApplicationMetadataType();
 		amt.setLogger(logger);
 	}
 
 	/**
 	 * Constructor for an ApplicationMetadataXML with a SyslogType
 	 *
 	 * @param syslog the SyslogType
 	 * @throws Exception
 	 */
 	protected ApplicationMetadataXML(SyslogType syslog) throws Exception {
 		if(syslog == null) {
 			throw new JALException("syslog cannot be null when instantiating a SyslogXML");
 		}
 		of = new ObjectFactory();
 		amt = new ApplicationMetadataType();
 		amt.setSyslog(syslog);
 	}
 
 	/**
 	 * Constructor for an ApplicationMetadataXML with a custom string
 	 *
 	 * @param custom the string to use for the custom field
 	 * @throws Exception
 	 */
 	protected ApplicationMetadataXML(String custom) throws Exception {
 		if(custom == null || "".equals(custom)) {
 			throw new JALException("custom cannot be null when instantiating a CustomXML");
 		}
 		of = new ObjectFactory();
 		amt = new ApplicationMetadataType();
 		amt.setCustom(custom);
 	}
 
 	/**
 	 * Gets the JournalMetadataType
 	 *
 	 * @return	the ApplicationMetadataType's JournalMetadataType object
 	 */
 	public JournalMetadataType getJournalMetadata() {
 		return amt.getJournalMetadata();
 	}
 
 	/**
 	 * Sets the JournalMetadataType
 	 *
 	 * @param jmt	the JournalMetadataType to set
 	 */
 	public void setJournalMetadata(JournalMetadataType jmt) {
 		amt.setJournalMetadata(jmt);
 	}
 
 	/**
 	 * Gets the eventId
 	 *
 	 * @return the ApplicationMetadataType's eventId String
 	 */
 	public String getEventId() {
 		return amt.getEventID();
 	}
 
 	/**
 	 * Sets the eventId
 	 *
 	 * @param eventId	the String to set
 	 */
 	public void setEventId(String eventId) {
 		amt.setEventID(eventId);
 	}
 
 	/**
 	 * Gets the JID
 	 *
 	 * @return the ApplicationMetadataType's JID string
 	 */
 	public String getJID() {
 		return amt.getJID();
 	}
 
 	/**
 	 * Only to be used by {@link LoggerXML} to get the LoggerType
 	 *
 	 * @return the ApplicationMetadataType's LoggerType object
 	 */
 	protected LoggerType getLogger() {
 		return amt.getLogger();
 	}
 
 	/**
 	 * Only to be used by {@link SyslogXML} to get the SyslogType
 	 *
 	 * @return the ApplicationMetadataType's SyslogType object
 	 */
 	protected SyslogType getSyslog() {
 		return amt.getSyslog();
 	}
 
 	/**
 	 * Only to be used by {@link CustomXML} to get the custom object
 	 *
 	 * @return	the ApplicationMetadataType's custom object as a String
 	 */
 	protected String getCustom() {
 		return (String) amt.getCustom();
 	}
 
 	/**
 	 * Enum for the different types that can be added to the {@link TransformType}'s xorOrAES128OrAES192 list.
 	 *
 	 */
 	public enum XorAesType {
 		AES128CBC, AES192CBC, AES256CBC, XorECB
 	}
 
 	/**
 	 * Creates a  JAXBElement of the specified type to add to the {@link TransformType}'s xorOrAES128OrAES192 list.
 	 *
 	 * @param iv	the initialization vector byte array
 	 * @param key	the key byte array
 	 * @param type	an XorAesType that is to be created
 	 * @return		a JAXBElement of the given type
 	 * @throws Exception
 	 */
 	public JAXBElement createXorAesType(byte[] iv, byte[] key, XorAesType type) throws Exception {
 
 		if(key == null || type == null) {
 			throw new JALException("key and type are required");
 		}
 
 		switch (type) {
 			case AES128CBC:
 				if(iv == null) {
 					throw new JALException("iv is required for type AES128CBC");
 				}
 				if(iv.length != 16 || key.length != 16) {
 					throw new JALException("Type AES128CBC must have key and iv of length 16.");
 				}
 				AES128CBCType aes128 = of.createAES128CBCType();
 				aes128.setIV(iv); // 16
 				aes128.setKey(key); // 16
 				return of.createAES128(aes128);
 
 			case AES192CBC:
 				if(iv == null) {
 					throw new JALException("iv is required for type AES192CBC");
 				}
 				if(iv.length != 16 || key.length != 24) {
 					throw new JALException("Type AES192CBC must have a key of length 24 and iv of length 16.");
 				}
 				AES192CBCType aes192 = of.createAES192CBCType();
 				aes192.setIV(iv); // 16
 				aes192.setKey(key); // 24
 				return of.createAES192(aes192);
 
 			case AES256CBC:
 				if(iv == null) {
 					throw new JALException("iv is required for type AES256CBC");
 				}
 				if(iv.length != 16 || key.length != 32) {
 					throw new JALException("Type AES256CBC must have a key of length 32 and iv of length 16.");
 				}
 				AES256CBCType aes256 = of.createAES256CBCType();
 				aes256.setIV(iv); // 16
 				aes256.setKey(key); // 32
 				return of.createAES256(aes256);
 
 			case XorECB:
 				if(key.length != 4) {
 					throw new JALException("Type XorECB must have a key of length 4.");
 				}
 				XorECBType xorEcb = of.createXorECBType();
 				xorEcb.setKey(key); // 4
 				return of.createXOR(xorEcb);
 		}
 		return null;
 	}
 
 	/**
 	 * Should be overridden in subclasses to set the given params before creating the xml.
 	 * Subclasses should still call this method to generate a jid.
 	 *
 	 * @param hostName			the name of the host
 	 * @param applicationName	the name of the application
 	 */
 	public void prepareSend(String hostName, String applicationName) {
 		// To be overridden in subclasses if necessary
 		UUID jid = UUID.randomUUID();
 		amt.setJID("UUID-"+jid.toString());
 	}
 
 }

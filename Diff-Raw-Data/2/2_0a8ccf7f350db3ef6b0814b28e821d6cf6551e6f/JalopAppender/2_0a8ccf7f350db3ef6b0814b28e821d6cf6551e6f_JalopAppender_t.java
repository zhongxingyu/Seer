 /*
  * Source code in 3rd-party is licensed and owned by their respective
  * copyright holders.
  *
  * All other source code is copyright Tresys Technology and licensed as below.
  *
  * Copyright (c) 2012 Tresys Technology LLC, Columbia, Maryland, USA
  *
  * This software was developed by Tresys Technology LLC
  * with U.S. Government sponsorship.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.tresys.jalop.producer;
 
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.InputStream;
 import java.math.BigInteger;
 import java.security.KeyFactory;
 import java.security.PrivateKey;
 import java.security.PublicKey;
 import java.security.cert.CertificateFactory;
 import java.security.cert.X509Certificate;
 import java.security.spec.KeySpec;
 import java.security.spec.PKCS8EncodedKeySpec;
 import java.security.spec.X509EncodedKeySpec;
 import java.util.GregorianCalendar;
 import java.util.Set;
 
 import javax.xml.datatype.DatatypeConfigurationException;
 import javax.xml.datatype.DatatypeFactory;
 
 import org.apache.log4j.AppenderSkeleton;
 import org.apache.log4j.spi.LocationInfo;
 import org.apache.log4j.spi.LoggingEvent;
 
 import com.tresys.jalop.schemas.mil.dod.jalop_1_0.applicationmetadatatypes.ApplicationMetadataType;
 import com.tresys.jalop.schemas.mil.dod.jalop_1_0.applicationmetadatatypes.LoggerSeverityType;
 import com.tresys.jalop.schemas.mil.dod.jalop_1_0.applicationmetadatatypes.LoggerType;
 import com.tresys.jalop.schemas.mil.dod.jalop_1_0.applicationmetadatatypes.StackFrameType;
 
 /**
  * JalopAppender submits logs to the JALoP local store.
  */
 public class JalopAppender extends AppenderSkeleton {
 
 	private String path;
 	private String hostName;
 	private String appName;
 	private String publicKeyPath;
 	private String privateKeyPath;
 	private String certPath;
 	private boolean useLocation;
 
 	private static final String LOG4J = "LOG4J";
 
 	public JalopAppender() {
 		useLocation = true;
 	}
 
 	/**
 	 * Does nothing.
 	 */
 	public void activateOptions() {
 	}
 
 	/**
 	 * This method is where logs get sent to the local store
 	 */
 	public void append(LoggingEvent event) {
 
 		ApplicationMetadataXML xml = createLoggerMetadata(event);
 		Producer producer;
 		try {
 			producer = createProducer(xml, path, hostName, appName,
 					privateKeyPath, publicKeyPath, certPath);
 
 			producer.jalpLog((String) null);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Close this appender instance.
 	 */
 	public synchronized void close() {
 		if (this.closed)
 			return;
 		this.closed = true;
 	}
 
 	/*
 	 * This method creates and fills out a Producer with the given data
 	 */
 	private Producer createProducer(ApplicationMetadataXML xml,
 			String socketPath, String hostname, String appname,
 			String privateKeyPath, String publicKeyPath, String certPath)
 			throws Exception {
 
 		Producer producer = new Producer(xml);
 		producer.setSocketFile(socketPath);
 		producer.setApplicationName(appname);
 		producer.setHostName(hostname);
 
 		if (privateKeyPath != null && !"".equals(privateKeyPath)) {
 
 			File privateKeyFile = new File(privateKeyPath);
 			DataInputStream privateDis = new DataInputStream(
 					new FileInputStream(privateKeyFile));
 			byte[] privateKeyBytes = new byte[(int) privateKeyFile.length()];
 			privateDis.readFully(privateKeyBytes);
 			privateDis.close();
 
 			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
 			KeySpec privateKs = new PKCS8EncodedKeySpec(privateKeyBytes);
 			PrivateKey privateKey = keyFactory.generatePrivate(privateKs);
 
 			File publicKeyFile = new File(publicKeyPath);
 			DataInputStream publicDis = new DataInputStream(
 					new FileInputStream(publicKeyFile));
 			byte[] publicKeyBytes = new byte[(int) publicKeyFile.length()];
 			publicDis.readFully(publicKeyBytes);
 			publicDis.close();
 
 			KeySpec publicKs = new X509EncodedKeySpec(publicKeyBytes);
 			PublicKey publicKey = keyFactory.generatePublic(publicKs);
 
 			producer.setPrivateKey(privateKey);
 			producer.setPublicKey(publicKey);
 		}
 
 		if (certPath != null && !"".equals(certPath)) {
 			InputStream inputStream = new FileInputStream(certPath);
 			CertificateFactory cf = CertificateFactory.getInstance("X.509");
 			X509Certificate cert = (X509Certificate) cf
 					.generateCertificate(inputStream);
 			inputStream.close();
 			producer.setCertificate(cert);
 		}
 
 		return producer;
 	}
 
 	public boolean requiresLayout() {
 		return false;
 	}
 
 	/*
 	 * This method creates ApplicationMetadataXML from a LoggingEvent
 	 */
 	private ApplicationMetadataXML createLoggerMetadata(LoggingEvent event) {
 
 		ApplicationMetadataType amt = new ApplicationMetadataType();
 		LoggerType lt = new LoggerType();
 
 		// ApplicationName
 		lt.setApplicationName(appName);
 
 		// HostName
 		lt.setHostname(hostName);
 
 		// Location Info
 		if (useLocation) {
 			LocationInfo loInfo = event.getLocationInformation();
 			LoggerType.Location ltl = new LoggerType.Location();
 			StackFrameType stackFrame = new StackFrameType();
 			// stackFrame.setCallerName(value);
 			stackFrame.setClassName(loInfo.getClassName());
 			// stackFrame.setDepth(value);
 			stackFrame.setFileName(loInfo.getFileName());
 			try {
 				stackFrame.setLineNumber(BigInteger.valueOf(Long.valueOf(loInfo
 						.getLineNumber())));
 			} catch (NullPointerException e1) {
 				// this is optional. Fall through
			} catch (NumberFormatException e1) {
				// this is optional. Fall through
 			}
 
 			ltl.getStackFrame().add(stackFrame);
 			lt.setLocation(ltl);
 		}
 
 		// Logger Name
 		lt.setLoggerName(LOG4J);
 
 		// get mapped diagnostic context
 		Set<String> temp = event.getPropertyKeySet();
 		String mdc = null;
 		try {
 			mdc = (String) event.getMDC((String) temp.toArray()[0]);
 		} catch (NullPointerException e) {
 			// this is optional. Fall though
 		}
 		lt.setMappedDiagnosticContext(mdc);
 
 		// Message
 		lt.setMessage((String) event.getMessage());
 
 		// NDC
 		lt.setNestedDiagnosticContext(event.getNDC());
 
 		// Set Severity Type
 		LoggerSeverityType lst = new LoggerSeverityType();
 		try {
 			lst.setName(event.getLevel().toString());
 			lst.setValue(BigInteger.valueOf(event.getLevel().toInt()));
 		} catch (NullPointerException e) {
 			// These are optional. Fall though
 		}
 		lt.setSeverity(lst);
 
 		// Thread ID
 		lt.setThreadID(event.getThreadName());
 
 		// Timestamp
 		GregorianCalendar calendar = new GregorianCalendar();
 		calendar.setTimeInMillis(event.getTimeStamp());
 		try {
 			lt.setTimestamp(DatatypeFactory.newInstance()
 					.newXMLGregorianCalendar(calendar));
 		} catch (DatatypeConfigurationException e1) {
 			// timestamp is optional, fall though
 		}
 
 		amt.setLogger(lt);
 
 		// Create ApplicationMetadataXML with filled out ApplicationMetadataType
 		ApplicationMetadataXML xml = null;
 		try {
 			xml = new LoggerXML(amt.getLogger());
 		} catch (Exception e) {
 			// This is optional, fall though
 		}
 		xml.setEventId(amt.getEventID());
 
 		return xml;
 	}
 
 	public void setPath(String path) {
 		this.path = path;
 	}
 
 	public String getPath() {
 		return path;
 	}
 
 	public void setHostName(String hostname) {
 		this.hostName = hostname;
 	}
 
 	public String getHostName() {
 		return hostName;
 	}
 
 	public void setAppName(String appName) {
 		this.appName = appName;
 	}
 
 	public String getAppName() {
 		return appName;
 	}
 
 	public void setPrivateKeyPath(String privateKeyPath) {
 		this.privateKeyPath = privateKeyPath;
 	}
 
 	public String getPrivateKeyPath() {
 		return privateKeyPath;
 	}
 
 	public void setPublicKeyPath(String publicKeyPath) {
 		this.publicKeyPath = publicKeyPath;
 	}
 
 	public String getPublicKeyPath() {
 		return publicKeyPath;
 	}
 
 	public void setCertPath(String certPath) {
 		this.certPath = certPath;
 	}
 
 	public String getCertPath() {
 		return certPath;
 	}
 
 	public void setUseLocation(boolean useLocation) {
 		this.useLocation = useLocation;
 	}
 
 	public boolean getUseLocation() {
 		return useLocation;
 	}
 
 }

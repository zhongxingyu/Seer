 /*
  * JBoss, Home of Professional Open Source.
  * Copyright 2012, Red Hat Middleware LLC, and individual contributors
  * as indicated by the @author tags. See the copyright.txt file in the
  * distribution for a full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package org.jboss.jca.adapters.sap.impl;
 
 import java.io.PrintWriter;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import javax.resource.ResourceException;
 import javax.resource.spi.ConnectionManager;
 import javax.resource.spi.ConnectionRequestInfo;
 import javax.resource.spi.ManagedConnection;
 import javax.resource.spi.ManagedConnectionFactory;
 import javax.resource.spi.ResourceAdapter;
 import javax.resource.spi.ResourceAdapterAssociation;
 import javax.resource.spi.security.PasswordCredential;
 import javax.security.auth.Subject;
 
 import org.jboss.jca.adapters.sap.cci.JBossSAPConnectionSpec;
 
 import com.sap.conn.jco.ext.DestinationDataProvider;
 
 /**
  * Implements the {@link ManagedConnectionFactory } and {@link ResourceAdapterAssociation } interfaces for the JBoss SAP
  * JCA Connector.
  * 
  * @author William Collins
  * 
  * @version $Id: 311164c81e43fe42ff1fb2ef00fe3c25cd624209 $
  */
 public class ManagedConnectionFactoryImpl implements ManagedConnectionFactory, ResourceAdapterAssociation {
 
 	/** 
 	 * The serial version UID 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	/** 
 	 * The resource adapter 
 	 */
 	private ResourceAdapterImpl ra;
 
 	/** 
 	 * The logwriter set by application server.
 	 */
 	private PrintWriter logwriter;
 
 	/** 
 	 * Default connection configuration properties.
 	 * 
 	 * This property set is populated by the application server with configuration properties found in the deployment
 	 * descriptor of the resource adapter for this manage connection factory class. The application server populates the 
 	 * configuration set by calling the property setters of this class. 
 	 */
 	private final JBossSAPConnectionSpec defaultConnectionRequestInfo = new JBossSAPConnectionSpec();
 
 	/**
 	 * The set of active managed connection currently managed by this factory.
 	 */
 	private final Set<ManagedConnectionImpl> connections = new HashSet<ManagedConnectionImpl>();
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public Object createConnectionFactory(ConnectionManager cxManager) throws ResourceException {
 		return new ConnectionFactoryImpl(this, cxManager);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public Object createConnectionFactory() throws ResourceException {
 		throw new ResourceException("This resource adapter doesn't support non-managed environments");
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public ManagedConnection createManagedConnection(Subject subject, ConnectionRequestInfo cxRequestInfo)
 			throws ResourceException {
 
 		// validate connection request info type
 		if (cxRequestInfo != null && !(cxRequestInfo instanceof JBossSAPConnectionSpec))
 			throw new ResourceException("jboss-sap-managed-connection-factory-invalid-connection-request-info-type");
 
 		// merge client connection request info with defaults
 		JBossSAPConnectionSpec cri = new JBossSAPConnectionSpec(defaultConnectionRequestInfo);
 		if (cxRequestInfo != null)
 			cri.addProperties((JBossSAPConnectionSpec) cxRequestInfo);
 
 		// use credentials in non-null subject.
 		if (subject != null) {
 			boolean foundCredential = false;
 			for (PasswordCredential credential : subject.getPrivateCredentials(PasswordCredential.class)) {
 				if (credential.getManagedConnectionFactory().equals(this)) {
 					cri.put(DestinationDataProvider.JCO_USER, credential.getUserName());
 					cri.put(DestinationDataProvider.JCO_PASSWD, new String(credential.getPassword()));
 					foundCredential = true;
 					break;
 				}
 			}
 			if (!foundCredential)
 				// Did not find sufficient credentials in subject.
 				throw new SecurityException("jboss-sap-managed-connection-factory-insufficient-credentials");
 		}
 
 		return new ManagedConnectionImpl(this, cri);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@SuppressWarnings("rawtypes")
 	public ManagedConnection matchManagedConnections(Set connectionSet, Subject subject,
 			ConnectionRequestInfo cxRequestInfo) throws ResourceException {
 		
 		ManagedConnection result = null;
 
 		// Use credentials in non-null subject for matching.
 		PasswordCredential subjectCredential = null;
 		if (subject != null) {
 			for (PasswordCredential credential : subject.getPrivateCredentials(PasswordCredential.class)) {
				ManagedConnectionFactory credentialManagedConnectionFactory = credential.getManagedConnectionFactory();
				if (credentialManagedConnectionFactory !=null && credentialManagedConnectionFactory.equals(this)) {
 					subjectCredential =  credential;
 					break;
 				}
 			}
 			if (subjectCredential == null)
 				// Did not find sufficient credentials in passed subject for this managed connection factory: create new connection.
 				return null;
 		}
 		
 		// If the application server does not provide connection request properties to match,
 		// search the connection set for a managed connection that matches the default connection properties.
 		if (cxRequestInfo == null)
 			cxRequestInfo = defaultConnectionRequestInfo;
 
 		// Search through the connection set for a managed connection that matches the passed credentials and connection request properties. 
 		Iterator it = connectionSet.iterator();
 		searchConnectionSet: while (result == null && it.hasNext()) {
 			ManagedConnection mc = (ManagedConnection) it.next();
 			ManagedConnectionImpl candidateConnection = (ManagedConnectionImpl) mc;
 
 			// Validate passed subject credentials match those of the connection.
 			if (subjectCredential != null) {
 				
				String candidateConnectionUserName = candidateConnection.getProperties().getUserName();
				String candidateConnectionPassword = candidateConnection.getProperties().getPassword();
				String subjectCredentialUserName = subjectCredential.getUserName();
				String subjectCredentialPassword = new String(subjectCredential.getPassword());
				
				if (candidateConnectionUserName == null ? subjectCredentialUserName != null : !candidateConnectionUserName.equals(subjectCredentialUserName)) {
 					continue searchConnectionSet;
				} else if (candidateConnectionPassword == null ? subjectCredentialPassword != null : !candidateConnectionPassword.equals(subjectCredentialPassword)) {
 					continue searchConnectionSet;
 				}
 				// Subject credentials match.
 			}
 
 			// Validate passed connection request info match those of the connection.
 			// NB: the set of passed connection request properties need only be a subset of managed connection's set of properties to match.  
 			if (cxRequestInfo != null) {
 				if (!(cxRequestInfo instanceof JBossSAPConnectionSpec))
 					continue searchConnectionSet;
 				JBossSAPConnectionSpec jCxRequestInfo = (JBossSAPConnectionSpec) cxRequestInfo;
 				searchConnectionRequestProperties: for (Entry<Object, Object> entry : jCxRequestInfo.entrySet()) {

 					if (subject != null
 							&& (entry.getKey().equals(DestinationDataProvider.JCO_USER) || entry.getKey().equals(
 									DestinationDataProvider.JCO_PASSWD)))
 						// Already checked managed connection's credentials against subject credentials which override
 						// credentials in connection request info.
 						continue searchConnectionRequestProperties;
					
					Object candidateConnectionPropertyValue = candidateConnection.getProperties().get(entry.getKey());
					Object cxRequestPropertyValue = entry.getValue();
					
					if (candidateConnectionPropertyValue == null ? cxRequestPropertyValue != null
							: !candidateConnectionPropertyValue.equals(cxRequestPropertyValue))
 						continue searchConnectionSet;
 				}
 				// All connection request properties match.
 			}
 
 			// Found a managed connection that matches.
 			result = candidateConnection;
 			break;
 		}
 
 		return result;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public PrintWriter getLogWriter() throws ResourceException {
 		return logwriter;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void setLogWriter(PrintWriter out) throws ResourceException {
 		logwriter = out;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public ResourceAdapterImpl getResourceAdapter() {
 		return ra;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void setResourceAdapter(ResourceAdapter ra) {
 		if (!(ra instanceof ResourceAdapterImpl))
 			throw new IllegalArgumentException(
 					"managed-connection-factory-impl-set-resouce-adapter-invalid-resource-adapter");
 		this.ra = (ResourceAdapterImpl) ra;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public int hashCode() {
 		return defaultConnectionRequestInfo.hashCode();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public boolean equals(Object other) {
 		if (other == null)
 			return false;
 		if (other == this)
 			return true;
 		if (!(other instanceof ManagedConnectionFactoryImpl))
 			return false;
 		ManagedConnectionFactoryImpl obj = (ManagedConnectionFactoryImpl) other;
 		return obj.defaultConnectionRequestInfo.equals(defaultConnectionRequestInfo);
 	}
 
 	/*
 	 * Destination Authentication Information
 	 */
 
 	/**
 	 * Authentication type used by the destination. Known types are
 	 * 
 	 * CONFIGURED_USER - the destination configured for the specified user only. All connections opened via this
 	 * destination belongs to the same user. This value is used in default case, if this property is missing.
 	 * 
 	 * CURRENT_USER - the connection created using this destination belongs to the current user. Before the connection
 	 * is opened the runtime check the property "jco.client.current_user" in order to get the current user name.
 	 * Note:This type is supported in SAP NetWeaver AS only
 	 * 
 	 * Note:This property is optional, default value is CONFIGURED_USER
 	 * 
 	 * @return Authentication type used by the destination. Known types are
 	 */
 	public String getAuthType() {
 
 		return defaultConnectionRequestInfo.getProperty(DestinationDataProvider.JCO_AUTH_TYPE);
 	}
 
 	/**
 	 * Sets authentication type used by the destination.
 	 * 
 	 * @param authType
 	 *            - authentication type used by the destination.
 	 */
 	public void setAuthType(String authType) {
 
 		defaultConnectionRequestInfo.setProperty(DestinationDataProvider.JCO_AUTH_TYPE, authType);
 	}
 
 	/**
 	 * User identity which is used for logon to the ABAP AS.
 	 * 
 	 * Used by the JCo runtime, if the destination configuration uses SSO/assertion ticket, certificate, "current" user
 	 * or SNC environment for authentication. The user id is mandatory, if neither user not user alias is set. This id
 	 * will never be sent to SAP backend, it will be used by the JCo runtime locally.
 	 * 
 	 * @return User identity which is used for logon to the ABAP AS.
 	 */
 	public String getUserId() {
 
 		return defaultConnectionRequestInfo.getProperty(DestinationDataProvider.JCO_USER_ID);
 	}
 
 	/**
 	 * Set user identity which is used for logon to the ABAP AS.
 	 * 
 	 * @param userId
 	 *            - User identity which is used for logon to the ABAP AS.
 	 */
 	public void setUserId(String userId) {
 
 		defaultConnectionRequestInfo.setProperty(DestinationDataProvider.JCO_USER_ID, userId);
 	}
 
 	/*
 	 * User Logon Properties
 	 */
 
 	/**
 	 * SAP client, mandatory logon parameter.
 	 * 
 	 * @return SAP client.
 	 */
 	public String getClient() {
 
 		return defaultConnectionRequestInfo.getProperty(DestinationDataProvider.JCO_CLIENT);
 	}
 
 	/**
 	 * Set SAP client.
 	 * 
 	 * @param client
 	 *            - SAP client.
 	 */
 	public void setClient(String client) {
 
 		defaultConnectionRequestInfo.setProperty(DestinationDataProvider.JCO_CLIENT, client);
 	}
 
 	/**
 	 * Logon user, logon parameter for password based authentication.
 	 * 
 	 * @return Logon user, logon parameter for password based authentication.
 	 */
 	public String getUser() {
 
 		return defaultConnectionRequestInfo.getProperty(DestinationDataProvider.JCO_USER);
 	}
 
 	/**
 	 * Set logon user, logon parameter for password based authentication.
 	 * 
 	 * @param user
 	 */
 	public void setUser(String user) {
 
 		defaultConnectionRequestInfo.setProperty(DestinationDataProvider.JCO_USER, user);
 	}
 
 	/**
 	 * Logon user alias, can be used instead of logon user.
 	 * 
 	 * @return Logon user alias.
 	 */
 	public String getAliasUser() {
 
 		return defaultConnectionRequestInfo.getProperty(DestinationDataProvider.JCO_USER);
 	}
 
 	/**
 	 * Set logon user alias, can be used instead of logon user.
 	 * 
 	 * @param user
 	 *            - logon user alias.
 	 */
 	public void setAliasUser(String user) {
 
 		defaultConnectionRequestInfo.setProperty(DestinationDataProvider.JCO_ALIAS_USER, user);
 	}
 
 	/**
 	 * Logon password, logon parameter for password based authentication.
 	 * 
 	 * @return Logon password, logon parameter for password based authentication.
 	 */
 	public String getPasswd() {
 
 		return defaultConnectionRequestInfo.getProperty(DestinationDataProvider.JCO_PASSWD);
 	}
 
 	/**
 	 * Set logon password, logon parameter for password based authentication.
 	 * 
 	 * @param passwd
 	 *            - Logon password, logon parameter for password based authentication.
 	 */
 	public void setPasswd(String passwd) {
 
 		defaultConnectionRequestInfo.setProperty(DestinationDataProvider.JCO_PASSWD, passwd);
 	}
 
 	/**
 	 * Logon language, if not defined the default user language is used.
 	 * 
 	 * @return Logon language, if not defined the default user language is used.
 	 */
 	public String getLang() {
 
 		return defaultConnectionRequestInfo.getProperty(DestinationDataProvider.JCO_LANG);
 	}
 
 	/**
 	 * Sets logon language.
 	 * 
 	 * @param lang
 	 *            - Logon language.
 	 */
 	public void setLang(String lang) {
 
 		defaultConnectionRequestInfo.setProperty(DestinationDataProvider.JCO_LANG, lang);
 	}
 
 	/**
 	 * The SAP Cookie Version 2 used as logon ticket for SSO based authentication.
 	 * 
 	 * @return The SAP Cookie Version 2 used as logon ticket for SSO based authentication.
 	 */
 	public String getMysapsso2() {
 
 		return defaultConnectionRequestInfo.getProperty(DestinationDataProvider.JCO_MYSAPSSO2);
 	}
 
 	/**
 	 * Set the SAP Cookie Version 2 used as logon ticket for SSO based authentication.
 	 * 
 	 * @param mysapsso2
 	 *            - The SAP Cookie Version 2 used as logon ticket for SSO based authentication.
 	 */
 	public void setMysapsso2(String mysapsso2) {
 
 		defaultConnectionRequestInfo.setProperty(DestinationDataProvider.JCO_MYSAPSSO2, mysapsso2);
 	}
 
 	/**
 	 * The specified X509 certificate used for certificate based authentication
 	 * 
 	 * @return The specified X509 certificate used for certificate based authentication
 	 */
 	public String getX509cert() {
 
 		return defaultConnectionRequestInfo.getProperty(DestinationDataProvider.JCO_X509CERT);
 	}
 
 	/**
 	 * Set the specified X509 certificate used for certificate based authentication
 	 * 
 	 * @param x509cert
 	 *            - The specified X509 certificate used for certificate based authentication
 	 */
 	public void setX509cert(String x509cert) {
 
 		defaultConnectionRequestInfo.setProperty(DestinationDataProvider.JCO_X509CERT, x509cert);
 	}
 
 	/**
 	 * Additional logon parameter to define the codepage type of the SAP System,
 	 * 
 	 * 1 - non unicode, 2 - unicode enabled.
 	 * 
 	 * Used in special cases only
 	 * 
 	 * @return Additional logon parameter to define the codepage type of the SAP System.
 	 */
 	public String getPcs() {
 
 		return defaultConnectionRequestInfo.getProperty(DestinationDataProvider.JCO_PCS);
 	}
 
 	/**
 	 * Set additional logon parameter to define the codepage type of the SAP System
 	 * 
 	 * @param pcs
 	 *            - Additional logon parameter to define the codepage type of the SAP System
 	 */
 	public void setPcs(String pcs) {
 
 		defaultConnectionRequestInfo.setProperty(DestinationDataProvider.JCO_PCS, pcs);
 	}
 
 	/**
 	 * Type of remote host.
 	 * 
 	 * The type will be recognized automatically and should not be set manually.
 	 * 
 	 * @return Type of remote host.
 	 */
 	public String getType() {
 
 		return defaultConnectionRequestInfo.getProperty(DestinationDataProvider.JCO_TYPE);
 	}
 
 	/**
 	 * Set type of remote host.
 	 * 
 	 * @param type
 	 *            - Type of remote host.
 	 */
 	public void setType(String type) {
 
 		defaultConnectionRequestInfo.setProperty(DestinationDataProvider.JCO_TYPE, type);
 	}
 
 	/*
 	 * Connection Configuration
 	 */
 
 	/**
 	 * SAP Router string for connection to systems behind a SAP Router.
 	 * 
 	 * SAP Router string contains the chain of SAP Routers and its port numbers and has the form:
 	 * 
 	 * (/H/<host>[/S/<port>])+
 	 * 
 	 * @return SAP Router string for connection to systems behind a SAP Router.
 	 */
 	public String getSaprouter() {
 
 		return defaultConnectionRequestInfo.getProperty(DestinationDataProvider.JCO_SAPROUTER);
 	}
 
 	/**
 	 * Set SAP Router string for connection to systems behind a SAP Router.
 	 * 
 	 * @param saprouter
 	 *            - SAP Router string for connection to systems behind a SAP Router.
 	 */
 	public void setSaprouter(String saprouter) {
 
 		defaultConnectionRequestInfo.setProperty(DestinationDataProvider.JCO_SAPROUTER, saprouter);
 	}
 
 	/**
 	 * System number of the SAP ABAP application server, mandatory for a direct connection.
 	 * 
 	 * @return - System number of the SAP ABAP application server, mandatory for a direct connection.
 	 */
 	public String getSysnr() {
 
 		return defaultConnectionRequestInfo.getProperty(DestinationDataProvider.JCO_SYSNR);
 	}
 
 	/**
 	 * Set system number of the SAP ABAP application server.
 	 * 
 	 * @param sysnr
 	 *            - System number of the SAP ABAP application server.
 	 */
 	public void setSysnr(String sysnr) {
 
 		defaultConnectionRequestInfo.setProperty(DestinationDataProvider.JCO_SYSNR, sysnr);
 	}
 
 	/**
 	 * SAP message server, mandatory property for a load balancing connection.
 	 * 
 	 * @return SAP message server.
 	 */
 	public String getAshost() {
 
 		return defaultConnectionRequestInfo.getProperty(DestinationDataProvider.JCO_ASHOST);
 	}
 
 	/**
 	 * Set SAP message server, mandatory property for a load balancing connection.
 	 * 
 	 * @param ashost
 	 *            - SAP message server.
 	 */
 	public void setAshost(String ashost) {
 
 		defaultConnectionRequestInfo.setProperty(DestinationDataProvider.JCO_ASHOST, ashost);
 	}
 
 	/**
 	 * SAP message server port, optional property for a load balancing connection.
 	 * 
 	 * In order to resolve the service names sapmsXXX a lookup in etc/services is performed by the network layer of the
 	 * operating system. If using port numbers instead of symbolic service names, no lookups are performed and no
 	 * additional entries are needed.
 	 * 
 	 * @return SAP message server port.
 	 */
 	public String getMshost() {
 
 		return defaultConnectionRequestInfo.getProperty(DestinationDataProvider.JCO_MSHOST);
 	}
 
 	/**
 	 * Set SAP message server port, optional property for a load balancing connection.
 	 * 
 	 * @param mshost
 	 *            - SAP message server port.
 	 */
 	public void setMshost(String mshost) {
 
 		defaultConnectionRequestInfo.setProperty(DestinationDataProvider.JCO_MSHOST, mshost);
 	}
 
 	/**
 	 * SAP message server port, optional property for a load balancing connection.
 	 * 
 	 * In order to resolve the service names sapmsXXX a lookup in etc/services is performed by the network layer of the
 	 * operating system. If using port numbers instead of symbolic service names, no lookups are performed and no
 	 * additional entries are needed.
 	 * 
 	 * @return SAP message server port.
 	 */
 	public String getMsserv() {
 
 		return defaultConnectionRequestInfo.getProperty(DestinationDataProvider.JCO_MSSERV);
 	}
 
 	/**
 	 * Set SAP message server port
 	 * 
 	 * @param msserv
 	 *            - SAP message server port -
 	 */
 	public void setMsserv(String msserv) {
 
 		defaultConnectionRequestInfo.setProperty(DestinationDataProvider.JCO_MSSERV, msserv);
 	}
 
 	/**
 	 * Allows specifying a concrete gateway, which should be used for establishing the connection to an application
 	 * server.
 	 * 
 	 * If not specified the gateway on the application server is used.
 	 * 
 	 * @return Gateway used for establishing the connection to an application server.
 	 */
 	public String getGwhost() {
 
 		return defaultConnectionRequestInfo.getProperty(DestinationDataProvider.JCO_GWHOST);
 	}
 
 	/**
 	 * Set Gateway used for establishing the connection to an application server.
 	 * 
 	 * @param gwhost
 	 *            - Gateway used for establishing the connection to an application server.
 	 */
 	public void setGwhost(String gwhost) {
 
 		defaultConnectionRequestInfo.setProperty(DestinationDataProvider.JCO_GWHOST, gwhost);
 	}
 
 	/**
 	 * Gateway server port.
 	 * 
 	 * Should be set, when using JCO_GWHOST.
 	 * 
 	 * Allows specifying the port used on that gateway. If not specified the port of the gateway on the application
 	 * server is used.
 	 * 
 	 * In order to resolve the service names sapgwXXX a lookup in etc/services is performed by the network layer of the
 	 * operating system. If using port numbers instead of symbolic service names, no lookups are performed and no
 	 * additional entries are needed.
 	 * 
 	 * @return Gateway server port.
 	 */
 	public String getGwserv() {
 
 		return defaultConnectionRequestInfo.getProperty(DestinationDataProvider.JCO_GWSERV);
 	}
 
 	/**
 	 * Set Gateway server port.
 	 * 
 	 * @param gwserv
 	 *            - Gateway server port. -
 	 */
 	public void setGwserv(String gwserv) {
 
 		defaultConnectionRequestInfo.setProperty(DestinationDataProvider.JCO_GWSERV, gwserv);
 	}
 
 	/**
 	 * Host of external server.
 	 * 
 	 * Not supported in all runtime environments.
 	 * 
 	 * @return Host of external server.
 	 */
 	public String getTphost() {
 
 		return defaultConnectionRequestInfo.getProperty(DestinationDataProvider.JCO_TPHOST);
 	}
 
 	/**
 	 * Set host of external server.
 	 * 
 	 * @param tphost
 	 *            - Host of external server.
 	 */
 	public void setTphost(String tphost) {
 
 		defaultConnectionRequestInfo.setProperty(DestinationDataProvider.JCO_TPHOST, tphost);
 	}
 
 	/**
 	 * Program ID of external server.
 	 * 
 	 * Not supported in all runtime environments.
 	 * 
 	 * @return Program ID of external server.
 	 */
 	public String getTpname() {
 
 		return defaultConnectionRequestInfo.getProperty(DestinationDataProvider.JCO_TPNAME);
 	}
 
 	/**
 	 * Set program ID of external server.
 	 * 
 	 * @param tpname
 	 *            - Program ID of external server.
 	 */
 	public void setTpname(String tpname) {
 
 		defaultConnectionRequestInfo.setProperty(DestinationDataProvider.JCO_TPNAME, tpname);
 	}
 
 	/**
 	 * System ID of the SAP system, mandatory property for a load balancing connection.
 	 * 
 	 * @return System ID of the SAP system.
 	 */
 	public String getR3name() {
 
 		return defaultConnectionRequestInfo.getProperty(DestinationDataProvider.JCO_R3NAME);
 	}
 
 	/**
 	 * Set System ID of the SAP system.
 	 * 
 	 * @param r3name
 	 *            - System ID of the SAP system.
 	 */
 	public void setR3name(String r3name) {
 
 		defaultConnectionRequestInfo.setProperty(DestinationDataProvider.JCO_R3NAME, r3name);
 	}
 
 	/**
 	 * Group of SAP application servers, mandatory property for a load balancing connection.
 	 * 
 	 * @return Group of SAP application servers.
 	 */
 	public String getGroup() {
 
 		return defaultConnectionRequestInfo.getProperty(DestinationDataProvider.JCO_GROUP);
 	}
 
 	/**
 	 * Set group of SAP application servers.
 	 * 
 	 * @param group
 	 *            - Group of SAP application servers.
 	 */
 	public void setGroup(String group) {
 
 		defaultConnectionRequestInfo.setProperty(DestinationDataProvider.JCO_GROUP, group);
 	}
 
 	/*
 	 * Trace Configuration
 	 */
 
 	/**
 	 * Enable/disable RFC trace (0 or 1).
 	 * 
 	 * @return trace level of RFC trace (0 or 1).
 	 */
 	public String getTrace() {
 
 		return defaultConnectionRequestInfo.getProperty(DestinationDataProvider.JCO_TRACE);
 	}
 
 	/**
 	 * Trace level of RFC trace
 	 * 
 	 * @param trace
 	 *            - Trace level of RFC trace: Enable/disable RFC trace (0 or 1).
 	 */
 	public void setTrace(String trace) {
 
 		defaultConnectionRequestInfo.setProperty(DestinationDataProvider.JCO_TRACE, trace);
 	}
 
 	/**
 	 * Enable/disable CPIC trace [0..3].
 	 * 
 	 * @return Trace level of CPIC trace [0..3].
 	 */
 	public String getCpicTrace() {
 
 		return defaultConnectionRequestInfo.getProperty(DestinationDataProvider.JCO_CPIC_TRACE);
 	}
 
 	/**
 	 * Set trace level of CPIC trace [0..3].
 	 * 
 	 * @param cpicTrace
 	 *            - Trace level of CPIC trace: [0..3].
 	 */
 	public void setCpicTrace(String cpicTrace) {
 
 		defaultConnectionRequestInfo.setProperty(DestinationDataProvider.JCO_CPIC_TRACE, cpicTrace);
 	}
 
 	/*
 	 * Special Parameters
 	 */
 
 	/**
 	 * Enable/Disable logon check at open time, 1 (enable) or 0 (disable).
 	 * 
 	 * Postpones the authentication until the first call - 1 (enable).
 	 * 
 	 * Used in special cases only.
 	 * 
 	 * @return 1 (enabled) or 0 (disabled).
 	 */
 	public String getLcheck() {
 
 		return defaultConnectionRequestInfo.getProperty(DestinationDataProvider.JCO_LCHECK);
 	}
 
 	/**
 	 * Set whether to Enable/Disable logon check at open time, 1 (enable) or 0 (disable).
 	 * 
 	 * @param lcheck
 	 *            - 1 (enabled) or 0 (disabled).
 	 */
 	public void setLcheck(String lcheck) {
 
 		defaultConnectionRequestInfo.setProperty(DestinationDataProvider.JCO_LCHECK, lcheck);
 	}
 
 	/**
 	 * Start an SAP GUI and associate with the connection. (0 - do not start [default], 1 start GUI, 2 start GUI and
 	 * hide if not used)
 	 * 
 	 * @return 0 - do not start [default], 1 start GUI, 2 start GUI and hide if not used
 	 */
 	public String getUseSapgui() {
 
 		return defaultConnectionRequestInfo.getProperty(DestinationDataProvider.JCO_USE_SAPGUI);
 	}
 
 	/**
 	 * Set whether to start an SAP GUI and associate with the connection. (0 - do not start [default], 1 start GUI, 2
 	 * start GUI and hide if not used)
 	 * 
 	 * @param useSapgui
 	 *            - do not start [default], 1 start GUI, 2 start GUI and hide if not used.
 	 */
 	public void setUseSapgui(String useSapgui) {
 
 		defaultConnectionRequestInfo.setProperty(DestinationDataProvider.JCO_USE_SAPGUI, useSapgui);
 	}
 
 	/**
 	 * Initial codepage in SAP notation.
 	 * 
 	 * Additional logon parameter to define the codepage that will used to convert the logon parameters.
 	 * 
 	 * Used in special cases only.
 	 * 
 	 * @return Initial codepage in SAP notation.
 	 */
 	public String getCodepage() {
 
 		return defaultConnectionRequestInfo.getProperty(DestinationDataProvider.JCO_CODEPAGE);
 	}
 
 	/**
 	 * Set initial codepage in SAP notation.
 	 * 
 	 * @param codepage
 	 *            - Initial codepage in SAP notation.
 	 */
 	public void setCodepage(String codepage) {
 
 		defaultConnectionRequestInfo.setProperty(DestinationDataProvider.JCO_CODEPAGE, codepage);
 	}
 
 	/**
 	 * Get/Don't get a SSO ticket after logon (1 or 0)
 	 * 
 	 * Order a SSO ticket after logon, the obtained ticket is available in the destination attributes.
 	 * 
 	 * @return Get/Don't get a SSO ticket after logon (1 or 0)
 	 */
 	public String getGetsso2() {
 
 		return defaultConnectionRequestInfo.getProperty(DestinationDataProvider.JCO_GETSSO2);
 	}
 
 	/**
 	 * Set whether to Get/Don't get a SSO ticket after logon (1 or 0).
 	 * 
 	 * @param getsso2
 	 *            - Get/Don't get a SSO ticket after logon (1 or 0).
 	 */
 	public void setGetsso2(String getsso2) {
 
 		defaultConnectionRequestInfo.setProperty(DestinationDataProvider.JCO_GETSSO2, getsso2);
 	}
 
 	/**
 	 * Deny usage of initial passwords (0[default] or 1).
 	 * 
 	 * If set to 1, using initial passwords will lead to an exception (default=0).
 	 * 
 	 * @return Deny usage of initial passwords (0[default] or 1)
 	 */
 	public String getDenyInitialPassword() {
 
 		return defaultConnectionRequestInfo.getProperty(DestinationDataProvider.JCO_DENY_INITIAL_PASSWORD);
 	}
 
 	/**
 	 * Set whether to deny usage of initial passwords (0[default] or 1).
 	 * 
 	 * @param denyInitialPassword
 	 *            - whether to deny usage of initial passwords (0[default] or 1).
 	 */
 	public void setDenyInitialPassword(String denyInitialPassword) {
 
 		defaultConnectionRequestInfo
 				.setProperty(DestinationDataProvider.JCO_DENY_INITIAL_PASSWORD, denyInitialPassword);
 	}
 
 	/*
 	 * Destination Pool Configuration
 	 */
 
 	/**
 	 * Maximum number of active connections that can be created for a destination simultaneously.
 	 * 
 	 * A value of 0 allows an unlimited number of active connections, otherwise if the value is less than the value of
 	 * jco.destination.pool_capacity, it will be automatically increased to this value.
 	 * 
 	 * Default setting is the value of jco.destination.pool_capacity, or in case of jco.destination.pool_capacity not
 	 * being specified as well, the default is 0 (unlimited).
 	 * 
 	 * @return Maximum number of active connections that can be created for a destination simultaneously
 	 */
 	public String getPeakLimit() {
 
 		return defaultConnectionRequestInfo.getProperty(DestinationDataProvider.JCO_PEAK_LIMIT);
 	}
 
 	/**
 	 * Set maximum number of active connections that can be created for a destination simultaneously
 	 * 
 	 * A value of 0 allows an unlimited number of active connections, otherwise if the value is less than the value of
 	 * jco.destination.pool_capacity, it will be automatically increased to this value.
 	 * 
 	 * @param peakLimit
 	 *            - Maximum number of active connections that can be created for a destination simultaneously
 	 */
 	public void setPeakLimit(String peakLimit) {
 
 		defaultConnectionRequestInfo.setProperty(DestinationDataProvider.JCO_PEAK_LIMIT, peakLimit);
 	}
 
 	/**
 	 * Maximum number of idle connections kept open by the destination. A value of 0 has the effect that there is no
 	 * connection pooling, i.e. connections will be closed after each request.
 	 * 
 	 * A value of 0 has the effect that there is no connection pooling (default=1)
 	 * 
 	 * @return Maximum number of idle connections kept open by the destination.
 	 */
 	public String getPoolCapacity() {
 
 		return defaultConnectionRequestInfo.getProperty(DestinationDataProvider.JCO_POOL_CAPACITY);
 	}
 
 	/**
 	 * Set maximum number of idle connections kept open by the destination.
 	 * 
 	 * A value of 0 has the effect that there is no connection pooling (default=1)
 	 * 
 	 * @param poolCapacity
 	 *            - Maximum number of idle connections kept open by the destination.
 	 */
 	public void setPoolCapacity(String poolCapacity) {
 
 		defaultConnectionRequestInfo.setProperty(DestinationDataProvider.JCO_POOL_CAPACITY, poolCapacity);
 	}
 
 	/**
 	 * Time in ms after that a free connections hold internally by the destination can be closed.
 	 * 
 	 * @return Time in ms after that a free connections hold internally by the destination can be closed
 	 */
 	public String getExpirationTime() {
 
 		return defaultConnectionRequestInfo.getProperty(DestinationDataProvider.JCO_EXPIRATION_TIME);
 	}
 
 	/**
 	 * Set the time in ms after that a free connections hold internally by the destination can be closed
 	 * 
 	 * @param expirationTime
 	 *            - Time in ms after that a free connections hold internally by the destination can be closed
 	 */
 	public void setExpirationTime(String expirationTime) {
 
 		defaultConnectionRequestInfo.setProperty(DestinationDataProvider.JCO_EXPIRATION_TIME, expirationTime);
 	}
 
 	/**
 	 * Interval in ms with which the timeout checker thread checks the connections in the pool for expiration.
 	 * 
 	 * @return Interval in ms with which the timeout checker thread checks the connections in the pool for expiration.
 	 */
 	public String getExpirationPeriod() {
 
 		return defaultConnectionRequestInfo.getProperty(DestinationDataProvider.JCO_EXPIRATION_PERIOD);
 	}
 
 	/**
 	 * Set interval in ms with which the timeout checker thread checks the connections in the pool for expiration.
 	 * 
 	 * @param expirationPeriod
 	 *            - Interval in ms with which the timeout checker thread checks the connections in the pool for
 	 *            expiration.
 	 */
 	public void setExpirationPeriod(String expirationPeriod) {
 
 		defaultConnectionRequestInfo.setProperty(DestinationDataProvider.JCO_EXPIRATION_PERIOD, expirationPeriod);
 	}
 
 	/**
 	 * Max time in ms to wait for a connection, if the max allowed number of connections is allocated by the application
 	 * SNC configuration
 	 * 
 	 * @return Max time in ms to wait for a connection.
 	 */
 	public String getMaxGetTime() {
 
 		return defaultConnectionRequestInfo.getProperty(DestinationDataProvider.JCO_MAX_GET_TIME);
 	}
 
 	/**
 	 * Set the max time in ms to wait for a connection
 	 * 
 	 * @param maxGetTime
 	 *            - Max time in ms to wait for a connection
 	 */
 	public void setMaxGetTime(String maxGetTime) {
 
 		defaultConnectionRequestInfo.setProperty(DestinationDataProvider.JCO_MAX_GET_TIME, maxGetTime);
 	}
 
 	/*
 	 * SNC Configuration
 	 */
 
 	/**
 	 * Secure network connection (SNC) mode, 0 (off) or 1 (on).
 	 * 
 	 * @return Secure network connection (SNC) mode, 0 (off) or 1 (on).
 	 */
 	public String getSncMode() {
 
 		return defaultConnectionRequestInfo.getProperty(DestinationDataProvider.JCO_SNC_MODE);
 	}
 
 	/**
 	 * Set the secure network connection (SNC) mode, 0 (off) or 1 (on).
 	 * 
 	 * @param sncMode
 	 *            - Secure network connection (SNC) mode, 0 (off) or 1 (on).
 	 */
 	public void setSncMode(String sncMode) {
 
 		defaultConnectionRequestInfo.setProperty(DestinationDataProvider.JCO_SNC_MODE, sncMode);
 	}
 
 	/**
 	 * SNC partner, e.g. p:CN=R3, O=XYZ-INC, C=EN
 	 * 
 	 * @return SNC partner.
 	 */
 	public String getSncPartnername() {
 
 		return defaultConnectionRequestInfo.getProperty(DestinationDataProvider.JCO_SNC_PARTNERNAME);
 	}
 
 	/**
 	 * Set SNC partner.
 	 * 
 	 * e.g. p:CN=R3, O=XYZ-INC, C=EN
 	 * 
 	 * @param sncPartnername
 	 *            - SNC partner.
 	 */
 	public void setSncPartnername(String sncPartnername) {
 
 		defaultConnectionRequestInfo.setProperty(DestinationDataProvider.JCO_SNC_PARTNERNAME, sncPartnername);
 	}
 
 	/**
 	 * SNC level of security, 1 to 9.
 	 * 
 	 * @return SNC level of security, 1 to 9.
 	 */
 	public String getSncQop() {
 
 		return defaultConnectionRequestInfo.getProperty(DestinationDataProvider.JCO_SNC_QOP);
 	}
 
 	/**
 	 * Set SNC level of security, 1 to 9.
 	 * 
 	 * @param sncQop
 	 *            - SNC level of security, 1 to 9.
 	 */
 	public void setSncQop(String sncQop) {
 
 		defaultConnectionRequestInfo.setProperty(DestinationDataProvider.JCO_SNC_QOP, sncQop);
 	}
 
 	/**
 	 * SNC name.
 	 * 
 	 * Overrides default SNC partner.
 	 * 
 	 * @return SNC name.
 	 */
 	public String getSncMyname() {
 
 		return defaultConnectionRequestInfo.getProperty(DestinationDataProvider.JCO_SNC_MYNAME);
 	}
 
 	/**
 	 * Set SNC name.
 	 * 
 	 * @param sncMyname
 	 *            - SNC name.
 	 */
 	public void setSncMyname(String sncMyname) {
 
 		defaultConnectionRequestInfo.setProperty(DestinationDataProvider.JCO_SNC_MYNAME, sncMyname);
 	}
 
 	/**
 	 * Path to library which provides SNC service.
 	 * 
 	 * @return Path to library which provides SNC service
 	 */
 	public String getSncLibrary() {
 
 		return defaultConnectionRequestInfo.getProperty(DestinationDataProvider.JCO_SNC_LIBRARY);
 	}
 
 	/**
 	 * Set path to library which provides SNC service
 	 * 
 	 * @param sncLibrary
 	 *            - Path to library which provides SNC service
 	 */
 	public void setSncLibrary(String sncLibrary) {
 
 		defaultConnectionRequestInfo.setProperty(DestinationDataProvider.JCO_SNC_LIBRARY, sncLibrary);
 	}
 
 	/*
 	 * Repository Configuration
 	 */
 
 	/**
 	 * Specifies which destination should be used as repository, i.e. use this destination's repository.
 	 * 
 	 * @return Destination that should be used as repository
 	 */
 	public String getRepositoryDest() {
 
 		return defaultConnectionRequestInfo.getProperty(DestinationDataProvider.JCO_REPOSITORY_DEST);
 	}
 
 	/**
 	 * Set destination that should be used as repository.
 	 * 
 	 * @param repositoryDest
 	 *            - Destination that should be used as repository
 	 */
 	public void setRepositoryDest(String repositoryDest) {
 
 		defaultConnectionRequestInfo.setProperty(DestinationDataProvider.JCO_REPOSITORY_DEST, repositoryDest);
 	}
 
 	/**
 	 * User to use for repository calls.
 	 * 
 	 * Optional: If repository destination is not set, and this property is set, it will be used as user for repository
 	 * queries. This allows using a different user for repository lookups and restrict the permissions accordingly.
 	 * 
 	 * @return User used for repository calls.
 	 */
 	public String getRepositoryUser() {
 
 		return defaultConnectionRequestInfo.getProperty(DestinationDataProvider.JCO_REPOSITORY_USER);
 	}
 
 	/**
 	 * Set user to use for repository calls.
 	 * 
 	 * Optional: If repository destination is not set, and this property is set, it will be used as user for repository
 	 * queries. This allows using a different user for repository lookups and restrict the permissions accordingly.
 	 * 
 	 * @param repositoryUser
 	 *            - User used for repository calls.
 	 */
 	public void setRepositoryUser(String repositoryUser) {
 
 		defaultConnectionRequestInfo.setProperty(DestinationDataProvider.JCO_REPOSITORY_USER, repositoryUser);
 	}
 
 	/**
 	 * The password for a repository user. Mandatory, if a repository user should be used.
 	 * 
 	 * @return The password for a repository user.
 	 */
 	public String getRepositoryPasswd() {
 
 		return defaultConnectionRequestInfo.getProperty(DestinationDataProvider.JCO_REPOSITORY_PASSWD);
 	}
 
 	/**
 	 * Set the password for a repository user.
 	 * 
 	 * @param repositoryPasswd
 	 *            - The password for a repository user.
 	 */
 	public void setRepositoryPasswd(String repositoryPasswd) {
 
 		defaultConnectionRequestInfo.setProperty(DestinationDataProvider.JCO_REPOSITORY_PASSWD, repositoryPasswd);
 	}
 
 	/**
 	 * If SNC is used for this destination.
 	 * 
 	 * Optional: It is possible to turn it off for repository connections, if this property is set to 0.
 	 * 
 	 * Default setting is the value of jco.client.snc_mode. For special cases only.
 	 * 
 	 * @return If SNC is used for this destination.
 	 */
 	public String getRepositorySnc() {
 
 		return defaultConnectionRequestInfo.getProperty(DestinationDataProvider.JCO_REPOSITORY_SNC);
 	}
 
 	/**
 	 * Set if SNC is used for this destination.
 	 * 
 	 * Optional: It is possible to turn it off for repository connections, if this property is set to 0.
 	 * 
 	 * @param repositorySnc
 	 *            - if SNC is used for this destination.
 	 */
 	public void setRepositorySnc(String repositorySnc) {
 
 		defaultConnectionRequestInfo.setProperty(DestinationDataProvider.JCO_REPOSITORY_SNC, repositorySnc);
 	}
 
 	/**
 	 * Is the usage of RFC_METADATA_GET API enabled, which is providing repository data in one single roundtrip.
 	 * 
 	 * 1 indicates the usage of RFC_METADATA_GET in ABAP System is forced, 0 indicated it is deactivated.
 	 * 
 	 * If the property is not set, the destination will initially do a remote call to check whether RFC_METADATA_GET is
 	 * available. In case it is available, it will use it.
 	 * 
 	 * Note: If the repository is already initialized, for example because it is used by some other destination, this
 	 * property does not have any effect. Generally, this property is related to the ABAP System, and should have the
 	 * same value on all destinations pointing to the same ABAP System.
 	 * 
 	 * @return Is the usage of RFC_METADATA_GET API forced (1) or deactivated (0).
 	 */
 	public String getRepositoryRoundtripOptimization() {
 
 		return defaultConnectionRequestInfo.getProperty(DestinationDataProvider.JCO_REPOSITORY_ROUNDTRIP_OPTIMIZATION);
 	}
 
 	/**
 	 * Enable the usage of RFC_METADATA_GET API, which is providing repository data in one single roundtrip.
 	 * 
 	 * 1 forces the usage of RFC_METADATA_GET in ABAP System, 0 deactivates it.
 	 * 
 	 * If the property is not set, the destination will initially do a remote call to check whether RFC_METADATA_GET is
 	 * available. In case it is available, it will use it.
 	 * 
 	 * Note: If the repository is already initializated, for example because it is used by some other destination, this
 	 * property does not have any effect. Generally, this property is related to the ABAP System, and should have the
 	 * same value on all destinations pointing to the same ABAP System.
 	 * 
 	 * @param repositoryRoundtripOptimization
 	 *            - Force(1)/Deactivate(0) the usage of RFC_METADATA_GET API.
 	 */
 	public void setRepositoryRoundtripOptimization(String repositoryRoundtripOptimization) {
 
 		defaultConnectionRequestInfo.setProperty(DestinationDataProvider.JCO_REPOSITORY_ROUNDTRIP_OPTIMIZATION,
 				repositoryRoundtripOptimization);
 	}
 
 	/**
 	 * Associate the given managed connection with this factory.
 	 *  
 	 * @param connection - The managed connection to be associated.
 	 */
 	void associateConnection(ManagedConnectionImpl connection) {
 		synchronized (connections) {
 			connections.add(connection);
 		}
 	}
 
 	/**
 	 * Dissociate the given managed connection with this factory 
 	 * @param connection
 	 */
 	void dissociateConnection(ManagedConnectionImpl connection) {
 		synchronized (connections) {
 			connections.remove(connection);
 		}
 	}
 }

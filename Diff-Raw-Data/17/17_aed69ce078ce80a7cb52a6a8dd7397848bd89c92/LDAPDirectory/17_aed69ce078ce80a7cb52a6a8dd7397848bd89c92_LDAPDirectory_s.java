 /**
  * 
  * Copyright 2002 NCHELP
  * 
  * Author:		Tim Bornholtz,  Priority Technologies, Inc.
  * 
  * 
  * This code is part of the Meteor system as defined and specified 
  * by the National Council of Higher Education Loan Programs, Inc. 
  * (NCHELP) and the Meteor Sponsors, and developed by Priority 
  * Technologies, Inc. (PTI). 
  *
  * 
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *	
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *	
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  ****************************************************************************/
 
 package org.nchelp.meteor.registry;
 
 import java.io.ByteArrayInputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.security.Security;
 import java.security.cert.CertificateFactory;
 import java.security.cert.X509Certificate;
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Vector;
 
 import javax.naming.Context;
 import javax.naming.NamingEnumeration;
 import javax.naming.NamingException;
 import javax.naming.directory.Attribute;
 import javax.naming.directory.Attributes;
 import javax.naming.directory.DirContext;
 import javax.naming.directory.InitialDirContext;
 import javax.naming.directory.SearchControls;
 import javax.naming.directory.SearchResult;
 
 import org.nchelp.meteor.logging.Logger;
 import org.nchelp.meteor.provider.AuthenticationProvider;
 import org.nchelp.meteor.provider.DataProvider;
 import org.nchelp.meteor.provider.IndexProvider;
 import org.nchelp.meteor.util.Resource;
 import org.nchelp.meteor.util.ResourceFactory;
 import org.nchelp.meteor.util.exception.DirectoryException;
 
 /**
 * This is the wrapper object that will actually communicate
 * with the LDAP server.  For the sake of data consistency, 
 * this should be the <b>only</b> class that reads and writes 
 * to the LDAP server.  All ofthe configuration parameters 
 * for this are in the file directory.properties.
 *  
 * @version   $Revision$ $Date$
 * @since     Meteor1.0
 * 
 */
 
 public class LDAPDirectory implements Directory {
 
 	private final Logger log = Logger.create(this.getClass());
 	private static final String resourcePrefix = "directory.";
 
 	// Make both of these static for performance reasons
 	// We really don't want to create the initial context over and over
 	private static String baseDN;
 	private static DirContext context;
 
 	static {
 		// If we are using SSL, then set up java.net.URL to handle this
 		System.setProperty(
 			"java.protocol.handler.pkgs",
 			"com.sun.net.ssl.internal.www.protocol");
 		Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
 
 	}
 
 	public LDAPDirectory() throws DirectoryException {
 		try {
 			init();
 		} catch (NamingException e) {
 			log.error("Error initializing LDAPDirectory: " + e);
 			throw new DirectoryException(e);
 		}
 
 	}
 	
 	
 
 	private void init() throws NamingException {
 
 		if (context == null) {
 			Resource res =
 				ResourceFactory.createResource("directory.properties");
 
 			String initialContextFactory =
 				res.getProperty(
 					resourcePrefix + "initialcontextfactory",
 					"com.sun.jndi.ldap.LdapCtxFactory");
 			String providerURL =
 				res.getProperty(resourcePrefix + "providerurl");
 			baseDN = res.getProperty(resourcePrefix + "basedn");
 			String authType =
 				res.getProperty(resourcePrefix + "authentication.type");
 			String authUser =
 				res.getProperty(resourcePrefix + "authentication.principal");
 			String authPassword =
 				res.getProperty(resourcePrefix + "authentication.credentials");
 			String protocol = res.getProperty(resourcePrefix + "protocol");
 
 			Hashtable env = new Hashtable();
 			env.put(Context.INITIAL_CONTEXT_FACTORY, initialContextFactory);
 			env.put(Context.PROVIDER_URL, providerURL);
 
 			
 			if ("ssl".equalsIgnoreCase(protocol)) {
 				// This is really the only supported protocol
 				env.put(Context.SECURITY_PROTOCOL, protocol);
 			}
 			else if("custom".equalsIgnoreCase(protocol)){
 				env.put(Context.SECURITY_PROTOCOL, "ssl");
 				env.put("java.naming.ldap.factory.socket", "org.nchelp.meteor.util.MeteorSSLSocketFactory");
 			}
 
 			if (authType != null && 
 				    ! "".equals(authType) && 
 				    authUser != null && 
 				    authPassword != null) {
 				env.put(Context.SECURITY_AUTHENTICATION, authType);
 				env.put(Context.SECURITY_PRINCIPAL, authUser);
 				env.put(Context.SECURITY_CREDENTIALS, authPassword);
 
 			}
 
 			log.debug("Connecting to LDAP:  " + providerURL + "/" + baseDN);
 			context = new InitialDirContext(env);		
 		}
 
 	}
 
 	/**
 	 * @see Directory#getAuthenticationLevel(String, String, String)
 	 */
 	public int getAuthenticationLevel(
 		String id,
 		String authProcID,
 		String providerType)
 		throws DirectoryException {
 		try {
 			if (context == null) {
 				init();
 			}
 
 			String searchString =
 				"AuthenticationProcess="
 					+ authProcID
 					+ ", File="
 					+ providerType
 					+ ", FileTypeFamily=Meteor, Institution="
 					+ id
 					+ ", "
 					+ baseDN;
 
 			Attributes attrs = context.getAttributes(searchString);
 			Attribute attr = attrs.get("AuthenticationLevel");
 
 			int retInt = Integer.parseInt((String) attr.get());
 			this.closeConnection();
 			
 			return retInt;
 
 		} catch (Exception e) {
 			// This could be a NumberFormatException or a NamingException
 			throw new DirectoryException(e);
 		}
 		
 	}
 
 	/**
 	 * @see Directory#getCertificate(String, String)
 	 */
 	public X509Certificate getCertificate(String id, String providerType)
 		throws DirectoryException {
 		try {
 			if (context == null) {
 				init();
 			}
 			String searchString =
 				"File="
 					+ providerType
 					+ ", FileTypeFamily=Meteor, Institution="
 					+ id
 					+ ", "
 					+ baseDN;
 
 			Attributes attrs = context.getAttributes(searchString);
 			Attribute attr = attrs.get("PreferredEncryption");
 
 			Attributes attrsSub = context.getAttributes((String) attr.get());
 			Attribute cert = attrsSub.get("CryptData;binary");
 
 			CertificateFactory certificatefactory =
 				CertificateFactory.getInstance("X.509");
 
 			ByteArrayInputStream bio =
 				new ByteArrayInputStream((byte[]) cert.get());
 			X509Certificate x509certificate =
 				(X509Certificate) certificatefactory.generateCertificate(bio);
 
 			this.closeConnection();
 			
 			return x509certificate;
 		} catch (Exception e) {
 			// This could be a NamingException or a CertificateException
 			context = null;
 			throw new DirectoryException(e);
 		}
 	}
 
 	/**
 	 * Method getIndexProviders.
 	 * @return List    A java.util.List object.  Each element of this list is 
 	 *                  of the type org.nchelp.meteor.provider.IndexProvider
 	 * @throws DirectoryException
 	 */
 	public List getIndexProviders() throws DirectoryException {
 		try {
 			if (context == null) {
 				init();
 			}
 
 			Vector returnVector = new Vector();
 
 			NamingEnumeration ne = this.search("File=IndexProvider");
 			log.debug("Searching for Index Providers");
 			while (ne.hasMore()) {
 				SearchResult sr = (SearchResult) ne.next();
 
 				String name = sr.getName();
 
 				Attributes attrs = null;
 				try {
 					attrs =
 						context.getAttributes(
 							"File=IndexProvider, "
 								+ sr.getName()
 								+ ", "
 								+ baseDN);
 				} catch (NamingException namingException) {
 					// This just means that this entry wansn't configured
 					// corectly.  Just skip this iteration of the loop.
 					log.debug(
 						"Non-fatal NamingException thrown: "
 							+ namingException.getMessage());
 					continue;
 				}
 
 				Attribute status = attrs.get("Status");
 
 				String strStatus = null;
 
 				if (status != null) {
 					strStatus = (String) status.get();
 					log.debug(name + " Status: " + strStatus);
 				} else {
 					strStatus = "";
 				}
 
 				if (strStatus == null || !"AC".equals(strStatus)) {
 					log.info(
 						"ID: "
 							+ name
 							+ " has a status of '"
 							+ strStatus
 							+ "' instead of 'AC' so it is not being included in the list of Index Providers");
 					continue;
 				}
 
 				Attribute attr = attrs.get("PreferredTransport");
 
 				Attributes attrsSub =
 					context.getAttributes((String) attr.get());
 				Attribute attrSub = attrsSub.get("URL");
 
 				IndexProvider ip = new IndexProvider();
 
 				// Parse out the name of the institution
 				// I could probably just as easily walk
 				// the LDAP directory, but I already know 
 				// what the string looks like:
 				// FileTypeFamily=Meteor, Institution=XXX
 				// and its the XXX that we want to get
 				String instName =
 					name.substring(name.lastIndexOf("Institution=") + 12);
 				ip.setIdentifier(instName);
 				try {
 					ip.setURL(new URL((String) attrSub.get()));
 				} catch (MalformedURLException e) {
 					log.error(
 						"Error Creating a URL Object for "
 							+ (String) attrSub.get(),
 						e);
 					// Just continue on with the rest of them
 					continue;
 				}
 				log.debug(
 					"Adding Index Provider with URL: "
 						+ ip.getURL().toString());
 				returnVector.add(ip);
 			}
 
 			this.closeConnection();
 						
 			return returnVector;
 		} catch (NamingException e) {
 			context = null;
 			throw new DirectoryException(e);
 		}
 	}
 
 	/**
 	 * Method getDataProviders.
 	 * @return List    A java.util.List object.  Each element of this list is 
 	 *                  of the type org.nchelp.meteor.provider.DataProvider
 	 * @throws DirectoryException
 	 */
 	public List getDataProviders() throws DirectoryException {
 
 		try {
 			if (context == null) {
 				init();
 				if(context == null){ 
 					throw new DirectoryException("Unable to connect to Meteor Registry");
 				}
 			}
 
 			List returnList = new ArrayList();
 
 			NamingEnumeration ne =
 				this.search("File=" + Directory.TYPE_DATA_PROVIDER);
 			log.debug("Searching for Data Providers");
 			while (ne.hasMore()) {
 				SearchResult sr = (SearchResult) ne.next();
 
 				if(sr == null){ continue; }
 				
 				String name = sr.getName();
 
 
 				Attributes attrs = null;
 				try {
 					if(context == null){
 						log.error("Context is null");
 					}
 					attrs =	context.getAttributes(
 							"File=DataProvider, " + name + ", " + baseDN);
 				} catch (NamingException namingException) {
 					// This just means that this entry wansn't configured
 					// corectly.  Just skip this iteration of the loop.
 					log.debug(
 						"Non-fatal NamingException thrown looking for \"File=DataProvider, " + name + ", " + baseDN
 							+ namingException.getMessage());
 					continue;
 				}
 
 				Attribute status = attrs.get("Status");
 
 				String strStatus = null;
 
 				if (status != null) {
 					strStatus = (String) status.get();
 					log.debug(name + " Status: " + strStatus);
 				} else {
 					strStatus = "";
 				}
 
 				if (strStatus == null || !"AC".equals(strStatus)) {
 					log.info(
 						"ID: "
 							+ name
 							+ " has a status of '"
 							+ strStatus
 							+ "' instead of 'AC' so it is not being included in the list of Data Providers");
 					continue;
 				}
 
 				Attribute attr = attrs.get("PreferredTransport");
 
 				Attribute attrSub;
 				try {
 					Attributes attrsSub =
 						context.getAttributes((String) attr.get());
 					attrSub = attrsSub.get("URL");
 				} catch (NamingException e) {
 					log.info(
 						"ID: "
 							+ name
 							+ " does not have the correct structure for the URL so it is not being included in the list of Data Providers");
 					continue;
 				}
 				DataProvider dp = new DataProvider();
 
 				// Parse out the name of the institution
 				// I could probably just as easily walk
 				// the LDAP directory, but I already know 
 				// what the string looks like:
 				// FileTypeFamily=Meteor, Institution=XXX
 				// and its the XXX that we want to get
 				String instName =
 					name.substring(name.lastIndexOf("Institution=") + 12);
 				dp.setId(instName);
 				//dp.setName(this.getInstitutionName(instName));
 				try {
 					dp.setURL(new URL((String) attrSub.get()));
 				} catch (MalformedURLException e) {
 					log.error(
 						"Error Creating a URL Object for "
 							+ (String) attrSub.get(),
 						e);
 					// Just continue on with the rest of them
 					continue;
 				}
 				log.debug(
 					"Adding Data Provider with URL: " + dp.getURL().toString());
 				returnList.add(dp);
 			}
 
 			this.closeConnection();
 			
 			return returnList;
 		} catch (NamingException e) {
 			log.error(
 				"Naming Exception getting the Data Providers: "
 					+ e.getMessage());
 			context = null;
 			throw new DirectoryException(e);
 		}
 	}
 
 	public List getAuthenticationProviders(int minimumAuthLevel)
 		throws DirectoryException {
 		try {
 			if (context == null) {
 				init();
 			}
 
 			Vector returnVector = new Vector();
 
 			NamingEnumeration ne =
 				this.search("File=" + Directory.TYPE_AUTHENTICATION_PROVIDER);
 			log.debug("Searching for Authentication Providers");
 			while (ne.hasMore()) {
 				SearchResult sr = (SearchResult) ne.next();
 
 				String name = sr.getName();
 
 				Attributes attrs = null;
 				try {
 					attrs =
 						context.getAttributes(
 							"File="
 								+ Directory.TYPE_AUTHENTICATION_PROVIDER
 								+ ", "
 								+ sr.getName()
 								+ ", "
 								+ baseDN);
 				} catch (NamingException namingException) {
 					// This just means that this entry wansn't configured
 					// corectly.  Just skip this iteration of the loop.
 					log.debug(
 						"Non-fatal NamingException thrown",
 						namingException);
 					continue;
 				}
 				Attribute attr = attrs.get("PreferredTransport");
 
 				Attributes attrsSub =
 					context.getAttributes((String) attr.get());
 				Attribute attrSub = attrsSub.get("URL");
 
 				// Parse out the name of the institution
 				// I could probably just as easily walk
 				// the LDAP directory, but I already know 
 				// what the string looks like:
 				// FileTypeFamily=Meteor, Institution=XXX
 				// and its the XXX that we want to get
 
 				AuthenticationProvider aup = new AuthenticationProvider();
 
 				String instName =
 					name.substring(name.lastIndexOf("Institution=") + 12);
 				aup.setName(instName);
 				try {
 					URL url = new URL((String) attrSub.get());
 					log.debug(
 						"Adding Authentication Provider with URL: "
 							+ url.toString());
 					aup.setUrl(url);
 				} catch (MalformedURLException e) {
 					log.error(
 						"Error Creating a URL Object for "
 							+ (String) attrSub.get(),
 						e);
 					// Just continue on with the rest of them
 					continue;
 				}
 				returnVector.add(aup);
 			}
 
 			log.debug("Done Searching for Authentication Providers");
 
 			this.closeConnection();
 			
 			return returnVector;
 		} catch (NamingException e) {
 			context = null;
 			throw new DirectoryException(e);
 		}
 
 	}
 	/**
 	 * Return the Description of the desired institution
 	 * @param  id	Identifier to query the description
 	 * @return String  Description of the institution
 	 * @throws DirectoryException
 	 */
 	public String getInstitutionName(String id) throws DirectoryException {
 		try {
 			if (context == null) {
 				init();
 			}
 
 			Attributes attrs =
 				context.getAttributes("Institution=" + id + ", " + baseDN);
 			if (attrs == null) {
 				log.debug(
 					"LDAPDirectory search for: 'Institution="
 						+ id
 						+ "' returned null");
 				return null;
 			}
 
 			Attribute attr = attrs.get("description");
 			if (attr == null) {
 				log.debug(
 					"Attribute 'description' in 'Institution="
 						+ id
 						+ "' returned null");
 				return null;
 			}
 
 			String retStr = (String) attr.get();
 			
 			this.closeConnection();
 			
 			return retStr;
 			
 		} catch (NamingException e) {
 			context = null;
 			throw new DirectoryException(e);
 		}
 
 	}
 
 	/**
 	 * @see Directory#getProviderURL(String, String)
 	 */
 	public URL getProviderURL(String id, String providerType)
 		throws DirectoryException {
 		try {
 			if (context == null) {
 				init();
 			}
 
 			Attributes attrs =
 				context.getAttributes(
 					"File="
 						+ providerType
 						+ ", FileTypeFamily=Meteor, Institution="
 						+ id
 						+ ", "
 						+ baseDN);
 			Attribute attr = attrs.get("PreferredTransport");
 
 			Attributes attrsSub = context.getAttributes((String) attr.get());
 			Attribute attrSub = attrsSub.get("URL");
 
 			URL url = new URL((String) attrSub.get());
 			
 			this.closeConnection();
 			
 			return url;
 		} catch (Exception e) {
 			log.error(e);
 			context = null;
 			throw new DirectoryException(e);
 		}
 
 	}
 
 	public String getStatus(String id, String providerType)
 		throws DirectoryException {
 		try {
 			if (context == null) {
 				init();
 			}
 
 			Attributes attrs =
 				context.getAttributes(
 					"File="
 						+ providerType
 						+ ", FileTypeFamily=Meteor, Institution="
 						+ id
 						+ ", "
 						+ baseDN);
 			Attribute attr = attrs.get("Status");
 
 			String retStr = (String) attr.get();
 			
 			this.closeConnection();
 			
 			return retStr;
 			
 		} catch (Exception e) {
 			log.error(e);
 			context = null;
 			throw new DirectoryException(e);
 		}
 	}
 
 	private NamingEnumeration search(String searchString)
 		throws NamingException {
 			if (context == null) {
 				init();
 			}
 
 		SearchControls sc = new SearchControls();
 		sc.setSearchScope(SearchControls.SUBTREE_SCOPE);
 
 		log.debug(
 			"Searching with baseDN: "
 				+ baseDN
 				+ " and filter: "
 				+ searchString);
 		return context.search(baseDN, searchString, sc);
 
 	}
 
 	private void closeConnection(){
 		if(context != null){
 			try {
 				context.close();
 			} catch (NamingException e) {}
 			context = null;	
 		}	
 	}
 	
 //	private boolean isConnected() {
 //		log.debug("Checking to see if we are connected to LDAP");
 //
 //		boolean result;
 //
 //		try {
 //			Attributes attrs = context.getAttributes("");
 //			result = true;
 //		} catch (NamingException e) {
 //			result = false;
 //			// force the invalidation
 //			try {
 //				context.close();
 //			} catch (NamingException sube) {
 //				log.debug("In the catch of context.close()");
 //			}
 //			context = null;
 //		}
 //
 //		log.debug("checkConnected() is returning " + result);
 //		return result;
 //	}
 
 }

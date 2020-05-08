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
  ********************************************************************************/
 
 package org.nchelp.meteor.registry;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.security.Security;
 import java.security.cert.X509Certificate;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.Properties;
 import java.util.Vector;
 
 import org.nchelp.meteor.logging.Logger;
 import org.nchelp.meteor.provider.AuthenticationProvider;
 import org.nchelp.meteor.provider.DataProvider;
 import org.nchelp.meteor.provider.IndexProvider;
 import org.nchelp.meteor.util.Resource;
 import org.nchelp.meteor.util.ResourceFactory;
 import org.nchelp.meteor.util.exception.DirectoryException;
 
 /**
  * This is an implementation of the Directory interface
  * that reads the data from a simple .properties file
  * This is mainly needed for demoing Meteor when an 
  * implementation of OpenLDAP is not available.
  * This probably isn't good enough for production use.
  * The format for the keys is as follows:
  * 
  * Note: "KeyValue" is the string that the user of this class 
  * will request the data for (Usually the ED ID).
  * 
  * KeyValue.Name = Institution Name
  * KeyValue.AuthenticationLevel = (Integer) Default Auth Level
  * KeyValue.AuthenticationProcessID = Auth Proc ID
  * KeyValue.Certificate = Pointer to the file containing the X.509 certificate for this institution
  * KeyValue.URL = URL endpoint for the Meteor Call
  * 
  * 
  * IndexProvider.1 = KeyValue
  * IndexProvider.2 = KeyValue
  * 
  * Note: For the IndexProviders, the KeyValue section must also exist in this file
  */
 public class PropertiesDirectory implements Directory {
 	
 	private final Logger log = Logger.create(this.getClass().getName());
 	private String dataFile = "directorydata.properties";
 	private Resource file;
 
 	/**
 	 * Constructor for PropertiesDirectory.
 	 */
 	public PropertiesDirectory() throws DirectoryException {
 		super();
 		this.file = ResourceFactory.createResource(dataFile);
 	}
 
 	/**
 	 * @see Directory#getAuthenticationLevel(String, String, String)
 	 */
 	public int getAuthenticationLevel(String id, String authProcID, String providerType) throws DirectoryException {
 		String level = this.file.getProperty(id + ".AuthenticationLevel");
 		try{
 			return Integer.parseInt(level);
 		} catch(NumberFormatException e){
 			log.error("Error parsing Authentication level '" + level +
			          " for ID '" + id + "'");
 			return -1;
 		}
 	}
 
 	/**
 	 * @see Directory#getCertificate(String, String)
 	 */
 	public X509Certificate getCertificate(String id, String probiderType) throws DirectoryException {
 		String cert = this.file.getProperty(id + ".Certificate");
 		
 		X509Certificate x509 = null;
 		return x509;
 	}
 
 	/**
 	 * @see Directory#getIndexProviders()
 	 */
 	public List getIndexProviders() throws DirectoryException {
 		List providers = new Vector();
 		
 		Enumeration enum = this.file.keys();
 		while(enum.hasMoreElements()){
 			String key = (String)enum.nextElement();
 			// If it doesn't start with "IndexProvider." then ignore it
 			if(! key.startsWith("IndexProvider.")) continue;
 			
 			// I know this key is of the form "IndexProvider.x = KeyValue
 			String id = this.file.getProperty(key);
 			
 			IndexProvider ip = new IndexProvider();
 			ip.setIdentifier(id);
 			ip.setURL(this.getProviderURL(id, Directory.TYPE_INDEX_PROVIDER));
 			
 			providers.add(ip);
 		}
 		
 		return providers;
 	}
 
 	/**
 	 * Return a list of DataProvider's from directorydata.properties
 	 * @see Directory#getDataProviders()
 	 */
 	public List getDataProviders() throws DirectoryException {
 		List providers = new Vector();
 		
 		Enumeration enum = this.file.keys();
 		while(enum.hasMoreElements()){
 			String key = (String)enum.nextElement();
 
 			// If it doesn't contain "DataProvider.URL" then ignore it
 			if(!key.endsWith("DataProvider.URL")) continue;
 
 			// I know this key is of the form "xxx.xxx.DataProvider.URL = KeyValue
 			int index = key.indexOf("DataProvider.URL");
 			String url = this.file.getProperty(key);
 			String nameKey = key.substring(0,index) + "Name";
 
 			DataProvider dp = new DataProvider();
 			dp.setName(this.file.getProperty(nameKey));
 			try{
 				dp.setURL(new URL(url));
 			} catch(Exception e) {
 				log.error("Error instantiating URL", e);
 				throw new DirectoryException(e);
 			}
 
 
 			providers.add(dp);
 		}
 
 		return providers;
 	}
 
 	public List getAuthenticationProviders(int minimumAuthLevel) throws DirectoryException
 	{
 		List providers = new Vector();
 		
 		Enumeration enum = this.file.keys();
 		while(enum.hasMoreElements()){
 			String key = (String)enum.nextElement();
 			// If it doesn't contain "AuthenticationProvider" then ignore it
 			if(key.indexOf(Directory.TYPE_AUTHENTICATION_PROVIDER) <= 0 ) continue;
 			
 			// I know this key is of the form "EDID.AuthenticationProvider.x = KeyValue
 			String url = this.file.getProperty(key);
 			
 			int index = key.indexOf("." + Directory.TYPE_AUTHENTICATION_PROVIDER);
 			String id = key.substring(0,index);
 			
 			if(minimumAuthLevel > this.getAuthenticationLevel(id, "", Directory.TYPE_AUTHENTICATION_PROVIDER)){
 				continue;
 			}
 			
 			AuthenticationProvider aup = new AuthenticationProvider();
 			
 			aup.setName(this.getInstitutionName(id));
 			try {
 				URL u = new URL(url);
 				
 				aup.setUrl(u);
 			} catch (MalformedURLException e) {
 				log.warn("Error creating a URL object for the string: '" + url + "'");
 				continue;
 			}
 			providers.add(aup);
 		}
 		
 		return providers;
 		
 	}
 
 	/**
 	 * @see Directory#getInstitutionName(String)
 	 */
 	public String getInstitutionName(String id) throws DirectoryException {
 		return this.file.getProperty(id + ".Name");
 	}
 
 	/**
 	 * @see Directory#getProviderURL(String, String)
 	 */
 	public URL getProviderURL(String id, String providerType) throws DirectoryException {
 		try {
 			String property = id + "." + providerType + ".URL";
 			log.debug("Looking up entry: " + property);
 			
 			
 			Properties properties = System.getProperties();
 						
 			String handlers = System.getProperty("java.protocol.handler.pkgs");
 			
 			if (handlers == null) {
 				// nothing specified yet (expected case)
 				properties.put("java.protocol.handler.pkgs", "com.sun.net.ssl.internal.www.protocol");
 			}
 			else {
 				// something already there, put ourselves out front
 				properties.put("java.protocol.handler.pkgs", "com.sun.net.ssl.internal.www.protocol|".concat(handlers));              
 			}
 			
 	        Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
 			
 	
 			String url = this.file.getProperty(property);
 			log.debug("Instantiating URL: " + url);
 			return new URL(url);
 		} catch(Exception e) {
 			log.error("Error instantiating URL", e);
 			throw new DirectoryException(e);
 		}
 	}
 
 	/**
 	 * @see org.nchelp.meteor.registry.Directory#getStatus(String, String)
 	 */
 	public String getStatus(String id, String providerType)
 		throws DirectoryException {
 		return "AC";
 	}
 
 }

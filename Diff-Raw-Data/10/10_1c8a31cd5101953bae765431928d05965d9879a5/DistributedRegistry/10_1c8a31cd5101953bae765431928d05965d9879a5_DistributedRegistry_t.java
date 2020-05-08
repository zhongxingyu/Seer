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
 
 import java.io.FileInputStream;
 import java.security.KeyStore;
 import java.security.PrivateKey;
 import java.security.cert.X509Certificate;
 import java.util.List;
 
 import org.nchelp.meteor.logging.Logger;
 import org.nchelp.meteor.security.SecurityToken;
 import org.nchelp.meteor.security.Signature;
 import org.nchelp.meteor.util.Resource;
 import org.nchelp.meteor.util.ResourceFactory;
 import org.nchelp.meteor.util.XMLParser;
 import org.nchelp.meteor.util.exception.DirectoryException;
 import org.nchelp.meteor.util.exception.SignatureException;
 import org.w3c.dom.Document;
 
 /**
 * Public interface to whatever becomes of the Meteor Registry.
 * This could be properties files, UDDI, LDAP, Shibboleth or 
 * some other crazy registry scheme that I haven't thought of yet
 *  
 * @version   $Revision$ $Date$
 * @since     Meteor1.0
 * 
 */
 public class DistributedRegistry {
 
 	private final Logger log = Logger.create(this.getClass());
 	private static DistributedRegistry singleton;
 
 	/**
 	 * Method singleton.
 	 * @return DistributedRegistry
 	 */
 	public static DistributedRegistry singleton() {
 
 		if (singleton == null) {
 			synchronized (DistributedRegistry.class) {
 				if (singleton == null)
 					singleton = new DistributedRegistry();
 			}
 		}
 		return singleton;
 	}
 	
 	/**
 	 * Method getAuthentication.
 	 * @return SecurityToken
 	 */
 	public SecurityToken getAuthentication() throws SignatureException {
 		/*
 		 * In this method, we need to get the userid (ED ID)
 		 * of this institution.  This needs to come from a 
 		 * properties file since the registry really doesn't 
 		 * know which provider it is talking to.
 		 * 
 		 * We need to get the authentication level
 		 * 
 		 * We need to get the private key information
 		 */
 		SecurityToken token = new SecurityToken();
 		Resource res = ResourceFactory.createResource("authentication.properties");
 		
 		// Let's start by getting the userid
 		String authProviderID = res.getProperty("authentication.identifier");
 		
 		try {
 			token.setUserid(authProviderID);
 		} catch(SignatureException e) {
 			// The only way this can get thrown is
 			// when the token is signed and then 
 			// an attempt is made to change a value.
 			// Since we just created this token 
 			// about 5 lines ago, we can be really
 			// sure that it is not signed, duh!
 			// So don't do anything here
 		}
 		
 		// now get the authentication level from the registry
 		Directory ldap = null;
 		try {
 			ldap = DirectoryFactory.getInstance().getDirectory();
 		
 			token.setCurrentAuthLevel(ldap.getAuthenticationLevel(authProviderID));
 			token.setAuthenticationProcessID(ldap.getAuthenticationProcessID(authProviderID));
 		
 		} catch(DirectoryException e) {
 			throw new SignatureException(e);
 		}
 		
 		// If the property == "No" then the should sign variable is false
 		boolean shouldSign = (! "No".equalsIgnoreCase(res.getProperty("authentication.signassertion", "Yes")));
 		
 		if(shouldSign){		
 			// OK, now we need to get the private key stuff
 		    String keystoreType = res.getProperty("authentication.keystore.type");
 		    String keystoreFile = res.getProperty("authentication.keystore.file");
 		    String keystorePass = res.getProperty("authentication.keystore.password");
 			String privateKeyAlias = res.getProperty("authentication.privatekey.alias");
 			String privateKeyPass = res.getProperty("authentication.privatekey.password");
 			Boolean includeCertificate = new Boolean(res.getProperty("authentication.certificate.include"));
 			String certificateAlias = res.getProperty("authentication.certificate.alias");
 	
 		    PrivateKey privateKey = null;
 		    X509Certificate cert = null;
 	
 			try {
 				KeyStore ks = KeyStore.getInstance(keystoreType);
 				FileInputStream fis = new FileInputStream(keystoreFile);
 				
 				ks.load(fis, keystorePass.toCharArray());
 				
 				privateKey = (PrivateKey) ks.getKey(privateKeyAlias, privateKeyPass.toCharArray());
 		         
 		        cert = (X509Certificate) ks.getCertificate(certificateAlias);
 			} catch(Exception e) {
 				throw new SignatureException(e);
 			}
 		
 			token.setPrivateKey(privateKey);
 		}		
 		return token;
 	}
 
     
 	/**
 	 * This method returns a list of index providers from the 
 	 * LDAP server
 	 * 
 	 * @return List java.util.List with each element is of the type org.nchelp.meteor.provider.IndexProvider
 	 */
 	public List getIndexProviders() {
 		try {
 			Directory dir = DirectoryFactory.getInstance().getDirectory();
 			return dir.getIndexProviders();
 		} catch(DirectoryException e) {
 			/* If something goes wrong here 
 			 * there really isn't much I can
 			 * do except log the exception
 			 * and return an empty list, right???
 			 */
 			 
 			 log.error(e);
 			 return null;
 		}		
 	}
 	
 	/**
 	 * Method authenticateProvider.
 	 * @param token
 	 * @return boolean True if the provider is authenticated, false if not.
 	 */
 	public boolean authenticateProvider(SecurityToken token){
		if(token == null){ return false; }
		
		log.info("Validating Security Token from: " + token.getUserid());
 		if(! token.signed()){
 			// If it isn't signed what do we do??
 			
 			Resource res = ResourceFactory.createResource("authentication.properties");
 			String requireSign = res.getProperty("authentication.requiresignedassertions");
 	
 		
 			if("No".equalsIgnoreCase(requireSign)){
 				// If they set this flag and it isn't signed
 				// then treat the token as a perfectly valid token
 				return true;
 			} else {
 				// Default is to require signed assertions
 				return false;
 			}
 		}
 		
 		String entity = token.getUserid();
 		try {
 			Directory dir = DirectoryFactory.getInstance().getDirectory();
 			X509Certificate cert = dir.getCertificate(entity, Directory.TYPE_ACCESS_PROVIDER);
 			
 			Document doc = token.toXML();
			boolean result = Signature.validate(doc, cert);
			log.info("Signature from " + token.getUserid() + (result ? " valid" : " invalid"));
			return result;
 		} catch(Exception e) {
 			log.error("Error authenticating Provider: " + entity, e);
 			return false;
 		}
 	}
 
 }

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
 
 package org.nchelp.meteor.security;
 
 import java.security.PrivateKey;
 import java.security.cert.X509Certificate;
 import java.security.interfaces.DSAPrivateKey;
 import java.security.interfaces.RSAPrivateKey;
 
 import javax.xml.transform.TransformerException;
 
 import org.apache.xml.security.Init;
 import org.apache.xml.security.exceptions.XMLSecurityException;
 import org.apache.xml.security.keys.KeyInfo;
 import org.apache.xml.security.keys.keyresolver.KeyResolverException;
 import org.apache.xml.security.signature.XMLSignature;
 import org.apache.xml.security.signature.XMLSignatureException;
 import org.apache.xml.security.transforms.Transforms;
 import org.apache.xml.security.utils.Constants;
 import org.apache.xml.security.utils.XMLUtils;
 import org.apache.xpath.XPathAPI;
 import org.nchelp.meteor.logging.Logger;
 import org.nchelp.meteor.util.exception.SignatureException;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 /**
 * Use this class to sign and verify XML signatures.
 *  
 * @version   $Revision$ $Date$
 * @since     Meteor Phase II Milestone 2
 * 
 */
 public class Signature {
	private static Logger log = Logger.create(Signature.class);
 	
 	static {
 		Init.init();
 	}
 		
 		
 	/**
 	 * Sign the document.  The signature will be an enveloped
 	 * signature.  This means that the signature information
 	 * will be in this document as a child to the root node.
 	 * 
 	 * @param doc   Document to be signed
 	 * @param privateKey  This is the private key that we will use to sign the document.
 	 * @throws SignatureException  Any errors will throw this exception.
 	 */
 	public static void sign(Document doc, PrivateKey privateKey)
 	                  throws SignatureException {
 		sign(doc, privateKey, null); 
 	}
 	
 	/**
 	 * Sign the document.  The signature will be an enveloped
 	 * signature.  This means that the signature information
 	 * will be in this document as a child to the root node.
 	 * 
 	 * @param doc   Document to be signed
 	 * @param privateKey  This is the private key that we will use to sign the document.
 	 * @param publicKey   If you want to include your X.509 Certificate (Public Key only) 
 	 *                     in the signature, then passit in here
 	 * @throws SignatureException  Any errors will throw this exception.
 	 */
 	public static void sign(Document doc, PrivateKey privateKey, X509Certificate publicKey) 
 	                  throws SignatureException {
 		log.debug("Starting to sign document");
 		String baseURI = "";
 		XMLSignature dsig = null;
 		
 		String algorithm = null;
 		if(privateKey instanceof RSAPrivateKey){
 			algorithm = XMLSignature.ALGO_ID_SIGNATURE_RSA;
 		} else if (privateKey instanceof DSAPrivateKey) {
 			algorithm = XMLSignature.ALGO_ID_SIGNATURE_DSA;
 		} else {
 			throw new SignatureException("Private Key implements an unknown algorithm.  The only supported algorithms are RSA and DSA.");	
 		}
 		
 		try {
 			dsig = new XMLSignature(doc, baseURI, algorithm);
 			
 			Element elem = doc.getDocumentElement();
 			elem.appendChild(dsig.getElement());
 		} catch(XMLSecurityException e) {
 			log.error(e);
 			throw new SignatureException(e);
 		}
 
 		Transforms transforms = new Transforms(doc);
 		try {
 			transforms.addTransform(Transforms.TRANSFORM_ENVELOPED_SIGNATURE);
 	        transforms.addTransform(Transforms.TRANSFORM_C14N_WITH_COMMENTS);
             dsig.addDocument("", transforms, Constants.ALGO_ID_DIGEST_SHA1);
 		} catch(Exception e) {
 			log.error(e);
 			throw new SignatureException(e);
 		}	
 		
 		if(publicKey != null){
 			log.debug("Adding public key to signature");
 	        try {
 				dsig.addKeyInfo(publicKey);
 		        dsig.addKeyInfo(publicKey.getPublicKey());
 			} catch(XMLSecurityException e) {
 				log.error(e);
 				throw new SignatureException(e);
 			}
 		}
 
 		try {
 			dsig.sign(privateKey);
 		} catch(XMLSignatureException e) {
 			log.error(e);
 			throw new SignatureException(e);
 		}
 		log.debug("Finished signing document");
 		return;
 	}
 
 
 	/**
 	 * Validate the XML Signature in the passed Document
 	 * @param doc	Document that contains the signature
 	 * @param publicKey X509Certificate that will be used to validate the signature
 	 * 					 If this parameter is null, the the X.509 certificate must be
 	 *                   contained in the document itself.
 	 * @return boolean  True if the document can be validated with the public key, false otherwise.
 	 * @throws SignatureException  Thrown if any errors occur
 	 */
 	public static boolean validate(Document doc, X509Certificate publicKey)
 							throws SignatureException {
 		Element nscontext = XMLUtils.createDSctx(doc, "ds", Constants.SignatureSpecNS);
 		Element sigElement = null;
 		
 		try {
 			sigElement = (Element) XPathAPI.selectSingleNode(doc,
 			                         "//ds:Signature[1]", nscontext);
 		} catch(TransformerException e) {
 			throw new SignatureException(e);
 		}
 		                         
 		String baseURI = "";
 		XMLSignature signature = null;
 		
 		try {
 			signature = new XMLSignature(sigElement, baseURI);
 		} catch(Exception e) {
 			throw new SignatureException(e);
 		}
 
 		/*
 		 * If the publicKey that was passed into this method
 		 * is null, then we can look into the document itself
 		 * and see if there is a public key embedded in it.
 		 * If so, then we'll validate it against that.
 		 * 
 		 * NOTE:
 		 * 	This probably needs to be a configuration parameter
 		 *  whether to accept public keys that are in the document
 		 *  or force them to be read from the Meteor Registry
 		 */
 		if(publicKey == null){	
 			log.debug("Public key was not passed to method, trying to use key in the document");
 			KeyInfo ki = signature.getKeyInfo();
 			if(ki == null){
 				log.warn("Public key was not contained in the message.  Therefore, the XML Signature cannot be validated");
 				return false;
 			}
 			try {
 				publicKey = ki.getX509Certificate();
 			} catch(KeyResolverException e) {
 				throw new SignatureException(e);
 			}
 		}
 		
 		if(publicKey == null){
 			log.error("Even as a last resort, the certificate is null");
 		}
 		
 		try {
 			return signature.checkSignatureValue(publicKey);
 		} catch(XMLSignatureException e) {
 			throw new SignatureException(e);
 		}
 	}
 	
 	public static boolean isSigned(Document doc) throws SignatureException{
 			Element nscontext = XMLUtils.createDSctx(doc, "ds", Constants.SignatureSpecNS);
 			Element sigElement = null;
 			
 			try {
 				sigElement = (Element) XPathAPI.selectSingleNode(doc,
 				                         "//ds:Signature[1]", nscontext);
 			} catch(TransformerException e) {
 				throw new SignatureException(e);
 			}
 			
 			// If the XPath Expression found a ds:Signature element,
 			// then it is signed.  else it isn't.
 			// So that means this:
 			// sigElement == null return false
 			// sigElement != null return true
 			return (sigElement != null);	
 		}
 }

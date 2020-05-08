 /**
  * 
  * Copyright 2003 NCHELP
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
 package org.nchelp.meteor.provider.access;
 
 import java.io.FileInputStream;
 import java.io.InputStream;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.net.URL;
 import java.util.Iterator;
 import java.util.Map;
 
 import javax.servlet.ServletContext;
 import javax.xml.transform.Source;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.stream.StreamSource;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.nchelp.meteor.provider.MeteorParameters;
 import org.nchelp.meteor.security.SecurityToken;
 import org.nchelp.meteor.util.Resource;
 import org.nchelp.meteor.util.ResourceFactory;
 
 /**
  *  TransformXML.java
  *
  *   @author  timb
  *   @version $Revision$ $Date$
  *   @since   Apr 17, 2003
  */
 public class TransformXML {
 	private final Log log = LogFactory.getLog(this.getClass());
 
 	public String transform(MeteorParameters parms, 
 							 String level,
 	                         Map xsltParameterMap,
 	                         ServletContext context,
 	                         String xml) throws TransformerException {
 
 		String xslFile = "";
 		String currentRole = parms.getRole();
 
 		if(currentRole == null){
 			currentRole = "null";
 		}
 
 		/*
 		 * When we're working with the APCSR role, we're 
 		 * going to use the XSLT from either the borower
 		 * or FAA perspective.  So, if the level is anything
 		 * other than 'query' then use either the borrower 
 		 * or faa screens.
 		 */
 		 
 		 if((SecurityToken.roleAPCSR.equals(currentRole) ||
 		      SecurityToken.roleLENDER.equals(currentRole) )
 		    &&
 		    ! "query".equalsIgnoreCase(level)){
 		 	
 		 	String inquiryType = parms.getInquiryType();
 		 	if(inquiryType == null || "".equals(inquiryType)){
 		 		log.error("Error getting the Inquiry Type from the Meteor Parameters object");
 		 	}
 		 	log.debug("Changing the role from " + currentRole + " to " + inquiryType);
 		 	currentRole = inquiryType;
 		 }
 		 
 		 
 		Resource resource = ResourceFactory.createResource("accessprovider.properties");
 		String propertyName = "accessprovider." + currentRole + "." + level;
 			
 		xslFile = resource.getProperty(propertyName);
 		if (xslFile == null) {
 			log.info("Property: '" + propertyName + "' was not found in accessprovider.properties" );
 				
 			propertyName = "accessprovider." + currentRole + ".default";
 			xslFile = resource.getProperty(propertyName);
 		}
 		if (xslFile == null) {
 			log.info("Property: '" + propertyName + "' was not found in accessprovider.properties" );
 			xslFile = resource.getProperty("accessprovider.default");
 		}
 		//If it is still null, then call the handleError method
 		if (xslFile == null) {
 			throw new TransformerException( "Error reading XSL stylesheet");
 		}
 
 		try {
 			// Get the XML input document and the stylesheet, both in the servlet
 			// engine document directory.
 			Source xmlSource = new StreamSource(new StringReader(xml));
 
 			URL xslURL;
 			InputStream is;
 			
 			if(context == null){
 				xslURL = new URL("file://" + xslFile);
 				is = new FileInputStream(xslFile);
 			} else {
 				xslURL= context.getResource(xslFile);
 				is = context.getResourceAsStream(xslFile);
 			}
 			
 			StreamSource xslSource = new StreamSource(is);
 			// @TODO: If the file doesn't exist, then xslURL is null here. 
 			String xslpath = xslURL.toExternalForm();
			log.debug("Loanding XSL File: " + xslpath);
 			xslSource.setSystemId(xslpath);
 
 			// Generate the transformer.
 			TransformerFactory tFactory = TransformerFactory.newInstance();
 			Transformer transformer = tFactory.newTransformer(xslSource);
 
 			Iterator i = xsltParameterMap.keySet().iterator();
 			while (i.hasNext()) {
 				String key = (String) i.next();
 				Object value = xsltParameterMap.get(key);
 
 				transformer.setParameter(key, value);
 			}
 
 			StringWriter writer = new StringWriter();
 			
 			// Perform the transformation, sending the output to the response.
 			transformer.transform(xmlSource, new StreamResult(writer));
 
 			return writer.toString();
 
 		}
 		// If an Exception occurs, return the error to the client.
 		catch (Exception e) {
 			// Send it to the console too
 			log.error("Error Transforming XSL: " + xslFile, e);
 			throw new TransformerException(e);
 		}
 		
 	}
 }

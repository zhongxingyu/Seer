 /**
  * Class containing common utility functions.
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
 package com.tresys.jalop.common;
 
 import java.io.File;
 import java.util.GregorianCalendar;
 
 import javax.xml.XMLConstants;
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBElement;
 import javax.xml.bind.Marshaller;
import javax.xml.crypto.dsig.DigestMethod;

 import javax.xml.datatype.DatatypeFactory;
 import javax.xml.datatype.XMLGregorianCalendar;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.validation.Schema;
 import javax.xml.validation.SchemaFactory;
 
 import org.w3c.dom.Document;
 
 public class JALUtils {
 	
 	public static final String SCHEMA_LOCATION = "src/main/java/com/tresys/jalop/schemas/applicationMetadataTypes.xsd";
 
 	/**
 	 * Builds a document and marshals the xml into the document. 
 	 * This also validates the xml against the given schema.
 	 * 
 	 * @param jc		the JAXBContext of the correct class
 	 * @param element	the JAXBElement created by ObjectFactory for the correct class
 	 * @return	the marshaled document
 	 * @throws Exception 
 	 */
 	public static Document marshal(JAXBContext jc, JAXBElement element) throws Exception {
 		Marshaller m = jc.createMarshaller();
 		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
 
 		SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
 		Schema schema = sf.newSchema(new File(SCHEMA_LOCATION));
 		m.setSchema(schema);
 
 		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 		dbf.setNamespaceAware(true);
 		DocumentBuilder db = dbf.newDocumentBuilder();
 		Document document = db.newDocument();
 
 		m.marshal(element, document);
 
 		return document;
 	}
 
 	/**
	 * An enum for the different types of digest methods that can be used
	 */
	public enum DMType {
		SHA256 (DigestMethod.SHA256),
		SHA512 (DigestMethod.SHA512),
		SHA384 ("http://www.w3.org/2001/04/xmldsig-more#sha384");

		private String digestMethod;

		DMType(String digestMethod) {
			this.digestMethod = digestMethod;
		}

		private String digestMethod() { return digestMethod; }
	}

	/**
 	 * Creates a calendar with the current date and time to set the timestamp
 	 * 
 	 * @return	XMLGregorianCalendar with the current date
 	 * @throws Exception
 	 */
 	public static XMLGregorianCalendar getCurrentTime() throws Exception {
 		GregorianCalendar gc = new GregorianCalendar();
 		XMLGregorianCalendar xmlCal = DatatypeFactory.newInstance().newXMLGregorianCalendar(gc);
 		return xmlCal;
 	}
 }

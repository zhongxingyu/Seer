 /* ***** BEGIN LICENSE BLOCK *****
  * Version: MPL 1.1
  *
  * The contents of this file are subject to the Mozilla Public License Version
  * 1.1 (the "License"); you may not use this file except in compliance with
  * the License. You may obtain a copy of the License at
  * http://www.mozilla.org/MPL/
  *
  * Software distributed under the License is distributed on an "AS IS" basis,
  * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
  * for the specific language governing rights and limitations under the
  * License.
  *
  * The Original Code is S23M.
  *
  * The Initial Developer of the Original Code is
  * The S23M Foundation.
  * Portions created by the Initial Developer are
  * Copyright (C) 2012 The S23M Foundation.
  * All Rights Reserved.
  *
  * Contributor(s):
  * Andrew Shewring
  * ***** END LICENSE BLOCK ***** */
 package org.s23m.cell.communication.xml;
 
 import java.io.ByteArrayInputStream;
 import java.net.URL;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.transform.Source;
 import javax.xml.transform.stream.StreamSource;
 import javax.xml.validation.Schema;
 import javax.xml.validation.Validator;
 
 import junit.framework.TestCase;
 
import org.junit.Test;
 import org.s23m.cell.S23MKernel;
 import org.xml.sax.SAXException;
 
 import com.google.common.io.Resources;
 
 // TODO fix up root element - wrapper nodes can have a complexType child as in 
 // http://www.w3schools.com/schema/schema_example.asp
 public class XmlDocumentValidationTest extends TestCase {
	
	@Test
	public void testExampleInstanceValidatesAgainstGeneratedSchema() throws Exception {
 		S23MKernel.boot();
 		
 		XmlSchemaFactory xmlSchemaFactory = new XmlSchemaFactory();
 		XmlSchemaTerminology terminology = DefaultXmlSchemaTerminology.getInstance();
 		Schema schema = xmlSchemaFactory.createSchema(terminology);
 		
 		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 		factory.setValidating(false);
 		factory.setNamespaceAware(true);
 		factory.setSchema(schema);
 		
 		// load document
 		
 		URL resource = Resources.getResource("cell-communication-instance.xml");
 		byte[] byteArray = Resources.toByteArray(resource);
 		
 		Source xmlFile = new StreamSource(new ByteArrayInputStream(byteArray));
 		
 		Validator validator = schema.newValidator();
 		try {
 		  validator.validate(xmlFile);
 		} catch (SAXException e) {
		  fail("Validation failed: " + e.getMessage());
 		}
 	}
 }

 package org.eclipse.rmf.serialization.tests.env.xerces;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import java.io.IOException;
 import java.util.List;
 import java.util.Vector;
 
 import javax.xml.XMLConstants;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 import javax.xml.transform.Source;
 import javax.xml.transform.stream.StreamSource;
 import javax.xml.validation.Schema;
 import javax.xml.validation.SchemaFactory;
 import javax.xml.validation.Validator;
 
 import org.apache.xerces.impl.Constants;
 import org.eclipse.rmf.serialization.tests.env.emf.myreqif.MyreqifPackage;
 import org.junit.Test;
 import org.xml.sax.ErrorHandler;
 import org.xml.sax.SAXException;
 import org.xml.sax.SAXParseException;
 import org.xml.sax.XMLReader;
 
 @SuppressWarnings("nls")
 public class XercesSchemaValidationTests {
 	public static final String DATA_BASEDIR = "input/org.eclipse.rmf.serialization.tests.env/data/";
 	public static final String MODEL_BASEDIR = "input/org.eclipse.rmf.serialization.tests.env/model/";
 
 	class MyErrorHandler implements ErrorHandler {
 		public List<SAXParseException> errors = new Vector<SAXParseException>();
 		public List<SAXParseException> warnings = new Vector<SAXParseException>();
 		public List<SAXParseException> fatalErrors = new Vector<SAXParseException>();
 
 		public void warning(SAXParseException exception) throws SAXException {
 			warnings.add(exception);
 
 		}
 
 		public void error(SAXParseException exception) throws SAXException {
 			errors.add(exception);
 
 		}
 
 		public void fatalError(SAXParseException exception) throws SAXException {
 			fatalErrors.add(exception);
 
 		}
 
 	};
 
 	@Test
 	public void testSchemaValidationWithValidFile() {
 		StreamSource[] schemaDocuments = new StreamSource[] { new StreamSource(MODEL_BASEDIR + "myreqif.xsd") };
 		Source instanceDocument = new StreamSource(DATA_BASEDIR + "simple.reqif");
 
 		SchemaFactory sf = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
 		Schema s;
 		try {
 			s = sf.newSchema(schemaDocuments);
 			Validator v = s.newValidator();
 			v.validate(instanceDocument);
 
 		} catch (SAXException ex) {
 			assertTrue(ex.getMessage(), false);
 		} catch (IOException ex) {
 			assertTrue(ex.getMessage(), false);
 		}
 	}
 
 	@Test
 	public void testSchemaValidationWithInvalidFile() {
 		StreamSource[] schemaDocuments = new StreamSource[] { new StreamSource(MODEL_BASEDIR + "myreqif.xsd") };
 		Source instanceDocument = new StreamSource(DATA_BASEDIR + "bare.reqif");
 		SchemaFactory sf = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
 		Schema s;
 		try {
 			s = sf.newSchema(schemaDocuments);
 			Validator v = s.newValidator();
 			v.validate(instanceDocument);
 			assertTrue(false);
 		} catch (SAXException ex) {
			assertTrue(ex.getMessage().contains("cvc-complex-type.2.4.a: Invalid content was found starting with element 'CORE-CONTENT'"));
 		} catch (IOException ex) {
 			assertTrue(ex.getMessage(), false);
 		}
 	}
 
 	@Test
 	public void testSchemaValidationOnLoadWithValidFile() {
 		try {
 			MyErrorHandler errorHandler = new MyErrorHandler();
 			SAXParserFactory factory = SAXParserFactory.newInstance();
 			SAXParser parser = factory.newSAXParser();
 			XMLReader reader = parser.getXMLReader();
 			reader.setErrorHandler(errorHandler);
 			reader.setFeature(Constants.SAX_FEATURE_PREFIX + Constants.NAMESPACES_FEATURE, true);
 			reader.setFeature(Constants.SAX_FEATURE_PREFIX + Constants.VALIDATION_FEATURE, true);
 			reader.setFeature(Constants.XERCES_FEATURE_PREFIX + Constants.SCHEMA_VALIDATION_FEATURE, true);
 			reader.setProperty(Constants.JAXP_PROPERTY_PREFIX + Constants.SCHEMA_LANGUAGE, XMLConstants.W3C_XML_SCHEMA_NS_URI);
 			reader.setProperty(Constants.XERCES_PROPERTY_PREFIX + Constants.SCHEMA_LOCATION, MyreqifPackage.eNS_URI + " ../model/myreqif.xsd");
 
 			reader.parse(DATA_BASEDIR + "simple.reqif");
 
 			assertEquals(toString(errorHandler.errors), 0, errorHandler.errors.size());
 			assertEquals(toString(errorHandler.warnings), 0, errorHandler.warnings.size());
 			assertEquals(toString(errorHandler.fatalErrors), 0, errorHandler.fatalErrors.size());
 
 		} catch (SAXException ex) {
 			assertTrue(ex.getMessage(), false);
 		} catch (IOException ex) {
 			assertTrue(ex.getMessage(), false);
 		} catch (ParserConfigurationException ex) {
 			// TODO Auto-generated catch block
 		}
 	}
 
 	@Test
 	public void testSchemaValidationOnLoadWithInvalidFile() {
 		try {
 			MyErrorHandler errorHandler = new MyErrorHandler();
 			SAXParserFactory factory = SAXParserFactory.newInstance();
 			SAXParser parser = factory.newSAXParser();
 			XMLReader reader = parser.getXMLReader();
 			reader.setErrorHandler(errorHandler);
 			reader.setFeature(Constants.SAX_FEATURE_PREFIX + Constants.NAMESPACES_FEATURE, true);
 			reader.setFeature(Constants.SAX_FEATURE_PREFIX + Constants.VALIDATION_FEATURE, true);
 			reader.setFeature(Constants.XERCES_FEATURE_PREFIX + Constants.SCHEMA_VALIDATION_FEATURE, true);
 			reader.setProperty(Constants.JAXP_PROPERTY_PREFIX + Constants.SCHEMA_LANGUAGE, XMLConstants.W3C_XML_SCHEMA_NS_URI);
 			reader.setProperty(Constants.XERCES_PROPERTY_PREFIX + Constants.SCHEMA_LOCATION, MyreqifPackage.eNS_URI + " ../model/myreqif.xsd");
 
 			reader.parse(DATA_BASEDIR + "bare.reqif");
 
 			assertEquals(toString(errorHandler.errors), 1, errorHandler.errors.size());
 			assertEquals(toString(errorHandler.warnings), 0, errorHandler.warnings.size());
 			assertEquals(toString(errorHandler.fatalErrors), 0, errorHandler.fatalErrors.size());
 
 		} catch (SAXException ex) {
 			assertTrue(ex.getMessage(), false);
 		} catch (IOException ex) {
 			assertTrue(ex.getMessage(), false);
 		} catch (ParserConfigurationException ex) {
 			// TODO Auto-generated catch block
 		}
 	}
 
 	@Test
 	public void testSchemaValidationWithValidToolExtensionsFile() {
 		StreamSource[] schemaDocuments = new StreamSource[] { new StreamSource(MODEL_BASEDIR + "myreqif.xsd") };
 		Source instanceDocument = new StreamSource(DATA_BASEDIR + "toolExtensions.reqif");
 
 		SchemaFactory sf = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
 		Schema s;
 		try {
 			s = sf.newSchema(schemaDocuments);
 			Validator v = s.newValidator();
 			v.validate(instanceDocument);
 
 		} catch (SAXException ex) {
 			assertTrue(ex.getMessage(), false);
 		} catch (IOException ex) {
 			assertTrue(false);
 		}
 	}
 
 	/**
 	 * check if the error in the Xerces XML Validator that raises an error against not resolvable types in the any
 	 * section still persists.
 	 */
 	@Test
 	public void testSchemaValidationWithValidToolExtensionsXsiTypeFile() {
 		StreamSource[] schemaDocuments = new StreamSource[] { new StreamSource(MODEL_BASEDIR + "myreqif.xsd") };
 		Source instanceDocument = new StreamSource(DATA_BASEDIR + "toolExtensions_xsiType.reqif");
 
 		SchemaFactory sf = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
 		Schema s;
 		try {
 			s = sf.newSchema(schemaDocuments);
 			Validator v = s.newValidator();
 			v.validate(instanceDocument);
 
 		} catch (SAXException ex) {
			assertTrue(ex.getMessage().contains("cvc-elt.4.2: Cannot resolve 'aType' to a type definition for element 'ext:myExtension'"));
 		} catch (IOException ex) {
 			assertTrue(false);
 		}
 	}
 
 	private String toString(List<SAXParseException> exceptions) {
 		StringBuffer buffer = new StringBuffer();
 		for (SAXParseException exception : exceptions) {
 			buffer.append(exception.getMessage());
 			buffer.append(" ### ");
 		}
 		return buffer.toString();
 	}
 
 }

 package org.xbrlapi.sax.tests;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URI;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.xbrlapi.cache.CacheImpl;
 import org.xbrlapi.sax.EntityResolverImpl;
 import org.xbrlapi.utilities.BaseTestCase;
 import org.xbrlapi.utilities.XBRLException;
 import org.xml.sax.Attributes;
 import org.xml.sax.EntityResolver;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import org.xml.sax.XMLReader;
 import org.xml.sax.helpers.DefaultHandler;
 
 /**
  * @author Geoffrey Shuetrim (geoff@galexy.net)
  */
 public class EntityResolverTestCase extends BaseTestCase {
 
 	protected void setUp() throws Exception {
 		super.setUp();
 	}
 
 	protected void tearDown() throws Exception {
 		super.tearDown();
 	}
 
 	public EntityResolverTestCase(String arg0) {
 		super(arg0);
 	}
 
 	/**
 	* Test that the entity resolver constructor 
 	* throws an xbrlapiexception when the cache
 	* root does not exist.
 	*/
 	public final void testEntityResolverImplFailure() throws Exception {
 		try {
 			String cache = configuration.getProperty("nonexistent.cache");
 			logger.info("Trying to create an entity resolver using cache " + cache);
 			new EntityResolverImpl(new File(cache));
 			fail("The entity resolver failed to detect a non-existent cache root");
 		} catch (XBRLException expected) {
 		}
 	}
 
 	public final void testInputSourceCreatedByResolveEntity() {
 		String originalURI = "http://www.xbrl.org/2003/xbrl-instance-2003-12-31.xsd";
 		try {
 			logger.info("Trying to create an entity resolver using cache " + cachePath);
 			EntityResolverImpl resolver = new EntityResolverImpl(new File(cachePath));
 			InputSource is = resolver.resolveEntity("",originalURI);
 			assertNull("Public ID for the input source is not null.",is.getPublicId());
		    System.out.println(new File(cachePath + "/http/www.xbrl.org/80/2003/xbrl-instance-2003-12-31.xsd").toURI().toString());			    
            System.out.println(new File(is.getSystemId().toString()));
			assertEquals("System ID is wrong.",new File(cachePath + "/http/www.xbrl.org/-1/2003/xbrl-instance-2003-12-31.xsd").toURI().toString(),is.getSystemId());
 		} catch (XBRLException e) {
 			fail("The entity resolver constructor failed unexpectedly.");
 		} finally {
 			try {
 				CacheImpl cache = new CacheImpl(new File(cachePath));
 				cache.purge(new URI(originalURI));
 			} catch (Exception e) {
 				fail(e.getMessage());
 			}
 		}
 	}
 
 	
 	public final void testSAXParserUsageOfCustomEntityResolver() {
 		
 		String uri = getURI("test.data.cacheableURI.A");
 
         ContentHandler contentHandler = new EntityResolverTestCase.ContentHandler();
         XMLReader reader = null;
 		try {
 			SAXParserFactory factory = SAXParserFactory.newInstance();
 			factory.setValidating(false);
 			factory.setNamespaceAware(true);
 			SAXParser parser = factory.newSAXParser();
 			reader = parser.getXMLReader();
 			reader.setContentHandler(contentHandler);
         } catch (ParserConfigurationException e) {
             fail(e.getMessage());
         } catch (SAXException e) {
             fail(e.getMessage());
         }
         
         logger.info("Trying to create an entity resolver using cache " + cachePath);
         EntityResolver entityResolver = null;
         try {
 			entityResolver = new EntityResolverImpl(new File(cachePath));
 		} catch (XBRLException e) {
 			fail(e.getMessage());
 		}
         reader.setEntityResolver(entityResolver);
 		
 		try {
             reader.parse(uri.toString());
         } catch (SAXException e) {
             fail(e.getMessage());
 		} catch (IOException e) {
 		    e.printStackTrace();
 			fail(e.getMessage());
 		} finally {
 			try {
 				CacheImpl cache = new CacheImpl(new File(cachePath));
 				cache.purge(new URI(uri));
 			} catch (Exception e) {
 			    e.printStackTrace();
 				fail(e.getMessage());
 			}
 		}
 	}
 	
 	class ContentHandler extends DefaultHandler {
 		
 	    public void startDocument() throws SAXException 
 		{
 	    	;
 	    }
 	    
 	    public void startElement( String namespaceURI, String lName, String qName, Attributes attrs) throws SAXException 
 		{
 	        ;
 		}
 		
 	}
 	
 	
 }

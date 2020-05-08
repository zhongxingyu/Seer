 package org.xbrlapi.sax.identifiers.tests;
 
 import java.net.URI;
 import java.util.List;
 
 import org.xbrlapi.AttributeDeclaration;
 import org.xbrlapi.AttributeGroupDeclaration;
 import org.xbrlapi.ComplexTypeDeclaration;
 import org.xbrlapi.ElementDeclaration;
 import org.xbrlapi.Fragment;
 import org.xbrlapi.Schema;
 import org.xbrlapi.SchemaSequenceCompositor;
 import org.xbrlapi.SimpleTypeDeclaration;
 import org.xbrlapi.data.dom.tests.BaseTestCase;
 import org.xbrlapi.utilities.Constants;
 
 /**
 * Test the loader implementation.
  * @author Geoffrey Shuetrim (geoff@galexy.net)
  */
 public class SchemaIdentifierTestCase extends BaseTestCase {
 	
 	private final String STARTING_POINT = "real.data.xbrl.instance.schema";
 	
 	protected void setUp() throws Exception {
 		super.setUp();
 	}
 	
 	public SchemaIdentifierTestCase(String arg0) {
 		super(arg0);
 	}
 	
 	/**
 	 * Test basic fragment identification
 	 */
 	public void testSchemaFragmentIdentification() {
 		try {
 	        URI uri = getURI(this.STARTING_POINT);
 			loader.discover(uri);
 			List<Schema> schemas = store.<Schema>getXMLResources("Schema");
 			assertTrue(schemas.size() > 0);
             List<AttributeDeclaration> attributes = store.<AttributeDeclaration>getXMLResources("AttributeDeclaration");
             assertTrue(attributes.size() > 0);
             List<SimpleTypeDeclaration> simpleTypes = store.<SimpleTypeDeclaration>getXMLResources("SimpleTypeDeclaration");
             assertTrue(simpleTypes.size() > 0);            
             List<ComplexTypeDeclaration> complexTypes = store.<ComplexTypeDeclaration>getXMLResources("ComplexTypeDeclaration");
             assertTrue(complexTypes.size() > 0);
             List<AttributeGroupDeclaration> attributeGroups = store.<AttributeGroupDeclaration>getXMLResources("AttributeGroupDeclaration");
             assertTrue(attributeGroups.size() > 0);
             List<ElementDeclaration> elementDeclarations = store.<ElementDeclaration>getXMLResources("ElementDeclaration");
             assertTrue(elementDeclarations.size() > 0);
             List<SchemaSequenceCompositor> ssc = store.<SchemaSequenceCompositor>getXMLResources("SchemaSequenceCompositor");
             assertTrue(ssc.size() > 0);
             
             ComplexTypeDeclaration type = store.<ComplexTypeDeclaration>getSchemaContent(Constants.XBRL21Namespace,"fractionItemType");
             List<Fragment> children = type.getAllChildren();
             assertTrue(children.size() > 0);
             
             ElementDeclaration element = store.<ElementDeclaration>getSchemaContent(Constants.XBRL21Namespace,"identifier");
             assertTrue(element.hasLocalComplexType());
             ComplexTypeDeclaration ctd = element.getLocalComplexType();
             assertNotNull(ctd);
             ctd.serialize();
 
             
 		} catch (Exception e) {
 		    e.printStackTrace();
 			fail("Unexpected " + e.getMessage());
 		}
 	}
 
 }

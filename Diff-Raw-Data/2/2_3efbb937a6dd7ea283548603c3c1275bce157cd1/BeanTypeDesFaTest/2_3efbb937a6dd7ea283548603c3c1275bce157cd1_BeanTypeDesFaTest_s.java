 package org.oddjob.arooa.types;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.custommonkey.xmlunit.XMLTestCase;
 import org.custommonkey.xmlunit.XMLUnit;
 import org.oddjob.arooa.ArooaParseException;
 import org.oddjob.arooa.ArooaType;
 import org.oddjob.arooa.design.DesignElementProperty;
 import org.oddjob.arooa.design.DesignInstance;
 import org.oddjob.arooa.design.DesignListener;
 import org.oddjob.arooa.design.DesignParser;
 import org.oddjob.arooa.design.DesignStructureEvent;
 import org.oddjob.arooa.design.DynamicDesignInstance;
 import org.oddjob.arooa.design.GenericDesignFactory;
 import org.oddjob.arooa.design.MappedDesignProperty;
 import org.oddjob.arooa.design.ParsableDesignInstance;
 import org.oddjob.arooa.design.view.ViewMainHelper;
 import org.oddjob.arooa.life.SimpleArooaClass;
 import org.oddjob.arooa.standard.StandardArooaSession;
 import org.oddjob.arooa.xml.XMLArooaParser;
 import org.oddjob.arooa.xml.XMLConfiguration;
 import org.xml.sax.SAXException;
 
 public class BeanTypeDesFaTest extends XMLTestCase {
 
 	private static final Logger logger = 
 			Logger.getLogger(BeanTypeDesFa.class);
 	
 	DesignInstance design;
 	
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		
 		logger.info("-----------------------------------  " +
 				getName() + "  ----------------------------------");
 	}
 	
 	public void testDesign() throws Exception {
 		
 		DesignParser parser = new DesignParser(
 				new StandardArooaSession(),
 				new BeanTypeDesFa());
 		
 		String xml = 
 				"<bean class='org.oddjob.arooa.deploy.ArooaDescriptorBean'>" +
 				" <components>" +
 				"  <is/>" +
 				" </components>" +
 				"</bean>";
 		
 		parser.parse(new XMLConfiguration("TEST", xml));
 		
 		design = parser.getDesign();
 		
 		XMLArooaParser xmlParser = new XMLArooaParser();
 		
 		xmlParser.parse(design.getArooaContext().getConfigurationNode());
 		
 		String expected = 
 				"<bean class='org.oddjob.arooa.deploy.ArooaDescriptorBean'>" +
 				" <components>" +
 				"  <is/>" +
 				" </components>" +
 				"</bean>";
 		
 		XMLUnit.setIgnoreWhitespace(true);
 		
 		assertXMLEqual(expected, xmlParser.getXml());
 		
 	}
 	
 	
 	public void testDesignComponent() throws Exception {
 		
 		DesignParser parser = new DesignParser(
 				new StandardArooaSession(),
 				new BeanTypeDesFa());
 		
 		parser.setArooaType(ArooaType.COMPONENT);
 		
 		String xml = 
 				"<bean class='org.oddjob.arooa.deploy.ArooaDescriptorBean'" +
 				"      id='mybean'>" +
 				" <components>" +
 				"  <is/>" +
 				" </components>" +
 				"</bean>";
 		
 		parser.parse(new XMLConfiguration("TEST", xml));
 		
 		design = parser.getDesign();
 		
 		XMLArooaParser xmlParser = new XMLArooaParser();
 		
 		xmlParser.parse(design.getArooaContext().getConfigurationNode());
 		
 		String expected = 
 				"<bean class='org.oddjob.arooa.deploy.ArooaDescriptorBean'" +
 				"      id='mybean'>" +
 				" <components>" +
 				"  <is/>" +
 				" </components>" +
 				"</bean>";
 		
 		XMLUnit.setIgnoreWhitespace(true);
 		
 		assertXMLEqual(expected, xmlParser.getXml());
 	}
 	
 	public void testNoSettableProperties() throws Exception {
 		
 		DesignParser parser = new DesignParser(
 				new StandardArooaSession(),
 				new BeanTypeDesFa());
 		
 		parser.setArooaType(ArooaType.COMPONENT);
 		
 		String xml = 
 				"<bean class='java.lang.Object'" +
 				"      id='mybean'/>";
 		
 		parser.parse(new XMLConfiguration("TEST", xml));
 		
 		design = parser.getDesign();
 		
 		XMLArooaParser xmlParser = new XMLArooaParser();
 		
 		xmlParser.parse(design.getArooaContext().getConfigurationNode());
 		
 		String expected = 
 				"<bean class='java.lang.Object'" +
 				"      id='mybean'/>";
 		
 		XMLUnit.setIgnoreWhitespace(true);
 		
 		assertXMLEqual(expected, xmlParser.getXml());
 	}
 	
 	public static class Stuff {
 		
 		public void setThings(String key, Object thing) {}
 		
 	}
 	
 	private class OurListener implements DesignListener {
 		List<DesignInstance> children = new ArrayList<DesignInstance>();
 		
 		public void childAdded(DesignStructureEvent event) {
 			children.add(event.getIndex(), event.getChild());
 		}
 		
 		public void childRemoved(DesignStructureEvent event) {
 			children.remove(event.getIndex());
 		}
 	}	
 	
 	public void testMappedProperties() throws ArooaParseException, SAXException, IOException {
 		
 		String xml =
 				"<stuff>" +
 				" <things>" +
 				"  <bean key='something' class='java.lang.Object'/>" +
 				" </things>" +
 				"</stuff>";
 		
 		DesignParser parser = new DesignParser(new GenericDesignFactory(
 				new SimpleArooaClass(Stuff.class)));
 		parser.parse(new XMLConfiguration("TEST", xml));
 		
 		ParsableDesignInstance top = 
 				(ParsableDesignInstance) parser.getDesign();
 		
 		DesignElementProperty things = 
 				(DesignElementProperty) top.children()[0];
 		
 		OurListener ourListener = new OurListener();
 		
 		things.addDesignListener(ourListener);
 		
 		MappedDesignProperty.InstanceWrapper wrapper = 
 				(MappedDesignProperty.InstanceWrapper) 
 				ourListener.children.get(0);
 		
 		DynamicDesignInstance test = (DynamicDesignInstance) 
 				wrapper.getWrapping();
 		
 		test.setClassName("java.lang.String");
 		
 		String expected =
 				"<stuff>" +
 				" <things>" +
 				"  <bean key='something' class='java.lang.String'/>" +
 				" </things>" +
 				"</stuff>";
 		
 		XMLArooaParser xmlParser = new XMLArooaParser();
 		xmlParser.parse(top.getArooaContext().getConfigurationNode());
 
 		String actual = xmlParser.getXml();
 		
 		logger.info(actual);
 		
 		assertXMLEqual(expected, actual);
 	}
 	
 	
 	public static void main(String args[]) throws Exception {
 
 		BeanTypeDesFaTest test = new BeanTypeDesFaTest();
 		test.testNoSettableProperties();
 		
 		ViewMainHelper helper = new ViewMainHelper(test.design);
 		helper.run();
 	}
 }

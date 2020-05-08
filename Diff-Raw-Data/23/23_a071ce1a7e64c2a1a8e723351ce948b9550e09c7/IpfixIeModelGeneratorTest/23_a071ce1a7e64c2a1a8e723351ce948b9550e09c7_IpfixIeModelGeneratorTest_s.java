 package de.fhg.fokus.net.ipfix.mgen;
 
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.thoughtworks.xstream.XStream;
 
 import de.fhg.fokus.net.ipfix.mojo.mgen.IanaRecord;
 import de.fhg.fokus.net.ipfix.mojo.mgen.IanaRegistry;
 import de.fhg.fokus.net.ipfix.mojo.mgen.IpfixIeModelGenerator;
 import de.fhg.fokus.net.ipfix.mojo.mgen.TextTemplate;
 import de.fhg.fokus.net.ipfix.mojo.mgen.tmpl.IpfixIeTmpl;
 
 public class IpfixIeModelGeneratorTest {
 	private final static Logger logger = LoggerFactory.getLogger(IpfixIeModelGeneratorTest.class);
 	private final static File TEST_DIR = new File("target","test-classes");
 	private static XStream xstream;
 	public static void setupXmlReader(){
 		xstream = new XStream();
 		xstream.alias("registry", IanaRegistry.class);
 		xstream.alias("record", IanaRecord.class);
 		xstream.alias("xref", String.class);
 		xstream.addImplicitCollection(IanaRegistry.class, "records",
 				IanaRecord.class);
 		xstream.addImplicitCollection(IanaRegistry.class, "children",
 				IanaRegistry.class);
 		xstream.addImplicitCollection(IanaRecord.class, "xrefs");
 		xstream.aliasAttribute(IanaRegistry.class, "id", "id");
 //		xstream.processAnnotations(IanaRegistry.class);
 	}
 	@BeforeClass
 	public static void setUpBeforeClass() throws IOException {
 		logger.debug("Setup");
 		setupXmlReader();
 		
 	}
 	@Test
 	public void testGenerateEnterprise() throws IOException{
 		logger.debug("IPFIX IE model generator test: ENTERPRISE");
 //		File modeFile= new File(TEST_DIR,"prism.xml");
		File modeFile= new File(TEST_DIR,"pt.xml");
 		
 		
 		TextTemplate txtTemplate = new TextTemplate(IpfixIeTmpl.FILE);
 		FileInputStream fis = new FileInputStream(modeFile );
 		IanaRegistry ianaRegistry = (IanaRegistry) xstream.fromXML(fis);
 		
 		IpfixIeModelGenerator gen = new IpfixIeModelGenerator(txtTemplate, ianaRegistry
		, "de.fhg.fokus.net.ipfix.model.ie.fokus.pt",new File("target","generated-sources"));
 		gen.generate();
 	}
 	@Test
 	public void testGenerateRFC() throws IOException{
 		logger.debug("IPFIX IE model generator test: RFC ");
 		File modeFile= new File(TEST_DIR,"ipfix.xml");
 		
 		TextTemplate txtTemplate = new TextTemplate(IpfixIeTmpl.FILE);
 		FileInputStream fis = new FileInputStream(modeFile );
 		IanaRegistry ianaRegistry = (IanaRegistry) xstream.fromXML(fis);
 		
 		IpfixIeModelGenerator gen = new IpfixIeModelGenerator(txtTemplate, ianaRegistry
 		, "de.fhg.fokus.net.ipfix.model.ie",new File("target","generated-sources"));
 		gen.generate();
 	}
 
 	@Before
 	public void setUp() throws Exception {
 	}
 
 	@After
 	public void tearDown() throws Exception {
 	}
 
 }

 package cz.cuni.mff.odcleanstore.linker.impl;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import cz.cuni.mff.odcleanstore.configuration.ObjectIdentificationConfig;
 import cz.cuni.mff.odcleanstore.configuration.exceptions.ConfigurationException;
 import cz.cuni.mff.odcleanstore.data.RDFprefix;
 import cz.cuni.mff.odcleanstore.linker.rules.Output;
 import cz.cuni.mff.odcleanstore.linker.rules.SilkRule;
 import cz.cuni.mff.odcleanstore.transformer.TransformationContext;
 import cz.cuni.mff.odcleanstore.transformer.TransformedGraph;
 import cz.cuni.mff.odcleanstore.transformer.TransformerException;
 
 import org.junit.Test;
 import org.mockito.Mockito;
 import org.w3c.dom.Document;
 import org.xml.sax.SAXException;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import java.io.File;
 import java.io.IOException;
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 
 public class ConfigBuilderTest {
 
 	private static final String RULE_LABEL = "testRule";
 	private static final String RULE_TYPE = "owl:sameAs";
 	private static final String RULE_SOURCE_RESTRICTION = "?a rdf:type foo .";
 	private static final String RULE_TARGET_RESTRICTION = "?b rdf:type poo .";
 	private static final Integer RULE_FILTER_LIMIT = 5;
 	private static final BigDecimal RULE_FILTER_THRESHOLD = new BigDecimal("0.9");
 	private static final String RULE_LINKAGE_RULE = "<LinkageRule>ruleContent</LinkageRule>";
 	private static final BigDecimal RULE_MIN_CONFIDENCE = new BigDecimal("0.95");
 	private static final BigDecimal RULE_MAX_CONFIDENCE = new BigDecimal("0.98");
 
 	@Test
 	public void testCreateConfigFile() throws TransformerException, ParserConfigurationException, SAXException,
 	IOException, ConfigurationException {
 		List<SilkRule> rules = new ArrayList<SilkRule>();
 		SilkRule rule = new SilkRule();
 		rule.setLabel(RULE_LABEL);
 		rule.setLinkType(RULE_TYPE);
 		rule.setSourceRestriction(RULE_SOURCE_RESTRICTION);
 		rule.setTargetRestriction(RULE_TARGET_RESTRICTION);
 		rule.setFilterLimit(RULE_FILTER_LIMIT);
 		rule.setFilterThreshold(RULE_FILTER_THRESHOLD);
 		rule.setLinkageRule(RULE_LINKAGE_RULE);
 
 		List<Output> outputs = new ArrayList<Output>();
 		Output output = new Output();
 		output.setMinConfidence(RULE_MIN_CONFIDENCE);
 		output.setMaxConfidence(RULE_MAX_CONFIDENCE);
 		outputs.add(output);
 		rule.setOutputs(outputs);
 
 		rules.add(rule);
 
 		List<RDFprefix> prefixes = new ArrayList<RDFprefix>();
 		prefixes.add(new RDFprefix("rdf:", "http://www.w3.org/1999/02/22-rdf-syntax-ns#"));
 		prefixes.add(new RDFprefix("rdfs:", "http://www.w3.org/2000/01/rdf-schema#"));
 
 		TransformedGraph graph = new TransformedGraphMock("http://odcs.mff.cuni.cz/transformedGraph");
 		TransformationContext context = new TransformationContextMock("target/linkerTest");
 
 		Properties properties = Mockito.mock(Properties.class);
 		Mockito.when(properties.getProperty(ObjectIdentificationConfig.GROUP_PREFIX + "links_graph_uri_prefix")).thenReturn("http://www.seznam.cz");
 		Mockito.when(properties.getProperty(ObjectIdentificationConfig.GROUP_PREFIX + "link_within_graph")).thenReturn("false");
        Mockito.when(properties.getProperty("db.clean.sparql.endpoint_url")).thenReturn("http://www.google.cz");
        Mockito.when(properties.getProperty("db.dirty_update.sparql.endpoint_url")).thenReturn("http://www.yahoo.com");
        Mockito.when(properties.getProperty("db.dirty_update.sparql.endpoint_username")).thenReturn("Pepa");
	    Mockito.when(properties.getProperty("db.dirty_update.sparql.endpoint_password")).thenReturn("heslo");
 	    ObjectIdentificationConfig config = ObjectIdentificationConfig.load(properties);
 
 	    DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
 		File configFile = ConfigBuilder.createLinkConfigFile(rules, prefixes, graph,
 				context, config, false);
 		Document configDoc = builder.parse(configFile);
 
 		File expectedFile = new File("src/test/resources/expectedLinkConfig.xml");
 		Document expectedDoc = builder.parse(expectedFile);
 
 		assertTrue(configDoc.isEqualNode(expectedDoc));
 	}
 
 	@Test
 	public void testParseRule()
 			throws javax.xml.transform.TransformerException, ParserConfigurationException, SAXException, IOException {
 
 		SilkRule rule = ConfigBuilder.parseRule(new File("src/test/resources/expectedLinkConfig.xml"));
 		assertEquals(RULE_TYPE, rule.getLinkType());
 		assertEquals(RULE_SOURCE_RESTRICTION, rule.getSourceRestriction());
 		assertEquals(RULE_TARGET_RESTRICTION, rule.getTargetRestriction());
 		assertEquals(RULE_FILTER_LIMIT, rule.getFilterLimit());
 		assertEquals(RULE_FILTER_THRESHOLD, rule.getFilterThreshold());
 		assertEquals(RULE_LINKAGE_RULE, rule.getLinkageRule());
 		assertEquals(RULE_MIN_CONFIDENCE, rule.getOutputs().get(0).getMinConfidence());
 		assertEquals(RULE_MAX_CONFIDENCE, rule.getOutputs().get(0).getMaxConfidence());
 	}
 }

 package cytoscape.data.readers;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import junit.framework.TestCase;
 import cytoscape.CyNetwork;
 import cytoscape.Cytoscape;
 import cytoscape.data.CyAttributes;
 import static cytoscape.data.readers.TextFileDelimiters.*;
 
 public class DefaultAttributeTableReaderTest extends TestCase {
 
 	private DefaultAttributeTableReader tableReader;
 
 	/*
 	 * Toy example created from galFiltered.sif and its attribute files.
 	 */
 	private static final String DATA_FILE = "testData/annotation/galSubnetworkAnnotation2.txt";
 	private static final String NETWORK_FILE = "testData/galSubnetwork.sif";
 
 	private static final String DATA_FILE2 = "testData/annotation/annotationSampleForYeast.txt";
 	private static final String NETWORK_FILE2 = "testData/galFiltered.sif";
 
 	protected void setUp() throws Exception {
 		super.setUp();
 		Cytoscape.buildOntologyServer();
 	}
 
 	protected void tearDown() throws Exception {
 		super.tearDown();
 
 	}
 
 	public void testReadTable() throws Exception {
 		File network = new File(NETWORK_FILE);
 		File galNetwork = new File(NETWORK_FILE2);
 		//
 		CyNetwork net = Cytoscape.createNetworkFromFile(network
 				.getAbsolutePath());
 		File source = new File(DATA_FILE);
 
 		CyNetwork galNet = Cytoscape.createNetworkFromFile(galNetwork
 				.getAbsolutePath());
 
 		File galSource = new File(DATA_FILE2);
 
 		/*
 		 * Test1
 		 */
 		List<String> delimiters = new ArrayList<String>();
 		delimiters.add(TextFileDelimiters.TAB.toString());
 
 		List<Integer> aliasList = new ArrayList<Integer>();
 		aliasList.add(2);
 
 		String[] galAttrName = { "ID", "ID in SGD", "Synonyms",
 				"Description of Genes", "Date", "Sample Boolean Attr2",
 				"gal1RGexp", "gal1RGsig", "String List"};
 		byte[] galAttrTypes = { CyAttributes.TYPE_STRING,
 				CyAttributes.TYPE_STRING, CyAttributes.TYPE_STRING,
 				CyAttributes.TYPE_STRING, CyAttributes.TYPE_INTEGER,
 				CyAttributes.TYPE_BOOLEAN, CyAttributes.TYPE_FLOATING,
 				CyAttributes.TYPE_FLOATING, CyAttributes.TYPE_SIMPLE_LIST };
 
 		for (int i = 0; i < galAttrTypes.length; i++) {
 			System.out.println("GAL Data Type " + i + " = " + galAttrTypes[i]);
 		}
 
 		AttributeMappingParameters mapping = new AttributeMappingParameters(
 				TextTableReader.ObjectType.NODE, null, COMMA.toString(), 0, "ID", aliasList,
 				galAttrName, galAttrTypes, null);
		tableReader = new DefaultAttributeTableReader(source.toURL(), mapping);
 		tableReader.readTable();
 
 		assertEquals("ribosomal protein S28A (S33A) (YS27)", Cytoscape
 				.getNodeAttributes().getStringAttribute("YOR167C",
 						"Description of Genes"));
 		assertEquals(new Integer(20010118), Cytoscape.getNodeAttributes()
 				.getIntegerAttribute("YHR141C", "Date"));
 		assertEquals(4, Cytoscape.getNodeAttributes()
 				.getAttributeList("YER112W", "alias").size());
 		assertEquals(7, Cytoscape.getNodeAttributes()
 				.getAttributeList("YDR277C", "String List").size());
 
 		assertEquals("List", Cytoscape.getNodeAttributes()
 				.getAttributeList("YDR277C", "String List").get(5));
 
 		/*
 		 * Test2
 		 */
 
 		List<Integer> aliases = new ArrayList<Integer>();
 		aliases.add(2);
 		String[] cols = { "Object Name in SGD", "key", "ali", "Taxon ID" };
 
 		tableReader = new DefaultAttributeTableReader(galSource.toURL(),
 				TextTableReader.ObjectType.NODE, delimiters, PIPE.toString(), 1, "ID",
				aliases, cols, null, null);
 		tableReader.readTable();
 
 		System.out.println("* YOL123W Object Name in SGD = "
 				+ Cytoscape.getNodeAttributes().getStringAttribute("YOL123W",
 						"Object Name in SGD"));
 		assertEquals("S000005483", Cytoscape.getNodeAttributes()
 				.getStringAttribute("YOL123W", "Object Name in SGD"));
 		assertEquals("S000006010", Cytoscape.getNodeAttributes()
 				.getStringAttribute("YPL089C", "Object Name in SGD"));
 		assertEquals("taxon:4932", Cytoscape.getNodeAttributes()
 				.getStringAttribute("YDR009W", "Taxon ID"));
 		assertTrue(Cytoscape.getNodeAttributes().getAttributeList("YOR315W",
 				"alias").contains("SFG1"));
 
 		assertTrue(Cytoscape.getOntologyServer().getNodeAliases().getAliases(
 				"YLR319C").contains("AIP3"));
 
 		Cytoscape.destroyNetwork(galNet);
 		Cytoscape.destroyNetwork(net);
 	}
 }

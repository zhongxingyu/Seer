 package cytoscape.data.readers;
 import static cytoscape.data.readers.TextFileDelimiters.COMMA;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import junit.framework.TestCase;
 
 import org.apache.poi.hssf.usermodel.HSSFSheet;
 import org.apache.poi.hssf.usermodel.HSSFWorkbook;
 import org.apache.poi.poifs.filesystem.POIFSFileSystem;
 
 import cytoscape.CyNetwork;
 import cytoscape.Cytoscape;
 import cytoscape.data.CyAttributes;
 import cytoscape.data.readers.AttributeMappingParameters;
 import cytoscape.data.readers.ExcelAttributeSheetReader;
 import cytoscape.data.readers.TextFileDelimiters;
 import cytoscape.data.readers.TextTableReader;
 
 public class ExcelAttributeSheetReaderTest extends TestCase {
 
 	private static final String WORKBOOK1 = "testData/annotation/galSubnetworkAnnotation3.xls";
 	private static final String NETWORK_FILE = "testData/galSubnetwork.sif";
 
 	protected void setUp() throws Exception {
 		super.setUp();
 		Cytoscape.buildOntologyServer();
 	}
 
 	protected void tearDown() throws Exception {
 		super.tearDown();
 	}
 
 	public void testReadTable() throws Exception {
 
 		/*
 		 * Load test network
 		 */
 		File network = new File(NETWORK_FILE);
 		CyNetwork net = Cytoscape.createNetworkFromFile(network
 				.getAbsolutePath());
 
 		/*
 		 * Single Sheet Test
 		 */
 		POIFSFileSystem excelIn = new POIFSFileSystem(new FileInputStream(
 				WORKBOOK1));
 		HSSFWorkbook wb = new HSSFWorkbook(excelIn);
 
 		HSSFSheet sheet = wb.getSheetAt(0);
 
 		List<String> delimiters = new ArrayList<String>();
 		delimiters.add(TextFileDelimiters.TAB.toString());
 
 		List<Integer> aliasList = new ArrayList<Integer>();
 		aliasList.add(2);
 
 		String[] galAttrName = { "ID", "ID in SGD", "Synonyms",
 				"Description of Genes", "Date", "Sample Boolean Attr2",
 				"gal1RGexp", "gal1RGsig", "String List" };
 		byte[] galAttrTypes = { CyAttributes.TYPE_STRING,
 				CyAttributes.TYPE_STRING, CyAttributes.TYPE_STRING,
 				CyAttributes.TYPE_STRING, CyAttributes.TYPE_INTEGER,
 				CyAttributes.TYPE_BOOLEAN, CyAttributes.TYPE_FLOATING,
 				CyAttributes.TYPE_FLOATING, CyAttributes.TYPE_SIMPLE_LIST };
 
 		for (int i = 0; i < galAttrTypes.length; i++) {
 			System.out.println("GAL Data Type " + i + " = " + galAttrTypes[i]);
 		}
 
 		AttributeMappingParameters mapping = new AttributeMappingParameters(
 				TextTableReader.ObjectType.NODE, null, COMMA.toString(), 0,
 				"ID", aliasList, galAttrName, galAttrTypes, null);
 
		TextTableReader rd = new ExcelAttributeSheetReader(sheet, mapping, 0);
 		rd.readTable();
 
 		assertEquals("ribosomal protein S28A (S33A) (YS27)", Cytoscape
 				.getNodeAttributes().getStringAttribute("YOR167C",
 						"Description of Genes"));
 		assertEquals(new Integer(20010118), Cytoscape.getNodeAttributes()
 				.getIntegerAttribute("YHR141C", "Date"));
 		assertEquals(4, Cytoscape.getNodeAttributes().getAttributeList(
 				"YER112W", "alias").size());
 //		assertEquals(7, Cytoscape.getNodeAttributes().getAttributeList(
 //				"YDR277C", "String List").size());
 //
 //		assertEquals("List", Cytoscape.getNodeAttributes().getAttributeList(
 //				"YDR277C", "String List").get(5));
 
 		/*
 		 * Multiple sheet test (not yet supported)
 		 */
 
 		Cytoscape.destroyNetwork(net);
 	}
 
 }

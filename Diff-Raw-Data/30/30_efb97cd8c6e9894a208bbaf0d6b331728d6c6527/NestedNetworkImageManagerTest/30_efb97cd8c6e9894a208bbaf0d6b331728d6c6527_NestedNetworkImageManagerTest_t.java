 package cytoscape.util;
 
 import junit.framework.TestCase;
import java.util.List;
 import cytoscape.Cytoscape;
import cytoscape.CyNode;
 import cytoscape.data.readers.NNFReader;
 

 public class NestedNetworkImageManagerTest extends TestCase {
 	private static final String FILE_LOCATION = "testData/NNFData/";
 

 	protected void setUp() throws Exception {
 		super.setUp();

 		// Instantiate Nested Network Image Manager singleton
 		NestedNetworkImageManager.getNetworkImageGenerator();
 	}
 

 	protected void tearDown() throws Exception {
 		super.tearDown();
 	}

 	
 	public void testNestedNetworkImageManager() throws Exception {
 		TestUtil.destroyNetworksEdgesAndNodes();

 		// Load nested network
 		final NNFReader reader = new NNFReader(FILE_LOCATION + "good3.nnf");
 		reader.read();
 		
		final CyNode m1 = Cytoscape.getCyNode("M1");
		assertNotNull(m1);
 		assertNotNull(Cytoscape.getCyNode("M2"));
 		assertNotNull(Cytoscape.getCyNode("M3"));
 		assertEquals(3, NestedNetworkImageManager.getNetworkImageGenerator().getImageCount());
 
		m1.setNestedNetwork(null);
		assertEquals(2, NestedNetworkImageManager.getNetworkImageGenerator().getImageCount());	
	}
 }

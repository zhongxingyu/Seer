 package ch.hsr.bieridee.test;
 
 import org.junit.Assert;
 import org.junit.Test;
 import org.neo4j.graphdb.Node;
 
 import ch.hsr.bieridee.models.BeerModel;
 import ch.hsr.bieridee.utils.DBUtil;
 
 /**
  * Tests the <code>BeerModel</code>.
  * 
  * @author jfurrer
  * 
  */
 public class BeerModelTest {
 
 	/**
 	 * TEST.
 	 */
 	@Test
 	public void getNodeTest() {
 
		final Node n = DBUtil.getNodeById(4);
		final BeerModel bm = new BeerModel(4);
 		final Node beernode = bm.getNode();
 
 		Assert.assertEquals(n, beernode);
 	}
 
 }

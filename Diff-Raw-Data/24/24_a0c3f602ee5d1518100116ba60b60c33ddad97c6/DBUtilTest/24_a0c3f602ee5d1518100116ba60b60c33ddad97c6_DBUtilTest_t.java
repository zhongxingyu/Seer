 package ch.hsr.bieridee.test;
 
 import java.util.List;
 
 import org.junit.Assert;
 import org.junit.Test;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.NotFoundException;
 import org.neo4j.server.rest.web.NodeNotFoundException;
 
 import ch.hsr.bieridee.exceptions.WrongNodeTypeException;
 import ch.hsr.bieridee.models.BeerModel;
 import ch.hsr.bieridee.models.TagModel;
 import ch.hsr.bieridee.utils.DBUtil;
 
 /**
  * Tests DBUtil functionality.
  * 
  */
 public class DBUtilTest extends DBTest {
 
 	/**
 	 * Tests if a beer could be obtained by name.
 	 * 
 	 */
 	@Test
 	public void getBeerByNameTest() {
 		final String beerName = "Falken First Cool";
 		BeerModel bm1 = null;
 		final Node beerNode = DBUtil.getBeerByName(beerName);
 		try {
 			bm1 = new BeerModel(beerNode);
 		} catch (NotFoundException e) {
 			Assert.fail();
 			e.printStackTrace();
 		} catch (WrongNodeTypeException e) {
 			Assert.fail();
 			e.printStackTrace();
 		}
 
 		Assert.assertNotNull(bm1);
 		Assert.assertTrue(bm1.getName().equals(beerName));
 	}
 
 	/**
 	 * @throws WrongNodeTypeException
 	 * @throws NotFoundException
 	 * 
 	 */
 	@Test(expected = NotFoundException.class)
 	public void getNotExistingBeerByNameTest() throws NotFoundException, WrongNodeTypeException {
 		final String beerName = "Chrigis Super Brew!";
 		final Node beerNode = DBUtil.getBeerByName(beerName);
 		System.out.println(beerNode);
 		final BeerModel bm1 = new BeerModel(beerNode);
 		Assert.assertNull(beerNode);
 	}
 
 	/**
 	 * Test the Filter by Tag Function of DBUtil.
 	 * 
 	 */
 	@Test
 	public void getBeersByTag() {
 
 		BeerModel bm1 = null;
 		BeerModel bm2 = null;
 		BeerModel bm3 = null;
 		try {
 			bm1 = new BeerModel(31);
 			bm2 = new BeerModel(32);
 			bm3 = new BeerModel(30);
 		} catch (NotFoundException e1) {
 			Assert.fail();
 			e1.printStackTrace();
 		} catch (WrongNodeTypeException e1) {
 			Assert.fail();
 			e1.printStackTrace();
 		}
 
 		final Node tagNode = DBUtil.getTagByName("wuerzig");
 		TagModel tm = null;
 		try {
 			tm = new TagModel(tagNode);
 		} catch (NotFoundException e) {
 			Assert.fail();
 			e.printStackTrace();
 		} catch (WrongNodeTypeException e) {
 			Assert.fail();
 			e.printStackTrace();
 		}
 
 		List<BeerModel> beerModels = null;
 		try {
			beerModels = BeerModel.getAll(tm.getName());
 		} catch (WrongNodeTypeException e) {
 			Assert.fail();
 			e.printStackTrace();
 		}
 
 		Assert.assertTrue(beerModels.contains(bm1));
 		Assert.assertTrue(beerModels.contains(bm2));
 		Assert.assertTrue(!beerModels.contains(bm3));
 
 	}
 
 }

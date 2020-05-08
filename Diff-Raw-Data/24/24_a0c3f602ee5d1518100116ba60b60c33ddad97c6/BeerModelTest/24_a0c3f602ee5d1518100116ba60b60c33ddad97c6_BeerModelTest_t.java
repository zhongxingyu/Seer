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
  * Tests the <code>BeerModel</code>.
  */
 public class BeerModelTest extends DBTest {
 
 	/**
 	 * TEST.
 	 * 
 	 * @throws WrongNodeTypeException
 	 */
 
 	@Test
 	public void getBeersByTag() {
 
 		BeerModel bm1 = null;
 		BeerModel bm2 = null;
 		try {
 			bm1 = new BeerModel(36);
 			bm2 = new BeerModel(32);
 		} catch (NotFoundException e1) {
 			e1.printStackTrace();
 		} catch (WrongNodeTypeException e1) {
 			e1.printStackTrace();
 		}
 
 		final Node tagNode = DBUtil.getTagByName("wuerzig");
 		TagModel tm = null;
 		try {
 			tm = new TagModel(tagNode);
 		} catch (NotFoundException e) {
 			e.printStackTrace();
 		} catch (WrongNodeTypeException e) {
 			e.printStackTrace();
 		}
 
 		List<BeerModel> beerModels = null;
 		try {
			beerModels = BeerModel.getAll(tm.getName());
 		} catch (WrongNodeTypeException e) {
 			e.printStackTrace();
 		}
 
 		Assert.assertTrue(beerModels.contains(bm1));
 		Assert.assertTrue(beerModels.contains(bm2));
 
 	}
 }

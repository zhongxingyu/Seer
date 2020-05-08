 package ch.hsr.bieridee.test;
 
 import org.junit.Assert;
 import org.junit.Test;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.NotFoundException;
 
 import ch.hsr.bieridee.exceptions.WrongNodeTypeException;
 import ch.hsr.bieridee.models.RatingModel;
 import ch.hsr.bieridee.utils.DBUtil;
 
public class RatingModelTest {
 	@Test
 	public void getRatingByNodeTest() {
		Node ratingNode = DBUtil.getNodeById(54);
 		RatingModel rm = null;
 		try {
 			rm = new RatingModel(ratingNode);
 		} catch (NotFoundException e) {
			Assert.fail();
 			e.printStackTrace();
		} catch (WrongNodeTypeException e) {
 			Assert.fail();
 			e.printStackTrace();
 		}
 
 		Assert.assertNotNull(rm);
 		Assert.assertEquals(rm.getRating(), 1);
 		Assert.assertEquals(rm.getNode(), ratingNode);
 //		Assert.assertEquals(1335460591430, rm.getDomainObject().getDate().getTime());
 	}
 
 }

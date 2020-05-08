 package ch.hsr.bieridee.test;
 
 import org.junit.Assert;
 import org.junit.Test;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.NotFoundException;
 
 import ch.hsr.bieridee.exceptions.WrongNodeTypeException;
 import ch.hsr.bieridee.models.RatingModel;
 import ch.hsr.bieridee.utils.DBUtil;
 
public class RatingModelTest extends DBTest{
 	@Test
 	public void getRatingByNodeTest() {
		System.out.println("begin");
		final Node ratingNode = DBUtil.getNodeById(54);
		System.out.println("rating node:"+ratingNode);
 		RatingModel rm = null;
 		try {
 			rm = new RatingModel(ratingNode);
 		} catch (NotFoundException e) {
 			e.printStackTrace();
 			Assert.fail();
		} catch (WrongNodeTypeException e) {
 			e.printStackTrace();
			Assert.fail();
 		}
 
 		Assert.assertNotNull(rm);
 		Assert.assertEquals(rm.getRating(), 1);
 		Assert.assertEquals(rm.getNode(), ratingNode);
 //		Assert.assertEquals(1335460591430, rm.getDomainObject().getDate().getTime());
 	}
 
 }

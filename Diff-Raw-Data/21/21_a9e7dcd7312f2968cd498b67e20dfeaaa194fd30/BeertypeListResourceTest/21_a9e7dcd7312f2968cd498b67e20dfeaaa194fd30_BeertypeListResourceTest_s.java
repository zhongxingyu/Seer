 package ch.hsr.bieridee.test.resources;
 
 import java.io.IOException;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.junit.Assert;
 import org.junit.Test;
 import org.neo4j.graphdb.NotFoundException;
 import org.restlet.representation.Representation;
 
 import ch.hsr.bieridee.config.Res;
 import ch.hsr.bieridee.exceptions.WrongNodeTypeException;
 import ch.hsr.bieridee.models.BeertypeModel;
 
 /**
  * Tests for the beer consumptions.
  * 
  */
 public class BeertypeListResourceTest extends ResourceTest {
 
 	/**
 	 * Tests the creation of a new beertype.
 	 */
 	@Test
 	public void createBeertype() {
 		final String uri = Res.PUBLIC_API_URL + Res.BEERTYPE_COLLECTION;
 		final JSONObject beertype = new JSONObject();
 		final String name = "BestesBier";
 		final String description = "Das Beste Bier überhaupt wird im Zürcher Oberland gebraut!";
 		try {
 			beertype.put("name", name);
 			beertype.put("description", description);
 		} catch (JSONException e) {
 			e.printStackTrace();
 			Assert.fail();
 		}
 
		final Representation beertypeRep = this.postJson(uri, beertype);
 		try {
			final JSONObject responseObject = new JSONObject(beertypeRep.getText());
 			final BeertypeModel btm = new BeertypeModel(responseObject.getLong("id"));
 
 			Assert.assertEquals(responseObject.getString("name"), name);
 			Assert.assertEquals(responseObject.getString("description"), description);
 			Assert.assertEquals(Res.getResourceUri(btm), responseObject.getString("uri"));
 		} catch (JSONException e1) {
 			e1.printStackTrace();
 			Assert.fail();
 		} catch (NumberFormatException e) {
 			e.printStackTrace();
 			Assert.fail();
 		} catch (NotFoundException e) {
 			e.printStackTrace();
 			Assert.fail();
		} catch (IOException e) {
			e.printStackTrace();
 		} catch (WrongNodeTypeException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	/**
 	 * Requests all beertypes and checks if the first matches ale beertype.
 	 */
 	@Test
 	public void getAllBeertypes() {
 		final String uri = Res.PUBLIC_API_URL + Res.BEERTYPE_COLLECTION;
 		final JSONArray array = this.getJSONArray(uri);
 		try {
 			final BeertypeModel bm = new BeertypeModel(9);
 			final JSONObject ale = array.getJSONObject(0);
 			Assert.assertEquals(bm.getName(), ale.getString("name"));
 			Assert.assertEquals(bm.getDescription(), ale.getString("description"));
 			Assert.assertEquals(bm.getId(), ale.getLong("id"));
 		} catch (JSONException e) {
 			e.printStackTrace();
 			Assert.fail();
 		} catch (NotFoundException e) {
 			e.printStackTrace();
 			Assert.fail();
 		} catch (WrongNodeTypeException e) {
 			e.printStackTrace();
 			Assert.fail();
 		}
 
 	}
 }

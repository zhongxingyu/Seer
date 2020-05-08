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
 import ch.hsr.bieridee.models.BreweryModel;
 
 /**
  * Tests for the beer consumptions.
  * 
  */
 public class BreweryListResourceTest extends ResourceTest {
 
 	/**
 	 * Tests the creation of a new beertype.
 	 */
 	@Test
 	public void createBrewery() {
 		final String uri = Res.PUBLIC_API_URL + Res.BREWERY_COLLECTION;
 		final JSONObject breweryJSON = new JSONObject();
 		final String name = "Z-Augenbräu";
 		final String description = "Das Beste Brauerei im Zürcher Oberland! Gelegen im schönen idyllischen Rüti.";
 		final String size = "XS";
 		final String picture = "brewery123.jpg";
 
 		try {
 			breweryJSON.put("name", name);
 			breweryJSON.put("description", description);
 			breweryJSON.put("size", size);
 			breweryJSON.put("picture", picture);
 		} catch (JSONException e) {
 			e.printStackTrace();
 			Assert.fail();
 		}
 
		final String breweryJSONString = this.postJson(uri, breweryJSON);
 		try {
			final JSONObject responseObject = new JSONObject(breweryJSONString);
 			System.out.println("brewery id was: "+responseObject.getLong("id"));
 			final BreweryModel bm = new BreweryModel(responseObject.getLong("id"));
 
 			Assert.assertEquals(responseObject.getString("name"), name);
 			Assert.assertEquals(responseObject.getString("description"), description);
 			Assert.assertEquals(responseObject.getString("size"), size);
 			Assert.assertEquals(responseObject.getString("picture"), picture);
 			Assert.assertEquals(Res.getResourceUri(bm), responseObject.getString("uri"));
 		} catch (JSONException e1) {
 			e1.printStackTrace();
 			Assert.fail();
 		} catch (NumberFormatException e) {
 			e.printStackTrace();
 			Assert.fail();
 		} catch (NotFoundException e) {
 			e.printStackTrace();
 			Assert.fail();
 		} catch (WrongNodeTypeException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	/**
 	 * Requests all beertypes and checks if the first matches ale beertype.
 	 */
 	@Test
 	public void getAllBreweries() {
 		final String uri = Res.PUBLIC_API_URL + Res.BREWERY_COLLECTION;
 		final JSONArray array = this.getJSONArray(uri);
 		try {
 			final BreweryModel bm = new BreweryModel(71);
 			System.out.println("brewery: "+bm);
 			final JSONObject locher = array.getJSONObject(0);
 			Assert.assertEquals(bm.getName(), locher.getString("name"));
 			Assert.assertEquals(bm.getDescription(), locher.getString("description"));
 			Assert.assertEquals(bm.getId(), locher.getLong("id"));
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

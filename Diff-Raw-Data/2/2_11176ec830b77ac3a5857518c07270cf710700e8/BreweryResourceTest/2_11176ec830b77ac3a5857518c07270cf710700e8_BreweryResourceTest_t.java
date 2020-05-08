 package ch.hsr.bieridee.test.resources;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.junit.Assert;
 import org.junit.Test;
 
 import ch.hsr.bieridee.config.Res;
 import ch.hsr.bieridee.test.helpers.Helpers;
 import ch.hsr.bieridee.utils.NodeProperty;
 
 /**
  * Brewery resource tests.
  */
 public class BreweryResourceTest extends ResourceTest {
 
 	/**
 	 * Verify that a brewery resource returns a brewery object with all required fields.
 	 */
 	@Test
 	public void testSingleBrewery() {
 		final String uri = Helpers.buildResourceUri("/breweries/66");
 		final JSONObject brewery = this.getJSON(uri);
 		try {
 			Assert.assertEquals(66, brewery.getInt("id"));
			Assert.assertEquals(Res.PUBLIC_API_URL + "/breweries/66", brewery.getString("uri"));
 			Assert.assertEquals("Felschlösschen", brewery.getString(NodeProperty.Brewery.NAME));
 			Assert.assertEquals("national", brewery.getString(NodeProperty.Brewery.SIZE));
 			Assert.assertTrue(brewery.getString(NodeProperty.Brewery.DESCRIPTION).startsWith("Feldschlösschen ist eine gesichtslose und gänzlich uninspirierte Brauerei."));
 			Assert.assertEquals("", brewery.getString(NodeProperty.Brewery.PICTURE));
 		} catch (JSONException e) {
 			e.printStackTrace();
 			Assert.fail();
 		}
 	}
 }

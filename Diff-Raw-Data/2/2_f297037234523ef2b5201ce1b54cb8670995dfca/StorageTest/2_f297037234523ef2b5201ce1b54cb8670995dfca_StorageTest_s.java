 package cs169.project.thepantry.test;
 
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.test.AndroidTestCase;
 import cs169.project.thepantry.Storage;
 
 public class StorageTest extends AndroidTestCase {
 	JSONObject recipeJSON;
 	JSONObject attributionJSON;
 	JSONObject recpImgJSON;
 	JSONObject searchResultJSON;
 	JSONObject searchMatchJSON;
 
 	public void setUp() throws Exception {
 		recipeJSON = new JSONObject();
 		attributionJSON = new JSONObject();
 		recpImgJSON = new JSONObject();
 		searchResultJSON = new JSONObject();
 		searchMatchJSON = new JSONObject();	
 	}
 
 	public void tearDown() throws Exception {
 		recipeJSON = null;
 		attributionJSON = null;
 		recpImgJSON = null;
 		searchResultJSON = null;
 		searchMatchJSON = null;
 	}
 	
 	public void testRecipeObject() throws JSONException {
 		recipeJSON.put("id", "1");
 		recipeJSON.put("name","pizza");
 		recipeJSON.put("attribution",attributionJSON);
 		recipeJSON.put("ingredientLines", new JSONArray());
 		Storage storage = new Storage();
 		storage.makeRecipe(recipeJSON);
 		assertEquals("1", storage.getRecipeId());
 		assertEquals("pizza", storage.getRecipeName());
 		assertEquals(new ArrayList<String>(), storage.getRecipeIngLines());
 	}
 	
 	public void testAttributionObject() throws JSONException {
 		attributionJSON.put("url", "1");
 		attributionJSON.put("text","pizza");
 		attributionJSON.put("logo","logo");
 		Storage storage = new Storage();
 		storage.makeAtt(attributionJSON);
 		assertEquals("1", storage.getAttUrl());
 		assertEquals("logo", storage.getAttLogo());
 		assertEquals("pizza", storage.getAttText()); 
 	}
 	
 	public void testRecipeImagesObject() throws JSONException {
 		recpImgJSON.put("hostedLargeUrl", "1");
 		recpImgJSON.put("hostedSmallUrl","pizza");	
 		Storage storage = new Storage();
 		storage.makeImg(recpImgJSON);
 		assertEquals("1", storage.getImgLUrl());
		assertEquals("logo", storage.getImgSUrl());
 	}
 
 }

 /*
  * Class Name:			ContentLoader.java
  * Class Purpose:		Class in charge of loading content from JSON files 
  * Created by:			boris on 2011-11-04
  */
 package name.bobnet.android.rl.core.ents.factory;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.json.JSONTokener;
 
 import android.content.res.Resources;
 import android.content.res.Resources.NotFoundException;
 import android.util.Log;
 import name.bobnet.android.rl.R;
 import name.bobnet.android.rl.core.util.TreeNode;
 
 public class ContentLoader {
 
 	// constants
 	private static final int[] C_HOLDERS = { R.raw.items };
 	public static final String[] P_WEPAONS = { "item", "weapon" };
 
 	// singleton
 	private static ContentLoader contentLoader;
 
 	// variables
 	private TreeNode classTree;
 	private EntityFactory factory;
 
 	private ContentLoader() {
 		factory = EntityFactory.getEntityFactory();
 	}
 
 	/**
 	 * Get the content loader singleton
 	 * 
 	 * @return the content loader singleton
 	 */
 	public static ContentLoader getContentLoader() {
 		// create a new content loader if needed
 		if (contentLoader == null) {
 			contentLoader = new ContentLoader();
 		}
 
 		// return the content loader
 		return contentLoader;
 	}
 
 	/**
 	 * Load the content from file and give it to the Factory
 	 * 
 	 * @param res
 	 *            the context of the application to that data can be loaded
 	 */
 	public void loadContent(Resources res) {
 		// variables
 		JSONArray tmpJsonArray;
 
 		// create the tree of classes
 		classTree = new TreeNode("root");
 
 		// load the classes into the tree
 		try {
 			tmpJsonArray = ((JSONObject) (readJSON(res, R.raw.classes))
 					.nextValue()).getJSONArray("classes");
 			loadClasses(tmpJsonArray, classTree, "classes");
 		} catch (NotFoundException e) {
 			Log.d("RL", "Classes file not found, can't load content");
 		} catch (JSONException e) {
 			Log.d("RL", "Classes file malformed, can't load content");
 		}
 
 		// load content
 		for (int contentID : C_HOLDERS) {
 			try {
 				// get the templates array from the file
 				tmpJsonArray = ((JSONObject) readJSON(res, contentID)
 						.nextValue()).getJSONArray("templates");
 
 				// Load content HERE
 
 				// weapons
 				loadTemplates(WeaponTemplate.class, tmpJsonArray, P_WEPAONS, 0);
 
 			} catch (NotFoundException e) {
 				// Items file not found aborting load
 				Log.d("RL", "Items file not found, can't load content");
 			} catch (JSONException e) {
 				// Items file malformed aborting load
 				Log.d("RL", "Items file malformed, can't load content");
 			}
 		}
 	}
 
 	// load templates form file
 	private <T extends Template> void loadTemplates(Class<T> templateClass,
 			JSONArray root, String[] path, int pathOffset) {
 		// variables
 		String[] nPath;
 		JSONObject tObject;
 		JSONArray tArray = root;
 
 		// navigate to the path
		for (int o = pathOffset; o < root.length(); o++) {
 			String cName = path[o];
 
 			// variables
 			boolean success = false;
 
 			// search for the name
 			for (int i = 0; i < tArray.length(); i++) {
 				try {
 					tObject = tArray.getJSONObject(i);
 					if (tObject.getString("name").equals(cName)
 							&& tObject.getString("type").equals("class")) {
 						tArray = tObject.getJSONArray("members");
 						success = true;
 						break;
 					}
 				} catch (JSONException e) {
 					// bad object ignore it
 				}
 			}
 
 			if (!success)
 				// couln't find the class (bas path)
 				return;
 		}
 
 		// go thought the JSONArray
 		for (int i = 0; i < tArray.length(); i++) {
 			try {
 				// try to get an object
 				tObject = tArray.getJSONObject(i);
 
 				// check it's type
 				if (tObject.getString("type").equals("template")) {
 					// create a template from said object
 					Template t = templateClass.newInstance();
 					t.load(tObject);
 					factory.addTemplate(path, t);
 				} else if (tObject.getString("type").equals("class")) {
 					// create a new path
 					nPath = new String[path.length + 1];
 
 					// fill the old values
 					for (int j = 0; j < path.length; j++) {
 						nPath[j] = path[j];
 					}
 
 					// add the new name to the path
					nPath[nPath.length + 1] = tObject.getString("name");
 
 					// search the new path
					loadTemplates(templateClass,
							tObject.getJSONArray("members"), nPath, path.length);
 				}
 			} catch (Exception e) {
 				// Abandon ship
 			}
 		}
 	}
 
 	// load classes into the tree
 	private void loadClasses(JSONArray classes, TreeNode cNode, String className) {
 		// variable
 		JSONObject tmpObj;
 
 		// load all the classes in this array (recursively)
 		for (int i = 0; i < classes.length(); i++) {
 			try {
 				// load the object
 				tmpObj = (JSONObject) classes.getJSONObject(i);
 
 				// add it to the tree
 				TreeNode nNode = new TreeNode(tmpObj, tmpObj.getString("name"));
 				cNode.addChild(nNode);
 
 				// check if it has subclasses
 				JSONArray subClasses = tmpObj.optJSONArray("sub_classes");
 
 				if (subClasses != null) {
 					// load all the subclasses
 					loadClasses(subClasses, nNode, nNode.getName());
 				}
 			} catch (JSONException e) {
 				// something is wrong with this object log it
 				Log.d("RL", "Coudln't load object #" + i + " in " + className
 						+ ", " + e.getMessage());
 			}
 		}
 
 	}
 
 	// get a JSONTokener from file
 	private JSONTokener readJSON(Resources res, int id) {
 		// variables
 		JSONTokener out;
 		StringBuilder builder;
 		BufferedReader reader;
 
 		// get the stream
 		reader = new BufferedReader(new InputStreamReader(
 				res.openRawResource(id)));
 
 		// build the string
 		builder = new StringBuilder();
 		try {
 			for (String line = null; (line = reader.readLine()) != null;)
 				builder.append(line);
 
 			// create the new tokener
 			out = new JSONTokener(builder.toString());
 		} catch (IOException e) {
 			// something failed
 			return null;
 		}
 
 		// return the tokener
 		return out;
 	}
 }

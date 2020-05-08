 package io.trigger.forge.android.modules.database;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.os.AsyncTask;
 import android.util.Log;
 
 import io.trigger.forge.android.core.ForgeApp;
 import io.trigger.forge.android.core.ForgeParam;
 import io.trigger.forge.android.core.ForgeTask;
 
 
 public class API {
 	private static NotesDatabase notesDB;
 
 	private static void initDB(){
 		Log.e("init notesdb: ","INITING NOTES DB");
 
 		if(notesDB == null){
 			Log.e("init notesdb: ","FO REALS");
 			notesDB = new NotesDatabase(ForgeApp.getActivity());
 		}
 	}
 
 	private static JSONArray/*<String>*/ extractNames(JSONArray/*<JSONObject>*/ array) throws JSONException{
 		JSONArray results = new JSONArray();
 		for(int i = 0; i < array.length(); i++)
 			results.put(array.getJSONObject(i).getString("name"));
 		return results;
 	}
 	
 	public static void createTables(final ForgeTask task, @ForgeParam("schema") JSONArray schema){
 		try{
 			NotesDatabase.setQueries(schema);
 			initDB();
			notesDB.createTables(schema);
 			task.success();
 		}catch(Exception e){
 			e.printStackTrace();
 			task.error(e);
 		}
 	}
 
 
 	public static void query(final ForgeTask task, @ForgeParam("query") String query){
 		initDB();
 		try{					
 			task.success(notesDB.queryToObjects(query));
 		}catch(Exception e){
 			task.error(e);
 		}
 	}
 
 		
 	public static void writeAll(final ForgeTask task, @ForgeParam("queries") JSONArray queries){
 		initDB();
 		try{
 			notesDB.open();
 			JSONArray toRet = new JSONArray();
 			for(int i = 0; i < queries.length(); i++){
 				JSONObject query = queries.getJSONObject(i);
 				toRet.put(notesDB.writeQuery(query.getString("query"), query.getJSONArray("args")));
 			}
 			notesDB.close();
 			task.success(toRet);
 		}catch(Exception e){
 			notesDB.close();
 			task.error(e);
 		}
 	}
 		
 	public static void dropTables(final ForgeTask task, @ForgeParam("tables") JSONArray tables){
 		initDB();
 		try{
 			notesDB.dropTables(tables);
 			task.success();
 		}catch(Exception e){
 			task.error(e);
 		}
 	}
 
 
 }

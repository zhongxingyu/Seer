 package edu.sysubbs.argoandroid.argoobject;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 public class ArgoMailBox extends BaseObject {
 	
	public int total;
 	public String used_size;
 	public String total_size;
 
 	@Override
 	public void parse(JSONObject object) {
 		try {
 			total = object.getInt("total");
 			used_size = object.getString("used_size");
 			total_size = object.getString("used_size");
 		} catch (JSONException e) {
 			// TODO 自动生成的 catch 块
 			e.printStackTrace();
 		}
 		
 	}
 
 }

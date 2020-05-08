 package co.firebase.tasks.specialized.json;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import co.firebase.tasks.FireTask;
 import co.firebase.tasks.FireTaskResult;
 import co.firebase.tasks.specialized.OuterTask;
 
 public class JSONTask<InnerParams, InnerProgress> 
 extends OuterTask<InnerParams, InnerProgress, JSONResult, InnerParams, InnerProgress, FireTaskResult<String>> {
 	
 	public JSONTask(
 			FireTask<InnerParams, InnerProgress, FireTaskResult<String>> inner) {
 		super(inner);
 	}
 	
 	@Override
 	protected JSONResult doInBackground(InnerParams... params) {
 		FireTaskResult<String> jsonStringResult = this.executeInner(params);
 		
 		if(jsonStringResult.isSuccess()) {
 			String jsonResult = jsonStringResult.getValue();
 			if (jsonResult != null && !"".equalsIgnoreCase(jsonResult)) {
 				Object json;
 				try {
 					json = jsonResult.startsWith("[") ? new JSONArray(jsonResult) : new JSONObject(jsonResult);
 				} catch (JSONException e) {
					return jsonStringResult.asInnerOf(new JSONResult(null,e));
 				}
				return jsonStringResult.asInnerOf(new JSONResult(json, null));
 			}
			return jsonStringResult.asInnerOf(new JSONResult(null));
 		}
 		else {
			return jsonStringResult.asInnerOf(new JSONResult(null));
 		}
 	}
 	
 }

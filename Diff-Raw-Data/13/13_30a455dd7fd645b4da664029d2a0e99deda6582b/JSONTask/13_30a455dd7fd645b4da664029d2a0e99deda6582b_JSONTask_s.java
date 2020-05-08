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
					return new JSONResult(null,e);
 				}
				return new JSONResult(json, null);
 			}
			return new JSONResult(null);
 		}
 		else {
			JSONResult result = new JSONResult(null);
			result.setInnerResult(jsonStringResult);
			return result;
 		}
 	}
 	
 }

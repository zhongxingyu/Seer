 package edu.cmusv.lions.petmobile.util;
 
import android.annotation.SuppressLint;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
<<<<<<< HEAD
@SuppressLint("DefaultLocale")
=======
import android.util.Log;

>>>>>>> 6ef5f88461559d3338350110ec0d2534a8748ac6
 public class DataSource {
 
 	private static final String SERVICE_URL = "http://petlions.herokuapp.com";
 
 	public interface JsonResultHandler {
 		void onJsonResult(JSONArray jsonArray);
 
 		void onJsonResult(JSONObject jsonObject);
 		
 		void onInternetFailure();
 	}
 
 	private JsonResultHandler jsonResultHandler = new JsonResultHandler() {
 		@Override
 		public void onJsonResult(JSONArray jsonArray) {
 		}
 
 		@Override
 		public void onJsonResult(JSONObject jsonObject) {
 		}
 
 		@Override
 		public void onInternetFailure() {
 		}
 		
 		
 	};
 
 	public void setJsonResultHandler(JsonResultHandler handler) {
 		this.jsonResultHandler = handler;
 	}
 
 	public void getProjectListAsync() {
 		String url = String.format("%s/projects.json", SERVICE_URL);
 		makeHttpRequest(url);
 	}
 
 	public void getProjectAsync(String projectId) {
 		String url = String.format("%s/projects/%s.json", SERVICE_URL, projectId);
 		makeHttpRequest(url);
 	}
 
 	public void getProjectPhaseListAsync(String projectId) {
 		String url = String.format("%s/projects/%s/project_phases.json", SERVICE_URL, projectId);
 		makeHttpRequest(url);
 	}
 
 	public void getProjectPhaseAsync(String projectId, String projectPhaseId) {
 		String url = String.format("%s/projects/%s/project_phases/%s.json", SERVICE_URL, projectId, projectPhaseId);
 		makeHttpRequest(url);
 	}
 
	public void getDelivearblesAsync(int projectId, int projectPhaseId) {
		String url = String.format("%s/projects/%d/project_phases/%d/deliverables.json", SERVICE_URL, projectId,
 				projectPhaseId);
 		makeHttpRequest(url);
 	}
 
	public void getDeliverableAsync(int projectId, int projectPhaseId, int deliverableId) {
		String url = String.format("%s/projects/%d/project_phases/%d/deliverables/%d.json", SERVICE_URL, projectId,
 				projectPhaseId, deliverableId);
 		makeHttpRequest(url);
 	}
 
 	private void makeHttpRequest(String url) {
 		HttpRequestAsyncTask requestTask = new HttpRequestAsyncTask() {
 			@Override
 			protected void onPostExecute(String result) {
 				super.onPostExecute(result);
 				// result will be null if the user does not have an internet
 				// connection
 				if (result != null) {
 					try {
 						if (isJsonArray(result)) {
 							jsonResultHandler.onJsonResult(new JSONArray(result));
 						} else {
 							jsonResultHandler.onJsonResult(new JSONObject(result));
 						}
 					} catch (JSONException e) {
 						e.printStackTrace();
 					}
 				} else {
 					jsonResultHandler.onInternetFailure();
 				}
 			}
 		};
 		requestTask.execute(url);
 	}
 
 	private boolean isJsonArray(String json) {
 		return json.startsWith("[");
 	}
 }

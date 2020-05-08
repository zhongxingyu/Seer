 package coupling.app.com;
 
 import java.util.ArrayList;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import coupling.app.App;
 import coupling.app.Mediator;
 import coupling.app.Utils;
 
 import static coupling.app.com.Constants.*;
 
 public class API { // Mabe move the ITask responsibility to another object
 
 	private String TAG = this.getClass().getName(); 
 
 	private User sender;
 
 
 	private static ServerUtils server = ServerUtils.getInstance();
 
 	private static API api;
 	private ITask apiTasker;
 	private ArrayList<ITask> taskers;
 	
 	
 	private API(){
 		this.sender = App.getOwner();
 		taskers = new ArrayList<ITask>();
 		apiTasker = new ITask() {
 			
 			@Override
 			public void onTaskComplete(Request request, Response response) {
 				//Utils.Log(API.class.getSimpleName(), "onTaskComplete", response.getResponse());
 				Mediator.getInstance().manage(response.getJson());
 			}
 
 		};
 	}
 	
 	
 	
 	public static API getInstance(){
 		if(api == null)
 			api = new API();
 		return api;
 	}
 
 	public void registerUser(){
 		Request request = new Request();
 
 		JSONObject userDetails = sender.toJson();
 
 		request.setMethod(REGISTER);
 		request.setParams(userDetails);
 
 		server.post(request, apiTasker, false);
 	}
 
 	public void sync(Message message){
 		Request request = new Request();
 		
 		try{
 
 			JSONObject params = new JSONObject();
 			params.put(EMAIL, sender.getEmail());//TODO: change back
 			params.put(MESSAGE, message.toJson());
 
 			//Utils.Log(API.class.getSimpleName(), "sync", "email: "+sender.getEmail()+"Message: " + message.toJson().toString());
 			
 			request.setMethod(SYNC);
 			request.setParams(params);
 			
 			server.post(request, apiTasker, true);
 
 		}catch(JSONException e){
 			e.printStackTrace();
 		}	
 
 	}
 
 	public void messageRecieved(long messageId) {
 		Request request = new Request();
 		try {
 
 			JSONObject params = new JSONObject();
 			params.put(EMAIL, sender.getEmail());
 			params.put(MESSAGE_ID, messageId);
 
 			request.setMethod(MESSAGE_RECIEVED);
 			request.setParams(params);
 
 			server.post(request, apiTasker, true);
 
 		} catch(JSONException e){
 			e.printStackTrace();
 		}	
 	}
 
 	public JSONObject getMessage(long messageId) {
 		Request request = new Request();
 		JSONObject resp = null;
 		try {
 
 			JSONObject params = new JSONObject();
 			params.put(EMAIL, sender.getEmail());
 			params.put(MESSAGE_ID, messageId);
 
 			request.setMethod(GET_MESSAGE);
 			request.setParams(params);
 
 			resp = server.post(request, apiTasker, false);
 
 		} catch(JSONException e){
 			e.printStackTrace();
 		}	
 		
 		return resp;
 	}
 
 	public void invite(String email) {
 		Request request = new Request();
 
 		try{
 
 			JSONObject params = new JSONObject();
 			params.put(EMAIL, sender.getEmail());
 			params.put(FRIEND_EMAIL, email);
 
 			request.setMethod(INVITE);
 			request.setParams(params);
 
			server.post(request, apiTasker, true);
 		}catch(JSONException e){
 			e.printStackTrace();
 		}	
 	}
 	
 	
 	public void registerTasker(ITask tasker){
 		if(tasker != null)
 			if(taskers.add(tasker)){}
 				//Utils.Log(TAG, "registerTasker", tasker.getClass().getName() + " is register successfully");
 	}
 
 	public void unRegisterTasker(ITask tasker){
 		if(tasker != null)
 			if(taskers.remove(tasker)){}
 				//Utils.Log(TAG, "unRegisterTasker", tasker.getClass().getName() + " unregister successfully");
 	}
 
 }

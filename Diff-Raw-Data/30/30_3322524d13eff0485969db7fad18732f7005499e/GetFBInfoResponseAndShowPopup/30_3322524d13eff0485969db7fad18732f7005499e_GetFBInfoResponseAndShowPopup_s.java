 package in.co.hopin.Server;
 
 import in.co.hopin.Activities.NewUserDialogActivity;
 import in.co.hopin.Platform.Platform;
 import in.co.hopin.Users.NearbyUser;
 import in.co.hopin.Users.UserFBInfo;
 
 import org.apache.http.HttpResponse;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Intent;
 import android.util.Log;
 
 
 public class GetFBInfoResponseAndShowPopup extends ServerResponseBase{
 
 	String status;
 	
 	NearbyUser thisNearbyUser ;
 	UserFBInfo thisNearbyUserFBInfo;	
 	
 	
 	private static final String TAG = "in.co.hopin.Server.GetFBInfoResponseAndShowPopup";
	public GetFBInfoResponseAndShowPopup(HttpResponse response,String jobjStr) {
 		super(response,jobjStr);		
 	}
 	
 	@Override
 	public void process() {
 		//Log.i(TAG,"processing GetFBInfoResponse response.status:"+this.getStatus());	
 		
 		//jobj = JSONHandler.getInstance().GetJSONObjectFromHttp(serverResponse);
 		//Log.i(TAG,"got json "+jobj.toString());
 		try {
 			body = jobj.getJSONObject("body");
 			JSONArray nearbyUSers =  body.getJSONArray("NearbyUsers");
 			JSONObject thisNEarbyUSerjobj = nearbyUSers.getJSONObject(0);
 			Intent i = new Intent(Platform.getInstance().getContext(),NewUserDialogActivity.class);
 			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 			i.putExtra("nearbyuserjsonstr", thisNEarbyUSerjobj.toString());
 			Platform.getInstance().getContext().startActivity(i);
 			//status = body.getString("Status");			
 			//ThisUserConfig.getInstance().putBool(ThisUserConfig.FBINFOSENTTOSERVER, true);
 			
 			//ToastTracker.showToast("fb save:"+status);
 		} catch (JSONException e) {			
 			//Log.e(TAG, "Error returned by server get fb info for user and show popup");
 			e.printStackTrace();
 		}
 		
 	}
 	
 }

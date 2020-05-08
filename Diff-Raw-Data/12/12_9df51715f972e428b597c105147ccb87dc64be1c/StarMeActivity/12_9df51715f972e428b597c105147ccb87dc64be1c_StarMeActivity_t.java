 package info.liuqy.adc.aroundme;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.location.Location;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.Spinner;
 import android.widget.Toast;
 
 public class StarMeActivity extends Activity {
 	public static final String EXTRA_LOCATION = "location";
 
 	Location location;
 
 	CouchDbAdapter couchdb;
 	ProgressDialog progressDialog;
 	String nickname;
 	long until;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.starme);
 
 		couchdb = new CouchDbAdapter(handler);
 
 		Bundle data = this.getIntent().getExtras();
 		location = (Location) data.getParcelable(StarMeActivity.EXTRA_LOCATION);
 	}
 
 	public void starme(View v) {
 		EditText nick = (EditText) this.findViewById(R.id.nickname);
 		Spinner stay = (Spinner) this.findViewById(R.id.stay);
 
 		nickname = nick.getText().toString();
 		int stayfor = stay.getSelectedItemPosition();
 		double lat0 = location.getLatitude();
 		double long0 = location.getLongitude();
 
 		long now = System.currentTimeMillis();
 		switch (stayfor) {
 		case 0: //30 min
 			until = now + 30 * 60 * 1000;
 			break;
 		case 1: //1 hour
 			until = now + 60 * 60 * 1000;
 			break;
 		case 2: //2 hour
 			until = now + 2 * 60 * 60 * 1000;
 			break;
 		case 3: //half a day
 			until = now + 4 * 60 * 60 * 1000;
 			break;
 		case 4: //1 day
 			until = now + 8 * 60 * 60 * 1000;
 			break;
 		default: //30 min
 			until = now + 30 * 60 * 1000;
 		}
 
		String json = "{\"type\":\"star\",\"name\":\"" + nickname
				+ "\",\"until\":" + until + ",\"long\":" + long0 + ",\"lat\":"
				+ lat0 + "}";
 
 		couchdb.doPost(json);
 	}
 
 	private Handler handler = new Handler() {
 		@Override
 		public void handleMessage(Message msg) {
 			Bundle data;
 			switch (msg.what) {
 			case CouchDbAdapter.RESULT:
 				progressDialog.dismiss();
 				data = msg.getData();
 				String page = data.getString("result");
 				
 				String jsonText = page.substring(4); // 200:{jsonText}
 				//{"ok":true,"id":"e4fe0d7643f7969678868de459b3723b","rev":"1-a89de4cc9c0b5aed75c699ab77014431"}
 				try {
 					JSONObject json = new JSONObject(jsonText);
 					String id = json.getString("id");
 					AroundMeActivity.myId = id;
 					AroundMeActivity.myNickname = nickname;
 				    
 					// claim my id to the chat server
 					Intent in = new Intent(ChatAgent.SEND_ACTION);
 					in.putExtra(ChatAgent.EXTRA_MESSAGE, "id " + AroundMeActivity.myId);
 					sendBroadcast(in);
 				    
 					Log.d("XXX", "set myId to " + AroundMeActivity.myId + " and nickname to " + AroundMeActivity.myNickname);
 				} catch (JSONException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				
 				//TODO a better way to do
 				Toast.makeText(StarMeActivity.this, "result!" + page,
 						Toast.LENGTH_SHORT).show();
 				
 				finish();
 				break;
 			case CouchDbAdapter.START:
 				progressDialog = ProgressDialog.show(StarMeActivity.this, "", 
                         "Loading. Please wait...", true);
 				break;
 			default:
 				//TODO	
 			}
 		}
 	};
 
 }

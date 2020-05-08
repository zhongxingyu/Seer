 package android.HLMPConnect;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.widget.SimpleAdapter;
 
 
 import hlmp.CommLayer.Communication;
 import hlmp.CommLayer.NetUser;
 
 import android.HLMPConnect.Managers.UsersManager;
 
 //TODO: agregar ping, chat y transferencia de archivos
 
 public class UsersActivity extends ListActivity implements OnClickListener {
     
     public static final int ADD_USER = 0;
     public static final int REFRESH_USER = 1;
 	public static final int REFRESH_LOCAL_USERS = 2;
 	public static final int REMOVE_USER = 3;
 	
 	protected static final String USERNAME = "USERNAME";
 	protected static final String IP = "IP";
     
 	private Communication communication;
 	protected UsersManager userManagers;
 	protected ArrayList<HashMap<String, String>> users;
 	private SimpleAdapter adapter;
 
 	private final Handler mHandler = new Handler() {
         @Override
         public void handleMessage(Message msg) {
         	if (msg.what != REFRESH_LOCAL_USERS) {
         		users.clear();
         		for(NetUser netUser: communication.getNetUserList().userListToArray()) {
         			HashMap<String, String> userMap = new HashMap<String, String>();
         			userMap.put(USERNAME, netUser.getName());
         			userMap.put(IP, netUser.getIp().getHostAddress());
 					users.add(userMap);
 				}
 				adapter.notifyDataSetChanged();
 			}
         }
     };
 	
 	
     
     @Override
     public void onCreate(Bundle savedInstanceState) {
     	super.onCreate(savedInstanceState);
     	
         HLMPApplication application = (HLMPApplication)getApplicationContext();
         this.userManagers = application.getUsersManager();
         this.communication = application.getCommunication();
         
         users = new ArrayList<HashMap<String, String>>();
         this.adapter = new SimpleAdapter(
 					this,
 					users,
 					R.layout.list_two_info_per_item,
 					new String[] {USERNAME, IP},
 					new int[] {R.id.text_1, R.id.text_2});
         this.setListAdapter(this.adapter);
         
         this.userManagers.setHandler(mHandler);
     }
 
 	
 	@Override
 	public void onBackPressed() {
 	    AlertDialog.Builder builder = new AlertDialog.Builder(this);
 	    builder.setMessage("Are you sure you want to exit?")
         .setCancelable(false)
         .setPositiveButton("YES", this)
         .setNegativeButton("NO", this);
 
         AlertDialog alert = builder.create();
         alert.show();
 	}
 	
 	public void onClick(DialogInterface dialog, int which) {
 		if (DialogInterface.BUTTON_POSITIVE == which) {
 			super.onBackPressed();
 		}
 		else if (DialogInterface.BUTTON_NEGATIVE == which) {
 			dialog.cancel();
 		}
 	}
 }

 package core.september.textmesecure;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.jivesoftware.smack.XMPPException;
 import org.jivesoftware.smack.packet.Presence;
 import org.jivesoftware.smack.packet.Presence.Mode;
 
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.widget.DrawerLayout;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 
 import com.quickblox.module.users.model.QBUser;
 
 import core.september.textmesecure.configs.Config;
 import core.september.textmesecure.interfaces.IAppManager;
 
 public class UsersListActivity extends O9BaseActivity {
 	private final static String TAG = UsersListActivity.class.getSimpleName();
 	private IAppManager imService;
 	 private ListView usersList;
 	 private ProgressDialog progressDialog;
 	 private DrawerLayout mDrawerLayout;
 	
 	    
 	 private static String retrieveState_mode(Mode userMode, boolean isOnline) {
 	        int userState = 0;
 	        /** 0 for offline, 1 for online, 2 for away,3 for busy*/
 	        if(userMode == Mode.dnd) {
 	            userState = 3;
 	        } else if (userMode == Mode.away || userMode == Mode.xa) {   
 	            userState = 2;
 	        } else if (isOnline) {
 	            userState = 1;
 	        }
 	        //return userState;
 	        String state = "";
 	        switch (userState) {
 			case 0:
 				state = "OFFLINE";
 				break;
 			case 1:
 				state = "ONLINE";
 				break;
 			case 2:
 				state = "AWAY";
 				break;
 			case 3:
 				state = "BUSY";
 				break;
 			default:
 				state = "UNKNOW";
 				break;
 			}
 	        return state;
 	}
 	 
 	    @Override
 	    protected void onCreate(Bundle savedInstanceState) {
 	        super.onCreate(savedInstanceState);
 	        setContentView(R.layout.activity_users_list);
 
 	        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
	        usersList = (ListView) findViewById(R.id.usersList);
 
 	        progressDialog = new ProgressDialog(this);
 	        progressDialog.setMessage("Loading fiends list");
 	        progressDialog.show();
 	        
 	        Bundle extras = getIntent().getExtras();
 
 	        //QBUser me = new QBUser();
 	       int id = extras.getInt(Config.MY_ID);
 	       String login =  extras.getString(Config.MY_LOGIN);
 	       String password = extras.getString(Config.MY_PASSWORD);
 
 	        // ================= QuickBlox ===== Step 4 =================
 	        // Get all users of QB application.
 	       
 	        try {
 				imService.setUpController(login, password);
 			} catch (XMPPException e) {
 				// TODO Auto-generated catch block
 				android.util.Log.d(TAG,e.getMessage(),e);
 			}
 	        //imService.getFriendList();
 	        
 	        final List<QBUser> users = imService.getFriendList();
             ArrayList<Map<String, String>> usersListForAdapter = new ArrayList<Map<String, String>>();
            
             for (QBUser u : users) {
                 Map<String, String> umap = new HashMap<String, String>();
                 umap.put(Config.USER_LOGIN, u.getLogin());
                 //umap.put(Config.CHAT_LOGIN, QBChat.getChatLoginFull(u));
                 Presence availability = imService.getPresence(u.getLogin());
                 Mode userMode = availability.getMode();
                 umap.put(Config.USER_STATUS, retrieveState_mode(userMode,availability.isAvailable()));
                 usersListForAdapter.add(umap);
             }
 
             // Put users list into adapter.
             SimpleAdapter usersAdapter = new SimpleAdapter(this, usersListForAdapter,
                     android.R.layout.simple_list_item_2,
                     new String[]{Config.USER_LOGIN, Config.USER_STATUS},
                     new int[]{android.R.id.text1, android.R.id.text2});
 
             usersList.setAdapter(usersAdapter);
             usersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                 @Override
                 public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
 
                     // Prepare QBUser objects to pass it into next activities using bundle.
                     QBUser friendUser = users.get(i);
 
                     Intent intent = new Intent(UsersListActivity.this, ChatActivity.class);
                     Bundle extras = getIntent().getExtras();
                     intent.putExtra(Config.FRIEND_ID, friendUser.getId());
                     intent.putExtra(Config.FRIEND_LOGIN, friendUser.getLogin());
                     intent.putExtra(Config.FRIEND_PASSWORD, friendUser.getPassword());
                     // Add extras from previous activity.
                     intent.putExtras(extras);
 
                     startActivity(intent);
                 }
             });
 	        
 	    }
 }

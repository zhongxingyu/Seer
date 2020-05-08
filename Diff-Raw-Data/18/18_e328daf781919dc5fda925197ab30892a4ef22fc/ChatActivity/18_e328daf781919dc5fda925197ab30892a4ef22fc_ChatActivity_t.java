 package pl.edu.agh.mobile.adhoccom;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
import pl.edu.agh.mobile.adhoccom.chatprotocol.Message;
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.ServiceConnection;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 
 public class ChatActivity extends Activity implements OnClickListener {
 	private ListView mMessagesView;
 	private Button mSendButton;
 	private EditText mEditMessage;
 	private ArrayAdapter<String> mMessageAdapter;
 
 	private BroadcastReceiver mBroadcastReceiver;
 	private ChatService mChatService;
 	private boolean mServiceBound = false;
 	private ChatDbAdapter mDbAdapter;
 	private String mCurrentGroup = ChatService.DEFAULT_GROUP;
 
 	private AppConfig mConfig;
 
 	private static final String TAG = "OnClickListener";
 
 	public static final int JOIN_GROUP_REQ = 0;
 	public static final int MANAGE_GROUPS_REQ = 1;
 	public static final int SETTINGS_REQ = 2;
 
 	private ServiceConnection mConnection = new ServiceConnection() {
 		public void onServiceConnected(ComponentName className, IBinder service) {
 			mChatService = ((ChatService.ChatServiceBinder) service)
 					.getService();
 			mChatService.joinGroup(ChatService.DEFAULT_GROUP, "");
 			initializeData();
 		}
 
 		public void onServiceDisconnected(ComponentName className) {
 			mChatService = null;
 			mServiceBound = false;
 		}
 	};
 
 	private void initializeWidgets() {
 		mMessagesView = (ListView) findViewById(R.id.msg_view);
 		mEditMessage = (EditText) findViewById(R.id.msg_edit);
 		mSendButton = (Button) findViewById(R.id.send);
 		mSendButton.setOnClickListener(this);
 	}
 
 	private String createMessageString(Cursor c) {
 		StringBuilder msg = new StringBuilder();
 		msg.append(c.getString(1)).append(":\n");
 		msg.append(c.getString(2));
 		return msg.toString();
 	}
 
 	private void setTitle() {
 		StringBuilder title = new StringBuilder(
 				getString(R.string.current_group));
 		title.append(": ").append(mCurrentGroup);
 		setTitle(title.toString());
 	}
 
 	private void initializeData() {
 		List<String> messages = new ArrayList<String>(
 				mConfig.getMaxMessages() + 1);
 		mMessageAdapter = new ArrayAdapter<String>(this,
 				R.layout.message_layout2, messages);
 		Cursor c = mDbAdapter.featchMessages(mConfig.getMaxMessages(),
 				mCurrentGroup);
 		startManagingCursor(c);
 		if (c.moveToLast()) {
 			while (!c.isBeforeFirst()) {
 				mMessageAdapter.add(createMessageString(c));
 				c.moveToPrevious();
 			}
 		}
 		mMessagesView.setAdapter(mMessageAdapter);
 		setTitle();
 	}
 
 	private void updateData(long newMsgId, String groupName) {
 		if (mCurrentGroup.equals(groupName)) {
 			Cursor c = mDbAdapter.fetchMessage(newMsgId);
 			if (c != null && c.getCount() > 0) {
 				mMessageAdapter.add(createMessageString(c));
 				if (mMessageAdapter.getCount() > mConfig.getMaxMessages()) {
 					mMessageAdapter.remove(mMessageAdapter.getItem(0));
 				}
 			}
 			c.deactivate();
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.menu, menu);
 		return true;
 	}
 
 	private void joinGroup() {
 		startActivityForResult(new Intent(this, JoinGroupActivity.class),
 				JOIN_GROUP_REQ);
 	}
 
 	private void manageGroups() {
 		startActivityForResult(new Intent(this, ManageGroupsActivity.class),
 				MANAGE_GROUPS_REQ);
 	}
 
 	private void settings() {
 		startActivity(new Intent(this, SettingsActivity.class));
 	}
 
 	private void quit() {
 		setResult(RESULT_OK);
 		finish();
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.join_group:
 			joinGroup();
 			return true;
 		case R.id.manage_groups:
 			manageGroups();
 			return true;
 		case R.id.settings:
 			settings();
 			return true;
 		case R.id.quit:
 			quit();
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	protected void onStart() {
 		super.onStart();
 		if (!mServiceBound) {
 			Intent bindIntent = new Intent(ChatActivity.this, ChatService.class);
 			bindService(bindIntent, mConnection, Context.BIND_AUTO_CREATE);
 			mServiceBound = true;
 		}
 	}
 
 	protected void onStop() {
 		super.onStop();
 		if (mServiceBound) {
 			unbindService(mConnection);
 			mServiceBound = false;
 		}
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.chat_layout);
 		mConfig = AppConfig.getInstance();
 		mConfig.setAppContext(getApplicationContext());
 		initializeWidgets();
 		mDbAdapter = (new ChatDbAdapter(this)).open();
 		Intent svc = new Intent(this, ChatService.class);
 		startService(svc);
 
 		IntentFilter filter = new IntentFilter(ChatService.MESSAGE_RECEIVED);
 		if (mBroadcastReceiver == null)
 			mBroadcastReceiver = new MessageReceiver();
 		registerReceiver(new MessageReceiver(), filter);
 	}
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		mDbAdapter.close();
 	}
 
 	@Override
 	public void onClick(View v) {
 		if (v == mSendButton) {
 			if (mEditMessage.getText().length() > 0) {
 				try {
 					mChatService.sendMessage(new Message(mEditMessage.getText()
 							.toString().getBytes(), mConfig.getUserNickname(),
 							(int) (System.currentTimeMillis() / 100),
 							mCurrentGroup));
 				} catch (IOException e) {
 					Log.d(TAG, "Exception while sending msg", e);
 				}
 				mEditMessage.setText("");
 			}
 		}
 	}
 
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
 		if (requestCode == JOIN_GROUP_REQ) {
 			if (resultCode == RESULT_OK) {
 				String groupName = intent
 						.getStringExtra(JoinGroupActivity.GROUP_NAME);
 				String groupPasswd = intent
 						.getStringExtra(JoinGroupActivity.GROUP_PASSWORD);
 				mChatService.joinGroup(groupName, groupPasswd);
 				mCurrentGroup = groupName;
 				initializeData();
 			}
 		} else if (requestCode == MANAGE_GROUPS_REQ) {
 			if (resultCode == RESULT_OK) {
 				String groupName = intent
 						.getStringExtra(ManageGroupsActivity.SELECTED_CHAT_GROUP);
 				if (!mCurrentGroup.equals(groupName)) {
 					mCurrentGroup = groupName;
 					initializeData();
 				}
 			} else {
 				if (!mChatService.isAttachedToGroup(mCurrentGroup)) {
 					mCurrentGroup = ChatService.DEFAULT_GROUP;
 					initializeData();
 				}
 			}
 		}
 	}
 
 	public class MessageReceiver extends BroadcastReceiver {
 
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			updateData(intent.getLongExtra("newMsgId", -1),
 					intent.getStringExtra(ChatService.NEW_MSG_GROUP_EXTRA));
 		}
 
 	}
 }

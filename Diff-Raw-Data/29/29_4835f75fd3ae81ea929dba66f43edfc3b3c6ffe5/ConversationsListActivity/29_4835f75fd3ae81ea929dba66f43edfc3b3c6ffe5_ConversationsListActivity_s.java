 package edu.utsa.cs.smsmessenger.activity;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 import edu.utsa.cs.smsmessenger.R;
 import edu.utsa.cs.smsmessenger.adapter.ConversationPreviewAdapter;
 import edu.utsa.cs.smsmessenger.adapter.MessageSearchContainerAdapter;
 import edu.utsa.cs.smsmessenger.model.ConversationPreview;
 import edu.utsa.cs.smsmessenger.model.MessageContainer;
 import edu.utsa.cs.smsmessenger.util.SmsMessageHandler;
 import android.os.Bundle;
 import android.app.Activity;
 import android.app.NotificationManager;
 import android.app.SearchManager;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.ListView;
 import android.widget.SearchView;
 import android.widget.Toast;
 
 /**
  * This class is the Activity that shows a preview list of all conversations
  * 
  * @author Michael Madrigal
  * @version 1.0
  * @since 1.0
  * 
  */
 public class ConversationsListActivity extends Activity implements
 		SearchView.OnQueryTextListener, SearchView.OnCloseListener {
 
 	private ListView conversationsListView;
 	private ConversationPreviewAdapter conversationPreviewAdapter;
 	private SmsMessageHandler smsMessageHandler;
 
 	private BroadcastReceiver newMsgReceiver = new BroadcastReceiver() {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			String action = intent.getAction();
 			if (action.equalsIgnoreCase(SmsMessageHandler.UPDATE_MSG_INTENT)) {
 				fillConversationsList();
 			}
 			Log.d("ConversationsListActivity - BroadcastReceiver",
 					"New intent received.");
 		}
 	};
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.conversations_list);
 		registerNewMsgReceiver();
 		fillConversationsList();
 	}
 
 	@Override
 	protected void onPause() {
 		unregisterReceiver(newMsgReceiver);
 		super.onPause();
 	}
 
 	@Override
 	protected void onResume() {
 		registerNewMsgReceiver();
 		super.onResume();
 		fillConversationsList();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.conversations_list, menu);
 
 		// Get the SearchView and set the searchable configuration
 		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
 		SearchView searchView = (SearchView) menu.findItem(
 				R.id.action_message_search).getActionView();
 		// Assumes current activity is the searchable activity
 		searchView.setSearchableInfo(searchManager
 				.getSearchableInfo(getComponentName()));
 		searchView.setIconifiedByDefault(false);
 
 		searchView.setOnQueryTextListener(this);
 
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 
 
 		switch (item.getItemId()) {
 		case R.id.action_new_message:
 			Intent newConversationintent = new Intent(this,
 					NewConversationActivity.class);
 			startActivity(newConversationintent);
 
 			break;
 		case R.id.action_settings:
 			Intent settingsIntent = new Intent(this, AppSettingsActivity.class);
 			startActivity(settingsIntent);
 			break;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	//
 	// This method fetches closes all notifications for this app
 	//
	private void closeExistingNotifications() {
		NotificationManager notificationmanager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		notificationmanager.cancelAll();
	}
 	
 	//
 	// This method fetches the latest conversation preview lists from the app
 	// message database.
 	//
 	private void fillConversationsList() {
 		HashMap<String, ConversationPreview> convPrevMap = getSmsMessageHandler()
 				.getConversationPreviewItmes(this);
 
 		ArrayList<ConversationPreview> convPrevArrayList = new ArrayList<ConversationPreview>();
 
 		for (Map.Entry<String, ConversationPreview> entry : convPrevMap
 				.entrySet()) 
 			convPrevArrayList.add(entry.getValue());
 
 		//Sort array list
 		Collections.sort(convPrevArrayList, Collections.reverseOrder());
 		
 		if (convPrevArrayList.size() > 0) {
 			conversationPreviewAdapter = new ConversationPreviewAdapter(this,
 					R.layout.conversations_list_item, convPrevArrayList);
 			getConversationListView().setAdapter(conversationPreviewAdapter);
 		} else {
 			if (getConversationListView().getAdapter() != null) {
 				ConversationPreviewAdapter emptyAdapter = new ConversationPreviewAdapter(
 						this, R.layout.conversations_list_item,
 						new ArrayList<ConversationPreview>());
 				getConversationListView().setAdapter(emptyAdapter);
 			}
 		}
		closeExistingNotifications();
 	}
 
 	private SmsMessageHandler getSmsMessageHandler() {
 		if (smsMessageHandler == null)
 			smsMessageHandler = new SmsMessageHandler(this);
 		return smsMessageHandler;
 	}
 
 	public void registerNewMsgReceiver() {
 		IntentFilter filter = new IntentFilter();
 		filter.addAction(SmsMessageHandler.UPDATE_MSG_INTENT);
 		registerReceiver(newMsgReceiver, filter);
 	}
 
 	public ListView getConversationListView() {
 		if (conversationsListView == null)
 			conversationsListView = (ListView) findViewById(R.id.conversationsListView);
 		return conversationsListView;
 	}
 
 	private void searchForMessage(String query) {
 		ArrayList<MessageContainer> msgList = getSmsMessageHandler()
 				.queryMessages(query);
 		getSmsMessageHandler().close();
 
 		MessageSearchContainerAdapter messageSearchContainerAdapter = new MessageSearchContainerAdapter(
 				this, R.layout.conversation_from_message_item, msgList);
 
 		getConversationListView().setAdapter(messageSearchContainerAdapter);
 	}
 
 	@Override
 	public boolean onQueryTextChange(String newText) {
 		if (newText != null && newText.length() > 0)
 			searchForMessage(newText);
 		else
 			fillConversationsList();
 		return false;
 	}
 
 	@Override
 	public boolean onQueryTextSubmit(String query) {
 		return false;
 	}
 
 	@Override
 	public boolean onClose() {
 		fillConversationsList();
 		return false;
 	}
 }

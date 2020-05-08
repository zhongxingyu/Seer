 package com.uppidy.android.demo.app;
 
 import java.util.Collections;
 import java.util.Date;
 
 import org.springframework.social.connect.ConnectionRepository;
 
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.Toast;
 
 import com.uppidy.android.sdk.R;
 import com.uppidy.android.sdk.api.ApiContact;
 import com.uppidy.android.sdk.api.ApiContactInfo;
 import com.uppidy.android.sdk.api.ApiContainer;
 import com.uppidy.android.sdk.api.ApiMessage;
 import com.uppidy.android.sdk.api.ApiSync;
 import com.uppidy.android.sdk.api.Uppidy;
 import com.uppidy.android.sdk.backup.BackupService;
 import com.uppidy.android.sdk.connect.UppidyConnectionFactory;
 
 /**
  * @author arudnev@uppidy.com
  */
 public class UppidyActivity extends AbstractAsyncActivity {
 
 	protected static final String TAG = UppidyActivity.class.getSimpleName();
 
 	private ConnectionRepository connectionRepository;
 
 	private UppidyConnectionFactory connectionFactory;
 
 	// ***************************************
 	// Activity methods
 	// ***************************************
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		this.connectionRepository = getApplicationContext().getConnectionRepository();
 		this.connectionFactory = getApplicationContext().getUppidyConnectionFactory();
 	}
 
 	@Override
 	public void onStart() {
 		super.onStart();
 		if (isConnected()) {
 			showUppidyOptions();
 		} else {
 			showConnectOption();
 		}
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 	}
 
 	// ***************************************
 	// Private methods
 	// ***************************************
 	private boolean isConnected() {
 		return connectionRepository.findPrimaryConnection(Uppidy.class) != null;
 	}
 
 	private void disconnect() {
 		this.connectionRepository.removeConnections(this.connectionFactory.getProviderId());
 	}
 
 	private void showConnectOption() {
 		String[] options = { "Connect" };
 		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, options);
 		ListView listView = (ListView) this.findViewById(R.id.uppidy_activity_options_list);
 		listView.setAdapter(arrayAdapter);
 
 		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 			public void onItemClick(AdapterView<?> parentView, View childView, int position, long id) {
 				switch (position) {
 				case 0:
 					displayUppidyAuthorization();
 					break;
 				default:
 					break;
 				}
 			}
 		});
 	}
 
 	private void showUppidyOptions() {
 		String[] options = { "Disconnect", "Fake Backup", "Load From Backup", "Run SMS Backup", "Browse" };
 		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, options);
 		ListView listView = (ListView) this.findViewById(R.id.uppidy_activity_options_list);
 		listView.setAdapter(arrayAdapter);
 
 		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 			public void onItemClick(AdapterView<?> parentView, View childView, int position, long id) {
 				Intent intent;
 				switch (position) {
 				case 0:
 					disconnect();
 					showConnectOption();
 					break;
 				case 1:
 					new FakeSyncTask().execute();
 					break;
 				case 2:
 					intent = new Intent();
 					intent.setClass(parentView.getContext(), UppidyLoadActivity.class);
 					startActivity(intent);
 					break;
 				case 3:
					startService(new Intent(BackupService.ACTION_START));
 					break;
 				case 4:
 					intent = new Intent();
 					intent.setClass(parentView.getContext(), UppidyBrowseActivity.class);
 					startActivity(intent);
 					break;
 				default:
 					break;
 				}
 			}
 		});
 	}
 
 	private void displayUppidyAuthorization() {
 		Intent intent = new Intent();
 		intent.setClass(this, UppidyWebOAuthActivity.class);
 		startActivity(intent);
 		finish();
 	}
 	
 	private void showResult(String result) {
 		Toast.makeText(this, result, Toast.LENGTH_LONG).show();
 	}
 	
 	// ***************************************
 	// Private classes
 	// ***************************************
 	private class FakeSyncTask extends AsyncTask<Void, Void, String> {
 
 		@Override
 		protected void onPreExecute() {
 			showProgressDialog("Backing up a message...");
 		}
 
 		@Override
 		protected String doInBackground(Void... params) {
 			try {
 				Uppidy uppidy = getApplicationContext().getConnectionRepository().findPrimaryConnection(Uppidy.class).getApi();
 				
 				String containerId = getApplicationContext().getContainerId();
 				ApiContainer container = getApplicationContext().getContainer();
 				
				if(container == null) return "Container is not initialized";
 				
 				Log.d(TAG, "Container for sync: " + container);
 				
 				ApiContactInfo myInfo = container.getOwner();
 				
 				ApiSync sync = new ApiSync();
 				sync.setClientVersion("12.07");
 
 				ApiContactInfo contactInfo = new ApiContactInfo();
 				contactInfo.setName("Uppidy User");
 				contactInfo.setAddress("8181868174");
 
 				ApiMessage message = new ApiMessage();
 				message.setSentTime(new Date());
 				message.setSent(Math.random() > 0.5);				
 				message.setFrom(message.isSent() ? myInfo : contactInfo);
 				message.setTo(Collections.singletonList(message.isSent() ? contactInfo : myInfo));
 				message.setText("Fake message generated at " + message.getSentTime());
 				
 				sync.setMessages(Collections.singletonList(message));
 				
 				ApiContact contact = new ApiContact();
 				contact.setName(contactInfo.getName());
 				contact.setAddressByType(Collections.singletonMap("phone", Collections.singletonList(contactInfo.getAddress())));
 				
 				sync.setContacts(Collections.singletonList(contact));
 
 				uppidy.backupOperations().sync(containerId, sync);
 				
 				
 				return "The following sync is done: " + sync;
 
 			} catch (Exception e) {
 				Log.e(TAG, e.getLocalizedMessage(), e);
 			
 				return "Error happened during fake sync: " + e.getLocalizedMessage();
 			}
 		}
 		
 		@Override
 		protected void onPostExecute(String result) {
 			dismissProgressDialog();
 			showResult(result);
 		}
 	}
 
 
 }

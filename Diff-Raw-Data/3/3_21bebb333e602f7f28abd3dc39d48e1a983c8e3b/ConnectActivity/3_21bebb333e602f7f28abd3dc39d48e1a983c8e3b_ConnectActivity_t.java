 package rc.client;
 
 import java.io.IOException;
 import java.net.SocketException;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import rc.client.R;
 import rc.network.Network;
 import tools.SerializationTool;
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.Toast;
 import android.widget.AdapterView.OnItemClickListener;
 
 import commands.Command;
 import commands.CommandWord;
 
 public class ConnectActivity extends Activity {
 	public static final String TAG = "ConnectActivity";
 
 	// Trucated list, according to text entry
 	private ArrayList<String> partialNames = new ArrayList<String>();
 	private ArrayList<String> searchNames = null;
 	private HashMap<String, String> ipTable = null;
 
 	// Field where user enters his search criteria
 	private EditText ipAdressT;
 	private EditText portT;
 	private Button connectB;
 
 	// List of names matching criteria are listed here
 	private ListView ipList;
 	private ArrayAdapter<String> adapter;
 
 	// Get the app's shared preferences
 	private SharedPreferences preferences;
 	private SharedPreferences.Editor preferencesEditor;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		Global.network = new Network();
 		Global.network.setPort(4242);
 
 		setContentView(R.layout.connect);
 
 		ipAdressT = (EditText) findViewById(R.id.ipAdressTextEdit);
 		ipAdressT.addTextChangedListener(textChangedWatcher);
 
 		portT = (EditText) findViewById(R.id.portTextEdit);
 		connectB = (Button) findViewById(R.id.connectButton);
 
 		ipList = (ListView) findViewById(R.id.ipListView);
 		adapter = new ArrayAdapter<String>(this,
 				android.R.layout.simple_list_item_1, partialNames);
 		ipList.setAdapter(adapter);
 		ipList.setOnItemClickListener(itemClickListener);
 
 		preferences = PreferenceManager.getDefaultSharedPreferences(this);
 		preferencesEditor = preferences.edit();
 
 		ipTable = new HashMap<String, String>();
 		ipTable = (HashMap<String, String>) SerializationTool
 				.stringToMap(preferences.getString("ip", "fail"));
 		System.out.println(ipTable.keySet());
 
 		searchNames = new ArrayList<String>(ipTable.size());
 		for (String s : ipTable.keySet()) {
 			searchNames.add(s);
 		}
 		System.out.println(searchNames);
 
 		alterAdapter();
 		connectB.setOnClickListener(connectClickListener);
 
 		Toast.makeText(this, "Toast it !!! Roast it !", Toast.LENGTH_SHORT)
 				.show();
 	}
 
 	private OnClickListener connectClickListener = new OnClickListener() {
 		@Override
 		public void onClick(View v) {
 			connect();
 		}
 	};
 
 	private void connect() {
 		connect(ipAdressT.getText().toString(), portT.getText().toString());
 	}
 
 	private void connect(String ip, String port) {
 		Global.network.setIp(ip);
 		Global.network.setPort(Integer.parseInt(port));
 
 		new ConnectNetwork().execute(ip);
 	}
 
 	/**
 	 * Establish a connection to the server in an asynchronous way. Thus, it
 	 * will not block the interface while connecting, and allow the use of
 	 * progress bar for instance. The UI is updated from the function
 	 * onProgressUpdate since it runs in the UI thread, as opposed of the rest
 	 * of the class, running in its own thread
 	 */
 	private class ConnectNetwork extends AsyncTask<String, Integer, Void> {
 		private ProgressDialog dialog = null;
 
 		/**
 		 * Called before the execution of doInBackground, used to set up dialogs
 		 * for instance
 		 */
 		protected void onPreExecute() {
 			dialog = new ProgressDialog(ConnectActivity.this);
 			dialog.setMessage("Connecting...");
 			dialog.show();
 		}
 
 		/**
 		 * Effectively handles the creation of the connection. This task may
 		 * take some time according to the network capabilities, thus the
 		 * threading
 		 */
 		protected Void doInBackground(String... IP) {
 			int nb = IP.length;
 			String ip = (nb > 0) ? IP[0] : "";
 			Global.network.setIp(ip);
 			Global.network
 					.setPort(Integer.parseInt(portT.getText().toString()));
 			try {
 				Global.network.connect();
 			} catch (SocketException e) {
 				publishProgress(0);
 				Log.e(TAG, "Socket exeption\n" + e.toString());
 			} catch (UnknownHostException e) {
 				publishProgress(1);
 				Log.e(TAG, "Unknown Host\n" + e.toString());
 			} catch (IOException e) {
 				publishProgress(1);
 				Log.e(TAG, "IO Exception\n" + e.toString());
 			}
 			return null;
 		}
 
 		/**
 		 * Update the UI on the status of the connection
 		 * 
 		 * @param progress
 		 */
 		@SuppressWarnings("unused")
 		protected void onProgressUpdate(Integer progress) {
 			Log.i(TAG, "Progress");
 			if (progress == 1) {
 				Toast.makeText(ConnectActivity.this,
 						"Network connection failed!", Toast.LENGTH_SHORT)
 						.show();
 			} else if (progress == 2) {
 				Toast.makeText(ConnectActivity.this, "Unkown host!",
 						Toast.LENGTH_SHORT).show();
 			}
 		}
 
 		/**
 		 * Called once the doInBackground thread ends, manages the UI. On
 		 * success, shows the GridView used to manage the list of available
 		 * application, effectively giving the user access to the rest of the
 		 * application.
 		 */
 		protected void onPostExecute(Void result) {
 			Log.i(TAG, "Connection finished ");
 
 			dialog.dismiss();
 			if (Global.network.isConnected()) {
 				Toast.makeText(ConnectActivity.this, "Connected",
 						Toast.LENGTH_SHORT).show();
 
 				// Start the command parser thread
 				Thread t = new Thread(Global.network.getCommandParser(),
 						"CommandParser Thread");
 				t.start();
 
 				ipTable.put(ipAdressT.getText().toString(), portT.getText()
 						.toString());
 				System.out.println(ipTable.keySet());
 				preferencesEditor.putString("ip", SerializationTool
 						.mapToString(ipTable));
 				// save preferences
 				preferencesEditor.commit();
 				alterAdapter();
 
 				// Start application choice activity
 				startApplicationSelectorActivity();
 
 			} else {
 				Toast.makeText(ConnectActivity.this,
 						"Network connection failed", Toast.LENGTH_SHORT).show();
 			}
 
 			Global.network.sendCommand(new Command(CommandWord.HELLO));
 		}
 	} // ConnectNetwork (AsyncTask)
 
 	private TextWatcher textChangedWatcher = new TextWatcher() {
 
 		// As the user types in the search field, the list is
 		@Override
 		public void onTextChanged(CharSequence arg0, int arg1, int arg2,
 				int arg3) {
 			alterAdapter();
 		}
 
 		@Override
 		public void afterTextChanged(Editable arg0) {
 		}
 
 		@Override
 		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
 				int arg3) {
 		}
 	};
 
 	/**
 	 * We click on a server ip, start the application choice activity.
 	 */
 	private OnItemClickListener itemClickListener = new OnItemClickListener() {
 		@Override
 		public void onItemClick(AdapterView<?> parent, View v, int position,
 				long id) {
 			String ip = (String) adapter.getItem(position);
 			System.out.println("ip " + ip + "\tport "
 					+ portT.getText().toString());
 			connect(ip, portT.getText().toString());
 		}
 	};
 
 	// Filters list of contacts based on user search criteria. If no information
 	// is filled in, contact list will be fully shown
 	private void alterAdapter() {
 		System.out.println("alter " + ipAdressT.getText().toString());
 		if (ipAdressT.getText().toString().isEmpty()) {
			partialNames.clear();
			partialNames.addAll(searchNames);
 		} else {
 			partialNames.clear();
 			for (int i = 0; i < searchNames.size(); i++) {
 				if (searchNames.get(i).toString().toLowerCase().contains(
 						ipAdressT.getText().toString().toLowerCase())) {
 					partialNames.add(searchNames.get(i).toString());
 				}
 			}
 		}
 		adapter.notifyDataSetChanged();
 		System.out.println("partial " + partialNames);
 	}
 
 	private void startApplicationSelectorActivity() {
 		Intent intent = new Intent(this, ApplicationSelectorActivity.class);
 		startActivity(intent);
 	}
 
 }

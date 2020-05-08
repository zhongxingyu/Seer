 package de.uni.stuttgart.informatik.ToureNPlaner.UI.Activities;
 
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.Toast;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import de.uni.stuttgart.informatik.ToureNPlaner.Data.AlgorithmInfo;
 import de.uni.stuttgart.informatik.ToureNPlaner.Data.ServerInfo;
 import de.uni.stuttgart.informatik.ToureNPlaner.Net.Handler.RawHandler;
 import de.uni.stuttgart.informatik.ToureNPlaner.Net.Handler.ServerInfoHandler;
 import de.uni.stuttgart.informatik.ToureNPlaner.Net.Observer;
 import de.uni.stuttgart.informatik.ToureNPlaner.Net.Session;
 import de.uni.stuttgart.informatik.ToureNPlaner.R;
 import de.uni.stuttgart.informatik.ToureNPlaner.UI.Dialogs.MyProgressDialog;
 import de.uni.stuttgart.informatik.ToureNPlaner.UI.Dialogs.TextDialog;
 
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.ArrayList;
 import java.util.Collections;
 
 public class ServerScreen extends SherlockFragmentActivity implements Observer {
 	private static final String SERVERLIST_FILENAME = "serverlist";
 	private ArrayAdapter<String> adapter;
 	private ServerInfoHandler handler;
 	private ArrayList<String> servers;
 
 	public static class ConnectionProgressDialog extends MyProgressDialog {
 		static final String IDENTIFIER = "connecting";
 
 		public static ConnectionProgressDialog newInstance(String title, String message) {
 			return (ConnectionProgressDialog) MyProgressDialog.newInstance(new ConnectionProgressDialog(), title, message);
 		}
 
 		@Override
 		public void onCancel(DialogInterface dialog) {
 			((ServerScreen) getActivity()).cancelConnection();
 		}
 	}
 
 	public static class EditDialog extends TextDialog {
 		static final String IDENTIFIER = "edit";
 
 		public static EditDialog newInstance(String title, String content, int id) {
 			EditDialog dialog = (EditDialog) TextDialog.newInstance(new EditDialog(), title, content);
 			dialog.getArguments().putInt("id", id);
 			return dialog;
 		}
 
 		@Override
 		public void doPositiveClick() {
 			((ServerScreen) getActivity()).editServer(getArguments().getInt("id"), getInputField().getText().toString());
 		}
 
 		@Override
 		public void doNegativeClick() {
 		}
 	}
 
 	public static class NewDialog extends TextDialog {
 		static final String IDENTIFIER = "new";
 
 		public static NewDialog newInstance(String title, String content) {
 			return (NewDialog) TextDialog.newInstance(new NewDialog(), title, content);
 		}
 
 		@Override
 		public void doPositiveClick() {
 			((ServerScreen) getActivity()).newServer(getInputField().getText().toString());
 		}
 
 		@Override
 		public void doNegativeClick() {
 		}
 	}
 
 
 	private void editServer(int id, String server) {
 		servers.set(id, server);
 		Collections.sort(servers);
 		adapter.notifyDataSetChanged();
 		saveServerList();
 	}
 
 	private void newServer(String server) {
 		servers.add(server);
 		Collections.sort(servers);
 		adapter.notifyDataSetChanged();
 		saveServerList();
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		checkForMultipleInstances();
 
 		setContentView(R.layout.serverscreen);
 
 		loadServerList();
 
 		setupListView();
 
 		initializeHandler();
 	}
 
 	private void checkForMultipleInstances() {
 		// Possible work around for market launches. See http://code.google.com/p/android/issues/detail?id=2373
 		// for more details. Essentially, the market launches the main activity on top of other activities.
 		// we never want this to happen. Instead, we check if we are the root and if not, we finish.
 		if (!isTaskRoot()) {
 			final Intent intent = getIntent();
 			final String intentAction = intent.getAction();
 			if (intent.hasCategory(Intent.CATEGORY_LAUNCHER) && intentAction != null && intentAction.equals(Intent.ACTION_MAIN)) {
 				Log.w("TP", "Main Activity is not the root.  Finishing Main Activity instead of launching.");
 				finish();
 			}
 		}
 	}
 
 	private void saveServerList() {
 		try {
 			FileOutputStream outputStream = openFileOutput(SERVERLIST_FILENAME, MODE_PRIVATE);
 			try {
 				ObjectOutputStream out = new ObjectOutputStream(outputStream);
 				out.writeObject(servers);
 			} finally {
 				outputStream.close();
 			}
 		} catch (Exception e) {
 			Toast.makeText(getBaseContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	private void loadServerList() {
 		try {
 			FileInputStream inputStream = openFileInput(SERVERLIST_FILENAME);
 			try {
 				ObjectInputStream in = new ObjectInputStream(inputStream);
 				servers = (ArrayList<String>) in.readObject();
 				Collections.sort(servers);
 			} finally {
 				inputStream.close();
 			}
 		} catch (Exception e) {
			Toast.makeText(getBaseContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
 			servers = new ArrayList<String>();
 		}
 	}
 
 	private void initializeHandler() {
 		handler = (ServerInfoHandler) getLastCustomNonConfigurationInstance();
 
 		if (handler != null)
 			handler.setListener(this);
 		else {
 			MyProgressDialog dialog = (MyProgressDialog) getSupportFragmentManager()
 					.findFragmentByTag(ConnectionProgressDialog.IDENTIFIER);
 			if (dialog != null)
 				dialog.dismiss();
 		}
 	}
 
 	private void setupListView() {
 		ListView listView = (ListView) findViewById(android.R.id.list);
 		adapter = new ArrayAdapter<String>(this, R.layout.list_item, servers);
 		listView.setAdapter(adapter);
 
 		listView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
 			@Override
 			public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
 				AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) contextMenuInfo;
 				contextMenu.setHeaderTitle(servers.get(info.position));
 				String[] menuItems = {getResources().getString(R.string.edit), getResources().getString(R.string.delete)};
 				for (int i = 0; i < menuItems.length; i++) {
 					contextMenu.add(Menu.NONE, i, i, menuItems[i]);
 				}
 			}
 		});
 
 		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
 				serverSelected((String) adapterView.getItemAtPosition(i));
 			}
 		});
 	}
 
 	@Override
 	public boolean onContextItemSelected(android.view.MenuItem item) {
 		final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
 		switch (item.getItemId()) {
 			case 0: // edit
 				EditDialog.newInstance(getResources().getString(R.string.enter_server),
 						servers.get(info.position), info.position)
 						.show(getSupportFragmentManager(), EditDialog.IDENTIFIER);
 				break;
 			case 1: // delete
 				servers.remove(info.position);
 				adapter.notifyDataSetChanged();
 				saveServerList();
 				break;
 		}
 		return true;
 	}
 
 	private void cancelConnection() {
 		if (handler != null)
 			handler.cancel(true);
 		handler = null;
 	}
 
 	@Override
 	public void onCompleted(RawHandler caller, Object object) {
 		handler = null;
 		MyProgressDialog dialog = (MyProgressDialog) getSupportFragmentManager()
 				.findFragmentByTag(ConnectionProgressDialog.IDENTIFIER);
 		try {
 			dialog.dismiss();
 		} catch (IllegalStateException e) {
 			// Can not perform this action after onSaveInstanceState
 		}
 		Session session = (Session) object;
 		Intent myIntent;
 		if (session.getServerInfo().getServerType() == ServerInfo.ServerType.PUBLIC) {
 			myIntent = new Intent(getBaseContext(), AlgorithmScreen.class);
 		} else {
 			myIntent = new Intent(getBaseContext(), LoginScreen.class);
 		}
 
 		session.getServerInfo().getAlgorithms().add(AlgorithmInfo.createMock());
 
 		myIntent.putExtra(Session.IDENTIFIER, session);
 		startActivity(myIntent);
 	}
 
 	@Override
 	public void onError(RawHandler caller, Object object) {
 		handler = null;
 		MyProgressDialog dialog = (MyProgressDialog) getSupportFragmentManager()
 				.findFragmentByTag(ConnectionProgressDialog.IDENTIFIER);
 		try {
 			dialog.dismiss();
 		} catch (IllegalStateException e) {
 			// Can not perform this action after onSaveInstanceState
 		}
 		Toast.makeText(getApplicationContext(), object.toString(), Toast.LENGTH_LONG).show();
 	}
 
 	@Override
 	public Object onRetainCustomNonConfigurationInstance() {
 		return handler;
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		if (handler != null)
 			handler.setListener(null);
 	}
 
 	private void serverSelected(String url) {
 		// prevent double clicks
 		if (handler != null)
 			return;
 
 		// fix urls without http prefix
 		if (!url.startsWith("http")) {
 			url = "http://" + url;
 		}
 
 		ConnectionProgressDialog.newInstance(getResources().getString(R.string.connecting), url)
 				.show(getSupportFragmentManager(), ConnectionProgressDialog.IDENTIFIER);
 		handler = Session.createSession(url, this);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getSupportMenuInflater().inflate(R.menu.serverscreenmenu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 			case R.id.servercertificates:
 				startActivity(new Intent(this, CertificateScreen.class));
 				return true;
 			case R.id.add_server:
 				NewDialog.newInstance(getResources().getString(R.string.enter_server), "")
 						.show(getSupportFragmentManager(), NewDialog.IDENTIFIER);
 				return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 }

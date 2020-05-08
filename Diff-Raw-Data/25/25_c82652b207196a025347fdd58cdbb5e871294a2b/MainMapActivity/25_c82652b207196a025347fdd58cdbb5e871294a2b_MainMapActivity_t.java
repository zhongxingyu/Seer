 package pl.edu.agh.io.coordinator;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import pl.edu.agh.io.coordinator.resources.Group;
 import pl.edu.agh.io.coordinator.resources.Layer;
 import pl.edu.agh.io.coordinator.resources.MapItem;
 import pl.edu.agh.io.coordinator.resources.Message;
 import pl.edu.agh.io.coordinator.resources.Point;
 import pl.edu.agh.io.coordinator.resources.User;
 import pl.edu.agh.io.coordinator.resources.UserItem;
 import pl.edu.agh.io.coordinator.resources.UserState;
 import pl.edu.agh.io.coordinator.utils.Alerts;
 import pl.edu.agh.io.coordinator.utils.UserFilter;
 import pl.edu.agh.io.coordinator.utils.chat.ChatState;
 import pl.edu.agh.io.coordinator.utils.container.DataContainer;
 import pl.edu.agh.io.coordinator.utils.container.DataContainer.OnDataContainerChangesListener;
 import pl.edu.agh.io.coordinator.utils.layersmenu.LayersMenuListener;
 import pl.edu.agh.io.coordinator.utils.layersmenu.LayersMenuState;
 import pl.edu.agh.io.coordinator.utils.net.INetworkProxy;
 import pl.edu.agh.io.coordinator.utils.net.JSonProxy;
 import pl.edu.agh.io.coordinator.utils.net.exceptions.InvalidGroupException;
 import pl.edu.agh.io.coordinator.utils.net.exceptions.InvalidLayerException;
 import pl.edu.agh.io.coordinator.utils.net.exceptions.InvalidMapItemException;
 import pl.edu.agh.io.coordinator.utils.net.exceptions.InvalidSessionIDException;
 import pl.edu.agh.io.coordinator.utils.net.exceptions.InvalidUserException;
 import pl.edu.agh.io.coordinator.utils.net.exceptions.NetworkException;
 import android.app.Activity;
 import android.app.FragmentManager;
 import android.app.FragmentTransaction;
 import android.content.Intent;
 import android.location.Location;
 import android.os.AsyncTask;
 import android.os.AsyncTask.Status;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.WindowManager;
 import android.widget.Toast;
 
 import com.google.android.gms.common.ConnectionResult;
 import com.google.android.gms.common.GooglePlayServicesClient;
 import com.google.android.gms.location.LocationClient;
 import com.google.android.gms.maps.CameraUpdate;
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
 import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
 import com.google.android.gms.maps.MapFragment;
 import com.google.android.gms.maps.model.BitmapDescriptorFactory;
 import com.google.android.gms.maps.model.CameraPosition;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.Marker;
 import com.google.android.gms.maps.model.MarkerOptions;
 
 public class MainMapActivity extends Activity implements
 		ChatFragment.OnFragmentInteractionListener, LayersMenuListener,
 		OnDataContainerChangesListener,
 		GooglePlayServicesClient.ConnectionCallbacks,
 		GooglePlayServicesClient.OnConnectionFailedListener {
 
 	private static final LatLng DEFAULT_LOCATION = new LatLng(50.061671d, 19.937341d);
 	
 	public final static String ITEM_POSITION = "pl.edu.agh.io.coordinator.ITEM_POSITION";
 	public final static String EXTRA_CONTENT = "pl.edu.agh.io.coordinator.EXTRA_CONTENT";
 	public final static String EXTRA_CONTENT_TYPE = "pl.edu.agh.io.coordinator.EXTRA_CONTENT_TYPE";
 	public static MainMapActivity currentInstance = null;
 
 	public enum ContentType {
 		NOTE, IMAGE, VIDEO, USER;
 	}
 
 	private boolean layersMenuVisible = false;
 	private boolean chatVisible = false;
 
 	private boolean isMenuCreated = false;
 
 	private volatile boolean isActive = false;
 
 	private DataContainer dataContainer = new DataContainer(this);
 	private boolean loggingOut = false;
 	private LayersMenuFragment layersFragment = new LayersMenuFragment();
 	private LayersMenuState savedLayersMenuState = null;
 	private ChatState savedChatState = null;
 
 	private GoogleMap googleMap;
 	private LocationClient locationClient;
 	private Location myLocation;
 
 	private ChatFragment chatFragment = ChatFragment.newInstance();
 
 	private Set<Layer> layers;
 
 	private HashMap<MapItem, Marker> mapItemToMarker = new HashMap<MapItem, Marker>();
 	private HashMap<User, Marker> mapUserToMarker = new HashMap<User, Marker>();
 
 	private MainThread mainThread;
 
 	private Map<String, Boolean> activeLayers = new HashMap<String, Boolean>();
 
 	private Map<Marker, ContentType> markerTypes = Collections.synchronizedMap(new HashMap<Marker, ContentType>());
 	
 	private UserFilter userFilter = new UserFilter();
 
 	public UserFilter getUserFilter() {
 		return userFilter;
 	}
 
 	public ContentType getMarkerType(Marker marker) {
 		return markerTypes.get(marker);
 	}
 	
 	private class MainThread extends Thread {
 
 		private boolean stopped = false;
 
 		public synchronized void safelyStop() {
 			stopped = true;
 		}
 
 		@Override
 		public void run() {
 			boolean isStopped = false;
 			GetUsersInBackground usersThread = null;
 			GetMessagesInBackground messagesThread = null;
 			Map<Layer, GetMapItemsInBackground> mapItemsThreadMap = new HashMap<Layer, GetMapItemsInBackground>();
 			while (true) {
 				try {
 					Thread.sleep(1000);
 					Log.d("MainMapActivity", "getting data from server");
 					usersThread = new GetUsersInBackground();
 					usersThread
 							.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
 					if (chatFragment.isActive()) {
 						messagesThread = new GetMessagesInBackground();
 						messagesThread
 								.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
 					}
 					if (layers != null)
 						for (Layer layer : layers) {
 							mapItemsThreadMap.put(layer,
 									new GetMapItemsInBackground());
 							mapItemsThreadMap.get(layer).executeOnExecutor(
 									AsyncTask.THREAD_POOL_EXECUTOR, layer);
 						}
 					synchronized (this) {
 						Log.d("MainMapActivity.MainThread",
 								"checking isStopped");
 						isStopped = stopped;
 					}
 					if (usersThread != null) {
 						while (true) {
 							Status status = usersThread.getStatus();
 							if (status == Status.FINISHED) {
 								break;
 							}
 						}
 					}
 					if (messagesThread != null) {
 						while (true) {
 							Status status = messagesThread.getStatus();
 							if (status == Status.FINISHED) {
 								break;
 							}
 						}
 					}
 					for (GetMapItemsInBackground mapItemThread : mapItemsThreadMap
 							.values()) {
 						while (true) {
 							Status status = mapItemThread.getStatus();
 							if (status == Status.FINISHED) {
 								break;
 							}
 						}
 					}
 					if (isStopped) {
 						Log.d("MainMapActivity.MainThread",
 								"stopping main thread");
 						break;
 					}
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}
 
 	}
 
 	public LayersMenuState getSavedLayersMenuState() {
 		return this.savedLayersMenuState;
 	}
 
 	public void setSavedLayersMenuState(LayersMenuState state) {
 		this.savedLayersMenuState = state;
 	}
 
 	public ChatState getSavedChatState() {
 		return this.savedChatState;
 	}
 
 	public void setSavedChatState(ChatState state) {
 		this.savedChatState = state;
 	}
 
 	public boolean isLayerActive(String layer) {
 		if (activeLayers.containsKey(layer)) {
 			return this.activeLayers.get(layer);
 		} else {
 			return false;
 		}
 	}
 
 	private void initFragments(FragmentManager fragmentManager) {
 		FragmentTransaction fragmentTransaction = fragmentManager
 				.beginTransaction();
 		fragmentTransaction.add(R.id.layersFrame, layersFragment);
 		fragmentTransaction.hide(layersFragment);
 		fragmentTransaction.add(R.id.chatFrame, chatFragment);
 		fragmentTransaction.hide(chatFragment);
 		fragmentTransaction.commit();
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		Log.d("MainMapActivity", "starting onCreate");
 		super.onCreate(savedInstanceState);
 
 		currentInstance = this;
 
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
 				WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		setContentView(R.layout.activity_main_map);
 
 		FragmentManager fragmentManager = getFragmentManager();
 		initFragments(fragmentManager);
 
 		new GetLayersInBackground().executeOnExecutor(
 				AsyncTask.THREAD_POOL_EXECUTOR, new Intent());
 
 		locationClient = new LocationClient(this, this, this);
 		setUpMapIfNeeded();
 
 		if (savedInstanceState != null) {
 			Log.d("MainMapActivity", "starting getting saved state");
 			layersMenuVisible = savedInstanceState
 					.getBoolean("layersMenuVisible");
 			chatVisible = savedInstanceState.getBoolean("chatVisible");
 			savedLayersMenuState = savedInstanceState
 					.getParcelable("savedLayersMenuState");
 			activeLayers = new HashMap<String, Boolean>(
 					savedLayersMenuState.layersChecks);
 			savedChatState = savedInstanceState.getParcelable("savedChatState");
 			if (savedChatState != null) {
 				Log.d("MainMapActivity", "starting ... "
 						+ savedChatState.messages.size());
 			} else {
 				Log.d("MainMapActivity", "starting ... " + "null");
 			}
 			CameraUpdate update = CameraUpdateFactory
 					.newCameraPosition((CameraPosition) savedInstanceState
 							.getParcelable("cameraPosition"));
 			googleMap.moveCamera(update);
 		} else {
 			Map<String, Boolean> activeLayers = new HashMap<String, Boolean>();
 			activeLayers.put("notes", true);
 			activeLayers.put("images", true);
 			activeLayers.put("videos", true);
 			this.activeLayers = activeLayers;
 			AsyncTask<Void, Void, Void> setLocation = new AsyncTask<Void, Void, Void>() {
 
 				private LatLng latLng;
 
 				@Override
 				protected Void doInBackground(Void... params) {
 					while (!locationClient.isConnected()) {
 					}
 					Location location = locationClient.getLastLocation();
 					if (location == null) {
 						latLng = DEFAULT_LOCATION;
 					} else {
 						latLng = new LatLng(location.getLatitude(), location.getLongitude());
 						Log.d("MainMapActivity", "LOCATION: " + location.getLatitude() + " " + location.getLongitude());
 					}
 					return null;
 				}
 
 				@Override
 				protected void onPostExecute(Void result) {
 					CameraUpdate cameraUpdate = CameraUpdateFactory
 							.newLatLngZoom(latLng, 16.0f);
 					googleMap.moveCamera(cameraUpdate);
 				}
 
 			};
 			setLocation.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
 					(Void[]) null);
 		}
 
 		googleMap.setMyLocationEnabled(true);
 		googleMap.getUiSettings().setMyLocationButtonEnabled(true);
 
 	}
 
 	@Override
 	protected void onRestart() {
 		Log.d("MainMapActivity", "starting onRestart");
 		FragmentManager fragmentManager = getFragmentManager();
 		initFragments(fragmentManager);
 		super.onRestart();
 	}
 
 	@Override
 	protected void onStart() {
 		Log.d("MainMapActivity", "starting onStart");
 		super.onStart();
 	}
 
 	@Override
 	protected void onResume() {
 		Log.d("MainMapActivity", "starting onResume");
 		setUpMapIfNeeded();
 		if (!isMenuCreated) {
 			invalidateOptionsMenu();
 		}
 		mainThread = new MainThread();
 		mainThread.start();
 		isActive = true;
 		locationClient.connect();
 		super.onResume();
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		Log.d("MainMapActivity", "starting onSaveInstanceState");
 		FragmentManager fragmentManager = getFragmentManager();
 		FragmentTransaction fragmentTransaction = fragmentManager
 				.beginTransaction();
 		fragmentTransaction.remove(layersFragment);
 		fragmentTransaction.remove(chatFragment);
 		fragmentTransaction.commit();
 		outState.putBoolean("layersMenuVisible", layersMenuVisible);
 		outState.putBoolean("chatVisible", chatVisible);
 		outState.putParcelable("savedLayersMenuState", savedLayersMenuState);
 		outState.putParcelable("savedChatState", savedChatState);
 		outState.putParcelable("cameraPosition", googleMap.getCameraPosition());
 		super.onSaveInstanceState(outState);
 	}
 
 	@Override
 	protected void onPause() {
 		Log.d("MainMapActivity", "starting onPause");
 		isMenuCreated = false;
 		mainThread.safelyStop();
 		isActive = false;
 		locationClient.disconnect();
 		super.onPause();
 	}
 
 	@Override
 	protected void onStop() {
 		Log.d("MainMapActivity", "starting onStop");
 		super.onStop();
 	}
 
 	@Override
 	protected void onDestroy() {
 		Log.d("MainMapActivity", "starting onDestroy");
 		super.onDestroy();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		Log.d("MainMapActivity", "starting onCreateOptionsMenu");
 		getMenuInflater().inflate(R.menu.main_map, menu);
 		menu.getItem(0).setChecked(chatVisible);
 		menu.getItem(1).setChecked(layersMenuVisible);
 		isMenuCreated = true;
 		return true;
 	}
 
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		Log.d("MainMapActivity", "starting onPrepareOptionsMenu");
 		FragmentManager fragmentManager = getFragmentManager();
 		if (layersMenuVisible) {
 			Log.d("MainMapActivity", "getting saved state: layersMenuVisible");
 			FragmentTransaction lmvTransaction = fragmentManager
 					.beginTransaction();
 			lmvTransaction.show(layersFragment);
 			lmvTransaction.commit();
 		}
 		if (chatVisible) {
 			Log.d("MainMapActivity", "getting saved state: chatVisible");
 			FragmentTransaction cvTransaction = fragmentManager
 					.beginTransaction();
 			cvTransaction.show(chatFragment);
 			cvTransaction.commit();
 		}
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.actionChat:
 			Log.d("MainMapActivity", "LMDEBUG: Chat clicked");
 			chatVisible = !item.isChecked();
 			item.setChecked(!item.isChecked());
 			FragmentManager fragmentManager = getFragmentManager();
 			FragmentTransaction fragmentTransaction = fragmentManager
 					.beginTransaction();
 			if (item.isChecked()) {
 				fragmentTransaction.show(chatFragment);
 			} else
 				fragmentTransaction.hide(chatFragment);
 			fragmentTransaction.commit();
 			return true;
 		case R.id.actionLayers:
 			Log.d("MainMapActivity", "LMDEBUG: Layers menu clicked");
 			layersMenuVisible = !item.isChecked();
 			item.setChecked(!item.isChecked());
 			FragmentManager fragmentManager2 = getFragmentManager();
 			FragmentTransaction fragmentTransaction2 = fragmentManager2
 					.beginTransaction();
 			if (item.isChecked()) {
 				// fragmentTransaction2.add(R.id.layersFrame, layersFragment);
 				fragmentTransaction2.show(layersFragment);
 			} else
 				// fragmentTransaction2.remove(layersFragment);
 				fragmentTransaction2.hide(layersFragment);
 			fragmentTransaction2.commit();
 			return true;
 		case R.id.actionCreateGroup:
 			Intent intentCreateGroup = new Intent(MainMapActivity.this,
 					CreateGroupActivity.class);
 			startActivity(intentCreateGroup);
 			return true;
 		case R.id.actionRemoveGroup:
 			Intent intentRemoveGroup = new Intent(MainMapActivity.this,
 					RemoveGroupActivity.class);
 			startActivity(intentRemoveGroup);
 			return true;
 		case R.id.actionCreateItem:
 			myLocation = locationClient.getLastLocation();
 			Intent intentCreateItem = new Intent(MainMapActivity.this,
 					CreateMapItemActivity.class);
 			intentCreateItem.putExtra(ITEM_POSITION, myLocation);
 			startActivity(intentCreateItem);
 			return true;
 		case R.id.actionSettings:
 			Intent intentSettings = new Intent(MainMapActivity.this,
 					NotImplementedYetActivity.class);
 			startActivity(intentSettings);
 			return true;
 		case R.id.actionLogout:
 			if (!loggingOut) {
 				loggingOut = true;
 				Intent intent = new Intent();
 				new LogoutInBackground().executeOnExecutor(
 						AsyncTask.THREAD_POOL_EXECUTOR, intent);
 				this.finish();
 			}
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if (keyCode == KeyEvent.KEYCODE_BACK) {
 			Intent intent = new Intent();
 			new LogoutInBackground().executeOnExecutor(
 					AsyncTask.THREAD_POOL_EXECUTOR, intent);
 		}
 		return super.onKeyDown(keyCode, event);
 	}
 
 	private void setUpMapIfNeeded() {
 		if (googleMap == null) {
 			googleMap = ((MapFragment) getFragmentManager().findFragmentById(
 					R.id.mainMapFragment)).getMap();
 			if (googleMap == null) {
 				// TODO: print error
 			} else {
 				googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
 
 				googleMap
 						.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
 
 							@Override
 							public void onInfoWindowClick(Marker marker) {
 								Log.d("MainMapActivity", "clicked marker, type = " + getMarkerType(marker));
 								Toast.makeText(getApplicationContext(),
 										marker.getSnippet(), Toast.LENGTH_SHORT)
 										.show();
 								Intent content = new Intent(
 										MainMapActivity.this,
 										DisplayContentActivity.class);
 								content.putExtra(EXTRA_CONTENT_TYPE, getMarkerType(marker).toString());
 								content.putExtra(EXTRA_CONTENT,
 										marker.getSnippet());
 								startActivity(content);
 							}
 
 						});
 
 				googleMap
 						.setOnMapLongClickListener(new OnMapLongClickListener() {
 
 							@Override
 							public void onMapLongClick(LatLng arg0) {
 								Intent intentCreateItem = new Intent(
 										MainMapActivity.this,
 										CreateMapItemActivity.class);
 								Location location = new Location("Map item");
 								location.setLatitude(arg0.latitude);
 								location.setLongitude(arg0.longitude);
 								intentCreateItem.putExtra(ITEM_POSITION,
 										location);
 								startActivity(intentCreateItem);
 							}
 
 						});
 
 			}
 		}
 	}
 
 	private class LogoutInBackground extends AsyncTask<Intent, Void, Exception> {
 
 		@Override
 		protected Exception doInBackground(Intent... params) {
 			INetworkProxy proxy = JSonProxy.getInstance();
 			try {
 				mainThread.safelyStop();
 				while (mainThread.isAlive()) {
 				}
 				proxy.logout();
 			} catch (InvalidSessionIDException e) {
 				return e;
 			} catch (NetworkException e) {
 				return e;
 			}
 
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(Exception result) {
 
 			if (result == null) {
 				MainMapActivity.this.finish();
 			} else if (result instanceof NetworkException) {
 				Alerts.networkProblem(MainMapActivity.this);
 				loggingOut = false;
 			} else if (result instanceof InvalidSessionIDException) {
 				if (isActive) {
 					Alerts.invalidSessionId(MainMapActivity.this,
 							new LogoutInBackground());
 				}
 			}
 		}
 
 	}
 
 	private class GetUsersInBackground extends AsyncTask<Void, Void, Exception> {
 
 		private Set<User> users;
 		private Map<User, UserState> userStates;
 		private Map<User, Set<String>> userItemSets;
 		private Set<UserItem> userItems;
 		private Set<Group> groups;
 		private Map<Group, Set<String>> groupUserSets;
 
 		@Override
 		protected Exception doInBackground(Void... params) {
 			Log.d("MainMapActivity", "GetUsersInBackground.doInBackground()");
 			INetworkProxy proxy = JSonProxy.getInstance();
 			userStates = new HashMap<User, UserState>();
 			userItemSets = new HashMap<User, Set<String>>();
 			groupUserSets = new HashMap<Group, Set<String>>();
 			try {
 				users = proxy.getUsers();
 				for (User u : users) {
 					UserState state = proxy.getUserState(u.getId());
 					Set<String> items = proxy.getUserItems(u);
 					if (state != null) {
 						userStates.put(u, state);
 					}
 					if (items != null) {
 						userItemSets.put(u, items);
 					}
 				}
 				userItems = proxy.getPossibleUserItems();
 				groups = proxy.getGroups();
 				for (Group g : groups) {
 					Set<String> users = proxy.getGroupUsers(g);
 					if (users != null) {
 						groupUserSets.put(g, users);
 					}
 				}
 				if (locationClient.isConnected()) {
 					Location location = locationClient.getLastLocation();
 					if (location != null) {
 						UserState currentState = new UserState(new Point(
 								location.getLatitude(), location.getLongitude()),
 								location.getSpeed());
 						proxy.updateSelfState(currentState);
 					}
 				}
 			} catch (InvalidSessionIDException e) {
 				return e;
 			} catch (NetworkException e) {
 				return e;
 			} catch (InvalidUserException e) {
 				return e;
 			} catch (InvalidGroupException e) {
 				return e;
 			}
 
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(Exception result) {
 
 			if (result == null) {
 				if (MainMapActivity.this.layersFragment != null) {
 					MainMapActivity.this.layersFragment.setPeople(users);
 					MainMapActivity.this.layersFragment.setItems(userItems);
 					MainMapActivity.this.layersFragment.setGroups(groups);
 					dataContainer.newUsersSet(userStates);
 					dataContainer.newUserItemsSet(userItemSets);
 					dataContainer.newGroupsSet(groupUserSets);
 				}
 				if (layersFragment != null) {
 
 				}
 			} else if (result instanceof NetworkException) {
 				if (isActive) {
 					Alerts.networkProblem(MainMapActivity.this);
 				}
 			} else if (result instanceof InvalidSessionIDException) {
 				if (isActive) {
 					Alerts.invalidSessionId(MainMapActivity.this,
 							new LogoutInBackground());
 				}
 			} else if (result instanceof InvalidUserException) {
 				// Alerts.invalidUser(MainMapActivity.this); // wywalone bo
 				// niepotrzebnie wyswietla alert
 			} else if (result instanceof InvalidGroupException) {
 				// jak wyzej
 			}
 		}
 
 	}
 
 	private class GetLayersInBackground extends
 			AsyncTask<Intent, Void, Exception> {
 
 		@Override
 		protected Exception doInBackground(Intent... params) {
 			INetworkProxy proxy = JSonProxy.getInstance();
 
 			try {
 				layers = proxy.getLayers();
 			} catch (InvalidSessionIDException e) {
 				return e;
 			} catch (NetworkException e) {
 				return e;
 			}
 
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(Exception result) {
 			if (result == null) {
 				layersFragment.setLayers(layers);
 			} else if (result instanceof NetworkException) {
 				if (isActive) {
 					Alerts.networkProblem(MainMapActivity.this);
 				}
 			} else if (result instanceof InvalidSessionIDException) {
 				if (isActive) {
 					Alerts.invalidSessionId(MainMapActivity.this,
 							new LogoutInBackground());
 				}
 			}
 		}
 
 	}
 
 	private class GetMapItemsInBackground extends
 			AsyncTask<Layer, Void, Exception> {
 
 		Set<MapItem> mapItems;
 		Layer layer;
 
 		@Override
 		protected Exception doInBackground(Layer... params) {
 
 			Log.d("MainMapActivity", "GetMapItemsInBackground.doInBackground()");
 
 			INetworkProxy proxy = JSonProxy.getInstance();
 
 			try {
 				mapItems = proxy.getMapItems(params[0]);
 				layer = params[0];
 			} catch (InvalidSessionIDException e) {
 				return e;
 			} catch (NetworkException e) {
 				return e;
 			} catch (InvalidLayerException e) {
 				return e;
 			}
 
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(Exception result) {
 			if (result == null) {
 				if (layersFragment != null)
 					dataContainer.newMapItemsSet(layer, mapItems);
 			} else if (result instanceof NetworkException) {
 				if (isActive) {
 					Alerts.networkProblem(MainMapActivity.this);
 				}
 			} else if (result instanceof InvalidSessionIDException) {
 				if (isActive) {
 					Alerts.invalidSessionId(MainMapActivity.this,
 							new LogoutInBackground());
 				}
 			} else if (result instanceof InvalidLayerException) {
 				if (isActive) {
 					Alerts.invalidLayer(MainMapActivity.this);
 				}
 			}
 		}
 
 	}
 
 	private class RemoveMapItemInBackground extends
 			AsyncTask<MapItem, Void, Exception> {
 
 		@Override
 		protected Exception doInBackground(MapItem... params) {
 			INetworkProxy proxy = JSonProxy.getInstance();
 
 			try {
 				proxy.removeMapItem(params[0]);
 			} catch (InvalidSessionIDException e) {
 				return e;
 			} catch (NetworkException e) {
 				return e;
 			} catch (InvalidMapItemException e) {
 				return e;
 			}
 
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(Exception result) {
 
 			if (result instanceof NetworkException) {
 				if (isActive) {
 					Alerts.networkProblem(MainMapActivity.this);
 				}
 			} else if (result instanceof InvalidSessionIDException) {
 				if (isActive) {
 					Alerts.invalidSessionId(MainMapActivity.this,
 							new LogoutInBackground());
 				}
 			} else if (result instanceof InvalidMapItemException) {
 				if (isActive) {
 					Alerts.invalidMapItem(MainMapActivity.this);
 				}
 			}
 		}
 
 	}
 
 	private class SendMessageInBackground extends
 			AsyncTask<String, Void, Exception> {
 
 		@Override
 		protected Exception doInBackground(String... params) {
 			INetworkProxy proxy = JSonProxy.getInstance();
 			Log.d("MainMapActivity", "SendMessageInBackground.doInBackground()");
 			try {
 				proxy.sendMessage(params[0]);
 			} catch (InvalidSessionIDException e) {
 				return e;
 			} catch (NetworkException e) {
 				return e;
 			}
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(Exception result) {
 			Log.d("MainMapActivity", "SendMessageInBackground.onPostExecute()");
 			if (result instanceof NetworkException) {
 				if (isActive) {
 					Alerts.networkProblem(MainMapActivity.this);
 				}
 			} else if (result instanceof InvalidSessionIDException) {
 				if (isActive) {
 					Alerts.invalidSessionId(MainMapActivity.this,
 							new LogoutInBackground());
 				}
 			}
 		}
 
 	}
 
 	private class GetMessagesInBackground extends
 			AsyncTask<Void, Void, Exception> {
 
 		List<Message> messages;
 
 		@Override
 		protected Exception doInBackground(Void... params) {
 			INetworkProxy proxy = JSonProxy.getInstance();
 
 			try {
 				messages = proxy.getMessages();
 			} catch (InvalidSessionIDException e) {
 				return e;
 			} catch (NetworkException e) {
 				return e;
 			}
 
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(Exception result) {
 
 			if (result == null) {
 				for (Message m : messages) {
 					if (!chatFragment.isActive()) {
 						Log.d("MainMapActivity", "starting dropping message");
 						new WaitForChatActivation(m).start();
 						// new
 						// AddMessageAfterActivation(m).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
 						// (Void) null);
 					} else {
 						Log.d("MainMapActivity", "starting adding message");
 						chatFragment.newMessage(m);
 					}
 				}
 			} else if (result instanceof NetworkException) {
 				if (isActive) {
 					Alerts.networkProblem(MainMapActivity.this);
 				}
 			} else if (result instanceof InvalidSessionIDException) {
 				if (isActive) {
 					Alerts.invalidSessionId(MainMapActivity.this,
 							new LogoutInBackground());
 				}
 			}
 		}
 
 	}
 
 	private class WaitForChatActivation extends Thread {
 
 		private Message message;
 
 		public WaitForChatActivation(Message message) {
 			this.message = message;
 		}
 
 		@Override
 		public void run() {
 			Log.d("MainMapActivity", "starting waiting thread");
 			ChatFragment chatFragment = MainMapActivity.this.chatFragment;
 			while (!chatFragment.isActive()) {
 				ChatFragment tempChatFragment = MainMapActivity.currentInstance.chatFragment;
 				if (tempChatFragment != null) {
 					chatFragment = tempChatFragment;
 				}
 			}
 			Log.d("MainMapActivity", "finished waiting thread");
 			new AddMessageAfterActivation(message).executeOnExecutor(
 					AsyncTask.THREAD_POOL_EXECUTOR, (Void) null);
 		}
 
 	}
 
 	private class AddMessageAfterActivation extends AsyncTask<Void, Void, Void> {
 
 		private Message message;
 
 		public AddMessageAfterActivation(Message message) {
 			this.message = message;
 		}
 
 		@Override
 		protected Void doInBackground(Void... params) {
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(Void result) {
 			ChatFragment chatFragment = MainMapActivity.currentInstance.chatFragment;
 			if ((chatFragment != null) && (chatFragment.isActive())) {
 				Log.d("MainMapActivity",
 						"starting adding message after waiting");
 				chatFragment.newMessage(message);
 			} else {
 				Log.d("MainMapActivity", "starting dropping message again");
 				new WaitForChatActivation(message).start();
 			}
 		}
 
 	}
 
 	@Override
 	public void onChatSendMessage(String text) {
 		new SendMessageInBackground().executeOnExecutor(
 				AsyncTask.THREAD_POOL_EXECUTOR, text);
 	}
 
 	@Override
 	public void itemChecked(String item) {
 		Log.d("MainMapActivity", "executing itemChecked, item = " + item);
 		userFilter.addUserItem(item);
 		dataContainer.repaintUsers();
 	}
 
 	@Override
 	public void itemUnchecked(String item) {
 		Log.d("MainMapActivity", "executing itemUnhecked, item = " + item);
 		userFilter.removeUserItem(item);
 		dataContainer.repaintUsers();
 	}
 
 	@Override
 	public void userChecked(String user) {
 		Log.d("MainMapActivity", "executing userChecked, user = " + user);
 		userFilter.addUser(user);
 		dataContainer.repaintUsers();
 	}
 
 	@Override
 	public void userUnchecked(String user) {
 		Log.d("MainMapActivity", "executing userUnchecked, user = " + user);
 		userFilter.removeUser(user);
 		dataContainer.repaintUsers();
 	}
 
 	@Override
 	public void groupChecked(String group) {
 		Log.d("MainMapActivity", "executing groupChecked, group = " + group);
 		userFilter.addGroup(group);
 		dataContainer.repaintUsers();
 	}
 
 	@Override
 	public void groupUnchecked(String group) {
 		Log.d("MainMapActivity", "executing groupUnchecked, group = " + group);
 		userFilter.removeGroup(group);
 		dataContainer.repaintUsers();
 	}
 
 	@Override
 	public void layerChecked(String layer) {
 		Log.d("MainMapActivity", "executing layerChecked, layer = " + layer);
 		activeLayers.put(layer, true);
 		dataContainer.showLayer(layer);
 	}
 
 	@Override
 	public void layerUnchecked(String layer) {
 		Log.d("MainMapActivity", "executing layerUnchecked, layer = " + layer);
 		activeLayers.put(layer, false);
 		dataContainer.hideLayer(layer);
 	}
 
 	@Override
 	public void mapItemAdded(Layer layer, MapItem mapItem) {
 		Log.d("MainMapActivity", "adding mapItem " + mapItem.getData()
 				+ " to layer " + layer.getName());
 		if (layer.getName().equals("notes")) {
 			if (googleMap != null) {
 				Marker marker = googleMap.addMarker(new MarkerOptions()
 						.position(
 								new LatLng(mapItem.getPosition().getLatitude(),
 										mapItem.getPosition().getLongitude()))
 						.title(getString(R.string.layer_note))
 						.snippet(mapItem.getData())
 						.icon(BitmapDescriptorFactory
 								.fromResource(R.drawable.note)));
 				// .icon(BitmapDescriptorFactory.fromResource(R.drawable.action_help)));
 				mapItemToMarker.put(mapItem, marker);
 				markerTypes.put(marker, ContentType.NOTE);
 			}
 		} else if (layer.getName().equals("images")) {
 			if (googleMap != null) {
 				Marker marker = googleMap.addMarker(new MarkerOptions()
 						.position(
 								new LatLng(mapItem.getPosition().getLatitude(),
 										mapItem.getPosition().getLongitude()))
 						.title(getString(R.string.layer_image))
 						.snippet(mapItem.getData())
 						.icon(BitmapDescriptorFactory
 								.fromResource(R.drawable.photo)));
 				mapItemToMarker.put(mapItem, marker);
 				markerTypes.put(marker, ContentType.IMAGE);
 			}
 		} else if (layer.getName().equals("videos")) {
 			if (googleMap != null) {
 				Marker marker = googleMap.addMarker(new MarkerOptions()
 						.position(mapItem.getPosition().getLatLng())
 						.title(getString(R.string.layer_video))
 						.snippet(mapItem.getData())
 						.icon(BitmapDescriptorFactory
 								.fromResource(R.drawable.video)));
 				mapItemToMarker.put(mapItem, marker);
 				markerTypes.put(marker, ContentType.VIDEO);
 			}
 		}
 	}
 
 	@Override
 	public void mapItemRemoved(Layer layer, MapItem mapItem) {
 		Log.d(this.toString(), "removing mapItem " + mapItem.getData()
 				+ " from layer " + layer.getName());
 		if (mapItemToMarker.containsKey(mapItem)) {
 			Marker marker = mapItemToMarker.get(mapItem);
 			mapItemToMarker.remove(mapItem);
 			markerTypes.remove(marker);
 			marker.remove(); // remove Marker from map
 		}
 	}
 
 	@Override
 	public void userAdded(User user, UserState state) {
 		if (googleMap != null) {
 			if (mapUserToMarker.containsKey(user)) {
 				Marker marker = mapUserToMarker.get(user);
				marker.setPosition(state.getPosition().getLatLng());
			} else {
				Marker marker = googleMap.addMarker(new MarkerOptions().position(state.getPosition().getLatLng())
						.title(user.getName() + " " + user.getSurname())
						.icon(BitmapDescriptorFactory.fromResource(R.drawable.person)));
				mapUserToMarker.put(user, marker);
				markerTypes.put(marker, ContentType.USER);
 			}
 		}
 	}
 
 	@Override
 	public void userRemoved(User user) {
 		if (mapUserToMarker.containsKey(user)) {
 			Marker marker = mapUserToMarker.get(user);
 			mapUserToMarker.remove(user);
 			markerTypes.remove(marker);
 			marker.remove();
 		}
 	}
 
 	@Override
 	public void onConnectionFailed(ConnectionResult arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onConnected(Bundle arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onDisconnected() {
 		// TODO Auto-generated method stub
 
 	}
 
 }

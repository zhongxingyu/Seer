 package de.uni.stuttgart.informatik.ToureNPlaner.UI.Activities;
 
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.drawable.Drawable;
 import android.location.Location;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.view.*;
 import android.widget.Toast;
 import de.uni.stuttgart.informatik.ToureNPlaner.Data.Node;
 import de.uni.stuttgart.informatik.ToureNPlaner.Data.NodeModel;
 import de.uni.stuttgart.informatik.ToureNPlaner.Data.Result;
 import de.uni.stuttgart.informatik.ToureNPlaner.Net.*;
 import de.uni.stuttgart.informatik.ToureNPlaner.R;
 import de.uni.stuttgart.informatik.ToureNPlaner.UI.Overlays.NodeOverlay;
 import org.mapsforge.android.maps.*;
 
 import java.util.ArrayList;
 
 public class MapScreen extends MapActivity {
 	private MapView mapView;
 	private ArrayWayOverlay wayOverlay;
 	private Session session;
 	public final static int REQUEST_CODE_MAP_SCREEN = 0;
 	private NodeOverlay nodeOverlay;
 	private RequestHandler handler = null;
 
 	private final ArrayList<RequestNN> requestList = new ArrayList<RequestNN>();
 
 	private final Observer requestListener = new Observer() {
 		@Override
 		public void onCompleted(ConnectionHandler caller, Object object) {
 			handler = null;
 			Result result = (Result) object;
 			session.setResult(result);
 			addPathToMap(result.getPoints());
 			setProgressBarIndeterminateVisibility(false);
 		}
 
 		@Override
 		public void onError(ConnectionHandler caller, Object object) {
 			handler = null;
 			setProgressBarIndeterminateVisibility(false);
 			Toast.makeText(getApplicationContext(), object.toString(),
 					Toast.LENGTH_LONG).show();
 		}
 	};
 
 	private final Observer nnsListener = new Observer() {
 		@Override
 		public void onCompleted(ConnectionHandler caller, Object object) {
 			((RequestNN) caller).getNode().setGeoPoint((GeoPoint) object);
 			nodeOverlay.onModelChanged();
 			requestList.remove((RequestNN) caller);
 		}
 
 		@Override
 		public void onError(ConnectionHandler caller, Object object) {
 			Toast.makeText(getApplicationContext(), object.toString(),
 					Toast.LENGTH_LONG).show();
 			requestList.remove((RequestNN) caller);
 		}
 	};
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		outState.putSerializable(Session.IDENTIFIER, session);
 		super.onSaveInstanceState(outState);
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 
 		boolean isFirstStart = savedInstanceState == null;
 		// If we get created for the first time we get our data from the intent
 		if (savedInstanceState != null) {
 			session = (Session) savedInstanceState.getSerializable(Session.IDENTIFIER);
 		} else {
 			session = (Session) getIntent().getSerializableExtra(Session.IDENTIFIER);
 		}
 
 		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
 
 		// setting properties of the mapview
 		setContentView(R.layout.activity_mapscreen);
 		this.mapView = (MapView) findViewById(R.id.mapView);
 		mapView.setClickable(true);
 		mapView.setLongClickable(true);
 		mapView.setBuiltInZoomControls(true);
 		mapView.setMapViewMode(MapViewMode.CUSTOM_TILE_DOWNLOAD);
 		mapView.setMapTileDownloadServer("gerbera.informatik.uni-stuttgart.de/osm/tiles");
 		// mapView.setMapFile("/sdcard/berlin.map");
 		//mapView.setFpsCounter(true);
 		mapView.setMemoryCardCachePersistence(true);
 		mapView.setMemoryCardCacheSize(100);//overlay for nodeItems
 
 		initializeHandler();
 
 		setupWayOverlay();
 
 		setupGPS(isFirstStart);
 
 		mapView.getOverlays().add(nodeOverlay);
 	}
 
 	private void setupGPS(boolean isFirstStart) {
 		LocationManager locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 		Location loc = locManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
 
 		GeoPoint gpsGeoPoint = null;
 
 		if (loc != null) {
 			gpsGeoPoint = new GeoPoint(loc.getLatitude(), loc.getLongitude());
 		}
 
 		// setting up LocationManager and set MapFocus on lastknown GPS-Location
 		if (isFirstStart) {
 			mapView.getController().setCenter(gpsGeoPoint);
 		}
 
 		Drawable drawable = this.getResources().getDrawable(R.drawable.markericon);
 		nodeOverlay = new NodeOverlay(this, session.getSelectedAlgorithm(), session.getNodeModel(), gpsGeoPoint, drawable);
 
 		// 5 minutes, 50 meters
 		locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5 * 60 * 1000, 50, nodeOverlay);
 	}
 
 	private void setupWayOverlay() {
 		Paint wayDefaultPaintOutline = new Paint(Paint.ANTI_ALIAS_FLAG);
 		wayDefaultPaintOutline.setStyle(Paint.Style.STROKE);
 		wayDefaultPaintOutline.setColor(Color.BLUE);
 		wayDefaultPaintOutline.setAlpha(160);
 		wayDefaultPaintOutline.setStrokeWidth(5);
 		wayDefaultPaintOutline.setStrokeJoin(Paint.Join.ROUND);
 
 		// create the WayOverlay and add the ways
 		wayOverlay = new ArrayWayOverlay(wayDefaultPaintOutline, null);
 		mapView.getOverlays().add(wayOverlay);
 		Result result = session.getResult();
 		if (result != null) {
 			addPathToMap(result.getPoints());
 		}
 	}
 
 	// ----------------Menu-----------------
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.mapscreenmenu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		switch (item.getItemId()) {
 			case R.id.nodelist:
 				// generates an intent from the class NodeListScreen
 				Intent myIntent = new Intent(this, NodelistScreen.class);
 				myIntent.putExtra(Session.IDENTIFIER, session);
 				startActivityForResult(myIntent, REQUEST_CODE_MAP_SCREEN);
 				return true;
 			case R.id.reset:
 				// clear nodes
 				nodeOverlay.clear();
 				session.setResult(null);
 				onInvalidate();
 				return true;
 			case R.id.calculate:
 				if (session.getNodeModel().size() > 1) {
 					handler = (RequestHandler) new RequestHandler(session, requestListener).execute();
 					setProgressBarIndeterminateVisibility(true);
 				}
 				return true;
 
 			case R.id.resultlist:
 
 //				Intent myIntentResult = new Intent(this, NodeResultlistScreen.class);
 //				myIntentResult.putExtra(Session.IDENTIFIER, session);
 //				startActivity(myIntentResult);
 				return true;
 			case R.id.gps:
 				mapView.getController().setCenter(nodeOverlay.getGpsPosition());
 				return true;
 
 			case R.id.back:
 				finish();
 				return true;
 //		case R.id.gotofirst:
 //		if (nodeOverlay.getNodeModel().size() > 0){
 //			mapView.getController().setCenter(nodeOverlay.getNodeModel().get(0).getGeoPoint());
 //			}
 //		return true;
 
 			default:
 				return super.onOptionsItemSelected(item);
 		}
 
 
 	}
 
 	//---------------Key-Events--------------------
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if (keyCode == KeyEvent.KEYCODE_BACK) {
 			int NodeModelsize = nodeOverlay.getNodeModel().size();
 
 			if (NodeModelsize > 0) {
 				// create a tempNodeModel to force a redraw of the NodeOverlay
 				nodeOverlay.getNodeModel().remove(NodeModelsize - 1);
 				onInvalidate();
 				return true;
 			}
 		}
 		return super.onKeyDown(keyCode, event);
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 
 		switch (requestCode) {
 			case REQUEST_CODE_MAP_SCREEN:
 
 				session.setNodeModel((NodeModel) data.getExtras().getSerializable(
 						NodeModel.IDENTIFIER));
 				nodeOverlay.setNodeModel(session.getNodeModel());
 				onInvalidate();
 				break;
 			case NodeOverlay.REQUEST_CODE_ITEM_OVERLAY:
 				switch (resultCode) {
 					case RESULT_OK:
 						session.getNodeModel().getNodeVector().set(data.getExtras().getInt("index"), (Node) data.getSerializableExtra("node"));
 						onInvalidate();
 						break;
 					case EditNodeScreen.RESULT_DELETE:
 						session.getNodeModel().getNodeVector().remove(data.getExtras().getInt("index"));
 						onInvalidate();
 				}
 		}
 	}
 
 	private void onInvalidate() {
 		nodeOverlay.onModelChanged();
 		wayOverlay.clear();
 	}
 
 	public void addPathToMap(GeoPoint[][] points) {
 		wayOverlay.clear();
 		wayOverlay.addWay(new OverlayWay(points));
 	}
 
 	public void triggerNNlookup(Node node) {
 		requestList.add((RequestNN) new RequestNN(nnsListener, session, node).execute());
 	}
 
 	private void initializeHandler() {
 		handler = (RequestHandler) getLastNonConfigurationInstance();
 
 		if (handler != null) {
 			handler.setListener(requestListener);
 			setProgressBarIndeterminateVisibility(true);
 		} else {
 			setProgressBarIndeterminateVisibility(false);
 		}
 	}
 
 	@Override
 	public Object onRetainNonConfigurationInstance() {
 		return handler;
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 
 		if (handler != null)
 			handler.setListener(null);
 
 		for (RequestNN request : requestList) {
 			request.setListener(null);
 		}
 	}
 }

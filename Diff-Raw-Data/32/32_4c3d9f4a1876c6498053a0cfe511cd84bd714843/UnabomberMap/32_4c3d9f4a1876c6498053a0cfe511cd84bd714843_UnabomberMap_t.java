 package ui;
 
 import java.util.ArrayList;
 
 import ui.dialogs.Dialogs;
 import unabomber.client.R;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.drawable.Drawable;
 import android.net.Uri;
 import android.os.Bundle;
 import android.telephony.TelephonyManager;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapView;
 
 import connectivity.GCSTester;
 import engine.GameEngine;
 import engine.PlayerData;
 import engine.PlayerMessage;
 
 public class UnabomberMap extends MapActivity {
 	private MapView mapView;
 	private PlayerLocationOverlay playerLocationOverlay;
 	private GameEngine gameEngine;
 	private Intent worldUpdateIntent;
 	private OtherPlayersOverlay otherPlayersOverlay;
 	private PlayerData playerData;
 	private BombsOverlay bombsOverlay;
 
 
 	//server check var
 	private Boolean reachable=false;
 	private GCSTester tester;
 	private ArrayList<PlayerMessage> messages = new ArrayList<PlayerMessage>();
 
 
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		Dialogs.setActivity(this);
 
 
 		tester=new GCSTester(this);
 
 		//showVideo();
 		//showVideo in not working at the moment: we need a mp4 video, swf are not supported
 		
 		//reachable=tester.testGCS();  //era commentata
 		showDemoAlert(this);
 
 		if(!reachable){ 
 
 			setUpMap();
 			showPlayerLocation();
 			authenticatePlayer();
 			followPlayers();
			bombsOverlay = new BombsOverlay(getResources().getDrawable(R.drawable.bomb), this.getPlayerData().getPlayerId(), this.getEngine(), this);
 		}
 
 
 
 
 	}
 	
 	public void showVideo(){
 		Intent i = new Intent(UnabomberMap.this, VideoIntro.class);
 		UnabomberMap.this.startActivity(i);
 	}
 	
 	//start mixare code
 	public void startMixare(Context c){
 
 		Intent i = new Intent();
 		i.setAction(Intent.ACTION_VIEW);
 		i.setDataAndType(Uri.parse("http://your_server/JSONEndpoint"), "application/mixare-json");
 		startActivity(i);    
 	}
 
 
 
 	//demo start code
 	public void showDemoAlert(final UnabomberMap app){
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);  
 		builder.setMessage("Welcome to Unabomber Demo! Press Play Demo to start a new game or Exit to close the application. Enjoy!")  
 		.setCancelable(false)  
 		.setPositiveButton("Play Demo",  
 				new DialogInterface.OnClickListener(){  
 			public void onClick(DialogInterface dialog, int id){ 
 
 				dialog.cancel();
 			}  
 		});  
 		builder.setNegativeButton("Exit",  
 				new DialogInterface.OnClickListener(){  
 			public void onClick(DialogInterface dialog, int id){  
 
 				app.finish();
 
 			}  
 		});  
 		AlertDialog alert = builder.create();  
 		alert.show();  
 	}
 
 
 	//menu
 	public boolean onCreateOptionsMenu(Menu menu){
 		MenuInflater inflater=getMenuInflater();
 		inflater.inflate(R.menu.game_menu, menu);
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// handle item selection
 		switch (item.getItemId()) {
 		case R.id.show:
 			//codice
 
 			Intent i = new Intent();
 			i.setAction(Intent.ACTION_VIEW);
 			i.setDataAndType(Uri.parse("http://your_server/JSONEndpoint"), "application/mixare-json");
 			startActivity(i);
 
 			return true;
 		case R.id.messages:
 			//messages
 
 			Intent message_intent = new Intent(UnabomberMap.this, MessagesView.class );
 			message_intent.putExtra("messages", this.messages);
 			UnabomberMap.this.startActivity(message_intent);
 
 
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 
 
 
 	@Override
 	protected Dialog onCreateDialog(int id) {
 		Dialog dialog = Dialogs.get(id);
 		return dialog;
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		if(reachable!=false)
 			stopFollowingPlayers();
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		if(reachable!=false)
 			stopFollowingPlayers();
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		if(reachable!=false)
 			followPlayers();
 	}
 
 	private void stopFollowingPlayers() {
 		stopService(worldUpdateIntent);
 	}
 
 	private void followPlayers() {
 		Drawable defaultMarker = getResources().getDrawable(R.drawable.androidmarker);
 		int playerId = playerData.getPlayerId();
 		otherPlayersOverlay = new OtherPlayersOverlay(defaultMarker, playerId, this.gameEngine, this, this);
 
 		WorldUpdateService.setActivity(this);
 		worldUpdateIntent = new Intent(this, WorldUpdateService.class);
 		startService(worldUpdateIntent);
 	}
 
 	private void authenticatePlayer() {
 		playerData = gameEngine.authenticate();
 	}
 
 	private void showPlayerLocation() {
 		playerLocationOverlay = new PlayerLocationOverlay(this);
 		playerLocationOverlay.enableMyLocation();
 		mapView.getOverlays().add(playerLocationOverlay);
 	}
 
 	private void setUpMap() {
 		mapView = (MapView)findViewById(R.id.mapview);
 		mapView.setBuiltInZoomControls(true);
 		gameEngine = new GameEngine(deviceId());
 	}
 
 	private String deviceId() {
 		TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
 		String deviceId = telephonyManager.getDeviceId() + Math.round(Math.random() * 1000);
 		return deviceId;
 	}
 
 	public GameEngine getEngine() {
 		return gameEngine;
 	}
 
 	@Override
 	protected boolean isRouteDisplayed() {
 		return false;
 	}
 
 	public MapView getMapView() {
 		return mapView;
 	}
 
 	public OtherPlayersOverlay getOtherPlayersOverlay() {
 		return otherPlayersOverlay;
 	}
 
 	public BombsOverlay getBombsOverlay() {
 		return bombsOverlay;
 	}
 
 	public PlayerData getPlayerData() {
 		return playerData;
 	}
 
 	public void setMessages(ArrayList<PlayerMessage> messages) {
 		this.messages = messages;
 	}
 }

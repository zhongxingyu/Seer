 package edu.vub.at.nfcpoker.ui;
 
 import java.util.HashMap;
 import java.util.Random;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.IntentFilter.MalformedMimeTypeException;
 import android.net.wifi.WifiManager;
 import android.nfc.NfcAdapter;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.LinearLayout.LayoutParams;
 import android.widget.TextView;
 import edu.vub.at.commlib.CommLib;
 import edu.vub.at.nfcpoker.Card;
 import edu.vub.at.nfcpoker.ConcretePokerServer;
 import edu.vub.at.nfcpoker.ConcretePokerServer.GameState;
 import edu.vub.at.nfcpoker.Constants;
 import edu.vub.at.nfcpoker.QRNFCFunctions;
 import edu.vub.at.nfcpoker.R;
 
 public class ServerActivity extends Activity implements ServerViewInterface {
 
 	public interface ServerStarter {
 		public void start(String ipAddress, String broadcastAddress);
 		public void setWifiDirect(String groupName, String password, String ipAddress, int port);
 	}
 	
 	// Connectivity
 	protected String currentWifiGroupName;
 	protected String currentWifiPassword; 
 	protected String currentIpAddress;
 	protected int currentPort;
 	private boolean isWifiDirect;
 	
 	// UI
 	private final int MIN_AVATAR_ID = 1;
 	private final int MAX_AVATAR_ID = 15;
 	private final int MAX_NUMBER_AVATARS_SIDE = 4;
 	private Random random = new Random();
 	
 	// NFC
 	private PendingIntent pendingIntent;
 	private IntentFilter[] intentFiltersArray;
 	private NfcAdapter nfcAdapter;
 
 	// UI
 	int nextToReveal = 0;
 	@SuppressLint("UseSparseArrays")
 	HashMap<Integer, View> playerBadges = new HashMap<Integer, View>();
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
     	super.onCreate(savedInstanceState);
     	boolean isTV = getPackageManager().hasSystemFeature("com.google.android.tv");
     	if (isTV) {
     		requestWindowFeature(Window.FEATURE_NO_TITLE);
     		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
     	}
     	
     	setContentView(R.layout.activity_server);
     	View tablet_layout = findViewById(R.id.tablet_layout);
     	View server_layout = findViewById(R.id.server_layout);
     	final boolean isDedicated = tablet_layout != null || server_layout != null || isTV;
     	isWifiDirect = getIntent().getBooleanExtra(Constants.INTENT_WIFI_DIRECT, false);
     	
 		final Activity act = this;
     	nfcAdapter = NfcAdapter.getDefaultAdapter(this);
     	
     	if (nfcAdapter != null) {
     		pendingIntent = PendingIntent.getActivity(
     		    this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
     	
     		IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
     		IntentFilter all = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
     		try {
     			ndef.addDataType("*/*");    /* Handles all MIME based dispatches.
                                            You should specify only the ones that you need. */
     		}
     		catch (MalformedMimeTypeException e) {
     			throw new RuntimeException("fail", e);
     		}
     		intentFiltersArray = new IntentFilter[] { ndef, all };
     	}
     	
     	ImageButton wifi_btn = (ImageButton) findViewById(R.id.wifi_btn);
     	if (isTV) {
     		wifi_btn.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					QRNFCFunctions.showWifiConnectionDialog(ServerActivity.this, currentWifiGroupName, currentWifiPassword, currentIpAddress, currentPort, true);
 				}
 			});
     	} else {
     		wifi_btn.setVisibility(View.GONE);
     	}
     	
 		ServerStarter startServer = new ServerStarter() {
 			@Override
 			public void start(String ipAddress, String broadcastAddress) {
<<<<<<< HEAD
 				ConcretePokerServer cps = new ConcretePokerServer(ServerActivity.this, isDedicated, ipAddress, broadcastAddress);
 				currentIpAddress = ipAddress;
				currentPort = CommLib.SERVER_PORT;
 				cps.start();				
=======
				GameServer cps = new GameServer(ServerActivity.this, isDedicated, ipAddress, broadcastAddress);
				cps.start();
>>>>>>> ba59971... Correctly fixes aa70dd5fb4ff749318472a237489e30b65e49ace: setting up ports for the normal wifi case
 			}
 
 			@Override
 			public void setWifiDirect(final String groupName, final String password, final String ipAddress, final int port) {
 				currentWifiGroupName = groupName;
 				currentWifiPassword  = password;
 				currentIpAddress = ipAddress;
 				currentPort = port;
 				runOnUiThread(new Runnable() {
 					@Override
 					public void run() {
 						QRNFCFunctions.showWifiConnectionDialog(act, groupName, password, ipAddress, port, true);
 					}
 				});
 			}
 		};
     	
 		if (isWifiDirect) {
     		new WifiDirectManager.Creator(this, startServer).run();
     	} else {
     		WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
     		String ipAddress = CommLib.getIpAddress(this);
     		String broadcastAddress = CommLib.getBroadcastAddress(this);
     		currentWifiGroupName = wm.getConnectionInfo().getSSID();
			currentWifiPassword = CommLib.getWifiPassword(currentWifiGroupName);
			currentIpAddress = ipAddress;
			currentPort = CommLib.SERVER_PORT;
     		startServer.start(ipAddress, broadcastAddress);
     	}
 		
     }
     
     @Override
     public void onResume() {
         super.onResume();
         if (nfcAdapter != null) {
         	nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, null);
         }
     }
 
     @Override
     public void onNewIntent(Intent intent) {
         QRNFCFunctions.lastSeenNFCTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
     }
     
     @Override
     public void onPause() {
         super.onPause();
         if (nfcAdapter != null) {
         	nfcAdapter.disableForegroundDispatch(this);
         }
     }
     
 
     @Override
 	public boolean onOptionsItemSelected(MenuItem item) {
     	if (item.getItemId() == R.id.show_wifi_settings) {
     		QRNFCFunctions.showWifiConnectionDialog(this, currentWifiGroupName, currentWifiPassword, currentIpAddress, currentPort, true);
     		return true;
     	}
 		return super.onOptionsItemSelected(item);
 	}
 
 	@Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_server, menu);
         return true;
     }
 
 	public void revealCards(final Card[] cards) {
 		runOnUiThread(new Runnable() {
 			public void run() {
 				for (Card c : cards) {
 					Log.d("wePoker - Server", "Revealing card " + c);
 					LinearLayout ll = (LinearLayout) findViewById(R.id.cards);
 					ImageButton ib = (ImageButton) ll.getChildAt(nextToReveal++);
 					CardAnimation.setCardImage(ib, cardToResourceID(c));
 				}
 			}
 
 			public int cardToResourceID(Card c) {
 				return getResources().getIdentifier("edu.vub.at.nfcpoker:drawable/" + c.toString(), null, null);
 			}
 		});
 	}
 	
 
 	public void resetCards() {
 		Log.d("wePoker - Server", "Hiding cards again");
 		nextToReveal = 0;
 		runOnUiThread(new Runnable() {
 			public void run() {
 				LinearLayout ll = (LinearLayout) findViewById(R.id.cards);
 				for (int i = 0; i < 5; i++) {
 					final ImageButton ib = (ImageButton) ll.getChildAt(i);
 					CardAnimation.setCardImage(ib, R.drawable.backside);
 				}
 			}
 		});
 	}
 
 	public void showStateChange(final GameState newState) {
 		runOnUiThread(new Runnable() {
 			public void run() {
 				String prefix = getResources().getString(R.string.title_activity_server);
 				setTitle(prefix + " \u2014 " + newState.toString());
 				
 				TextView tv = (TextView) findViewById(R.id.current_phase);
 				tv.setText(newState.toString());
 			}
 		});
 	}
 
 	public String getNewAvatar(){
 		int rndNumberInRange = random.nextInt(MAX_AVATAR_ID - MIN_AVATAR_ID) + MIN_AVATAR_ID;
 		Log.d("wePoker - Server", "Avatar for player " + rndNumberInRange);
 		return "avatar_" + rndNumberInRange;
 	}
 	@Override
 	public void addPlayer(final int clientID, final String clientName, final int initialMoney) {
 		runOnUiThread(new Runnable() {
 			public void run() {
 				Log.d("wePoker - Server", "Adding player name " + clientName);
 				LinearLayout users;
 				 // todo make this variable.. now hardcoded 4 and 4 on each side.
 				if (playerBadges.size() < MAX_NUMBER_AVATARS_SIDE) {
 					users = (LinearLayout) findViewById(R.id.users_bottom);
 				} else{
 					users = (LinearLayout) findViewById(R.id.users_top);
 				}
 				View badge = getLayoutInflater().inflate(R.layout.user, users, false);
 				
 				ImageView avatar = (ImageView) badge.findViewById(R.id.avatar_user);
 				String avatarField = "edu.vub.at.nfcpoker:drawable/" + getNewAvatar();
 				Log.d("wePoker - Server", "Avatar for player " + avatarField);			
 				int id = getResources().getIdentifier(avatarField, null, null);
 				avatar.setImageDrawable(getResources().getDrawable(id));
 				avatar.setAdjustViewBounds(true);
 				avatar.setMaxHeight(users.getHeight());
 				avatar.setMaxWidth(users.getHeight());
 				
 				TextView name = (TextView) badge.findViewById(R.id.playerName);
 				name.setText(clientName);
 				TextView money = (TextView) badge.findViewById(R.id.playerMoney);
 				money.setText("\u20AC" + initialMoney);
 
 				playerBadges.put(clientID, badge);
 				
 				LinearLayout.LayoutParams params = (LayoutParams) badge.getLayoutParams();
 				params.setMargins(24, 0, 24, 0);
 				users.addView(badge, params);
 			}
 		});
 	}
 
 	@Override
 	public void setPlayerMoney(final Integer player, final int current) {
 		runOnUiThread(new Runnable() {
 			public void run() {
 				View badge = playerBadges.get(player);
 				if (badge != null) {
 					TextView money = (TextView) badge.findViewById(R.id.playerMoney);
 					money.setText("\u20AC" + current);
 				}
 			}
 		});
 	}
 
 	@Override
 	public void updatePoolMoney(int chipsPool) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void removePlayer(final Integer player) {
 		runOnUiThread(new Runnable() {
 			public void run() {
 				View badge = playerBadges.get(player);
 				if (badge != null) {
 					LinearLayout users_bottom = (LinearLayout) findViewById(R.id.users_bottom);
 					users_bottom.removeView(badge);
 					LinearLayout users_top = (LinearLayout) findViewById(R.id.users_top);
 					users_top.removeView(badge);
 					playerBadges.remove(player);
 				}
 			}
 		});
 	}
 
 	@Override
 	public Context getContext() {
 		return this;
 	}
 }

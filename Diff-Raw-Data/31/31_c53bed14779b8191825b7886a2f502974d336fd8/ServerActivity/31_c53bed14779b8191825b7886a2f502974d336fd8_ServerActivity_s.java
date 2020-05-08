 package edu.vub.at.nfcpoker.ui;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.IntentFilter.MalformedMimeTypeException;
 import android.graphics.Bitmap;
 import android.net.wifi.WifiManager;
 import android.nfc.NdefRecord;
 import android.nfc.NfcAdapter;
 import android.nfc.Tag;
 import android.nfc.tech.Ndef;
 import android.nfc.tech.NdefFormatable;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.webkit.MimeTypeMap;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 import com.google.zxing.WriterException;
 
 import edu.vub.at.commlib.CommLib;
 import edu.vub.at.nfcpoker.Card;
 import edu.vub.at.nfcpoker.ConcretePokerServer;
 import edu.vub.at.nfcpoker.ConcretePokerServer.GameState;
 import edu.vub.at.nfcpoker.QRFunctions;
 import edu.vub.at.nfcpoker.R;
 
 public class ServerActivity extends Activity implements ServerViewInterface {
 
 	public interface ServerStarter {
 		public void start(String ipAddress, String broadcastAddress);
 
 		public void setWifiDirect(String groupName, String password, String ipAddress);
 	}
 
 	@SuppressLint("UseSparseArrays")
 	HashMap<Integer, View> playerBadges = new HashMap<Integer, View>();
 	protected String currentWifiGroupName;
 	protected String currentWifiPassword; 
 	protected String currentIpAddress;
 
 	private boolean isWifiDirect;
 	
 	private PendingIntent pendingIntent;
 	private IntentFilter[] intentFiltersArray;
 	private NfcAdapter nfcAdapter;
 
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
     	super.onCreate(savedInstanceState);
     	setContentView(R.layout.activity_server);
     	View tablet_layout = findViewById(R.id.tablet_layout);
     	boolean isTV = getPackageManager().hasSystemFeature("com.google.android.tv");
     	final boolean isDedicated = tablet_layout != null || isTV;
    	isWifiDirect = getIntent().getBooleanExtra("wifiDirect", false);
     	
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
     	
 		ServerStarter startServer = new ServerStarter() {
 			@Override
 			public void start(String ipAddress, String broadcastAddress) {
 				ConcretePokerServer cps = new ConcretePokerServer(ServerActivity.this, isDedicated, ipAddress, broadcastAddress);
 				currentIpAddress = ipAddress; 
 				cps.start();				
 			}
 
 			@Override
 			public void setWifiDirect(final String groupName, final String password, final String ipAddress) {
 				// TODO setup NFC tag.
 				currentWifiGroupName = groupName;
 				currentWifiPassword  = password;
 				currentIpAddress = ipAddress;
 				runOnUiThread(new Runnable() {
 					@Override
 					public void run() {
 						QRFunctions.showWifiConnectionDialog(act, groupName, password, ipAddress, true);
 					}
 				});
 			}
 		};
     	
 		if (isWifiDirect) {
     		new WifiDirectManager.Creator(this, startServer).run();
     	} else {
     		WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
     		currentWifiGroupName = wm.getConnectionInfo().getSSID();
     		currentWifiPassword = "********";
     		
     		String ipAddress = CommLib.getIpAddress(this);
     		String broadcastAddress = CommLib.getBroadcastAddress(this);
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
         QRFunctions.lastSeenNFCTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
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
     		QRFunctions.showWifiConnectionDialog(this, currentWifiGroupName, currentWifiPassword, currentIpAddress, true);
     		return true;
     	}
 		return super.onOptionsItemSelected(item);
 	}
 
 	@Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_server, menu);
         return true;
     }
 
     private Dialog wifiConnectionDialog;
     
 	int nextToReveal = 0;
 
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
 				TextView phase = (TextView)findViewById(R.id.current_phase);
 				phase.setText(newState.toString());
 			}
 		});
 	}
 
 	@Override
 	public void addPlayer(final int clientID, final String clientName, final int initialMoney) {
 		runOnUiThread(new Runnable() {
 			public void run() {
 				Log.d("wePoker - Server", "Adding player name " + clientName);
 				LinearLayout users = (LinearLayout) findViewById(R.id.users);
 				View badge = getLayoutInflater().inflate(R.layout.user, null);
 				
 				TextView name = (TextView) badge.findViewById(R.id.playerName);
 				name.setText(clientName);
 				TextView money = (TextView) badge.findViewById(R.id.playerMoney);
 				money.setText("\u20AC" + initialMoney);
 
 				playerBadges.put(clientID, badge);
 				users.addView(badge);
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
 					LinearLayout users = (LinearLayout) findViewById(R.id.users);
 					users.removeView(badge);
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

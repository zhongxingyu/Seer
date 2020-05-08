 package edu.vub.at.nfcpoker.ui;
 
 import java.util.HashMap;
 
 import com.google.zxing.BarcodeFormat;
 import com.google.zxing.WriterException;
 import com.google.zxing.common.BitMatrix;
 import com.google.zxing.qrcode.QRCodeWriter;
 
 import edu.vub.at.commlib.CommLib;
 import edu.vub.at.nfcpoker.Card;
 import edu.vub.at.nfcpoker.ConcretePokerServer;
 import edu.vub.at.nfcpoker.ConcretePokerServer.GameState;
 import edu.vub.at.nfcpoker.Constants;
 import edu.vub.at.nfcpoker.R;
 import android.net.Uri;
 import android.net.wifi.WifiManager;
 import android.os.Bundle;
 import android.animation.Animator;
 import android.animation.AnimatorListenerAdapter;
 import android.animation.AnimatorSet;
 import android.animation.ObjectAnimator;
 import android.annotation.SuppressLint;
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 @TargetApi(11)
 public class ServerActivity extends Activity implements ServerViewInterface {
 
 	public interface ServerStarter {
 		public void start(String ipAddress, String broadcastAddress);
 
		public void setWifiDirect(String groupName, String password);
 	}
 
 	@SuppressLint("UseSparseArrays")
 	HashMap<Integer, View> playerBadges = new HashMap<Integer, View>();
 	protected String currentWifiGroupName;
 	protected String currentWifiPassword; 
 	protected String currentIpAddress;
 
 	private boolean isWifiDirect;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
     	super.onCreate(savedInstanceState);
     	setContentView(R.layout.activity_server);
     	View tablet_layout = findViewById(R.id.tablet_layout);
     	final boolean isDedicated = tablet_layout != null;
     	isWifiDirect = getIntent().getBooleanExtra("wifiDirect", false);
     	
 		ServerStarter startServer = new ServerStarter() {
 			
 			@Override
 			public void start(String ipAddress, String broadcastAddress) {
 				ConcretePokerServer cps = new ConcretePokerServer(ServerActivity.this, isDedicated, ipAddress, broadcastAddress);
 				currentIpAddress = ipAddress; 
 				cps.start();				
 			}
 
 			@Override
			public void setWifiDirect(String groupName, String password) {
 				// TODO setup NFC tag.
 				currentWifiGroupName = groupName;
 				currentWifiPassword  = password;
 				runOnUiThread(new Runnable() {
 					@Override
 					public void run() {
 						showWifiConnectionDialog();
 					}
 				});
 			}
 		};
     	
 		if (isWifiDirect) {
     		WifiDirectManager wdm = WifiDirectManager.create(this, getMainLooper(), true);
     		wdm.createGroup(startServer);
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
 	public boolean onOptionsItemSelected(MenuItem item) {
     	if (item.getItemId() == R.id.show_wifi_settings) {
     		showWifiConnectionDialog();
     	}
 		return super.onOptionsItemSelected(item);
 	}
 
 	@Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_server, menu);
         return true;
     }
     
     // Taken from the ZXing source code.
     private static final int WHITE = 0xFFFFFFFF;
     private static final int BLACK = 0xFF000000;
     
     private Bitmap encodeBitmap(String contents) throws WriterException {
     	int width = 200;
     	int height = 200;
 		BitMatrix result = new QRCodeWriter().encode(contents, BarcodeFormat.QR_CODE, width, height);
         int[] pixels = new int[width * height];
         for (int y = 0; y < height; y++) {
           int offset = y * width;
           for (int x = 0; x < width; x++) {
             pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
           }
         }
 
         Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
         bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
         return bitmap;
     }
 
     private Dialog wifiConnectionDialog;
     
 	private void showWifiConnectionDialog() {
 		if (wifiConnectionDialog == null) {
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			View dialogGuts = getLayoutInflater().inflate(R.layout.wifi_connection_dialog, null);
 			
 			TextView networkNameTV = (TextView) dialogGuts.findViewById(R.id.network_name);
 			networkNameTV.setText(this.currentWifiGroupName);
 			TextView passwordTV = (TextView) dialogGuts.findViewById(R.id.password);
 			passwordTV.setText(currentWifiPassword);
 			Button dismissButton = (Button) dialogGuts.findViewById(R.id.dismiss_btn);
 			dismissButton.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					wifiConnectionDialog.dismiss();	
 				}
 			});
 			
 			try {
 				String connectionString = createJoinUri();
 				Bitmap qrCode = encodeBitmap(connectionString);
 				ImageView qrCodeIV = (ImageView) dialogGuts.findViewById(R.id.qr_code);
 				qrCodeIV.setImageBitmap(qrCode);
 			} catch (WriterException e) {
 				Log.e("wePoker - Server", "Could not create QR code", e);
 			}
 			
 			
 			wifiConnectionDialog = 
 				builder.setTitle("Connection details")
 				       .setCancelable(true)
 				       .setView(dialogGuts)
 				       .create();
 		}
 		
 		wifiConnectionDialog.show();
 	}
 	
     private String createJoinUri() {
     	Uri uri = Uri.parse(Constants.INTENT_BASE_URL)
     			     .buildUpon()
     			     .appendQueryParameter(Constants.INTENT_WIFI_NAME, currentWifiGroupName)
     			     .appendQueryParameter(Constants.INTENT_WIFI_PASSWORD, currentWifiPassword)
     			     .appendQueryParameter(Constants.INTENT_WIFI_IS_DIRECT, "" + isWifiDirect)
     			     .appendQueryParameter(Constants.INTENT_SERVER_IP, currentIpAddress)
     			     .build();
     	
     	return uri.toString();
 
 	}
 
 	int nextToReveal = 0;
 
 	public void revealCards(final Card[] cards) {
 		runOnUiThread(new Runnable() {
 			public void run() {
 				for (Card c : cards) {
 					Log.d("wePoker - Server", "Revealing card " + c);
 					LinearLayout ll = (LinearLayout) findViewById(R.id.cards);
 					ImageButton ib = (ImageButton) ll.getChildAt(nextToReveal++);
 					ib.setImageResource(cardToResourceID(c));
 					ObjectAnimator anim = ObjectAnimator.ofFloat(ib, "alpha", 0.f, 1.f);
 					anim.setDuration(1000);
 					anim.start();
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
 					ObjectAnimator animX = ObjectAnimator.ofFloat(ib, "scaleX", 1.f, 0.f);
 					ObjectAnimator animY = ObjectAnimator.ofFloat(ib, "scaleY", 1.f, 0.f);
 					animX.setDuration(500); animY.setDuration(500);
 					final AnimatorSet scalers = new AnimatorSet();
 					scalers.play(animX).with(animY);
 					scalers.addListener(new AnimatorListenerAdapter() {
 
 						@Override
 						public void onAnimationEnd(Animator animation) {
 							ib.setScaleX(1.f);
 							ib.setScaleY(1.f);
 							ib.setImageResource(R.drawable.backside);
 						}
 
 					});
 					scalers.start();
 				}
 			}
 		});
 	}
 
 	public void showStatechange(final GameState newState) {
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

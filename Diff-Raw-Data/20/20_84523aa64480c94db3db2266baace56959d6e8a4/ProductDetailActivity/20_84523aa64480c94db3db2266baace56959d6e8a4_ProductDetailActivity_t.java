 package com.androidtitlan.endeavorsubasta;
 
 import java.text.NumberFormat;
 import java.util.StringTokenizer;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.graphics.BitmapFactory;
 import android.graphics.drawable.BitmapDrawable;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.os.Messenger;
 import android.os.RemoteException;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.SeekBar;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 import android.widget.TextView;
 
 import com.androidtitlan.endeavorsubasta.io.UpdateService;
 import com.androidtitlan.endeavorsubasta.ui.BiddingDialog;
 import com.androidtitlan.endeavorsubasta.ui.Dialog;
 import com.androidtitlan.endeavorsubasta.ui.HomeActivity;
 
 public class ProductDetailActivity extends Activity implements
 		android.widget.SeekBar.OnSeekBarChangeListener {
 
 	TextView offerer = null;
 	TextView latestPrice = null;
 	TextView startingPrice = null;
 	SeekBar seekBar = null;
 	TextView myOffer;
 
 	long actualPrice, userBid;
 	String bidderName;
 	SharedPreferences preferences;
	final String PREFS_NAME = "MySharedPrefs";
 
 	private static final int MAXIMUM_BID_AMOUNT = 100;
 	private int productId;
 	Messenger mService = null;
 	boolean mIsBound;
 	final Messenger mMessenger = new Messenger(new IncomingHandler());
 	Timer tmr = new Timer();
 
 	String fromService;
 
 	class IncomingHandler extends Handler {
 
 		@Override
 		public void handleMessage(Message msg) {
 			switch (msg.what) {
 			case UpdateService.MSG_SET_STRING_VALUE:
 				fromService = msg.getData().getString("Bid");
 				StringTokenizer tk = new StringTokenizer(fromService, "%$%");
 				String sActualPrice = tk.nextToken();
 				bidderName = tk.nextToken();
 				long tempActualPrice = (long) Float.parseFloat(sActualPrice);
 				if (tempActualPrice != 0) {
 					actualPrice = tempActualPrice;
 					Log.e("ActualPrice", "" + actualPrice);
 					// TODO Here's the String which prints to UI
 					SharedPreferences settings = getSharedPreferences(
 							PREFS_NAME, 0);
 					SharedPreferences.Editor editor = settings.edit();
 					editor.putLong("price", actualPrice);
 					editor.putString("offerer", bidderName);
 					editor.commit();
 
					String lastOfferer = settings.getString("offerer",
 							null);
					long lastPrice = settings.getLong("price", 0);
 					latestPrice.setText("$ " + lastPrice + " USD");
 					offerer.setText("" + lastOfferer);
 				}
 				break;
 			case UpdateService.MSG_SET_BOOL_VALUE:
 				// setContentView(R.layout.end_layout);
 			default:
 				super.handleMessage(msg);
 			}
 		}
 	}
 
 	private ServiceConnection mConnection = new ServiceConnection() {
 		public void onServiceConnected(ComponentName className, IBinder service) {
 			mService = new Messenger(service);
 			try {
 				Message msg = Message.obtain(null,
 						UpdateService.MSG_REGISTER_CLIENT);
 				msg.replyTo = mMessenger;
 				mService.send(msg);
 			} catch (RemoteException e) {
 			}
 		}
 
 		public void onServiceDisconnected(ComponentName className) {
 			mService = null;
 		}
 	};
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setActionBarCustomBackground();
 		doBindService();
 		tmr.schedule(new TimerTask() {
 			public void run() {
 				sendInts();
 			}
 		}, 500);
 		/**
 		 * Inflado especifico del layout para cada producto
 		 */
 		Bundle extras = getIntent().getExtras();
 		productId = extras.getInt("Product");
 		startUI(1, productId);
 	}
 
 	private void startUI(int x, int selectedProduct) {
 		switch (selectedProduct) {
 		case 1:
 			setContentView(R.layout.product_one);
 			setTitle(R.string.product_title1);
 			seekBar = (SeekBar) findViewById(R.id.seekbar);
 			offerer = (TextView) findViewById(R.id.bidder);
 			startingPrice = (TextView) findViewById(R.id.starting_price);
 			// TODO put here text from SharedPreferences
 			latestPrice = (TextView) findViewById(R.id.last_price);
 			myOffer = (TextView) findViewById(R.id.myoffer);
 			productId = 1;
 			break;
 		case 2:
 			setContentView(R.layout.product_two);
 			setTitle(R.string.product_title2);
 			seekBar = (SeekBar) findViewById(R.id.seekbar);
 			startingPrice = (TextView) findViewById(R.id.starting_price);
 			offerer = (TextView) findViewById(R.id.bidder);
 			latestPrice = (TextView) findViewById(R.id.last_price);
 			myOffer = (TextView) findViewById(R.id.myoffer);
 			productId = 2;
 			break;
 		case 3:
 			setContentView(R.layout.product_three);
 			setTitle(R.string.product_title3);
 			seekBar = (SeekBar) findViewById(R.id.seekbar);
 			startingPrice = (TextView) findViewById(R.id.starting_price);
 			offerer = (TextView) findViewById(R.id.bidder);
 			latestPrice = (TextView) findViewById(R.id.last_price);
 			myOffer = (TextView) findViewById(R.id.myoffer);
 			productId = 3;
 			break;
 		case 4:
 			setContentView(R.layout.product_four);
 			setTitle(R.string.product_title4);
 			seekBar = (SeekBar) findViewById(R.id.seekbar);
 			startingPrice = (TextView) findViewById(R.id.starting_price);
 			offerer = (TextView) findViewById(R.id.bidder);
 			latestPrice = (TextView) findViewById(R.id.last_price);
 			myOffer = (TextView) findViewById(R.id.myoffer);
 			productId = 4;
 			break;
 		case 5:
 			setContentView(R.layout.product_five);
 			setTitle(R.string.product_title5);
 			seekBar = (SeekBar) findViewById(R.id.seekbar);
 			startingPrice = (TextView) findViewById(R.id.starting_price);
 			offerer = (TextView) findViewById(R.id.bidder);
 			latestPrice = (TextView) findViewById(R.id.last_price);
 			myOffer = (TextView) findViewById(R.id.myoffer);
 			productId = 5;
 			break;
 		case 6:
 			setContentView(R.layout.product_six);
 			setTitle(R.string.product_title6);
 			seekBar = (SeekBar) findViewById(R.id.seekbar);
 			startingPrice = (TextView) findViewById(R.id.starting_price);
 			offerer = (TextView) findViewById(R.id.bidder);
 			latestPrice = (TextView) findViewById(R.id.last_price);
 			myOffer = (TextView) findViewById(R.id.myoffer);
 			productId = 6;
 			break;
 		case 7:
 			setContentView(R.layout.product_seven);
 			setTitle(R.string.product_title7);
 			seekBar = (SeekBar) findViewById(R.id.seekbar);
 			startingPrice = (TextView) findViewById(R.id.starting_price);
 			offerer = (TextView) findViewById(R.id.bidder);
 			latestPrice = (TextView) findViewById(R.id.last_price);
 			myOffer = (TextView) findViewById(R.id.myoffer);
 			productId = 7;
 			break;
 		case 8:
 			setContentView(R.layout.product_eight);
 			setTitle(R.string.product_title8);
 			seekBar = (SeekBar) findViewById(R.id.seekbar);
 			startingPrice = (TextView) findViewById(R.id.starting_price);
 			offerer = (TextView) findViewById(R.id.bidder);
 			latestPrice = (TextView) findViewById(R.id.last_price);
 			myOffer = (TextView) findViewById(R.id.myoffer);
 			productId = 8;
 			break;
 		case 9:
 			setContentView(R.layout.product_nine);
 			setTitle(R.string.product_title9);
 			seekBar = (SeekBar) findViewById(R.id.seekbar);
 			startingPrice = (TextView) findViewById(R.id.starting_price);
 			offerer = (TextView) findViewById(R.id.bidder);
 			latestPrice = (TextView) findViewById(R.id.last_price);
 			myOffer = (TextView) findViewById(R.id.myoffer);
 			productId = 9;
 			break;
 		case 10:
 			setContentView(R.layout.product_ten);
 			setTitle(R.string.product_title10);
 			seekBar = (SeekBar) findViewById(R.id.seekbar);
 			startingPrice = (TextView) findViewById(R.id.starting_price);
 			offerer = (TextView) findViewById(R.id.bidder);
 			latestPrice = (TextView) findViewById(R.id.last_price);
 			myOffer = (TextView) findViewById(R.id.myoffer);
 			productId = 10;
 			break;
 
 		case 11:
 			setContentView(R.layout.product_eleven);
 			setTitle(R.string.product_title11);
 			seekBar = (SeekBar) findViewById(R.id.seekbar);
 			startingPrice = (TextView) findViewById(R.id.starting_price);
 			offerer = (TextView) findViewById(R.id.bidder);
 			latestPrice = (TextView) findViewById(R.id.last_price);
 			myOffer = (TextView) findViewById(R.id.myoffer);
 			productId = 11;
 			break;
 
 		case 12:
 			setContentView(R.layout.product_twelve);
 			setTitle(R.string.product_title12);
 			seekBar = (SeekBar) findViewById(R.id.seekbar);
 			startingPrice = (TextView) findViewById(R.id.starting_price);
 			offerer = (TextView) findViewById(R.id.bidder);
 			latestPrice = (TextView) findViewById(R.id.last_price);
 			myOffer = (TextView) findViewById(R.id.myoffer);
 			productId = 12;
 			break;
 		case 13:
 			setContentView(R.layout.product_thirteen);
 			setTitle(R.string.product_title13);
 			seekBar = (SeekBar) findViewById(R.id.seekbar);
 			startingPrice = (TextView) findViewById(R.id.starting_price);
 			offerer = (TextView) findViewById(R.id.bidder);
 			latestPrice = (TextView) findViewById(R.id.last_price);
 			myOffer = (TextView) findViewById(R.id.myoffer);
 			productId = 13;
 			break;
 		case 14:
 			setContentView(R.layout.product_fourteen);
 			setTitle(R.string.product_title14);
 			seekBar = (SeekBar) findViewById(R.id.seekbar);
 			startingPrice = (TextView) findViewById(R.id.starting_price);
 			offerer = (TextView) findViewById(R.id.bidder);
 			latestPrice = (TextView) findViewById(R.id.last_price);
 			myOffer = (TextView) findViewById(R.id.myoffer);
 			productId = 14;
 			break;
 		case 15:
 			setContentView(R.layout.product_fifteen);
 			setTitle(R.string.product_title15);
 			seekBar = (SeekBar) findViewById(R.id.seekbar);
 			startingPrice = (TextView) findViewById(R.id.starting_price);
 			offerer = (TextView) findViewById(R.id.bidder);
 			latestPrice = (TextView) findViewById(R.id.last_price);
 			myOffer = (TextView) findViewById(R.id.myoffer);
 			productId = 15;
 			break;
 		case 16:
 			setContentView(R.layout.product_sixteen);
 			setTitle(R.string.product_title16);
 			seekBar = (SeekBar) findViewById(R.id.seekbar);
 			startingPrice = (TextView) findViewById(R.id.starting_price);
 			offerer = (TextView) findViewById(R.id.bidder);
 			latestPrice = (TextView) findViewById(R.id.last_price);
 			myOffer = (TextView) findViewById(R.id.myoffer);
 			productId = 16;
 			break;
 		}
 		// TODO Refactor, add SharedPreferences to ActualPrice
 		// String price = dataFromServer.getString("latest_price", null);
 		actualPrice = setStartingPrice(productId);
 
 		startingPrice.setText("$ " + setInitialPrice(productId) + " USD");
 		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
 
 			public void onStopTrackingTouch(SeekBar seekBar) {
 
 			}
 
 			public void onStartTrackingTouch(SeekBar seekBar) {
 
 			}
 
 			public void onProgressChanged(SeekBar seekBar, int progress,
 					boolean fromUser) {
 				updateMyOfferTextView(progress);
 			}
 		});
 		if (x == 1) {
 			// TODO Use persistence to keep last offer, last offerer and your
 			// offer
 
 			bidderName = "Nadie ha ofertado";
 			latestPrice.setText("$ " + setInitialPrice(productId) + " USD");
 
 			offerer.setText(bidderName);
 		}
 		if (x == 2) {
 			sendProdNumToService(productId);
 		}
 	}
 
 	@Override
 	protected void onRestart() {
 		super.onRestart();
 		doBindService();
 		tmr.schedule(new TimerTask() {
 
 			@Override
 			public void run() {
 				sendInts();
 			}
 		}, 500);
 	}
 
 	@Override
 	protected void onStart() {
 		super.onStart();
 		ActionBar actionBar = this.getActionBar();
 		actionBar.setDisplayHomeAsUpEnabled(true);
 	}
 
 	@Override
 	protected void onStop() {
 		sendAliveToService(UpdateService.DEAD);
 		doUnbindService();
 		super.onStop();
 	}
 
 	/**
 	 * Inflating the Action Bar menu with MenuInflater instance and using the
 	 * method inflate.
 	 */
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.product_menu_items, menu);
 		return true;
 	}
 
 	/**
 	 * This is a method binded with the Button Onclick property
 	 */
 	public void openBiddingDialog(View v) {
 		if (userBid <= actualPrice) {
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			builder.setMessage("Por favor, haga una oferta mayor a la actual")
 					.setCancelable(false)
 					.setNegativeButton("Ok",
 							new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog,
 										int id) {
 									dialog.cancel();
 								}
 							});
 			AlertDialog alertDialog = builder.create();
 			alertDialog.show();
 			return;
 		}
 		Intent intent = new Intent(this, BiddingDialog.class);
 		intent.putExtra("Bid", userBid);
 		intent.putExtra("Product", productId);
 		startUI(2, productId);
 		userBid = 0;
 		startActivity(intent);
 	}
 
 	/**
 	 * Listener for the ActionBar Menu.
 	 */
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			Intent intent = new Intent(this, HomeActivity.class);
 			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 			startActivity(intent);
 			return true;
 		case R.id.menu_help:
 			Dialog.showHelpMessage(this);
 			return true;
 		case R.id.menu_about:
 			Dialog.showAboutMessage(this);
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	public String darFormato(String s) {
 		String cleanString = s.toString().replaceAll("[$,.]", "");
 		double parsed = Double.parseDouble(cleanString);
 		String formated = NumberFormat.getCurrencyInstance().format(
 				(parsed / 100));
 		return formated;
 	}
 
 	public void showProductInfo(View v) {
 		String title = null, contents = null;
 		switch (productId) {
 		case (1):
 			title = (String) getText(R.string.product_title1);
 			contents = (String) getText(R.string.product_abstract1);
 			break;
 		case (2):
 			title = (String) getText(R.string.product_title2);
 			contents = (String) getText(R.string.product_abstract2);
 			break;
 		case (3):
 			title = (String) getText(R.string.product_title3);
 			contents = (String) getText(R.string.product_abstract3);
 			break;
 		case (4):
 			title = (String) getText(R.string.product_title4);
 			contents = (String) getText(R.string.product_abstract4);
 			break;
 		case (5):
 			title = (String) getText(R.string.product_title5);
 			contents = (String) getText(R.string.product_abstract5);
 			break;
 		case (6):
 			title = (String) getText(R.string.product_title6);
 			contents = (String) getText(R.string.product_abstract6);
 			break;
 		case (7):
 			title = (String) getText(R.string.product_title7);
 			contents = (String) getText(R.string.product_abstract7);
 			break;
 		case (8):
 			title = (String) getText(R.string.product_title8);
 			contents = (String) getText(R.string.product_abstract8);
 			break;
 		case (9):
 			title = (String) getText(R.string.product_title9);
 			contents = (String) getText(R.string.product_abstract9);
 			break;
 		case (10):
 			title = (String) getText(R.string.product_title10);
 			contents = (String) getText(R.string.product_abstract10);
 			break;
 		case (11):
 			title = (String) getText(R.string.product_title11);
 			contents = (String) getText(R.string.product_abstract11);
 			break;
 		case (12):
 			title = (String) getText(R.string.product_title12);
 			contents = (String) getText(R.string.product_abstract12);
 			break;
 		case (13):
 			title = (String) getText(R.string.product_title13);
 			contents = (String) getText(R.string.product_abstract13);
 			break;
 		case (14):
 			title = (String) getText(R.string.product_title14);
 			contents = (String) getText(R.string.product_abstract14);
 			break;
 		case (15):
 			title = (String) getText(R.string.product_title15);
 			contents = (String) getText(R.string.product_abstract15);
 			break;
 		case (16):
 			title = (String) getText(R.string.product_title16);
 			contents = (String) getText(R.string.product_abstract16);
 			break;
 		}
 		Dialog.showMoreInfo(this, title, contents);
 	}
 
 	public String setInitialPrice(int product) {
 		switch (product) {
 		case 1:
 			return "7000";
 		case 2:
 			return "1100";
 		case 3:
 			return "3500";
 		case 4:
 			return "1100";
 		case 5:
 			return "2550";
 		case 6:
 			return "10000";
 		case 7:
 			return "9000";
 		case 8:
 			return "11680";
 		case 9:
 			return "10220";
 		case 10:
 			return "8500";
 		case 11:
 			return "1230";
 		case 12:
 			return "900";
 		case 13:
 			return "2100";
 		case 14:
 			return "125500";
 		case 15:
 			return "1500";
 		case 16:
 			return "500";
 		}
 		return "???";
 	}
 
 	public long setStartingPrice(int product) {
 		switch (product) {
 		case 1:
 			return 7000;
 		case 2:
 			return 1100;
 		case 3:
 			return 3500;
 		case 4:
 			return 1100;
 		case 5:
 			return 2550;
 		case 6:
 			return 10000;
 		case 7:
 			return 9000;
 		case 8:
 			return 11680;
 		case 9:
 			return 10220;
 		case 10:
 			return 8500;
 		case 11:
 			return 1230;
 		case 12:
 			return 900;
 		case 13:
 			return 2100;
 		case 14:
 			return 125500;
 		case 15:
 			return 1500;
 		case 16:
 			return 500;
 
 		}
 		return 0;
 	}
 
 	public void updateMyOfferTextView(int progress) {
 		int bid = MAXIMUM_BID_AMOUNT * progress;
 		userBid = actualPrice + bid;
 		myOffer.setText("$ " + userBid + " USD");
 	}
 
 	void doBindService() {
 		bindService(new Intent(this, UpdateService.class), mConnection,
 				Context.BIND_AUTO_CREATE);
 		mIsBound = true;
 	}
 
 	void doUnbindService() {
 		if (mIsBound) {
 			if (mService != null) {
 				try {
 					Message msg = Message.obtain(null,
 							UpdateService.MSG_UNREGISTER_CLIENT);
 					msg.replyTo = mMessenger;
 					mService.send(msg);
 				} catch (RemoteException e) {
 				}
 			}
 			unbindService(mConnection);
 			mIsBound = false;
 		}
 	}
 
 	private void sendAliveToService(int intvaluetosend) {
 		if (mService != null) {
 			try {
 				Bundle b = new Bundle();
 				b.putInt("Alive", intvaluetosend);
 				Message msg = Message.obtain(null,
 						UpdateService.MSG_SET_INT_VALUE);
 				msg.setData(b);
 				mService.send(msg);
 			} catch (RemoteException e) {
 			}
 		}
 	}
 
 	private void sendProdNumToService(int intvaluetosend) {
 		if (mService != null) {
 			try {
 				Bundle b = new Bundle();
 				b.putInt("Product", intvaluetosend);
 				Message msg = Message.obtain(null,
 						UpdateService.MSG_SET_INT_VALUE);
 				msg.setData(b);
 				mService.send(msg);
 			} catch (RemoteException e) {
 			}
 		}
 	}
 
 	public void sendInts() {
 		sendAliveToService(UpdateService.ALIVE);
 		sendProdNumToService(productId);
 	}
 
 	@Override
 	public void onProgressChanged(SeekBar seekBar, int progress,
 			boolean fromUser) {
 
 	}
 
 	@Override
 	public void onStartTrackingTouch(SeekBar seekBar) {
 
 	}
 
 	@Override
 	public void onStopTrackingTouch(SeekBar seekBar) {
 
 	}
 
 	/**
 	 * Dirty hack to get ActionBar filled with a tile programatically
 	 * 
 	 * */
 	private void setActionBarCustomBackground() {
 		final ActionBar actionBar = getActionBar();
 		BitmapDrawable background = new BitmapDrawable(
 				BitmapFactory.decodeResource(getResources(),
 						R.drawable.actionbar_tile));
 		background.setTileModeX(android.graphics.Shader.TileMode.REPEAT);
 		actionBar.setBackgroundDrawable(background);
 	}
 }

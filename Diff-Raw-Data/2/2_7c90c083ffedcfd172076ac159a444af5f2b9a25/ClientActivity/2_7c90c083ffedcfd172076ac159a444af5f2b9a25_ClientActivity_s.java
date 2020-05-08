 package edu.vub.at.nfcpoker.ui;
 
 import java.io.IOException;
 import java.util.Set;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.UUID;
 
 import android.animation.Animator;
 import android.animation.AnimatorListenerAdapter;
 import android.animation.AnimatorSet;
 import android.animation.ObjectAnimator;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.text.InputType;
 import android.util.Log;
 import android.view.GestureDetector;
 import android.view.Gravity;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.esotericsoftware.kryonet.Client;
 import com.esotericsoftware.kryonet.Connection;
 import com.esotericsoftware.kryonet.Listener;
 
 import edu.vub.at.commlib.CommLibConnectionInfo;
 import edu.vub.at.nfcpoker.Card;
 import edu.vub.at.nfcpoker.ConcretePokerServer.GameState;
 import edu.vub.at.nfcpoker.R;
 import edu.vub.at.nfcpoker.comm.Message;
 import edu.vub.at.nfcpoker.comm.Message.CheatMessage;
 import edu.vub.at.nfcpoker.comm.Message.ClientAction;
 import edu.vub.at.nfcpoker.comm.Message.ClientActionMessage;
 import edu.vub.at.nfcpoker.comm.Message.NicknameMessage;
 import edu.vub.at.nfcpoker.comm.Message.ReceiveHoleCardsMessage;
 import edu.vub.at.nfcpoker.comm.Message.ReceivePublicCards;
 import edu.vub.at.nfcpoker.comm.Message.StateChangeMessage;
 import edu.vub.at.nfcpoker.comm.Message.ClientActionType;
 import edu.vub.at.nfcpoker.comm.Message.FutureMessage;
 import edu.vub.at.nfcpoker.comm.Message.SetIDMessage;
 import edu.vub.at.nfcpoker.comm.Message.RoundWinnersDeclarationMessage;
 import edu.vub.at.nfcpoker.comm.Message.RequestClientActionFutureMessage;
 import edu.vub.at.nfcpoker.settings.Settings;
 import edu.vub.at.nfcpoker.ui.tools.PageProvider;
 import fi.harism.curl.CurlView;
 
 public class ClientActivity extends Activity implements OnClickListener, ServerViewInterface {
 
 	public class ConnectAsyncTask extends AsyncTask<Void, Void, Client> {
 
 		private int port;
 		private String address;
 		private Listener listener;
 
 
 		public ConnectAsyncTask(String address, int port, Listener listener) {
 			this.address = address;
 			this.port = port;
 			this.listener = listener;
 		}
 
 		@Override
 		protected void onPreExecute() {
 			super.onPreExecute();
 			showBarrier("Connecting to server");
 		}
 
 		@Override
 		protected Client doInBackground(Void... params) {
 			try {
 				return CommLibConnectionInfo.connect(address, port, listener);
 			} catch (IOException e) {
 				Log.d("AMBIENTPOKER", "Could not connect to server", e);
 			}
 			return null;
 		}
 	}
 
 	private ProgressDialog barrier;
 
 	public int myClientID;
 
 	private void showBarrier(String cause) {
 		if (barrier == null) {
 			barrier = new ProgressDialog(ClientActivity.this);
 			barrier.setTitle(cause);
 			barrier.setCancelable(false);
 			barrier.setMessage("Please wait");
 			barrier.show();
 		} else {
 			barrier.setTitle(cause);
 		}
 	}
 
 	private void hideBarrier() {
 		if (barrier != null) {
 			barrier.dismiss();
 			barrier = null;
 		}
 	}
 
 
 
 
 
 	// Game state
 	//public static GameState GAME_STATE = GameState.INIT;
 	private static int currentMoney = 2000;
 	private int currentBet = 0;
 	private int currentTotalBet = 0;
 	private int minimumBet = 0;
 	private int currentChipSwiped = 0;
 
 	// Dedicated
 	private int nextToReveal = 0;
 	private boolean isDedicated = false;
 
 	// UI
 	private CurlView mCardView1;
 	private CurlView mCardView2;
 
 	//private int POKER_GREEN = Color.rgb(44, 103, 46);
 	public static final int POKER_GREEN = 0xFF2C672E;
 	private static final int[] DEFAULT_CARDS = new int[] { R.drawable.backside, R.drawable.backside };
 
 	// Interactivity
 	public static boolean incognitoMode;
 	private static final boolean useIncognitoMode = true;
 	private static final boolean useIncognitoLight = false;
 	private static final boolean useIncognitoProxmity = true;
 	private long incognitoLight;
 	private long incognitoProximity;
 	private Timer incognitoDelay;
 	private SensorManager sensorManager;
 
 	// Help
 	private boolean firstSwipe = true;
 
 	// egb: added for gestures.
 	private static final int SWIPE_MIN_DISTANCE = 120;
 	private static final int SWIPE_MAX_OFF_PATH = 250;
 	private static final int SWIPE_THRESHOLD_VELOCITY = 200;
 	private GestureDetector gestureDetector;
 	View.OnTouchListener gestureListener;
 	private Button bet;
 	private Button check;
 	private Button fold;
 
 	private UUID pendingFuture;
 	private Connection serverConnection;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		// Game state
 		isDedicated = getIntent().getBooleanExtra("isDedicated", false);
 		if (isDedicated) {
 			setContentView(R.layout.activity_client_is_dedicated);
 		} else {
 			setContentView(R.layout.activity_client);
 		}
 
 		// Interactivity
 		incognitoMode = false;
 		incognitoLight = -1;
 		incognitoProximity = -1;
 		incognitoDelay = new Timer();
 		sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
 
 
 		// Gesture detection
 		gestureDetector = new GestureDetector(this, new MyGestureDetector());
 		gestureListener = new View.OnTouchListener() {
 			@Override
 			public boolean onTouch(View arg0, MotionEvent arg1) {
 				if (firstSwipe) {
 					firstSwipe = false;
 					Toast.makeText(ClientActivity.this, "Swipe up or down to add or remove money", Toast.LENGTH_SHORT).show();
 				}
 				int viewSwiped = arg0.getId();
 				switch(viewSwiped) {
 				case R.id.whitechip: currentChipSwiped = 5; break;
 				case R.id.redchip: currentChipSwiped = 10; break;
 				case R.id.greenchip: currentChipSwiped = 20; break;
 				case R.id.bluechip: currentChipSwiped = 50; break;
 				case R.id.blackchip: currentChipSwiped = 100; break;
 				default: Log.v("AMBIENTPOKER", "wrong view swipped" + viewSwiped);
 				}
 				return gestureDetector.onTouchEvent(arg1);
 			}
 		};
 
 		final ImageView whitechip = (ImageView) findViewById(R.id.whitechip);
 		whitechip.setOnClickListener(ClientActivity.this);
 		whitechip.setOnTouchListener(gestureListener);
 
 		final ImageView redchip = (ImageView) findViewById(R.id.redchip);
 		redchip.setOnClickListener(ClientActivity.this);
 		redchip.setOnTouchListener(gestureListener);
 
 		final ImageView greenchip = (ImageView) findViewById(R.id.greenchip);
 		greenchip.setOnClickListener(ClientActivity.this);
 		greenchip.setOnTouchListener(gestureListener);
 
 		final ImageView bluechip = (ImageView) findViewById(R.id.bluechip);
 		bluechip.setOnClickListener(ClientActivity.this);
 		bluechip.setOnTouchListener(gestureListener);
 
 		final ImageView blackchip = (ImageView) findViewById(R.id.blackchip);
 		blackchip.setOnClickListener(ClientActivity.this);
 		blackchip.setOnTouchListener(gestureListener);
 
 		/*
 				ArrayList<Bitmap> mPages1 = new ArrayList<Bitmap>();
 		mPages1.add(BitmapFactory.decodeResource(getResources(), R.drawable.backside));
 		mPages1.add(BitmapFactory.decodeResource(getResources(), R.drawable.clubs_10c));
 
 				final PageCurlView card1 = (PageCurlView) findViewById(R.id.Card1);
 				card1.setPages(mPages1);
 
 				ArrayList<Bitmap> mPages2 = new ArrayList<Bitmap>();
 				mPages2.add(BitmapFactory.decodeResource(getResources(), R.drawable.backside));
 				mPages2.add(BitmapFactory.decodeResource(getResources(), R.drawable.diamonds_10d));
 
 				final PageCurlView card2 = (PageCurlView) findViewById(R.id.Card2);
 				card2.setPages(mPages2);
 		 */
 
 		mCardView1 = (CurlView) findViewById(R.id.Card1);
 		mCardView1.setPageProvider(new PageProvider(this, DEFAULT_CARDS));
 		mCardView1.setCurrentIndex(0);
 		mCardView1.setBackgroundColor(POKER_GREEN);
 		mCardView1.setAllowLastPageCurl(false);    
 
 		mCardView2 = (CurlView) findViewById(R.id.Card2);
 		mCardView2.setPageProvider(new PageProvider(this, DEFAULT_CARDS));
 		mCardView2.setCurrentIndex(0);
 		mCardView2.setBackgroundColor(POKER_GREEN);
 		mCardView2.setAllowLastPageCurl(false);    
 
 		bet = (Button) findViewById(R.id.Bet);
 		check = (Button) findViewById(R.id.Check);
 		fold = (Button) findViewById(R.id.Fold);
 
 		bet.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				runOnNotUiThread(new Runnable() {
 					public void run() {
 						currentMoney -= currentBet;
 						currentTotalBet += currentBet;
 						ClientAction ca = new ClientAction(ClientActionType.Bet, currentBet);
 						currentBet = 0;
 						serverConnection.sendTCP(new FutureMessage(pendingFuture, ca));
 					}
 				});
 				disableActions();
 			}
 		});
 
 		check.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				runOnNotUiThread(new Runnable() {
 					public void run() {
 						currentBet = 0;
 						ClientAction ca = new ClientAction(ClientActionType.Check);
 						serverConnection.sendTCP(new FutureMessage(pendingFuture, ca));
 					}
 				});
 				disableActions();
 			}
 		});
 
 		fold.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				runOnNotUiThread(new Runnable() {
 					public void run() {
 						currentBet = 0;
 						ClientAction ca = new ClientAction(ClientActionType.Fold);
 						serverConnection.sendTCP(new FutureMessage(pendingFuture, ca));
 					}
 				});
 				disableActions();
 			}
 		});
 
 		currentBet = 0;
 		currentTotalBet = 0;
 		currentChipSwiped = 0;
 		nextToReveal = 0;
 
 		listenToGameServer();
 	}
 
 
 	protected void runOnNotUiThread(Runnable runnable) {
 		new Thread(runnable).start();
 	}
 
 
 	private void setServerConnection(Connection c) {
 		serverConnection = c;
 	}
 
 	private void updateMoneyTitle() {
 		if (currentTotalBet > 0) {
 			setTitle("wePoker (" +currentMoney+"\u20AC -- Total bet: "+(currentTotalBet)+"\u20AC)");
 		} else {
 			setTitle("wePoker (" +currentMoney+"\u20AC)");
 		}
 	}
 
 	private void enableActions() {
 		runOnUiThread(new Runnable() {
 
 			@Override
 			public void run() {
 				bet.setEnabled(true);
 				check.setEnabled(true);
 				fold.setEnabled(true);
 				updateMoneyTitle();
 			}
 		});
 	}
 
 	private void disableActions() {
 		runOnUiThread(new Runnable() {
 
 			@Override
 			public void run() {
 				bet.setEnabled(false);
 				check.setEnabled(false);
 				fold.setEnabled(false);
 				updateMoneyTitle();
 			}
 		});
 	}
 
 	Listener listener = new Listener() {
 		@Override
 		public void connected(Connection arg0) {
 			super.connected(arg0);
 			setServerConnection(arg0);
 			Log.d("AMBIENTPOKER","Connected to server!");
 		}
 
 
 		@Override
 		public void received(Connection c, Object m) {
 			super.received(c, m);
 
 			Log.v("AMBIENTPOKER", "Received message " + m.toString());
 
 			if (m instanceof StateChangeMessage) {
 				StateChangeMessage scm = (StateChangeMessage) m;
 				GameState newGameState = scm.newState;
 				runOnUiThread(new Runnable() {
 					public void run() {
 						disableActions();
 					}});
 				String toastToShow = null;
 				switch (newGameState) {
 				case STOPPED:
 					Log.v("AMBIENTPOKER", "Game state changed to STOPPED");
 					runOnUiThread(new Runnable() {
 						public void run() {
 							showBarrier("Waiting for players");
 						}});
 					break;
 				case WAITING_FOR_PLAYERS:
 					Log.v("AMBIENTPOKER", "Game state changed to WAITING_FOR_PLAYERS");
 					runOnUiThread(new Runnable() {
 						public void run() {
 							showBarrier("Waiting for players");
 							hideCards();
 						}});
 					break;
 				case PREFLOP:
 					toastToShow = "Any preflop bet?";
 					Log.v("AMBIENTPOKER", "Game state changed to PREFLOP");
 					runOnUiThread(new Runnable() {
 						public void run() {
 							hideBarrier();
 							showCards();
 						}});
 					break;
 				case FLOP:
 					toastToShow = "Flopping cards...";
 					Log.v("AMBIENTPOKER", "Game state changed to FLOP");
 					break;
 				case TURN:
 					toastToShow = "Here is the turn";
 					Log.v("AMBIENTPOKER", "Game state changed to TURN");
 					break;
 				case RIVER:
 					toastToShow = "River card visible";
 					Log.v("AMBIENTPOKER", "Game state changed to RIVER");
 					break;
 				case END_OF_ROUND:
 					Log.v("AMBIENTPOKER", "Game state changed to END_OF_ROUND");
 					currentBet = 0;
 					currentTotalBet = 0;
 					currentChipSwiped = 0;
 					nextToReveal = 0;
 					runOnUiThread(new Runnable() {
 						public void run() {
 							updateMoneyTitle();
 							updateBetAmount();
 							updateMinBetAmount(0);
 						}});
 					break;
 				}
 
 				if (toastToShow != null) {
 					final String toastToShowFinal = toastToShow;
 					runOnUiThread(new Runnable() {
 						public void run() {
 							Toast toast = Toast.makeText(ClientActivity.this, toastToShowFinal, Toast.LENGTH_SHORT);
 							toast.show();
 						}
 					});
 				}
 			}
 
 			if (m instanceof ReceivePublicCards) {
 				ReceivePublicCards newPublicCards = (ReceivePublicCards) m;
 				Log.v("AMBIENTPOKER", "Received public cards: ");
 				Card[] cards = newPublicCards.cards;
 				for (int i = 0; i < cards.length; i++) {
 					Log.v("AMBIENTPOKER", cards[i].toString() + ", ");
 				}
 			}
 
 			if (m instanceof ReceiveHoleCardsMessage) {
 				final ReceiveHoleCardsMessage newHoleCards = (ReceiveHoleCardsMessage) m;
 				Log.v("AMBIENTPOKER", "Received hand cards: " + newHoleCards.toString());
 				ClientActivity.this.runOnUiThread(new Runnable() {
 					@Override
 					public void run() {
 						updateHandGui(newHoleCards);
 					}
 				});
 			}
 
 			if (m instanceof ClientActionMessage) {
 				final ClientActionMessage newClientActionMessage = (ClientActionMessage) m;
 				final ClientAction action = newClientActionMessage.getClientAction();
 				Log.v("AMBIENTPOKER", "Received client action message" + newClientActionMessage.toString());
 				if (action.getClientActionType() == Message.ClientActionType.Bet) {
 					final int amount = action.getExtra();
 					if (amount > minimumBet) {
 						runOnUiThread(new Runnable() {
 							public void run() {
 								updateMinBetAmount(amount);
 							}
 						});
 					}
 				}
 			}
 
 
 			if (m instanceof RequestClientActionFutureMessage) {
 				final RequestClientActionFutureMessage rcafm = (RequestClientActionFutureMessage) m;
 				pendingFuture = rcafm.futureId;
 				Log.d("AMBIENTPOKER", "Pending future: " + pendingFuture);
 
 				runOnUiThread(new Runnable() {
 					public void run() {
 						enableActions();
 					}});
 			}
 
 			if (m instanceof SetIDMessage) {
 				final SetIDMessage sidm = (SetIDMessage) m;
 				myClientID = sidm.id;
 			}
 
 			if (m instanceof RoundWinnersDeclarationMessage) {
 				final RoundWinnersDeclarationMessage rwdm = (RoundWinnersDeclarationMessage) m;
 				final Set<Integer> players = rwdm.bestPlayers;
 				if (players.contains(myClientID)) {
 					currentMoney += rwdm.chips / players.size();
 					runOnUiThread(new Runnable() {
 						public void run() {
 							updateMoneyTitle();
 							Toast.makeText(ClientActivity.this, "You won!!", Toast.LENGTH_LONG).show();
 						}});
 
 				} else {
 					// boe
 					runOnUiThread(new Runnable() {
 						public void run() {
 							Toast.makeText(ClientActivity.this, "You lost...", Toast.LENGTH_LONG).show();
 						}});
 				}
 			}
 		}
 	};
 
 
 	private void listenToGameServer() {
 		final String ip = getIntent().getStringExtra("ip");
 		final int port = getIntent().getIntExtra("port", 0);
 		new ConnectAsyncTask(ip, port, listener).execute();
 	}
 
 	private void updateHandGui(ReceiveHoleCardsMessage cards) {
 
 		int id1 = getResources().getIdentifier("edu.vub.at.nfcpoker:drawable/" + cards.card1.toString(), null, null);
 		int[] bitmapIds1 = new int[] { R.drawable.backside, id1 };
 		mCardView1.setPageProvider(new PageProvider(this, bitmapIds1));
 
 		int id2 = getResources().getIdentifier("edu.vub.at.nfcpoker:drawable/" + cards.card2.toString(), null, null);
 		int[] bitmapIds2 = new int[] { R.drawable.backside, id2 };
 		mCardView2.setPageProvider(new PageProvider(this, bitmapIds2));
 
 		updateMoneyTitle();
 	}
 
 	@Override
 	protected void onResume() {
 		if (useIncognitoMode) {
 			incognitoMode = false;
 			incognitoLight = -1;
 			incognitoProximity = -1;
 			incognitoDelay = new Timer();
 
 			if (useIncognitoLight) {
 				Sensor lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
 				if (lightSensor != null) {
 					sensorManager.registerListener(incognitoSensorEventListener,
 							lightSensor,
 							SensorManager.SENSOR_DELAY_NORMAL);
 					incognitoLight = 0;
 				}
 			}
 			if (useIncognitoProxmity) {
 				Sensor proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
 				if (proximitySensor != null) {
 					sensorManager.registerListener(incognitoSensorEventListener,
 							proximitySensor,
 							SensorManager.SENSOR_DELAY_NORMAL);
 					incognitoProximity = 0;
 				}
 			}
 		}
 		mCardView1.onResume();
 		mCardView2.onResume();
 		super.onResume();
 	}
 
 	@Override
 	protected void onPause() {
 		sensorManager.unregisterListener(incognitoSensorEventListener);
 		mCardView1.onPause();
 		mCardView2.onPause();
 		super.onPause();
 	}
 
 	@Override
 	public void onStop() {
 		super.onStop();
 		Settings.saveSettings(this);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		if (isDedicated) {
 			inflater.inflate(R.menu.activity_client_dedicated, menu);
 		} else {
 			inflater.inflate(R.menu.activity_client, menu);
 		}
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		switch (item.getItemId()) {
 		case R.id.itemSetName:
 			askNickName();
 			return true;
 		case R.id.itemAddMoney:
 			addMoney();
 			return true;
 		case R.id.itemAbout:
 			launchMainWebsite();
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 	
 	private void askNickName() {
 		final Context ctx = this;
 		final Dialog moneyDialog;
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		final EditText input = new EditText(this);
 		input.setInputType(InputType.TYPE_CLASS_TEXT);
 		builder.setView(input);
 		builder.setPositiveButton("Set", new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface di, int arg1) {
 						try {
 							String nickname = input.getText().toString();
 							Settings.saveSettings(ctx);
 							// TODO Server: User X added #{extra}
 							NicknameMessage ca = new NicknameMessage(nickname);
 							serverConnection.sendTCP(new FutureMessage(pendingFuture, ca));
 						} catch (Exception e) {	}
 					}
 				});
 		moneyDialog = builder.create();
 		moneyDialog.show();
 	}
 	
 	private void addMoney() {
 		final Dialog moneyDialog;
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		final EditText input = new EditText(this);
 		input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
 		builder.setView(input);
 		builder.setPositiveButton("Add Chips", new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface di, int arg1) {
 						try {
 							int extra = Integer.parseInt(input.getText().toString());
 							currentMoney += extra;
 							// TODO Server: User X added #{extra}
 							CheatMessage ca = new CheatMessage(extra);
 							serverConnection.sendTCP(new FutureMessage(pendingFuture, ca));
 						} catch (Exception e) {	}
 					}
 				});
 		moneyDialog = builder.create();
 		moneyDialog.show();
 	}
 
 	private void launchMainWebsite() {
 		try {
 			Intent intent = new Intent(Intent.ACTION_VIEW);
 			intent.setData(Uri.parse(Splash.WEPOKER_WEBSITE));
 			startActivity(intent);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 
 	enum ToastBetAmount {
 		Positive, Negative, OutOfMoney, MinimumBet
 	};
 
 	// Game
 	private void updateBetAmount(int value) {
 		ToastBetAmount toast = ToastBetAmount.Positive;
 		if (value < 0) toast = ToastBetAmount.Negative;
 		currentBet += value;
 		if (currentBet > currentMoney) {
 			currentBet = currentMoney;
 			toast = ToastBetAmount.OutOfMoney;
 		}
 		if (minimumBet > currentBet) {
 			currentBet = minimumBet;
 			toast = ToastBetAmount.MinimumBet;
 		}
 
 		Toast t;
 		switch (toast) {
 		case Positive:
 			t = Toast.makeText(ClientActivity.this, "Adding " + currentChipSwiped + "\u20AC to the bet", Toast.LENGTH_SHORT);
 			t.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 0);
 			t.show();
 			break;
 		case Negative:
 			t = Toast.makeText(ClientActivity.this, "Reducing bet with " + currentChipSwiped + "\u20AC", Toast.LENGTH_SHORT);
 			t.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 0);
 			t.show();
 			break;
 		case OutOfMoney:
 			Toast.makeText(ClientActivity.this, "Out of money !!", Toast.LENGTH_SHORT).show();
 			break;
 		case MinimumBet:
 			Toast.makeText(ClientActivity.this, "Minimum bet required", Toast.LENGTH_SHORT).show();
 			break;
 		}
 
 		final TextView textCurrentBet = (TextView) findViewById(R.id.currentBet);
 		textCurrentBet.setText(" " + currentBet);
 		updateMoneyTitle();
 	}
 
 	private void updateBetAmount() {
 		final TextView currentBet = (TextView) findViewById(R.id.currentBet);
 		currentBet.setText(" " + this.currentBet);
 	}
 	
 	private void updateMinBetAmount(int value) {
 		minimumBet = value;
 		final TextView textCurrentBet = (TextView) findViewById(R.id.minBet);
 		textCurrentBet.setText(" " + minimumBet);
 		updateMoneyTitle();
 	}
 
 	// Interactivity
 	SensorEventListener incognitoSensorEventListener = new SensorEventListener() {
 		@Override
 		public void onAccuracyChanged(Sensor sensor, int accuracy) {
 
 		}
 
 		@Override
 		public void onSensorChanged(SensorEvent event) {
 			if (event.sensor.getType()==Sensor.TYPE_LIGHT){
 				float currentReading = event.values[0];
 				if (currentReading < 10) {
 					if (incognitoLight == 0) incognitoLight = System.currentTimeMillis();
 					Log.d("Light SENSOR", "It's dark!" + currentReading);
 				} else {
 					incognitoLight = 0;
 					Log.d("Light SENSOR", "It's bright!" + currentReading);
 				}
 			}
 			if (event.sensor.getType()==Sensor.TYPE_PROXIMITY){
 				float currentReading = event.values[0];
 				if (currentReading < 1) {
 					if (incognitoProximity == 0) incognitoProximity = System.currentTimeMillis();
 					Log.d("Proximity SENSOR", "I found a hand!" + currentReading);
 				} else {
 					incognitoProximity = 0;
 					Log.d("Proximity SENSOR", "All clear!" + currentReading);
 				}
 			}
 			if ((incognitoLight != 0) && (incognitoProximity != 0)) {
 				if (!incognitoMode) {
 					incognitoMode = true;
 					incognitoDelay = new Timer();
 					incognitoDelay.schedule(new TimerTask() {
 						public void run() {
 							runOnUiThread(new Runnable() {
 								@Override
 								public void run() {
 									showCards();
 								}
 							});
 						}}, 750);
 				}
 			} else {
 				if (incognitoDelay != null) {
 					incognitoDelay.cancel();
 					incognitoDelay = null;
 				}
 				if (incognitoMode) {
 					incognitoMode = false;
 					runOnUiThread(new Runnable() {
 						@Override
 						public void run() {
 							hideCards();
 						}
 					});
 				}
 			}
 		}
 	};
 
 	// UI
 	private void showCards() {
 		mCardView1.setCurrentIndex(1);
 		mCardView2.setCurrentIndex(1);
 	}
 
 	private void hideCards() {
 		mCardView1.setCurrentIndex(0);
 		mCardView2.setCurrentIndex(0);
 	}
 
 	public void revealCards(final Card[] cards) {
 		runOnUiThread(new Runnable() {
 			public void run() {
 				for (Card c : cards) {
 					Log.d("PokerServer", "Revealing card " + c);
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
 		Log.d("PokerServer", "Hiding cards again");
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
 
 	@Override
 	public void onClick(View v) {
 		//	Filter f = (Filter) v.getTag();
 		//	FilterFullscreenActivity.show(this, input, f);
 
 	}
 
 	class MyGestureDetector extends GestureDetector.SimpleOnGestureListener {
 		@Override
 		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
 			try {
 				if (Math.abs(e1.getX() - e2.getX()) > SWIPE_MAX_OFF_PATH)
 					return false;
 				// right to left swipe
 				if(e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
 					updateBetAmount(currentChipSwiped);
 				}	else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
 					updateBetAmount(-currentChipSwiped);
 				}
 			} catch (Exception e) {
 				// nothing
 			}
 			return false;
 		}
 
 	}
 
 	@Override
 	public void addPlayer(int clientID, String clientName, int initialMoney) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void showStatechange(GameState newState) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void setPlayerMoney(Integer player, int current) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void removePlayer(Integer i) {
 		// TODO Auto-generated method stub
 
 	}
 }

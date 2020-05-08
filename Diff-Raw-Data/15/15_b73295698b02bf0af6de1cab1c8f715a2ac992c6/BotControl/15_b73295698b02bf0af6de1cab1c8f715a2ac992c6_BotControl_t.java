 package edu.ncsu.ieee.botcontrol;
 
 import java.util.Iterator;
 import java.util.regex.Pattern;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.ToggleButton;
 
 /** Touch-based bot control activity. */
 public class BotControl extends Activity implements TouchJoystick.JoystickListener, ZMQSubscriberThread.OnMessageListener {
 	private static final String TAG = "BotControl";
 
 	/** Convenience class for specifying control ranges. */
 	public static class ControlRange {
 		public float min, zero_min, zero, zero_max, max;
 		public float half_range, offset;
 
 		/** Initialize control range members, compute some useful derived values. */
 		ControlRange(float min, float zero_min, float zero, float zero_max, float max) {
 			// NOTE min < zero_min <= zero <= zero_max < max
 			this.min = min;
 			this.zero_min = zero_min;
 			this.zero = zero;
 			this.zero_max = zero_max;
 			this.max = max;
 
 			this.half_range = (this.max - this.min) / 2;
 			this.offset = (this.min + this.max) / 2;
 		}
 
 		/** Convert a normalized input value in [-1, 1] to a range-limited control value. */
 		public float fromNormalizedInput(float value) {
 			return applyLimits(offset + half_range * value);
 		}
 
 		/** Convert a range-limited control value to a normalized input value in [-1, 1]. */
 		public float toNormalizedInput(float value) {
 			return (value - offset) / half_range;
 		}
 
 		/** Apply range limits to control value. */
 		public float applyLimits(float value) {
 			return
 					(value < min
 							? min
 									: (value < zero_min
 											? value
 													: (value <= zero_max
 													? zero
 															: (value <= max
 															? value
 																	: max
 																	)
 															)
 											)
 							);
 		}
 	}
 
 	// Drive variables and ranges (TODO Check turn range)
 	private static final ControlRange forwardRange = new ControlRange(-100.f, -25.f, 0.f, 25.f, 100.f);
 	private float forward = forwardRange.zero;
 
 	private static final ControlRange strafeRange = new ControlRange(-100.f, -25.f, 0.f, 25.f, 100.f);
 	private float strafe = strafeRange.zero;
 
 	private static final ControlRange turnRange = new ControlRange(-100.f, -25.f, 0.f, 25.f, 100.f);
 	private float turn = turnRange.zero;
 	// TODO Turning not implemented yet; turn is always 0
 
 	private float lastForward = forward;
 	private float lastStrafe = strafe;
 	private float lastTurn = turn;
 
 	private JSONObject driveCmdObj = new JSONObject(); // cached JSON objects for frequent use
 
 	// Turret variables and ranges (TODO Check pitch and yaw ranges)
 	private static final ControlRange pitchRange = new ControlRange(60.f, 90.f, 90.f, 90.f, 120.f);
 	private float pitch = pitchRange.zero;
 
 	private static final ControlRange yawRange = new ControlRange(10.f, 90.f, 90.f, 90.f, 170.f);
 	private float yaw = yawRange.zero;
 
 	private float lastPitch = pitch;
 	private float lastYaw = yaw;
 
 	private JSONObject turretCmdObj;
 
 	// Gun variables
 	private int laser = 0;
 	private int lastLaser = laser;
 	private int spin = 0;
 	private int lastSpin = spin;
 
 	private JSONObject laserCmdObj;
	private JSONObject spinUpCmdObj;
	private JSONObject spinDownCmdObj;
 	private JSONObject fireCmdObj;
 
 	// Other control variables
 	private boolean pingOkay = false;
 	private JSONObject pingCmdObj;
 	private JSONObject irCmdObj;
 	private JSONObject irBinaryCmdObj;
 	private boolean irReadBinary = false; // should we read binary or full-range values
 	private int irBinaryThresh = 100; // TODO make this editable
 	private JSONObject estopCmdObj;
 	private JSONObject exitCmdObj;
 	
 	// Misc. variables
 	int unknownColor = Color.rgb(80, 80, 80);
 	int okayColor = Color.rgb(80, 120, 80);
 	int errorColor = Color.rgb(120, 80, 80);
 	int maxConsoleLength = 200; // keep low to clear frequently
 
 	// Topics of streaming messages to subscribe to
 	// TODO Enable topics: "drive" (forward, strafe, turn), "turret" (pitch, yaw), "ir" (front, back, left, right)
 	private String[] subscriptionTopics = { "turret_pitch", "turret_yaw", "ir" };
 
 	// Communication
 	private String serverProtocol = "tcp";
 	private String serverHost = null; // leave null to read from resource file (if not in preferences), or override here: e.g. 10.2.1.1 for the bot, 10.0.2.2 from an emulator refers to the host computer
 	private int serverPort = 60000;
 	private int pubServerPort = 60001;
 	private ZMQClientThread clientThread = null;
 	private ZMQSubscriberThread subscriberThread = null;
 	private Thread dataThread = null;
 	private long dataInterval = 500; // ms, time between reads
 
 	// View elements
 	private TextView txtConsole = null;
 	private TouchJoystick driveJoystick = null;
 	private TouchJoystick turretJoystick = null;
 	private TextView txtForward = null;
 	private TextView txtStrafe = null;
 	private TextView txtPitch = null;
 	private TextView txtYaw = null;
 	private Button btnPing = null;
 	private ToggleButton btnLaser = null;
 	private ToggleButton btnSpin = null;
 	private Button btnFire = null;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		loadPreferences();
 
 		// Setup view and obtain references to view elements
 		setContentView(R.layout.activity_main);
 		txtConsole = (TextView) findViewById(R.id.txtConsole);
 		driveJoystick = (TouchJoystick) findViewById(R.id.driveJoystick);
 		turretJoystick = (TouchJoystick) findViewById(R.id.turretJoystick);
 		txtForward = (TextView) findViewById(R.id.txtForward);
 		txtStrafe = (TextView) findViewById(R.id.txtStrafe);
 		txtPitch = (TextView) findViewById(R.id.txtPitch);
 		txtYaw = (TextView) findViewById(R.id.txtYaw);
 		btnPing = (Button) findViewById(R.id.btnPing);
 		btnLaser = (ToggleButton) findViewById(R.id.btnLaser);
 		btnSpin = (ToggleButton) findViewById(R.id.btnSpin);
 		btnFire = (Button) findViewById(R.id.btnFire);
 
 		// Initialize variables
 		lastForward = forward = forwardRange.zero;
 		lastStrafe = strafe = strafeRange.zero;
 		lastTurn = turn = turnRange.zero;
 		lastPitch = pitch = pitchRange.zero;
 		lastYaw = yaw = yawRange.zero;
 		lastLaser = laser = 0;
 		lastSpin = spin = 0;
 
 		// Initialize JSON objects that will be used frequently to make call requests
 		driveCmdObj = makeCallReq("driver", "move_forward_strafe", new String[] { "forward", "strafe" }, new Object[] { forward, strafe });
		turretCmdObj = makeCallReq("turret", "aim", new String[] { "yaw", "pitch" }, new Object[] { yaw, pitch });
 		pingCmdObj = makePingReq();
 		laserCmdObj = makeCallReq("gun", "set_laser", new String[] { "state" }, new Object[] { laser });
		spinUpCmdObj = makeCallReq("gun", "spin_up", null, null);
		spinDownCmdObj = makeCallReq("gun", "stop", null, null);
 		fireCmdObj = makeCallReq("gun", "fire", null, null);
 		irCmdObj = makeCallReq("ir_hub", "read_cached", new String[] { "max_staleness" }, new Object[] { dataInterval / 1000.f }); // dataInterval is in ms
 		irBinaryCmdObj = makeCallReq("ir_hub", "read_binary", new String[] { "thresh" }, new Object[] { irBinaryThresh });
 		estopCmdObj = makeCallReq("driver", "move_forward_strafe", new String[] { "forward", "strafe" }, new Object[] { forwardRange.zero, strafeRange.zero });
 		exitCmdObj = makeExitReq();
 
 		// Configure view elements
 		driveJoystick.setJoystickListener(this);
 		turretJoystick.setJoystickListener(this);
 		driveJoystick.updateKnob(strafeRange.toNormalizedInput(strafe), -forwardRange.toNormalizedInput(forward)); // NOTE Y-flip
 		turretJoystick.updateKnob(yawRange.toNormalizedInput(yaw), pitchRange.toNormalizedInput(pitch));
 		updateDriveViews();
 		updateTurretViews();
 		btnPing.setBackgroundColor(unknownColor);
 		btnPing.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				pingOkay = false; // assume not okay, unless it is
 				btnPing.setBackgroundColor(errorColor);
 				doPing(true); // block till reply (on reply, show visual indication using button color)
 			}
 		});
 		btnLaser.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				laser = (btnLaser.isChecked() ? 1 : 0);
 				doLaser(false); // ok to drop (on reply, show visual indication of success/failure with toggle button state)
 			}
 		});
 		btnSpin.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				spin = (btnSpin.isChecked() ? 1 : 0);
 				doSpin(false); // ok to drop (on reply, show visual indication of success/failure with toggle button state)
 			}
 		});
 		btnFire.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				doFire(false); // ok to drop (TODO show visual indication of success/failure, e.g. with button color?)
 			}
 		});
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		startClient();
 		//startSubscriber(); // NOTE subscriber disabled
 		startDataThread();
 		txtConsole.setText("[SYSTEM] Ready\n");
 	}
 
 	@Override
 	protected void onPause() {
 		stopDataThread();
 		//stopSubscriber(); // NOTE subscriber disabled
 		stopClient();
 		super.onPause();
 	}
 	
 	@Override
 	protected void onStop() {
 		savePreferences();
 		super.onStop();
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch(item.getItemId()) {
 		case R.id.action_zmqtest:
 			Intent zmqTestIntent = new Intent(this, ZMQTest.class);
 			startActivity(zmqTestIntent);
 			return true;
 
 		case R.id.action_serverparams:
 			Log.d(TAG, "onOptionsItemSelected(): Getting new server params...");
 			// Build a dialog
 			final AlertDialog.Builder serverParamsDialog = new AlertDialog.Builder(BotControl.this);
 			serverParamsDialog.setTitle("Connect to server");
 			serverParamsDialog.setMessage("IP address:");
 
 			// Set an EditText view to get user input 
 			final EditText txtServerHost = new EditText(BotControl.this);
 			txtServerHost.setText(serverHost);
 			serverParamsDialog.setView(txtServerHost);
 
 			// Set button actions
 			serverParamsDialog.setPositiveButton("Connect", new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int whichButton) {
 					String newServerHost = txtServerHost.getText().toString();
 					Log.d(TAG, "onOptionsItemSelected(): [serverParamsDialog] New server host: " + newServerHost);
 					if (!newServerHost.equals(serverHost)) {
 						// TODO Improve IP validation (current regex doesn't constrain the range of octet numbers); allow hostnames as well?
 						if(Pattern.matches("[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+", newServerHost)) {
 							setServerHost(newServerHost);
 						}
 						else {
 							Log.w(TAG, "onOptionsItemSelected(): [serverParamsDialog] Ignoring invalid server host IP: " + newServerHost);
 						}
 					}
 				}
 			});
 			serverParamsDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int whichButton) {
 					dialog.cancel();
 				}
 			});
 
 			// Show dialog
 			serverParamsDialog.show();
 
 			// TODO Create ServerParams activity instead of dialog
 			//Intent serverParamsIntent = new Intent(this, ServerParams.class);
 			//startActivity(serverParamsIntent);
 
 			return true;
 		
 		case R.id.action_toggle_ir:
 			irReadBinary = !irReadBinary;
 			Log.d(TAG, "onOptionsItemSelected(): IR toggled to " + (irReadBinary ? "binary" : "value") + " mode");
 			txtConsole.append("[IR] Mode = " + (irReadBinary ? "binary" : "value") + "\n");
 			break;
 		
 		case R.id.action_estop:
 			Log.d(TAG, "onOptionsItemSelected(): Sending E-Stop...");
 			txtConsole.append("[E-Stop]\n");
 			doEStop(true);
 			forward = forwardRange.zero;
 			strafe = strafeRange.zero;
 			return true;
 		
 		case R.id.action_killserver:
 			// Build a confirmation dialog
 			final AlertDialog.Builder killServerDialog = new AlertDialog.Builder(BotControl.this);
 			killServerDialog.setTitle("Kill server");
 			killServerDialog.setMessage("Are you sure you want to kill the server instance?");
 
 			// Set button actions
 			killServerDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int whichButton) {
 					Log.d(TAG, "onOptionsItemSelected(): [killServerDialog] Killing server...");
 					doKillServer(false); // ok to drop (try again later?)
 				}
 			});
 			killServerDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int whichButton) {
 					dialog.cancel();
 				}
 			});
 
 			// Show dialog
 			killServerDialog.show();
 			return true;
 
 		case R.id.action_settings:
 			Log.w(TAG, "onOptionsItemSelected(): Settings not implemented");
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	private void savePreferences() {
 		// Save current preferences
 		SharedPreferences prefs = getSharedPreferences(TAG, MODE_PRIVATE); // get preferences
 		SharedPreferences.Editor editor = prefs.edit(); // get an editor on those preferences
 		
 		// * Server host
 		editor.putString("server_host", serverHost); // 
 		
 		editor.commit(); // commit preference edits
 		Log.d(TAG, "Preferences saved");
 	}
 	
 	private void loadPreferences() {
 		// Load preferences, with appropriate defaults
 		SharedPreferences prefs = getSharedPreferences(TAG, MODE_PRIVATE);
 		
 		// * Server host
 		serverHost = prefs.getString("server_host", serverHost); // fetch from prefs, default to any value already initialized
 		if (serverHost == null)
 			serverHost = getResources().getString(R.string.server_host); // if not in prefs and no default, get from resources
 		
 		Log.d(TAG, "Preferences loaded");
 	}
 	
 	private void setServerHost(String serverHost) {
 		Log.d(TAG, "setServerHost(): Resetting client threads...");
 		stopSubscriber();
 		stopClient();
 		this.serverHost = serverHost;
 		savePreferences(); // make new server host persist
 		startClient();
 		startSubscriber();
 	}
 
 	private void startClient() {
 		stopClient(); // stop previously running client thread, if any
 		Log.d(TAG, "startClient(): Starting client thread...");
 		clientThread = new ZMQClientThread(serverProtocol, serverHost, serverPort);
 		clientThread.start();
 	}
 
 	private void stopClient() {
 		if (clientThread != null) {
 			Log.d(TAG, "stopClient(): Stopping client thread...");
 			clientThread.term();
 			clientThread = null;
 		}
 	}
 
 	private void startSubscriber() {
 		stopSubscriber();
 		Log.d(TAG, "startSubscriber(): Starting subscriber thread...");
 		subscriberThread = new ZMQSubscriberThread(serverProtocol, serverHost, pubServerPort);
 		subscriberThread.setTopics(subscriptionTopics);
 		subscriberThread.setListener(this);
 		subscriberThread.start();
 	}
 
 	private void stopSubscriber() {
 		if (subscriberThread != null) {
 			Log.d(TAG, "stopSubscriber(): Stopping subscriber thread...");
 			subscriberThread.term();
 			subscriberThread = null;
 		}
 	}
 	
 	private void startDataThread() {
 		stopDataThread();
 		Log.d(TAG, "startDataThread(): Starting data read thread...");
 		dataThread = new Thread() {
 			public void run() {
 				if (clientThread != null) {
 					while (true) {
 						try {
 							doIRRead(false); // don't block, may cause continuous stream of failed attempts
 							// TODO Check if a lot of continuous reads have failed, then break out
 							sleep(dataInterval);
 						} catch (InterruptedException e) {
 							Log.d(TAG, "dataThread: Interrupted! Exiting data read thread...");
 							break;
 						}
 					}
 				}
 			}
 		};
 		dataThread.start();
 	}
 	
 	private void stopDataThread() {
 		if (dataThread != null) {
 			Log.d(TAG, "stopDataThread(): Stopping data read thread...");
 			dataThread.interrupt();
 			dataThread = null;
 		}
 	}
 
 	@Override
 	public boolean onJoystickEvent(TouchJoystick joystick, int action, float x, float y) {
 		if (joystick == driveJoystick) {
 			switch(action) {
 			case MotionEvent.ACTION_DOWN:
 				//Log.d(TAG, "onJoystickEvent(): [drive/ACTION_DOWN] @ (" + x + ", " + y + ")");
 				driveJoystick.updateKnob(x, y);
 				// NOTE Don't produce movement command here, only on ACTION_MOVE, in case this is just a tap
 				return true;
 
 			case MotionEvent.ACTION_MOVE:
 				//Log.d(TAG, "onJoystickEvent(): [drive/ACTION_MOVE] @ (" + x + ", " + y + ")");
 				driveJoystick.updateKnob(x, y);
 
 				// Compute control inputs
 				forward = forwardRange.fromNormalizedInput(-driveJoystick.knobYNorm); // NOTE Y-flip
 				strafe  = strafeRange.fromNormalizedInput(driveJoystick.knobXNorm);
 				//Log.d(TAG, "onJoystickEvent(): [drive/ACTION_MOVE] forward = " + forward + ", strafe = " + strafe + ", turn = " + turn);
 
 				doDrive(false); // ok to drop
 				return true;
 
 			case MotionEvent.ACTION_UP:
 				//Log.d(TAG, "onJoystickEvent(): [drive/ACTION_UP] @ (" + x + ", " + y + ")");
 				forward = forwardRange.zero;
 				strafe = strafeRange.zero;
 				turn = turnRange.zero;
 				driveJoystick.updateKnob(forward, strafe); // spring back to neutral
 				doDrive(true); // NOT ok to drop
 				return true;
 			}
 		}
 		else if (joystick == turretJoystick) {
 			switch(action) {
 			case MotionEvent.ACTION_DOWN:
 				turretJoystick.updateKnob(x, y);
 				return true;
 
 			case MotionEvent.ACTION_MOVE:
 				turretJoystick.updateKnob(x, y);
 
 				// Compute control inputs
 				pitch = pitchRange.fromNormalizedInput(turretJoystick.knobYNorm);
 				yaw = yawRange.fromNormalizedInput(turretJoystick.knobXNorm);
 				//Log.d(TAG, "onJoystickEvent(): [turret/ACTION_MOVE] pitch = " + pitch + ", yaw = " + yaw);
 
 				doTurret(false); // ok to drop
 				return true;
 
 			case MotionEvent.ACTION_UP:
 				// NOP
 				return true;
 			}
 		}
 		return false;
 	}
 
 	private void doPing(final boolean block) {
 		// Ping the control server, get response time in milliseconds
 		txtConsole.append("[PING] Sending...\n"); // should be on UI thread
 		final long startTime = System.currentTimeMillis();
 		sendCommand(
 			pingCmdObj,
 			block,
 			new CommandReplyCallback() {
 				@Override
 				public void onReply(final String reply) {
 					// Parse JSON reply and update if valid response (TODO and result contained in reply?)
 					final long responseTime = (System.currentTimeMillis() - startTime);
 					if (reply == null)
 						return;
 					
 					try {
 						JSONObject replyObj = new JSONObject(reply);
 						if (replyObj == null || !replyObj.has("type") || !replyObj.getString("type").equals("ping_reply"))
 							return;
 						
 						// Update UI to show ping success and responseTime
 						runOnUiThread(new Runnable() {
 							public void run() {
 								// Update ping state
 								pingOkay = true;
 								btnPing.setBackgroundColor(okayColor);
 								txtConsole.append("[PING] Received (" + responseTime + " ms).\n");
 								Toast.makeText(BotControl.this, "Ping: " + responseTime + " ms", Toast.LENGTH_SHORT).show();
 							}
 						});
 					} catch (JSONException e) {
 						Log.e(TAG, "Error parsing JSON ping reply: " + e);
 					}
 				}
 			}
 		);
 	}
 	
 	private void doDrive(final boolean block) {
 		// Generate and send drive command, if different from last
 		if (forward != lastForward || strafe != lastStrafe || turn != lastTurn) {
 			setCallReqParam(driveCmdObj, "forward", forward);
 			setCallReqParam(driveCmdObj, "strafe", strafe);
 			//setCallReqParam(driveCmdObj, "turn", turn); // TODO turn currently not used
 
 			sendCommand(
 				driveCmdObj,
 				block,
 				new CommandReplyCallback() {
 					@Override
 					public void onReply(final String reply) {
 						// Parse JSON reply and update if valid response (TODO and result contained in reply?)
 						if (reply != null && parseCallReply(reply) != null) {
 							// Update drive state and views
 							lastForward = forward;
 							lastStrafe = strafe;
 							lastTurn = turn;
 							runOnUiThread(new Runnable() {
 								public void run() {
 									updateDriveViews();
 								}
 							});
 						}
 						else {
 							// Restore last drive state
 							forward = lastForward;
 							strafe = lastStrafe;
 							turn = lastTurn;
 						}
 					}
 				}
 			);
 		}
 	}
 
 	private void doTurret(final boolean block) {
 		// Generate and send turret command, if different from last
 		if ( yaw != lastYaw || pitch != lastPitch) {
 			setCallReqParam(turretCmdObj, "yaw", yaw);
 			setCallReqParam(turretCmdObj, "pitch", pitch);
 
 			sendCommand(
 				turretCmdObj,
 				block,
 				new CommandReplyCallback() {
 					@Override
 					public void onReply(final String reply) {
 						// Parse JSON reply and update if valid response (TODO and result contained in reply?)
 						if (reply != null && parseCallReply(reply) != null) {
 							// Update turret state and views
 							lastPitch = pitch;
 							lastYaw = yaw;
 							runOnUiThread(new Runnable() {
 								public void run() {
 									updateTurretViews();
 								}
 							});
 						}
 						else {
 							// Restore last turret state
 							pitch = lastPitch;
 							yaw = lastYaw;
 						}
 					}
 				}
 			);
 		}
 	}
 
 	private void doLaser(final boolean block) {
 		// Generate and send laser ON/OFF command
 		setCallReqParam(laserCmdObj, "state", laser);
 		sendCommand(
 			laserCmdObj,
 			block,
 			new CommandReplyCallback() {
 				@Override
 				public void onReply(final String reply) {
 					// Parse JSON reply and update if valid response (TODO and result contained in reply?)
 					if (reply != null && parseCallReply(reply) != null) {
 						// Update laser state
 						lastLaser = laser;
 					}
 					else {
 						runOnUiThread(new Runnable() {
 							public void run() {
 								// Restore last laser state and toggle button
 								laser = lastLaser;
 								btnLaser.setChecked(laser == 1);
 							}
 						});
 					}
 				}
 			}
 		);
 	}
 
 	private void doSpin(final boolean block) {
 		// Generate and send gun motor spin ON/OFF command
 		sendCommand(
			(spin == 1 ? spinUpCmdObj : spinDownCmdObj),
 			block,
 			new CommandReplyCallback() {
 				@Override
 				public void onReply(final String reply) {
 					// Parse JSON reply and update if valid response (TODO and result contained in reply?)
 					if (reply != null && parseCallReply(reply) != null) {
 						// Update spin state
 						lastSpin = spin;
 					}
 					else {
 						runOnUiThread(new Runnable() {
 							public void run() {
 								// Restore last spin state and toggle button
 								spin = lastSpin;
 								btnSpin.setChecked(spin == 1);
 							}
 						});
 					}
 				}
 			}
 		);
 	}
 
 	private void doFire(final boolean block) {
 		// Generate and send fire command
 		sendCommand(fireCmdObj, block, null);
 	}
 	
 	private void doIRRead(final boolean block) {
 		// Read IR sensor data
 		sendCommand(
 			(irReadBinary ? irBinaryCmdObj : irCmdObj),
 			block,
 			new CommandReplyCallback() {
 				@Override
 				public void onReply(final String reply) {
 					// Parse JSON reply and update if valid response (TODO and result contained in reply?)
 					if (reply == null)
 						return;
 					
 					try {
 						JSONObject replyObj = parseCallReply(reply);
 						if (replyObj == null || !replyObj.has("call_return"))
 							return;
 						
 						// Extract IR data and update UI
 						JSONObject dataObj = replyObj.getJSONObject("call_return");
 						final double time = (irReadBinary ? -1.0 : dataObj.getDouble("time"));
 						final boolean fresh = (irReadBinary ? true : dataObj.getBoolean("fresh"));
 						final JSONObject readings = (irReadBinary ? dataObj : dataObj.getJSONObject("readings"));
 						//Log.d(TAG, "[IR] time: " + time + ", fresh: " + fresh + ", len(readings): " + readings.length());
 						runOnUiThread(new Runnable() {
 							public void run() {
 								// Update UI
 								if (txtConsole.length() > maxConsoleLength)
 									txtConsole.setText(""); // clear if full
 								txtConsole.append("[IR] time: " + time + ", fresh: " + fresh + ", len(readings): " + readings.length() + "\n");
 								Iterator<?> names = readings.keys();
 								while (names.hasNext()) {
 									String name = (String) names.next();
 									try {
 										JSONArray reading = readings.getJSONArray(name);
 										txtConsole.append("[IR] " + name +": " + reading.toString() + "\n");
 									} catch (JSONException e) {
 										// Something went wrong!
 									}
 								}
 								
 							}
 						});
 					} catch (JSONException e) {
 						Log.e(TAG, "Error parsing JSON IR reply: " + e);
 					}
 				}
 			}
 		);
 	}
 
 	private void doEStop(final boolean block) {
 		// Generate and send E-Stop command to stop the bot
 		sendCommand(estopCmdObj, block, null);
 	}
 	
 	private void doKillServer(final boolean block) {
 		// Generate and send kill command to stop server
 		sendCommand(exitCmdObj, block, null);
 	}
 
 	public interface CommandReplyCallback {
 		public void onReply(final String reply);
 	}
 
 	private void sendCommand(final String cmdStr, final boolean block, final CommandReplyCallback callback) {
 		//Log.d(TAG, "sendCommand(): forward = " + forward + ", strafe = " + strafe + ", turn = " + turn);
 		// Send this command (JSON string) to the control server,
 		//   wait for ACK, deal with concurrency issues
 		if (clientThread != null && clientThread.isAlive()) {
 			// Start a new thread to avoid blocking the main (UI) thread
 			(new Thread() {
 				public void run() {
 					//Log.d(TAG, "Sending : " + cmdStr);
 					String reply = clientThread.serviceRequestSync(cmdStr, block);
 					//Log.d(TAG, "Received: " + reply);
 					if (callback != null) {
 						callback.onReply(reply);
 					}
 				}
 			}).start();
 		}
 	}
 
 	private void sendCommand(final JSONObject cmdObj, final boolean block, final CommandReplyCallback callback) {
 		try {
 			sendCommand(cmdObj.toString(), block, callback);
 		}
 		catch(NullPointerException e) {
 			Log.e(TAG, "Invalid command map: " + e);
 		}
 	}
 
 	private JSONObject makeCallReq(String obj_name, String method, String[] params, Object[] values) {
 		// NOTE It is easier to specify params and values in separate arrays than as a list of <key, value> pairs
 		try {
 			JSONObject cmdObj = (new JSONObject())
 					.put("type", "call_req")
 					.put("obj_name", obj_name)
 					.put("method", method)
 					.put("params", new JSONObject());
 			if (params != null && params.length > 0 && values != null && values.length == params.length) {
 				JSONObject paramsObj = cmdObj.getJSONObject("params");
 				for (int i = 0; i < params.length; i++) {
 					paramsObj.put(params[i], values[i]);
 				}
 			}
 			return cmdObj;
 		} catch (JSONException e) {
 			Log.e(TAG, "Error making JSON call_req object: " + e);
 			return null;
 		}
 	}
 
 	private JSONObject parseCallReply(String reply) {
 		try {
 			JSONObject replyObj = new JSONObject(reply);
 			if (replyObj.getString("type").equals("call_reply"))
 				return replyObj;
 			else {
 				Log.w(TAG, "Call reply type not favorable: " + replyObj.getString("type"));
 				return null;
 			}
 		} catch (JSONException e) {
 			Log.e(TAG, "Error parsing JSON reply: " + e);
 			return null;
 		} catch (NullPointerException e) {
 			Log.e(TAG, "Error parsing JSON reply: " + e);
 			return null;
 		}
 	}
 
 	private JSONObject makePingReq() {
 		try {
 			JSONObject cmdObj = (new JSONObject()).put("type", "ping_req");
 			return cmdObj;
 		} catch (JSONException e) {
 			Log.e(TAG, "Error making JSON ping_req object: " + e);
 			return null;
 		}
 	}
 
 	private JSONObject makeExitReq() {
 		try {
 			JSONObject cmdObj = (new JSONObject()).put("type", "exit_req");
 			return cmdObj;
 		} catch (JSONException e) {
 			Log.e(TAG, "Error making JSON exit_req object: " + e);
 			return null;
 		}
 	}
 
 	private void setCallReqParam(JSONObject cmdObj, String param, Object value) {
 		try {
 			JSONObject paramsObj = cmdObj.getJSONObject("params");
 			paramsObj.put(param, value);
 		} catch (JSONException e) {
 			Log.e(TAG, "Error setting call_req param (silent error): " + e);
 		}
 	}
 
 	@Override
 	public void onMessage(String message) {
 		String messageParts[] = message.split("\\s", 2); // split message into 2 parts on first whitespace
 		if (messageParts.length < 2) {
 			Log.w(TAG, "onMessage(): Ignoring invalid message: \"" + message + "\"");
 			return;
 		}
 
 		String topic = messageParts[0].trim();
 		String data = messageParts[1].trim();
 		try {
 			// Handle IR array updates
 			if (topic.startsWith("ir")) {
 				Log.d(TAG, "onMessage(): IR update:- topic: " + topic + ", data: " + data);
 				// TODO parse IR array values from message and update a representative view
 				/*
 				JsonReader myReader = new JsonReader(new StringReader(data));
 				runOnUiThread(new Runnable() {
 					public void run() {
 						updateIRViews();
 					};
 				});
 				 */
 			}
 		}
 		catch (NumberFormatException e) {
 			Log.w(TAG, "onMessage(): [topic=" + topic + "] Ignoring invalid data: \"" + data + "\"");
 		}
 	}
 
 	private void updateDriveViews() {
 		txtForward.setText(String.format("%7.2f", forward));
 		txtStrafe.setText(String.format("%7.2f", strafe));
 	}
 
 	private void updateTurretViews() {
 		txtPitch.setText(String.format("%7.2f", pitch));
 		txtYaw.setText(String.format("%7.2f", yaw));
 	}
 
 }

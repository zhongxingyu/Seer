 package domobi.IOIO.heli;
 
 //import ioio.lib.api.DigitalOutput;
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.util.Hashtable;
 import java.util.Timer;
 import java.util.TimerTask;
 import org.json.JSONObject;
 import ioio.lib.api.PwmOutput;
 import ioio.lib.api.TwiMaster;
 import ioio.lib.api.exception.ConnectionLostException;
 import ioio.lib.util.BaseIOIOLooper;
 import ioio.lib.util.IOIOLooper;
 import ioio.lib.util.android.IOIOActivity;
 import android.hardware.SensorManager;
 import android.os.Bundle;
 import android.widget.SeekBar;
 import android.widget.ToggleButton;
 import android.widget.TextView;
 import android.widget.Button;
 import com.pubnub.api.Callback;
 import com.pubnub.api.Pubnub;
 
 /**
  * This is the main activity of the HelloIOIO example application.
  * 
  * It displays a toggle button on the screen, which enables control of the
  * on-board LED. This example shows a very simple usage of the IOIO, by using
  * the {@link IOIOActivity} class. For a more advanced use case, see the
  * HelloIOIOPower example.
  */
 public class MainActivity extends IOIOActivity {
 	private ToggleButton toggleMainLift_;
 	private ToggleButton toggleEnableGui_;
 	private TextView textRotationTrim_;
 	private TextView textTailTrim_;
 	private TextView textMainLift_;
 	private SeekBar seekRotationTrim_;
 	private SeekBar seekTailTrim_;
 	private SeekBar seekMainLift_;
 	private Button btnLeft_;
 	private Button btnRight_;
 	private Button btnTailUp_;
 	private Button btnTailDown_;
 
 	// TODO
 	// pubnub javascript/browser interface
 
 	// Also pubnub updated to interact with Autopilot/AssistedFlyer
 	// (different channels)
 	// FlightPlanner(AssistedFlyer,Waypoint[])
 	// waypoint is Location/State(Land...etc)
 	// AssistedFlyer(Helicopter,Autopilot)
 
 	// Video (capture,streaming);
 
 	private SensorManager sM;
 	private Odometry odom;
 	private Autopilot autopilot;
 	private TwiMaster twi;
 
 	private Helicopter heli = new Helicopter();
 	private Helicopter test = new Helicopter();
 	// API keys here, uncomment for Demo Keys
 	// private apiConfigTemplate config = nre apiConfigTemplate();
 
 	// Comment out if using demo keys
 	private apiConfig config = new apiConfig();
 
 	private Pubnub pubnub = config.pubnub;
 	Timer timer = new Timer();
 
 	class PubnubCallback extends Callback {
 
 		public void successCallback(String channel, Object message) {
 			// call update method
 			try {
 				// setText(message.toString(), textMainLift_);
 				JSONObject setpoint = new JSONObject(message.toString());
 
 				JSONObject lastError = new JSONObject("{\"PitchErr\":"
 						+ Float.toString(autopilot.pitchError)
 						+ ",\"PitchCorr\":"
 						+ Float.toString(autopilot.kPt * autopilot.uPt
 								+ autopilot.kIt * autopilot.uIt + autopilot.kDt
 								* autopilot.uDt) + "}");
 				String Pubchannel = "error_debug";
 				pubnub.publish(Pubchannel, lastError, new Callback() {
 				});
 
 				autopilot.setOrientation(setpoint);
 				heli.setConnection(true);
 
				setNumber(autopilot.getSetPoint().mainPwr, textMainLift_);
				setNumber(autopilot.getSetPoint().yawVel, textRotationTrim_);
				setNumber(autopilot.getSetPoint().pitch, textTailTrim_);
 
 				// setNumber(autopilot.getSetPoint().mainPwr, textMainLift_);
 			} catch (Exception e) {
 				setText(e.toString(), textMainLift_);
 			}
 		}
 
 	}
 
 	class CheckForHeartbeat extends TimerTask {
 		@Override
 		public void run() {
 			if (heli.connectionBeat()) {
 				heli.setConnection(false);
 			} else {
 				heli.setMainPwr((float) (heli.getMainPwr() * 0.8));
 			}
 		}
 	}
 
 	/**
 	 * Called when the activity is first created. Here we normally initialize
 	 * our GUI.
 	 */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		/* GUI */
 		setContentView(R.layout.main);
 		toggleEnableGui_ = (ToggleButton) findViewById(R.id.toggleEnableGui);
 
 		toggleMainLift_ = (ToggleButton) findViewById(R.id.toggleMainLift);
 		textMainLift_ = (TextView) findViewById(R.id.textMainLift);
 		seekMainLift_ = (SeekBar) findViewById(R.id.seekMainLift);
 
 		seekRotationTrim_ = (SeekBar) findViewById(R.id.seekRotationTrim);
 		seekRotationTrim_.setProgress(50);
 		textRotationTrim_ = (TextView) findViewById(R.id.textRotationTrim);
 
 		seekTailTrim_ = (SeekBar) findViewById(R.id.seekTailTrim);
 		seekTailTrim_.setProgress(50);
 		textTailTrim_ = (TextView) findViewById(R.id.textTailTrim);
 
 		btnLeft_ = (Button) findViewById(R.id.btnLeft);
 		btnRight_ = (Button) findViewById(R.id.btnRight);
 		btnTailUp_ = (Button) findViewById(R.id.btnTailUp);
 		btnTailDown_ = (Button) findViewById(R.id.btnTailDown);
 
 		/* Pubnub Subscription and Callback */
 		String subChannel = "autopilot_channel";
 		Hashtable args = new Hashtable(1);
 		args.put("channel", subChannel);
 		Callback pubnubCallback = new PubnubCallback();
 		try {
 			pubnub.subscribe(args, pubnubCallback);
 		} catch (Exception e) {
 			setText("ERROR on Subscribe", textMainLift_);
 		}
 
 		// Pubnub Lost Connection Timer (connection dropped from pubnub
 		// Handler?)
 		TimerTask checkHeartbeat = new CheckForHeartbeat();
 		timer.scheduleAtFixedRate(checkHeartbeat, 0, 2500);
 
 		// Odometry
 		sM = (SensorManager) getSystemService(getBaseContext().SENSOR_SERVICE);
 		odom = new Odometry(sM);
 		autopilot = new Autopilot();
 	}
 
 	/**
 	 * This is the thread on which all the IOIO activity happens. It will be run
 	 * every time the application is resumed and aborted when it is paused. The
 	 * method setup() will be called right after a connection with the IOIO has
 	 * been established (which might happen several times!). Then, loop() will
 	 * be called repetitively until the IOIO gets disconnected.
 	 */
 	class Looper extends BaseIOIOLooper {
 
 		/** The on-board LED. */
 		// private DigitalOutput led_;
 		private PwmOutput pwmTailUp;
 		private PwmOutput pwmTailDown;
 		private PwmOutput pwmRotor1;
 		private PwmOutput pwmRotor2;
 
 		private float tailUpPwr;
 		private float tailDownPwr;
 		private float rotor1Pwr;
 		private float rotor2Pwr;
 
 		private float tailMax = 0.6f;
 		private float tailMin = 0.05f;
 		private float mainMax = 0.7f;
 		private float mainMin = 0.05f;
 
 		/**
 		 * Called every time a connection with IOIO has been established.
 		 * Typically used to open pins.
 		 * 
 		 * @throws ConnectionLostException
 		 *             When IOIO connection is lost.
 		 * 
 		 * @see ioio.lib.util.AbstractIOIOActivity.IOIOThread#setup()
 		 */
 		@Override
 		protected void setup() throws ConnectionLostException {
 
 			pwmTailUp = ioio_.openPwmOutput(14, 100);
 			pwmTailDown = ioio_.openPwmOutput(13, 100);
 			pwmRotor1 = ioio_.openPwmOutput(12, 100);
 			pwmRotor2 = ioio_.openPwmOutput(11, 100);
 
 			twi = ioio_.openTwiMaster(0, TwiMaster.Rate.RATE_100KHz, false);
 			odom.setTwi(twi);
 
 			autopilot.setOrientation(odom.getOrientation().pitch, 0.0f, 0.0f);
 		}
 
 		/**
 		 * Called repetitively while the IOIO is connected.
 		 * 
 		 * @throws ConnectionLostException
 		 *             When IOIO connection is lost.
 		 * @throws InterruptedException
 		 * 
 		 * @see ioio.lib.util.AbstractIOIOActivity.IOIOThread#loop()
 		 */
 		@Override
 		public void loop() throws ConnectionLostException, InterruptedException {
 
 			/* Use Gui to Set Helicopter State (if enabled) */
 			if (toggleEnableGui_.isChecked()) {
 				UpdateHelicopterStatefromGui();
 				heli.setConnection(true);
 			} else {
 				/* Update Helicopter State on GUI */
 				heli = autopilot.getNextState(odom);
				setNumber(autopilot.rotPower, textMainLift_);
				setNumber(autopilot.kDr, textRotationTrim_);
				setNumber(autopilot.getSetPoint().mainPwr, textTailTrim_);
 
 				// autopilot.kDt = (10f*(float)seekMainLift_.getProgress())/
 				// ((float)seekMainLift_.getMax());
 				/*
 				 * autopilot.kIt = ((float)seekRotationTrim_.getProgress())/
 				 * ((float)seekRotationTrim_.getMax()); autopilot.kDt =
 				 * ((float)seekTailTrim_.getProgress())/
 				 * ((float)seekTailTrim_.getMax());
 				 */
 			}
 
 			/* Take the Helicopter State and Apply to IOIO */
 			UpdateIOIOfromHelicopterState(heli);
 			pwmTailUp.setDutyCycle(tailUpPwr);
 			pwmTailDown.setDutyCycle(tailDownPwr);
 			pwmRotor1.setDutyCycle(rotor1Pwr);
 			pwmRotor2.setDutyCycle(rotor2Pwr);
 
 			try {
 				Thread.sleep(50);
 			} catch (InterruptedException e) {
 				// no rest for the wicked
 			}
 		}
 
 		private void UpdateHelicopterStatefromGui() {
 			if (toggleMainLift_.isChecked()) {
 				heli.setMainPwr(((float) seekMainLift_.getProgress())
 						/ ((float) seekMainLift_.getMax()));
 				setNumber(heli.getMainPwr(), textMainLift_);
 			} else {
 				heli.setMainPwr(0.0f);
 				setNumber(0.0f, textMainLift_);
 			}
 
 			if (btnTailUp_.isPressed() && !btnTailDown_.isPressed()) {
 				heli.setTailPwr(0.5f);
 			} else if (btnTailDown_.isPressed() && !btnTailUp_.isPressed()) {
 				heli.setTailPwr(-0.5f);
 			} else {
 				heli.setTailPwr(0.0f);
 			}
 
 			if (btnLeft_.isPressed() && !btnRight_.isPressed()) {
 				heli.setRotationPwr(0.2f);
 			} else if (btnRight_.isPressed() && !btnLeft_.isPressed()) {
 				heli.setRotationPwr(-0.2f);
 			}
 
 			heli.setTailTrim(((float) seekTailTrim_.getProgress())
 					/ ((float) seekTailTrim_.getMax()) * 2f - 1f);
 			setNumber(heli.getTailTrim(), textTailTrim_);
 
 			heli.setRotationTrim(((float) seekRotationTrim_.getProgress())
 					/ ((float) seekRotationTrim_.getMax()) * 2f - 1f);
 			setNumber(heli.getRotationTrim(), textRotationTrim_);
 		}
 
 		private void UpdateIOIOfromHelicopterState(Helicopter helicopter) {
 
 			float tailPwrTrimmed = helicopter.getTailPwr()
 					+ helicopter.getTailTrim();
 			float mainRotationTrimmed = helicopter.getRotationPwr()
 					+ helicopter.getRotationTrim();
 
 			/* Clip if Over Max Ranges */
 			if (tailPwrTrimmed > tailMax) {
 				tailPwrTrimmed = tailMax;
 			}
 			if (tailPwrTrimmed < -tailMax) {
 				tailPwrTrimmed = -tailMax;
 			}
 			if (mainRotationTrimmed > mainMax) {
 				mainRotationTrimmed = mainMax;
 			}
 			if (mainRotationTrimmed < -mainMax) {
 				mainRotationTrimmed = -mainMax;
 			}
 
 			/* Tail Logic */
 			if (tailPwrTrimmed >= 0) {
 				tailUpPwr = tailPwrTrimmed;
 				tailDownPwr = 0.0f;
 			} else if (tailPwrTrimmed < 0) {
 				tailUpPwr = 0.0f;
 				tailDownPwr = -tailPwrTrimmed;
 			}
 
 			/* Main Rotors */
 			if (mainRotationTrimmed >= 0) {
 				rotor1Pwr = helicopter.getMainPwr();
 				rotor2Pwr = helicopter.getMainPwr() * (1 - mainRotationTrimmed);
 			}
 			if (mainRotationTrimmed < 0) {
 				rotor1Pwr = helicopter.getMainPwr() * (1 + mainRotationTrimmed);
 				rotor2Pwr = helicopter.getMainPwr();
 			}
 
 			// Set pwr to zero if below Motor startup friction
 			if (tailUpPwr < tailMin) {
 				tailUpPwr = 0.0f;
 			}
 			if (tailDownPwr < tailMin) {
 				tailDownPwr = 0.0f;
 			}
 			if (rotor1Pwr < mainMin) {
 				rotor1Pwr = 0.0f;
 			}
 			if (rotor2Pwr < mainMin) {
 				rotor2Pwr = 0.0f;
 			}
 		}
 
 	}
 
 	/**
 	 * A method to create our IOIO thread.
 	 * 
 	 * @see ioio.lib.util.AbstractIOIOActivity#createIOIOThread()
 	 */
 	@Override
 	protected IOIOLooper createIOIOLooper() {
 		return new Looper();
 	}
 
 	private void setNumber(float f, final TextView textBox) {
 		final String str = String.format("%.2f", f);
 		runOnUiThread(new Runnable() {
 			@Override
 			public void run() {
 				textBox.setText(str);
 			}
 		});
 	}
 
 	private void setText(final String str, final TextView textBox) {
 		runOnUiThread(new Runnable() {
 			@Override
 			public void run() {
 				textBox.setText(str);
 			}
 		});
 	}
 
 }

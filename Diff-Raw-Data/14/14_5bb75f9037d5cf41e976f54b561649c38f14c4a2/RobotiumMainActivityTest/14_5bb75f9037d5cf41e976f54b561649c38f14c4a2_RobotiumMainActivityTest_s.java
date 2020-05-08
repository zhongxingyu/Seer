 package net.geekzy.mobile.brewclock.test;
 
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.TimeUnit;
 
 import net.geekzy.mobile.brewclock.R;
 import net.geekzy.mobile.brewclock.activities.MainActivity;
 import android.database.Cursor;
 import android.test.ActivityInstrumentationTestCase2;
import android.view.KeyEvent;
 import android.widget.Button;
 import android.widget.Spinner;
 import android.widget.TextView;
 
 import com.jayway.android.robotium.solo.Solo;
 
 public class RobotiumMainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {
 	protected final CountDownLatch signal = new CountDownLatch(1);
 	protected Solo solo;
 
 	/** Constants **/
 	private static final String INITIAL_PROFILE_SELECTION = "Earl Grey";
 	private static final String INITIAL_PROFILE_TIME = "3m";
 	private static final String ONE_MINUTE = "1m";
 	private static final String INIT_BREW_COUNT = "0";
 	private static final String BREW_COUNT_ONE = "1";
 	private static final String BUTTON_STOP = "Stop";
 	private static final String BUTTON_START = "Start";
 	private static final String BUTTON_MINUS = "-";
 
 	/** Properties **/
 	private Spinner mSpinnerPorfile;
 	private TextView mTextViewTime;
 	private TextView mTextViewCount;
 	private Button mButtonStart;
 
 	public RobotiumMainActivityTest(String name) {
 		super(MainActivity.class);
 		setName(name);
 	}
 
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		solo = new Solo(getInstrumentation(), getActivity());
 
 		// Connect interface elements to properties
 		mSpinnerPorfile = (Spinner) solo.getView(R.id.tea_spinner);
 		mTextViewTime = (TextView) solo.getView(R.id.brew_time);
 		mTextViewCount = (TextView) solo.getView(R.id.brew_count_label);
 		mButtonStart = (Button) solo.getView(R.id.brew_start);
 	}
 
 	public void testInitAndStart() throws Throwable {
 		// get selected spinner item as a Cursor
 		Cursor cursor = (Cursor) mSpinnerPorfile.getSelectedItem();
 		String mProfile = cursor.getString(1);
 		String mTimeInit = mTextViewTime.getText().toString();
 
 		// check selected profile name and value
 		assertEquals(INITIAL_PROFILE_SELECTION, mProfile);
 		assertEquals(INITIAL_PROFILE_TIME, mTimeInit);
 
 		// set to 1m
 		solo.clickOnButton(BUTTON_MINUS);
 		solo.clickOnButton(BUTTON_MINUS);
 
 		// check modified brewing time
 		String mTimeOne = mTextViewTime.getText().toString();
 		assertEquals(ONE_MINUTE, mTimeOne);
 
 		// start brew
 		solo.clickOnButton(BUTTON_START);
 
 		signal.await(61, TimeUnit.SECONDS); // delay while brewing
 
 		// check brew count label after completed brewing
 		String mCount = mTextViewCount.getText().toString();
 		assertEquals(BREW_COUNT_ONE, mCount);
 	}
 
 	public void testPickProfile() throws Throwable {
		solo.pressSpinnerItem(0, 0);

 		// scroll to index 2
		sendKeys(KeyEvent.KEYCODE_DPAD_CENTER);
		sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
		sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
		sendKeys(KeyEvent.KEYCODE_DPAD_CENTER);
 
 		// check selected time
 		String mTime = mTextViewTime.getText().toString();
 		assertEquals(ONE_MINUTE, mTime);
 
 		// start brew
 		solo.clickOnButton(BUTTON_START);
 		// check button start label should be switched to stop
 		String mStop = (String) mButtonStart.getText();
 		assertEquals(BUTTON_STOP, mStop);
 
		signal.await(10, TimeUnit.SECONDS); // delay while brewing
 
 		// stop brew
 		solo.clickOnButton(BUTTON_STOP);
 		// check button stop label should be switched back to start
 		String mStart = (String) mButtonStart.getText();
 		assertEquals(BUTTON_START, mStart);
 
 		// check brew count
 		String mCount = (String) mTextViewCount.getText();
 		assertEquals(INIT_BREW_COUNT, mCount);
 	}
 
 	@Override
 	protected void tearDown() throws Exception{
 		solo.finishOpenedActivities();
 	}
 }

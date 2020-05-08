 package winning.pwnies.recreativity;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 
 public class PlayActivity extends FragmentActivity {
 	static final int NUM_ITEMS = 2;
 
 	PlayAdapter mAdapter;
 
 	ViewPager mPager;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.play_activity);
 		Intent intent = getIntent();
 		Submission s = null;
 		Bundle b = intent.getExtras();
 		if (b != null) {
 			Flow f = b.getParcelable(Data.FLOW);
 			s = f.get(f.size() - 1);
 		}
 		if (s == null) {
 			s = Data.getFlow(1).get(Data.getFlow(1).size() - 1);
 		}
 
 		mAdapter = new PlayAdapter(getSupportFragmentManager(), s);
 		mPager = (ViewPager) findViewById(R.id.play_pager);
 		mPager.setAdapter(mAdapter);
 		mPager.setCurrentItem(0);
 	}
 
	public void compose(View view) {
		mPager.setCurrentItem(Data.COMPOSE);
	}
	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.menu_explore:
 			Data.goToExplore(this);
 			break;
 		case R.id.menu_play:
 			Data.goToPlay(this);
 			break;
 		case R.id.menu_profile:
 			Data.goToProfile(this);
 			break;
 		default:
 			break;
 		}
 		return true;
 	}
 
 	public static class PlayAdapter extends FragmentPagerAdapter {
 		Submission prompt;
 
 		public PlayAdapter(FragmentManager fm, Submission s) {
 			super(fm);
 			prompt = s;
 		}
 
 		@Override
 		public int getCount() {
 			return NUM_ITEMS;
 		}
 
 		@Override
 		public Fragment getItem(int i) {
 			Fragment fragment = new PlayFragment();
 			Bundle args = new Bundle();
 			if (i == Data.PROMPT) {
 				args.putParcelable(Data.ARG_OBJECT, prompt);
 			} else if (i == Data.COMPOSE) {
 				// Put stuff here
 			}
 			args.putInt(Data.STATUS, i);
 			fragment.setArguments(args);
 			return fragment;
 		}
 	}
 
 	public static class PlayFragment extends Fragment {
 		public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
 			Bundle args = getArguments();
 			int decision = args.getInt(Data.STATUS);
 			final Submission submission = args.getParcelable(Data.ARG_OBJECT);
 			if (decision == Data.PROMPT) {
 				LinearLayout casing = (LinearLayout) inflater.inflate(R.layout.prompt_layout, container, false);
 				ContentView content = (ContentView) casing.findViewById(R.id.contentView);
 				content.setContent(submission.getContent());
 				return casing;
 			} else if (decision == Data.COMPOSE) {
 				final LinearLayout casing = (LinearLayout) inflater.inflate(R.layout.compose_layout, container, false);
 				Button submit = (Button) casing.findViewById(R.id.submit);
 				submit.setOnClickListener(new Button.OnClickListener() {
 					public void onClick(View v) {
 						// gets the text the user entered, not sure what to do with it
 						EditText mEdit   = (EditText)casing.findViewById(R.id.response); 
 						String text = mEdit.getText().toString();
 						Data.getFlow(1).addItem(new BasicSubmission(TextContent.createTextContent(text), Data.getUser(1)));
 						Intent intent = new Intent(casing.getContext(), ViewSubmissionActivity.class);
 						intent.putExtra(Data.SUBMISSION, Data.getFlow(1).size() - 1);
 						intent.putExtra(Data.FLOW, Data.getFlow(1).serialNumber());
 						startActivity(intent);
 					}
 				});
 				return casing;
 			} else {
 				return inflater.inflate(R.layout.profile_layout, container);
 			}
 		}
 	}
 }
 /*
 public class PlayActivity extends Activity implements OnClickListener {
 	private static final int SWIPE_MIN_DISTANCE = 120;
 	private static final int SWIPE_MAX_OFF_PATH = 250;
 	private static final int SWIPE_THRESHOLD_VELOCITY = 200;
 	private GestureDetector gestureDetector;
 	View.OnTouchListener gestureListener;
 
 	private LinearLayout prompt;
 	private LinearLayout compose;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.activity_play);	
 
 		prompt = (LinearLayout) findViewById(R.id.prompt_fragment);
 
 		compose = (LinearLayout) findViewById(R.id.compose_fragment);
 
 		// Gesture detection
 		gestureDetector = new GestureDetector(this, new MyGestureDetector());
 		gestureListener = new View.OnTouchListener() {
 			public boolean onTouch(View v, MotionEvent event) {
 				return gestureDetector.onTouchEvent(event);
 			}
 		};
 
 		ImageButton goCompose = (ImageButton) findViewById(R.id.imageButton1);
 		goCompose.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				prompt.setVisibility(View.INVISIBLE);
 				compose.setVisibility(View.VISIBLE);
 			}
 		});   
 
 		Button goPrompt = (Button) findViewById(R.id.backToPrompt);
 		goPrompt.setOnClickListener(new Button.OnClickListener() {
 			public void onClick(View v) {
 				compose.setVisibility(View.INVISIBLE);
 				prompt.setVisibility(View.VISIBLE);
 				getWindow().setSoftInputMode(
 						WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
 			}
 		});
 
 		Button submit = (Button) findViewById(R.id.submit);
 		submit.setOnClickListener(new Button.OnClickListener() {
 			public void onClick(View v) {
 				// gets the text the user entered, not sure what to do with it
 				EditText mEdit   = (EditText)findViewById(R.id.response); 
 				String text = mEdit.getText().toString();
 				Data.getFlow(1).addItem(new BasicSubmission(TextContent.createTextContent(text), Data.getUser(1)));
 				Intent intent = new Intent(getBaseContext(), ViewSubmissionActivity.class);
 				intent.putExtra(Data.SUBMISSION, Data.getFlow(1).size() - 1);
 				intent.putExtra(Data.FLOW, Data.getFlow(1).serialNumber());
 				startActivity(intent);
 			}
 		});
 	}
 
 	class MyGestureDetector extends SimpleOnGestureListener {
 		@Override
 		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
 			try {
 				if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
 					return false;
 				// right to left swipe
 				if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
 					prompt.setVisibility(View.INVISIBLE);
 					compose.setVisibility(View.VISIBLE);
 				}  else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
 					compose.setVisibility(View.INVISIBLE);
 					prompt.setVisibility(View.VISIBLE);
 					getWindow().setSoftInputMode(
 							WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
 				}
 			} catch (Exception e) {
 				// nothing
 			}
 			return false;
 		}
 
 	}
 
 	public void onClick(View v) {
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.menu_explore:
 			Data.goToExplore(this);
 			break;
 		case R.id.menu_play:
 			Data.goToPlay(this);
 			break;
 		case R.id.menu_profile:
 			Data.goToProfile(this);
 			break;
 		default:
 			break;
 		}
 		return true;
 	}
 }
  */

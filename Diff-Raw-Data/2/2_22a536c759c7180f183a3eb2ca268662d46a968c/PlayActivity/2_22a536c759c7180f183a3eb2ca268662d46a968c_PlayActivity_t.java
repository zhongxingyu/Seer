 package winning.pwnies.recreativity;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.WindowManager;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 
 public class PlayActivity extends FragmentActivity {
 	static final int NUM_ITEMS = 2;
 
 	PlayAdapter mAdapter;
 
 	ViewPager mPager;
 	
 	AlertDialog.Builder adb;
 	
 	public static boolean composing = false;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.play_activity);
 		
 		LinearLayout profileButton = (LinearLayout) findViewById(R.id.menu_profile);
 		profileButton.setOnClickListener(new LinearLayout.OnClickListener() {
 		    public void onClick(View v) {
 		    	Intent myIntent = new Intent(PlayActivity.this, ProfileActivity.class);
 		    	PlayActivity.this.startActivity(myIntent);
 		    }
 		});
 		
 		LinearLayout exploreButton = (LinearLayout) findViewById(R.id.menu_explore);
 		exploreButton.setOnClickListener(new LinearLayout.OnClickListener() {
 		    public void onClick(View v) {
 		    	Intent myIntent = new Intent(PlayActivity.this, ExploreActivity.class);
 		    	PlayActivity.this.startActivity(myIntent);
 		    }
 		});
 		
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
 		
 		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN); 
 		
 		adb = new AlertDialog.Builder(this);
         adb.setTitle("Not implemented yet");
         adb.setMessage("This functionality is not yet implemented");
         adb.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
         	public void onClick(DialogInterface dialog, int id) {
         		dialog.cancel();
         		}
         });
 	}
 
 	public void compose() {
 		mPager.setCurrentItem(Data.COMPOSE);
 	}
 	
 	public void back(View view) {
 		mPager.setCurrentItem(Data.PROMPT);
 	}
 	
 	public void unimplemented(View view) {
 		Log.e("logging", "The button has been clicked");
 		Intent intent = new Intent(this, ProfileActivity.class);
 		startActivity(intent);
 	}
 	
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 	menu.clear();
 	if(composing){
 		getMenuInflater().inflate(R.menu.compose_menu, menu);
 	} else {
 		getMenuInflater().inflate(R.menu.prompt_menu, menu);
 	}
 	return super.onPrepareOptionsMenu(menu);
 	}
 	
 //	@Override
 //	public boolean onCreateOptionsMenu(Menu menu) {
 //		getMenuInflater().inflate(R.menu.prompt_menu, menu);
 //		return true;
 //	}
 	
 	@Override
 	  public boolean onOptionsItemSelected(MenuItem item) {
 	    switch (item.getItemId()) {
 	    case R.id.write_button:
 	    	composing = true;
 	    	compose();
 	      break;
 	    case R.id.record_button:
 	    	adb.show();
 	      break;
 	    case R.id.camera_button:
 	    	adb.show();
 	      break;  
 	    case R.id.submit_button:
 	    	// gets the text the user entered, not sure what to do with it				
 			EditText mEdit   = (EditText)findViewById(R.id.response);
 			String text = mEdit.getText().toString();
 			// TODO make sure not blank submission
 			text = text.replaceAll("\n", " \n ");
 			
 			Data.getFlow(1).addItem(new BasicSubmission(TextContent.createTextContent(text), Data.getUser(1)));
			Intent intent = new Intent(this, ViewSubmissionActivity.class);
 			intent.putExtra(Data.SUBMISSION, Data.getFlow(1).size() - 1);
 			intent.putExtra(Data.FLOW, Data.getFlow(1).serialNumber());
 			startActivity(intent);
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
 				composing = false;
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
 				content.setContent(submission.getContent(), true);
 				return casing;
 			} else if (decision == Data.COMPOSE) {
 				final LinearLayout casing = (LinearLayout) inflater.inflate(R.layout.compose_layout, container, false);
 				Button submit = (Button) casing.findViewById(R.id.submit);
 				submit.setOnClickListener(new Button.OnClickListener() {
 					public void onClick(View v) {
 						// gets the text the user entered, not sure what to do with it				
 						EditText mEdit   = (EditText)casing.findViewById(R.id.response);
 						String text = mEdit.getText().toString();
 						// TODO make sure not blank submission
 						text = text.replaceAll("\n", " \n ");
 						
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

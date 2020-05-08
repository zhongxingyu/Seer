 package ch.bfh.evoting.voterapp;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Fragment;
 import android.app.FragmentManager;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.pm.ActivityInfo;
 import android.content.pm.PackageManager;
 import android.nfc.NfcAdapter;
 import android.os.Bundle;
 import android.support.v4.content.LocalBroadcastManager;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.FrameLayout;
 import ch.bfh.evoting.voterapp.entities.Poll;
 import ch.bfh.evoting.voterapp.fragment.HelpDialogFragment;
 import ch.bfh.evoting.voterapp.fragment.PollReviewFragment;
 import ch.bfh.evoting.voterapp.util.BroadcastIntentTypes;
 import ch.bfh.evoting.voterapp.util.Utility;
 
 /**
  * Class displaying the activity showing the entire poll, in order to allow the user to check if it is correct
  * @author Philémon von Bergen
  *
  */
 public class ReviewPollVoterActivity extends Activity {
 	
 	private NfcAdapter nfcAdapter;
 	private boolean nfcAvailable;
 	private PendingIntent pendingIntent;
 
 	private BroadcastReceiver pollReceiver;
 	private AlertDialog dialogBack;
 
 	private Fragment fragment;
 	private Poll poll;
 	private String sender;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		if(getResources().getBoolean(R.bool.portrait_only)){
 			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
 		}
 
 		final FrameLayout overlayFramelayout = new FrameLayout(this);
 		FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
 				FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
 		layoutParams.setMargins(getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin), 0, getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin), 0);
 		overlayFramelayout.setLayoutParams(layoutParams);
 
 		View view = getLayoutInflater().inflate(R.layout.activity_review_poll_voter, overlayFramelayout,false);
 		overlayFramelayout.addView(view);
 
 		final SharedPreferences settings = getSharedPreferences(AndroidApplication.PREFS_NAME, MODE_PRIVATE);
 
 		if(settings.getBoolean("first_run_"+this.getClass().getSimpleName(), true)){
 			final View overlay_view = getLayoutInflater().inflate(R.layout.overlay_review_poll, null,false);
 			overlayFramelayout.addView(overlay_view);
 
 			overlay_view.setOnClickListener(new View.OnClickListener() {
 
 				@Override
 				public void onClick(View v) {
 					overlayFramelayout.removeView(overlay_view);
 					settings.edit().putBoolean("first_run_"+ReviewPollVoterActivity.this.getClass().getSimpleName(), false).commit();					
 				}
 			});
 		}
 		setContentView(overlayFramelayout);
 
 		if(savedInstanceState!=null){
 			poll = (Poll) savedInstanceState.getSerializable("poll");
 			sender = savedInstanceState.getString("sender");
 		}
 		Poll intentPoll = (Poll)this.getIntent().getSerializableExtra("poll");
 		if(intentPoll!=null){
 			poll = intentPoll;
 			sender = this.getIntent().getStringExtra("sender");
 		}
 
 		FragmentManager fm = getFragmentManager();
 		fragment = new PollReviewFragment();
 		Bundle bundle = new Bundle();
 		bundle.putSerializable("poll", poll);
 		bundle.putString("sender", sender);
 		fragment.setArguments(bundle);
 
 		fm.beginTransaction().replace(R.id.fragment_container, fragment, "review").commit();
 		AndroidApplication.getInstance().setCurrentActivity(this);
 		
 		// Is NFC available on this device?
 		nfcAvailable = this.getPackageManager().hasSystemFeature(
 				PackageManager.FEATURE_NFC);
 
 		if (nfcAvailable) {
 
 			nfcAdapter = NfcAdapter.getDefaultAdapter(this);
 
 			if (nfcAdapter.isEnabled()) {
 
 				// Setting up a pending intent that is invoked when an NFC tag
 				// is tapped on the back
 				pendingIntent = PendingIntent.getActivity(this, 0, new Intent(
 						this, getClass())
 						.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
 			} else {
 				nfcAvailable = false;
 			}
 		}
 	}
 	
 	@Override
 	protected void onPause() {
 		super.onPause();
 		if (nfcAvailable) {
 			nfcAdapter.disableForegroundDispatch(this);
 		}
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		AndroidApplication.getInstance().setCurrentActivity(this);
 		
 		if (nfcAdapter != null && nfcAdapter.isEnabled()) {
 			nfcAvailable = true;
 		}
 
 		// make sure that this activity is the first one which can handle the
 		// NFC tags
 		if (nfcAvailable) {
 			nfcAdapter.enableForegroundDispatch(this, pendingIntent,
 					Utility.getNFCIntentFilters(), null);
 		}
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		outState.putSerializable("poll", poll);
 		outState.putString("sender", sender);
 	}
 
 	@Override
 	protected void onDestroy() {
 		LocalBroadcastManager.getInstance(this).unregisterReceiver(pollReceiver);
 		super.onDestroy();
 	}
 
 	@Override
 	public void onBackPressed() {
 		//Show a dialog to ask confirmation to quit vote 
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		// Add the buttons
 		builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int id) {
 				dialogBack.dismiss();
 				startActivity(new Intent(ReviewPollVoterActivity.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK));
 			}
 		});
 		builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int id) {
 				dialogBack.dismiss();
 			}
 		});
 
 		builder.setTitle(R.string.dialog_title_back);
 		builder.setMessage(this.getString(R.string.dialog_back));
 
 		// Create the AlertDialog
 		dialogBack = builder.create();
		
		dialogBack.setOnShowListener(new DialogInterface.OnShowListener() {
			@Override
			public void onShow(DialogInterface dialog) {
				Utility.setTextColor(dialog, getResources().getColor(R.color.theme_color));
				dialogBack.getButton(AlertDialog.BUTTON_NEGATIVE).setBackgroundResource(
						R.drawable.selectable_background_votebartheme);
				dialogBack.getButton(AlertDialog.BUTTON_POSITIVE).setBackgroundResource(
						R.drawable.selectable_background_votebartheme);

			}
		});
		
 		dialogBack.show();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.review, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.help:
 			HelpDialogFragment hdf = HelpDialogFragment.newInstance( getString(R.string.help_title_review), getString(R.string.help_text_review_voter) );
 			hdf.show( getFragmentManager( ), "help" );
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.app.Activity#onNewIntent(android.content.Intent)
 	 */
 	@Override
 	public void onNewIntent(Intent intent) {
 		Intent broadcastIntent = new Intent(BroadcastIntentTypes.nfcTagTapped);
 		broadcastIntent.putExtra(NfcAdapter.EXTRA_TAG, intent.getParcelableExtra(NfcAdapter.EXTRA_TAG));
 		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
 	}
 
 }

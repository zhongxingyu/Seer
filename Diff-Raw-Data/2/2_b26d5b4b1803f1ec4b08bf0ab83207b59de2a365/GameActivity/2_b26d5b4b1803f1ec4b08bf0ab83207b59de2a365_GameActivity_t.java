 package android.game.guessmynumber;
 
 import java.text.SimpleDateFormat;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.app.AlertDialog;
 import android.app.DialogFragment;
 import android.app.Fragment;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.game.guessmynumber.TimerService.MyTimerTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.ResultReceiver;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentStatePagerAdapter;
 import android.support.v4.view.PagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.TextView;
 
 
 public class GameActivity extends FragmentActivity
 							implements QuitGameDialogFragment.QuitDialogListener{
 	
 	private static final int NUM_PAGES = 6; //Number of pages we have
 	private ViewPager mPager;	
 	private PagerAdapter mPagerAdapter;
 	final Context context = this;
 	Settings setting = new Settings(GameActivity.this);
 	Timer timer = new Timer();
 	TextView timerView;
	int timeit = 0 ;
 	 
 	 /*MyResultReceiver resultReceiver;
 	 Intent intent;
 	 TextView txtview;*/
 
 	/**get the pager from activity_game_developer and set it with adapter**/
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_game_viewpager);
 		//startService(new Intent(GameActivity.this, TimerService.class));
 	
 		//timer.scheduleAtFixedRate(timerTask, 1000, 1000);
 		timerView = (TextView) findViewById(R.id.Timer);
 		
 		NumberGenerator.PrimeGenerator();
 		
 		mPager = (ViewPager) findViewById(R.id.pager);
         mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
         mPager.setAdapter(mPagerAdapter);
         mPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
             @Override
             public void onPageSelected(int position) {
             	
             	if(position + 1 == 6){
             		// get prompts.xml view
     				LayoutInflater li = LayoutInflater.from(context);
     				View promptsView = li.inflate(R.layout.dialog_enter_number, null);
      
     				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
     						context);
      
     				// set prompts.xml to alertdialog builder
     				alertDialogBuilder.setView(promptsView);
     				
     				// set dialog message
     				alertDialogBuilder
     					.setCancelable(false)
     					.setPositiveButton("OK",
     					  new DialogInterface.OnClickListener() {
     					    public void onClick(DialogInterface dialog,int id) {
     						// get user input and set it to result
     						// edit text
     					    	finish();
     					    }
     					  })
     					.setNegativeButton("I don't Know",
     					  new DialogInterface.OnClickListener() {
     					    public void onClick(DialogInterface dialog,int id) {
     							finish();
     					    }
     					  });
     				
     				// create alert dialog
     				AlertDialog alertDialog = alertDialogBuilder.create();
      
     				// show it
     				alertDialog.show();
             	}
                 invalidateOptionsMenu();
             }
         });   
         
         /*resultReceiver = new MyResultReceiver(null);
         intent = new Intent(this, TimerService.class);
 		intent.putExtra("receiver", resultReceiver);
 		startService(intent);*/
         timeIt();
 	}
 	
 	@Override
 	public boolean onMenuItemSelected(int featureId, MenuItem item) {
 		// TODO Auto-generated method stub
 		
 		switch(item.getItemId()){
 		case R.id.menu_quit:
 			DialogFragment quitDialog = new QuitGameDialogFragment();
 			quitDialog.show(getFragmentManager() , "quitDialog");
 		}
 		return super.onMenuItemSelected(featureId, item);
 	}
 	
 	public void getPosition(){
 		Log.d("Page No" , Integer.toString(mPager.getCurrentItem()));
 	}
 	
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.inside_game_menu, menu);
 		return true;
 	}
 	/**adapter**/
 	private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter{
 
 		public ScreenSlidePagerAdapter(android.support.v4.app.FragmentManager fragmentManager) {
 			super(fragmentManager);
 			// TODO Auto-generated constructor stub
 		}
 
 		//Called when ever user "Time"swipes and goes to new fragment
 		@Override
 		public android.support.v4.app.Fragment getItem(int position) {
 			// TODO Auto-generated method stub
 			 /*calls the create method from GameActivityPageFragement class
 			  and gets the newly created fragment back which was created from the create
 			  method*/
 			//Log.d("Page No" , Integer.toString(mPager.getCurrentItem()));
 			//Fragment fragment = GameActivityPageFragment.ne 
 			GameActivityPageFragment fragment = GameActivityPageFragment.create(position);
         	Log.d("Page No" , Integer.toString(fragment.getPageNumber()));
 			return GameActivityPageFragment.create(position);
 		}
 		
 		@Override
 		public int getItemPosition(Object object) {
 			// TODO Auto-generated method stub
 			return super.getItemPosition(object);
 		}
 
 		@Override
 		//gets called multiple times and returns total pages
 		public int getCount() {
 			// TODO Auto-generated method stub
 			return NUM_PAGES;
 		}
 		
 	}
 
 	/**On quit game dialog click**/
 	@Override
 	public void onDialogPositiveClick(DialogFragment quitDialog) {
 		// TODO Auto-generated method stub
 		//If yes start new activity
 		finish();
 	}
 
 	@Override
 	public void onDialogNegativeClick(DialogFragment quitDialog) {
 		// TODO Auto-generated method stub
 		
 	} 
 	
 	//Onpause pause the music
 	@Override
 	protected void onPause() {
 		// TODO Auto-generated method stub
 		super.onPause();
 		MyMusic MM = new MyMusic();
 		if(setting.getMusic() == true)
 			MM.pauseSong();
 		
 	}
 	//onResume start the music
 	@Override
 	protected void onResume() {
 		// TODO Auto-generated method stub
 		super.onResume();
 		if(setting.getMusic())
 			startService(new Intent(this, MyMusic.class));
 	}
 	
 	protected void timeIt(){
 		timer.schedule(new TimerTask() {
 			public void run() {
 			    timeit++;
 			    Log.d("timer", "timer");
 			    runOnUiThread(new Runnable() {
 
 			    @Override
 			    public void run() {
 			    	timerView.setText("Timer:" + timeit);
 			            }
 			    });
 			        }
 		}, 10, 1000);
 	}
 }

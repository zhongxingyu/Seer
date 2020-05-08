 package android.game.guessmynumber;
 
 import java.util.Date;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.app.AlertDialog;
 import android.app.DialogFragment;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentStatePagerAdapter;
 import android.support.v4.view.PagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 
 public class GameActivity extends FragmentActivity
 							implements QuitGameDialogFragment.QuitDialogListener{
 	
 	private static int NUM_PAGES = 0; //Number of pages we have
 	private ViewPager mPager;	
 	private PagerAdapter mPagerAdapter;
 	final Context context = this;
 	Settings setting = new Settings(GameActivity.this);
 	SecretNumber number;
 	Timer timer = new Timer();
 	TextView timerView, textViewHint;	
 	EditText secretNumber;
 	Button btn_no , btn_yes;
 	int timeit = 0 , sum = 0,Maxtime, attempt;
 	NumberGenerator generator;
 	String item;
 	String []result = new String[5];
 	String CardMode;
 	String name;
 	boolean TimeFlag = false;
 	boolean colorChange = false;
 	boolean clockMusic ;
 	boolean dialogShownOnce = false;
 	boolean tickticksound = false;
 	int sec = 30;
 	AppLogic logic ;
 	Music clock ;
 	 
 
 	/**get the pager from activity_game_developer and set it with adapter**/
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_game_viewpager);
 	
 		timerView = (TextView) findViewById(R.id.Timer);
 		textViewHint = (TextView) findViewById(R.id.textViewHint);
 		//generator.PrimeGenerator();
 		generator = new NumberGenerator(setting.getGameMode() 
 						,(Integer.parseInt(setting.getRange()) + 10));
 		
 		Maxtime = Integer.parseInt(setting.getTime()) + 10;
 		attempt = Integer.parseInt(setting.getAttempt());
 		clockMusic = setting.getMusic();
 		Bundle extras = getIntent().getExtras();
 	    if (extras != null) {
 	    	name = extras.getString("name");
 	    }
 	    
 		CustomViewPager view = new CustomViewPager(getApplicationContext());
 		
 		CardMode = setting.getCardMode();
 		btn_no = (Button)findViewById(R.id.no);
 		btn_yes = (Button)findViewById(R.id.yes);
 		
 		switch(Integer.parseInt(CardMode)){
 		case 0:
 			clockMusic = false;
 			btn_no.setVisibility(View.VISIBLE);
 			btn_yes.setVisibility(View.VISIBLE);
 			timerView.setVisibility(View.GONE);
 			NUM_PAGES = NumberGenerator.NUM_OF_CARDS + 1 ;
 			view.setSwipe(false);
 			textViewHint.setText("Is your secret number on this card?");
 			break;
 		case 1:
 			clock = new Music("clock", getApplicationContext());
 			TimeFlag = true;
 		case 2:
 			NUM_PAGES = NumberGenerator.NUM_OF_CARDS + 1;
 			view.setSwipe(true);
 			btn_no.setVisibility(View.GONE);
 			btn_yes.setVisibility(View.GONE);
 			item = generator.selectNumber();		
 			number = new SecretNumber(item);
 			timeIt(TimeFlag);
 			TimeFlag = false;
 			List<String> numbers2 ; 
 			numbers2 = NumberGenerator.SplitCardValues(0);
 			if(numbers2.contains(number.getNumber())){
 				textViewHint.setText("Secret number is on this card");
 			}
 			else{
 				textViewHint.setText("Secret number is NOT on this card");
 			}
 			
 		}
 		
 		setbackground();
 		
 		mPager = (ViewPager) findViewById(R.id.pager);
         mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
         mPager.setAdapter(mPagerAdapter);
         mPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
         	
             @Override
             public void onPageSelected(int position) {
             	
             	switch(Integer.parseInt(CardMode)){
         		case 0:
         			
         			if(position == NUM_PAGES -1 ){
         				//sum = logic.getSum();
         				textViewHint.setVisibility(View.GONE);
         				btn_no.setVisibility(View.GONE);
         				btn_yes.setVisibility(View.GONE);
         				
         				finish();
         				prepareAndStartResultActivity(2);
         			}
         			else{
         				textViewHint.setText("Is your secret number on this card?");
         			}
         			break;
         		case 1:
         		case 2:
         			if(position == NUM_PAGES - 1){
         				textViewHint.setVisibility(View.GONE);
         				userInputDialog();        				
                 	}
         			else{
         				List<String> numbers2 ; 
             			numbers2 = NumberGenerator.SplitCardValues(position);
             			if(numbers2.contains(number.getNumber())){
             				textViewHint.setText("Secret number is on this card");
             			}
             			else{
             				textViewHint.setText("Secret number is NOT on this card");
             			}
         			}
         		}
             	setbackground();
                 invalidateOptionsMenu();
             }
         });   
 	}
 	public void setbackground(){
 		DisplayResult displayResult = new DisplayResult();
  	    String imageName = displayResult.returnbackgroundName();
  	    int id1 = getResources().getIdentifier(
  	    		imageName, "drawable", getPackageName() );
 
  	    RelativeLayout layout = (RelativeLayout)findViewById(R.id.gamelayout);
  	    layout.setBackgroundResource(id1);
 	}
 	public void userInputDialog(){
 		// get prompts.xml view
 		if(setting.getCardMode().equals("1") && setting.getMusic() && !clockMusic){
 			clockMusic = false;
 			clock.Stop();
 		}
 		LayoutInflater li = LayoutInflater.from(context);
 		View promptsView = li.inflate(R.layout.dialog_enter_number, null);
 	 	attempt = attempt - 1;
 	 	dialogShownOnce = true;
 		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
 				context);
 
 		// set prompts.xml to alertdialog builder
 		alertDialogBuilder.setView(promptsView);
 		
 		secretNumber = ((EditText)promptsView.findViewById(R.id.editText_SecretNumber));
 		// set dialog message
 		alertDialogBuilder
 			.setCancelable(false)
 			.setPositiveButton("OK",
 			  new DialogInterface.OnClickListener() {
 			    public void onClick(DialogInterface dialog,int id) {
 				// get user input and set it to result
 				// edit text
 			    	if(attempt != 0 && !secretNumber.getText().toString().equals(item)){
 			    		System.out.println(attempt);
 			    		userInputDialog();
 			    		/*finish();
 			    		prepareAndStartResultActivity(0);*/
 			    	}
 			    	else{
 			    		finish();
 			    		prepareAndStartResultActivity(0);
 			    	}
 			    }
 			  })
 			.setNegativeButton("I don't Know",
 			  new DialogInterface.OnClickListener() {
 			    public void onClick(DialogInterface dialog,int id) {
 			    	finish();
 			    	prepareAndStartResultActivity(1);	
 			    }
 			    
 			  });
 		
 		// create alert dialog
 		AlertDialog alertDialog = alertDialogBuilder.create();
 
 		// show it
 		alertDialog.show();
 	}
 	public void prepareAndStartResultActivity(int status){
 		
 		Date now = new Date();
 		switch(status){
 		case 0:
 			result[0] = item;
 			result[1] = secretNumber.getText().toString();
 			result[2] = Integer.toString(timeit);
 			result[3] =  now.toString();
 			result[4] = name;
 			
 			break;
 		case 1:
 			result[0] = item;
 			result[1] = null;
 			result[2] = Integer.toString(timeit);
 			result[3] = now.toString();
 			result[4] = name;
 			
 			break;
 		case 2:
 			result[0] = Integer.toString(sum);
 			result[1] = null;
 			result[2] = Integer.toString(timeit);
 			result[3] = now.toString();
 			result[4] = name;
 			
 		}
 		startResultActivity();
 	}
 	public void startResultActivity(){
 		if(setting.getBackGroundMusic()){
 			BackgroundMusic.onActivityPause();
 		}
 		Intent intent = new Intent(GameActivity.this , ResultActivity.class);
 		intent.putExtra("result", result);
 		startActivity(intent);
 	}
 	
 	@Override
 	public boolean onMenuItemSelected(int featureId, MenuItem item) {
 		// TODO Auto-generated method stub
 		
 		switch(item.getItemId()){
 		case R.id.menu_quit:
 			if(!((GameActivity.this).isFinishing())){
 				DialogFragment quitDialog = new QuitGameDialogFragment();
 				quitDialog.show(getFragmentManager() , "quitDialog");
 			}
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
 
 	/**On quit game dialog click**/
 	@Override
 	public void onDialogPositiveClick(DialogFragment quitDialog) {
 		// TODO Auto-generated method stub
 		//If yes start new activity
 		BackgroundMusic.setFalse();
 		if(setting.getCardMode().equals("1") && setting.getMusic() && !clockMusic){
 			clockMusic = false;
 			clock.Stop();
 		}
 		finish();
 		
 	}
 
 	@Override
 	public void onDialogNegativeClick(DialogFragment quitDialog) {
 		// TODO Auto-generated method stub
 		
 	} 
 	protected void timeIt(final boolean TimeFlag){
		
 		timer.schedule(new TimerTask() {
 			public void run() {
			    timeit++;
 			    runOnUiThread(new Runnable() {
 
 			    	@Override
 			    	public void run() {
 			    		timerView.setText("Timer:" + timeit);
 			    		if(TimeFlag && timeit == Maxtime && !((GameActivity.this).isFinishing())){
 			    			timer.cancel();
 			    			if(!dialogShownOnce){
 			    				userInputDialog();
 			    			}
 			    		}
 			    		if(timeit > Maxtime - 11 && setting.getCardMode().equals("1") &&  !((GameActivity.this).isFinishing())){
 			    			if(clockMusic){
 			    				clockMusic = false;
 			    				tickticksound = true;
 			    				clock.Start();
 			    				if(setting.getBackGroundMusic() && tickticksound){
 			    					BackgroundMusic.onActivityPause();
 			    				}
 			    			}
 			    			if(colorChange){
 			    				timerView.setTextColor(Color.RED);
 			    				colorChange = false;
 			    			}
 			    			else{
 			    				timerView.setTextColor(Color.CYAN);
 			    				colorChange = true;
 			    			}
 			    		}
 			    	}
 			    });
 			        }
 		}, 1000, 1000);
 	}
 	
 	public void onDecisionBtnClick(View view){
 		switch(view.getId()){
 		case R.id.no:
 			 mPager.setCurrentItem(mPager.getCurrentItem() + 1);
 			break;
 		case R.id.yes:
 			 List<String> numbers = NumberGenerator.SplitCardValues(mPager.getCurrentItem());
 			 logic = new AppLogic(numbers);
 			 sum = logic.getSmallest() + sum;
 			 mPager.setCurrentItem(mPager.getCurrentItem() + 1);
 			break;
 		}
 	}
 	
 	@Override
 	protected void onPause() {
 		// TODO Auto-generated method stub
 		super.onPause();
 		if(setting.getCardMode().equals("1") && setting.getMusic() && !clockMusic){
 			clockMusic = false;
 			clock.Stop();
 		}
 		if(setting.getBackGroundMusic() && !tickticksound){
 			BackgroundMusic.onActivityPause();
 		}
 	}
 	
 	@Override
 	protected void onResume() {
 		// TODO Auto-generated method stub
 		super.onResume();
 		if(setting.getBackGroundMusic() && !tickticksound){
 			BackgroundMusic.startMusic();
 		}
 	}
 	@Override
 	public void onBackPressed() {
 		// TODO Auto-generated method stub
 		DialogFragment quitDialog = new QuitGameDialogFragment();
 		quitDialog.show(getFragmentManager() , "quitDialog");
 		//return super.onKeyDown(keyCode, event);
 		return;
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
 	
 	
 }
 

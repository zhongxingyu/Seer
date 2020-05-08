 package com.example.phonepet;
 
 import java.text.NumberFormat;
 import java.util.List;
 
 import com.example.connect4.Connect4Activity;
 import com.example.controllers.PetController;
 import com.example.utils.DatabaseHelper;
 import com.example.utils.Point;
 import com.example.views.HomeView;
 import com.example.vos.Food;
 import com.example.vos.OnChangeListener;
 import com.example.vos.PetVo;
 import com.example.vos.Poop;
 import com.example.vos.Ball;
 
 import android.os.Bundle;
 import android.os.CountDownTimer;
 import android.provider.Settings.System;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.support.v4.view.MotionEventCompat;
 import android.text.format.Time;
 import android.util.Log;
 import android.view.DragEvent;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnDragListener;
 import android.view.View.OnTouchListener;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class HomeActivity extends Activity implements OnChangeListener<PetVo> {
 
 	private static final int INVALID_POINTER_ID = 0;
 
 	public HomeActivity() {
 
 	}
 
 	private PetController controller;
 	private PetVo pet; // Pet
 	
 	private HomeView hView;
 	
 	// Window parameters
 	private int windowWidth;
 	private int windowHeight;
 	private int playgroundHeight;
 	private int playgroundWidth;
 	private int menuWidth;
 	private int menuHeight;
 	
 	// The active pointer is the one currently moving our object.
 	private int mActivePointerId = INVALID_POINTER_ID;
 	private float mLastTouchX = 0;
 	private float mLastTouchY = 0;
 	private float mPosX = 0;
 	private float mPosY = 0;
 	private float spongeX, spongeY;
 	//boolean petIsClicked = false;
 	private String fileName = "preferences";
 	DatabaseHelper db;
 	private boolean testButtonJustHeld = false;
 	private boolean poopIsClicked, need;
 	private boolean spongeClicked;
 	private int whatIsHappening = 0; // 0 means nothing, 1 means scooping poop, 2 means cleaning
 	private int id;
 	private Poop temp, temp2;
 	List<Poop> list;
 	ImageButton playButton, accessorizeButton, poopButton, feedButton, cleanButton;
 	
 	//TextView myTextView = (TextView) findViewById(R.id.mytextview); myTextView.setText("My double value is " + doubleValue);
 	TextView happinessValue, hungerValue;//, energyValue;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_home);
 
 		// Instantiate pet model and set the HomeActivity as an observer.
 		// Now any time the model changes, the onChange method gets called. 
 		pet = PetVo.getInstance();
 		pet.addListener(this);
 		
 		db = new DatabaseHelper(this);
 		final HomeView homeview = (HomeView)findViewById(R.id.HomeView);
 		this.hView = homeview;
 		need = false;
 		controller = new PetController(pet, getApplicationContext());
 		
 		// Identify buttons
 		playButton = (ImageButton)findViewById(R.id.Play);
 		accessorizeButton = (ImageButton)findViewById(R.id.Accessorize);
 		poopButton = (ImageButton)findViewById(R.id.Poop);
 		feedButton = (ImageButton)findViewById(R.id.Feed);
 		cleanButton = (ImageButton)findViewById(R.id.Sponge);
 		
 		// Identify status variables
 		happinessValue = (TextView) findViewById(R.id.HappinessValue);
 		hungerValue = (TextView) findViewById(R.id.HungerValue);
 		
 		
 		playButton.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				//controller.handleMessage(PetController.MESSAGE_PLAY);
 				if(hView.ballInPlay)
 				{
 					// Stops playing Fetch
 					hView.ballInPlay = false;
 					hView.removeBall();
 					hView.setOnTouchListener(new DefaultListener());
 				}
 				else
 				{
 					showPlayOptions(v);
 				}
 			}
 		});
 		
 		accessorizeButton.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				// Send message to the controller asking it to handle event.
 				// When the event is handled, the controller updates the model.
 				controller.handleMessage(PetController.MESSAGE_ACCESSORIZE);
 				
 			}
 		});
 		
 		feedButton.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				
 				// Spawn food.
 				drawFood();
 			
 			}
 		});
 		
 		poopButton.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				// Scoop poop was already happening. Deactivate scoop poop mode.
 				if (whatIsHappening == 1) {
 					whatIsHappening = 0; // Nothing is happening
 					hView.setOnTouchListener(new DefaultListener());
 					hView.setTrash(false);  
 				}
 				// Scoop poop was not already happening. Activate scoop poop mode.
 				else {
 					whatIsHappening = 1; // Scoop poop mode
 					hView.setOnTouchListener(new ScoopPoopListener());
 					hView.setTrash(true); 
 					poopCleaner();
 				}
 					
 			}
 		});
 		
 		
 		cleanButton.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				// Cleaning was already happening. Deactivate cleaning mode.
 				if (whatIsHappening == 2) {
 
 					hView.notCleaning();
 					pet.notCleaning();
 					whatIsHappening = 0; // Nothing is happening
 					hView.setOnTouchListener(new DefaultListener());
 				}
 				// Cleaning was not already happening.  Activate cleaning mode.
 				else {
 					whatIsHappening = 2; // Cleaning mode
 					hView.setOnTouchListener(new CleanListener());
 					
 					controller.handleMessage(PetController.MESSAGE_CLEAN);				
 					hView.cleaning();
 					pet.cleaning();
 					spongeX = (float ) (hView.getBackgroundWidth()/2.0);
 					spongeY = (float) (hView.getBackgroundHeight()/2.0);
 					spongeBath();
 				}
 			}
 		});
 
 		
 		// Load pet information.
 		controller.handleMessage(PetController.MESSAGE_LOAD, getApplicationContext());
 		
 		// Set default OnTouchListener
 		hView.setOnTouchListener(new DefaultListener());
 	}
 	/*
 	 * Simply delegate the logic to the controller by sending a message asking it to handle the
 	 * KeyEvent for us.  Our controller returns a boolean indicating whether or not the message
 	 * handled. 
 	 */
 	
 		public void drawFood()
 		{
 			Food food;
 			int tempX, tempY;
 			int width, height;
 			
 			width = hView.getBackgroundWidth() - hView.getPetWidth();
 			height = hView.getBackgroundHeight();
 			
 			do
 			{
 				tempX = (int)(Math.random() * width);
 				tempY = (int)(Math.random() * (height/2) + (height/2));
 				
 				if(tempY > height - pet.getHeight())
 				{
 					tempY = tempY - height/12;
 				}
 			}
 			while(!controller.isWithinPlayground(tempX, tempY));
 			
 			pet.setPetIsEating(true);
 			food = new Food(tempX, tempY);       
 			hView.drawFood(food);
 			
 			controller.handleMessage(PetController.MESSAGE_FEED, food);
 			Log.v("end of draw food", "is pet at food yet?");
 	
 		} // End method drawFood
 		
 		public void drawPoop()
 		{
 
 			int tempX, tempY;
 			Poop poop;
 			int width, height;
 			
 			width = hView.getBackgroundWidth() - hView.getPetWidth();
 			height = hView.getBackgroundHeight();
 				
 				do
 				{
 					tempX = (int)(Math.random() * width);
 					tempY = (int)(Math.random() * (height/2) + (height/2));
 					
 					if(tempY > height - pet.getHeight()) {
 						tempY = tempY - height/12;
 					}
 					poop = new Poop(tempX, tempY);
 				}
 				while(!controller.isWithinPlayground(tempX, tempY));
 				db.addPoop(poop);
 			
 	        list = db.getAllPoop();			
 			hView.drawPoop(list);
 			
 		} // End method drawPoop
 
 		
 		public void drawBall(float tempX, float tempY)
 		{
 			Ball ball;
 			int integerX, integerY;
 			
 			integerX = (int)tempX;
 			integerY = (int)tempY;
 			
 			
 			
 			if(!controller.isWithinPlayground(integerX, integerY))
 			{
 				Toast.makeText(getApplicationContext(), "Cannot place ball here!", Toast.LENGTH_SHORT).show();
 				return;
 			}
 			
 			ball = new Ball(tempX, tempY);
			hView.drawBall(ball, false);
 			
 			//pet.move((int)tempX, (int)tempY);
 		}
 		
 		
 	public void spongeBath(){
 		hView.setOnTouchListener(new CleanListener());
 	}
 
 
 	public void poopCleaner() {
 		
 		
 			hView.setOnTouchListener(new ScoopPoopListener());
 				
 		}
 		 // Set onTouchListener
 	
 	/**
 	 * Once the model notifies the view of a change, we just update the views on the UI thread.
 	 * @param pet
 	 */
 	@Override
 	public void onChange(PetVo pet) {
 		/* Since a change has occurred we need to update the view. All views must be modified
 		 * on the UI Thread. Since we don't know what thread called onChange, we need to switch
 		 * over to the UI thread before making a modification. The updateView() method is the
 		 * one that is responsible for syncing all of our widgets to our model's data.
 		 * 
 		 * This implements data binding. Our activity is registered as an observer and whenever 
 		 * our model is updated we know our UI is going to stay right in-sync.
 		*/ 
 		runOnUiThread(new Runnable() {
 			@Override
 			public void run() {
 				updateView();
 			}
 		});
 	}
 	
 	private void updateView() {
 		// Update pet on the screen.
 		this.hView.drawPet(pet.getXCoord(), pet.getYCoord());
 		
 		NumberFormat formatter = NumberFormat.getNumberInstance();
 		formatter.setMinimumFractionDigits(2);
 		formatter.setMaximumFractionDigits(2);
 		// Update status levels
 		happinessValue.setText(formatter.format(pet.getPetHappiness()) + "%  ");
 		hungerValue.setText(formatter.format(pet.getPetHunger()) + "%  ");
 		
 
 		// Display thought bubble if...
 		// ..Pet is sleeping
 		if (pet.getPetIsSleeping()) {
 			hView.showThought(1);
 		}
 		// ..Pet is hungry
 		else if (pet.getPetHunger() <= 10) {
 			hView.showThought(2);
 		}
 		// ..Pet is unhappy
 		else if (pet.getPetHappiness() <= 10) {
 			hView.showThought(3);
 		}
 		else {
 			hView.showThought(0); // No thought 
 		}
 		
 		// Don't draw the food on the screen unless feeding is happening.
 		if(!pet.getPetIsEating())
 		{
 			this.hView.removeFood();
 		}
 		
 		// Don't draw poop until after the pet has eaten.
 		if(pet.getPetIsPooping())
 		{
 			pet.setPetIsPooping(false);
 			poopTimer();
 		}
 
 		//control dirtLevels
 		/*hView.setDirtAmt(pet.dirtyness());*/
 		hView.setDirtAmt(0);
 	}
 
 	@Override
 	protected void onStop()
 	{
 		super.onStop();
 		
 		writeTimers();
 		for(Poop e: list)
 		{
 			db.addPoop(e);
 		}
 		
 	} // End method onStop
 	
 	public void writeTimers()
 	{
 		// Get current time
 		Time currTime = new Time();
 		currTime.setToNow();
 		Log.v("stop current time", currTime.toString());
 		
 		// Get the preferences file and create the editor
 		SharedPreferences sharedPref = getSharedPreferences(fileName, Context.MODE_PRIVATE);
 		SharedPreferences.Editor editor = sharedPref.edit();
 		
 		// Put data into preferences file
 		editor.putLong("lastSavedTimeMillis", currTime.toMillis(true)); // ignore daylight savings time
 		try
 		{
 			long t = controller.getCountdownTimeLeft(1);
 			Log.v("getRunawayTimeLeft", Long.toString(t));
 			editor.putLong("runawayTimeLeft", t);
 		}
 		catch(NullPointerException e)
 		{
 			Log.v("assuming t was null", "put no time remaining");
 			editor.putLong("runawayTimeLeft", 0);
 		}
 		
 		// Currently don't keep track of sleep timer(s).
 		
 		// Status
 		editor.putFloat("happinessStatus", pet.getPetHappiness());
 		editor.putFloat("hungerStatus", pet.getPetHunger());
 		
 		try
 		{
 			editor.putLong("happinessTimeLeft", controller.getCountdownTimeLeft(3));
 		}
 		catch(NullPointerException e)
 		{
 			editor.putLong("happinessTimeLeft", 0);
 		}
 		try
 		{
 			editor.putLong("hungerTimeLeft", controller.getCountdownTimeLeft(4));
 		}
 		catch(NullPointerException e)
 		{
 			editor.putLong("hungerTimeLeft", 0);
 		}
 		editor.putLong("lastTimeAte", pet.getLastTimeAte());
 		editor.putLong("lastTimePlayedWith", pet.getLastTimePlayedWith());
 		
 		editor.commit();
 		
 	} // End method writeTimers
 	
 	@Override
 	protected void onResume()
 	{
 		super.onResume();
 		Log.v("on resume", "app is resuming.");
 		
 		// Get the preferences file
 		SharedPreferences sharedPref = getSharedPreferences(fileName, Context.MODE_PRIVATE);
 		
 		long lastTimeSaved = sharedPref.getLong("lastSavedTimeMillis", 0);
 		
 		// Get current time
 		Time currentTime = new Time();
 		currentTime.setToNow();
 		Log.v("resume current time", Long.toString(currentTime.toMillis(true)));
 		Log.v("resume last time saved", Long.toString(lastTimeSaved));
 		
 		// Calculate time elapsed
 		long timePassed = currentTime.toMillis(true) - lastTimeSaved;
 		Log.v("resume timeElapsed", Long.toString(timePassed));
 		
 		
 		// Update pet status.
 		boolean brandNewPet = sharedPref.getBoolean("petCreation", false); // Default is that pet already exists.
 		if(brandNewPet)
 		{
 			// Delete variable from sharedpref, pet is no longer "brand new".
 			SharedPreferences.Editor editor = sharedPref.edit();
 			editor.remove("petCreation");
 			editor.commit();
 			
 			// Pet was just created, every status is 50%.
 			pet.setPetHappiness(50);
 			happinessValue.setText("50%  ");
 			pet.setPetHunger(50);
 			hungerValue.setText("50%  ");
 			
 			controller.handleMessage(PetController.MESSAGE_SET_HAPPINESS_TIMER, pet.getDefaultStatusTime()/2);
 			controller.handleMessage(PetController.MESSAGE_SET_HUNGER_TIMER, pet.getDefaultStatusTime()/2);
 		}
 		else
 		{
 			// Load pet status from file.
 			long getHappy = sharedPref.getLong("happinessTimeLeft", currentTime.toMillis(true));
 			if(getHappy != currentTime.toMillis(true) && ((getHappy - timePassed) > 0))
 			{
 				pet.setPetHappiness(sharedPref.getFloat("happinessStatus", 50));
 				happinessValue.setText(pet.getPetHappiness() + "%  ");
 				controller.handleMessage(PetController.MESSAGE_SET_HAPPINESS_TIMER, (getHappy - timePassed));
 			}
 			else
 			{
 				pet.setPetHappiness(0);
 				happinessValue.setText(pet.getPetHappiness() + "%  ");
 				controller.handleMessage(PetController.MESSAGE_PET_UNHAPPY);
 			}
 			
 			long getHungry = sharedPref.getLong("hungerTimeLeft", currentTime.toMillis(true));
 			if(getHungry != currentTime.toMillis(true) && ((getHungry - timePassed) > 0))
 			{
 				pet.setPetHunger(sharedPref.getFloat("hungerStatus", 50));
 				hungerValue.setText(pet.getPetHunger() + "%  ");
 				controller.handleMessage(PetController.MESSAGE_SET_HUNGER_TIMER, (getHungry - timePassed));
 			}
 			else
 			{
 				pet.setPetHunger(0);
 				happinessValue.setText(pet.getPetHunger() + "%  ");
 				controller.handleMessage(PetController.MESSAGE_PET_HUNGRY);
 			}
 			
 			pet.setLastTimeAte(sharedPref.getLong("lastTimeAte", 0));
 			pet.setLastTimePlayedWith(sharedPref.getLong("lastTimePlayedWith", 0));
 			
 			
 			
 		}
 		
 		// Set the sleep timer.
 		controller.handleMessage(PetController.MESSAGE_SET_SLEEP_TIMER);
 
 		// Set runaway timer		
 		Long time = determineRunaway(sharedPref, currentTime, timePassed);
 		if(time > 0)
 		{
 			// Update counter.
 			controller.handleMessage(PetController.MESSAGE_SET_RUNAWAY_TIMER, time);
 		}
 		
 		// Pet is home.
 		controller.handleMessage(PetController.MESSAGE_PET_RETURNING);
 		// Redraw
 		this.hView.loadBitmaps();
 		
 		updateView();
 		list = db.getAllPoop();
 		hView.drawPoop(list);
 	} // End method onResume
 	
 	
 	public long determineRunaway(SharedPreferences sharedP, Time currTime, long timeElapsed)
 	{
 		// Reset countdown timer
 		long timerRemaining = sharedP.getLong("runawayTimeLeft", pet.getDefaultRunawayTime());
 		Log.v("resume timerRemaining", Long.toString(timerRemaining));
 		
 		long updatedRunawayTimeLeft = -1;
 		
 		// If timeElapsed == currTime, there was no data in preferences file.
 		if(timeElapsed != currTime.toMillis(true)) 
 		{
 			// Pet ran away while the app was closed!
 			if(timeElapsed >= timerRemaining)
 			{
 				// Launch RunawayActivity.
 				Intent myIntent = new Intent(getBaseContext(), RunawayActivity.class);
 				myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 				startActivity(myIntent);
 			}
 			else
 			{
 				// Update counter to reflect time elapsed. Pet hasn't run away yet.
 				updatedRunawayTimeLeft = timerRemaining - timeElapsed;
 			}
 		}
 		else
 		{
 			// Set the time left to the default time. (New pet)
 			updatedRunawayTimeLeft = pet.getDefaultRunawayTime();
 		}
 		
 		return updatedRunawayTimeLeft;
 	} // End method determineRunaway
 	
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.home, menu);
 		return true;
 	}
 	
 	public int getWindowWidth() {
 		return this.windowWidth;
 	}
 	public int getWindowHeight() {
 		return this.windowHeight;
 	}
 	public int getPlaygroundWidth() {
 		return this.playgroundWidth;
 	}
 	public int getPlaygroundHeight() {
 		return this.playgroundHeight;
 	}
 	public int getMenuWidth() {
 		return this.menuWidth;
 	}
 	public int getMenuHeight() {
 		return this.menuHeight;
 	}
 	
 	private void showPlayOptions(final View v)
 	{
 		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
 		final String[] playOptions = {"Fetch", "Connect 4", "Go Back"};
 		dialogBuilder.setTitle("What would you like to play");
 		dialogBuilder.setItems(playOptions, new DialogInterface.OnClickListener(){
 		
 			@Override
 			public void onClick(DialogInterface dialog, int which){
 			
 				switch(which)
 				{
 					case 0: Toast.makeText(getApplicationContext(), "Playing Fetch", Toast.LENGTH_SHORT).show();
 							if(hView.ballInPlay)
 								hView.ballInPlay =false;
 							else
 								hView.ballInPlay = true;
 							
 							hView.setOnTouchListener(new BallListener());
 							break;
 		
 					case 1: Intent intent2 = new Intent(v.getContext(), Connect4Activity.class);
 							startActivityForResult(intent2, 0);
 							break;
 							
 					default: break;
 				}
 			}
 		});
 		AlertDialog alertDialog = dialogBuilder.create();
 		alertDialog.show();
 	}
 	
 	// Making fetch seem more fluid...
 	public void fetchTimer(int x, int y) {
 		
 		final int horizontal = x;
 		final int vertical = y;
 		
 		final int incrementX = Math.abs(x - pet.getXCoord())/4;
 		final int incrementY = Math.abs(y - pet.getYCoord())/4;
 		
 		
 		new CountDownTimer(3000, 1000) {
 
 		     public void onTick(long millisUntilFinished) {
 		    	 pet.setXYCoord(pet.getXCoord() + incrementX, pet.getYCoord() + incrementY);
 		     }
 
 		     public void onFinish() {
 		    	pet.setXYCoord(horizontal - pet.getWidth()/2, vertical - pet.getHeight());
 				//hView.removeBall();
 			}
 		  }.start();
 		 
 	}
 	
 	public void poopTimer() {
 		
 		new CountDownTimer(8000, 1000) {
 
 		     public void onTick(long millisUntilFinished) {
 		     }
 
 		     public void onFinish() {
 		    	// Spawn poop
 				drawPoop();
 			}
 		  }.start();
 		 
 	}
 
 /*	public void dirtTimer(){
 		new CountDownTimer(8000, 1000){
 			public void onTick(long millisUntilFinished){
 
 			}
 			public void onFinish(){
 				pet.makeDirty();
 			}
 		}.start();
 
 
 	}*/
 	public int isSpongeClicked(float userX, float userY){
 		
 		int margin = hView.getBackgroundWidth()/10;
 		if(userX >= spongeX - margin && userX <= spongeX + margin){
 			if(userY >= spongeY - margin && userY <= spongeY+margin){
 				spongeClicked = true;
 				
 
 				return 1;
 			}
 			spongeClicked = false;
 			return -1;
 		}
 		spongeClicked = false;
 		return -1;
 	}
 
 	public int isSpongeOnPet(float userX, float userY){
 		float petX = hView.petX();
 		float petY = hView.petY();
 		float margin = (float)hView.getPetWidth();
 		float ymargin = (float)hView.getPetHeight();
 		if(userX >= petX && userX <= petX+margin && userY >= petY && userY <= petY+ymargin){
 				return 1;			
 		}
 		return -1;
 	}
 
 	public int isPoopClicked(float userX, float userY) {
 		
 		int x, y, count = 0;
 		List<Poop> myList = db.getAllPoop(); 
 		for(Poop e: myList) {
 			x = e.getX();
 			y = e.getY();
 			
 			// Is click within pet image X-range
 			if ((userX >= x)
 					&& (userX <= x + hView.getPetWidth()/2)) {
 				Log.v("userx", Float.toString(userX));
 				Log.v("x", Integer.toString(x));
 			
 			    // Is click within pet image Y-range
 				if ((userY >= y) 
 						&& (userY <= y + hView.getPetHeight())) {
 				
 					// Pet has been selected
 					Log.v("userY", Float.toString(userY));
 					Log.v("y", Integer.toString(y));
 					poopIsClicked = true;
 					return count;
 				}
 			}
 			count++;
 		}
 		
 		poopIsClicked = false;
 		return -1;
 		
 	} // End method 
 	
 	// Define Listeners here
 	private class DefaultListener implements OnTouchListener {
 		@Override
 		public boolean onTouch(View v, MotionEvent event) {
 			switch (event.getAction()) {
 			case MotionEvent.ACTION_UP: {
 				final int pointerIndex = MotionEventCompat.getActionIndex(event); 
 				final float userX = MotionEventCompat.getX(event, pointerIndex); 
 				final float userY = MotionEventCompat.getY(event, pointerIndex); 
 		    
 				Point point = new Point(userX, userY);
 		    
 				controller.handleMessage(PetController.MESSAGE_TAPPED, point);
 				break;
 			}
 			}
 			return true;
 		}
 	
 	}
 	
 	private class ScoopPoopListener implements OnTouchListener {
 		public boolean onTouch(View v, MotionEvent event) {
 			
 			final int action = MotionEventCompat.getActionMasked(event); 
 				
 				switch (event.getAction()) {
 				case MotionEvent.ACTION_DOWN: {
 					final int pointerIndex = MotionEventCompat.getActionIndex(event); 
 			        final float userX = MotionEventCompat.getX(event, pointerIndex); 
 			        final float userY = MotionEventCompat.getY(event, pointerIndex); 
 			            
 			        // Check if something is clicked.
 			        id = isPoopClicked(userX, userY);
 		       
 			        //Log.v("petIsClicked, down", Boolean.toString(petIsClicked));
 			        
 			        // Remember where we started (for dragging)
 			    	mLastTouchX = userX;
 			        mLastTouchY = userY;
 			      
 			        // Save the ID of this pointer (for dragging)
 			        mActivePointerId = MotionEventCompat.getPointerId(event, 0);
 			        break;
 					
 				} // End case MotionEvent.ACTION_DOWN
 				
 			case MotionEvent.ACTION_MOVE: {
 			        // Find the index of the active pointer and fetch its position
 			        final int pointerIndex = 
 			                MotionEventCompat.findPointerIndex(event, mActivePointerId);  
 			            
 			        final float userX = MotionEventCompat.getX(event, pointerIndex);
 			        final float userY = MotionEventCompat.getY(event, pointerIndex);
 			            
 			        // Calculate the distance moved
 			       final float dx = userX - mLastTouchX;
 			       final float dy = userY - mLastTouchY;
 			       hView.setTrash(true);
 			       mPosX += dx;
 			       mPosY += dy;
 			       list = db.getAllPoop();
 			      
 			        // Invalidate
 			        if (poopIsClicked && id != -1) {
 			        
 			        	temp = list.get(id);  
 			        	list.remove(id);
 			        	temp.setX((int)userX);
 			        	temp.setY((int)userY);
 			        	temp.setID(id+1);
 			        	
 			        	if((temp.getX() > hView.getBackgroundWidth()/10 
 			        		&& temp.getX() < hView.getBackgroundWidth()/10 + hView.getPetWidth())
 			        		&& (temp.getY() >  + hView.getBackgroundHeight()/6
 			        		&& temp.getY() < hView.getBackgroundHeight()/6 + hView.getBackgroundHeight()/3 + hView.getPetHeight())) {
 			        			
 			        		list.remove(temp);
 		        		
 			        	}
 			        	else {
 			        		list.add(temp);
 			        	}
 			        	
 			        	hView.dragPoop(list);	        	
 			        }
 			
 			        // Remember this touch position for the next move event
 			       mLastTouchX = userX;
 		           mLastTouchY = userY;
 
 			        break;
 				}
 				
 				case MotionEvent.ACTION_UP: {
 					
 					db.deleteDatabse();
 					for(Poop e: list) {
 						db.addPoop(e);
 						
 					}
 			        mActivePointerId = INVALID_POINTER_ID;
 			        break;
 				}	
 			            
 			    case MotionEvent.ACTION_CANCEL: {
 			        mActivePointerId = INVALID_POINTER_ID;
 			        break;
 			    }
 			        
 			    case MotionEvent.ACTION_POINTER_UP: {
 			           
 			        final int pointerIndex = MotionEventCompat.getActionIndex(event); 
 			        final int pointerId = MotionEventCompat.getPointerId(event, pointerIndex); 
 			        if (pointerId == mActivePointerId) {
 			            // This was our active pointer going up. Choose a new
 			            // active pointer and adjust accordingly.
 			            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
 			            mLastTouchX = MotionEventCompat.getX(event, newPointerIndex); 
 			            mLastTouchY = MotionEventCompat.getY(event, newPointerIndex); 
 			            mActivePointerId = MotionEventCompat.getPointerId(event, newPointerIndex);
 			        }
 			       break;
 			    }
 			
 					
 				}
 				return true;
 
 			
 			
 			}	
 	}
 	
 	private class CleanListener implements OnTouchListener {
 			public boolean onTouch(View v, MotionEvent event){
 				final int action = MotionEventCompat.getActionMasked(event);
 
 				switch(event.getAction()){
 					case MotionEvent.ACTION_DOWN:{
 						
 						final int pointerIndex = MotionEventCompat.getActionIndex(event);
 						final float userX = MotionEventCompat.getX(event, pointerIndex);
 						final float userY =  MotionEventCompat.getY(event, pointerIndex);
 						/*Log.v("USER X", Float.toString(userX));
 						Log.v("USER Y", Float.toString(userY));
 						Log.v("isSpongeClicked", Integer.toString(isSpongeClicked(userX,userY)));*/
 					/*	Log.v("isSpongeOnPet", Integer.toString(isSpongeOnPet(userX,userY)));*/
 					
 			        	if(isSpongeClicked(userX,userY) == 1){
 			        		spongeX = userX;
 							spongeY = userY;
 						
 							id = isSpongeClicked(userX, userY);
 
 							mLastTouchX = userX;
 			        		mLastTouchY = userY;
 			        		hView.drawSponge(userX, userY);
 			        	}	
 			        	mActivePointerId = MotionEventCompat.getPointerId(event, 0);
 						//hView.drawSponge(userX, userY);
 						break;
 					}
 					case MotionEvent.ACTION_MOVE: {
 			        // Find the index of the active pointer and fetch its position
 			        final int pointerIndex = 
 			                MotionEventCompat.findPointerIndex(event, mActivePointerId);  
 			            
 			        final float userX = MotionEventCompat.getX(event, pointerIndex);
 			        final float userY = MotionEventCompat.getY(event, pointerIndex);
 			            
 			         // Calculate the distance moved
 			        final float dx = userX - mLastTouchX;
 			        final float dy = userY - mLastTouchY;
 			        
 			        mPosX += dx;
 			        mPosY += dy;
 			        
 			      
 			        // Invalidate
 			        if (isSpongeClicked(userX, userY) == 1) {
 			        
 			        	
 			        	/*temp.setX((int)userX);
 			        	temp.setY((int)userY);*/
 
 			        	spongeX = userX;
 			        	spongeY = userY;
 
 			        	hView.drawSponge(spongeX, spongeY);
 			        	if(isSpongeOnPet(spongeX, spongeY) == 1){
 			        		whatIsHappening = 0;
 							pet.makeClean();
 							hView.notCleaning();
 							pet.notCleaning();
 			        	}
 			        }
 			
 			        // Remember this touch position for the next move event
 			       mLastTouchX = userX;
 		           mLastTouchY = userY;
 
 			        break;
 				}
 					case MotionEvent.ACTION_UP: {
 					
 					hView.setTrash(false);
 					
 			        mActivePointerId = INVALID_POINTER_ID;
 			        break;
 				}	
 					case MotionEvent.ACTION_POINTER_UP: {
 			           
 			        final int pointerIndex = MotionEventCompat.getActionIndex(event); 
 			        final int pointerId = MotionEventCompat.getPointerId(event, pointerIndex); 
 			        if (pointerId == mActivePointerId) {
 			            // This was our active pointer going up. Choose a new
 			            // active pointer and adjust accordingly.
 			            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
 			            mLastTouchX = MotionEventCompat.getX(event, newPointerIndex); 
 			            mLastTouchY = MotionEventCompat.getY(event, newPointerIndex); 
 			            mActivePointerId = MotionEventCompat.getPointerId(event, newPointerIndex);
 			        }
 			       break;
 			    }
 				};
 				return true;
 			}
 	}
 	
 	
 	private class BallListener implements OnTouchListener
 	{
 
 		@Override
 		public boolean onTouch(View v, MotionEvent event) {
 			// TODO Auto-generated method stub
 			switch(event.getAction())
 			{
 					case MotionEvent.ACTION_DOWN:
 						final int pointerIndex = MotionEventCompat.getActionIndex(event);
 						final float userX = MotionEventCompat.getX(event, pointerIndex);
 						final float userY =  MotionEventCompat.getY(event, pointerIndex);
 						
 						//mLastTouch is just to know where we last clicked, not used in this instance
 						mLastTouchX = userX;
 				        mLastTouchY = userY;
 				        drawBall(userX, userY);
 				        //pet.setXYCoord((int)userX, (int)userY);
 			}
 			return true;
 		}
 		
 	}
 	//void feedPet() {};
 	//void bathePet() {};
 	//void removePoop() {};
 	//void notifyOwnerOfNeglect(){};
 	//void runawayPet(Pet pet) {};
 
 } // End class HomeActivity

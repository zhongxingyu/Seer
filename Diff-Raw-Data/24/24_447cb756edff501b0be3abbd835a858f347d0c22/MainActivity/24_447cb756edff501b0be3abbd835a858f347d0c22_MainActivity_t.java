 package com.tracme.localize;
 
 import java.util.concurrent.ExecutionException;
 
import com.tracme.R;
 import com.tracme.training.TestingTask;
 import com.tracme.util.*;
 
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.os.Messenger;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Canvas;
 import android.graphics.Matrix;
 import android.graphics.drawable.Drawable;
 import android.graphics.drawable.ShapeDrawable;
 import android.graphics.drawable.shapes.OvalShape;
 import android.util.AttributeSet;
 import android.util.DisplayMetrics;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.widget.ImageView;
 import android.widget.ProgressBar;
 import android.widget.SeekBar;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 
 /**
  * Main Activity for TracMe localization. 
  * This activity will handle user interaction with the floorplan/image,
  * as well as Message/Messenger/Message Handling for the intent service that will handle scanning for signals
  *  
  * @author Kwaku Farkye
  * @author Ken Ugo
  *
  */
 public class MainActivity extends Activity implements OnTouchListener {
 	
 	APTable apTable;
 	
 	/******** TODO: HOW ARE WE GOING TO SET THESE? *******/
 	String apfilename;
 	
 	/* Options for localization (set these in settings) */
 	LocalizeOptions options;
 	
 	/* Intent to start the LocalizeService */
 	Intent localizeIntent;
 	
 	private String rawFile = "cc1_76_nexus.txt"; // Name of the rawfile
 	private String trainFile = "train_p0.0.txt_sub_1.0.1.txt"; // Name of the training file
 	private int nX = 30; // Number of classes in x dimension
 	private int nY = 30; // Number of classes in y dimension
 	
 	AndroidLog localizationLog;
 	
 	/*********************END********************************/
 	
 	/* Progress Bar used to show initial loading of localization classes */
 	public ProgressBar initialProgBar;
 	public boolean doneLoading = false;
 	public int count = 1;
 	
 	double[] prediction = new double[2]; // Prediction of the corresponding point
 	double[] rssis;
 	
 	// Interface to localization classes provided by Dr. Tran
 	private TestingTask localize; 
 	
 	/***********************************************
 	 * Variables for the Image Manipulation aspect *
 	 *                                             *
 	 *                                             *
 	 ***********************************************/
 	
 	// Views for the Background Image and positioning Icon
 	private ImageView imgView;
 	private MyDrawableView myDView;
 	
 	private LocalizeDisplay ld;
 	
 	private int numScans = 1;
 	private int numScansPending;
 	
 	/****************** END *************************/
 	
 	/**
 	 *  Handler for message communication between main activity and signal scanning intent service 
 	 */
 	private class ScanHandler extends Handler {	
 		
 		/**
 		 * Method that is called when a message is received from a Messenger
 		 */
 		@Override
 		public void handleMessage(Message msg)
 		{
 			
 			//Receive the message and, using the information 
 			//received from the message, update the location of the user on the map 
 			if (msg.arg1 == RESULT_OK)
 			{
 				// Receive the bundle that was passed by the message
 				Bundle inData = msg.getData();
 				
 				// Receive the double array in the bundle, representing the results of the scan
 				rssis = inData.getDoubleArray(LocalizeService.SCANARRAY_KEY);
 				
 				// Call to Training interface: Predict the location
 				prediction = localize.getEstLocation(rssis);
 				
 				translatePoint(prediction);
 				
 				// Restart the service
 				initIntentService();
 						
 			}
 			else {
 				Toast.makeText(MainActivity.this, "Didnt receive anything back",
 						Toast.LENGTH_LONG).show();
 			}
 			return;
 		}
 	}
 	
 	private ScanHandler sHandler = new ScanHandler();
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);		
 		setContentView(R.layout.activity_two);
 
 		imgView = (ImageView) findViewById(R.id.imageView1);
 
 		myDView = (MyDrawableView) findViewById(R.id.circleView1);
 		myDView.setVisibility(View.INVISIBLE);
 
 		imgView.setOnTouchListener(this);
 		
 		initialProgBar = (ProgressBar) findViewById(R.id.progressBar1);
 		
 		// Set the max value of the progress bar to the number of classes that we must load
		initialProgBar.setMax(100);
 		
		setInitialValues();
		
		initTraining();
		
		Toast.makeText(this, "Localize", Toast.LENGTH_LONG)
		.show();
		
		initIntentService();
		ld = new LocalizeDisplay();
 		ld.drawable = getResources().getDrawable(R.drawable.cc_1);
 		ld.calcInitScale();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 	
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.action_settings:
 			showSeek();
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	public void showSeek() {
 		final TextView tvBetVal;
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		LayoutInflater inflater = this.getLayoutInflater();
 		View v = inflater.inflate(R.layout.dialog, null);
 		builder
 				.setView(v)
 				.setTitle(
 						"Enter the sampling speed (Note: Higher speeds may reduce accuracy)")
 				.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						// confirmValues();
 						// SAVE THE PROGRESS BAR VALUE SOMEWHERE
 						numScans = (numScansPending == 0) ? (1) : (numScansPending);
 						dialog.dismiss();
 					}
 				}).setNeutralButton("Cancel", null).show();
 		SeekBar sbBetVal = (SeekBar) v.findViewById(R.id.sbBetVal);
 		tvBetVal = (TextView) v.findViewById(R.id.tvBetVal);
 		sbBetVal.setMax(10);
 		sbBetVal.setProgress(numScans);
 		sbBetVal.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
 
 			@Override
 			public void onStopTrackingTouch(SeekBar seekBar) {
 				// TODO Auto-generated method stub
 
 			}
 
 			@Override
 			public void onStartTrackingTouch(SeekBar seekBar) {
 				// TODO Auto-generated method stub
 
 			}
 
 			@Override
 			public void onProgressChanged(SeekBar seekBar, int progress,
 					boolean fromUser) {
 				// TODO Auto-generated method stub
 				tvBetVal.setText(String.valueOf(progress));
 				numScansPending = progress;
 			}
 		});
 	}	
 	@Override
 	public void onDestroy()
 	{
 		super.onDestroy();
 		stopService(localizeIntent);
 	}
 	
 	public boolean onTouch(View v, MotionEvent event) {
 		DisplayMetrics metrics = new DisplayMetrics();
 		getWindowManager().getDefaultDisplay().getMetrics(metrics);
 
 		// handle touch events here
 		ImageView view = (ImageView) v;
 
 		switch (event.getAction() & MotionEvent.ACTION_MASK) {
 		case MotionEvent.ACTION_DOWN:
 			ld.actionDown(event);
 			ld.mode = LocalizeDisplay.DRAG;
 			break;
 		case MotionEvent.ACTION_POINTER_DOWN:
 			System.out.println("Subsequent presses");
 			ld.pointerDown(event);
 			
 			ld.oldDist = ld.spacing(event);
 			if (ld.oldDist > 10f) {
 				ld.savedMatrix.set(ld.matrix);
 				ld.midPoint(ld.mid, event);
 				ld.mode = LocalizeDisplay.ZOOM;
 			}
 			break;
 		case MotionEvent.ACTION_UP:
 		case MotionEvent.ACTION_POINTER_UP:
 			ld.checkEdgeCases(view);
 
 			ld.mode = LocalizeDisplay.NONE;
 			break;
 		case MotionEvent.ACTION_MOVE:
 			if (ld.mode == LocalizeDisplay.DRAG) {
 				ld.drag(event);
 			}
 			else if (ld.mode == LocalizeDisplay.ZOOM) {
         ld.zoomAndRotate(event, view);
 			}
 			break;
 		}
 
 		ld.matrix.getValues(ld.eventMatrix);
 
 		/* The point specified will be given by the localization function */
 		plotPoint(365, 261);
 		view.setImageMatrix(ld.matrix);
 		return true;
 	}	
 	
 	/**
 	 * Initialize the information that will be sent to the service.
 	 * Once the data is bundled within the intent, start the service.
 	 */
 	public void initIntentService() {
 		// Create a Messenger for communication back and forth
 		Messenger messenger = new Messenger(sHandler);
 		// Add the Messenger info to the intent, so the
 		// intent service knows how who to give the message to
 		localizeIntent.putExtra(LocalizeService.MESSENGER_KEY, messenger);
 		localizeIntent.putExtra(LocalizeService.OPTIONS_KEY, options);
 		localizeIntent.putExtra(LocalizeService.APTABLE_KEY, apTable);
 		localizeIntent.putExtra(LocalizeService.COUNT_KEY, count++);
 		localizeIntent.putExtra("PATH", "Hello World");
 		Toast.makeText(this, "IntentService", Toast.LENGTH_LONG)
 		.show();
 		startService(localizeIntent);
 	}
 
 	private void translatePoint(double[] prediction)
 	{
 		String res = "Predicted Location: " + prediction[0] + "," + prediction[1];
 		localizationLog.save(res + "\n");
 		Toast.makeText(MainActivity.this,
 				res, Toast.LENGTH_LONG)
 				.show();
 	}
 	
 	/**
 	 * Instantiate and set the initial values for objects/variables
 	 * used
 	 */
 	private void setInitialValues()
 	{
 		localizationLog = new AndroidLog("loc_first_run" + ".txt");
 		// Load AP Table
 		apfilename = "apcc1_76_nexus";
 		apTable = new APTable(apfilename);
 		apTable.loadTable();
 		
 		Toast.makeText(this, "AP Table Loaded " + Integer.valueOf(apTable.getAPTable().size()).toString(), Toast.LENGTH_LONG).show();
 		
 		//Initialize options
 		options = new LocalizeOptions();
 		localizeIntent = new Intent(this, LocalizeService.class);
 		
 	}
 	
 	/**
 	 * Initialize the training interface and all that is necessary to predict a location
 	 * 
 	 * @throws ExecutionException 
 	 * @throws InterruptedException 
 	 * 
 	 */
 	private void initTraining()
 	{
 		initialProgBar.setVisibility(View.VISIBLE);
 		System.out.println("GOING TO ESTIMATE LOCATION...");
 		localize = new TestingTask(rawFile, trainFile);
 		localize.setProgBar(initialProgBar);
 		//System.out.println("SETTING CLASSES");
 		//localize.execute(nX, nY, this.initialProgBar).get();
 		localize.setNumClasses(nX, nY);
 	}
 	
 	/*
 	 * Function that will plot the point correctly regardless of scale or position
 	 */
 	private void plotPoint(float x, float y) {
 		// TODO adjust for rotation
 
 		myDView.setVisibility(View.INVISIBLE);
 		myDView.setX(ld.getAdjustedX(x));
 		myDView.setY(ld.getAdjustedY(y));
 		myDView.setVisibility(View.VISIBLE);
 		return;
 	}	
 }
 
 /*
  * Tracking circle
  */
 class MyDrawableView extends View {
 	private ShapeDrawable mDrawable;
 
 	public MyDrawableView(Context context) {
 		super(context);
 
 		int x = 10;
 		int y = 10;
 		int width = 300;
 		int height = 50;
 
 		mDrawable = new ShapeDrawable(new OvalShape());
 		mDrawable.getPaint().setColor(0xff74AC23);
 		mDrawable.setBounds(x, y, x + width, y + height);
 	}
 
 	public MyDrawableView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 
 		int x = 0;
 		int y = 0;
 		int width = 50;
 		int height = 50;
 
 		mDrawable = new ShapeDrawable(new OvalShape());
 		mDrawable.getPaint().setColor(0xff74AC23);
 		mDrawable.setBounds(x, y, x + width, y + height);
 	}
 
 	protected void onDraw(Canvas canvas) {
 		mDrawable.draw(canvas);
 	}
 }

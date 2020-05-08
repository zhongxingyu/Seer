 package edu.upenn.cis350.algoviz;
 
 import edu.upenn.cis350.algoviz.R;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.os.SystemClock;
 import android.view.View;
 import android.widget.Chronometer;
 import android.widget.TextView;
 
 public class BinPackingActivity extends Activity {
 	
 	String _problemName;
 	int[] _finished;
 	BinPackingProblemFactory _factory;
 	
 	private Chronometer _mChronometer;
 	
 	public static final String PREFS_NAME = "MyPrefsFile";
 
 	private static final int READY_DIALOG = 1;
 	private static final int CORRECT_DIALOG = 2;
 	private static final int INCORRECT_DIALOG = 3;
 	
 	private CharSequence _text;
 	
 	private long _mtime1, _mtime2;
 	private SharedPreferences scores;
 	
 	private double _percent;
 
 	
 	/** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
    		super.onCreate(savedInstanceState);
    		setContentView(R.layout.bin_packing);
    		_problemName = "Baby Packer";
    		_finished = new int[6];
    		showDialog(READY_DIALOG);
    		
    		_percent=0.0;
    		_mChronometer=(Chronometer) findViewById(R.id.chronometer1);
    		
    		
    		scores = getSharedPreferences(PREFS_NAME, 0);
    		
       
     }
     
     public String getProblemName() {
     	return _problemName;
     }
     
     public void onNextLevelClick(View v){
     	nextLevelHelper();
     	showDialog(READY_DIALOG);
     }
     
     
     
     private void nextLevelHelper(){
     	if (!"Pack Master".equalsIgnoreCase(_problemName)){ //There's a next level to be played
     		BinPackingView view = ((BinPackingView) this.findViewById(R.id.binview));
     		_factory = view.getFactory();
     		
     		_problemName = nextProblem(_problemName);
     		
     		view.reset();
     	
     		TextView count_text=(TextView)findViewById(R.id.textView2);
     		count_text.setText(_problemName);
     		
     	}
     	else
     	{
     		/*CharSequence text = "Congratulation! All levels finished!!!";
     		int duration = Toast.LENGTH_SHORT;
     		CharSequence text2 = "Not all level finished, go back to finish.";
     		Toast toast1 = new Toast(getApplicationContext());
     		toast1.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
     		toast1.setDuration(Toast.LENGTH_LONG);
     		toast1.setText(text);
     		toast1.show();*/
     		
     	}
     	
     	
     }
     
     
     @SuppressWarnings("deprecation")
 	public void onDoneClick(View v){
     	double result=((BinPackingView) this.findViewById(R.id.binview)).submit();
     	
     	if (result>=1){
     		
     		_mtime2=System.currentTimeMillis();
         	int stime=(int) ((_mtime2-_mtime1)/1000.0);
         	
         	String str1=((Integer)stime).toString();
         	int result2=storeScore(stime);
         	
 
         	if (result2==1){
         		_text = "It's correct! You used "+str1+" seconds this time. You are the new top score! Click Yes to next level.";
         	}
         	else{
     			_text = "It's correct! You used "+str1+" seconds this time. Click Yes to next level.";  		
         	}
         	removeDialog(CORRECT_DIALOG);
         	showDialog(CORRECT_DIALOG);
     	
     	}
     	else{
     		_percent=result;
     		showDialog(INCORRECT_DIALOG);}
     }
     
     public void onResetClick(View v){
     	((BinPackingView) this.findViewById(R.id.binview)).reset();
     }
     
     public void onBackClick(View v){
     	if (!"Baby Packer".equalsIgnoreCase(_problemName)){
     	String previousProblem = previousProblem(_problemName);
     	if (previousProblem != null){
     		_problemName = previousProblem;
         	
     		((BinPackingView) this.findViewById(R.id.binview)).reset();
     	
     		TextView count_text=(TextView)findViewById(R.id.textView2);
    		count_text.setText(_problemName);
 
     	}
     	showDialog(READY_DIALOG);
     	}
     	else{
     		super.onBackPressed();    		
     	}
     }
     
     
     private String nextProblem(String problemName) {
     	boolean sawProblem = false;
 		for (String prob : _factory.getProblemNames()) {
 			if (sawProblem) return prob;
 			if (prob.equalsIgnoreCase(problemName)) sawProblem = true;
 		}
 		return null;
     }
     
     private String previousProblem(String problemName) {
     	String prevProblem = null;
 		for (String prob : _factory.getProblemNames()) {
 			if (prob.equalsIgnoreCase(problemName)) break;
 			prevProblem = prob;
 		}
 		return prevProblem;
     }
     
     private int getLevelCount() {
     	int count = 0;
     	for (String prob : _factory.getProblemNames()) {
     		count++;
     		if (_problemName.equalsIgnoreCase(prob)) break;
     	}
     	return count;
     }
     
     
     
     
     protected Dialog onCreateDialog(int id, Bundle savedInstanceState) {
     	if (id == READY_DIALOG) {
 	    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
                 // this is the message to display
 	    	builder.setMessage(R.string.ready); 
                 // this is the button to display
 	    	builder.setPositiveButton(R.string.yes,
 	    		new DialogInterface.OnClickListener() {
                            // this is the method to call when the button is clicked 
 	    	           public void onClick(DialogInterface dialog, int id) {
                                    // this will hide the dialog
 	    	        	   
 	    	        	   _mChronometer.setBase(SystemClock.elapsedRealtime());
 	    	        	   _mChronometer.start();
 	    	        	   _mtime1=System.currentTimeMillis();
 	    	        	   dialog.cancel();
 	    	           }
 	    	         });
     		return builder.create();
     	}
     	else
     	//create the correct dialog
     	if (id==CORRECT_DIALOG){
     		AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
     		builder2.setMessage(_text);  
    		
             // this is the button to display
     		builder2.setPositiveButton(R.string.yes,
     		new DialogInterface.OnClickListener() {
                        // this is the method to call when the button is clicked 
     	           public void onClick(DialogInterface dialog, int id) {
                                // this will hide the dialog
     	        	      	   
     	        	   showDialog(READY_DIALOG);
     	        	   _mChronometer.setBase(SystemClock.elapsedRealtime());
     	        	   nextLevelHelper();
     	        	   dialog.cancel();   
     	           }
     	         });
     		return builder2.create();
     		
     	}
     	else
     		//create the incorrect dialog
     		if (id==INCORRECT_DIALOG){
     		
             	double p=_percent*100;
             	String str1=((Double)p).toString();
     			//CharSequence text = str1+"% to the best solution. Click on Yes to restart.";
             	CharSequence text = "Incorrect. Click on Yes to restart.";
     			_mChronometer.stop();
         		AlertDialog.Builder builder3 = new AlertDialog.Builder(this);
                 // this is the message to display
         		builder3.setMessage(text); 
                 // this is the button to display
         		builder3.setPositiveButton(R.string.yes,
         		new DialogInterface.OnClickListener() {
                            // this is the method to call when the button is clicked 
         	           public void onClick(DialogInterface dialog, int id) {
                                    // this will hide the dialog
         	        	  
         	        	   ((BinPackingView) findViewById(R.id.binview)).reset();
         	        	   _mChronometer.start();
         	        	   dialog.cancel();
         	        	   
         	           }
         	         });
         		return builder3.create();
         		
         	}
     	
     	else return null;
     }
 
     private int storeScore(int time){
     	scores = getSharedPreferences(PREFS_NAME, 0);
     	
     	int currScore=scores.getInt(_problemName, -1);
     	
     	if ((currScore<0)){
     	    	SharedPreferences.Editor editor = scores.edit();
     	    	editor.putInt(_problemName, time);
 
     	    	// Commit the edits!
     	    	editor.commit();
     	    	return 1;}
     	else{
     		if (currScore>time){
     			SharedPreferences.Editor editor = scores.edit();
     			editor.putInt(_problemName, time);
     			editor.commit();
     			return 1;    			
     		}
     		else{
     			
     			return -1;
     			
     			
     		}
     	}
 
     }
     
 }
 
 

 package com.BizQuiz;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONException;
 import org.json.JSONObject;
 import com.BizQuiz.R;
 import com.BizQuiz.R.id;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.DialogInterface.OnCancelListener;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.CountDownTimer;
 import android.os.Handler;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class QuesFetch extends Activity {
 
 	int qid;
 	int catid;
 	Button previous;
 	Button submit;
 	Button next;
 	EditText etanswer;
 	TextView tvquestion;
 	TextView timer;
 	AlertDialog.Builder builder;
 	Handler mHandler; 
 	Runnable mUpdateUITimerTask;
 	Context context;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qfetch);
        
        context=this;
        mHandler = new Handler();
        previous=(Button) findViewById(R.id.previous);
        submit=(Button) findViewById(R.id.submit);
        next=(Button) findViewById(R.id.next);
        tvquestion=(TextView) findViewById(R.id.question);
        etanswer=(EditText) findViewById(R.id.answer);
        timer=(TextView) findViewById(R.id.timer);
        
        qid=QuizDetails.getqid();
    	   catid=QuizDetails.getcategory();       
 	   Log.d("id + questionNo.",Integer.toString(catid)+"+"+Integer.toString(qid));
        
 	   mCountDown.start();
 	   timer.setText(2+":"+59);
 	   
 	if(catid==1)
 	{
 		new Questionfetch(QuesFetch.this).execute("Category1");
 	}else if(catid==2)
 	{
 		new Questionfetch(QuesFetch.this).execute("Category2");
 	}else if(catid==3){
 		new Questionfetch(QuesFetch.this).execute("Category3");
 	}else if(catid==4){
 		new Questionfetch(QuesFetch.this).execute("Category4");
 	}else if(catid==5){
 		new Questionfetch(QuesFetch.this).execute("Category5");
 	}else if(catid==6){
 		new Questionfetch(QuesFetch.this).execute("Category6");
 	}else if(catid==7){
 		new Questionfetch(QuesFetch.this).execute("Category7");
 	}else if(catid==8){
 		new Questionfetch(QuesFetch.this).execute("Category8");
 	}
 
 	
 		previous.setEnabled(false);
 	
   previous.setOnClickListener(new OnClickListener() {
 	
 	@Override
 	public void onClick(View v) {
 		
 		qid=QuizDetails.getqid();
 	  	QuizDetails.setqid(qid-1);
 	  		 
 	  	new Questionfetch(QuesFetch.this).execute("Category1");
 		
 	}
 });
   
   submit.setOnClickListener(new OnClickListener() {
 	
 	@Override
 	public void onClick(View v) {
 		
 		String ansbyuser=etanswer.getText().toString();
 		
 	    validateanswer(ansbyuser);
 	}
 });
  
   next.setOnClickListener(new OnClickListener() {
 		
 		@Override
 		public void onClick(View v) {
 		
 			qid=QuizDetails.getqid();
 		  	QuizDetails.setqid(qid+1);
 		  	previous.setEnabled(true);
 		  	
 		  	new Questionfetch(QuesFetch.this).execute("Category1");
 			
 		}
 	});
 
   	}
 	
 	
 class Questionfetch extends AsyncTask<String, Void, Boolean> {
 
 	Context context;
 	ProgressDialog pDialog;
 	JSONObject jsonObject = new JSONObject();
 	List<NameValuePair> params = new ArrayList<NameValuePair>();
     JSONParser jsonParser = new JSONParser();
     String url="http://practice.site11.com/questionfetch.php";
     String nameofcat;
     String question;
     String qanswer;
     int quesid;
     
     public Questionfetch(Context ctx) {
     	this.context = ctx;
         pDialog= new ProgressDialog(context);
 	}
 
 	@Override
 	protected void onPreExecute(){
 		pDialog.setTitle("Please Wait...");
 		pDialog.setMessage("Loading Question");
 		pDialog.setIndeterminate(true);
 		pDialog.show();
 	}
 	
 	@Override
 	protected Boolean doInBackground(String... str) {
 		nameofcat=str[0];
 		
 		Log.d("Category + QuestionNo",nameofcat+" "+Integer.toString(qid));
 		
 		params.add(new BasicNameValuePair("selectedcat",nameofcat));
 		params.add(new BasicNameValuePair("QuestionNO",Integer.toString(QuizDetails.getqid())));
 		jsonObject = jsonParser.makeHttpRequest(url, "GET", params);
 		
 		try {
 			 quesid = jsonObject.getInt("id");
 			 question=jsonObject.getString("question");
 			 qanswer=jsonObject.getString("answer");
 			
 			Log.d("qid",Integer.toString(qid));
 			Log.d("question+answer",question+" + "+qanswer);
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		
 		return true;
 		
 	}
 	 
 	protected void onPostExecute(Boolean b){
 		
 		
 		QuizDetails.setques(question);
 		QuizDetails.setans(qanswer);
 		tvquestion.setText(QuizDetails.getques());		
 		
 		pDialog.dismiss();
 	   
 	}
 
 }
 
     void validateanswer(String useranswer){
 	
       	ProgressDialog pDialog;
      	pDialog= new ProgressDialog(this);
      	pDialog.setTitle("Please Wait...");
 	    pDialog.setMessage("Validating Answer");
 	    pDialog.setIndeterminate(true);
 	    pDialog.show();
 	
 	    int length=useranswer.length();
 	    int threshvalue=length/2+1;
 	    
 	    Log.d("ThreshValue",Integer.toString(threshvalue));
 	    Boolean check=compareanswer(threshvalue,useranswer);
 	      
 	    if(check){
	  //       	Intent intent=new Intent(this,QuesFetch.class);
 	  	        QuizDetails.setqid(QuizDetails.getqid()+1);
 	  	        Toast.makeText(this, "Correct Answer", Toast.LENGTH_LONG).show();
 	  	        QuizDetails.set_score(QuizDetails.getscore()+1);
	  	        pDialog.dismiss();
	  	        
	  	      new Questionfetch(QuesFetch.this).execute("Category1");
	 // 	        startActivity(intent);
 	    }else{
 
		//          Intent intent=new Intent(this,QuesFetch.class);
 		          QuizDetails.setqid(QuizDetails.getqid());
 	  	          Toast.makeText(this, "Incorrect Answer.Correct Answer is"+QuizDetails.getans(), Toast.LENGTH_LONG).show();
	  	 //         startActivity(intent);
	  	          
	  	        new Questionfetch(QuesFetch.this).execute("Category1");
	  	        pDialog.dismiss();
         }
 
      }
 	
     Boolean compareanswer(int threshval,String ans){
     	int counter = 0;
     	ans=ans.toLowerCase();
     	String correctans=QuizDetails.getans().toLowerCase();
     	
     	for(int i=0;i<ans.length();i++){
     		
     		if(ans.charAt(i)==correctans.charAt(i)){
     		   counter++;
     		}
     		
     	}
     	
     	     if(threshval<=counter){
     	    	      return true;
     	     }else
     	     {
                       return false;  
              }
    
     }
     
     @Override
 	protected void onResume(){
 	
 		super.onResume();	
 		mUpdateUITimerTask = new Runnable() {
 			int min=2,sec=59;
 			public void run() {
 		    	
 		        
 		        timer.setText(Integer.toString(min)+":"+Integer.toString(sec));
 		        sec--;
 		        if(sec==0)
 		        {
 		        	min--;
 		        	sec=59;
 		        }
 		    }
 		};
 	}
 	
 	@Override
 	protected void onPause(){
 		mHandler.removeCallbacks(mUpdateUITimerTask);
 		mCountDown.cancel();
 		super.onPause();
 		
 		
 	}
     
     protected CountDownTimer mCountDown = new CountDownTimer(178000, 1000)
     {
 
         @Override
         public void onTick(long millisUntilFinished)
         {
         	
         	Log.d("Timer","time for 3mins");
         	mHandler.post(mUpdateUITimerTask);
         	            
         	        }
 
         @Override
         public void onFinish()
         {   
         	Log.d("Timer","3mins finish");
         	builder = new AlertDialog.Builder(context);
     		builder.setMessage(
     				"Sorry your time is over.Press Ok to play again and Back to Quit")
     				.setCancelable(true)
     				.setOnCancelListener(new OnCancelListener() {
     					
     					public void onCancel(DialogInterface dialog) {
     						// TODO Auto-generated method stub
     						mCountDown.cancel();
     						Intent returnhome=new Intent(QuesFetch.this,Categories.class);
     						startActivity(returnhome);
     						
     					}
     				})
     				.setPositiveButton("Ok",new DialogInterface.OnClickListener() {
     					
     					public void onClick(DialogInterface dialog, int which) {
     						// TODO Auto-generated method stub
     						Intent playconti=new Intent(QuesFetch.this,QuesFetch.class);
     						startActivity(playconti);
     						
     					}
     				})
     				.show();
             }
         };
     
         @Override
         public void onBackPressed() {
            Log.d("CDA", "onBackPressed Called");
            Intent back = new Intent(QuesFetch.this,Categories.class);
 //           setIntent.addCategory(Intent.CATEGORY_HOME);
 //           setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(back);
         }
         
         @Override
         public boolean onTouchEvent(MotionEvent event) {
             InputMethodManager imm = (InputMethodManager)getSystemService(Context.
                                                             INPUT_METHOD_SERVICE);
             imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
             return true;
         }
 } 
 

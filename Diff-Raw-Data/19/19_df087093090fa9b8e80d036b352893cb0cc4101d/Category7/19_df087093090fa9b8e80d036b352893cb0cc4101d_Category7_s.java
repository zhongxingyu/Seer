 package in.bizquiz.app;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.text.method.ScrollingMovementMethod;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class Category7 extends Activity {
 	
 	JSONArray jArray=new JSONArray();
 	TextView fact;
 	Button cat7_prev;
 	Button cat7_next;
 	String[] array_fact;
 	int index=0;
 	int month_id;
 	TextView name_of_quiz;
 	
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cat7);
        
        
        Calendar c = Calendar.getInstance();   // this takes current date
        int day_of_month=c.get(Calendar.DAY_OF_MONTH);
        int month=c.get(Calendar.MONTH);
        Log.d("day_of_month",Integer.toString(day_of_month));
        
        switch(month){
 	     
 	     case 0: month_id=4;
 	             day_of_month=31+30+31+day_of_month;
 	             break;
 	     case 1: month_id=5;
 	             day_of_month=31+30+31+31+day_of_month;
 	             break;
 	     case 2: month_id=6;
 	             day_of_month=31+30+31+31+28+day_of_month;
 	              break;
 	     case 3: month_id=7;
 	             day_of_month=31+30+31+31+28+31+day_of_month;
 	             break;
 	     case 4: month_id=8;
 	             day_of_month=31+30+31+31+28+31+30+day_of_month;
 	             break;
 	     case 5: month_id=9;
 	             day_of_month=31+30+31+31+28+31+30+31+day_of_month;
 	             break;
 	     case 6: month_id=10;
 	             day_of_month=31+30+31+31+28+31+30+31+30+day_of_month;
 	             break;
 	     case 7: month_id=11;
 	             day_of_month=31+30+31+31+28+31+30+31+30+31+day_of_month;
 	             break;
 	     case 8: month_id=12;
 	             day_of_month=31+30+31+31+28+31+30+31+30+31+31+day_of_month;
 	             break; 
 	     case 9: month_id=1;
 	             break;
 	     case 10: month_id=2;
 	              day_of_month=31+day_of_month;
 	             break;
 	     case 11: month_id=3;
 	              day_of_month=31+30+day_of_month;         
 	                 break;
 	     	     
 	     default: month_id=0;
 	     }
 
        
        name_of_quiz=(TextView) findViewById(R.id.heading);
        fact=(TextView) findViewById(R.id.fact);
        fact.setMovementMethod(new ScrollingMovementMethod());
        cat7_prev=(Button) findViewById(R.id.cat7_prev);
        cat7_next=(Button) findViewById(R.id.cat7_next);
        
        name_of_quiz.setText("Biz Jargons");
        new Facts(Category7.this).execute(Integer.toString(day_of_month));
        
        
        if(index==0){
     	   cat7_prev.setEnabled(false);
        }
        
        cat7_prev.setOnClickListener(new OnClickListener() {
 		
 		@Override
 		public void onClick(View v) {
 			 
 			index=index-1;
 			setFact();
 			
 		}
 	});
        
        cat7_next.setOnClickListener(new OnClickListener() {
    		
    		@Override
    		public void onClick(View v) {
    			 
    			index=index+1;
    			setFact();
    			
    		}
    	});
 	}
 	
 	
 	class Facts extends AsyncTask<String, Void, Boolean> {
 
 		Context context;
 		ProgressDialog pDialog;
 		List<NameValuePair> params = new ArrayList<NameValuePair>();
 	    JSONParser jsonParser = new JSONParser();
 	    String url="http://bizquiz.in/BizQuiz/Category7.php";
 	    
 	    JSONObject json=new JSONObject();       
 	       
 	   public Facts(Context ctx) {
 	    	this.context = ctx;
 	        pDialog= new ProgressDialog(context);
 		}
 
 		@Override
 		protected void onPreExecute(){
 			pDialog.setTitle("Please Wait...");
 			pDialog.setMessage("Loading..");
 			pDialog.setIndeterminate(true);
 			
 			pDialog.show();
 		}
 		
 		@Override
 		protected Boolean doInBackground(String... str) {
 			
 			
 			params.add(new BasicNameValuePair("day_of_month",str[0]));
 			params.add(new BasicNameValuePair("month_id",Integer.toString(1)));
 			
 			json = jsonParser.makeHttpRequest(url, "GET", params);
 					
 			try {
 				jArray= json.getJSONArray("fact");
 				System.out.println("*****JARRAY*****"+jArray.length());
 				
 				array_fact=new String[jArray.length()];
 				
 				for(int i=jArray.length()-1,j=0;i>=0;i--,j++){
 				
 				JSONObject json_data = jArray.getJSONObject(i);
                 
 				array_fact[j]=json_data.getString("Fact");
 								
 				Log.i("log_tag","fact"+json_data.getString("Fact"));
                   
 				}			
 			} catch (JSONException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}	
 			
 			
 			return true;
 			
 		}
 		 
 		protected void onPostExecute(Boolean b){
 			
 			
 			pDialog.dismiss();
 			setFact();
 			
 	       						
 		}
 		
 	}
 
 void setFact(){
 	
 	if(index==0){
  	   cat7_prev.setEnabled(false);
     }else{
     	cat7_prev.setEnabled(true);
     }
 	if(index==jArray.length()-1){
 		cat7_next.setEnabled(false);
 	}else{
 		cat7_next.setEnabled(true);
 	}
 		
     	
 	fact.setText(array_fact[index]);
 }
 
 @Override
 public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_home, menu);
 		return true;
 	}
     
 
   
 public boolean onOptionsItemSelected(MenuItem item) {
 
 		switch (item.getItemId()) {
 		case R.id.menu_settings:
 			startActivity(new Intent(this, Settings.class));
 			finish();
 			return true;
 		case R.id.menu_exit:
 			Intent intent = new Intent(Intent.ACTION_MAIN);
 			intent.addCategory(Intent.CATEGORY_HOME);
 			finish();
 			startActivity(intent);
 			return true;	
 		case R.id.menu_feedback:
 			Intent intent1=new Intent(this,Feedback.class);
 			finish();
 			startActivity(intent1);
 			return true;
 		case R.id.menu_statistics:
 		     startActivity(new Intent(this,Statistics.class));
 		     finish();
 		     return true;
 		case R.id.menu_archive:
 			Intent intent3 = new Intent(this,ArchiveMonthsActivity.class);
 			finish();
 			startActivity(intent3);
 			return true;	
 		
 		     
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 
    }
 
 @Override
 public void onBackPressed() {
    Log.d("CDA", "onBackPressed Called");
    Intent back = new Intent(this,Categories.class);
 //   setIntent.addCategory(Intent.CATEGORY_HOME);
 //   setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    startActivity(back);
 }
 
 }

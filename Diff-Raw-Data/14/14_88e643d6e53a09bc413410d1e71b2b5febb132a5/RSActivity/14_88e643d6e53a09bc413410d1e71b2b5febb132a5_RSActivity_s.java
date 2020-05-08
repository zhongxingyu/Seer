 package com.beef.repplan;
 
 
 import java.util.ArrayList;
 
 
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 
 import android.content.Context;
 
 import android.view.View;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.AdapterView;
 
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class RSActivity extends Activity {
 final int algoc = 14;
 final int year = 2013;
 int day = 0;
 int month = 0;
 String finaldate;
 ArrayList<String> spinnerArray = new ArrayList<String>();
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		
  		final Context context = this;
  		
 		
 		createTime();
 		final boolean staus1;
 		
 			//Toast toast = Toast.makeText(this, "check internet connection", Toast.LENGTH_LONG);
 			//toast.show();
 		
 		Spinner s1 = (Spinner) findViewById(R.id.spinner1);
 		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, spinnerArray);
 		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		s1.setAdapter(adapter);
 		 s1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
 				
 			 public void onItemSelected(AdapterView<?> parent,View v, int position, long id){
 				 String item = spinnerArray.get(position);
 				load(item);
 			
 			
 				
 				 
 
 			 }
 			 public void onNothingSelected(AdapterView<?> parent){
 				 
 			 }
 		 
 			 
 	});
 		
 }
 	public void showToast(){
 		Toast toast = Toast.makeText(this, "loading Image", Toast.LENGTH_SHORT);
 		toast.show();
 	}
 	
 	public void createTime(){
 		for(int i =0;i < algoc; i++){
 			day++;
 			if(day ==  1){
 				month++;
 			}
 			String dateval;
 			if(day > 9){
 				dateval = String.valueOf(day);
 			}
 			else{
 				dateval = "0" + String.valueOf(day);
 			}
 			finaldate = dateval + "." +"0"+ String.valueOf(month) + "." + String.valueOf(year);
 			spinnerArray.add(finaldate);
 		}
 	}
 	public void load(String position){
 		final ProgressDialog dialog = new ProgressDialog(this);
		WebView webView = (WebView) findViewById(R.id.webView1);
 		dialog.setTitle("Loading!");
 		dialog.setMessage("Take a coffe!");
 		dialog.show();
		webView.loadUrl("http://recklessfistsss.funpic.de/IGS/"+position+".jpeg");
 		webView.setWebViewClient(new WebViewClient() {
 	        @Override
 	        public void onPageFinished(WebView view, String url) {                  
 	            if (dialog.isShowing()) {
 	               dialog.dismiss();
 	           }
 	        }
 	    });
 		
 	}
 	
 	
 
 }

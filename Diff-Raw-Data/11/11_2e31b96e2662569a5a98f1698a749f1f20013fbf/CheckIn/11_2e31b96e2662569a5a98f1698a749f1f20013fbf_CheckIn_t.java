 package edu.ntu.mpp.keymap;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.MalformedURLException;
 
 import org.json.JSONArray;
 
 import com.facebook.android.AsyncFacebookRunner;
 import com.facebook.android.Facebook;
 import com.facebook.android.FacebookError;
 import com.facebook.android.AsyncFacebookRunner.RequestListener;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class CheckIn extends Activity{
 	static Facebook facebook=KeyMap.facebook;
 	static AsyncFacebookRunner fbAsyncFacebookRunner = KeyMap.fbAsyncFacebookRunner;
 	private Button back,send;
 	TextView loc;
 	EditText text;
 	Intent intent;
 	String token="";
 	private String provider;
 	private LocationManager locationManager;
 	
 	private RequestListener postlistener = new RequestListener(){
 		@Override
 		public void onMalformedURLException(MalformedURLException e,Object state){
 			Log.e("error mal",e.toString());
 		}
 		
 		@Override
 		public void onIOException(IOException e, Object state){
 			Log.e("error io",e.toString());
 		}
 		
 		@Override
 		public void onFileNotFoundException(FileNotFoundException e, Object state){
 			Log.e("error file",e.toString());
 		}
 		@Override
 		public void onFacebookError(FacebookError e, Object state){
 			Log.e("error fb",e.toString());
 		}
 		@Override
 		public void onComplete(String response, Object state){
 			CheckIn.this.runOnUiThread(new Runnable(){
 				@Override
 				public void run(){
 
					Toast.makeText(CheckIn.this, "上傳成功", Toast.LENGTH_LONG).show();
 					finish();
 				}
 			});
 		}
 	};
 	
 	@Override
 	public void onActivityResult(int requestCode, int resultCode,Intent data){
 		super.onActivityResult(requestCode, resultCode, data);
 		//Log.e("ya", "123@");
 		facebook.authorizeCallback(requestCode, resultCode, data);
     	if (!facebook.isSessionValid()) {
     		facebook.setAccessToken(null);
     		facebook.setAccessExpires(0);
     		facebook.authorizeCallback(requestCode, resultCode, data);
     	}
 	}
 
 	/** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.write);
         intent = getIntent();
         
         
         //back = (Button)findViewById(R.id.back);
         send = (Button)findViewById(R.id.check);
         loc = (TextView)findViewById(R.id.location);
         text = (EditText)findViewById(R.id.text);
         loc.setText("@ "+intent.getStringExtra("p"));
         token = intent.getStringExtra("token");
     	/*back.setOnClickListener(new OnClickListener() {
     		public void onClick(View v){
     			finish();
     		}
     	});*/
         send.setOnTouchListener(new Button.OnTouchListener(){
             @Override
            public boolean onTouch(View arg0, MotionEvent motionEvent) {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {  //���U���ɭԧ��ܭI�����C��
                 	send.setBackgroundResource(R.drawable.in_on);
                 }  
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {  //�_�Ӫ��ɭԫ�_�I���P�C��
                 	send.setBackgroundResource(R.drawable.in);  
                 }  
             return false;
            }
         });
     	send.setOnClickListener(new OnClickListener() {
     		public void onClick(View v){
                 //if(intent2.hasExtra("p")&&intent2.hasExtra("id")&&intent2.hasExtra("token")){
             	
             	//item_name = intent2.getStringExtra("name");
            	//textview.setText("�w�ǰe�T����"+item_name);
     			
 
             	Bundle b = new Bundle();
             	b.putString("method", "POST");
             	b.putString("message",text.getText().toString());
             	b.putString("access_token",intent.getStringExtra("token"));
             	b.putString("place",intent.getStringExtra("id"));
             	b.putString("coordinates","{\"latitude\":"+intent.getStringExtra("lat")+
             					",\"longitude\":"+intent.getStringExtra("lng")+"}");
             	fbAsyncFacebookRunner.request("me/checkins",b,postlistener);
             
             //}
             	/*
     			Intent intent2 = new Intent();
     			intent2.putExtra("text", text.getText().toString());
             	intent2.putExtra("token", intent.getStringExtra("token"));
 				intent2.putExtra("p", intent.getStringExtra("p"));
 				intent2.putExtra("lat", intent.getStringExtra("lat"));
 				intent2.putExtra("lng", intent.getStringExtra("lng"));
 				intent2.putExtra("id", intent.getStringExtra("id"));
             	Log.e("msg",text.getText().toString());
 				intent2.setClass(CheckIn.this, KeyMap.class);
             	
             	startActivity(intent2);
                 //Intent intent2 = getIntent();
                 
 				*/
     		}
     	});
     }
     
 }

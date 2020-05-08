 package com.vivianhhuang.briefly;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.NameValuePair;
 import org.apache.http.StatusLine;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.util.EntityUtils;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
import android.content.SharedPreferences;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.CountDownTimer;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class TimerActivity extends Activity {
 
 	private ArrayList<String> numbers;
 	private String charge;
 	private CountDownTimer timer;
    private String ACCESS_TOKEN;

 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_timer);

        SharedPreferences settings = getApplicationContext().getSharedPreferences(LoginActivity.PREFS, 0);
        ACCESS_TOKEN = settings.getString(LoginActivity.AUTH_ACCESS_TOKEN, "");

 		Intent intent = getIntent();
        Group group = intent.getParcelableExtra(GroupActivity.GROUPNAME);
		String time_input = group.getTime();
		charge = group.getAmt();
 		numbers = intent.getStringArrayListExtra(GroupActivity.ALL_NUMBERS);
         ArrayList<String> names = intent.getStringArrayListExtra(GroupActivity.ALL_NAMES);
 		
 		TextView initialTimer = (TextView)findViewById(R.id.timer);
 		initialTimer.setText(time_input);
 	
 		TextView money = (TextView)findViewById(R.id.money_text);
        if (charge.contains(".")) {
            if (charge.substring(charge.indexOf(".") + 1).length() == 1) {
                money.setText("$" + charge + "0");
            } else {
                money.setText("$" + charge);
            }
        } else {
		    money.setText("$" + charge + ".00");
        }
 		
 		TextView info = (TextView) findViewById(R.id.info);
 		int num = names.size();
 		
 		if (num == 1) {
 			info.setText(num + " person, " + Integer.parseInt(time_input)/num + " seconds");
 		} else {
 			info.setText(num + " people, " + Integer.parseInt(time_input)/num + " secs each");
 		}
 
 		int initialTime = Integer.parseInt(time_input);
 		timer = new MyTimer(initialTime*1000, 1000);
 		timer.start();
 	}
 
 	public class MyTimer extends CountDownTimer {
 
 		public MyTimer(long millisInFuture, long countDownInterval) {
 			super(millisInFuture, countDownInterval);
 
 		}
 		
 		@Override
 		public void onTick(long timeLeft) {
 			((TextView)findViewById(R.id.timer)).setText(String.valueOf(timeLeft/1000));
 		}
 		
 		@Override
 		public void onFinish() {
 			((TextView)findViewById(R.id.timer)).setText("0");
 			new CreateChargeTask().execute();
 
 			Context context = getApplicationContext();
 			CharSequence text = "over time welp";
 			int duration = Toast.LENGTH_SHORT;
 			Toast toast = Toast.makeText(context, text, duration);
 			toast.show();
 		}
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		return true;
 	}
 	
 	public void stopTimer(View view) {
 		timer.cancel();
 		
 		Context context = getApplicationContext();
 		CharSequence text = "no charge today!";
 		int duration = Toast.LENGTH_SHORT;
 		Toast toast = Toast.makeText(context, text, duration);
 		toast.show();
 	}
 	
 	private class CreateChargeTask extends AsyncTask<Void, Void, Void> {
 		@Override
 		protected Void doInBackground(Void... voids) {
             String phoneNum;
 			charge = "-" + charge;
 			for (int i = 0; i < numbers.size(); i++) {
 				phoneNum = numbers.get(i);
 				doTheCharge(phoneNum);
 			}
 			return null;
 		}
 		
 		protected void doTheCharge(String phoneNum) {
 			HttpClient httpclient = new DefaultHttpClient();
 			HttpResponse response;
 			try {
 				HttpPost httpPost = new HttpPost("https://api.venmo.com/payments");
 				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
				nameValuePairs.add(new BasicNameValuePair("access_token", ACCESS_TOKEN));
 				nameValuePairs.add(new BasicNameValuePair("phone", phoneNum));
 				nameValuePairs.add(new BasicNameValuePair("amount", charge));
 				nameValuePairs.add(new BasicNameValuePair("note", "scrum over time"));
 				nameValuePairs.add(new BasicNameValuePair("audience", "friends"));
 				
 				httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
 				response = httpclient.execute(httpPost);
 				
 				StatusLine statusLine = response.getStatusLine();
 				if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
 				} else {
 					response.getEntity().getContent().close();
 					throw new IOException(statusLine.getReasonPhrase());
 				}
 			} catch (ClientProtocolException e) {
 				
 			} catch (IOException e) {
 				
 			}
 		}
 	}
 }

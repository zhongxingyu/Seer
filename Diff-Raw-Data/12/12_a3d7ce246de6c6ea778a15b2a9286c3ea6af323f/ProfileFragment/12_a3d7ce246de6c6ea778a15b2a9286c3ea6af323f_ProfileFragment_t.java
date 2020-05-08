 package com.example.pupi;
 
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class ProfileFragment extends Fragment{
 	private TextView text_name;
 	private TextView text_email;
 	private TextView text_intro;
 
 
	
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		final View view = inflater.inflate(R.layout.fragment_profile,
 				container, false);
 		text_name = (TextView) view.findViewById(R.id.txt_account_name);
 		text_email = (TextView) view.findViewById(R.id.txt_account_email);
 		text_intro = (TextView) view.findViewById(R.id.txt_account_intro);
		
 		((Button)view.findViewById(R.id.btn_edit)).setOnClickListener(new OnClickListener(){
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				Intent intent = new Intent(v.getContext(),EditProfileActivity.class);
 				intent.putExtra("NAME", text_name.getText().toString());
 				intent.putExtra("EMAIL", text_email.getText().toString());
 				intent.putExtra("INTRO", text_intro.getText().toString());
 				v.getContext().startActivity(intent);
 				
 			}
 			
 		});
 		return view;
 	}
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		new AsyncGetinfoAgent().execute(this);
	}
 	private class AsyncGetinfoAgent extends AsyncTask{
 		
 		@Override
 		protected Object doInBackground(Object... params) {
 				String username = MainActivity.userId;
 				String resultString = null;
 				List<NameValuePair> nameValPair = new ArrayList<NameValuePair>();
 				nameValPair.add(new BasicNameValuePair("username", username));
 				resultString = PHPLoader.getStringFromPhp(PHPLoader.GETINFO_PHP,nameValPair);
 				return resultString;
 		}
 		
 		@Override
 		protected void onPostExecute(Object result) {
 			String stringResult = (String)result;
 			String name = stringResult.substring(stringResult.indexOf("####name:")+9,stringResult.indexOf("####email:"));
 			String email = stringResult.substring(stringResult.indexOf("####email:")+10,stringResult.indexOf("####info:"));
 			String intro = stringResult.substring(stringResult.indexOf("####info:")+9,stringResult.length());
 			text_name.setText(name);
 			text_email.setText(email);
 			text_intro.setText(intro);
 			
 		}
 	}
 	
 }

 package nl.bneijt.tryhaskell;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.View.OnKeyListener;
 import android.widget.EditText;
 import android.widget.TextView;
 
 public class TryhaskellActivity extends Activity implements OnKeyListener {
     private Api api;
 	private EditText inputLine;
 	private TextView outputConsole;
 	
 	 private class TryHaskellTask extends AsyncTask<String, Void, JSONObject> {
 		 
 		 private String haskellLine;
 		@Override
 	     protected JSONObject doInBackground(String... code) {
 			 haskellLine = code[0];
 			 return api.send(haskellLine);
 		 }
 	     protected void onPostExecute(JSONObject result) {
 	         try {
 	        	 if(result != null) {
 	        		 if (result.has("error")) {
	        			 outputConsole.append(result.getString("error"));
 	        			 return;
 	        		 }
 	        		 if(result.has("expr")) {
 	        			 outputConsole.append(result.getString("expr"));
 	        		 }
 	        		 if(result.has("type")) {
	        			 outputConsole.append(" :: " + result.getString("type"));
 	        		 }
 	        		 if(result.has("result")) {
 	        			 outputConsole.append("\n");
//	        			 SpannableString text = new SpannableString("Lorem ipsum dolor sit amet");  
//	        			// make "Lorem" (characters 0 to 5) red  
//	        			text.setSpan(new ForegroundColorSpan(Color.RED), 0, 5, 0);  
//	        			textView.setText(text, BufferType.SPANNABLE);
 	        			 outputConsole.append(result.getString("result"));
 	        			 outputConsole.append("\n");
 	        		 }
 	        	 }
 			} catch (JSONException e) {
 				outputConsole.append("Error");
 			}
 	     }

 	 }
 
 	/** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         api = new Api();
         
         outputConsole = (TextView) findViewById(R.id.outputConsole);
         
         inputLine = (EditText) findViewById(R.id.inputLine);        
         inputLine.setOnKeyListener(this);
     }
 
 	@Override
 	public boolean onKey(View v, int keyCode, KeyEvent event) {
 		if(keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
 			new TryHaskellTask().execute(inputLine.getText().toString());
 			inputLine.setText("");
 			return true;
 		}
 		return false;
 	}
 }

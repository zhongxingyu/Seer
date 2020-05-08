 package com.atech.mpso;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.inputmethod.EditorInfo;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MPsoActivity extends Activity implements ResponseCallback{
 
 	private EditText editText;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setTitle(R.string.title);
 		setContentView(R.layout.activity_mpso);
 		
 		editText = (EditText)findViewById(R.id.editTUC);
 		
 		editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
 	        @Override
 	        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
 	            if (actionId == EditorInfo.IME_ACTION_DONE) {
 	            		search();
 	                return true;
 	            }
 	            return false;
 	        }
 	    });
 		
 		findViewById(R.id.search).setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				search();
 			}
 		});
 		
 		if (BuildConfig.DEBUG) 
 			editText.setText("00442039");
 	}
 	
 	private void search() {
 		String tarjetaTUC = editText.getText().toString();
 		if (valid(tarjetaTUC))
 			new MPesoCaller(getBaseContext()).consultarSaldo(tarjetaTUC, MPsoActivity.this);
	
 	}
 	
 	private boolean valid(String tarjetaTUC) {
 		
 		try {
 			Integer.parseInt(tarjetaTUC);
 		} catch(NumberFormatException ex) {
 			return false;
 		}
 		
 		return tarjetaTUC.length() == 8;
 	}
 
 	@Override
 	public void response(String saldo) {
 		Toast.makeText(this, "saldo is " + saldo, Toast.LENGTH_LONG).show();;
 	}
 
 	@Override
 	public void error(String message) {
 		Toast.makeText(this, "saldo is " + message, Toast.LENGTH_LONG).show();
 	}
 
 }

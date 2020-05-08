 package ovgu.gruppe1.ehskapp;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnKeyListener;
 import android.widget.Button;
 import android.widget.EditText;
 
 public class UserCodeActivity extends Activity implements OnKeyListener {
 	
 	EditText textField1, textField2, textField3, textField4, textField5;
 	String text1, text2, text3, text4, text5;
 	Button submitUserCodeButton;
     
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.user_code_layout);
         
         textField1 = (EditText) findViewById(R.id.editText1);
         textField2 = (EditText) findViewById(R.id.EditText01);
         textField3 = (EditText) findViewById(R.id.EditText02);
         textField4 = (EditText) findViewById(R.id.EditText03);
         textField5 = (EditText) findViewById(R.id.EditText04);
         textField1.setOnKeyListener(this);
         textField2.setOnKeyListener(this);
         textField3.setOnKeyListener(this);
         textField4.setOnKeyListener(this);
         textField5.setOnKeyListener(this);
         
         submitUserCodeButton = (Button) findViewById(R.id.button1);
         submitUserCodeButton.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				if(text1 == null || text2 == null || text3 == null || text4 == null || text5 == null){
					
 				} else {
 					finishActivity();
 				}
 				
 			}
 		});
         
     }
 
 	/**
 	 * closes the activity
 	 */
 	private void finishActivity(){
 		this.finish();
 	}
 
 	@Override
 	public boolean onKey(View v, int keyCode, KeyEvent event) {
 		
 		if(v==textField1){
 			text1 = textField1.getText().toString();
 			if(text1.length()>1){
 				textField1.setText("" + text1.toCharArray()[0]);
 			}
 		} else
 		
 		if(v==textField2){
 			text2 = textField2.getText().toString();
 			if(text2.length()>1){
 				textField2.setText("" + text2.toCharArray()[0]);
 			}
 		} else 
 		
 		if(v==textField3){
 			text3 = textField3.getText().toString();
 			if(text3.length()>1){
 				textField3.setText("" + text3.toCharArray()[0]);
 			}
 		} else 
 			
 		if(v==textField4){
 			text4 = textField4.getText().toString();
 			if(text4.length()>1){
 				textField4.setText("" + text4.toCharArray()[0]);
 			}
 		} else {
 			text5 = textField5.getText().toString();
 			if(text5.length()>1){
 				textField5.setText("" + text5.toCharArray()[0]);
 			}
 		}
 		
 		return false;
 	}
 
 }

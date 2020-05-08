 package com.shootthesquare.ermahgerd;
 
import android.R.color;
 import android.app.Activity;
 import android.content.Context;
 import android.os.Bundle;
 import android.text.ClipboardManager;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 
 public class Ermahgerd extends Activity {
 
 	private EditText inputText;
 	private EditText outputText;
 	private Button translateButton; 
 	private Button copyButton;
 	private Dialect ermahgerd = new ErmahgerdDialect();
 
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_ermahgerd);
         
         inputText = (EditText)findViewById(R.id.inputText);
         outputText = (EditText)findViewById(R.id.outputText);
         translateButton = (Button)findViewById(R.id.translateButton);
         copyButton = (Button)findViewById(R.id.copyButton);
                 
         translateButton.setOnClickListener(TranslateButtonListener);
         copyButton.setOnClickListener(CopyButtonListener);
         
         
     }
 
     private OnClickListener TranslateButtonListener = new OnClickListener() {
 		   public void onClick(View v) {
 			   String textEntered = inputText.getText().toString();
 			   String translatedText = ermahgerd.translate(textEntered);
 			   outputText.setText(translatedText);
 			 InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
 		        imm.hideSoftInputFromWindow(inputText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
 		   }
 	   };
 
 	   
 	    private OnClickListener CopyButtonListener = new OnClickListener() {
 			   public void onClick(View v) {
 				 InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
 			        imm.hideSoftInputFromWindow(inputText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
 			        if (outputText.getText().toString().length()>0) {
 
 			        	final ClipboardManager clipBoard = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
 			        	clipBoard.setText(outputText.getText().toString());
 			        	Context context = getApplicationContext();
 						   CharSequence text = "Ermahgerd! Terxt Cerperd ter dah clerpberd!";
 						   int duration = Toast.LENGTH_SHORT;
 
 						   Toast toast = Toast.makeText(context, text, duration);
 						   toast.show();
 			        }
 
 			   }
 		   };
 	   
 
 	   
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_ermahgerd, menu);
         return true;
     }
 
     
 }

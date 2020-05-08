package ru.sixteenfourteen.translator;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnKeyListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 
 @SuppressLint("NewApi")
 public class MainActivity extends Activity {
 	Context context;
 	Button translateButton;
 	EditText inputText;
 	public boolean sec = false;
 
 	public static boolean isOneWord(String word) {
 		if (word.equals(""))
 			return false;
 		for (int i = 0; i < word.length(); i++) {
 			if (word.charAt(i) < 'A' || word.charAt(i) > 'Z'
 					&& word.charAt(i) < 'a' || word.charAt(i) > 'z')
 				return false;
 		}
 		return true;
 	}
 
 	@Override
 	public void onBackPressed() {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setTitle("Exit")
 				.setMessage("Do u rly wanna exit?")
 				.setCancelable(false)
 				.setPositiveButton("Yep",
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog, int id) {
 								MainActivity.super.onBackPressed();
 							}
 						}).setNegativeButton("Nah", null);
 		builder.create().show();
 	}
 
 	public void goNextWindow() {
 		TextView wordToTranslate = (TextView) findViewById(R.id.engWord);
 		if (isOneWord(wordToTranslate.getText().toString())) {
 			Intent intent = new Intent(MainActivity.this, SecondActivity.class);
 			intent.putExtra("engWord", wordToTranslate.getText().toString());
 			startActivity(intent);
 			finish();
 		} else {
 			TextView label = (TextView) findViewById(R.id.label1);
 			label.setText("ONLY ONE ENGLISH WORD ALLOWED!");
 			label.setTextColor(Color.RED);
 		}
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		// Button click
 
 		this.translateButton = (Button) this.findViewById(R.id.translateButton);
 		this.translateButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				goNextWindow();
 			}
 		});
 		this.inputText = (EditText) this.findViewById(R.id.engWord);
 		this.inputText.setOnKeyListener(new OnKeyListener() {
 
 			@Override
 			public boolean onKey(View v, int keyCode, KeyEvent event) {
 				if ((event.getAction() == KeyEvent.ACTION_DOWN)
 						&& (keyCode == KeyEvent.KEYCODE_ENTER)) {
 					goNextWindow();
 					return true;
 				}
 				return false;
 			}
 		});
 	}
 }

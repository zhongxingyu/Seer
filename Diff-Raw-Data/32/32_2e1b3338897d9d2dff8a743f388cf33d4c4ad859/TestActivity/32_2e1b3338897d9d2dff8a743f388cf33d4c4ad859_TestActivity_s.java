 package com.tcc.activity;
 
 import java.util.ArrayList;
 
import android.app.AlertDialog;
 import android.content.Context;
import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.tcc.app.Image;
 import com.tcc.app.ManagerTest;
 
 /**
  * Activity principal do aplicativo
  * 
  * @authors Fabrício e Manoel
  * 
  */
 public class TestActivity extends MenuOptions {
 
 	private ImageButton answerButton;
 	private ImageView imgImageView;
 	private TextView inputTextView;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		//sherlock theme
         setTheme(R.style.Theme_Sherlock); 
 
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.test_activity);
 
 		this.answerButton = (ImageButton) findViewById(R.id.responder_imageButton);
 		this.imgImageView = (ImageView) findViewById(R.id.imagem_imageView);
 		this.inputTextView = (TextView) findViewById(R.id.entrada_editText);
 
 		startTest();
 		listeners();
 	}
 
 	/**
 	 * Visualiza a activity de resultado
 	 */
 	private void resultActivity() {
 		
 //		Intent i = new Intent(this, ResultActivity.class);
 		
 		Intent i = new Intent(this, GetInfoActivity.class);
 		startActivity(i);
 	}
 
 	
 
 	/**
 	 * Contem os eventos listeners dos objetos
 	 */
 	private void listeners() {
 
 
 		answerButton.setOnClickListener(new View.OnClickListener() {
			@SuppressWarnings("deprecation")
 			public void onClick(View v) {
 				String input = inputTextView.getText().toString().trim();
 
 				if (input.isEmpty()) {
 					
 					// Show alert message and close the application
				    AlertDialog alert = new AlertDialog.Builder(TestActivity.this)
				            .create();
				    //alert.setTitle("Error");
				    alert.setMessage("Digite o símbolo que voce ve!");
				    alert.setButton("OK", new DialogInterface.OnClickListener() {
				        public void onClick(DialogInterface dialog, int which) { }
				    });
				    alert.show();
				 
					return;
 				}
 
 				ManagerTest.setAnswer(input);
 				if (ManagerTest.hasNext()) {
 
 					imgImageView.setImageResource(ManagerTest.next()
 							.getResourceId());
 					// clear text
 					inputTextView.setText("");
 
 					// answerButton.requestFocus();
 					// Log.i(ManagerTest.APP_NAME,
 					// ""+getWindow().getCurrentFocus());
 
 					// close keyboard
 					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
 					imm.hideSoftInputFromWindow(inputTextView.getWindowToken(),
 							0);
 
 				} else {
 					resultActivity();
 				}
 
 			}
 		});
 	}
 
 	/**
 	 * Configura as imagens do teste, os grupos de teste, e as quantidades dos
 	 * mesmos
 	 */
 	private void startTest() {
 		ArrayList<Image> images = new ArrayList<Image>();
 		int i = 0;
		images.add(new Image(i++, R.drawable.img_206_e, "e"));
 		images.add(new Image(i++, R.drawable.img_277_3, "3"));
 		images.add(new Image(i++, R.drawable.img_279_a, "a"));
 		images.add(new Image(i++, R.drawable.img_280_4, "4"));
 		images.add(new Image(i++, R.drawable.img_281_a, "a"));
 		images.add(new Image(i++, R.drawable.img_282_6, "6"));
 		images.add(new Image(i++, R.drawable.img_283_e, "e"));
 		images.add(new Image(i++, R.drawable.img_284_h, "h"));
 		images.add(new Image(i++, R.drawable.img_285_4, "4"));
 		images.add(new Image(i++, R.drawable.img_286_6, "6"));
 
 		ManagerTest.startTest(images);
 	}
 
 }

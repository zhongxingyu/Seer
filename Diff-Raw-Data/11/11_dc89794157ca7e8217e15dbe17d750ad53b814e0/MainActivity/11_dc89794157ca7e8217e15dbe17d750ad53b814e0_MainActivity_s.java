 package com.example.bbqtipcalculator;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
import android.view.ViewGroup.LayoutParams;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
import android.widget.RelativeLayout;
 
 public class MainActivity extends Activity {
 
 	// references to the view contained in the layout
 	private LinearLayout mPrevGratLayout;
 	private LinearLayout mPrevCardTipLayout;
 	private Button mAddGratButton;
 	private Button mAddCardTipButton;
 	private Button mCalculateButton;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		// save the nested layouts of the dynamic portions of the main layout
 		mPrevGratLayout = (LinearLayout) findViewById(R.id.layout_prev_grat);
 		mPrevCardTipLayout = (LinearLayout) findViewById(R.id.layout_prev_card_tips);
 
 		// save references to the action buttons
 		mAddGratButton = (Button) findViewById(R.id.button_add_prev_grat);
 		mAddCardTipButton = (Button) findViewById(R.id.button_add_prev_card_tip);
 		mCalculateButton = (Button) findViewById(R.id.button_calculate);
 	}
 
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	/**
 	 * Adds a new slot for a tip from a previous shift to the main layout.
 	 * 
 	 * @param view TODO
 	 */
 	public void addCardTipSlot(View view) {
 		EditText newCardTipSlot = (EditText) getLayoutInflater().
 				inflate(R.layout.edit_text_template, null);
 		mPrevCardTipLayout.addView(newCardTipSlot);
 	}  
 	
 	/**
 	 * Adds a new slot for a gratuity from a previous shift to the main layout.
 
 	 * @param view TODO
 	 */
 	public void addGratSlot(View view) {
 		EditText newGratSlot = (EditText) getLayoutInflater().
				inflate(R.layout.edit_text_template, null);
//		RelativeLayout.LayoutParams params = new RelativeLayout.
//				LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
//		params.addRule(RelativeLayout.ABOVE, R.id.button_add_prev_grat);
//		newGratSlot.setLayoutParams(params);
 		mPrevGratLayout.addView(newGratSlot);
 	}
 	
 	/**
 	 * Calculates the current shift's tip based on the current user input.
 	 */
 	public void calculateTip() {
 		
 	}
 
 }

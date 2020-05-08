 package net.pillageandplunder.chickenfooter;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class ScoreNew extends Activity {
 	private TextView mValueText;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.score_new);
 		
         Bundle extras = getIntent().getExtras();
         if (extras != null) {
         	setTitle("Add score for " + extras.getString("name"));
         }
 
         mValueText = (TextView) findViewById(R.id.number);
         
         ((Button)findViewById(R.id.ok)).setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
 				Intent mIntent = new Intent();
 				mIntent.putExtra("value", mValueText.getText().toString());
 				setResult(RESULT_OK, mIntent);
 				finish();
             }
         });
         
         ((Button)findViewById(R.id.back)).setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
             	String text = mValueText.getText().toString();
            	mValueText.setText(text.substring(0, text.length()-1));
             }
         });
         
         ((Button)findViewById(R.id.n0)).setOnClickListener(new NumberButtonListener(mValueText, "0"));
         ((Button)findViewById(R.id.n1)).setOnClickListener(new NumberButtonListener(mValueText, "1"));
         ((Button)findViewById(R.id.n2)).setOnClickListener(new NumberButtonListener(mValueText, "2"));
         ((Button)findViewById(R.id.n3)).setOnClickListener(new NumberButtonListener(mValueText, "3"));
         ((Button)findViewById(R.id.n4)).setOnClickListener(new NumberButtonListener(mValueText, "4"));
         ((Button)findViewById(R.id.n5)).setOnClickListener(new NumberButtonListener(mValueText, "5"));
         ((Button)findViewById(R.id.n6)).setOnClickListener(new NumberButtonListener(mValueText, "6"));
         ((Button)findViewById(R.id.n7)).setOnClickListener(new NumberButtonListener(mValueText, "7"));
         ((Button)findViewById(R.id.n8)).setOnClickListener(new NumberButtonListener(mValueText, "8"));
         ((Button)findViewById(R.id.n9)).setOnClickListener(new NumberButtonListener(mValueText, "9"));
 	}
 	
 	private class NumberButtonListener implements View.OnClickListener {
 		private TextView mValueText;
 		private String mNumber;
 		
 		NumberButtonListener(TextView v, String n) {
 			mValueText = v;
 			mNumber = n;
 		}
 		
 		@Override
 		public void onClick(View v) {
 			mValueText.setText(mValueText.getText() + mNumber);
 		}
 	}
 }

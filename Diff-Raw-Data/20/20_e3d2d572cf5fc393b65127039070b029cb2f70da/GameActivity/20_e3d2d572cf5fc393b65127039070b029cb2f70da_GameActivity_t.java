 package com.moonbc.numbercalc;
 
 
 import java.util.HashSet;
 import java.util.Set;
 
 import android.app.Activity;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class GameActivity extends Activity {
 	int value =0;
 	TextView mTimeCount;
 	
 	Button mNumber1Button;
 	Button mNumber2Button;
 	Button mNumber3Button;
 	Button mNumber4Button;
 	Button mNumber5Button;
 	Button mNumber6Button;
 	Button mNumber7Button;
 	Button mNumber8Button;	
 	Button mNumber9Button;
 	Button mNumber10Button;
 	Button mNumber11Button;
 	Button mNumber12Button;	
 	Button mNumber13Button;
 	Button mNumber14Button;
 	Button mNumber15Button;
 	Button mNumber16Button;	
 	Button mNumber17Button;
 	Button mNumber18Button;
 	Button mNumber19Button;
 	Button mNumber20Button;	
 	Button mNumber21Button;	
 	Button mNumber22Button;
 	Button mNumber23Button;
 	Button mNumber24Button;
 	Button mNumber25Button;		
 	Button mNumber26Button;	
 	Button mNumber27Button;	
 	Button mNumber28Button;
 	Button mNumber29Button;
 	Button mNumber30Button;
 	Button mNumber31Button;		
 	Button mNumber32Button;	
 	Button mNumber33Button;
 	Button mNumber34Button;
 	Button mNumber35Button;
 	Button mNumber36Button;		
 	
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main_game);
 		
 		
 		
 		mTimeCount = (TextView)findViewById(R.id.time_counter_text);
 		mHandler.sendEmptyMessage(0);
 		
 		
 		mNumber1Button = (Button)findViewById(R.id.number1_button);
 		mNumber2Button = (Button)findViewById(R.id.number2_button);
 		mNumber3Button = (Button)findViewById(R.id.number3_button);
 		mNumber4Button = (Button)findViewById(R.id.number4_button);
 		mNumber5Button = (Button)findViewById(R.id.number5_button);
 		mNumber6Button = (Button)findViewById(R.id.number6_button);
 		mNumber7Button = (Button)findViewById(R.id.number7_button);
 		mNumber8Button = (Button)findViewById(R.id.number8_button);		
 
 		mNumber9Button = (Button)findViewById(R.id.number9_button);
 		mNumber10Button = (Button)findViewById(R.id.number10_button);
 		mNumber11Button = (Button)findViewById(R.id.number11_button);
 		mNumber12Button = (Button)findViewById(R.id.number12_button);
 		
 		mNumber13Button = (Button)findViewById(R.id.number13_button);
 		mNumber14Button = (Button)findViewById(R.id.number14_button);
 		mNumber15Button = (Button)findViewById(R.id.number15_button);
 		mNumber16Button = (Button)findViewById(R.id.number16_button);
 		
 		mNumber17Button = (Button)findViewById(R.id.number17_button);
 		mNumber18Button = (Button)findViewById(R.id.number18_button);
 		mNumber19Button = (Button)findViewById(R.id.number19_button);
 		mNumber20Button = (Button)findViewById(R.id.number20_button);		
 		mNumber21Button = (Button)findViewById(R.id.number21_button);
 		mNumber22Button = (Button)findViewById(R.id.number22_button);
 		mNumber23Button = (Button)findViewById(R.id.number23_button);
 		mNumber24Button = (Button)findViewById(R.id.number24_button);
 		

 		mNumber25Button = (Button)findViewById(R.id.number25_button);
 		mNumber26Button = (Button)findViewById(R.id.number26_button);
 		mNumber27Button = (Button)findViewById(R.id.number27_button);
 		mNumber28Button = (Button)findViewById(R.id.number28_button);			
 
 		mNumber29Button = (Button)findViewById(R.id.number29_button);
 		mNumber30Button = (Button)findViewById(R.id.number30_button);
 		mNumber31Button = (Button)findViewById(R.id.number31_button);
 		mNumber32Button = (Button)findViewById(R.id.number32_button);	
 
 		
 		mNumber33Button = (Button)findViewById(R.id.number33_button);
 		mNumber34Button = (Button)findViewById(R.id.number34_button);
 		mNumber35Button = (Button)findViewById(R.id.number35_button);
 		mNumber36Button = (Button)findViewById(R.id.number36_button);			
 		
 		mNumber1Button.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				//mNumber1Button.setBackgroundColor(Color.BLUE);
 
 				mNumber1Button.setEnabled(false);
 				Toast.makeText(getApplicationContext(), "button1", Toast.LENGTH_LONG).show();
 			}
 		});	
 		
 		mNumber2Button.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				//mNumber1Button.setBackgroundColor(Color.BLUE);
 
 				mNumber1Button.setEnabled(false);
 				Toast.makeText(getApplicationContext(), "button1", Toast.LENGTH_LONG).show();
 			}
 		});		
 		
 		mNumber3Button.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				//mNumber1Button.setBackgroundColor(Color.BLUE);
 
 				mNumber1Button.setEnabled(false);
 				Toast.makeText(getApplicationContext(), "button1", Toast.LENGTH_LONG).show();
 			}
 		});		
 		
 
 		
 		initNumberSet();
 			
 	
 	}
	
 	Handler mHandler = new Handler() {
 		public void handleMessage(android.os.Message msg) {
 			value++;
 			
 			
 			mTimeCount.setText(String.valueOf(value));
 			mHandler.sendEmptyMessageDelayed(0, 1000);
 		}
 	};
 	
 	
 	public void initNumberSet() {
 		
 		Set set = new HashSet();
 		
 		for(int i =0; i < 36; i++ ) {
 			int num = (int)(Math.random()*10);
 			
 			set.add(new Integer(num));
 			Log.d("MoonTest", "num = " + num + " i= " + i);
 			switch(i) {
 				case 0:
 					mNumber1Button.setText(String.valueOf(num));
 					break;
 				case 1:
 					mNumber2Button.setText(String.valueOf(num));
 					break;
 				case 2:
 					mNumber3Button.setText(String.valueOf(num));
 					break;
 				case 3:
 					mNumber4Button.setText(String.valueOf(num));
 					break;
 				case 4:
 					mNumber5Button.setText(String.valueOf(num));
 					break;			
 				case 5:
 					mNumber6Button.setText(String.valueOf(num));
 					break;
 				case 6:
 					mNumber7Button.setText(String.valueOf(num));
 					break;
 				case 7:
 					mNumber8Button.setText(String.valueOf(num));
 					break;
 				case 8:
 					mNumber9Button.setText(String.valueOf(num));
 					break;
 				case 9:
 					mNumber10Button.setText(String.valueOf(num));
 					break;	
 				case 10:
 					mNumber11Button.setText(String.valueOf(num));
 					break;
 				case 11:
 					mNumber12Button.setText(String.valueOf(num));
 					break;
 				case 12:
 					mNumber13Button.setText(String.valueOf(num));
 					break;
 				case 13:
 					mNumber14Button.setText(String.valueOf(num));
 					break;
 				case 14:
 					mNumber15Button.setText(String.valueOf(num));
 					break;
 				case 15:
 					mNumber16Button.setText(String.valueOf(num));
 					break;
 				case 16:
 					mNumber17Button.setText(String.valueOf(num));
 					break;
 				case 17:
 					mNumber18Button.setText(String.valueOf(num));
 					break;
 				case 18:
 					mNumber19Button.setText(String.valueOf(num));
 					break;
 				case 19:
 					mNumber20Button.setText(String.valueOf(num));
 					break;	
 				case 20:
 					mNumber21Button.setText(String.valueOf(num));
 					break;
 				case 21:
 					mNumber22Button.setText(String.valueOf(num));
 					break;
 				case 22:
 					mNumber23Button.setText(String.valueOf(num));
 					break;
 				case 23:
 					mNumber24Button.setText(String.valueOf(num));
 					break;
 				case 24:
 					mNumber25Button.setText(String.valueOf(num));
 					break;	
 				case 25:
 					mNumber26Button.setText(String.valueOf(num));
 					break;
 				case 26:
 					mNumber27Button.setText(String.valueOf(num));
 					break;
 				case 27:
 					mNumber28Button.setText(String.valueOf(num));
 					break;
 				case 28:
 					mNumber29Button.setText(String.valueOf(num));
 					break;
 				case 29:
 					mNumber30Button.setText(String.valueOf(num));
 					break;	
 				case 30:
 					mNumber31Button.setText(String.valueOf(num));
 					break;
 				case 31:
 					mNumber32Button.setText(String.valueOf(num));
 					break;
 				case 32:
 					mNumber33Button.setText(String.valueOf(num));
 					break;
 				case 33:
 					mNumber34Button.setText(String.valueOf(num));
 					break;
 				case 34:
 					mNumber35Button.setText(String.valueOf(num));
 					break;		
 				case 35:
 					mNumber36Button.setText(String.valueOf(num));
 					break;						
 			}
 			
 		}
 		
 	}
 	
 	
 	
 	@Override
 	public void onBackPressed() {
 		// TODO Auto-generated method stub
 		Toast.makeText(getApplicationContext(), "back key", Toast.LENGTH_SHORT).show();
 		super.onBackPressed();
 	}
 }

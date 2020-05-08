 package jones.code.timer;
 
 import java.text.DecimalFormat;
 
 import jones.code.AppExtender;
 import jones.code.GlobalStuff;
 import jones.code.InputHandler;
 import jones.code.QLPickupItemType;
 import jones.code.R;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.PorterDuff.Mode;
 import android.os.Bundle;
 import android.os.SystemClock;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class QLTTestActivity extends Activity {
 
 	private String currentValue;
 	private QLTimerTestHandler timerHandler;
 	private Context ctx;
 	
 	private long startValue = 0;
 	private long endValue = 0;
 	
 	private static final int QL_RESULTVIEW_ACTIVITY = 0;
 	
 	@Override
 	protected void onCreate(Bundle bundle) {
 		super.onCreate(bundle);
 		setContentView(R.layout.qltestactivity);
 	
 		AppExtender share = (AppExtender) getApplication();
 		timerHandler = share.timerHandler;
 		
 		// reset the value entered by user
 		currentValue = "0";
 		updateTextFieldForInput();
 		
 		ctx = this;
 		
 		createNewPickupTime();
 		
 		Button buttonClear = (Button) findViewById(R.id.clearButton);
 		buttonClear.setOnClickListener(handleClearListener);
 		buttonClear.getBackground().setColorFilter(GlobalStuff.buttonColor, Mode.MULTIPLY);
 		
 		Button buttonSubmit = (Button) findViewById(R.id.submitButton);
 		buttonSubmit.setOnClickListener(handleSubmitListener);
 		buttonSubmit.getBackground().setColorFilter(GlobalStuff.buttonColor, Mode.MULTIPLY);
 		
 		Button buttonOne = (Button) findViewById(R.id.numberOne);
         buttonOne.setOnClickListener(handelButtonOneListener);
         
         Button buttonTwo = (Button) findViewById(R.id.numberTwo);
         buttonTwo.setOnClickListener(handleButtonTwoListener);
         
         Button buttonThree = (Button) findViewById(R.id.numberThree);
         buttonThree.setOnClickListener(handleButtonThreeListener);
         
         Button buttonFour = (Button) findViewById(R.id.numberFour);
         buttonFour.setOnClickListener(handleButtonFourListener);
         
         Button buttonFive = (Button) findViewById(R.id.numberFive);
         buttonFive.setOnClickListener(handleButtonFiveListener);
         
         Button buttonSix = (Button) findViewById(R.id.numberSix);
         buttonSix.setOnClickListener(handleButtonSixListener);
         
         Button buttonSeven = (Button) findViewById(R.id.numberSeven);
         buttonSeven.setOnClickListener(handleButtonSevenListener);
         
         Button buttonEight = (Button) findViewById(R.id.numberEight);
         buttonEight.setOnClickListener(handleButtonEightListener);
         
         Button buttonNine = (Button) findViewById(R.id.numberNine);
         buttonNine.setOnClickListener(handleButtonNineListener);
         
         Button buttonZero = (Button) findViewById(R.id.numberZero);
         buttonZero.setOnClickListener(handleButtonZeroListener);
 	}
 
 	private OnClickListener handelButtonOneListener = new OnClickListener() {
 		@Override
 		public void onClick(View v) {
 			currentValue += "1";
 			updateTextFieldForInput();
 		}
 	};
 
 	private OnClickListener handleButtonTwoListener = new OnClickListener() {
 		@Override
 		public void onClick(View v) {
 			currentValue += "2";
 			updateTextFieldForInput();
 		}
 	};
 	
 	private OnClickListener handleButtonThreeListener = new OnClickListener() {
 		@Override
 		public void onClick(View v) {
 			currentValue += "3";
 			updateTextFieldForInput();
 		}
 	};
 	
 	private OnClickListener handleButtonFourListener = new OnClickListener() {
 		@Override
 		public void onClick(View v) {
 			currentValue += "4";
 			updateTextFieldForInput();
 		}
 	};
 	
 	private OnClickListener handleButtonFiveListener = new OnClickListener() {
 		@Override
 		public void onClick(View v) {
 			currentValue += "5";
 			updateTextFieldForInput();
 		}
 	};
 	
 	private OnClickListener handleButtonSixListener = new OnClickListener() {
 		@Override
 		public void onClick(View v) {
 			currentValue += "6";
 			updateTextFieldForInput();
 		}
 	};
 	
 	private OnClickListener handleButtonSevenListener = new OnClickListener() {
 		@Override
 		public void onClick(View v) {
 			currentValue += "7";
 			updateTextFieldForInput();
 		}
 	};
 	
 	private OnClickListener handleButtonEightListener = new OnClickListener() {
 		@Override
 		public void onClick(View v) {
 			currentValue += "8";
 			updateTextFieldForInput();
 		}
 	};
 	
 	private OnClickListener handleButtonNineListener = new OnClickListener() {
 		@Override
 		public void onClick(View v) {
 			currentValue += "9";
 			updateTextFieldForInput();
 		}
 	};
 	
 	private OnClickListener handleButtonZeroListener = new OnClickListener() {
 		@Override
 		public void onClick(View v) {
 			currentValue += "0";
 			updateTextFieldForInput();
 		}
 	};
 	
 	private OnClickListener handleClearListener = new OnClickListener() {
 		@Override
 		public void onClick(View v) {
 			clearInputValueField();
 		}
 	};
 	
 	private OnClickListener handleSubmitListener = new OnClickListener() {
 		@Override
 		public void onClick(View v) {
 			DecimalFormat df = new DecimalFormat("#.##");
 			
 			endValue = SystemClock.elapsedRealtime();
 			long diff = endValue - startValue;
 			double diff2 = (double)diff/1000;
 			String difference = df.format(diff2);
 			
 			timerHandler.isCorrectAnswer(currentValue, difference);
 			updateResults();
 			
 			if (!timerHandler.isTestEmpty())
 			{
 				createNewPickupTime();
 			} else
 			{	
 				Intent i = new Intent(ctx, QLTResultActivity.class);
 				startActivityForResult(i, QL_RESULTVIEW_ACTIVITY);
 			}
 		}
 	};
 	
 	private void updateTextFieldForInput(){
 		TextView inputValue = (TextView) findViewById(R.id.inputTextValue);
 		currentValue = new InputHandler().formatInputTimerValue(currentValue);
 		inputValue.setText(currentValue);
 	}
 	
 	private void createNewPickupTime() {
 		TextView pickupTime = (TextView) findViewById(R.id.pickupTime);
 		
 		String template = "XX:";
 		String pickupTimeValue = timerHandler.getTestList().get(0).getCurrentPickupTimeValue();
 		String textToDisplay = "";
 		
 		if (pickupTimeValue.length() == 1)
 			template += "0";
 		
 		textToDisplay = template + pickupTimeValue;
 		pickupTime.setText(textToDisplay);
 		
 		/* updates after each test value*/
 		startValue = SystemClock.elapsedRealtime();
 		clearInputValueField();
 		updateIconForNextTypeofPickup(timerHandler.getTestList().get(0).getPickupType());
 		updateResults();
 	}
 	
 	private void updateResults()
 	{
 		TextView tv = (TextView) findViewById(R.id.currentTestStatus);
 		String totalTests = Integer.toString(timerHandler.getTotalNumberOfTestOrdered());
 		String numberOfTestExecuted = Integer.toString(timerHandler.getResultList().size());
 		
 		String numberOfPassedTests = Integer.toString(timerHandler.getTotalNumberOfSuccessTests());
 		
 		String text = String.format("Tests done: %s/%s\nPassrate: %s/%s",numberOfTestExecuted,totalTests,numberOfPassedTests, totalTests);
 		tv.setText(text);
 	}
 	
 	private void updateIconForNextTypeofPickup(QLPickupItemType qlTimerPickupType) {
 		ImageView iv = (ImageView) findViewById(R.id.pickup_icon);
 		if (qlTimerPickupType == QLPickupItemType.RA)
 		{ iv.setImageResource(R.drawable.ic_menu_ra); }
 		else {
 			iv.setImageResource(R.drawable.ic_menu_mh);
 		}
 		iv.refreshDrawableState();
 	}
 	
 	private void clearInputValueField() {
 		currentValue = new InputHandler().clearInputValue();
 		updateTextFieldForInput();
 	}
 
 	
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (requestCode == QL_RESULTVIEW_ACTIVITY) {
 			if (resultCode == RESULT_OK) {
 				setResult(RESULT_OK);
 				finish();
 			} else if (resultCode == RESULT_CANCELED) {
 				setResult(RESULT_OK);
 				finish();
 			}
 		}
 	}
 }

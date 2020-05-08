 package jones.code.timer;
 
 import jones.code.AppExtender;
 import jones.code.GlobalStuff;
 import jones.code.R;
 import jones.code.ResultHolder;
 import android.app.Activity;
 import android.graphics.Color;
 import android.graphics.PorterDuff.Mode;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.Toast;
 import android.widget.LinearLayout.LayoutParams;
 import android.widget.TextView;
 
 public class QLTResultActivity extends Activity {
 	
 	private QLTimerTestHandler timerHandler;
 
 	
 	@Override
 	protected void onCreate(Bundle bundle) {
 		super.onCreate(bundle);
 		setContentView(R.layout.resultactivity);
 	
 		AppExtender share = (AppExtender) getApplication();
 		timerHandler = share.timerHandler;
				
 		createResultReport();
 		
 		Button confirmButton = (Button) findViewById(R.id.done_result_button);
 		confirmButton.getBackground().setColorFilter(GlobalStuff.buttonColor, Mode.MULTIPLY);
 		confirmButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View view) {
 				setResult(RESULT_OK);
 				finish();
 			}
 		});
 		
 		
 	}
 	
 	private void createResultReport() {
 		LinearLayout resultLinLay=(LinearLayout) this.findViewById(R.id.resultLinLay);
 		LayoutParams lparams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
 		
 		for (int i = 0; i < timerHandler.getResultList().size(); i++) {
 			
 			TextView tv = new TextView(this);
 			tv.setLayoutParams(lparams);
 			ResultHolder currentResult = timerHandler.getResultList().get(i);
 			
 			String text = String.format("[%s: %s,%s]",
 					currentResult.getPickupType(),
 					currentResult.getPickupTime(),
 					currentResult.getGuessedTime());
 			
 		    boolean isOverThresHold = isResultOverTreshold(
 		    		currentResult.getTime());
 		    
 			/* set color */
 		    if (currentResult.isPassedResult())
 			{
 		    	text += " PASSED, t = " + currentResult.getTime();
 				if (isOverThresHold)
 				{
 					tv.setTextColor(Color.YELLOW);
 					text += " FIX";
 				}
 				else
 				{
 					tv.setTextColor(Color.GREEN);
 					text += " NICE";
 				}
 			} 
 			else 
 			{
 				text += " FAILED, t = " + currentResult.getTime();
 				text += " FIX HARDER";
 				tv.setTextColor(Color.RED);
 			}
 		    
 		    tv.setText(text);
 			resultLinLay.addView(tv);
 		}
 	}
 
 	private boolean isResultOverTreshold(String time) {
 		
 		try {
 			time = time.replace(",",".");
 			double timeValue = Double.parseDouble(time);
 			
 			if (timeValue > timerHandler.getThresholdValue())
 				return true;
 			
 		} catch (NumberFormatException nfe)
 		{
 			Toast.makeText(
 					this,
 					"Numberformatexception: " + nfe.getMessage(), Toast.LENGTH_LONG).show();
 		} catch (Exception e)
 		{
 			Toast.makeText(
 					this,
 					"Exception: " + e.getMessage(), Toast.LENGTH_LONG).show();
 		}
 		
 		return false;
 	}
 }

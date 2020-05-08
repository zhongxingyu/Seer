 package com.example.calcbasic;
 
 import com.example.calcbasic.CalcController;
 import com.example.calcbasic.R;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
 
 	private class FlagAutoClear {
 		private Boolean flag = false;
 		public void set() { flag = true; }
 		public void cancel() { flag = false; }
 		public Boolean get() {
 			Boolean copyOfFlag = flag;
 			cancel();
 			return copyOfFlag;
 		}
 	}
 	private CalcController controller;
 	private FlagAutoClear resetAtNextInput;
 	private Toast myToast = null;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		controller = new CalcController();
 		resetAtNextInput = new FlagAutoClear();
 		setContentView(R.layout.activity_main);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	private void toastError(String errorText) {
 		if (myToast != null) myToast.cancel();
 		myToast = Toast.makeText(getApplicationContext(), errorText, Toast.LENGTH_SHORT);
 		myToast.show();
 	}
 
 	private void update(Boolean updateResult) {
 		// Input display
 		String inputText = controller.getInputText();
 		if (inputText.isEmpty()) inputText = "0";
 		((TextView) findViewById(R.id.display)).setText(inputText);
 		// Result display
 		if (updateResult && controller.isReady()) {
 			((TextView) findViewById(R.id.result)).setText("=" + controller.getValueText());
 			resetAtNextInput.set();
 		} else {
 			((TextView) findViewById(R.id.result)).setText("=");
 		}
 		// Enable for debug
 		// ((TextView) findViewById(R.id.statusDisplay)).setText(controller.getStatusText() + " " + controller.getInputText());
 	}
 	
 	private void enterLetter(String userInput, Boolean reuseResultWhenReset) {
     	if (resetAtNextInput.get()) {
    			String copyOfLastValue = controller.getValueTextValidAsInput();
    			Boolean valueOverflow = !copyOfLastValue.equals(controller.getValueText());
    			controller.clear();
     		if (reuseResultWhenReset) {
     			if (valueOverflow) {
     				toastError("Overflow");
     			}
     			controller.enter(copyOfLastValue);
     		}
     	}
     	controller.enter(userInput);
     	if (controller.isError()) {
     		toastError("Sorry, input '" + userInput + "' breaks your calcuration");
     		controller.undo();
     	}
 	}
 
     public void onClickNum(View view) {
     	enterLetter(((Button) view).getText().toString(), false);
     	update(false);
     }
 
     public void onClickOp(View view) {
     	if (controller.isBinaryOperationReady()) {
    		controller.execute();
     		resetAtNextInput.set();
     	}
     	enterLetter(((Button) view).getText().toString(), true);
     	update(true);
     }
 
     public void onClickClear(View view) {
     	controller.clear();
     	update(false);
     }
 
     public void onClickUndo(View view) {
     	controller.undo();
     	resetAtNextInput.cancel();
     	update(false);
     }
 
     public void onClickEq(View view) {
    	controller.execute();
     	update(true);
     }
 }

 package com.tal.calculator;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 public class MainActivity extends Activity implements OnClickListener {
 	private Calculator calc;
 	private TextView resultText;
 	private boolean inputtingNew = false;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         
         resultText = (TextView)this.findViewById(R.id.resultText);
         
         calc = new Calculator();
         
         //Set all button's event handlers to the Activity itself
         
         //Numbers...
         View zeroButton = this.findViewById(R.id.button0);
         zeroButton.setOnClickListener(this);
         
         View oneButton = this.findViewById(R.id.button1);
         oneButton.setOnClickListener(this);
         
         View twoButton = this.findViewById(R.id.button2);
         twoButton.setOnClickListener(this);
         
         View threeButton = this.findViewById(R.id.button3);
         threeButton.setOnClickListener(this);
         
         View fourButton = this.findViewById(R.id.button4);
         fourButton.setOnClickListener(this);
         
         View fiveButton = this.findViewById(R.id.button5);
         fiveButton.setOnClickListener(this);
         
         View sixButton = this.findViewById(R.id.button6);
         sixButton.setOnClickListener(this);
         
         View sevenButton = this.findViewById(R.id.button7);
         sevenButton.setOnClickListener(this);
         
         View eightButton = this.findViewById(R.id.button8);
         eightButton.setOnClickListener(this);
         
         View nineButton = this.findViewById(R.id.button9);
         nineButton.setOnClickListener(this);  
         
         //Operations
         View addButton = this.findViewById(R.id.buttonAdd);
         addButton.setOnClickListener(this); 
         
         View subButton = this.findViewById(R.id.buttonSubtract);
         subButton.setOnClickListener(this); 
         
         View multButton = this.findViewById(R.id.buttonMultiply);
         multButton.setOnClickListener(this); 
         
         View divButton = this.findViewById(R.id.buttonDivide);
         divButton.setOnClickListener(this); 
         
         //Point
         View pointButton = this.findViewById(R.id.buttonPoint);
         pointButton.setOnClickListener(this); 
         
         //Equals
         View eqButton = this.findViewById(R.id.buttonEquals);
         eqButton.setOnClickListener(this); 
         
         //Clear
         View clButton = this.findViewById(R.id.buttonClear);
         clButton.setOnClickListener(this); 
         
         //Backspace
         View bkspButton = this.findViewById(R.id.buttonBackspace);
         bkspButton.setOnClickListener(this); 
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
 
 	@Override
 	public void onClick(View v) {
 		
 		//Use this one onClick for all buttons to save resources
 		switch(v.getId()) {
 		
 		//Number buttons enter into input field
 		case R.id.button0:
 			processNumber(0);
 			break;
 		case R.id.button1:
 			processNumber(1);
 			break;
 		case R.id.button2:
 			processNumber(2);
 			break;
 		case R.id.button3:
 			processNumber(3);
 			break;
 		case R.id.button4:
 			processNumber(4);
 			break;
 		case R.id.button5:
 			processNumber(5);
 			break;
 		case R.id.button6:
 			processNumber(6);
 			break;
 		case R.id.button7:
 			processNumber(7);
 			break;
 		case R.id.button8:
 			processNumber(8);
 			break;
 		case R.id.button9:
 			processNumber(9);
 			break;
 		case R.id.buttonPoint:
 			addDecimalPoint();
 			break;	
 		case R.id.buttonAdd:
 			doCalc();
 			calc.setOperation(Operation.OP_ADD);
 			break;
 		case R.id.buttonSubtract:
 			doCalc();
 			calc.setOperation(Operation.OP_SUBTRACT);
 			break;
 		case R.id.buttonMultiply:
 			doCalc();
 			calc.setOperation(Operation.OP_MULTIPLY);		
 			break;
 		case R.id.buttonDivide:
 			doCalc();
 			calc.setOperation(Operation.OP_DIVIDE);	
 			break;
 		case R.id.buttonClear:
 			clear();
 			break;
 		case R.id.buttonEquals:
 			doCalc();
 			calc.reInitResults();
 			runKeyAnim();
 			break;
 		case R.id.buttonBackspace:
 			backSpace();
 			break;
 
 		}	
 	}
 	
 	//Clear function. First call clears input field, second press clears all...
 	private void clear(){
 		String displayed = (String)resultText.getText();
 		
 		if(displayed == "0"){
 			calc.clearAllBuffers();
 		}
 		else{
 			calc.clearInputBuffer();
 			resultText.setText("0");
 		}		
 	}
 	
 	//Visualisation of inputting numbers...
 	private void processNumber(int num){
 		String displayed = (String)resultText.getText();
 		
 		if(inputtingNew){
 			displayed = "";
 			inputtingNew = false;
 		}
 		else{
 			if(displayed.startsWith("0") && !displayed.startsWith("0.")){
 				displayed = "";
 			}	
 		}
 		
 		displayed += num;
 		resultText.setText(displayed);
 		calc.setInputBuffer(Double.parseDouble(displayed));
 	}
 	
 	private void addDecimalPoint(){
 		String displayed = (String)resultText.getText();
 		
 		if(!displayed.contains(".")){
 			resultText.setText(displayed + ".");
 		}	
 	}
 	
 	private void backSpace(){
 		String displayed = (String)resultText.getText();
 		displayed = (displayed.substring(0, displayed.length() - 1));
 		
 		if(displayed.length() <= 0)
 			displayed = "0";
 		
 		resultText.setText(displayed);
 	}
 	
 	//'Equals' - do the calculation and update result display
 	private void doCalc(){
 		resultText.setText("" + calc.getResult());
 		inputtingNew = true;
 	}
 	
 	//Animation stuff
 	private void runKeyAnim(){		
 		final Animation keyAnim = AnimationUtils.loadAnimation(this, R.anim.key_anim);
 		
 		for(int i=0; i<=9; i++){
 			int resID = getResources().getIdentifier("button" + i, "id", getPackageName());
 			Button b = (Button) this.findViewById(resID);
 			b.startAnimation(keyAnim);
 		}
 	}
 }

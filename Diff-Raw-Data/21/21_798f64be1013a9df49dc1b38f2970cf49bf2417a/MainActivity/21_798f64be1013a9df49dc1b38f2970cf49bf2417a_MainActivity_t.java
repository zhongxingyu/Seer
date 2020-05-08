 package my.android.mycalculator;
 
import java.text.DecimalFormat;

 import android.os.Bundle;
 import android.app.Activity;
 import android.text.method.ScrollingMovementMethod;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class MainActivity extends Activity implements OnClickListener {
 	//       
 	TextView Args, editArgs;
 	// 
 	Button btnClr, btnClrE, btnPls;
 	Button btn1, btn2, btn3, btnMns;
 	Button btn4, btn5, btn6, btnMlt;
 	Button btn7, btn8, btn9, btnDvd;
 	Button btn0, btnDot, btnEqvl;
 	//   (+, -, *, /)
 	int action = 0;
 	//  
 	float arg1;
 	
 	Calculator calc = new Calculator();
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         //  
         Args = (TextView) findViewById(R.id.tvArgs);
         //      
         Args.setMovementMethod(new ScrollingMovementMethod());
         editArgs = (TextView) findViewById(R.id.tvEditArgs);
     	(btnClr = (Button) findViewById(R.id.btnClr)).setOnClickListener(this);
     	(btnClrE = (Button) findViewById(R.id.btnClrE)).setOnClickListener(this);
     	(btnPls = (Button) findViewById(R.id.btnPls)).setOnClickListener(this);
     	(btn1 = (Button) findViewById(R.id.btn1)).setOnClickListener(this);
     	(btn2 = (Button) findViewById(R.id.btn2)).setOnClickListener(this);
     	(btn3 = (Button) findViewById(R.id.btn3)).setOnClickListener(this);
     	(btnMns = (Button) findViewById(R.id.btnMns)).setOnClickListener(this);
     	(btn4 = (Button) findViewById(R.id.btn4)).setOnClickListener(this);
     	(btn5 = (Button) findViewById(R.id.btn5)).setOnClickListener(this);
     	(btn6 = (Button) findViewById(R.id.btn6)).setOnClickListener(this);
     	(btnMlt = (Button) findViewById(R.id.btnMlt)).setOnClickListener(this);
     	(btn7 = (Button) findViewById(R.id.btn7)).setOnClickListener(this);
     	(btn8 = (Button) findViewById(R.id.btn8)).setOnClickListener(this);
     	(btn9 = (Button) findViewById(R.id.btn9)).setOnClickListener(this);
     	(btnDvd = (Button) findViewById(R.id.btnDvd)).setOnClickListener(this);
     	(btn0 = (Button) findViewById(R.id.btn0)).setOnClickListener(this);
     	(btnDot = (Button) findViewById(R.id.btnDot)).setOnClickListener(this);
     	(btnEqvl = (Button) findViewById(R.id.btnEqvl)).setOnClickListener(this);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
 
 	public void onClick(View arg0) {
		//  
		DecimalFormat argFormat = new DecimalFormat("###.###");
		
 		//        
 		switch(arg0.getId()) {
 		//    
 		case R.id.btnClr:						
 			editArgs.setText("0");
 			action = 0;
 			break;
 
 		//        
 		case R.id.btnClrE:						
 			editArgs.setText("0");
 			Args.setText("");
 			action = 0;
 			break;
 		
 		//  +
 		case R.id.btnPls:
 			if(action == 0) {						//     - 
 				arg1 = Float.parseFloat(editArgs.getText().toString());		//  
				Args.setText(Args.getText() + "-------\n"+ argFormat.format(arg1) + " +");	//   
 				editArgs.setText("0");				//      0
 				action = 1;							//  
 			}
 			break;
 		
 		//  -
 		case R.id.btnMns:
 			if(action == 0) {						//     - 
 				arg1 = Float.parseFloat(editArgs.getText().toString());		//  
				Args.setText(Args.getText() + "-------\n"+ argFormat.format(arg1) + " -");	//   
 				editArgs.setText("0");				//      0
 				action = 2;							//  
 			}
 			break;
 			
 		//  *
 		case R.id.btnMlt:
 			if(action == 0) {						//     - 
 				arg1 = Float.parseFloat(editArgs.getText().toString());		//  
				Args.setText(Args.getText() + "-------\n"+ argFormat.format(arg1) + " *");	//   
 				editArgs.setText("0");				//      0
 				action = 3;							//  
 			}
 			break;
 		
 		//  /
 		case R.id.btnDvd:
 			if(action == 0) {						//     - 
 				arg1 = Float.parseFloat(editArgs.getText().toString());		//  
				Args.setText(Args.getText() + "-------\n"+ argFormat.format(arg1) + " /");	//   
 				editArgs.setText("0");				//      0
 				action = 4;							//  
 			}
 			break;
 			
 		//  0
 		case R.id.btn0:
 			if(Float.parseFloat(editArgs.getText().toString()) == 0) {		//     
 				editArgs.setText("0");										//   
 			} else {														// 
 				editArgs.setText(editArgs.getText() + "0");					//    0
 			}
 			break;
 			
 		//  1			
 		case R.id.btn1:
 			if(Float.parseFloat(editArgs.getText().toString()) == 0) {		//      
 				editArgs.setText("1");										//   1
 			} else {														// 
 				editArgs.setText(editArgs.getText() + "1");					//    1
 			}
 			break;
 		
 		//  2	
 		case R.id.btn2:
 			if(Float.parseFloat(editArgs.getText().toString()) == 0) {		//     
 				editArgs.setText("2");										//   2
 			} else {														// 
 				editArgs.setText(editArgs.getText() + "2");					//    2
 			}
 			break;
 			
 		//  3	
 		case R.id.btn3:
 			if(Float.parseFloat(editArgs.getText().toString()) == 0) {		//     
 				editArgs.setText("3");										//   3
 			} else {														// 
 				editArgs.setText(editArgs.getText() + "3");					//    3
 			}
 			break;
 			
 		//  4	
 		case R.id.btn4:
 			if(Float.parseFloat(editArgs.getText().toString()) == 0) {		//     
 				editArgs.setText("4");										//   4
 			} else {														// 
 				editArgs.setText(editArgs.getText() + "4");					//    4
 			}
 			break;
 			
 		//  5
 		case R.id.btn5:
 			if(Float.parseFloat(editArgs.getText().toString()) == 0) {		//     
 				editArgs.setText("5");										//   5
 			} else {														// 
 				editArgs.setText(editArgs.getText() + "5");					//    5
 			}
 			break;
 
 		//  6	
 		case R.id.btn6:
 			if(Float.parseFloat(editArgs.getText().toString()) == 0) {		//     
 				editArgs.setText("6");										//   6
 			} else {														// 
 				editArgs.setText(editArgs.getText() + "6");					//    6
 			}
 			break;
 			
 		//  7	
 		case R.id.btn7:
 			if(Float.parseFloat(editArgs.getText().toString()) == 0) {		//     
 				editArgs.setText("7");										//   7
 			} else {														// 
 				editArgs.setText(editArgs.getText() + "7");					//    7
 			}
 			break;
 			
 		//  8	
 		case R.id.btn8:
 			if(Float.parseFloat(editArgs.getText().toString()) == 0) {		//     
 				editArgs.setText("8");										//   8
 			} else {														// 
 				editArgs.setText(editArgs.getText() + "8");					//    8
 			}
 			break;
 			
 		//  9	
 		case R.id.btn9:
 			if(Float.parseFloat(editArgs.getText().toString()) == 0) { 		//     
 				editArgs.setText("9");										//   9
 			} else {														// 
 				editArgs.setText(editArgs.getText() + "9");					//    9
 			}
 			break;
 			
 		//  .
 		case R.id.btnDot:
 			if(!editArgs.getText().toString().contains(".")) {				//     
 				editArgs.setText(editArgs.getText() + ".");					//    
 			}
 			break;
 			
 		//  =			
 		case R.id.btnEqvl:
 			//     
 			float result = 0, arg2 = Float.parseFloat(editArgs.getText().toString());
 			switch(action) {
 			// 
 			case 1:
 				result = calc.Add(arg1, arg2);
 				break;
 			
 			//
 			case 2:
 				result = calc.Deduct(arg1, arg2);
 				break;
 				
 			// 					
 			case 3:
 				result = calc.Multiply(arg1, arg2);
 				break;
 				
 			// 					
 			case 4:
 				result = calc.Divide(arg1, arg2);
 				break;					
 			}
 			
 			if(action != 0) {												//    
				Args.setText(Args.getText() + " " + argFormat.format(arg2) + " = " + argFormat.format(result) + "\n");	//  
 				editArgs.setText("0");										//     
 				arg1 = 0;													//   
 				action = 0;													//  
 			}
 			break;
 		}		
 	}
 }

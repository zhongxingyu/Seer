 package au.edu.qut.inn372.greenhat.activity;
 
 import java.io.Serializable;
 
 import android.os.Bundle;
 import android.os.StrictMode;
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.View;
 import android.widget.EditText;
 import au.edu.qut.inn372.greenhat.bean.Calculator;
 import au.edu.qut.inn372.greenhat.mediator.CalculatorMediator;
 
 public class LoginActivity extends Activity {
 	
 	//Added by Martins
 	private CalculatorMediator calculatorMediator;
 
 	@SuppressLint({ "NewApi", "NewApi", "NewApi" })
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_login);
         StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
         StrictMode.setThreadPolicy(policy);
         
         //Added by Martins
        calculatorMediator = new CalculatorMediator(); //creates a new calculator
     	
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_login, menu);
         return true;
     }
     
     public void login (View view){
     	Intent intent = new Intent(this, TabbedActivity.class);
     	
     	//Added by Martin
     	//intent.putExtra("CalculatorMediator", getCalculatorMediator());
    	calculatorMediator.validateCredentials();
     	
     	startActivity(intent);
     	
     }
     
     public void register (View view) {
     	Intent intent = new Intent(this, RegistrationActivity.class);
     	startActivity(intent);
     }
     
     
     
     
     
     //Added by Martins
 	/**
 	 * Saves current input data to the calculator bean
 	 */
 	private void saveData() {
 		Calculator calculator = calculatorMediator.getCalculator();
 		calculator.getCustomer().getUserProfile().setEmail(new String(((EditText)findViewById(R.id.editUser)).getText().toString()));
 		calculator.getCustomer().getUserProfile().setPassword(new String(((EditText)findViewById(R.id.editPassword)).getText().toString()));
 		//TODO Insert input fields for other information
 	}
 	/**
 	 * @return the calculatorMediator
 	 */
 	public CalculatorMediator getCalculatorMediator() {
 		return calculatorMediator;
 	}
 	
 	/**
 	 * Perform the WS call to calculate energy production (and financial output)
 	 */
 	public void validateCredentials() {
 		calculatorMediator.validateCredentials();
 	}
 }

 package com.example.RobolectricDemo;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.RadioGroup;
 
 public class CalculatorActivity extends Activity {
     RadioGroup radioGroupOperations = null;
     EditText firstOperandTextView = null;
     EditText secondOperandTextView = null;
     public static final String RESULT_EXTRAS = "result";
 
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         radioGroupOperations = (RadioGroup) findViewById(R.id.radio_group_operations);
         radioGroupOperations.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
             @Override
             public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                 switch (checkedId) {
                     case R.id.radio_add: makeBothOperandsVisible();
                         break;
                     case R.id.radio_factorial: hideSecondOperand();
                         break;
 
                 }
             }
         });
     }
 
     private void makeBothOperandsVisible() {
         findViewById(R.id.first_operand).setVisibility(View.VISIBLE);
         findViewById(R.id.second_operand).setVisibility(View.VISIBLE);
     }
 
     private void hideSecondOperand() {
         findViewById(R.id.second_operand).setVisibility(View.GONE);
     }
 
     public void performOperation(View view){
         int secondOperand = 0;
         int firstOperand = 0;
         firstOperandTextView = (EditText) findViewById(R.id.first_operand);
         secondOperandTextView = (EditText) findViewById(R.id.second_operand);
 
         firstOperand = Integer.parseInt(this.firstOperandTextView.getText().toString());
        if (secondOperandTextView.getVisibility() == View.VISIBLE)
             secondOperand = Integer.parseInt(this.secondOperandTextView.getText().toString());
 
         RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radio_group_operations);
         switch (radioGroup.getCheckedRadioButtonId()){
             case R.id.radio_add: setResultInResultView(add(firstOperand, secondOperand));
                 break;
             case R.id.radio_factorial: setResultInResultView(factorial(firstOperand));
                 break;
         }
 
         showResultInNewActivity();
     }
 
     private void showResultInNewActivity() {
         EditText resultTextView = (EditText) findViewById(R.id.text_result);
         String resultText = resultTextView.getText().toString();
         Intent intent = new Intent(this, ViewResultActivity.class);
         intent.putExtra(RESULT_EXTRAS, resultText);
         startActivity(intent);
     }
 
     private int factorial(int operand) {
         int factorial = 1;
         for(int i = 1; i <= operand; i++)
             factorial = factorial * i;
 
         return factorial;
     }
 
     private int add(int firstOperand, int secondOperand) {
         return (firstOperand + secondOperand);
     }
 
     private void setResultInResultView(int result) {
         EditText resultTextView = (EditText) findViewById(R.id.text_result);
         resultTextView.setText(String.valueOf(result));
     }
 }

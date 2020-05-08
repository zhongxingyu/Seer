 package com.dennisideler.calculator;
 
 import java.text.DecimalFormat;
 import java.util.LinkedList;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.widget.TextView;
 
 public class MainActivity extends Activity {
 	
 	public static final String ADD = "\u002B";
 	public static final String SUB = "\u2212";
 	public static final String DIV = "\u00F7";
 	public static final String MUL = "\u2715";
 	public String value = "";
 	public LinkedList<String> operators = new LinkedList<String>();
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         //getMenuInflater().inflate(R.menu.activity_main, menu); // There are no settings for this app.
         return true;
     }
     
     // Event handlers must be public, void, and have a view object as their only parameter!
     public void registerKey(View view)
     {
     	switch(view.getId())
     	{
     	case R.id.button0:
     		safelyPlaceOperand("0");
     		break;
     	case R.id.button1:
     		safelyPlaceOperand("1");
     		break;
     	case R.id.button2:
     		safelyPlaceOperand("2");
     		break;
     	case R.id.button3:
     		safelyPlaceOperand("3");
     		break;
     	case R.id.button4:
     		safelyPlaceOperand("4");
     		break;
     	case R.id.button5:
     		safelyPlaceOperand("5");
     		break;
     	case R.id.button6:
     		safelyPlaceOperand("6");
     		break;
     	case R.id.button7:
     		safelyPlaceOperand("7");
     		break;
     	case R.id.button8:
     		safelyPlaceOperand("8");
     		break;
     	case R.id.button9:
     		safelyPlaceOperand("9");
     		break;
     	case R.id.buttonAdd: 
     		safelyPlaceOperator(ADD);
     		break;
     	case R.id.buttonSub:
     		safelyPlaceOperator(SUB);
     		break;
     	case R.id.buttonDiv:
     		safelyPlaceOperator(DIV);
     		break;
     	case R.id.buttonMul:
     		safelyPlaceOperator(MUL);
     		break;
     	case R.id.buttonDel:
     		deleteFromLeft();
     		break;
     	}
     	display();
     }
     
     private void display()
     {
     	TextView tvAns = (TextView) findViewById(R.id.textViewAns);
     	tvAns.setText(value);
     }
     
     private void display(String s)
     {
     	TextView tvAns = (TextView) findViewById(R.id.textViewAns);
     	tvAns.setText(s);
     }
     
    private void safelyPlaceOperand(String op) // FIXME: this doesn't allow double zeroes at all! don't check end, check front of last operand
     {
    	if (op.equals("0") && !value.endsWith(op))  // Avoid double zeroes.
    	{
    		value += op;
    	}
    	else
    	{
    		deleteLeadingZero();
    		value += op;
    	}
     }
     
     private void safelyPlaceOperator(String op)
     {
     	if (endsWithOperator())  // Avoid double operators by replacing operator.
 		{
 			deleteFromLeft();
 			value += op;
 			operators.add(op);
 		}
 		else if (endsWithNumber())  // Safe to place operator right away.
 		{
 			value += op;
 			operators.add(op);
 		}
     	// else: Operator by itself is an illegal operation, do not place operator.
     }
     
    private void deleteLeadingZero()
     {
     	if (value.endsWith("0")) deleteFromLeft();
     }
     
     private void deleteFromLeft()
     {
     	if (value.length() > 0)
     	{
     		if (endsWithOperator()) operators.removeLast(); 
     		value = value.substring(0, value.length()-1);
     	}
     }
     
     private boolean endsWithNumber()
     {
     	// If we had a decimal point button, we'd have to grab the digit before it.
     	return value.length() > 0 && Character.isDigit(value.charAt(value.length()-1));
     }
      
     private boolean endsWithOperator()
     {
     	if (value.endsWith(ADD) || value.endsWith(SUB) || value.endsWith(MUL) || value.endsWith(DIV)) return true;
     	else return false;
     }
     
     public void calculate(View view)  // Note: only called by the '=' button.
     {
     	if (operators.isEmpty()) return;  // There is no operation to perform yet.
     	if (endsWithOperator())
     	{
     		display("incorrect format");
     		value = "";
     		return;
     	}
     		
     	// StringTokenizer is deprecated. Using String.split instead.
     	String[] operands = value.split("\\u002B|\\u2212|\\u00F7|\\u2715");
 
     	int i = 0;
     	double ans = Double.parseDouble(operands[i]); // TODO: catch NumberFormatException?
     	for (String operator : operators)
     		ans = applyOperation(operator, ans, Double.parseDouble(operands[++i]));
 
     	DecimalFormat df = new DecimalFormat("0.###");
     	display(df.format(ans));
     	resetCalculator();
     	// TODO: overwrite value with ans. reset value on next keypress if it's an operand (leave it if it's an operator)
     }
     
     private double applyOperation(String operator, double operand1, double operand2)
     {
     	// Not using the string with switch syntax because Android doesn't support JDK 7 (JRE 1.7) yet.
     	if (operator.equals(ADD)) return operand1 + operand2;
     	if (operator.equals(SUB)) return operand1 - operand2;
     	if (operator.equals(MUL)) return operand1 * operand2;
     	if (operator.equals(DIV)) return operand1 / operand2;  // Don't need to check for div by 0, Java already does.
     	return 0.0;
     }
     
     private void resetCalculator()
     {
     	value = "";
     	operators.clear();
     }
 }

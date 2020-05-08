 package tschida.david.calculatorsimple;
 
 import tschida.david.utils.Calculator;
 import android.os.Bundle;
 import android.app.Activity;
 import android.graphics.Color;
 import android.graphics.Typeface;
 import android.view.Menu;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.TextView;
 
 /**
  * This class contains the code for a simple calculator for Android. It was
  * built as a sample project for EPICS APPS.
  * 
  * Currently it is able to do basic commands (+*-/). Upon loading, the user sees
  * two EditText's where they can input numbers. Under those are a set of buttons
  * for the calculator operations. Answers, and errors, are displayed below the
  * buttons.
  * 
  * @author David Tschida (Vidia)
  * @version v0.0.1 (Beta)
  */
 public class MainActivity extends Activity
 {
 	// Fields for the GUI items that are accessed by more than one method.
 	private TextView outputBox; // Where the solution is displayed.
 	private TextView txt_signPlaceholder; // Displays the selected operation
 											// (sign) between the operands.
 	
 	private EditText lOperand; // Left operand
 	private EditText rOperand; // Right...
 	
 	boolean signChosen; // A variable to state whether or not an operator button
 	
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu)
 	{
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 	
 	
 	/**
 	 * Inherited from Activity. Initializes the fields above.
 	 * 
 	 * @see android.app.Activity#onCreate(android.os.Bundle)
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		
 		txt_signPlaceholder = (TextView) findViewById(R.id.txt_signPlaceholder);
 		signChosen = false;
 		
 		lOperand = (EditText) findViewById(R.id.etxt_lOperand);
 		rOperand = (EditText) findViewById(R.id.etxt_rOperand);
 		outputBox = (TextView) findViewById(R.id.txt_solution);
 		
 		// Font source < www.fontspace.com/blue-vinyl/pocket-calculator >
 		String fontPath = "fonts/calc_font/POCKC___.TTF";
 		Typeface tf = Typeface.createFromAsset(getAssets(), fontPath);
 		outputBox.setTypeface(tf);
 	}
 	
 	/**
 	 * Called by the ClickListener for the "add" button. Changes the
 	 * txt_signPlaceholder to display the appropriate sign.
 	 */
 	public void click_btn_add(View view)
 	{
 		txt_signPlaceholder.setText("+");
 		signChosen = true;
 	}
 	
 	/**
 	 * Called by the ClickListener for the "subtract" button. Changes the
 	 * txt_signPlaceholder to display the appropriate sign.
 	 */
 	public void click_btn_subtract(View view)
 	{
 		txt_signPlaceholder.setText("-");
 		signChosen = true;
 	}
 	
 	/**
 	 * Called by the ClickListener for the "multiply" button. Changes the
 	 * txt_signPlaceholder to display the appropriate sign.
 	 */
 	public void click_btn_multiply(View view)
 	{
 		txt_signPlaceholder.setText("*");
 		signChosen = true;
 	}
 	
 	/**
 	 * Called by the ClickListener for the "divide" button. Changes the
 	 * txt_signPlaceholder to display the appropriate sign.
 	 */
 	public void click_btn_divide(View view)
 	{
 		txt_signPlaceholder.setText("/");
 		signChosen = true;
 	}
 	
 	/**
 	 * Called by the ClickListener for the "add" button. Handles all of the
 	 * error checking and calculating for the app. Checks that all of the values
 	 * have been inputed and alerts the user if there are problems.
 	 * 
 	 * This method works well as it is, but I feel like it could be split into a
 	 * few methods to simplify the code. - Calculation code will be moved into a
 	 * separate "Calculator" class at a later time with exception handling.
 	 */
 	public void click_btn_finish(View view)
 	{
 		printErr(false, ""); // clears the error output box.
 		double solution = 0;
 		
 		boolean error_occurred = false;
 		
 		String leftOpStr = lOperand.getText().toString();
 		String rightOpStr = rOperand.getText().toString();
 		
 		char operator = ((String) txt_signPlaceholder.getText()).charAt(0);
 		
 		double leftOperand = 0, rightOperand = 0;
 		
 		/* Begin error handling */
 		try
 		{
 			leftOperand = Double.parseDouble(leftOpStr);
 		} catch (NumberFormatException e)
 		{
 			error_occurred = true;
 			printErr(true, "Enter a valid number as the left operand.\n");
 		}
 		try
 		{
 			rightOperand = Double.parseDouble(rightOpStr);
 		} catch (NumberFormatException e)
 		{
 			error_occurred = true;
 			printErr(true, "Enter a valid number as the right operand.\n");
 		}
 		
 		if (!error_occurred)
 		{
 			try
 			{
 				solution = Calculator.calculate(leftOperand, operator,
 						rightOperand);
 			} catch (ArithmeticException a)
 			{
 				printErr(true,
 						"Cannot Divide by Zero! Some men just want to watch the world burn!\n");
				error_occurred=true; 
 			} catch (IllegalArgumentException i)
 			{
 				printErr(true, "Please select an operator.\n");
				error_occurred=true;
 			}
 		}
 		
 		if (!error_occurred)
 			outputBox.setText("Answer: " + solution);
 		else
 			outputBox.setText("Answer: ");
 		
 		/*
 		 * if (!error_occurred) { switch (operator) { case '+': solution =
 		 * leftOperand + rightOperand; break; case '-': solution = leftOperand -
 		 * rightOperand; break; case '*': solution = leftOperand * rightOperand;
 		 * break; case '/': if (rightOperand == 0) {
 		 * printErr("Cannot divide by 0! Are you trying to blow up the world?");
 		 * /* This error was not handled earlier to prevent having to check the
 		 * operator a second time. \*\/ error_occurred = true; } else solution =
 		 * leftOperand / rightOperand; break; default: printErr(
 		 * "That operator is not valid. I don't know how you managed to do that."
 		 * );
 		 * 
 		 * } if (!error_occurred) //If this value is true at this point, then
 		 * the only option is that a divide by zero was attempted.
 		 * outputBox.setText("Answer: " + solution); else
 		 * outputBox.setText("Answer: UNDEFINED"); }
 		 * 
 		 * 
 		 * /* TODO: 1. Possibly make Calculator helper class (or use Math). 2.
 		 * Organize code in more readable format. 3. Search for best way to
 		 * handle getting GUI items. 4. Move GUI items into some type of storage
 		 * area. (fields or storage class like R) 5. Add task bar with various
 		 * funny statuses. 6. Change format of app to look nicer. 7. Proper way
 		 * to use string literals in android code. 8. Then of course test and
 		 * comment. (1 comment per 5 lines, be impressive)
 		 */
 	}
 	
 	/**
 	 * A currently unused print method that takes its argument string and writes
 	 * it to a TextView on the screen.
 	 * 
 	 * @param text
 	 *            The text to be written to the screen.
 	 */
 	private void print(boolean append, String text)
 	{
 		TextView outputBox = (TextView) findViewById(R.id.txt_status);
 		outputBox.setTextColor(Color.BLACK);
 		if (append)
 			outputBox.append(text);
 		else
 			outputBox.setText(text);
 	}
 	
 	/**
 	 * A print error method that prints red text to the screen to alert the user
 	 * of errors in their input. It works mostly the same as print(String).
 	 * 
 	 * @param text
 	 *            The text to be written to the screen.
 	 */
 	private void printErr(boolean append, String text)
 	{
 		TextView outputBox = (TextView) findViewById(R.id.txt_status);
 		outputBox.setTextColor(Color.RED);
 		if (append)
 			outputBox.append(text);
 		else
 			outputBox.setText(text);
 	}
 	
 	public void easter_egg(View view)
 	{
 		print(false, "Testing...");
 	}
 	
 }

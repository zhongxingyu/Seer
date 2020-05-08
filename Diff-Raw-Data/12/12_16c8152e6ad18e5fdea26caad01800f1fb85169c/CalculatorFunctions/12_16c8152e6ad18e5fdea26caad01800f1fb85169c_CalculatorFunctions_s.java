 package project1.calculator;
 
 /**
  * A class designed to hold all of the functions of a basic calculator.
  * This class gives functionality to the calculator buttons.
  * It also manages the logic behind the calculator.
  * @author 
  *
  */
 public class CalculatorFunctions {
 
 	private String firstInput="q";
 	private boolean dotTyped = false;
 	private String secondInput;
 	private String operator="0";
 	private String lastPressedButton="0";
 	private boolean secondInputReady = false;
 	private String lastResult="0";
 	private String memory = "0";
	//Last result temporally  saves the result computed in the previous iteration. If the user clicks equals the firstInput is set to "q" (sentinel value)
 	
 
 	/**
 	 * Computes the square root of the given number.
 	 * @param num the number to find the square root of.
 	 * @return the square root.
 	 */
 	public String sqrt(String num)
 	{ 
 		secondInputReady = true;
 		return Double.toString(Math.sqrt(Double.parseDouble(num)));
 	}
 
 	/**
 	 * Computes the inverse of the given number.
 	 * @param num the number to find the inverse of.
 	 * @return the inverse.
 	 */
 	public String inverse(String num)
 	{
 		secondInputReady = true;
 		return Double.toString(1 / (Double.parseDouble(num)));
 	}
 	
 	/**
 	 * Multiplies by minus one.
 	 * @param num the number to be multiplied by minus one.
 	 * @return the number multiplied by one.
 	 */
 	public String signChange(String num)
 	{
 		if(!num.equals("0"))
 		{
 			String eliminateZero = Double.toString(Double.parseDouble(num));
 			if(eliminateZero.charAt(eliminateZero.length() - 1 ) == '0' && eliminateZero.charAt(eliminateZero.length() - 2 ) =='.')
 			{
 				eliminateZero = Double.toString(Double.parseDouble(eliminateZero) * (-1));
 				eliminateZero = eliminateZero.substring(0, eliminateZero.length() - 2);
 			}
 			else
 			{
 				eliminateZero = Double.toString(Double.parseDouble(eliminateZero) * (-1));
 			}
 			return eliminateZero;
 		}
 		else
 			return "0";
 	}
 
 	/**
 	 * Gets the last button the user pressed.
 	 * @return the last button the user pressed.
 	 */
 	public String getLastPressedButton()
 	{
 		return lastPressedButton;
 	}
 
 	/**
 	 * Saves the last button the user pressed.
 	 * @param lastPressedButton the last button the user pressed.
 	 */
 	public void setLastPressedButton(String lastPressedButton)
 	{
 		this.lastPressedButton = lastPressedButton;
 	}
 
 	/**
 	 * Performs the sum of the number in the display and the number in memory(firstInput is the number in memory).
 	 * @param currentCalculatorNumber
 	 */
 	public String sumPressed(String currentNumberInDisplay)
 	{
 		if(!firstInput.equals("q") && secondInputReady == true )
 		{
 			secondInput = currentNumberInDisplay;
 			firstInput = this.compute(firstInput,secondInput);
 		}
 		else
 			firstInput = currentNumberInDisplay;
 		
 		operator = "+";
 		secondInputReady = false;
 		dotTyped = false;
 		return firstInput;
 	}
 	
 	/**
 	 *Performs the subtraction of the number in the display and the number in memory (firstInput is the number on memory).
 	 *@param currentNumberInDisplay
 	 *@return firstInput
 	 */
 	public String minusPressed(String currentNumberInDisplay)
 	{
 		if(!firstInput.equals("q") && secondInputReady == true )
 		{
 			secondInput = currentNumberInDisplay;
 			firstInput = this.compute(firstInput,secondInput);
 		}
 		else
 			firstInput = currentNumberInDisplay;
 		
 		operator = "-";
 		dotTyped = false;
 		secondInputReady = false;
 		return firstInput;
 	
 	}
 	
 	/**
 	 * Performs the multiplication of the number in the display and the number in memory (firstInput is the number on memory).
 	 * @param currentNumberInDisplay
 	 * @return firstInput
 	 */
 	public String multPressed(String currentNumberInDisplay )
 	{
 		if(!firstInput.equals("q") && secondInputReady == true )
 		{
 			secondInput = currentNumberInDisplay;
 			firstInput = this.compute(firstInput,secondInput);
 		}
 		else
 			firstInput = currentNumberInDisplay;
 		
 		operator = "*";
 		secondInputReady = false;
 		dotTyped = false;
 		return firstInput;
 	}
 	
 	/**
 	 * Performs the division of the number in the display and the number in memory (firstInput is the number on memory).
 	 * @param currentNumberInDisplay
 	 * @return firstInput
 	 */
 	public String divPressed(String currentNumberInDisplay )
 	{
 		// If the firstInput is not the sentinel value "q" and secondInputReady
 		// is true (it can only be set to true by the press of a button, the sqrt or inverse button)
 		// then we have an input in memory and we compute the result of that and the last operator 
 		// pressed. 
 		if(!firstInput.equals("q") && secondInputReady == true )
 		{
 			secondInput = currentNumberInDisplay;
 			firstInput = this.compute(firstInput,secondInput);
 		}
 		
 		// If it is then we need to save what's currently in the display to perform an operation
 		// once the second input is available for the compute method to work. 
 		else
 			firstInput = currentNumberInDisplay;
 		
 		operator = "/";
 		secondInputReady = false;
 		dotTyped = false;
 		return firstInput;
 	}
 	
 	/**
 	 * Handles what happens if the dot is pressed.
 	 * @return the dot in the desired position or clear the text field if an operator or the 
 	 *  the equals button was pressed.
 	 */
 	public String dotPressed(String currentNumberInDisplay)
 	{
 		if(lastPressedButton.equals("=") 
 				|| lastPressedButton.equals("+") 
 				|| lastPressedButton.equals("-")
 				|| lastPressedButton.equals("/")
 				|| lastPressedButton.equals("*"))
 		{
 			dotTyped = true;
 			return "0.";
 		}
 		else if(dotTyped)
 			return currentNumberInDisplay;
 		else
 		{	
 			dotTyped = true;
 			return currentNumberInDisplay + ".";
 		}
 	}
 	/**	
 	 * Computes the percentage of the given number.
 	 * @param num percentage number.
 	 * @return the percentage 
 	 */
 	public String percent(String num)
 	{
 		String result = "";
 		
 		if (operator.equals("0"))
 			result = "0";
 		
 		else if(!firstInput.equals("q"))
 			result = Double.toString((Double.parseDouble(num) / 100) * Double.parseDouble(firstInput));
 		
 		//This handles when the equals button was pressed, since firstInput = "q" the calculator computes the percentage 
 		// of the last result and the current number in the display
 		else if (firstInput.equals("q"))
 			result = Double.toString((Double.parseDouble(num) / 100) * Double.parseDouble(lastResult));
 		
 		return result;
 	}
 
 	/**
 	 * Computes the result of the computation as a result of the press of the equal button.
 	 * It performs the computation with the last operator pressed.
 	 * @param fTerm the firstInput. (should be in memory as a result of the press of an operator )
 	 * @param sTerm the secondInput. (should be passed when an operator is pressed)
 	 * @return the result of the computation of the last operator pressed.
 	 */
 	public String compute(String fTerm, String sTerm)
 	{
 		double result=0;
 		
 		/*if(lastPressedButton.equals("="))
 		{
 			fTerm = lastResult;
 		}*/
 		
 		if(operator.equals("+"))
 		{
 			result = Double.parseDouble(fTerm) + Double.parseDouble(sTerm);
 		}
 		else if(operator.equals("-"))
 		{
 			result = Double.parseDouble(fTerm) - Double.parseDouble(sTerm);
 		}
 		else if(operator.equals("*"))
 		{
 			result = Double.parseDouble(fTerm) * Double.parseDouble(sTerm);
 		}
 		else if(operator.equals("/"))
 		{
 			result = Double.parseDouble(fTerm) / Double.parseDouble(sTerm);
 		}
 		
 		
 		firstInput = String.valueOf(result);
 	
 		return String.valueOf(result);
 	}
 	
 	/**
 	 * Performs the addition between the number in the screen and the number in the memory.
 	 * It stores the result in memory.
 	 * @param numToAdd the number to be added to the memory.
 	 */
 	public void memoryPlus(String numToAdd)
 	{
 		if(this.getNumInMemory().equals("0"))
 		{//Do nothing
 			
 		}
 		
 		else
 		memory = Double.toString(Double.parseDouble(memory) + Double.parseDouble(numToAdd));
 	}
 	
 	/**
 	 * Return the last result  computed when the = button was pressed.
 	 * @return the last result.
 	 */
 	public String getLastResult()
 	{
 		return lastResult;
 	}
 	
 	/**
 	 * Set the last result in case the user pressed the equals button repeatedly or the percent button
 	 * @param last the currentNumber in the textField after the pressed of the = button.
 	 */
 	public void setLastResult(String last)
 	{
 		lastResult = last;
 	}
 	
 	/**
 	 * Gets the firstInput
 	 * @return the firstInput
 	 */
 	public String getFirstInput()
 	{
 		return firstInput;
 	}
 	
 	/**
 	 * It returns the number that it is stored on memory.
 	 * @return number in memory.
 	 */
 	public String getNumInMemory()
 	{
 		return memory;
 	}
 	
 	/**
 	 * Method to set the first input  field
 	 * @param value the value to set the firstInput to. 
 	 */
 	public void setFirstInput(String value)
 	{
 		firstInput = value;
 	}
 	
 	/**
 	 * Get the secondInput.
 	 * @return the secondInput.
 	 */
 	public String getSecondInput()
 	{
 		return secondInput;
 	}
 	
 	/**
 	 * Setting the second input with the value give.
 	 * @param value the number to set to.
 	 */
 	public void setSecondInput(String value)
 	{
 		secondInput = value;
 	}
 
 	/**
 	 * Get the last operator pressed.
 	 * @return the operator.
 	 */
 	public String getOperator()
 	{
 		return operator;
 	}
 	
 	/**
 	 * Setting the operator when an operator button is pressed.
 	 * @param op the operator.
 	 */
 	public void setOperator(String op)
 	{
 		operator = op;
 	}
 	
 
 	/**
 	 * Sets/resets the memory to the number in screen.
 	 * @param numToMemory the number for which the memory will be set/reset.
 	 */
 	public void setMemoryState(String numToMemory)
 	{
 		memory = numToMemory;
 	}
 	
 	/**
 	 * Check if the calculator is ready for performing an operation.
 	 * @return true if a number, sqrt or inverse button has been pressed.
 	 * false if not.
 	 */
 	public boolean isReadyForSecondInput()
 	{
 		return secondInputReady;
 	}
 	
 	/**
 	 * Sets the calculator ready to perform a calculation or sets it to receive input.
 	 * @param t true if it's going to be ready, false otherwise. 
 	 */
 	public void setReadyForSecondInput(boolean t)
 	{
 		secondInputReady = t;
 	}
 	
 	/**
 	 * Checks if the decimal point has been pressed(to prevent the user to press it again)
 	 * @return true if the dot has been typed, false otherwise.
 	 */
 	public boolean isDotTyped()
 	{
 		return dotTyped;
 	}
 	
 	/**
 	 * Sets the decimal point to pressed(true) or not .
 	 * @param t true if it was typed false otherwise.
 	 */
 	public void setDotTyped(boolean t)
 	{
 		dotTyped = t;
 	}
 	
 	
 	public String equalsPressed(String currentNumberInDisplay)
 	{
 		
 		// If no operator has been pressed then the text field stays the same.	
 		if(operator.equals("0"))
 		{	
 			lastPressedButton = "=";
 			return currentNumberInDisplay;
 		}
 		
 		// If the last pressed button is the =, sqrt, 1/x, or the first input has not been set
 		//we check some conditions.
 		else if(lastPressedButton.equals("=") 
 				|| lastPressedButton.equals("sqrt") 
 				|| firstInput.equals("q")
 				|| lastPressedButton.equals("1/x") )
 
 		{
 		
 			// If we enter here because the last pressed button was any of these then the 
 			// current number in the textField should be passed as second input to the calculator. 
 			if(lastPressedButton.equals("sqrt")
 					||lastPressedButton.equals("1/x") 
 					||lastPressedButton.equals("%") )
 			{
 				secondInput = currentNumberInDisplay;
 			}
 			
 			String result = this.compute(lastResult, secondInput);
 			
 			lastResult = result;
 			lastPressedButton = "=";
 			return result;
 		}
 		//If we enter here then the calculator is ready for normal operation.
 		else 
 		{
 				
 			secondInput= currentNumberInDisplay;
 			this.compute(firstInput, secondInput);
 			lastResult = firstInput;
 			firstInput = "q";
 			secondInputReady = false;
 			dotTyped = false;
 			lastPressedButton = "=";
 			return lastResult;
 		}
 
 	}
 	
 	
 }

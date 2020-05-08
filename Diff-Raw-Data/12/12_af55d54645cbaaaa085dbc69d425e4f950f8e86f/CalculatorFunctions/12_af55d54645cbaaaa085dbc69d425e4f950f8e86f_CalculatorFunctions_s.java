 package project1.calculator;
 
 public class CalculatorFunctions {
 
 	private String firstInput="q";
 	private String secondInput;
 	private String operator="0";
 	private String lastPressedButton="0";
 	private boolean secondInputReady = false;
 	private String lastResult="0";
 	//Last result temporally  saves the result computed in the previous iteration. If the user clicks equals the firstInput is set to "q" (sentinel value)
 	//
 
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
 	
 	public String signChange(String num)
 	{
		
		return Double.toString(Double.parseDouble(num) * (-1));
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
 		return firstInput;
 	}
 	
 	/**
 	 * Performs the division of the number in the display and the number in memory (firstInput is the number on memory).
 	 * @param currentNumberInDisplay
 	 * @return firstInput
 	 */
 	public String divPressed(String currentNumberInDisplay )
 	{
 		if(!firstInput.equals("q") && secondInputReady == true )//&& !lastPressedButton.equals("sqrt"))
 		{
 			secondInput = currentNumberInDisplay;
 			firstInput = this.compute(firstInput,secondInput);
 		}
 		else
 			firstInput = currentNumberInDisplay;
 		
 		operator = "/";
 		secondInputReady = false;
 		return firstInput;
 	}
 
 	/**
 	 * 
 	 * @param fTerm
 	 * @param sTerm
 	 * @return
 	 */
 	public String compute(String fTerm, String sTerm)
 	{
 		double result=0;
 		
 		if(lastPressedButton.equals("="))
 		{
 			fTerm = lastResult;
 		}
 		
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
 	
 	public String getLastResult()
 	{
 		return lastResult;
 	}
 	
 	public void setLastResult(String last)
 	{
 		lastResult = last;
 	}
 	
 	/**
 	 *
 	 * @return
 	 */
 	public String getFirstInput()
 	{
 		return firstInput;
 	}
 	
 	/**
 	 * Method to reset the first input  field
 	 */
 	public void setFirstInput(String value)
 	{
 		firstInput = value;
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public String getSecondInput()
 	{
 		return secondInput;
 	}
 	
 	/**
 	 * Reseting the second input.
 	 */
 	public void setSecondInput(String value)
 	{
 		secondInput = value;
 	}
 
 	/**
 	 * 
 	 * @return
 	 */
 	public String getOperator()
 	{
 		return operator;
 	}
 	
 	/**
 	 * Reseting the operator.
 	 */
 	public void setOperator(String op)
 	{
 		operator = op;
 	}
 	
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public boolean isReadyForSecondInput()
 	{
 		return secondInputReady;
 	}
 	
 	/**
 	 * 
 	 * @param t
 	 */
 	public void setReadyForSecondInput(boolean t)
 	{
 		secondInputReady = t;
 	}
 	
 	
 }

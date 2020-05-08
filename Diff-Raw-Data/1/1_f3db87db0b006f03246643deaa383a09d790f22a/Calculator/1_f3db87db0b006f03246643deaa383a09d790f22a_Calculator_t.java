 
 /*
  * TEAM 8
  * Michael Glander msglande
  * Travis Tippens tctippen
  * Shreye Saxena sjsaxena
  */
 
 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
 import java.math.BigDecimal;
 import java.util.Stack;
 
 public class Calculator implements ActionListener
 {
 	// Main - loads program object
 	public static void main(String[] args){
 		System.out.println("Team 8: Michael Glander, Travis Tippens, Shreye Saxena");
 		new Calculator();
 		}
 	
 	
 	// newLine
 	private String newLine = System.getProperty("line.separator");
 		
 	//GUI Objects
 	private JFrame window = new JFrame("Calculator");
 	private JTextField inputTextField = new JTextField(20);
 	private JTextField outputTextField = new JTextField(10);
 	private JTextField precisionTextField = new JTextField(5); 
 	private JTextField highXTextField = new JTextField(5);
 	private JTextField lowXTextField = new JTextField(5);
 	private JTextField incrementXTextField = new JTextField(5);
 	private JTextField xTextField = new JTextField(15);
 	private JTextArea logTextArea = new JTextArea();
 	private JScrollPane logScrollPane = new JScrollPane(logTextArea);
 	private JLabel inputLabel = new JLabel("Enter input");
 	private JLabel outputLabel = new JLabel(" Result -> ");
 	private JLabel errorLabel = new JLabel("");
 	private JLabel xRangeLabel = new JLabel("    low X          high X     X increment");
 	private JButton xButton = new JButton("Update X = ");
 	private JButton xRangeButton = new JButton("Update X Range");
 	private JButton precisionButton = new JButton("Update Precision:");
 	private JButton clearButton = new JButton("Clear");
 	private JButton recallButton = new JButton("Recall");
 	private JButton graphButton = new JButton("Graph!");
 	private JPanel topPanel = new JPanel(new GridBagLayout());
 	GridBagConstraints c = new GridBagConstraints();
 	private JRadioButton accumulatorMode = new JRadioButton("Accumulator", true);
 	private JRadioButton calculatorMode = new JRadioButton("Calculator", false);
 	private JRadioButton testMode = new JRadioButton("Test Mode", false);
 	private ButtonGroup bGroup = new ButtonGroup();
 	private JPanel bottomPanel = new JPanel();
 	private boolean xRangeIsOkay = false;
 	
 	public Calculator()
 		{
 		// -------------------------------------			
 		// ----------- BUILD GUI ---------------
 		// -------------------------------------
 		
 		//topPanel is created using GridBag to make error message appear on new row
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.gridx = 0;
 		c.gridy = 0;
 		topPanel.add(recallButton, c);
 		c.gridx = 1;
 		topPanel.add(inputLabel, c);	
 		c.gridx = 2;
 		topPanel.add(inputTextField, c);
 		c.gridx = 3;
 		topPanel.add(outputLabel, c);
 		c.gridx = 4;
 		topPanel.add(outputTextField, c);
 		c.gridx = 5;
 		topPanel.add(clearButton, c);	
 		c.gridx = 6;
 		topPanel.add(lowXTextField,c);
 		c.gridx = 7;
 		topPanel.add(highXTextField,c);
 		c.gridx = 8;
 		topPanel.add(incrementXTextField,c);
 		c.gridx = 9;
 		topPanel.add(xRangeButton,c);
 		c.gridwidth = 10;
 		c.gridx = 0;
 		c.gridy = 1;
 		topPanel.add(errorLabel,c);
 		c.gridx = 6;
 		topPanel.add(xRangeLabel,c);
 		
 		
 		bottomPanel.setLayout(new GridLayout(1,3));
 		bottomPanel.add(accumulatorMode);
 		bottomPanel.add(calculatorMode);
 		bottomPanel.add(testMode);
 		bottomPanel.add(precisionButton);
 		bottomPanel.add(precisionTextField);
 		bottomPanel.add(xButton);
 		bottomPanel.add(xTextField);
 		bottomPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Mode Select"));
 		
 		
 		window.getContentPane().add(topPanel, "North");
 		window.getContentPane().add(logScrollPane, "Center");
 		window.getContentPane().add(bottomPanel, "South");
 		
 		// Set GUI attributes
 		bGroup.add(accumulatorMode);
 		bGroup.add(calculatorMode);
 		bGroup.add(testMode);
 		logTextArea.setEditable(false);
 		logTextArea.setFont(new Font("default", Font.BOLD, 20));
 		clearButton.setBackground((Color.BLACK));
 		clearButton.setForeground(Color.YELLOW);
 		errorLabel.setForeground(Color.RED);
 		inputTextField.setEditable(true);
 		outputTextField.setEditable(false);
 		window.setLocation(10, 10); // horizontal, vertical
 		window.setSize(1400, 500); // width,height in pixels
 		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 	    window.setVisible(true);
 	    logTextArea.setText(""); //  clear log
 	    precisionTextField.setText("2");
 	    xTextField.setText("0.00");
 		
 		// Set event notification
 		inputTextField.addActionListener(this);
 		precisionButton.addActionListener(this);
 		recallButton.addActionListener(this);
 		clearButton.addActionListener(this);
 		accumulatorMode.addActionListener(this);
 		calculatorMode.addActionListener(this);
 		testMode.addActionListener(this);
 		xButton.addActionListener(this);
 		graphButton.addActionListener(this);
 		xRangeButton.addActionListener(this);
 		
 		}
 
 	String mode = "accumulator";
 	int precision = 2; 
 	double userX = 0.00;
 	Stack<String> expressionLIFO = new Stack<String>();
 	
 	public void actionPerformed(ActionEvent ae)
 		{
 		String input = null;
 		String result = null;
 		String logEntry = null;
 		// When input is entered...
 		if(ae.getSource() == inputTextField)
 			{
 			// error message is removed
 			errorLabel.setText("");
 			
 			// input is received
 			input = inputTextField.getText().trim();
 			input = input.replace(" ", "");
 			input = input.toLowerCase();
 			
 			if(input.length() == 0)
 				{
 				errorLabel.setText("Please enter a value");
 				return;
 				}
 			// check to make sure input is comprised of numbers and operators,
 			// does not begin or end with an operator,
 			// and does not contain multiple operators in a row
 			if(containsLetters(input))
 				{
 				errorLabel.setText("Input can only be comprised of numbers, operators, e, pi, or x");
 				return;
 				}
 			if((input.contains("p") && !(input.contains("pi"))) || (input.contains("i") && !(input.contains("pi"))))
 				{
 				errorLabel.setText("Input can only be comprised of numbers, operators, e, pi, or x");
 				return;
 				}
 			if(input.contains("pp") || input.contains("ii") || input.contains("ee") || input.contains("xx") || input.contains("pipi"))
 				{
 				errorLabel.setText("Substituted values must precede or follow an operator");
 				return;
 				}
 			if( input.startsWith("*") || input.startsWith("/") || input.endsWith("*") || input.endsWith("/") || input.endsWith("+") || input.endsWith("-") || input.startsWith("+"))
 				{
 				errorLabel.setText("Cannot begin or end expression with an operator");
 				return;
 				}
 			if(input.contains("++") || input.contains("---") || input.contains("**") || input.contains("//")||input.contains("+*")||input.contains("-*")||input.contains("+/")||input.contains("-/"))
 				{
 				errorLabel.setText("You have multiple operators in a row!");
 				return;
 				}
 			if(numberOf('(', input) != numberOf(')', input))
 				{
 				errorLabel.setText("Must have an equal number of ( and )");
 				return;
 				}
 			if( (input.indexOf(')') < input.indexOf('(')) && input.contains("("))
 				{
 				errorLabel.setText("Check your parentheses");
 				return;
 				}
 			if( (input.contains("+)")) || (input.contains("*)")) || (input.contains("/)")) || (input.contains("r)")) || (input.contains("^)")))
 				{
 				errorLabel.setText("Check your operators and parentheses!");
 				return;
 				}
 			if( (input.contains("(+")) || (input.contains("(*")) || (input.contains("(/")) || (input.contains("(r")) || (input.contains("(^")))
 				{
 				errorLabel.setText("Check your operators and parentheses!");
 				return;
 				}
 			if(input.contains("0(")||input.contains("1(")||input.contains("2(")||input.contains("3(")||input.contains("4(")||input.contains("5(")||input.contains("6(")||input.contains("7(")||input.contains("8(")||input.contains("9("))
 				{
 				errorLabel.setText("No implicit operations alloweD!");
 				return;
 				}
 			if(input.contains(")0")||input.contains(")1")||input.contains(")2")||input.contains(")3")||input.contains(")4")||input.contains(")5")||input.contains(")6")||input.contains(")7")||input.contains(")8")||input.contains(")9")||input.contains(")("))
 				{
 				errorLabel.setText("No implicit operations allowed!");
 				return;
 				}
 			
 			// -------------------------------------			
 			// -------- PROCESSING INPUT -----------
 			// -------------------------------------
 			String displayInput = "input";
 			// ACCUMULATOR MODE
 			if(mode.equals("accumulator"))
 				{
 				
 				// input cannot contain * or /, and can only use + or - to indicate sign
 				if(input.contains("*") || input.contains("/"))
 					{
 					errorLabel.setText("For * and /, please use calculator mode");
 					return;
 					}
 				if(input.startsWith("+") || input.startsWith("-"))
 					{
 					String temp = input.substring(1);
 					if(temp.contains("+") || temp.contains("-"))
 						{
 						errorLabel.setText("+/- can only set sign. Use calculator for expresions");
 						return;
 						}
 					}
 				if(input.contains("."))
 					{
 					String temp = input.substring(input.indexOf("."));
 					if(temp.length() != (precision+1))
 						{
 						errorLabel.setText("Must use " + precision + " digits of precision.");
 						return;
 						}
 					}
 				
 				
 				// process accumulator
 				Double previousTotal = runningTotal;
 				expressionLIFO.push(input);
 				
 				boolean usesX = false;
 				displayInput = input;
 				if(input.contains("e"))
 					input = input.replace("e", Double.toString(Math.E));
 				
 				if(input.contains("pi"))
 					input = input.replace("pi", Double.toString(Math.PI));
 				
 				if(input.contains("x")){
 					input = input.replace("x", Double.toString(userX));
 					usesX = true;
 				}
 				
 				result = accumulatorFunction(input);
 				outputTextField.setText(result);
 				logEntry = (new BigDecimal(previousTotal).setScale(precision, BigDecimal.ROUND_HALF_UP)).toString() + " + " + displayInput + " = " + result;
 				if(usesX){
 					logEntry = logEntry + " for x = " +  (new BigDecimal(userX).setScale(precision, BigDecimal.ROUND_HALF_UP)).toString();
 				}
 				logEntry = logEntry + newLine;
 				logTextArea.append(logEntry);
 				inputTextField.setText("");
 				} 
 			
 			// CALCULATOR MODE
 			if(mode.equals("calculator"))
 				{
 					
 				// input cannot contain =
 				if(input.contains("="))
 					{
 					errorLabel.setText("For = comparisons, please use test mode");
 					return;
 					}
 				
 				
 				//process calculator
 				expressionLIFO.push(input);
 				
 				boolean usesX = false;
 				displayInput = input;
 				if(input.contains("e"))
 					input = input.replace("e", Double.toString(Math.E));
 				
 				if(input.contains("pi"))
 					input = input.replace("pi", Double.toString(Math.PI));
 				
 				if(input.contains("x")){
 					input = input.replace("x", Double.toString(userX));
 					usesX = true;
 				}
 				
 				result = calculatorFunction(input);
 				logEntry = displayInput + " = " + result;
 				if(usesX){
 					logEntry = logEntry + " for x = " +  (new BigDecimal(userX).setScale(precision, BigDecimal.ROUND_HALF_UP)).toString();
 				}
 				logEntry = logEntry + newLine;
 				logTextArea.append(logEntry);
 				outputTextField.setText(result);
 				inputTextField.setText("");
 				}
 			
 			// TEST MODE
 			if(mode.equals("test"))
 				{
 				
 				
 				if(input.indexOf("=") != input.lastIndexOf("="))
 					{
 					errorLabel.setText("You can only have one = sign in the expression!");
 					return;
 					}
 				
 				
 				expressionLIFO.push(input);
 				
 				boolean usesX = false;
 				displayInput = input;
 				if(input.contains("e"))
 					input = input.replace("e", Double.toString(Math.E));
 				
 				if(input.contains("pi"))
 					input = input.replace("pi", Double.toString(Math.PI));
 				
 				if(input.contains("x")){
 					input = input.replace("x", Double.toString(userX));
 					usesX = true;
 				}
 				
 				result = testFunction(input);
 				logEntry = result;
 				if(usesX){
 					logEntry = logEntry + " for x = " + (new BigDecimal(userX).setScale(precision, BigDecimal.ROUND_HALF_UP)).toString();
 				}
 				logEntry = logEntry + newLine;
 				logTextArea.append(logEntry);
 				outputTextField.setText(logEntry);
 				//inputTextField.setText("");
 				}
 				
 			} 
 			// end of inputTextField response
 		
 		// Clear button
 		if(ae.getSource() == clearButton)
 			{
 			// clear fields
 			inputTextField.setText("");
 			outputTextField.setText("");
 			runningTotal = 0.00;
 			inputTextField.requestFocus();
 			}
 		
 		if(ae.getSource() == xButton)
 			{
 			try{
 				userX = Double.parseDouble(xTextField.getText());
 				errorLabel.setText("");
 				inputTextField.requestFocus();
 			} catch(NumberFormatException nfe)
 				{
 				errorLabel.setText("Your X value is not valid.");
 				}
 			}
 		
 		// If a mode radio button is pressed, clear the fields and set the mode
 		if(ae.getSource() == accumulatorMode)
 			{
 			inputTextField.setText("");
 			outputTextField.setText("");
 			mode = "accumulator";
 			inputTextField.requestFocus();
 			}
 		if(ae.getSource() == calculatorMode)
 			{
 			inputTextField.setText("");
 			outputTextField.setText("");
 			mode = "calculator";
 			inputTextField.requestFocus();
 			}
 		if(ae.getSource() == testMode)
 			{
 			inputTextField.setText("");
 			outputTextField.setText("");
 			mode = "test";
 			inputTextField.requestFocus();
 			}
 		
 		if(ae.getSource() == recallButton)
 			{
 			if(!expressionLIFO.empty())
 				{
 				inputTextField.setText(expressionLIFO.pop());
 				}
 			inputTextField.requestFocus();
 			}
 		
 		if(ae.getSource() == precisionButton)
 			{
 			try
 				{
 				int temp = Integer.parseInt(precisionTextField.getText());
 				if(temp <= 0)
 					{
 					errorLabel.setText("Precision must be a positive integer");
 					precisionTextField.setText(Integer.toString(precision));
 					}
 				else
 					{
 					precision = temp;
 					errorLabel.setText("");
 					inputTextField.requestFocus();
 					}
 				}
 			catch(NumberFormatException nfe)
 				{
 				errorLabel.setText("Precision must be a positive integer");
 				}
 			}
 		
 		if(ae.getSource() == xRangeButton)
 			{
 			double lowX = -10;
 			double highX = 10;
 			double incrementX = 1;
			errorLabel.setText("");
 			try
 				{
 				lowX = Double.parseDouble(lowXTextField.getText());
 				highX = Double.parseDouble(highXTextField.getText());
 				incrementX = Double.parseDouble(incrementXTextField.getText());
 				}
 			catch(Exception e)
 				{
 				errorLabel.setText("Please make sure that you have input valid numerical values");
 				xRangeIsOkay = false;
 				return;
 				}
 			if((highX<lowX)||((5*incrementX) > (highX-lowX))||((20*incrementX)<(highX-lowX))||(incrementX<=0))
 				{
 				errorLabel.setText("Please fix your graph scale and increment to display 5-20 points");
 				xRangeIsOkay = false;
 				return;
 				}
 			xRangeIsOkay = true;
 			}
 		
 		if(ae.getSource() == graphButton)
 			{
 			errorLabel.setText("");
 			if(xRangeIsOkay)
 				{
 				// prepareGraphData
 				// calculate X range
 				// create Grapher object
 					// pass X values, Y values, X range, X increment, and address of Calculator object (this)
 				}
 			else
 				errorLabel.setText("Please set your range first!");
 			}
 		
 		}
 		
 	//ACCUMULATOR IMPLEMENTATION
 	Double runningTotal = 0.00;
 	public String accumulatorFunction(String entry)
 		{
 		Double temp = Double.parseDouble(entry);
 		runningTotal += temp;
 		BigDecimal bd = new BigDecimal(runningTotal).setScale(precision, BigDecimal.ROUND_HALF_UP);
 		String result = bd.toString();
 		return result;
 		}
 	
 	public String calculatorFunction(String entry)
 	  {
 		String result = "";
 		String subEntry = "";
 		Double temp = 0.00;
 	  int openBrace=0,closeBrace=0;
 	  int i=0,j=0;
 	  boolean closed = false;
 	  
 	  while(entry.contains("("))
 	    {
 		//parentheses stuff
 		for(i=0;i<entry.length();i++)
 		  {
 		  if((entry.charAt(i)=='('))
 		    {
 			openBrace = i;
 			
 			for(j=i+1;j<entry.length();j++)
 			  {
 			  if(entry.charAt(j)=='(')
 			    {
 				openBrace = j;
 				System.out.println("openBrace: " + openBrace);
 				i=j;
 				continue;
 			    }
 			  if(entry.charAt(j)==')')
 			    {
 				closeBrace = j;
 				System.out.println("closeBrace: " + closeBrace);
 				closed = true;
 				break;
 			    }
 			  }//for j
 
 		    } // if
 			if(closed)
 			  {
 			  closed = false;
 			  break;
 			  }  
 		  }//for i
 		//System.out.println("openBrace: " + openBrace);
 		//System.out.println("closeBrace: " + closeBrace);
 		subEntry = entry.substring(openBrace+1,closeBrace);
 		subEntry = calculateParentheses(subEntry);
 		entry = entry.substring(0,openBrace) + subEntry + entry.substring(closeBrace+1,entry.length());
 		//System.out.println("entry: " + entry);
 		
 	    }//while parentheses
 	  result = calculateParentheses(entry);
 	  //rounding
 	  temp = Double.parseDouble(result);
 	  BigDecimal bd = new BigDecimal(temp).setScale(precision, BigDecimal.ROUND_HALF_UP);
 	  result = bd.toString();
 	  return result;
 	  }
 	
 	//CALCULATOR IMPLEMENTATION
 	public String calculateParentheses(String entry)
 	  {
 	  String result = "";
 
 	  CharSequence negOps[] = {"+-","--"};
 	  CharSequence negOps2[] = {"-","+"};
 
 	  for(int x = 0; x < 2;x++){
 		  while(entry.contains(negOps[x])){
 		  entry = entry.replace(negOps[x],negOps2[x]);
 		  }
 		  }
 
 
         // count our operator occurrences
 		  int opCount = 0;
 
 		  // array of acceptable operators
 		  // note 2 arrays for left/right precedence in order of operations
 		  char ops[]  = {'^','*','+'};
 		  char ops2[] = {'r','/','-'};
 
 		  // operand
 		  char desiredOp = '0';
 
 		  // variables for operators
 		  String op1 = null;
 		  String op2 = null;
 
 		  // boolean for first operator
 		 // boolean firstOp = true;
 
 		// initialize operator indexes
 		  int j = 0,i = 0, k = 0,left = 0,middle = 0,right = 0;	  
 
 
 		  // count number of operators
 		  for(i=1;i<entry.length();i++)
 		    {
 			for(j=0;j<ops.length;j++)
 			  {
 			  if((entry.charAt(i) == ops[j])||(entry.charAt(i) == ops2[j]))
 			    {
 				opCount += 1;  
 				break;
 			    }
 			  }
 		    }
 		  // System.out.println("Total number of operators in entry = " + opCount);
 
 	  if(opCount <= 0) // no operators found
 	    {
 		result = entry;
 		return result;
 	    }
 	  while(opCount > 0) // operators in expression
 	    {
 
 	    for(j=0;j<ops.length;j++)
 	      { // loop through operators to accomplish "order of operations"
 
 
 	    	for(int x = 0; x < 2;x++){
 	    		while(entry.contains(negOps[x])){
 	    		entry = entry.replace(negOps[x],negOps2[x]);
 	    		}
 	    		}
 
 
 	    	if(opCount==0) break;
 	  	// System.out.println("entry is: " + entry);
 	  	// System.out.println("op count is currently at: " + opCount);
 	    	char desiredOp1 = ops[j];
 	    	char desiredOp2 = ops2[j];
 	  	// System.out.println("Desired operator on this iteration is: " + desiredOp1 + " or " + desiredOp2);
 
 	  	// System.out.println("j = " + j);
 	  	// System.out.println("opCount = " + opCount);
 
 
 
 
 
 
 
 
 
 		    if(!(entry.contains(Character.toString(desiredOp1))||(entry.contains(Character.toString(desiredOp2)))))
 		    {    
 		    continue;
 		    }
 		    	// System.out.println("Entry does indeed contain the desired operator");
 	  	for(i=1;i<entry.length();i++)
 	  	  {
 
 
 
 	  	  char A = entry.charAt(i);
 
 	  	  if((A == '^')||(A == 'r')||(A == '*')||(A == '/')||(A == '+')||(A == '-'))
 	  	    { // an operator has been found, don't know which operator
 
 	  		if((A != desiredOp1)&&(A != desiredOp2))
 	  	      {
 	  	      left = middle;
 	  	      middle = i;
 	  	      // System.out.println("Cycling through non op, left = " + left + ", mid = " + middle);
 	  	      if(opCount == 1)
 	  	        {
 	  	    	right = entry.length();
 	  	    	break;
 	  	        }
 
 	  	      continue;
 	  	      }
 
 	  		else if ((A == desiredOp1)||(A == desiredOp2))
 	  		  {
 	  		  desiredOp = A;
 	  		  // System.out.println("just found out our desired op is: " + desiredOp);
 	  		  left = middle;
 	  		  middle = i;
 
 	  		  // System.out.println("found desired op, left = " + left + ", mid = " + middle);
 	  		// // System.out.println("haven't set firstOp yet, = " + firstOp);
 	  		 //if(left == 0) firstOp = true;
 	  		 // // System.out.println("first op affected? = " + firstOp);
 	  		  for(k = i+1;k<entry.length();k++)
 	  		    {
 	  			  char B = entry.charAt(k);
 	  			  if((B == '^')||(B == 'r')||(B == '*')||(B == '/')||(B == '+')||(B == '-'))
 	  			    {
 	  				if((k-middle)==1){
 	  					opCount--;
 	  					continue;
 	  				}
 	  				right = k; // third operator
 
 	  				// System.out.println("expression is: " + entry.substring(left+1,right));
 	  				break;
 	  			    }
 	  		    }
 
 	  		  // reached end of entry, desiredOp must be last operand in entry
 	  		  right = k;
 	  		  break;
 	  		  }
 
 
 
 	  	    }
 	  	  }// for loop through entry
 
 
 
 	  	// call calculate function and update entry string with answer
 
 	  	if(left==0)
 	  	  { 
 	  		//// System.out.println("getting ready to find op1, desiredOp first op in entry");
 
 	  	    // System.out.println("op1" + "     " + entry.substring(0,middle));
 	        op1 = entry.substring(0,middle);
 	        //firstOp = false;
 	  	  }
 	  	else 
 	  		{
 	  		// System.out.println(entry.charAt(left+1));
 	  		// System.out.println(entry.charAt(middle));
 	  		// System.out.println("getting ready to find op1, desiredOp not first in entry");
 	  		// System.out.println("op1" + "       "  +entry.substring(left+1,middle));
 	  		op1 = entry.substring(left+1,middle);
 	  		}
 	  	// System.out.println("getting ready to calculate op2");
 	  	// System.out.println("right = " + right);
 	  	// System.out.println("op2" + "      " + entry.substring(middle+1,right));
 	  	op2 = entry.substring(middle+1,right);
 	  	// System.out.println("opCount before decrement = " + opCount);
 
 	  	opCount--;
       // System.out.println("opCount after decrement now  = " + opCount);
 	  	String answer = calculate(op1,op2,desiredOp);
 
 	  	if(left==0)
 	  	{
 	  	entry = entry.substring(0,left) + answer + entry.substring(right,entry.length());	
 	  	}
 	  	else{
 	  	entry = entry.substring(0,left+1) + answer + entry.substring(right,entry.length());
 	  	}
 
 
 
 	  	left = 0;
 	  	middle = 0;
 	  	right = 0;
 	  	i = 0;
 	  	j = -1;
 	  	k = 0;
 	      }// for loop through ops
     }
   result = entry;
   // System.out.println("Final result is: " + result);
 	return result;
 	}
 
 	public String testFunction(String entry)
 	{
 	String result = "lolcatz";
 	int i = entry.indexOf("=");
 	String left = entry.substring(0,i);
 	String right = entry.substring(i+1,entry.length());
 	// System.out.println("*******LEFT = " + left);
 	// System.out.println("*******RIGHT = " + right);
 	left = calculatorFunction(entry.substring(0,i));
 	right = calculatorFunction(entry.substring(i+1,entry.length()));
 	if(left.equals(right)){
 	result = "CORRECT!";
 	// System.out.println("got here");
 	}
 	else{
 	result = "OOPS!";
 	}
 	return result;
 	}
 
 	// istance variable for handling negative numbers
 	boolean negative = false;
 	public String calculate(String op1, String op2, char operator)
 	  {
 		String answer = null;
 		  Double result = 0.0;
 		  // System.out.println("Getting ready to parse ops to double in calculate()");
 		  // System.out.println("Our current desired operator is: " + operator);
 		  Double leftNumber = Double.parseDouble(op1);
 		  Double rightNumber = Double.parseDouble(op2);
 		  // System.out.println("leftNumber in calculate() is: " + leftNumber);
 		  // System.out.println("rightNumber in calculate() is: " + rightNumber);
 		  switch(operator)
 		    {
 		    case '*' : result = leftNumber * rightNumber; break;
 		    case '/' : result = leftNumber / rightNumber; break;
 		    case '+' : result = leftNumber + rightNumber; break;
 		    case '-' : result = leftNumber - rightNumber; break;
 		    case '^' : result = Math.pow(leftNumber, rightNumber); break;
 		    case 'r' : result = Math.pow(leftNumber, (1/rightNumber)); break;
 		    }
 		/*  if(result < 0)
 		    {
 			negative = true;
 			result = result*-1;
 			answer = Double.toString(result);
 			
 		    }*/
 		  answer = Double.toString(result);
 		  // System.out.println("Result of calculate = " + answer);
 		  return answer;
 	  }
 	
 	public boolean containsLetters(String input)
 		{
 	    char[] chars = input.toCharArray();
 
 	    for (char c : chars)
 	    	{
 	        if(!(c == '0'))
 	        {
 	        	if(!(c == '1'))
 		        {
 	        		if(!(c == '2'))
 	    	        {
 	        			if(!(c == '3'))
 	        	        {
 	        				if(!(c == '4'))
 	        		        {
 	        					if(!(c == '5'))
 	        			        {
 	        						if(!(c == '6'))
 	        				        {
 	        							if(!(c == '7'))
 	        					        {
 	        								if(!(c == '8'))
 	        						        {
 	        									if(!(c == '9'))
 	        							        {
 	        										if(!(c == '.'))
 	        								        {
 	        											if(!(c == '('))
 		        								        {
 	        												if(!(c == ')'))
 	    	        								        {
 		        								        
 	        											if(!(c == '+'))
 	        									        {
 	        												if(!(c == '-'))
 	        										        {
 	        													if(!(c == '*'))
 	        											        {
 	        														if(!(c == '/'))
 	        												        {
 	        															if(!(c == 'e'))
 	        															{
 	        																if(!(c == 'p'))
 	        																{
 	        																	if(!(c == 'i'))
 	        																	{
 	        																		if(!(c == 'x'))
 	        																		{
 					        															if(!(c == '='))
 						        												        {
 					        																if(!(c == '^'))
 							        												        {
 					        																	if(!(c == 'r'))
 								        												        {
 							        												        
 						        												        	return true;
 								        												        }
 							        												           }
 						        												             }
 							        												       }
 						        												        }
 	        																		}
 	        																	}
 	        																}
 	        												        	}
 	        												        }
 	        											        }
 	        										        }
 	        									        }
 	        								        }
 	        							        }
 	        						        }
 	        					        }
 	        				        }
 	        			        }
 	        		        }
 	        	        }
 	    	        }
 		        }
 	        }
 	    	}
 	    return false;
 		}
 	
 	int numberOf(char test, String entry)
 		{
 		int i;
 		int count = 0;
 		for(i=0;i<entry.length();i++)
 			{
 			if(entry.charAt(i) == test)
 				{
 				count++;
 				}
 			}
 		return count;
 		}
 	
 	
 }

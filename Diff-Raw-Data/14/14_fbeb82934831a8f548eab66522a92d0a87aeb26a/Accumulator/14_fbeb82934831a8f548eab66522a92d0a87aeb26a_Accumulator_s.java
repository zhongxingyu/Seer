 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
 
 import java.math.BigDecimal;
 import java.util.Stack;
 
 public class Accumulator implements ActionListener
 {
 	// Main - loads program object
 	public static void main(String[] args){
 		new Accumulator();
 		}

 	// newLine
 	private String newLine = System.getProperty("line.separator");
	
 	//GUI Objects
 	private JFrame window = new JFrame("Calculator");
 	private JTextField inputTextField = new JTextField(20);
 	private JTextField outputTextField = new JTextField(20);
 	private JTextField precisionTextField = new JTextField(20); 
 	private JTextArea logTextArea = new JTextArea();
 	private JScrollPane logScrollPane = new JScrollPane(logTextArea);
 	private JLabel inputLabel = new JLabel("Enter input");
 	private JLabel outputLabel = new JLabel(" Result -> ");
 	private JLabel errorLabel = new JLabel("");
 	private JButton precisionButton = new JButton("Update Precision:");
 	private JButton clearButton = new JButton("Clear");
 	private JButton recallButton = new JButton("Prev");
 	private JPanel topPanel = new JPanel(new GridBagLayout());
 	GridBagConstraints c = new GridBagConstraints();
 	private JRadioButton accumulatorMode = new JRadioButton("Accumulator", true);
 	private JRadioButton calculatorMode = new JRadioButton("Calculator", false);
 	private JRadioButton testMode = new JRadioButton("Test Mode", false);
 	private ButtonGroup bGroup = new ButtonGroup();
 	private JPanel bottomPanel = new JPanel();
 	
	
 	public Accumulator()
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
 		c.gridwidth = 6;
 		c.gridx = 0;
 		c.gridy = 1;
 		topPanel.add(errorLabel,c);
 		
 		bottomPanel.setLayout(new GridLayout(1,3));
 		bottomPanel.add(accumulatorMode);
 		bottomPanel.add(calculatorMode);
 		bottomPanel.add(testMode);
 		bottomPanel.add(precisionButton);
 		bottomPanel.add(precisionTextField);
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
 		clearButton.setBackground((Color.black));
 		clearButton.setForeground(Color.yellow);
 		errorLabel.setForeground(Color.red);
 		inputTextField.setEditable(true);
 		outputTextField.setEditable(false);
 		window.setLocation(10, 10); // horizontal, vertical
 		window.setSize(800, 500); // width,height in pixels
 		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 	    window.setVisible(true);
 	    logTextArea.setText(""); //  clear log
 		
 		// Set event notification
 		inputTextField.addActionListener(this);
 		precisionButton.addActionListener(this);
 		recallButton.addActionListener(this);
 		clearButton.addActionListener(this);
 		accumulatorMode.addActionListener(this);
 		calculatorMode.addActionListener(this);
 		testMode.addActionListener(this);
 		
 		
 		}
 
 	String mode = "accumulator";
 	int precision = 2; 
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
 				errorLabel.setText("Input can only be comprised of numbers and operators");
 				return;
 				}
 			if( input.startsWith("*") || input.startsWith("/") || input.endsWith("*") || input.endsWith("/") || input.endsWith("+") || input.endsWith("-"))
 				{
 				errorLabel.setText("Cannot begin or end expression with an operator");
 				return;
 				}
 			if(input.contains("++") || input.contains("--") || input.contains("**") || input.contains("//"))
 				{
 				errorLabel.setText("You have multiple operators in a row!");
 				return;
 				}
 			
 			// -------------------------------------			
 			// -------- PROCESSING INPUT -----------
 			// -------------------------------------
 			
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
 				result = accumulatorFunction(input);
 				outputTextField.setText(result);
 				logEntry = Double.toString(previousTotal) + " + " + input + " = " + result + newLine;
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
 				result = calculatorFunction(input);
 				logEntry = input + " = " + result + newLine;
 				logTextArea.append(logEntry);
 				outputTextField.setText(result);
 				}
 			
 			// TEST MODE
 			if(mode.equals("test"))
 				{
 				expressionLIFO.push(input);
 				result = testFunction(input);
 				logEntry = result + newLine;
 				logTextArea.append(logEntry);
 				outputTextField.setText(logEntry);
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
 			}
 		
 		// If a mode radio button is pressed, clear the fields and set the mode
 		if(ae.getSource() == accumulatorMode)
 			{
 			inputTextField.setText("");
 			outputTextField.setText("");
 			mode = "accumulator";
 			}
 		if(ae.getSource() == calculatorMode)
 			{
 			inputTextField.setText("");
 			outputTextField.setText("");
 			mode = "calculator";
 			}
 		if(ae.getSource() == testMode)
 			{
 			inputTextField.setText("");
 			outputTextField.setText("");
 			mode = "test";
 			}
 		
 		if(ae.getSource() == recallButton)
 			{
 			if(!expressionLIFO.empty())
 				{
 				inputTextField.setText(expressionLIFO.pop());
 				}
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
 					}
 				}
 			catch(NumberFormatException nfe)
 				{
 				errorLabel.setText("Precision must be a positive integer");
 				}
 			}
 		
 		}
 		
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
 		String result = "lolcatz";
 		
 		return result;
 		}
 	
 	public String testFunction(String entry)
 		{
 		String result = "lolcatz";
 		
 		return result;
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
 	        											if(!(c == '+'))
 	        									        {
 	        												if(!(c == '-'))
 	        										        {
 	        													if(!(c == '*'))
 	        											        {
 	        														if(!(c == '/'))
 	        												        {
 	        															if(!(c == '='))
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
 	    return false;
 		}
 	
 }

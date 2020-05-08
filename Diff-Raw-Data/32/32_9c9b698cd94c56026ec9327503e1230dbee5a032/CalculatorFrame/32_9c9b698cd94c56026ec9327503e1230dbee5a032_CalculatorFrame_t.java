 /*
  * Filename: Calculator.java
  * Author: 2S03MakeYouLearnGood
  * Description: This file will teach you about how GUIs are created in Java.
  *              We adapted the code for homework, all the core code can refer 
  *              to the core.grantlj.*;
  */
 
 /*
  * Java provides us with 3 default layouts that
  * can be used to well, layout our interfaces.
  * They come from the java.awt package that
  * contains all of the classes for creating
  * user interfaces and for painting graphics and images.
  */
 
 package ui;
 
 import java.awt.GridLayout;
 
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import core.grant.*;
 
 
 import javax.swing.ButtonGroup;
 /*
  * We're going to be using the Swing package.
  * It provides a set of "lightweight" (all-Java language) components that
  * (as best as possible) work the same on all platforms.
  *
  * The general hierarchy is: text fields, labels, buttons etc. are ADDED to panels
  * and panels are ADDED to frames.
  *
  * We are going to use panels, frames, text fields and button so we import them.
  */
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.JTextField;
 import javax.swing.JButton;
 
 
 /* We're making this class CalculatorFrame extend
  * JFrame, which means it in and of itself IS A JFrame.
  */
 class CalculatorFrame extends JFrame {
 
 	private static final int NUMBER_PAD_WIDTH = 4;
 	private static final int NUMBER_PAD_HEIGHT = 5;
 	
 	private static final int CALCULATOR_WIDTH = 1;
 	private static final int CALCULATOR_HEIGHT = 3;
 	
 	private int workMode=1;                   //1 is integer mode;2 is double mode.
 	private boolean showing=false;
 	private boolean standby=true;
 	
 	/* In the end this is what we're looking for
 	 *    -------------------
 	 *    |               0 |
 	 *    -------------------
 	 *    | ( | ) | B | AC  |
 	 *    -------------------
 	 *    | 7 | 8 | 9 |  /  |
 	 *    -------------------
 	 *    | 4 | 5 | 6 |  *  |
 	 *    -------------------
 	 *    | 1 | 2 | 3 |  -  |
 	 *    -------------------
 	 *    | 0 |   | = |  +  |
 	 *    -------------------
 	 */
 
 	private static final long serialVersionUID = 1L;
 
 	/* We could use many different categories, but we going to use
 	 * 3 JPanels, a resultPanel for showing our results, a numberPanel
 	 * for holding our buttons and operations and a mainPanel
 	 * which will serve as a container for both.
 	 */
 	private JPanel mainPanel, resultPanel, numberPanel,optPanel;
 	
 	private static Set<String> operators = new HashSet<String>();
 	private static Set<String> digits = new HashSet<String>();
 	Map<String, JButton> buttons = new HashMap<String, JButton>();
 	String[][] buttonOrder = new String[][]{
 
 			{ "(", ")", "B", "AC" },
 			{ "7", "8", "9", "/" },
 			{ "4", "5", "6", "*" },
 			{ "1", "2", "3", "-" },
 			{ "0", " ", "=", "+" }
 	};
 	
 	
 	// Static blocks work like a constructor, but are outside out constructor
 	// and typically only used for variable initialization. Lets create a set
 	// for our symbols and operators!
 	static {
 		
 		for(String x : new String[]{"(", ")", "+", "-", "*", "/", "B", "=", " ", "AC"})
 			operators.add(x);
 		
 		for(int i = 0; i < 10; i++)
 			digits.add(Integer.toString(i));
 	}
 	
 
 	
 	/* We could have made use of other things such as JLabel, JRadioButton, and JCheckBox etc */
 
 	/* and finally we need a JTextField to hold our results */
 	private JTextField resultField;
 
 	public CalculatorFrame() {
 
 		/* Set the title of the window. */
 		super("My Calculator");
 
 		/* result panel */
 		resultPanel = new JPanel();
 
 		/* FlowLayout positions components row wise from left to right */
 		resultPanel.setLayout(new FlowLayout());
 		
 		/* We want it to initially say 0 and have a width of about 20 columns */
 		resultField = new JTextField("0", 20);
 		
 		/* Add the text field to its panel */
 		resultPanel.add(resultField);
 		
 		/* Set the alignment */
 		resultField.setHorizontalAlignment(JTextField.RIGHT);
 		
 		/* We don't want it to be editable we just want to display results */
 		resultField.setEditable(false);
 
 		/* number panel*/
 		numberPanel = new JPanel();
 		
 		numberPanel.setLayout(new GridLayout(NUMBER_PAD_HEIGHT, NUMBER_PAD_WIDTH));
 		
 
 		
 		// Lets build the buttons for digits.
 		for(String x : digits)
 			buttons.put(x, new JButton(x));
 		
 		// Lets build the buttons for operators.
 		for(String x : operators)
 			buttons.put(x, new JButton(x));
 		
 		// The numbers will appear organized as we have them here. This does not have
 		// to be a 2-dimensional array, but it helps visualize things better.
 		
 		
 		// Lets add our rows to the number panel!
 		for(int i = 0; i < NUMBER_PAD_HEIGHT; i++)
 			for(int j = 0; j < NUMBER_PAD_WIDTH; j++)
 				numberPanel.add(buttons.get(buttonOrder[i][j]));
 		
 		// Create and add a digit listener to each digit button. Check the implementation
 		// of buildDigitListener() below!
 		
 		ActionListener digitListener = buildDigitListener();
 		
 		for(String x : digits)
 			buttons.get(x).addActionListener(digitListener);
 		
 		// Create and add an operator listener to each operator button. Check the implementation
 		// of buildOperatorListener() below!
 		
 		ActionListener operatorListener = buildOperatorListener();
 		
 		for(String x : operators)
 			buttons.get(x).addActionListener(operatorListener);
 		
 		//The options panel.
 		optPanel=new JPanel();
 		ButtonGroup bg=new ButtonGroup();
 		JRadioButton jb1=new JRadioButton("Integer Mode");
 		JRadioButton jb2=new JRadioButton("Float   Mode");
 		
 		//The default mode is integer mode.
 		bg.add(jb1);bg.add(jb2);
 		jb1.setSelected(true);
 		workMode=1;
 		buttons.get(buttonOrder[4][1]).setEnabled(false);
 		ActionListener OptListener=buildOptListener();
 	
 		jb1.addActionListener(OptListener);
 		jb2.addActionListener(OptListener);
 		
 		optPanel.add(jb1);optPanel.add(jb2);
 		
 		
 		/* we then create our mainPanel which we're going to add everything else to */
 		mainPanel = new JPanel();
 
 		/* we make it have 2 rows and 1 column so we can stack our panels */
 		mainPanel.setLayout(new GridLayout(CALCULATOR_HEIGHT, CALCULATOR_WIDTH));
 
 		/* and we add both in t;
 		 * he order we want them to be displayed */
 		mainPanel.add(resultPanel);
 	    mainPanel.add(optPanel);
 		mainPanel.add(numberPanel);
 		/* We add our mainPanel to the JFrame */
 		add(mainPanel);
 
 		/* Specify that the window should close when the exit button provided by the OS is clicked. */
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 		/* We then call pack() which causes the window to be sized
 		 * to fit the preferred size and layouts of its subcomponents
 		 */
 		pack();
 
 		/* and finally set it's visibility to true */
 		setVisible(true);
 	}
 	
 	/**
 	 * Creates an action listener for digits. This should only be called once!
 	 * 
 	 * @return An action listener for digits.
 	 */
 	
 	private ActionListener buildOptListener()
 	{
 		return new ActionListener()
 		{
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				JRadioButton j=(JRadioButton) e.getSource();
 				char c=j.getText().charAt(0);
 			    if (c=='I')
 			    {
 			    	//Set workMode to Integer Mode.
 			    	workMode=1;
 			    	buttons.get(buttonOrder[4][1]).setEnabled(false);
 			    	resultField.setText("0");
 			    	showing=false;
 					standby=true;
 			    	
 			    }
 			    
 			    if (c=='F')
 			    {
 			    	//Set workMode to Float Mode.
 			    	workMode=2;
 			    	buttons.get(buttonOrder[4][1]).setEnabled(true);
 			    	resultField.setText("0");
 			    	showing=false;
 					standby=true;
 			    }
 				
 			}
 			
 		};
 	}
 	private ActionListener buildDigitListener(){
 		
 		return new ActionListener(){
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				
 				/*
 				 * We know only JButtons will trigger this event, so it is safe to type case
 				 * the 'source' of the event to a JButton type.
 				 */
 				JButton j = (JButton) e.getSource();
 				
 				// If the result field should be cleared, do so while adding this digit. Lets also
 				// prevent leading zeroes.
 				//if(clearResultField || resultField.getText().equals("0")){
 					if (standby==true || resultField.getText().equals("0")){
 					// We probably should not be relying on the text in the digit as our 'identifier'
 					// for that button, but it is sufficient for our purposes for now.
 					resultField.setText(j.getText());
 					standby=false;
 				}
 				else {
                     if (showing==true)
                     {
                     	resultField.setText(j.getText());
 						showing=false;
 						standby=false;
                     }
                     else 
 					  resultField.setText(resultField.getText() + j.getText());
 				}
 			}
 		};
 	}
 	
 	/**
 	 * Creates an action listener for operators. This should only be called once!
 	 * 
 	 * @return An action listener for operators.
 	 */
 	private ActionListener buildOperatorListener(){
 		
 		return new ActionListener(){
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 			
 				
 				JButton j = (JButton) e.getSource();
 				
 				String operator = j.getText();
 				switch(operator.charAt(0)){
 				  case ' ':{
 					         resultField.setText(resultField.getText()+".");
 				             break;
 				           }
 				  
 				  case 'A': // The clear operation.
 					{
 						resultField.setText("0");
 						showing=false;
 						standby=true;
 				        break; 
 					}
 					
 					case '=':
 					{
 					        //"=" pressed, check the showing statement and standby statement. 
 						    if (showing==false && standby==false)
 					         {
 						     // System.out.println(resultField.getText());
 						      
 						      //We show out the result ONLY we have pressed expressions.
 						      String ans=resultField.getText()+"="+new SuffixCalc(new MidToSuffix(resultField.getText()).getSuffix(),workMode).getAns(); 
 					          resultField.setText(ans);
 					          showing=true;
 					         }
 					          break;
 					}
 					case 'B':
 					{
 						//Backspace pressed down, so we set the showing statement to false.
 						showing=false;
 						
 						
 						if (resultField.getText().length()==1)
 						{
 							//length is 1, it means it is the last char,
 							//so set the resultFiled to 0 and make it standby.
 							resultField.setText("0");
 							standby=true;
 						}
 						
 						else
 						{
 							//to delete a CALCULATED EXPRESSION, we need to delete everything after
 							//the "=";
 							
 							if (resultField.getText().indexOf('=')>=0) 
 								resultField.setText(resultField.getText().substring(0,resultField.getText().indexOf('=')));
 							else
 							    //no "=" found, just delete the last character.
 								resultField.setText(resultField.getText().substring(0,resultField.getText().length()-1));
 						break;
 						}
 					}
 					
 				    default:
 				    {
 				        if ((operator.charAt(0)=='+' || operator.charAt(0)=='-' || operator.charAt(0)=='*' || operator.charAt(0)=='/') && showing==true)
 				        {
 				        	showing=false;
 				        	String tmp;
 				        	int t=resultField.getText().indexOf('=');
 				        	tmp=resultField.getText().substring(t+1);
 				            resultField.setText(tmp+operator);	
 				        	
 				        }
 				        else
 				        {
 				        
 				    	//To handle the negative number conditions. 
				    	if ( ( (operator.charAt(0)=='-' || operator.charAt(0)=='(') && standby==true) || (operator.charAt(0)=='(' && showing==true))			         
				    	{
 				        	 standby=false;
 				        	 showing=false;
 				        	 resultField.setText(operator);
 				         }
 				         else
 				         {
 				        	 standby=false;
 				        	 showing=false;
 				        	 resultField.setText(resultField.getText()+operator);
 				         }
 				        }
 				       break;
 				    }
 							
 				}
 			}
 		};
 	}
 	
 }

 package project1.calculator;
 
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import javax.swing.BoxLayout;
 import javax.swing.JPanel;
 
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.InputEvent;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 
 import javax.swing.*;
 
 public class CalculatorButtonsV01 implements ActionListener, KeyListener {
 	
 	//Public for testing
 	public JButton[] buttons = new JButton[27];
 	public JTextField txtField = new JTextField(36);
 	public CalculatorFunctions calcFunctions;
 	
 	public CalculatorButtonsV01(CalculatorFunctions f)
 	{
 		calcFunctions = f;
 		for(int i = 0 ; i < 10; i++)
 		{
 			buttons[i] = new JButton(Integer.toString(i));
 		}
 		
 		//Starts from for loop ends (10);
 		String[] buttonText = {"/", "*","1/x","%", "sqrt", "-","+","=",".","+/-",
 				"MC","MR","MS","M+", "Backspace","CE", "C"};
 		
 		for(int u = 10; u < 27 ; u++)
 		{
 			buttons[u] = new JButton(buttonText[u-10]);
 		}
 		
 		txtField.setMaximumSize(txtField.getPreferredSize());
 		txtField.setEditable(false);
 		txtField.setHorizontalAlignment(JTextField.RIGHT);
 		txtField.setText("0");
 		txtField.requestFocus();
 	}
 	
 	/**
 	 * 
 	 * @param frame
 	 */
 	public void addGUIToFrame(JFrame frame)
 	{
 		Dimension buttonSize = new Dimension(60,60);
 		buttons[24].setPreferredSize(new Dimension(103,50));
 		buttons[25].setPreferredSize(new Dimension(103,50));
 		buttons[26].setPreferredSize(new Dimension(103,50));
 		
 		for(int i = 0 ; i < 24; i++)
 		{
 			buttons[i].setPreferredSize(buttonSize);
 		}
 		
 		JPanel numberGrid1 = new JPanel(new FlowLayout());
 		JPanel numberGrid2 = new JPanel(new FlowLayout());
 		JPanel numberGrid3 = new JPanel(new FlowLayout());
 		JPanel numberGrid4 = new JPanel(new FlowLayout());
 		JPanel clearButtonsRow = new JPanel(new FlowLayout());
 		
 		clearButtonsRow.add(Box.createRigidArea(new Dimension(70,0)));//Empty area to make the backspace button move to the right. 
 		clearButtonsRow.add(buttons[24]);
 		clearButtonsRow.add(buttons[25]);
 		clearButtonsRow.add(buttons[26]);
 		
 		numberGrid1.add(buttons[20]);
 		numberGrid1.add(Box.createRigidArea(new Dimension(5,0)));
 		numberGrid1.add(buttons[7]);
 		numberGrid1.add(buttons[8]);
 		numberGrid1.add(buttons[9]);
 		numberGrid1.add(buttons[10]);
 		numberGrid1.add(buttons[14]);
 		
 		numberGrid2.add(buttons[21]);
 		numberGrid2.add(Box.createRigidArea(new Dimension(5,0)));
 		numberGrid2.add(buttons[4]);
 		numberGrid2.add(buttons[5]);
 		numberGrid2.add(buttons[6]);
 		numberGrid2.add(buttons[11]);
 		numberGrid2.add(buttons[13]);
 		
 		numberGrid3.add(buttons[22]);
 		numberGrid3.add(Box.createRigidArea(new Dimension(5,0)));
 		numberGrid3.add(buttons[1]);
 		numberGrid3.add(buttons[2]);
 		numberGrid3.add(buttons[3]);
 		numberGrid3.add(buttons[15]);
 		numberGrid3.add(buttons[12]);
 		
 		numberGrid4.add(buttons[23]);
 		numberGrid4.add(Box.createRigidArea(new Dimension(5,0)));
 		numberGrid4.add(buttons[0]);
 		numberGrid4.add(buttons[19]);
 		numberGrid4.add(buttons[18]);
 		numberGrid4.add(buttons[16]);
 		numberGrid4.add(buttons[17]);
 		
 		JPanel all = new JPanel();
 		all.setLayout(new BoxLayout(all, BoxLayout.Y_AXIS));
 		all.add(Box.createRigidArea(new Dimension(10,20))); //Space between top and textField.
 		all.add(txtField);
 		all.add(Box.createRigidArea(new Dimension(10,10)));//Space between textField and the first row of buttons.
 		all.add(clearButtonsRow);
 		all.add(numberGrid1);
 		all.add(numberGrid2);
 		all.add(numberGrid3);
 		all.add(numberGrid4);
 		all.add(Box.createRigidArea(new Dimension(50,20))); // Space in the bottom.
 		
 		for(int i =0 ; i < buttons.length; i++ )
 		{
 			buttons[i].addActionListener(this);
 			buttons[i].addKeyListener(this);
 		}
 		
 		frame.getContentPane().add(all);		
 	}
 
 	
 	/**
 	 * Calls the appropriate CalculatorFunctions based on the input. 
 	 */
 	public void actionPerformed(ActionEvent e) {
 		//Number 0
 		if(e.getSource() == buttons[0])
 		{
 			if(txtField.getText().length() == 1 && txtField.getText().charAt(0)=='0' 
 					|| calcFunctions.getLastPressedButton().equals("sqrt") 
 					|| calcFunctions.getLastPressedButton().equals("+")
 					|| calcFunctions.getLastPressedButton().equals("-")
 					|| calcFunctions.getLastPressedButton().equals("*")
 					|| calcFunctions.getLastPressedButton().equals("/")
 					|| calcFunctions.getLastPressedButton().equals("="))
 				txtField.setText("0");
 			else
 				txtField.setText(txtField.getText()+"0");
 			calcFunctions.setLastPressedButton("0");
 			calcFunctions.setReadyForSecondInput(true);
 		}
 		
 		//Number 1
 		else if(e.getSource() == buttons[1])
 		{
 			if(txtField.getText().length() == 1 && txtField.getText().charAt(0)=='0'
 					|| calcFunctions.getLastPressedButton().equals("sqrt")
 					|| calcFunctions.getLastPressedButton().equals("+")
 					|| calcFunctions.getLastPressedButton().equals("-")
 					|| calcFunctions.getLastPressedButton().equals("*")
 					|| calcFunctions.getLastPressedButton().equals("/")
 					|| calcFunctions.getLastPressedButton().equals("="))
 				txtField.setText("1");
 			else
 				txtField.setText(txtField.getText()+"1");
 			calcFunctions.setLastPressedButton("1");
 			calcFunctions.setReadyForSecondInput(true);
 		}
 		
 		//Number 2
 		else if(e.getSource() == buttons[2])
 		{
 			if(txtField.getText().length() == 1 && txtField.getText().charAt(0)=='0'
 					|| calcFunctions.getLastPressedButton().equals("sqrt") 
 					|| calcFunctions.getLastPressedButton().equals("+")
 					|| calcFunctions.getLastPressedButton().equals("-")
 					|| calcFunctions.getLastPressedButton().equals("*")
 					|| calcFunctions.getLastPressedButton().equals("/")
 					|| calcFunctions.getLastPressedButton().equals("="))
 				txtField.setText("2");
 			else
 				txtField.setText(txtField.getText()+"2");
 			calcFunctions.setLastPressedButton("2");
 			calcFunctions.setReadyForSecondInput(true);
 		}
 		
 		//Number 3
 		else if(e.getSource() == buttons[3])
 		{
 			if(txtField.getText().length() == 1 && txtField.getText().charAt(0)=='0'
 					|| calcFunctions.getLastPressedButton().equals("sqrt") 
 					|| calcFunctions.getLastPressedButton().equals("+")
 					|| calcFunctions.getLastPressedButton().equals("-")
 					|| calcFunctions.getLastPressedButton().equals("*")
 					|| calcFunctions.getLastPressedButton().equals("/")
 					|| calcFunctions.getLastPressedButton().equals("="))
 				txtField.setText("3");
 			else
 				txtField.setText(txtField.getText()+"3");
 			calcFunctions.setLastPressedButton("3");
 			calcFunctions.setReadyForSecondInput(true);
 		}
 		
 		//Number 4
 		else if(e.getSource() == buttons[4])
 		{
 			if(txtField.getText().length() == 1 && txtField.getText().charAt(0)=='0'
 					|| calcFunctions.getLastPressedButton().equals("sqrt")
 					|| calcFunctions.getLastPressedButton().equals("+")
 					|| calcFunctions.getLastPressedButton().equals("-")
 					|| calcFunctions.getLastPressedButton().equals("*")
 					|| calcFunctions.getLastPressedButton().equals("/")
 					|| calcFunctions.getLastPressedButton().equals("="))
 				txtField.setText("4");
 			else
 				txtField.setText(txtField.getText()+"4");
 			calcFunctions.setLastPressedButton("4");
 			calcFunctions.setReadyForSecondInput(true);
 		}
 		
 		//Number 5
 		else if(e.getSource() == buttons[5])
 		{
 			if(txtField.getText().length() == 1 && txtField.getText().charAt(0)=='0'
 					|| calcFunctions.getLastPressedButton().equals("sqrt") 
 					|| calcFunctions.getLastPressedButton().equals("+")
 					|| calcFunctions.getLastPressedButton().equals("-")
 					|| calcFunctions.getLastPressedButton().equals("*")
 					|| calcFunctions.getLastPressedButton().equals("/")
 					|| calcFunctions.getLastPressedButton().equals("="))
 				txtField.setText("5");
 			else
 				txtField.setText(txtField.getText()+"5");
 			calcFunctions.setLastPressedButton("5");
 			calcFunctions.setReadyForSecondInput(true);
 		}
 		
 		//Number 6
 		else if(e.getSource() == buttons[6])
 		{
 			if(txtField.getText().length() == 1 && txtField.getText().charAt(0)=='0'
 					|| calcFunctions.getLastPressedButton().equals("sqrt")
 					|| calcFunctions.getLastPressedButton().equals("+")
 					|| calcFunctions.getLastPressedButton().equals("-")
 					|| calcFunctions.getLastPressedButton().equals("*")
 					|| calcFunctions.getLastPressedButton().equals("/")
 					|| calcFunctions.getLastPressedButton().equals("="))
 				txtField.setText("6");
 			else
 				txtField.setText(txtField.getText()+"6");
 			calcFunctions.setLastPressedButton("6");
 			calcFunctions.setReadyForSecondInput(true);
 		}
 		
 		//Number 7
 		else if(e.getSource() == buttons[7])
 		{
 			if(txtField.getText().length() == 1 && txtField.getText().charAt(0)=='0'
 					|| calcFunctions.getLastPressedButton().equals("sqrt") 
 					|| calcFunctions.getLastPressedButton().equals("+")
 					|| calcFunctions.getLastPressedButton().equals("-")
 					|| calcFunctions.getLastPressedButton().equals("*")
 					|| calcFunctions.getLastPressedButton().equals("/")
 					|| calcFunctions.getLastPressedButton().equals("="))
 				txtField.setText("7");
 			else
 				txtField.setText(txtField.getText()+"7");
 			calcFunctions.setLastPressedButton("7");
 			calcFunctions.setReadyForSecondInput(true);
 		}
 		
 		//Number 8
 		else if(e.getSource() == buttons[8])
 		{
 			if(txtField.getText().length() == 1 && txtField.getText().charAt(0)=='0'
 					|| calcFunctions.getLastPressedButton().equals("sqrt")
 					|| calcFunctions.getLastPressedButton().equals("+")
 					|| calcFunctions.getLastPressedButton().equals("-")
 					|| calcFunctions.getLastPressedButton().equals("*")
 					|| calcFunctions.getLastPressedButton().equals("/")
 					|| calcFunctions.getLastPressedButton().equals("="))
 				txtField.setText("8");
 			else
 				txtField.setText(txtField.getText()+"8");
 			calcFunctions.setLastPressedButton("8");
 			calcFunctions.setReadyForSecondInput(true);
 		}
 		
 		//Number 9
 		else if(e.getSource() == buttons[9])
 		{
 			if(txtField.getText().length() == 1 && txtField.getText().charAt(0)=='0'
 					|| calcFunctions.getLastPressedButton().equals("sqrt") 
 					|| calcFunctions.getLastPressedButton().equals("+")
 					|| calcFunctions.getLastPressedButton().equals("-")
 					|| calcFunctions.getLastPressedButton().equals("*")
 					|| calcFunctions.getLastPressedButton().equals("/")
 					|| calcFunctions.getLastPressedButton().equals("="))
 				txtField.setText("9");
 			else
 				txtField.setText(txtField.getText()+"9");
 			calcFunctions.setLastPressedButton("9");
 			calcFunctions.setReadyForSecondInput(true);
 		}
 		
 		//Esc = C button
 		
 		// Division
 		else if(e.getSource() == buttons[10])
 		{
 			String result = calcFunctions.divPressed(txtField.getText());
 			txtField.setText(result);
 			calcFunctions.setLastPressedButton("/");
 		}
 		
 		// Multiplication 
 		else if(e.getSource() == buttons[11])
 		{
 			String result = calcFunctions.multPressed(txtField.getText());
 			txtField.setText(result);
 			calcFunctions.setLastPressedButton("*");
 		}
 		
 		//Inverse
 		else if (e.getSource() == buttons[12])
 		{
 			calcFunctions.setLastPressedButton("inverse");
 			if(txtField.getText().equals("inverse"))
 				txtField.setText("0");
 			else if(txtField.getText().length() != 0)
 				txtField.setText(calcFunctions.inverse(txtField.getText()));
 		}
 		
 		// Square root
 		else if(e.getSource() == buttons[14])
 		{
 			calcFunctions.setLastPressedButton("sqrt");
 			if(txtField.getText().equals("0"))
 				txtField.setText("0");
 			else if(txtField.getText().length() != 0)
 				txtField.setText(calcFunctions.sqrt(txtField.getText()));
 		}
 		
 		//- button
 		else if(e.getSource() == buttons[15])
 		{
 
 			String result = calcFunctions.minusPressed(txtField.getText());
 			txtField.setText(result);	
 			calcFunctions.setLastPressedButton("-");
 		}
 		
 		// + button
 		else if(e.getSource() == buttons[16])
 		{
 			String result = calcFunctions.sumPressed(txtField.getText());
 			txtField.setText(result);	
 			calcFunctions.setLastPressedButton("+");
 		}
 		
 		// = button
 		else if(e.getSource() == buttons[17])
 		{
 
 			if(calcFunctions.getOperator().equals("0"))
 				txtField.setText(txtField.getText());
 			
 			
 			else if(calcFunctions.getLastPressedButton().equals("=") ||calcFunctions.getLastPressedButton().equals("sqrt") )
 			{
 				double temporary=0;
 				if(calcFunctions.getLastPressedButton().equals("sqrt"))
 				{
 					calcFunctions.setSecondInput(txtField.getText());
 				}
 				if(calcFunctions.getOperator().equals("+"))
 					temporary = Double.parseDouble(calcFunctions.getLastResult()) + Double.parseDouble(calcFunctions.getSecondInput());
 				
 				else if(calcFunctions.getOperator().equals("-"))
 					temporary = Double.parseDouble(calcFunctions.getLastResult()) - Double.parseDouble(calcFunctions.getSecondInput());
 				
 				else if(calcFunctions.getOperator().equals("*"))
 					temporary = Double.parseDouble(calcFunctions.getLastResult()) * Double.parseDouble(calcFunctions.getSecondInput());
 				
 				else if(calcFunctions.getOperator().equals("/"))
 					temporary = Double.parseDouble(calcFunctions.getLastResult()) / Double.parseDouble(calcFunctions.getSecondInput());
 				
 				calcFunctions.setLastResult(String.valueOf(temporary)) ;
 				txtField.setText(calcFunctions.getLastResult());
 			}
 			
 			else 
 			{
 				calcFunctions.setSecondInput(txtField.getText() );
 				calcFunctions.compute(calcFunctions.getFirstInput(), calcFunctions.getSecondInput() );
 				calcFunctions.setLastResult(calcFunctions.getFirstInput());
 				calcFunctions.setFirstInput("q");
 				txtField.setText(calcFunctions.getLastResult());
 				calcFunctions.setReadyForSecondInput(false);
 			}
 			calcFunctions.setLastPressedButton("=");
 		}
 		
 		//Backspace
 		else if(e.getSource() == buttons[24]) 
 		{
 			if(calcFunctions.getLastPressedButton().equals("sqrt") || calcFunctions.getLastPressedButton().equals("="))
 			{//Do nothing
 				
 			}
 			else if(txtField.getText().length() == 1)
 				txtField.setText("0");
 			else//(txtField.getText().length() != 0)
 			{
 				String currentlyInTextField = txtField.getText(); 
 				txtField.setText(currentlyInTextField.substring(0, currentlyInTextField.length()-1));
 			}
 			
 		}
 		
 		//Clear E button
 		else if(e.getSource() == buttons[25])
 		{
 			txtField.setText("0");
 		}
 		
 		//Clear button
 		else if(e.getSource() == buttons[26])
 		{
 			txtField.setText("0");
 			calcFunctions.setLastPressedButton("clear");
 			calcFunctions.setFirstInput("q");
 			calcFunctions.setOperator("0");
 			calcFunctions.setSecondInput("0");
 			calcFunctions.setReadyForSecondInput(false);
 		}
 		
 	}
 
 	//Action listeners implementation
 	public void keyPressed(KeyEvent arg0) 
 	{
 		if(arg0.getKeyCode() == KeyEvent.VK_0)
 		{	
 			buttons[0].doClick();
 		}
 		
 		else if(arg0.getKeyCode() == KeyEvent.VK_1)
 		{
 			buttons[1].doClick();
 		}
 		
 		else if(arg0.getKeyCode() == KeyEvent.VK_2)
 		{
 			buttons[2].doClick();
 		}
 		
 		else if(arg0.getKeyCode() == KeyEvent.VK_3)
 		{
 			buttons[3].doClick();
 		}
 		
 		else if(arg0.getKeyCode() == KeyEvent.VK_4)
 		{
 			buttons[4].doClick();
 		}
 		
 		else if(arg0.getKeyCode() == KeyEvent.VK_5)
 		{
 			buttons[5].doClick();
 		}
 		
 		else if(arg0.getKeyCode() == KeyEvent.VK_6)
 		{
 			buttons[6].doClick();
 		}
 		
 		else if(arg0.getKeyCode() == KeyEvent.VK_7)
 		{
 			buttons[7].doClick();
 		}
 		
 		else if(arg0.getKeyCode() == KeyEvent.VK_8)
 		{
 			buttons[8].doClick();
 		}
 		
 		else if(arg0.getKeyCode() == KeyEvent.VK_9)
 		{
 			buttons[9].doClick();
 		}
 		
 		else if(arg0.getKeyCode() == KeyEvent.VK_ENTER)
 		{
 			buttons[17].doClick();
 		}
 		
 		else if(arg0.getKeyCode() == KeyEvent.VK_SLASH)
 		{
 			buttons[10].doClick();
 		}
 		
 		else if(arg0.getKeyCode() == KeyEvent.VK_ASTERISK)
 		{
 			buttons[11].doClick();
 		}
 		
 		else if(arg0.getKeyCode() == KeyEvent.VK_R)
 		{
 			buttons[12].doClick();
 		}
 		
 		else if(arg0.getKeyCode() == KeyEvent.VK_AT)
 		{
 			buttons[14].doClick();
 		}
 		
 		else if(arg0.getKeyCode() == KeyEvent.VK_SUBTRACT)
 		{
 			buttons[15].doClick();
 		}
 		
 		else if(arg0.getKeyCode() == KeyEvent.VK_BACK_SPACE)
 		{
 			buttons[24].doClick();
 		}
 		
 		else if(arg0.getKeyCode() == KeyEvent.VK_DELETE)
 		{
 			buttons[25].doClick();
 		}
 			
 		else if(arg0.getKeyCode() == KeyEvent.VK_ESCAPE)
 		{
 			buttons[26].doClick();
 		}
 	
 			
 		else if(arg0.getKeyCode() == KeyEvent.VK_ADD)
 		{
 			buttons[16].doClick();
 		}
 			
 	}
 
 	public void keyReleased(KeyEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 
 	public void keyTyped(KeyEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	
 
 }

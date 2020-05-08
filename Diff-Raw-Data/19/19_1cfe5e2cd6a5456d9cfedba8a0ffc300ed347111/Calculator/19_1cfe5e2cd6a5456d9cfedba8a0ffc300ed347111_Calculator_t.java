 package calculator.basic;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 
 @SuppressWarnings("serial")
 public class Calculator extends JFrame implements ActionListener, KeyListener{
 	private JButton buttons[] = new JButton[20];
 	private JTextField IO = new JTextField();
 	
 	private String tempVal = "0";
 	private String op = "";
 	private double val1 = 0;
	private boolean holdPrint = false;
 	
 	public Calculator() {
 		buttons[0] = new JButton("C");
 		buttons[1] = new JButton("/");
 		buttons[2] = new JButton("*");
 		buttons[3] = new JButton("-");
 		buttons[4] = new JButton("7");
 		buttons[5] = new JButton("8");
 		buttons[6] = new JButton("9");
 		buttons[7] = new JButton("+");
 		buttons[8] = new JButton("4");
 		buttons[9] = new JButton("5");
 		buttons[10] = new JButton("6");
 		buttons[11] = new JButton("X^3");
 		buttons[12] = new JButton("1");
 		buttons[13] = new JButton("2");
 		buttons[14] = new JButton("3");
 		buttons[15] = new JButton("X^2");
 		buttons[16] = new JButton("Enter");
 		buttons[17] = new JButton("0");
 		buttons[18] = new JButton(".");
 		buttons[19] = new JButton("sqrt");
 		
 		JPanel buttonPanel = new JPanel();
 		buttonPanel.setLayout(new GridLayout(5, 4));
 		for (int i = 0; i < buttons.length; i++) {
 			buttons[i].addActionListener(this);
 			buttons[i].addKeyListener(this);
 			buttonPanel.add(buttons[i]);
 		}
 		
 		IO.setEditable(false);
 		IO.setPreferredSize(new Dimension(200, 50));
 		IO.setText(tempVal);
 		IO.setHorizontalAlignment(JTextField.RIGHT);
 		JPanel IOPanel = new JPanel();
 		IOPanel.add(IO);
 		
 		add(buttonPanel, BorderLayout.CENTER);
 		add(IOPanel, BorderLayout.NORTH);
 	}
 	
 	public static void main(String[] args) {
 		Calculator calc = new Calculator();
 		calc.setSize(200, 250);
 		calc.setTitle("JGreeny Calculator");
 		calc.setResizable(false);
 		calc.setDefaultCloseOperation(EXIT_ON_CLOSE);
 		calc.setLocationRelativeTo(null);
 		calc.setVisible(true);
 	}
 	
 	@Override
 	public void actionPerformed(ActionEvent e) {
		holdPrint = false;
		
 		if (tempVal.equals("0"))
 			tempVal = "";
 		
 		if (e.getSource() == buttons[0]) {		// Clear
 			tempVal = "0";
 			val1 = 0;
 			op = "";
 		}
 		else if (e.getSource() == buttons[1])	// Divide
 			performOperation("/");
 			
 		else if (e.getSource() == buttons[2])	// Multiply
 			performOperation("*");
 		
 		else if (e.getSource() == buttons[3])	//	Subtract
 			performOperation("-");
 
 		else if (e.getSource() == buttons[4])	// 7
 			tempVal = tempVal + "7";
 
 		else if (e.getSource() == buttons[5])	// 8
 			tempVal = tempVal + "8";
 
 		else if (e.getSource() == buttons[6])	// 9
 			tempVal = tempVal + "9";
 
 		else if (e.getSource() == buttons[7])	// Add
 			performOperation("+");
 
 		else if (e.getSource() == buttons[8])	// 4
 			tempVal = tempVal + "4";
 
 		else if (e.getSource() == buttons[9])	// 5
 			tempVal = tempVal + "5";
 
 		else if (e.getSource() == buttons[10])	// 6
 			tempVal = tempVal + "6";
 
 		else if (e.getSource() == buttons[11]) {// X^3
 			performOperation("");
 			tempVal = String.valueOf(Math.pow(val1, 3));
 		}
 
 		else if (e.getSource() == buttons[12]) 	// 1
 			tempVal = tempVal + "1";
 
 		else if (e.getSource() == buttons[13]) 	// 2
 			tempVal = tempVal + "2";
 
 		else if (e.getSource() == buttons[14]) 	// 3
 			tempVal = tempVal + "3";
 
 		else if (e.getSource() == buttons[15])	{// X^2
 			performOperation("");
 			tempVal = String.valueOf(val1 * val1);
 		}
 
 		else if (e.getSource() == buttons[16]) {// Enter
 			if (op.equals("+"))
 				val1 += Double.parseDouble(tempVal);
 			else if (op.equals("-"))
 				val1 -= Double.parseDouble(tempVal);
 			else if (op.equals("*"))
 				val1 *= Double.parseDouble(tempVal);
 			else if (op.equals("/"))
 				val1 /= Double.parseDouble(tempVal);
 			else
 				val1 = Double.parseDouble(tempVal);
 
 			op = "";
			holdPrint = true;
			tempVal = "0";
 		}
 		
 		else if (e.getSource() == buttons[17]) 	// 0
 			tempVal = tempVal + "0";
 
 		else if (e.getSource() == buttons[18]) {// Period
 			if (!tempVal.contains(".")) {
 				if (tempVal.equals(""))
 					tempVal = "0.";
 				else
 					tempVal = tempVal + ".";
 			}
 		}
 		
 		else if (e.getSource() == buttons[19]) {// Square Root
 			performOperation("");
 			tempVal = String.valueOf(Math.sqrt(val1));
 		}
 		
 		/** Prints the input/output in the IO panel. **/
		if (holdPrint)
			IO.setText(String.valueOf(val1));
		else if (!op.equals(""))
 			IO.setText(String.format("%8s %8s %8s", String.valueOf(val1), op, tempVal));
 		else
 			IO.setText(tempVal);
 	}
 
 	@Override
 	public void keyTyped(KeyEvent e) {
 		
 	}
 	@Override
 	public void keyPressed(KeyEvent e){}
 	@Override
 	public void keyReleased(KeyEvent e) {}
 
 	private void performOperation(String oper) {
		if (tempVal.equals(""))
			tempVal = "0";
		
 		if (op.equals(""))
 			val1 = Double.parseDouble(tempVal);
 		
 		else if (op.equals("+"))
 			val1 += Double.parseDouble(tempVal);
 		
 		else if (op.equals("-"))
 			val1 -= Double.parseDouble(tempVal);
 		
 		else if (op.equals("*")) {
 			if (val1 == 0)
 				val1 = 1;
 			
 			val1 *= Double.parseDouble(tempVal);
 		}
 		
 		else if (op.equals("/")) {
 			val1 /= Double.parseDouble(tempVal);
 		}
 		
 		tempVal = "0";
 		op = oper;
 	}
 }

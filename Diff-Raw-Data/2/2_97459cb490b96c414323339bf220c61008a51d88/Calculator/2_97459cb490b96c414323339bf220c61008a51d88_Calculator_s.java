 import java.util.Stack;
 import java.awt.Font;
 import java.awt.GridLayout;
 import java.awt.BorderLayout;
 import java.awt.FlowLayout;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JButton;
 import javax.swing.JTextField;
 
 /**
  * The calculator window and entry point
  */
 class Calculator extends JFrame {
 	/**
 	 * Display area. Numbers should not be entered here by the user.
 	 */
 	private JTextField display = new JTextField();
 	
 	/**
 	 * Operand storage.
 	 * 
 	 * This is a LIFO storage object. For more information,
 	 * see http://en.wikipedia.org/wiki/Stack_(data_structure)
 	 */
 	private Stack<Double> stack = new Stack<Double>();
 	
 	/**
 	 * The staging area for new numbers.
 	 * 
 	 * Numeral buttons and the point button append their values here as
 	 * strings, and the whole object is converted and pushed to the
 	 * stack when needed.
 	 */
 	private StringBuilder stage = new StringBuilder();
 	
 	public Calculator() {
 		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		this.setSize(350, 200);
 		this.setTitle("Calculator");
 		this.setLayout(new BorderLayout(5,5));
 		
 		this.display.setColumns(24);
 		this.display.setHorizontalAlignment(JTextField.RIGHT);
 		//this.display.setFont(new Font(
 		
 		// Calculator Display
 		this.add(new JPanel() {{
 			setLayout(new FlowLayout(FlowLayout.RIGHT, 5,5));
 			
 			add(display);
 		}}, BorderLayout.NORTH);
 		
 		// Function buttons
 		this.add(new JPanel() {{
 			setLayout(new GridLayout(0,2,5,5));
 			
 			// the 'square' button
 			// This adds a new button as an anonymous class to the
 			// functions panel. On the click event, the button pushes
 			// the current number to the stack, pops it off the stack
 			// (all function buttons work like this, pulling operands
 			// only from the stack), squares it, and pushes it back on
 			// the stack.
 			add(new JButton("x^2") {{
 				addActionListener(new ActionListener() {
 					public void actionPerformed(ActionEvent e) {
 						operand();
 						System.out.println("x squared");
 						stack.push(Math.pow(stack.pop(), 2));
 						updateResult();
 					}
 				});
 			}});
 			
 			// the 'percent' button
 			add(new JButton("%") {{
 				addActionListener(new ActionListener() {
 					public void actionPerformed(ActionEvent e) {
 						// push any newly entered number to the stack.
 						operand();
 						
 						// verify that there is at least 1number to
 						// operate on (operand can push nothing if
 						// there is nothing entered).
 						if(verifyOperands(1)) {
 							System.out.println("x%");
 							
 							// in order, pop the top number off the
 							// stack, divide it by 100, and push it
 							// back onto the stack.
 							stack.push(stack.pop() / 100.00);
 							
 							// update the display with the top of the
 							// stack.
 							updateResult();
 						}
 					}
 				});
 			}});
 			
 			// the 'cube' button
 			add(new JButton("x^3") {{
 				addActionListener(new ActionListener() {
 					public void actionPerformed(ActionEvent e) {
 						// push any newly entered number to the stack.
 						operand();
 						
 						System.out.println("x cubed");
 						
 						// in order, pop the top number off the stack,
 						// raise it to the third power, and push it
 						// back onto the stack.
 						stack.push(Math.pow(stack.pop(), 3));
 						
 						// update the display with the top of the
 						// stack.
 						updateResult();
 					}
 				});
 			}});
 			
 			// the 'inverse' button
 			add(new JButton("1/x") {{
 				addActionListener(new ActionListener() {
 					public void actionPerformed(ActionEvent e) {
 						operand();
 						System.out.println("1 over x");
 						stack.push(1.0 / stack.pop());
 						updateResult();
 					}
 				});
 			}});
 			
 			// the 'square root' button
 			add(new JButton("sqrt") {{
 				addActionListener(new ActionListener() {
 					public void actionPerformed(ActionEvent e) {
 						operand();
 						System.out.println("square root x");
 						stack.push(Math.sqrt(stack.pop()));
 						updateResult();
 					}
 				});
 			}});
 			
 			// the 'cube root' button
 			add(new JButton("cbrt") {{
 				addActionListener(new ActionListener() {
 					public void actionPerformed(ActionEvent e) {
 						System.out.println("cube root x");
 						
 					}
 				});
 			}});
 			
 			// the 'sine' button
 			add(new JButton("sin") {{
 				addActionListener(new ActionListener() {
 					public void actionPerformed(ActionEvent e) {
 						operand();
 						stack.push(Math.sin(stack.pop()));
 						updateResult();
 						System.out.println("sin x");
 					}
 				});
 			}});
 			
 			// the 'cosine' button
 			add(new JButton("cos") {{
 				addActionListener(new ActionListener() {
 					public void actionPerformed(ActionEvent e) {
 						operand();
 						stack.push(Math.cos(stack.pop()));
 						updateResult();
 						System.out.println("cosin x");
 					}
 				});
 			}});
 			
 			// the 'tan' button
 			add(new JButton("tan") {{
 				addActionListener(new ActionListener() {
 					public void actionPerformed(ActionEvent e) {
 						operand();
 						stack.push(Math.tan(stack.pop()));
 						updateResult();
 						System.out.println("tan x");
 					}
 				});
 			}});
 			
 			// the 'natural logarithm' button
 			add(new JButton("ln") {{
 				addActionListener(new ActionListener() {
 					public void actionPerformed(ActionEvent e) {
 						System.out.println("ln x");
 					}
 				});
 			}});
 		}}, BorderLayout.WEST);
 		
 		// Numeral and basic arithmetic operator buttons
 		this.add(new JPanel() {{
 			setLayout(new GridLayout(0,4,5,5));
 			
 			// the '7' button
 			add(new JButton("7") {{
 				addActionListener(new ActionListener() {
 					public void actionPerformed(ActionEvent e) {
 						// append '7' to the StringBuilder stage
 						stage.append("7");
 						
 						// update the display with the current contents
 						// of the stage.
 						updateStaged();
 					}
 				});
 			}});
 			
 			// the '8' button
 			add(new JButton("8") {{
 				addActionListener(new ActionListener() {
 					public void actionPerformed(ActionEvent e) {
 						stage.append("8");
 						
 						updateStaged();
 					}
 				});
 			}});
 			
 			// the '9' button
 			add(new JButton("9") {{
 				addActionListener(new ActionListener() {
 					public void actionPerformed(ActionEvent e) {
 						stage.append("9");
 						
 						updateStaged();
 					}
 				});
 			}});
 			
 			// the '+' button
 			add(new JButton("+") {{
 				addActionListener(new ActionListener() {
 					public void actionPerformed(ActionEvent e) {
 						System.out.print(" + ");
 						operand();
 						
 						if(verifyOperands(2)) {
 							stack.push(stack.pop() + stack.pop());
 							System.out.println(stack.peek());
 							
 							updateResult();
 						}
 					}
 				});
 			}});
 			
 			// the '4' button
 			add(new JButton("4") {{
 				addActionListener(new ActionListener() {
 					public void actionPerformed(ActionEvent e) {
 						stage.append("4");
 						
 						updateStaged();
 					}
 				});
 			}});
 			
 			// the '5' button
 			add(new JButton("5") {{
 				addActionListener(new ActionListener() {
 					public void actionPerformed(ActionEvent e) {
 						stage.append("5");
 						
 						updateStaged();
 					}
 				});
 			}});
 			
 			// the '6' button
 			add(new JButton("6") {{
 				addActionListener(new ActionListener() {
 					public void actionPerformed(ActionEvent e) {
 						stage.append("6");
 						
 						updateStaged();
 					}
 				});
 			}});
 			
 			// the '-' button
 			add(new JButton("-") {{
 				addActionListener(new ActionListener() {
 					public void actionPerformed(ActionEvent e) {
 						System.out.print(" - ");
 						operand();
 						
 						// subtraction
 						if(verifyOperands(2)) {
 							double b = stack.pop();
 							double a = stack.pop();
 							double c = a - b;
 							System.out.println(c);
 							stack.push(c);
 							System.out.println(stack.peek());
 							
 							updateResult();
 						}
 						
 						// negation
 						else if(verifyOperands(1)) {
 							stack.push(0 - stack.pop());
 							
 							updateResult();
 						}
 					}
 				});
 			}});
 			
 			// the '1' button
 			add(new JButton("1") {{
 				addActionListener(new ActionListener() {
 					public void actionPerformed(ActionEvent e) {
 						stage.append("1");
 						
 						updateStaged();
 					}
 				});
 			}});
 			
 			// the '2' button
 			add(new JButton("2") {{
 				addActionListener(new ActionListener() {
 					public void actionPerformed(ActionEvent e) {
 						stage.append("2");
 						
 						updateStaged();
 					}
 				});
 			}});
 			
 			// the '3' button
 			add(new JButton("3") {{
 				addActionListener(new ActionListener() {
 					public void actionPerformed(ActionEvent e) {
 						stage.append("3");
 						
 						updateStaged();
 					}
 				});
 			}});
 			
 			// the '*' button
 			add(new JButton("*") {{
 				addActionListener(new ActionListener() {
 					public void actionPerformed(ActionEvent e) {
 						System.out.print(" * ");
 						operand();
 						
 						if(verifyOperands(2)) {
 							stack.push(stack.pop() * stack.pop());
 							System.out.println(stack.peek());
 							
 							updateResult();
 						}
 					}
 				});
 			}});
 			
 			// the '.' button
 			add(new JButton(".") {{
 				addActionListener(new ActionListener() {
 					public void actionPerformed(ActionEvent e) {
 						stage.append(".");
 						
 						updateStaged();
 					}
 				});
 			}});
 			
 			// the '0' button
 			add(new JButton("0") {{
 				addActionListener(new ActionListener() {
 					public void actionPerformed(ActionEvent e) {
 						stage.append("0");
 						
 						updateStaged();
 					}
 				});
 			}});
 			
 			// the '=' button
 			add(new JButton("=") {{
 				addActionListener(new ActionListener() {
 					public void actionPerformed(ActionEvent e) {
 						System.out.print(" = ");
 						operand();
 						
 						updateResult();
 					}
 				});
 			}});
 			
 			// the '/' button
 			add(new JButton("/") {{
 				addActionListener(new ActionListener() {
 					public void actionPerformed(ActionEvent e) {
 						System.out.print(" / ");
 						operand();
 						
 						if(verifyOperands(2)) {
 							double b = stack.pop();
 							double a = stack.pop();
 							
 							stack.push(a / b);
 						}
 						
 						updateResult();
 					}
 				});
 			}});
 		}}, BorderLayout.CENTER);
 	}
 	
 	/**
 	 * Update the calculator display with the last number on the stack.
 	 * Usually this is the last function result.
 	 */
 	private void updateResult() {
 		// peek at the top value (not pop, which removes the top
 		// value!), convert it to string and set it as the text of the
 		// display area.
 		this.display.setText(stack.peek().toString());
 	}
 	
 	/**
 	 * Update the display with the current staged number.
 	 *
 	 * This is used by the numeral and decimal buttons.
 	 */
 	private void updateStaged() {
 		// convert the stage to string and display it in the display
 		// area.
 		this.display.setText(stage.toString());
 	}
 	
 	/**
 	 * Verify the stack has at least a given number of numbers on it.
 	 */
 	private boolean verifyOperands(int count) {
		return count >= this.stack.size();
 	}
 	
 	/**
 	 * Push the staged number onto the stack for use by the functions.
 	 */
 	private void operand() {
 		// check to make sure there is something on the stage.
 		if(this.stage.length() >= 1) {
 			// convert the stage to string, and then to double.
 			double operand = Double.valueOf(stage.toString());
 			
 			// make a new stage, since the number in the old one is
 			// used.
 			this.stage = new StringBuilder();
 			
 			System.out.println(operand);
 			
 			// push the new number onto the top of the stack.
 			this.stack.push(operand);
 			
 			// update the display.
 			this.updateResult();
 		}
 	}
 	/**
 	 * Application entry point.
 	 */
 	public static void main(String[] args) {
 		Calculator calc = new Calculator();
 		calc.setVisible(true);
 	}
 }

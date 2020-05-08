 /*
 	FileName:GratuityCalculator
 	Name: Ryan Quinn
 	Date: November 6, 2011
 */
 
 import java.awt.*;
 import java.awt.event.*;
 
 import javax.swing.*;
 import javax.swing.border.*;
 import javax.swing.event.*;
 import java.text.*;
 
 public class GratuityCalculatorInterface extends JFrame {
 	//Declarations
 	Color black = new Color(0, 0, 0);
 	Color white = new Color(255, 255, 255);
 	
 	JLabel billAmountJLabel;
 	JTextField billAmountJTextField;
 	
 	JLabel gratuityAmountJLabel;
 	JTextField gratuityAmountJTextField;
 	
 	JLabel totalBillAmountJLabel;
 	JTextField totalBillAmountJTextField;
 	
 	JButton enterJButton;
 	JButton clearJButton;
 	JButton closeJButton;
 	
 	double billAmount, gratuityAmount, totalBillAmount;
 	DecimalFormat currencyForDollars = new DecimalFormat("$0.00");
 	DoCalculations doCalculations;
 	
 	public GratuityCalculatorInterface() {
       	createUserInterface();
    	}
 	
 	private void createUserInterface() {
 		Container contentPane = getContentPane();
 		contentPane.setBackground(Color.white);
 		contentPane.setLayout(null);
 		
 		billAmountJLabel = new JLabel();
 		billAmountJLabel.setBounds(50, 50, 120, 20);
 		billAmountJLabel.setFont(new Font("Default", Font.PLAIN, 9));
 		billAmountJLabel.setText("Enter the amount of the bill:");
 		billAmountJLabel.setForeground(black);
 		billAmountJLabel.setHorizontalAlignment(JLabel.LEFT);
 		contentPane.add(billAmountJLabel);
 		
 		billAmountJTextField = new JTextField();
 		billAmountJTextField.setBounds(225, 50, 100, 20);
 		billAmountJTextField.setFont(new Font("Default", Font.PLAIN, 12));
 		billAmountJTextField.setHorizontalAlignment(JTextField.CENTER);
 		billAmountJTextField.setForeground(black);
 		billAmountJTextField.setBackground(white);
 		billAmountJTextField.setEditable(true);
 		contentPane.add(billAmountJTextField);
 		
 		gratuityAmountJLabel = new JLabel();
 		gratuityAmountJLabel.setBounds(50, 200, 120, 20);
 		gratuityAmountJLabel.setFont(new Font("Default", Font.PLAIN, 12));
 		gratuityAmountJLabel.setText("Amount of Gratuity:");
 		gratuityAmountJLabel.setForeground(black);
 		gratuityAmountJLabel.setHorizontalAlignment(JLabel.LEFT);
 		contentPane.add(gratuityAmountJLabel);
 		
 		gratuityAmountJTextField = new JTextField();
 		gratuityAmountJTextField.setBounds(225, 200, 100, 20);
 		gratuityAmountJTextField.setFont(new Font("Default", Font.PLAIN, 12));
 		gratuityAmountJTextField.setHorizontalAlignment(JTextField.CENTER);
 		gratuityAmountJTextField.setForeground(black);
 		gratuityAmountJTextField.setBackground(white);
 		gratuityAmountJTextField.setEditable(false);
 		contentPane.add(gratuityAmountJTextField);
 		
 		totalBillAmountJLabel = new JLabel();
 		totalBillAmountJLabel.setBounds(50, 250, 120, 20);
 		totalBillAmountJLabel.setFont(new Font("Default", Font.PLAIN, 12));
 		totalBillAmountJLabel.setText("Total Amount Due:");
 		totalBillAmountJLabel.setForeground(black);
 		totalBillAmountJLabel.setHorizontalAlignment(JLabel.LEFT);
 		contentPane.add(totalBillAmountJLabel);
 		
 		totalBillAmountJTextField = new JTextField();
 		totalBillAmountJTextField.setBounds(225, 250, 100, 20);
 		totalBillAmountJTextField.setFont(new Font("Default", Font.PLAIN, 12));
 		totalBillAmountJTextField.setHorizontalAlignment(JTextField.CENTER);
 		totalBillAmountJTextField.setForeground(black);
 		totalBillAmountJTextField.setBackground(white);
 		totalBillAmountJTextField.setEditable(false);
 		contentPane.add(totalBillAmountJTextField);
 		
 		enterJButton = new JButton();
 		enterJButton.setBounds(20, 300, 100, 20);
 		enterJButton.setFont(new Font("Default", Font.PLAIN, 12));
 		enterJButton.setText("Enter");
 		enterJButton.setForeground(black);
 		enterJButton.setBackground(white);
 		contentPane.add(enterJButton);
 		enterJButton.addActionListener(
 		
 			new ActionListener() {
 				public void actionPerformed(ActionEvent event) {
 					enterJButtonActionPerformed(event);
 				}
 			}
 		);
 		
 		clearJButton = new JButton();
 		clearJButton.setBounds(130, 300, 100, 20);
 		clearJButton.setFont(new Font("Default", Font.PLAIN, 12));
 		clearJButton.setText("Clear");
 		clearJButton.setForeground(black);
 		clearJButton.setBackground(white);
 		contentPane.add(clearJButton);
 		clearJButton.addActionListener(
 		
 			new ActionListener() {
 				public void actionPerformed(ActionEvent event) {
 					clearJButtonActionPerformed(event);
 				}
 			}
			
			
 		);
 		
 		closeJButton = new JButton();
 		closeJButton.setBounds(240, 300, 100, 20);
 		closeJButton.setFont(new Font("Default", Font.PLAIN, 12));
 		closeJButton.setText("Close");
 		closeJButton.setForeground(black);
 		closeJButton.setBackground(white);
 		contentPane.add(closeJButton);
 		closeJButton.addActionListener(
 
 			new ActionListener()
 			{
 				public void actionPerformed(ActionEvent event) {
 					closeJButtonActionPerformed(event);
 				}
 			}
 		);
 		
 		setTitle("Gratutity Calculator");
 		setSize( 400, 400 );
 		setVisible(true);
 	}	
 	
 	public static void main(String[] args) {
       	GratuityCalculatorInterface application = new GratuityCalculatorInterface();
       	application.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 	}
 	
    	public void enterJButtonActionPerformed(ActionEvent event) {
 		getBillAmount();
 	}
 	
    	public double getBillAmount() {
 		try {
 			billAmount = Double.parseDouble(billAmountJTextField.getText());
 			calculateResults();
 		}
 		
 		catch(NumberFormatException exception) {
 		      	JOptionPane.showMessageDialog(this,
 		       	"Please enter first integer!",
 		        "Number Format Error", JOptionPane.ERROR_MESSAGE );
 		        billAmountJTextField.setText("");
 		        billAmountJTextField.requestFocusInWindow();
 		}
 		
 		return billAmount;	
    	}
    	
    	public void calculateResults() {
    		doCalculations = new DoCalculations(billAmount);
    		printResults();	
    	}
    	
    	public void printResults() {
    		gratuityAmount = doCalculations.returnGratuity();
    		totalBillAmount = doCalculations.returnTotalBill();
    		
    		gratuityAmountJTextField.setText("" + currencyForDollars.format(gratuityAmount));
    		totalBillAmountJTextField.setText("" + currencyForDollars.format(totalBillAmount));
    	}
    	
 	public void clearJButtonActionPerformed(ActionEvent event)
 	{
 		billAmountJTextField.setText("");
 		billAmountJTextField.requestFocusInWindow();
 		gratuityAmountJTextField.setText("");
 		totalBillAmountJTextField.setText("");
 	}
 	
 	public void closeJButtonActionPerformed(ActionEvent event)
 	{
 		GratuityCalculatorInterface.this.dispose();
 	}
 }
 
 class DoCalculations {
 	double numberOne, billAmount, gratuity, totalBill;
 	final double GRATUITY_RATE = .15;
 	
 	public DoCalculations(double numberOne) {
 		billAmount = numberOne;
 		gratuity = billAmount * GRATUITY_RATE;
 		totalBill = billAmount + gratuity;
 	}
 	
 	public double returnGratuity() {
 		return gratuity;
 	}
 	
 	public double returnTotalBill() {
 		return totalBill;
 	}
 }
 

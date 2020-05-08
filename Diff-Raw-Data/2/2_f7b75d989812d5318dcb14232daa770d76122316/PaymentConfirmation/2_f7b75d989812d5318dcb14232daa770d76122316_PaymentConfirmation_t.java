 package ee.ut.math.tvt.salessystem.ui;
 
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JFormattedTextField;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 
 /**
  * Purhase confirmation dialog
  * 
  * @author TKasekamp
  * 
  */
 public class PaymentConfirmation extends JDialog {
 
 	private boolean accepted;
 	private double payment = 0;
 	private double sum;
 	private static final long serialVersionUID = 1L;
 	private JLabel sumPayment;
 	private JTextField changePayment;
 	private JTextField amountField;
 	private JButton acceptPayment;
 	private JButton cancelPayment;
 	private JLabel message;
 
 	public PaymentConfirmation(JFrame frame, boolean modal, double sum) {
 		super(frame, modal);
 		this.sum = sum;
 		this.add(draw());
 		setTitle("Confirm purchase");
 		pack();
 
 		int width = 300;
 		int height = 200;
 		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
 		this.setLocation((screen.width - width) / 2,
 				(screen.height - height) / 2);
 		this.setSize(width, height);
 		setVisible(true);
 	}
 
 	private JPanel draw() {
 		JPanel panel = new JPanel();
 		setLayout(new GridLayout(5, 2));
 
 		// Initialize the buttons
 		acceptPayment = createAcceptPaymentButton();
 		cancelPayment = createCancelPaymentButton();
 
 		// Sum from purchase table
 		sumPayment = new JLabel();
 		sumPayment.setText(Double.toString(sum));
 
 		changePayment = new JTextField();
 		changePayment.setText("0");
 		changePayment.setEnabled(false);
 
 		amountField = new JFormattedTextField();
 		amountField.setText("0");
 		amountField.setColumns(10);
 		amountField.getDocument().addDocumentListener(new Dkuular());
 
 		// Warning message
 		message = new JLabel("");
 
 		add(new JLabel("Total: "));
 		add(sumPayment);
 
 		add(new JLabel("Return amount: "));
 		add(changePayment);
 
 		add(new JLabel("Enter money here: "));
 		add(amountField);
 
 		add(acceptPayment);
 		add(cancelPayment);
 
 		add(message);
 		return panel;
 
 	}
 
 	/**
 	 * For checking if the purchase was accepted in the <code>PurchaseTab</code>
 	 * 
 	 * @return boolean
 	 */
 	public boolean isAccepted() {
 		return accepted;
 	}
 
 	/**
 	 * Creates the Accept payment button
 	 * 
 	 * @return button
 	 */
 	private JButton createAcceptPaymentButton() {
 		JButton b = new JButton("Accept");
 		b.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				acceptButtonClicked();
 			}
 		});
 
 		return b;
 	}
 
 	/**
 	 * Creates the Cancel payment button
 	 * 
 	 * @return button
 	 */
 	private JButton createCancelPaymentButton() {
 		JButton b = new JButton("Cancel");
 		b.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				cancelButtonClicked();
 			}
 		});
 
 		return b;
 	}
 
 	private void acceptButtonClicked() {
 		boolean allnumbers = true;
 		if (amountField.getText().isEmpty()) {
 			allnumbers = false;
 			message.setText("Enter more money");
 		}
 		for (int i = 0; i < amountField.getText().length(); i++) {
 			if (!Character.isDigit(amountField.getText().charAt(i))) {
 				message.setText("Enter integers only");
 				allnumbers = false;
 			}
 		}
 
 		if (allnumbers == true) {
 			// Checking if there is enough money entered
 			payment = ((Number) Double.parseDouble(amountField.getText()))
 					.doubleValue();
 			if ((payment - sum) >= 0) {
 				message.setText("Purchase accepted");
 				accepted = true;
 				setVisible(false);
 			} else {
 				message.setText("Enter more money");
 			}
 		}
 
 	}
 
 	private void cancelButtonClicked() {
 		amountField.setText("0");
 		changePayment.setText("0");
 		accepted = false;
 		setVisible(false);
 
 	}
 
 	/**
 	 * Updates return amount in confirmation window.
 	 * 
 	 * 
 	 */
 	class Dkuular implements DocumentListener {
 
 		@Override
 		public void insertUpdate(DocumentEvent e) {
 			update();
 		}
 
 		@Override
 		public void removeUpdate(DocumentEvent e) {
 			if (amountField.getText().isEmpty() == false) {
 				update();
 			} else {
 				changePayment.setText("0");
 			}
 		}
 
 		@Override
 		public void changedUpdate(DocumentEvent e) {
 			update();
 		}
 
 		private void update() {
 			try {
 
 				payment = ((Number) Double.parseDouble(amountField.getText()))
 						.doubleValue();
				changePayment.setText(Double.toString(Math.round((payment - sum)*100)/100.0));
 			} catch (NumberFormatException ex) { // handle your exception
 				changePayment.setText("0");
 			}
 		}
 
 	}
 
 }

 package payStation.gui;
 
 import java.awt.Component;
 import java.awt.EventQueue;
 import java.awt.Font;
 
 import javax.swing.Box;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JTextField;
 
 import net.miginfocom.swing.MigLayout;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 
 import payStation.payStationInterface.*;
 import payStation.payStationInterface.exception.IllegalCoinException;
 import payStation.impl.*;
 
 /**
  * Most of this is automatically created by WindowBuilder in Eclipse
  */
 public class PayStationGUI {
 
 	private JFrame frmPayStation;
 	private JTextField coinInputTextField;
 	private JLabel centLabel;
 	private JLabel minutesLabel;
 
 	private PayStation payStation = new PayStationImpl();
 
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					PayStationGUI window = new PayStationGUI();
 					window.frmPayStation.setVisible(true);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Create the application.
 	 */
 	public PayStationGUI() {
 		initialize();
 	}
 
 	/**
 	 * Initialize the contents of the frame.
 	 */
 	private void initialize() {
 		frmPayStation = new JFrame();
 		frmPayStation.setTitle("Pay Station");
 		frmPayStation.setBounds(100, 100, 419, 194);
 		frmPayStation.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frmPayStation.getContentPane().setLayout(
 				new MigLayout("", "[][][][][][][grow][][]", "[][][][][]"));
 
 		Component horizontalStrut = Box.createHorizontalStrut(30);
 		frmPayStation.getContentPane().add(horizontalStrut, "cell 3 0");
 
 		Component horizontalStrut_2 = Box.createHorizontalStrut(30);
 		frmPayStation.getContentPane().add(horizontalStrut_2, "cell 5 0");
 
 		JLabel lblYouHavePaid = new JLabel("You have paid:");
 		frmPayStation.getContentPane().add(lblYouHavePaid, "cell 1 1");
 
 		centLabel = new JLabel("0");
 		frmPayStation.getContentPane().add(centLabel, "cell 3 1");
 
 		JLabel lblCents = new JLabel("cent");
 		frmPayStation.getContentPane().add(lblCents, "cell 4 1");
 
 		JLabel lblInsertCoin = new JLabel("Insert coin");
 		lblInsertCoin.setFont(new Font("Tahoma", Font.BOLD, 11));
 		frmPayStation.getContentPane().add(lblInsertCoin,
 				"cell 6 1,alignx center");
 
 		JLabel lblYouHavePaid_1 = new JLabel("Your parking time:");
 		frmPayStation.getContentPane().add(lblYouHavePaid_1, "cell 1 2");
 
 		minutesLabel = new JLabel("0");
 		frmPayStation.getContentPane().add(minutesLabel, "cell 3 2");
 
 		JLabel lblMinutes = new JLabel("minutes");
 		frmPayStation.getContentPane().add(lblMinutes, "cell 4 2");
 
 		coinInputTextField = new JTextField();
 		coinInputTextField.addActionListener(new ActionListener() {
 			/*
 			 * (non-Javadoc) When enter is pressed after a number has been
 			 * input, we try to get the number and send it to the payStation. If
 			 * the coin is deemed invalid by the pay station, an
 			 * IllegalCoinException is thrown
 			 */
 			public void actionPerformed(ActionEvent arg0) {
 				try {
 					int coinValue;
 					coinValue = Integer.parseInt(coinInputTextField.getText());
 					coinInputTextField.setText(null);
 					payStation.addPayment(coinValue);
 					updateLabels();
 				} catch (NumberFormatException ex) {
 					// User entered something other than a number, do nothing
 				} catch (IllegalCoinException ex) {
					JOptionPane.showInternalMessageDialog(frmPayStation,
 							"Invalid coin inserted: coin returned",
 							"Invalid coin", JOptionPane.INFORMATION_MESSAGE);
 				}
 			}
 		});
 		frmPayStation.getContentPane()
 				.add(coinInputTextField, "cell 6 2,growx");
 		coinInputTextField.setColumns(10);
 
 		JLabel lblCents_1 = new JLabel("cent");
 		frmPayStation.getContentPane().add(lblCents_1, "cell 7 2,alignx left");
 
 		Component horizontalStrut_1 = Box.createHorizontalStrut(20);
 		frmPayStation.getContentPane().add(horizontalStrut_1, "cell 8 2");
 
 		JButton buyButton = new JButton("Buy");
 		buyButton.addActionListener(new ActionListener() {
 			/*
 			 * (non-Javadoc) When the buy button is clicked, we request a
 			 * receipt from the payStation and display it
 			 */
 			public void actionPerformed(ActionEvent arg0) {
 				Receipt receipt = payStation.buy();
 				updateLabels();
 				JOptionPane.showInternalMessageDialog(
 						frmPayStation.getContentPane(),
 						"Payment: " + receipt.getPayment() + "\nParking Time: "
 								+ receipt.getMinutes(), "Receipt",
 						JOptionPane.INFORMATION_MESSAGE);
 			}
 		});
 		frmPayStation.getContentPane().add(buyButton, "cell 1 4");
 
 		JButton cancelButton = new JButton("Cancel");
 		cancelButton.addActionListener(new ActionListener() {
 			/*
 			 * (non-Javadoc) When the cancel button is pressed, the payStation
 			 * is reset and the labels are updated
 			 */
 			public void actionPerformed(ActionEvent arg0) {
 				payStation.cancel();
 				updateLabels();
 			}
 		});
 		frmPayStation.getContentPane().add(cancelButton, "cell 2 4 3 1");
 	}
 
 	/**
 	 * Updates the cent and the minutes labels with their current value in
 	 * payStation
 	 */
 	private void updateLabels() {
 		centLabel.setText(Integer.toString(payStation.getPayment()));
 		minutesLabel.setText(Integer.toString(payStation.getMinutes()));
 	}
 }

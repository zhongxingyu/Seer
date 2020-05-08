 package ee.ut.math.tvt.salessystem.ui.popups;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 import ee.ut.math.tvt.salessystem.ui.model.SalesSystemModel;
 import ee.ut.math.tvt.salessystem.ui.tabs.PurchaseTab;
 
 public class PurchaseConfirmPopup extends JFrame {
 
 	private static final long serialVersionUID = 1L; // class version
 
 	private DefaultPopup frame;
 
 	private Map<String, JTextField> elements = new HashMap<String, JTextField>();
 
 	private SalesSystemModel model;
 
 	private String totalField = "Total:";
 	private String paymentField = "Payment:";
 	private String changeField = "Change:";
 
 	// builds add product panel
 	public PurchaseConfirmPopup(final double total, SalesSystemModel mod,
 			final PurchaseTab parent) {
 		String title = "Add product";
 
 		this.model = mod;
 
 		// Fill the change field if the payment field loses focus
 		final JTextField payment = new JTextField();
 		final JTextField change = new JTextField();
 		payment.addFocusListener(new FocusListener() {
 			public void focusGained(FocusEvent e) {
 			}
 
 			public void focusLost(FocusEvent e) {
 				try {
					change.setText(String.format("%.2g%n",
							Double.valueOf(payment.getText()) - total));
 				} catch (NumberFormatException ex) {
					if (!payment.getText().equals("")) {
						JOptionPane.showMessageDialog(
								PurchaseConfirmPopup.this,
								"Payment must be a number!",
								"Purchase not completed",
								JOptionPane.PLAIN_MESSAGE);
					}
 				}
 			}
 		});
 
 		// Make the elements
 		elements = new HashMap<String, JTextField>();
 		elements.put(totalField, new JTextField(String.valueOf(total)));
 		elements.put(paymentField, payment);
 		elements.put(changeField, change);
 
 		// Make the window
 		frame = new DefaultPopup(title, 400, 280, 1);
 
 		// Make the panel
 		JPanel panel = frame.makePanel(title, elements, false);
 
 		// Add ok button
 		JButton okButton = new JButton("Confirm purchase");
 		okButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				try {
 					if (Double.valueOf(payment.getText()) < total) {
 						JOptionPane.showMessageDialog(
 								PurchaseConfirmPopup.this,
 								"Payment is not large enough!",
 								"Purchase not completed",
 								JOptionPane.PLAIN_MESSAGE);
 					} else {
 						parent.submitPurchaseButtonClicked();
 						frame.dispose();
 					}
 				} catch (NumberFormatException ex) {
 					JOptionPane
 							.showMessageDialog(PurchaseConfirmPopup.this,
 									"Payment must be a number!",
 									"Purchase not completed",
 									JOptionPane.PLAIN_MESSAGE);
 				}
 			}
 		});
 		panel.add(okButton);
 
 		// Add cancel button
 		JButton cancelButton = new JButton("Cancel");
 		cancelButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				frame.dispose();
 			}
 		});
 		panel.add(cancelButton);
 
 		// Fill window
 		frame.add(panel);
 		frame.repaint();
 		frame.setVisible(true);
 	}
 }

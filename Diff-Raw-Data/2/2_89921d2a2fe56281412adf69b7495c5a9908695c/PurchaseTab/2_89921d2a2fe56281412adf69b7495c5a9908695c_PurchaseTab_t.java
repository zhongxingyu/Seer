 package ee.ut.math.tvt.salessystem.ui.tabs;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.text.DateFormat;
 import java.text.DecimalFormat;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 
 import net.miginfocom.swing.MigLayout;
 
 import org.apache.log4j.Logger;
 
 import ee.ut.math.tvt.salessystem.domain.controller.SalesDomainController;
 import ee.ut.math.tvt.salessystem.domain.data.AcceptedOrder;
 import ee.ut.math.tvt.salessystem.domain.exception.VerificationFailedException;
 import ee.ut.math.tvt.salessystem.ui.model.SalesSystemModel;
 import ee.ut.math.tvt.salessystem.ui.panels.PurchaseItemPanel;
 
 /**
  * Encapsulates everything that has to do with the purchase tab (the tab
  * labelled "Point-of-sale" in the menu).
  */
 public class PurchaseTab {
 
 	private static final Logger log = Logger.getLogger(PurchaseTab.class);
 
 	private final SalesDomainController domainController;
 
 	private JButton newPurchase;
 
 	private JButton submitPurchase;
 
 	private JButton cancelPurchase;
 
 	private JButton makePurchase;
 
 	private JButton returnToPurchase;
 
 	private JFrame confirmationFrame;
 
 	private JPanel confirmationPanel;
 
 	private PurchaseItemPanel purchasePane;
 
 	private final SalesSystemModel model;
 
 	public PurchaseTab(SalesDomainController controller, SalesSystemModel model) {
 		this.domainController = controller;
 		this.model = model;
 	}
 
 	/**
 	 * The purchase tab. Consists of the purchase menu, current purchase dialog
 	 * and shopping cart table.
 	 */
 	public Component draw() {
 		JPanel panel = new JPanel();
 		// Layout
 		panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
 		panel.setLayout(new GridBagLayout());
 		// Add the purchase menu
 		panel.add(getPurchaseMenuPane(), getConstraintsForPurchaseMenu());
 		// Add the main purchase-panel
 		purchasePane = new PurchaseItemPanel(model);
 		panel.add(purchasePane, getConstraintsForPurchasePanel());
 		return panel;
 	}
 
 	// The purchase menu. Contains buttons "New purchase", "Submit", "Cancel".
 	private Component getPurchaseMenuPane() {
 		JPanel panel = new JPanel();
 		// Initialize layout
 		panel.setLayout(new GridBagLayout());
 		GridBagConstraints gc = getConstraintsForMenuButtons();
 		// Initialize the buttons
 		newPurchase = createNewPurchaseButton();
 		submitPurchase = createConfirmButton();
 		cancelPurchase = createCancelButton();
 		// Add the buttons to the panel, using GridBagConstraints we defined above
 		panel.add(newPurchase, gc);
 		panel.add(submitPurchase, gc);
 		panel.add(cancelPurchase, gc);
 		return panel;
 	}
 
 	// Creates the button "New purchase"
 	private JButton createNewPurchaseButton() {
 		JButton b = new JButton("New purchase");
 		b.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				newPurchaseButtonClicked();
 			}
 		});
 		return b;
 	}
 
 	// Creates the "Confirm" button
 	private JButton createConfirmButton() {
 		JButton b = new JButton("Confirm");
 		b.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				submitPurchaseButtonClicked();
 			}
 		});
 		b.setEnabled(false);
 		return b;
 	}
 
 	// Creates the "Cancel" button
 	private JButton createCancelButton() {
 		JButton b = new JButton("Cancel");
 		b.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				cancelPurchaseButtonClicked();
 			}
 		});
 		b.setEnabled(false);
 		return b;
 	}
 
 	// Creates the "Make purchase" button
 	private JButton createMakePurchaseButton() {
 		JButton b = new JButton("Make purchase");
 		b.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				makePurchaseButtonClicked();
 			}
 		});
 		return b;
 	}
 
 	// Creates the "Return to purchase" button
 	private JButton createReturnToPurchaseButton() {
 		JButton b = new JButton("Cancel purchase");
 		b.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				returnToPurchaseButtonClicked();
 			}
 		});
 		return b;
 	}
 
 	// Purchase confirmation popup screen
 	private void popConfirmationBox() {
 		showConfirmationBox();
 
 		confirmationPanel = new JPanel(new MigLayout("nogrid","","fill, grow"));
 
 		confirmationFrame = new JFrame("Confirm");
 		confirmationFrame.setSize(new Dimension(320, 140));
 		confirmationFrame.setLocationRelativeTo(null);
 		confirmationFrame.setResizable(false);
 		confirmationFrame.add(confirmationPanel);
 		confirmationFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
 
 		// PopupBox for asking paymentAmount
 		String paymentAmount = (String) JOptionPane.showInputDialog(
 			confirmationFrame,
 			"Sum: " + model.getCurrentPurchaseTableModel().getPurchaseSum(),
 			"",
 			JOptionPane.PLAIN_MESSAGE,
 			null,
 			null,
 			"Payment amount");
 		
 		Double returnAmount = getReturnAmount(paymentAmount);
 
 		if (returnAmount < 0) {
 			JOptionPane.showMessageDialog(null, "The entered amount is too small", "Warning", JOptionPane.WARNING_MESSAGE);
 			continuePurchase();
 			return;
 		}
 		// Purchase sum textlabel
 		confirmationPanel.add(new JLabel("Sum: " + model.getCurrentPurchaseTableModel().getPurchaseSum()));
 		// Payment amount textlabel
 		confirmationPanel.add(new JLabel("Payment amount: " + paymentAmount), "newline");
 		// Change amount textlabel
 		confirmationPanel.add(new JLabel("Amount to return: " + returnAmount), "newline");
 
 		// Initializing make and cancel purchase buttons
 		makePurchase = createMakePurchaseButton();
 		returnToPurchase = createReturnToPurchaseButton();
 
 		// Adding the buttons
 		confirmationPanel.add(makePurchase, "newline, w 50%");
 		confirmationPanel.add(returnToPurchase, "w 50%");
 
 		confirmationFrame.setVisible(true);
 	}
 	
 	// Parses and formats the string into double
 	private double getReturnAmount(String paymentAmount) {
 		return Double.valueOf(new DecimalFormat("0.00").format(
 				Double.parseDouble(paymentAmount) - Double.parseDouble(model.getCurrentPurchaseTableModel().getPurchaseSum())
 				).replace(',', '.'));
 	}
 
 	/*
 	 * === Event handlers for the menu buttons (get executed when the buttons are
 	 * clicked)
 	 */
 	/** Event handler for the <code>new purchase</code> event. */
 	protected void newPurchaseButtonClicked() {
 		log.info("New sale process started");
 		try {
 			domainController.startNewPurchase();
 			startNewSale();
 			purchasePane.fillDialogFields();
 		}
 		catch (VerificationFailedException e1) {
 			log.error(e1.getMessage());
 		}
 	}
 
 	/** Event handler for the <code>cancel purchase</code> event. */
 	protected void cancelPurchaseButtonClicked() {
 		log.info("Sale cancelled");
 		try {
 			domainController.cancelCurrentPurchase();
 			endSale();
 			model.getCurrentPurchaseTableModel().clear();
 		}
 		catch (VerificationFailedException e1) {
 			log.error(e1.getMessage());
 		}
 	}
 
 	/** Event handler for the <code>submit purchase</code> event. */
 	protected void submitPurchaseButtonClicked() {
 		try {
 			popConfirmationBox();
		} catch (NullPointerException n1) {
			continuePurchase();			
 		} catch (Exception e1) {
 			JOptionPane.showMessageDialog(null, "Incorrect input, try again", "Warning", JOptionPane.WARNING_MESSAGE);
 			continuePurchase();
 		}
 	}
 
 	/** Event handler for the <code>make purchase</code> event. */
 	protected void makePurchaseButtonClicked() {
 		try {
 			log.debug("Contents of the current basket:\n" + model.getCurrentPurchaseTableModel());
 			domainController.submitCurrentPurchase(model.getCurrentPurchaseTableModel().getTableRows());
 			endSale();
 			savePurchase();
 			log.info("Sale complete");
 			model.getCurrentPurchaseTableModel().clear();
 		} catch (VerificationFailedException e1) {
 			log.error(e1.getMessage());
 		}
 	}
 
 	// Saving the current purchase
 	protected void savePurchase(){
 		AcceptedOrder order = new AcceptedOrder(
 			model.getCurrentPurchaseTableModel().getTableRows(),
 			((DateFormat)new SimpleDateFormat("yyyy/MM/dd")).format(Calendar.getInstance().getTime()),
 			((DateFormat)new SimpleDateFormat("HH:mm:ss")).format(Calendar.getInstance().getTime())
 			);
 		model.getHistoryTableModel().addOrder(order);
 		model.getWarehouseTableModel().decreaseItemsQuantity(order.getSoldItems());
 	}
 
 	/** Event handler for the <code>return to purchase</code> event. */
 	protected void returnToPurchaseButtonClicked() {
 		log.info("Returning to basket");
 		continuePurchase();
 	}
 
 	/*
 	 * === Helper methods that bring the whole purchase-tab to a certain state
 	 * when called.
 	 */
 	// switch UI to the state that allows to proceed with the purchase
 	private void startNewSale() {
 		purchasePane.reset();
 		purchasePane.setEnabled(true);
 		submitPurchase.setEnabled(true);
 		cancelPurchase.setEnabled(true);
 		newPurchase.setEnabled(false);
 	}
 
 	// switch UI to the state that allows to manage confirmation box
 	private void showConfirmationBox() {
 		purchasePane.setEnabled(false);
 		submitPurchase.setEnabled(false);
 		cancelPurchase.setEnabled(false);
 		newPurchase.setEnabled(false);
 	}
 
 	// switch UI to the state that allows to continue the purchase
 	private void continuePurchase() {
 		confirmationFrame.dispose();
 		purchasePane.setEnabled(true);
 		submitPurchase.setEnabled(true);
 		cancelPurchase.setEnabled(true);
 		newPurchase.setEnabled(false);
 
 	}
 
 	// switch UI to the state that allows to initiate new purchase
 	private void endSale() {
 		confirmationFrame.dispose();
 		purchasePane.reset();
 		cancelPurchase.setEnabled(false);
 		submitPurchase.setEnabled(false);
 		newPurchase.setEnabled(true);
 		purchasePane.setEnabled(false);
 	}
 
 	/*
 	 * === Next methods just create the layout constraints objects that control
 	 * the the layout of different elements in the purchase tab. These definitions
 	 * are brought out here to separate contents from layout, and keep the methods
 	 * that actually create the components shorter and cleaner.
 	 */
 	private GridBagConstraints getConstraintsForPurchaseMenu() {
 		GridBagConstraints gc = new GridBagConstraints();
 		gc.fill = GridBagConstraints.HORIZONTAL;
 		gc.anchor = GridBagConstraints.NORTH;
 		gc.gridwidth = GridBagConstraints.REMAINDER;
 		gc.weightx = 1.0d;
 		gc.weighty = 0d;
 		return gc;
 	}
 
 	private GridBagConstraints getConstraintsForPurchasePanel() {
 		GridBagConstraints gc = new GridBagConstraints();
 		gc.fill = GridBagConstraints.BOTH;
 		gc.anchor = GridBagConstraints.NORTH;
 		gc.gridwidth = GridBagConstraints.REMAINDER;
 		gc.weightx = 1.0d;
 		gc.weighty = 1.0;
 		return gc;
 	}
 
 	// The constraints that control the layout of the buttons in the purchase menu
 	private GridBagConstraints getConstraintsForMenuButtons() {
 		GridBagConstraints gc = new GridBagConstraints();
 		gc.weightx = 0;
 		gc.anchor = GridBagConstraints.CENTER;
 		gc.gridwidth = GridBagConstraints.RELATIVE;
 		return gc;
 	}
 }

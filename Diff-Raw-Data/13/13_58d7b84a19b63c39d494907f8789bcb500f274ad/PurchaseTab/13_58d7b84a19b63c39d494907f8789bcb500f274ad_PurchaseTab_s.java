 package ee.ut.math.tvt.salessystem.ui.tabs;
 
 import ee.ut.math.tvt.salessystem.domain.data.HistoryItem;
 import ee.ut.math.tvt.salessystem.domain.exception.VerificationFailedException;
 import ee.ut.math.tvt.salessystem.domain.controller.ConfirmationStatusEvent;
 import ee.ut.math.tvt.salessystem.domain.controller.SalesDomainController;
 import ee.ut.math.tvt.salessystem.ui.model.SalesSystemModel;
 import ee.ut.math.tvt.salessystem.ui.panels.PurchaseItemPanel;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.text.ParseException;
 
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JPanel;
 
 import org.apache.log4j.Logger;
 
 /**
  * Encapsulates everything that has to do with the purchase tab (the tab
  * labeled "Point-of-sale" in the menu).
  */
 public class PurchaseTab {
 
 	private static final Logger log = Logger.getLogger(PurchaseTab.class);
 
 	private final SalesDomainController domainController;
 
 	static JButton newPurchase;
 
 	static JButton submitPurchase;
 
 	static JButton cancelPurchase;
 
 	static PurchaseItemPanel purchasePane;
 
 	private static SalesSystemModel model;
 
 
 	public PurchaseTab(SalesDomainController controller,
 			SalesSystemModel model)
 	{
 		this.domainController = controller;
 		this.setModel(model);
 	}
 
 
 	/**
 	 * The purchase tab. Consists of the purchase menu, current purchase dialog and
 	 * shopping cart table.
 	 */
 	public Component draw() {
 		JPanel panel = new JPanel();
 
 		// Layout
 		panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
 		panel.setLayout(new GridBagLayout());
 
 		// Add the purchase menu
 		panel.add(getPurchaseMenuPane(), getConstraintsForPurchaseMenu());
 
 		// Add the main purchase-panel
 		purchasePane = new PurchaseItemPanel(getModel());
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
 
 	private double getTotalCost(){
 		double total=0;
 		for(int i=0;i<getModel().getCurrentPurchaseTableModel().getRowCount();i++){
 			total+=(double)getModel().getCurrentPurchaseTableModel().getValueAt(i, 4);
 		}
 		return total;
 	}
 
 
 	// Creates the button "New purchase"
 	private JButton createNewPurchaseButton() {
 		JButton b = new JButton("New purchase");
 		b.addActionListener(new ActionListener() {
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
 			public void actionPerformed(ActionEvent e) {
 				submitPurchaseButtonClicked();
 			}
 		});
 		b.setEnabled(false);
 		
 		//Listener for the confirm popup box events
 		domainController.addConfirmationStatusListener(new ConfirmationStatusEvent() {
 			public void SaleConfirmed(boolean success) {
 				if(success) {
 					model.getPurchaseHistoryTableModel().addItem(
 							new HistoryItem(model.getCurrentPurchaseTableModel().getTableRows()));
 					
 					model.getWarehouseTableModel().removeItems(model.getCurrentPurchaseTableModel().getTableRows());
 					
 					getModel().getCurrentPurchaseTableModel().clear();
 					endSale();
 				} else {
 					setEnabled(true);
 				}
 			}
 		});
 
 		return b;
 	}
 
 
 	// Creates the "Cancel" button
 	private JButton createCancelButton() {
 		JButton b = new JButton("Cancel");
 		b.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				cancelPurchaseButtonClicked();
 			}
 		});
 		b.setEnabled(false);
 
 		return b;
 	}
 
 
 	/* === Event handlers for the menu buttons
 	 *     (get executed when the buttons are clicked)
 	 */
 
 	/** Event handler for the <code>new purchase</code> event. */
 	protected void newPurchaseButtonClicked() {
 		log.info("New sale process started");
 		try {
 			domainController.startNewPurchase();
 			startNewSale();
 		} catch (VerificationFailedException e1) {
 			log.error(e1.getMessage());
 		}
 	}
 
 
 	/**  Event handler for the <code>cancel purchase</code> event. */
 	protected void cancelPurchaseButtonClicked() {
 		log.info("Sale cancelled");
 		try {
 			domainController.cancelCurrentPurchase();
 			endSale();
 			getModel().getCurrentPurchaseTableModel().clear();
 		} catch (VerificationFailedException e1) {
 			log.error(e1.getMessage());
 		}
 	}
 
 
 	/** Event handler for the <code>submit purchase</code> event. */
 	protected void submitPurchaseButtonClicked() {
		log.info("Sale complete");
 		try {
 			log.debug("Contents of the current basket:\n" + getModel().getCurrentPurchaseTableModel());
 
 			domainController.submitCurrentPurchase(
 					getModel().getCurrentPurchaseTableModel().getTableRows());
 			
 			setEnabled(false);
 		} catch (VerificationFailedException e) {
 			log.error(e.getLocalizedMessage());
 		}
 	}
 
 
 
 	/* === Helper methods that bring the whole purchase-tab to a certain state
 	 *     when called.
 	 */
 
 	// switch UI to the state that allows to proceed with the purchase
 	private void startNewSale() {
 		purchasePane.reset();
 
 		setEnabled(true);
 		newPurchase.setEnabled(false);
 	}
 
 	// switch UI to the state that allows to initiate new purchase
 	private void endSale() {
 		purchasePane.reset();
 		
 		setEnabled(false);
 		newPurchase.setEnabled(true);
 	}
 	
 	public void setEnabled(boolean e) {
 		cancelPurchase.setEnabled(e);
 		submitPurchase.setEnabled(e);
 		newPurchase.setEnabled(e);
 		purchasePane.setEnabled(e);
 	}
 
 
 
 
 	/* === Next methods just create the layout constraints objects that control the
 	 *     the layout of different elements in the purchase tab. These definitions are
 	 *     brought out here to separate contents from layout, and keep the methods
 	 *     that actually create the components shorter and cleaner.
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
 
 
 	public static SalesSystemModel getModel() {
 		return model;
 	}
 
 
 	public void setModel(SalesSystemModel model) {
 		this.model = model;
 	}
 
 }

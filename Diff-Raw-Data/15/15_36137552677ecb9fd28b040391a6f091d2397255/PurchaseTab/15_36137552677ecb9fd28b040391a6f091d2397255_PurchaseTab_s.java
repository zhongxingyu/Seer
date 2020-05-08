 package ee.ut.math.tvt.salessystem.ui.tabs;
 
 import ee.ut.math.tvt.salessystem.domain.exception.VerificationFailedException;
 import ee.ut.math.tvt.salessystem.domain.controller.SalesDomainController;
 import ee.ut.math.tvt.salessystem.ui.model.SalesSystemModel;
 import ee.ut.math.tvt.salessystem.ui.panels.PurchaseItemPanel;
 
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.EventQueue;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 
 import org.apache.log4j.Logger;
 
 /**
  * Encapsulates everything that has to do with the purchase tab (the tab
  * labelled "Point-of-sale" in the menu).
  */
 public class PurchaseTab {
 
   private static final Logger log = Logger.getLogger(PurchaseTab.class);
 
   private final SalesDomainController domainController;
 
   private static JButton newPurchase;
 
   private static JButton submitPurchase;
 
   private static JButton cancelPurchase;
 
   private static PurchaseItemPanel purchasePane;
 
   private static SalesSystemModel model;
   
   public static double round(double value, int places){
 	  if(places<0)throw new IllegalArgumentException();
 	  
 	  long factor = (long)Math.pow(10, places);
 	  value = value*factor;
 	  long tmp = Math.round(value);
 	  return (double) tmp/factor;
   }
 
   public PurchaseTab(SalesDomainController controller,
       SalesSystemModel model)
   {
     this.domainController = controller;
     this.model = model;
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
       model.getCurrentPurchaseTableModel().clear();
     } catch (VerificationFailedException e1) {
       log.error(e1.getMessage());
     }
   }
 
 
   /** Event handler for the <code>submit purchase</code> event. */
   protected void submitPurchaseButtonClicked() {
     log.info("Sale complete");
     try {
     	
       log.debug("Contents of the current basket:\n" + model.getCurrentPurchaseTableModel());
       createConfirmFrame();
       domainController.submitCurrentPurchase(
           model.getCurrentPurchaseTableModel().getTableRows()
       );
       endSale();
       model.getCurrentPurchaseTableModel().clear();
     } catch (VerificationFailedException e1) {
       log.error(e1.getMessage());
     }
   }
 public static void createConfirmFrame(){
 	EventQueue.invokeLater(new Runnable(){
 
 		@Override
 		public void run() {
 			int width = 250;
 			int height = 250;
 			
 			final JFrame frame = new JFrame("Confirmation");
 			GridBagLayout gbl = new GridBagLayout();
 			GridBagConstraints c = new  GridBagConstraints();
 			frame.setLayout(gbl);
 			double total = 0;
 			for(int i = 0;i < model.getCurrentPurchaseTableModel().getRowCount();i++){
 				total = total + (double)model.getCurrentPurchaseTableModel().getValueAt(i, 4);
 			}
 			final double total1 = total;
 			
 			JLabel sum = new JLabel("Total: ");
 			c.gridx = 0;
 			c.gridy = 0;
 			frame.add(sum,c);
 			
 			JLabel sum1 = new JLabel(total + "");
 			c.gridx = 1;
 			c.gridy = 0;
 			frame.add(sum1,c);
 			
 			JLabel cash = new JLabel("Cash: ");
 			c.gridx = 0;
 			c.gridy = 1;
 			frame.add(cash,c);
 			
 			final JTextField change1 = new JTextField();
 			c.gridx = 1;
 			c.gridy = 2;
 			change1.setEditable(false);
 			c.fill = GridBagConstraints.HORIZONTAL;
 			frame.add(change1,c);
 			
 			final JTextField cash1 = new JTextField();
 			c.gridx = 1;
 			c.gridy = 1;
 			c.fill = GridBagConstraints.HORIZONTAL;
 			cash1.setEditable(true);
 			cash1.getDocument().addDocumentListener(new DocumentListener(){
 				
 				public void insertUpdate(DocumentEvent e) {
 					warn();
 				}
 
 				@Override
 				public void removeUpdate(DocumentEvent e) {
 					warn();
 				}
 
 				@Override
 				public void changedUpdate(DocumentEvent e) {
 					warn();
 				}
 				
 				public void warn(){
					if(!cash1.getText().isEmpty()){
						if(Double.parseDouble(cash1.getText())<0){
							JOptionPane.showMessageDialog(null, "Please Insert a value larger than 0.","Error Message", JOptionPane.ERROR_MESSAGE);
						}
						
 						double morechange = Double.parseDouble(cash1.getText()) - total1;
 						morechange = round(morechange,2);
 						change1.setText(morechange +"");
 					}
 					
 				}
 				
 			});
 			frame.add(cash1,c);
 			
 			JLabel change = new JLabel("Change: ");
 			c.gridx = 0;
 			c.gridy = 2;
 			c.fill = GridBagConstraints.HORIZONTAL;
 			frame.add(change,c);
 			
 			JButton Submit = new JButton("Submit");
 			c.gridx = 0;
 			c.gridy = 3;
 			c.anchor = GridBagConstraints.PAGE_END;
 			Submit.addActionListener(new ActionListener(){
 
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					//return date of order, time of order and total price(total1)
 					
 				}
 				
 			});
 			frame.add(Submit,c);
 			
 			JButton Cancel  = new JButton("Cancel");
 			c.gridx = 1;
 			c.gridy = 3;
 			c.anchor = GridBagConstraints.PAGE_END;
 			Cancel.addActionListener(new ActionListener(){
 
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					frame.dispose();
 					
 					
 				}
 				
 			});
 			frame.add(Cancel,c);
 			
 			frame.setSize(width, height);
 			Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
 			frame.setLocation((screen.width - width) / 2,
 					(screen.height - height) / 2);
 			frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 			c.anchor = GridBagConstraints.PAGE_START;
 
 			
 			
 			
 			frame.setVisible(true);
 			
 		}
 		
 	});
 }
 
 
   /* === Helper methods that bring the whole purchase-tab to a certain state
    *     when called.
    */
 
   // switch UI to the state that allows to proceed with the purchase
   private void startNewSale() {
     purchasePane.reset();
 
     purchasePane.setEnabled(true);
     submitPurchase.setEnabled(true);
     cancelPurchase.setEnabled(true);
     newPurchase.setEnabled(false);
   }
 
   // switch UI to the state that allows to initiate new purchase
   private static void endSale() {
     purchasePane.reset();
 
     cancelPurchase.setEnabled(false);
     submitPurchase.setEnabled(false);
     newPurchase.setEnabled(true);
     purchasePane.setEnabled(false);
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
 
 }

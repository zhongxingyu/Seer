 package ee.ut.math.tvt.salessystem.ui.tabs;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Vector;
 
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 import javax.swing.table.JTableHeader;
 
 import org.apache.log4j.Logger;
 
 import ee.ut.math.tvt.salessystem.domain.controller.ConfirmationStatusEvent;
 import ee.ut.math.tvt.salessystem.domain.data.StockItem;
 import ee.ut.math.tvt.salessystem.ui.model.SalesSystemModel;
 
 
 public class StockTab implements SelectableTab {
 	private static final Logger log = Logger.getLogger(PurchaseTab.class);
 	private JButton addItem;
 	private SalesSystemModel model;
 	private JTable warehouseTable;
 	protected Vector<ConfirmationStatusEvent> _listeners;
 	
 	public void addConfirmationStatusListener(ConfirmationStatusEvent listener) {
 		if (_listeners == null) {
 			_listeners = new Vector<ConfirmationStatusEvent>();
 		}
 			
 		_listeners.addElement(listener);
 	}
 	
 	
 	protected void confirmTransaction(boolean success) {
 		if (_listeners != null) {
 			for(ConfirmationStatusEvent e : _listeners) {
 				e.SaleConfirmed(success);
 			}
 		}
 	}
 
 	public StockTab(SalesSystemModel model) {
 		this.model = model;
 	}
 
 	// warehouse stock tab - consists of a menu and a table
 	public Component draw() {
 		JPanel panel = new JPanel();
 		panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
 
 		GridBagLayout gb = new GridBagLayout();
 		GridBagConstraints gc = new GridBagConstraints();
 		panel.setLayout(gb);
 
 		gc.fill = GridBagConstraints.HORIZONTAL;
 		gc.anchor = GridBagConstraints.NORTH;
 		gc.gridwidth = GridBagConstraints.REMAINDER;
 		gc.weightx = 1.0d;
 		gc.weighty = 0d;
 
 		panel.add(drawStockMenuPane(), gc);
 
 		gc.weighty = 1.0;
 		gc.fill = GridBagConstraints.BOTH;
 		panel.add(drawStockMainPane(), gc);
 		return panel;
 	}
 
 	// warehouse menu
 	private Component drawStockMenuPane() {
 		JPanel panel = new JPanel();
 
 		GridBagConstraints gc = new GridBagConstraints();
 		GridBagLayout gb = new GridBagLayout();
 
 		panel.setLayout(gb);
 
 		gc.anchor = GridBagConstraints.NORTHWEST;
 		gc.weightx = 0;
 
 		addItem = createAddButton();
 		gc.gridwidth = GridBagConstraints.RELATIVE;
 		gc.weightx = 1.0;
 		panel.add(addItem, gc);
 
 		panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
 		return panel;
 	}
 
 	// Creates the button "Add"
 	private JButton createAddButton() {
 		JButton b = new JButton("Add");
 		b.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				AddButtonClicked();
 			}
 		});
 
 		return b;
 	}
 
 	/* === Event handlers for the menu buttons
 	 *     (get executed when the buttons are clicked)
 	 */
 
 
 	/** Event handler for the <code>Add</code> event. */
 	protected void AddButtonClicked() {
 		log.info("Adding new stock");
 
 		final JFrame panel = new JFrame("Add new product");
 
 		GridBagLayout gb = new GridBagLayout();    
 		panel.setLayout(gb);    
 
 		GridBagConstraints gc = new GridBagConstraints();
 		gc.fill = GridBagConstraints.HORIZONTAL;
 		gc.anchor = GridBagConstraints.NORTH;
 		gc.gridwidth = GridBagConstraints.REMAINDER;
 		gc.fill = GridBagConstraints.BOTH;    
 
 		JButton addNewProductButton = new JButton("Add new Product");
 		JButton cancelButton = new JButton("Cancel");
 		JLabel idLabel = new JLabel("Id:");
 		JLabel nameLabel = new JLabel("Name:");
 		final JLabel descriptionLabel = new JLabel("Description:");
 		JLabel priceLabel = new JLabel("Price:");
 		JLabel quantityLabel = new JLabel("Quantity:");
 
 		final JTextField idTextField = new IntegerField();
 		final JTextField nameTextField = new JTextField();
 		JTextField descriptionTextField = new JTextField();
 		final JTextField priceTextField = new DoubleField();
 		final JTextField quantityTextField = new IntegerField();
 
 		panel.add(idLabel,gc);
 		panel.add(idTextField,gc);
 
 		panel.add(nameLabel,gc);
 		panel.add(nameTextField,gc);
 
 		panel.add(descriptionLabel,gc);
 		panel.add(descriptionTextField,gc);
 
 		panel.add(priceLabel,gc);
 		panel.add(priceTextField,gc);
 
 		panel.add(quantityLabel,gc); 
 		panel.add(quantityTextField,gc);
 
 		panel.add(addNewProductButton,gc);
 		panel.add(cancelButton, gc);
 
 		panel.setSize(160, 270);
 		panel.setResizable(false);
 		panel.setVisible(true);
 		
 		
 //		//Install a listener and get this when the OK button is pressed
 //		final StockItem newProduct = new StockItem(Long.parseLong(idTextField.getText()),
 //				nameTextField.getText(), 
 //				descriptionLabel.getText(), 
 //				Double.parseDouble(priceTextField.getText()),
 //				Integer.parseInt(quantityTextField.getText())
 //				);
 		
 		
 		
 		addNewProductButton.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				final StockItem newProduct = new StockItem(Long.parseLong(idTextField.getText()),
 						nameTextField.getText(), 
 						descriptionLabel.getText(), 
 						Double.parseDouble(priceTextField.getText()),
 						Integer.parseInt(quantityTextField.getText())
 						);
 				model.getWarehouseTableModel().addItem(newProduct);
 				panel.dispose();
 				confirmTransaction(true);
 			}
 			
 		});
 		
 		
 		cancelButton.addActionListener(new ActionListener(){
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				panel.dispose();
 				confirmTransaction(false);
 				
 			}
 			
 		});
 
 	}
 
 	// table of the wareshouse stock
 	private Component drawStockMainPane() {
 		JPanel panel = new JPanel();
 
 		warehouseTable = new JTable(model.getWarehouseTableModel());
 		JTableHeader header = warehouseTable.getTableHeader();
 		header.setReorderingAllowed(false);
 
 		JScrollPane scrollPane = new JScrollPane(warehouseTable);
 
 		GridBagConstraints gc = new GridBagConstraints();
 		GridBagLayout gb = new GridBagLayout();
 		gc.fill = GridBagConstraints.BOTH;
 		gc.weightx = 1.0;
 		gc.weighty = 1.0;
 
 		panel.setLayout(gb);
 		panel.add(scrollPane, gc);
 
 		panel.setBorder(BorderFactory.createTitledBorder("Warehouse status"));
 		return panel;
 	}
 
 
 	@Override
 	public void onSelected() {
		warehouseTable.setModel(model.getWarehouseTableModel());
 	}
 }

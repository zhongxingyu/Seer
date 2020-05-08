 package ee.ut.math.tvt.salessystem.ui.tabs;
 
 import ee.ut.math.tvt.ateamplusone.Intro;
 import ee.ut.math.tvt.salessystem.domain.controller.SalesDomainController;
 import ee.ut.math.tvt.salessystem.domain.data.StockItem;
 import ee.ut.math.tvt.salessystem.ui.model.SalesSystemModel;
 import ee.ut.math.tvt.salessystem.ui.panels.PurchaseItemPanel;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.FlowLayout;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.GridLayout;
 import java.util.List;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 import javax.swing.SwingConstants;
 import javax.swing.WindowConstants;
 import javax.swing.table.JTableHeader;
 
 import org.apache.log4j.Logger;
 
 public class StockTab {
 
 	private JButton addItem;
 	private SalesSystemModel model;
 	private static final Logger log = Logger.getLogger(Intro.class);
 	final JFrame frame = new JFrame("Add a new item");
 	private final SalesDomainController domainController;
 	StockItem stockitem;
 	
 	public StockTab(SalesDomainController controller, SalesSystemModel model) {
 		this.domainController = controller;
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
 
 		addItem = new JButton("Add");
 		gc.gridwidth = GridBagConstraints.RELATIVE;
 		gc.weightx = 1.0;
 		panel.add(addItem, gc);
 		addItem.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				addItemEventHandler();
 			}
 		});
 
 		panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
 		return panel;
 	}
 
 	// table of the wareshouse stock
 	private Component drawStockMainPane() {
 		JPanel panel = new JPanel();
 
 		JTable table = new JTable(model.getWarehouseTableModel());
 
 		JTableHeader header = table.getTableHeader();
 		header.setReorderingAllowed(false);
 
 		JScrollPane scrollPane = new JScrollPane(table);
 
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
 
 	/**
 	 * Function to add a new item to the warehouse.
 	 * 
 	 * @author Juhan
 	 */
 	public void addItemEventHandler() {
 		frame.setLayout(new FlowLayout());
 		frame.setResizable(false);
 		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
 		frame.setSize(300, 170);
 		JPanel panel1 = new JPanel();
 		JPanel panel2 = new JPanel();
 		panel1.setLayout(new GridLayout(0, 2, 0, 5));
 		JButton confirmButton = new JButton("Confirm");
 		panel2.add(confirmButton);
 		frame.add(panel1);
 		frame.add(panel2);
 
 		final JTextField nameField = new JTextField(20);
 		final JTextField descField = new JTextField(20);
 		final JTextField priceField = new JTextField(20);
 		final JTextField quantityField = new JTextField(20);
 		JLabel nameLabel = new JLabel("Name:", SwingConstants.CENTER);
 		JLabel quantityLabel = new JLabel("Quantity:", SwingConstants.CENTER);
		JLabel priceLabel = new JLabel("Price:", SwingConstants.CENTER);
		JLabel descLabel = new JLabel("Description:", SwingConstants.CENTER);
 
 		panel1.add(nameLabel);
 		panel1.add(nameField);
 		panel1.add(quantityLabel);
 		panel1.add(quantityField);
		panel1.add(priceLabel);
		panel1.add(priceField);
		panel1.add(descLabel);
		panel1.add(descField);
 
 		frame.setLocationRelativeTo(null);
 		frame.setVisible(true);
 
 		confirmButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				try {
 					String name = nameField.getText();
 					int quantity = Integer.parseInt(quantityField.getText());
 
 					if (name.trim() == "") {
 						log.debug("You must enter a name for the new item.");
 					} else {
 
 						List<StockItem> list = model.getWarehouseTableModel()
 								.getTableRows();
 						boolean alreadyExists = false;
 						for (StockItem item : list) {
 							if (item.getName().equals(name)) {
 								alreadyExists = true;
 								stockitem=item;
 							}
 						}
 						if (!alreadyExists) {
 							String desc = descField.getText();
 							double price = Double.parseDouble(priceField.getText());
 							StockItem item = list.get(list.size() - 1);
 							long id = item.getId() + 1;
 							
 							StockItem newItem = new StockItem(id, name.trim(), desc, price,
 									quantity);
 							model.getWarehouseTableModel().addItem(newItem
 									);
 							
 							// Adding to database
 							domainController.addToWarehouse(newItem);
 							PurchaseItemPanel.fillItemNameBox();
 							frame.setVisible(false);
 							frame.dispose();
 						} else {
 							domainController.changeStockItemQuantity(stockitem, quantity);
 							PurchaseItemPanel.fillItemNameBox();
 							frame.setVisible(false);
 							frame.dispose();
 							log.debug("Quantity of "+name+" increased by "+quantity+".");
 						}
 					}
 				} catch (Exception e1) {
 					JOptionPane.showMessageDialog(frame,
 							("You have entered unsuitable attributes."),
 							"Unsuitable attributes", JOptionPane.ERROR_MESSAGE);
 					log.debug("You have entered unsuitable attributes.");
 					log.debug(e1);
 				}
 			}
 		});
 	}
 
 }

 package devopsdistilled.operp.client.items.views;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.inject.Inject;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 import net.miginfocom.swing.MigLayout;
 import devopsdistilled.operp.client.abstracts.SubTaskPane;
 import devopsdistilled.operp.client.items.controllers.EditItemPaneController;
 import devopsdistilled.operp.client.items.exceptions.ItemNameExistsException;
 import devopsdistilled.operp.client.items.exceptions.NullFieldException;
 import devopsdistilled.operp.client.items.exceptions.ProductBrandPairExistsException;
 import devopsdistilled.operp.client.items.models.observers.EditItemPaneModelObserver;
 import devopsdistilled.operp.server.data.entity.items.Brand;
 import devopsdistilled.operp.server.data.entity.items.Item;
 import devopsdistilled.operp.server.data.entity.items.Product;
 
 public class EditItemPane extends SubTaskPane implements
		EditItemPaneModelObserver {
 
 	@Inject
 	private EditItemPaneController controller;
 
 	@Inject
 	private ItemDetailsDialog itemDetailsDialog;
 
 	private final JPanel pane;
 	private final JTextField itemNameField;
 	private final JTextField priceField;
 	private final JComboBox<Brand> comboBrands;
 	private final JComboBox<Product> comboProducts;
 
 	public EditItemPane() {
 		pane = new JPanel();
 		pane.setLayout(new MigLayout("debug, flowy", "[][][grow][]",
 				"[][][][][]"));
 
 		JLabel lblProductName = new JLabel("Product Name");
 		pane.add(lblProductName, "cell 0 0,alignx trailing");
 
 		comboProducts = new JComboBox<Product>();
 		comboProducts.setSelectedItem(null);
 		pane.add(comboProducts, "flowx,cell 2 0,growx");
 
 		JLabel lblBrandName = new JLabel("Brand Name");
 		pane.add(lblBrandName, "cell 0 1,alignx trailing");
 
 		comboBrands = new JComboBox<Brand>();
 		comboBrands.setSelectedItem(null);
 		pane.add(comboBrands, "flowx,cell 2 1,growx");
 
 		JLabel lblItemId = new JLabel("Item Name");
 		pane.add(lblItemId, "cell 0 2,alignx trailing");
 
 		itemNameField = new JTextField();
 
 		pane.add(itemNameField, "cell 2 2,growx");
 		itemNameField.setColumns(10);
 
 		JLabel lblPrice = new JLabel("Price");
 		pane.add(lblPrice, "cell 0 3,alignx trailing");
 
 		priceField = new JTextField();
 		pane.add(priceField, "cell 2 3,growx");
 		priceField.setColumns(10);
 
 		JButton btnCancel = new JButton("Cancel");
 		btnCancel.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				getDialog().dispose();
 			}
 		});
 		pane.add(btnCancel, "flowx,cell 2 4");
 		JButton btnUpdate = new JButton("Update");
 		btnUpdate.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				Item item = new Item();
 				Brand brand = (Brand) comboBrands.getSelectedItem();
 				item.setBrand(brand);
 				Product product = (Product) comboProducts.getSelectedItem();
 				item.setProduct(product);
 				String itemName = itemNameField.getText().trim();
 				item.setItemName(itemName);
 				String itemPrice = priceField.getText().trim();
 
 				try {
 
 					Double price = Double.parseDouble(itemPrice);
 					item.setPrice(price);
 
 					try {
 						controller.validate(item);
 
 						// validated
 
 						item = controller.save(item);
 
 						getDialog().dispose();
 
 						itemDetailsDialog.show(item);
 					} catch (NullFieldException ex) {
 						JOptionPane.showMessageDialog(getPane(),
 								"Required field(s) are Null");
 					} catch (ProductBrandPairExistsException ex) {
 						JOptionPane
 								.showMessageDialog(getPane(),
 										"Item with selected pair of Product and Brand already exists.");
 					} catch (ItemNameExistsException ex) {
 						JOptionPane.showMessageDialog(getPane(),
 								"Item Name already exists");
 					}
 				} catch (NumberFormatException ex) {
 					JOptionPane.showMessageDialog(getPane(),
 							"Price must be a Numeric value");
 				}
 
 			}
 		});
 		pane.add(btnUpdate, "cell 2 4");
 
 		JButton btnNewProduct = new JButton("New Product");
 		pane.add(btnNewProduct, "cell 2 0");
 
 		JButton btnNewBrand = new JButton("New Brand");
 		btnNewBrand.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 			}
 		});
 		pane.add(btnNewBrand, "cell 2 1");
 	}
 
 	@Override
 	public JComponent getPane() {
 		return pane;
 	}
 
 	@Override
 	public void updateItem(Item item) {
 
 	}
 
 }

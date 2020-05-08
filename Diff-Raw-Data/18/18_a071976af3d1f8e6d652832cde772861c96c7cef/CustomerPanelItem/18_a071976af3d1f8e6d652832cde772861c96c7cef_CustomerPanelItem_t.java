 package ntnu.it1901.gruppe4.gui.ordergui;
 
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 import ntnu.it1901.gruppe4.db.Address;
 import ntnu.it1901.gruppe4.db.Customer;
 import ntnu.it1901.gruppe4.db.DataAPI;
 import ntnu.it1901.gruppe4.gui.Layout;
 import ntnu.it1901.gruppe4.gui.OrderSummary;
 
 /**
  * A panel used in the {@link CustomerPanel} to display details about a given {@link Customer} and functions to edit its information.
  * 
  * @author Leo
  */
 public class CustomerPanelItem extends JPanel {
 	private Customer customer;
 	private JLabel prefixes, info, errorMessage;
 	private JTextField nameInput, numberInput, addressInput, postNoInput;
 	private JButton save, edit;
 	private boolean beingEdited;
 	private OrderSummary orderSummary;
 
 	/**
 	 * Creates a new item containing a {@link Customer} object.
 	 * 
 	 * @param customer {@link Customer} object containing name and phone number.
 	 */
 	public CustomerPanelItem(Customer customer) {
 		this(customer, null);
 	}
 	
 	/**
 	 * Creates a new item containing a {@link Customer} object which will update the given {@link OrderSummary} when edited.
 	 * 
 	 * @param customer {@link Customer} object containing name and phone number.
 	 * @param orderSummary {@link OrderSummary} that will be updated when a user is edited.
 	 */
 	public CustomerPanelItem(Customer customer, OrderSummary orderSummary) {
 		this.customer = customer;
 		this.orderSummary = orderSummary;
 		nameInput = new JTextField();
 		numberInput = new JTextField();
 		addressInput = new JTextField();
 		postNoInput = new JTextField();
 		save = new JButton("Lagre");
 		edit = new JButton("Endre");
 		prefixes = new JLabel("<html><table>" +
 				"<tr><td>Navn:</td></tr>" +
 				"<tr><td>Nummer:</td></tr>" +
 				"<tr><td>Adresse:</td></tr>" +
 				"<tr><td>Postnummer:</td></tr>" +
 				"</table></html>");
 		info = new JLabel();
 		errorMessage = new JLabel(" ");
 
 		setBorder(Layout.customerItemPadding);
 		setLayout(new GridBagLayout());
 
 		nameInput.setFont(Layout.itemFont);
 		numberInput.setFont(Layout.itemFont);
 		addressInput.setFont(Layout.itemFont);
 		postNoInput.setFont(Layout.itemFont);
 		save.setFont(Layout.itemFont);
 		prefixes.setFont(Layout.itemFont);
 		info.setFont(Layout.itemFont);
 		errorMessage.setFont(Layout.errorFont);
 		errorMessage.setForeground(Layout.errorColor);
 
 		changeFunction(false);
 
 		save.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				Customer customer = CustomerPanelItem.this.customer;
 				String name = nameInput.getText();
 				String number = numberInput.getText();
				String addressLine = addressInput.getText();
 				String postNo = postNoInput.getText();
 				int newPostNo = 0;
 
 				if (name.isEmpty()) {
 					errorMessage.setText("Ugyldig navn");
 					return;
 				}
 				else if (number.isEmpty()) {
 					errorMessage.setText("Ugyldig nummer");
 					return;
 				}
				else if (addressLine.isEmpty()) {
 					errorMessage.setText("Ugyldig adresse");
 					return;
 				}
 				else if (postNo.isEmpty()) {
 					errorMessage.setText("Ugyldig postnummer");
 					return;
 				}
 
 				try {
 					newPostNo = Integer.parseInt(postNo);
 				}
 				catch (NumberFormatException nfe) {
 					errorMessage.setText("Ugyldig postnummer");
 					return;
 				}
 				customer.setName(name);
 				customer.setPhone(number);
				customer.save();
 				
				Address address = DataAPI.getAddresses(customer).get(0);
				address.setIdCustomer(customer);
				address.setAddressLine(addressLine);
				address.setPostalCode(newPostNo);
				address.save();
 				
 				changeFunction(false);
 				
 				if (CustomerPanelItem.this.orderSummary != null) {
 					CustomerPanelItem.this.orderSummary.update();
 				}
 			}
 		});
 
 		// To prevent this component's height from growing
 		setMaximumSize(new Dimension(Short.MAX_VALUE, getPreferredSize().height));
 	}
 
 	/**
 	 * Changes the function of this {@link MenuPanelItem} to either edit the description
 	 * of the contained {@link Order} or just showing it.
 	 * 
 	 * @param editing If the description of the current <code>Order</code> should be edited.
 	 */
 	public void changeFunction(boolean editing) {
 		beingEdited = editing;
 
 		if (editing) {
 			removeAll();
 
 			nameInput.setText(customer.getName());
 			numberInput.setText(customer.getPhone());
 			addressInput.setText(DataAPI.getAddresses(customer).get(0).getAddressLine());
 			postNoInput.setText(Integer.toString(DataAPI.getAddresses(customer).get(0).getPostalCode()));
 			errorMessage.setText(" ");
 
 			GridBagConstraints gbc = new GridBagConstraints();
 			gbc.fill = GridBagConstraints.HORIZONTAL;
 			gbc.anchor = GridBagConstraints.WEST;
 			gbc.weighty = 1;
 			gbc.weightx = gbc.gridy = gbc.gridx = 0;
 			gbc.gridheight = 4;
 			add(prefixes, gbc);
 
 			gbc.weightx = 1;
 			gbc.gridx++;
 			gbc.gridheight = 1;
 			add(nameInput, gbc);
 
 			gbc.gridy++;
 			add(numberInput, gbc);
 
 			gbc.gridy++;
 			add(addressInput, gbc);
 
 			gbc.gridy++;
 			add(postNoInput, gbc);
 
 			gbc.anchor = GridBagConstraints.SOUTHEAST;
 			gbc.fill = GridBagConstraints.NONE;
 			gbc.gridx++;
 			add(save, gbc);
 
 			//Place the error message directly below the text fields
 			gbc.anchor = GridBagConstraints.WEST;
 			gbc.gridy++;
 			gbc.gridx--;
 			add(errorMessage, gbc);
 
 			nameInput.grabFocus();
 
 			//Remove buttom padding from border
 			Insets borderInsets = Layout.customerItemPadding.getBorderInsets(this);
 			borderInsets.bottom = 0;
 			setBorder(BorderFactory.createEmptyBorder(borderInsets.top, borderInsets.left,
 					borderInsets.bottom, borderInsets.right));
 
 			setMaximumSize(new Dimension(Short.MAX_VALUE, getPreferredSize().height));
 		}
 		else {
 			removeAll();
 
 			info.setText("<html><table>" +
 					"<tr><td>" + customer.getName() + "</td></tr>" +
 					"<tr><td>" + customer.getPhone() + "</td></tr>" +
 					"<tr><td>" + DataAPI.getAddresses(customer).get(0).getAddressLine() + "</td></tr>" +
 					"<tr><td>" + DataAPI.getAddresses(customer).get(0).getPostalCode() + "</td></tr>" +
 					"</table></html>");
 			errorMessage.setText(" ");
 
 			GridBagConstraints gbc = new GridBagConstraints();
 			gbc.anchor = GridBagConstraints.WEST;
 			gbc.weighty = 1;
 			gbc.weightx = gbc.gridy = gbc.gridx = 0;
 			add(prefixes, gbc);
 
 			gbc.weightx = 1;
 			gbc.gridx++;
 			add(info, gbc);
 
 			//Add edit and delete buttons
 			gbc.anchor = GridBagConstraints.NORTHEAST;
 			gbc.weightx--;
 			gbc.gridx++;
 			add(edit, gbc);
 
 			//Set bottom padding to the height of the error message to avoid "shaking" when editing an item
 			Insets borderInsets = Layout.menuItemPadding.getBorderInsets(this);
 			borderInsets.bottom = errorMessage.getPreferredSize().height;
 			setBorder(BorderFactory.createEmptyBorder(borderInsets.top, borderInsets.left,
 					borderInsets.bottom, borderInsets.right));
 
 			setMaximumSize(new Dimension(Short.MAX_VALUE, getPreferredSize().height));
 		}
 		revalidate();
 		repaint();
 	}
 
 	/**
 	 * Whether or not the {@link Order} contained within this {@link CustomerPanelItem} is currently being edited.
 	 * 
 	 * @return True if the <code>Order</code> is being edited, false if it is not.
 	 */
 	public boolean isBeingEdited() {
 		return beingEdited;
 	}
 
 	/**
 	 * Adds a listener that will be called when the edit button in this {@link CustomerPanelItem} is clicked.
 	 * 
 	 * @param listener The listener that will be called when the item is clicked.
 	 */
 	public void addEditButtonListener(ActionListener listener) {
 		edit.addActionListener(listener);
 	}
 
 	/**
 	 * Returns the {@link Customer} object associated with this item.
 	 * 
 	 * @return a {@link Customer} object.
 	 */
 	public Customer getCustomer() {
 		return customer;
 	}
 }

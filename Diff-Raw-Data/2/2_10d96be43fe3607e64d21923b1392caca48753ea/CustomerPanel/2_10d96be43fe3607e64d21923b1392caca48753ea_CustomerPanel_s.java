 package ntnu.it1901.gruppe4.gui.ordergui;
 
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.Collection;
 
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 
 import ntnu.it1901.gruppe4.db.Address;
 import ntnu.it1901.gruppe4.db.Customer;
 import ntnu.it1901.gruppe4.db.DataAPI;
 import ntnu.it1901.gruppe4.gui.Layout;
 
 public class CustomerPanel extends JPanel {
 	CustomerList customerList;
 	
 	private SearchBox searchInput;
 	private SearchBox nameInput;
 	private SearchBox numberInput;
 	private SearchBox addressInput;
 	private SearchBox postNoInput;
 	private JButton newCustomer;
 	private JButton createCustomer;
 	private JButton cancel;
 	private JLabel errorMessage;
 	private OperatorOrderSummary currentOrder;
 
 	public class CustomerList extends JPanel {
 		/**
 		 * Creates a new CustomerList containing a list of {@link CustomerPanelItem CustomerPanelItems}.<br>
 		 * Only the CustomerPanel is allowed to do this.
 		 */
 		private CustomerList() {
 			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
 		}
 		
 		/**
 		 * Converts all customers in the given collection to {@link CustomerPanelItem},
 		 * adds them to the {@link CustomerList} and repaints the panel.
 		 *  
 		 * @param customers The customers to be added to the {@link CustomerList}.
 		 */
 		public void addCustomers(Collection<Customer> customers) {
 			int counter = 0;
 			removeAll();
 			
 			for (final Customer customer : customers) {
 				CustomerPanelItem item = new CustomerPanelItem(customer);
 				
 				//Fired whenever a customer panel item is clicked
 				item.addMouseListener(new MouseAdapter() {
 					@Override
 					public void mousePressed(MouseEvent e) {
 						CustomerPanel.this.currentOrder.setCustomer(customer);
 					}
 				});
 				
 				if (counter++ % 2 == 0) {
 					item.setBackground(Layout.bgColor1);
 				}
 				else {
 					item.setBackground(Layout.bgColor2);
 				}
 				add(item);
 			}
 			revalidate();
 			repaint();
 		}
 	}
 
 	public CustomerPanel(OperatorOrderSummary orderSummary) {
 		currentOrder = orderSummary;
 		customerList = new CustomerList();
 		searchInput = new SearchBox();
 		nameInput = new SearchBox();
 		numberInput = new SearchBox();
 		addressInput = new SearchBox();
 		postNoInput = new SearchBox();
 		newCustomer = new JButton("Ny kunde");
 		createCustomer = new JButton("Opprett ny kunde");
 		cancel = new JButton("Avbryt");
 		errorMessage = new JLabel();
 		
 		setBorder(Layout.panelPadding);
 		searchInput.setFont(Layout.searchBoxFont);
 		numberInput.setFont(Layout.searchBoxFont);
 		newCustomer.setAlignmentY(TOP_ALIGNMENT + 0.1f);
 		newCustomer.setFont(Layout.summaryTextFont);
 		createCustomer.setFont(Layout.summaryTextFont);
 		cancel.setFont(Layout.summaryTextFont);
 		errorMessage.setFont(Layout.errorFont);
 		errorMessage.setForeground(Layout.errorColor);
 		
 		//Set the initial mode of the panel to searching
 		changeMode(false);
 		
 		searchInput.addKeyListener(new KeyAdapter() {
 			/*keyReleased() used for searching as getText() does not return the
 			 *updated content of the search box when keyTyped() is called 
 			 */
 			@Override
 			public void keyReleased(KeyEvent e) {
 				SearchBox source = (SearchBox)e.getSource();
 				String boxContent = source.getText();
 				
 				//If the search box is empty, interrupt and restore the list of results
 				if (boxContent.equals("")) {
 					customerList.addCustomers(DataAPI.findCustomers(""));
 					return;
 				}
 
 				//Search for name and number using the DataAPI
 				customerList.addCustomers(DataAPI.findCustomers(boxContent));
 			}
 		});
 		
 		newCustomer.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				changeMode(true);
 			}
 		});
 		
 		createCustomer.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (nameInput.getText().isEmpty()) {
 					errorMessage.setText("Fyll inn navnet til kunden du ønsker å registrere");
 				}
 				else if (numberInput.getText().isEmpty()) {
 					errorMessage.setText("Fyll inn telefonnummeret til kunden du ønsker å registrere");
 				}
 				else if (addressInput.getText().isEmpty()) {
 					errorMessage.setText("Fyll inn adressen til kunden du ønsker å registrere");
 				}
 				else if (postNoInput.getText().isEmpty()) {
 					errorMessage.setText("Fyll inn postnummeret til kunden du ønsker å registrere");
 				}
 				else {
 					int postNo = 0;
 					
 					try {
 						postNo = Integer.parseInt(postNoInput.getText());
 					}
 					catch (NumberFormatException exception) {
 						errorMessage.setText("Fyll inn et gyldig postnummer til kunden du ønsker å registrere");
 						return;
 					}
 					Customer newCustomer = new Customer(nameInput.getText(), numberInput.getText());
 					DataAPI.saveCustomer(newCustomer);
 					
 					Address newAddress = new Address(newCustomer, addressInput.getText(), postNo);
 					DataAPI.saveAddress(newAddress);
 					
 					changeMode(false);
 				}
 			}
 		});
 		
 		cancel.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				changeMode(false);
 			}
 		});
 	}
 	
 	/**
 	 * Changes the layout of the {@link CustomerPanel} to either support adding new customers or searching
 	 * for existing ones.
 	 * 
 	 * @param addingCustomer True if the <code>CustomerPanel</code> is to be used for adding new customers,
 	 * false if it is to be used for searching for existing customers.
 	 */
 	private void changeMode(boolean addingCustomer) {
 		removeAll();
 		
 		if (addingCustomer) {
 			setLayout(new GridBagLayout());
 			GridBagConstraints gbc = new GridBagConstraints();
 			JLabel name = new JLabel("Navn: ");
 			JLabel phone = new JLabel("Telefonnummer: ");
 			JLabel address = new JLabel("Adresse: ");
 			JLabel postNo = new JLabel("Postnummer: ");
 			
 			name.setFont(Layout.summaryTextFont);
 			phone.setFont(Layout.summaryTextFont);
 			address.setFont(Layout.summaryTextFont);
 			postNo.setFont(Layout.summaryTextFont);
 			
 			gbc.fill = GridBagConstraints.HORIZONTAL;
 			gbc.anchor = GridBagConstraints.BASELINE_LEADING;
 			gbc.weightx = 0;
 			gbc.weighty = 1;
 			gbc.gridx = 0;
 			gbc.gridy = 0;
 			add(name, gbc);
 			
 			gbc.gridy++;
 			add(phone, gbc);
 			
 			gbc.gridy++;
 			add(address, gbc);
 			
 			gbc.gridy++;
 			add(postNo, gbc);
 			
 			gbc.gridwidth = 2;
 			gbc.gridx++;
 			gbc.gridy = 0;
 			gbc.weightx = 1;
 			add(nameInput, gbc);
 			
 			gbc.gridy++;
 			add(numberInput, gbc);
 
 			gbc.gridy++;
 			add(addressInput, gbc);
 			
 			gbc.gridy++;
 			add(postNoInput, gbc);
 
 			gbc.anchor = GridBagConstraints.NORTHWEST;
 			gbc.fill = GridBagConstraints.NONE;
 			gbc.gridy++;
 			add(cancel, gbc);
 			
 			gbc.anchor = GridBagConstraints.NORTHEAST;
 			gbc.gridx++;
 			add(createCustomer, gbc);
 			
 			gbc.anchor = GridBagConstraints.NORTHWEST;
 			gbc.weighty = Layout.newCustomerDensity;
 			gbc.gridy++;
 			gbc.gridx--;
 			add(errorMessage, gbc);
 			
 			nameInput.grabFocus();
 		}
 		else {
 			//Reload all customers from the database and add them to the list
 			customerList.addCustomers(DataAPI.findCustomers(""));
 			
 			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			errorMessage.setText("");
 			
 			//Box used to place the new customer button to the right of the name input box
 			//with three spaces between them
 			Box horizontalBox = Box.createHorizontalBox();
 			horizontalBox.add(searchInput);
 			horizontalBox.add(new JLabel("  "));
 			horizontalBox.add(newCustomer);
 			add(horizontalBox);
 			add(Box.createVerticalStrut(Layout.spaceAfterSearchBox));
 			
 			//Wrap the customer list inside a JScrollPane
 			JScrollPane sp = new JScrollPane(customerList);
 			sp.setBorder(null);
 			add(sp);
 			
 			searchInput.grabFocus();
 		}
 		revalidate();
 		repaint();
 	}
 	
 	@Override
 	public void grabFocus() {
 		searchInput.grabFocus();
 	}
 }

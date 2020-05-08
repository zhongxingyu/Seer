 package viewPanels;
 
 import java.awt.Color;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.ArrayList;
 import java.util.Observable;
 
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.ListSelectionModel;
 import javax.swing.RowFilter;
 import javax.swing.border.TitledBorder;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.table.TableRowSorter;
 
 import localization.Messages;
 
 import components.IconCellRenderer;
 import components.LibraryExcption;
 import components.MySearchField;
 
 import settings.Icons;
 import viewModels.CustomerTableModel;
 import views.CustomerEditor;
 import views.CustomerViewer;
 import domain.Customer;
 import domain.Library;
 
 public class CustomerPanel extends AbstractPanel {
 	private static final long serialVersionUID = 6034035113335278353L;
 	private static final Color background_Color = new Color(226, 226, 226);
 	private Library library;
 	private JTable customer_table;
 	private JLabel displayNrCustomer;
 	private java.util.List<RowFilter<Object, Object>> filters_customer = new ArrayList<RowFilter<Object, Object>>(
 			3);
 	private JButton btnDisplayCustomer;
 
 	public CustomerPanel(Library library) {
 		super();
 		this.library = library;
 		library.addObserver(this);
 		initialize();
 	}
 
 	private void initialize() {
 		GridBagLayout gbl_customerTab = new GridBagLayout();
 		gbl_customerTab.columnWidths = new int[] { 0, 0 };
 		gbl_customerTab.rowHeights = new int[] { 48, 136, 0 };
 		gbl_customerTab.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
 		gbl_customerTab.rowWeights = new double[] { 0.0, 1.0,
 				Double.MIN_VALUE };
 		setLayout(gbl_customerTab);
 
 		JPanel panelCustomerStats = new JPanel();
 		panelCustomerStats.setBackground(background_Color);
 		panelCustomerStats.setBorder(new TitledBorder(null,
 				"Customer Statistics", TitledBorder.LEADING, TitledBorder.TOP,
 				null, null));
 		GridBagConstraints gbc_panelCustomerStats = new GridBagConstraints();
 		gbc_panelCustomerStats.insets = new Insets(0, 0, 5, 0);
 		gbc_panelCustomerStats.fill = GridBagConstraints.BOTH;
 		gbc_panelCustomerStats.gridx = 0;
 		gbc_panelCustomerStats.gridy = 0;
 		add(panelCustomerStats, gbc_panelCustomerStats);
 		GridBagLayout gbl_panelCustomerStats = new GridBagLayout();
 		gbl_panelCustomerStats.columnWidths = new int[] { 76, 91, 0, 0, 0 };
 		gbl_panelCustomerStats.rowHeights = new int[] { 16, 0 };
 		gbl_panelCustomerStats.columnWeights = new double[] { 0.0, 0.0, 0.0,
 				0.0, Double.MIN_VALUE };
 		gbl_panelCustomerStats.rowWeights = new double[] { 0.0,
 				Double.MIN_VALUE };
 		panelCustomerStats.setLayout(gbl_panelCustomerStats);
 
 		JLabel lblNrCustomers = new JLabel(Messages.getString("CustomerPanel.lblNumberOfCustomersText.title"));
 
 		GridBagConstraints gbc_lblNrCustomers = new GridBagConstraints();
 		gbc_lblNrCustomers.insets = new Insets(0, 0, 0, 5);
 		gbc_lblNrCustomers.anchor = GridBagConstraints.NORTHWEST;
 		gbc_lblNrCustomers.gridx = 0;
 		gbc_lblNrCustomers.gridy = 0;
 		panelCustomerStats.add(lblNrCustomers, gbc_lblNrCustomers);
 
 		displayNrCustomer = new JLabel(library.getCustomers().size() + "");
 		GridBagConstraints gbc_displayNrCustomer = new GridBagConstraints();
 		gbc_displayNrCustomer.insets = new Insets(0, 0, 0, 5);
 		gbc_displayNrCustomer.anchor = GridBagConstraints.WEST;
 		gbc_displayNrCustomer.gridx = 1;
 		gbc_displayNrCustomer.gridy = 0;
 		panelCustomerStats.add(displayNrCustomer, gbc_displayNrCustomer);
 
 		JPanel panel_1 = new JPanel();
 		panel_1.setBackground(background_Color);
 		panel_1.setBorder(null);
 		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
 		gbc_panel_1.fill = GridBagConstraints.BOTH;
 		gbc_panel_1.gridx = 0;
 		gbc_panel_1.gridy = 1;
 		add(panel_1, gbc_panel_1);
 		GridBagLayout gbl_panel_1 = new GridBagLayout();
 		gbl_panel_1.columnWidths = new int[] { 0, 0, 200, 0, 0 };
 		gbl_panel_1.rowHeights = new int[] { 0, 0, 0 };
 		gbl_panel_1.columnWeights = new double[] { 0.0, 1.0, 0.0, 0.0,
 				Double.MIN_VALUE };
 		gbl_panel_1.rowWeights = new double[] { 1.0, 0.0, Double.MIN_VALUE };
 		panel_1.setLayout(gbl_panel_1);
 
 		JScrollPane scrollPane_1 = new JScrollPane();
 		GridBagConstraints gbc_scrollPane_1 = new GridBagConstraints();
 		gbc_scrollPane_1.gridwidth = 4;
 		gbc_scrollPane_1.fill = GridBagConstraints.BOTH;
 		gbc_scrollPane_1.insets = new Insets(0, 0, 5, 0);
 		gbc_scrollPane_1.gridx = 0;
 		gbc_scrollPane_1.gridy = 0;
 		panel_1.add(scrollPane_1, gbc_scrollPane_1);
 
 		customer_table = new JTable();
 		customer_table.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				if (e.getClickCount() == 2) {
 					openEditCustomerWindow();
 				}
 			}
 
 		});
 		customer_table.addKeyListener(new KeyAdapter() {
 			@Override
 			public void keyPressed(KeyEvent arg0) {
 				if(arg0.getKeyCode()==KeyEvent.VK_ENTER){
 					arg0.consume();
 					openEditCustomerWindow();
 				}
 			
 			}
 		});
 		customer_table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		ListSelectionModel listSelectionModel = customer_table.getSelectionModel();
 		listSelectionModel.addListSelectionListener(new ListSelectionListener() {
 			
 			@Override
 			public void valueChanged(ListSelectionEvent e) {
 				if (customer_table.getSelectedRowCount()==1)
 					btnDisplayCustomer.setEnabled(true);
 				else
 					btnDisplayCustomer.setEnabled(false);
 				
 			}
 		});
 		scrollPane_1.setViewportView(customer_table);
 		setModel();
 		customer_table.setSurrendersFocusOnKeystroke(false);
 		@SuppressWarnings({ "unchecked", "rawtypes" })
 		TableRowSorter<CustomerTableModel> customerSorter = new TableRowSorter(customer_table.getModel());
 		customer_table.setRowSorter(customerSorter);
 		customerSorter.setSortsOnUpdates(true);
 
 		btnDisplayCustomer = new JButton(Messages.getString("CustomersAddView.CustomersAddViewCenterTitle.title"),Icons.IconEnum.DETAIL.getIcon(24));
 		btnDisplayCustomer.setEnabled(false);
 		btnDisplayCustomer.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				
 					openEditCustomerWindow();
 				
 			}
 		});
 
 		
 
 		searchfield = new MySearchField(customer_table, 1, filters_customer);
 		GridBagConstraints gbc_txtSearchfield_1 = new GridBagConstraints();
 		gbc_txtSearchfield_1.insets = new Insets(0, 0, 0, 5);
 		gbc_txtSearchfield_1.fill = GridBagConstraints.HORIZONTAL;
 		gbc_txtSearchfield_1.gridx = 1;
 		gbc_txtSearchfield_1.gridy = 1;
 		panel_1.add(searchfield, gbc_txtSearchfield_1);
 		searchfield.setColumns(10);
 		GridBagConstraints gbc_btnDisplaySelected_1 = new GridBagConstraints();
 		gbc_btnDisplaySelected_1.anchor = GridBagConstraints.EAST;
 		gbc_btnDisplaySelected_1.insets = new Insets(0, 0, 0, 5);
 		gbc_btnDisplaySelected_1.gridx = 2;
 		gbc_btnDisplaySelected_1.gridy = 1;
 		panel_1.add(btnDisplayCustomer, gbc_btnDisplaySelected_1);
 
 		JButton btnNewCustomer = new JButton(Messages.getString("CustomersAddView.CustomersAddViewTabTitle.title"),Icons.IconEnum.ADD.getIcon(24));
 		btnNewCustomer.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 //				newCustomerWindow = new NewCustomer(new Customer("Last",
 //						"First"), library);
 //				newCustomerWindow.setVisible();
 				newCustomer();
 			}
 		});
 		GridBagConstraints gbc_btnNewCustomer = new GridBagConstraints();
 		gbc_btnNewCustomer.gridx = 3;
 		gbc_btnNewCustomer.gridy = 1;
 		panel_1.add(btnNewCustomer, gbc_btnNewCustomer);
 
 	}
 
 	/**
 	 * 
 	 */
 	private void setModel() {
 		customer_table.setModel(new CustomerTableModel(library));
 		customer_table.getColumnModel().getColumn(0).setMaxWidth(90);
 		customer_table.getColumnModel().getColumn(0).setPreferredWidth(90);
		customer_table.getColumnModel().getColumn(1).setMaxWidth(40);
 		customer_table.getColumnModel().getColumn(2).setPreferredWidth(100);
 		customer_table.getColumnModel().getColumn(3).setPreferredWidth(100);
 		customer_table.getColumnModel().getColumn(4).setPreferredWidth(100);
 		customer_table.getColumnModel().getColumn(5).setPreferredWidth(1);
 		customer_table.getColumnModel().getColumn(6).setPreferredWidth(100);
 		customer_table.getColumnModel().getColumn(0).setCellRenderer(new IconCellRenderer());
 	}
 
 	public void updateFields() {
 		displayNrCustomer.setText(library.getCustomers().size() + "");
 	}
 
 	@Override
 	public void update(Observable arg0, Object arg1) {
 		updateFields();
 	}
 
 	private Customer getSelectedCustomer() {
 		return library.getCustomers().get(
 				customer_table.convertRowIndexToModel(customer_table
 						.getSelectedRow()));
 	}
 
 	private void openEditCustomerWindow() {
 		try {
 			new CustomerViewer(library,getSelectedCustomer());
 		} catch (LibraryExcption e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	private void newCustomer(){
 		CustomerEditor ce = new CustomerEditor(library, this);
 		
 		if (!ce.isValid()){
 			System.out.println("Cancelled");
 		}
 		else
 		try {
 			new CustomerViewer(library, ce.getCustomer());
 		} catch (LibraryExcption e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 }

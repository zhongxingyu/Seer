 package application;
 
 
 import javax.swing.JFrame;
 import javax.swing.JTabbedPane;
 import javax.swing.JPanel;
 import javax.swing.RowFilter;
 
 import java.awt.GridBagLayout;
 import java.awt.GridBagConstraints;
 
 import javax.swing.border.TitledBorder;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.table.TableRowSorter;
 import javax.swing.JLabel;
 import java.awt.Insets;
 import javax.swing.JButton;
 import domain.Library;
 import domain.TableModel;
 
 import javax.swing.JTable;
 import javax.swing.JScrollPane;
 import java.awt.Dimension;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.util.Observable;
 import java.util.Observer;
 import javax.swing.JTextField;
 import java.awt.Font;
 import java.awt.Color;
 import java.awt.event.FocusAdapter;
 import java.awt.event.FocusEvent;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import javax.swing.JCheckBox;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
 
 class BookMaster implements Observer{
 
 
 	private static final Color background_Color = new Color(226, 226, 226);
 	private static final int minimum_window_height = 700;
 	private static final int minimum_window_witdh = 600;
 	private static final String label_NUMBER_OF_TITELS = "Number of Titels: ";
 	private static final String borderLabel_INVENTORY_STATISTICS = "Inventory Statistics";
 	private static final String TabLabel_LENDING = "Lending";
 	private static final String bookTabLabel = "Books";
 	private JFrame frame;
 	private JTable table;
 	private JLabel display_number_of_titles;
 	private JLabel display_number_of_books;
 	JLabel lblSelectednumber = new JLabel("0");
 	private Library library;
 	private BookDetail detailwindow = new BookDetail();
	private NewCustomer newCustomerWindow = new NewCustomer();
 	private JTextField txtSearch;
 	private JTextField txtSearchfield;
 	private JTable table_1;
 	private JTextField txtSearchfield_1;
 	private JTable table_2;
 	public BookMaster(Library library) {
 		this.library = library;
 		initialize();
 		library.addObserver(this);
 		frame.setVisible(true);
 	}
 	
 	private void initialize() {
 		frame = new JFrame();
 		frame.setMinimumSize(new Dimension(minimum_window_witdh, minimum_window_height));
 		frame.setBounds(100, 100, 567, 610);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		GridBagLayout gridBagLayout = new GridBagLayout();
 		gridBagLayout.columnWidths = new int[]{567, 0};
 		gridBagLayout.rowHeights = new int[]{588, 0};
 		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
 		gridBagLayout.rowWeights = new double[]{1.0, Double.MIN_VALUE};
 		frame.getContentPane().setLayout(gridBagLayout);
 		
 		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
 		GridBagConstraints gbc_tabbedPane = new GridBagConstraints();
 		gbc_tabbedPane.fill = GridBagConstraints.BOTH;
 		gbc_tabbedPane.gridx = 0;
 		gbc_tabbedPane.gridy = 0;
 		frame.getContentPane().add(tabbedPane, gbc_tabbedPane);
 		
 		JPanel bookTab = new JPanel();
 		tabbedPane.addTab(bookTabLabel, null, bookTab, null);
 		GridBagLayout gbl_bookTab = new GridBagLayout();
 		gbl_bookTab.columnWidths = new int[]{0, 0};
 		gbl_bookTab.rowHeights = new int[]{48, 0, 0};
 		gbl_bookTab.columnWeights = new double[]{1.0, Double.MIN_VALUE};
 		gbl_bookTab.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
 		bookTab.setLayout(gbl_bookTab);
 		
 		JPanel panelInventoryStatistik = new JPanel();
 		panelInventoryStatistik.setBackground(background_Color);
 		panelInventoryStatistik.setBorder(new TitledBorder(null, borderLabel_INVENTORY_STATISTICS, TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		GridBagConstraints gbc_panelLendingStatistik = new GridBagConstraints();
 		gbc_panelLendingStatistik.insets = new Insets(0, 0, 5, 0);
 		gbc_panelLendingStatistik.fill = GridBagConstraints.BOTH;
 		gbc_panelLendingStatistik.gridx = 0;
 		gbc_panelLendingStatistik.gridy = 0;
 		bookTab.add(panelInventoryStatistik, gbc_panelLendingStatistik);
 		GridBagLayout gbl_panelInventoryStatistik = new GridBagLayout();
 		gbl_panelInventoryStatistik.columnWidths = new int[]{0, 0, 50, 0, 0, 0};
 		gbl_panelInventoryStatistik.rowHeights = new int[]{0, 0};
 		gbl_panelInventoryStatistik.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
 		gbl_panelInventoryStatistik.rowWeights = new double[]{0.0, Double.MIN_VALUE};
 		panelInventoryStatistik.setLayout(gbl_panelInventoryStatistik);
 		
 		JLabel lblNumberOfTitles = new JLabel(label_NUMBER_OF_TITELS);
 		GridBagConstraints gbc_lblNumberOfBooks = new GridBagConstraints();
 		gbc_lblNumberOfBooks.insets = new Insets(0, 0, 0, 5);
 		gbc_lblNumberOfBooks.gridx = 0;
 		gbc_lblNumberOfBooks.gridy = 0;
 		panelInventoryStatistik.add(lblNumberOfTitles, gbc_lblNumberOfBooks);
 		
 		display_number_of_titles = new JLabel(library.getBooks().size() + "");
 		GridBagConstraints gbc_lblNumber_2 = new GridBagConstraints();
 		gbc_lblNumber_2.insets = new Insets(0, 0, 0, 5);
 		gbc_lblNumber_2.gridx = 1;
 		gbc_lblNumber_2.gridy = 0;
 		panelInventoryStatistik.add(display_number_of_titles, gbc_lblNumber_2);
 		
 		JLabel lblNumberOfBooks_1 = new JLabel("Number of Books");
 		GridBagConstraints gbc_lblNumberOfBooks_1 = new GridBagConstraints();
 		gbc_lblNumberOfBooks_1.insets = new Insets(0, 0, 0, 5);
 		gbc_lblNumberOfBooks_1.gridx = 3;
 		gbc_lblNumberOfBooks_1.gridy = 0;
 		panelInventoryStatistik.add(lblNumberOfBooks_1, gbc_lblNumberOfBooks_1);
 		
 		display_number_of_books = new JLabel(library.getCopies().size() + "");
 		GridBagConstraints gbc_lblNumber_1 = new GridBagConstraints();
 		gbc_lblNumber_1.gridx = 4;
 		gbc_lblNumber_1.gridy = 0;
 		panelInventoryStatistik.add(display_number_of_books, gbc_lblNumber_1);
 		
 		JPanel panelBookInventory = new JPanel();
 		panelBookInventory.setBackground(background_Color);
 		GridBagConstraints gbc_panel2 = new GridBagConstraints();
 		gbc_panel2.fill = GridBagConstraints.BOTH;
 		gbc_panel2.gridx = 0;
 		gbc_panel2.gridy = 1;
 		bookTab.add(panelBookInventory, gbc_panel2);
 		GridBagLayout gbl_panelBookInventory = new GridBagLayout();
 		gbl_panelBookInventory.columnWidths = new int[]{0, 0, 50, 0, 0, 0};
 		gbl_panelBookInventory.rowHeights = new int[]{0, 0, 0};
 		gbl_panelBookInventory.columnWeights = new double[]{0.0, 0.0, 1.0, 1.0, 0.0, Double.MIN_VALUE};
 		gbl_panelBookInventory.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
 		panelBookInventory.setLayout(gbl_panelBookInventory);
 		
 		JScrollPane scrollPane = new JScrollPane();
 		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
 		gbc_scrollPane.fill = GridBagConstraints.BOTH;
 		gbc_scrollPane.gridwidth = 5;
 		gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
 		gbc_scrollPane.gridx = 0;
 		gbc_scrollPane.gridy = 0;
 		panelBookInventory.add(scrollPane, gbc_scrollPane);
 		
 		//---------JTable---------------------
 		table = new JTable();
 		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
 
 		    public void valueChanged(ListSelectionEvent lse) {
 		        if (!lse.getValueIsAdjusting()) {
 		        		lblSelectednumber.setText(table.getSelectedRows().length+"");
 		        }
 		    }
 		});
 		scrollPane.setViewportView(table);
 		TableModel tableModel = new TableModel(library.getBooks());
 		table.setModel(tableModel);
 		JLabel lblSelected = new JLabel("Selected: ");
 		GridBagConstraints gbc_lblSelected = new GridBagConstraints();
 		gbc_lblSelected.anchor = GridBagConstraints.WEST;
 		gbc_lblSelected.insets = new Insets(0, 0, 0, 5);
 		gbc_lblSelected.gridx = 0;
 		gbc_lblSelected.gridy = 1;
 		panelBookInventory.add(lblSelected, gbc_lblSelected);
 		final TableRowSorter<TableModel> sorter = new TableRowSorter(table.getModel()); 
 		table.setRowSorter(sorter);
 		//---------JTable---------------------
 				
 		GridBagConstraints gbc_lblSelectednumber = new GridBagConstraints();
 		gbc_lblSelectednumber.anchor = GridBagConstraints.EAST;
 		gbc_lblSelectednumber.insets = new Insets(0, 0, 0, 5);
 		gbc_lblSelectednumber.gridx = 1;
 		gbc_lblSelectednumber.gridy = 1;
 		panelBookInventory.add(lblSelectednumber, gbc_lblSelectednumber);
 		
 		JButton btnDisplaySelected = new JButton("Display Selected");
 
 		btnDisplaySelected.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				detailwindow.setVisible();
 				detailwindow.setBook(library.getBooks().get(table.convertRowIndexToModel(table.getSelectedRow())));
 			}
 		});
 		//------------Search Field --------------
 		txtSearch = new JTextField();
 		txtSearch.addKeyListener(new KeyAdapter() {
 			@Override
 			public void keyPressed(KeyEvent e) {
 				//newFilter();
 			}
 
 			@Override
 			public void keyReleased(KeyEvent e) {
 				newFilter();
 			}
 			
 			private void newFilter() {
 				RowFilter<TableModel, Object> rf = null;
 				//If current expression doesn't parse, don't update.
 				try {
 					rf = RowFilter.regexFilter(txtSearch.getText(), 0);
 				} catch (java.util.regex.PatternSyntaxException e) {
 					return;
 				}
 				sorter.setRowFilter(rf);
 			}
 		});
 		txtSearch.addFocusListener(new FocusAdapter() {
 			@Override
 			public void focusGained(FocusEvent e) {
 				txtSearch.setText("");
 				txtSearch.setForeground(Color.black);
 				txtSearch.setFont(new Font("Lucida Grande", Font.PLAIN, 13));
 			}
 			@Override
 			public void focusLost(FocusEvent e) {
 				if(txtSearch.getText().isEmpty()){
 					reset_search();
 				}
 			}
 		});
 		reset_search();
 		GridBagConstraints gbc_txtSearch = new GridBagConstraints();
 		gbc_txtSearch.fill = GridBagConstraints.HORIZONTAL;
 		gbc_txtSearch.gridx = 2;
 		gbc_txtSearch.gridy = 1;
 		panelBookInventory.add(txtSearch, gbc_txtSearch);
 		txtSearch.setColumns(10);
 		GridBagConstraints gbc_btnDisplaySelected = new GridBagConstraints();
 		gbc_btnDisplaySelected.anchor = GridBagConstraints.EAST;
 		gbc_btnDisplaySelected.insets = new Insets(0, 0, 0, 5);
 		gbc_btnDisplaySelected.gridx = 3;
 		gbc_btnDisplaySelected.gridy = 1;
 		panelBookInventory.add(btnDisplaySelected, gbc_btnDisplaySelected);
 		//------------Search Field --------------
 
 		//------------Button Add new Book--------		
 		JButton btnAddNewBook = new JButton("Add new Book");
 		GridBagConstraints gbc_btnAddNewBook = new GridBagConstraints();
 		gbc_btnAddNewBook.gridx = 4;
 		gbc_btnAddNewBook.gridy = 1;
 		panelBookInventory.add(btnAddNewBook, gbc_btnAddNewBook);
 		//------------Button Add new Book--------
 		
 		JPanel lendingTab = new JPanel();
 		lendingTab.setBackground(background_Color);
 		tabbedPane.addTab(TabLabel_LENDING, null, lendingTab, null);
 		GridBagLayout gbl_lendingTab = new GridBagLayout();
 		gbl_lendingTab.columnWidths = new int[]{0, 0};
 		gbl_lendingTab.rowHeights = new int[]{48, 0, 0};
 		gbl_lendingTab.columnWeights = new double[]{1.0, Double.MIN_VALUE};
 		gbl_lendingTab.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
 		lendingTab.setLayout(gbl_lendingTab);
 		
 		JPanel panelrentstatistics = new JPanel();
 		panelrentstatistics.setBorder(new TitledBorder(null, "Rent Statistics", TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		GridBagConstraints gbc_panelrentstatistics = new GridBagConstraints();
 		gbc_panelrentstatistics.insets = new Insets(0, 0, 5, 0);
 		gbc_panelrentstatistics.fill = GridBagConstraints.BOTH;
 		gbc_panelrentstatistics.gridx = 0;
 		gbc_panelrentstatistics.gridy = 0;
 		lendingTab.add(panelrentstatistics, gbc_panelrentstatistics);
 		GridBagLayout gbl_panelrentstatistics = new GridBagLayout();
 		gbl_panelrentstatistics.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
 		gbl_panelrentstatistics.rowHeights = new int[]{0, 0};
 		gbl_panelrentstatistics.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
 		gbl_panelrentstatistics.rowWeights = new double[]{0.0, Double.MIN_VALUE};
 		panelrentstatistics.setLayout(gbl_panelrentstatistics);
 		
 		JLabel lblNumber_of_Rents = new JLabel("Books rented: ");
 		GridBagConstraints gbc_lblNumber_of_Rents = new GridBagConstraints();
 		gbc_lblNumber_of_Rents.insets = new Insets(0, 0, 0, 5);
 		gbc_lblNumber_of_Rents.anchor = GridBagConstraints.EAST;
 		gbc_lblNumber_of_Rents.gridx = 0;
 		gbc_lblNumber_of_Rents.gridy = 0;
 		panelrentstatistics.add(lblNumber_of_Rents, gbc_lblNumber_of_Rents);
 		
 		JLabel display_number_of_rents = new JLabel("%nr");
 		GridBagConstraints gbc_display_number_of_rents = new GridBagConstraints();
 		gbc_display_number_of_rents.insets = new Insets(0, 0, 0, 5);
 		gbc_display_number_of_rents.anchor = GridBagConstraints.WEST;
 		gbc_display_number_of_rents.gridx = 1;
 		gbc_display_number_of_rents.gridy = 0;
 		panelrentstatistics.add(display_number_of_rents, gbc_display_number_of_rents);
 		
 		JLabel lbl_number_of_lendings = new JLabel("Nr. of Lendings");
 		GridBagConstraints gbc_lbl_number_of_lendings = new GridBagConstraints();
 		gbc_lbl_number_of_lendings.insets = new Insets(0, 0, 0, 5);
 		gbc_lbl_number_of_lendings.gridx = 3;
 		gbc_lbl_number_of_lendings.gridy = 0;
 		panelrentstatistics.add(lbl_number_of_lendings, gbc_lbl_number_of_lendings);
 		
 		JLabel display_number_of_lendings = new JLabel("%nr");
 		GridBagConstraints gbc_display_number_of_lendings = new GridBagConstraints();
 		gbc_display_number_of_lendings.insets = new Insets(0, 0, 0, 5);
 		gbc_display_number_of_lendings.gridx = 4;
 		gbc_display_number_of_lendings.gridy = 0;
 		panelrentstatistics.add(display_number_of_lendings, gbc_display_number_of_lendings);
 		
 		JLabel lblOverdue = new JLabel("Overdue: ");
 		GridBagConstraints gbc_lblOverdue = new GridBagConstraints();
 		gbc_lblOverdue.insets = new Insets(0, 0, 0, 5);
 		gbc_lblOverdue.gridx = 6;
 		gbc_lblOverdue.gridy = 0;
 		panelrentstatistics.add(lblOverdue, gbc_lblOverdue);
 		
 		JLabel display_overdue = new JLabel("%nr");
 		GridBagConstraints gbc_display_overdue = new GridBagConstraints();
 		gbc_display_overdue.gridx = 7;
 		gbc_display_overdue.gridy = 0;
 		panelrentstatistics.add(display_overdue, gbc_display_overdue);
 		
 		JPanel panel = new JPanel();
 		GridBagConstraints gbc_panel = new GridBagConstraints();
 		gbc_panel.fill = GridBagConstraints.BOTH;
 		gbc_panel.gridx = 0;
 		gbc_panel.gridy = 1;
 		lendingTab.add(panel, gbc_panel);
 		GridBagLayout gbl_panel = new GridBagLayout();
 		gbl_panel.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0};
 		gbl_panel.rowHeights = new int[]{0, 0, 0};
 		gbl_panel.columnWeights = new double[]{0.0, 0.0, 1.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
 		gbl_panel.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
 		panel.setLayout(gbl_panel);
 		
 		JScrollPane scrollPane_2 = new JScrollPane();
 		GridBagConstraints gbc_scrollPane_2 = new GridBagConstraints();
 		gbc_scrollPane_2.gridwidth = 6;
 		gbc_scrollPane_2.fill = GridBagConstraints.BOTH;
 		gbc_scrollPane_2.insets = new Insets(0, 0, 5, 0);
 		gbc_scrollPane_2.gridx = 0;
 		gbc_scrollPane_2.gridy = 0;
 		panel.add(scrollPane_2, gbc_scrollPane_2);
 		
 		table_1 = new JTable();
 		scrollPane_2.setViewportView(table_1);
 		
 		JLabel lblSelected_1 = new JLabel("Selected: ");
 		GridBagConstraints gbc_lblSelected_1 = new GridBagConstraints();
 		gbc_lblSelected_1.insets = new Insets(0, 0, 0, 5);
 		gbc_lblSelected_1.gridx = 0;
 		gbc_lblSelected_1.gridy = 1;
 		panel.add(lblSelected_1, gbc_lblSelected_1);
 		
 		JLabel DisplaySelected = new JLabel("%nr");
 		GridBagConstraints gbc_DisplaySelected = new GridBagConstraints();
 		gbc_DisplaySelected.insets = new Insets(0, 0, 0, 5);
 		gbc_DisplaySelected.anchor = GridBagConstraints.EAST;
 		gbc_DisplaySelected.gridx = 1;
 		gbc_DisplaySelected.gridy = 1;
 		panel.add(DisplaySelected, gbc_DisplaySelected);
 		
 		txtSearchfield = new JTextField();
 		txtSearchfield.setText("Search_field");
 		GridBagConstraints gbc_txtSearchfield = new GridBagConstraints();
 		gbc_txtSearchfield.insets = new Insets(0, 0, 0, 5);
 		gbc_txtSearchfield.fill = GridBagConstraints.HORIZONTAL;
 		gbc_txtSearchfield.gridx = 2;
 		gbc_txtSearchfield.gridy = 1;
 		panel.add(txtSearchfield, gbc_txtSearchfield);
 		txtSearchfield.setColumns(10);
 		
 		JCheckBox chckbxOverdue = new JCheckBox("Only Overdue");
 		GridBagConstraints gbc_chckbxOverdue = new GridBagConstraints();
 		gbc_chckbxOverdue.insets = new Insets(0, 0, 0, 5);
 		gbc_chckbxOverdue.gridx = 3;
 		gbc_chckbxOverdue.gridy = 1;
 		panel.add(chckbxOverdue, gbc_chckbxOverdue);
 		
 		JButton btnDisplay_selected = new JButton("Display Selected");
 		GridBagConstraints gbc_btnDisplay_selected = new GridBagConstraints();
 		gbc_btnDisplay_selected.insets = new Insets(0, 0, 0, 5);
 		gbc_btnDisplay_selected.gridx = 4;
 		gbc_btnDisplay_selected.gridy = 1;
 		panel.add(btnDisplay_selected, gbc_btnDisplay_selected);
 		
 		JButton btnNew_rent = new JButton("New Rent");
 		btnNew_rent.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 			}
 		});
 		GridBagConstraints gbc_btnNew_rent = new GridBagConstraints();
 		gbc_btnNew_rent.gridx = 5;
 		gbc_btnNew_rent.gridy = 1;
 		panel.add(btnNew_rent, gbc_btnNew_rent);
 		
 		JPanel customerTab = new JPanel();
 		tabbedPane.addTab("Customers", null, customerTab, null);
 		GridBagLayout gbl_customerTab = new GridBagLayout();
 		gbl_customerTab.columnWidths = new int[]{0, 0};
 		gbl_customerTab.rowHeights = new int[]{48, 0, 0};
 		gbl_customerTab.columnWeights = new double[]{1.0, Double.MIN_VALUE};
 		gbl_customerTab.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
 		customerTab.setLayout(gbl_customerTab);
 		
 		JPanel panelCustomerStats = new JPanel();
 		panelCustomerStats.setBorder(new TitledBorder(null, "Customer Statistics", TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		GridBagConstraints gbc_panelCustomerStats = new GridBagConstraints();
 		gbc_panelCustomerStats.insets = new Insets(0, 0, 5, 0);
 		gbc_panelCustomerStats.fill = GridBagConstraints.BOTH;
 		gbc_panelCustomerStats.gridx = 0;
 		gbc_panelCustomerStats.gridy = 0;
 		customerTab.add(panelCustomerStats, gbc_panelCustomerStats);
 		GridBagLayout gbl_panelCustomerStats = new GridBagLayout();
 		gbl_panelCustomerStats.columnWidths = new int[]{76, 91, 0, 0, 0};
 		gbl_panelCustomerStats.rowHeights = new int[]{16, 0};
 		gbl_panelCustomerStats.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
 		gbl_panelCustomerStats.rowWeights = new double[]{0.0, Double.MIN_VALUE};
 		panelCustomerStats.setLayout(gbl_panelCustomerStats);
 		
 		JLabel lblNrCustomers = new JLabel("Nr. Customers");
 		GridBagConstraints gbc_lblNrCustomers = new GridBagConstraints();
 		gbc_lblNrCustomers.insets = new Insets(0, 0, 0, 5);
 		gbc_lblNrCustomers.anchor = GridBagConstraints.NORTHWEST;
 		gbc_lblNrCustomers.gridx = 0;
 		gbc_lblNrCustomers.gridy = 0;
 		panelCustomerStats.add(lblNrCustomers, gbc_lblNrCustomers);
 		
 		JLabel displayNrCustomer = new JLabel("%nr");
 		GridBagConstraints gbc_displayNrCustomer = new GridBagConstraints();
 		gbc_displayNrCustomer.insets = new Insets(0, 0, 0, 5);
 		gbc_displayNrCustomer.anchor = GridBagConstraints.WEST;
 		gbc_displayNrCustomer.gridx = 1;
 		gbc_displayNrCustomer.gridy = 0;
 		panelCustomerStats.add(displayNrCustomer, gbc_displayNrCustomer);
 		
 		JLabel lblCustomOverdue = new JLabel("Custom. Overdue");
 		GridBagConstraints gbc_lblCustomOverdue = new GridBagConstraints();
 		gbc_lblCustomOverdue.insets = new Insets(0, 0, 0, 5);
 		gbc_lblCustomOverdue.gridx = 2;
 		gbc_lblCustomOverdue.gridy = 0;
 		panelCustomerStats.add(lblCustomOverdue, gbc_lblCustomOverdue);
 		
 		JLabel displayCustomerOverdue = new JLabel("%nr");
 		GridBagConstraints gbc_displayCustomerOverdue = new GridBagConstraints();
 		gbc_displayCustomerOverdue.gridx = 3;
 		gbc_displayCustomerOverdue.gridy = 0;
 		panelCustomerStats.add(displayCustomerOverdue, gbc_displayCustomerOverdue);
 		
 		JPanel panel_1 = new JPanel();
 		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
 		gbc_panel_1.fill = GridBagConstraints.BOTH;
 		gbc_panel_1.gridx = 0;
 		gbc_panel_1.gridy = 1;
 		customerTab.add(panel_1, gbc_panel_1);
 		GridBagLayout gbl_panel_1 = new GridBagLayout();
 		gbl_panel_1.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0};
 		gbl_panel_1.rowHeights = new int[]{0, 0, 0};
 		gbl_panel_1.columnWeights = new double[]{0.0, 0.0, 1.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
 		gbl_panel_1.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
 		panel_1.setLayout(gbl_panel_1);
 		
 		JScrollPane scrollPane_1 = new JScrollPane();
 		GridBagConstraints gbc_scrollPane_1 = new GridBagConstraints();
 		gbc_scrollPane_1.gridwidth = 6;
 		gbc_scrollPane_1.fill = GridBagConstraints.BOTH;
 		gbc_scrollPane_1.insets = new Insets(0, 0, 5, 0);
 		gbc_scrollPane_1.gridx = 0;
 		gbc_scrollPane_1.gridy = 0;
 		panel_1.add(scrollPane_1, gbc_scrollPane_1);
 		
 		table_2 = new JTable();
 		scrollPane_1.setViewportView(table_2);
 		
 		JLabel lblSelected_2 = new JLabel("Selected: ");
 		GridBagConstraints gbc_lblSelected_2 = new GridBagConstraints();
 		gbc_lblSelected_2.insets = new Insets(0, 0, 0, 5);
 		gbc_lblSelected_2.anchor = GridBagConstraints.EAST;
 		gbc_lblSelected_2.gridx = 0;
 		gbc_lblSelected_2.gridy = 1;
 		panel_1.add(lblSelected_2, gbc_lblSelected_2);
 		
 		JLabel displaySelected = new JLabel("%nr");
 		GridBagConstraints gbc_displaySelected = new GridBagConstraints();
 		gbc_displaySelected.insets = new Insets(0, 0, 0, 5);
 		gbc_displaySelected.anchor = GridBagConstraints.EAST;
 		gbc_displaySelected.gridx = 1;
 		gbc_displaySelected.gridy = 1;
 		panel_1.add(displaySelected, gbc_displaySelected);
 		
 		txtSearchfield_1 = new JTextField();
 		txtSearchfield_1.setText("search_field");
 		GridBagConstraints gbc_txtSearchfield_1 = new GridBagConstraints();
 		gbc_txtSearchfield_1.insets = new Insets(0, 0, 0, 5);
 		gbc_txtSearchfield_1.fill = GridBagConstraints.HORIZONTAL;
 		gbc_txtSearchfield_1.gridx = 2;
 		gbc_txtSearchfield_1.gridy = 1;
 		panel_1.add(txtSearchfield_1, gbc_txtSearchfield_1);
 		txtSearchfield_1.setColumns(10);
 		
 		JCheckBox chckbxOnlyOverdue = new JCheckBox("Only Overdue");
 		GridBagConstraints gbc_chckbxOnlyOverdue = new GridBagConstraints();
 		gbc_chckbxOnlyOverdue.insets = new Insets(0, 0, 0, 5);
 		gbc_chckbxOnlyOverdue.gridx = 3;
 		gbc_chckbxOnlyOverdue.gridy = 1;
 		panel_1.add(chckbxOnlyOverdue, gbc_chckbxOnlyOverdue);
 		
 		JButton btnDisplaySelected_1 = new JButton("Display Selected");
 		GridBagConstraints gbc_btnDisplaySelected_1 = new GridBagConstraints();
 		gbc_btnDisplaySelected_1.insets = new Insets(0, 0, 0, 5);
 		gbc_btnDisplaySelected_1.gridx = 4;
 		gbc_btnDisplaySelected_1.gridy = 1;
 		panel_1.add(btnDisplaySelected_1, gbc_btnDisplaySelected_1);
 		
 		JButton btnNewCustomer = new JButton("New Customer");
		btnNewCustomer.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				newCustomerWindow.setVisible();
			}
		});
 		GridBagConstraints gbc_btnNewCustomer = new GridBagConstraints();
 		gbc_btnNewCustomer.gridx = 5;
 		gbc_btnNewCustomer.gridy = 1;
 		panel_1.add(btnNewCustomer, gbc_btnNewCustomer);
 		
 	}
 
 	private void reset_search() {
 		txtSearch.setForeground(Color.LIGHT_GRAY);
 		txtSearch.setFont(new Font("Lucida Grande", Font.ITALIC, 13));
 		txtSearch.setText("Search");
 	}
 
 	@Override
 	public void update(Observable arg0, Object arg1) {
 		display_number_of_titles.setText(library.getBooks().size() + "");
 		display_number_of_books.setText(library.getCopies().size() + "");		
 	}
 }

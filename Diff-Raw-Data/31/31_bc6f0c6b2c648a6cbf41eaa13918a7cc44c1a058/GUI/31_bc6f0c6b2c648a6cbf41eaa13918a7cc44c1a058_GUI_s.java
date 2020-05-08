 package hbs.sff.no;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.util.Arrays;
 
 import javax.swing.AbstractButton;
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 import javax.swing.JToolBar;
 import javax.swing.ListSelectionModel;
 import javax.swing.SpringLayout;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.table.TableColumn;
 
 public class GUI {
 
 	private JFrame frame;
 	private JButton btnExit;
 	private JButton btnHelp;
 	private JButton btnGenerateReport;
 	private JTextField idField;
 	private JTextField nameField;
 	private JLabel lblID;
 	private JLabel lblName;
 	private JButton btnSearch;
 	private JLabel lblInvalidInput;
 	private JLabel lblInvalidInput_1;
 	private JScrollPane scrollPane;
 	private JTable table_selection;
 	private JScrollPane scrollPaneCustomers;
 	private JTable table_customers;
 	private JScrollPane scrollPaneProjects;
 	private JTable table_projects;
 	private JScrollPane scrollPaneStatuses;
 	private JTable table_statuses;
 	private SelectionTableModel stm_display_cust;
 	private SelectionTableModel stm_display_proj;
 	private SelectionTableModel stm_display_stat;
 	private SelectionTableModel stm_select_cust;
 	private SelectionTableModel stm_select_proj;
 	private SelectionTableModel stm_select_stat;
 	private JToolBar toolBar;
 	private JPanel panel;
 	private SpringLayout sl_panel;
 	private JButton bCustomers;
 	private JButton bProjects;
 	private JButton bStatuses;
 	private JLabel lblSelection;
 	private CheckBoxHeader header;
 	private Data data;
 
 	public JFrame getFrame() {
 		return frame;
 	}
 
 	public GUI() {
 		initialize();
 	}
 
 	private void initialize() {
 		try {
 			UIManager.setLookAndFeel(
 					UIManager.getSystemLookAndFeelClassName());
 		} catch (ClassNotFoundException | InstantiationException
 				| IllegalAccessException | UnsupportedLookAndFeelException e) {
 			e.printStackTrace();
 		}
 
 		SpringLayout springLayout = createFrame();
 
 		Font headline = new Font("Serif", Font.PLAIN, 24);
 		Font subheadline = new Font("Serif", Font.PLAIN, 16);
 		Font errorMessage = new Font("Serif", Font.PLAIN, 14);
 
 		JPanel panel_1 = createPanelOne(springLayout);
 		JPanel panel_2 = createPanelTwo(springLayout, panel_1);
 
 		sl_panel = new SpringLayout();
 		sl_panel.putConstraint(SpringLayout.WEST, toolBar, 10, SpringLayout.WEST, panel);
 		sl_panel.putConstraint(SpringLayout.EAST, toolBar, 334, SpringLayout.WEST, panel);
 		panel_1.setLayout(sl_panel);
 
 		JLabel lblSelectCustomers = createSelectionLabel(headline, panel_1,
 				sl_panel);
 		createIdField(panel_1, sl_panel, lblSelectCustomers);
 		createNameField(panel_1, sl_panel);
 		createIdLabel(subheadline, panel_1, sl_panel);
 		createNameLabel(subheadline, panel_1, sl_panel);
 		createSearchButton(panel_1, sl_panel);		
 		createErrorLabelOne(errorMessage, panel_1, sl_panel);		
 		createErrorLabelTwo(errorMessage, panel_1, sl_panel);		
 
 		frame.getContentPane().add(panel_2);
 		SpringLayout sl_panel_2 = new SpringLayout();
 		panel_2.setLayout(sl_panel_2);
 
 		JLabel lblSelected = createSelectedHeadline(headline, panel_2,
 				sl_panel_2);
 
 		createExitButton(panel_2);
 		createHelpButton(panel_2, sl_panel_2, lblSelected);
 		createReportButton(panel_2, sl_panel_2);				
 
 		addScrollPaneOne(panel_2, sl_panel_2, lblSelected);		
 		addScrollPane(panel_1, sl_panel);		
 		addScrollPaneTwo(panel_2, sl_panel_2);		
 		addScrollPaneThree(panel_2, sl_panel_2);	
 		createSelectionModels();
 		addTableStatuses();		
 		addTableCustomers();				
 		addTableSelection();		
 		addTableProjects();	
 		data = new Data();
 		data.LoadData();
 
 		enableCustomerSelection();		
 	}
 
 	private void addTableStatuses() {
 		String[] colNames_stat = {"", "Status"};
 		Object[][] rowData = {{new Boolean(true), "ALL"}};
 		stm_display_stat = new SelectionTableModel(colNames_stat, rowData);
 		table_statuses = new JTable(stm_display_stat);
 		configureTableColumns(table_statuses);
 		scrollPaneStatuses.setViewportView(table_statuses);
 	}	
 
 	private void addTableProjects() {
 		String[] colNames_proj = {"", "Project"};
 		Object[][] rowData = {{new Boolean(true), "ALL"}};
 		stm_display_proj = new SelectionTableModel(colNames_proj, rowData);
 		table_projects = new JTable(stm_display_proj);
 		configureTableColumns(table_projects);
 		scrollPaneProjects.setViewportView(table_projects);
 	}
 
 	private void addTableCustomers() {
 		String[] colNames_comp = {"", "ID", "Company"};
 		Object[][] rowData = {{new Boolean(true), "ALL", ""}};
 		stm_display_cust = new SelectionTableModel(colNames_comp, rowData);
 		table_customers = new JTable(stm_display_cust);
 		configureTableColumns(table_customers);
 		scrollPaneCustomers.setViewportView(table_customers);
 	}
 
 	private void addTableSelection() {
 		table_selection = new JTable(stm_select_cust);
 		table_selection.getSelectionModel().
 		addListSelectionListener(new ListSelectionListenerImpl());		
 		TableColumn tc = configureTableColumns(table_selection);
 		header = new CheckBoxHeader(new MyItemListener());
 		tc.setHeaderRenderer(header);
 		scrollPane.setViewportView(table_selection);
 		scrollPane.getViewport().setBackground(Color.white);		
 	}
 
 	private void setCustomerSelectionModel(){		
 		table_selection.setModel(stm_select_cust);
 		TableColumn tc = configureTableColumns(table_selection);		
 		tc.setHeaderRenderer(header);
 	}
 
 	private void setProjectSelectionModel(){
 		table_selection.setModel(stm_select_proj);
 		TableColumn tc = configureTableColumns(table_selection);		
 		tc.setHeaderRenderer(header);
 	}
 
 	private void setStatusSelectionModel(){
 		table_selection.setModel(stm_select_stat);
 		TableColumn tc = configureTableColumns(table_selection);		
 		tc.setHeaderRenderer(header);
 	}
 
 	private void createSelectionModels(){
 		String[] colNames_sComp = {"", "ID", "Customers"};
 		Object[][] rowData = {};
 		stm_select_cust = new SelectionTableModel(colNames_sComp, rowData);
 		String[] colNames_sProj = {"", "Project"};
 		stm_select_proj = new SelectionTableModel(colNames_sProj, rowData);
 		String[] colNames_sStat = {"", "Status"};
 		stm_select_stat = new SelectionTableModel(colNames_sStat, rowData);
 
 	}
 
 	private void addScrollPaneThree(JPanel panel_2, SpringLayout sl_panel_2) {
 		scrollPaneStatuses = new JScrollPane();
 		sl_panel_2.putConstraint(SpringLayout.NORTH, scrollPaneStatuses, 27, 
 				SpringLayout.SOUTH, scrollPaneProjects);
 		sl_panel_2.putConstraint(SpringLayout.WEST, scrollPaneStatuses, 10, 
 				SpringLayout.WEST, panel_2);
 		sl_panel_2.putConstraint(SpringLayout.SOUTH, scrollPaneStatuses, 227, 
 				SpringLayout.SOUTH, scrollPaneProjects);
 		sl_panel_2.putConstraint(SpringLayout.EAST, scrollPaneStatuses, 454, 
 				SpringLayout.WEST, panel_2);
 		panel_2.add(scrollPaneStatuses);
 	}
 
 	private void addScrollPaneTwo(JPanel panel_2, SpringLayout sl_panel_2) {
 		scrollPaneProjects = new JScrollPane();
 		sl_panel_2.putConstraint(SpringLayout.NORTH, scrollPaneProjects, 318, 
 				SpringLayout.NORTH, panel_2);
 		sl_panel_2.putConstraint(SpringLayout.WEST, scrollPaneProjects, 10, 
 				SpringLayout.WEST, panel_2);
 		sl_panel_2.putConstraint(SpringLayout.SOUTH, scrollPaneProjects, 230, 
 				SpringLayout.SOUTH, scrollPaneCustomers);
 		sl_panel_2.putConstraint(SpringLayout.EAST, scrollPaneProjects, 454, 
 				SpringLayout.WEST, panel_2);
 		panel_2.add(scrollPaneProjects);
 	}
 
 	private void addScrollPane(JPanel panel_1, SpringLayout sl_panel_1) {
 		scrollPane = new JScrollPane();
 		sl_panel.putConstraint(SpringLayout.SOUTH, toolBar, -13, SpringLayout.NORTH, scrollPane);
 		scrollPane.setBorder(BorderFactory.createLineBorder(Color.black));
 		sl_panel_1.putConstraint(SpringLayout.NORTH, scrollPane, 13, 
 				SpringLayout.SOUTH, btnSearch);
 		sl_panel_1.putConstraint(SpringLayout.WEST, scrollPane, 10, 
 				SpringLayout.WEST, panel_1);
 		sl_panel_1.putConstraint(SpringLayout.SOUTH, scrollPane, -33, 
 				SpringLayout.SOUTH, panel_1);
 		sl_panel_1.putConstraint(SpringLayout.EAST, scrollPane, 454, 
 				SpringLayout.WEST, panel_1);
 		panel_1.add(scrollPane);
 	}
 
 	private void addScrollPaneOne(JPanel panel_2, SpringLayout sl_panel_2,
 			JLabel lblSelected) {
 		scrollPaneCustomers = new JScrollPane();
 		sl_panel_2.putConstraint(SpringLayout.NORTH, scrollPaneCustomers, 46, 
 				SpringLayout.SOUTH, lblSelected);
 		sl_panel_2.putConstraint(SpringLayout.WEST, scrollPaneCustomers, 10, 
 				SpringLayout.WEST, panel_2);
 		sl_panel_2.putConstraint(SpringLayout.SOUTH, scrollPaneCustomers, -560, 
 				SpringLayout.SOUTH, panel_2);
 		sl_panel_2.putConstraint(SpringLayout.EAST, scrollPaneCustomers, 454, 
 				SpringLayout.WEST, panel_2);
 		panel_2.add(scrollPaneCustomers);
 	}
 
 	private void createReportButton(JPanel panel_2, SpringLayout sl_panel_2) {
 		btnGenerateReport = new JButton("Generate report");
 		sl_panel_2.putConstraint(SpringLayout.EAST, btnGenerateReport, -29, 
 				SpringLayout.EAST, panel_2);
 		sl_panel_2.putConstraint(SpringLayout.WEST, btnHelp, 0, 
 				SpringLayout.WEST, btnGenerateReport);
 		btnGenerateReport.setForeground(Color.blue);
 		btnGenerateReport.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 			}
 		});
 		sl_panel_2.putConstraint(SpringLayout.SOUTH, btnGenerateReport, -10,
 				SpringLayout.SOUTH, panel_2);
 		panel_2.add(btnGenerateReport);
 	}
 
 	private void createHelpButton(JPanel panel_2, SpringLayout sl_panel_2,
 			JLabel lblSelected) {
 		btnHelp = new JButton("Help");
 		sl_panel_2.putConstraint(SpringLayout.EAST, lblSelected, -83,
 				SpringLayout.WEST, btnHelp);
 		sl_panel_2.putConstraint(SpringLayout.NORTH, btnExit, 0,
 				SpringLayout.NORTH, btnHelp);
 		sl_panel_2.putConstraint(SpringLayout.WEST, btnExit, 6,
 				SpringLayout.EAST, btnHelp);
 		sl_panel_2.putConstraint(SpringLayout.NORTH, btnHelp, 10,
 				SpringLayout.NORTH, panel_2);
 		btnHelp.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 			}
 		});
 		panel_2.add(btnHelp);
 	}
 
 	private void createExitButton(JPanel panel_2) {
 		btnExit = new JButton("Exit");
 		btnExit.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.exit(0);
 			}
 		});
 		panel_2.add(btnExit);
 	}
 
 	private JLabel createSelectedHeadline(Font headline, JPanel panel_2,
 			SpringLayout sl_panel_2) {
 		JLabel lblSelected = new JLabel("Selected");
 		sl_panel_2.putConstraint(SpringLayout.NORTH, lblSelected, 10, 
 				SpringLayout.NORTH, panel_2);
 		lblSelected.setFont(headline);
 		panel_2.add(lblSelected);
 		return lblSelected;
 	}
 
 	private void createErrorLabelTwo(Font errorMessage, JPanel panel_1,
 			SpringLayout sl_panel_1) {
 		lblInvalidInput_1 = new JLabel("Invalid input");
 		sl_panel.putConstraint(SpringLayout.WEST, lblInvalidInput_1, 340, SpringLayout.WEST, panel);
 		sl_panel.putConstraint(SpringLayout.EAST, nameField, -6, SpringLayout.WEST, lblInvalidInput_1);
 		sl_panel.putConstraint(SpringLayout.NORTH, lblInvalidInput_1, 22, SpringLayout.SOUTH, lblInvalidInput);
 		lblInvalidInput_1.setForeground(Color.red);
 		lblInvalidInput_1.setFont(errorMessage);
 		lblInvalidInput_1.setVisible(false);
 		sl_panel_1.putConstraint(SpringLayout.EAST, lblInvalidInput, 0,
 				SpringLayout.EAST, lblInvalidInput_1);
 		panel_1.add(lblInvalidInput_1);
 	}
 
 	private void createErrorLabelOne(Font errorMessage, JPanel panel_1,
 			SpringLayout sl_panel_1) {
 		lblInvalidInput = new JLabel("Invalid input");
 		lblInvalidInput.setForeground(Color.red);
 		lblInvalidInput.setFont(errorMessage);
 		lblInvalidInput.setVisible(false);
 		sl_panel_1.putConstraint(SpringLayout.NORTH, lblInvalidInput, 10, 
 				SpringLayout.NORTH, idField);
 		panel_1.add(lblInvalidInput);
 	}
 
 	private void createSearchButton(JPanel panel_1, SpringLayout sl_panel_1) {
 		btnSearch = new JButton("Search");
 		sl_panel.putConstraint(SpringLayout.NORTH, toolBar, 0, SpringLayout.NORTH, btnSearch);
 		sl_panel_1.putConstraint(SpringLayout.WEST, btnSearch, 364,
 				SpringLayout.WEST, panel_1);
 		sl_panel_1.putConstraint(SpringLayout.SOUTH, btnSearch, -664, 
 				SpringLayout.SOUTH, panel_1);
 		sl_panel_1.putConstraint(SpringLayout.EAST, btnSearch, -26, 
 				SpringLayout.EAST, panel_1);
 		btnSearch.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 			}
 		});
 		panel_1.add(btnSearch);
 	}
 
 	private void createNameLabel(Font subheadline, JPanel panel_1,
 			SpringLayout sl_panel_1) {
 		lblName = new JLabel("Customer Name");
 		sl_panel.putConstraint(SpringLayout.NORTH, lblName, 4, SpringLayout.NORTH, nameField);
 		sl_panel.putConstraint(SpringLayout.WEST, lblName, 0, SpringLayout.WEST, toolBar);
 		lblName.setFont(subheadline);
 		panel_1.add(lblName);
 	}
 
 	private void createIdLabel(Font subheadline, JPanel panel_1,
 			SpringLayout sl_panel_1) {
 		lblID = new JLabel("Customer ID");
 		sl_panel_1.putConstraint(SpringLayout.WEST, idField, 59, 
 				SpringLayout.EAST, lblID);
 		sl_panel_1.putConstraint(SpringLayout.NORTH, lblID, 4, 
 				SpringLayout.NORTH, idField);
 		sl_panel_1.putConstraint(SpringLayout.WEST, lblID, 10, 
 				SpringLayout.WEST, panel_1);
 		lblID.setFont(subheadline);
 		panel_1.add(lblID);
 	}
 
 	private void createNameField(JPanel panel_1, SpringLayout sl_panel_1) {
 		nameField = new JTextField();
 		sl_panel.putConstraint(SpringLayout.NORTH, nameField, 6, SpringLayout.SOUTH, idField);
 		sl_panel.putConstraint(SpringLayout.WEST, nameField, 148, SpringLayout.WEST, panel);
 		sl_panel.putConstraint(SpringLayout.SOUTH, nameField, -19, SpringLayout.NORTH, toolBar);
 		panel_1.add(nameField);
 		nameField.setColumns(10);
 	}
 
 	private void createIdField(JPanel panel_1, SpringLayout sl_panel_1,
 			JLabel lblSelectCustomers) {
 		idField = new JTextField();
 		sl_panel.putConstraint(SpringLayout.NORTH, idField, 16, SpringLayout.SOUTH, lblSelection);
 		sl_panel.putConstraint(SpringLayout.SOUTH, idField, -747, SpringLayout.SOUTH, panel);
 		sl_panel_1.putConstraint(SpringLayout.EAST, idField, -130, 
 				SpringLayout.EAST, panel_1);
 		panel_1.add(idField);
 		idField.setColumns(10);
 	}
 
 	private JLabel createSelectionLabel(Font headline, JPanel panel_1,
 			SpringLayout sl_panel_1) {
 		lblSelection = new JLabel("Select Customers");
 		sl_panel_1.putConstraint(SpringLayout.NORTH, lblSelection, 0, 
 				SpringLayout.NORTH, panel_1);
 		sl_panel_1.putConstraint(SpringLayout.WEST, lblSelection, 0, 
 				SpringLayout.WEST, panel_1);
 		sl_panel_1.putConstraint(SpringLayout.SOUTH, lblSelection, 50, 
 				SpringLayout.NORTH, panel_1);
 		sl_panel_1.putConstraint(SpringLayout.EAST, lblSelection, 203, 
 				SpringLayout.WEST, panel_1);
 		lblSelection.setFont(headline);
 		panel_1.add(lblSelection);
 		return lblSelection;
 	}
 
 	private JPanel createPanelTwo(SpringLayout springLayout, JPanel panel_1) {
 		JPanel panel_2 = new JPanel();
 		springLayout.putConstraint(SpringLayout.NORTH, panel_2, 10,
 				SpringLayout.NORTH, frame.getContentPane());
 		springLayout.putConstraint(SpringLayout.WEST, panel_2, 6, 
 				SpringLayout.EAST, panel_1);
 		springLayout.putConstraint(SpringLayout.SOUTH, panel_2, 0, 
 				SpringLayout.SOUTH, panel_1);
 		springLayout.putConstraint(SpringLayout.EAST, panel_2, -10, 
 				SpringLayout.EAST, frame.getContentPane());
 		return panel_2;
 	}
 
 	private JPanel createPanelOne(SpringLayout springLayout) {
 		panel = new JPanel();
 		springLayout.putConstraint(SpringLayout.NORTH, panel, 10,
 				SpringLayout.NORTH, frame.getContentPane());
 		springLayout.putConstraint(SpringLayout.WEST, panel, 10, 
 				SpringLayout.WEST, frame.getContentPane());
 		springLayout.putConstraint(SpringLayout.SOUTH, panel, -10,
 				SpringLayout.SOUTH, frame.getContentPane());
 		springLayout.putConstraint(SpringLayout.EAST, panel, -499, 
 				SpringLayout.EAST, frame.getContentPane());
 		panel.setBackground(Color.white);
 		frame.getContentPane().add(panel);
 
 		toolBar = new JToolBar();
 		toolBar.setLayout(new GridLayout());
 		toolBar.setFloatable(false);
 		addButtons(toolBar);
 		panel.add(toolBar);
 		return panel;
 	}
 
 	private void addButtons(JToolBar toolBar) {
 		bCustomers = new JButton("Customers");
 		bCustomers.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				customerSelection();
 			}
 		});
 		bCustomers.setSelected(true);
 		bCustomers.setBorder(BorderFactory.createSoftBevelBorder(0));
 		toolBar.add(bCustomers);
 		bProjects = new JButton("Projects");
 		bProjects.setBorder(BorderFactory.createSoftBevelBorder(0));
 		bProjects.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				projectSelection();
 			}
 		});
 		toolBar.add(bProjects);
 		bStatuses = new JButton("Statuses");
 		bStatuses.setBorder(BorderFactory.createSoftBevelBorder(0));
 		bStatuses.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				statusSelection();
 			}
 		});
 		toolBar.add(bStatuses);
 	}
 
 	private SpringLayout createFrame() {
 		frame = new JFrame();
 		frame.setBounds(100, 100, 989, 906);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		SpringLayout springLayout = new SpringLayout();
 		frame.getContentPane().setLayout(springLayout);
 		frame.getContentPane().setBackground(Color.white);
 		return springLayout;
 	}
 
 	class MyItemListener implements ItemListener{
 		public void itemStateChanged(ItemEvent e){
 			Object source = e.getSource();
 			if(source instanceof AbstractButton == false) return;
 			boolean checked =  e.getStateChange() == ItemEvent.SELECTED;
 			for(int x = 0, y = table_selection.getRowCount(); x < y; x++){
 				table_selection.setValueAt(new Boolean(checked), x, 0);
 			}
 			((SelectionTableModel) table_selection.getModel()).
 			fireTableDataChanged();
 		}
 	}
 
 	private void customerSelection(){
 		lblSelection.setText("Select Customers");
 		lblName.setVisible(true);
 		lblName.setText("Customer Name");
 		lblID.setVisible(true);
 		lblID.setText("Customer ID");
 		nameField.setVisible(true);
 		idField.setVisible(true);
 
 		setCustomerSelectionModel();
 
 		enableCustomerSelection();
 		bCustomers.setSelected(true);
 		bProjects.setSelected(false);
 		bStatuses.setSelected(false);
 
 		// TODO: Mark disabled fields at startup
 		// TODO: Make disabled fields editable
 		// TODO: Select all stays checked when switching tabs
 	}
 
 	private void enableCustomerSelection() {
 		table_customers.setBackground(Color.white);
 		table_projects.setBackground(Color.lightGray);
 		table_statuses.setBackground(Color.lightGray);
 		scrollPaneProjects.getViewport().setBackground(Color.lightGray);
 		scrollPaneStatuses.getViewport().setBackground(Color.lightGray);
 		scrollPaneCustomers.getViewport().setBackground(Color.white);
 
 		// TODO: Disable edit in other frames completely, not just visually
 	}
 
 	private void projectSelection(){
 		lblSelection.setText("Select Projects");
 		lblName.setVisible(true);
 		lblName.setText("Project Name");
 		lblID.setVisible(false);
 		nameField.setVisible(true);
 		idField.setVisible(false);
 
 		setProjectSelectionModel();
 
 		enableProjectSelection();
 		bCustomers.setSelected(false);
 		bProjects.setSelected(true);
 		bStatuses.setSelected(false);	
 	}
 
 	private void enableProjectSelection() {
 		table_projects.setBackground(Color.white);
 		table_customers.setBackground(Color.lightGray);
 		table_statuses.setBackground(Color.lightGray);
 		scrollPaneCustomers.getViewport().setBackground(Color.lightGray);
 		scrollPaneStatuses.getViewport().setBackground(Color.lightGray);
 		scrollPaneProjects.getViewport().setBackground(Color.white);
 	}
 
 	private void statusSelection(){
 		lblSelection.setText("Select Statuses");
 		lblName.setVisible(false);
 		lblID.setVisible(false);
 		nameField.setVisible(false);
 		idField.setVisible(false);
 
 		setStatusSelectionModel();
 		Object[][] rowData = new Object[data.getSize(Data.Type.STATUS)][2];
 		data.putData(rowData, Data.Type.STATUS);
 		stm_select_stat.setRowData(rowData);
 		stm_select_stat.fireTableDataChanged();
 
 		enableStatusSelection();
 		bCustomers.setSelected(false);
 		bProjects.setSelected(false);
 		bStatuses.setSelected(true);		
 	}
 
 	private void enableStatusSelection() {
 		table_statuses.setBackground(Color.white);
 		table_customers.setBackground(Color.lightGray);
 		table_projects.setBackground(Color.lightGray);
 		scrollPaneCustomers.getViewport().setBackground(Color.lightGray);
 		scrollPaneProjects.getViewport().setBackground(Color.lightGray);
 		scrollPaneStatuses.getViewport().setBackground(Color.white);
 	}
 
 	private TableColumn configureTableColumns(JTable table) {
 		table.setSelectionMode(
 				ListSelectionModel.SINGLE_SELECTION);
 		// TODO: set row sorter after searches 
 		// watch out for interaction with select all
 		// table.setAutoCreateRowSorter(true);
 		table.getColumnModel().getColumn(0).setMaxWidth(80);
 		table.getTableHeader().setReorderingAllowed(false);
 		table.getTableHeader().setResizingAllowed(false);
 		TableColumn tc = table.getColumnModel().getColumn(0);
 		tc.setCellEditor(table.getDefaultEditor(Boolean.class));
 		tc.setCellRenderer(table.getDefaultRenderer(Boolean.class));
 		return tc;
 	}
 
	class ListSelectionListenerImpl implements ListSelectionListener {
 		public void valueChanged(ListSelectionEvent e) {
 			ListSelectionModel lsm = (ListSelectionModel)e.getSource();
 			if(lsm.isSelectionEmpty()){
			} else{
				if((boolean) table_selection.getValueAt
						(table_selection.getSelectedRow(), 0)){
					table_selection.setValueAt(
							new Boolean(false), table_selection.getSelectedRow(), 0);
				}
				else{
					table_selection.setValueAt(
							new Boolean(true), table_selection.getSelectedRow(), 0);
 				}
 			}
 		}
 	}
 }
 
 

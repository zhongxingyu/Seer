 package com.vetapp.customer;
 
 /*
  * CustomersGUI.java
  * A GUI class that displays the main customer list.
  * End user can search for, create or display a customer record.
  */
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.DefaultRowSorter;
 import javax.swing.GroupLayout;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 import javax.swing.RowSorter;
 import javax.swing.SortOrder;
 import javax.swing.border.Border;
 import javax.swing.border.EtchedBorder;
 import javax.swing.table.DefaultTableModel;
 
 import com.vetapp.main.ShopGUI;
 import com.vetapp.main.VetApp;
 
 //import com.vetapp.customer.CreateCustomerGUI.cancelButtonListener;
 //import com.vetapp.customer.CreateCustomerGUI.createButtonListener;
 //import com.vetapp.shop.ShopGUI;
 
 public class CustomersGUI extends JFrame implements ActionListener {
 
 	//JButton labels strings declared as constants
 	private static String NEW_BUTTON_LABEL = "New Customer";
 	private static String SELECT_BUTTON_LABEL = "Select Customer";
 	private static String BACK_BUTTON_LABEL = "Back";
 
 	private JTextField searchTxt;
 	private JButton newBtn;
 	private JButton selectBtn;	//***temporary solution***
 	private JButton backBtn;
 	private JTable customerTbl;
 
 	private JPanel upperPnl;	//containing controPnl
 	private JPanel tablePnl;	//containing customerTbl
 	private JPanel lowerPnl;	//containing buttonPnl
 	private JPanel buttonPnl;	//containing selectBtn & backBtn
 	private JPanel controlPnl;	//containing searchTxt & newBtn
 
 	private BorderLayout paneLayout;
 	private BoxLayout upperLayout; 
 	private BoxLayout lowerLayout;
 	private BoxLayout buttonLayout;
 	private BoxLayout tableLayout;
 	private BoxLayout controlLayout;
 	public List <Customer> customers = new ArrayList <Customer> () ; //Dhmiourgia listas pelatwn
 
 	public static MyTableModel model = new MyTableModel();
 
 	public static SimpleDateFormat ft = new SimpleDateFormat ("yyyy-MM-dd hh:mm:ss");
 	public static SimpleDateFormat displayDateFormat = new SimpleDateFormat ("EEE dd-MM-yyyy 'at' hh:mm");
 
 	public CustomersGUI(List<Customer> resList) {
 
 		//List<Customer> list = resList;
 		//Frame configuration
 		customers = resList;
 		setResizable(false);
 		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 		setTitle(VetApp.MAIN_WINDOW_TITLE + " - " + this.getClass().getName());	//gets window title from constant in com.vetapp.main.VetApp
 
 		upperPnl = new JPanel();	
 		lowerPnl = new JPanel();	
 		tablePnl = new JPanel();	
 
 		//contentPane layout (BorderLayout)
 		paneLayout = new BorderLayout();
 		getContentPane().setLayout(paneLayout);
 
 		getContentPane().add(upperPnl, BorderLayout.NORTH);
 		getContentPane().add(tablePnl, BorderLayout.CENTER);
 		getContentPane().add(lowerPnl, BorderLayout.SOUTH);
 
 		//upperPnl layout (vertical BoxLayout)
 		controlPnl = new JPanel();
 		upperLayout = new BoxLayout(upperPnl, BoxLayout.Y_AXIS);
 		upperPnl.setLayout(upperLayout);
 
 		upperPnl.add(Box.createRigidArea(new Dimension(0, 10)));
 		upperPnl.add(controlPnl);
 		upperPnl.add(Box.createRigidArea(new Dimension(0, 10)));
 
 		//controlPnl layout (horizontal BoxLayout) 
 		searchTxt = new JTextField("Search...",10);
 		newBtn = new JButton(NEW_BUTTON_LABEL);
 		controlLayout = new BoxLayout(controlPnl, BoxLayout.X_AXIS);
 		controlPnl.setLayout(controlLayout);
 
 		controlPnl.add(Box.createRigidArea(new Dimension(10, 0)));
 		controlPnl.add(searchTxt);
 		controlPnl.add(Box.createRigidArea(new Dimension(60, 0)));
 		controlPnl.add(newBtn);
 		controlPnl.add(Box.createRigidArea(new Dimension(10, 0)));
 
 		//JTable configuration
 
 		customerTbl = new JTable(model);
 		model.reloadJTable(customers);
 		customerTbl.setAutoCreateRowSorter(true);									//enable row sorters						
 
 		DefaultRowSorter sorter = ((DefaultRowSorter)customerTbl.getRowSorter());	//default sort by Last Name
 		ArrayList list = new ArrayList();
 		list.add( new RowSorter.SortKey(0, SortOrder.ASCENDING));
 		sorter.setSortKeys(list);
 		sorter.sort();
 
 		customerTbl.getColumnModel().getColumn(0).setPreferredWidth(100);	//set Last Name column preferred width
 		customerTbl.getColumnModel().getColumn(1).setPreferredWidth(80);	//set First Name column preferred width
 		customerTbl.getColumnModel().getColumn(2).setPreferredWidth(150);	//set Last Visit column preferred width   
 
 		//tablePnl layout (horizontal BoxLayout)
 		JScrollPane scrollPane = new JScrollPane(customerTbl);
 		customerTbl.setFillsViewportHeight(true);
 		scrollPane.setPreferredSize(new Dimension(350,250));
 
 		tableLayout = new BoxLayout(tablePnl, BoxLayout.X_AXIS);
 		tablePnl.setLayout(tableLayout);
 
 		tablePnl.add(Box.createRigidArea(new Dimension(30, 0)));
 		tablePnl.add(scrollPane);
 		tablePnl.add(Box.createRigidArea(new Dimension(30, 0)));
 
 		//buttonPnl layout (horizontal BoxLayout)					
 		backBtn = new JButton(BACK_BUTTON_LABEL);					//
 		selectBtn = new JButton(SELECT_BUTTON_LABEL);				//	
 		buttonPnl = new JPanel();									//
 		buttonLayout = new BoxLayout(buttonPnl,BoxLayout.X_AXIS);	//																		//Proswrini lysi mexri na 
 		buttonPnl.add(Box.createRigidArea(new Dimension(50, 0)));	//diorthwthei to thema me to 
 		buttonPnl.add(selectBtn);									//diplo click sto JTable.
 		buttonPnl.add(Box.createRigidArea(new Dimension(15, 0)));	//
 		buttonPnl.add(backBtn);										//
 		buttonPnl.add(Box.createRigidArea(new Dimension(50, 0)));	//
 
 		//lowerPnl layout (vertical BoxLayout)	    
 		lowerLayout = new BoxLayout(lowerPnl,BoxLayout.Y_AXIS);
 		lowerPnl.setLayout(lowerLayout);	    
 
 		lowerPnl.add(Box.createRigidArea(new Dimension(0, 10)));
 		lowerPnl.add(buttonPnl);
 		lowerPnl.add(Box.createRigidArea(new Dimension(0, 10)));
 
 		//backBtn.setAlignmentX(Component.CENTER_ALIGNMENT);	//set "Back" JButton alignment to CENTER
 
 		//ActionListeners
 		newBtn.addActionListener(this);
 		selectBtn.addActionListener(this);
 		backBtn.addActionListener(this);
 		searchTxt.addActionListener(this);
 
 		//MouseAdapter
 		customerTbl.addMouseListener(new MouseAdapter() {
 			public void mouseClicked(MouseEvent e) {
 				if (e.getClickCount() == 2) {
 					//System.out.println("Double click detected!");
 					//System.out.println("Customer on " + customerTbl.getSelectedRow() + " row selected!");
 					int row = customerTbl.getSelectedRow();
 					for(int i=0; i< customers.size(); i++){
 						if (customers.get(i).getLastName()==customerTbl.getValueAt(row, 0) 
 								&& customers.get(i).getFirstName()==customerTbl.getValueAt(row, 1)) {
 							new CustomerGUI(customers.get(i),customers);
 						}
 					}
 				}
 			}
 		});
 
 		//Pack, Center & Enable visibility for JFrame & all containers
 		pack();
 		setLocationRelativeTo(null);
 		setVisible(true);
 		getContentPane().setVisible(true);
 		upperPnl.setVisible(true);
 		tablePnl.setVisible(true);
 		lowerPnl.setVisible(true);
 		buttonPnl.setVisible(true);
 		controlPnl.setVisible(true);
 	}
 
 	public void setCustomer(Customer aCustomer)
 	{
 		customers.add(aCustomer);
 	}
 
 	public List<Customer> getCustomersList() {
 		return customers;
 
 	}
 
 	//-------------------- ACTION LISTENERS (CUSTOMERS GUI) ------------------------
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if (e.getActionCommand().equals(BACK_BUTTON_LABEL)) {
 			new ShopGUI();
 			this.dispose();
 		} else if (e.getActionCommand().equals(NEW_BUTTON_LABEL)) {
 			new CreateCustomerGUI();
 		} else if (e.getActionCommand().equals(SELECT_BUTTON_LABEL)) {
 			int row = customerTbl.getSelectedRow();
 			for(int i=0; i< customers.size(); i++){
 				if (customers.get(i).getLastName()==customerTbl.getValueAt(row, 0) 
 						&& customers.get(i).getFirstName()==customerTbl.getValueAt(row, 1)) {
 					new CustomerGUI(customers.get(i), customers);
 				}
 			}
 			
 		} else if (e.getSource()==searchTxt) {
 			String value = searchTxt.getText();
 			for (int row = 0; row <= customerTbl.getRowCount() - 1; row++) {
 				for (int col = 0; col <= customerTbl.getColumnCount() - 1; col++) {
 					if (value.equalsIgnoreCase(customerTbl.getValueAt(row, col).toString())) {
 						// this will automatically set the view of the scroll in the location of the value
 						customerTbl.scrollRectToVisible(customerTbl.getCellRect(row, 0, true));
 						// this will automatically set the focus of the searched/selected row/value
 						customerTbl.setRowSelectionInterval(row, row);
 					}
 				}
 			}
 		}
 	}
 
 	//-------------------- CUSTOMER JTABLE MODEL ------------------------
 
 	public static class MyTableModel extends DefaultTableModel {
 
 		private String[] columnNames = {"Last Name","First Name","Next Visit"};		//column header labels
 		private Object[][] data = new Object[100][3];
 
 		public void reloadJTable(List<Customer> list) {
 			clearJTable();
 			for(int i=0; i< list.size(); i++){
 				data[i][0] = list.get(i).getLastName();
 				data[i][1] = list.get(i).getFirstName();
 				if (list.get(i).getNextVisit()==null) {
 					data[i][2] = "NOT SET";
 				} else {
 					String date = displayDateFormat.format(list.get(i).getNextVisit().getTime());
 					data[i][2] = date;
 				}
 				model.addRow(data);
 			}
 		}
 
 		public void clearJTable() {
 			model.setRowCount(0);
 		}
 
 		public String getColumnName(int col) {
 			return columnNames[col];
 		}
 
 		public Object getValueAt(int row, int col) {
 			return data[row][col];
 		}
 
 		@Override
 		public int getColumnCount() {
 			return columnNames.length;
 		}
 		/*
 		 * JTable uses this method to determine the default renderer/
 		 * editor for each cell.  If we didn't implement this method,
 		 * then the last column would contain text ("true"/"false"),
 		 * rather than a check box.
 		 */
 		//        public Class getColumnClass(int c) {
 		//            return getValueAt(0, c).getClass();
 		//        }
 
 		/*
 		 * Don't need to implement this method unless your table's
 		 * editable.
 		 */ 
 		public boolean isCellEditable(int row, int col) {
 			//Note that the data/cell address is constant,
 			//no matter where the cell appears onscreen.
 			if (col < 2) {
 				return false;
 			} else {
 				return true;
 			}
 		}
 	}
 
 	//====================== CREATE CUSTOMER GUI CLASS ====================== 
 
 	public class CreateCustomerGUI extends JFrame implements ActionListener {         
 
 		//Title and JButton label strings declared as constants
 		private static final String CREATE_BUTTON_LABEL = "Create";
 		private static final String CANCEL_BUTTON_LABEL = "Cancel";
 		private static final String TITLE_LABEL = "Create New Customer";
 
 		private JLabel titleLlb;
 		private JLabel fNameLbl;
 		private JLabel lNameLbl;
 		private JLabel addrLbl;
 		private JLabel hPhoneLbl;
 		private JLabel mPhoneLbl;
 
 		private JTextField fNameTxt;
 		private JTextField lNameTxt;
 		private JTextField addrTxt;
 		private JTextField hPhoneTxt;
 		private JTextField mPhoneTxt;
 
 		private JPanel mainPnl;		//containing inputPnl & controlPnl
 		private JPanel inputPnl;	//containing input JLabels & JTextFields		
 		private JPanel controlPnl;	//containing control JButtons (createBtn & cancelBtn)
 		private JButton createBtn;	
 		private JButton cancelBtn;
 
 		private BoxLayout paneLayout;		//contentPane layout
 		private BoxLayout mainLayout; 		//mainPnl layout
 		private BoxLayout controlLayout;	//controlPnl layout
 
 		private Border loweredetched;	//border type of inputPnl
 
 		private Customer aCustomer;
 		private boolean flag = true;
 
 		public CreateCustomerGUI() {
 
 			//Frame configuration
 			setResizable(false);
 			setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 			setTitle(VetApp.MAIN_WINDOW_TITLE + " - " + this.getClass().getName());	//gets window title from constant in com.vetapp.main.VetApp
 
 			//contentPane layout (horizontal BoxLayout)
 			mainPnl = new JPanel();
 			paneLayout = new BoxLayout(getContentPane(), BoxLayout.X_AXIS);
 			getContentPane().setLayout(paneLayout);
 
 			getContentPane().add(Box.createRigidArea(new Dimension(15, 0)));
 			getContentPane().add(mainPnl);
 			getContentPane().add(Box.createRigidArea(new Dimension(15, 0)));
 
 			//mainPnl layout (vertical BoxLayout)
 			titleLlb = new JLabel(TITLE_LABEL);
 			inputPnl = new JPanel();
 			controlPnl = new JPanel();
 			mainLayout = new BoxLayout(mainPnl,BoxLayout.PAGE_AXIS);
 			mainPnl.setLayout(mainLayout);
 
 			mainPnl.add(Box.createRigidArea(new Dimension(0, 10)));
 			mainPnl.add(titleLlb);
 			mainPnl.add(Box.createRigidArea(new Dimension(0, 10)));
 			mainPnl.add(inputPnl);
 			mainPnl.add(Box.createRigidArea(new Dimension(0, 10)));
 			mainPnl.add(controlPnl);
 			mainPnl.add(Box.createRigidArea(new Dimension(0, 10)));
 
 			titleLlb.setAlignmentX(Component.CENTER_ALIGNMENT);	//set title JLabel alignment to CENTER
 
 			//inputPnl layout (GroupLayout [2x5])
 			fNameLbl = new JLabel("First Name*:");
 			lNameLbl = new JLabel("Last Name*:");
 			addrLbl = new JLabel("Address:");
 			hPhoneLbl = new JLabel("Home Phone:");
 			mPhoneLbl = new JLabel("Mobile Phone:");
 			fNameTxt = new JTextField(10);
 			lNameTxt = new JTextField(10);
 			addrTxt = new JTextField(10);
 			hPhoneTxt = new JTextField(10);
 			mPhoneTxt = new JTextField(10);
 			GroupLayout inputLayout = new GroupLayout(inputPnl);
 			inputPnl.setLayout(inputLayout);
 
 			inputLayout.setAutoCreateGaps(true);			//enable gaps between JLabels & JTextFields
 			inputLayout.setAutoCreateContainerGaps(true);	//enable margin-gaps
 
 			inputLayout.setHorizontalGroup(
 					inputLayout.createSequentialGroup()
 					.addGroup(inputLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
 							.addComponent(fNameLbl)
 							.addComponent(lNameLbl)
 							.addComponent(addrLbl)
 							.addComponent(hPhoneLbl)
 							.addComponent(mPhoneLbl))
 							.addGroup(inputLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
 									.addComponent(fNameTxt)
 									.addComponent(lNameTxt)
 									.addComponent(addrTxt)
 									.addComponent(hPhoneTxt)
 									.addComponent(mPhoneTxt))
 					);
 			inputLayout.setVerticalGroup(
 					inputLayout.createSequentialGroup()
 					.addGroup(inputLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
 							.addComponent(fNameLbl)
 							.addComponent(fNameTxt))
 							.addGroup(inputLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
 									.addComponent(lNameLbl)
 									.addComponent(lNameTxt))
 									.addGroup(inputLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
 											.addComponent(addrLbl)
 											.addComponent(addrTxt))
 											.addGroup(inputLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
 													.addComponent(hPhoneLbl)
 													.addComponent(hPhoneTxt))
 													.addGroup(inputLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
 															.addComponent(mPhoneLbl)
 															.addComponent(mPhoneTxt))
 					);
 
 			loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);	
 			inputPnl.setBorder(loweredetched);		//set a visible "Lowered-Etched" border for inputPnl
 
 			//controlPnl layout (horizontal BoxLayout)
 			createBtn = new JButton(CREATE_BUTTON_LABEL);
 			cancelBtn = new JButton(CANCEL_BUTTON_LABEL);
 			controlLayout = new BoxLayout(controlPnl, BoxLayout.X_AXIS);
 			controlPnl.setLayout(controlLayout);
 
 			controlPnl.add(Box.createRigidArea(new Dimension(30,0)));
 			controlPnl.add(createBtn);
 			controlPnl.add(Box.createRigidArea(new Dimension(20,0)));
 			controlPnl.add(cancelBtn);
 			controlPnl.add(Box.createRigidArea(new Dimension(30,0)));
 
 			//Pack, Center & Enable visibility for JFrame & all containers
 			pack();
 			setVisible(true);
 			setLocationRelativeTo(null);
 			getContentPane().setVisible(true);
 			mainPnl.setVisible(true);
 			inputPnl.setVisible(true);
 			controlPnl.setVisible(true);
 
 			//ActionListeners
 			createBtn.addActionListener(this);
 			cancelBtn.addActionListener(this);
 		}
 
 		public Customer getaCustomer ()
 		{
 			return aCustomer;         
 		}
 
 		public boolean getFlag()
 		{
 			return flag;
 		}
 
 		//-------------------- ACTION LISTENERS (CREATE CUSTOMER GUI) ------------------------
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			// TODO Auto-generated method stub
 			
			//fixed this
 			if (e.getActionCommand().equals(CREATE_BUTTON_LABEL)) {
				if  ((fNameTxt.getText().trim()!=null)&&(!fNameTxt.getText().trim().isEmpty())&&(lNameTxt.getText().trim()!=null)&&(!lNameTxt.getText().trim().isEmpty())) {
					//elegxos ean to First Name h Last Name einai adeia h exoun mono kena
					//h methodos trim() afairei leading kai trailing kena apo ta strings
					aCustomer = new Customer( fNameTxt.getText().trim(),lNameTxt.getText().trim(),addrTxt.getText(),hPhoneTxt.getText(),mPhoneTxt.getText());  //Dhmiourgia pelath
					//flag = false;
					JOptionPane information = new JOptionPane();
					information.showMessageDialog(null,"Customer Added!");   	// Emfanish mhnymatos epityxias
					VetApp.db.DBCreateCustomer(aCustomer);						// Eisagwgh tou pelath sth vasi
					CustomersGUI.this.setCustomer(aCustomer);     				// Eisagwgh tou pelath sth lista pelatwn ths klasshs customersGUI
					model.reloadJTable(customers);								// Epanafortwsi tis listas pelatwn tou CustomersGUI
					this.dispose();												// Kleisimo frame
				}
				else
				{
					JOptionPane error = new JOptionPane();						// Emfanish mhnymatos sfalmatos
					error.showMessageDialog(null,
						    "First and Last name are required",
						    "Warning",
						    JOptionPane.WARNING_MESSAGE);
				}
				//end of fix											// Kleisimo frame
 
 			} else if (e.getActionCommand().equals(CANCEL_BUTTON_LABEL)) {
 				this.dispose();
 			}
 		}
 	}
 }

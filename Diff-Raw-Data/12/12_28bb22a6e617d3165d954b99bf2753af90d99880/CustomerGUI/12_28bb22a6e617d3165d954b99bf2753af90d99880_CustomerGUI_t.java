 package com.vetapp.customer;
 
 import javax.swing.BorderFactory;
 import javax.swing.ButtonGroup;
 import javax.swing.DefaultRowSorter;
 import javax.swing.JFormattedTextField;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.JSeparator;
 import javax.swing.JTextField;
 import javax.swing.RowSorter;
 import javax.swing.SortOrder;
 import javax.swing.SwingConstants;
 import javax.swing.UIManager;
 import javax.swing.border.Border;
 import javax.swing.border.EmptyBorder;
 import javax.swing.border.EtchedBorder;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.JLabel;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import com.jgoodies.forms.layout.FormLayout;
 import com.jgoodies.forms.layout.ColumnSpec;
 import com.jgoodies.forms.factories.FormFactory;
 import com.jgoodies.forms.layout.RowSpec;
 import com.vetapp.customer.CustomersGUI.*;
 import com.vetapp.main.VetApp;
 import com.vetapp.pet.Pet;
 import com.vetapp.pet.PetGUI;
 import com.vetapp.util.PropetJMenuBar;
 
 import javax.swing.JButton;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.Font;
 import javax.swing.JTable;
 import javax.swing.JScrollPane;
 import javax.swing.BoxLayout;
 
 import org.jdesktop.xswingx.PromptSupport;
 import org.jdesktop.xswingx.PromptSupport.FocusBehavior;
 
 import java.awt.GridLayout;
 
 
 public class CustomerGUI extends JFrame implements ActionListener {
 	//Simeiosi: xrisimopoio tin default grammatoseira, i opoia stin ektelesi fainetai ligo megaluteri ap' oti i8ela. 
 
 	private JPanel contentPane;
 	private JTable petTable;
 	private Customer customer = new Customer();
 	private List<Pet> petList = new ArrayList<Pet>();		
 	private JFormattedTextField nextVisit; 						//to be available in NextVisit & refresh on changes
 	public MyPetTableModel petModel = new MyPetTableModel();
 	public SimpleDateFormat ft = new SimpleDateFormat ("yyyy-MM-dd hh:mm");
 	private PropetJMenuBar bar = new PropetJMenuBar();
 
 
 	public CustomerGUI(Customer cus) {
 
 		//passing & getting required objects
 		customer = cus;
 		petList = VetApp.db.DBGetAllPets(customer);
 		
 		//JMenuBar
 		setJMenuBar(bar.drawJMenuBar());
 		
 		Border loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED); //to default frame border gia ta panels me perigramma
 		setBounds(100, 100, 415, 360);
 		contentPane = new JPanel();
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		setContentPane(contentPane);
 		contentPane.setLayout(null);
 		setTitle(VetApp.MAIN_WINDOW_TITLE + " - " + this.getClass().getName());	//gets window title from constant in com.vetapp.main.VetApp
 
 		//-------------------- CUSTOMER INFO PANEL ------------------------
 
 		JPanel customerInfo_panel = new JPanel(); 		
 		customerInfo_panel.setBounds(10, 11, 191, 150);
 		customerInfo_panel.setBorder(BorderFactory.createTitledBorder(loweredetched, "Customer Info"));
 		contentPane.add(customerInfo_panel);
 		customerInfo_panel.setLayout(new GridLayout(5, 0, 0, 0));
 
 		JLabel lastName = new JLabel(customer.getLastName());
 		customerInfo_panel.add(lastName);
 
 		//Dedomena tou xristi (8a antikatastountai me ali8ina dedomena kata tin dimiourgia tou frame)
 		JLabel firstName = new JLabel(customer.getFirstName());
 		customerInfo_panel.add(firstName);
 
 		JLabel address = new JLabel(customer.getAddress());
 		customerInfo_panel.add(address);
 
 		JLabel homeNumber = new JLabel(customer.getHomeNumber());
 		customerInfo_panel.add(homeNumber);
 
 		JLabel mobileNumber = new JLabel(customer.getMobileNumber());
 		customerInfo_panel.add(mobileNumber);
 
 		//-------------------- VISITS PANEL ------------------------
 
 		JPanel visits_panel = new JPanel();				
 		visits_panel.setBounds(10, 172, 191, 60);
 		visits_panel.setBorder(BorderFactory.createTitledBorder(loweredetched, "Next/Last Visit"));
 		contentPane.add(visits_panel);
 		visits_panel.setLayout(null);
 
 		nextVisit = new JFormattedTextField(new SimpleDateFormat("dd/MM/yyyy"));
 		nextVisit.setFont(new Font("Tahoma", Font.PLAIN, 12));
 		if (customer.getNextVisit()==null)
 			nextVisit.setText("_ _ /_ _ /_ _ _ _ ,  _ _ : _ _");
 		else
 			nextVisit.setText(ft.format(customer.getNextVisit().getTime()));
 		nextVisit.setEditable(false);
 		nextVisit.setBorder(javax.swing.BorderFactory.createEmptyBorder());
 		nextVisit.setBounds(10, 23, 170, 26);
 		visits_panel.add(nextVisit);
 
 		JButton createcancelAppointmentButton = new JButton("Create/Cancel Appointment");	//Create/Cancel app. button (8elei auction listener)
 		createcancelAppointmentButton.addActionListener(this);
 		createcancelAppointmentButton.setBounds(10, 235, 191, 28);
 		contentPane.add(createcancelAppointmentButton);
 
 		JButton deleteCustomerButton = new JButton("Delete Customer (!)");			//Delete customer button (8elei auction listener)
 		deleteCustomerButton.addActionListener(this);
 		deleteCustomerButton.setBounds(10, 271, 145, 28);
 		contentPane.add(deleteCustomerButton);
 
 		JButton editCustomerButton = new JButton("Edit Customer");					//Edit customer button (8elei auction listener)
 		editCustomerButton.addActionListener(this);
 		editCustomerButton.setBounds(165, 271, 130, 28);
 		contentPane.add(editCustomerButton);
 
 		//-------------------- PET TABLE --------------------
 
 		//String[] columnNames = {"Photo","Name"};
 		//Object[][] petData = { {"<pic0>", "Rex"}, {"<pic1>", "Epameinwndas"} };	 //Ta dedomena ton pet lamvanontai apo alli klasi kata tin dimiourgia tou frame
 
 		petTable = new JTable(petModel);
 
 		petModel.reloadPetJTable(customer);
 		petTable.setAutoCreateRowSorter(true);									//enable row sorters						
 
 		DefaultRowSorter sorter = ((DefaultRowSorter)petTable.getRowSorter());	//default sort by Last Name
 		ArrayList sortlist = new ArrayList();
 		sortlist.add( new RowSorter.SortKey(1, SortOrder.ASCENDING));
 		sorter.setSortKeys(sortlist);
 		sorter.sort();
 
 		petTable.getColumnModel().getColumn(0).setPreferredWidth(100);	//set Photo column preferred width
 		petTable.getColumnModel().getColumn(1).setPreferredWidth(80);	//set Pet name column preferred width
 
 		JScrollPane scrollPane = new JScrollPane();
 		scrollPane.setBounds(211, 11, 184, 150);
 		contentPane.add(scrollPane);
 		scrollPane.setViewportView(petTable);
 
 		JButton newPetButton = new JButton("New Pet");      //New Pet button (8elei auction listener)
 		newPetButton.addActionListener(this);
 		newPetButton.setBounds(258, 165, 89, 28);
 		contentPane.add(newPetButton);
 
 		JButton backButton = new JButton("Back");			//Back button (8elei auction listener)
 		backButton.addActionListener(this);
 		backButton.setBounds(305, 271, 89, 28);
 		contentPane.add(backButton);
 
 		//MouseAdapter gia JTable
 		petTable.addMouseListener(new MouseAdapter() {
 			public void mouseClicked(MouseEvent e) {
 				if (e.getClickCount() == 2) {
 					int row = petTable.getSelectedRow();
					System.out.println("double click! " + petTable.getValueAt(row, 1).toString());
 					for(int i=0; i< petList.size(); i++){
 						if (petList.get(i).getName().equals(petTable.getValueAt(row, 1).toString())) {
 							new PetGUI(customer, petList.get(i));
 						}
 					}
 				}
 			}
 		});
 
 		this.setResizable(false);
 		this.setVisible(true);
 		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 
 	}
 
 	//----------------------------- CustomerGUI ACTION LISTENERS ------------------------------------------
 
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		// TODO Auto-generated method stub
 
 		if (e.getActionCommand().equals("Create/Cancel Appointment")) {
 			new NextVisitGUI();
 		} else if (e.getActionCommand().equals("Delete Customer (!)")) {
 			Object[] options = {"Yes, delete this customer.",
 			"No way!"};
 			int n = JOptionPane.showOptionDialog(this,
 					"Are you sure you want to delete this customer record?\n"
 							+ "Warning: this action cannot be undone!",
 							"Confirm Customer Deletion",
 							JOptionPane.YES_NO_OPTION,
 							JOptionPane.WARNING_MESSAGE,
 							null,     //no custom icon
 							options,  //the titles of buttons
 							options[0]); //default button title
 			if (n == JOptionPane.YES_OPTION) {
 				VetApp.db.DBDeleteCustomer(customer);
 				com.vetapp.customer.CustomersGUI.model.reloadJTable();
 				this.dispose();
 			}
 		} else if (e.getActionCommand().equals("Edit Customer")) {
 			new editCustomerGUI();
 			CustomerGUI.this.dispose();
 		} else if (e.getActionCommand().equals("New Pet")) {
 			new createPetGUI(customer);
 		} else if (e.getActionCommand().equals("Back")) {
 			this.dispose();
 		}
 	}
 
 	//-------------------- PET JTABLE MODEL ------------------------
 
 	public static class MyPetTableModel extends DefaultTableModel {
 
 		private String[] columnNames = {"Photo","Pet Name"};		//column header labels
 		private Object[][] data = new Object[100][2];
 
 		public void reloadPetJTable(Customer cus) {
 			//System.out.println("loading pet table #2: " + list.get(0).getName());
 			List<Pet> list = new ArrayList<Pet>();
 			list = VetApp.db.DBGetAllPets(cus);
 			clearJTable();
 			for(int i=0; i<list.size(); i++){
 				if (list.get(i).getPhotoPath() == null) {
 					data[i][0] = "NO_PHOTO";
 				} else {
 					data[i][0] = list.get(i).getPhotoPath();
 				}
 				data[i][1] = list.get(i).getName();
 				this.addRow(data);
 			}
 			System.out.println("loading pet table..");
 		}
 
 		public void clearJTable() {
 			this.setRowCount(0);
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
 
 	//============================================================================================
 	//--------------------------------- createPetGUI CLASS ---------------------------------------
 	//============================================================================================
 
 
 	//Odhgia pros synaderfous : Dhmiourghsa to parakatw frame me font times new Roman 14
 	public class createPetGUI extends JFrame implements ActionListener {
 
 		private JPanel contentPane;
 		private JTextField speciesTxt;   //Species
 		private JTextField nameTxt; //Name
 		private	JRadioButton maleRButton;  // Gender
 		private	JRadioButton femaleRButton;// Gender
 		private JTextField dayTxt;    // Birth Day
 		private JTextField monthTxt;   // Birth Month
 		private JTextField yearTxt; // Birth Year
 		private JTextField furColorTxt; // Fur Colour
 		private JTextField spCharactTxt;  // Special Characteristics
 		private JTextField chipNumTxt;  // Chip Number
 		private JButton createButton = new JButton("Create");
 		private JButton cancelButton = new JButton("Cancel");
 		private Customer cust ; //  Deikths pelath ston opoio tha anhkei to pet
 
 		public createPetGUI(Customer aCustomer) {
 			Border loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
 
 			cust = new Customer();
 			cust = aCustomer;
 			setBounds(100, 100, 337, 320);
 			contentPane = new JPanel();
 			contentPane.setBackground(UIManager.getColor("menu")); //Background colour
 			contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 			setContentPane(contentPane);
 			contentPane.setLayout(null);
 			setTitle(VetApp.MAIN_WINDOW_TITLE + " - " + this.getClass().getName());	//gets window title from constant in com.vetapp.main.VetApp
 
 
 			JLabel createPetLabel = new JLabel("Create New Pet");
 			createPetLabel.setHorizontalAlignment(SwingConstants.CENTER);
 			createPetLabel.setBounds(10, 11, 301, 9);
 			contentPane.add(createPetLabel);
 
 			createButton.setBounds(77, 246, 78, 25);
 			contentPane.add(createButton);
 
 			cancelButton.setBounds(165, 246, 78, 25);
 			contentPane.add(cancelButton);
 
 			JPanel petInfoPane = new JPanel();
 			petInfoPane.setBounds(10, 31, 300, 202);
 			petInfoPane.setBorder(BorderFactory.createTitledBorder(loweredetched));
 			contentPane.add(petInfoPane);
 			petInfoPane.setLayout(new FormLayout(new ColumnSpec[] {
 					FormFactory.RELATED_GAP_COLSPEC,
 					FormFactory.DEFAULT_COLSPEC,
 					FormFactory.RELATED_GAP_COLSPEC,
 					FormFactory.DEFAULT_COLSPEC,
 					FormFactory.RELATED_GAP_COLSPEC,
 					FormFactory.DEFAULT_COLSPEC,
 					FormFactory.RELATED_GAP_COLSPEC,
 					ColumnSpec.decode("default:grow"),
 					FormFactory.RELATED_GAP_COLSPEC,
 					FormFactory.DEFAULT_COLSPEC,
 					FormFactory.RELATED_GAP_COLSPEC,
 					ColumnSpec.decode("default:grow"),
 					FormFactory.RELATED_GAP_COLSPEC,
 					FormFactory.DEFAULT_COLSPEC,
 					FormFactory.RELATED_GAP_COLSPEC,
 					FormFactory.DEFAULT_COLSPEC,
 					FormFactory.RELATED_GAP_COLSPEC,
 					FormFactory.DEFAULT_COLSPEC,
 					FormFactory.RELATED_GAP_COLSPEC,
 					FormFactory.DEFAULT_COLSPEC,},
 					new RowSpec[] {
 					FormFactory.RELATED_GAP_ROWSPEC,
 					FormFactory.DEFAULT_ROWSPEC,
 					FormFactory.RELATED_GAP_ROWSPEC,
 					FormFactory.DEFAULT_ROWSPEC,
 					FormFactory.RELATED_GAP_ROWSPEC,
 					FormFactory.DEFAULT_ROWSPEC,
 					FormFactory.RELATED_GAP_ROWSPEC,
 					RowSpec.decode("default:grow"),
 					FormFactory.RELATED_GAP_ROWSPEC,
 					RowSpec.decode("default:grow"),
 					FormFactory.DEFAULT_ROWSPEC,
 					FormFactory.RELATED_GAP_ROWSPEC,
 					FormFactory.DEFAULT_ROWSPEC,
 					FormFactory.RELATED_GAP_ROWSPEC,
 					FormFactory.DEFAULT_ROWSPEC,
 					FormFactory.RELATED_GAP_ROWSPEC,
 					FormFactory.DEFAULT_ROWSPEC,}));
 
 
 
 			JLabel lblSpecies = new JLabel("Species*:");
 			petInfoPane.add(lblSpecies, "2, 2");
 
 			speciesTxt = new JTextField();
 			petInfoPane.add(speciesTxt, "6, 2, 13, 1");
 			speciesTxt.setColumns(10);
 
 			JLabel lblName = new JLabel("Name*:");
 			petInfoPane.add(lblName, "2, 4");
 
 			nameTxt = new JTextField();
 			petInfoPane.add(nameTxt, "6, 4, 13, 1");
 			nameTxt.setColumns(10);
 
 			JLabel lblGender = new JLabel("Gender*:");
 			petInfoPane.add(lblGender, "2, 6");
 
 			maleRButton = new JRadioButton("Male");
 			petInfoPane.add(maleRButton, "6, 6, 4, 1");
 
 			femaleRButton = new JRadioButton("Female");
 			petInfoPane.add(femaleRButton, "10, 6, 11, 1");
 
 			ButtonGroup group = new ButtonGroup();
 			group.add(maleRButton);
 			group.add(femaleRButton);
 			maleRButton.setSelected(true);	//to default gender 8a einai male
 
 			JLabel lblDateOfBirth = new JLabel("Date Of Birth:");
 			petInfoPane.add(lblDateOfBirth, "2, 8");
 
 			JPanel panel = new JPanel();
 			petInfoPane.add(panel, "6, 7, 13, 4, fill, fill");
 			panel.setLayout(null);
 
 			dayTxt = new JTextField();
 			dayTxt.setHorizontalAlignment(SwingConstants.CENTER);
 			dayTxt.setBounds(0, 2, 25, 20);
 			panel.add(dayTxt);
 			PromptSupport.setPrompt("dd", dayTxt); 		//prompt text - using xswingx library
 			PromptSupport.setFocusBehavior(FocusBehavior.SHOW_PROMPT, dayTxt);
 			dayTxt.setColumns(10);
 
 			monthTxt = new JTextField();
 			monthTxt.setHorizontalAlignment(SwingConstants.CENTER);
 			monthTxt.setBounds(35, 2, 31, 20);
 			panel.add(monthTxt);
 			PromptSupport.setPrompt("mm", monthTxt); 		//prompt text - using xswingx library
 			PromptSupport.setFocusBehavior(FocusBehavior.SHOW_PROMPT, monthTxt);
 			monthTxt.setColumns(10);
 
 			yearTxt = new JTextField();
 			yearTxt.setHorizontalAlignment(SwingConstants.CENTER);
 			yearTxt.setBounds(77, 2, 44, 20);
 			panel.add(yearTxt);
 			PromptSupport.setPrompt("yyyy", yearTxt); 		//prompt text - using xswingx library
 			PromptSupport.setFocusBehavior(FocusBehavior.SHOW_PROMPT, yearTxt);
 			yearTxt.setColumns(10);
 
 			JLabel lblFurColour = new JLabel("Fur Colour:");
 			petInfoPane.add(lblFurColour, "2, 11");
 
 			furColorTxt = new JTextField();
 			petInfoPane.add(furColorTxt, "6, 11, 13, 1");
 			furColorTxt.setColumns(10);
 
 			JLabel lblSpecialCharacteristics = new JLabel("Special Characteristics:");
 			petInfoPane.add(lblSpecialCharacteristics, "2, 13");
 
 			spCharactTxt = new JTextField();
 			petInfoPane.add(spCharactTxt, "6, 13, 13, 1");
 			spCharactTxt.setColumns(10);
 
 			JLabel lblChipNumber = new JLabel("Chip Number:");
 			petInfoPane.add(lblChipNumber, "2, 15");
 
 			chipNumTxt = new JTextField();
 			petInfoPane.add(chipNumTxt, "6, 15, 13, 1");
 			chipNumTxt.setColumns(10);
 			this.setVisible(true);
 			this.setResizable(false);
 			this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
 
 			createButton.addActionListener(this);
 			cancelButton.addActionListener(this);
 
 		}
 
 		//----------------------------- createPetGUI ACTION LISTENERS ------------------------------------------
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 
 			// Dhmiourgia Pet
 			if (e.getActionCommand().equals("Create")) {
 				String species = speciesTxt.getText();
 				String name = nameTxt.getText();
 				String birthDay =  dayTxt.getText();
 				String birthMonth = monthTxt.getText();
 				String birthYear =  yearTxt.getText();
 				String furColour = furColorTxt.getText();
 				String special = spCharactTxt.getText();
 				String chip =  chipNumTxt.getText();
 				String gender;
 
 				// Kwdikas gia apaloifh twn kenwn
 				species.trim();
 				name.trim();
 				birthDay.trim();
 				birthMonth.trim();
 				birthYear.trim();
 				furColour.trim();
 				special.trim();
 				chip.trim();
 
 				if (maleRButton.isSelected()) 
 					gender = "Male";
 				else 
 					gender ="Female";
 
 				if(species.equals("")||name.equals(""))
 				{
 					JOptionPane.showMessageDialog(null, "Please fill the required fields", "Error", JOptionPane.ERROR_MESSAGE);
 				}
 				else 
 				{
 					int year = Integer.parseInt(birthYear);
 					int month = Integer.parseInt(birthMonth);
 					int day = Integer.parseInt(birthDay);
 
 					Calendar cl = Calendar.getInstance();
 
 					if(year<1900 || year>2013 ||  month>12 || month <0 || day <=0|| day >31 ) 
 					{
 						JOptionPane error = new JOptionPane();
 						error.showMessageDialog(null, "Conflicting types", "Error", JOptionPane.ERROR_MESSAGE);
 					}
 					else {
 
 						String birthDate = birthYear + "-" + birthMonth + "-" + birthDay + " 00:00:00"; //("yyyy-MM-dd hh:mm:ss")
 						Date date = null;
 						try {
 							date = ft.parse(birthDate);
 						} catch (ParseException e1) {
 							System.out.println("Error parsing pet birth date: " + e1.getMessage());
 						}
 						Calendar cal = new GregorianCalendar();
 						cal.setTime(date);
 
 						Pet pet = new Pet(species,name,gender,cal,furColour,special,chip);
 						cust.addPet(pet);
						pet = VetApp.db.DBCreatePet(cust, pet);						// Eisagwgh tou pet sth vasi
						petList.add(pet);
						petModel.reloadPetJTable(cust);								// Reload the PetTable
						JOptionPane.showMessageDialog(null,"Pet Added!");   	// Emfanish mhnymatos epityxias
 						createPetGUI.this.dispose();     // Kleisimo tou frame
 					}
 
 				}                          
 			} else if (e.getActionCommand().equals("Cancel")) {
 				this.dispose();
 
 			}
 		}
 	}
 
 
 	//============================================================================================
 	//------------------------------ editCustomerGUI CLASS ---------------------------------------
 	//============================================================================================
 
 	public class editCustomerGUI extends JFrame implements ActionListener {
 
 		private JPanel contentPane;
 		private JTable petTable;
 		private JTextField firstNameTxt;
 		private JTextField lastNameTxt;
 		private JTextField addressTxt;
 		private JTextField homePhoneTxt;
 		private JTextField mobilePhoneTxt;
 
 
 		public editCustomerGUI() {
 			Border loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED); //to default frame border gia ta panels me perigramma
 			setBounds(100, 100, 465, 360);
 			contentPane = new JPanel();
 			contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 			setContentPane(contentPane);
 			contentPane.setLayout(null);
 			setTitle(VetApp.MAIN_WINDOW_TITLE + " - " + this.getClass().getName());	//gets window title from constant in com.vetapp.main.VetApp
 
 
 			//-------------------- CUSTOMER INFO PANEL ------------------------
 
 			JPanel customerInfo_panel = new JPanel(); 		
 			customerInfo_panel.setBounds(10, 11, 274, 170);
 			customerInfo_panel.setBorder(BorderFactory.createTitledBorder(loweredetched, "Customer Info"));
 			contentPane.add(customerInfo_panel);
 			customerInfo_panel.setLayout(new FormLayout(new ColumnSpec[] {
 					FormFactory.RELATED_GAP_COLSPEC,
 					FormFactory.DEFAULT_COLSPEC,
 					ColumnSpec.decode("max(5dlu;default)"),
 					FormFactory.RELATED_GAP_COLSPEC,
 					ColumnSpec.decode("max(8dlu;default)"),
 					FormFactory.RELATED_GAP_COLSPEC,
 					FormFactory.DEFAULT_COLSPEC,
 					FormFactory.RELATED_GAP_COLSPEC,
 					ColumnSpec.decode("max(57dlu;default):grow"),
 					FormFactory.RELATED_GAP_COLSPEC,
 					ColumnSpec.decode("max(0dlu;default):grow"),},
 					new RowSpec[] {
 					FormFactory.RELATED_GAP_ROWSPEC,
 					FormFactory.DEFAULT_ROWSPEC,
 					FormFactory.RELATED_GAP_ROWSPEC,
 					FormFactory.DEFAULT_ROWSPEC,
 					FormFactory.RELATED_GAP_ROWSPEC,
 					FormFactory.DEFAULT_ROWSPEC,
 					FormFactory.RELATED_GAP_ROWSPEC,
 					FormFactory.DEFAULT_ROWSPEC,
 					FormFactory.RELATED_GAP_ROWSPEC,
 					FormFactory.DEFAULT_ROWSPEC,}));
 
 			//ta textfields pairnoun ta dedomena tou customer kata tin dimiourgia tou para8urou, eno apo8ukeuontai oi allages me to "save changes"
 			JLabel firstNameLabel = new JLabel("First Name*:");
 			customerInfo_panel.add(firstNameLabel, "2, 2");
 
 			firstNameTxt = new JTextField();
 			firstNameTxt.setText(customer.getFirstName());
 			customerInfo_panel.add(firstNameTxt, "4, 2, 7, 1, fill, default");
 			firstNameTxt.setColumns(10);
 
 			JLabel lastNameLabel = new JLabel("Last Name*:");
 			customerInfo_panel.add(lastNameLabel, "2, 4");
 
 			lastNameTxt = new JTextField();
 			lastNameTxt.setText(customer.getLastName());
 			customerInfo_panel.add(lastNameTxt, "4, 4, 7, 1, fill, default");
 			lastNameTxt.setColumns(10);
 
 			JLabel addressLabel = new JLabel("Address:");
 			customerInfo_panel.add(addressLabel, "2, 6");
 
 			addressTxt = new JTextField();
 			addressTxt.setText(customer.getAddress());
 			customerInfo_panel.add(addressTxt, "4, 6, 7, 1, fill, default");
 			addressTxt.setColumns(10);
 
 			JLabel homePhoneLabel = new JLabel("Home Phone:");
 			customerInfo_panel.add(homePhoneLabel, "2, 8");
 
 			homePhoneTxt = new JTextField();
 			homePhoneTxt.setText(customer.getHomeNumber());
 			customerInfo_panel.add(homePhoneTxt, "4, 8, 7, 1, fill, default");
 			homePhoneTxt.setColumns(10);
 
 			JLabel mobilePhoneLabel = new JLabel("Mobile Phone:");
 			customerInfo_panel.add(mobilePhoneLabel, "2, 10");
 
 			mobilePhoneTxt = new JTextField();
 			mobilePhoneTxt.setText(customer.getMobileNumber());
 			customerInfo_panel.add(mobilePhoneTxt, "4, 10, 7, 1, fill, default");
 			mobilePhoneTxt.setColumns(10);
 
 
 			//-------------------- VISITS PANEL ------------------------
 
 			JPanel visits_panel = new JPanel();				
 			visits_panel.setBounds(10, 185, 274, 62);
 			visits_panel.setBorder(BorderFactory.createTitledBorder(loweredetched, "Next/Last Visit"));
 			contentPane.add(visits_panel);
 
 			JFormattedTextField nextVisit = new JFormattedTextField(ft);
 			nextVisit.setBounds(10, 22, 254, 29);
 			nextVisit.setFont(new Font("Tahoma", Font.PLAIN, 12));
 			nextVisit.setBorder(javax.swing.BorderFactory.createEmptyBorder());
 			nextVisit.setEditable(false);
 
 			if (customer.getNextVisit()==null)
 				nextVisit.setText("_ _ /_ _ /_ _ _ _ ,  _ _ : _ _");
 			else
 				nextVisit.setText(ft.format(customer.getNextVisit().getTime()));
 
 			visits_panel.setLayout(null);
 			visits_panel.add(nextVisit);
 
 
 			JButton createcancelAppointmentButton = new JButton("Create/Cancel Appointment");	//Create/Cancel app. button (8elei auction listener)
 			createcancelAppointmentButton.setEnabled(false);
 			createcancelAppointmentButton.setBounds(10, 250, 273, 28);
 			contentPane.add(createcancelAppointmentButton);
 
 			JButton saveChangesButton = new JButton("Save Changes");					//Edit customer button (8elei auction listener)
 			saveChangesButton.addActionListener(this);
 			saveChangesButton.setBounds(147, 285, 136, 28);
 			contentPane.add(saveChangesButton);
 
 			//-------------------- PET TABLE --------------------
 
 			petTable = new JTable(petModel);
 
 			petModel.reloadPetJTable(customer);
 			petTable.setAutoCreateRowSorter(true);									//enable row sorters						
 
 			DefaultRowSorter sorter = ((DefaultRowSorter)petTable.getRowSorter());	//default sort by Last Name
 			ArrayList sortlist = new ArrayList();
 			sortlist.add( new RowSorter.SortKey(1, SortOrder.ASCENDING));
 			sorter.setSortKeys(sortlist);
 			sorter.sort();
 
 			petTable.getColumnModel().getColumn(0).setPreferredWidth(100);	//set Photo column preferred width
 			petTable.getColumnModel().getColumn(1).setPreferredWidth(80);	//set Pet name column preferred width
 
 			JScrollPane scrollPane = new JScrollPane();
 			scrollPane.setBounds(294, 10, 145, 170);
 			contentPane.add(scrollPane);
 			scrollPane.setViewportView(petTable);
 
 			JButton newPetButton = new JButton("New Pet");      //New Pet button (8elei auction listener)
 			newPetButton.setEnabled(false);
 			newPetButton.setBounds(322, 185, 89, 28);
 			contentPane.add(newPetButton);
 
 			JButton cancelButton = new JButton("Cancel");			//Back button (8elei auction listener)
 			cancelButton.addActionListener(this);
 			cancelButton.setBounds(314, 285, 105, 28);
 			contentPane.add(cancelButton);
 
 			//this.setResizable(false);
 			this.setVisible(true);
 			this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);		
 		}
 
 
 		//----------------------------- editCustomerGUI ACTION LISTENERS ------------------------------------------
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			if (e.getActionCommand().equals("Save Changes")) {
 				//TODO
 				//edited this
 				if  ((firstNameTxt.getText().trim()!=null)&&(!firstNameTxt.getText().trim().isEmpty())&&(lastNameTxt.getText().trim()!=null)&&(!lastNameTxt.getText().trim().isEmpty())) {
 					//elegxos ean to First Name h Last Name einai adeia h exoun mono kena
 					//h methodos trim() afairei leading kai trailing kena apo ta strings
 					JOptionPane information = new JOptionPane();
 					information.showMessageDialog(null,"Customer Edited!");   		// Emfanish mhnymatos epityxias
 					Customer newCustomer = new Customer( firstNameTxt.getText().trim(),lastNameTxt.getText().trim(),addressTxt.getText(),homePhoneTxt.getText(),mobilePhoneTxt.getText());  
 					//Dhmiourgia pelath
 					VetApp.db.DBUpdateCustomer(customer, newCustomer);			//Update tou pelath sth vasi
 					com.vetapp.customer.CustomersGUI.model.reloadJTable();		//Epanafortwsi tis listas pelatwn
 					dispose();															//kleisimo tou editCustomerGUI
 					new CustomerGUI(newCustomer);								//epanafortwsi CustomerGUI
 				}
 				else
 				{
 					JOptionPane.showMessageDialog(null,							// Emfanish mhnymatos sfalmatos
 							"First and Last name are required",
 							"Warning",
 							JOptionPane.WARNING_MESSAGE);
 				}
 				//end of edit
 			} else if (e.getActionCommand().equals("Cancel")) {
 				new CustomerGUI(customer);
 				this.dispose();
 			}
 		}
 	}
 
 	//============================================================================================
 	//--------------------------------- NextVisitGUI CLASS ---------------------------------------
 	//============================================================================================
 
 	public class NextVisitGUI extends JFrame implements ActionListener {
 
 		private JPanel contentPane;
 		private JTextField txtDd;  // Day textfield
 		private JTextField txtMm;   // Month Textfield
 		private JTextField txtYyyy; // Year Textfield
 		private JTextField txtHh;   // Hours Textfield
 		private JTextField txtMin; // Minutes Textfield
 		private  JButton btnSet = new JButton("Set");
 		private  JButton btnCancel = new JButton("Cancel");
 		Border loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
 
 		public NextVisitGUI() {
 			setResizable(false);
 			setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 			setBounds(100, 100, 404, 152);
 			contentPane = new JPanel();
 			contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 			setContentPane(contentPane);
 			setTitle(VetApp.MAIN_WINDOW_TITLE + " - " + this.getClass().getName());	//gets window title from constant in com.vetapp.main.VetApp
 			contentPane.setLayout(null);
 
 
 			JLabel lblCreatrNewAppointment = new JLabel("Create New Appointment");
 			lblCreatrNewAppointment.setHorizontalAlignment(SwingConstants.CENTER);
 			lblCreatrNewAppointment.setBounds(0, 0, 398, 28);
 			JPanel datePanel = new JPanel();
 			datePanel.setBounds(33, 37, 328, 42);
 			datePanel.setBorder(BorderFactory.createTitledBorder(loweredetched));
 			contentPane.add(datePanel);
 			datePanel.setLayout(null);
 
 			JLabel lblNextVisit = new JLabel("Next Visit:");
 			lblNextVisit.setBounds(10, 14, 58, 14);
 			datePanel.add(lblNextVisit);
 
 			txtDd = new JTextField();
 			txtDd.setBounds(78, 11, 27, 20);
 			datePanel.add(txtDd);
 			txtDd.setColumns(10);
 
 			txtMm = new JTextField();
 			txtMm.setBounds(115, 11, 27, 20);
 			datePanel.add(txtMm);
 			txtMm.setColumns(10);
 
 			txtYyyy = new JTextField();
 			txtYyyy.setBounds(152, 11, 32, 20);
 			datePanel.add(txtYyyy);
 			txtYyyy.setColumns(10);
 
 			JLabel lblTime = new JLabel("Time:");
 			lblTime.setBounds(213, 14, 37, 14);
 			datePanel.add(lblTime);
 
 			txtHh = new JTextField();
 			txtHh.setBounds(248, 11, 27, 20);
 			datePanel.add(txtHh);
 			txtHh.setColumns(10);
 
 			txtMin = new JTextField();
 			txtMin.setBounds(285, 11, 27, 20);
 			datePanel.add(txtMin);
 			txtMin.setColumns(10);
 			contentPane.add(lblCreatrNewAppointment);
 			//txtDd.setText("DD");
 			PromptSupport.setPrompt("dd", txtDd); 		//prompt text - using xswingx library
 			PromptSupport.setFocusBehavior(FocusBehavior.SHOW_PROMPT, txtDd);
 			//txtMm.setText("MM");
 			PromptSupport.setPrompt("mm", txtMm);
 			PromptSupport.setFocusBehavior(FocusBehavior.SHOW_PROMPT, txtMm);
 			//txtYyyy.setText("YYYY");
 			PromptSupport.setPrompt("yyyy", txtYyyy);
 			PromptSupport.setFocusBehavior(FocusBehavior.SHOW_PROMPT, txtYyyy);
 			//txtHh.setText("HH");
 			PromptSupport.setPrompt("hh", txtHh);
 			PromptSupport.setFocusBehavior(FocusBehavior.SHOW_PROMPT, txtMm);
 			//txtHh_1.setText("MM");
 			PromptSupport.setPrompt("mm", txtMin);
 			PromptSupport.setFocusBehavior(FocusBehavior.SHOW_PROMPT, txtMm);
 			btnSet.setBounds(98, 90, 89, 23);
 			contentPane.add(btnSet);
 			btnCancel.setBounds(208, 90, 89, 23);
 			contentPane.add(btnCancel);
 
 			setLocationRelativeTo(null);
 			this.setVisible(true);
 
 			btnSet.addActionListener(this);
 			btnCancel.addActionListener(this);
 		}
 
 		//----------------------------- NextVisitGUI ACTION LISTENERS ------------------------------------------
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			// TODO Auto-generated method stub
 			if (e.getActionCommand().equals("Set")) {
 				if(txtYyyy.getText().equals("")||txtMm.getText().equals("")||txtDd.getText().equals("")||txtHh.getText().equals("")||txtMin.getText().equals(""))	
 				{
 					JOptionPane error = new JOptionPane();
 					error.showMessageDialog(null, "Please fill the required fields", "Error", JOptionPane.ERROR_MESSAGE);
 				}
 				else {
 					Customer cus;
 					cus = customer;
 
 					int year = Integer.parseInt(txtYyyy.getText());
 					int month = Integer.parseInt(txtMm.getText());
 					int day = Integer.parseInt(txtDd.getText());
 					int hour = Integer.parseInt(txtHh.getText());
 					int minutes = Integer.parseInt(txtMin.getText());
 					// Yparxei ena bug pou emfanizei ton epomeno mhna apo auton pou kataxwrei o xrhsths
 
 					Calendar cl = Calendar.getInstance();
 
 					if(year<2013 || year>2015 ||  month>12 || month <0 || day <=0|| day >31 || hour < 0 ||hour >24 ||  minutes <0 || minutes >59) 
 					{
 						JOptionPane error = new JOptionPane();
 						error.showMessageDialog(null, "Conflicting types", "Error", JOptionPane.ERROR_MESSAGE);
 					}
 					else 
 					{
 						month = month -1 ;
 						cl.set(year,month,day,hour,minutes);
 						customer.setNextVisit(cl);
 						VetApp.db.DBUpdateCustomer(cus, customer);
 						nextVisit.setText(ft.format(customer.getNextVisit().getTime()));	//refreshes the value on CustomerGUI
 						CustomersGUI.model.reloadJTable();
 						JOptionPane information = new JOptionPane();
 						information.showMessageDialog(null,"Next Visit Set!");
 						dispose();
 					}
 				}
 			} else if (e.getActionCommand().equals("Cancel")) {
 				customer.setNextVisit(null);
 				VetApp.db.DBUpdateCustomer(customer, customer);
 				nextVisit.setText("_ _ /_ _ /_ _ _ _ ,  _ _ : _ _");
 				com.vetapp.customer.CustomersGUI.model.reloadJTable();		// Epanafortwsi tis listas pelatwn
 				JOptionPane information = new JOptionPane();
 				information.showMessageDialog(null,"Appointment has been canceled!");
 				dispose();
 			}
 
 
 		}
 	}
 
 }

 package views;
 
 import javax.swing.JFrame;
 
 import domain.Customer;
 
 import java.awt.Component;
 import java.awt.GridBagLayout;
 import javax.swing.JPanel;
 import java.awt.GridBagConstraints;
 import javax.swing.UIManager;
 import javax.swing.JLabel;
 import java.awt.Insets;
 import javax.swing.JButton;

 import components.MyJTextField;
 import java.awt.Dimension;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.util.Observable;
 import java.util.Observer;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.awt.Color;
 
 
 public class EditCustomer implements Observer{
 	
 
 	private static final Color myBlackColor = new Color(0, 0, 0);
 	private static final Color myBadColor = new Color(255, 99, 71);
 	protected JFrame frame;
 	protected JLabel lblCustomerWindow;
 	private MyJTextField txtFirstName;
 	private MyJTextField txtStreetName;
 	private MyJTextField txtCityName;
 	private MyJTextField txtPLZ;
 	private MyJTextField txtLastName;
 	protected Customer realCustomer;
 	private JButton btnSave;
 	private JButton btnReload;
 	
 	
 	public EditCustomer(Customer customer) {
 		setCustomer(customer);
 		customer.addObserver(this);
 		initialize();
 		lblCustomerWindow.setText("Edit Customer");
 		frame.getRootPane().setDefaultButton(btnSave);
 	}
 
 
 	public static void main(String[] args) {
 		Customer testCustomer = new Customer("Senbony", "Tony");
 		testCustomer.setAdress("Adamstreet", 8000, "Zrich");
//		EditCustomer editcustomer_window2 = new EditCustomer(testCustomer);
//		editcustomer_window2.setVisible();
 		EditCustomer editcustomer_window = new EditCustomer(testCustomer);
 		editcustomer_window.setVisible();
 		editcustomer_window.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 	}
 	public void setCustomer(Customer customer){
 		realCustomer = customer;
 	}
 
 
 	@Override
 	public void update(Observable arg0, Object arg1) {
 		btnSave.setEnabled(false);
 		btnReload.setEnabled(true);
 		btnReload.setForeground(myBadColor);
 	}
 	public void update(MyJTextField field, Object arg1){
 	}
 
 
 	public void setVisible(){
 		frame.setVisible(true);
 	}
 
 
 	private void updateFields() {
 		setTxtFirstName(realCustomer.getSurname());
 		setTxtLastName(realCustomer.getName());
 		setTxtStreetName(realCustomer.getStreet());
 		setTxtCityName(realCustomer.getCity());
 		setTxtPLZ(realCustomer.getZip()+"");
 		
 	}
 
 	private void updateCustomer() {
 		realCustomer.setName(getTxtLastName());
		txtLastName.setTextReload();
 		realCustomer.setSurname(getTxtFirstName());
		txtFirstName.setTextReload();
 		realCustomer.setAdress(getTxtStreetName(), Integer.parseInt(getTxtPLZ()), getTxtCityName());
		txtStreetName.setTextReload();
 	}
 
 	protected String getLblCustomerWindow() {
 		return lblCustomerWindow.getText();
 	}
 
 	protected void setLblCustomerWindow(String input) {
 		this.lblCustomerWindow.setText(input);
 	}
 
 	protected String getTxtFirstName() {
 		return txtFirstName.getText();
 	}
 
 	protected void setTxtFirstName(String input) {
 		this.txtFirstName.setTextReload(input);
 	}
 
 	protected String getTxtStreetName() {
 		return txtStreetName.getText();
 	}
 
 	protected void setTxtStreetName(String input) {
 		this.txtStreetName.setTextReload(input);
 	}
 
 	protected String getTxtCityName() {
 		return txtCityName.getText();
 	}
 
 	protected void setTxtCityName(String input) {
 		this.txtCityName.setTextReload(input);
 	}
 
 	protected String getTxtPLZ() {
 		return txtPLZ.getText();
 	}
 
 	protected void setTxtPLZ(String input) {;
 		this.txtPLZ.setTextReload(input);
 	}
 
 	protected String getTxtLastName() {
 		return txtLastName.getText();
 	}
 
 	protected void setTxtLastName(String input) {
 		txtLastName.setWrittingSettings();
 		this.txtLastName.setTextReload(input);
 	}
 
 	private void initialize() {
 		frame = new JFrame();
 		frame.setResizable(false);
 		frame.setBounds(100, 100, 416, 262);
 		GridBagLayout gridBagLayout = new GridBagLayout();
 		gridBagLayout.columnWidths = new int[]{0, 0};
 		gridBagLayout.rowHeights = new int[]{0, 0};
 		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
 		gridBagLayout.rowWeights = new double[]{1.0, Double.MIN_VALUE};
 		frame.getContentPane().setLayout(gridBagLayout);
 		
 		final JPanel panel = new JPanel();
 		panel.setMinimumSize(new Dimension(60, 30));
 		panel.setBorder(UIManager.getBorder("InsetBorder.aquaVariant"));
 		GridBagConstraints gbc_panel = new GridBagConstraints();
 		gbc_panel.fill = GridBagConstraints.BOTH;
 		gbc_panel.gridx = 0;
 		gbc_panel.gridy = 0;
 		frame.getContentPane().add(panel, gbc_panel);
 		GridBagLayout gbl_panel = new GridBagLayout();
 		gbl_panel.columnWidths = new int[]{0, 170, 0, 82, 0, 0};
 		gbl_panel.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
 		gbl_panel.columnWeights = new double[]{0.0, 1.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
 		gbl_panel.rowWeights = new double[]{0.0, 1.0, 0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
 		panel.setLayout(gbl_panel);
 		
 		lblCustomerWindow = new JLabel("New Customer");
 		GridBagConstraints gbc_lblNewCustomer = new GridBagConstraints();
 		gbc_lblNewCustomer.gridwidth = 5;
 		gbc_lblNewCustomer.insets = new Insets(0, 0, 5, 0);
 		gbc_lblNewCustomer.gridx = 0;
 		gbc_lblNewCustomer.gridy = 0;
 		panel.add(lblCustomerWindow, gbc_lblNewCustomer);
 		
 		JLabel lblName = new JLabel("Name");
 		GridBagConstraints gbc_lblName = new GridBagConstraints();
 		gbc_lblName.insets = new Insets(0, 0, 5, 5);
 		gbc_lblName.anchor = GridBagConstraints.EAST;
 		gbc_lblName.gridx = 0;
 		gbc_lblName.gridy = 2;
 		panel.add(lblName, gbc_lblName);
 		
 		txtFirstName = new MyJTextField("First",realCustomer.getSurname());
 		GridBagConstraints gbc_txtFirstName = new GridBagConstraints();
 		gbc_txtFirstName.insets = new Insets(0, 0, 5, 5);
 		gbc_txtFirstName.fill = GridBagConstraints.HORIZONTAL;
 		gbc_txtFirstName.gridx = 1;
 		gbc_txtFirstName.gridy = 2;
 		panel.add(txtFirstName, gbc_txtFirstName);
 		txtFirstName.setColumns(10);
 		
 		txtLastName = new MyJTextField("Last",realCustomer.getName());
 		GridBagConstraints gbc_txtLastName = new GridBagConstraints();
 		gbc_txtLastName.gridwidth = 2;
 		gbc_txtLastName.insets = new Insets(0, 0, 5, 5);
 		gbc_txtLastName.fill = GridBagConstraints.HORIZONTAL;
 		gbc_txtLastName.gridx = 2;
 		gbc_txtLastName.gridy = 2;
 		panel.add(txtLastName, gbc_txtLastName);
 		txtLastName.setColumns(10);
 		
 		JLabel lblStreet = new JLabel("Street");
 		GridBagConstraints gbc_lblStreet = new GridBagConstraints();
 		gbc_lblStreet.anchor = GridBagConstraints.EAST;
 		gbc_lblStreet.insets = new Insets(0, 0, 5, 5);
 		gbc_lblStreet.gridx = 0;
 		gbc_lblStreet.gridy = 3;
 		panel.add(lblStreet, gbc_lblStreet);
 		
 		txtStreetName = new MyJTextField("Street",realCustomer.getStreet());
 		GridBagConstraints gbc_txtStreetName = new GridBagConstraints();
 		gbc_txtStreetName.insets = new Insets(0, 0, 5, 5);
 		gbc_txtStreetName.fill = GridBagConstraints.HORIZONTAL;
 		gbc_txtStreetName.gridx = 1;
 		gbc_txtStreetName.gridy = 3;
 		panel.add(txtStreetName, gbc_txtStreetName);
 		txtStreetName.setColumns(10);
 		
 		JLabel lblCity = new JLabel("City");
 		GridBagConstraints gbc_lblCity = new GridBagConstraints();
 		gbc_lblCity.anchor = GridBagConstraints.WEST;
 		gbc_lblCity.insets = new Insets(0, 0, 5, 5);
 		gbc_lblCity.gridx = 0;
 		gbc_lblCity.gridy = 4;
 		panel.add(lblCity, gbc_lblCity);
 		
 		txtCityName = new MyJTextField("City",realCustomer.getCity());
 		GridBagConstraints gbc_txtCityName = new GridBagConstraints();
 		gbc_txtCityName.insets = new Insets(0, 0, 5, 5);
 		gbc_txtCityName.fill = GridBagConstraints.HORIZONTAL;
 		gbc_txtCityName.gridx = 1;
 		gbc_txtCityName.gridy = 4;
 		panel.add(txtCityName, gbc_txtCityName);
 		txtCityName.setColumns(10);
 		
 		txtPLZ = new MyJTextField("Plz",realCustomer.getZip()+"");
 		txtPLZ.setMaximumSize(new Dimension(60, 2147483647));
 		txtPLZ.setMinimumSize(new Dimension(60, 28));
 		txtPLZ.setPreferredSize(new Dimension(60, 28));
 		GridBagConstraints gbc_txtPLZ = new GridBagConstraints();
 		gbc_txtPLZ.anchor = GridBagConstraints.WEST;
 		gbc_txtPLZ.insets = new Insets(0, 0, 5, 5);
 		gbc_txtPLZ.gridx = 2;
 		gbc_txtPLZ.gridy = 4;
 		panel.add(txtPLZ, gbc_txtPLZ);
 		txtPLZ.setColumns(10);
 		
 		for(Component f : panel.getComponents()){
 			if(f instanceof MyJTextField){
 				final MyJTextField mytextfieldwithMemory = (MyJTextField) f;
 				mytextfieldwithMemory.addKeyListener(new KeyAdapter() {
 					@Override
 					public void keyReleased(KeyEvent e) {
 						super.keyReleased(e);
 						if(mytextfieldwithMemory.changed()){
 							btnSave.setEnabled(true);
 							btnReload.setEnabled(true);
 						} else {
 							btnSave.setEnabled(false);							
 						}
 					}
 					
 				});;
 			}
 		}
 		btnReload = new JButton("Reload");
 		btnReload.setEnabled(false);
 		btnReload.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				btnSave.setEnabled(false);
 				btnReload.setForeground(myBlackColor);
 				updateFields();		
 			}
 		});
 		GridBagConstraints gbc_btnRload = new GridBagConstraints();
 		gbc_btnRload.anchor = GridBagConstraints.EAST;
 		gbc_btnRload.insets = new Insets(0, 0, 0, 5);
 		gbc_btnRload.gridx = 1;
 		gbc_btnRload.gridy = 6;
 		panel.add(btnReload, gbc_btnRload);
 		
 		btnSave = new JButton("Save");
 		btnSave.setEnabled(false);
 		btnSave.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				updateCustomer();
 				btnSave.setEnabled(false);
 				btnReload.setEnabled(false);
 				btnReload.setForeground(myBlackColor);
 			}
 		});
 		GridBagConstraints gbc_btnSave = new GridBagConstraints();
 		gbc_btnSave.insets = new Insets(0, 0, 0, 5);
 		gbc_btnSave.gridwidth = 2;
 		gbc_btnSave.anchor = GridBagConstraints.WEST;
 		gbc_btnSave.gridx = 2;
 		gbc_btnSave.gridy = 6;
 		panel.add(btnSave, gbc_btnSave);
 	}
 
 }

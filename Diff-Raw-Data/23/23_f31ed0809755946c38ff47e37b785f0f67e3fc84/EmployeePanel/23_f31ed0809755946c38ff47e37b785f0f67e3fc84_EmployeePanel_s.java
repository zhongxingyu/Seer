 package ch.bli.mez.view.employee;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JTextField;
 import javax.swing.Timer;
 
 
 public class EmployeePanel extends JPanel {
 
 	private static final long serialVersionUID = 2105749706100780883L;
 
 	private JTextField lastnameTextField;
 	private JTextField firstnameTextField;
 	private JTextField streetTextField;
 	private JTextField cityTextField;
 	private JTextField plzTextField;
 	private JTextField homeNumberTextField;
 	private JTextField mobileNumberTextField;
 	private JTextField emailTextField;
 	
 	private JLabel messageLabel;
 	
 	private JButton saveButton;
 	private JButton statusButton;
 	
 	private JPanel contractContentPanel;
 	private JPanel holidayContentPanel;
 
 
 	public EmployeePanel() {
 		
 		setLayout(new BorderLayout());
 		
 		JPanel northPanel = new JPanel();
 		add(new JScrollPane(northPanel), BorderLayout.NORTH);
 		
 		JPanel topPanel = new JPanel(new BorderLayout());
 		northPanel.add(topPanel);
 		
 		topPanel.add(createEntryPanel(), BorderLayout.CENTER);
 		
 		JPanel messagePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
 		topPanel.add(messagePanel, BorderLayout.SOUTH);
 
 		messageLabel = new JLabel(" ");
 		messagePanel.add(messageLabel);
 		
 		JPanel centerPanel = new JPanel(new GridLayout(1, 0));
 		add(centerPanel, BorderLayout.CENTER);
 		
 		// HolidayPanel
 		JPanel centerLeftPanel = new JPanel(new BorderLayout());
 		centerPanel.add(centerLeftPanel);
 		
 		JPanel holidayTitlePanel = new JPanel();
 		centerLeftPanel.add(new JScrollPane(holidayTitlePanel), BorderLayout.NORTH);
 		
 		JLabel holidayTitleLabel = new JLabel("Ferien");
 		holidayTitlePanel.add(holidayTitleLabel);
 		
 		JPanel holidayHelpPanel = new JPanel();
 		centerLeftPanel.add(new JScrollPane(holidayHelpPanel), BorderLayout.CENTER);
 		
 		holidayContentPanel = new JPanel();
 		holidayContentPanel.setLayout(new BoxLayout(holidayContentPanel, BoxLayout.Y_AXIS));
 		holidayHelpPanel.add(holidayContentPanel);
 		
 		
 		// ContractPanel (center)
 		JPanel centerRightPanel = new JPanel(new BorderLayout());
 		centerPanel.add(centerRightPanel);
 		
 		JPanel contractTitlePanel = new JPanel();
 		centerRightPanel.add(new JScrollPane(contractTitlePanel), BorderLayout.NORTH);
 		
 		JLabel contractTitleLabel = new JLabel("Verträge");
 		contractTitlePanel.add(contractTitleLabel);
 		
 		JPanel contractHelpPanel = new JPanel();
 		centerRightPanel.add(new JScrollPane(contractHelpPanel), BorderLayout.CENTER);
 		
 		contractContentPanel = new JPanel();
 		contractHelpPanel.add(contractContentPanel);
 
 		
 		addGuiFeatureListener();
 	}
 	
 	/**
 	 * @param contractPanel contractPanel wird im Center des EmployeePanels eingefügt.
 	 *  Layout des ParentPanel ist der default JPanel Layout
 	 */
 	public void addContractPanel(ContractPanel contractPanel){
 		contractContentPanel.add(contractPanel);
 	}
 	
 	public void addEmployeeHolidayListEntry(EmployeeHolidayListEntry employeeHolidayListEntry){
 		holidayContentPanel.add(employeeHolidayListEntry);
 	}
 	
 
 	/**
 	 * @param name Vor- und Nachname des Mitarbeiters. Meldung wird generisch
 	 * 	erstellt: "'name' wurde gespeichert."
 	 */
 	public void showConfirmation(String name){
 		messageLabel.setForeground(new Color(0, 128, 0));
 		messageLabel.setText(name + " wurde gespeichert.");
 		hideMessage();
 	}
 	
 	/**
 	 * 
 	 * @param errorFieldName fehlerhaftes Feld. Fehlermeldung wird generisch
 	 * 	mit dem Feldname erstellt: "'errorFieldName' ist nicht gültig!"
 	 */
 	public void showError(String errorFieldName){
 		messageLabel.setForeground(new Color(255, 0, 0));
 		messageLabel.setText(errorFieldName + " ist nicht gültig!");
 		hideMessage();
 	}
 
 	private void hideMessage(){
 		Timer timer = new Timer(1800, new ActionListener() {
 			public void actionPerformed(ActionEvent evt) {
 				messageLabel.setText(" ");
 			}});
 		timer.setRepeats(false);
 		timer.start();
 	}
 	
 	public void cleanFields(){
 		setFirstname("");
 		setLastname("");
 		setStreet("");
 		setPlz("");
 		setCity("");
 		setMobileNumber("");
 		setHomeNumber("");
 		setEmail("");
 	}
 	
 	public void updateTabName(){
 	  JTabbedPane parent = (JTabbedPane)getParent();
 	  parent.setTitleAt(parent.indexOfComponent(this),
 	      getFirstname() + " " + getLastname());
 	}
 	
 	public boolean validateFields(){
	  // @Ste: to implement, Git an test dafür..
 	  // wänn öpis nöd guta isch direkt fähler azeiga.
	  return false;
 	}
 	
 	public void hideStatusButton(){
 		statusButton.setVisible(false);
 	}
 	
 	
 	// getter & setter
 	public void setStatusButtonName(String value){
 		statusButton.setName(value);
 	}
 	
 	public String getStatusButtonName(){
 		return statusButton.getName();
 	}
 	
 	public String getFirstname(){
 		return firstnameTextField.getText();
 	}
 	
 	public String getLastname(){
 		return lastnameTextField.getText();
 	}
 	
 	public String getStreet(){
 		return streetTextField.getText();
 	}
 	
 	public String getCity(){
 		return cityTextField.getText();
 	}
 	
 	public String getPlz(){
 		return plzTextField.getText();
 	}
 	
 	public String getMobileNumber(){
 		return mobileNumberTextField.getText();
 	}
 	
 	public String getHomeNumber(){
 		return homeNumberTextField.getText();
 	}
 	
 	public String getEmail(){
 		return emailTextField.getText();
 	}
 
 	public void setLastname(String value) {
 		this.lastnameTextField.setText(value);
 	}
 
 	public void setFirstname(String value) {
 		this.firstnameTextField.setText(value);
 	}
 
 	public void setStreet(String value) {
 		this.streetTextField.setText(value);
 	}
 
 	public void setCity(String value) {
 		this.cityTextField.setText(value);
 	}
 
 	public void setPlz(String value) {
 		this.plzTextField.setText(value);
 	}
 
 	public void setHomeNumber(String value) {
 		this.homeNumberTextField.setText(value);
 	}
 
 	public void setMobileNumber(String value) {
 		this.mobileNumberTextField.setText(value);
 	}
 
 	public void setEmail(String value) {
 		this.emailTextField.setText(value);
 	}
 
 	
 	// setListeners
 	public void setSaveEmployeeListener(ActionListener actionListener) {
 		saveButton.addActionListener(actionListener);
 	}
 	
 	public void setStatusButtonListener(ActionListener actionListener) {
 		statusButton.addActionListener(actionListener);
 	}
 	
 	
 	// internal methods
 	private JPanel createEntryPanel(){
 		JPanel entryPanel = new JPanel();
 		entryPanel.setLayout(new GridBagLayout());
 		entryPanel.setPreferredSize(new Dimension(600, 162));
 		
 		// helpPanel to create distance between first & second column
 		addPersonalComponent(entryPanel, new JPanel(), 2, 0, 1, 1.0);
 		
 		JLabel lbllastname = new JLabel("Name");
 		lbllastname.setFont(lbllastname.getFont().deriveFont(Font.BOLD));
 		addPersonalComponent(entryPanel, lbllastname, 0, 0, 1, 0);
 		
 		lastnameTextField = new JTextField();
 		addPersonalComponent(entryPanel, lastnameTextField, 1, 0, 1, 1.0);
 
 		JLabel lblfirstname = new JLabel("Vorname");
 		lblfirstname.setFont(lblfirstname.getFont().deriveFont(Font.BOLD));
 		addPersonalComponent(entryPanel, lblfirstname, 3, 0, 1, 0);
 		
 		firstnameTextField = new JTextField();
 		addPersonalComponent(entryPanel, firstnameTextField, 4, 0, 1, 1.0);
 		
 		JLabel lblstreet = new JLabel("Strasse");
 		addPersonalComponent(entryPanel, lblstreet, 0, 1, 1, 0);
 		
 		streetTextField = new JTextField();
 		addPersonalComponent(entryPanel, streetTextField, 1, 1, 1, 1.0);
 
 		JLabel lblplz = new JLabel("PLZ");
 		addPersonalComponent(entryPanel, lblplz, 0, 2, 1, 0);
 		
 		plzTextField = new JTextField();
 		plzTextField.setColumns(4);
 		addPersonalComponent(entryPanel, plzTextField, 1, 2, 1, 1.0);
 		
 		JLabel lblcity = new JLabel("Ort");
 		addPersonalComponent(entryPanel, lblcity, 3, 2, 1, 0);
 		
 		cityTextField = new JTextField();
 		cityTextField.setColumns(10);
 		addPersonalComponent(entryPanel, cityTextField, 4, 2, 2, 1.0);
 
 		JLabel lblhomeNumber = new JLabel("Festnetz");
 		addPersonalComponent(entryPanel, lblhomeNumber, 0, 3, 1, 0);
 		
 		homeNumberTextField = new JTextField();
 		homeNumberTextField.setColumns(10);
 		addPersonalComponent(entryPanel, homeNumberTextField, 1, 3, 1, 1.0);
 		
 		JLabel lblmobileNumber = new JLabel("Handy");
 		addPersonalComponent(entryPanel, lblmobileNumber, 3, 3, 1, 0);
 		
 		mobileNumberTextField = new JTextField();
 		mobileNumberTextField.setColumns(10);
 		addPersonalComponent(entryPanel, mobileNumberTextField, 4, 3, 1, 1.0);
 		
 		JLabel lblemail = new JLabel("E-Mail");
 		addPersonalComponent(entryPanel, lblemail, 0, 4, 1, 0);
 		
 		emailTextField = new JTextField();
 		emailTextField.setColumns(20);
 		addPersonalComponent(entryPanel, emailTextField, 1, 4, 4, 1.0);
 		
 		saveButton = new JButton("Speichern");
 		addPersonalComponent(entryPanel, saveButton, 4, 5, 1, 0);
 		
 		statusButton = new JButton("Deaktivieren");
 		addPersonalComponent(entryPanel, statusButton, 1, 5, 1, 0);
 		
 		return entryPanel;
 	}
 	
 	private void addPersonalComponent(JPanel panel, Component component, int x, int y, int width, double weightx) {
 		GridBagConstraints constraints = new GridBagConstraints();
 		constraints.fill = GridBagConstraints.HORIZONTAL;
 		constraints.gridx = x;
 		constraints.gridy = y;
 		constraints.gridwidth = width;
 		constraints.gridheight = 1;
 		constraints.weightx = weightx;
 		constraints.weighty = 0;
 		((GridBagLayout) panel.getLayout()).setConstraints(component, constraints);
 		panel.add(component);
 	}
 	
 	private void addGuiFeatureListener(){
 		KeyListener enterKeyListener = new KeyListener() {
 			public void keyTyped(KeyEvent arg0) {
 			}
 			public void keyReleased(KeyEvent arg0) {
 			}
 			public void keyPressed(KeyEvent arg0) {
 				if (arg0.getKeyCode() == KeyEvent.VK_ENTER) {
 					saveButton.doClick();
 				}
 			}
 		};
 		lastnameTextField.addKeyListener(enterKeyListener);
 		firstnameTextField.addKeyListener(enterKeyListener);
 		streetTextField.addKeyListener(enterKeyListener);
 		cityTextField.addKeyListener(enterKeyListener);
 		plzTextField.addKeyListener(enterKeyListener);
 		homeNumberTextField.addKeyListener(enterKeyListener);
 		mobileNumberTextField.addKeyListener(enterKeyListener);
 		emailTextField.addKeyListener(enterKeyListener);
 	}
 }

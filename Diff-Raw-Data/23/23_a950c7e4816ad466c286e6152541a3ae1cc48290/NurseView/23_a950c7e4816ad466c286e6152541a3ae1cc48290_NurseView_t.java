 package hms.views;
 
 import java.awt.EventQueue;
 
 import hms.models.Ward;
 
 import javax.swing.JFrame;
 import java.awt.CardLayout;
 import java.awt.Color;
 import java.awt.GridBagLayout;
 import javax.swing.BoxLayout;
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Date;
 
 import javax.swing.GroupLayout;
 import javax.swing.GroupLayout.Alignment;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPasswordField;
 import javax.swing.JTextField;
 import javax.swing.LayoutStyle.ComponentPlacement;
 import javax.swing.SpringLayout;
 import javax.swing.JRadioButton;
 import javax.swing.JComboBox;
 
 import hms.models.NurseTableModel;
 import hms.models.Nurse;
 import hms.util.*;
 
 public class NurseView {
 
 	public JFrame frame;
 	private JTextField txtName;
 	private JTextField txtSocialInsuranceNumber;
 	private JTextField txtPhoneNumber;
 	private JTextField txtPagerNumber;
 	private JTextField txtEmail;
 	private JTextField txtHomeAddress;
 	private JTextField textField;
 	private int idNumber;
 	private JRadioButton rdbtnMale;
 	private JRadioButton rdbtnFemale;
 	public static boolean isNew = false;
 	private NurseTableModel mainViewTableModel;
 	private JComboBox comboBox;
 	private JLabel lblName;
 	private JLabel lblSocialInsuranceNumber;
 	private JLabel lblPhoneNumber;
 	private JLabel lblSalary;
 	private JLabel lblInvalidInput;
 	private JLabel lblPassword = new JLabel("Password");
 	private JPasswordField passwordField = new JPasswordField("");
 
 	/**
 	 * Create the application.
 	 */
 	public NurseView(NurseTableModel mainViewTableModel) {
 		this.mainViewTableModel = mainViewTableModel;
 		initialize(true);
 	}
 
 	public NurseView(NurseTableModel mainViewTableModel, String[] row) {
 		this.mainViewTableModel = mainViewTableModel;
 		initialize(false);
 
 		if(row.length == 11) {
 			//Name
 			if(row[0] != null) {
 				txtName.setText(row[0]);
 			}
 			//phone number
 			if(row[1] != null) {
 				txtPhoneNumber.setText(row[1]);
 			}
 			//pager number
 			if(row[2] != null) {
 				txtPagerNumber.setText(row[2]);
 			}
 			//email address
 			if(row[3] != null) {
 				txtEmail.setText(row[3]);
 			}
 			//home address
 			if(row[4] != null) {
 				txtHomeAddress.setText(row[4]);
 			}
 			//social insurance number
 			if(row[5] != null) {
 				txtSocialInsuranceNumber.setText(row[5]);
 			}
 			if(row[6] != null) {
 				idNumber = Integer.parseInt(row[6]);
 			}
 			//gender
 			if(row[7] != null) {
 				if(row[7].equals("m") || row[7].equals("M")) {
 					rdbtnMale.setSelected(true);
 					rdbtnFemale.setSelected(false);
 				}
 				else if(row[7].equals("f") || row[7].equals("F")) {
 					rdbtnMale.setSelected(false);
 					rdbtnFemale.setSelected(true);
 				}
 			}
 			//salary
 			if(row[8] != null) {
 				textField.setText(row[8]);
 			}
 			//wards
 			if(row[9] != null){
 
 				if(row[9] != null){
 					String wardName = row[9];
 					int i = 0;
 					for(i = 0; i< comboBox.getItemCount(); i++){
 						try{
 							if(((Ward)comboBox.getItemAt(i)).getSingleWardName(i).equals(wardName)){
 								break;
 							}
 						}
 						catch(Throwable e){};
 					}
 					if( i < comboBox.getItemCount())
 						comboBox.setSelectedIndex(i);
 				}
 			}
 			//password
 			if(row[10] != null){
 				passwordField.setText(row[10]);
 			}
 		}
 	}
 
 	/**
 	 * Initialize the contents of the frame.
 	 */
 	private void initialize(boolean isNew) {
 
 		lblInvalidInput = new JLabel("Invalid input, please try again.");
 		lblInvalidInput.setVisible(false);
 		lblInvalidInput.setForeground(Color.red);
 
 		this.isNew = isNew;
 
 		frame = new JFrame();
		frame.setBounds(100, 100, 600, 400);
 		//frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 		JButton button = new JButton("Save");
 		button.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				if(validated()) {
 					if(NurseView.isNew)
 						createNurse();
 					else 
 						createNurse(idNumber);
 					mainViewTableModel.fireTableDataChanged();
 				}
 			}
 		});
 
 		JButton button_1 = new JButton("Save and Close");
 		button_1.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				if(validated()) {
 					if(NurseView.isNew)
 						createNurse();
 					else 
 						createNurse(idNumber);
 					mainViewTableModel.fireTableDataChanged();
 					frame.dispose();
 				}
 			}
 		});
 
 		JButton button_2 = new JButton("Close");
 		button_2.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				frame.dispose();
 			}
 		});
 
 		lblName = new JLabel("* Name");
 
 		txtName = new JTextField();
 		txtName.setColumns(10);
 
 		lblSocialInsuranceNumber = new JLabel("* Social Insurance Number");
 
 		txtSocialInsuranceNumber = new JTextField();
 		txtSocialInsuranceNumber.setColumns(10);
 		SpringLayout springLayout = new SpringLayout();
 		springLayout.putConstraint(SpringLayout.WEST, lblInvalidInput, 0, SpringLayout.WEST, frame.getContentPane());
 		springLayout.putConstraint(SpringLayout.SOUTH, lblInvalidInput, 0, SpringLayout.SOUTH, frame.getContentPane());
 		springLayout.putConstraint(SpringLayout.EAST, lblInvalidInput, 0, SpringLayout.EAST, frame.getContentPane());
 		springLayout.putConstraint(SpringLayout.WEST, button_2, 364, SpringLayout.WEST, frame.getContentPane());
 		springLayout.putConstraint(SpringLayout.SOUTH, button_2, -21, SpringLayout.SOUTH, frame.getContentPane());
		//springLayout.putConstraint(SpringLayout.EAST, button_2, -24, SpringLayout.EAST, frame.getContentPane());
 		springLayout.putConstraint(SpringLayout.SOUTH, button_1, -21, SpringLayout.SOUTH, frame.getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, button_1, 225, SpringLayout.WEST, frame.getContentPane());
 		springLayout.putConstraint(SpringLayout.WEST, button, 137, SpringLayout.WEST, frame.getContentPane());
 		springLayout.putConstraint(SpringLayout.SOUTH, button, -21, SpringLayout.SOUTH, frame.getContentPane());
		//springLayout.putConstraint(SpringLayout.EAST, button, -250, SpringLayout.EAST, frame.getContentPane());
 		springLayout.putConstraint(SpringLayout.NORTH, txtName, 11, SpringLayout.NORTH, frame.getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, txtName, 200, SpringLayout.WEST, frame.getContentPane());
 		springLayout.putConstraint(SpringLayout.EAST, txtName, 426, SpringLayout.WEST, frame.getContentPane());
 		springLayout.putConstraint(SpringLayout.NORTH, txtSocialInsuranceNumber, 37, SpringLayout.NORTH, frame.getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, txtSocialInsuranceNumber, 200, SpringLayout.WEST, frame.getContentPane());
 		springLayout.putConstraint(SpringLayout.EAST, txtSocialInsuranceNumber, 426, SpringLayout.WEST, frame.getContentPane());
 		springLayout.putConstraint(SpringLayout.NORTH, lblSocialInsuranceNumber, 40, SpringLayout.NORTH, frame.getContentPane());
 		springLayout.putConstraint(SpringLayout.WEST, lblSocialInsuranceNumber, 11, SpringLayout.WEST, frame.getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, lblSocialInsuranceNumber, 200, SpringLayout.WEST, frame.getContentPane());
 		springLayout.putConstraint(SpringLayout.NORTH, lblName, 11, SpringLayout.NORTH, frame.getContentPane());
 		springLayout.putConstraint(SpringLayout.WEST, lblName, 11, SpringLayout.WEST, frame.getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, lblName, 65, SpringLayout.WEST, frame.getContentPane());
 		frame.getContentPane().setLayout(springLayout);
 		frame.getContentPane().add(lblInvalidInput);
 		frame.getContentPane().add(button);
 		frame.getContentPane().add(button_1);
 		frame.getContentPane().add(button_2);
 		frame.getContentPane().add(lblName);
 		frame.getContentPane().add(lblSocialInsuranceNumber);
 		frame.getContentPane().add(txtSocialInsuranceNumber);
 		frame.getContentPane().add(txtName);
 
 		rdbtnMale = new JRadioButton("Male");
 		rdbtnMale.setSelected(true);
 		springLayout.putConstraint(SpringLayout.NORTH, rdbtnMale, 6, SpringLayout.SOUTH, txtSocialInsuranceNumber);
 		springLayout.putConstraint(SpringLayout.WEST, rdbtnMale, 0, SpringLayout.WEST, txtSocialInsuranceNumber);
 		frame.getContentPane().add(rdbtnMale);
 
 		rdbtnFemale = new JRadioButton("Female");
 		springLayout.putConstraint(SpringLayout.NORTH, rdbtnFemale, 6, SpringLayout.SOUTH, txtSocialInsuranceNumber);
 		springLayout.putConstraint(SpringLayout.WEST, rdbtnFemale, 25, SpringLayout.EAST, rdbtnMale);
 		springLayout.putConstraint(SpringLayout.EAST, rdbtnFemale, -182, SpringLayout.EAST, frame.getContentPane());
 		frame.getContentPane().add(rdbtnFemale);
 
 		rdbtnMale.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				rdbtnFemale.setSelected(!rdbtnMale.isSelected());
 			}
 		});
 		rdbtnFemale.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				rdbtnMale.setSelected(!rdbtnFemale.isSelected());
 			}
 		});
 
 		lblPhoneNumber = new JLabel("* Phone Number");
 		springLayout.putConstraint(SpringLayout.NORTH, lblPhoneNumber, 41, SpringLayout.SOUTH, lblSocialInsuranceNumber);
 		springLayout.putConstraint(SpringLayout.WEST, lblPhoneNumber, 0, SpringLayout.WEST, lblName);
 		frame.getContentPane().add(lblPhoneNumber);
 
 		txtPhoneNumber = new JTextField();
 		springLayout.putConstraint(SpringLayout.NORTH, txtPhoneNumber, 6, SpringLayout.SOUTH, rdbtnMale);
 		springLayout.putConstraint(SpringLayout.WEST, txtPhoneNumber, 0, SpringLayout.WEST, txtSocialInsuranceNumber);
 		springLayout.putConstraint(SpringLayout.EAST, txtPhoneNumber, -8, SpringLayout.EAST, frame.getContentPane());
 		txtPhoneNumber.setColumns(10);
 		frame.getContentPane().add(txtPhoneNumber);
 
 		JLabel lblPagerNumber = new JLabel("Pager Number");
 		springLayout.putConstraint(SpringLayout.EAST, lblPhoneNumber, 22, SpringLayout.EAST, lblPagerNumber);
 		springLayout.putConstraint(SpringLayout.WEST, lblPagerNumber, 11, SpringLayout.WEST, frame.getContentPane());
 		springLayout.putConstraint(SpringLayout.EAST, lblPagerNumber, 102, SpringLayout.WEST, frame.getContentPane());
 		frame.getContentPane().add(lblPagerNumber);
 
 		txtPagerNumber = new JTextField();
 		springLayout.putConstraint(SpringLayout.NORTH, txtPagerNumber, 8, SpringLayout.SOUTH, txtPhoneNumber);
 		springLayout.putConstraint(SpringLayout.WEST, txtPagerNumber, 0, SpringLayout.WEST, txtSocialInsuranceNumber);
 		springLayout.putConstraint(SpringLayout.EAST, txtPagerNumber, -9, SpringLayout.EAST, frame.getContentPane());
 		txtPagerNumber.setColumns(10);
 		frame.getContentPane().add(txtPagerNumber);
 
 		JLabel lblEmail = new JLabel("Email");
 		springLayout.putConstraint(SpringLayout.SOUTH, lblPagerNumber, -12, SpringLayout.NORTH, lblEmail);
 		springLayout.putConstraint(SpringLayout.NORTH, lblEmail, 149, SpringLayout.NORTH, frame.getContentPane());
 		springLayout.putConstraint(SpringLayout.WEST, lblEmail, 0, SpringLayout.WEST, lblName);
 		springLayout.putConstraint(SpringLayout.EAST, lblEmail, 0, SpringLayout.EAST, lblName);
 		frame.getContentPane().add(lblEmail);
 
 		txtEmail = new JTextField();
 		springLayout.putConstraint(SpringLayout.NORTH, txtEmail, 6, SpringLayout.SOUTH, txtPagerNumber);
 		springLayout.putConstraint(SpringLayout.WEST, txtEmail, 0, SpringLayout.WEST, txtSocialInsuranceNumber);
 		springLayout.putConstraint(SpringLayout.EAST, txtEmail, -10, SpringLayout.EAST, frame.getContentPane());
 		txtEmail.setColumns(10);
 		frame.getContentPane().add(txtEmail);
 
 		txtHomeAddress = new JTextField();
 		springLayout.putConstraint(SpringLayout.NORTH, txtHomeAddress, 6, SpringLayout.SOUTH, txtEmail);
 		springLayout.putConstraint(SpringLayout.WEST, txtHomeAddress, 0, SpringLayout.WEST, txtSocialInsuranceNumber);
 		springLayout.putConstraint(SpringLayout.EAST, txtHomeAddress, 0, SpringLayout.EAST, txtPagerNumber);
 		txtHomeAddress.setColumns(10);
 		frame.getContentPane().add(txtHomeAddress);
 
 		JLabel lblHomeAdress = new JLabel("Home Address");
 		springLayout.putConstraint(SpringLayout.NORTH, lblHomeAdress, 3, SpringLayout.NORTH, txtHomeAddress);
 		springLayout.putConstraint(SpringLayout.WEST, lblHomeAdress, 11, SpringLayout.WEST, frame.getContentPane());
 		springLayout.putConstraint(SpringLayout.EAST, lblHomeAdress, -63, SpringLayout.WEST, txtHomeAddress);
 		frame.getContentPane().add(lblHomeAdress);
 
 		lblSalary = new JLabel("* Salary");
 		springLayout.putConstraint(SpringLayout.WEST, lblSalary, 10, SpringLayout.WEST, frame.getContentPane());
 		springLayout.putConstraint(SpringLayout.EAST, lblSalary, 75, SpringLayout.WEST, lblName);
 		frame.getContentPane().add(lblSalary);
 
 		textField = new JTextField();
 		springLayout.putConstraint(SpringLayout.NORTH, lblSalary, 3, SpringLayout.NORTH, textField);
 		springLayout.putConstraint(SpringLayout.NORTH, textField, 6, SpringLayout.SOUTH, txtHomeAddress);
 		springLayout.putConstraint(SpringLayout.WEST, textField, 22, SpringLayout.WEST, txtSocialInsuranceNumber);
 		springLayout.putConstraint(SpringLayout.EAST, textField, 0, SpringLayout.EAST, txtPagerNumber);
 		textField.setColumns(10);
 		frame.getContentPane().add(textField);
 
 		JLabel label = new JLabel("$");
 		springLayout.putConstraint(SpringLayout.WEST, label, 0, SpringLayout.WEST, txtSocialInsuranceNumber);
 		springLayout.putConstraint(SpringLayout.SOUTH, label, 0, SpringLayout.SOUTH, lblSalary);
 		springLayout.putConstraint(SpringLayout.EAST, label, -6, SpringLayout.WEST, textField);
 		frame.getContentPane().add(label);
 
 		JLabel label_1 = new JLabel("Ward");
 		springLayout.putConstraint(SpringLayout.WEST, label_1, 0, SpringLayout.WEST, lblName);
 		springLayout.putConstraint(SpringLayout.EAST, label_1, 0, SpringLayout.EAST, lblSalary);
 		
 		
 		frame.getContentPane().add(label_1);
 
 		comboBox = new JComboBox();
 		Ward[] wards = Ward.getWards();
 		if(wards != null){
 			for(int i = 0; i<wards.length; i++){
 				comboBox.addItem(wards[i]);
 			}
 		}
 		springLayout.putConstraint(SpringLayout.NORTH, passwordField, 0, SpringLayout.SOUTH,comboBox);
 		springLayout.putConstraint(SpringLayout.WEST, passwordField, 0, SpringLayout.WEST,comboBox);
 		springLayout.putConstraint(SpringLayout.EAST, passwordField, 0, SpringLayout.EAST,comboBox);
 		
 		springLayout.putConstraint(SpringLayout.NORTH, lblPassword, 5, SpringLayout.SOUTH,label_1);
 		springLayout.putConstraint(SpringLayout.WEST, lblPassword, 0, SpringLayout.WEST,label_1);
 		
 		springLayout.putConstraint(SpringLayout.NORTH, label_1, 3, SpringLayout.NORTH, comboBox);
 		springLayout.putConstraint(SpringLayout.NORTH, comboBox, 6, SpringLayout.SOUTH, textField);
 		springLayout.putConstraint(SpringLayout.WEST, comboBox, 0, SpringLayout.WEST, txtSocialInsuranceNumber);
 		springLayout.putConstraint(SpringLayout.EAST, comboBox, 0, SpringLayout.EAST, button_1);
 		frame.getContentPane().add(comboBox);
 		frame.getContentPane().add(passwordField);
 		frame.getContentPane().add(lblPassword);
 	}
 
 	private void createNurse() {
 		//TODO test to see if nurse is in database
 		Nurse temp = new Nurse(
 				txtName.getText(),
 				txtPhoneNumber.getText(),
 				txtPagerNumber.getText(), 
 				txtEmail.getText(),
 				txtHomeAddress.getText(),
 				txtSocialInsuranceNumber.getText(),
 				Nurse.generateIDNumber(),
 				rdbtnMale.isSelected()? "M":"F",
 				Integer.parseInt(textField.getText()),
 				((Ward)comboBox.getSelectedItem()).getWardNumber(),
 				passwordField.getText());
 		try{
 			temp.create();
 		}catch(Exception e1){
 			return;
 		}
 	}
 
 	private void createNurse(int id_number) {
 		Nurse temp = new Nurse(
 				txtName.getText(),
 				txtPhoneNumber.getText(),
 				txtPagerNumber.getText(), 
 				txtEmail.getText(),
 				txtHomeAddress.getText(),
 				txtSocialInsuranceNumber.getText(),
 				id_number,
 				rdbtnMale.isSelected()? "M":"F",
 						Integer.parseInt(textField.getText()),
 						((Ward)comboBox.getSelectedItem()).getWardNumber(),
 						passwordField.getText());
 		try{
 			temp.delete();
 			temp.create();
 		}catch(Exception e1){
 			return;
 		}
 	}
 
 	private boolean validated() {
 		boolean validated = true;
 
 		lblInvalidInput.setVisible(false);
 
 		lblName.setForeground(Color.black);
 		lblSocialInsuranceNumber.setForeground(Color.black);
 		lblPhoneNumber.setForeground(Color.black);
 
 		//Check Name
 		if(txtName.getText().equals("")){
 			lblName.setForeground(Color.red);
 			validated = false;
 			lblInvalidInput.setVisible(true);
 		}
 
 		//Check SocialInsuranceNumber
 		try {
 			Integer.parseInt(txtSocialInsuranceNumber.getText());
 		} catch(NumberFormatException e) {
 			lblSocialInsuranceNumber.setForeground(Color.red);
 			validated = false;
 			lblInvalidInput.setVisible(true);
 		}
 
 		//Check phone number
 		if(txtPhoneNumber.getText().equals("")) {
 			lblPhoneNumber.setForeground(Color.red);
 			validated = false;
 			lblInvalidInput.setVisible(true);
 		}
 
 		//Check Salary
 		try {
 			Integer.parseInt(textField.getText());
 		} catch(NumberFormatException e) {
 			lblSalary.setForeground(Color.red);
 			validated = false;
 			lblInvalidInput.setVisible(true);
 		}
 
 		//		private JLabel lblSocialInsuranceNumber;
 		//		private JLabel lblPhoneNumber;
 		//		private JLabel lblSalary;
 		//		private JLabel lblInvalidInput;
 
 		return validated;
 	}
 }

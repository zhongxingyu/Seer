 package final_project.view;
 
 import javax.swing.*;
 import java.awt.*;
 import javax.swing.text.*;
 
 public class AddNewSubscriberPanel extends JPanel {
 	private JTextField nameTextField;
 	private JButton btnCancel;
 	private JFormattedTextField phoneNumberTextField;
 	JLabel lblNoResultsFound;
 	private JTextField textField;
 	private JButton doneButton;
 
 	/**
 	 * Create the panel.
 	 */
 	public AddNewSubscriberPanel() {
 		setPreferredSize(new Dimension(301, 180));
 		setSize(new Dimension(270, 200));
 		setOpaque(false);
 		GridBagLayout gridBagLayout = new GridBagLayout();
 		gridBagLayout.columnWidths = new int[]{87, 55, 143, 0, 0};
 		gridBagLayout.rowHeights = new int[]{26, 0, 32, 0, 0, 0, 0};
 		gridBagLayout.columnWeights = new double[]{0.0, 1.0, 1.0, 0.0, Double.MIN_VALUE};
 		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
 		setLayout(gridBagLayout);
 		
 		lblNoResultsFound = new JLabel("<html><i>No Results Found</i></html>");
 		GridBagConstraints gbc_lblNoResultsFound = new GridBagConstraints();
 		gbc_lblNoResultsFound.fill = GridBagConstraints.HORIZONTAL;
 		gbc_lblNoResultsFound.gridwidth = 4;
 		gbc_lblNoResultsFound.insets = new Insets(0, 0, 5, 0);
 		gbc_lblNoResultsFound.gridx = 0;
 		gbc_lblNoResultsFound.gridy = 0;
 		add(lblNoResultsFound, gbc_lblNoResultsFound);
 		
 		JLabel lblAddNewPerson = new JLabel("<html><b>Add new person</b></html>");
 		GridBagConstraints gbc_lblAddNewPerson = new GridBagConstraints();
 		gbc_lblAddNewPerson.fill = GridBagConstraints.HORIZONTAL;
 		gbc_lblAddNewPerson.gridwidth = 4;
 		gbc_lblAddNewPerson.insets = new Insets(0, 0, 5, 0);
 		gbc_lblAddNewPerson.gridx = 0;
 		gbc_lblAddNewPerson.gridy = 1;
 		add(lblAddNewPerson, gbc_lblAddNewPerson);
 		
 		JLabel lblName = new JLabel("Name:");
 		GridBagConstraints gbc_lblName = new GridBagConstraints();
 		gbc_lblName.anchor = GridBagConstraints.EAST;
 		gbc_lblName.insets = new Insets(0, 0, 5, 5);
 		gbc_lblName.gridx = 0;
 		gbc_lblName.gridy = 2;
 		add(lblName, gbc_lblName);
 		
 		nameTextField = new JTextField();
 		GridBagConstraints gbc_nameTextField = new GridBagConstraints();
 		gbc_nameTextField.gridwidth = 3;
 		gbc_nameTextField.fill = GridBagConstraints.HORIZONTAL;
 		gbc_nameTextField.insets = new Insets(0, 0, 5, 0);
 		gbc_nameTextField.gridx = 1;
 		gbc_nameTextField.gridy = 2;
 		add(nameTextField, gbc_nameTextField);
 		
 		JLabel lblPhoneNumber = new JLabel("Phone #:");
 		GridBagConstraints gbc_lblPhoneNumber = new GridBagConstraints();
 		gbc_lblPhoneNumber.anchor = GridBagConstraints.EAST;
 		gbc_lblPhoneNumber.insets = new Insets(0, 0, 5, 5);
 		gbc_lblPhoneNumber.gridx = 0;
 		gbc_lblPhoneNumber.gridy = 3;
 		add(lblPhoneNumber, gbc_lblPhoneNumber);
 		
 		MaskFormatter formatter = null;
 		try {
 		    formatter = new MaskFormatter("(###)-###-####");
 		    formatter.setPlaceholderCharacter('*');
 		} catch (java.text.ParseException e) {
 		}
 		phoneNumberTextField = new JFormattedTextField(formatter);
 		GridBagConstraints gbc_phoneNumberTextField = new GridBagConstraints();
 		gbc_phoneNumberTextField.gridwidth = 3;
 		gbc_phoneNumberTextField.insets = new Insets(0, 0, 5, 0);
 		gbc_phoneNumberTextField.fill = GridBagConstraints.HORIZONTAL;
 		gbc_phoneNumberTextField.gridx = 1;
 		gbc_phoneNumberTextField.gridy = 3;
 		add(phoneNumberTextField, gbc_phoneNumberTextField);
 		
 		JLabel lblRegistration = new JLabel("Registration #:");
 		GridBagConstraints gbc_lblRegistration = new GridBagConstraints();
 		gbc_lblRegistration.anchor = GridBagConstraints.EAST;
 		gbc_lblRegistration.insets = new Insets(0, 0, 5, 5);
 		gbc_lblRegistration.gridx = 0;
 		gbc_lblRegistration.gridy = 4;
 		add(lblRegistration, gbc_lblRegistration);
 		
 		textField = new JTextField();
 		GridBagConstraints gbc_textField = new GridBagConstraints();
 		gbc_textField.gridwidth = 3;
 		gbc_textField.insets = new Insets(0, 0, 5, 0);
 		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
 		gbc_textField.gridx = 1;
 		gbc_textField.gridy = 4;
 		add(textField, gbc_textField);
 		textField.setColumns(10);
 		
 		doneButton = new JButton("Done");
 		GridBagConstraints gbc_doneButton = new GridBagConstraints();
 		gbc_doneButton.insets = new Insets(0, 0, 0, 5);
 		gbc_doneButton.anchor = GridBagConstraints.EAST;
 		gbc_doneButton.gridx = 2;
 		gbc_doneButton.gridy = 5;
 		add(doneButton, gbc_doneButton);
 		
 		btnCancel = new JButton("Cancel");
 		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
 		gbc_btnCancel.anchor = GridBagConstraints.EAST;
 		gbc_btnCancel.gridx = 3;
 		gbc_btnCancel.gridy = 5;
 		add(btnCancel, gbc_btnCancel);
 
 	}
 	
 	public void setNoResults(boolean noResults) {
 		if (noResults) {
 			lblNoResultsFound.setVisible(true);
 		}
 		else 
 			lblNoResultsFound.setVisible(false);
 	}
 
 	public JTextField getNameTextField() {
 		return nameTextField;
 	}
 	public JButton getCancelButton() {
 		return btnCancel;
 	}
 	public JTextField getPhoneNumberTextField() {
 		return phoneNumberTextField;
 	}
 	public JButton getDoneButton() {
 		return doneButton;
 	}
 }

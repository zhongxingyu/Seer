 package oving2;
 
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JSlider;
 import javax.swing.JTextField;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 public class PersonPanel extends JPanel implements ChangeListener, KeyListener, ItemListener {
 	GridBagConstraints gbc;
 	JLabel nameLabel, emailLabel, birthdayLabel, genderLabel, heightLabel;
 	JTextField nameField, emailField, birthdayField;
 	JComboBox genderComboBox;
 	JSlider heightSlider;
 	private Person model = null;
 
 	public PersonPanel() {
 		setLayout(new GridBagLayout());
 		gbc = new GridBagConstraints();
 		gbc.ipadx = 2;
 		gbc.ipady = 4;
 		createLabels();
 		createFields();
 		setNames();
 	}
 	
 	public void setModel(Person newPerson){
 		this.model = newPerson;
 		updateFields();
 	}
 	public Person getModel(){
 		return this.model;
 	}
 	private void updateModel(){
 		this.model.setName(nameField.getText());
 		this.model.setEmail(emailField.getText());
 		this.model.setDateOfBirth(birthdayField.getText());
 		this.model.setGender((Gender) genderComboBox.getSelectedItem());
 		this.model.setHeight(heightSlider.getValue());
 	}
 	private void updateFields(){
 		nameField.setText(this.model.getName());
 		emailField.setText(this.model.getName());
 		birthdayField.setText(this.model.getName());
 		genderComboBox.setSelectedItem(this.model.getGender());
 		heightSlider.setValue(this.model.getHeight());
 	}
 	private void createLabels() {
 		gbc.gridx = 0;gbc.gridy = 0;
		nameLabel = new JLabel("Name:");
 		this.add(nameLabel, gbc);
 		
 		gbc.gridx = 0;gbc.gridy = 1;
		emailLabel = new JLabel("Email:");
 		this.add(emailLabel, gbc);
 
 		gbc.gridx = 0;gbc.gridy = 2;
 		birthdayLabel = new JLabel("Birthday:");
 		this.add(birthdayLabel, gbc);
 
 		gbc.gridx = 0;gbc.gridy = 3;
		genderLabel = new JLabel("Gender:");
 		this.add(genderLabel, gbc);
 
 		gbc.gridx = 0;gbc.gridy = 4;
		heightLabel = new JLabel("Height:");
 		this.add(heightLabel, gbc);
 	}
 	
 	private void createFields() {
 		gbc.gridx = 1;gbc.gridy = 0;
 		nameField = new JTextField(20);
 		nameField.addKeyListener(this);
 		this.add(nameField, gbc);
 		
 		gbc.gridx = 1;gbc.gridy = 1;
 		emailField = new JTextField(20);
 		emailField.addKeyListener(this);
 		this.add(emailField, gbc);
 		
 		gbc.gridx = 1;gbc.gridy = 2;
 		birthdayField = new JTextField(20);
 		birthdayField.addKeyListener(this);
 		this.add(birthdayField, gbc);
 		
 		gbc.gridx = 1;gbc.gridy = 3;
 		genderComboBox = new JComboBox(Gender.values());
 		genderComboBox.addItemListener(this);
 		this.add(genderComboBox, gbc);
 		
 		gbc.gridx = 1;gbc.gridy = 4;
 		heightSlider = new JSlider(120, 220);
 		heightSlider.addChangeListener(this);
 		this.add(heightSlider, gbc);
 	}
 
 	private void setNames() {
 		nameField.setName("NamePropertyComponent");
 		emailField.setName("EmailPropertyComponent");
 		birthdayField.setName("DateOfBirthPropertyComponent");
 		genderComboBox.setName("GenderPropertyComponent");
 		heightSlider.setName("HeightPropertyComponent");
 	}
 
 	@Override
 	public void stateChanged(ChangeEvent e) {
 		updateModel();
 	}
 
 	@Override
 	public void itemStateChanged(ItemEvent e) {
 		updateModel();
 	}
 
 	@Override
 	public void keyPressed(KeyEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void keyReleased(KeyEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void keyTyped(KeyEvent e) {
 		updateModel();
 		
 	}
 
 
 }
 

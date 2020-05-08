 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.util.regex.*;
 
 public class EditStudentScreen extends Screen implements ActionListener
 {
 	public final static long serialVersionUID = 1L;
 
 	Student student;
 	Screen parentScreen;
 	TFSFrame mainFrame;
 
 	JTextField lastNameField = new JTextField();
 	JTextField firstNameField = new JTextField();
 	JTextField emailField = new JTextField();
 	JTextField phoneField = new JTextField();
 	JTextField GPAField = new JTextField();
 
 	boolean isNewStudent = false;
 
 	JButton updateButton;
 	JButton cancelButton = new JButton("Cancel");
 
 	public EditStudentScreen(Screen parent, Student st, boolean isNewStudent)
 	{
 		mainFrame = TFSFrame.getInstance();
 		parentScreen = parent;
 		student = st;
 		this.isNewStudent = isNewStudent;
 
 		initComponents();
 		buildPanel();
 
 		if (!isNewStudent) {
 			prePopulateFields();
 		}
 	}
 
 	public String getScreenTitle()
 	{
 		return "Edit Student";
 	}
 
 	public void actionPerformed(ActionEvent e)
 	{
 		Object source = e.getSource();
 
 		if (source == updateButton && saveStudentData()) {
 			Project proj = mainFrame.getCurrentProject();
 
 			if (isNewStudent) {
 				proj.addStudent(student);
 			}
 
 			parentScreen.reloadData(proj);
 			mainFrame.setScreen(parentScreen);
 
 		} else if (source == cancelButton) {
 			mainFrame.setScreen(parentScreen);
 		}
 	}
 
 	private boolean saveStudentData()
 	{
 		String errorMessages = "";
 		boolean isValid = true;
 
 		String firstName = firstNameField.getText().trim();
 		if (firstName.isEmpty()) {
 			errorMessages += "Enter a first name.\n";
 			isValid = false;
 		}
 
 		String lastName = lastNameField.getText().trim();
 		if (lastName.isEmpty()) {
 			errorMessages += "Enter a last name.\n";
 			isValid = false;
 		}
 
 		String email = emailField.getText().trim().toLowerCase();
 		if (!Pattern.matches("^[a-z]{3}\\d{6}@utdallas\\.edu", email)) {
 			errorMessages += "Use a valid UTD email address (e.g. abc123000@utdallas.edu)\n";
 			isValid = false;
 		}
 
 		// optional
 		String phone = phoneField.getText().trim();
 		if (!phone.isEmpty() && !Pattern.matches("^\\d{10,}$", phone)) {
 			errorMessages += "Phone numbers may only contain numbers and must be at least 10 digits long.\n";
 			isValid = false;
 		}
 
 		String txt = GPAField.getText().trim();
 		boolean isGpaValid = true;
 		double gpa = 0.0;
 		try {
 			gpa = Double.parseDouble(txt);
 			if (gpa < 0.0 || gpa > 4.0) {
 				isGpaValid = false;
 			}
 		} catch (NumberFormatException ex) {
 			isGpaValid = false;
 		} finally {
 			if (!isGpaValid) {
 				errorMessages += "A GPA must be between 0.0 and 4.0.\n";
 				isValid = false;
 			}
 		}
 
 		// TODO: get skillset
 
 		if (!isValid) {
 			mainFrame.showError(errorMessages);
 		} else {
 			student.setFirstName(firstName);
 			student.setLastName(lastName);
 			student.setUtdEmail(email);
 			student.setPhoneNumber(phone);
 			student.setGPA(gpa);
 		}
 
 		return isValid;
 	}
 
 	private void initComponents()
 	{
 		if (isNewStudent) {
 			updateButton = new JButton("Add");
 		} else {
 			updateButton = new JButton("Update");
 		}
 
 		updateButton.addActionListener(this);
 		cancelButton.addActionListener(this);
 	}
 
 	private void buildPanel()
 	{
 		JPanel row;
 		JPanel alignColumn = GuiHelpers.column();
 		JPanel layoutColumn = GuiHelpers.column();
 		
 		row = GuiHelpers.row();
 		row.add(new JLabel("First name: "));
 		row.add(firstNameField);
 		row.add(Box.createHorizontalGlue());
 		alignColumn.add(row);
 
 		row = GuiHelpers.row();
 		row.add(new JLabel("Last name: "));
 		row.add(lastNameField);
 		row.add(Box.createHorizontalGlue());
 		alignColumn.add(row);
 
 		row = GuiHelpers.row();
 		row.add(new JLabel("UTD email: "));
 		row.add(emailField);
 		row.add(Box.createHorizontalGlue());
 		alignColumn.add(row);
 
 		row = GuiHelpers.row();
 		row.add(new JLabel("Phone: "));
 		row.add(phoneField);
 		row.add(Box.createHorizontalGlue());
 		alignColumn.add(row);
 
 		row = GuiHelpers.row();
 		row.add(new JLabel("GPA: "));
 		row.add(GPAField);
 		row.add(Box.createHorizontalGlue());
 		alignColumn.add(row);
 
 		layoutColumn.add(alignColumn);
 		
 		Skill[] skills = student.getSkills();
 		for (int i = 0; i < skills.length; i++) {
 			layoutColumn.add(new SkillPanel(skills[i]));
 		}
 
 		row = GuiHelpers.row();
 		row.add(updateButton);
 		row.add(cancelButton);
 		layoutColumn.add(row);
 
 		add(layoutColumn);
 	}
 
 	private void prePopulateFields()
 	{
 		lastNameField.setText(student.getLastName());
 		firstNameField.setText(student.getFirstName());
 		emailField.setText(student.getUtdEmail());
 		phoneField.setText(student.getPhoneNumber());
 		GPAField.setText(String.valueOf(student.getGPA()));
 	}
 }
 
 class SkillPanel extends JPanel implements ActionListener
 {
 	public final static long serialVersionUID = 1L;
 
 	Skill skill;
 	ButtonGroup buttons;
 	JRadioButton[] radios = new JRadioButton[5];
 
 	SkillPanel(Skill skill)
 	{
 		this.skill = skill;
 		initComponents();
 		buildPanel();
 	}
 
 	public void actionPerformed(ActionEvent e)
 	{
 		int rating = Integer.parseInt(e.getActionCommand());
 		skill.setRating(rating);
 	}
 
 	private void initComponents()
 	{
 		buttons = new ButtonGroup();
 		for (int i = 0; i < radios.length; i++) {
 			String label = String.valueOf(i+1);
 			radios[i] = new JRadioButton(label);
 			radios[i].setActionCommand(label);
 			radios[i].addActionListener(this);
 			buttons.add(radios[i]);
 		}
 
 		int rating = skill.getRating();
		if (rating > 0) {
 			buttons.setSelected(radios[rating-1].getModel(), true);
 		}
 	}
 
 	private void buildPanel()
 	{
 		JPanel row = GuiHelpers.row();
 		row.add(new JLabel(skill.getSkillName() + ": "));
 		row.add(Box.createHorizontalGlue());
 
 		for (int i = 0; i < radios.length; i++) {
 			row.add(radios[i]);
 		}
 
 		add(row);
 	}
 }
 

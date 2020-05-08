 package gui;
 
 import java.awt.*;
 
 import javax.swing.*;
 
 import gui.custom.RoundPanel;
 
 /************************************************************************************
  * Name: StudentInfo.java
  * 
  * Description: This panel is responsible for displaying questions relating to
  * the personal information of the student.
  * 
  ************************************************************************************/
 
 public class StudentInfo extends RoundPanel implements GUI {
 
 	public StudentInfo() {
 
 		/*********************************
 		 * Declare / Instantiate Variables
 		 *********************************/
 		/** Containers **/
 		Container container = new Container();
 		Container genderContainer = new Container();
 		
 		/** Layouts **/
		BoxLayout lytContainer = new BoxLayout(container, 1);
 		FlowLayout lytGenderContainer = new FlowLayout();
 
 		/** Labels **/
 		JLabel lblGroup = new JLabel("Reality-U Group: ");
 		JLabel lblPeriod = new JLabel("Class Period: ");
 		JLabel lblTeacher = new JLabel("Teacher: ");
 		JLabel lblFName = new JLabel("First Name: ");
 		JLabel lblLName = new JLabel("Last Name: ");
 		JLabel lblGender = new JLabel("Gender: ");
 		JLabel lblGPA = new JLabel("Current GPA: ");
 		JLabel lblEdu = new JLabel("Planned Education: ");
 
 		/** Text Fields **/
 		JTextField txtGroup = new JTextField(8);
 		JTextField txtTeacher = new JTextField(8);
 		JTextField txtFName = new JTextField(8);
 		JTextField txtLName = new JTextField(8);
 		JTextField txtGPA = new JTextField(6);
 
 		/** Combo Box Arrays **/
 		String arrPeriod[] = { "1", "2", "3", "4", "5", "6", "7", "8" };
 		String[] arrEdu = { "High School", "On-the-job training",
 				"Community College", "Technical School",
 				"College, Bachelor's Degree", "College + graduate school" };
 
 		/** Combo Boxes **/
 		JComboBox cboPeriod = new JComboBox(arrPeriod);
 		JComboBox cboEdu = new JComboBox(arrEdu);
 
 		/** Radio Buttons and Groups **/
 		ButtonGroup bgGender = new ButtonGroup();
 		JRadioButton btnGenderMale = new JRadioButton("Male");
 		JRadioButton btnGenderFemale = new JRadioButton("Female");
 
 		/*********************************
 		 * Configure Panel Properties
 		 *********************************/
 		this.setBackground(PANEL_BACKGROUND);
 
 		/*********************************
 		 * Configure Components
 		 *********************************/
 		/** Containers **/
 		container.setLayout(lytContainer);
 		genderContainer.setLayout(lytGenderContainer);
 		
 		/** Text Boxes **/
 		txtGroup.setText("RealityU Group");
 		txtTeacher.setText("Teacher");
 		txtFName.setText("First Name");
 		txtLName.setText("Last Name");
 		txtGPA.setText("GPA");
 
 		/** Radio Buttons **/
 		// Button Groups
 		bgGender.add(btnGenderMale);
 		bgGender.add(btnGenderFemale);
 
 		/*********************************
 		 * Assemble Containers and Panel
 		 *********************************/
 		/** Gender Container **/
 		genderContainer.add(lblGender);
 	    genderContainer.add(btnGenderMale);
 	    genderContainer.add(btnGenderFemale);
 	    
 		/** Main Container **/
 		// First Row
 		container.add(txtGroup);
 		container.add(cboPeriod);
 		container.add(txtTeacher);
 		
 		// Second Row
 		container.add(txtFName);
 		container.add(txtLName);
 		container.add(genderContainer);
 
 		// Third Row
 		container.add(txtGPA);
 		container.add(cboEdu);		
 
 		/** Assemble Panel **/
 		this.add(container);
 	}
 
 }

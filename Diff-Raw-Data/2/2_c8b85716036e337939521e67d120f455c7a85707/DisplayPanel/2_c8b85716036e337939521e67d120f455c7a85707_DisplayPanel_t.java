 package com.github.markyc.applicationcenter;
 
 import java.awt.BorderLayout;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.List;
 
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 
 public class DisplayPanel extends JPanel implements CardPanel {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -5548765433681976361L;
 	public static final String CARD_NAME = "displaypanel";
 	
 	//private static final String SELECT_STUDENT 		= "Please select a student to view their admission status";
 	private static final String SELECT_STUDENT_LIST = "Select a student by typing their name above";
 	//private static final String[] NO_STUDENTS 		= { "There are no students currently in the database" };
 	
 	private JTextField searchField;
 	private JTextArea area;
 	
 	List<Student> students;
 
 	
 	public DisplayPanel() {
 		this.setLayout(new BorderLayout());
 		
 		// the first field in this window is a combo box containing the names of all Students
 		this.searchField = new JTextField();
 		this.searchField.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				String name = ((JTextField) e.getSource()).getText(); 
 				
 				// attempt to show the student, showing an empty ("please select a student...") panel on error
 				for (Student s : DisplayPanel.this.students)
 					if ( s.getName().equals( name ) ) {
 						DisplayPanel.this.showStudent( s );
 						return;
 					}
 				
 				// If we get here there is no Student with that name
 				new ErrorDialog("No Student named " + name + " found");
 			}
 			
 		});
 		
 		// the other field in this window displays the toString() of the selected Student
 		this.area = new JTextArea( SELECT_STUDENT_LIST );
 		this.area.setEditable( true );
 		this.area.setFont(new Font( "Arial", Font.PLAIN, 14 )); // 14pt Arial
 		
 		this.add( this.searchField, BorderLayout.NORTH) ;
 		this.add( new JScrollPane( this.area ), BorderLayout.CENTER );
 	}
 
 	private void showStudent(Student student) {
 		this.area.setText( student.toString() );
 		
 	}
 
 	@Override
 	public String getCardName() {
 		return CARD_NAME;
 	}
 
 	public void setStudents(List<Student> students) {
 		
 		this.students = students;
 		
 		// Create text out of Students in the List
 		/*String[] names = new String[students.size() + 1];
 		names[0] = SELECT_STUDENT;
 		for (int i = 1; i < names.length; i++) names[i] = students.get( i - 1 ).getName();
 		
 		// Set the text of the field to the Students from the List
 		this.comboBox.setModel(new DefaultComboBoxModel<String>(names));*/
 		
 		// Call for redraw
 		//this.revalidate();
 		//this.repaint(); 
 	}
 
 }
 

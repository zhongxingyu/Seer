 package no.bluebit.gui.bigexample;
 
 import java.awt.BorderLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 import no.bluebit.pojo.Person;
 
 public class Client extends JFrame {
 
 	private JTextField[]  lstTextFields;
 	
 	private DefaultListModel<Person> lstModel;
 	private ArrayList<Person> lstPeople;
 	private JList lstBox;
 	
 	public Client() {
 		super("My Application");
 		
 		//Add JPanel objects to the JFrame
 		add(BorderLayout.NORTH, new Input());
 		add(BorderLayout.CENTER, new Output());
 		add(BorderLayout.SOUTH, new Menu());
 		
 		//JFrame setup
 		setSize(500,300);
 		setVisible(true);
 		setResizable(false);
 		setDefaultCloseOperation(EXIT_ON_CLOSE);
 		setLocationRelativeTo(null);
 	}
 	
 	private class Input extends JPanel {
 		
 		//String-array for text placed on infotext for input fields
 		private String[] lstLabelNames = 
 			{ "Firstname: " , "Lastname: " , "Age: " };
 		
 		public Input() {
 			setLayout(new GridLayout(1,6));
 			//Creates the array of inputfields
 			lstTextFields = new JTextField[lstLabelNames.length];
 			
 			//Adds the labels and inputfields to the JPanel
 			for (int i = 0; i < lstLabelNames.length; i++) {
 				//add labels
 				JLabel lbl = new JLabel(lstLabelNames[i]);
 				add(lbl);
 				
 				//add inputfields and add to array to access them
 				JTextField txtField = new JTextField();
 				lstTextFields[i] = txtField;
 				add(txtField);
 			}
 		}
 	}
 	
 	private class Output extends JPanel {
		//contains a list of people shown in the JPanel
		ArrayList<Person> lstPeople;
 		
 		public Output() {
 			setLayout(new GridLayout(1,1));
			//initiate the people list
			lstPeople = new ArrayList<Person>();
 			
 			//Sets the format for the list, default model
 			lstModel = new DefaultListModel<Person>();
 			//Initiate the list with a JList, model: default
 			lstBox = new JList<Person>(lstModel);
 			
 			//Add the list to the JPanel
 			add(lstBox);
 		}
 	}
 	
 	private class Menu extends JPanel {
 		//uses a list of names on the different buttons.
 		//Try to add or remove an item to see what happens
 		private String[] btnLabels = 
 			{"New", "Show", "Update", "Delete", "Exit"};
 		
 		public Menu() {
 			setLayout(new GridLayout(1, btnLabels.length));
 			//Initiate ActionListener class to listen to the buttons
 			EventHandler event = new EventHandler();
 			
 			JButton btn;
 			for (int i = 0; i < btnLabels.length; i++) {
 				//Initiate a new button / re-initiate the previous one
 				btn = new JButton(btnLabels[i]);
 				//Add actionlistener to the button
 				btn.addActionListener(event);
 				//Add button to panel
 				add(btn);
 			}
 		}
 	}
 	
 	private class EventHandler implements ActionListener {
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			//NOTE! swtich case based on a String is only available in Java 7
 			
 			//Get the text on the button - this for easy to read code
 			String btnLabel = e.getActionCommand();
 		
 			switch (btnLabel) {
 				case "New"		:	createClicked(); 	break;
 				case "Show"		:	readClicked(); 		break;
 				case "Update"	:	updateClicked(); 	break;
 				case "Delete"	:	deleteClicked();	break;
 				case "Exit"		:	exitClicked(); 		break;
 			}
 		}
 		
 		//Methods for each button
 		private void createClicked() {
 			//Get indata
 			String inputFirstname 	= lstTextFields[0].getText();
 			String inputLastname 	= lstTextFields[1].getText();
 			String inputAge 		= lstTextFields[2].getText();
 			
 			//Validates the indata
 			if(validateName(inputFirstname, inputLastname) && validateAge(inputAge)) {
 				//initiate new Person object based on validated input data
 				Person p = new Person(inputFirstname, inputLastname, Integer.parseInt(inputAge));
 				//Add the element to the list in JPanel
 				lstModel.addElement(p);
 				//Add person to the array - used in "show" method
 				lstPeople.add(p);
 				
 				//reset inputfields 
 				for (int i = 0; i < lstTextFields.length; i++) {
 					lstTextFields[i].setText("");
 				}
 			} else {
 				//Indata is not validated - show error screen
 				JOptionPane.showMessageDialog(null, "Input error!");
 			}
 		}
 		
 		private void readClicked() {
 			//Get selected index in the JPanel
 			int index = lstBox.getSelectedIndex();
 			//Get Person's data from the list ArrayList based on selected index
 			Person p = lstPeople.get(index);
 			
 			//Output String
 			String output = p.getFirstname() + " " + p.getLastname() + "\nAge: " + p.getAge();
 			//Show selected person
 			JOptionPane.showMessageDialog(null, output);
 		}
 		
 		private void updateClicked() {
 			int index = lstBox.getSelectedIndex();
 			Person p = lstPeople.get(index);
 			String firstname = "", lastname = "";
 			
 			//Update firstname
 			firstname = updateText("Update Firstname: ",p.getFirstname());
 			if(firstname == null) {
 				//Cancel clicked, break method
 				return;
 			}
 			lastname = updateText("Update Lastname: ", p.getLastname());
 			if(lastname == null) {
 				return;
 			}
 			
 			//Create a new Person based on the input values
 			Person newPerson = new Person(firstname, lastname, p.getAge());
 			
 			//Replace the person in both lists
 			lstPeople.remove(index);
 			lstPeople.add(index, newPerson);
 			lstModel.remove(index);
 			lstModel.add(index, newPerson);
 			
 		}
 		
 		private void deleteClicked() {
 			int index = lstBox.getSelectedIndex();
 			Person p = lstPeople.get(index);
 			String output = p.getFirstname() + " " + p.getLastname() + "\nAge: " + p.getAge() + "";
 			
 			int answer = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete:\n" + output);
 			if(answer == JOptionPane.YES_OPTION) {
 				//Removing selected person from both lists
 				lstPeople.remove(index);
 				lstModel.remove(index);
 			}
 		}
 		
 		private void exitClicked() {
 			//Exits the program
 			System.exit(0);
 		}
 		
 		//Helper methods
 		private boolean validateName(String first, String last) {
 			//If both strings are validated, return true
 			if(validateString(first) || validateString(last)) {
 				return true;
 			}
 			return false;
 		}
 		
 		private boolean validateString(String string) {
 			//If string is empty or null, return false
 			if(string.equals("") || string == null) {
 				return false;
 			}
 			return true;
 		}
 		
 		private boolean validateAge(String age) {
 			//Try to parse the age
 			try {
 				int a = Integer.parseInt(age);
 				//age is a number
 				
 				//... and the number is positive
				if(a < 0) {
 					//.. return true
 					return true;
 				}
 				
 			} catch (Exception e) {
 				
 				return false;
 			}
 			return false;
 		}
 		
 		//Method used to get text from the user in the edit section
 		public String updateText(String label, String text) {
 			String firstname = null; 
 			do {
 				firstname = JOptionPane.showInputDialog(null, label, text);
 				
 				if(firstname == null || !validateString(firstname)) {
 					firstname = null;
 				}
 			}
 			while(firstname == null);
 			//Loop runs as long as the input is not validated
 			//And then returns the value
 			return firstname;
 		}
 	}
 	
 	public static void main(String[] args) {
 		new Client();
 	}
 }

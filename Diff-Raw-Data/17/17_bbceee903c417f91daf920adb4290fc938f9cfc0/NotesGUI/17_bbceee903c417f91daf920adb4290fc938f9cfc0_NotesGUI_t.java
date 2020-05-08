 package gui;
 
 import java.awt.GridLayout;
 
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 public class NotesGUI extends JDialog {
 	
 	public NotesGUI() {
 		setTitle("Dectective Notes");
		setSize(600,600);
 		setLayout(new GridLayout(3, 2));
 		
 		//Room, person and weapon checkbox panels are in their own classes
 		//Along with combo box panels
 		PeoplePanel people = new PeoplePanel();
 		WeaponPanel weapons = new WeaponPanel();
 		RoomPanel rooms = new RoomPanel();
 		PguessPanel pGuess = new PguessPanel();
 		RguessPanel rGuess = new RguessPanel();
 		WguessPanel wGuess = new WguessPanel();
 		
 		add(people);
 		add(pGuess);
 		add(weapons);
 		add(wGuess);
 		add(rooms);
 		add(rGuess);
 		
 	}
 
 }

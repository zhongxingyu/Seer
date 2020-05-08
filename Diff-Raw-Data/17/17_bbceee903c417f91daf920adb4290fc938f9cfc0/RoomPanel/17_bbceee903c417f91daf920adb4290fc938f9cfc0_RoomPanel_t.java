 package gui;
 
import java.awt.FlowLayout;
 import java.awt.GridLayout;
 import javax.swing.JCheckBox;
 import javax.swing.JPanel;
 import javax.swing.border.EtchedBorder;
 import javax.swing.border.TitledBorder;
 
 public class RoomPanel extends JPanel {
 	
 	JCheckBox gallery;
 	JCheckBox billiardsRoom;
 	JCheckBox library;
	JCheckBox bathroom;
 	JCheckBox bedroom;
 	JCheckBox ballroom;
 	JCheckBox conservatory;
 	JCheckBox kitchen;
 	JCheckBox pantry;
 	
 	public RoomPanel() {
		setLayout(new FlowLayout());
 		gallery = new JCheckBox("Gallery");
 		billiardsRoom = new JCheckBox("Billiards Room");
 		library = new JCheckBox("Libarary");
		bathroom = new JCheckBox("Bathhroom");
 		bedroom = new JCheckBox("Bedroom");
 		ballroom = new JCheckBox("Ballroom");
 		conservatory = new JCheckBox("Conservatory");
 		kitchen = new JCheckBox("Kitchen");
 		pantry = new JCheckBox("Pantry");
 		
 		add(gallery);
 		add(billiardsRoom);
 		add(library);
		add(bathroom);
 		add(bedroom);
 		add(ballroom);
 		add(conservatory);
 		add(kitchen);
 		add(pantry);
 		
 		setBorder(new TitledBorder(new EtchedBorder(), "Rooms"));
 	}
 
 }

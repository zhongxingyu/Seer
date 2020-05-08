 package dialog;
 
 import java.awt.Component;
 import java.awt.GridLayout;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.ButtonGroup;
 import javax.swing.JComboBox;
 import javax.swing.JDialog;
 import javax.swing.JCheckBox;
 import javax.swing.JPanel;
 import javax.swing.border.Border;
 import javax.swing.border.EtchedBorder;
 import javax.swing.border.TitledBorder;
 
 import csci306.Board;
 import csci306.Card;
 import csci306.Player;
 
 public class NotesDialog extends JDialog {
 	private static final long serialVersionUID = 3370549787533395603L;
 	Board board;
 	
 	public NotesDialog(Board board){
 		setTitle("Detective Notes");
		setSize(600,800);
 		setLayout(new GridLayout(0,2));
 		this.board = board;
 		add(createPeopleCheckbox());
 		add(createPeopleCombo());
 		add(createRoomCheckbox());
 		add(createRoomCombo());
 		add(createWeaponCheckBox());
 		add(createWeaponCombo());
 		setVisible(true);
 		
 	}
 	
 	private JPanel createWeaponCombo(){
 		JPanel panel = new JPanel();
 		panel.setBorder(new TitledBorder(new EtchedBorder(), "Weapon Choice"));
 		JComboBox box = new JComboBox();
 		for(Card c: board.allCards){
 			if(c.getType().equalsIgnoreCase("WEAPON")){
 				box.addItem(c.getName());
 			}
 		}
 		panel.add(box);
 		return panel;
 	}
 	
 	private JPanel createWeaponCheckBox(){
 		JPanel panel = new JPanel();
 		panel.setBorder(new TitledBorder(new EtchedBorder(), "Weapons"));
 		for(Card c: board.allCards){
 			if(c.getType().equalsIgnoreCase("WEAPON")){
 				JCheckBox box = new JCheckBox(c.getName());
 				panel.add(box);
 			}
 		}
 		return panel;
 	}
 	
 	private JPanel createRoomCombo() {
 		JPanel panel = new JPanel();
 		panel.setBorder(new TitledBorder(new EtchedBorder(), "Room Guess"));
 		JComboBox box = new JComboBox();
 		for(String s: board.rooms.values()){
 			box.addItem(s);
 		}
 		panel.add(box);
 		return panel;
 	}
 
 	private JPanel createRoomCheckbox() {
 		JPanel panel = new JPanel();
 		panel.setBorder(new TitledBorder (new EtchedBorder(), "Rooms"));
 		for(String room: board.rooms.values()){
 			JCheckBox box = new JCheckBox(room);
 			panel.add(box);
 		}
 		return panel;
 	}
 
 	private JPanel createPeopleCombo(){
 		JPanel panel = new JPanel();
 		panel.setBorder(new TitledBorder (new EtchedBorder(), "Person Guess"));
 		JComboBox b = new JComboBox();
 		for(Player p: board.players){
 			b.addItem(p.getName());
 		}
 		panel.add(b);
 		return panel;
 	}
 	
 	private JPanel createPeopleCheckbox(){
 		JPanel boxes = new JPanel();
 		boxes.setBorder(new TitledBorder (new EtchedBorder(), "People"));
 		for(Player p: board.players){
 			JCheckBox box = new JCheckBox(p.getName());
 			boxes.add(box);
 		}
 		return boxes;
 	} 
 
 }

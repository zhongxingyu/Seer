 package gui;
 
 import javax.swing.JFrame;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.ListCellRenderer;
 
 import java.awt.Component;
 import java.awt.GridBagLayout;
 import javax.swing.JLabel;
 import java.awt.GridBagConstraints;
 import javax.swing.JComboBox;
 
 import db.MeetingPoint;
 import db.User;
 
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import javax.swing.JButton;
 
 import core.CalendarProgram;
 import javax.swing.GroupLayout;
 import javax.swing.GroupLayout.Alignment;
 import java.awt.Color;
 import javax.swing.LayoutStyle.ComponentPlacement;
 
 public class MeetingPanel extends JPanel implements ActionListener{
 
 	protected JComboBox<MeetingPoint> comboBox;
 	protected ParticipantListPanel plp;
 
 	protected ArrayList<User> userList;
	protected ArrayList<MeetingPoint> allPlaces = new ArrayList<MeetingPoint>();
 
 	protected CalendarProgram cp;
 	
 	public MeetingPanel() {
 		setBackground(Color.PINK);
 		
 		JLabel lblNewLabel = new JLabel("Participants");
 		
 		plp = new ParticipantListPanel(cp);
 		
 		JLabel lblPlace = new JLabel("Place");
 		
 		comboBox = new JComboBox<MeetingPoint>();
 		
 		JButton findButton = new JButton("Find Place");
 		findButton.addActionListener(this);
 		findButton.setActionCommand("Cancel");
 		GroupLayout groupLayout = new GroupLayout(this);
 		groupLayout.setHorizontalGroup(
 			groupLayout.createParallelGroup(Alignment.LEADING)
 				.addGroup(groupLayout.createSequentialGroup()
 					.addComponent(lblPlace, GroupLayout.PREFERRED_SIZE, 48, GroupLayout.PREFERRED_SIZE)
 					.addGap(27)
 					.addComponent(comboBox, GroupLayout.PREFERRED_SIZE, 163, GroupLayout.PREFERRED_SIZE)
 					.addPreferredGap(ComponentPlacement.RELATED, 124, Short.MAX_VALUE)
 					.addComponent(findButton))
 				.addComponent(lblNewLabel, GroupLayout.PREFERRED_SIZE, 94, GroupLayout.PREFERRED_SIZE)
 				.addComponent(plp, GroupLayout.DEFAULT_SIZE, 469, Short.MAX_VALUE)
 		);
 		groupLayout.setVerticalGroup(
 			groupLayout.createParallelGroup(Alignment.LEADING)
 				.addGroup(groupLayout.createSequentialGroup()
 					.addComponent(lblNewLabel)
 					.addPreferredGap(ComponentPlacement.UNRELATED)
 					.addComponent(plp, GroupLayout.PREFERRED_SIZE, 140, GroupLayout.PREFERRED_SIZE)
 					.addGap(19)
 					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
 						.addComponent(comboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 						.addComponent(lblPlace)
 						.addComponent(findButton)))
 		);
 		setLayout(groupLayout);
 		
 		/*userList = cp.getUsers();
 		System.out.println(userList);
 		plp.makeCheckListItem(userList.get(1)); */
 		
 		
 	}
 	
 	public ArrayList<User> getParticipants(){
 		return this.plp.getParticipantList();
 	}
 	
 	public ArrayList<MeetingPoint> filterPlaces(ArrayList<User> participants, ArrayList<MeetingPoint> allPlaces){
 		ArrayList<MeetingPoint> places = new ArrayList<MeetingPoint>();
 		for (int i = 0; i < allPlaces.size(); i++){
 			if(allPlaces.get(i).getCapacity() >= participants.size()-1){
 				places.add(allPlaces.get(i));
 			}
 		}
 		return places;
 	}
 	
 	public MeetingPoint getMeetingPoint(){
 		return (MeetingPoint) comboBox.getSelectedItem();
 	}
 	
 	public static void main(String[] args){
 
 		JFrame frame = new JFrame();
 		MeetingPanel mp = new MeetingPanel();
 		frame.getContentPane().add(mp);
 		frame.pack();
 		frame.setSize(300, 100);
 		frame.setVisible(true);
 	}
 
 
 	
 	public void actionPerformed(ActionEvent e) {
 		comboBox.removeAllItems();
 		ArrayList<MeetingPoint> filter = filterPlaces(plp.getParticipantList(), allPlaces);
 		for(int i = 0; i < filter.size(); i++){
 			comboBox.addItem(filter.get(i));
 		}
 	}
 
 }

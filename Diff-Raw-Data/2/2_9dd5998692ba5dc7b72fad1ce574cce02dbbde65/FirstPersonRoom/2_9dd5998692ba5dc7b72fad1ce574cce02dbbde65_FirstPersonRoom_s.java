 package view;
 
 import java.awt.Color;
 import java.awt.event.MouseListener;
 
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.SwingConstants;
 
 /**
  * A 3D representation of a room
  * @author seanbyron
  *
  */
 
 public class FirstPersonRoom extends MapRoom {
 	
 	private MouseListener listener;
 	
 	public FirstPersonRoom(String description) {
 		super(description);
 		listener = null;
 	}
 	
 	public FirstPersonRoom(String description, MouseListener listener) {
 		super(description);
 		this.listener = listener;
 	}
 	
 	/**
 	 * Returns a JPanel depicting the given looking direction 
 	 * @param direction
 	 * @return
 	 */
 	public JPanel getView(String direction) {
 		
 		JPanel panel = new JPanel();
 		
 		panel.setLayout(null);
 		
 		if (getWall(direction).getMonster() != null && getWall(direction).getMonster().isAlive())
 		{
 			//Add a monster image
 			JLabel monster = new JLabel("");
 			monster.setToolTipText("attack," + getWall(direction).getMonster().getName());
 			monster.setIcon(new ImageIcon(FirstPersonRoom.class.getResource(((FirstPersonMonster)getWall(direction).getMonster()).getImage())));
 			monster.setBounds(219, 253, 150, 250);
 			monster.addMouseListener(listener); //Add the mouse listener to the door, so when it is clicked the listener fires
 			panel.add(monster);
 		}
 		if(getWall(direction).getItem() != null)
 		{
 			//Add a item image
 			JLabel item = new JLabel("");
 			item.setToolTipText("pick," + getWall(direction).getItem().getItemName());
 			item.setIcon(new ImageIcon(FirstPersonRoom.class.getResource(((FirstPersonItem)getWall(direction).getItem()).getImage())));
			item.setBounds(405, 253, 180, 250);
 			item.addMouseListener(listener); //Add the mouse listener to the door, so when it is clicked the listener fires
 			panel.add(item);
 		}
 		
 		//If there is an exit on this side of the room, show a door
 		if (getExit(direction) != null) {
 			//Create a label which will show the name of the room the door leads to
 			JLabel roomLabel = new JLabel(getExit(direction).getDescription());
 			roomLabel.setHorizontalAlignment(SwingConstants.CENTER);
 			roomLabel.setForeground(Color.WHITE);
 			roomLabel.setBounds(238, 280, 141, 16);
 			panel.add(roomLabel);
 			
 			//Add a door image
 			JLabel door = new JLabel("");
 			door.setToolTipText("go," + direction);
 			door.setIcon(new ImageIcon(FirstPersonRoom.class.getResource("/img/firstperson/door.png")));
 			door.setBounds(219, 253, 180, 250);
 			door.addMouseListener(listener); //Add the mouse listener to the door, so when it is clicked the listener fires
 			panel.add(door);
 		}
 		
 		//Add 
 		JLabel room = new JLabel("");
 		room.setIcon(new ImageIcon(FirstPersonRoom.class.getResource("/img/firstperson/room.png")));
 		room.setBounds(0, 0, 600, 600);
 		panel.add(room);
 		
 		return panel;
 	}
 
 }

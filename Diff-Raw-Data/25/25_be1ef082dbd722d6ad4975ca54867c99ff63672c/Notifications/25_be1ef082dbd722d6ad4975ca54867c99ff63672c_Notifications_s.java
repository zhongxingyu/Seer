 package gui;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 
 import javax.swing.*;
 
 import Models.Event;
 import Models.User;
 
 import connection.Client;
 
 @SuppressWarnings("serial")
 public class Notifications extends JPanel {
 	
 	private Client client;
 	
	//testfields
	private User user = new User(14, "What's updog");
	private long a = 2142145111;
	private Timestamp timestamp1 = new Timestamp(a);
	private Event eve= new Event(1337, user, "supsup", timestamp1, timestamp1, "Shaky", "bacon");
	
	
 	public void newNotification(Event event){
 		
 		JPanel newNotificationPanel = new JPanel();
 		JLabel newHeader = new JLabel();
 		JSeparator newSeperator = new JSeparator();
 		JLabel newTimeLabel = new JLabel();
 		JLabel newTime = new JLabel();
 		JLabel newDateLabel = new JLabel();
 		JLabel newDate = new JLabel();
 		JLabel newRoomLabel	= new JLabel();
 		JLabel newRoom = new JLabel();
 		JLabel newDescriptionLabel = new JLabel();
 		JTextArea newDescription = new JTextArea();
 		JScrollPane newScrollPane = new JScrollPane();
 		
 		JButton newAcceptButton = new JButton();
 		newAcceptButton.addActionListener(new AcceptAction(event));
 		
 		JButton newDenyButton = new JButton();
 		newDenyButton.addActionListener(new DenyAction(event));
 		
 		newHeader.setFont(new java.awt.Font("Trebuchet MS", 0, 18));
 		newHeader.setText(event.getTitle());
 		
 		newTimeLabel.setFont(new java.awt.Font("Trebuchet MS", 0, 13));
 		newTimeLabel.setText("Klokkeslett");
 		
 		newDateLabel.setFont(new java.awt.Font("Trebuchet MS", 0, 13));
 		newDateLabel.setText("Dato");
 
         newTime.setFont(new java.awt.Font("Trebuchet MS", 0, 13));
         newTime.setText(event.getStartTime().toString() + " - " + event.getEndTime().toString());
 
         newDate.setFont(new java.awt.Font("Trebuchet MS", 0, 13));
         newDate.setText(event.getStartTime().toString() + " - " + event.getEndTime().toString());
 
         newRoomLabel.setFont(new java.awt.Font("Trebuchet MS", 0, 13));
         newRoomLabel.setText("Møterom");
 
         newRoom.setFont(new java.awt.Font("Trebuchet MS", 0, 13));
         newRoom.setText(event.getLocation());
 
         newDescriptionLabel.setFont(new java.awt.Font("Trebuchet MS", 0, 13));
         newDescriptionLabel.setText("Beskrivelse");
         
         newDescription.setColumns(20);
         newDescription.setEditable(false);
         newDescription.setRows(5);
         newDescription.setText(event.getAgenda());
         
         newScrollPane.setViewportView(newDescription);
 
         newAcceptButton.setText("Godta");
 
         newDenyButton.setText("Forkast");
         
         GroupLayout newLayout = new GroupLayout(newNotificationPanel);
         newNotificationPanel.setLayout(newLayout);
         newLayout.setHorizontalGroup(
             newLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
             .addGroup(newLayout.createSequentialGroup()
                 .addGroup(newLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                     .addComponent(newHeader)
                     .addComponent(newSeperator, GroupLayout.DEFAULT_SIZE, 322, Short.MAX_VALUE)
                     .addGroup(newLayout.createSequentialGroup()
                         .addGroup(newLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                             .addComponent(newTimeLabel)
                             .addComponent(newDateLabel)
                             .addComponent(newRoomLabel))
                         .addGap(18, 18, 18)
                         .addGroup(newLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                             .addComponent(newRoom)
                             .addComponent(newDate)
                             .addComponent(newTime)))
                     .addGroup(newLayout.createSequentialGroup()
                         .addComponent(newDescriptionLabel)
                         .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                         .addGroup(newLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                             .addGroup(newLayout.createSequentialGroup()
                                 .addComponent(newAcceptButton)
                                 .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                 .addComponent(newDenyButton))
                             .addComponent(newScrollPane, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))))
                 .addContainerGap())
                 );
         
         newLayout.setVerticalGroup(
                 newLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                 .addGroup(newLayout.createSequentialGroup()
                     .addComponent(newHeader)
                     .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                     .addComponent(newSeperator, GroupLayout.PREFERRED_SIZE, 8, GroupLayout.PREFERRED_SIZE)
                     .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                     .addGroup(newLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                         .addComponent(newTimeLabel)
                         .addComponent(newTime))
                     .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                     .addGroup(newLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                         .addComponent(newDateLabel)
                         .addComponent(newDate))
                     .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                     .addGroup(newLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                         .addComponent(newRoomLabel)
                         .addComponent(newRoom))
                     .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                     .addGroup(newLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                         .addComponent(newDescriptionLabel)
                         .addComponent(newScrollPane, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                     .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                     .addGroup(newLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                         .addComponent(newAcceptButton)
                         .addComponent(newDenyButton))
                     .addContainerGap(39, Short.MAX_VALUE))
             );
         
 	        GroupLayout notificationsPanelLayout = new GroupLayout(this);
 	        this.setLayout(notificationsPanelLayout);
 	        notificationsPanelLayout.setHorizontalGroup(
 	            notificationsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
 	            .addGroup(notificationsPanelLayout.createSequentialGroup()
 	                .addContainerGap()
 	                .addComponent(newNotificationPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 	                .addContainerGap())
 	        );
 	        notificationsPanelLayout.setVerticalGroup(
 	            notificationsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
 	            .addGroup(notificationsPanelLayout.createSequentialGroup()
 	                .addContainerGap()
 	                .addComponent(newNotificationPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 	                .addContainerGap(438, Short.MAX_VALUE))
 	        );
 	}
 	
 	
 	
 	public Notifications() {
		newNotification(eve);
 	}
 	
 	class AcceptAction implements ActionListener{
 		Event event;
 		public AcceptAction(Event tempEvent) {
 			this.event = tempEvent;
 		}
 		public void actionPerformed(ActionEvent e) {
 			client.meetingAcceptAction(event);
 		}
 	}
 	
 	class DenyAction implements ActionListener{
 		Event event;
 		public DenyAction(Event tempEvent){
 			this.event = tempEvent;
 		}
 		public void actionPerformed(ActionEvent e) {
 			client.meetingDeclineAction(event);
 		}
 		
 	}
 	
 	public void addListener(Client client){
 		this.client = client;
 	}
 }

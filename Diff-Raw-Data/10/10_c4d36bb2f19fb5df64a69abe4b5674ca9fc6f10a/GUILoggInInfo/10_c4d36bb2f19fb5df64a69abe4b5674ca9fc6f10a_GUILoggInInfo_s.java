 package gui;
 
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.DefaultListModel;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 
 import synclogic.SyncListener;
 
 import model.Notification;
 import model.SaveableClass;
 import model.User;
 
 public class GUILoggInInfo extends JPanel{
 	private JLabel nameLabel;
 	private JLabel ansattNrLabel;
 	private JComboBox notificationBox;
 	private String messageFromAppointment;
 	private String [] notificationStrings;
 	private List<Notification> notifications;
 	private GridBagConstraints c = new GridBagConstraints();
 	
 	public GUILoggInInfo(){
 		
 		setLayout(new GridBagLayout());
 		this.notifications = new ArrayList<Notification>();
 		nameLabel = new JLabel();
 		nameLabel.setText(getName());
 		c.gridx=1;
 		c.gridy=1;
 		add(nameLabel,c);
 		
 //		ansattNrLabel = new JLabel();
 //		ansattNrLabel.setText(getAnsattNr());
 //		c.gridx=1;
 //		c.gridy=2;
 //		add(ansattNrLabel,c);
 		
 //		notifications = new String[20];
 //		notifications[0]=("Notifications");
 //		for (int i = 1; i < notifications.length-1; i++) {
 //			
 //			try {
 //				notifications[i]=(sendNotification(i));
 //			} catch (Exception e) {
 //				// TODO Auto-generated catch block
 //			}
 //		}
 		
 		notificationBox = new JComboBox();
 		c.gridx=1;
 		c.gridy=4;
 		add(notificationBox,c);
 		notificationBox.addActionListener (new ActionListener () {
 		    public void actionPerformed(ActionEvent e) {
 		        getSelectedNotification();
 		        
 		    }
 		});
 		getSelectedNotification();
 		
 	}
 	
 	public void addNotification(Notification not) {
 		this.notifications.add(not);
 	}
 	
 	public void loadNotifications() {
 		for (SyncListener listener : XCal.getCSU().getObjectsFromID(SaveableClass.Notification, null)) {
 			if(!this.notifications.contains(listener)) {
 				this.notifications.add((Notification) listener);
 			}
 		}
 		DefaultListModel model = ((DefaultListModel)this.notificationBox.getModel());
 		for (String text : this.getNotificationStrings()) {
 			model.set(model.getSize() + 1, text);
 		}
 	}
 	
 	public String[] getNotificationStrings() {
 		List<String> nots = new ArrayList<String>();
 		for (Notification no : this.notifications) {
 			if(no.isRead()) {
 				continue;
 			}
 			switch(no.getType()) {
 			case MEETING_CANCELLED:
 				nots.add("Meeting at " + no.getInvitation().getMeeting().getStartTime() + " has been cancelled!");
 				break;
 			case INVITATION_RECEIVED:
 				nots.add("Meeting: " + no.getInvitation().getMeeting().getStartTime());
 				break;
 			case INVITATION_ACCEPTED:
 				nots.add("Invitation was accepted by " + no.getTriggeredBy().getUsername());
 				break;
 			case INVITATION_REJECTED:
 				nots.add("Invitation was rejected by " + no.getTriggeredBy().getUsername());
 				break;
 			case INVITATION_REVOKED:
 				nots.add("Your invitation for " + no.getInvitation().getMeeting().getStartTime() + " has been revoked.");
 				break;
 			case MEETING_TIME_CHANGED:
 				nots.add("Meeting blablabla's time has changed");
 				break;
 			case MEETING_CHANGE_REJECTED:
 				nots.add("Your meeting change was rejected by " + no.getTriggeredBy().getUsername());
 				break;
 			}
 //			switch(no.getInvitation().getStatus()) {
 //			case NOT_ANSWERED:
 //				nots.add("");
 //				break;
 //			case ACCEPTED:
 //				
 //				break;
 //			case REJECTED:
 //				
 //				break;
 //			case REVOKED:
 //				
 //				break;
 //			case NOT_ANSWERED_TIME_CHANGED:
 //				
 //				break;
 //			}
 		}
 		return (String[]) nots.toArray();
 	}
 	
 //	public void setNotificationText(){
 //		AppointmentGui aGui = new AppointmentGui();
 //		messageFromAppointment=aGui.descriptionField.getText();
 //		System.out.println(messageFromAppointment);
 //	}
 	public String sendNotification(int a){
 		
 		String message []={"Notifications","Meeting deleted","Invitation","Meeting declined","Meeting accepted"};
 		
 		return message[a];
 	}
 	public void getSelectedNotification(){
		Object value = notificationBox.getSelectedItem();
		if(value.equals("Meeting deleted")){
			JOptionPane.showMessageDialog(null,"Meeting"+""+" was deleted");
		}
 		//Her skal det skje noe mer naar man velger en notification
 	}
 //	private String getAnsattNr() {
 //		// TODO Auto-generated method stub
 //		String nr=" 00001";
 //		String text="Employee Nr: ";
 //		
 //		return text+nr;
 //	}
 	
 	public String getName(){
 		//hent opp info fra Logg inn siden
 		String info2 = XCal.usernameField.getText();
 		for (User o : XCal.getCSU().getAllUsers()) {
 			if (o.getUsername().equalsIgnoreCase(XCal.usernameField.getText())){
 				return o.getFirstname() + " " + o.getSurname();
 			}
 		}
 		
 		return info2;
 	}
 }
